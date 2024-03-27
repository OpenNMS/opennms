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

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.mock.MockMonitoredService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

/**
 * Test class for HostResourceSWRunMonitorIT.
 *
 * @author <A HREF="mailto:agalue@opennms.org">Alejandro Galue</A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 */

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml"
})
@JUnitConfigurationEnvironment
@JUnitSnmpAgent(port=HostResourceSWRunMonitorIT.TEST_SNMP_PORT,host=HostResourceSWRunMonitorIT.TEST_IP_ADDRESS, resource="classpath:/org/opennms/netmgt/snmp/snmpTestData1.properties")
public class HostResourceSWRunMonitorIT implements InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(HostResourceSWRunMonitorIT.class);
    static final int TEST_SNMP_PORT = 9161;
    static final String TEST_IP_ADDRESS = "127.0.0.1";


    @Autowired
    private SnmpPeerFactory m_snmpPeerFactory;
 
    private HostResourceSwRunMonitor monitor;
    
    private boolean m_ignoreWarnings = false;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        m_ignoreWarnings = false;
        MockLogAppender.setupLogging();
        monitor = new HostResourceSwRunMonitor();
        SnmpPeerFactory.setInstance(m_snmpPeerFactory);
    }

    @After
    public void tearDown() throws Exception {
        if (!m_ignoreWarnings ) {
            MockLogAppender.assertNoWarningsOrGreater();
        }
    }

    @Test
    public void testUnknownService() throws Exception {
        Map<String, Object> parameters = createBasicParams();
        parameters.put("service-name", "this service does not exist!");
        PollStatus status = monitor.poll(createMonitor(), parameters);
        Assert.assertFalse(status.isAvailable());
        log(status.getReason());
    }

    @Test
    public void testMonitorWithRegex() throws Exception {
        Map<String, Object> parameters = createBasicParams();
        PollStatus status = monitor.poll(createMonitor(), parameters);
        Assert.assertTrue(status.isAvailable());
    }

    @Test
    public void testMonitorWithoutRegex() throws Exception {
        Map<String, Object> parameters = createBasicParams();
        parameters.put("service-name", "eclipse");
        PollStatus status = monitor.poll(createMonitor(), parameters);
        Assert.assertTrue(status.isAvailable());
    }

    @Test
    public void testMinServices() throws Exception {
        Map<String, Object> parameters = createBasicParams();
        parameters.put("min-services", "2");
        PollStatus status = monitor.poll(createMonitor(), parameters);
        Assert.assertTrue(status.isAvailable());
    }

    @Test
    public void testInvalidMinServices() throws Exception {
        Map<String, Object> parameters = createBasicParams();
        parameters.put("min-services", "5");
        PollStatus status = monitor.poll(createMonitor(), parameters);
        Assert.assertFalse(status.isAvailable());
        log(status.getReason());
    }

    @Test
    public void testMaxServices() throws Exception {
        Map<String, Object> parameters = createBasicParams();
        parameters.put("max-services", "5");
        PollStatus status = monitor.poll(createMonitor(), parameters);
        Assert.assertTrue(status.isAvailable());
    }

    @Test
    public void testInvalidMaxServices() throws Exception {
        Map<String, Object> parameters = createBasicParams();
        parameters.put("max-services", "3");
        PollStatus status = monitor.poll(createMonitor(), parameters);
        Assert.assertFalse(status.isAvailable());
        log(status.getReason());
    }

    @Test
    public void testServicesRange() throws Exception {
        Map<String, Object> parameters = createBasicParams();
        parameters.put("min-services", "2");
        parameters.put("max-services", "5");
        PollStatus status = monitor.poll(createMonitor(), parameters);
        Assert.assertTrue(status.isAvailable());
    }

    @Test
    public void testInvalidRange() throws Exception {
        m_ignoreWarnings = true; // warning is expected here, skip the assert in tearDown()
        Map<String, Object> parameters = createBasicParams();
        parameters.put("min-services", "8");
        parameters.put("max-services", "5");
        PollStatus status = monitor.poll(createMonitor(), parameters);
        Assert.assertFalse(status.isAvailable());
        log(status.getReason());
    }

    @Test
    public void testServicesRangeWithoutMatchAll() throws Exception {
        Map<String, Object> parameters = createBasicParams();
        parameters.put("min-services", "1");
        parameters.put("max-services", "3");
        parameters.put("match-all", "false");
        PollStatus status = monitor.poll(createMonitor(), parameters);
        Assert.assertTrue(status.isAvailable());
    }

    @Test
    public void testInvalidServicesRange() throws Exception {
        Map<String, Object> parameters = createBasicParams();
        parameters.put("min-services", "1");
        parameters.put("max-services", "3");
        PollStatus status = monitor.poll(createMonitor(), parameters);
        Assert.assertFalse(status.isAvailable());
        log(status.getReason());
    }

    private Map<String, Object> createBasicParams() {
        Map<String, Object> parameters = new HashMap<String,Object>();
        parameters.put("port", m_snmpPeerFactory.getAgentConfig(InetAddressUtils.getInetAddress(TEST_IP_ADDRESS)).getPort());
        parameters.put("service-name", "~^(auto|sh).*");
        parameters.put("match-all", "true");
        parameters.put("agent", m_snmpPeerFactory.getAgentConfig(InetAddressUtils.getInetAddress(TEST_IP_ADDRESS)));
        return parameters;
    }

    private MonitoredService createMonitor() throws UnknownHostException {
        MonitoredService svc = new MockMonitoredService(1, "test-server", InetAddressUtils.getInetAddress(TEST_IP_ADDRESS), "SWRUN-TEST");
        return svc;
    }

    private void log(String message) {
        LOG.debug(message);
    }

}
