package org.arkanos.avb.activities;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

public class Trial extends Activity {

	private static final int WAIT_TIME = 30000; // 30 sec.
	private static final int ALTERNATIVES = 4;
	private static final int SIZE = 18;
	private static final int PARTITION = ALTERNATIVES * ALTERNATIVES * SIZE;

	public static final String LANGUAGE = "language";

	private List<Translation> selected;
	private HashMap<Translation, List<Translation>> others;

	private HashMap<Translation, String> incorrect;

	private String language = null;

	private AsyncTask<Void, Void, Void> timer = null;

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
		Log.i("AVB-Trial", "Created but no intent with information.");
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (intent != null) {
			language = intent.getStringExtra(LANGUAGE);
			startUpTest();
		}
		Log.e("AVB-Trial", "Hope this never happens.");
	}

	private synchronized void startUpTest() {
		// BabelTower.prepareTranslations(this); // TODO remove this or do more elegantly

		final WaitingDialog dialog = new WaitingDialog(this);
		dialog.replaceTitle(getString(R.string.trial_load));
		dialog.replaceMessage(getString(R.string.trial_load_text));
		dialog.startIt();
		// TODO nothing when there aren't enough items for 1 game and alternatives
		new AsyncTask<Void, Void, List<Translation>>() {

			@Override
			protected List<Translation> doInBackground(Void... params) {
				Log.d("AVB-Trial", "Getting partition for " + language);
				List<Translation> selection = BabelTower.getPartition(PARTITION, language);
				Log.d("AVB-Trial", "Got partition of size " + selection.size());
				return selection;
			}

			@Override
			protected void onPostExecute(List<Translation> partition) {
				others = new HashMap<Translation, List<Translation>>();
				selected = new LinkedList<Translation>();
				// Log.d("AVB-Trial", "Antes " + partition.size());
				while (selected.size() < SIZE && partition.size() > 0) {
					Translation t = partition.remove((int) (Math.random() * (partition.size() - 1)));
					if (t != null) {
						selected.add(t);
						// Log.d("AVB-Trial", "Adding " + t);
					}
				}
				// Log.d("AVB-Trial", "Depois " + partition.size());

				for (Translation t : selected) {
					List<Translation> lt = new LinkedList<Translation>();
					// TODO check for "right" alternatives in the "wrong" place.
					// TODO get one antonym
					// Log.d("AVB-Trial", "Fazendo " + partition.size() + " <> " + t);
					int i = ALTERNATIVES - 1;
					List<Translation> bin = new LinkedList<Translation>();
					while (i > 0 && partition.size() > 0) {
						if (partition.size() <= i) {
							Translation rest = partition.remove(0);
							do {
								if (rest != null) {
									// Log.d("AVB-Trial", "Putting rest " + rest);
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
								if (t.getKey().equals(possible.getKey())) {// FIXME NPE
									bin.add(possible);
									// Log.d("AVB-Trial", "Discarding same key " + possible + " <> " + t);
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
										// Log.d("AVB-Trial", "Discarding same term " + possible + " <> " + t);
									}
									else {
										lt.add(possible);
										--i;
									}
								}
							}
							else {
								// Log.d("AVB-Trial", "Essa porra é nula " + partition.size() + " <> " + t + " <> " + bla);
							}
						}
					}
					/* Recovering the discarded */
					for (Translation discarded : bin) {
						partition.add(discarded);
						// Log.d("AVB-Trial", "Putting back " + discarded);
					}
					bin = new LinkedList<Translation>();

					if (lt.size() >= ALTERNATIVES - 1) {
						others.put(t, lt);
					}
					else {
						// Log.d("AVB-Trial", "Ignoring " + t + " <> " + lt.size());
					}
				}

				dialog.finishIt();

				moveToNext();
			}
		}.execute();

	}

	private void moveToNext() {
		if (selected.size() > 0) {
			createNewStep(selected.remove(0));
		}
		else {
			if (timer != null) {
				timer.cancel(true);
				timer = null;
			}
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

				String sentence = "<b>" + s.getHead().replace('_', ' ') + ":</b><br/>";
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

		final int rightone = (int) (Math.random() * (ALTERNATIVES - 1));
		if (rightone >= alternatives.size() - 1) {
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
					break;
				}
				RadioGroup rg = (RadioGroup) content.findViewById(R.id.trial_options);
				if (rg.getCheckedRadioButtonId() == correct) {
					answer.changeConfidence(1f);
					BabelTower.saveTranslationConfidence(answer);
					Log.d("AVB-Trial", "Correct Selection!");
				}
				else {
					RadioButton rb = (RadioButton) content.findViewById(rg.getCheckedRadioButtonId());
					incorrect.put(answer, rb.getText().toString());
					answer.changeConfidence(-1f);
					BabelTower.saveTranslationConfidence(answer);
					Log.d("AVB-Trial", "Wrong, expected: " + answer.getTerm());
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
