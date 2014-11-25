/**
 * Copyright (C) 2014 Matheus Borges Teixeira
 * 
 * This is a part of Arkanos Vocabulary Builder (AVB)
 * AVB is an Android application to improve vocabulary on foreign languages.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.arkanos.avb.data;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.arkanos.avb.AVBApp;

import android.database.Cursor;

/**
 * Models the content of a Wordnet sense.
 * 
 * @version 1.0
 * @author Matheus Borges Teixeira
 */
public class Sense implements Comparable<Sense> {
	/** Tag for debug outputs **/
	public static final String TAG = AVBApp.TAG + "Sense";

	// TODO Feature#06 Remove unnecessary complexity and unused code.
	public enum GrammarClass {
		ADVERB, VERB, ADJECTIVE, NOUN;

		public static GrammarClass convert(String key) {
			if (key.charAt(0) == 'r')
				return ADVERB;
			if (key.charAt(0) == 'n')
				return NOUN;
			if (key.charAt(0) == 'a' || key.charAt(0) == 's')
				return ADJECTIVE;
			if (key.charAt(0) == 'v')
				return VERB;

			return null;
		}

		@Override
		public String toString() {
			// TODO Feature#07 Move names to strings.xml
			return this.name().toLowerCase(Locale.getDefault());
		}
	};

	/** Table name where sense data is stored **/
	public static final String TABLE = "senses";
	/** Table name where search data is stored **/
	public static final String TABLE_TEXT = "senses_text";

	/** Column name for the sense key **/
	public static final String SENSE = "sense";
	/** Column name for the sense glossary **/
	public static final String GLOSSARY = "glossary";
	/** Column name for the sense synonyms **/
	public static final String SYNONYMS = "synonyms";
	/** Column name for the sense grammar class **/
	public static final String GRAMMAR_CLASS = "grammar_class";
	/** Column name for the sense antonyms **/
	public static final String ANTONYMS = "antonyms";
	/** Column name for the sense priority **/
	public static final String PRIORITY = "priority";

	/** Sense key **/
	String key = null;
	/** Sense glossary **/
	String glossary = null;
	/** Sense synonyms **/
	String synonyms = null;
	/** Sense grammar class **/
	GrammarClass grammar_class = null;
	/** Sense antonyms **/
	String antonyms = null;
	/** Sense priority **/
	int priority = 0;

	/** Query dependent relevance to be used for sorting **/
	String sort_power;

	/** Translations per language available for the sense **/
	HashMap<String, Translation> translations;

	/**
	 * Constructs a sense based on database cursor results.
	 * 
	 * @param c defines the content of the sense.
	 */
	public Sense(Cursor c) {
		translations = new HashMap<String, Translation>();
		// String debug = "";
		key = c.getString(c.getColumnIndex(SENSE));
		// debug += key + "/";
		glossary = c.getString(c.getColumnIndex(GLOSSARY));
		// debug += glossary + "/";
		synonyms = c.getString(c.getColumnIndex(SYNONYMS));
		// debug += synonyms;
		grammar_class = GrammarClass.convert(key);
		// Log.d(TAG, debug);
	}

	/**
	 * Changes priority value for the sense.
	 * 
	 * @param value defines the new priority.
	 */
	public void setPriority(int value) {
		priority = value;
	}

	/**
	 * Changes the antonyms of for the sense.
	 * 
	 * @param references defines a new set of antonyms as a set of sense keys.
	 */
	public void setAntonyms(String references) {
		antonyms = references;
	}

	/**
	 * Gathers all SQL commands required to create storage tables.
	 * 
	 * @return list of strings with SQL code to execute.
	 */
	public static String[] createSQLTables() {
		String[] results = new String[2];
		results[0] = "CREATE TABLE " + Sense.TABLE + " ("
				+ Sense.SENSE + " TEXT PRIMARY KEY,"
				+ Sense.ANTONYMS + " TEXT,"
				+ Sense.PRIORITY + " INTEGER NOT NULL DEFAULT 0 );";
		results[1] = "CREATE VIRTUAL TABLE " + Sense.TABLE_TEXT + " USING fts4("
				+ Sense.SENSE + "," + Sense.SYNONYMS + "," + Sense.GLOSSARY + ");";
		return results;
	}

	/**
	 * Gathers all SQL commands required to remove storage tables.
	 * 
	 * @return list of strings with SQL code to execute.
	 */
	public static String[] purgetSQLTables() {
		String[] results = new String[2];
		results[0] = "DROP TABLE IF EXISTS " + Sense.TABLE + ";";
		results[1] = "DROP TABLE IF EXISTS " + Sense.TABLE_TEXT + ";";
		return results;
	}

	/**
	 * Sets the query to which the sense matched to be used in sorting.
	 * 
	 * @param query specifies the query which matched the sense.
	 */
	public void setSortPower(String query) {
		sort_power = query.replace(' ', '_').toLowerCase(Locale.getDefault());
	}

	/**
	 * Attaches a new translation to the sense.
	 * 
	 * @param translation defines the translation to be attached.
	 */
	public void addTranslation(Translation translation) {
		translations.put(translation.getLanguage(), translation);
	}

	/**
	 * Gets all available translations for the sense.
	 * 
	 * @return the translations for each available language.
	 */
	public HashMap<String, Translation> getTranslations() {
		return translations;
	}

	/**
	 * Calculates the sorting value depending on the performed query.
	 * 
	 * @return a number which indicates the sense's sorting relevance.
	 */
	public int calculateSort() {
		if (this.sort_power == null) {
			return 0;
		}
		int value = 0;
		int size = sort_power.length();
		String simple_syn = synonyms.toLowerCase(Locale.getDefault());

		if (simple_syn.contains(sort_power)) {
			if (sort_power.equals(simple_syn.substring(0, size))) {
				// Log.d(TAG, "Query is head.");
				value += 20;
				if (simple_syn.length() >= size + 1 && simple_syn.substring(0, size + 1).equals(sort_power + " ")) {
					// Log.d(TAG, "Query is head and full term.");
					value += 10;
				}
				else {
					if (simple_syn.length() == size) {
						// Log.d(TAG, "Query is head and full term.");
						value += 10;
					}
				}
			}
			else {
				// Log.d(TAG, "Query is contained.");
				value += 10;
				if (simple_syn.contains(sort_power + " ")) {
					// Log.d(TAG, "Query is contained in full term.");
					value += 5;
				}
			}
		}

		for (Map.Entry<String, Translation> t : translations.entrySet()) {
			String translation = t.getValue().getPrettySynonyms();
			if (translation.contains(sort_power)) {
				if (translation.contains(sort_power + " ")) {
					// Log.d(TAG, "Translation is contained.");
					value += 5;
				}
				else {
					// Log.d(TAG, "Translation is partial.");
					value += 1;
				}
			}
		}

		if (glossary.contains(sort_power)) {
			value += 1;
		}

		return value;
	}

	/**
	 * @see Comparable#compareTo(Object)
	 */
	@Override
	public int compareTo(Sense s) {
		int first = this.calculateSort();
		int second = s.calculateSort();
		return first - second;
	}

	/**
	 * Gets the main word for the sense.
	 * 
	 * @return the main word for the sense.
	 */
	public String getPrettyHead() {
		if (synonyms != null && synonyms.indexOf(' ') > 0) {
			return synonyms.substring(0, synonyms.indexOf(' ')).replace(" ", ", ").replace('_', ' ');
		}
		else if (synonyms != null) {
			return synonyms.replace(" ", ", ").replace('_', ' ');
		}
		return null;
	}

	/**
	 * Gets the key for the sense.
	 * 
	 * @return the key for the sense.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Gets the synonyms for the sense.
	 * 
	 * @return the synonyms for the sense.
	 */
	public String getSynonyms() {
		return synonyms.replace(" ", ", ").replace('_', ' ');
	}

	/**
	 * Gets the grammar class for the sense.
	 * 
	 * @return the grammar class for the sense.
	 */
	public String getGrammarClass() {
		return grammar_class.toString();
	}

	/**
	 * Gets the glossary for the sense.
	 * 
	 * @return the glossary for the sense.
	 */
	public String getGlossary() {
		return glossary;
	}
}
