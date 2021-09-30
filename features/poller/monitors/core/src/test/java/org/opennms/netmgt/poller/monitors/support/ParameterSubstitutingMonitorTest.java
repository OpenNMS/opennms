/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.monitors.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.mock.MockMonitoredService;

public class ParameterSubstitutingMonitorTest {

    @Before
    public void setUp() throws Exception {
        MockLogAppender.setupLogging(true);
    }

    /** see also: https://issues.opennms.org/browse/NMS-12556 */
    @Test
    public void unknownPlaceholdersShouldNotLeadToNullpointer() throws UnknownHostException {
        ParameterSubstitutingMonitor monitor = new ParameterSubstitutingMonitor() {
            @Override
            public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
                throw new UnsupportedOperationException("we are a mock method.");
            }
        };
        Map<String, Object> m = new HashMap<>();
        m.put("port", 80);
        m.put("retries", 0);
        m.put("timeout", 2000);
        m.put("userid", "{ipAddr}");
        m.put("password", "{nodeLabel}");
        m.put("server-name", "{nodelabel}"); // written wrong, correct name would be "nodeLabel"
        MockMonitoredService svc = new MockMonitoredService(1, "Node One", InetAddress.getByName("127.0.0.1"), "FTP");
        Map<String, Object> subbedParams = monitor.getRuntimeAttributes(svc, m);
        assertEquals("127.0.0.1", subbedParams.get("subbed-userid"));
        assertEquals("Node One", subbedParams.get("subbed-password"));
        assertNull(subbedParams.get("subbed-server-name")); // server name should not be found since it was written wrong
    }
}
