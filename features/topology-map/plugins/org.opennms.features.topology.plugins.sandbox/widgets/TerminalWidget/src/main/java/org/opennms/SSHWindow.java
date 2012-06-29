package org.opennms;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class SSHWindow extends Window {

	public SSHWindow(int width, int height) {
		setCaption("SSH");
		this.setWidth("" + width + "px");
		this.setHeight(""+ height + "px");
		SSHTerminal terminal = new SSHTerminal(80, 24);
		HorizontalLayout hPanel = new HorizontalLayout();
		hPanel.addComponent(terminal);
		hPanel.setComponentAlignment(terminal, Alignment.MIDDLE_CENTER);
        addComponent(hPanel);
	}
}
