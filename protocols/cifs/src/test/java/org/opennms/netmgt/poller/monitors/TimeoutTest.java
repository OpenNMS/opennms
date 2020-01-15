/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
        testTimout("169.254.123.123",500, 10000);
        // but after that the timeouts are correctly applied
        testTimout("169.254.123.124",1000, 1100);
        testTimout("169.254.123.125",2000, 2100);
        testTimout("169.254.123.126",3000, 3100);
    }
}
