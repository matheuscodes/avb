package org.arkanos.avb.fragments;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.arkanos.avb.R;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
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
		content += getString(R.string.app_version);
		content += getString(R.string.copyright_external);
		InputStream in = getResources().openRawResource(R.raw.data_adv);
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		try {
			String s;
			s = reader.readLine();
			while (s != null) {
				content += s;
				Log.d("Dict", s);
				s = reader.readLine();
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		textView.setText(Html.fromHtml(content));
		return rootView;
	}
}
