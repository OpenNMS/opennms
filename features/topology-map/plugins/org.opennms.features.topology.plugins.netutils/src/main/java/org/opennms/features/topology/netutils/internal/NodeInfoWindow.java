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
