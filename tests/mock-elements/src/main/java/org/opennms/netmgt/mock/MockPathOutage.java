/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
