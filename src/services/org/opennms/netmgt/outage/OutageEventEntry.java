//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 08: Changed SQL "= null" to "is null" to work with Postgres 7.2.
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
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.netmgt.outage;



/**
 * This class defines the entries in the Outage cache.
 *
 * @author <A HREF="mailto:tarus@opennms.org">Tarus</A>
 * 
 */
public final class OutageEventEntry
{
	/**
	 * Lookup the event type
	 */
	public static final int	EVENT_TYPE_REGAINED_SERVICE 	= 1;
	public static final int EVENT_TYPE_LOST_SERVICE 	= 2;
	public static final int EVENT_TYPE_INTERFACE_UP 	= 3;
	public static final int EVENT_TYPE_INTERFACE_DOWN 	= 4;
	public static final int EVENT_TYPE_NODE_UP	 	= 5;
	public static final int EVENT_TYPE_NODE_DOWN 		= 6;

	private long 	m_nodeId;
	private String  m_ipAddr;
	private long    m_serviceId;
	private long	m_eventId;
	private String  m_eventTime;
	private int     m_type;   

	public OutageEventEntry(long eventId, long nodeId, String ipAddr, long serviceId, String eventTime, int type)
	{
		m_nodeId = nodeId;
		m_ipAddr = ipAddr;
		m_serviceId = serviceId;
		m_eventId = eventId;
		m_eventTime = eventTime;
		m_type = type;
	}
	
	long getNodeId()
	{
		return m_nodeId;
	}
	
	String getIpAddr()
	{
		return m_ipAddr;
	}
	
	long getSvcId()
	{
		return m_serviceId;
	}
	
	String getEventTime()
	{
		return m_eventTime;
	}
	
	int getType()
	{
		return m_type;
	}
	
	long getEventId()
	{
		return m_eventId;
	}
}
