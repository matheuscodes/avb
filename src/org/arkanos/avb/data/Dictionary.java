package org.arkanos.avb.data;

import java.util.LinkedList;
import java.util.List;

import org.arkanos.avb.ui.DictionaryLoadingDialog;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class Dictionary {
	private static SQLiteDatabase db_write = null;
	private static SQLiteDatabase db_read = null;

	private static Wordnet wordnet = null;

	public static synchronized Wordnet loadWordnet(Activity context) {
		if (Dictionary.wordnet == null) {
			DictionaryLoadingDialog progressDialog = new DictionaryLoadingDialog(context);
			DatabaseHelper dbh = new DatabaseHelper(context);
			db_read = dbh.getReadableDatabase();
			db_write = dbh.getWritableDatabase();
			Dictionary.wordnet = new Wordnet(progressDialog, context);
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

	public static void addSense(ContentValues data) {
		try {
			db_write.insert(Sense.TABLE, null, data);
		} catch (SQLiteException e) {
			// TODO auto
			e.printStackTrace();
		}
	}

	public static List<Sense> searchSenses(String match) {
		List<Sense> results = new LinkedList<Sense>();
		try {
			Cursor c = db_read.rawQuery("SELECT * FROM " + Sense.TABLE
					+ " WHERE " + Sense.Fields.SYNONYMS
					+ " LIKE '%" + match + "%'", null);
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

	public static void createDictionary(SQLiteDatabase sql_db) {
		// TODO not drop
		sql_db.execSQL("DROP TABLE IF EXISTS " + Sense.TABLE + ";");
		sql_db.execSQL(Sense.createSQLTable());
	}

}
