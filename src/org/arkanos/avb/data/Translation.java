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

import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.arkanos.avb.AVBApp;

import android.database.Cursor;

/**
 * Models the content of a translation for a sense.
 * 
 * @version 1.0
 * @author Matheus Borges Teixeira
 */
public class Translation {
	/** Tag for debug outputs **/
	public static final String TAG = AVBApp.TAG + "Translation";

	/** Table name where sense data is stored **/
	public static final String TABLE = "translations";
	/** Table name where search data is stored **/
	public static final String TABLE_TEXT = "translations_text";

	/** Column name for the sense key of the translation **/
	public static final String SENSE_KEY = "sense_key";
	/** Column name for the translation synonyms **/
	public static final String SYNONYMS = "synonyms";
	/** Column name for the translation term **/
	public static final String TERM = "term";
	/** Column name for the translation grammar class **/
	public static final String GRAMMAR = "grammar";
	/** Column name for the translation trust **/
	public static final String TRUST = "trust";
	/** Column name for the translation user confidence **/
	public static final String CONFIDENCE = "confidence";
	/** Column name for the translation language **/
	public static final String LANGUAGE = "language";

	/** Translation language **/
	private String language = null;
	/** Translation sense key **/
	private String key = null;
	/** Translation synonyms **/
	private String synonyms = null;
	/** Translation term **/
	private String term = null;
	/** Translation grammar class **/
	private String grammar_class = null;
	/** Translation trust, it is defined by the way the translations are compiled and associated. **/
	private float trust = 0f;
	/** Translation confidence, it expresses the user knowledge status for the translation. **/
	private float confidence = 0f;

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString() {
		return language + "/" + key + "/" + synonyms + "/" + term + "/" + grammar_class + "/" + trust;
	}

	/**
	 * Constructs an empty translation for a sense and a language.
	 * 
	 * @param key defines the sense for the empty translation.
	 * @param language defines the language for the empty translation.
	 */
	public Translation(String key, String language) {
		this.key = key;
		this.language = language;
	}

	/**
	 * Constructs a translation based on a database cursor result.
	 * 
	 * @param c defines the contents for the translation.
	 * @param language defines the language for the translation.
	 */
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
		this.confidence = c.getFloat(c.getColumnIndex(CONFIDENCE));
		// debug += confidence + "/";
		this.trust = c.getFloat(c.getColumnIndex(TRUST));
		// debug += trust;
		// Log.d(TAG, debug);
	}

	/**
	 * Gets the SQL command required to create storage tables for search data.
	 * 
	 * @return string with SQL code to execute.
	 */
	public static String createSQLTable() {
		return "CREATE VIRTUAL TABLE " + Translation.TABLE_TEXT + " USING fts4("
				+ Translation.SENSE_KEY + "," + Translation.SYNONYMS + "," + Translation.LANGUAGE + ");";
	}

	/**
	 * Gets the SQL command required to create storage tables for individual data.
	 * 
	 * @return string with SQL code to execute.
	 */
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

	/**
	 * Gets the SQL command required to remove storage tables.
	 * 
	 * @return string with SQL code to execute.
	 */
	public static String purgetSQLTable() {
		return "DROP TABLE IF EXISTS " + Translation.TABLE_TEXT + ";";
	}

	/**
	 * Gets the translation language.
	 * 
	 * @return the translation language.
	 */
	public String getLanguage() {
		return language;
	}

	/**
	 * Sets the synonyms for the translation.
	 * 
	 * @param string defines the synonyms to be set.
	 */
	public void setSynonyms(String string) {
		synonyms = string;
	}

	/**
	 * Gets the translation synonyms.
	 * 
	 * @return the translation synonyms.
	 */
	public String getPrettySynonyms() {
		if (synonyms != null) {
			return synonyms.replace(" ", ", ").replace('_', ' ');
		}
		return null;
	}

	/**
	 * Sets the term for the translation.
	 * 
	 * @param string defines the new term to be set.
	 */
	public void setTerm(String string) {
		term = string;
	}

	/**
	 * Removes additional data which is not relevant for a query.
	 * 
	 * @param query defines what information is relevant.
	 */
	public void cleanSynonyms(String query) {
		// TODO Feature#02 Break query in terms (make_out, make, out).
		String newsynonyms = "";
		String query_right = query.replace(' ', '_').toLowerCase(Locale.ENGLISH);
		for (String s : breakToTerms(synonyms)) {
			if (s.toLowerCase(Locale.ENGLISH).contains(query_right)) {
				newsynonyms += s + " ";
			}
		}
		setSynonyms(newsynonyms.trim());
		return;
	}

	/**
	 * Breaks one string to a list of terms.
	 * 
	 * @param s defines the string to be broken.
	 * @return list of individual terms inside the given string.
	 */
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

	/**
	 * Gets the translation sense key.
	 * 
	 * @return the translation sense key.
	 */
	public String getKey() {
		return key;
	}

	/**
	 * Gets the translation term.
	 * 
	 * @return the translation term.
	 */
	public String getTerm() {
		return term;
	}

	/**
	 * Gets the translation trust.
	 * 
	 * @return the translation trust.
	 */
	public float getTrust() {
		return trust;
	}

	/**
	 * Sets the translation trust.
	 * 
	 * @param value
	 */
	public void setTrust(float value) {
		trust = value;
	}

	/**
	 * Increases the current trust by a value.
	 * 
	 * @param value defines the amount to be increased.
	 */
	public void increaseTrust(float value) {
		trust += value;
	}

	/**
	 * Gets the translation grammar class.
	 * 
	 * @return the translation grammar class.
	 */
	public String getGrammar() {
		return grammar_class;
	}

	/**
	 * Gets the translation confidence.
	 * 
	 * @return the translation confidence.
	 */
	public float getConfidence() {
		return confidence;
	}

	/**
	 * Changes the existing confidence value.
	 * The previous value will always be used as part of the calculation.
	 * 
	 * @param value defines the change amount.
	 */
	public void changeConfidence(float value) {
		confidence = (confidence + value) / 2;
	}

}
