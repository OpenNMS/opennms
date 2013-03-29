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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.apache.sshd.ClientSession;
import org.apache.sshd.SshClient;
import org.junit.Before;
import org.junit.Test;

import com.vaadin.server.VaadinRequest;
import com.vaadin.ui.UI;

public class SSHWindowTest {
    
	UI app;
    SSHWindow sshWindow;
    SSHWindow sshWindow2;
    SshClient client;
    ClientSession session;
    String testHost = "debian.opennms.org";
    int testPort = 22;
    
    @SuppressWarnings("serial")
    @Before
    public void setup () {
        app = new UI() {
            @Override
            public void init(VaadinRequest request) {}
        };
        sshWindow = new SSHWindow(null, 200, 200);
        client = SshClient.setUpDefaultClient();
        client.start();
        try {
			session = client.connect(testHost, testPort).await().getSession();
		} catch (Exception e) {
			fail("Could not connect to host");
		}
        sshWindow2 = new SSHWindow(session, 200, 200);
        app.addWindow(sshWindow);
        app.addWindow(sshWindow2);
        
    }
    
    @Test
    public void testAttach() {
    	assertTrue(app.getWindows().contains(sshWindow));
    	app.removeWindow(sshWindow);
    	assertFalse(app.getWindows().contains(sshWindow));
    }
    
    @Test
    public void testClose() {
    	sshWindow2.close();
    	assertTrue(true); //Should execute above line without failure
    }
    
}
