package org.opennms;

import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class SSHWindow extends Window {

	public SSHWindow(int width, int height) {
		setCaption("SSH");
		setSizeUndefined();
		ColorPicker terminal = new ColorPicker();
        addComponent(terminal);
	}
}
