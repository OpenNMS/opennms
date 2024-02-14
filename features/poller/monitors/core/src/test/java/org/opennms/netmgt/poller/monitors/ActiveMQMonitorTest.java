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

import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Test;
import org.junit.Rule;
import org.junit.rules.TestName;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.ClassRule;
import org.opennms.core.test.activemq.ActiveMQBroker;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.support.SimpleMonitoredService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author roskens
 */
public class ActiveMQMonitorTest {
    private static final Logger LOG = LoggerFactory.getLogger(ActiveMQMonitorTest.class);
    private static final String DEFAULT_BROKERURL = "tcp://localhost:61616?transport.threadName&transport.trace=false&transport.soTimeout=20000";

    @Rule
    public TestName m_test = new TestName();

    @ClassRule
    public static ActiveMQBroker broker = new ActiveMQBroker(DEFAULT_BROKERURL);

    @Before
    public void startUp() throws Exception {
        LOG.info("======== Starting test " + m_test.getMethodName());
    }

    @After
    public void tearDown() throws Exception {
        LOG.info("======== Finished test " + m_test.getMethodName());
    }

    /**
     * Test of poll method, of class ActiveMQMonitor.
     */
    @Test
    public void testPoll() {
        MonitoredService svc = new SimpleMonitoredService(InetAddressUtils.addr("127.0.0.1"), "ActiveMQ");
        Map<String, Object> parameters = new HashMap<>();
        ActiveMQMonitor instance = new ActiveMQMonitor();
        PollStatus result = instance.poll(svc, parameters);
        assertEquals(PollStatus.SERVICE_AVAILABLE, result.getStatusCode());
    }

    @Test
    public void testPollNodeLabel() {
        MonitoredService svc = new SimpleMonitoredService(InetAddressUtils.addr("127.0.0.1"), 0, "localhost", "ActiveMQ");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("broker-url", "tcp://169.254.0.0:61616?trace=false&soTimeout=20000");
        parameters.put("use-nodelabel", Boolean.TRUE);
        ActiveMQMonitor instance = new ActiveMQMonitor();
        PollStatus result = instance.poll(svc, parameters);
        assertEquals(PollStatus.SERVICE_AVAILABLE, result.getStatusCode());
    }

    @Test
    public void testPollClientID() {
        MonitoredService svc = new SimpleMonitoredService(InetAddressUtils.addr("127.0.0.1"), 0, "localhost", "ActiveMQ");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("broker-url", "tcp://169.254.0.0:61616?trace=false&soTimeout=20000");
        parameters.put("use-nodelabel", Boolean.TRUE);
        parameters.put("client-id", "clientID-"+System.currentTimeMillis());
        ActiveMQMonitor instance = new ActiveMQMonitor();
        PollStatus result = instance.poll(svc, parameters);
        assertEquals(PollStatus.SERVICE_AVAILABLE, result.getStatusCode());
    }

    @Test
    public void testPollWithSession() {
        MonitoredService svc = new SimpleMonitoredService(InetAddressUtils.addr("127.0.0.1"), 0, "localhost", "ActiveMQ");
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("broker-url", "tcp://169.254.0.0:61616?trace=false&soTimeout=20000");
        parameters.put("use-nodelabel", Boolean.TRUE);
        parameters.put("client-id", "clientID-"+System.currentTimeMillis());
        parameters.put("create-session", Boolean.TRUE);
        ActiveMQMonitor instance = new ActiveMQMonitor();
        PollStatus result = instance.poll(svc, parameters);
        assertEquals(PollStatus.SERVICE_AVAILABLE, result.getStatusCode());
    }

}
