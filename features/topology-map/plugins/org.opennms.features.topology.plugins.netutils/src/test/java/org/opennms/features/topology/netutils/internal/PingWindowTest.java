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

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.Application;
import com.vaadin.ui.Window;

public class PingWindowTest {

	PingWindow pingWindow;
	PingWindow pingWindow2;
	PingWindow pingWindow3;
	Window mainWindow;
	Application app;
	
	@Before
	public void setUp() throws Exception {
		Node testNode1 = new Node(9,"172.20.1.10","Cartman");
		
		pingWindow = new PingWindow(testNode1, "http://Localhost:8080/");
		pingWindow2 = new PingWindow(null, "http://localhost:8080/");
		pingWindow3 = new PingWindow(testNode1, "");
		
		mainWindow = new Window();
		app = new Application() { //Empty Application
			@Override
			public void init() {}
		};
		app.setMainWindow(mainWindow);
		app.getMainWindow().addWindow(pingWindow);
		app.getMainWindow().addWindow(pingWindow2);
		app.getMainWindow().addWindow(pingWindow3);
	}
	
	@Test
	public void testBuildURL_correctInput() {
		pingWindow.numericalDataCheckBox.setValue(true);
		pingWindow.packetSizeDropdown.setValue("32");
		pingWindow.requestsField.setValue("100");
		pingWindow.timeoutField.setValue("100");
		assertNotNull(pingWindow.buildURL());
	}
	
	@Test
	public void testBuildURL_malformedURL() {
		pingWindow3.numericalDataCheckBox.setValue(true);
		pingWindow3.packetSizeDropdown.setValue("32");
		pingWindow3.requestsField.setValue("100");
		pingWindow3.timeoutField.setValue("100");
		assertNull(pingWindow3.buildURL());
	}
	
	@Test
	public void testBuildURL_upperBounds() {
		pingWindow.numericalDataCheckBox.setValue(true);
		pingWindow.packetSizeDropdown.setValue("1024");
		pingWindow.requestsField.setValue("10000");
		pingWindow.timeoutField.setValue("10000");
		assertNull(pingWindow.buildURL());
	}
	
	@Test
	public void testBuildURL_lowerBounds() {
		pingWindow.packetSizeDropdown.setValue("16");
		pingWindow.requestsField.setValue("0");
		pingWindow.timeoutField.setValue("0");
		assertNull(pingWindow.buildURL());
	}
	
	@Test
	public void testBuildURL_nonIntegerInput() {
		pingWindow.numericalDataCheckBox.setValue(true);
		pingWindow.packetSizeDropdown.setValue("1024");
		pingWindow.requestsField.setValue("abcd");
		pingWindow.timeoutField.setValue("abcd");
		assertNull(pingWindow.buildURL());
	}
	
	@Test
	public void testBuildURL_negativeIntegers() {
		pingWindow.numericalDataCheckBox.setValue(true);
		pingWindow.packetSizeDropdown.setValue("1024");
		pingWindow.requestsField.setValue("-99");
		pingWindow.timeoutField.setValue("-1024");
		assertNull(pingWindow.buildURL());
	}
	
	@Test
	public void testBuildURL_invalidRequests() {
		pingWindow.numericalDataCheckBox.setValue(true);
		pingWindow.packetSizeDropdown.setValue("1024");
		pingWindow.requestsField.setValue("10000");
		pingWindow.timeoutField.setValue("100");
		assertNull(pingWindow.buildURL());
	}
	
	@Test
	public void testButtonClick() {
		pingWindow.pingButton.click();
		
		pingWindow2.numericalDataCheckBox.setValue(true);
		pingWindow2.packetSizeDropdown.setValue("32");
		pingWindow2.requestsField.setValue("100");
		pingWindow2.timeoutField.setValue("100");
		pingWindow2.pingButton.click();
	}
	
	@Test
	public void testAttach() {
		assertTrue(app.getMainWindow().getChildWindows().contains(pingWindow));
		app.getMainWindow().removeWindow(pingWindow);
		assertFalse(app.getMainWindow().getChildWindows().contains(pingWindow));
	}

}
