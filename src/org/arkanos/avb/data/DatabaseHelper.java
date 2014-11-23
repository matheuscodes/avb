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

import org.arkanos.avb.AVBApp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Helper to manage Database configurations.
 * 
 * @version 1.0
 * @author Matheus Borges Teixeira
 */
public class DatabaseHelper extends SQLiteOpenHelper {
	/** Tag for debug outputs **/
	public static final String TAG = AVBApp.TAG + "DatabaseHelper";

	/** Database filename **/
	public static final String DATABASE_NAME = "avb_data.db";
	/** Current version for reference **/
	public static final int DATABASE_VERSION = 24;

	/**
	 * Constructor starts database in the SD card.
	 * 
	 * @param context specifies the context of the application.
	 */
	public DatabaseHelper(Context context) {
		// TODO check for the SD card first, if fails, close.
		super(context, context.getExternalFilesDir(null).getAbsolutePath() + "/" + DATABASE_NAME, null, DATABASE_VERSION);
	}

	/**
	 * @see SQLiteOpenHelper#onCreate(SQLiteDatabase)
	 */
	@Override
	public void onCreate(SQLiteDatabase sql_db) {
		LanguageSettings.upgradeFrom(0, sql_db);
		Dictionary.upgradeFrom(0, sql_db);
		BabelTower.upgradeFrom(0, sql_db);
	}

	/**
	 * @see SQLiteOpenHelper#onUpgrade(SQLiteDatabase, int, int)
	 */
	@Override
	public void onUpgrade(SQLiteDatabase sql_db, int old_version, int new_version) {
		LanguageSettings.upgradeFrom(old_version, sql_db);
		Dictionary.upgradeFrom(old_version, sql_db);
		BabelTower.upgradeFrom(old_version, sql_db);
	}

}
