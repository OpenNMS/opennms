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
 * This class encapsulates all the information required by the 
 * SNMP collector in order to perform data collection for an 
 * individual interface and store that data in an appropriately 
 * named RRD file.  
 *
 * @author <a href="mailto:mike@opennms.org">Mike Davidson</a>
 * @author <a href="http://www.opennms.org/">OpenNMS</a>
 */
final class IfInfo
{
	private int	m_index;
	private int 	m_type;
	private String	m_label;
	private String	m_collType;
	private boolean m_isPrimary;
	
	private List	m_oidList;
	private List	m_dsList;
	
	public IfInfo(int ifIndex, int ifType, String ifLabel, String collType)
	{
		m_index = ifIndex;
		m_type = ifType;
		m_label = ifLabel;
		m_collType = collType;
		m_isPrimary = false;
		m_oidList = null;
		m_dsList = null;
	}
	
	public void setIsPrimary(boolean isPrimary)
	{
		m_isPrimary = isPrimary;
	}
	
	public void setDsList(List dsList)
	{
		m_dsList = dsList;
	}
	
	public void setOidList(List oidList)
	{
		m_oidList = oidList;
	}
	
	public int getIndex()
	{
		return m_index;
	}
	
	public int getType()
	{
		return m_type;
	}
	
	public String getLabel()
	{
		return m_label;
	}
	
	public String getCollType()
	{
		return m_collType;
	}
	
	public boolean getIsPrimary()
	{
		return m_isPrimary;
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
