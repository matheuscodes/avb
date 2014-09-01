package org.arkanos.avb.activities;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.arkanos.avb.R;
import org.arkanos.avb.data.BabelTower;
import org.arkanos.avb.data.Dictionary;
import org.arkanos.avb.data.Sense;
import org.arkanos.avb.data.Translation;
import org.arkanos.avb.ui.DictionaryEntryHelper;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;

public class Trial extends Activity {

	private static final int WAIT_TIME = 30000; // 30 sec.
	private static final int SIZE = 18;
	private static final int PARTITION = 3 * 3 * SIZE;
	private static final int ALTERNATIVES = 4;

	private List<Translation> selected;
	HashMap<Translation, List<Translation>> others;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trial_master);

		// BabelTower.prepareTranslations(this); // TODO remove this or do more elegantly

		// TODO nothing when there aren't enough items for 1 game and alternatives
		Log.d("AVB-Trial", "Getting partition...");
		List<Translation> partition = BabelTower.getPartition(PARTITION, BabelTower.GERMAN);
		Log.d("AVB-Trial", "Got partition of size " + partition.size());

		others = new HashMap<Translation, List<Translation>>();
		selected = new LinkedList<Translation>();
		while (selected.size() < SIZE && partition.size() > 0) {
			Translation t = partition.remove((int) (Math.random() * (partition.size() - 1)));
			selected.add(t);
		}

		for (Translation t : selected) {
			List<Translation> lt = new LinkedList<Translation>();
			// TODO check for "right" alternatives in the "wrong" place.
			// TODO get one antonym
			int i = ALTERNATIVES - 1;
			List<Translation> bin = new LinkedList<Translation>();
			while (i > 0) {
				if (partition.size() <= i) {
					for (Translation rest : partition) {
						lt.add(rest);
						--i;
					}
				}
				else {
					Translation possible = partition.remove((int) (Math.random() * (partition.size() - 1)));
					if (t.getKey().equals(possible.getKey())) {
						bin.add(possible);
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
						}
						else {
							lt.add(possible);
							--i;
						}
					}
				}

			}
			others.put(t, lt);
		}

		moveToNext();
	}

	private void moveToNext() {
		if (selected.size() > 0) {
			createNewStep(selected.remove(0));
		}
		else {
			finish();
		}
	}

	private void createNewStep(Translation t) {
		final RelativeLayout content = (RelativeLayout) this.findViewById(R.id.trial_content);
		content.removeAllViews();
		getLayoutInflater().inflate(R.layout.trial_multiple, content);

		Sense s = Dictionary.getSense(t.getKey());
		getLayoutInflater().inflate(R.layout.dictionary_entry, (RelativeLayout) this.findViewById(R.id.trial_entry));
		DictionaryEntryHelper.fillEntry(content, s);

		List<Translation> alternatives = others.get(t);

		final int rightone = (int) (Math.random() * (ALTERNATIVES - 1));
		if (rightone >= alternatives.size() - 1) {
			alternatives.add(t);
		}
		else {
			alternatives.add(rightone, t);
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
					Log.d("AVB-Trial", "Correct Selection!");
				}
				else {
					RadioButton rb = (RadioButton) content.findViewById(correct);
					Log.d("AVB-Trial", "Wrong, expected: " + rb.getText());
				}
				moveToNext();
			}
		});

		final ProgressBar progress = (ProgressBar) this.findViewById(R.id.trial_countdown);
		progress.setMax(100);
		progress.setProgress(100);
		progress.setActivated(true);
		new AsyncTask<Void, Void, Void>() {
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
				moveToNext();
			}
		}.execute();

	}
}
