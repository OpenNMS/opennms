package org.opennms.features.topology.ssh.internal.gwt.client.ui;

import com.google.gwt.event.dom.client.HasAllKeyHandlers;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.KeyPressHandler;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FocusPanel;

/**
 * The GwtTerminal class is a widget which emulates a VT100 terminal
 * @author Leonardo Bell
 * @author Philip Grenon
 */
public class GwtTerminal extends Composite implements HasAllKeyHandlers {

	private Element div; //Outer container for the Terminal <span>'s
	private FocusPanel fPanel; //A container that provides KeyHandling functionality

	/**
	 * The GwtTerminal() constructor sets up the layout of the widget and assigns
	 * CSS styles for HTML elements
	 */
	public GwtTerminal() {
		fPanel = new FocusPanel();
		fPanel.getElement().setClassName("focusPanel");
		div = DOM.createDiv();
		div.setClassName("term");
		DOM.appendChild(fPanel.getElement(), div);
		initWidget(fPanel);
	}

	/**
	 * The addKeyUpHandler method allows other classes to assign KeyUpHandlers 
	 * to the FocusPanel within this widget
	 */
	public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
		return fPanel.addKeyUpHandler(handler);
	}

	/**
	 * The addKeyDownHandler method allows other classes to assign KeyDownHandlers 
	 * to the FocusPanel within this widget
	 */
	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		return fPanel.addKeyDownHandler(handler);
	}

	/**
	 * The addKeyPressHandler method allows other classes to assign KeyPressHandlers 
	 * to the FocusPanel within this widget
	 */
	public HandlerRegistration addKeyPressHandler(KeyPressHandler handler) {
		return fPanel.addKeyPressHandler(handler);
	}
    
	/**
	 * The dump method sets the inner HTML of the main Div element.
	 * This method is called whenever the server sends the client the current
	 * representation of the Terminal
	 * @param receivedBytes
	 */
    public void dump(String receivedBytes) {
    	div.setInnerHTML(receivedBytes);
    }
    
}