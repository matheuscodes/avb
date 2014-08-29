package org.arkanos.avb.fragments;

import org.arkanos.avb.R;
import org.arkanos.avb.activities.Trial;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class Stats extends Fragment {

	public Stats() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		TextView textView = (TextView) rootView.findViewById(R.id.section_label);
		String content = "";
		content += getString(R.string.copyright);
		content += getString(R.string.app_version);
		content += getString(R.string.copyright_external);

		textView.setText(Html.fromHtml(content));

		Button buttonView = (Button) rootView.findViewById(R.id.button1);
		buttonView.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				Log.d("AVB-Stats", "Crap.");
				Intent intent = new Intent(rootView.getContext(), Trial.class);
				startActivity(intent);
			}

		});

		return rootView;
	}
}
