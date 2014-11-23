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

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Process responsible for importing translation data.
 * 
 * @version 1.0
 * @author Matheus Borges Teixeira
 */
public class TranslationImporter extends AsyncTask<Void, Integer, Void> {
	/** Tag for debug outputs **/
	public static final String TAG = AVBApp.TAG + "TranslationImporter";

	/** Amount of items to be processed before updating progress **/
	static private final Integer BATCH = Integer.valueOf(100);
	/** Specification for the file path **/
	static private final String FILE_NAME = "languages/data_";

	/** Reference to the progress bar dialog **/
	private static ProgressObserver dialog;
	/** Language being loaded **/
	private String language;
	/** Title for the dialog **/
	private String title = null;
	/** Message for the dialog **/
	private String message = null;
	/** UI context where the progress dialog is contained **/
	private Context parent = null;

	/**
	 * Constructs the process and dialog to track progress.
	 * 
	 * @param l defines the language which will be loaded.
	 * @param c specifies the application context.
	 */
	public TranslationImporter(String l, Context c) {
		dialog = new LoadingDialog(c);
		title = c.getString(R.string.load_translation).replace("{language}", LanguageSettings.prettyName(l, c));
		message = c.getString(R.string.load_translation_start);
		language = l;
		parent = c;
	}

	/**
	 * @see AsyncTask#onPreExecute()
	 */
	@Override
	protected void onPreExecute() {
		dialog.replaceTitle(title);
		dialog.replaceMessage(parent.getString(R.string.load_translation_clean));
		dialog.startIt();
		// FIXME the dialog does not appear while cleaning.
		BabelTower.clean(language);
		dialog.replaceMessage(message);
	}

	/**
	 * @see AsyncTask#onProgressUpdate(Integer...)
	 */
	@Override
	protected void onProgressUpdate(Integer... i) {
		dialog.replaceMessage(message.replace("{count}", "" + i[0].intValue()));
		if (i.length > 1) {
			dialog.increaseBy(i[1].intValue());
		}
		// TODO Set message when optimizing.
	}

	/**
	 * @see AsyncTask#doInBackground(Void...)
	 */
	@Override
	protected Void doInBackground(Void... arg0) {
		// Only one importer shall run at a time.
		Log.d(TAG, "Started importing " + language);
		// TODO this desperately needs optimization, maybe turn off-on indexes.
		synchronized (dialog) {
			message = parent.getString(R.string.load_translation_text);
			try {
				BabelTower.prepare(language);
				AssetManager am = parent.getAssets();
				InputStream in = am.open(FILE_NAME + language);
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String temp = reader.readLine();
				int total = Integer.parseInt(temp);
				dialog.defineEnd(total);
				publishProgress(Integer.valueOf(total));
				while (total > 0) {
					int count = 0;
					while (total > 0 && count++ < BATCH) {
						String key = reader.readLine();
						ContentValues data = new ContentValues();
						for (int syns = Integer.parseInt(reader.readLine()); syns > 0; --syns) {
							String translation = reader.readLine();
							String gclass = reader.readLine();
							float trust = Float.parseFloat(reader.readLine());

							if (gclass != null && gclass.length() > 0) {
								data.put(Translation.GRAMMAR, language + "_" + gclass);
							}
							data.put(Translation.SENSE_KEY, key);
							data.put(Translation.TRUST, trust);
							data.put(Translation.TERM, translation);
							BabelTower.addTranslation(data, language);
						}
						String synonyms = reader.readLine();
						BabelTower.addTranslation(key, synonyms.trim(), language);
						--total;
					}
					publishProgress(Integer.valueOf(total), BATCH);
				}
				BabelTower.optimize(language); // TODO tell the user
				reader.close();
			} catch (IOException e) {
				Log.e(TAG, e.toString());
			}
		}
		return null;
	}

	/**
	 * @see AsyncTask#onPostExecute(Void)
	 */
	@Override
	protected void onPostExecute(Void v) {
		dialog.finishIt();
		Log.d(TAG, "Finished importing " + language);
	}
}
