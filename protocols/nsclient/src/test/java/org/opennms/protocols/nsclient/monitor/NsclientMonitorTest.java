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
package org.opennms.protocols.nsclient.monitor;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.mock.MockMonitoredService;
import org.opennms.protocols.nsclient.AbstractNsclientTest;

/**
 * <p>JUnit Test Class for NsclientMonitor.</p>
 *
 * @author Alejandro Galue <agalue@opennms.org>
 * @version $Id: $
 */
public class NsclientMonitorTest extends AbstractNsclientTest {

    @Test
    public void testMonitorSuccess() throws Exception {
        startServer("None&1", "NSClient++ 0.3.8.75 2010-05-27");
        NsclientMonitor monitor = new NsclientMonitor();
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("port", getServer().getLocalPort());
        PollStatus status = monitor.poll(createMonitoredService(), parameters);
        Assert.assertTrue(status.isAvailable());
        stopServer();
    }

    @Test
    public void testMonitorFail() throws Exception {
        startServer("None&1", "ERROR: I don't know what you mean");
        NsclientMonitor monitor = new NsclientMonitor();
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("port", getServer().getLocalPort());
        PollStatus status = monitor.poll(createMonitoredService(), parameters);
        Assert.assertFalse(status.isAvailable());
        stopServer();
    }

    private MonitoredService createMonitoredService() {
        return new MockMonitoredService(1, "winsrv", getServer().getInetAddress(), "NSClient");
    }

}
