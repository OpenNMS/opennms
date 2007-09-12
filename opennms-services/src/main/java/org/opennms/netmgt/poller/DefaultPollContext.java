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
package org.opennms.netmgt.poller;

import java.net.InetAddress;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.eventd.EventListener;
import org.opennms.netmgt.poller.pollables.PendingPollEvent;
import org.opennms.netmgt.poller.pollables.PollContext;
import org.opennms.netmgt.poller.pollables.PollEvent;
import org.opennms.netmgt.poller.pollables.PollableService;
import org.opennms.netmgt.xml.event.Event;

/**
 * Represents a DefaultPollContext 
 *
 * @author brozow
 */
public class DefaultPollContext implements PollContext, EventListener {
    
    private Poller m_poller;
    private boolean m_listenerAdded = false;
    private List m_pendingPollEvents = new LinkedList();

    /**
     * @param poller
     */
    public DefaultPollContext(Poller poller) {
        m_poller = poller;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.pollables.PollContext#getCriticalServiceName()
     */
    public String getCriticalServiceName() {
        return m_poller.getPollerConfig().getCriticalService();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.pollables.PollContext#isNodeProcessingEnabled()
     */
    public boolean isNodeProcessingEnabled() {
        return m_poller.getPollerConfig().nodeOutageProcessingEnabled();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.pollables.PollContext#isPollingAllIfCritServiceUndefined()
     */
    public boolean isPollingAllIfCritServiceUndefined() {
        return m_poller.getPollerConfig().pollAllIfNoCriticalServiceDefined();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.pollables.PollContext#sendEvent(org.opennms.netmgt.xml.event.Event)
     */
    public PollEvent sendEvent(Event event) {
        if (!m_listenerAdded) {
            m_poller.getEventManager().addEventListener(this);
            m_listenerAdded = true;
        }
        PendingPollEvent pollEvent = new PendingPollEvent(event);
        synchronized (m_pendingPollEvents) {
            m_pendingPollEvents.add(pollEvent);
        }
        ThreadCategory.getInstance(getClass()).info("Sending "+event.getUei()+" for element "+event.getNodeid()+":"+event.getInterface()+":"+event.getService());
        m_poller.getEventManager().sendNow(event);
        return pollEvent;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.pollables.PollContext#createEvent(java.lang.String, int, java.net.InetAddress, java.lang.String, java.util.Date)
     */
    public Event createEvent(String uei, int nodeId, InetAddress address, String svcName, Date date, String reason) {
        return m_poller.createEvent(uei, nodeId, address, svcName, date, reason);
    }

    public void openOutage(PollableService svc, final PollEvent svcLostEvent) {
        final int nodeId = svc.getNodeId();
        final String ipAddr = svc.getIpAddr();
        final String svcName = svc.getSvcName();
        Runnable r = new Runnable() {
            public void run() {
                m_poller.getQueryMgr().openOutage(m_poller.getPollerConfig().getNextOutageIdSql(), nodeId, ipAddr, svcName, svcLostEvent.getEventId(), EventConstants.formatToString(svcLostEvent.getDate()));
            }
        };
        if (svcLostEvent instanceof PendingPollEvent) {
            ((PendingPollEvent)svcLostEvent).addPending(r);
        }
        else {
            r.run();
        }
        
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.pollables.PollContext#resolveOutage(org.opennms.netmgt.poller.pollables.PollableService, org.opennms.netmgt.xml.event.Event)
     */
    public void resolveOutage(PollableService svc, final PollEvent svcRegainEvent) {
        final int nodeId = svc.getNodeId();
        final String ipAddr = svc.getIpAddr();
        final String svcName = svc.getSvcName();
        Runnable r = new Runnable() {
            public void run() {
                m_poller.getQueryMgr().resolveOutage(nodeId, ipAddr, svcName, svcRegainEvent.getEventId(), EventConstants.formatToString(svcRegainEvent.getDate()));
            }
        };
        if (svcRegainEvent instanceof PendingPollEvent) {
            ((PendingPollEvent)svcRegainEvent).addPending(r);
        }
        else {
            r.run();
        }
    }
    
    public void reparentOutages(String ipAddr, int oldNodeId, int newNodeId) {
        m_poller.getQueryMgr().reparentOutages(ipAddr, oldNodeId, newNodeId);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.pollables.PollContext#isServiceUnresponsiveEnabled()
     */
    public boolean isServiceUnresponsiveEnabled() {
        return m_poller.getPollerConfig().serviceUnresponsiveEnabled();
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.eventd.EventListener#getName()
     */
    public String getName() {
        return m_poller.getName()+".DefaultPollContext";
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.eventd.EventListener#onEvent(org.opennms.netmgt.xml.event.Event)
     */
    public void onEvent(Event e) {
        synchronized (m_pendingPollEvents) {
            for (Iterator it = m_pendingPollEvents .iterator(); it.hasNext();) {
                PendingPollEvent pollEvent = (PendingPollEvent) it.next();
                if (e.equals(pollEvent.getEvent())) {
                    pollEvent.complete(e);
                }
            }
            
            for (Iterator it = m_pendingPollEvents.iterator(); it.hasNext(); ) {
                PendingPollEvent pollEvent = (PendingPollEvent) it.next();
                if (pollEvent.isPending()) continue;
                
                pollEvent.processPending();
                it.remove();
                
            }
        }
        
    }

}
