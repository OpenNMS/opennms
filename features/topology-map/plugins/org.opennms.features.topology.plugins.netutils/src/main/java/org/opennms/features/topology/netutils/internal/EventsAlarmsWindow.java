/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * The EventsAlarmsWindow class constructs a custom Window component which
 * contains an embedded browser for both the Events page and Alarm page of the selected node.
 * Tabs are used to switch back and forth between the two.
 * @author Leonardo Bell
 * @author Philip Grenon
 * @version 1.0
 */
@SuppressWarnings("serial")
public class EventsAlarmsWindow extends Window {

	private static final double sizePercentage = 0.80; // Window size ratio to the main window
	private static final int widthCushion = 50; //Border cushion for width of window;
	private static final int heightCushion = 110; //Border cushion for height of window
	private Embedded eventsBrowser = null; //Browser component which is directed at the events page
	private Embedded alarmsBrowser = null; //Browser component which is directed at the alarms page
	private static final String noLabel = "no such label"; //Label given to vertexes that have no real label.
    private TabSheet m_tabSheet;

	private static String makeLabel(final Node node) {
		String label = node == null? "" : node.getLabel();
		/*Sets up window settings*/
		if (label == null || label.equals("") || label.equalsIgnoreCase(noLabel)) {
			label = "";
		} else {
			label = " - " + label;
		}
		return label;
	}

	/**
	 * The EventsAlarmsWindow method constructs a sub-window instance which can be added to a
	 * main window. The sub-window contains two embedded browsers which are directed at the Events
	 * and Alarms page of the selected node
	 * @param node Selected node
	 * @param width Width of main window
	 * @param height Height of main window
	 * @throws MalformedURLException
	 */
	public EventsAlarmsWindow(final Node node, final URL eventsURL, final URL alarmsURL) {
		super("Events & Alarms" + makeLabel(node));
		eventsBrowser = new Embedded("", new ExternalResource(eventsURL));
        eventsBrowser.setSizeFull();
		alarmsBrowser = new Embedded("", new ExternalResource(alarmsURL));
        alarmsBrowser.setSizeFull();
		
		setImmediate(true);
		setResizable(false);
		
		/*Adds the two browsers to separate tabs in a tabsheet layout*/
        m_tabSheet = new TabSheet();
        m_tabSheet.setSizeFull();
        m_tabSheet.addTab(eventsBrowser, "Events");
        m_tabSheet.addTab(alarmsBrowser, "Alarms");
		
		/*Adds tabsheet component to the main layout of the sub-window*/
		VerticalLayout layout = new VerticalLayout();
		layout.addComponent(m_tabSheet);
		layout.setSizeFull();
		setContent(layout);

	}
	
	@Override
	public void attach() {
		super.attach();
		
		int width = (int)getUI().getPage().getBrowserWindowWidth();
    	int height = (int)getUI().getPage().getBrowserWindowHeight();

		/*Sets the browser and window sizes based on the main window*/
		int browserWidth = (int)(sizePercentage * width);
        int browserHeight = (int)(sizePercentage * height);
		setWidth("" + browserWidth + "px");
		setHeight("" + browserHeight + "px");
		setPositionX((width - browserWidth)/2);
		setPositionY((height - browserHeight)/2);
        int viewHeight = browserHeight - 76;

		/*Changes the size of the browsers to fit within the sub-window*/
        alarmsBrowser.setType(Embedded.TYPE_BROWSER);
        alarmsBrowser.setHeight(viewHeight + "px");
        eventsBrowser.setType(Embedded.TYPE_BROWSER);
        eventsBrowser.setHeight(viewHeight + "px"); //424 When I set it to this size it works but otherwise its doesn't
	}
	
}
