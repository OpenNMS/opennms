package org.opennms.features.topology.ssh.internal.gwt.client.ui;

import com.vaadin.terminal.gwt.client.ApplicationConnection;
import com.vaadin.terminal.gwt.client.Paintable;
import com.vaadin.terminal.gwt.client.UIDL;

/**
 * The VTerminal class is associated with the GwtTerminal widget and handles all of the communication 
 * from the client and sends it to the server.  It also listens for responses from the server and 
 * updates the client side view.
 * @author Leonardo Bell
 * @author Philip Grenon
 */
public class VTerminal extends GwtTerminal implements Paintable {

	String uidlId; //Component identifier in UIDL communications.
	ApplicationConnection client; //Reference to the server connection object.
	private TermHandler termHandler; //Key handler for VT100 codes
	private boolean isClosed; //Lets the server know the status of the Handler

	/**
	 * The VTerminal() constructor creates a GwtTerminal Widget and assigns the TermHandler
	 * to each of its key handlers and initializes the status of the Terminal
	 */
	public VTerminal() {
		super();
		termHandler = new TermHandler(this);
		addKeyDownHandler(termHandler);
		addKeyPressHandler(termHandler);
		addKeyUpHandler(termHandler);
		isClosed = false;
	}

	/**
	 * The update method is used by the server whenever it requests an update from 
	 * the TermHandler in order to receive the current key presses from the client.
	 */
	public void update() {
		termHandler.update();
	}

	/**
	 * The updateFromUIDL method handles all communication from the server and passes
	 * the data along to the GwtTerminal widget which updates the client side view.
	 */
	public void updateFromUIDL(UIDL uidl, ApplicationConnection client) {
		/************************************************ 
		This call should be made first. Ensure correct implementation,
		and let the containing layout manage caption, etc.
		************************************************/
		if (client.updateComponent(this, uidl, true)) {
			return;
		}
		/************************************************
		Save reference to server connection object to be able to send
		user interaction later
		************************************************/
		this.client = client;
		/************************************************
		Save the UIDL identifier for the component
		************************************************/
		this.uidlId = uidl.getId();
		/************************************************
		Check if the server wants the TermHandler to close, if so, send a
		response back to the server that it was closed successfully
		************************************************/
		if (uidl.getBooleanVariable("closeClient")) {
			termHandler.close();
			isClosed = true;
			sendBytes("");
		}
		/************************************************
		Check if the server wants the TermHandler to update manually
		************************************************/
		if (uidl.getBooleanVariable("update")) update();
		/************************************************
		Take the current representation of the Terminal from the server
		and set the Inner HTML of the widget
		************************************************/
		dump(uidl.getStringVariable("fromSSH"));
	}

	public void sendBytes(String inputKeys){
		/************************************************
		Send the server the current state of the TermHandler
		************************************************/
		client.updateVariable(uidlId, "isClosed", isClosed, true);
		/************************************************
		Send the server the current KeyBuffer from the client
		************************************************/
		if (!isClosed) {
			client.updateVariable(uidlId, "toSSH", inputKeys, true);
		}
	}

}
