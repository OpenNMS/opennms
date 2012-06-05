package org.opennms.sandbox;

import java.net.MalformedURLException;
import java.net.URL;
import com.vaadin.terminal.ExternalResource;
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

	private final double sizePercentage = 0.80; // Window size ratio to the main window
	private final int widthCushion = 50; //Border cushion for width of window;
	private final int heightCushion = 110; //Border cushion for height of window
	private URL eventsURL = null; //Web address for the events page of the selected node
	private URL alarmsURL = null; //Web address for the alarms page of the selected node
	private Embedded eventsBrowser = null; //Browser component which is directed at the events page
	private Embedded alarmsBrowser = null; //Browser component which is directed at the alarms page

	/**
	 * The EventsAlarmsWindow method constructs a sub-window instance which can be added to a
	 * main window. The sub-window contains two embedded browsers which are directed at the Events
	 * and Alarms page of the selected node
	 * @param node Selected node
	 * @param width Width of main window
	 * @param height Height of main window
	 * @throws MalformedURLException
	 */
	public EventsAlarmsWindow(Node node, float width, float height) throws MalformedURLException {
		
		/*Sets the URLs to the currently selected node that is passed in and initializes the browsers*/
		eventsURL = new URL("http://demo.opennms.org/opennms/event/list?filter=node%3D" + node.getNodeID());
		alarmsURL = new URL("http://demo.opennms.org/opennms/alarm/list.htm?sortby=id&acktype=unack&limit=20&filter=node%3D" + node.getNodeID());
		eventsBrowser = new Embedded("", new ExternalResource(eventsURL));
		alarmsBrowser = new Embedded("", new ExternalResource(alarmsURL));
		
		/*Sets the browser and window sizes based on the main window*/
		int browserWidth = (int)(sizePercentage * width), browserHeight = (int)(sizePercentage * height);
		int windowWidth = browserWidth + widthCushion, windowHeight = browserHeight + heightCushion;
		
		/*Setting up the properties of the sub-window*/
		setCaption("Events & Alarms - " + node.getName());
		setImmediate(true);
		setResizable(false);
		setWidth("" + windowWidth + "px");
		setHeight("" + windowHeight + "px");
		setPositionX((int)((1.0 - windowWidth/width)/2.0 * width));
		setPositionY((int)((1.0 - windowHeight/height)/2.0 * height));
		
		/*Changes the size of the browsers to fit within the sub-window*/
		alarmsBrowser.setType(Embedded.TYPE_BROWSER);
		alarmsBrowser.setWidth("" + browserWidth + "px");
		alarmsBrowser.setHeight("" + browserHeight + "px");
		eventsBrowser.setType(Embedded.TYPE_BROWSER);
		eventsBrowser.setWidth("" + browserWidth + "px");
		eventsBrowser.setHeight("" + browserHeight + "px");
		
		/*Adds the two browsers to separate tabs in a tabsheet layout*/
		TabSheet tabsheet = new TabSheet();
		tabsheet.addTab(eventsBrowser, "Events");
		tabsheet.addTab(alarmsBrowser, "Alarms");
		
		/*Adds tabsheet component to the main layout of the sub-window*/
		VerticalLayout layout = new VerticalLayout();
		layout.addComponent(tabsheet);
		
		addComponent(layout);
	}
	
}
