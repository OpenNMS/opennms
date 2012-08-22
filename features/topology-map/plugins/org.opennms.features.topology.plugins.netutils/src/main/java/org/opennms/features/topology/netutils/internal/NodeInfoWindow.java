package org.opennms.features.topology.netutils.internal;

import java.net.MalformedURLException;
import java.net.URL;

import com.vaadin.terminal.ExternalResource;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * The NodeInfoWindow class constructs a custom Window component that contains an embedded 
 * browser displaying the Node information of the currently selected node
 * @author Leonardo Bell
 * @author Philip Grenon
 * @version 1.0
 */
@SuppressWarnings("serial")
public class NodeInfoWindow extends Window {

    private final double sizePercentage = 0.80; // Window size ratio to the main window
    private final int widthCushion = 50; //Border cushion for width of window;
    private final int heightCushion = 110; //Border cushion for height of window
    private URL nodeInfoURL = null; //Web address of the Resources Graphs page
    private Embedded nodeInfoBrowser = null; //Browser component which is directed at the Resource Graphs page
	private final String noLabel = "no such label"; //Label given to vertexes that have no real label.

    /**
     * The NodeInfoWindow method constructs a sub-window instance which can be added to a main window.
     * The sub-window contains an embedded browser which displays the Node Info page of the currently selected
     * node.
     * @param node Selected node
     * @param width Width of the main window
     * @param height Height of the main window
     * @throws MalformedURLException
     */
    public NodeInfoWindow(Node n, String baseURL) throws MalformedURLException{
        
		Node node = n;
		if (node == null) {
			node = new Node(-1, "", "");
		}
		/*Sets the URLs to the currently selected node that is passed in and initializes the browsers*/
		if (node.getNodeID() >= 0) {
			baseURL += node.getNodeID();
		}
    	/*Sets the web address to the Resource Graphs page of the selected node and initializes the browser*/
    	nodeInfoURL = new URL(baseURL);
        nodeInfoBrowser = new Embedded("", new ExternalResource(nodeInfoURL));

		String label = node.getLabel();
		/*Sets up window settings*/
		if (label == null || label.equals("") || label.equalsIgnoreCase(noLabel)) {
			label = "";
		} else label = " - " + label;
		setCaption("Node Info" + label);
        setImmediate(true);
        setResizable(false);
        
        /*Adds the browser to the main layout*/
        VerticalLayout layout = new VerticalLayout();
        layout.addComponent(nodeInfoBrowser);

        addComponent(layout);
    }
    
    @Override
    public void attach() {
    	super.attach();
    	
    	int width = (int)getApplication().getMainWindow().getWidth();
    	int height = (int)getApplication().getMainWindow().getHeight();
    	
    	/*Sets the browser and window sizes based on the main window*/
        int browserWidth = (int)(sizePercentage * width), browserHeight = (int)(sizePercentage * height);
        int windowWidth = browserWidth + widthCushion, windowHeight = browserHeight + heightCushion;
        setWidth("" + windowWidth + "px");
        setHeight("" + windowHeight + "px");
        setPositionX((width - windowWidth)/2);
		setPositionY((height - windowHeight)/2);
        
        /*Sets the size of the browser to fit within the sub-window*/
        nodeInfoBrowser.setType(Embedded.TYPE_BROWSER);
        nodeInfoBrowser.setWidth("" + browserWidth + "px");
        nodeInfoBrowser.setHeight("" + browserHeight + "px");
    }
    
}
