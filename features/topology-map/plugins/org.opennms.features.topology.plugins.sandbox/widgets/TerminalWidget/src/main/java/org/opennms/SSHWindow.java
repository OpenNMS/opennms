package org.opennms;

import org.apache.sshd.ClientSession;

import com.vaadin.ui.Alignment;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class SSHWindow extends Window {

	private SSHTerminal terminal;
	
	public SSHWindow(TerminalApplication app, ClientSession session, int width, int height) {
		setCaption("SSH");
		setImmediate(true);
		setResizable(false);
		this.setWidth("" + width + "px");
		this.setHeight(""+ height + "px");
		terminal = new SSHTerminal(app, this, session, 80, 24);
		VerticalLayout vPanel = new VerticalLayout();
		vPanel.setWidth("100%");
		vPanel.setHeight("100%");
		int posX = (int)(app.getMainWindow().getWidth() - this.getWidth())/2;
		int posY = (int)(app.getMainWindow().getHeight() - this.getHeight())/2;
		this.setPositionX(posX);
		this.setPositionY(posY);
		vPanel.addComponent(terminal);
		vPanel.setComponentAlignment(terminal, Alignment.TOP_CENTER);
        addComponent(vPanel);
	}
	
	@Override
	public void close(){
		terminal.close();
	}

}
