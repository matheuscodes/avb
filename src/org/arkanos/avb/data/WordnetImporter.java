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
package org.arkanos.avb.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.arkanos.avb.AVBApp;
import org.arkanos.avb.R;
import org.arkanos.avb.interfaces.ProgressObserver;
import org.arkanos.avb.ui.LoadingDialog;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Process responsible for importing Wordnet data.
 * 
 * WordNet® 3.0 is intelectual property of Princeton University.
 * http://wordnet.princeton.edu/wordnet/license/
 * 
 * @version 1.0
 * @author Matheus Borges Teixeira
 */
public class WordnetImporter extends AsyncTask<Void, Integer, Void> {
	/** Tag for debug outputs **/
	public static final String TAG = AVBApp.TAG + "WordnetImporter";

	/** Version of the Wordnet being loaded **/
	public static final String WN_VERSION = "3.0";
	/** Total amount of senses to be read **/
	public static final int WN_TOTAL = 117659;
	/** Total amount of nouns **/
	public static final int WN_NOUNS = 82115;
	/** Total amount of verbs **/
	public static final int WN_VERBS = 13767;
	/** Total amount of adjectives **/
	public static final int WN_ADJECTIVES = 18156;
	/** Total amount of adverbs **/
	public static final int WN_ADVERBS = 3621;

	/** Amount of items to be processed before updating progress **/
	private static final int BATCH = 100;
	/** Fixed amount of bytes to skip from the Wordnet files **/
	private static final long SKIP = 1740;

	/** Reference to the progress bar dialog **/
	private static ProgressObserver progress_observer = null;
	/** UI context where the progress dialog is contained **/
	private static Context parent = null;

	/**
	 * @see AsyncTask#onProgressUpdate(Integer...)
	 */
	@Override
	protected void onProgressUpdate(Integer... i) {
		progress_observer.replaceMessage(parent.getString(i[0]).replace("{count}", "\n" + i[1]));
		if (i.length > 2) {
			progress_observer.increaseBy(i[2].intValue());
		}
	}

	/**
	 * @see AsyncTask#doInBackground(Void...)
	 */
	@Override
	protected Void doInBackground(Void... v) {
		progress_observer.defineEnd(WN_TOTAL);
		progress_observer.defineStep(0);
		// TODO Feature#01 Add a resume function.
		Dictionary.clean();
		loadFile(WN_ADVERBS, R.raw.data_adv, R.string.load_dict_adv);
		loadFile(WN_VERBS, R.raw.data_verb, R.string.load_dict_verb);
		loadFile(WN_ADJECTIVES, R.raw.data_adj, R.string.load_dict_adj);
		loadFile(WN_NOUNS, R.raw.data_noun, R.string.load_dict_noun);
		progress_observer.replaceMessage(parent.getString(R.string.load_dict_optimize));
		Dictionary.optimize();
		return null;
	}

	/**
	 * @see AsyncTask#onPostExecute(Void)
	 */
	@Override
	protected void onPostExecute(Void v) {
		progress_observer.finishIt();
		Log.d(TAG, "Dictionary has now " + Dictionary.getSize() + " words.");
	}

	/**
	 * Processes a Wordnet file.
	 * 
	 * @param size specifies the amount of words in the file.
	 * @param file specifies the file to be opened.
	 * @param message defines the message on the progress update.
	 */
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
			reader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Constructs the process and dialog to track the progress.
	 * 
	 * @param who specifies the application context.
	 */
	public WordnetImporter(Context who) {
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
					// parent.finish();//TODO Feature#08 Handle the case user declines data loading.
					Log.d(TAG, "Loading cancelled.");
				}
			});
			ad.setPositiveButton(parent.getString(R.string.yes), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					progress_observer.startIt();
					execute();
					Log.d(TAG, "Loading started.");
				}
			});
			ad.show();
		}
		else {
			Log.d(TAG, "Dictionary is already full.");
		}
	}

	/**
	 * Parses one single sense from the Wordnet file line.
	 * 
	 * @param from defines the content to be parsed.
	 * @param to_data recipient of content to be put on individual table.
	 * @param to_text recipient of content to be put on search table.
	 */
	private static void readSense(String from, ContentValues to_data, ContentValues to_text) {
		// Log.d(TAG, from);
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

		to_text.put(Sense.SENSE, ss_type + synset_offset);

		to_text.put(Sense.GLOSSARY, glossary.trim());
		to_text.put(Sense.SYNONYMS, synonyms);

		to_data.put(Sense.SENSE, ss_type + synset_offset);
		priority += readAntonyms(processed, to_data);
		to_data.put(Sense.PRIORITY, priority);
		// Log.d(TAG, debug + " " + glossary);
	}

	/**
	 * Parses the antonym part of the sense string.
	 * 
	 * @param from defines the content to be parsed.
	 * @param to recipient for the antonyms.
	 * @return the amount of antonyms read.
	 */
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
		to.put(Sense.ANTONYMS, antonyms);
		return priority;
	}
}
