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
package org.opennms.netmgt.mock;

import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockMonitor extends AbstractServiceMonitor {

    private static final Logger LOG = LoggerFactory.getLogger(MockMonitor.class);

    private MockNetwork m_network;

    private String m_svcName;

    /**
     * Simple constructor so that the MockMonitor can be used as a placeholder
     * {@link ServiceMonitor} inside config files.
     */
    public MockMonitor() {
        m_network = new MockNetwork(); // So that this can be use in
                                       // synchronized() statements
    }

    /**
     * @param network
     * @param svcName
     */
    public MockMonitor(MockNetwork network, String svcName) {
        m_network = network;
        m_svcName = svcName;
    }

    @Override
    public PollStatus poll(MonitoredService monSvc, Map<String, Object> parameters) {
        if (parameters.containsKey("status")) {
            final int statusCode = getKeyedInteger(parameters, "status", PollStatus.SERVICE_UNKNOWN);
            final String reason = getKeyedString(parameters, "reason", null);
            return PollStatus.get(statusCode, reason);
        }

        synchronized (m_network) {
            return doPoll(monSvc.getNodeId(), monSvc.getIpAddr(), m_svcName);
        }
    }

    private PollStatus doPoll(int nodeId, String ipAddr, String svcName) {
        synchronized (m_network) {
            MockService svc = m_network.getService(nodeId, ipAddr, svcName);
            if (svc == null) {
                LOG.info("Invalid Poll: {}/{}/{}", nodeId, ipAddr, svcName);
                m_network.receivedInvalidPoll(ipAddr, svcName);
                return PollStatus.unknown("Mock.");
            } else {
                LOG.info("Poll: [{}/{}/{}]", svc.getInterface().getNode().getLabel(), ipAddr, svcName);
                PollStatus pollStatus = svc.poll();
                return PollStatus.get(pollStatus.getStatusCode(), pollStatus.getReason());
            }
        }
    }

    @Override
    public Map<String, Object> getRuntimeAttributes(MonitoredService svc, Map<String, Object> parameters) {
        final Map<String, Object> attributes = new HashMap<>();
        final PollStatus pollStatus = doPoll(svc.getNodeId(), svc.getIpAddr(), m_svcName);
        attributes.put("status", Integer.toString(pollStatus.getStatusCode()));
        attributes.put("reason", pollStatus.getReason());
        return attributes;
    }

}
