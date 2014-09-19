package org.arkanos.avb.data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.arkanos.avb.AVBApp;
import org.arkanos.avb.R;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class BabelTower {
	public static final String TAG = AVBApp.TAG + "BabelTower";

	// TODO everyone synchronized
	private static SQLiteDatabase db_write = null;
	private static SQLiteDatabase db_read = null;

	private static final int NOUN_CLASS = 0;
	private static final int NOUN_DESCRIPTION = 1;

	private static HashMap<String, String[]> languages_configuration;

	public static final String CONFIG_PATH = "config";

	// TODO optimization: one table per language +faster drop +table size

	private static void loadConfigs(Context c) {
		languages_configuration = new HashMap<String, String[]>();
		String[] helper;
		/** Nouns **/
		/* DE */
		helper = new String[2];
		helper[NOUN_CLASS] = "Femininum";
		helper[NOUN_DESCRIPTION] = c.getString(R.string.de_femininum);
		languages_configuration.put(LanguageSettings.GERMAN + "_f", helper);

		helper = new String[2];
		helper[NOUN_CLASS] = "Maskulinum";
		helper[NOUN_DESCRIPTION] = c.getString(R.string.de_maskulinum);
		languages_configuration.put(LanguageSettings.GERMAN + "_m", helper);

		helper = new String[2];
		helper[NOUN_CLASS] = "Neutrum";
		helper[NOUN_DESCRIPTION] = c.getString(R.string.de_neutrum);
		languages_configuration.put(LanguageSettings.GERMAN + "_n", helper);

		helper = new String[2];
		helper[NOUN_CLASS] = "Plural";
		helper[NOUN_DESCRIPTION] = c.getString(R.string.de_plural);
		languages_configuration.put(LanguageSettings.GERMAN + "_p", helper);

		/* SV */
		helper = new String[2];
		helper[NOUN_CLASS] = "neutrum";
		helper[NOUN_DESCRIPTION] = c.getString(R.string.sv_neutrum);
		languages_configuration.put(LanguageSettings.SWEDISH + "_n", helper);

		helper = new String[2];
		helper[NOUN_CLASS] = "utrum";
		helper[NOUN_DESCRIPTION] = c.getString(R.string.sv_utrum);
		languages_configuration.put(LanguageSettings.SWEDISH + "_u", helper);

		helper = new String[2];
		helper[NOUN_CLASS] = "Neutrum";
		helper[NOUN_DESCRIPTION] = c.getString(R.string.sv_plural);
		languages_configuration.put(LanguageSettings.SWEDISH + "_p", helper);
	}

	public static synchronized void initialize(Context where) {
		Log.i(TAG, "Initializing.");
		loadConfigs(where);
		DatabaseHelper dbh = new DatabaseHelper(where);
		db_read = dbh.getReadableDatabase();
		db_write = dbh.getWritableDatabase();
	}

	public static void upgradeFrom(int version, SQLiteDatabase sql_db) {
		if (version < 17) {
			sql_db.execSQL(Translation.purgetSQLTable());
			sql_db.execSQL(Translation.createSQLTable());
		}
	}

	public static void prepare(String language) {
		db_write.execSQL(Translation.createSQLTable(language));
	}

	public static void clean(String language) {
		// TODO either move to translation or get purge to here.
		db_write.execSQL("DROP TABLE IF EXISTS " + Translation.TABLE + "_" + language + ";");
		db_write.execSQL("DELETE FROM " + Translation.TABLE_TEXT + " WHERE " + Translation.LANGUAGE + " MATCH '" + language + "';");
	}

	public static void optimize(String language) {
		db_write.execSQL("INSERT INTO " + Translation.TABLE_TEXT + "(" + Translation.TABLE_TEXT + ") VALUES('optimize');");
		db_write.execSQL("CREATE INDEX IF NOT EXISTS translation_index_confidence_trust_" + language + " ON "
				+ Translation.TABLE + "_" + language + " ("
				+ Translation.CONFIDENCE + ","
				+ Translation.TRUST + ");");
	}

	public static void addTranslation(ContentValues data, String language) {
		try {
			db_write.insert(Translation.TABLE + "_" + language, null, data);
		} catch (SQLiteException e) {
			Log.e(TAG, e.toString());
		}
	}

	public static void addTranslation(String key, String synonyms, String language) {
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

	public static List<Sense> searchTranslations(String query) {
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

	public static List<Translation> getPartition(int size, String language) {
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

	private static List<Translation> selectPartition(int count, String language, float c_min, float c_max) {
		List<Translation> result = new LinkedList<Translation>();
		try {
			String sql = "SELECT * FROM " + Translation.TABLE + "_" + language
					+ " WHERE " + Translation.CONFIDENCE + " <= " + c_max
					+ " AND " + Translation.CONFIDENCE + " >= " + c_min
					+ " LIMIT " + count + ";";
			// Log.d(TAG, "SQL: " + sql);
			// Log.d("AVB-BabelTower", "Trying to fetch " + count + ".");
			Cursor c = db_read.rawQuery(sql, null);

			if (c.moveToFirst()) {
				// Log.d("AVB-BabelTower", "Fetching done.");
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

	public static List<Translation> getTranslations(String key, String language) {
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

	public static void deleteTranslation(Translation t) {
		String sql_table = "DELETE FROM " + Translation.TABLE + "_" + t.getLanguage()
				+ " WHERE " + Translation.SENSE_KEY + " = '" + t.getKey() + "'"
				+ " AND " + Translation.TERM + " = '" + t.getTerm() + "';";
		String clean;
		// TODO maybe move this code to Translation
		if (t.getSynonyms() == null) {
			clean = getTranslationSynonyms(t.getKey(), t.getLanguage()).trim();
		}
		else {
			clean = t.getSynonyms();
		}
		clean = clean.replace(t.getTerm(), "").replace("  ", " ");
		t.setSynonyms(clean);
		String sql_text;
		if (t.getSynonyms().length() > 0) {
			sql_text = "UPDATE " + Translation.TABLE_TEXT
					+ " SET " + Translation.SYNONYMS + " = '" + t.getSynonyms() + "'"
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

	public static String getTranslationSynonyms(String key, String language) {
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

	public static void addTranslation(Translation t) {
		ContentValues map = new ContentValues();
		map.put(Translation.SENSE_KEY, t.getKey());
		map.put(Translation.TERM, t.getTerm());
		map.put(Translation.TRUST, t.getTrust());
		if (t.getGrammar() != null) {
			map.put(Translation.GRAMMAR, t.getGrammar());
		}
		addTranslation(map, t.getLanguage());

		String synonyms = getTranslationSynonyms(t.getKey(), t.getLanguage());
		// TODO maybe move this logic to Translation
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

	public static void saveTranslationTrust(Translation t) {
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

	public static void saveTranslationConfidence(Translation t) {
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

	public static void fillTranslationTrustLists(String language, List<Integer> amounts, List<Float> trusts) {
		String sql = "SELECT COUNT(trusts) AS how_many,trusts "
				+ "FROM (SELECT " + Translation.SENSE_KEY + ",AVG(" + Translation.TRUST + ") AS trusts "
				+ "FROM " + Translation.TABLE + "_" + language + " "
				+ "GROUP BY " + Translation.SENSE_KEY + ") "
				+ "GROUP BY trusts ORDER BY trusts DESC;";
		Cursor c = db_read.rawQuery(sql, null);
		while (c.moveToNext()) {
			int a = c.getInt(0);
			float t = c.getFloat(1);
			amounts.add(a);
			trusts.add(t);
		}
		c.close();
	}

	public static void fillTranslationKnownLists(String language, List<Integer> amounts, List<Float> trusts) {
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

	public static void fillTranslationUnknownLists(String language, List<Integer> amounts, List<Float> trusts) {
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

	public static String getSenseToTranslate(String language) {
		String sql = "SELECT MAX(" + Sense.Fields.PRIORITY + ")," + Sense.Fields.SENSE + " "
				+ "FROM " + Sense.TABLE + " LEFT JOIN " + Translation.TABLE + "_" + language + " "
				+ "ON " + Sense.Fields.SENSE + " = " + Translation.SENSE_KEY + " "
				+ "WHERE " + Translation.SENSE_KEY + " IS NULL;";
		String result = null;
		Cursor c = db_read.rawQuery(sql, null);
		if (c.moveToFirst()) {
			result = c.getString(c.getColumnIndex(Sense.Fields.SENSE.toString()));
		}
		c.close();
		return result;
	}
}
