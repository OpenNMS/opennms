package org.opennms.gwt.client.ui;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.DivElement;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FocusWidget;

/**
 * A regular GWT component without integration with IT Mill Toolkit.
 */
public class GwtTerminal extends FocusWidget {
	
	private static GwtTerminalUiBinder uiBinder = GWT
			.create(GwtTerminalUiBinder.class);

	interface GwtTerminalUiBinder extends UiBinder<DivElement, GwtTerminal> {
	}

	@UiField
	DivElement focusPanel;
    
    public GwtTerminal() {
        setElement(uiBinder.createAndBindUi(this));
    }
    
    public void dump(String receivedBytes) {
    	focusPanel.setInnerHTML(receivedBytes);
    }

	public void fireEvent(GwtEvent<?> event) {
		// TODO Auto-generated method stub
		
	}
}