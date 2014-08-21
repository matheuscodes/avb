package org.arkanos.avb.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "avb_data.db";
	public static final int DATABASE_VERSION = 1;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sql_db) {
		Dictionary.createDictionary(sql_db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sql_db, int old_version, int new_version) {
		sql_db.execSQL("DROP TABLE IF EXISTS dictionary;");
		Dictionary.createDictionary(sql_db);
	}

}