package org.opennms.features.topology.ssh.internal.gwt.client.ui;

import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;

/**
 * The Code class takes generic KeyEvents and extracts all relevant information from them.
 * @author Leonardo Bell
 * @author Philip Grenon
 */
public class Code {
	
	private int keyCode = 0; //Key code from the passed in event
	private int charCode = 0; //Char code from the passed in event
	private KeyPressEvent kP_Event = null; //remains null unless event is an instance of KeyPressEvent
	private KeyDownEvent kD_Event = null; //remains null unless event is an instance of KeyDownEvent
	private boolean isCtrlDown; //Whether the CTRL key is currently held down or not
	private boolean isAltDown; //Whether the ALT key is currently held down or not
	private boolean isShiftDown; //Whether the SHIFT key is current held down or not
	private boolean isFunctionKey; //Whether the event was a function key or not
	/*List of special key codes*/
	private final int[] keyCodes = new int[] { 9, 8, 13, 27, 33, 34, 35, 36, 37, 38, 39, 40, 45, 46, 112,
															   113, 114, 115, 116, 117, 118, 119, 120, 121, 122, 123 };
	
	/**
	 * The Code(KeyEvent event) constructor takes a generic KeyEvent and decides whether
	 * it is a KeyPressEvent or KeyDownEvent. Any relevant information about the event
	 * is extracted and stored in class variables
	 * @param event generic KeyEvent
	 */
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
		
		if (kP_Event != null){
			charCode = kP_Event.getUnicodeCharCode();
		} else if (kD_Event != null){
			keyCode = kD_Event.getNativeKeyCode();
		} 
		
		isFunctionKey = false;
		for (int k : keyCodes){
			if (keyCode == k) {
				isFunctionKey = true;
				break;
			}
		}
	}
	
	/**
	 * The getCharCode method returns the Char code extracted from the event
	 * @return Char code of event
	 */
	public int getCharCode() {
		return charCode;
	}
	
	/**
	 * The getKeyCode method returns the Key code extracted from the event
	 * @return Key code of event
	 */
	public int getKeyCode() {
		return keyCode;
	}
	
	/**
	 * The isCtrlDown method returns whether the CTRL key was held down
	 * during the event
	 * @return Whether CTRL was held down
	 */
	public boolean isCtrlDown() {
		return isCtrlDown;
	}
	
	/**
	 * The isAltDown method returns whether the ALT key was held down
	 * during the event
	 * @return Whether ALT was held down
	 */
	public boolean isAltDown() {
		return isAltDown;
	}
	
	/**
	 * The isShitDown method returns whether the SHIFT key was held down
	 * during the event
	 * @return Whether SHIFT was held down
	 */
	public boolean isShiftDown() {
		return isShiftDown;
	}
	
	/**
	 * The isFunctionKey method returns whether the event was
	 * a special, non printable key
	 * @return Whether the event was a function Key
	 */
	public boolean isFunctionKey() {
		return isFunctionKey;
	}
	
	/**
	 * The isFunctionKey method returns whether the event was
	 * the CTRL key
	 * @return Whether the event was a CTLR Key
	 */
	public boolean isControlKey() {
		return (getKeyCode() >= 16 && getKeyCode() <= 18);
	}
	
}
