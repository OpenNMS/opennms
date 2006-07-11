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
package org.opennms.netmgt.dao.jdbc.ipif;

import org.opennms.netmgt.model.OnmsIpInterface;

public class IpInterfaceId {
	private Integer m_nodeId;
	private String m_ipAddr;
	private Integer m_ifIndex;
	
	public IpInterfaceId(Integer nodeId, String ipAddr, Integer ifIndex) {
		m_nodeId = nodeId;
		m_ipAddr = ipAddr;
		m_ifIndex = ifIndex;
	}
	
	public IpInterfaceId(OnmsIpInterface iface) {
		this(iface.getNode().getId(), iface.getIpAddress(), iface.getIfIndex());
	}

	public boolean equals(Object obj) {
		if (obj instanceof IpInterfaceId) {
			IpInterfaceId key = (IpInterfaceId) obj;
			return (
					m_nodeId.equals(key.m_nodeId)
					&& m_ipAddr.equals(key.m_ipAddr)
					&& (m_ifIndex == null ? key.m_ifIndex == null : m_ifIndex.equals(key.m_ifIndex))
					);
		}
		return false;
	}

	public int hashCode() {
		return m_nodeId.hashCode() ^ m_ipAddr.hashCode();
	}

	public String toSqlClause() {
		if (m_ifIndex == null) {
			return "nodeid = ? and ipAddr = ? and ifIndex is null";
		} else {
			return "nodeid = ? and ipAddr = ? and ifIndex = ?";
		}
	}
	
	public Object[] toSqlParmArray() {
		if (m_ifIndex == null) {
			return new Object[] { m_nodeId, m_ipAddr };
		} else {
			return new Object[] { m_nodeId, m_ipAddr, m_ifIndex };
		}
	}

	public Integer getNodeId() {
		return m_nodeId;
	}
	
	public String getIpAddr() {
		return m_ipAddr;
	}
	
	public Integer getIfIndex() {
		return m_ifIndex;
	}
	
}
