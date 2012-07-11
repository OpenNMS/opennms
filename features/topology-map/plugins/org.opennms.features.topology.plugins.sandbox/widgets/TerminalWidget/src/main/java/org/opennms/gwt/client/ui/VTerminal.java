/* 
 * Copyright 2009 IT Mill Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opennms.gwt.client.ui;

import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;

public class VTerminal extends GwtTerminal implements Paintable {

	/** Component identifier in UIDL communications. */
	String uidlId;

	/** Reference to the server connection object. */
	ApplicationConnection client;

	private TermHandler termHandler;
	private String isClosed;

	/**
	 * The constructor should first call super() to initialize the component and
	 * then handle any initialization relevant to Vaadin.
	 */
	public VTerminal() {
		// The superclass has a lot of relevant initialization
		super();
		termHandler = new TermHandler(this);
		addKeyDownHandler(termHandler);
		addKeyPressHandler(termHandler);
		addKeyUpHandler(termHandler);
		isClosed = "false";
		
	}

	public void update() {
		termHandler.update();
	}

	/**
	 * This method must be implemented to update the client-side component from
	 * UIDL data received from server.
	 * 
	 * This method is called when the page is loaded for the first time, and
	 * every time UI changes in the component are received from the server.
	 */
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		// This call should be made first. Ensure correct implementation,
		// and let the containing layout manage caption, etc.
		if (client.updateComponent(this, uidl, true)) {
			return;
		}

		// Save reference to server connection object to be able to send
		// user interaction later
		this.client = client;
		// Save the UIDL identifier for the component
		this.uidlId = uidl.getId();
		if (uidl.getStringVariable("closeClient").equals("true")) {
			termHandler.close();
			isClosed = "true";
			sendBytes("");
		}
		if (uidl.getBooleanVariable("update")) update();
		dump(uidl.getStringVariable("fromSSH"));
	}

	public void sendBytes(String inputKeys){
		client.updateVariable(uidlId, "isClosed", isClosed, true);
		if (isClosed.equals("false")) {
			client.updateVariable(uidlId, "toSSH", inputKeys, true);
		}
	}

}
