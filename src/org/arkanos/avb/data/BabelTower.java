package org.arkanos.avb.data;

import java.util.HashMap;

import org.arkanos.avb.R;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

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

	public static void loadTranslations(Context where) {
		loadConfigs(where);
		DatabaseHelper dbh = new DatabaseHelper(where);
		db_read = dbh.getReadableDatabase();
		db_write = dbh.getWritableDatabase();
	}

	public static void upgradeFrom(int version, SQLiteDatabase sql_db) {
		if (version < 13) {
			for (String sql : Translation.purgetSQLTables()) {
				sql_db.execSQL(sql);
			}
			for (String sql : Translation.createSQLTables()) {
				sql_db.execSQL(sql);
			}
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
}
