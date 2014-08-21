package org.arkanos.avb.data;

import java.util.Locale;

import android.database.Cursor;

public class Sense {
	public enum GrammarClass {
		ADVERB, VERB, ADJECTIVE, NOUN
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
	float priority;

	public Sense(Cursor c) {
		key = c.getString(Fields.SENSE.ordinal());
		glossary = c.getString(Fields.GLOSSARY.ordinal());
		synonyms = c.getString(Fields.SYNONYMS.ordinal());
		grammar_class = GrammarClass.valueOf(c.getString(Fields.GRAMMAR_CLASS.ordinal()));
		antonyms = c.getString(Fields.ANTONYMS.ordinal());
		priority = c.getFloat(Fields.PRIORITY.ordinal());
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
}
