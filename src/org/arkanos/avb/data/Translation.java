package org.arkanos.avb.data;

import java.util.LinkedList;
import java.util.List;
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

	@Override
	public String toString() {
		return language + "/" + key + "/" + synonyms + "/" + term + "/" + grammar_class + "/" + trust;
	}

	public Translation(String key, String language) {
		this.key = key;
		this.language = language;
	}

	public Translation(Cursor c) {
		// String debug = "";
		key = c.getString(c.getColumnIndex(Fields.SENSE_KEY.toString()));
		// debug += key + "/";
		language = c.getString(c.getColumnIndex(Fields.LANGUAGE.toString()));
		// debug += language + "/";
		term = c.getString(c.getColumnIndex(Fields.TERM.toString()));
		// debug += term + "/";
		grammar_class = c.getString(c.getColumnIndex(Fields.GRAMMAR_CLASS.toString()));
		// debug += grammar_class + "/";
		trust = c.getFloat(c.getColumnIndex(Fields.TRUST.toString()));
		// debug += trust;
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

	public void setSynonyms(String string) {
		synonyms = string;
	}

	public String getSynonyms() {
		return synonyms;
	}

	public void setTerm(String string) {
		term = string;
	}

	public void cleanSynonyms(String query) {
		// TODO check for each term of the query as well make_out, make, out...
		String newsynonyms = "";
		String query_right = query.replace(' ', '_').toLowerCase(Locale.getDefault());
		for (String s : breakToTerms(synonyms)) {
			if (s.toLowerCase(Locale.getDefault()).contains(query_right)) {
				newsynonyms += s + " ";
			}
		}
		setSynonyms(newsynonyms.trim());
		return;
	}

	private static List<String> breakToTerms(String s) {
		List<String> results = new LinkedList<String>();
		String processed = s.trim();
		while (processed.length() > 0) {
			if (processed.indexOf(' ') > 0) {
				results.add(processed.substring(0, processed.indexOf(' ')));
				processed = processed.substring(processed.indexOf(' ') + 1);
			}
			else {
				results.add(processed);
				processed = "";
			}
			processed = processed.trim();
		}
		return results;
	}

	public String getKey() {
		return key;
	}

	public String getTerm() {
		return term.replace('_', ' ');
	}

	public float getTrust() {
		return trust;
	}

	public void setTrust(float value) {
		trust = value;
	}

	public void increaseTrust(float value) {
		trust += value;
	}

	public String getGrammar() {
		return grammar_class;
	}

}
