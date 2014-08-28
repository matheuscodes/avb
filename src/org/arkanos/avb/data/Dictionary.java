package org.arkanos.avb.data;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

public class Dictionary {
	// TODO everyone synchronized
	private static SQLiteDatabase db_write = null;
	private static SQLiteDatabase db_read = null;

	private static WordnetImporter wordnet = null;

	public static synchronized WordnetImporter loadWordnet(Activity context) {
		if (Dictionary.wordnet == null) {
			DatabaseHelper dbh = new DatabaseHelper(context);
			db_read = dbh.getReadableDatabase();
			db_write = dbh.getWritableDatabase();
			Dictionary.wordnet = new WordnetImporter(context);
		}
		return wordnet;
	}

	public static int getSize() {
		try {
			Cursor c = db_read.rawQuery("SELECT COUNT(*) FROM " + Sense.TABLE + ";", null);
			if (c.moveToFirst()) {
				return c.getInt(0);
			}
		} catch (SQLiteException e) {
			// TODO auto
			e.printStackTrace();
		}
		return -1;
	}

	public static void addSense(ContentValues data, ContentValues text) {
		try {
			db_write.insert(Sense.TABLE, null, data);
			db_write.insert(Sense.TABLE_TEXT, null, text);
		} catch (SQLiteException e) {
			// TODO auto
			e.printStackTrace();
		}
	}

	public static List<Sense> searchSenses(String match) {
		List<Sense> results = new LinkedList<Sense>();
		try {
			Cursor c = db_read.rawQuery("SELECT * FROM " + Sense.TABLE_TEXT
					+ " WHERE " + Sense.Fields.SYNONYMS
					+ " MATCH '" + match + "';", null);
			if (c.moveToFirst()) {
				do {
					Sense newone = new Sense(c);
					results.add(newone);
				} while (c.moveToNext());
			}
		} catch (SQLiteException e) {
			// TODO auto
			e.printStackTrace();
		}
		return results;
	}

	public static void upgradeFrom(int version, SQLiteDatabase sql_db) {
		if (version < 12) {
			for (String sql : Sense.purgetSQLTables()) {
				sql_db.execSQL(sql);
			}
			for (String sql : Sense.createSQLTables()) {
				sql_db.execSQL(sql);
			}
		}
		if (version < 18) {
			Log.d("AVB-Dictionary", "Moving to version 18.");
			sql_db.execSQL("CREATE INDEX dictionary_index_priority_desc ON " + Sense.TABLE + " (" + Sense.Fields.PRIORITY + " DESC);");
		}
	}

	public static void clean() {
		// TODO only remove data
		for (String sql : Sense.purgetSQLTables()) {
			db_write.execSQL(sql);
		}
		for (String sql : Sense.createSQLTables()) {
			db_write.execSQL(sql);
		}
	}

	public static void optimize() {
		db_write.execSQL("INSERT INTO " + Sense.TABLE_TEXT + "(" + Sense.TABLE_TEXT + ") VALUES('optimize');");
	}

	public static Sense getSense(String key) {
		try {
			Cursor c = db_read.rawQuery("SELECT * FROM " + Sense.TABLE_TEXT
					+ " WHERE " + Sense.Fields.SENSE
					+ " MATCH '" + key + "';", null);
			if (c.moveToFirst()) {
				Sense newone = new Sense(c);
				if (c.moveToNext()) {
					Log.e("AVB-Dictionary", "Danger Will Robinson! Danger! Duplicated sense key.");
				}
				return newone;
			}
		} catch (SQLiteException e) {
			// TODO auto
			e.printStackTrace();
		}
		return null;
	}

}
