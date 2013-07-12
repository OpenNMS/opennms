package org.opennms.features.topology.ssh.internal.gwt.client.ui;

import org.opennms.features.topology.ssh.internal.SSHTerminal;
import org.opennms.features.topology.ssh.internal.SSHTerminalState;

import com.vaadin.client.communication.StateChangeEvent;
import com.vaadin.client.ui.AbstractComponentConnector;
import com.vaadin.shared.ui.Connect;

@Connect(SSHTerminal.class)
public class TerminalConnector extends AbstractComponentConnector {

	private static final long serialVersionUID = 5235402876358956383L;

	@Override
    public VTerminal getWidget() {
        return (VTerminal) super.getWidget();
    }
	
	@Override
	public SSHTerminalState getState() {
	    return (SSHTerminalState) super.getState();
	}
	
	@Override
	public void onStateChanged(StateChangeEvent event) {
	    super.onStateChanged(event);
	}
}
