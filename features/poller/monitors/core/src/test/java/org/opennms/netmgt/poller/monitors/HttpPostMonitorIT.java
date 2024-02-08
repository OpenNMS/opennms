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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.http.JUnitHttpServerExecutionListener;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.core.test.http.annotations.Webapp;
import org.opennms.core.utils.Base64;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.mock.MockMonitoredService;
import org.opennms.netmgt.poller.mock.MonitorTestUtils;
import org.opennms.netmgt.utils.DnsUtils;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.mock.MockUtil;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
    "classpath:/META-INF/opennms/emptyContext.xml",
})
@JUnitConfigurationEnvironment
@DirtiesContext
public class HttpPostMonitorIT {
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
    }

    @Test
    public void testParameterSubstitution() throws UnknownHostException {
        HttpPostMonitor monitor = new HttpPostMonitor();
        Map<String, Object> parameters = new ConcurrentSkipListMap<String, Object>();
        parameters.put("url", "/{nodeLabel}.html");
        MockMonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", DnsUtils.resolveHostname("localhost", false), "HTTP");
        Map<String, Object> subbedParams = monitor.getRuntimeAttributes(svc, parameters);
        assertTrue(subbedParams.get("subbed-url").toString().equals("/localhost.html"));
    }

    @Test
    @JUnitHttpServer(basicAuth = true, webapps = @Webapp(context = "/opennms", path = "src/test/resources/loginTestWar"))
    public void testHeaders() throws UnknownHostException {
        final int port = JUnitHttpServerExecutionListener.getPort();
        final Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();
        final ServiceMonitor monitor = new HttpPostMonitor();
        final MonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", DnsUtils.resolveHostname("localhost", false), "HTTP");

        if (port > 0) {
            m.put("port", String.valueOf(port));
        } else {
            throw new IllegalStateException("Unable to determine what port the HTTP server started on!");
        }

        m.put("retry", "0");
        m.put("timeout", "500");
        m.put("banner", "");
        m.put("uri", "/opennms/j_spring_security_check");
        m.put("payload", "foo");
        m.put("header0", "Authorization: Basic " + new String(Base64.encodeBase64(("admin:istrator").getBytes())));

        final PollStatus status1 = monitor.poll(svc, m);
        MockUtil.println("Reason: " + status1.getReason());
        assertEquals(PollStatus.SERVICE_AVAILABLE, status1.getStatusCode());
        assertNull(status1.getReason());

        m.put("header0", "Authorization: Basic " + new String(Base64.encodeBase64(("admin:wrong").getBytes())));

        final PollStatus status2 = monitor.poll(svc, m);
        MockUtil.println("Reason: " + status2.getReason());
        assertEquals(PollStatus.SERVICE_UNAVAILABLE, status2.getStatusCode());
        assertNotNull(status2.getReason());
    }
}
