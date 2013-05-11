/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.mock;

import java.util.Enumeration;

import junit.framework.TestCase;

import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Service;

/**
 * @author brozow
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MockPollerConfigTest extends TestCase {
    
    MockPollerConfig m_pollerConfig;
    
    @Override
    public void setUp() {
        MockNetwork network = new MockNetwork();
        network.setCriticalService("ICMP");
        network.addNode(1, "Router");
        network.addInterface("192.168.1.1");
        network.addService("ICMP");
        network.addService("SMTP");
        network.addInterface("192.168.1.2");
        network.addService("ICMP");
        network.addService("SMTP");
        network.addNode(2, "Server");
        network.addInterface("192.168.1.3");
        network.addService("ICMP");
        network.addService("HTTP");
        network.addInterface("192.168.1.2");

        m_pollerConfig = new MockPollerConfig(network);
        m_pollerConfig.addPackage("TestPackage");
        m_pollerConfig.addDowntime(1000L, 0L, -1L, false);
        m_pollerConfig.setDefaultPollInterval(1000L);
        m_pollerConfig.populatePackage(network);
        m_pollerConfig.setPollInterval("ICMP", 500L);

        
    }
    
    public void testPollerConfig() {
        m_pollerConfig.setNodeOutageProcessingEnabled(true);
        m_pollerConfig.setPollInterval("HTTP", 750L);
        m_pollerConfig.setPollerThreads(5);
        m_pollerConfig.setCriticalService("YAHOO");
        
        // test the nodeOutageProcessing setting works
        assertTrue(m_pollerConfig.isNodeOutageProcessingEnabled());

        // test to ensure that the poller has packages
        Enumeration<Package> pkgs = m_pollerConfig.enumeratePackage();
        assertNotNull(pkgs);
        int pkgCount = 0;
        Package pkg = null;

        while (pkgs.hasMoreElements()) {
            pkg = pkgs.nextElement();
            pkgCount++;
        }
        assertTrue(pkgCount > 0);

        // ensure a sample interface is in the package
        assertTrue(m_pollerConfig.isInterfaceInPackage("192.168.1.1", pkg));
        assertFalse(m_pollerConfig.isInterfaceInPackage("192.168.1.7", pkg));

        Enumeration<Service> svcs = pkg.enumerateService();
        assertNotNull(svcs);
        
        int svcCount = 0;
        boolean icmpFound = false;
        boolean httpFound = false;
        while (svcs.hasMoreElements()) {
            Service svc = svcs.nextElement();
            svcCount++;
            if ("ICMP".equals(svc.getName())) {
                icmpFound = true;
                assertEquals(500L, svc.getInterval());
            }
            else if ("HTTP".equals(svc.getName())) {
                httpFound = true;
                assertEquals(750L, svc.getInterval());
            }
            else {
                assertEquals(1000L, svc.getInterval());
            }
        }
        
        assertTrue(icmpFound);
        assertTrue(httpFound);
        assertEquals(3, svcCount);

        // ensure that setting the thread worked
        assertEquals(5, m_pollerConfig.getThreads());

        // ensure that setting the critical service worked
        assertEquals("YAHOO", m_pollerConfig.getCriticalService());

        // ensure that we have service monitors to the sevices
        assertNotNull(m_pollerConfig.getServiceMonitor("SMTP"));

    }



}
