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
		fPanel.getElement().setId("termFocusPanel");
		div = DOM.createDiv();
		div.setClassName("term");
		DOM.appendChild(fPanel.getElement(), div);
		initWidget(fPanel);
	}

	/**
	 * The addKeyUpHandler method allows other classes to assign KeyUpHandlers 
	 * to the FocusPanel within this widget
	 */
        @Override
	public HandlerRegistration addKeyUpHandler(KeyUpHandler handler) {
		return fPanel.addKeyUpHandler(handler);
	}

	/**
	 * The addKeyDownHandler method allows other classes to assign KeyDownHandlers 
	 * to the FocusPanel within this widget
	 */
        @Override
	public HandlerRegistration addKeyDownHandler(KeyDownHandler handler) {
		return fPanel.addKeyDownHandler(handler);
	}

	/**
	 * The addKeyPressHandler method allows other classes to assign KeyPressHandlers 
	 * to the FocusPanel within this widget
	 */
        @Override
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
    
    public void focus() {
    	fPanel.getElement().focus();
    }
    
}