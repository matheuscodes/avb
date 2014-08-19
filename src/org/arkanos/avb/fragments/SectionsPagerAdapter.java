package org.arkanos.avb.fragments;

import java.util.Locale;

import org.arkanos.all.R;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to one
 * of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {
	private Context who;

	public SectionsPagerAdapter(FragmentManager fm, Context c) {
		super(fm);
		this.who = c;
	}

	@Override
	public Fragment getItem(int position) {
		// getItem is called to instantiate the fragment for the given page.
		// Return a PlaceholderFragment (defined as a static inner class
		// below).
		return PlaceholderFragment.newInstance(position + 1);
	}

	@Override
	public int getCount() {
		// Show 3 total pages.
		return 3;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		Locale l = Locale.getDefault();
		switch (position) {
		case 0:
			return who.getString(R.string.title_section3).toUpperCase(l);
		case 1:
			return who.getString(R.string.title_section2).toUpperCase(l);
		case 2:
			return who.getString(R.string.about_tab).toUpperCase(l);
		}
		return null;
	}
}
