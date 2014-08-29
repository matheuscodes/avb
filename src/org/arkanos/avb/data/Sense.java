package org.arkanos.avb.data;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import android.database.Cursor;

public class Sense implements Comparable<Sense> {
	// TODO maybe remove unnecessary complexity
	public enum GrammarClass {
		ADVERB, VERB, ADJECTIVE, NOUN;

		public static GrammarClass convert(String key) {
			if (key.charAt(0) == 'r')
				return ADVERB;
			if (key.charAt(0) == 'n')
				return NOUN;
			// TODO break
			if (key.charAt(0) == 'a' || key.charAt(0) == 's')
				return ADJECTIVE;
			if (key.charAt(0) == 'v')
				return VERB;

			return null;
		}

		@Override
		public String toString() {
			// TODO move names to @string
			return this.name().toLowerCase(Locale.getDefault());
		}
	};

	public static final String TABLE = "senses";
	public static final String TABLE_TEXT = "senses_text";

	public enum Fields {
		SENSE, GLOSSARY, SYNONYMS, GRAMMAR_CLASS, ANTONYMS, PRIORITY;

		@Override
		public String toString() {
			return this.name().toLowerCase(Locale.getDefault());
		}
	};

	// TODO defaults
	String key;
	String glossary;
	String synonyms;
	GrammarClass grammar_class;
	String antonyms;
	int priority;

	String sort_power;

	HashMap<String, Translation> translations;

	public Sense(Cursor c) {
		translations = new HashMap<String, Translation>();
		// String debug = "";
		key = c.getString(c.getColumnIndex(Fields.SENSE.toString()));
		// debug += key + "/";
		glossary = c.getString(c.getColumnIndex(Fields.GLOSSARY.toString()));
		// debug += glossary + "/";
		synonyms = c.getString(c.getColumnIndex(Fields.SYNONYMS.toString()));
		// debug += synonyms;
		grammar_class = GrammarClass.convert(key);
		// Log.d("AVB-Sense", debug);
	}

	public void setPriority(int value) {
		priority = value;
	}

	public void setAntonyms(String references) {
		antonyms = references;
	}

	public static String[] createSQLTables() {
		String[] results = new String[2];
		results[0] = "CREATE TABLE " + Sense.TABLE + " ("
				+ Sense.Fields.SENSE + " TEXT PRIMARY KEY,"
				+ Sense.Fields.ANTONYMS + " TEXT,"
				+ Sense.Fields.PRIORITY + " INTEGER NOT NULL DEFAULT 0 );";
		results[1] = "CREATE VIRTUAL TABLE " + Sense.TABLE_TEXT + " USING fts4("
				+ Sense.Fields.SENSE + "," + Sense.Fields.SYNONYMS + "," + Sense.Fields.GLOSSARY + ");";
		return results;
	}

	public String getString(Fields field) {
		// TODO remove this and make getters
		switch (field) {
		case SENSE:
			return key;
		case GLOSSARY:
			return glossary;
		case SYNONYMS:
			return synonyms;
		case GRAMMAR_CLASS:
			return grammar_class.toString();
		case ANTONYMS:
			return antonyms;
		case PRIORITY:
			return priority + "";
		default:
			return null;
		}
	}

	public static String[] purgetSQLTables() {
		String[] results = new String[2];
		results[0] = "DROP TABLE IF EXISTS " + Sense.TABLE + ";";
		results[1] = "DROP TABLE IF EXISTS " + Sense.TABLE_TEXT + ";";
		return results;
	}

	public void setSortPower(String query) {
		sort_power = query.replace(' ', '_').toLowerCase(Locale.getDefault());
	}

	public void addTranslation(Translation translation) {
		translations.put(translation.getLanguage(), translation);
	}

	public HashMap<String, Translation> getTranslations() {
		return translations;
	}

	public int calculateSort() {
		if (this.sort_power == null) {
			return 0;
		}
		int value = 0;
		int size = sort_power.length();
		String simple_syn = synonyms.toLowerCase(Locale.getDefault());

		if (simple_syn.contains(sort_power)) {
			if (sort_power.equals(simple_syn.substring(0, size))) {
				// Log.d("AVB-Sense", "Query is head.");
				value += 20;
				if (simple_syn.length() >= size + 1 && simple_syn.substring(0, size + 1).equals(sort_power + " ")) {
					// Log.d("AVB-Sense", "Query is head and full term.");
					value += 10;
				}
				else {
					if (simple_syn.length() == size) {
						// Log.d("AVB-Sense", "Query is head and full term.");
						value += 10;
					}
				}
			}
			else {
				// Log.d("AVB-Sense", "Query is contained.");
				value += 10;
				if (simple_syn.contains(sort_power + " ")) {
					// Log.d("AVB-Sense", "Query is contained in full term.");
					value += 5;
				}
			}
		}

		for (Map.Entry<String, Translation> t : translations.entrySet()) {
			String translation = t.getValue().getSynonyms();
			if (translation.contains(sort_power)) {
				if (translation.contains(sort_power + " ")) {
					// Log.d("AVB-Sense", "Translation is contained.");
					value += 5;
				}
				else {
					// Log.d("AVB-Sense", "Translation is partial.");
					value += 1;
				}
			}
		}

		if (glossary.contains(sort_power)) {
			value += 1;
		}

		return value;
	}

	@Override
	public int compareTo(Sense s) {
		int first = this.calculateSort();
		int second = s.calculateSort();
		return first - second;
	}

	public String getHead() {
		if (synonyms != null && synonyms.indexOf(' ') > 0) {
			return synonyms.substring(0, synonyms.indexOf(' '));
		}
		return synonyms;
	}
}
