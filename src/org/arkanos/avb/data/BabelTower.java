package org.arkanos.avb.data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.arkanos.avb.R;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.Drawable;
import android.text.Html.ImageGetter;
import android.util.Log;

public class BabelTower {
	// TODO everyone synchronized
	private static SQLiteDatabase db_write = null;
	private static SQLiteDatabase db_read = null;

	private static final int NOUN_CLASS = 0;
	private static final int NOUN_DESCRIPTION = 1;

	public static final String GERMAN = "de";
	public static final String SWEDISH = "sv";

	private static HashMap<String, String[]> languages_configuration;

	public static final String CONFIG_PATH = "config";

	private static BabelTower reference = null;
	private static Wordnet dictionary = null;

	private static ImageGetter flags = null;

	// TODO see if sync is needed
	public static synchronized ImageGetter getFlags(final Context c) {
		if (flags == null) {
			flags = new ImageGetter() {
				public Drawable getDrawable(String source) {
					Drawable d = c.getResources().getDrawable(R.drawable.flag_usgb);

					if (source.equals(SWEDISH))
						d = c.getResources().getDrawable(R.drawable.flag_sv);

					if (source.equals(GERMAN))
						d = c.getResources().getDrawable(R.drawable.flag_de);

					d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
					return d;
				}
			};
		}
		return flags;
	}

	private static void loadConfigs(Context c) {
		languages_configuration = new HashMap<String, String[]>();
		String[] helper;
		/** Nouns **/
		/* DE */
		helper = new String[2];
		helper[NOUN_CLASS] = "Femininum";
		helper[NOUN_DESCRIPTION] = c.getString(R.string.de_femininum);
		languages_configuration.put(GERMAN + "_f", helper);

		helper = new String[2];
		helper[NOUN_CLASS] = "Maskulinum";
		helper[NOUN_DESCRIPTION] = c.getString(R.string.de_maskulinum);
		languages_configuration.put(GERMAN + "_m", helper);

		helper = new String[2];
		helper[NOUN_CLASS] = "Neutrum";
		helper[NOUN_DESCRIPTION] = c.getString(R.string.de_neutrum);
		languages_configuration.put(GERMAN + "_n", helper);

		helper = new String[2];
		helper[NOUN_CLASS] = "Plural";
		helper[NOUN_DESCRIPTION] = c.getString(R.string.de_plural);
		languages_configuration.put(GERMAN + "_p", helper);

		/* SV */
		helper = new String[2];
		helper[NOUN_CLASS] = "neutrum";
		helper[NOUN_DESCRIPTION] = c.getString(R.string.sv_neutrum);
		languages_configuration.put(SWEDISH + "_n", helper);

		helper = new String[2];
		helper[NOUN_CLASS] = "utrum";
		helper[NOUN_DESCRIPTION] = c.getString(R.string.sv_utrum);
		languages_configuration.put(SWEDISH + "_u", helper);

		helper = new String[2];
		helper[NOUN_CLASS] = "Neutrum";
		helper[NOUN_DESCRIPTION] = c.getString(R.string.sv_plural);
		languages_configuration.put(SWEDISH + "_p", helper);
	}

	public static synchronized BabelTower prepareTranslations(Activity where) {
		if (reference == null) {
			reference = new BabelTower(); // TODO this is ugly as hell.
			dictionary = Dictionary.loadWordnet(where); // Mapping dependency.
			loadConfigs(where);
			DatabaseHelper dbh = new DatabaseHelper(where);
			db_read = dbh.getReadableDatabase();
			db_write = dbh.getWritableDatabase();
		}
		return reference;
	}

	public static void upgradeFrom(int version, SQLiteDatabase sql_db) {
		if (version < 17) {
			for (String sql : Translation.purgetSQLTables()) {
				sql_db.execSQL(sql);
			}
			for (String sql : Translation.createSQLTables()) {
				sql_db.execSQL(sql);
			}
		}
		if (version < 18) {
			Log.d("AVB-BabelTower", "Moving to version 18.");
			// TODO call optmize to tranlation text table
			// TODO probably tables need optimization every time new language installed
			// TODO IF NOT EXISTS
			sql_db.execSQL("CREATE INDEX translation_index_confidence_desc ON " + Translation.TABLE + " (" + Translation.Fields.CONFIDENCE + " DESC);");
			sql_db.execSQL("CREATE INDEX translation_index_confidence_asc ON " + Translation.TABLE + " (" + Translation.Fields.CONFIDENCE + " ASC);");
			sql_db.execSQL("CREATE INDEX translation_index_language ON " + Translation.TABLE + " (" + Translation.Fields.LANGUAGE + ");");
		}

		if (version < 21) {
			Log.d("AVB-BabelTower", "Moving to version 21.");
			sql_db.execSQL("CREATE INDEX translation_index_trust_desc ON " + Translation.TABLE + " (" + Translation.Fields.TRUST + " DESC);");
		}
	}

	public static void clean() {
		// TODO only remove data
		for (String sql : Translation.purgetSQLTables()) {
			db_write.execSQL(sql);
		}
		for (String sql : Translation.createSQLTables()) {
			db_write.execSQL(sql);
		}
	}

	public static void clean(String language) {
		db_write.execSQL("DELETE FROM " + Translation.TABLE + " WHERE language = '" + language + "';");
		db_write.execSQL("DELETE FROM " + Translation.TABLE_TEXT + " WHERE language MATCH '" + language + "';");
	}

	public static void optimize() {
		db_write.execSQL("INSERT INTO " + Translation.TABLE_TEXT + "(" + Translation.TABLE_TEXT + ") VALUES('optimize');");
	}

	public static String prettyName(String l, Context c) {
		if (l.equals(GERMAN))
			return c.getString(R.string.languages_de_pretty);
		if (l.equals(SWEDISH))
			return c.getString(R.string.languages_sv_pretty);
		return "";
	}

	public static void addTranslation(ContentValues data, String language) {
		data.put(Translation.Fields.LANGUAGE.toString(), language);
		try {
			db_write.insert(Translation.TABLE, null, data);
		} catch (SQLiteException e) {
			Log.e("AVB-BabelTower", e.toString());
		}
	}

	public static void addTranslation(String key, String synonyms, String language) {
		ContentValues data = new ContentValues();
		data.put(Translation.Fields.LANGUAGE.toString(), language);
		data.put(Translation.Fields.SYNONYMS.toString(), synonyms);
		data.put(Translation.Fields.SENSE_KEY.toString(), key);
		try {
			db_write.insert(Translation.TABLE_TEXT, null, data);
		} catch (SQLiteException e) {
			Log.e("AVB-BabelTower", e.toString());
		}
	}

	public static List<Sense> searchTranslations(String query) {
		List<Sense> results = new LinkedList<Sense>();
		try {
			Cursor c = db_read.rawQuery("SELECT * FROM " + Translation.TABLE_TEXT
					+ " WHERE " + Translation.Fields.SYNONYMS
					+ " MATCH '" + query + "';", null);
			if (c.moveToFirst()) {
				do {
					Sense newone = Dictionary.getSense(c.getString(c.getColumnIndex(Translation.Fields.SENSE_KEY.toString())));
					newone.addTranslation(new Translation(c));
					results.add(newone);
				} while (c.moveToNext());
			}
		} catch (SQLiteException e) {
			// TODO auto
			e.printStackTrace();
		}
		return results;
	}

	private static Translation getTranslation(String key, String language) {
		try {
			Cursor c = db_read.rawQuery("SELECT * FROM " + Translation.TABLE_TEXT + " WHERE "
					+ Translation.Fields.SENSE_KEY + " MATCH '" + key + "';", null);
			if (c.moveToFirst()) {
				do {
					Log.d("AVB-Taskforce", "Reading each key.");
					Translation newone = new Translation(c);
					if (c.getString(c.getColumnIndex(Translation.Fields.LANGUAGE.toString())).equals(language)) {
						newone = new Translation(c);
						return newone;
					}
				} while (c.moveToNext());
			}
		} catch (SQLiteException e) {
			// TODO auto
			e.printStackTrace();
		}
		return null;
	}

	public static Translation[] getBestKnown(int count, String language) {
		Translation[] result = new Translation[count];
		int read = 0;
		try {
			Log.d("AVB-BabelTower", "Fetching best known.");
			Cursor c = db_read.rawQuery("SELECT * FROM " + Translation.TABLE
					+ " WHERE " + Translation.Fields.LANGUAGE + " = '" + language + "' "
					+ " ORDER BY " + Translation.Fields.CONFIDENCE + " DESC LIMIT " + count + ";", null);
			Log.d("AVB-BabelTower", "Fetching done.");
			if (c.moveToFirst()) {
				do {
					Log.d("AVB-Taskforce", "Reading each term.");
					result[read] = BabelTower.getTranslation(c.getString(c.getColumnIndex(Translation.Fields.SENSE_KEY.toString())), language);
					result[read++].setTerm(c.getString(c.getColumnIndex(Translation.Fields.TERM.toString())));
				} while (c.moveToNext());
			}
			Log.d("AVB-BabelTower", "Fetched " + read + " best known.");
			return result;
		} catch (SQLiteException e) {
			// TODO auto
			e.printStackTrace();
		}
		return null;
	}

	public static Translation[] getWorstKnown(int count, String language) {
		Translation[] result = new Translation[count];
		int read = 0;
		try {
			Log.d("AVB-BabelTower", "Fetching worst known.");
			Cursor c = db_read.rawQuery("SELECT * FROM " + Translation.TABLE
					+ " WHERE " + Translation.Fields.LANGUAGE + " = '" + language + "' "
					+ " ORDER BY " + Translation.Fields.CONFIDENCE + " ASC, "
					+ Translation.Fields.TRUST + " DESC LIMIT " + count + ";", null);

			if (c.moveToFirst()) {
				do {
					result[read] = BabelTower.getTranslation(c.getString(c.getColumnIndex(Translation.Fields.SENSE_KEY.toString())), language);
					result[read++].setTerm(c.getString(c.getColumnIndex(Translation.Fields.TERM.toString())));
				} while (c.moveToNext());
			}
			Log.d("AVB-BabelTower", "Fetched " + read + " worst known.");
			return result;
		} catch (SQLiteException e) {
			// TODO auto
			e.printStackTrace();
		}
		return null;
	}

	public static Translation[] getAverageKnown(int count, String language) {
		Translation[] result = new Translation[count];
		int up = count / 2;

		int read = 0;
		try {
			Log.d("AVB-BabelTower", "Fetching average known.");
			Cursor c = db_read.rawQuery("SELECT AVG(" + Translation.Fields.CONFIDENCE + ") FROM " + Translation.TABLE
					+ " WHERE " + Translation.Fields.LANGUAGE + " = '" + language + "';", null);
			if (c.moveToFirst()) {
				float avg = c.getFloat(0);
				c.close();
				c = db_read.rawQuery("SELECT * FROM " + Translation.TABLE
						+ " WHERE " + Translation.Fields.LANGUAGE + " = '" + language + "' "
						+ " AND " + Translation.Fields.CONFIDENCE + " >= " + avg
						+ " ORDER BY " + Translation.Fields.CONFIDENCE + " ASC, "
						+ Translation.Fields.TRUST + " DESC LIMIT " + up + ";", null);
				if (c.moveToFirst()) {
					do {
						result[read] = BabelTower.getTranslation(c.getString(c.getColumnIndex(Translation.Fields.SENSE_KEY.toString())), language);
						result[read++].setTerm(c.getString(c.getColumnIndex(Translation.Fields.TERM.toString())));
					} while (c.moveToNext());
				}
				c.close();

				c = db_read.rawQuery("SELECT * FROM " + Translation.TABLE
						+ " WHERE " + Translation.Fields.LANGUAGE + " = '" + language + "' "
						+ " AND " + Translation.Fields.CONFIDENCE + " <= " + avg
						+ " ORDER BY " + Translation.Fields.CONFIDENCE + " DESC, "
						+ Translation.Fields.TRUST + " DESC LIMIT " + (count - read) + ";", null);

				if (c.moveToFirst()) {
					do {
						result[read] = BabelTower.getTranslation(c.getString(c.getColumnIndex(Translation.Fields.SENSE_KEY.toString())), language);
						result[read++].setTerm(c.getString(c.getColumnIndex(Translation.Fields.TERM.toString())));
					} while (c.moveToNext());
				}
				c.close();
				// TODO if read < count, relocate the array.

				Log.d("AVB-BabelTower", "Fetched " + read + " average known.");
				return result;
			}
			c.close();// TODO close in all other cursors too dict and babel
		} catch (SQLiteException e) {
			// TODO auto
			e.printStackTrace();
		}

		return null;
	}
}
