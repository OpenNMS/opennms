/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import static org.junit.matchers.JUnitMatchers.containsString;
import org.junit.rules.TestName;
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
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * Test class for DiskUsageMonitorTest.
 *
 * @author <A HREF="mailto:ronald.roskens@gmail.com">Ronald Roskens</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml"
})
@JUnitSnmpAgent(host = DiskUsageMonitorTest.TEST_IP_ADDRESS, resource = "classpath:/org/opennms/netmgt/snmp/snmpTestData1.properties")
public class DiskUsageMonitorTest implements InitializingBean {

    static final String TEST_IP_ADDRESS = "127.0.0.1";

    @Rule
    public TestName m_test = new TestName();

    @Autowired
    private SnmpPeerFactory m_snmpPeerFactory;
    
    private DiskUsageMonitor monitor;
    
    
    private boolean m_ignoreWarnings = false;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        System.out.println("------------ begin test " + m_test.getMethodName() + " ------------");
        m_ignoreWarnings = false;
        MockLogAppender.setupLogging();
        monitor = new DiskUsageMonitor();
        SnmpPeerFactory.setInstance(m_snmpPeerFactory);
    }

    @After
    public void tearDown() throws Exception {
        if (!m_ignoreWarnings ) {
            MockLogAppender.assertNoWarningsOrGreater();
        }
        System.out.println("------------ end test " + m_test.getMethodName() + " ------------");
    }

    @Test(expected = RuntimeException.class)
    public void testDiskNull() throws Exception {
        Map<String, Object> parameters = createBasicParams();
        parameters.remove("disk");
        PollStatus status = monitor.poll(createMonitor(), parameters);
        Assert.assertFalse(status.isAvailable());
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidMatchTypeParameter() throws Exception {
        Map<String, Object> parameters = createBasicParams();
        parameters.put("match-type", "invalid");
        PollStatus status = monitor.poll(createMonitor(), parameters);
        Assert.assertFalse(status.isAvailable());
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidRequireTypeParameter() throws Exception {
        Map<String, Object> parameters = createBasicParams();
        parameters.put("require-type", "invalid");
        PollStatus status = monitor.poll(createMonitor(), parameters);
        Assert.assertFalse(status.isAvailable());
    }

    @Test
    public void testParameters() throws Exception {
        Map<String, Object> parameters = createBasicParams();
        PollStatus status = monitor.poll(createMonitor(), parameters);
        Assert.assertTrue(status.isAvailable());
    }

    @Test
    public void testInvalidDiskRegex() throws Exception {
        Map<String, Object> parameters = createBasicParams();
        parameters.put("disk", "^[A-Z:");
        parameters.put("match-type", "regex");
        PollStatus status = monitor.poll(createMonitor(), parameters);
        Assert.assertFalse(status.isAvailable());
        Assert.assertThat(status.getReason(), containsString("Invalid SNMP Criteria: Unclosed character class"));
    }

    @Test
    public void testAllDisks() throws Exception {
        Map<String, Object> parameters = createBasicParams();
        parameters.put("match-type", "startswith");
        parameters.put("require-type", "all");
        PollStatus status = monitor.poll(createMonitor(), parameters);
        Assert.assertFalse(status.isAvailable());
    }

    @Test
    public void testDiskCase1() throws Exception {
        Map<String, Object> parameters = createBasicParams();
        parameters.put("free", "25");
        PollStatus status = monitor.poll(createMonitor(), parameters);
        Assert.assertTrue(status.isAvailable());
    }

    @Test
    public void testDiskNotFound() throws Exception {
        Map<String, Object> parameters = createBasicParams();
        parameters.put("disk", "/data");
        PollStatus status = monitor.poll(createMonitor(), parameters);
        Assert.assertFalse(status.isAvailable());
        Assert.assertThat(status.getReason(), containsString("Could not find /data in hrStorageTable"));
    }

    @Test
    public void testDiskRegex() throws Exception {
        Map<String, Object> parameters = createBasicParams();
        parameters.put("disk", "^/$");
        parameters.put("match-type", "regex");
        PollStatus status = monitor.poll(createMonitor(), parameters);
        Assert.assertTrue(status.isAvailable());
    }

    private Map<String, Object> createBasicParams() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("port", m_snmpPeerFactory.getAgentConfig(InetAddressUtils.getInetAddress(TEST_IP_ADDRESS)).getPort());
        parameters.put("disk", "/");
        parameters.put("free", 15);
        parameters.put("match-type", "exact");
        parameters.put("require-type", "any");
        parameters.put("agent", m_snmpPeerFactory.getAgentConfig(InetAddressUtils.getInetAddress(TEST_IP_ADDRESS)));
        return parameters;
    }

    private MonitoredService createMonitor() throws UnknownHostException {
        MonitoredService svc = new MockMonitoredService(1, "test-server", InetAddressUtils.getInetAddress(TEST_IP_ADDRESS), "DUM-TEST");
        return svc;
    }

}
