package org.arkanos.avb.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.arkanos.avb.R;
import org.arkanos.avb.interfaces.ProgressObserver;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.AsyncTask;
import android.util.Log;

public class TranslationImporter extends AsyncTask<Void, Integer, Void> {

	static private final Integer BATCH = Integer.valueOf(100);
	static private final String FILE_NAME = "languages/data_";

	private static ProgressObserver dialog;
	private String language;
	private String title = null;
	private String message = null;
	private Context parent = null;

	public TranslationImporter(ProgressObserver po, String l, Context c) {
		title = c.getString(R.string.load_translation).replace("{language}", BabelTower.prettyName(l, c));
		message = c.getString(R.string.load_translation_start);
		language = l;
		parent = c;
		dialog = po;
	}

	@Override
	protected void onPreExecute() {
		dialog.replaceTitle(title);
		dialog.replaceMessage(message);
		dialog.startIt();
	}

	@Override
	protected void onProgressUpdate(Integer... i) {
		dialog.replaceMessage(message.replace("{count}", "" + i[0].intValue()));
		if (i.length > 1) {
			dialog.increaseBy(i[1].intValue());
		}
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		// Only one importer shall run at a time.
		Log.d("AVB-TranslationImporter", "Started importing " + language);
		synchronized (dialog) {
			message = parent.getString(R.string.load_translation_text);
			try {
				AssetManager am = parent.getAssets();
				InputStream in = am.open(FILE_NAME + language);
				BufferedReader reader = new BufferedReader(new InputStreamReader(in));
				String temp = reader.readLine();
				int total = 500;// Integer.parseInt(temp);
				dialog.defineEnd(total);
				publishProgress(Integer.valueOf(total));
				while (total > 0) {
					int count = 0;
					while (total > 0 && count++ < BATCH) {
						String key = reader.readLine();
						String synonyms = " ";
						ContentValues data = new ContentValues();
						ContentValues text = new ContentValues();
						for (int syns = Integer.parseInt(reader.readLine()); syns > 0; --syns) {
							String translation = reader.readLine();
							String gclass = reader.readLine();
							float trust = Float.parseFloat(reader.readLine());
							// data.put(key, value);
							// Translations.addTranslation(data);
							synonyms += translation + " ";
						}
						--total;
					}
					publishProgress(Integer.valueOf(total), BATCH);
				}
				reader.close();
			} catch (IOException e) {
				Log.e("AVB-TranslationImporter", e.toString());
			}
		}
		return null;
	}

	@Override
	protected void onPostExecute(Void v) {
		dialog.finishIt();
		Log.d("AVB-TranslationImporter", "Finished importing " + language);
	}
}
