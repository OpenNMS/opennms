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
// Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
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
//      http://www.opennms.com/
//

package org.opennms.web.element;


public class Service
{
    int     m_nodeId;
    int     m_ifIndex;
    String  m_ipAddr;
    int     m_serviceId;
    String  m_serviceName;
    String  m_lastGood;
    String  m_lastFail;
    String  m_notify;
    char    m_status;

    public Service() {}

    public Service( int nodeid, 
                            int ifindex, 
                            String ipaddr, 
                            int serviceid, 
                            String serviceName,
                            String lastGood, 
                            String lastFail,
                            String notify,
                            char status)
    {
        m_nodeId = nodeid;
        m_ifIndex = ifindex;
        m_ipAddr = ipaddr;
        m_serviceId = serviceid;
        m_serviceName = serviceName;
        m_lastGood = lastGood;
        m_lastFail = lastFail;
        m_notify = notify;
        m_status = status;
    }


    public int getNodeId()
    {
        return m_nodeId;
    }

    public int getIfIndex()
    {
        return m_ifIndex;
    }

    public String getIpAddress()
    {
        return m_ipAddr;
    }

    public int getServiceId()
    {
        return m_serviceId;
    }

    public String getServiceName()
    {
        return m_serviceName;
    }

    public String getLastGood()
    {
        return m_lastGood;
    }

    public String getLastFail()
    {
        return m_lastFail;
    }
    
    public String getNotify()
    {
        return m_notify;
    }

    public char getStatus() {
        return m_status;
    }
    
    public boolean isManaged()
    {
            return (m_status == 'A');
    }    

    
    public String toString()
    {
        StringBuffer str = new StringBuffer("Node Id = " + m_nodeId + "\n" );
        str.append("Ifindex = " + m_ifIndex + "\n" );
        str.append("Ipaddr = " + m_ipAddr + "\n" );
        str.append("Service id = " + m_serviceId + "\n" );
        str.append("Service name = " + m_serviceName + "\n");
        str.append("Last Good = " + m_lastGood + "\n" );
        str.append("Last Fail  = " + m_lastFail + "\n" );
        str.append("Status = " + m_status + "\n" );
        return str.toString();
    }
}
