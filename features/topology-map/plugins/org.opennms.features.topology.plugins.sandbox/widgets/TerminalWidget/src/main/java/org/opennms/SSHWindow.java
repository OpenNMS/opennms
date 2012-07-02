package org.opennms;

import org.apache.sshd.ClientSession;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

@SuppressWarnings("serial")
public class SSHWindow extends Window {

	private final Window thisWindow;
	public SSHWindow(ClientSession session, int width, int height) {
		setCaption("SSH");
		setImmediate(true);
		thisWindow = this;
		setResizable(false);
		this.setWidth("" + width + "px");
		this.setHeight(""+ height + "px");
		final SSHTerminal terminal = new SSHTerminal(80, 24);
		VerticalLayout vPanel = new VerticalLayout();
		vPanel.setWidth("100%");
		vPanel.setHeight("100%");
		vPanel.addComponent(terminal);
		vPanel.setComponentAlignment(terminal, Alignment.TOP_CENTER);
		final Button closeButton = new Button("Close");
		closeButton.addListener(new Button.ClickListener() {
			public void buttonClick(ClickEvent event) {
				getApplication().getMainWindow().removeWindow(thisWindow);
			}
		});
		vPanel.addComponent(closeButton);
		vPanel.setComponentAlignment(closeButton, Alignment.BOTTOM_CENTER);
        addComponent(vPanel);
	}
}
