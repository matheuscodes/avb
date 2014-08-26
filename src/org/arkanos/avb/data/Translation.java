package org.arkanos.avb.data;

import java.util.Locale;

public class Translation {

	public static final String TABLE = "translations";
	public static final String TABLE_TEXT = "translations_text";

	public enum Fields {
		SENSE_KEY, SYNONYMS, TERM, GRAMMAR_CLASS, TRUST;

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

	public static String[] createSQLTables() {
		String[] results = new String[2];
		results[0] = "CREATE TABLE " + Translation.TABLE + " ("
				+ Translation.Fields.SENSE_KEY + " TEXT PRIMARY KEY,"
				+ Translation.Fields.TERM + " TEXT,"
				+ Translation.Fields.GRAMMAR_CLASS + " TEXT,"
				+ Translation.Fields.TRUST + " REAL NOT NULL DEFAULT 0 );";
		results[1] = "CREATE VIRTUAL TABLE " + Translation.TABLE_TEXT + " USING fts4("
				+ Translation.Fields.SENSE_KEY + "," + Translation.Fields.SYNONYMS + ");";
		return results;
	}

	public static String[] purgetSQLTables() {
		String[] results = new String[2];
		results[0] = "DROP TABLE IF EXISTS " + Translation.TABLE + ";";
		results[1] = "DROP TABLE IF EXISTS " + Translation.TABLE_TEXT + ";";
		return results;
	}

}
