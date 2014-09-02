package org.arkanos.avb;

import org.arkanos.avb.data.BabelTower;
import org.arkanos.avb.data.Dictionary;
import org.arkanos.avb.data.LanguageSettings;

import android.app.Application;
import android.util.Log;

// TODO rename class
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

	public static boolean failedInitialize() {
		return failed;
	}

}
