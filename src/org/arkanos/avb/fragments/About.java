package org.arkanos.avb.fragments;

import org.arkanos.avb.R;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class About extends Fragment {

	public About() {

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.about, container, false);
		TextView textView = (TextView) rootView.findViewById(R.id.about_text);
		String content = "";
		content += getString(R.string.copyright);
		content += getString(R.string.app_version);
		content += getString(R.string.copyright_external);

		textView.setText(Html.fromHtml(content));
		return rootView;
	}
}
