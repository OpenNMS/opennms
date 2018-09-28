/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.mock;

import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.poller.support.AbstractServiceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Mark this as {@link Distributable} so that we can reuse it for the remote
 * poller tests.
 */
@Distributable
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
