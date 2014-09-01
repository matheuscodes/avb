package org.arkanos.avb.data;

import java.util.HashMap;

import org.arkanos.avb.AVBApp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class LanguageSettings {

	public static final String TAG = AVBApp.TAG + "LanguageSettings";

	private static final String TABLE = "settings";

	private static final String LANGUAGE = "language";
	private static final String INSTALLED = "active";

	private static SQLiteDatabase db_write = null;
	private static SQLiteDatabase db_read = null;

	public static void upgradeFrom(int version, SQLiteDatabase sql_db) {
		if (version < 23) {
			sql_db.execSQL("CREATE TABLE " + TABLE + "("
					+ LANGUAGE + " TEXT PRIMARY KEY,"
					+ INSTALLED + " TEXT NOT NULL DEFAULT 'f');");

			ContentValues language = new ContentValues();
			language.put(LANGUAGE, "sv");
			language.put(INSTALLED, "f");
			sql_db.insert(TABLE, null, language);

			language = new ContentValues();
			language.put(LANGUAGE, "de");
			language.put(INSTALLED, "f");
			sql_db.insert(TABLE, null, language);
		}
	}

	public static synchronized void initialize(Context where) {
		Log.d(TAG, "Initializing.");
		DatabaseHelper dbh = new DatabaseHelper(where);
		db_read = dbh.getReadableDatabase();
		db_write = dbh.getWritableDatabase();
	}

	public static HashMap<String, Boolean> getInstalledLanguages() {
		HashMap<String, Boolean> states = new HashMap<String, Boolean>();
		String sql = "SELECT " + LANGUAGE + "," + INSTALLED + " FROM " + TABLE + ";";
		try {
			Cursor c = db_write.rawQuery(sql, null);
			while (c.moveToNext()) {
				if (c.getString(c.getColumnIndex(INSTALLED)).equals("t")) {
					states.put(c.getString(c.getColumnIndex(LANGUAGE)), true);
				}
			}
		} catch (SQLiteException e) {
			Log.e(TAG, e.toString());
		}
		return states;
	}

	public static void installLanguage(String language, Context c) {
		TranslationImporter caller;

		// TODO testing, not adding
		// caller = new TranslationImporter(language, c);
		// caller.execute();

		String sql = "UPDATE " + TABLE + " SET " + INSTALLED + " = 't' WHERE " + LANGUAGE + " = ?;";

		try {
			db_write.execSQL(sql, new Object[] { language });
		} catch (SQLiteException e) {
			Log.e(TAG, e.toString());
		}
	}

	public static void removeLanguage(String language) {
		// TODO testing, not removing.
		// BabelTower.clean();

		String sql = "UPDATE " + TABLE + " SET " + INSTALLED + " = 'f' WHERE " + LANGUAGE + " = ?;";

		try {
			db_write.execSQL(sql, new Object[] { language });
		} catch (SQLiteException e) {
			Log.e(TAG, e.toString());
		}
	}
}
