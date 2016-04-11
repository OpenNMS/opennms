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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.UI;

public class PingWindowTest {

	PingWindow pingWindow;
	PingWindow pingWindow2;
	DummyUI app;

	@Before
	public void setUp() throws Exception {
		Node testNode1 = new Node(9,"192.0.2.10","Cartman");

		pingWindow = new PingWindow(testNode1, "/opennms/ExecCommand?command=ping");
		pingWindow2 = new PingWindow(null, "/opennms/ExecCommand?command=ping");

		app = new DummyUI();
		app.addWindow(pingWindow);
		app.addWindow(pingWindow2);
		UI.setCurrent(app);
	}

	@Test
	public void testBuildURL_correctInput() {
		pingWindow.numericalDataCheckBox.setValue(true);
		pingWindow.packetSizeDropdown.setValue("32");
		pingWindow.requestsField.setValue("100");
		pingWindow.timeoutField.setValue("100");
		URL url = pingWindow.buildURL();
		assertNotNull(url == null ? "null" : url.toString(), pingWindow.buildURL());
		assertFalse(app.isNotified());
	}

	@Test
	public void testBuildURL_upperBounds() {
		pingWindow.numericalDataCheckBox.setValue(true);
		pingWindow.packetSizeDropdown.setValue("1024");
		pingWindow.requestsField.setValue("10000");
		pingWindow.timeoutField.setValue("10000");
		URL url = pingWindow.buildURL();
		assertNull(url == null ? "null" : url.toString(), pingWindow.buildURL());
		assertTrue(app.isNotified());
	}

	@Test
	public void testBuildURL_lowerBounds() {
		pingWindow.packetSizeDropdown.setValue("16");
		pingWindow.requestsField.setValue("0");
		pingWindow.timeoutField.setValue("0");
		URL url = pingWindow.buildURL();
		assertNull(url == null ? "null" : url.toString(), pingWindow.buildURL());
		assertTrue(app.isNotified());
	}

	@Test
	public void testBuildURL_nonIntegerInput() {
		pingWindow.numericalDataCheckBox.setValue(true);
		pingWindow.packetSizeDropdown.setValue("1024");
		pingWindow.requestsField.setValue("abcd");
		pingWindow.timeoutField.setValue("abcd");
		URL url = pingWindow.buildURL();
		assertNull(url == null ? "null" : url.toString(), pingWindow.buildURL());
		assertTrue(app.isNotified());
	}

	@Test
	public void testBuildURL_negativeIntegers() {
		pingWindow.numericalDataCheckBox.setValue(true);
		pingWindow.packetSizeDropdown.setValue("1024");
		pingWindow.requestsField.setValue("-99");
		pingWindow.timeoutField.setValue("-1024");
		URL url = pingWindow.buildURL();
		assertNull(url == null ? "null" : url.toString(), pingWindow.buildURL());
		assertTrue(app.isNotified());
	}

	@Test
	public void testBuildURL_invalidRequests() {
		pingWindow.numericalDataCheckBox.setValue(true);
		pingWindow.packetSizeDropdown.setValue("1024");
		pingWindow.requestsField.setValue("10000");
		pingWindow.timeoutField.setValue("100");
		URL url = pingWindow.buildURL();
		assertNull(url == null ? "null" : url.toString(), pingWindow.buildURL());
		assertTrue(app.isNotified());
	}

	@Test
	public void testButtonClick() {
		pingWindow.pingButton.click();

		pingWindow2.numericalDataCheckBox.setValue(true);
		pingWindow2.packetSizeDropdown.setValue("32");
		pingWindow2.requestsField.setValue("100");
		pingWindow2.timeoutField.setValue("100");
		pingWindow2.pingButton.click();
		assertFalse(app.isNotified());
	}

	@Test
	public void testAttach() {
		assertTrue(app.getWindows().contains(pingWindow));
		app.removeWindow(pingWindow);
		assertFalse(app.getWindows().contains(pingWindow));
		assertFalse(app.isNotified());
	}

}
