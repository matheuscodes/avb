package org.arkanos.avb.data;

import java.util.HashMap;
import java.util.Locale;

import android.database.Cursor;

public class Sense {
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

	public int sortValue(String query) {
		int sort = 0;

		// Head of the sense chain
		if (synonyms.substring(0, query.length()).compareTo(query) == 0) {
			sort += 10;
		}

		// int count = 0;
		String processed = synonyms;
		while (processed.contains(query)) {
			// count++;// TODO finish
		}

		return sort;
	}

	public void addTranslation(Translation translation) {
		translations.put(translation.getLanguage(), translation);
	}

	public HashMap<String, Translation> getTranslations() {
		return translations;
	}
}
