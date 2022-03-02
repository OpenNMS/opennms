/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
public class HttpPostMonitorTest {
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
