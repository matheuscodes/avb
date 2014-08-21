package org.arkanos.avb.ui;

import org.arkanos.avb.R;
import org.arkanos.avb.interfaces.ProgressObserver;

import android.app.Activity;
import android.app.ProgressDialog;

public class DictionaryLoadingDialog extends ProgressDialog implements
		ProgressObserver {

	String text = null;
	Activity parent = null;

	public DictionaryLoadingDialog(Activity context) {
		super(context);
		parent = context;
		// Set the progress dialog to display a horizontal progress bar
		this.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		this.setTitle(R.string.load_dict);
		text = context.getString(R.string.load_dict_start);
		this.setMessage(text);
		this.setCancelable(false);
		this.setIndeterminate(false);
		this.setMax(100);
		this.setProgress(0);
	}

	@Override
	public void defineEnd(int value) {
		this.setMax(value);
	}

	@Override
	public void increaseBy(int value) {
		this.incrementProgressBy(value);
	}

	@Override
	public void startIt() {
		this.show();
	}

	@Override
	public void finishIt() {
		this.dismiss();
	}

	@Override
	public void replaceMessage(String what) {
		this.text = what;
		parent.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				setMessage(text);
			}
		});
	}

	@Override
	public void defineStep(int value) {
		this.setSecondaryProgress(value);
	}

	@Override
	public void increaseStepBy(int value) {
		this.incrementSecondaryProgressBy(value);
	}

}
