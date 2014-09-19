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

public class LanguageSettings {

	public static final String TAG = AVBApp.TAG + "LanguageSettings";

	private static final String TABLE = "settings";

	private static final String LANGUAGE = "language";
	private static final String INSTALLED = "active";

	private static SQLiteDatabase db_write = null;
	private static SQLiteDatabase db_read = null;

	public static final String GERMAN = "de";
	public static final String SWEDISH = "sv";
	public static final String NORWEGIAN = "no";
	public static final String PORTUGUESE = "pt";
	public static final String POLISH = "pl";

	private static ImageGetter flags = null;

	public static void upgradeFrom(int version, SQLiteDatabase sql_db) {
		// TODO version reduction
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

	public static synchronized void initialize(Context where) {
		Log.i(TAG, "Initializing.");
		DatabaseHelper dbh = new DatabaseHelper(where);
		db_read = dbh.getReadableDatabase();
		db_write = dbh.getWritableDatabase();
	}

	public static List<String> getAllLanguages() {
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

	public static List<String> getInstalledLanguages() {
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

	public static void installLanguage(String language, Context where) {
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

	public static void removeLanguage(final String language, Context where) {
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

	// TODO see if sync is needed
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

	public static String prettyName(String l, Context c) {
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
}
