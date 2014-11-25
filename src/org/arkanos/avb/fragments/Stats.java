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
package org.arkanos.avb.fragments;

import org.arkanos.avb.R;
import org.arkanos.avb.activities.Change;
import org.arkanos.avb.activities.Trial;
import org.arkanos.avb.data.BabelTower;
import org.arkanos.avb.data.LanguageSettings;
import org.arkanos.avb.ui.StatusWheel;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Fragment for observing statistics, test and maintain vocabulary.
 * 
 * @version 1.0
 * @author Matheus Borges Teixeira
 */
public class Stats extends Fragment {

	/**
	 * Default constructor.
	 */
	public Stats() {

	}

	/**
	 * @see Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.stats, container, false);

		LinearLayout ll = (LinearLayout) rootView.findViewById(R.id.stats_content);
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);

		TextView title = new TextView(rootView.getContext());
		title.setText(this.getString(R.string.stats_intro));
		ll.addView(title, llp);

		ll.addView(new StatusWheel(rootView.getContext()), llp);

		for (final String l : LanguageSettings.getInstalledLanguages()) {
			String helper;

			Button test = new Button(rootView.getContext());
			helper = this.getString(R.string.stats_trial).replace("{language}", LanguageSettings.prettyName(l, rootView.getContext()));
			test.setText(helper);
			test.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(rootView.getContext(), Trial.class);
					intent.putExtra(Trial.LANGUAGE, l);// TODO make a constant
					startActivity(intent);
				}
			});
			ll.addView(test, llp);

			Button insert = new Button(rootView.getContext());
			helper = this.getString(R.string.stats_add).replace("{language}", LanguageSettings.prettyName(l, rootView.getContext()));
			insert.setText(helper);
			insert.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Intent intent = new Intent(rootView.getContext(), Change.class);
					intent.putExtra(Change.LANGUAGE, l);
					intent.putExtra(Change.KEY, BabelTower.getSenseToTranslate(l));
					startActivity(intent);
				}
			});
			ll.addView(insert, llp);

		}
		return rootView;
	}
}
