package org.arkanos.avb.fragments;

import org.arkanos.avb.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
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
		View rootView = inflater.inflate(R.layout.fragment_main, container, false);
		TextView textView = (TextView) rootView.findViewById(R.id.section_label);
		String content = "";
		content += getString(R.string.copyright);
		content += getString(R.string.copyright_external);
		textView.setText(Html.fromHtml(content));
		return rootView;
	}
}
