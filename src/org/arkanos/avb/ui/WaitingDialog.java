package org.arkanos.avb.ui;

import org.arkanos.avb.interfaces.ProgressObserver;

import android.app.ProgressDialog;
import android.content.Context;

public class WaitingDialog extends ProgressDialog implements ProgressObserver {

	public WaitingDialog(Context context) {
		super(context);
		// Set the progress dialog to display a horizontal progress bar
		this.setProgressStyle(ProgressDialog.STYLE_SPINNER);
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
	public void replaceMessage(String text) {
		setMessage(text);
	}

	@Override
	public void replaceTitle(String text) {
		setTitle(text);
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
