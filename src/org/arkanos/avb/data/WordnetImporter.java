package org.arkanos.avb.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.arkanos.avb.R;
import org.arkanos.avb.interfaces.ProgressObserver;
import org.arkanos.avb.ui.LoadingDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

public class WordnetImporter extends AsyncTask<Void, Integer, Void> {

	public static final String WN_VERSION = "3.0";
	public static final int WN_TOTAL = 117659;
	public static final int WN_NOUNS = 82115;
	public static final int WN_VERBS = 13767;
	public static final int WN_ADJECTIVES = 18156;
	public static final int WN_ADVERBS = 3621;

	private static final int BATCH = 100;
	private static final long SKIP = 1740;

	private static ProgressObserver progress_observer = null;
	private static Activity parent = null;

	@Override
	protected void onProgressUpdate(Integer... i) {
		progress_observer.replaceMessage(parent.getString(i[0]).replace("{count}", "\n" + i[1]));
		if (i.length > 2) {
			progress_observer.increaseBy(i[2].intValue());
		}
	}

	@Override
	protected Void doInBackground(Void... v) {
		synchronized (this) { // TODO remove this
			progress_observer.defineEnd(WN_TOTAL);
			progress_observer.defineStep(0);
			// TODO add a resume feature
			Dictionary.clean(); // TODO remove this
			loadFile(WN_ADVERBS, R.raw.data_adv, R.string.load_dict_adv);
			loadFile(WN_VERBS, R.raw.data_verb, R.string.load_dict_verb);
			loadFile(WN_ADJECTIVES, R.raw.data_adj, R.string.load_dict_adj);
			loadFile(WN_NOUNS, R.raw.data_noun, R.string.load_dict_noun);
			progress_observer.replaceMessage(parent.getString(R.string.load_dict_optimize));
			Dictionary.optimize();
			return null;
		}
	}

	@Override
	protected void onPostExecute(Void v) {
		progress_observer.finishIt();
		Log.d("AVB-Wordnet", "Dictionary has now " + Dictionary.getSize() + " words.");
	}

	private void loadFile(int size, int file, int message) {
		try {
			int total = size;
			progress_observer.increaseStepBy(size);
			InputStream in = parent.getResources().openRawResource(file);
			BufferedReader reader = new BufferedReader(new InputStreamReader(in));
			String s;
			reader.skip(WordnetImporter.SKIP);
			s = reader.readLine();
			publishProgress(message, total);
			while (s != null) {
				int count = 0;

				while (s != null && count++ < BATCH) {
					ContentValues data = new ContentValues();
					ContentValues text = new ContentValues();
					readSense(s, data, text);
					Dictionary.addSense(data, text);
					s = reader.readLine();
				}
				total -= BATCH;
				publishProgress(message, total, BATCH);
			}
			reader.close(); // TODO check if ok.
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public WordnetImporter(Activity who) {
		progress_observer = new LoadingDialog(who);
		progress_observer.replaceTitle(who.getString(R.string.load_dict));
		progress_observer.replaceMessage(who.getString(R.string.load_dict_start));
		parent = who;
		if (Dictionary.getSize() < WN_TOTAL) {
			AlertDialog.Builder ad = new AlertDialog.Builder(parent);
			ad.setTitle(R.string.load_dict_missing);
			ad.setMessage(parent.getString(R.string.load_dict_confirm));
			ad.setCancelable(false);
			ad.setNegativeButton(parent.getString(R.string.no), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					progress_observer.finishIt();
					parent.finish();
					Log.d("AVB-Wordnet", "Loading cancelled.");
				}
			});
			ad.setPositiveButton(parent.getString(R.string.yes), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					progress_observer.startIt();
					execute();
					Log.d("AVB-Wordnet", "Loading started.");
				}
			});
			ad.show();
		}
		else {
			Log.d("AVB-Wordnet", "Dictionary is already full.");
		}
	}

	private static void readSense(String from, ContentValues to_data, ContentValues to_text) {
		// Log.d("AVB-Wordnet", from);
		String processed = from;
		int priority = 0;
		// String debug = "";
		String glossary = from.substring(from.indexOf('|') + 1);

		String synset_offset = processed.substring(0, processed.indexOf(' '));
		processed = processed.substring(processed.indexOf(' ') + 1); // next
		// debug += "<" + synset_offset + ">";

		processed = processed.substring(processed.indexOf(' ') + 1); // skip
																		// lex_filenum

		String ss_type = processed.substring(0, processed.indexOf(' '));
		processed = processed.substring(processed.indexOf(' ') + 1); // next
		// debug += "<" + ss_type + ">";

		String w_cnt = processed.substring(0, processed.indexOf(' '));
		processed = processed.substring(processed.indexOf(' ') + 1); // next
		// debug += "<" + w_cnt + ">";

		String synonyms = null;
		int count = Integer.valueOf(w_cnt, 16);
		priority += count;
		for (int i = 0; i < count; ++i) {
			String word = processed.substring(0, processed.indexOf(' '));
			if (synonyms != null) {
				synonyms += " " + word;
			}
			else {
				synonyms = word;
			}
			processed = processed.substring(processed.indexOf(' ') + 1); // next
			processed = processed.substring(processed.indexOf(' ') + 1); // skip
																			// lex_id
		}
		// debug += "<" + synonyms + ">";
		// TODO test fix
		// TODO review priority (too high)
		to_text.put(Sense.Fields.SENSE.toString(), ss_type + synset_offset);

		to_text.put(Sense.Fields.GLOSSARY.toString(), glossary.trim());
		to_text.put(Sense.Fields.SYNONYMS.toString(), synonyms);

		to_data.put(Sense.Fields.SENSE.toString(), ss_type + synset_offset);
		priority += readAntonyms(processed, to_data);
		to_data.put(Sense.Fields.PRIORITY.toString(), priority);
		// Log.d("AVB-Wordnet", debug + " " + glossary);
	}

	private static int readAntonyms(String from, ContentValues to) {
		String processed = from;
		int priority = 0;
		String p_cnt = processed.substring(0, processed.indexOf(' '));
		processed = processed.substring(processed.indexOf(' ') + 1); // next

		String antonyms = null;
		int count = Integer.valueOf(p_cnt);
		for (int i = 0; i < count; ++i) {
			String pointer_symbol = processed.substring(0, processed.indexOf(' '));
			processed = processed.substring(processed.indexOf(' ') + 1); // next
			String p_synset_offset = processed.substring(0, processed.indexOf(' '));
			processed = processed.substring(processed.indexOf(' ') + 1); // next
			String pos = processed.substring(0, processed.indexOf(' '));
			processed = processed.substring(processed.indexOf(' ') + 1); // next
			processed = processed.substring(processed.indexOf(' ') + 1); // skip
																			// source/target
			if (pointer_symbol.compareTo("!") == 0) {

				if (antonyms != null) {
					antonyms += " " + pos + p_synset_offset;
					++priority;
				}
				else {
					antonyms = pos + p_synset_offset;
					++priority;
				}
			}
		}
		to.put(Sense.Fields.ANTONYMS.toString(), antonyms);
		return priority;
	}
}
