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

import java.sql.Timestamp;


public class Outage {
    int m_nodeId;
    String m_ipAddr;
    int m_serviceId;
    String m_serviceName;
    int m_lostEventId;
    int m_regainedEventId;
    Timestamp m_lostEventTime;
    Timestamp m_regainedEventTime;

    Outage(int nodeId, String ipAddr, int serviceId) {
        m_nodeId = nodeId;
        m_ipAddr = ipAddr;
        m_serviceId = serviceId;
    }
    
    Outage(MockService svc) {
        this(svc.getNodeId(), svc.getIpAddr(), svc.getId());
    }

    public void setLostEvent(int eventId, Timestamp eventTime) {
        m_lostEventId = eventId;
        m_lostEventTime = eventTime;
    }
    
    public void setRegainedEvent(int eventId, Timestamp eventTime) {
        m_regainedEventId = eventId;
        m_regainedEventTime = eventTime;
    }
    
    public boolean isForService(MockService svc) {
        return m_nodeId == svc.getNodeId() &&
            m_ipAddr.equals(svc.getIpAddr()) &&
            m_serviceId == svc.getId();
    }
    
    public int hashCode() {
        return 0;
    }
    
    public String toString() {
        return "Outage["+m_nodeId+"/"+m_ipAddr+"/"+(m_serviceName == null ? ""+m_serviceId : m_serviceName)+" cause: "+m_lostEventId+" resolution: "+m_regainedEventId+" ]";
    }
    
    public boolean equals(Object o) {
        if (! (o instanceof Outage)) return false;
        Outage outage = (Outage)o;
        return (
                (m_nodeId == outage.m_nodeId) && 
                (m_ipAddr.equals(outage.m_ipAddr)) && 
                (m_serviceId == outage.m_serviceId) &&
                (m_lostEventId == outage.m_lostEventId) &&
                (m_lostEventTime == null ? outage.m_lostEventTime == null : m_lostEventTime.equals(outage.m_lostEventTime)) &&
                (m_regainedEventId == outage.m_regainedEventId) &&
                (m_regainedEventTime == null ? outage.m_regainedEventTime == null : m_regainedEventTime.equals(outage.m_regainedEventTime))
                );
    }

    /**
     * @return
     */
    public String toDetailedString() {
        return "Outage[" +
                m_nodeId + ":" +
                m_ipAddr + ":" +
                m_serviceId + ":" +
                m_lostEventId + ":" +
                m_lostEventTime + ":" +
                m_regainedEventId + ":" +
                m_regainedEventTime + 
                "]";
    }

    public int getServiceId() {
        return m_serviceId;
    }
    
    public void setServiceName(String svcName) {
        m_serviceName = svcName;
    }
    
    
}
