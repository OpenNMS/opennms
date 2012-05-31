package org.opennms.sandbox;

import java.net.MalformedURLException;
import java.net.URL;
import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

@SuppressWarnings("serial")
public class EventsAlarmsWindow extends Window {

	private final double sizePercentage = 0.80; // Window size proportionate to main window
	private final int widthCushion = 50; //Border cushion for width of window;
	private final int heightCushion = 110; //Border cushion for height of window
	private final URL eventsURL = new URL("http://demo.opennms.org/opennms/event/list");
	private final URL alarmsURL = new URL("http://demo.opennms.org/opennms/alarm/list.htm?display=long");
	private Embedded eventsBrowser = new Embedded("", new ExternalResource(eventsURL));
	private Embedded alarmsBrowser = new Embedded("", new ExternalResource(alarmsURL));

	
	public EventsAlarmsWindow(float width, float height) throws MalformedURLException {
		
		int browserWidth = (int)(sizePercentage * width), browserHeight = (int)(sizePercentage * height);
		int windowWidth = browserWidth + widthCushion, windowHeight = browserHeight + heightCushion;
		
		setCaption("Events & Alarms");
		setImmediate(true);
		setResizable(false);
		setWidth("" + windowWidth + "px");
		setHeight("" + windowHeight + "px");
		setPositionX((int)((1.0 - windowWidth/width)/2.0 * width));
		setPositionY((int)((1.0 - windowHeight/height)/2.0 * height));
		
		VerticalLayout layout = new VerticalLayout();
		TabSheet tabsheet = new TabSheet();
		alarmsBrowser.setType(Embedded.TYPE_BROWSER);
		alarmsBrowser.setWidth("" + browserWidth + "px");
		alarmsBrowser.setHeight("" + browserHeight + "px");
		eventsBrowser.setType(Embedded.TYPE_BROWSER);
		eventsBrowser.setWidth("" + browserWidth + "px");
		eventsBrowser.setHeight("" + browserHeight + "px");
		tabsheet.addTab(eventsBrowser, "Events");
		tabsheet.addTab(alarmsBrowser, "Alarms");
		
		layout.addComponent(tabsheet);
		addComponent(layout);
	}
	
}
