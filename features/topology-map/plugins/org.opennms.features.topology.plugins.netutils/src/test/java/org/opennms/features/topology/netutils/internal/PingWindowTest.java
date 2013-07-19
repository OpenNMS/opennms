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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

public class PingWindowTest {

	PingWindow pingWindow;
	PingWindow pingWindow2;
	UI app;

	boolean didNotify = false;

	@Before
	public void setUp() throws Exception {
		didNotify = false;

		Node testNode1 = new Node(9,"172.20.1.10","Cartman");

		pingWindow = new PingWindow(testNode1, "/opennms/ExecCommand.map?command=ping");
		pingWindow2 = new PingWindow(null, "/opennms/ExecCommand.map?command=ping");
		
		app = new UI() { //Empty Application

			private static final long serialVersionUID = -6761162156810032609L;

			@Override
			public void init(VaadinRequest request) {}

			@Override
			public Page getPage() {
				Page page = EasyMock.createMock(Page.class);
				try {
					EasyMock.expect(page.getLocation()).andReturn(new URI("http://localhost:8080/servlet/")).anyTimes();
					page.showNotification(EasyMock.anyObject(Notification.class));
					// If Notification.show() is called, then set didNotify to true
					EasyMock.expectLastCall().andAnswer(new IAnswer<Object>() {
						@Override
						public Object answer() throws Throwable {
							System.out.println("Notification was called: " + ((Notification)EasyMock.getCurrentArguments()[0]).getCaption());
							didNotify = true;
							return null;
						}
					}).anyTimes();
				} catch (URISyntaxException e) {
					// Should never be thrown
				}
				EasyMock.replay(page);
				return page;
			}
		};
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
		assertFalse(didNotify);
	}

	@Test
	public void testBuildURL_upperBounds() {
		pingWindow.numericalDataCheckBox.setValue(true);
		pingWindow.packetSizeDropdown.setValue("1024");
		pingWindow.requestsField.setValue("10000");
		pingWindow.timeoutField.setValue("10000");
		URL url = pingWindow.buildURL();
		assertNull(url == null ? "null" : url.toString(), pingWindow.buildURL());
		assertTrue(didNotify);
	}

	@Test
	public void testBuildURL_lowerBounds() {
		pingWindow.packetSizeDropdown.setValue("16");
		pingWindow.requestsField.setValue("0");
		pingWindow.timeoutField.setValue("0");
		URL url = pingWindow.buildURL();
		assertNull(url == null ? "null" : url.toString(), pingWindow.buildURL());
		assertTrue(didNotify);
	}

	@Test
	public void testBuildURL_nonIntegerInput() {
		pingWindow.numericalDataCheckBox.setValue(true);
		pingWindow.packetSizeDropdown.setValue("1024");
		pingWindow.requestsField.setValue("abcd");
		pingWindow.timeoutField.setValue("abcd");
		URL url = pingWindow.buildURL();
		assertNull(url == null ? "null" : url.toString(), pingWindow.buildURL());
		assertTrue(didNotify);
	}

	@Test
	public void testBuildURL_negativeIntegers() {
		pingWindow.numericalDataCheckBox.setValue(true);
		pingWindow.packetSizeDropdown.setValue("1024");
		pingWindow.requestsField.setValue("-99");
		pingWindow.timeoutField.setValue("-1024");
		URL url = pingWindow.buildURL();
		assertNull(url == null ? "null" : url.toString(), pingWindow.buildURL());
		assertTrue(didNotify);
	}

	@Test
	public void testBuildURL_invalidRequests() {
		pingWindow.numericalDataCheckBox.setValue(true);
		pingWindow.packetSizeDropdown.setValue("1024");
		pingWindow.requestsField.setValue("10000");
		pingWindow.timeoutField.setValue("100");
		URL url = pingWindow.buildURL();
		assertNull(url == null ? "null" : url.toString(), pingWindow.buildURL());
		assertTrue(didNotify);
	}

	@Test
	public void testButtonClick() {
		pingWindow.pingButton.click();

		pingWindow2.numericalDataCheckBox.setValue(true);
		pingWindow2.packetSizeDropdown.setValue("32");
		pingWindow2.requestsField.setValue("100");
		pingWindow2.timeoutField.setValue("100");
		pingWindow2.pingButton.click();
		assertFalse(didNotify);
	}

	@Test
	public void testAttach() {
		assertTrue(app.getWindows().contains(pingWindow));
		app.removeWindow(pingWindow);
		assertFalse(app.getWindows().contains(pingWindow));
		assertFalse(didNotify);
	}

}
