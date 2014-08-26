package org.arkanos.avb.fragments;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.arkanos.avb.R;
import org.arkanos.avb.data.BabelTower;
import org.arkanos.avb.data.TranslationImporter;
import org.arkanos.avb.ui.LoadingDialog;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;

public class LanguageSettings extends Fragment {

	HashMap<String, Boolean> states;

	public LanguageSettings() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		states = new HashMap<String, Boolean>();
		try {
			FileInputStream fis = container.getContext().openFileInput(BabelTower.CONFIG_PATH);
			int size = fis.read();
			while (size > 0) {
				byte[] b = new byte[size];
				fis.read(b);
				states.put(new String(b), true);
				size = fis.read();
			}
		} catch (FileNotFoundException fnfe) {
			Log.i("AVB-LanguageSettings", "No config file.");
		} catch (IOException ioe) {
			// TODO move all these tags to class member
			Log.e("AVB-LanguageSettings", ioe.toString());
		}

		final View rootView = inflater.inflate(R.layout.languages_selection, container, false);

		final LoadingDialog dialog = new LoadingDialog(container.getContext());

		/** SV **/
		configureCheckbox(rootView, R.id.use_sv, states, dialog, BabelTower.SWEDISH);

		/** DE **/
		configureCheckbox(rootView, R.id.use_de, states, dialog, BabelTower.GERMAN);

		return rootView;
	}

	@Override
	public void onDestroyView() {
		try {
			FileOutputStream fos = this.getView().getContext().openFileOutput(BabelTower.CONFIG_PATH, Context.MODE_PRIVATE);
			for (Map.Entry<String, Boolean> e : states.entrySet()) {
				if (e.getValue() == true) {
					fos.write(e.getKey().getBytes().length);
					fos.write(e.getKey().getBytes());
				}
			}
		} catch (FileNotFoundException fnfe) {
			Log.e("AVB-LanguageSettings", fnfe.toString());
		} catch (IOException ioe) {
			Log.e("AVB-LanguageSettings", ioe.toString());
		}
		super.onDestroyView();
	}

	private void configureCheckbox(final View root, final int id, HashMap<String, Boolean> states, final LoadingDialog ld, final String language) {
		final CheckBox cb = ((CheckBox) root.findViewById(id));
		boolean state = false;
		if (states.get(language) != null) {
			state = states.get(language);
		}
		cb.setChecked(state);
		cb.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				handleCheck(cb, ld, language);
			}
		});
	}

	private void handleCheck(CheckBox cb, LoadingDialog ld, String language) {
		if (cb.isChecked()) {
			TranslationImporter caller;
			LoadingDialog progressDialog = new LoadingDialog(cb.getContext());
			caller = new TranslationImporter(progressDialog, language, cb.getContext());
			caller.execute();
			states.remove(language);
			states.put(language, true);
		}
		else {
			states.put(language, false);
			// TODO confirm dialog
			BabelTower.clean();
		}
	}
}
