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

public class DictionaryEntryHelper {
	private static final String[] from = new String[] { "dict_word", "dict_class", "dict_glossary", "dict_extras" };
	private static final int[] to = new int[] { R.id.dict_word, R.id.dict_class, R.id.dict_glossary, R.id.dict_extras };

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

	public static void fillEntry(View entry, Sense s) {
		List<Sense> ls = new LinkedList<Sense>();
		ls.add(s);
		// TODO check if this is not a problem ""
		HashMap<String, Spanned> fill_map = buildMaps(ls, "!@!", entry.getContext()).get(0);
		for (int i = 0; i < to.length; ++i) {
			TextView tv = (TextView) entry.findViewById(to[i]);
			tv.setText((fill_map.get(from[i])));
		}
	}

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
			map.put("dict_class", format("<i>" + s.getString(Sense.Fields.GRAMMAR_CLASS) + "</i>", query, where));
			map.put("dict_glossary", format(s.getString(Sense.Fields.GLOSSARY), query, where));
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
