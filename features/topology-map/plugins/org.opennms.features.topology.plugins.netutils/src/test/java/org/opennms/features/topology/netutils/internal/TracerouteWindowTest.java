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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.URI;
import java.net.URISyntaxException;

import org.easymock.EasyMock;
import org.easymock.IAnswer;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.server.Page;
import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;

public class TracerouteWindowTest {

	TracerouteWindow traceWindow;
	TracerouteWindow traceWindow2;
	UI app;

	boolean didNotify = false;

	@Before
	public void setUp() throws Exception {
		didNotify = false;
		Node testNode1 = new Node(9,"172.20.1.10","Cartman");
		traceWindow = new TracerouteWindow(testNode1, "http://localhost:8080/");
		traceWindow2 = new TracerouteWindow(null, "http://localhost:8080/");
		app = new UI() { //Empty Application
			private static final long serialVersionUID = -2169800806621592419L;
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
		app.addWindow(traceWindow);
		app.addWindow(traceWindow2);
		UI.setCurrent(app);
	}
	
	@Test
	public void testBuildURL_correctInput() {
		traceWindow.numericalDataCheckBox.setValue(true);
		traceWindow.forcedHopField.setValue("127.0.0.1");
		assertNotNull(traceWindow.buildURL());
	}
	
	@Test
	public void testBuildURL_upperBounds() {
		traceWindow.numericalDataCheckBox.setValue(true);
		traceWindow.forcedHopField.setValue("256.256.256.256");
		assertNull(traceWindow.buildURL());
	}
	
	@Test
	public void testBuildURL_lowerBounds() {
		traceWindow.forcedHopField.setValue("-1.-1.-1.-1");
		assertNull(traceWindow.buildURL());
	}
	
	@Test
	public void testBuildURL_nonIntegerInput() {
		traceWindow.forcedHopField.setValue("abc.def.ghi.jkl");
		assertNull(traceWindow.buildURL());
	}
	
	@Test
	public void testBuildURL_notIPAddress() {
		traceWindow.forcedHopField.setValue("not an IP");
		assertNull(traceWindow.buildURL());
	}
	
	@Test
	public void testBuildURL_negativeIntegers() {
		traceWindow.forcedHopField.setValue("-255.-255.-255.-255");
		assertNull(traceWindow.buildURL());
	}
	
	@Test
	public void testButtonClick() {
		traceWindow.tracerouteButton.click();
		
		traceWindow.numericalDataCheckBox.setValue(true);
		traceWindow.forcedHopField.setValue("127.0.0.1");
		traceWindow.tracerouteButton.click();
	}
	
	@Test
	public void testAttach() {
//		assertTrue(app.getChildWindows().contains(traceWindow));
//		app.getMainWindow().removeWindow(traceWindow);
//		assertFalse(app.getMainWindow().getChildWindows().contains(traceWindow));
	}
}
