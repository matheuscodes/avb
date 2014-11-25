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
package org.arkanos.avb.ui;

import org.arkanos.avb.interfaces.ProgressObserver;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.WindowManager;

/**
 * Simple dialog to wait for progress updates.
 * 
 * @version 1.0
 * @author Matheus Borges Teixeira
 */
public class WaitingDialog extends ProgressDialog implements ProgressObserver {

	/**
	 * Constructs a simple dialog to wait.
	 * 
	 * @param context defines the application context.
	 */
	public WaitingDialog(Context context) {
		super(context);
		this.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		this.setCancelable(false);
		this.setIndeterminate(false);
		this.setMax(100);
		this.setProgress(0);
	}

	/**
	 * @see ProgressObserver#defineEnd(int)
	 */
	@Override
	public void defineEnd(int value) {
		this.setMax(value);
	}

	/**
	 * @see ProgressObserver#increaseBy(int)
	 */
	@Override
	public void increaseBy(int value) {
		this.incrementProgressBy(value);
	}

	/**
	 * @see ProgressObserver#startIt()
	 */
	@Override
	public void startIt() {
		this.show();
		this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	/**
	 * @see ProgressObserver#finishIt()
	 */
	@Override
	public void finishIt() {
		this.dismiss();
		this.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	/**
	 * @see ProgressObserver#replaceMessage(String)
	 */
	@Override
	public void replaceMessage(String text) {
		setMessage(text);
	}

	/**
	 * @see ProgressObserver#replaceTitle(String)
	 */
	@Override
	public void replaceTitle(String text) {
		setTitle(text);
	}

	/**
	 * @see ProgressObserver#defineStep(int)
	 */
	@Override
	public void defineStep(int value) {
		this.setSecondaryProgress(value);
	}

	/**
	 * @see ProgressObserver#increaseStepBy(int)
	 */
	@Override
	public void increaseStepBy(int value) {
		this.incrementSecondaryProgressBy(value);
	}
}
