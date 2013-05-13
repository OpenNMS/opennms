/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.mock;

import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.opennms.core.test.db.MockDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.mock.MockEventUtil;
import org.opennms.netmgt.mock.MockNetwork;
import org.opennms.netmgt.mock.MockService;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventIpcManager;
import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.poller.pollables.PendingPollEvent;
import org.opennms.netmgt.poller.pollables.PollContext;
import org.opennms.netmgt.poller.pollables.PollEvent;
import org.opennms.netmgt.poller.pollables.PollableService;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.test.mock.MockUtil;


public class MockPollContext implements PollContext, EventListener {
    private String m_critSvcName;
    private boolean m_nodeProcessingEnabled;
    private boolean m_pollingAllIfCritServiceUndefined;
    private boolean m_serviceUnresponsiveEnabled;
    private EventIpcManager m_eventMgr;
    private MockDatabase m_db;
    private MockNetwork m_mockNetwork;
    private List<PendingPollEvent> m_pendingPollEvents = new LinkedList<PendingPollEvent>();
    

    @Override
    public String getCriticalServiceName() {
        return m_critSvcName;
    }
    
    public void setCriticalServiceName(String svcName) {
        m_critSvcName = svcName;
    }
    
    @Override
    public boolean isNodeProcessingEnabled() {
        return m_nodeProcessingEnabled;
    }
    public void setNodeProcessingEnabled(boolean nodeProcessingEnabled) {
        m_nodeProcessingEnabled = nodeProcessingEnabled;
    }
    @Override
    public boolean isPollingAllIfCritServiceUndefined() {
        return m_pollingAllIfCritServiceUndefined;
    }
    public void setPollingAllIfCritServiceUndefined(boolean pollingAllIfCritServiceUndefined) {
        m_pollingAllIfCritServiceUndefined = pollingAllIfCritServiceUndefined;
    }
    
    public void setEventMgr(EventIpcManager eventMgr) {
        if (m_eventMgr != null) {
            m_eventMgr.removeEventListener(this);
        }
        m_eventMgr = eventMgr;
        if (m_eventMgr != null) {
            m_eventMgr.addEventListener(this);
        }
    }
    
    public void setDatabase(MockDatabase db) {
        m_db = db;
    }
    
    public void setMockNetwork(MockNetwork network) {
        m_mockNetwork = network;
    }
    @Override
    public PollEvent sendEvent(Event event) {
        PendingPollEvent pollEvent = new PendingPollEvent(event);
        synchronized (this) {
            m_pendingPollEvents.add(pollEvent);
        }
        m_eventMgr.sendNow(event);
        return pollEvent;
    }
    
    

    @Override
    public Event createEvent(String uei, int nodeId, InetAddress address, String svcName, Date date, String reason) {
        EventBuilder e = MockEventUtil.createEventBuilder("Test", uei, nodeId, (address == null ? null : InetAddressUtils.str(address)), svcName, reason);
        e.setCreationTime(date);
        e.setTime(date);
        return e.getEvent();
    }
    @Override
    public void openOutage(final PollableService pSvc, final PollEvent svcLostEvent) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                writeOutage(pSvc, svcLostEvent);
            }
        };
        if (svcLostEvent instanceof PendingPollEvent)
            ((PendingPollEvent)svcLostEvent).addPending(r);
        else  
            r.run();
    }
    
    private void writeOutage(PollableService pSvc, PollEvent svcLostEvent) {
        MockService mSvc = m_mockNetwork.getService(pSvc.getNodeId(), pSvc.getIpAddr(), pSvc.getSvcName());
        Timestamp eventTime = m_db.convertEventTimeToTimeStamp(EventConstants.formatToString(svcLostEvent.getDate()));
        MockUtil.println("Opening Outage for "+mSvc);
        m_db.createOutage(mSvc, svcLostEvent.getEventId(), eventTime);

    }
    @Override
    public void resolveOutage(final PollableService pSvc, final PollEvent svcRegainEvent) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                closeOutage(pSvc, svcRegainEvent);
            }
        };
        if (svcRegainEvent instanceof PendingPollEvent)
            ((PendingPollEvent)svcRegainEvent).addPending(r);
        else  
            r.run();
    }
    
    public void closeOutage(PollableService pSvc, PollEvent svcRegainEvent) {
        MockService mSvc = m_mockNetwork.getService(pSvc.getNodeId(), pSvc.getIpAddr(), pSvc.getSvcName());
        Timestamp eventTime = m_db.convertEventTimeToTimeStamp(EventConstants.formatToString(svcRegainEvent.getDate()));
        MockUtil.println("Resolving Outage for "+mSvc);
        m_db.resolveOutage(mSvc, svcRegainEvent.getEventId(), eventTime);
    }
    
    @Override
    public void reparentOutages(String ipAddr, int oldNodeId, int newNodeId) {
        m_db.update("update outages set nodeId = ? where nodeId = ? and ipaddr = ?", newNodeId, oldNodeId, ipAddr);
    }
    
    @Override
    public boolean isServiceUnresponsiveEnabled() {
        return m_serviceUnresponsiveEnabled;
    }
    public void setServiceUnresponsiveEnabled(boolean serviceUnresponsiveEnabled) {
        m_serviceUnresponsiveEnabled = serviceUnresponsiveEnabled;
    }

    @Override
    public String getName() {
        return "MockPollContext";
    }

    @Override
    public synchronized void onEvent(Event e) {
        synchronized (m_pendingPollEvents) {
            for (PendingPollEvent pollEvent : m_pendingPollEvents) {
                if (e.equals(pollEvent.getEvent())) {
                    pollEvent.complete(e);
                }
            }
            
            for (Iterator<PendingPollEvent> it = m_pendingPollEvents.iterator(); it.hasNext(); ) {
                PendingPollEvent pollEvent = it.next();
                if (pollEvent.isPending()) {
                    break;
                }
                
                pollEvent.processPending();
                it.remove();
            }
        }
    }
}