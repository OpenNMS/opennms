/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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
import java.util.Scanner;

import com.vaadin.server.ExternalResource;
import com.vaadin.shared.ui.MarginInfo;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.Notification;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.VerticalSplitPanel;
import com.vaadin.ui.Window;
import com.vaadin.ui.Button.ClickEvent;

/**
 * The TracerouteWindow class creates a Vaadin Sub-window with a form and results section
 * for the Traceroute functionality of a Node.
 * @author Leonardo Bell
 * @author Philip Grenon
 * @version 1.0
 */
@SuppressWarnings("serial")
public class TracerouteWindow extends Window {

	private final double sizePercentage = 0.80; // Window size proportionate to main window
	protected NativeSelect ipDropdown = null; //Dropdown component for IP Address
	private Label nodeLabel = null; //Label displaying the name of the Node at the top of the window
	protected TextField forcedHopField = null; //Textfield for the "Forced Hop" variable
	protected CheckBox numericalDataCheckBox = null; //Checkbox which toggles numeric output
	protected Button tracerouteButton; //Button to execute the traceroute operation
	private Embedded resultsBrowser = null; //Browser component which displays the results of Traceroute
	private VerticalLayout topLayout = null; //Contains the form for Traceroute
	private VerticalLayout bottomLayout = null; //Contains the results Browser for a Traceroute
	private VerticalSplitPanel vSplit = null; //Splits up the top and bottom layout
	private int margin = 40; //Padding around the results browser
	private int splitHeight = 180;//Height from top of the window to the split location in pixels
	private int topHeight = 220;//Set height size for everything above the split
	private final String noLabel = "no such label"; //Label given to vertexes that have no real label.
	private String tracerouteUrl;

	/**
	 * The TracerouteWindow method constructs a TracerouteWindow component with a size proportionate to the 
	 * width and height of the main window.
	 * @param node 
	 * @param width Width of Main window
	 * @param height Height of Main window
	 */
	public TracerouteWindow(final Node node, final String url) {

		this.tracerouteUrl = url;
		
		String label = "";
		String ipAddress = "";
		if (node != null) {
			label = node.getLabel();
			ipAddress = node.getIPAddress();
		}
		String caption = "";
		/*Sets up window settings*/
		if (label == null || label.equals("") || label.equalsIgnoreCase(noLabel)) {
			label = "";
		} 
		if (!label.equals("")) caption = " - " + label;
		setCaption("Traceroute" + caption);
		setImmediate(true);
		setResizable(false);

		/*Initialize the header of the Sub-window with the name of the selected Node*/
		String nodeName = "<div style=\"text-align: center; font-size: 18pt; font-weight:bold;\">" + label + "</div>";
		nodeLabel = new Label(nodeName);
		nodeLabel.setContentMode(ContentMode.HTML);

		/*Creating various layouts to encapsulate all of the components*/
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		vSplit = new VerticalSplitPanel();
		topLayout = new VerticalLayout();
		bottomLayout = new VerticalLayout();
		VerticalLayout form = new VerticalLayout();
		GridLayout grid = new GridLayout(2,2);
		grid.setWidth("420");
		grid.setHeight("62");

		/*Sets up IP Address dropdown with the Name as default*/
		ipDropdown = new NativeSelect();
		ipDropdown.addItem(ipAddress);
		ipDropdown.select(ipAddress);

		/*Creates the Numerical Output Check box and sets up the listener*/
		numericalDataCheckBox = new CheckBox("Use Numerical Node Names");
		numericalDataCheckBox.setImmediate(true);
		numericalDataCheckBox.setValue(false);

		/*Creates the form labels and text fields*/
		Label ipLabel = new Label("IP Address: ");
		Label forcedHopLabel = new Label("Forced Hop IP: ");
		forcedHopField = new TextField();
		forcedHopField.setMaxLength(15);

		/*Add all of the components to the GridLayout*/
		grid.addComponent(ipLabel);
		grid.setComponentAlignment(ipLabel, Alignment.MIDDLE_LEFT);
		grid.addComponent(ipDropdown);
		grid.setComponentAlignment(ipDropdown, Alignment.MIDDLE_LEFT);
		grid.addComponent(forcedHopLabel);
		grid.setComponentAlignment(forcedHopLabel, Alignment.MIDDLE_LEFT);
		grid.addComponent(forcedHopField);
		grid.setComponentAlignment(forcedHopField, Alignment.MIDDLE_LEFT);

		/*Creates the Ping button and sets up the listener*/
		tracerouteButton = new Button("Traceroute"); 
		tracerouteButton.addClickListener(new Button.ClickListener() {
                        @Override
			public void buttonClick(ClickEvent event) {
				changeBrowserURL(buildURL());
			}
		}); 

		/*Adds components to the form and sets the width and spacing*/
		form.addComponent(grid);
		form.addComponent(numericalDataCheckBox);
		form.addComponent(tracerouteButton);
		form.setWidth("100%");
		form.setSpacing(true);

		/*Adds components to the Top Layout and sets the width and margins*/
		topLayout.addComponent(nodeLabel);
		topLayout.setComponentAlignment(nodeLabel, Alignment.MIDDLE_CENTER);
		topLayout.addComponent(form);
		topLayout.setSizeFull();
		topLayout.setMargin(new MarginInfo(true, true, false, true));

		/*Adds components to the Bottom Layout and sets the width and margins*/
		bottomLayout.setSizeFull();
		bottomLayout.setMargin(true);
		bottomLayout.setImmediate(true);

		buildEmbeddedBrowser();

		/*Setting first and second components for the split panel and setting the panel divider position*/
		vSplit.setFirstComponent(topLayout);
		vSplit.setSecondComponent(bottomLayout);
		vSplit.setSplitPosition(splitHeight, Unit.PIXELS);
		vSplit.setLocked(true);

		/*Adds split panel to the main layout and expands the split panel to 100% of the layout space*/
		mainLayout.addComponent(vSplit);
		mainLayout.setExpandRatio(vSplit, 1);

		setContent(mainLayout);
	}

	@Override
	public void attach() {
		super.attach();

		int width = (int)getUI().getWidth();
		int height = (int)getUI().getHeight();

		int windowWidth = (int)(sizePercentage * width), windowHeight = (int)(sizePercentage * height);
		setWidth("" + windowWidth + "px");
		setHeight("" + windowHeight + "px");
		setPositionX((width - windowWidth)/2);
		setPositionY((height - windowHeight)/2);

		resultsBrowser.setWidth("" + (int)(this.getWidth()-margin) + "px"); //Cuts off "close" button from window
		resultsBrowser.setHeight("" + (int)(this.getHeight() - topHeight - margin) + "px");
	}

	/**
	 * The changeBrowserURL method changes the address of the results browser whenever a new
	 * traceroute request form is submitted and refreshes the browser.
	 * @param url New web address
	 */
	private void changeBrowserURL(URL url) {
		if (url != null) {
			/* This setVisible(false/true) toggle is used to refresh the browser.
			 * Due to to the fact that the updates to the client require a call to
			 * the server, this is currently one of the only ways to accomplish the
			 * the needed update.
			 */
			resultsBrowser.setVisible(false);
			resultsBrowser.setSource(new ExternalResource(url));
			resultsBrowser.setVisible(true);
		}
	}

	/**
	 * The buildURL method takes the current values in the form and formats them into the
	 * URL string that is used to redirect the results browser to the Traceroute page.
	 * @return Web address for traceroute command with submitted parameters
	 * @throws MalformedURLException
	 */
	protected URL buildURL() {
		boolean validInput = false;
		try {
			validInput = validateInput();
		} catch (Exception e) {
			Notification.show(e.getMessage(), Notification.Type.WARNING_MESSAGE);
			return null;
		}
		if (validInput) {
			final StringBuilder options = new StringBuilder(tracerouteUrl);
			try {
				URL baseUrl = getUI().getPage().getLocation().toURL();

				options.append(tracerouteUrl.contains("?") ? "&" : "?");

				options.append("address=").append(ipDropdown.getValue());
				if (!("".equals(forcedHopField.getValue().toString()))) {
					options.append("&hopAddress=").append(forcedHopField.getValue());
				}
				if (numericalDataCheckBox.getValue().equals(true)) {
					options.append("&numericOutput=true");
				}

				return new URL(baseUrl, options.toString());
			} catch (final MalformedURLException e) {
				Notification.show("Could not build URL: " + options.toString(), Notification.Type.WARNING_MESSAGE);
				return null;
			}
		} else {
			Notification.show("Invalid IP addresss", Notification.Type.WARNING_MESSAGE);
			return null;
		}
	}

	/**
	 * The validateInput method checks the forced hop text field to
	 * make sure the input given by the user was formatted correctly.
	 * @return Whether input was correctly formatted
	 * @throws Exception If an input field was left empty
	 */
	protected boolean validateInput() throws Exception {
		String forcedHop = forcedHopField.getValue().toString();
		if ("".equals(forcedHop)) return true;
		Scanner line = new Scanner(forcedHop);
		line.useDelimiter("[.]");
		int count = 0;
		while (line.hasNextInt()) {
			int n = line.nextInt();
			if (n < 0 || n > 255) return false; //Integers in an IP address must be within 0-255
			else count++;
		}
		return (count == 4); //IP address must has 4 integer values separated by a '.' 
	}

	/**
	 * The buildEmbeddedBrowser method creates a new browser instance and adds it to the 
	 * bottom layout. The browser is set to invisible by default.
	 */
	private void buildEmbeddedBrowser() {
		resultsBrowser = new Embedded();
		resultsBrowser.setType(Embedded.TYPE_BROWSER);
		resultsBrowser.setImmediate(true);
		resultsBrowser.setVisible(false);
		bottomLayout.addComponent(resultsBrowser);
	}

}
