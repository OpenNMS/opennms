/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.gwtterminal.client;

import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;

public class Code {

	private int keyCode = 0;
	private int charCode = 0;
	private KeyPressEvent kP_Event = null;
	private KeyDownEvent kD_Event = null;
	private boolean isCtrlDown;
	private boolean isAltDown;
	private boolean isShiftDown;
	private boolean isFunctionKey;
	private final int[] keyCodes = new int[] { 9, 8, 13, 27, 33, 34, 35, 36, 37, 38, 39, 40, 45, 46, 112, 113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123 };

	@SuppressWarnings("rawtypes")
	public Code(KeyEvent event){
		if (event != null){
			if (event instanceof KeyPressEvent){
				kP_Event = (KeyPressEvent)event;
			} else if (event instanceof KeyDownEvent){
				kD_Event = (KeyDownEvent)event;
			}
			isCtrlDown = event.isControlKeyDown();
			isAltDown = event.isAltKeyDown();
			isShiftDown  = event.isShiftKeyDown();
		}
		int temp = 0;
		if (kP_Event != null){
			charCode = kP_Event.getUnicodeCharCode();
		} else if (kD_Event != null){
			temp = keyCode = kD_Event.getNativeKeyCode();
		} 
		isFunctionKey = false;
		for (int k : keyCodes){
			if (temp == k) {
				isFunctionKey = true;
				break;
			}
		}
	}
	
	public int getCharCode() {
		return charCode;
	}
	
	public int getKeyCode() {
		return keyCode;
	}
	
	public boolean isCtrlDown() {
		return isCtrlDown;
	}
	
	public boolean isAltDown() {
		return isAltDown;
	}
	
	public boolean isShiftDown() {
		return isShiftDown;
	}
	
	public boolean isFunctionKey() {
		return isFunctionKey;
	}
	
	public boolean isControlKey() {
		return (getKeyCode() >= 16 && getKeyCode() <= 18);
	}
}
