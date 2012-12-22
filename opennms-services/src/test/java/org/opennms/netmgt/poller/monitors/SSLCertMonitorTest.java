/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.mock.MonitorTestUtils;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.mock.MockUtil;
import org.springframework.test.context.ContextConfiguration;


@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={"classpath:/META-INF/opennms/emptyContext.xml"})
@JUnitConfigurationEnvironment
public class SSLCertMonitorTest {

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging();
    }

    @Test
    @JUnitHttpServer(port=10342, https=true)
    public void testNMS4142() throws UnknownHostException {
        SSLCertMonitor monitor = new SSLCertMonitor();
        Map<String, Object> parameters = new ConcurrentSkipListMap<String, Object>();
        parameters.put("port", "10342");
        parameters.put("retry", "0");
        parameters.put("timeout", "500");
        parameters.put("verbose", "true");
        parameters.put("days", "5");

        MonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", "SSLCert", false);
        PollStatus status = monitor.poll(svc, parameters);
        MockUtil.println("Reason: "+status.getReason());
        assertFalse(status.isAvailable());
    }

    @Test
    @JUnitHttpServer(port=10342, https=true)
    public void testValidDateForCertificate() throws UnknownHostException {
        SSLCertMonitor monitor = new SSLCertMonitor();
        Map<String, Object> parameters = new ConcurrentSkipListMap<String, Object>();
        parameters.put("port", "10342");
        parameters.put("retry", "0");
        parameters.put("timeout", "500");
        parameters.put("verbose", "true");
        parameters.put("days", "5");

        /* The certificate JUnitHttpServer uses is valid from Fri Jan 15 17:25:10 CST 2010 to
         * Thu Apr 15 18:25:10 CDT 2010.
         */
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTimeInMillis(1271373909000L - 86400000 * 5);
        monitor.setCalendar(cal);
        MonitoredService svc = MonitorTestUtils.getMonitoredService(3, "localhost", "SSLCert", false);
        PollStatus status = monitor.poll(svc, parameters);
        assertTrue(status.isAvailable());
    }

    @Test
    @Ignore
    public void testInternetWebsite() throws UnknownHostException {
        SSLCertMonitor monitor = new SSLCertMonitor();
        Map<String, Object> parameters = new ConcurrentSkipListMap<String, Object>();
        parameters.put("port", "443");
        parameters.put("retry", "0");
        parameters.put("timeout", "500");
        parameters.put("verbose", "true");
        parameters.put("days", "5");

        MonitoredService svc = MonitorTestUtils.getMonitoredService(3, "www.google.com", "SSLCert", false);
        PollStatus status = monitor.poll(svc, parameters);
        assertTrue(status.isAvailable());
    }
}
