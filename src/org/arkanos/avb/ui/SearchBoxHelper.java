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

public class SearchBoxHelper {

	public static void activateBox(Activity root, Menu menu) {
		MenuInflater inflater = root.getMenuInflater();
		inflater.inflate(R.menu.search, menu);

		SearchManager searchManager = (SearchManager) root.getSystemService(Context.SEARCH_SERVICE);
		SearchView sv = (SearchView) menu.findItem(R.id.search_box).getActionView();
		SearchableInfo si = searchManager.getSearchableInfo(new ComponentName(root, Search.class));
		sv.setSearchableInfo(si);
		sv.setIconified(false);
		return;
	}

}
