package org.opennms.sandbox;

import java.net.MalformedURLException;
import java.net.URL;
import com.vaadin.terminal.ExternalResource;
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
	private URL rgURL = null; //Web address for the Resource Graphs page
	private Embedded rgBrowser = null; //Browser component which is directed at the Resource Graphs page
	
	/**
	 * The ResourceGraphsWindow method constructs a sub-window instance which can be added to a
	 * main window. The sub-window contains an embedded browser which displays the Resource Graphs
	 * page of the currently selected node
	 * @param node Selected node
	 * @param width Width of the main window
	 * @param height Height of the main window
	 * @throws MalformedURLException
	 */
	public ResourceGraphsWindow (Node node, float width, float height) throws MalformedURLException{
		
		/*Sets the URLs to the currently selected node that is passed in and initializes the browsers*/
		rgURL = new URL("http://demo.opennms.org/opennms/graph/chooseresource.htm?reports=all&parentResourceId=node" + "[" + node.getNodeID() + "]");
		rgBrowser = new Embedded("", new ExternalResource(rgURL));
		
		/*Sets the browser and window size based on the main window*/
		int browserWidth = (int)(sizePercentage * width), browserHeight = (int)(sizePercentage * height);
		int windowWidth = browserWidth + widthCushion, windowHeight = browserHeight + heightCushion;
		
		/*Sets the properties of the sub-window*/
		setCaption("Resource Graphs - " + node.getName());
		setImmediate(true);
		setResizable(false);
		setWidth("" + windowWidth + "px");
		setHeight("" + windowHeight + "px");
		setPositionX((int)((1.0 - windowWidth/width)/2.0 * width));
		setPositionY((int)((1.0 - windowHeight/height)/2.0 * height));
		
		/*Changes the size of the browser to fit within the sub-window*/
		rgBrowser.setType(Embedded.TYPE_BROWSER);
		rgBrowser.setWidth("" + browserWidth + "px");
		rgBrowser.setHeight("" + browserHeight + "px");
		
		/*Adds the browser component to the main layout*/
		VerticalLayout layout = new VerticalLayout();
		layout.addComponent(rgBrowser);
		
		addComponent(layout);
	}
}
