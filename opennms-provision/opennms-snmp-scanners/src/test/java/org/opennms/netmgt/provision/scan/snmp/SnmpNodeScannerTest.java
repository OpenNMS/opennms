/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.scan.snmp;

import static org.junit.Assert.assertEquals;

import java.net.InetAddress;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.utils.BeanUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.provision.ScanContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
		"classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml"
})
@JUnitSnmpAgent(host=SnmpNodeScannerTest.TEST_IP_ADDRESS, resource="classpath:org/opennms/netmgt/provision/scan/snmp/snmpTestData1.properties")
public class SnmpNodeScannerTest implements InitializingBean {
	static final String TEST_IP_ADDRESS = "172.20.1.205";
	
	@Autowired
	private SnmpPeerFactory m_snmpPeerFactory;
    
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

        @Override
        public InetAddress getAgentAddress(String agentType) {
            return m_agentAddress;
        }

        @Override
        public void updateSysObjectId(String sysObjectId) {
            m_sysObjectId = sysObjectId;
        }

        public String getSysObjectId() {
            return m_sysObjectId;
        }

        public String getSysContact() {
            return m_sysContact;
        }

        @Override
        public void updateSysContact(String sysContact) {
            m_sysContact = sysContact;
        }

        public String getSysDescription() {
            return m_sysDescription;
        }

        @Override
        public void updateSysDescription(String sysDescription) {
            m_sysDescription = sysDescription;
        }

        public String getSysLocation() {
            return m_sysLocation;
        }

        @Override
        public void updateSysLocation(String sysLocation) {
            m_sysLocation = sysLocation;
        }

        public String getSysName() {
            return m_sysName;
        }

        @Override
        public void updateSysName(String sysName) {
            m_sysName = sysName;
        }

    }

    private InetAddress m_agentAddress;
    private MockScanContext m_scanContext;
    
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        m_agentAddress = InetAddressUtils.addr(TEST_IP_ADDRESS);
        m_scanContext = new MockScanContext(m_agentAddress);

    }

    @Test
    public void testScan() throws Exception {

    	final SnmpNodeScanner scanner = new SnmpNodeScanner();
        scanner.setSnmpAgentConfigFactory(m_snmpPeerFactory);
        scanner.init();
        scanner.scan(m_scanContext);
        
        assertEquals(".1.3.6.1.4.1.8072.3.2.255", m_scanContext.getSysObjectId());
        assertEquals("brozow.local", m_scanContext.getSysName());
        assertEquals("Darwin brozow.local 7.9.0 Darwin Kernel Version 7.9.0: Wed Mar 30 20:11:17 PST 2005; root:xnu/xnu-517.12.7.obj~1/RELEASE_PPC  Power Macintosh", m_scanContext.getSysDescription());
        assertEquals("Unknown", m_scanContext.getSysLocation());
        assertEquals("root@@no.where", m_scanContext.getSysContact());
    }
}
