/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.mock;

import java.util.Map;


import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MockMonitor implements ServiceMonitor {
	
	private static final Logger LOG = LoggerFactory.getLogger(MockMonitor.class);


    private MockNetwork m_network;

    private String m_svcName;

    /**
     * Simple constructor so that the MockMonitor can be used as a placeholder {@link ServiceMonitor}
     * inside config files.
     */
    public MockMonitor() {}

    /**
     * @param network
     * @param svcName
     */
    public MockMonitor(MockNetwork network, String svcName) {
        m_network = network;
        m_svcName = svcName;
    }

    @Override
    public void initialize(MonitoredService svc) {
    }

    @Override
    public void initialize(Map<String, Object> parameters) {
    }

    @Override
    public PollStatus poll(MonitoredService monSvc, Map<String, Object> parameters) {
        synchronized(m_network) {
            int nodeId = monSvc.getNodeId();
            String ipAddr = monSvc.getIpAddr();
            MockService svc = m_network.getService(nodeId, ipAddr, m_svcName);
            if (svc == null) {
                LOG.info("Invalid Poll: {}{}", ipAddr, m_svcName);
                m_network.receivedInvalidPoll(ipAddr, m_svcName);
                return PollStatus.unknown();
            } else {
                LOG.info("Poll: [{}{}{}]", svc.getInterface().getNode().getLabel(), ipAddr, m_svcName);
                PollStatus pollStatus = svc.poll();
				return PollStatus.get(pollStatus.getStatusCode(), pollStatus.getReason());
            }
        }
    }

    @Override
    public void release() {
    }

    @Override
    public void release(MonitoredService svc) {
    }

}
