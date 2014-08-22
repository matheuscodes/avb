package org.arkanos.avb.activities;

import org.arkanos.avb.R;
import org.arkanos.avb.data.DatabaseHelper;
import org.arkanos.avb.data.Dictionary;
import org.arkanos.avb.data.Wordnet;
import org.arkanos.avb.fragments.About;
import org.arkanos.avb.ui.DictionaryLoadingDialog;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.SearchView;

public class Main extends Activity implements ActionBar.TabListener {

	private static Wordnet wordnet = null;

	Fragment[] tabs = null;

	private static synchronized Wordnet makeWordnet(Activity context) {
		if (Main.wordnet == null) {
			DictionaryLoadingDialog progressDialog = new DictionaryLoadingDialog(context);
			Dictionary d = new Dictionary(new DatabaseHelper(context));
			Main.wordnet = new Wordnet(d, progressDialog, context);
		}
		return wordnet;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		makeWordnet(this);

		setContentView(R.layout.activity_main);

		// Set up the action bar.

		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		tabs = new Fragment[1];

		/** Begin **/
		FragmentTransaction ft = getFragmentManager().beginTransaction();

		Fragment helper = new About();
		ft.add(android.R.id.content, helper, "about");
		ft.detach(helper);
		tabs[0] = helper;
		actionBar.addTab(
				actionBar
						.newTab()
						.setText(R.string.about_tab)
						.setTabListener(this));

		if (savedInstanceState != null) {
			actionBar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 0));
		}
		else {
			actionBar.setSelectedNavigationItem(0);
			ft.attach(tabs[0]);
		}

		ft.commit();
		/** End **/
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the options menu from XML
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.search, menu);

		// Associate searchable configuration with the SearchView
		SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) menu.findItem(R.id.search_box).getActionView();
		searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

		searchView.setIconified(false);

		return true;
	}

	@Override
	public void onTabReselected(Tab t, FragmentTransaction ft) {
		/** Do nothing **/
	}

	@Override
	public void onTabSelected(Tab t, FragmentTransaction ft) {
		if (tabs != null && t != null && tabs[t.getPosition()] != null) {
			if (ft != null)
				ft.attach(tabs[t.getPosition()]);
		}
	}

	@Override
	public void onTabUnselected(Tab t, FragmentTransaction ft) {
		if (tabs != null && t != null && tabs[t.getPosition()] != null) {
			if (ft != null)
				ft.detach(tabs[t.getPosition()]);
		}
	}
}
