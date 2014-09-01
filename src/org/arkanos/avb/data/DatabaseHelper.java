package org.arkanos.avb.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

	public static final String DATABASE_NAME = "avb_data.db";
	public static final int DATABASE_VERSION = 23;

	public DatabaseHelper(Context context) {
		// super(context, DATABASE_NAME, null, DATABASE_VERSION);
		// TODO check for the SD card first, if fails, close.
		super(context, context.getExternalFilesDir(null).getAbsolutePath() + "/" + DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase sql_db) {
		LanguageSettings.upgradeFrom(0, sql_db);
		Dictionary.upgradeFrom(0, sql_db);
		BabelTower.upgradeFrom(0, sql_db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase sql_db, int old_version, int new_version) {
		LanguageSettings.upgradeFrom(old_version, sql_db);
		Dictionary.upgradeFrom(old_version, sql_db);
		BabelTower.upgradeFrom(old_version, sql_db);
	}

}
