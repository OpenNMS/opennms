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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.ui.UI;

public class TracerouteWindowTest {

	TracerouteWindow traceWindow;
	TracerouteWindow traceWindow2;
	UI app;

	@Before
	public void setUp() throws Exception {
		Node testNode1 = new Node(9,"192.0.2.10","Cartman");
		traceWindow = new TracerouteWindow(testNode1, "http://localhost:8080/");
		traceWindow2 = new TracerouteWindow(null, "http://localhost:8080/");
		app = new DummyUI();
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
