/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.ssh.internal;

import org.apache.sshd.ClientSession;

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
	 * @param session The current SSH session
	 * @param width The width of the window
	 * @param height The height of the window
	 */
	public SSHWindow(ClientSession session, int width, int height) {
		super("SSH");
		setImmediate(true);
		setResizeLazy(false);
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
		setContent(vPanel);
	}
	
	@Override
	public void attach() {
		super.attach();
		 
		int posX = (int)(getUI().getPage().getBrowserWindowWidth() - getWidth())/2;
		int posY = (int)(getUI().getPage().getBrowserWindowHeight() - getHeight())/2;
		
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
