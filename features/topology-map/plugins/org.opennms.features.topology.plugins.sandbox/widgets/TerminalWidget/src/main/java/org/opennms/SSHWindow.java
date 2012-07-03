package org.opennms;

import org.apache.sshd.ClientSession;

import com.vaadin.Application;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

@SuppressWarnings("serial")
public class SSHWindow extends Window {

	private SSHTerminal terminal;
	
	public SSHWindow(TerminalApplication app, ClientSession session, int width, int height) {
		setCaption("SSH");
		setReadOnly(true);
		setImmediate(true);
		setResizable(false);
		this.setWidth("" + width + "px");
		this.setHeight(""+ height + "px");
		terminal = new SSHTerminal(app, this, 80, 24);
		VerticalLayout vPanel = new VerticalLayout();
		vPanel.setWidth("100%");
		vPanel.setHeight("100%");
		int posX = (int)(app.getMainWindow().getWidth() - this.getWidth())/2;
		int posY = (int)(app.getMainWindow().getHeight() - this.getHeight())/2;
		this.setPositionX(posX);
		this.setPositionY(posY);
		vPanel.addComponent(terminal);
		vPanel.setComponentAlignment(terminal, Alignment.TOP_CENTER);
//		final Button closeButton = new Button("Close");
//		closeButton.addListener(this);
//		vPanel.addComponent(closeButton);
//		vPanel.setComponentAlignment(closeButton, Alignment.BOTTOM_CENTER);
        addComponent(vPanel);
	}

//	public void buttonClick(ClickEvent event) {
//		boolean doneClosing = false;
//		try {
//			doneClosing = terminal.closeClient();
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		if (doneClosing){
//			getApplication().getMainWindow().executeJavaScript("vaadin.forceSync();");
//			getApplication().getMainWindow().removeWindow(this);
//		} else {
//			getWindow().showNotification("Window was not closed properly");
//		}
//	}
}
