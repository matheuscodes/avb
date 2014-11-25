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
package org.arkanos.avb.ui;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.arkanos.avb.R;
import org.arkanos.avb.data.LanguageSettings;
import org.arkanos.avb.data.Sense;
import org.arkanos.avb.data.Translation;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.SimpleAdapter;
import android.widget.TextView;

/**
 * Helper class to format and configure dictionary entries.
 * 
 * @version 1.0
 * @author Matheus Borges Teixeira
 */
public class DictionaryEntryHelper {
	/** Mapping origin to define connection between data and UI components **/
	private static final String[] from = new String[] { "dict_word", "dict_class", "dict_glossary", "dict_extras" };
	/** Mapping destination to define connection between data and UI components **/
	private static final int[] to = new int[] { R.id.dict_word, R.id.dict_class, R.id.dict_glossary, R.id.dict_extras };

	/**
	 * Composes a list of dictionary entries.
	 * 
	 * @param ls specifies the senses to be inserted.
	 * @param query defines a query for highlighting results.
	 * @param where specifies the application context.
	 * @return a SimpleAdapter with all given senses.
	 */
	public static SimpleAdapter buildListAdapter(List<Sense> ls, String query, Context where) {
		// TODO optimize me please... use 75% of the entire time.

		// create the grid item mapping
		List<HashMap<String, Spanned>> fillMaps = buildMaps(ls, query, where);

		// fill in the grid_item layout
		SimpleAdapter adapter = new SimpleAdapter(where, fillMaps, R.layout.dictionary_entry, from, to);
		adapter.setViewBinder(new SimpleAdapter.ViewBinder() {
			public boolean setViewValue(View view, Object data, String textRepresentation) {
				if (data instanceof Spanned && view instanceof TextView) {
					((TextView) view).setText((Spanned) data);
				} else {
					((TextView) view).setText(String.valueOf(data));
				}
				return true;
			}
		});
		return adapter;
	}

	/**
	 * Writes the data from a sense into the UI.
	 * 
	 * @param entry specifies where data will be written.
	 * @param s defines the data to be written.
	 */
	public static void fillEntry(View entry, Sense s) {
		List<Sense> ls = new LinkedList<Sense>();
		ls.add(s);
		HashMap<String, Spanned> fill_map = buildMaps(ls, "!@!", entry.getContext()).get(0);
		for (int i = 0; i < to.length; ++i) {
			TextView tv = (TextView) entry.findViewById(to[i]);
			tv.setText((fill_map.get(from[i])));
		}
	}

	/**
	 * Organizes senses data into formatted string maps.
	 * 
	 * @param ls specifies the list of senses to be organized.
	 * @param query defines the query to be highlighted.
	 * @param where specifies the application context.
	 * @return an organized list for each data item with its formatted string.
	 */
	private static List<HashMap<String, Spanned>> buildMaps(List<Sense> ls, String query, Context where) {
		List<HashMap<String, Spanned>> fillMaps = new LinkedList<HashMap<String, Spanned>>();
		for (Sense s : ls) {
			HashMap<String, Spanned> map = new HashMap<String, Spanned>(4);
			String word = s.getHead();
			String rest = s.getSynonyms().replace(word, "");
			if (rest.indexOf(',') == 0) {
				rest = rest.substring(1);
			}
			rest = rest.trim();
			map.put("dict_word", format("<img src=\"en\"/>  " + word, query, where));
			map.put("dict_class", format("<i>" + s.getGrammarClass() + "</i>", query, where));
			map.put("dict_glossary", format(s.getGlossary(), query, where));
			String extras = "";
			if (rest.length() > 0) {
				extras = "<i>" + where.getString(R.string.dict_synonyms) + ":</i> " + rest;
			}
			for (Map.Entry<String, Translation> t : s.getTranslations().entrySet()) {
				extras += "<br/><br/><img src=\"" + t.getKey() + "\"/>  " + t.getValue().getSynonyms();
			}
			map.put("dict_extras", format(extras, query, where));
			fillMaps.add(map);
		}
		return fillMaps;
	}

	/**
	 * Formats the text and highlight content based on a query.
	 * 
	 * @param s specifies the text to be formatted.
	 * @param query defines the query to be highlighted.
	 * @param where specifies the application context.
	 * @return the formatted string in a Spanned class.
	 */
	private static Spanned format(String s, String query, Context where) {
		String r = s;
		if (s.endsWith("\"")) {
			r += "</i>";
		}
		return Html.fromHtml(r
				.trim()
				.replace(" \"", " <i>\"")
				.replace("\" ", "\"</i> ")
				.replace(";", "<br/>")
				.replace(query, "<u>" + query + "</u>"), LanguageSettings.getFlags(where), null);
	}
}
