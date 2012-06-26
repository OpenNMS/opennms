//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
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
package org.opennms.netmgt.invd;

import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.poller.InetNetworkInterface;
import org.opennms.netmgt.model.OnmsIpInterface;

import java.net.InetAddress;

public class ScanningClient extends InetNetworkInterface {
	private static final long serialVersionUID = -1976925187647808430L;
	private int m_nodeId = -1;
    //private InetAddress m_inetAddress = null;

    private Integer m_ifaceId;
    private IpInterfaceDao m_ifaceDao;

    public InetAddress getAddress() {
        if (m_address == null) {
        	m_address = getIpInterface().getIpAddress();
        }
        return m_address;
    }

    public ScanningClient(Integer ifaceId, IpInterfaceDao ifaceDao) {
        super(null);
        
        m_ifaceDao = ifaceDao;
        m_ifaceId = ifaceId;
    }

    public String getHostAddress() {
        return getAddress().getHostAddress();
    }
    
    @Override
    public InetAddress getInetAddress() {
    	return getAddress();
    }

    public int getNodeId() {
        if (m_nodeId == -1) {
            m_nodeId = getIpInterface().getNode().getId() == null ? -1 : getIpInterface().getNode().getId().intValue();;
        }
        return m_nodeId;
    }

    public void validateAgent() {
        // Not sure if there is anything to do here.
    }

    public String toString() {
        return "Agent[nodeid = "+getNodeId()+" ipaddr= "+getHostAddress()+']';
    }

    OnmsIpInterface getIpInterface() {
        return m_ifaceDao.load(m_ifaceId);
    }
}
