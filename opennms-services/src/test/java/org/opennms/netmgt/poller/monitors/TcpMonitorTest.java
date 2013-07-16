/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.mock.MonitorTestUtils;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.mock.MockUtil;
import org.springframework.test.context.ContextConfiguration;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/emptyContext.xml"})
@JUnitConfigurationEnvironment
public class TcpMonitorTest {
    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
    }

    /*
     * Test method for 'org.opennms.netmgt.poller.monitors.TcpMonitor.poll(NetworkInterface, Map, Package)'
     */
    @Test
    public void testExternalServerConnection() throws UnknownHostException {
        Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();
        Parameter p = new Parameter();

        ServiceMonitor monitor = new TcpMonitor();
        MonitoredService svc = MonitorTestUtils.getMonitoredService(99, "www.opennms.org", "TCP");

        p.setKey("port");
        p.setValue("3020");
        m.put(p.getKey(), p.getValue());

        p.setKey("retry");
        p.setValue("1");
        m.put(p.getKey(), p.getValue());

        p.setKey("timeout");
        p.setValue("500");
        m.put(p.getKey(), p.getValue());

        PollStatus status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_UNAVAILABLE, status.getStatusCode());
        assertNotNull(status.getReason());

    }

    @Test
    @JUnitHttpServer(port=10342)
    public void testLocalhostConnection() throws UnknownHostException {
        Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();
        Parameter p = new Parameter();

        ServiceMonitor monitor = new TcpMonitor();
        MonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", "TCP");

        p.setKey("port");
        p.setValue("10342");
        m.put(p.getKey(), p.getValue());

        p.setKey("retry");
        p.setValue("1");
        m.put(p.getKey(), p.getValue());

        p.setKey("timeout");
        p.setValue("500");
        m.put(p.getKey(), p.getValue());

        PollStatus status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
        assertNull(status.getReason());
    }

    @Test
    @JUnitHttpServer(port=10342)
    public void testLocalhostIPv6Connection() throws UnknownHostException {
        if (Boolean.getBoolean("skipIpv6Tests")) return;

        Map<String, Object> m = new ConcurrentSkipListMap<String, Object>();
        Parameter p = new Parameter();

        ServiceMonitor monitor = new TcpMonitor();
        MonitoredService svc = MonitorTestUtils.getMonitoredService(3, "::1", "TCP");

        p.setKey("port");
        p.setValue("10342");
        m.put(p.getKey(), p.getValue());

        p.setKey("retry");
        p.setValue("1");
        m.put(p.getKey(), p.getValue());

        p.setKey("timeout");
        p.setValue("500");
        m.put(p.getKey(), p.getValue());

        PollStatus status = monitor.poll(svc, m);
        MockUtil.println("Reason: "+status.getReason());
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
        assertNull(status.getReason());
    }
}
