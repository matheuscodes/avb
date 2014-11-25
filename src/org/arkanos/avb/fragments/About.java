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

import android.app.Fragment;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Fragment for displaying copyright information.
 * 
 * @version 1.0
 * @author Matheus Borges Teixeira
 */
public class About extends Fragment {

	/**
	 * Default constructor.
	 */
	public About() {

	}

	/**
	 * @see Fragment#onCreateView(LayoutInflater, ViewGroup, Bundle)
	 */
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
