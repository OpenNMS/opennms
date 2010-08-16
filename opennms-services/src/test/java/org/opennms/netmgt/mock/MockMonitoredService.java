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

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.opennms.netmgt.poller.IPv4NetworkInterface;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;

public class MockMonitoredService implements MonitoredService {
    int m_nodeId;
    String m_nodeLabel;
    String m_ipAddr;
    String m_svcName;
    private InetAddress m_inetAddr;

    public MockMonitoredService(int nodeId, String nodeLabel, String ipAddr, String svcName) throws UnknownHostException {
        m_nodeId = nodeId;
        m_nodeLabel = nodeLabel;
        m_ipAddr = ipAddr;
        m_svcName = svcName;
        m_inetAddr = InetAddress.getByName(ipAddr);
    }

    public String getSvcName() {
        return m_svcName;
    }

    public String getIpAddr() {
        return m_ipAddr;
    }

    public int getNodeId() {
        return m_nodeId;
    }

    public String getNodeLabel() {
        return m_nodeLabel;
    }

    public NetworkInterface getNetInterface() {
        return new IPv4NetworkInterface(getAddress());
    }

    public InetAddress getAddress() {
        return m_inetAddr;
    }

	public String getSvcUrl() {
		// TODO Auto-generated method stub
		return null;
	}
    
}