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
import org.arkanos.avb.R;
import org.arkanos.avb.ui.WaitingDialog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.text.Html.ImageGetter;
import android.util.Log;

/**
 * Singleton with application settings.
 * 
 * @version 1.0
 * @author Matheus Borges Teixeira
 */
public class LanguageSettings {
	/** Tag for debug outputs **/
	public static final String TAG = AVBApp.TAG + "LanguageSettings";
	/** Table name where settings are stored **/
	private static final String TABLE = "settings";

	/** Column name for the language **/
	private static final String LANGUAGE = "language";
	/** Column name for the installation status **/
	private static final String INSTALLED = "active";

	/** Writing link to the database **/
	private static SQLiteDatabase db_write = null;

	/** Language code for German **/
	public static final String GERMAN = "de";
	/** Language code for Swedish **/
	public static final String SWEDISH = "sv";
	/** Language code for Norwegian **/
	public static final String NORWEGIAN = "no";
	/** Language code for Portuguese **/
	public static final String PORTUGUESE = "pt";
	/** Language code for Polish **/
	public static final String POLISH = "pl";

	/** Special getter for flag images **/
	private static ImageGetter flags = null;

	/**
	 * Upgrades database structure from a particular previous version.
	 * 
	 * @param version specifies the current version of the database.
	 * @param sql_db defines a connection to the database.
	 */
	public static synchronized void upgradeFrom(int version, SQLiteDatabase sql_db) {
		// TODO Reduce the version count numbers.
		ContentValues language = null;
		if (version < 23) {
			sql_db.execSQL("CREATE TABLE " + TABLE + "("
					+ LANGUAGE + " TEXT PRIMARY KEY,"
					+ INSTALLED + " TEXT NOT NULL DEFAULT 'f');");

			language = new ContentValues();
			language.put(LANGUAGE, SWEDISH);
			language.put(INSTALLED, "f");
			sql_db.insert(TABLE, null, language);

			language = new ContentValues();
			language.put(LANGUAGE, GERMAN);
			language.put(INSTALLED, "f");
			sql_db.insert(TABLE, null, language);
		}
		if (version < 24) {
			language = new ContentValues();
			language.put(LANGUAGE, NORWEGIAN);
			language.put(INSTALLED, "f");
			sql_db.insert(TABLE, null, language);

			language = new ContentValues();
			language.put(LANGUAGE, PORTUGUESE);
			language.put(INSTALLED, "f");
			sql_db.insert(TABLE, null, language);

			language = new ContentValues();
			language.put(LANGUAGE, POLISH);
			language.put(INSTALLED, "f");
			sql_db.insert(TABLE, null, language);
		}
	}

	/**
	 * Open the connections to the database.
	 * 
	 * @param context defines the application context.
	 */
	public static synchronized void initialize(Context where) {
		Log.i(TAG, "Initializing.");
		DatabaseHelper dbh = new DatabaseHelper(where);
		db_write = dbh.getWritableDatabase();
	}

	/**
	 * Gets all languages in the database.
	 * 
	 * @return list with all languages.
	 */
	public static synchronized List<String> getAllLanguages() {
		List<String> states = new LinkedList<String>();
		String sql = "SELECT " + LANGUAGE + " FROM " + TABLE + ";";
		try {
			Cursor c = db_write.rawQuery(sql, null);
			while (c.moveToNext()) {
				states.add(c.getString(c.getColumnIndex(LANGUAGE)));
			}
			c.close();
		} catch (SQLiteException e) {
			Log.e(TAG, e.toString());
		}
		return states;
	}

	/**
	 * Gets only installed languages.
	 * 
	 * @return list with the installed languages.
	 */
	public static synchronized List<String> getInstalledLanguages() {
		List<String> states = new LinkedList<String>();
		String sql = "SELECT " + LANGUAGE + "," + INSTALLED + " FROM " + TABLE + " WHERE " + INSTALLED + " = 't';";
		try {
			Cursor c = db_write.rawQuery(sql, null);
			while (c.moveToNext()) {
				states.add(c.getString(c.getColumnIndex(LANGUAGE)));
			}
			c.close();
		} catch (SQLiteException e) {
			Log.e(TAG, e.toString());
		}
		return states;
	}

	/**
	 * Updates the installation status in language settings and installs it.
	 * 
	 * @param language specifies the language to be updated.
	 * @param where specifies the application context.
	 */
	public static synchronized void installLanguage(String language, Context where) {
		TranslationImporter caller;

		caller = new TranslationImporter(language, where);
		caller.execute();

		String sql = "UPDATE " + TABLE + " SET " + INSTALLED + " = 't' WHERE " + LANGUAGE + " = ?;";

		try {
			db_write.execSQL(sql, new Object[] { language });
		} catch (SQLiteException e) {
			Log.e(TAG, e.toString());
		}
	}

	/**
	 * Updates the installation status in language settings and removes it.
	 * 
	 * @param language specifies the language to be updated.
	 * @param where specifies the application context.
	 */
	public static synchronized void removeLanguage(final String language, Context where) {
		final WaitingDialog wd = new WaitingDialog(where);
		wd.replaceTitle(where.getString(R.string.unload_translation).replace("{language}", prettyName(language, where)));
		wd.replaceMessage(where.getString(R.string.unload_translation_text));
		wd.startIt();
		new AsyncTask<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... a) {
				BabelTower.clean(language);
				return null;
			}

			@Override
			protected void onPostExecute(Void a) {
				String sql = "UPDATE " + TABLE + " SET " + INSTALLED + " = 'f' WHERE " + LANGUAGE + " = ?;";

				try {
					db_write.execSQL(sql, new Object[] { language });
				} catch (SQLiteException e) {
					Log.e(TAG, e.toString());
				}
				wd.finishIt();
			}

		}.execute();
	}

	/**
	 * Gets the reference to the flag image helper.
	 * This can be used to resolve flag images.
	 * 
	 * @param c specifies the application context.
	 * @return reference to the flag image helper.
	 */
	public static synchronized ImageGetter getFlags(final Context c) {
		if (flags == null) {
			flags = new ImageGetter() {
				public Drawable getDrawable(String source) {
					Drawable d = c.getResources().getDrawable(R.drawable.flag_usgb);

					if (source.equals(SWEDISH))
						d = c.getResources().getDrawable(R.drawable.flag_sv);

					if (source.equals(GERMAN))
						d = c.getResources().getDrawable(R.drawable.flag_de);

					if (source.equals(NORWEGIAN))
						d = c.getResources().getDrawable(R.drawable.flag_no);

					if (source.equals(PORTUGUESE))
						d = c.getResources().getDrawable(R.drawable.flag_ptbr);

					if (source.equals(POLISH))
						d = c.getResources().getDrawable(R.drawable.flag_pl);

					d.setBounds(0, 0, d.getIntrinsicWidth(), d.getIntrinsicHeight());
					return d;
				}
			};
		}
		return flags;
	}

	/**
	 * Converts a language code into its complete displayable name.
	 * 
	 * @param l specifies the language.
	 * @param c specifies the application context.
	 * @return a string with the language name.
	 */
	public static synchronized String prettyName(String l, Context c) {
		if (l.equals(LanguageSettings.GERMAN))
			return c.getString(R.string.languages_de_pretty);
		if (l.equals(LanguageSettings.SWEDISH))
			return c.getString(R.string.languages_sv_pretty);
		if (l.equals(LanguageSettings.NORWEGIAN))
			return c.getString(R.string.languages_no_pretty);
		if (l.equals(LanguageSettings.PORTUGUESE))
			return c.getString(R.string.languages_pt_pretty);
		if (l.equals(LanguageSettings.POLISH))
			return c.getString(R.string.languages_pl_pretty);
		return "";
	}

	/*
	 * TODO Clean this code.
	 * private static final int NOUN_CLASS = 0;
	 * private static final int NOUN_DESCRIPTION = 1;
	 * 
	 * private static HashMap<String, String[]> languages_configuration;
	 * private static void loadConfigs(Context c) {
	 * languages_configuration = new HashMap<String, String[]>();
	 * String[] helper;
	 * // Nouns
	 * // DE
	 * helper = new String[2];
	 * helper[NOUN_CLASS] = "Femininum";
	 * helper[NOUN_DESCRIPTION] = c.getString(R.string.de_femininum);
	 * languages_configuration.put(LanguageSettings.GERMAN + "_f", helper);
	 * 
	 * helper = new String[2];
	 * helper[NOUN_CLASS] = "Maskulinum";
	 * helper[NOUN_DESCRIPTION] = c.getString(R.string.de_maskulinum);
	 * languages_configuration.put(LanguageSettings.GERMAN + "_m", helper);
	 * 
	 * helper = new String[2];
	 * helper[NOUN_CLASS] = "Neutrum";
	 * helper[NOUN_DESCRIPTION] = c.getString(R.string.de_neutrum);
	 * languages_configuration.put(LanguageSettings.GERMAN + "_n", helper);
	 * 
	 * helper = new String[2];
	 * helper[NOUN_CLASS] = "Plural";
	 * helper[NOUN_DESCRIPTION] = c.getString(R.string.de_plural);
	 * languages_configuration.put(LanguageSettings.GERMAN + "_p", helper);
	 * 
	 * // SV
	 * helper = new String[2];
	 * helper[NOUN_CLASS] = "neutrum";
	 * helper[NOUN_DESCRIPTION] = c.getString(R.string.sv_neutrum);
	 * languages_configuration.put(LanguageSettings.SWEDISH + "_n", helper);
	 * 
	 * helper = new String[2];
	 * helper[NOUN_CLASS] = "utrum";
	 * helper[NOUN_DESCRIPTION] = c.getString(R.string.sv_utrum);
	 * languages_configuration.put(LanguageSettings.SWEDISH + "_u", helper);
	 * 
	 * helper = new String[2];
	 * helper[NOUN_CLASS] = "Neutrum";
	 * helper[NOUN_DESCRIPTION] = c.getString(R.string.sv_plural);
	 * languages_configuration.put(LanguageSettings.SWEDISH + "_p", helper);
	 * }
	 */
}
