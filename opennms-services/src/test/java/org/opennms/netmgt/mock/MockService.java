// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2008 Feb 09: Java 5 generics. - dj@opennms.org
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

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.IPv4NetworkInterface;
import org.opennms.netmgt.poller.MonitoredService;
import org.opennms.netmgt.poller.NetworkInterface;
import org.opennms.netmgt.xml.event.Event;

/**
 * 
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class MockService extends MockElement implements MonitoredService {
    
    public static enum SvcMgmtStatus {
        ACTIVE("A"),
        DELETED("D"),
        UNMANAGED("U"),
        FORCE_UNMANAGED("F"),
        NOT_POLLED("N"),
        REMOTE_ONLY("X");
        
        private final String m_dbString;
        
        SvcMgmtStatus(String dbString) {
            m_dbString = dbString;
        }
        
        public String toDbString() {
            return m_dbString;
        }
        
        public static SvcMgmtStatus fromDbString(String dbString) {
            if (dbString == null) {
                return null;
            }
            for (SvcMgmtStatus status : SvcMgmtStatus.values()) {
                if (status.toDbString().equals(dbString)) {
                    return status;
                }
            }
            throw new IllegalArgumentException(dbString + "is not an legal SvcMgmtStatus string");
        }
     }

    private int m_pollCount;
    
    private PollStatus m_pollStatus;

    private int m_serviceId;

    private String m_svcName;
    
    private SvcMgmtStatus m_mgmtStatus = SvcMgmtStatus.ACTIVE;

    private List<PollAnticipator> m_triggers = new ArrayList<PollAnticipator>();

    private NetworkInterface m_netAddr;

   public MockService(MockInterface iface, String svcName, int serviceId) {
        super(iface);
        m_svcName = svcName;
        m_serviceId = serviceId;
        m_pollStatus = PollStatus.up();
        m_pollCount = 0;

    }
   
   // test
   public void bringDown(String reason) {
       setServicePollStatus(PollStatus.down(reason));
   }


   // FIXME: model? make generic poll listener
    public void addAnticipator(PollAnticipator trigger) {
        m_triggers.add(trigger);
    }

    // model
    public int getId() {
        return m_serviceId;
    }

    // model
    public MockInterface getInterface() {
        return (MockInterface) getParent();
    }

    // model
    public String getIpAddr() {
        return getInterface().getIpAddr();
    }

    // impl
    Object getKey() {
        return m_svcName;
    }

    // model
    public String getSvcName() {
        return m_svcName;
    }

    // model
    public MockNetwork getNetwork() {
        return getInterface().getNetwork();
    }

    // model
    public MockNode getNode() {
        return getInterface().getNode();
    }

    // model
    public int getNodeId() {
        return getNode().getNodeId();
    }

    // model
    public String getNodeLabel() {
        return getNode().getLabel();
    }

    // stats
    public int getPollCount() {
        return m_pollCount;
    }
    
    // test
    public PollStatus getPollStatus() {
        return m_pollStatus;
    }
    
    public SvcMgmtStatus getMgmtStatus() {
        return m_mgmtStatus;
    }
    
    public void setMgmtStatus(SvcMgmtStatus mgmtStatus) {
        m_mgmtStatus = mgmtStatus;
    }

    // test
    public PollStatus poll() {
        m_pollCount++;
        
        for (PollAnticipator trigger : m_triggers) {
            trigger.poll(this);
        }

        return getPollStatus();

    }

    // FIXME: model? make generic poll listener
    public void removeAnticipator(PollAnticipator trigger) {
        m_triggers.remove(trigger);
    }

    // stats
    public void resetPollCount() {
        m_pollCount = 0;
    }

    //  test
    public void setPollStatus(PollStatus status) {
        m_pollStatus = status;
    }

    // impl
    public void visit(MockVisitor v) {
        super.visit(v);
        v.visitService(this);
    }
    
    public String toString() {
        return "Svc["+getNodeLabel()+"/"+getIpAddr()+"/"+getSvcName()+"]";
    }

    /**
     * @return
     */
    public Event createDownEvent() {
        return MockEventUtil.createNodeLostServiceEvent("Test", this, "Service Not Responding.");
    }

    /**
     * @return
     */
    public Event createUpEvent() {
        return MockEventUtil.createNodeRegainedServiceEvent("Test", this);
    }

    /**
     * @return
     */
    public Event createUnresponsiveEvent() {
        return MockEventUtil.createServiceUnresponsiveEvent("Test", this, String.valueOf(PollStatus.SERVICE_UNAVAILABLE));
    }

    /**
     * @return
     */
    public Event createResponsiveEvent() {
        return MockEventUtil.createServiceResponsiveEvent("Test", this);
    }
    
    public Event createNewEvent() {
        return MockEventUtil.createNodeGainedServiceEvent("Test", this);
    }

    public Event createDeleteEvent() {
        return MockEventUtil.createServiceDeletedEvent("Test", this);
    }

    public NetworkInterface getNetInterface() {
        if (m_netAddr == null)
            m_netAddr = new IPv4NetworkInterface(getAddress());
        
        return m_netAddr;
    }

    public InetAddress getAddress() {
        return getInterface().getAddress();    
    }

	public Event createDemandPollEvent(int demandPollId) {
		return MockEventUtil.createDemandPollServiceEvent("Test", this, demandPollId);
	}

}
