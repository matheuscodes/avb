package org.arkanos.avb.data;

import java.util.Locale;

import android.database.Cursor;

public class Translation {

	public static final String TABLE = "translations";
	public static final String TABLE_TEXT = "translations_text";

	public enum Fields {
		SENSE_KEY, SYNONYMS, TERM, GRAMMAR_CLASS, TRUST, CONFIDENCE, LANGUAGE;

		@Override
		public String toString() {
			return this.name().toLowerCase(Locale.getDefault());
		}
	};

	private String language;
	// TODO defaults
	private String key;
	private String synonyms;
	private String term;
	private String grammar_class;
	private float trust;

	public Translation(Cursor c) {
		// String debug = "";
		key = c.getString(c.getColumnIndex(Fields.SENSE_KEY.toString()));
		// debug += key + "/";
		language = c.getString(c.getColumnIndex(Fields.LANGUAGE.toString()));
		// debug += language + "/";
		synonyms = c.getString(c.getColumnIndex(Fields.SYNONYMS.toString()));
		// debug += synonyms;
		// Log.d("AVB-Translation", debug);
	}

	public static String[] createSQLTables() {
		String[] results = new String[2];
		// TODO add foreign key
		results[0] = "CREATE TABLE " + Translation.TABLE + " ("
				+ Translation.Fields.SENSE_KEY + " TEXT,"
				+ Translation.Fields.LANGUAGE + " TEXT,"
				+ Translation.Fields.TERM + " TEXT,"
				+ Translation.Fields.GRAMMAR_CLASS + " TEXT,"
				+ Translation.Fields.CONFIDENCE + "  REAL NOT NULL DEFAULT 0,"
				+ Translation.Fields.TRUST + " REAL NOT NULL DEFAULT 0, "
				+ "PRIMARY KEY ("
				+ Translation.Fields.SENSE_KEY + ","
				+ Translation.Fields.LANGUAGE + ","
				+ Translation.Fields.TERM
				+ "));";
		results[1] = "CREATE VIRTUAL TABLE " + Translation.TABLE_TEXT + " USING fts4("
				+ Translation.Fields.SENSE_KEY + "," + Translation.Fields.SYNONYMS + "," + Translation.Fields.LANGUAGE + ");";
		return results;
	}

	public static String[] purgetSQLTables() {
		String[] results = new String[2];
		results[0] = "DROP TABLE IF EXISTS " + Translation.TABLE + ";";
		results[1] = "DROP TABLE IF EXISTS " + Translation.TABLE_TEXT + ";";
		return results;
	}

	public String getLanguage() {
		return language;
	}

	public String getContent() {
		// TODO what to do when it is term instead?
		return synonyms;
	}

	public void setTerm(String string) {
		term = string;
	}

	public String getTerm() {
		return term;
	}

}
