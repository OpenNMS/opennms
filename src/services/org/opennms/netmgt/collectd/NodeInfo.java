//
// Copyright (C) 2002 Sortova Consulting Group, Inc.  All rights reserved.
// Parts Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.sortova.com/
//
//
// Tab Size = 8
//
package org.opennms.netmgt.collectd;

import java.lang.*;
import java.util.*;

/**
 * This class encapsulates all of the node-level data required by
 * the SNMP data collector in order to successfully perform data
 * collection for a scheduled primary SNMP interface.
 *
 * @author <a href="mailto:mike@opennms.org">Mike Davidson</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 */
final class NodeInfo
{
	private int 	m_nodeId;
	private int 	m_primarySnmpIfIndex;
	private List 	m_oidList;
	private List	m_dsList;
	
	
	public NodeInfo(int nodeId, int primaryIfIndex)
	{
		m_nodeId = nodeId;
		m_primarySnmpIfIndex = primaryIfIndex;
		m_oidList = null;
		m_dsList = null;
	}
	
	public int getNodeId()
	{
		return m_nodeId;
	}
	
	public void setNodeId(int nodeId)
	{
		m_nodeId = nodeId;
	}
	
	public int getPrimarySnmpIfIndex()
	{
		return m_primarySnmpIfIndex;
	}
	
	public void setDsList(List dsList)
	{
		m_dsList = dsList;
	}
	
	public void setOidList(List oidList)
	{
		m_oidList = oidList;
	}
	
	public List getDsList()
	{
		return m_dsList;
	}
	
	public List getOidList()
	{
		return m_oidList;
	}
} // end class
