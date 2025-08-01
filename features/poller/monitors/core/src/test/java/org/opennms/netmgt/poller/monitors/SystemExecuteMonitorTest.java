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

import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.mock.MockMonitoredService;

public class SystemExecuteMonitorTest {

    private SystemExecuteMonitor monitor;

    private final String NODELABEL = "www.OpenNMS.org";
    private MonitoredService svc;
    private Map<String, Object> parameters;


    @Before
    public void setup() throws Exception {
        monitor = new SystemExecuteMonitor();
        svc = new MockMonitoredService(42, NODELABEL, InetAddress.getLocalHost(), "myService");
        parameters = new HashMap<String, Object>();
    }

    @Test
    public void testPollScriptParameterNotSet() {
        monitor = new SystemExecuteMonitor();
        PollStatus pollStatus = monitor.poll(svc, parameters);
        Assert.assertEquals("Unknown", pollStatus.getStatusName());
    }

    @Test
    public void testPollScriptParameterEmpty() {
        monitor = new SystemExecuteMonitor();
        parameters.put("script", "");
        PollStatus pollStatus = monitor.poll(svc, parameters);
        Assert.assertEquals("Unknown", pollStatus.getStatusName());
    }
}
