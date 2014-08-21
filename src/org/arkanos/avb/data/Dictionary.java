package org.arkanos.avb.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class Dictionary {
	private SQLiteDatabase db_write = null;
	private SQLiteDatabase db_read = null;

	public Dictionary(DatabaseHelper dbh) {
		db_read = dbh.getReadableDatabase();
		db_write = dbh.getWritableDatabase();
	}

	public int getSize() {
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

	public void addSense(ContentValues data) {
		try {
			db_write.insert(Sense.TABLE, null, data);
		} catch (SQLiteException e) {
			// TODO auto
			e.printStackTrace();
		}
	}

	public static void createDictionary(SQLiteDatabase sql_db) {
		sql_db.execSQL("DROP TABLE IF EXISTS " + Sense.TABLE + ";");
		sql_db.execSQL(Sense.createSQLTable());
	}

}
