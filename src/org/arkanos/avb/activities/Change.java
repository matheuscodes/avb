package org.arkanos.avb.activities;

import java.util.List;

import org.arkanos.avb.R;
import org.arkanos.avb.data.BabelTower;
import org.arkanos.avb.data.Dictionary;
import org.arkanos.avb.data.Sense;
import org.arkanos.avb.data.Translation;
import org.arkanos.avb.ui.DictionaryEntryHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class Change extends Activity {

	public static final String LANGUAGE = "language";
	public static final String KEY = "key";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change);

		Intent intent = getIntent();
		if (intent != null) {
			handleIntent(intent);
		}
		Log.e("AVB-Change", "Created but no intent with information.");
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (intent != null) {
			handleIntent(intent);
		}
		Log.e("AVB-Change", "Hope this never happens.");
	}

	private void handleIntent(Intent intent) {
		final String language = intent.getStringExtra(LANGUAGE);
		final String sense_key = intent.getStringExtra(KEY);
		String title = this.getResources().getString(R.string.change_title);
		Sense sense = Dictionary.getSense(sense_key);

		title = title.replace("{language}", BabelTower.prettyName(language, this));

		if (sense != null) {
			title = title.replace("{word}", sense.getHead());
			this.setTitle(title);

			List<Translation> lt = BabelTower.getTranslations(sense.getKey(), language);

			getLayoutInflater().inflate(R.layout.dictionary_entry, (RelativeLayout) this.findViewById(R.id.change_entry));
			DictionaryEntryHelper.fillEntry(this.findViewById(R.id.change_entry), sense);

			final TableLayout list = (TableLayout) this.findViewById(R.id.change_content);
			final LayoutParams fill = new TableLayout.LayoutParams(
					TableLayout.LayoutParams.MATCH_PARENT,
					TableLayout.LayoutParams.WRAP_CONTENT,
					1.0f);

			final int button_height = (int) (getResources().getDisplayMetrics().density * 40 + 0.5f);

			for (Translation t : lt) {
				final TableRow tr = new TableRow(this);

				final TextView tv = new TextView(this);
				tv.setText(t.getTerm());
				tr.addView(tv, new TableRow.LayoutParams(
						TableRow.LayoutParams.MATCH_PARENT,
						TableRow.LayoutParams.WRAP_CONTENT,
						1.0f));

				final Translation item = t;

				final Button remove_button = new Button(this, null, android.R.attr.buttonStyleSmall);
				final Button confirm_button = new Button(this, null, android.R.attr.buttonStyleSmall);

				confirm_button.setText(this.getString(R.string.change_confirm));
				confirm_button.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, button_height));
				confirm_button.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						tr.removeView(confirm_button);
						tr.removeView(remove_button);
						tv.setTextColor(getResources().getColor(R.color.change_confirmed));
						item.increaseTrust(1f); // TODO reference in a constant somewhere
						BabelTower.saveTranslationTrust(item);
					}
				});
				if (item.getTrust() < 1f) {// TODO reference in a constant somewhere
					tr.addView(confirm_button);
				}
				else {
					tv.setTextColor(getResources().getColor(R.color.change_confirmed));
				}

				remove_button.setText(this.getString(R.string.change_remove));
				remove_button.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, button_height));
				remove_button.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						tr.removeView(confirm_button);
						tr.removeView(remove_button);
						tv.setTextColor(getResources().getColor(R.color.change_removed));
						BabelTower.deleteTranslation(item);
					}
				});
				tr.addView(remove_button);

				list.addView(tr, fill);
			}

			final TableRow new_row = new TableRow(this);

			Button new_button = new Button(this, null, android.R.attr.buttonStyleSmall);
			new_button.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, button_height));
			new_row.addView(new_button, new TableRow.LayoutParams(
					TableRow.LayoutParams.MATCH_PARENT,
					TableRow.LayoutParams.WRAP_CONTENT,
					1.0f));
			new_button.setText(R.string.change_new);
			new_button.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					list.removeView(new_row);
					final TableRow extra_row = new TableRow(v.getContext());

					final EditText new_term = new EditText(v.getContext());
					extra_row.addView(new_term, new TableRow.LayoutParams(
							TableRow.LayoutParams.MATCH_PARENT,
							TableRow.LayoutParams.WRAP_CONTENT,
							1.0f));

					Button add = new Button(v.getContext(), null, android.R.attr.buttonStyleSmall);
					add.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, button_height));
					add.setText(R.string.change_add);
					add.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							extra_row.removeAllViews();

							String text = new_term.getText().toString();
							Translation new_one = new Translation(sense_key, language);
							new_one.setTerm(text.trim().replace(' ', '_'));
							new_one.setTrust(1f);
							BabelTower.addTranslation(new_one);

							TextView added = new TextView(v.getContext());
							added.setText(text);
							added.setTextColor(getResources().getColor(R.color.change_confirmed));
							extra_row.addView(added);
						}
					});
					extra_row.addView(add);

					list.addView(extra_row, fill);
					list.addView(new_row, fill);
				}
			});

			list.addView(new_row, fill);
		}
	}
}
