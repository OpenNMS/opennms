/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.ssh.internal.gwt.client.ui;

import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.Timer;

/**
 * The TermHandler class listens to all input from the client and converts each
 * key sequence into VT100 Standard format and then sends it to the server.
 * @author Leonardo Bell
 * @author Philip Grenon
 */
public class TermHandler implements KeyUpHandler, KeyDownHandler, KeyPressHandler{

	private KeyBuffer keybuf; //List of all pending key presses yet to be sent
	private Code code; //Object used to decipher different Key events
	private VTerminal vTerm; //Instance of the Client widget used for communication
	private boolean isClosed; //Current status of the TermHandler
	private Timer updateTimer; //Used to make scheduled updates at certain intervals

	/**
	 * The TermHandler(VTerminal vTerm) constructor creates a Handler for all client key presses
	 * @param vTerm Instance of the widget using this handler
	 */
	public TermHandler(VTerminal vTerm){
		this.vTerm = vTerm;
		keybuf = new KeyBuffer();
		code = null;
		isClosed = false;
		updateTimer = new Timer() {
			@Override
			public void run() {
				update();
			}
		};
	}

	/**
	 * The getKeybuf method returns the current list of Keys
	 * @return Current KeyBuffer
	 */
	public KeyBuffer getKeybuf() {
		return keybuf;
	}
	
	/**
	 * The onKeyDown method handles all keys that are held down, before
	 * KeyUp and KeyPress events are triggered.
	 */
        @Override
	public void onKeyDown(KeyDownEvent event) {
		code = new Code(event);
		if (!code.isControlKey()){
			if (code.isFunctionKey() || code.isCtrlDown() || code.isAltDown()) {
				processCode(code);
				event.getNativeEvent().stopPropagation();
				event.getNativeEvent().preventDefault();
			}
		}
	}
	
	/**
	 * The onKeyPress method handles all keys that were held down and then lifted up,
	 * after the KeyDown and KeyUp events are triggered
	 */
        @Override
	public void onKeyPress(KeyPressEvent event) {
		code = new Code(event);
		if (code.getCharCode() > 31 && code.getCharCode() < 127) {
			processCode(code);
			event.getNativeEvent().stopPropagation();
			event.getNativeEvent().preventDefault();
		}
	}
	
	/**
	 * The onKeyUp method handles all keys that were lifted up, after the KeyDown
	 * event is triggered and before the KeyPress event is triggered
	 */
        @Override
	public void onKeyUp(KeyUpEvent event) {/*Do not handle KeyUp events*/}

	/**
	 * The queue method puts each processed key into a KeyBuffer that
	 * is eventually sent to the server when update() is called
	 * @param keyString
	 */
	private void queue(String keyString) {
		keybuf.add(keyString);
		updateTimer.schedule(1);
	}

	/**
	 * The update method sends the current Keys in the buffer to the server
	 * at specified intervals.
	 */
	protected synchronized void update() {
		if (!isClosed) {
			vTerm.sendBytes(keybuf.drain());
			updateTimer.schedule(50);
		}
	}
	
	/**
	 * The close method stops the update timer and sets the status of the 
	 * TermHandler to closed, preventing out of sync errors.
	 */
	public synchronized void close() {
		isClosed = true;
		updateTimer.cancel();
	}

	/**
	 * The processCode method deciphers each key press or combination of key
	 * presses and converts them into VT100 format bytes
	 * @param c Key/Char code
	 */
	public void processCode(Code c){
		int k = 0;
		boolean isCharCode = false;
		if (c.getCharCode() != 0) {
			k = c.getCharCode();
		}
		else if (c.getKeyCode() != 0) k = c.getKeyCode();
		
		if (c.isCtrlDown()) {
			k = ctrlPressed(k);
			if (k == -1) return;
		} else if (c.isFunctionKey() || c.isAltDown()) {
			k = fromKeyDownSwitch(k);
			if (k == -1) return;
		}
		if (buildCharacter(k, isCharCode) != null){
			queue(buildCharacter(k, isCharCode));
		}
	}
	
	/**
	 * The ctrlPressed method deciphers a key/char code that
	 * was pressed while the CTRL key was held down
	 * @param k Key/Char code to decipher
	 * @return VT100 formatted code
	 */
	private int ctrlPressed(int k){
		if (k >= 0 && k <= 32);
		else if (k >= 65 && k <= 90)
			k -= 64;
		else if (k >= 97 && k <= 122)
			k -= 96;
		else {
			switch (k) {
			case 54:  k=30; break;	// Ctrl-^
			case 109: k=31; break;	// Ctrl-_
			case 219: k=27; break;	// Ctrl-[
			case 220: k=28; break;	// Ctrl-\
			case 221: k=29; break;	// Ctrl-]
			default: break;
			}
		}
		return k;
	}

	/**
	 * The fromKeyDownSwitch method preps key codes so they can be converted into
	 * VT100 format in the buildCharacter method
	 * @param k Key code
	 * @return converted Key code
	 */
	private int fromKeyDownSwitch(int k) {
		switch(k) {
		case 8: break;			     // Backspace
		case 9: break;               // Tab
		case 13: break;				 // Enter
		case 27: break;			     // ESC
		case 33:  k = 63276; break; // PgUp
		case 34:  k = 63277; break; // PgDn
		case 35:  k = 63275; break; // End
		case 36:  k = 63273; break; // Home
		case 37:  k = 63234; break; // Left
		case 38:  k = 63232; break; // Up
		case 39:  k = 63235; break; // Right
		case 40:  k = 63233; break; // Down
		case 45:  k = 63302; break; // Ins
		case 46:  k = 63272; break; // Del
		case 112: k = 63236; break; // F1
		case 113: k = 63237; break; // F2
		case 114: k = 63238; break; // F3
		case 115: k = 63239; break; // F4
		case 116: k = 63240; break; // F5
		case 117: k = 63241; break; // F6
		case 118: k = 63242; break; // F7
		case 119: k = 63243; break; // F8
		case 120: k = 63244; break; // F9
		case 121: k = 63245; break; // F10
		case 122: k = 63246; break; // F11
		case 123: k = 63247; break; // F12
		default: return -1;
		}
		return k;
	}
	
	/**
	 * The buildCharacter method deciphers key/char codes and converts
	 * them into VT100 format codes
	 * @param k Key/Char code to be converted
	 * @param isCharCode Whether code is Char or not
	 * @return VT100 formatted code
	 */
	private String buildCharacter(int k, boolean isCharCode) {
		String s;
		// Build character
		switch (k) {
		case 126:   s = "~~"; break;
		case 63232: s = "~A"; break; // Up
		case 63233: s = "~B"; break; // Down
		case 63234: s = "~D"; break; // Left
		case 63235: s = "~C"; break; // Right
		case 63276: s = "~1"; break; // PgUp
		case 63277: s = "~2"; break; // PgDn
		case 63273: s = "~H"; break; // Home
		case 63275: s = "~F"; break; // End
		case 63302: s = "~3"; break; // Ins
		case 63272: s = "~4"; break; // Del
		case 63236: s = "~a"; break; // F1
		case 63237: s = "~b"; break; // F2
		case 63238: s = "~c"; break; // F3
		case 63239: s = "~d"; break; // F4
		case 63240: s = "~e"; break; // F5
		case 63241: s = "~f"; break; // F6
		case 63242: s = "~g"; break; // F7
		case 63243: s = "~h"; break; // F8
		case 63244: s = "~i"; break; // F9
		case 63245: s = "~j"; break; // F10
		case 63246: s = "~k"; break; // F11
		case 63247: s = "~l"; break; // F12
		default:    s = ("" + (char)k); break;
		}
		return s;
	}
	
}
