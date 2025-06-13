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
package org.opennms.netmgt.poller.monitors;

import static org.junit.matchers.JUnitMatchers.containsString;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
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
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * Test class for DiskUsageMonitorIT.
 *
 * @author <A HREF="mailto:ronald.roskens@gmail.com">Ronald Roskens</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml"
})
@JUnitConfigurationEnvironment
@JUnitSnmpAgent(host = DiskUsageMonitorIT.TEST_IP_ADDRESS, resource = "classpath:/org/opennms/netmgt/snmp/snmpTestData1.properties")
public class DiskUsageMonitorIT implements InitializingBean {

    static final String TEST_IP_ADDRESS = "127.0.0.1";

    @Rule
    public TestName m_test = new TestName();

    @Autowired
    private SnmpPeerFactory m_snmpPeerFactory;
    
    private DiskUsageMonitor monitor;
    
    
    private boolean m_ignoreWarnings = false;

    @Override
    public void afterPropertiesSet() throws Exception {

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
