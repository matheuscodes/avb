package org.arkanos.avb.data;

import java.util.Locale;

import android.database.Cursor;

public class Sense {
	public enum GrammarClass {
		ADVERB, VERB, ADJECTIVE, NOUN;

		public static GrammarClass convert(String s) {
			// TODO remove this temporary fix.
			if (s.substring(0, 1).compareTo("r") == 0)
				return ADVERB;
			if (s.substring(0, 1).compareTo("n") == 0)
				return NOUN;
			// TODO break
			if (s.substring(0, 1).compareTo("a") == 0 || s.substring(0, 1).compareTo("s") == 0)
				return ADJECTIVE;
			if (s.substring(0, 1).compareTo("v") == 0)
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

	public enum Fields {
		SENSE, GLOSSARY, SYNONYMS, GRAMMAR_CLASS, ANTONYMS, PRIORITY;

		@Override
		public String toString() {
			return this.name().toLowerCase(Locale.getDefault());
		}
	};

	String key;
	String glossary;
	String synonyms;
	GrammarClass grammar_class;
	String antonyms;
	int priority;

	public Sense(Cursor c) {
		// String debug = "";
		key = c.getString(Fields.SENSE.ordinal());
		// debug += key + "/";
		glossary = c.getString(Fields.GLOSSARY.ordinal());
		// debug += glossary + "/";
		synonyms = c.getString(Fields.SYNONYMS.ordinal());
		// debug += synonyms + "/";
		grammar_class = GrammarClass.convert(c.getString(Fields.GRAMMAR_CLASS.ordinal()));
		// debug += c.getString(Fields.GRAMMAR_CLASS.ordinal()) + "/";
		antonyms = c.getString(Fields.ANTONYMS.ordinal());
		// debug += antonyms + "/";
		priority = c.getInt(Fields.PRIORITY.ordinal());
		// debug += priority + "";
		// Log.d("AVB-Sense", debug);
	}

	public static String createSQLTable() {
		String sql = "CREATE TABLE " + Sense.TABLE + " ("
				+ Sense.Fields.values()[0] + " TEXT PRIMARY KEY,"
				+ Sense.Fields.values()[1] + " TEXT NOT NULL,"
				+ Sense.Fields.values()[2] + " TEXT NOT NULL,"
				+ Sense.Fields.values()[3] + " TEXT NOT NULL,"
				+ Sense.Fields.values()[4] + " TEXT,"
				+ Sense.Fields.values()[5] + " INTEGER NOT NULL DEFAULT 0 );";
		return sql;
	}

	public String getString(Fields field) {
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
}
