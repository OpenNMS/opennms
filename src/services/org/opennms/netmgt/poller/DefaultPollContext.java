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

import org.opennms.netmgt.poller.pollables.PollContext;
import org.opennms.netmgt.poller.pollables.PollableService;
import org.opennms.netmgt.xml.event.Event;

/**
 * Represents a DefaultPollContext 
 *
 * @author brozow
 */
public class DefaultPollContext implements PollContext {
    
    Poller m_poller;

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
    public Event sendEvent(Event event) {
        m_poller.getEventManager().sendNow(event);
        return event;
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.pollables.PollContext#createEvent(java.lang.String, int, java.net.InetAddress, java.lang.String, java.util.Date)
     */
    public Event createEvent(String uei, int nodeId, InetAddress address, String svcName, Date date) {
        return m_poller.createEvent(uei, nodeId, address, svcName, date);
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.pollables.PollContext#openOutage(org.opennms.netmgt.poller.pollables.PollableService, org.opennms.netmgt.xml.event.Event)
     */
    public void openOutage(PollableService svc, Event svcLostEvent) {
        
        int serviceId = m_poller.getServiceIdByName(svc.getSvcName());
        m_poller.getQueryMgr().openOutage(m_poller.getPollerConfig().getNextOutageIdSql(), svc.getNodeId(), svc.getIpAddr(), serviceId, svcLostEvent.getDbid(), svcLostEvent.getTime());
        

    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.pollables.PollContext#resolveOutage(org.opennms.netmgt.poller.pollables.PollableService, org.opennms.netmgt.xml.event.Event)
     */
    public void resolveOutage(PollableService svc, Event svcRegainEvent) {
        int serviceId = m_poller.getServiceIdByName(svc.getSvcName());
        m_poller.getQueryMgr().resolveOutage(svc.getNodeId(), svc.getIpAddr(), serviceId, svcRegainEvent.getDbid(), svcRegainEvent.getTime());
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.poller.pollables.PollContext#isServiceUnresponsiveEnabled()
     */
    public boolean isServiceUnresponsiveEnabled() {
        return m_poller.getPollerConfig().serviceUnresponsiveEnabled();
    }

}
