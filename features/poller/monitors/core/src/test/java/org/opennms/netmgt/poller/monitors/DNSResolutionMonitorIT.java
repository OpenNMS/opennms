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

import static org.junit.Assert.assertEquals;
import static org.opennms.netmgt.poller.monitors.DNSResolutionMonitor.PARM_RESOLUTION_TYPE;
import static org.opennms.netmgt.poller.monitors.DNSResolutionMonitor.PARM_RESOLUTION_TYPE_BOTH;
import static org.opennms.netmgt.poller.monitors.DNSResolutionMonitor.PARM_RESOLUTION_TYPE_EITHER;
import static org.opennms.netmgt.poller.monitors.DNSResolutionMonitor.PARM_RESOLUTION_TYPE_V4;
import static org.opennms.netmgt.poller.monitors.DNSResolutionMonitor.PARM_RESOLUTION_TYPE_V6;
import static org.opennms.netmgt.poller.monitors.DNSResolutionMonitor.PARM_LOOKUP;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.mock.MockMonitoredService;

/**
 * DNSResolutionMonitorTest
 *
 * @author brozow
 */
public class DNSResolutionMonitorIT {

    @Rule
    public TestName m_test = new TestName();
    
    @Before
    public void setUp() {
        System.out.println("------------ begin test " + m_test.getMethodName() + " ------------");
        MockLogAppender.setupLogging(true);
        System.setProperty("mock.logLevel", "DEBUG");
    }

    @After
    public void tearDown() throws Exception {
        System.out.println("------------ end test " + m_test.getMethodName() + " ------------");
    }
    
    @Test
    public void testPoll() throws Exception {
        MockMonitoredService dual = new MockMonitoredService(1, "wipv6day.opennms.org", InetAddress.getLocalHost(), "RESOLVE");
        MockMonitoredService v4only = new MockMonitoredService(1, "choopa-ipv4.opennms.org", InetAddress.getLocalHost(), "RESOLVE");
        MockMonitoredService v6only = new MockMonitoredService(1, "choopa-ipv6.opennms.org", InetAddress.getLocalHost(), "RESOLVE");
        MockMonitoredService neither = new MockMonitoredService(1, "no-such-name.example.com", InetAddress.getLocalHost(), "RESOLVE");
        
        DNSResolutionMonitor monitor = new DNSResolutionMonitor();

        Map<String, Object> v4Parms = Collections.<String, Object>singletonMap(PARM_RESOLUTION_TYPE,
                                                                               PARM_RESOLUTION_TYPE_V4);
        Map<String, Object> v6Parms = Collections.<String, Object>singletonMap(PARM_RESOLUTION_TYPE,
                                                                               PARM_RESOLUTION_TYPE_V6);
        Map<String, Object> bothParms = Collections.<String, Object>singletonMap(PARM_RESOLUTION_TYPE,
                                                                                 PARM_RESOLUTION_TYPE_BOTH);
        Map<String, Object> eitherParms = Collections.<String, Object>singletonMap(PARM_RESOLUTION_TYPE,
                                                                                   PARM_RESOLUTION_TYPE_EITHER);
        
        assertEquals(PollStatus.available(), monitor.poll(dual, v4Parms));
        assertEquals(PollStatus.available(), monitor.poll(dual, v6Parms));
        assertEquals(PollStatus.available(), monitor.poll(dual, bothParms));
        assertEquals(PollStatus.available(), monitor.poll(dual, eitherParms));

        assertEquals(PollStatus.available(),   monitor.poll(v4only, v4Parms));
        assertEquals(PollStatus.unavailable(), monitor.poll(v4only, v6Parms));
        assertEquals(PollStatus.unavailable(), monitor.poll(v4only, bothParms));
        assertEquals(PollStatus.available(),   monitor.poll(v4only, eitherParms));

        assertEquals(PollStatus.unavailable(), monitor.poll(v6only, v4Parms));
        assertEquals(PollStatus.available(),   monitor.poll(v6only, v6Parms));
        assertEquals(PollStatus.unavailable(), monitor.poll(v6only, bothParms));
        assertEquals(PollStatus.available(),   monitor.poll(v6only, eitherParms));

        assertEquals(PollStatus.unavailable(), monitor.poll(neither, v4Parms));
        assertEquals(PollStatus.unavailable(), monitor.poll(neither, v6Parms));
        assertEquals(PollStatus.unavailable(), monitor.poll(neither, bothParms));
        assertEquals(PollStatus.unavailable(), monitor.poll(neither, eitherParms));

    }

    @Test
    public void testLookup() throws Exception {
        MockMonitoredService lookup = new MockMonitoredService(1, "no-such-name.example.com", InetAddress.getLocalHost(), "RESOLVE");

        DNSResolutionMonitor monitor = new DNSResolutionMonitor();

        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put(PARM_RESOLUTION_TYPE, PARM_RESOLUTION_TYPE_EITHER);
        parms.put(PARM_LOOKUP, "wipv6day.opennms.org");

        assertEquals(PollStatus.available(), monitor.poll(lookup, parms));
    }

}
