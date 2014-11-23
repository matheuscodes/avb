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
package org.arkanos.avb.activities;

import java.util.Collections;
import java.util.List;

import org.arkanos.avb.AVBApp;
import org.arkanos.avb.R;
import org.arkanos.avb.data.BabelTower;
import org.arkanos.avb.data.Dictionary;
import org.arkanos.avb.data.Sense;
import org.arkanos.avb.ui.DictionaryEntryHelper;
import org.arkanos.avb.ui.SearchBoxHelper;

import android.app.Activity;
import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.SearchView;

/**
 * Activity to display search results and search further.
 * 
 * @version 1.0
 * @author Matheus Borges Teixeira
 */
public class Search extends ListActivity {

	/** Reference to the query box **/
	SearchView search_box = null;
	/** Saved last query to avoid repeated search **/
	String last_query = null;
	/** Last results to be used in case of repeated search **/
	List<Sense> last_results = null;

	/**
	 * @see Activity#onCreate(Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dictionary_search);

		handleIntent(getIntent());
	}

	/**
	 * @see Activity#onCreateOptionsMenu(Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		search_box = SearchBoxHelper.activateBox(this, menu);
		if (last_query != null) {
			search_box.setQuery(last_query, false);
		}
		return true;
	}

	/**
	 * Creates the activity based on a given intent.
	 * 
	 * @param intent defines the search query which requested the activity.
	 */
	private void handleIntent(Intent intent) {
		if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
			String query = intent.getStringExtra(SearchManager.QUERY);
			Log.d(AVBApp.TAG + "Search", "Search request: " + query);

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

			Log.d(AVBApp.TAG + "Search", "Query executed...");

			this.setListAdapter(DictionaryEntryHelper.buildListAdapter(ls, query, this));

			Log.d(AVBApp.TAG + "Search", "Search finished: " + query);
		}
	}

	/**
	 * @see Activity#onNewIntent(Intent)
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		handleIntent(intent);
	}
}
