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

import org.arkanos.avb.R;
import org.arkanos.avb.activities.Search;

import android.app.Activity;
import android.app.SearchManager;
import android.app.SearchableInfo;
import android.content.ComponentName;
import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.SearchView;

/**
 * Helper class to configure search boxes.
 * 
 * @version 1.0
 * @author Matheus Borges Teixeira
 */
public class SearchBoxHelper {

	/**
	 * Builds a search box.
	 * 
	 * @param root specifies the activity holding the search box.
	 * @param menu defines the location to inflate content.
	 * @return the search view inside the inflated content.
	 */
	public static SearchView activateBox(Activity root, Menu menu) {
		MenuInflater inflater = root.getMenuInflater();
		inflater.inflate(R.menu.search, menu);

		SearchManager searchManager = (SearchManager) root.getSystemService(Context.SEARCH_SERVICE);
		SearchView sv = (SearchView) menu.findItem(R.id.search_box).getActionView();
		SearchableInfo si = searchManager.getSearchableInfo(new ComponentName(root, Search.class));
		sv.setSearchableInfo(si);
		sv.setIconified(false);
		sv.clearFocus();
		return sv;
	}
}
