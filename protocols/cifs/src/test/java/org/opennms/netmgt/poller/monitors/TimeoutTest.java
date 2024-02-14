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
import static org.junit.Assert.assertTrue;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.mock.MonitorTestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeoutTest {
    public static final Logger LOG = LoggerFactory.getLogger(TimeoutTest.class);

    public void testTimout(String host, final int timeout, final int limit) throws UnknownHostException {
        final MonitoredService svc = MonitorTestUtils.getMonitoredService(99, InetAddress.getByName(host), "JCIFS");
        final Map<String, Object> m = Collections.synchronizedMap(new TreeMap<String, Object>());
        final JCifsMonitor jCifsMonitor = new JCifsMonitor();

        m.put("username", "user");
        m.put("password", "pass");
        m.put("domain", "dom");
        m.put("mode", "PATH_EXIST");
        m.put("path", "/share");
        m.put("timeout", String.valueOf(timeout));
        m.put("retry", "0");

        long startTime = System.currentTimeMillis();
        final PollStatus pollStatus = jCifsMonitor.poll(svc, m);
        long delta = System.currentTimeMillis() - startTime;
        assertEquals(PollStatus.down(), pollStatus);

        LOG.info("Checking " + delta + " <= " + limit);
        assertTrue("Limit reached " + delta + " > " + limit, delta <= limit);
    }

    @Test
    public void testTimeouts() throws Exception {
        // first call took more time
        // jcifs-ng seems to take a lot longer but does vary based on the timeout provided
        testTimout("169.254.123.123",500, 30000);
        // but after that the timeouts are correctly applied
        testTimout("169.254.123.124",1000, 8000);
        testTimout("169.254.123.125",2000, 9000);
        testTimout("169.254.123.126",3000, 10000);
    }
}
