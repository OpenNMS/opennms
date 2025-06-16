/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
        this(svc.getNodeId(), svc.getIpAddr(), svc.getSvcId());
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
            m_serviceId == svc.getSvcId();
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