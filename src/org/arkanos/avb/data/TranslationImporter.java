package org.arkanos.avb.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.arkanos.avb.R;
import org.arkanos.avb.interfaces.ProgressObserver;
import org.arkanos.avb.ui.LoadingDialog;

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

	public TranslationImporter(String l, Context c) {
		dialog = new LoadingDialog(c);
		title = c.getString(R.string.load_translation).replace("{language}", BabelTower.prettyName(l, c));
		message = c.getString(R.string.load_translation_start);
		language = l;
		parent = c;
	}

	@Override
	protected void onPreExecute() {
		dialog.replaceTitle(title);
		dialog.replaceMessage(parent.getString(R.string.load_translation_clean));
		dialog.startIt();
		// FIXME the dialog does not appear while cleaning.
		BabelTower.clean(language);
		dialog.replaceMessage(message);
	}

	@Override
	protected void onProgressUpdate(Integer... i) {
		dialog.replaceMessage(message.replace("{count}", "" + i[0].intValue()));
		if (i.length > 1) {
			dialog.increaseBy(i[1].intValue());
		}
		// TODO Set message when optimizing.
	}

	@Override
	protected Void doInBackground(Void... arg0) {
		// Only one importer shall run at a time.
		Log.d("AVB-TranslationImporter", "Started importing " + language);
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
