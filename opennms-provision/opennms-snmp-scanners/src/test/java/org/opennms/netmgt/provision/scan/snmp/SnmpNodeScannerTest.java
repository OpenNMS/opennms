/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2008 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package org.opennms.netmgt.provision.scan.snmp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.mock.snmp.MockSnmpAgent;
import org.opennms.netmgt.dao.SnmpAgentConfigFactory;
import org.opennms.netmgt.provision.ScanContext;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.core.io.ClassPathResource;

public class SnmpNodeScannerTest {
    
    /**
     * @author brozow
     *
     */
    private static class MockScanContext implements ScanContext {
        String m_sysObjectId;
        String m_sysContact;
        String m_sysDescription;
        String m_sysLocation;
        String m_sysName;
        InetAddress m_agentAddress;

        public MockScanContext(InetAddress agentAddress) {
            m_agentAddress = agentAddress;
        }

        public InetAddress getAgentAddress(String agentType) {
            return m_agentAddress;
        }

        public void updateSysObjectId(String sysObjectId) {
            m_sysObjectId = sysObjectId;
        }

        public String getSysObjectId() {
            return m_sysObjectId;
        }

        public String getSysContact() {
            return m_sysContact;
        }

        public void updateSysContact(String sysContact) {
            m_sysContact = sysContact;
        }

        public String getSysDescription() {
            return m_sysDescription;
        }

        public void updateSysDescription(String sysDescription) {
            m_sysDescription = sysDescription;
        }

        public String getSysLocation() {
            return m_sysLocation;
        }

        public void updateSysLocation(String sysLocation) {
            m_sysLocation = sysLocation;
        }

        public String getSysName() {
            return m_sysName;
        }

        public void updateSysName(String sysName) {
            m_sysName = sysName;
        }

    }

    private InetAddress m_agentAddress;
    private SnmpAgentConfig m_agentConfig;
    private MockScanContext m_scanContext;
    private MockSnmpAgent m_agent;
    private static final Integer AGENT_PORT = 9161;
    
    private SnmpAgentConfigFactory snmpAgentConfigFactory() {
        return snmpAgentConfigFactory(m_agentConfig);
    }
    
    private SnmpAgentConfigFactory snmpAgentConfigFactory(final SnmpAgentConfig config) {
        return new SnmpAgentConfigFactory() {

            public SnmpAgentConfig getAgentConfig(InetAddress address) {
                assertEquals(config.getAddress(), address);
                return config;
            }
            
        };
    }

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        m_agentAddress = InetAddress.getLocalHost();
        
        m_agent = MockSnmpAgent.createAgentAndRun(
            new ClassPathResource("org/opennms/netmgt/provision/scan/snmp/snmpTestData1.properties"),
            m_agentAddress.getHostAddress()+"/"+AGENT_PORT
        );
        
        m_agentConfig = new SnmpAgentConfig(m_agentAddress);
        m_agentConfig.setPort(AGENT_PORT);
        
        m_scanContext = new MockScanContext(m_agentAddress);

    }

    @After
    public void tearDown() throws Exception {
        m_agent.shutDownAndWait();
    }
    
    @Test
    public void testScan() throws Exception {

        SnmpNodeScanner scanner = new SnmpNodeScanner();
        scanner.setSnmpAgentConfigFactory(snmpAgentConfigFactory());
        scanner.init();
        scanner.scan(m_scanContext);
        
        assertEquals(".1.3.6.1.4.1.8072.3.2.255", m_scanContext.getSysObjectId());
        assertEquals("brozow.local", m_scanContext.getSysName());
        assertEquals("Darwin brozow.local 7.9.0 Darwin Kernel Version 7.9.0: Wed Mar 30 20:11:17 PST 2005; root:xnu/xnu-517.12.7.obj~1/RELEASE_PPC  Power Macintosh", m_scanContext.getSysDescription());
        assertEquals("Unknown", m_scanContext.getSysLocation());
        assertEquals("root@@no.where", m_scanContext.getSysContact());
    }

    @Test
    @Ignore("this will only work on the OpenNMS internal network ;)")
    public void testOpennmsRouter() throws Exception {
        InetAddress agent = InetAddress.getByName("172.20.1.1");
        MockScanContext context = new MockScanContext(agent);
        SnmpNodeScanner scanner = new SnmpNodeScanner();
        SnmpAgentConfig ac = new SnmpAgentConfig(agent);
        scanner.setSnmpAgentConfigFactory(snmpAgentConfigFactory(ac));
        scanner.init();
        scanner.scan(context);

        assertTrue(context.getSysDescription().startsWith("Cisco IOS Software, C870 Software"));
        assertEquals(".1.3.6.1.4.1.9.1.569", context.getSysObjectId());
        assertEquals("", context.getSysContact());
        assertEquals("", context.getSysLocation());
        assertEquals("internal.opennms.com", context.getSysName());
    }
}
