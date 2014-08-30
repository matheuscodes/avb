package org.arkanos.avb.activities;

import java.util.LinkedList;
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
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class Change extends Activity {

	public static final String LANGUAGE = "language";
	public static final String KEY = "key";

	List<Translation> remove = null;
	List<Translation> confirm = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.change);

		remove = new LinkedList<Translation>();
		confirm = new LinkedList<Translation>();

		// BabelTower.prepareTranslations(this); // TODO remove this or do more elegantly

		Intent intent = getIntent();
		if (intent != null) {
			handleIntent(intent);
		}
		Log.e("AVB-Change", "No intent with information.");
	}

	@Override
	protected void onNewIntent(Intent intent) {
		if (intent != null) {
			handleIntent(intent);
		}
		Log.e("AVB-Change", "This doesn't make sense.");
	}

	private void handleIntent(Intent intent) {
		String language = intent.getStringExtra(LANGUAGE);
		String sense_key = intent.getStringExtra(KEY);
		Sense sense = Dictionary.getSense(sense_key);
		if (sense != null) {
			List<Translation> lt = BabelTower.getTranslations(sense.getKey(), language);
			getLayoutInflater().inflate(R.layout.dictionary_entry, (RelativeLayout) this.findViewById(R.id.change_entry));
			DictionaryEntryHelper.fillEntry(this.findViewById(R.id.change_entry), sense);
			TableLayout list = (TableLayout) this.findViewById(R.id.change_content);
			LayoutParams fill = new TableLayout.LayoutParams(
					TableLayout.LayoutParams.MATCH_PARENT,
					TableLayout.LayoutParams.WRAP_CONTENT,
					1.0f);
			for (Translation t : lt) {
				final TableRow tr = new TableRow(this);

				final TextView tv = new TextView(this);
				tv.setText(t.getTerm().replace('_', ' '));
				tr.addView(tv, new TableRow.LayoutParams(
						TableRow.LayoutParams.MATCH_PARENT,
						TableRow.LayoutParams.WRAP_CONTENT,
						1.0f));

				int height = (int) (getResources().getDisplayMetrics().density * 40 + 0.5f);

				final Translation item = t;

				final Button remove_button = new Button(this, null, android.R.attr.buttonStyleSmall);
				final Button confirm_button = new Button(this, null, android.R.attr.buttonStyleSmall);

				remove_button.setText(this.getString(R.string.change_confirm));
				remove_button.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, height));
				remove_button.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						tr.removeView(confirm_button);
						tr.removeView(remove_button);
						tv.setTextColor(getResources().getColor(R.color.change_confirmed));
						confirm.add(item);
					}
				});
				tr.addView(remove_button);

				confirm_button.setText(this.getString(R.string.change_remove));
				confirm_button.setLayoutParams(new TableRow.LayoutParams(LayoutParams.WRAP_CONTENT, height));
				confirm_button.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						tr.removeView(confirm_button);
						tr.removeView(remove_button);
						tv.setTextColor(getResources().getColor(R.color.change_removed));
						remove.add(item);
					}
				});
				tr.addView(confirm_button);

				list.addView(tr, fill);
			}
		}
	}
}
