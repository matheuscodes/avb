package org.arkanos.avb.views;

import org.arkanos.avb.R;
import org.arkanos.avb.data.DatabaseHelper;
import org.arkanos.avb.data.Dictionary;
import org.arkanos.avb.data.Wordnet;
import org.arkanos.avb.fragments.SectionsPagerAdapter;
import org.arkanos.avb.ui.DictionaryLoadingDialog;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

public class Main extends ActionBarActivity implements ActionBar.TabListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

	private static Wordnet wordnet = null;

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
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the
		// three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), Main.this);

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(
					int position) {
				actionBar
						.setSelectedNavigationItem(position);
			}
		});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter
				.getCount(); i++) {
			// Create a tab with text corresponding to the page title
			// defined by
			// the adapter. Also specify this Activity object, which
			// implements
			// the TabListener interface, as the callback (listener) for
			// when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab().setText(mSectionsPagerAdapter.getPageTitle(i)).setTabListener(Main.this));
		}
	}

	@Override
	public void onTabSelected(
			ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager
				.setCurrentItem(tab
						.getPosition());
	}

	@Override
	public void onTabUnselected(
			ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(
			ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}
}
