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

import static org.junit.Assert.assertTrue;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.mock.MockMonitoredService;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * This test runs the {@link PageSequenceMonitor} against an OpenNMS web UI
 * as a form of smoke testing.
 * 
 * @author Seth
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations="classpath:/META-INF/opennms/emptyContext.xml")
public class PageSequenceMonitorOpenNMSTest {

    AbstractServiceMonitor m_monitor;
    Map<String, Object> m_params;


    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();

        m_monitor = new PageSequenceMonitor();

        m_params = new HashMap<String, Object>();
        m_params.put("timeout", "8000");
        m_params.put("retries", "1");

    }

    protected MonitoredService getHttpService(String hostname) throws Exception {
        return getHttpService(hostname, InetAddressUtils.addr(hostname));
    }

    protected MonitoredService getHttpService(String hostname, InetAddress inetAddress) throws Exception {
        return new MockMonitoredService(1, hostname, inetAddress, "HTTP");
    }

    @After
    public void tearDown() throws Exception {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Test
    @Ignore
    public void testOpenNMSUserInterface() throws Exception {

        final StringBuilder config = new StringBuilder();
        LineNumberReader in = new LineNumberReader(
            new InputStreamReader(
                Thread.currentThread().getContextClassLoader().getResourceAsStream("opennmsPageSequence.xml"),
                StandardCharsets.UTF_8
            )
        );
        String line;
        while ((line = in.readLine()) != null) {
            config.append(line);
        }
        m_params.put("page-sequence", config.toString());
        m_params.put("virtualHost", "localhost");
        m_params.put("port", "8980");
        m_params.put("adminUsername", "admin");
        m_params.put("adminPassword", "admin");

        try {
            PollStatus status = m_monitor.poll(getHttpService("localhost"), m_params);
            assertTrue("Expected available but was "+status+": reason = "+status.getReason(), status.isAvailable());
        } finally {
            // Print some debug output if necessary
        }
    }
}
