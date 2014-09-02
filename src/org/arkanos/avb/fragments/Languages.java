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

public class Languages extends Fragment {

	public Languages() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		List<String> states = LanguageSettings.getInstalledLanguages();

		final View rootView = inflater.inflate(R.layout.languages_selection, container, false);

		/** SV **/
		configureCheckbox(rootView, R.id.use_sv, states, LanguageSettings.SWEDISH);

		/** DE **/
		configureCheckbox(rootView, R.id.use_de, states, LanguageSettings.GERMAN);

		return rootView;
	}

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

	private void handleCheck(CheckBox cb, String language) {
		if (cb.isChecked()) {
			LanguageSettings.installLanguage(language, cb.getContext());
		}
		else {
			LanguageSettings.removeLanguage(language, cb.getContext());
			// TODO confirm dialog
		}
	}
}
