package org.arkanos.avb.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Dictionary {
	private SQLiteDatabase db_write = null;
	private SQLiteDatabase db_read = null;

	public Dictionary(DatabaseHelper dbh) {
		db_read = dbh.getReadableDatabase();
		db_write = dbh.getWritableDatabase();
	}

	public int getSize() {
		Cursor c = db_read.rawQuery("SELECT COUNT(*) FROM dictionary;", null);
		if (c.moveToFirst()) {
			return c.getInt(0);
		}
		else {
			return -1;
		}
	}

	public static void createDictionary(SQLiteDatabase sql_db) {
		String sql = "CREATE TABLE dictionary ("
				+ "sense TEXT PRIMARY KEY,"
				+ "synonyms TEXT NOT NULL,"
				+ "class TEXT NOT NULL,"
				+ "antonyms TEXT);";
		sql_db.execSQL(sql);
	}

}
