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
package org.opennms.netmgt.poller.mock;

import java.net.InetAddress;
import java.util.Date;

import org.opennms.netmgt.eventd.EventIpcManager;
import org.opennms.netmgt.mock.MockDatabase;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.mock.MockUtil;
import org.opennms.netmgt.poller.pollables.PollContext;
import org.opennms.netmgt.poller.pollables.PollableService;
import org.opennms.netmgt.xml.event.Event;


public class MockPollContext implements PollContext {
    private String m_critSvcName;
    private boolean m_nodeProcessingEnabled;
    private boolean m_pollingAllIfCritServiceUndefined;
    private boolean m_serviceUnresponsiveEnabled;
    private EventIpcManager m_eventMgr;
    private MockDatabase m_db;
    private MockNetwork m_mockNetwork;

    public String getCriticalServiceName() {
        return m_critSvcName;
    }
    
    public void setCriticalServiceName(String svcName) {
        m_critSvcName = svcName;
    }
    
    public boolean isNodeProcessingEnabled() {
        return m_nodeProcessingEnabled;
    }
    public void setNodeProcessingEnabled(boolean nodeProcessingEnabled) {
        m_nodeProcessingEnabled = nodeProcessingEnabled;
    }
    public boolean isPollingAllIfCritServiceUndefined() {
        return m_pollingAllIfCritServiceUndefined;
    }
    public void setPollingAllIfCritServiceUndefined(boolean pollingAllIfCritServiceUndefined) {
        m_pollingAllIfCritServiceUndefined = pollingAllIfCritServiceUndefined;
    }
    
    public void setEventMgr(EventIpcManager eventMgr) {
        m_eventMgr = eventMgr;
    }
    
    public void setDatabase(MockDatabase db) {
        m_db = db;
    }
    
    public void setMockNetwork(MockNetwork network) {
        m_mockNetwork = network;
    }
    public Event sendEvent(Event event) {
        m_eventMgr.sendNow(event);
        return event;
    }

    public Event createEvent(String uei, int nodeId, InetAddress address, String svcName, Date date) {
        return MockUtil.createEvent("Test", uei, nodeId, (address == null ? null : address.getHostAddress()), svcName);
    }
    public void openOutage(PollableService pSvc, Event svcLostEvent) {
        MockService mSvc = m_mockNetwork.getService(pSvc.getNodeId(), pSvc.getIpAddr(), pSvc.getSvcName());
        MockUtil.println("Opening Outage for "+mSvc);
        m_db.createOutage(mSvc, svcLostEvent);

    }
    public void resolveOutage(PollableService pSvc, Event svcRegainEvent) {
        MockService mSvc = m_mockNetwork.getService(pSvc.getNodeId(), pSvc.getIpAddr(), pSvc.getSvcName());
        MockUtil.println("Resolving Outage for "+mSvc);
        m_db.resolveOutage(mSvc, svcRegainEvent);
    }
    public boolean isServiceUnresponsiveEnabled() {
        return m_serviceUnresponsiveEnabled;
    }
    public void setServiceUnresponsiveEnabled(boolean serviceUnresponsiveEnabled) {
        m_serviceUnresponsiveEnabled = serviceUnresponsiveEnabled;
    }
}