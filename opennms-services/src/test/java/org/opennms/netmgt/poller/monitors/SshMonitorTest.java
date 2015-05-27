/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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
import java.util.regex.PatternSyntaxException;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.mock.MockMonitoredService;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/META-INF/opennms/emptyContext.xml",
		"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
		"classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
		"classpath:/META-INF/opennms/applicationContext-soa.xml"})
@JUnitConfigurationEnvironment
public class SshMonitorTest {
    public static final String HOST_TO_TEST = "127.0.0.1";

    @Test
    public void testPoll() throws UnknownHostException {

        ServiceMonitor sm = new SshMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Router", InetAddressUtils.addr(HOST_TO_TEST), "SSH");
        Map<String, Object> parms = new HashMap<String, Object>();

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(createAssertMessage(ps, "Up"), ps.isUp());
        assertFalse(createAssertMessage(ps, "not Down"), ps.isDown());
    }

    @Test
    public void testPollWithMatch() throws UnknownHostException {

        ServiceMonitor sm = new SshMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Router", InetAddressUtils.addr(HOST_TO_TEST), "SSH");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("match", "SSH");

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(createAssertMessage(ps, "Up"), ps.isUp());
        assertFalse(createAssertMessage(ps, "not Down"), ps.isDown());
    }

    @Test
    public void testPollWithStarBanner() throws UnknownHostException {

        ServiceMonitor sm = new SshMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Router", InetAddressUtils.addr(HOST_TO_TEST), "SSH");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("banner", "*");

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(createAssertMessage(ps, "Up"), ps.isUp());
        assertFalse(createAssertMessage(ps, "not Down"), ps.isDown());
    }

    @Test
    public void testPollWithRegexpBanner() throws UnknownHostException {

        ServiceMonitor sm = new SshMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Router", InetAddressUtils.addr(HOST_TO_TEST), "SSH");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("banner", "^SSH");

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(createAssertMessage(ps, "Up"), ps.isUp());
        assertFalse(createAssertMessage(ps, "not Down"), ps.isDown());
    }

    @Test
    public void testPollWithBannerOpenSSH() throws UnknownHostException {

        ServiceMonitor sm = new SshMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Router", InetAddressUtils.addr(HOST_TO_TEST), "SSH");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("banner", "OpenSSH");

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(createAssertMessage(ps, "Up"), ps.isUp());
        assertFalse(createAssertMessage(ps, "not Down"), ps.isDown());
    }

    @Test
    public void testPollWithBannerMissing() throws UnknownHostException {

        ServiceMonitor sm = new SshMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Router", InetAddressUtils.addr(HOST_TO_TEST), "SSH");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("banner", "OpenNMS");

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(createAssertMessage(ps, "Down"), ps.isDown());
        assertFalse(createAssertMessage(ps, "not Up"), ps.isUp());
    }

    @Test
    public void testPollWithBannerOpenSSHRegexp() throws UnknownHostException {

        ServiceMonitor sm = new SshMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Router", InetAddressUtils.addr(HOST_TO_TEST), "SSH");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("banner", "^SSH\\-2\\.0\\-OpenSSH_\\d+\\.\\d+.*$");

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(createAssertMessage(ps, "Up"), ps.isUp());
        assertFalse(createAssertMessage(ps, "not Down"), ps.isDown());
    }

    @Test
    public void testPollWithInvalidRegexpBanner() throws UnknownHostException, PatternSyntaxException {

        ServiceMonitor sm = new SshMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Router", InetAddressUtils.addr(HOST_TO_TEST), "SSH");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("banner", "^SSH\\-2\\.0\\-OpenSSH_\\d+\\.\\d+\\g$");

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(ps.isUnavailable());
        assertTrue(createAssertMessage(ps, "Unavailable"), ps.isUnavailable());
    }

    @Test
    public void testPollWithInvalidRegexpMatch() throws UnknownHostException, PatternSyntaxException {

        ServiceMonitor sm = new SshMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Router", InetAddressUtils.addr(HOST_TO_TEST), "SSH");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("banner", "^SSH\\-2\\.0\\-OpenSSH_\\d+\\.\\d+\\g$");

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(createAssertMessage(ps, "Unavailable"), ps.isUnavailable());
    }

    @Test
    public void testPollWithInvalidHost() throws UnknownHostException {

        ServiceMonitor sm = new SshMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Router", InetAddressUtils.UNPINGABLE_ADDRESS, "SSH");
        Map<String, Object> parms = new HashMap<String, Object>();

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(createAssertMessage(ps, "Unavailable"), ps.isUnavailable());
    }

    @Test
    public void testPollWithNoIpAddress() throws UnknownHostException {

        ServiceMonitor sm = new SshMonitor();
        MonitoredService svc = new MockMonitoredService(1, "Router", null, "SSH");
        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put("banner", "OpenNMS");

        PollStatus ps = sm.poll(svc, parms);
        assertTrue(createAssertMessage(ps, "Down"), ps.isDown());
        assertFalse(createAssertMessage(ps, "not Up"), ps.isUp());
    }

    private String createAssertMessage(PollStatus ps, String expectation) {
        return "polled service is " + ps.toString() + " not " + expectation + " due to: " + ps.getReason() + " (do you have an SSH daemon running on " + HOST_TO_TEST + "?)";
    }
}
