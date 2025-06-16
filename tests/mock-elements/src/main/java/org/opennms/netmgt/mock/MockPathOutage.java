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

import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.xml.event.Event;

public class MockPathOutage extends MockElement{
	
	int m_nodeId;
	InetAddress m_ipAddr;
	String m_svcName;
	int m_lostEventId;
    int m_regainedEventId;
    Timestamp m_lostEventTime;
    Timestamp m_regainedEventTime;
	private int m_pollCount;
	private List<PollAnticipator> m_triggers = new ArrayList<>();
	
	
	public MockPathOutage(MockNetwork parent, int nodeId, InetAddress ipAddr, String svcName) {
		super(parent);
		m_nodeId = nodeId;
		m_ipAddr = ipAddr;
		m_svcName = svcName;
	}

	public MockPathOutage(MockNetwork parent, MockService svc) {
		this(parent,svc.getNodeId(),InetAddressUtils.addr(svc.getIpAddr()),svc.getSvcName());
		
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
            m_svcName == svc.getNodeLabel();
    }
    
    @Override
    public int hashCode() {
        return 0;
    }
    
    @Override
    public String toString() {
        return "Outage["+m_nodeId+"/"+m_ipAddr+"/"+(m_svcName == null ? ""+m_svcName : m_svcName)+" cause: "+m_lostEventId+" resolution: "+m_regainedEventId+" ]";
    }
    
    @Override
    public boolean equals(Object o) {
        if (! (o instanceof MockPathOutage)) return false;
        MockPathOutage outage = (MockPathOutage)o;
        return (
                (m_nodeId == outage.m_nodeId) && 
                (m_ipAddr.equals(outage.m_ipAddr)) && 
                (m_svcName == outage.m_svcName) &&
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
                m_svcName + ":" +
                m_lostEventId + ":" +
                m_lostEventTime + ":" +
                m_regainedEventId + ":" +
                m_regainedEventTime + 
                "]";
    }
    
 // impl
    /** {@inheritDoc} */
        @Override
    public void visit(MockVisitor v) {
        super.visit(v);
        v.visitPathOutage(this);
        
    }
    
    public int getNodeId() {
    	return m_nodeId;
    }
    
    public void setNodeId(int nodeId) {
    	m_nodeId = nodeId;
    }
    
    public InetAddress getIpAddress() {
    	return m_ipAddr;
    }
    
    public void setIpAddress(InetAddress ipAddr) {
    	m_ipAddr = ipAddr;
    }

    public String getServiceName() {
        return m_svcName;
    }
    
    public void setServiceName(String svcName) {
        m_svcName = svcName;
    }

    @Override
	public void addAnticipator(final PollAnticipator trigger) {
    	m_triggers.add(trigger);
	}

	@Override
	Object getKey() {
		return m_svcName;
	}

	@Override
	public int getPollCount() {
		return m_pollCount;
	}

	@Override
	public PollStatus getPollStatus() {
		return PollStatus.up();
	}

	@Override
	public void removeAnticipator(PollAnticipator trigger) {
		m_triggers.remove(trigger);
		
	}

	@Override
	public void resetPollCount() {
		m_pollCount = 0;
		
	}

	@Override
	public Event createDownEvent() {
		return null;
	}

	@Override
	public Event createUpEvent() {
		return null;
	}

	@Override
	public Event createNewEvent() {
		return null;
	}

	@Override
	public Event createDeleteEvent() {
		return null;
	}

}
