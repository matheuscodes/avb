package org.arkanos.avb.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.arkanos.avb.R;
import org.arkanos.avb.interfaces.ProgressObserver;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;

public class Wordnet extends AsyncTask<Void, Void, Void> {

	public static final String WN_VERSION = "3.0";
	public static final int WN_TOTAL = 117659;
	public static final int WN_NOUNS = 82115;
	public static final int WN_VERBS = 13767;
	public static final int WN_ADJECTIVES = 18156;
	public static final int WN_ADVERBS = 3621;

	private static final int BATCH = 100;

	private ProgressObserver progress_observer = null;
	private Activity parent = null;

	@Override
	protected Void doInBackground(Void... v) {
		synchronized (this) {
			progress_observer.defineEnd(WN_TOTAL);
			progress_observer.defineStep(0);
			loadFile(WN_ADVERBS, R.raw.data_adv, R.string.load_dict_adv);
			loadFile(WN_VERBS, R.raw.data_verb, R.string.load_dict_verb);
			loadFile(WN_ADJECTIVES, R.raw.data_adj, R.string.load_dict_adj);
			loadFile(WN_NOUNS, R.raw.data_noun, R.string.load_dict_noun);
			return null;
		}
	}

	@Override
	protected void onPostExecute(Void v)
	{
		progress_observer.finishIt();
	}

	private void loadFile(int size, int file, int message) {
		try {
			int total = size;
			progress_observer.increaseStepBy(size);
			InputStream in = parent.getResources().openRawResource(file);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String s;
			s = reader.readLine();
			while (s != null) {
				int count = 0;
				progress_observer.replaceMessage(parent.getString(message).replace("{count}", "" + total));
				while (s != null && count++ < BATCH) {
					// Log.d("AVB-Wordnet", s);
					s = reader.readLine();
				}
				progress_observer.increaseBy(BATCH);
				total -= BATCH;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public Wordnet(Dictionary d, ProgressObserver po, Activity who) {
		if (d.getSize() < WN_TOTAL) {
			progress_observer = po;
			parent = who;
			this.execute();
			Log.d("AVB-Wordnet", "Loading started.");
		}
		else {
			Log.d("AVB-Wordnet", "Dictionary is already full.");
		}
	}

	public static void readAdverb(String from) {

	}

}
