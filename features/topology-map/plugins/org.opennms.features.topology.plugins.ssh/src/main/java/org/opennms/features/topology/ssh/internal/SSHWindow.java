package org.opennms.features.topology.ssh.internal;

import org.apache.sshd.ClientSession;

import com.vaadin.terminal.Terminal;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Label;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * This class creates a window to hold the terminal emulator
 * @author lmbell
 * @author pdgrenon
 */
@SuppressWarnings("serial")
public class SSHWindow extends Window {
	private SSHTerminal terminal; // The terminal emulator
	private Label errorLabel = new Label("Could not create session");
	private final int TERM_WIDTH = 80;
	private final int TERM_HEIGHT = 24;
	
	/**
	 * The constructor for the SSH window
	 * 
	 * @param app The main application to which this window should be attached
	 * @param session The current SSH session
	 * @param width The width of the window
	 * @param height The height of the window
	 */
	public SSHWindow(ClientSession session, int width, int height) {
		setCaption("SSH");
		setImmediate(true);
		setResizable(false);
		setWidth("" + width + "px");
		setHeight(""+ height + "px");
		
		VerticalLayout vPanel = new VerticalLayout();
		vPanel.setWidth("100%");
		vPanel.setHeight("100%");
		
		if (session != null) {
			terminal = new SSHTerminal(this, session, TERM_WIDTH, TERM_HEIGHT);
			vPanel.addComponent(terminal);
			vPanel.setComponentAlignment(terminal, Alignment.TOP_CENTER);
		} else {
			vPanel.addComponent(errorLabel);
			vPanel.setComponentAlignment(errorLabel, Alignment.MIDDLE_CENTER);
		}
        
		addComponent(vPanel);
	}
	
	@Override
	public void attach() {
		super.attach();
		 
		int posX = (int)(getApplication().getMainWindow().getWidth() - getWidth())/2;
		int posY = (int)(getApplication().getMainWindow().getHeight() - getHeight())/2;
		setPositionX(posX);
		setPositionY(posY);
	}
	
	/**
	 * Overrides the window close method to instead close the terminal
	 */
	@Override
	public void close(){
		terminal.close();
	}
}
