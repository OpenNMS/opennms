//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//

package org.opennms.netmgt.mock;

import java.util.Map;

import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.test.mock.MockUtil;

public class MockMonitor implements ServiceMonitor {

    private MockNetwork m_network;

    private String m_svcName;

    /**
     * @param network
     * @param svcName
     */
    public MockMonitor(MockNetwork network, String svcName) {
        m_network = network;
        m_svcName = svcName;
    }

    public void initialize(MonitoredService svc) {
    }

    public void initialize(Map<String, Object> parameters) {
    }

    public PollStatus poll(MonitoredService monSvc, Map<String, Object> parameters) {
        synchronized(m_network) {
            int nodeId = monSvc.getNodeId();
            String ipAddr = monSvc.getIpAddr();
            MockService svc = m_network.getService(nodeId, ipAddr, m_svcName);
            if (svc == null) {
                MockUtil.println("Invalid Poll: " + ipAddr + "/" + m_svcName);
                m_network.receivedInvalidPoll(ipAddr, m_svcName);
                return PollStatus.unknown();
            } else {
                MockUtil.println("Poll: [" + svc.getInterface().getNode().getLabel() + "/" + ipAddr + "/" + m_svcName + "]");
                PollStatus pollStatus = svc.poll();
				return PollStatus.get(pollStatus.getStatusCode(), pollStatus.getReason());
            }
        }
    }

    public void release() {
    }

    public void release(MonitoredService svc) {
    }

}
