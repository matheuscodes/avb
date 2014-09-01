package org.arkanos.avb.data;

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import android.database.Cursor;

public class Translation {

	public static final String TABLE = "translations";
	public static final String TABLE_TEXT = "translations_text";

	public static final String SENSE_KEY = "sense_key";
	public static final String SYNONYMS = "synonyms";
	public static final String TERM = "term";
	public static final String GRAMMAR = "grammar";
	public static final String TRUST = "trust";
	public static final String CONFIDENCE = "confidence";
	public static final String LANGUAGE = "language";

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

	public Translation(Cursor c, String language) {
		// String debug = "";
		this.key = c.getString(c.getColumnIndex(SENSE_KEY));
		// debug += key + "/";
		this.language = language;
		// debug += language + "/";
		this.term = c.getString(c.getColumnIndex(TERM));
		// debug += term + "/";
		this.grammar_class = c.getString(c.getColumnIndex(GRAMMAR));
		// debug += grammar_class + "/";
		this.trust = c.getFloat(c.getColumnIndex(TRUST));
		// debug += trust;
		// Log.d("AVB-Translation", debug);
	}

	public static String createSQLTable() {
		return "CREATE VIRTUAL TABLE " + Translation.TABLE_TEXT + " USING fts4("
				+ Translation.SENSE_KEY + "," + Translation.SYNONYMS + "," + Translation.LANGUAGE + ");";
	}

	public static String createSQLTable(String language) {
		return "CREATE TABLE " + Translation.TABLE + "_" + language + " ("
				+ Translation.SENSE_KEY + " TEXT,"
				+ Translation.TERM + " TEXT,"
				+ Translation.GRAMMAR + " TEXT,"
				+ Translation.CONFIDENCE + "  REAL NOT NULL DEFAULT 0,"
				+ Translation.TRUST + " REAL NOT NULL DEFAULT 0, "
				+ "PRIMARY KEY ("
				+ Translation.SENSE_KEY + ","
				+ Translation.TERM
				+ "));";
	}

	public static String purgetSQLTable() {
		return "DROP TABLE IF EXISTS " + Translation.TABLE_TEXT + ";";
	}

	public String getLanguage() {
		return language;
	}

	public void setSynonyms(String string) {
		synonyms = string;
	}

	public String getSynonyms() {
		// TODO maybe rename to "getPretty" because of the set (which expects data-data)
		if (synonyms != null) {
			return synonyms.replace(" ", ", ").replace('_', ' ');
		}
		return null;
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
