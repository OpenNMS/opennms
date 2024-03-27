/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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

        int svcCount = 0;
        boolean icmpFound = false;
        boolean httpFound = false;
        for (final Service svc : pkg.getServices()) {
            svcCount++;
            if ("ICMP".equals(svc.getName())) {
                icmpFound = true;
                assertEquals(Long.valueOf(500L), svc.getInterval());
            }
            else if ("HTTP".equals(svc.getName())) {
                httpFound = true;
                assertEquals(Long.valueOf(750L), svc.getInterval());
            }
            else {
                assertEquals(Long.valueOf(1000L), svc.getInterval());
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
