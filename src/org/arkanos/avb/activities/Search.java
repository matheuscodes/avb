package org.arkanos.avb.activities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.arkanos.avb.R;
import org.arkanos.avb.data.Dictionary;
import org.arkanos.avb.data.Sense;
import org.arkanos.avb.data.Wordnet;
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
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class Search extends ListActivity {

	Wordnet reference = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dictionary_search);

		reference = Dictionary.loadWordnet(this);

		// Get the intent, verify the action and get the query
		handleIntent(getIntent());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		SearchBoxHelper.activateBox(this, menu);
		return true;
	}

	private void handleIntent(Intent intent) {
		// TODO fix two runs of the search.
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			Log.d("AVB-Search", "Search request: " + query);

			// create the grid item mapping
			String[] from = new String[] { "dict_word", "dict_class", "dict_glossary", "dict_extras" };
			int[] to = new int[] { R.id.dict_word, R.id.dict_class, R.id.dict_glossary, R.id.dict_extras };

			List<Sense> ls = Dictionary.searchSenses(query);

			// prepare the list of all records
			List<HashMap<String, Spanned>> fillMaps = new ArrayList<HashMap<String, Spanned>>();
			for (Sense s : ls) {
				HashMap<String, Spanned> map = new HashMap<String, Spanned>(4);
				String word = s.getString(Sense.Fields.SYNONYMS);
				if (word.indexOf(' ') > 0) {
					word = word.substring(0, word.indexOf(' '));
				}
				String rest = s.getString(Sense.Fields.SYNONYMS).substring(word.indexOf(' ') + 1);
				rest = rest.trim().replace(" ", ", ");
				map.put("dict_word", format(word, query));
				map.put("dict_class", format("<i>" + s.getString(Sense.Fields.GRAMMAR_CLASS) + "</i>", query));
				map.put("dict_glossary", format(s.getString(Sense.Fields.GLOSSARY), query));
				map.put("dict_extras", format("<i>" + getString(R.string.dict_synonyms) + ":</i> " + rest, query));
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
				.replace(query, "<u>" + query + "</u>"));
	}

}