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

import org.arkanos.avb.AVBApp;
import org.arkanos.avb.R;
import org.arkanos.avb.data.BabelTower;
import org.arkanos.avb.data.Dictionary;
import org.arkanos.avb.data.LanguageSettings;
import org.arkanos.avb.fragments.About;
import org.arkanos.avb.fragments.Languages;
import org.arkanos.avb.fragments.Stats;
import org.arkanos.avb.ui.SearchBoxHelper;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Menu;

/**
 * Main activity that routes to all functionalities.
 * 
 * @version 1.0
 * @author Matheus Borges Teixeira
 */
public class Main extends Activity implements ActionBar.TabListener {

	Fragment[] tabs = null;

	/**
	 * @see Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// FIXME Groom#02 Done in the launch activity because of the dialogs first time. (see Feature#04)
		if (AVBApp.failedInitialize()) {
			LanguageSettings.initialize(this);
			Dictionary.initialize(this);
			BabelTower.initialize(this);
		}

		// Set up the action bar.
		final ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		tabs = new Fragment[3];

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

		helper = new Languages();
		ft.add(android.R.id.content, helper, "languages");
		ft.detach(helper);
		tabs[1] = helper;
		actionBar.addTab(
				actionBar
						.newTab()
						.setText(R.string.languages_tab)
						.setTabListener(this));

		helper = new Stats();
		ft.add(android.R.id.content, helper, "stats");
		ft.detach(helper);
		tabs[2] = helper;
		actionBar.addTab(
				actionBar
						.newTab()
						.setText(R.string.stats_tab)
						.setTabListener(this));

		if (savedInstanceState != null) {
			actionBar.setSelectedNavigationItem(savedInstanceState.getInt("tab", 2));
		}
		else {
			actionBar.setSelectedNavigationItem(2);
			ft.attach(tabs[2]);
		}

		ft.commit();
		/** End **/
	}

	/**
	 * @see Activity#onSavedInstanceState(Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("tab", getActionBar().getSelectedNavigationIndex());
	}

	/**
	 * @see Activity#onCreateOptionsMenu(Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		SearchBoxHelper.activateBox(this, menu);
		return true;
	}

	/**
	 * @see ActionBar.TabListener#onTabReselected(Tab, FragmentTransaction)
	 */
	@Override
	public void onTabReselected(Tab t, FragmentTransaction ft) {
		/** Do nothing **/
	}

	/**
	 * @see ActionBar.TabListener#onTabSelected(Tab, FragmentTransaction)
	 */
	@Override
	public void onTabSelected(Tab t, FragmentTransaction ft) {
		if (tabs != null && t != null && tabs[t.getPosition()] != null) {
			if (ft != null)
				ft.attach(tabs[t.getPosition()]);
		}
	}

	/**
	 * @see ActionBar.TabListener#onTabUnselected(Tab, FragmentTransaction)
	 */
	@Override
	public void onTabUnselected(Tab t, FragmentTransaction ft) {
		if (tabs != null && t != null && tabs[t.getPosition()] != null) {
			if (ft != null)
				ft.detach(tabs[t.getPosition()]);
		}
	}
}
