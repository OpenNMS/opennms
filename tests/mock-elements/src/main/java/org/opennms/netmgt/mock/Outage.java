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

    public Outage(int nodeId, String ipAddr, int serviceId) {
        m_nodeId = nodeId;
        m_ipAddr = ipAddr;
        m_serviceId = serviceId;
    }
    
    public Outage(MockService svc) {
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
    
    @Override
    public int hashCode() {
        return 0;
    }
    
    @Override
    public String toString() {
        return "Outage["+m_nodeId+"/"+m_ipAddr+"/"+(m_serviceName == null ? ""+m_serviceId : m_serviceName)+" cause: "+m_lostEventId+" resolution: "+m_regainedEventId+" ]";
    }
    
    @Override
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