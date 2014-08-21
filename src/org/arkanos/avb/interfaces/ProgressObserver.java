package org.arkanos.avb.interfaces;

public interface ProgressObserver {

	public void defineEnd(int value);

	public void defineStep(int value);

	public void increaseBy(int value);

	public void increaseStepBy(int value);

	public void replaceMessage(String what);

	public void finishIt();

	public void startIt();

}
