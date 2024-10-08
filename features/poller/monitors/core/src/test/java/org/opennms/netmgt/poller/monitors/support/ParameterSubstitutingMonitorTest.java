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
