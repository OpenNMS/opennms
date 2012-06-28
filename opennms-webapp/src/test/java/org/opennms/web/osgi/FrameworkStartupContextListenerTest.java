/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.web.osgi;

import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.core.test.http.annotations.Webapp;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.mock.MockMonitoredService;
import org.opennms.netmgt.poller.monitors.PageSequenceMonitor;
import org.springframework.test.context.ContextConfiguration;

/**
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:META-INF/opennms/emptyContext.xml")
@JUnitHttpServer(port=10342)
public class FrameworkStartupContextListenerTest {

    PageSequenceMonitor m_monitor;
    Map<String, Object> m_params;

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        m_monitor = new PageSequenceMonitor();
        m_monitor.initialize(Collections.<String, Object>emptyMap());

        m_params = new HashMap<String, Object>();
        m_params.put("timeout", "8000");
        m_params.put("retries", "1");
    }

    protected MonitoredService getHttpService(String hostname, InetAddress inetAddress) throws Exception {
        MonitoredService svc = new MockMonitoredService(1, hostname, inetAddress, "HTTP");
        m_monitor.initialize(svc);
        return svc;
    }

    /**
     * TODO: Add some tests that request content from OSGi modules
     */
    @Test
    @JUnitHttpServer(port=10342, webapps=@Webapp(context="/opennms", path="src/test/resources/osgiTestWar"))
    public void testLogin() throws Exception {

        m_params.put("page-sequence", "" +
            "<?xml version=\"1.0\"?>" +
            "<page-sequence>\n" + 
            "  <page virtual-host=\"localhost\" path=\"/opennms/\" port=\"10342\" successMatch=\"Password\" />\n" + 
            "  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_check\" port=\"10342\" method=\"POST\" response-range=\"300-399\">\n" + 
            "    <parameter key=\"j_username\" value=\"demo\"/>\n" + 
            "    <parameter key=\"j_password\" value=\"demo\"/>\n" + 
            "  </page>\n" + 
            "  <page virtual-host=\"localhost\" path=\"/opennms/j_spring_security_logout\" port=\"10342\" successMatch=\"Login with Username and Password\" />\n" + 
            "</page-sequence>\n");

        PollStatus status = m_monitor.poll(getHttpService("localhost", InetAddressUtils.addr("127.0.0.1")), m_params);
        assertTrue("Expected available but was "+status+": reason = "+status.getReason(), status.isAvailable());

    }
}
