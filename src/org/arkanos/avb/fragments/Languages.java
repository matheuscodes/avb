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

import java.util.List;

import org.arkanos.avb.R;
import org.arkanos.avb.data.LanguageSettings;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;

/**
 * Fragment for installing and removing languages.
 * 
 * @version 1.0
 * @author Matheus Borges Teixeira
 */
public class Languages extends Fragment {

	/**
	 * Default constructor.
	 */
	public Languages() {

	}

	/**
	 * @see Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		List<String> states = LanguageSettings.getInstalledLanguages();

		final View rootView = inflater.inflate(R.layout.languages_selection, container, false);

		// TODO Feature#12 Use a generic building system based on LanguageSettings.getAllLanguages()
		/** SV **/
		configureCheckbox(rootView, R.id.use_sv, states, LanguageSettings.SWEDISH);

		/** DE **/
		configureCheckbox(rootView, R.id.use_de, states, LanguageSettings.GERMAN);

		/** NO **/
		configureCheckbox(rootView, R.id.use_no, states, LanguageSettings.NORWEGIAN);

		/** PT **/
		configureCheckbox(rootView, R.id.use_pt, states, LanguageSettings.PORTUGUESE);

		/** PL **/
		configureCheckbox(rootView, R.id.use_pl, states, LanguageSettings.POLISH);

		return rootView;
	}

	/**
	 * Performs basic configurations and events on the check boxes.
	 * 
	 * @param root provides the view where the check boxes are.
	 * @param id specifies the ID for the check box to be configured.
	 * @param states specifies the state of the languages.
	 * @param language specifies the language for the check box.
	 */
	private void configureCheckbox(final View root, final int id, List<String> states, final String language) {
		final CheckBox cb = ((CheckBox) root.findViewById(id));
		boolean state = false;
		if (states.contains(language)) {
			state = true;
		}
		cb.setChecked(state);
		cb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleCheck(cb, language);
			}
		});
	}

	/**
	 * Installs and removes languages.
	 * Call for the toggling of the check box.
	 * 
	 * @param cb specifies the check box who calls the action.
	 * @param language defines which language is to be handled.
	 */
	private void handleCheck(CheckBox cb, String language) {
		if (cb.isChecked()) {
			LanguageSettings.installLanguage(language, cb.getContext());
		}
		else {
			LanguageSettings.removeLanguage(language, cb.getContext());
			// TODO Feature#03 Add a confirm dialog before removing.
		}
	}
}
