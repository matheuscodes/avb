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

import java.util.LinkedList;
import java.util.List;

import org.arkanos.avb.AVBApp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

/**
 * Singleton holding all sense data operations.
 * 
 * @version 1.0
 * @author Matheus Borges Teixeira
 */
public class Dictionary {
	/** Tag for debug outputs **/
	public static final String TAG = AVBApp.TAG + "Dictionary";

	/** Writing link to the database **/
	private static SQLiteDatabase db_write = null;
	/** Reading link to the database **/
	private static SQLiteDatabase db_read = null;

	/** Reference to the Wordnet loading process **/
	private static WordnetImporter wordnet = null;

	/**
	 * Open the connections to the database and initializes the dictionary.
	 * 
	 * @param context defines the application context.
	 */
	public static synchronized void initialize(Context context) {
		Log.i(TAG, "Initializing.");
		DatabaseHelper dbh = new DatabaseHelper(context);
		db_read = dbh.getReadableDatabase();
		db_write = dbh.getWritableDatabase();
		if (Dictionary.wordnet == null) {
			Dictionary.wordnet = new WordnetImporter(context);
		}
	}

	/**
	 * Counts unique senses installed.
	 * 
	 * @return count of unique senses installed.
	 */
	public static synchronized int getSize() {
		try {
			Cursor c = db_read.rawQuery("SELECT COUNT(*) FROM " + Sense.TABLE + ";", null);
			if (c.moveToFirst()) {
				int result = c.getInt(0);
				c.close();
				return result;
			}
			c.close();
		} catch (SQLiteException e) {
			Log.e(TAG, e.toString());
		}
		return -1;
	}

	/**
	 * Inserts a new sense to the database.
	 * 
	 * @param data defines the row values for the unique sense list.
	 * @param text defines the string to be used in the search database.
	 */
	public static synchronized void addSense(ContentValues data, ContentValues text) {
		try {
			db_write.insert(Sense.TABLE, null, data);
			db_write.insert(Sense.TABLE_TEXT, null, text);
		} catch (SQLiteException e) {
			Log.e(TAG, e.toString());
		}
	}

	/**
	 * Searches the sense database.
	 * 
	 * @param match specifies the search query.
	 * @return a list of all senses which match the query.
	 */
	public static synchronized List<Sense> searchSenses(String match) {
		List<Sense> results = new LinkedList<Sense>();
		try {
			Cursor c = db_read.rawQuery("SELECT * FROM " + Sense.TABLE_TEXT
					+ " WHERE " + Sense.SYNONYMS
					+ " MATCH '" + match + "';", null);
			if (c.moveToFirst()) {
				do {
					Sense newone = new Sense(c);
					newone.setSortPower(match);
					results.add(newone);
				} while (c.moveToNext());
			}
			c.close();
		} catch (SQLiteException e) {
			Log.e(TAG, e.toString());
		}
		return results;
	}

	/**
	 * Upgrades database structure from a particular previous version.
	 * 
	 * @param version specifies the current version of the database.
	 * @param sql_db defines a connection to the database.
	 */
	public static synchronized void upgradeFrom(int version, SQLiteDatabase sql_db) {
		if (version < 12) {
			for (String sql : Sense.purgetSQLTables()) {
				sql_db.execSQL(sql);
			}
			for (String sql : Sense.createSQLTables()) {
				sql_db.execSQL(sql);
			}
		}
		if (version < 18) {
			Log.d(TAG, "Moving to version 18.");
			sql_db.execSQL("CREATE INDEX dictionary_index_priority ON " + Sense.TABLE + " (" + Sense.PRIORITY + " DESC);");
		}
	}

	/**
	 * Wipes out the senses data.
	 */
	public static synchronized void clean() {
		for (String sql : Sense.purgetSQLTables()) {
			db_write.execSQL(sql);
		}
		for (String sql : Sense.createSQLTables()) {
			db_write.execSQL(sql);
		}
	}

	/**
	 * Creates or updates all necessary indexes to support queries.
	 */
	public static synchronized void optimize() {
		db_write.execSQL("INSERT INTO " + Sense.TABLE_TEXT + "(" + Sense.TABLE_TEXT + ") VALUES('optimize');");
	}

	/**
	 * Fetches a specific sense.
	 * 
	 * @param key specifies the key for the sense.
	 * @return the sense in the key.
	 */
	public static synchronized Sense getSense(String key) {
		try {
			// Log.d(TAG, "Search sense key " + key);
			Cursor c = db_read.rawQuery("SELECT * FROM " + Sense.TABLE_TEXT
					+ " WHERE " + Sense.SENSE
					+ " MATCH '" + key + "';", null);
			if (c.moveToFirst()) {
				Sense newone = new Sense(c);
				// Log.d(TAG, "Found " + newone.getHead());
				if (c.moveToNext()) {
					Log.e(TAG, "Danger Will Robinson! Danger! Duplicated sense key.");
				}
				c.close();
				return newone;
			}
			c.close();
		} catch (SQLiteException e) {
			Log.e(TAG, e.toString());
		}
		return null;
	}

}
