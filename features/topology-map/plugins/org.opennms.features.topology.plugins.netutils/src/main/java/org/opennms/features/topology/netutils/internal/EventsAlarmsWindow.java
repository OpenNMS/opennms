/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.features.topology.netutils.internal;

import java.net.MalformedURLException;
import java.net.URL;

import com.vaadin.server.ExternalResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.TabSheet;
import com.vaadin.v7.ui.VerticalLayout;
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
	 * @throws MalformedURLException
	 */
	public EventsAlarmsWindow(final Node node, final URL eventsURL, final URL alarmsURL) {
		super("Events & Alarms" + makeLabel(node));
		eventsBrowser = new Embedded("", new ExternalResource(eventsURL));
        eventsBrowser.setSizeFull();
		alarmsBrowser = new Embedded("", new ExternalResource(alarmsURL));
        alarmsBrowser.setSizeFull();
		
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
