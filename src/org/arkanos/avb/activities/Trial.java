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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.arkanos.avb.AVBApp;
import org.arkanos.avb.R;
import org.arkanos.avb.data.BabelTower;
import org.arkanos.avb.data.Dictionary;
import org.arkanos.avb.data.Sense;
import org.arkanos.avb.data.Translation;
import org.arkanos.avb.ui.DictionaryEntryHelper;
import org.arkanos.avb.ui.WaitingDialog;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

/**
 * Activity to test the users knowledge.
 * 
 * @version 1.0
 * @author Matheus Borges Teixeira
 */
public class Trial extends Activity {

	/** Time for the user to select an answer **/
	private static final int WAIT_TIME = 30000; // 30 seconds.
	/** Number of alternatives to select aside the "I don't know" **/
	private static final int ALTERNATIVES = 4;
	/** Base for calculating a partition size **/
	private static final int SIZE = 18;
	/** Total amount of items inside a partition **/
	private static final int PARTITION = ALTERNATIVES * ALTERNATIVES * SIZE;

	/** String key for passing the language **/
	public static final String LANGUAGE = "language";

	/** Selection to be used in the test as correct items **/
	private List<Translation> selected;
	/** Incorrect alternatives for each translation **/
	private HashMap<Translation, List<Translation>> others;
	/** List of translations which a user answered incorrectly **/
	private HashMap<Translation, String> incorrect;
	/** Actual language being used in the activity **/
	private String language = null;
	/** Dummy timer **/
	private AsyncTask<Void, Void, Void> timer = null;

	/**
	 * @see Activity#onCreate(Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trial_master);

		incorrect = new HashMap<Translation, String>();

		Intent intent = getIntent();
		if (intent != null) {
			language = intent.getStringExtra(LANGUAGE);
			startUpTest();
		}
		Log.i(AVBApp.TAG + "Trial", "Created but no intent with information.");
	}

	/**
	 * @see Activity#onNewIntent(Intent)
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		if (intent != null) {
			language = intent.getStringExtra(LANGUAGE);
			startUpTest();
		}
		Log.e(AVBApp.TAG + "Trial", "Hope this never happens.");
	}

	/**
	 * Selects the content of the test and configures the alternatives.
	 * Most of the computation is performed in the background.
	 */
	private synchronized void startUpTest() {
		final WaitingDialog dialog = new WaitingDialog(this);
		dialog.replaceTitle(getString(R.string.trial_load));
		dialog.replaceMessage(getString(R.string.trial_load_text));
		dialog.startIt();
		// FIXME Error#02 Do nothing when there aren't enough items for 1 game and its alternatives.
		new AsyncTask<Void, Void, List<Translation>>() {

			@Override
			protected List<Translation> doInBackground(Void... params) {
				Log.d(AVBApp.TAG + "Trial", "Getting partition for " + language);
				List<Translation> selection = BabelTower.getPartition(PARTITION, language);
				Log.d(AVBApp.TAG + "Trial", "Got partition of size " + selection.size());
				return selection;
			}

			@Override
			protected void onPostExecute(List<Translation> partition) {
				others = new HashMap<Translation, List<Translation>>();
				selected = new LinkedList<Translation>();
				// Log.d(AVBApp.TAG + "Trial", "Antes " + partition.size());
				while (selected.size() < SIZE && partition.size() > 0) {
					Translation t = partition.remove((int) (Math.random() * (partition.size() - 1)));
					if (t != null) {
						selected.add(t);
						// Log.d(AVBApp.TAG + "Trial", "Adding " + t);
					}
				}
				// Log.d(AVBApp.TAG + "Trial", "Depois " + partition.size());

				for (Translation t : selected) {
					List<Translation> lt = new LinkedList<Translation>();
					// TODO Feature#13 Check for "right" alternatives in the "wrong" place.
					// TODO Feature#05 Add at least one Antonym.
					// Log.d(AVBApp.TAG + "Trial", "Fazendo " + partition.size() + " <> " + t);
					int i = ALTERNATIVES - 1;
					List<Translation> bin = new LinkedList<Translation>();
					while (i > 0 && partition.size() > 0) {
						if (partition.size() <= i) {
							Translation rest = partition.remove(0);
							do {
								if (rest != null) {
									// Log.d(AVBApp.TAG + "Trial", "Putting rest " + rest);
									lt.add(rest);
									--i;
								}
								rest = partition.remove(0);
							} while (partition.size() > 0);
						}
						else {
							int bla = (int) (Math.random() * (partition.size() - 1));
							Translation possible = partition.remove(bla);
							if (possible != null) {
								if (t.getKey().equals(possible.getKey())) {// FIXME Error#03 NPE when partition has not enough items.
									bin.add(possible);
									// Log.d(AVBApp.TAG + "Trial", "Discarding same key " + possible + " <> " + t);
								}
								else {
									boolean exists = false;
									for (Translation copy : lt) {
										if (copy.getTerm().equals(possible.getTerm())) {
											exists = true;
										}
									}
									if (exists) {
										bin.add(possible);
										// Log.d(AVBApp.TAG + "Trial", "Discarding same term " + possible + " <> " + t);
									}
									else {
										lt.add(possible);
										--i;
									}
								}
							}
							else {
								// Log.d(AVBApp.TAG + "Trial", "Essa porra é nula " + partition.size() + " <> " + t + " <> " + bla);
							}
						}
					}
					/* Recovering the discarded */
					for (Translation discarded : bin) {
						partition.add(discarded);
						// Log.d(AVBApp.TAG + "Trial", "Putting back " + discarded);
					}
					bin = new LinkedList<Translation>();

					if (lt.size() >= ALTERNATIVES - 1) {
						others.put(t, lt);
					}
					else {
						// Log.d(AVBApp.TAG + "Trial", "Ignoring " + t + " <> " + lt.size());
					}
				}

				dialog.finishIt();

				moveToNext();
			}
		}.execute();

	}

	/**
	 * Computes the result of the current selection and moves to the next.
	 */
	private void moveToNext() {
		if (selected.size() > 0) {
			createNewStep(selected.remove(0));
		}
		else {
			if (timer != null) {
				timer.cancel(true);
				timer = null;
			}
			if (incorrect.size() <= 0) {
				this.finish();
			}
			else {
				final RelativeLayout content = (RelativeLayout) this.findViewById(R.id.trial_super_master);
				content.removeAllViews();

				getLayoutInflater().inflate(R.layout.trial_report, content);
				TableLayout table = (TableLayout) content.findViewById(R.id.trial_report_content);
				for (Map.Entry<Translation, String> wrong_one : incorrect.entrySet()) {
					final Translation right = wrong_one.getKey();
					Sense s = Dictionary.getSense(right.getKey());
					String all = BabelTower.getTranslationSynonyms(right.getKey(), language);
					String given = wrong_one.getValue();

					TableRow tr = new TableRow(this);
					TextView tv = new TextView(this);

					String sentence = "<b>" + s.getPrettyHead().replace('_', ' ') + ":</b><br/>";
					if (given.equals(getString(R.string.trial_dont_know))) {
						sentence += getString(R.string.trial_report_skipped);
						tr.setBackgroundColor(getResources().getColor(R.color.trial_skipped));
					}
					else if (all.toLowerCase(Locale.getDefault()).contains(given.toLowerCase(Locale.getDefault()))) {
						sentence += getString(R.string.trial_report_half);
						tr.setBackgroundColor(getResources().getColor(R.color.trial_half));
					}
					else {
						sentence += getString(R.string.trial_report_wrong);
						tr.setBackgroundColor(getResources().getColor(R.color.trial_wrong));
					}
					sentence = sentence.replace("{wrong}", given);
					sentence = sentence.replace("{correct}", right.getTerm().replace('_', ' '));
					tv.setText(Html.fromHtml(sentence));
					tr.addView(tv, new TableRow.LayoutParams(
							TableRow.LayoutParams.MATCH_PARENT,
							TableRow.LayoutParams.WRAP_CONTENT,
							1.0f));
					tv.setGravity(Gravity.CENTER_VERTICAL);

					int button_height = (int) (getResources().getDisplayMetrics().density * 40 + 0.5f);
					final Button fix_button = new Button(this, null, android.R.attr.buttonStyleSmall);
					fix_button.setText(this.getString(R.string.trial_report_fix));
					fix_button.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, button_height));
					fix_button.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							Intent intent = new Intent(content.getContext(), Change.class);
							intent.putExtra(Change.LANGUAGE, language);
							intent.putExtra(Change.KEY, right.getKey());
							startActivity(intent);
						}
					});

					tr.addView(fix_button, new TableRow.LayoutParams(
							TableRow.LayoutParams.WRAP_CONTENT,
							TableRow.LayoutParams.WRAP_CONTENT,
							0.1f));

					tr.setGravity(Gravity.CENTER_VERTICAL);
					tr.setVerticalGravity(Gravity.CENTER_VERTICAL);

					table.addView(tr, new TableLayout.LayoutParams(
							TableRow.LayoutParams.MATCH_PARENT,
							TableRow.LayoutParams.WRAP_CONTENT,
							1.0f));
				}
			}
		}
	}

	/**
	 * Builds up the visual interface to select an alternative.
	 * 
	 * @param answer specifies the translation to be tested.
	 */
	private void createNewStep(final Translation answer) {
		List<Translation> alternatives = others.get(answer);
		if (alternatives == null) {
			moveToNext();
			return;
		}

		final RelativeLayout content = (RelativeLayout) this.findViewById(R.id.trial_content);
		content.removeAllViews();
		getLayoutInflater().inflate(R.layout.trial_multiple, content);

		Sense s = Dictionary.getSense(answer.getKey());
		getLayoutInflater().inflate(R.layout.dictionary_entry, (RelativeLayout) this.findViewById(R.id.trial_entry));
		DictionaryEntryHelper.fillEntry(content, s);

		final int rightone = (int) Math.round((Math.random() * (ALTERNATIVES - 1)));
		if (rightone >= alternatives.size()) {
			alternatives.add(answer);
		}
		else {
			alternatives.add(rightone, answer);
		}
		((RadioButton) content.findViewById(R.id.trial_option0)).setText(alternatives.remove(0).getTerm().replace('_', ' '));
		((RadioButton) content.findViewById(R.id.trial_option1)).setText(alternatives.remove(0).getTerm().replace('_', ' '));
		((RadioButton) content.findViewById(R.id.trial_option2)).setText(alternatives.remove(0).getTerm().replace('_', ' '));
		((RadioButton) content.findViewById(R.id.trial_option3)).setText(alternatives.remove(0).getTerm().replace('_', ' '));

		Button b = (Button) this.findViewById(R.id.trial_confirm);
		b.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				int correct = 0;
				switch (rightone) {
				case 0:
					correct = R.id.trial_option0;
					break;
				case 1:
					correct = R.id.trial_option1;
					break;
				case 2:
					correct = R.id.trial_option2;
					break;
				case 3:
					correct = R.id.trial_option3;
					break;
				default:
					Log.e(AVBApp.TAG + "Trial", "No choice, this should never come.");
					break;
				}
				RadioGroup rg = (RadioGroup) content.findViewById(R.id.trial_options);
				if (rg.getCheckedRadioButtonId() == correct) {
					answer.changeConfidence(1f);
					BabelTower.saveTranslationConfidence(answer);
					Log.d(AVBApp.TAG + "Trial", "Correct Selection!");
				}
				else {
					RadioButton rb = (RadioButton) content.findViewById(rg.getCheckedRadioButtonId());
					incorrect.put(answer, rb.getText().toString());
					answer.changeConfidence(-1f);
					BabelTower.saveTranslationConfidence(answer);
					Log.d(AVBApp.TAG + "Trial", "Wrong, expected: " + answer.getTerm());
				}
				moveToNext();
			}
		});

		final ProgressBar progress = (ProgressBar) this.findViewById(R.id.trial_countdown);
		progress.setMax(100);
		progress.setProgress(100);
		progress.setActivated(true);
		if (timer == null) {
			timer = new AsyncTask<Void, Void, Void>() {
				@Override
				protected Void doInBackground(Void... a) {
					synchronized (this) {
						try {
							while (progress.getProgress() > 0) {
								progress.incrementProgressBy(-1);
								wait(WAIT_TIME / 100);
							}
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					return null;
				}

				@Override
				protected void onPostExecute(Void a) {
					timer = null;
					answer.changeConfidence(-1f);
					BabelTower.saveTranslationConfidence(answer);
					moveToNext();
				}
			};

			timer.execute();
		}
	}
}
