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
import static org.junit.Assert.assertTrue;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import com.vaadin.ui.Window;

public class EventsAlarmsWindowTest {

	EventsAlarmsWindow window;
	EventsAlarmsWindow window2;
	Window mainWindow;
	UI app;
	@Before
	public void setUp() throws Exception {
		Node testNode1 = new Node(9,"172.20.1.10","Cartman");
		final URL url = new URL("http://localhost:8080/");
        window = new EventsAlarmsWindow(null, url, url);
		window2 = new EventsAlarmsWindow(testNode1, url, url);
		mainWindow = new Window();
		app = new UI() { //Empty Application

			private static final long serialVersionUID = -7197916089135471254L;

			@Override
			public void init(VaadinRequest request) {}
		};
	}

	@Test
	public void testAttach() {
		app.addWindow(mainWindow);
		app.addWindow(window);
		assertTrue(app.getWindows().contains(window));
		app.removeWindow(window);
		assertFalse(app.getWindows().contains(window));
	}

}
