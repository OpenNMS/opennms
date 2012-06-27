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
	private final int ENTER = KeyCodes.KEY_ENTER;
	private final int TAB = KeyCodes.KEY_TAB;
	private final int BACKSPACE = KeyCodes.KEY_BACKSPACE;
	private final int BACKSLASH = 220; //KeyCodes class doesnt have backslash so i made my own :)
	
	@Before
	public void setUp() throws Exception {
		termHandler = new TermHandler();
	}

	
	@Test
	public void testOnKeyDownHoldCtrl() {
		String expected = String.valueOf((char)CTRL_KEY);
		SudoKeyDownEvent ctrlPress = new SudoKeyDownEvent(CTRL_KEY, false, false, false);
		assertArrayEquals(expected.getBytes(), termHandler.processCode(new Code(ctrlPress)).getBytes()); //Holding down Ctr
	}
	
	@Test
	public void testOnKeyPressCtrlD() {
		String expected = String.valueOf((char)(0x04)); //Ctrl-D
		String ctrlString = String.valueOf((char)CTRL_KEY);
		SudoKeyDownEvent ctrlPress = new SudoKeyDownEvent(CTRL_KEY, false, false, false);
		SudoKeyPressEvent dPress = new SudoKeyPressEvent(0x64, true, false, false);
		assertArrayEquals(ctrlString.getBytes(), termHandler.processCode(new Code(ctrlPress)).getBytes()); //Holding down Ctr
		assertArrayEquals(expected.getBytes(), termHandler.processCode(new Code(dPress)).getBytes()); //Pressing 'd' on keyboard
	}
	
	@Test
	public void testOnKeyDownCtrlBackslash() {
		String expected = String.valueOf((char)(0x1C)); //Ctrl-\
		String ctrlString = String.valueOf((char)CTRL_KEY);
		SudoKeyDownEvent ctrlPress = new SudoKeyDownEvent(CTRL_KEY, false, false, false);
		SudoKeyDownEvent bSlashPress = new SudoKeyDownEvent(BACKSLASH, true, false, false); 
		assertArrayEquals(ctrlString.getBytes(), termHandler.processCode(new Code(ctrlPress)).getBytes()); //Holding down Ctr
		assertArrayEquals(expected.getBytes(), termHandler.processCode(new Code(bSlashPress)).getBytes()); //Pressing '\' on keyboard
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
