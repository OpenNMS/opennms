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

package org.opennms.features.topology.ssh.internal;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;


public class SSHTerminalTest {

	String testHost = "debian.opennms.org";
	int testPort = 22;
	SSHTerminal sshTerm;
	SSHTerminal.SessionTerminal sessionTerm;
	UI app;
	VerticalLayout mainWindow;

	@SuppressWarnings("serial")
	@Before
	public void setUp() throws Exception {

		app = new UI() {
			@Override
			public void init(VaadinRequest request) {}
		};
		mainWindow = new VerticalLayout();
		app.setContent(mainWindow);

		SSHWindow sshWindow = new SSHWindow(null, 200, 200);
		app.addWindow(sshWindow);

		SshClient client = SshClient.setUpDefaultClient();
		client.start();
		ClientSession session = null;
		try {
			session = client.connect(testHost, testPort).await().getSession();
		} catch (Exception e) {
			fail("Could not connect to host");
		}
		sshTerm = new SSHTerminal(sshWindow, session, 200, 200);
		sshWindow.setContent(sshTerm);
		UI.setCurrent(app);
	}

//	@Test
//	public void testPaintContent() {
//		try {
//			//sshTerm.paintContent(new SudoPaintTarget());
//		} catch (PaintException e) {
//			fail("PaintContent exception was thrown");
//		} 
//	}

	@Test
	@SuppressWarnings("unchecked")
	public void testChangeVariables() {
		Map map = new LinkedHashMap();

		map.put("isClosed", false);
		map.put("toSSH", "data to the ssh server");
		//sshTerm.changeVariables(new Object(), map);

		map.put("isClosed", true);
		map.put("toSSH", "data to the ssh server");
		//sshTerm.changeVariables(new Object(), map);
	}
	
	@Test
	public void testClose() {
		assertTrue(sshTerm.close());
	}

}
