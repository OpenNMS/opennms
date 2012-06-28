package org.opennms.gwtterminal.client;

import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyEvent;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
//import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Element;


public class TermHandler implements KeyUpHandler, KeyDownHandler, KeyPressHandler{

//	private int sending = 0;
//	private Timer updateTimer = null;
	private KeyBuffer keybuf;
	private Element output;
	private Code code;

	public TermHandler(){
		keybuf = new KeyBuffer();
		code = null;
		this.output = null;
	}
	
	public TermHandler(Element output){
		keybuf = new KeyBuffer();
		code = null;
		this.output = output;
	}

	@Override
	public void onKeyPress(KeyPressEvent event) {
		code = new Code(event);
		if (code.getCharCode() > 31 && code.getCharCode() < 127) processCode(code);
		if (!keybuf.toString().equals("") && output != null){
			for (int i = 0; i < keybuf.toString().getBytes().length; i++){
				output.setInnerHTML(output.getInnerHTML() + "[ " + keybuf.toString().getBytes()[i] + " ]");
			}
		}
		keybuf = new KeyBuffer();
		event.stopPropagation();
		event.preventDefault();
	}

	@Override
	public void onKeyDown(KeyDownEvent event) {
		code = new Code(event);
		if (!code.isControlKey()){
			if (code.isFunctionKey() || code.isCtrlDown() || code.isAltDown()) processCode(code);
		}
		if (!keybuf.toString().equals("") && output != null){
			for (int i = 0; i < keybuf.toString().getBytes().length; i++){
				output.setInnerHTML(output.getInnerHTML() + "[ " + keybuf.toString().getBytes()[i] + " ]");
			}
		}
		keybuf = new KeyBuffer();
//		event.stopPropagation();
//		event.preventDefault();
	}

	public KeyBuffer getKeybuf() {
		return keybuf;
	}

	@Override
	public void onKeyUp(KeyUpEvent event) {}

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
			keybuf.add(buildCharacter(k, isCharCode));
		}
	}

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

//	private void queue(String s) {
//		keybuf.add(s);
//		if (sending == 0) {
//			if (updateTimer != null){
//				updateTimer.cancel();
//			}
//			updateTimer = new Timer(){
//				public void run(){
//					update();
//				}
//			};
//			updateTimer.schedule(1);
//		}
//	}
//
//	private void update() {
//		if (sending == 0) {
//			sending = 1;
//			String send = "";
//			while (keybuf.size() > 0) {
//				send.concat(keybuf.pop());
//			}
//			//TODO Vaadin server side stuff
//		}
//	}

}
