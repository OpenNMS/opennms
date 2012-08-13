package org.opennms.features.topology.netutils.internal;

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
	private final String noLabel = "no such label"; //Label given to vertexes that have no real label.
	
	/**
	 * The ResourceGraphsWindow method constructs a sub-window instance which can be added to a
	 * main window. The sub-window contains an embedded browser which displays the Resource Graphs
	 * page of the currently selected node
	 * @param node Selected node
	 * @param width Width of the main window
	 * @param height Height of the main window
	 * @throws MalformedURLException
	 */
	public ResourceGraphsWindow(Node n, String baseURL) throws MalformedURLException{
		
		Node node = n;
		if (node == null) {
			node = new Node(-1, "", "");
		}
		/*Sets the URLs to the currently selected node that is passed in and initializes the browsers*/
		if (node.getNodeID() >= 0) {
			baseURL += ("[" + node.getNodeID() + "]");
		}
		
		/*Sets the URLs to the currently selected node that is passed in and initializes the browsers*/
		rgURL = new URL(baseURL);
		rgBrowser = new Embedded("", new ExternalResource(rgURL));
		
		String label = node.getLabel();
		/*Sets up window settings*/
		if (label == null || label.equals("") || label.equalsIgnoreCase(noLabel)) {
			label = "";
		} else label = " - " + label;
		setCaption("Resource Graphs" + label);
		setImmediate(true);
		setResizable(false);
		
		/*Adds the browser component to the main layout*/
		VerticalLayout layout = new VerticalLayout();
		layout.addComponent(rgBrowser);
		
		addComponent(layout);
	}
	
	@Override
	public void attach() {
		super.attach();
		
		int width = (int)getApplication().getMainWindow().getWidth();
    	int height = (int)getApplication().getMainWindow().getHeight();
    	
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
