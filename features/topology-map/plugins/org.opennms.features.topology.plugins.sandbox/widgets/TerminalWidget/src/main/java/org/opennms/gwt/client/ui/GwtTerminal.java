package org.opennms.gwt.client.ui;

import com.google.gwt.event.dom.client.HasAllKeyHandlers;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;

/**
 * A regular GWT component without integration with IT Mill Toolkit.
 */
public class GwtTerminal extends Composite implements HasAllKeyHandlers {

	private Element div;
	private FocusPanel fPanel;

	public GwtTerminal() {
		fPanel = new FocusPanel();
		fPanel.getElement().setClassName("focusPanel");
		div = DOM.createDiv();
		div.setClassName("term");
		DOM.appendChild(fPanel.getElement(), div);
		initWidget(fPanel);
	}

	

	public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
		return fPanel.addKeyUpHandler(handler);
	}

	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		return fPanel.addKeyDownHandler(handler);
	}

	public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
		return fPanel.addKeyPressHandler(handler);
	}
    
    public void dump(String receivedBytes) {
    	div.setInnerHTML(receivedBytes);
    }

	public void fireEvent(GwtEvent<?> event) {
		// TODO Auto-generated method stub
		
	}
}