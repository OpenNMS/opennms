package org.opennms.features.topology.netutils.internal;

import java.net.URL;

import org.opennms.features.topology.api.support.InfoWindow;

/**
 * The NodeInfoWindow class constructs a custom Window component that contains an embedded 
 * browser displaying the Node information of the currently selected node
 * @author Leonardo Bell
 * @author Philip Grenon
 * @version 1.0
 */
public class NodeInfoWindow extends InfoWindow {

	private static final long serialVersionUID = -9008855502553868300L;
	
	/**
	 * Label given to vertexes that have no real label.
	 */
	private static final String NO_LABEL_TEXT = "no such label"; 
	
	/**
     * The NodeInfoWindow method constructs a sub-window instance which can be added to a main window.
     * The sub-window contains an embedded browser which displays the Node Info page of the currently selected
     * node.
     * @param node Selected node
     * @param width Width of the main window
     * @param height Height of the main window
     */
	 public NodeInfoWindow(final Node node, final URL nodeURL) {
		 super(nodeURL, new LabelCreator() {
			
			@Override
			public String getLabel() {
				String label = node == null ? "" : node.getLabel();
				
		        /*Sets up window settings*/
		        if (label == null || label.equals("") || label.equalsIgnoreCase(NO_LABEL_TEXT)) {
		            label = "";
		        } else { 
		        	label = " - " + label; 
		        }
		        return "Node Info" + label;
			}
		});
	 }
}
