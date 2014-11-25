/**
 * Copyright (C) 2014 Matheus Borges Teixeira
 * 
 * This is a part of Arkanos Vocabulary Builder (AVB)
 * AVB is an Android application to improve vocabulary on foreign languages.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.arkanos.avb.data;

import java.util.LinkedList;
import java.util.List;

import org.arkanos.avb.AVBApp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

/**
 * Singleton holding all language data operations.
 * 
 * @version 1.0
 * @author Matheus Borges Teixeira
 */
public class BabelTower {
	/** Tag for debug outputs **/
	public static final String TAG = AVBApp.TAG + "BabelTower";

	/** Writing link to the database **/
	private static SQLiteDatabase db_write = null;
	/** Reading link to the database **/
	private static SQLiteDatabase db_read = null;

	/**
	 * Open the connections to the database.
	 * 
	 * @param where defines the context for the database.
	 */
	public static synchronized void initialize(Context where) {
		Log.i(TAG, "Initializing.");
		DatabaseHelper dbh = new DatabaseHelper(where);
		db_read = dbh.getReadableDatabase();
		db_write = dbh.getWritableDatabase();
	}

	/**
	 * Upgrades database structure from a particular previous version.
	 * 
	 * @param version specifies the current version of the database.
	 * @param sql_db defines a connection to the database.
	 */
	public static synchronized void upgradeFrom(int version, SQLiteDatabase sql_db) {
		if (version < 17) {
			sql_db.execSQL(Translation.purgetSQLTable());
			sql_db.execSQL(Translation.createSQLTable());
		}
	}

	/**
	 * Prepares the infrastructure for a new language.
	 * 
	 * @param language specifies the language to prepare.
	 */
	public static synchronized void prepare(String language) {
		db_write.execSQL(Translation.createSQLTable(language));
	}

	/**
	 * Wipes out the data for a language.
	 * 
	 * @param language specifies which language to wipe.
	 */
	public static synchronized void clean(String language) {
		db_write.execSQL("DROP TABLE IF EXISTS " + Translation.TABLE + "_" + language + ";");
		db_write.execSQL("DELETE FROM " + Translation.TABLE_TEXT + " WHERE " + Translation.LANGUAGE + " MATCH '" + language + "';");
	}

	/**
	 * Creates or updates all necessary indexes to support queries.
	 * 
	 * @param language specifies the language to optimize.
	 */
	public static synchronized void optimize(String language) {
		db_write.execSQL("INSERT INTO " + Translation.TABLE_TEXT + "(" + Translation.TABLE_TEXT + ") VALUES('optimize');");
		db_write.execSQL("CREATE INDEX IF NOT EXISTS translation_index_confidence_trust_" + language + " ON "
				+ Translation.TABLE + "_" + language + " ("
				+ Translation.CONFIDENCE + ","
				+ Translation.TRUST + ");");
	}

	/**
	 * Inserts a single translation into the database.
	 * 
	 * @param data defines the contents of the row.
	 * @param language specifies the language where the translation goes.
	 */
	public static synchronized void addTranslation(ContentValues data, String language) {
		try {
			db_write.insert(Translation.TABLE + "_" + language, null, data);
		} catch (SQLiteException e) {
			Log.e(TAG, e.toString());
		}
	}

	/**
	 * Inserts search able translations into the database.
	 * 
	 * @param key specifies the sense which the translations belong to.
	 * @param synonyms defines all translations for that sense.
	 * @param language specifies the language the translations belong to.
	 */
	public static synchronized void addTranslation(String key, String synonyms, String language) {
		ContentValues data = new ContentValues();
		data.put(Translation.SYNONYMS, synonyms);
		data.put(Translation.SENSE_KEY, key);
		data.put(Translation.LANGUAGE, language);
		try {
			db_write.insert(Translation.TABLE_TEXT, null, data);
		} catch (SQLiteException e) {
			Log.e(TAG, e.toString());
		}
	}

	/**
	 * Searches for senses which translations match to the query string.
	 * 
	 * @param query specifies the search query.
	 * @return a list with all senses which translations match to the query.
	 */
	public static synchronized List<Sense> searchTranslations(String query) {
		List<Sense> results = new LinkedList<Sense>();
		try {
			Cursor c = db_read.rawQuery("SELECT * FROM " + Translation.TABLE_TEXT
					+ " WHERE " + Translation.SYNONYMS
					+ " MATCH ? ;", new String[] { query });
			if (c.moveToFirst()) {
				do {
					String key = c.getString(c.getColumnIndex(Translation.SENSE_KEY));
					Sense newone = Dictionary.getSense(key);
					String list = c.getString(c.getColumnIndex(Translation.SYNONYMS));
					String language = c.getString(c.getColumnIndex(Translation.LANGUAGE));
					Translation t = new Translation(key, language);
					t.setSynonyms(list);
					t.cleanSynonyms(query);
					newone.addTranslation(t);
					newone.setSortPower(query);
					results.add(newone);
				} while (c.moveToNext());
			}
			c.close();
		} catch (SQLiteException e) {
			Log.e(TAG, e.toString());
		}
		return results;
	}

	/**
	 * Selects a partition from a language based on size.
	 * The partition is based on the confidence from the user.
	 * Results will include items from top, middle and bottom.
	 * The partition will try to get equal amounts on each.
	 * Results might contain less items than specified.
	 * 
	 * @param size specifies the total amount of desired items.
	 * @param language defines the language to fetch from.
	 * @return a list with fetched translations.
	 */
	public static synchronized List<Translation> getPartition(int size, String language) {
		float c_max = 0, c_avg = 0, c_min = 0;
		List<Translation> list = new LinkedList<Translation>();
		List<Translation> average = new LinkedList<Translation>();
		List<Translation> best = new LinkedList<Translation>();
		List<Translation> worst = new LinkedList<Translation>();
		Cursor c = db_read.rawQuery("SELECT"
				+ " MAX(" + Translation.CONFIDENCE + "),"
				+ " AVG(" + Translation.CONFIDENCE + "),"
				+ " MIN(" + Translation.CONFIDENCE + ")"
				+ " FROM " + Translation.TABLE + "_" + language + ";", null);
		if (c.moveToFirst()) {
			c_max = c.getFloat(0);
			c_avg = c.getFloat(1);
			c_min = c.getFloat(2);
		}
		c.close();

		average = selectPartition(size, language, (c_avg - c_min) * 0.2f + c_min, (c_max - c_avg) * 0.80f + c_avg);
		best = selectPartition(size, language, (c_max - c_avg) * 0.80f + c_avg, c_max);
		worst = selectPartition(size, language, c_min, (c_avg - c_min) * 0.2f + c_min);

		int count = 0;
		if (average.size() <= size / 3) {
			while (average.size() > 0) {
				list.add(average.remove(0));
				count++;
			}
		}

		if (best.size() <= size / 3) {
			while (best.size() > 0) {
				list.add(best.remove(0));
				count++;
			}
		}

		if (worst.size() <= size / 3) {
			while (worst.size() > 0) {
				list.add(worst.remove(0));
				count++;
			}
		}
		while (count < size && (best.size() > 0 || worst.size() > 0 || average.size() > 0)) {
			if (average.size() > 0) {
				list.add(average.remove(0));
				count++;
			}
			if (best.size() > 0) {
				list.add(best.remove(0));
				count++;
			}
			if (worst.size() > 0) {
				list.add(worst.remove(0));
				count++;
			}
		}

		return list;
	}

	/**
	 * Loads a continuous block of translations from the database.
	 * 
	 * @param count specifies the length of the list.
	 * @param language defines the language where to get data from.
	 * @param c_min specifies the bottom limit for the confidence.
	 * @param c_max specifies the top limit for the confidence.
	 * @return a list which satisfies the selection criteria.
	 */
	private static synchronized List<Translation> selectPartition(int count, String language, float c_min, float c_max) {
		List<Translation> result = new LinkedList<Translation>();
		try {
			String sql = "SELECT * FROM " + Translation.TABLE + "_" + language
					+ " WHERE " + Translation.CONFIDENCE + " <= " + c_max
					+ " AND " + Translation.CONFIDENCE + " >= " + c_min
					+ " LIMIT " + count + ";";
			// Log.d(TAG, "SQL: " + sql);
			// Log.d(TAG, "Trying to fetch " + count + ".");
			Cursor c = db_read.rawQuery(sql, null);

			if (c.moveToFirst()) {
				// Log.d(TAG, "Fetching done.");
				do {
					// result[read] = BabelTower.getTranslation(c.getString(c.getColumnIndex(Translation.Fields.SENSE_KEY.toString())), language);
					// result[read++].setTerm(c.getString(c.getColumnIndex(Translation.Fields.TERM.toString())));
					result.add(new Translation(c, language));
				} while (c.moveToNext());
			}
			c.close();
			return result;
		} catch (SQLiteException e) {
			Log.e(TAG, e.toString());
		}
		return null;
	}

	/**
	 * Fetches all translations for a given sense.
	 * 
	 * @param key defines the sense key.
	 * @param language specifies the language for the translations.
	 * @return a list with all individual translations.
	 */
	public static synchronized List<Translation> getTranslations(String key, String language) {
		List<Translation> results = new LinkedList<Translation>();

		try {
			Cursor c = db_read.rawQuery("SELECT * FROM " + Translation.TABLE + "_" + language
					+ " WHERE " + Translation.SENSE_KEY + " = ? ;", new String[] { key });
			if (c.moveToFirst()) {
				do {
					results.add(new Translation(c, language));
				} while (c.moveToNext());
			}

			c.close();
		} catch (SQLiteException e) {
			Log.e(TAG, e.toString());
		}
		return results;
	}

	/**
	 * Removes a translation from the database.
	 * 
	 * @param t specifies the translation to be removed.
	 */
	public static synchronized void deleteTranslation(Translation t) {
		String sql_table = "DELETE FROM " + Translation.TABLE + "_" + t.getLanguage()
				+ " WHERE " + Translation.SENSE_KEY + " = '" + t.getKey() + "'"
				+ " AND " + Translation.TERM + " = '" + t.getTerm() + "';";
		String clean;
		if (t.getPrettySynonyms() == null) {
			clean = getTranslationSynonyms(t.getKey(), t.getLanguage()).trim();
		}
		else {
			clean = t.getPrettySynonyms();
		}
		clean = clean.replace(t.getTerm(), "").replace("  ", " ");
		t.setSynonyms(clean);
		String sql_text;
		if (t.getPrettySynonyms().length() > 0) {
			sql_text = "UPDATE " + Translation.TABLE_TEXT
					+ " SET " + Translation.SYNONYMS + " = '" + t.getPrettySynonyms() + "'"
					+ " WHERE " + Translation.TABLE_TEXT + " MATCH '"
					+ Translation.SENSE_KEY + ":" + t.getKey() + " "
					+ Translation.LANGUAGE + ":" + t.getLanguage() + "';";
		}
		else {
			sql_text = "DELETE FROM " + Translation.TABLE_TEXT
					+ " WHERE " + Translation.TABLE_TEXT + " MATCH '"
					+ Translation.SENSE_KEY + ":" + t.getKey() + " "
					+ Translation.LANGUAGE + ":" + t.getLanguage() + "';";
		}

		try {
			db_write.execSQL(sql_table);
			db_write.execSQL(sql_text);
		} catch (SQLiteException e) {
			Log.e(TAG, e.toString());
		}
	}

	/**
	 * Fetches the translations for a given sense.
	 * 
	 * @param key specifies the sense key.
	 * @param language defines the language for the translations.
	 * @return the translations as a single string.
	 */
	public static synchronized String getTranslationSynonyms(String key, String language) {
		String result = null;
		try {
			Cursor c = db_read.rawQuery("SELECT * FROM " + Translation.TABLE_TEXT
					+ " WHERE " + Translation.TABLE_TEXT + " MATCH '"
					+ Translation.SENSE_KEY + ":" + key + " "
					+ Translation.LANGUAGE + ":" + language + "';", null);
			if (c.moveToFirst()) {
				result = c.getString(c.getColumnIndex(Translation.SYNONYMS.toString()));

				if (c.moveToNext()) {
					Log.e(TAG, "Danger Will Robinson! Danger! Duplicate language and key.");
				}
			}

			c.close();
		} catch (SQLiteException e) {
			Log.e(TAG, e.toString());
		}
		return result;
	}

	/**
	 * Inserts a translation into the database.
	 * 
	 * @param t defines the translation to be added.
	 */
	public static synchronized void addTranslation(Translation t) {
		ContentValues map = new ContentValues();
		map.put(Translation.SENSE_KEY, t.getKey());
		map.put(Translation.TERM, t.getTerm());
		map.put(Translation.TRUST, t.getTrust());
		if (t.getGrammar() != null) {
			map.put(Translation.GRAMMAR, t.getGrammar());
		}
		addTranslation(map, t.getLanguage());

		String synonyms = getTranslationSynonyms(t.getKey(), t.getLanguage());
		if (synonyms != null) {
			synonyms += " " + t.getTerm();
			synonyms.replace("  ", " ");
			String sql_text = "UPDATE " + Translation.TABLE_TEXT
					+ " SET " + Translation.SYNONYMS + " = ?"
					+ " WHERE " + Translation.TABLE_TEXT + " MATCH '"
					+ Translation.SENSE_KEY + ":" + t.getKey() + " "
					+ Translation.LANGUAGE + ":" + t.getLanguage() + "';";
			try {
				db_write.execSQL(sql_text, new String[] { synonyms });
			} catch (SQLiteException e) {
				Log.e(TAG, e.toString());
			}
		}
		else {
			synonyms = t.getTerm();
			addTranslation(t.getKey(), synonyms, t.getLanguage());
		}

	}

	/**
	 * Updates the trust of a given translation in the database.
	 * 
	 * @param t specifies the translation and trust to be saved.
	 */
	public static synchronized void saveTranslationTrust(Translation t) {
		String sql = "UPDATE " + Translation.TABLE + "_" + t.getLanguage()
				+ " SET " + Translation.TRUST + " = " + t.getTrust()
				+ " WHERE " + Translation.SENSE_KEY + " = '" + t.getKey() + "'"
				+ " AND " + Translation.TERM + " = '" + t.getTerm() + "';";

		try {
			db_write.execSQL(sql);
		} catch (SQLiteException e) {
			Log.e(TAG, e.toString());
		}
	}

	/**
	 * Updates the confidence of a given translation in the database.
	 * 
	 * @param t specifies the translation and confidence to be saved.
	 */
	public static synchronized void saveTranslationConfidence(Translation t) {
		String sql = "UPDATE " + Translation.TABLE + "_" + t.getLanguage()
				+ " SET " + Translation.CONFIDENCE + " = " + t.getConfidence()
				+ " WHERE " + Translation.SENSE_KEY + " = '" + t.getKey() + "'"
				+ " AND " + Translation.TERM + " = '" + t.getTerm() + "';";

		try {
			db_write.execSQL(sql);
		} catch (SQLiteException e) {
			Log.e(TAG, e.toString());
		}
	}

	/**
	 * Compiles a histogram of trust values.
	 * 
	 * @param language defines the language to be used.
	 * @param amounts stores count for each unique trust.
	 * @param trusts stores the unique trusts.
	 * @return total of items in the histogram (area).
	 */
	public static synchronized int fillTranslationTrustLists(String language, List<Integer> amounts, List<Float> trusts) {
		String sql = "SELECT COUNT(trusts) AS how_many,trusts "
				+ "FROM (SELECT " + Translation.SENSE_KEY + ",AVG(" + Translation.TRUST + ") AS trusts "
				+ "FROM " + Translation.TABLE + "_" + language + " "
				+ "GROUP BY " + Translation.SENSE_KEY + ") "
				+ "GROUP BY trusts ORDER BY trusts DESC;";
		Cursor c = db_read.rawQuery(sql, null);
		int total = 0;
		while (c.moveToNext()) {
			int a = c.getInt(0);
			float t = c.getFloat(1);
			amounts.add(a);
			trusts.add(t);
			total += a;
		}
		c.close();
		return total;
	}

	/**
	 * Compiles a histogram of positive confidence values.
	 * 
	 * @param language defines the language to be used.
	 * @param amounts stores count for each unique positive confidence.
	 * @param trusts stores the unique positive confidences.
	 * @return total of items in the histogram.
	 */
	public static synchronized void fillTranslationKnownLists(String language, List<Integer> amounts, List<Float> trusts) {
		String sql = "SELECT COUNT(confidences) AS how_many,confidences "
				+ "FROM (SELECT " + Translation.SENSE_KEY + ",AVG(" + Translation.CONFIDENCE + ") AS confidences "
				+ "FROM " + Translation.TABLE + "_" + language + " "
				+ "WHERE " + Translation.CONFIDENCE + "> 0 "
				+ "GROUP BY " + Translation.SENSE_KEY + ") "
				+ "GROUP BY confidences ORDER BY confidences DESC;";
		Cursor c = db_read.rawQuery(sql, null);
		while (c.moveToNext()) {
			int a = c.getInt(0);
			float t = c.getFloat(1);
			amounts.add(a);
			trusts.add(t);
		}
		c.close();
	}

	/**
	 * Compiles a histogram of negative confidence values.
	 * 
	 * @param language defines the language to be used.
	 * @param amounts stores count for each unique negative confidence.
	 * @param trusts stores the unique negative confidences.
	 * @return total of items in the histogram.
	 */
	public static synchronized void fillTranslationUnknownLists(String language, List<Integer> amounts, List<Float> trusts) {
		String sql = "SELECT COUNT(confidences) AS how_many,confidences "
				+ "FROM (SELECT " + Translation.SENSE_KEY + ",AVG(" + Translation.CONFIDENCE + ") AS confidences "
				+ "FROM " + Translation.TABLE + "_" + language + " "
				+ "WHERE " + Translation.CONFIDENCE + "< 0 "
				+ "GROUP BY " + Translation.SENSE_KEY + ") "
				+ "GROUP BY confidences ORDER BY confidences ASC;";
		Cursor c = db_read.rawQuery(sql, null);
		while (c.moveToNext()) {
			int a = c.getInt(0);
			float t = c.getFloat(1);
			amounts.add(a);
			trusts.add(t);
		}
		c.close();
	}

	/**
	 * Fetches a single sense for which there is no translation yet.
	 * 
	 * @param language specifies the language to be considered.
	 * @return the sense key.
	 */
	public static synchronized String getSenseToTranslate(String language) {
		String sql = "SELECT MAX(" + Sense.PRIORITY + ")," + Sense.SENSE + " "
				+ "FROM " + Sense.TABLE + " LEFT JOIN " + Translation.TABLE + "_" + language + " "
				+ "ON " + Sense.SENSE + " = " + Translation.SENSE_KEY + " "
				+ "WHERE " + Translation.SENSE_KEY + " IS NULL;";
		String result = null;
		Cursor c = db_read.rawQuery(sql, null);
		if (c.moveToFirst()) {
			result = c.getString(c.getColumnIndex(Sense.SENSE.toString()));
		}
		c.close();
		return result;
	}
}
