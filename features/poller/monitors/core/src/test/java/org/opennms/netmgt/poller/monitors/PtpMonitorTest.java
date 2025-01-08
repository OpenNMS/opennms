/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2025 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2025 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors;

import org.junit.*;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.mock.MockMonitoredService;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.HashMap;
import java.util.Map;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml"
})
@JUnitConfigurationEnvironment
@JUnitSnmpAgent(port = PtpMonitorTest.TEST_SNMP_PORT, host = PtpMonitorTest.TEST_IP_ADDRESS, resource = "classpath:/org/opennms/netmgt/snmp/arista-ptp.properties")
public class PtpMonitorTest implements InitializingBean {
    static final int TEST_SNMP_PORT = 9161;
    static final String TEST_IP_ADDRESS = "127.0.0.1";

    @Autowired
    private SnmpPeerFactory m_snmpPeerFactory;

    private PtpMonitor monitor;

    private boolean m_ignoreWarnings = false;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        m_ignoreWarnings = false;
        MockLogAppender.setupLogging();
        monitor = new PtpMonitor();
        SnmpPeerFactory.setInstance(m_snmpPeerFactory);
    }

    @After
    public void tearDown() throws Exception {
        if (!m_ignoreWarnings) {
            MockLogAppender.assertNoWarningsOrGreater();
        }
    }

    @Test
    public void testWorking() {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("port", m_snmpPeerFactory.getAgentConfig(InetAddressUtils.getInetAddress(TEST_IP_ADDRESS)).getPort());
        parameters.put("agent", m_snmpPeerFactory.getAgentConfig(InetAddressUtils.getInetAddress(TEST_IP_ADDRESS)));
        parameters.put("clock-port", "Ethernet2");
        parameters.put("port-state", "slave");
        final MonitoredService svc = new MockMonitoredService(1, "test-server", InetAddressUtils.getInetAddress(TEST_IP_ADDRESS), "PTP");
        PollStatus status = monitor.poll(svc, parameters);
        Assert.assertTrue(status.isAvailable());
    }

    @Test
    public void testWorkingIgnoreCase() {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("port", m_snmpPeerFactory.getAgentConfig(InetAddressUtils.getInetAddress(TEST_IP_ADDRESS)).getPort());
        parameters.put("agent", m_snmpPeerFactory.getAgentConfig(InetAddressUtils.getInetAddress(TEST_IP_ADDRESS)));
        parameters.put("clock-port", "ethernet2");
        parameters.put("port-state", "Slave");
        final MonitoredService svc = new MockMonitoredService(1, "test-server", InetAddressUtils.getInetAddress(TEST_IP_ADDRESS), "PTP");
        PollStatus status = monitor.poll(svc, parameters);
        Assert.assertTrue(status.isAvailable());
    }

    @Test
    public void testNonWorking() {
        final Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("port", m_snmpPeerFactory.getAgentConfig(InetAddressUtils.getInetAddress(TEST_IP_ADDRESS)).getPort());
        parameters.put("agent", m_snmpPeerFactory.getAgentConfig(InetAddressUtils.getInetAddress(TEST_IP_ADDRESS)));
        parameters.put("clock-port", "Ethernet1");
        parameters.put("port-state", "slave");
        final MonitoredService svc = new MockMonitoredService(1, "test-server", InetAddressUtils.getInetAddress(TEST_IP_ADDRESS), "PTP");
        PollStatus status = monitor.poll(svc, parameters);
        Assert.assertTrue(status.isDown());
    }
}
