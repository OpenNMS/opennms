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

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.support.SimpleMonitoredService;

/**
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class AvailabilityMonitorTest {

    /**
     * Test method for {@link org.opennms.netmgt.poller.support.AvailabilityMonitor#poll(org.opennms.netmgt.poller.MonitoredService, Map)}.
     */
    @Test
    public final void testPoll() {
        ServiceMonitor sm = new AvailabilityMonitor();
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("timeout", "3000");
        MonitoredService svc = new SimpleMonitoredService(InetAddressUtils.addr("127.0.0.1"), "ICMP");
        PollStatus status = sm.poll(svc, parameters);
        assertEquals(PollStatus.SERVICE_AVAILABLE, status.getStatusCode());
    }

}
