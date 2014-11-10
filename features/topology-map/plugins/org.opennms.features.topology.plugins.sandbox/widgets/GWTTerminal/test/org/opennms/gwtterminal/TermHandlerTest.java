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

package org.opennms.gwtterminal;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.gwtterminal.client.Code;
import org.opennms.gwtterminal.client.TermHandler;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;

@SuppressWarnings("unused")
public class TermHandlerTest {

	private TermHandler termHandler;
	private final int CTRL_KEY = KeyCodes.KEY_CTRL;
	private final int ALT_KEY = KeyCodes.KEY_ALT;
	private final int SHIFT = KeyCodes.KEY_SHIFT;
	private final int ENTER = KeyCodes.KEY_ENTER;
	private final int TAB = KeyCodes.KEY_TAB;
	private final int BACKSPACE = KeyCodes.KEY_BACKSPACE;
	private final int BACKSLASH = 220; //backslash javascript keycode
	private final int F1 = 112; //F1 javascript keycode
	
	@Before
	public void setUp() throws Exception {
		termHandler = new TermHandler();
	}

	
	@Test
	public void testHoldCtrl() {
		SudoKeyDownEvent ctrlPress = new SudoKeyDownEvent(CTRL_KEY, false, false, false);
		termHandler.onKeyDown(ctrlPress);
		assertEquals("", termHandler.getKeybuf().toString()); //Holding ctrl key
	}
	
	@Test
	public void testHoldShift() {
		SudoKeyDownEvent shiftPress = new SudoKeyDownEvent(SHIFT, false, false, false);
		termHandler.onKeyDown(shiftPress);
		assertEquals("", termHandler.getKeybuf().toString());
	}
	
	@Test
	public void testHoldAlt() {
		SudoKeyDownEvent altPress = new SudoKeyDownEvent(ALT_KEY, false, false, false);
		termHandler.onKeyDown(altPress);
		assertEquals("", termHandler.getKeybuf().toString());
	}
	
	@Test
	public void testCtrl_D() {
		String expected = String.valueOf((char)(0x04)); //Ctrl-D
		SudoKeyDownEvent dPress = new SudoKeyDownEvent(68, true, false, false); // Pressing 'd' key
		termHandler.onKeyDown(dPress);
		assertArrayEquals(expected.getBytes(), termHandler.getKeybuf().toString().getBytes());
	}
	
	@Test
	public void testCtrl_Backslash() {
		String expected = String.valueOf((char)(0x1C)); //Ctrl-\
		SudoKeyDownEvent bSlashPress = new SudoKeyDownEvent(BACKSLASH, true, false, false); 
		termHandler.processCode(new Code(bSlashPress));
		assertArrayEquals(expected.getBytes(), termHandler.getKeybuf().toString().getBytes()); //Pressing '\' key
	}
	
	@Test
	public void testBackspace() {
		String expected = String.valueOf((char)(KeyCodes.KEY_BACKSPACE));
		SudoKeyDownEvent backspacePress = new SudoKeyDownEvent(KeyCodes.KEY_BACKSPACE, false, false, false);
		termHandler.processCode(new Code(backspacePress));
		assertArrayEquals(expected.getBytes(), termHandler.getKeybuf().toString().getBytes());
	}
	
	@Test
	public void testF1() {
		String expected = "~a";
		SudoKeyDownEvent F1Press = new SudoKeyDownEvent(F1, false, false, false);
		termHandler.processCode(new Code(F1Press));
		assertArrayEquals(expected.getBytes(), termHandler.getKeybuf().toString().getBytes());
	}
	
	@After
	public void tearDown() {
		termHandler = null;
	}
	
	class SudoKeyPressEvent extends KeyPressEvent {
		private int charCode;
		private boolean isCtrlDown;
		private boolean isAltDown;
		private boolean isShiftDown;
		
		public SudoKeyPressEvent(int k, boolean isCtrlDown, boolean isAltDown, boolean isShiftDown) {
			charCode = k;
			this.isCtrlDown = isCtrlDown;
			this.isAltDown = isAltDown;
			this.isShiftDown = isShiftDown;
		}
		
		@Override
		public int getUnicodeCharCode() {
			return charCode;
		}
		
		@Override
		public boolean isControlKeyDown(){
			return isCtrlDown;
		}
		
		@Override
		public boolean isAltKeyDown(){
			return isAltDown;
		}
		
		@Override 
		public boolean isShiftKeyDown(){
			return isShiftDown;
		}
	}
	
	class SudoKeyDownEvent extends KeyDownEvent {
		private int keyCode;
		private boolean isCtrlDown;
		private boolean isAltDown;
		private boolean isShiftDown;
		
		public SudoKeyDownEvent(int k, boolean isCtrlDown, boolean isAltDown, boolean isShiftDown) {
			keyCode = k;
			this.isCtrlDown = isCtrlDown;
			this.isAltDown = isAltDown;
			this.isShiftDown = isShiftDown;
		}
		
		@Override
		public int getNativeKeyCode() {
			return keyCode;
		}
		
		@Override
		public boolean isControlKeyDown(){
			return isCtrlDown;
		}
		
		@Override
		public boolean isAltKeyDown(){
			return isAltDown;
		}
		
		@Override 
		public boolean isShiftKeyDown(){
			return isShiftDown;
		}
	}
}
