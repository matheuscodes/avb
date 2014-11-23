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
package org.arkanos.avb;

import org.arkanos.avb.data.BabelTower;
import org.arkanos.avb.data.Dictionary;
import org.arkanos.avb.data.LanguageSettings;

import android.app.Application;
import android.util.Log;

/**
 * Ensures proper initialization.
 * 
 * @version 1.0
 * @author Matheus Borges Teixeira
 */
public class AVBApp extends Application {
	public static final String TAG = "AVB-";
	private static boolean failed = false;

	@Override
	public void onCreate() {
		super.onCreate();
		// TODO find a way out to execute this with dialogs w/o first activity.
		try {
			LanguageSettings.initialize(this);
			Dictionary.initialize(this);
			BabelTower.initialize(this);
		} catch (Exception e) {
			Log.e("AVB", e.toString());
			failed = true;
		}
	}

	/**
	 * Checks whether initialization has failed.
	 * 
	 * @return true when there were problems.
	 */
	public static boolean failedInitialize() {
		return failed;
	}

}
