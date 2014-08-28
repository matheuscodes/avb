package org.arkanos.avb.activities;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.arkanos.avb.R;
import org.arkanos.avb.data.BabelTower;
import org.arkanos.avb.data.Dictionary;
import org.arkanos.avb.data.Sense;
import org.arkanos.avb.data.Translation;
import org.arkanos.avb.data.WordnetImporter;
import org.arkanos.avb.ui.SearchBoxHelper;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.SearchView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class Search extends ListActivity {

	WordnetImporter dictionary = null;
	BabelTower translations = null;
	SearchView search_box = null;
	String last_query = null;
	List<Sense> last_results = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dictionary_search);

		dictionary = Dictionary.loadWordnet(this);
		translations = BabelTower.prepareTranslations(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		search_box = SearchBoxHelper.activateBox(this, menu);
		if (last_query != null) {
			search_box.setQuery(last_query, false);
		}
		return true;
	}

	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			Log.d("AVB-Search", "Search request: " + query);

			// create the grid item mapping
			String[] from = new String[] { "dict_word", "dict_class", "dict_glossary", "dict_extras" };
			int[] to = new int[] { R.id.dict_word, R.id.dict_class, R.id.dict_glossary, R.id.dict_extras };

			List<Sense> ls = null;
			// FIXME This optimization makes double call not so heavy, but it still shouldn't happen.
			if (last_query != null && last_query.compareTo(query) == 0) {
				ls = last_results;
			}
			else {
				ls = Dictionary.searchSenses(query);
				ls.addAll(BabelTower.searchTranslations(query));
				if (ls != null) {
					last_query = query;
					last_results = ls;
				}
			}
			Log.d("AVB-Search", "Query executed...");
			// TODO optimize me please... use 75% of the entire time.
			List<HashMap<String, Spanned>> fillMaps = new LinkedList<HashMap<String, Spanned>>();
			for (Sense s : ls) {
				HashMap<String, Spanned> map = new HashMap<String, Spanned>(4);
				String word = s.getString(Sense.Fields.SYNONYMS);
				if (word.indexOf(' ') > 0) {
					word = word.substring(0, word.indexOf(' '));
				}
				String rest = s.getString(Sense.Fields.SYNONYMS).substring(word.indexOf(' ') + 1);
				rest = rest.trim().replace(" ", ", ");
				map.put("dict_word", format("<img src=\"en\"/>  " + word, query));
				map.put("dict_class", format("<i>" + s.getString(Sense.Fields.GRAMMAR_CLASS) + "</i>", query));
				map.put("dict_glossary", format(s.getString(Sense.Fields.GLOSSARY), query));
				String extras = "<i>" + getString(R.string.dict_synonyms) + ":</i> " + rest;
				for (Map.Entry<String, Translation> t : s.getTranslations().entrySet()) {
					extras += "<br/><br/><img src=\"" + t.getKey() + "\"/>  " + t.getValue().getContent();
				}
				map.put("dict_extras", format(extras, query));
				fillMaps.add(map);
			}

			Log.d("AVB-Search", "Search finished: " + query);
			// fill in the grid_item layout
			SimpleAdapter adapter = new SimpleAdapter(this, fillMaps, R.layout.dictionary_entry, from, to);
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
			this.setListAdapter(adapter);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
	}

	private Spanned format(String s, String query) {
		String r = s;
		if (s.endsWith("\"")) {
			r += "</i>";
		}
		return Html.fromHtml(r
				.trim()
				.replace(" \"", " <i>\"")
				.replace("\" ", "\"</i> ")
				.replace(";", "<br/>")
				.replace("_", " ")
				.replace(query, "<u>" + query + "</u>"), BabelTower.getFlags(this), null);
	}

}
