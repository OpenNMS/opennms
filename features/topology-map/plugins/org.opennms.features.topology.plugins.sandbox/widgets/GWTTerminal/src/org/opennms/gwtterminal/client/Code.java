/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
