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
import com.vaadin.v7.ui.VerticalLayout;
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

	private static final double sizePercentage = 0.80; // Window size ratio to the main window
	private static final int widthCushion = 50; //Border cushion for width of window;
	private static final int heightCushion = 110; //Border cushion for height of window
	private Embedded rgBrowser = null; //Browser component which is directed at the Resource Graphs page
	private static final String noLabel = "no such label"; //Label given to vertexes that have no real label.
	
	/**
	 * The ResourceGraphsWindow method constructs a sub-window instance which can be added to a
	 * main window. The sub-window contains an embedded browser which displays the Resource Graphs
	 * page of the currently selected node
	 * @param node Selected node
	 * @param nodeURL Node URL
	 * @throws MalformedURLException
	 */
	public ResourceGraphsWindow(final Node node, final URL nodeURL) {
		
		rgBrowser = new Embedded("", new ExternalResource(nodeURL));
		
		String label = node == null? "" : node.getLabel();
		/*Sets up window settings*/
		if (label == null || label.equals("") || label.equalsIgnoreCase(noLabel)) {
			label = "";
		} else {
		    label = " - " + label;
		}
		setCaption("Resource Graphs" + label);
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
