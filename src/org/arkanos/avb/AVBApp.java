package org.arkanos.avb;

import org.arkanos.avb.data.LanguageSettings;

import android.app.Application;

// TODO rename class
public class AVBApp extends Application {
	public static final String TAG = "AVB-";

	@Override
	public void onCreate() {
		super.onCreate();

		LanguageSettings.initialize(this);
	}

}
