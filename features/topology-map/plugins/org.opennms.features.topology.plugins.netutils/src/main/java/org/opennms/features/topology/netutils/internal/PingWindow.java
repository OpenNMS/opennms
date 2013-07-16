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
 * The PingWindow class creates a Vaadin Sub-window with a form and results section
 * for the Ping functionality of a Node.
 * @author Leonardo Bell
 * @author Philip Grenon
 * @version 1.0
 */
@SuppressWarnings("serial")
public class PingWindow extends Window{

	private final double sizePercentage = 0.80; // Window size proportionate to main window
	protected NativeSelect ipDropdown = null; //Dropdown component for IP Address
	protected NativeSelect packetSizeDropdown = null; //Dropdown component for Packet Size
	private Label nodeLabel = null; //Label displaying the name of the Node at the top of the window
	protected TextField requestsField = null; //Textfield for "Number of Requests" variable
	protected TextField timeoutField = null; //Textfield for "Time-Out (seconds)" variable
	protected CheckBox numericalDataCheckBox = null; //Checkbox for toggling numeric output
	protected Button pingButton; //Button to execute the ping operation
	private Embedded resultsBrowser = null; //Browser which displays the ping results
	private VerticalLayout topLayout = null; //Contains the form components
	private VerticalLayout bottomLayout = null; //Contains the results browser
	private VerticalSplitPanel vSplit = null; //Splits up the top layout and bottom layout
	private int margin = 40; //Padding around the results browser
	private int splitHeight = 240; //Height from top of the window to the split location in pixels
	private int topHeight = 280; //Set height size for everything above the split
	private final String noLabel = "no such label"; //Label given to vertexes that have no real label.
	private String pingUrl;
	
	/**
	 * The PingWindow method constructs a PingWindow component with a size proportionate to the 
	 * width and height of the main window.
	 * @param node 
	 * @param width Width of Main window
	 * @param height Height of Main window
	 */
	public PingWindow(final Node node, final String pingUrl){

		this.pingUrl = pingUrl;

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
		setCaption("Ping" + caption);
		setImmediate(true);
		setResizable(false);
        setSizeFull();

		/*Initialize the header of the Sub-window with the name of the selected Node*/
		String nodeName = "<div style=\"text-align: center; font-size: 18pt; font-weight:bold;\">" + label + "</div>";
		nodeLabel = new Label(nodeName);
		nodeLabel.setContentMode(ContentMode.HTML);

		/*Creating various layouts to encapsulate all of the components*/
		VerticalLayout mainLayout = new VerticalLayout();
		mainLayout.setSizeFull();
		vSplit = new VerticalSplitPanel();
        vSplit.setWidth(800, Unit.PIXELS);
        vSplit.setHeight(800, Unit.PIXELS);

		topLayout = new VerticalLayout();
		bottomLayout = new VerticalLayout();
		VerticalLayout form = new VerticalLayout();
		GridLayout grid = new GridLayout(2,4);
		grid.setWidth("420");
		grid.setHeight("120");

		/*Sets up IP Address dropdown with the Name as default*/
		ipDropdown = new NativeSelect();
		ipDropdown.addItem(ipAddress);
		ipDropdown.select(ipAddress);

		/*Sets up Packet Size dropdown with different values*/
		packetSizeDropdown = new NativeSelect();
		packetSizeDropdown.addItem("16");
		packetSizeDropdown.addItem("32");
		packetSizeDropdown.addItem("64");
		packetSizeDropdown.addItem("128");
		packetSizeDropdown.addItem("256");
		packetSizeDropdown.addItem("512");
		packetSizeDropdown.addItem("1024");
		packetSizeDropdown.select("16");

		/*Creates the Numerical Output Check box and sets up the listener*/
		numericalDataCheckBox = new CheckBox("Use Numerical Node Names");
		numericalDataCheckBox.setImmediate(true);
		numericalDataCheckBox.setValue(false);

		/*Creates the form labels and text fields*/
		Label ipLabel = new Label("IP Address: ");
		Label requestsLabel = new Label("Number of Requests: ");
		Label timeoutLabel = new Label("Time-Out (seconds): ");
		Label packetLabel = new Label("Packet Size: ");
		requestsField = new TextField();
		requestsField.setMaxLength(4); //Max buffer of 4 to prevent buffer overflow
		timeoutField = new TextField();
		timeoutField.setMaxLength(4); //Max buffer of 4 to prevent buffer overflow

		/*Add all of the components to the GridLayout*/
		grid.addComponent(ipLabel);
		grid.setComponentAlignment(ipLabel, Alignment.MIDDLE_LEFT);
		grid.addComponent(ipDropdown);
		grid.setComponentAlignment(ipDropdown, Alignment.MIDDLE_LEFT);
		grid.addComponent(requestsLabel);
		grid.setComponentAlignment(requestsLabel, Alignment.MIDDLE_LEFT);
		grid.addComponent(requestsField);
		grid.setComponentAlignment(requestsField, Alignment.MIDDLE_LEFT);
		grid.addComponent(timeoutLabel);
		grid.setComponentAlignment(timeoutLabel, Alignment.MIDDLE_LEFT);
		grid.addComponent(timeoutField);
		grid.setComponentAlignment(timeoutField, Alignment.MIDDLE_LEFT);
		grid.addComponent(packetLabel);
		grid.setComponentAlignment(packetLabel, Alignment.MIDDLE_LEFT);
		grid.addComponent(packetSizeDropdown);
		grid.setComponentAlignment(packetSizeDropdown, Alignment.MIDDLE_LEFT);

		/*Creates the Ping button and sets up the listener*/
		pingButton = new Button("Ping"); 
		pingButton.addClickListener(new Button.ClickListener() {
			@Override
			public void buttonClick(ClickEvent event) {
				changeBrowserURL(buildURL());
			}
		}); 

		/*Adds components to the form and sets the width and spacing*/
		form.addComponent(grid);
		form.addComponent(numericalDataCheckBox);
		form.addComponent(pingButton);
		form.setWidth("100%");
		form.setSpacing(true);

		/*Adds components to the Top Layout and sets the width and margins*/
		topLayout.addComponent(nodeLabel);
		topLayout.setComponentAlignment(nodeLabel, Alignment.MIDDLE_CENTER);
		topLayout.addComponent(form);
		topLayout.setSizeFull();
		topLayout.setMargin(new MarginInfo(true, true, false, true));

		/*Sets attributes for bottom layout component*/
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

		int width = (int)getUI().getPage().getBrowserWindowWidth();
		int height = (int)getUI().getPage().getBrowserWindowHeight();

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
	 * ping request form is submitted and refreshes the browser.
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
	 * URL string that is used to redirect the results browser to the Ping page.
	 * @return Web address for ping command with submitted parameters
	 * @throws MalformedURLException
	 */
    protected URL buildURL() {
        boolean validInput = false;
        try {
            validInput = validateInput();
        } catch (NumberFormatException e) {
            Notification.show("Inputs must be integers", Notification.Type.WARNING_MESSAGE);
            return null;
        }
        if (validInput) {
            final StringBuilder options = new StringBuilder(pingUrl);
            try {
                URL baseUrl = getUI().getPage().getLocation().toURL();

                options.append(pingUrl.contains("?") ? "&" : "?");

                options.append("address=").append(ipDropdown.getValue())
                    .append("&timeout=").append(timeoutField.getValue())
                    .append("&numberOfRequests=").append(requestsField.getValue())
                    .append("&packetSize=").append(Integer.parseInt(packetSizeDropdown.getValue().toString()) - 8);
                if (numericalDataCheckBox.getValue().equals(true)) {
                    options.append("&numericOutput=true");
                }

                return new URL(baseUrl, options.toString());
            } catch (final MalformedURLException e) {
                Notification.show("Could not build URL: " + options.toString(), Notification.Type.WARNING_MESSAGE);
                return null;
            }
        } else {
            Notification.show("Inputs must be between 0 and 9999", Notification.Type.WARNING_MESSAGE);
            return null;
        }
    }

	/**
	 * The validateInput method checks the timeout text field and the
	 * number of requests field to make sure the input given by the
	 * user was formatted correctly.
	 * @return Whether input was correctly formatted
	 */
	protected boolean validateInput() throws NumberFormatException {
		int timeout = 0, requests = 0;
		
		if ("".equals(timeoutField.getValue().toString())) timeout = 1;
		else timeout = Integer.parseInt(timeoutField.getValue().toString());
		if ("".equals(requestsField.getValue().toString())) requests = 1;
		else requests = Integer.parseInt(requestsField.getValue().toString());
		
		if (timeout < 1 || timeout > 9999) return false;
		if (requests < 1 || requests > 9999) return false;
		return true;
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
