package org.arkanos.avb.activities;

import java.util.Collections;
import java.util.List;

import org.arkanos.avb.R;
import org.arkanos.avb.data.BabelTower;
import org.arkanos.avb.data.Dictionary;
import org.arkanos.avb.data.Sense;
import org.arkanos.avb.ui.DictionaryEntryHelper;
import org.arkanos.avb.ui.SearchBoxHelper;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.SearchView;

public class Search extends ListActivity {

	SearchView search_box = null;
	String last_query = null;
	List<Sense> last_results = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dictionary_search);

		handleIntent(getIntent());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		search_box = SearchBoxHelper.activateBox(this, menu);
		if (last_query != null) {
			search_box.setQuery(last_query, false);
		}
		return true;
	}

	// TODO check if sync required
	private synchronized void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			Log.d("AVB-Search", "Search request: " + query);

			List<Sense> ls = null;
			// FIXME This optimization makes double call not so heavy, but it still shouldn't happen.
			if (last_results != null && last_query != null && last_query.compareTo(query) == 0) {
				ls = last_results;
			}
			else {
				ls = Dictionary.searchSenses(query);
				ls.addAll(BabelTower.searchTranslations(query));
				if (ls != null) {
					last_query = query;
					Collections.sort(ls, Collections.reverseOrder());
					last_results = ls;
				}
			}

			Log.d("AVB-Search", "Query executed...");

			this.setListAdapter(DictionaryEntryHelper.buildListAdapter(ls, query, this));

			Log.d("AVB-Search", "Search finished: " + query);
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
	}
}
