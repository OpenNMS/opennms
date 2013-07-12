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

package org.opennms.features.topology.netutils.internal;

import java.net.MalformedURLException;
import java.net.URL;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * The ResourceGraphsWindow class constructs a custom Window component which contains an
 * embedded browser that displays the Resource Graphs page of the currently selected node.
 * @author Leonardo Bell
 * @author Philip Grenon
 * @version 1.0
 */
@SuppressWarnings("serial")
public class ResourceGraphsWindow extends Window {

	private final double sizePercentage = 0.80; // Window size ratio to the main window
	private final int widthCushion = 50; //Border cushion for width of window;
	private final int heightCushion = 110; //Border cushion for height of window
	private Embedded rgBrowser = null; //Browser component which is directed at the Resource Graphs page
	private final String noLabel = "no such label"; //Label given to vertexes that have no real label.
	
	/**
	 * The ResourceGraphsWindow method constructs a sub-window instance which can be added to a
	 * main window. The sub-window contains an embedded browser which displays the Resource Graphs
	 * page of the currently selected node
	 * @param node Selected node
	 * @param nodeURL Node URL
	 * @throws MalformedURLException
	 */
	public ResourceGraphsWindow(final Node node, final URL nodeURL) throws MalformedURLException{
		
		rgBrowser = new Embedded("", new ExternalResource(nodeURL));
		
		String label = node == null? "" : node.getLabel();
		/*Sets up window settings*/
		if (label == null || label.equals("") || label.equalsIgnoreCase(noLabel)) {
			label = "";
		} else {
		    label = " - " + label;
		}
		setCaption("Resource Graphs" + label);
		setImmediate(true);
		setResizable(false);
		
		/*Adds the browser component to the main layout*/
		VerticalLayout layout = new VerticalLayout();
		layout.addComponent(rgBrowser);
		
		setContent(layout);
	}
	
	@Override
	public void attach() {
		super.attach();
		
		int width = getUI().getPage().getBrowserWindowWidth();
    	int height = getUI().getPage().getBrowserWindowHeight();
    	
		/*Sets the browser and window size based on the main window*/
		int browserWidth = (int)(sizePercentage * width), browserHeight = (int)(sizePercentage * height);
		int windowWidth = browserWidth + widthCushion, windowHeight = browserHeight + heightCushion;
		setWidth("" + windowWidth + "px");
		setHeight("" + windowHeight + "px");
		setPositionX((width - windowWidth)/2);
		setPositionY((height - windowHeight)/2);
		
		/*Changes the size of the browser to fit within the sub-window*/
		rgBrowser.setType(Embedded.TYPE_BROWSER);
		rgBrowser.setWidth("" + browserWidth + "px");
		rgBrowser.setHeight("" + browserHeight + "px");
	}
}
