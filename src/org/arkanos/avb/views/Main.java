package org.arkanos.avb.views;

import org.arkanos.avb.R;
import org.arkanos.avb.fragments.SectionsPagerAdapter;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		new LoadViewTask().execute();
	}

	@Override
	public boolean onCreateOptionsMenu(
			Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater()
				.inflate(
						R.menu.main,
						menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(
			MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item
				.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super
				.onOptionsItemSelected(item);
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

	// A ProgressDialog object
	private ProgressDialog progressDialog;

	// To use the AsyncTask, it must be subclassed
	private class LoadViewTask extends AsyncTask<Void, Integer, Void>
	{
		// Before running code in separate thread
		@Override
		protected void onPreExecute()
		{
			// Create a new progress dialog
			progressDialog = new ProgressDialog(Main.this);
			// Set the progress dialog to display a horizontal progress bar
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			// Set the dialog title to 'Loading...'
			progressDialog.setTitle("Loading...");
			// Set the dialog message to 'Loading application View, please
			// wait...'
			progressDialog.setMessage("Loading application View, please wait...");
			// This dialog can't be canceled by pressing the back key
			progressDialog.setCancelable(false);
			// This dialog isn't indeterminate
			progressDialog.setIndeterminate(false);
			// The maximum number of items is 100
			progressDialog.setMax(100);
			// Set the current progress to zero
			progressDialog.setProgress(0);
			// Display the progress dialog
			progressDialog.show();
		}

		// The code to be executed in a background thread.
		@Override
		protected Void doInBackground(Void... params)
		{
			/*
			 * This is just a code that delays the thread execution 4 times,
			 * during 850 milliseconds and updates the current progress. This is
			 * where the code that is going to be executed on a background
			 * thread must be placed.
			 */
			try
			{
				// Get the current thread's token
				synchronized (this)
				{
					// Initialize an integer (that will act as a counter) to
					// zero
					int counter = 0;
					// While the counter is smaller than four
					while (counter <= 4)
					{
						// Wait 850 milliseconds
						this.wait(2000);
						// Increment the counter
						counter++;
						// Set the current progress.
						// This value is going to be passed to the
						// onProgressUpdate() method.
						publishProgress(counter * 25);
					}
				}
			} catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			return null;
		}

		// Update the progress
		@Override
		protected void onProgressUpdate(Integer... values)
		{
			// set the current progress of the progress dialog
			progressDialog.setMessage("Loading " + values[0] + ", please wait...");

			progressDialog.setProgress(values[0]);
		}

		// after executing the code in the thread
		@Override
		protected void onPostExecute(Void result)
		{
			// close the progress dialog
			progressDialog.dismiss();
			setContentView(R.layout.activity_main);

			// Set up the action bar.
			final ActionBar actionBar = getSupportActionBar();
			actionBar
					.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

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
				actionBar
						.addTab(actionBar
								.newTab()
								.setText(
										mSectionsPagerAdapter
												.getPageTitle(i))
								.setTabListener(
										Main.this));
			}
		}
	}
}
