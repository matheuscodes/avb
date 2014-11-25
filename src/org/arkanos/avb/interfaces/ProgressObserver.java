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
package org.arkanos.avb.interfaces;

/**
 * Interface to encapsulate progress updates.
 * 
 * @version 1.0
 * @author Matheus Borges Teixeira
 */
public interface ProgressObserver {

	/**
	 * Defines the target where progress ends.
	 * 
	 * @param value specifies the target where progress ends.
	 */
	public void defineEnd(int value);

	/**
	 * Defines the target where the secondary progress ends.
	 * 
	 * @param value specifies the target where the secondary progress ends.
	 */
	public void defineStep(int value);

	/**
	 * Increases the current progress status by a fixed amount.
	 * 
	 * @param value specifies the amount to be increased.
	 */
	public void increaseBy(int value);

	/**
	 * Increases the current secondary progress status by a fixed amount.
	 * 
	 * @param value specifies the amount to be increased.
	 */
	public void increaseStepBy(int value);

	/**
	 * Replaces the message to be displayed.
	 * 
	 * @param value defines the new message.
	 */
	public void replaceMessage(String text);

	/**
	 * Replaces the title to be displayed.
	 * 
	 * @param value defines the new title.
	 */
	public void replaceTitle(String text);

	/**
	 * Terminates the progress display.
	 */
	public void finishIt();

	/**
	 * Triggers the progress display.
	 */
	public void startIt();
}
