package org.arkanos.avb.activities;

import org.arkanos.avb.R;
import org.arkanos.avb.data.BabelTower;
import org.arkanos.avb.data.Wordnet;
import org.arkanos.avb.fragments.About;
import org.arkanos.avb.fragments.LanguageSettings;
import org.arkanos.avb.ui.SearchBoxHelper;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class Main extends Activity implements ActionBar.TabListener {

	Fragment[] tabs = null;

	Wordnet dictionary = null;
	BabelTower translations = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		translations = BabelTower.prepareTranslations(this);
		BabelTower.optimize();
		Log.d("AVB-Main", "Started reading partition 18.");
		BabelTower.getBestKnown(18, BabelTower.GERMAN);
		BabelTower.getAverageKnown(18, BabelTower.GERMAN);
		BabelTower.getWorstKnown(18, BabelTower.GERMAN);
		Log.d("AVB-Main", "Finished reading partition 18.");

		Log.d("AVB-Main", "Started reading partition 90.");
		BabelTower.getBestKnown(90, BabelTower.GERMAN);
		BabelTower.getAverageKnown(90, BabelTower.GERMAN);
		BabelTower.getWorstKnown(90, BabelTower.GERMAN);
		Log.d("AVB-Main", "Finished reading partition 90.");

		setContentView(R.layout.activity_main);

		// Set up the action bar.

		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		tabs = new Fragment[2];

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

		helper = new LanguageSettings();
		ft.add(android.R.id.content, helper, "languages");
		ft.detach(helper);
		tabs[1] = helper;
		actionBar.addTab(
				actionBar
						.newTab()
						.setText(R.string.languages_tab)
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
		SearchBoxHelper.activateBox(this, menu);
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
