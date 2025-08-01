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
import static org.opennms.netmgt.poller.monitors.DNSResolutionMonitor.PARM_NAMESERVER;
import static org.opennms.netmgt.poller.monitors.DNSResolutionMonitor.PARM_RESOLUTION_TYPE;
import static org.opennms.netmgt.poller.monitors.DNSResolutionMonitor.PARM_RESOLUTION_TYPE_BOTH;
import static org.opennms.netmgt.poller.monitors.DNSResolutionMonitor.PARM_RESOLUTION_TYPE_EITHER;
import static org.opennms.netmgt.poller.monitors.DNSResolutionMonitor.PARM_RESOLUTION_TYPE_V4;
import static org.opennms.netmgt.poller.monitors.DNSResolutionMonitor.PARM_RESOLUTION_TYPE_V6;
import static org.opennms.netmgt.poller.monitors.DNSResolutionMonitor.PARM_LOOKUP;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.dns.annotations.DNSEntry;
import org.opennms.core.test.dns.annotations.DNSZone;
import org.opennms.core.test.dns.annotations.JUnitDNSServer;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.mock.MockMonitoredService;
import static org.opennms.netmgt.poller.monitors.DNSResolutionMonitor.PARM_RECORD_TYPES;
import static org.opennms.netmgt.poller.monitors.DNSResolutionMonitor.PARM_RECORD_TYPE_CNAME;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.mock.MockUtil;
import org.springframework.test.context.ContextConfiguration;

/**
 * DNSResolutionMonitorIT
 *
 * @author brozow
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/META-INF/opennms/emptyContext.xml"})
@JUnitDNSServer(port = 9153, zones = {
    @DNSZone(name = "example.com", entries = {
        @DNSEntry(hostname = "test", data = "192.168.0.1")
    })
    ,

    @DNSZone(name = "opennms.org", entries = {
        @DNSEntry(hostname = "wipv6day", data = "192.168.0.2")
        ,
        @DNSEntry(hostname = "wipv6day", data = "fded:beef:cafe:1::63", type = "AAAA")
        ,
        @DNSEntry(hostname = "choopa-ipv4", data = "192.168.0.3")
        ,
        @DNSEntry(hostname = "choopa-ipv6", data = "fded:beef:cafe:1::64", type = "AAAA")
        ,
        @DNSEntry(hostname = "www", data = "web02.opennms.org.", type = "CNAME")
        ,
        @DNSEntry(hostname = "web02", data = "192.168.0.4", type = "A")
        ,
        @DNSEntry(hostname = "web02", data = "fded:beef:cafe:1::65", type = "AAAA"),})
})
@JUnitConfigurationEnvironment
public class DNSResolutionMonitorIT {

    @Rule
    public TestName m_test = new TestName();

    @Before
    public void setUp() {
        MockUtil.println("------------ begin test " + m_test.getMethodName() + " ------------");
        MockLogAppender.setupLogging(true);
    }

    @After
    public void tearDown() throws Exception {
        MockUtil.println("------------ end test " + m_test.getMethodName() + " ------------");
    }

    @Test
    public void pollIPv4andIPv6() throws Exception {
        MockMonitoredService dual = new MockMonitoredService(1, "wipv6day.opennms.org", InetAddress.getLocalHost(), "RESOLVE");

        DNSResolutionMonitor monitor = new DNSResolutionMonitor();

        Map<String, Object> v4Parms = new HashMap<>();
        v4Parms.put(PARM_RESOLUTION_TYPE, PARM_RESOLUTION_TYPE_V4);
        v4Parms.put(PARM_NAMESERVER, "[::1]:9153");
        Map<String, Object> v6Parms = new HashMap<>();
        v6Parms.put(PARM_RESOLUTION_TYPE, PARM_RESOLUTION_TYPE_V6);
        v6Parms.put(PARM_NAMESERVER, "[::1]:9153");
        Map<String, Object> bothParms = new HashMap<>();
        bothParms.put(PARM_RESOLUTION_TYPE, PARM_RESOLUTION_TYPE_BOTH);
        bothParms.put(PARM_NAMESERVER, "[::1]:9153");
        Map<String, Object> eitherParms = new HashMap<>();
        eitherParms.put(PARM_RESOLUTION_TYPE, PARM_RESOLUTION_TYPE_EITHER);
        eitherParms.put(PARM_NAMESERVER, "[::1]:9153");

        assertEquals(PollStatus.available(), monitor.poll(dual, v4Parms));
        assertEquals(PollStatus.available(), monitor.poll(dual, v6Parms));
        assertEquals(PollStatus.available(), monitor.poll(dual, bothParms));
        assertEquals(PollStatus.available(), monitor.poll(dual, eitherParms));
    }

    @Test
    public void pollIPv4Only() throws Exception {
        MockMonitoredService v4only = new MockMonitoredService(1, "choopa-ipv4.opennms.org", InetAddress.getLocalHost(), "RESOLVE");

        DNSResolutionMonitor monitor = new DNSResolutionMonitor();

        Map<String, Object> v4Parms = new HashMap<>();
        v4Parms.put(PARM_RESOLUTION_TYPE, PARM_RESOLUTION_TYPE_V4);
        v4Parms.put(PARM_NAMESERVER, "[::1]:9153");
        Map<String, Object> v6Parms = new HashMap<>();
        v6Parms.put(PARM_RESOLUTION_TYPE, PARM_RESOLUTION_TYPE_V6);
        v6Parms.put(PARM_NAMESERVER, "[::1]:9153");
        Map<String, Object> bothParms = new HashMap<>();
        bothParms.put(PARM_RESOLUTION_TYPE, PARM_RESOLUTION_TYPE_BOTH);
        bothParms.put(PARM_NAMESERVER, "[::1]:9153");
        Map<String, Object> eitherParms = new HashMap<>();
        eitherParms.put(PARM_RESOLUTION_TYPE, PARM_RESOLUTION_TYPE_EITHER);
        eitherParms.put(PARM_NAMESERVER, "[::1]:9153");

        assertEquals(PollStatus.available(), monitor.poll(v4only, v4Parms));
        assertEquals(PollStatus.unavailable(), monitor.poll(v4only, v6Parms));
        assertEquals(PollStatus.unavailable(), monitor.poll(v4only, bothParms));
        assertEquals(PollStatus.available(), monitor.poll(v4only, eitherParms));
    }

    @Test
    public void pollIPv6Only() throws Exception {
        MockMonitoredService v6only = new MockMonitoredService(1, "choopa-ipv6.opennms.org", InetAddress.getLocalHost(), "RESOLVE");

        DNSResolutionMonitor monitor = new DNSResolutionMonitor();

        Map<String, Object> v4Parms = new HashMap<>();
        v4Parms.put(PARM_RESOLUTION_TYPE, PARM_RESOLUTION_TYPE_V4);
        v4Parms.put(PARM_NAMESERVER, "[::1]:9153");
        Map<String, Object> v6Parms = new HashMap<>();
        v6Parms.put(PARM_RESOLUTION_TYPE, PARM_RESOLUTION_TYPE_V6);
        v6Parms.put(PARM_NAMESERVER, "[::1]:9153");
        Map<String, Object> bothParms = new HashMap<>();
        bothParms.put(PARM_RESOLUTION_TYPE, PARM_RESOLUTION_TYPE_BOTH);
        bothParms.put(PARM_NAMESERVER, "[::1]:9153");
        Map<String, Object> eitherParms = new HashMap<>();
        eitherParms.put(PARM_RESOLUTION_TYPE, PARM_RESOLUTION_TYPE_EITHER);
        eitherParms.put(PARM_NAMESERVER, "[::1]:9153");

        assertEquals(PollStatus.unavailable(), monitor.poll(v6only, v4Parms));
        assertEquals(PollStatus.available(), monitor.poll(v6only, v6Parms));
        assertEquals(PollStatus.unavailable(), monitor.poll(v6only, bothParms));
        assertEquals(PollStatus.available(), monitor.poll(v6only, eitherParms));
    }

    @Test
    public void pollNeitherIPv4orIPv6() throws Exception {
        MockMonitoredService neither = new MockMonitoredService(1, "no-such-name.example.com", InetAddress.getLocalHost(), "RESOLVE");

        DNSResolutionMonitor monitor = new DNSResolutionMonitor();

        Map<String, Object> v4Parms = new HashMap<>();
        v4Parms.put(PARM_RESOLUTION_TYPE, PARM_RESOLUTION_TYPE_V4);
        v4Parms.put(PARM_NAMESERVER, "[::1]:9153");
        Map<String, Object> v6Parms = new HashMap<>();
        v6Parms.put(PARM_RESOLUTION_TYPE, PARM_RESOLUTION_TYPE_V6);
        v6Parms.put(PARM_NAMESERVER, "[::1]:9153");
        Map<String, Object> bothParms = new HashMap<>();
        bothParms.put(PARM_RESOLUTION_TYPE, PARM_RESOLUTION_TYPE_BOTH);
        bothParms.put(PARM_NAMESERVER, "[::1]:9153");
        Map<String, Object> eitherParms = new HashMap<>();
        eitherParms.put(PARM_RESOLUTION_TYPE, PARM_RESOLUTION_TYPE_EITHER);
        eitherParms.put(PARM_NAMESERVER, "[::1]:9153");

        assertEquals(PollStatus.unavailable(), monitor.poll(neither, v4Parms));
        assertEquals(PollStatus.unavailable(), monitor.poll(neither, v6Parms));
        assertEquals(PollStatus.unavailable(), monitor.poll(neither, bothParms));
        assertEquals(PollStatus.unavailable(), monitor.poll(neither, eitherParms));
    }

    @Test
    public void pollIPv4CNAME() throws Exception {
        MockMonitoredService cname = new MockMonitoredService(1, "www.opennms.org", InetAddress.getLocalHost(), "RESOLVE");

        DNSResolutionMonitor monitor = new DNSResolutionMonitor();

        Map<String, Object> params = new HashMap<>();
        params.put(PARM_RECORD_TYPES, PARM_RECORD_TYPE_CNAME);
        params.put(PARM_NAMESERVER, "[::1]:9153");

        assertEquals(PollStatus.available(), monitor.poll(cname, params));
    }

    @Test
    public void testLookup() throws Exception {
        MockMonitoredService lookup = new MockMonitoredService(1, "no-such-name.example.com", InetAddress.getLocalHost(), "RESOLVE");

        DNSResolutionMonitor monitor = new DNSResolutionMonitor();

        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put(PARM_RESOLUTION_TYPE, PARM_RESOLUTION_TYPE_EITHER);
        parms.put(PARM_LOOKUP, "wipv6day.opennms.org");
        parms.put(PARM_NAMESERVER, "[::1]:9153");

        assertEquals(PollStatus.available(), monitor.poll(lookup, parms));
    }

    @Test
    public void testSubbedLookup() throws Exception {
        MockMonitoredService lookup = new MockMonitoredService(1, "wipv6day.opennms.org", InetAddress.getLocalHost(), "RESOLVE");

        DNSResolutionMonitor monitor = new DNSResolutionMonitor();

        Map<String, Object> parms = new HashMap<String, Object>();
        parms.put(PARM_RESOLUTION_TYPE, PARM_RESOLUTION_TYPE_EITHER);
        parms.put(PARM_LOOKUP, "{nodeLabel}");
        parms.put(PARM_NAMESERVER, "[::1]:9153");

        Map<String, Object> subbedParams = monitor.getRuntimeAttributes(lookup, parms);
        // this would normally happen in the poller request builder implementation
        subbedParams.forEach((k, v) -> {
            parms.put(k, v);
        });
        assertEquals(PollStatus.available(), monitor.poll(lookup, parms));
    }
}
