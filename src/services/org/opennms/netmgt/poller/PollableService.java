//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2004 The OpenNMS Group, Inc.  All rights reserved.
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
import java.net.UnknownHostException;
import java.util.Date;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.xml.event.Event;

/**
 * Represents a PollableService 
 *
 * @author brozow
 */
public class PollableService extends PollableElement {

    private String m_svcName;
    private PollConfig m_pollConfig;
    private IPv4NetworkInterface m_netInterface;
    private boolean m_unresponsive;
    private boolean m_unresponsiveEventPending;
    private PollStatus m_oldStatus;

    /**
     * @param svcName
     * @param iface
     * 
     */
    public PollableService(PollableInterface iface, String svcName) {
        super(iface);
        m_svcName = svcName;
        m_netInterface = new IPv4NetworkInterface(iface.getAddress());
    }
    
    public PollableInterface getInterface() {
        return (PollableInterface)getParent();
    }
    
    public PollableNode getNode() {
        return getInterface().getNode();
    }

    public PollableNetwork getNetwork() {
        return getInterface().getNetwork();
    }
    
    public PollContext getContext() {
        return getInterface().getContext();
    }
/**
     * @return
     */
    public String getSvcName() {
        return m_svcName;
    }

    /**
     * @return
     */
    public String getIpAddr() {
        return getInterface().getIpAddr();
    }

    /**
     * @return
     */
    public int getNodeId() {
        return getInterface().getNodeId();
    }


    protected void visitThis(PollableVisitor v) {
        super.visitThis(v);
        v.visitService(this);
    }

    /**
     * @param pollConfig
     */
    public void setPollConfig(PollConfig pollConfig) {
        m_pollConfig = pollConfig;
    }

    /**
     * 
     */
    public PollStatus poll() {
        PollStatus newStatus = m_pollConfig.poll(this);
        updateStatus(newStatus);
        return getStatus();
    }

    private void setUnresponsive(boolean unresponsive) {
        m_unresponsive = unresponsive;
    }
    
    private boolean isUnresponsive() {
        return m_unresponsive;
    }
    
    private void setUnresponsiveEventPending(boolean pending) {
        m_unresponsiveEventPending = pending;
    }
    
    private boolean isUnresponsiveEventPending() {
        return m_unresponsiveEventPending;
    }

    /**
     * @return
     * @throws UnknownHostException
     */
    public NetworkInterface getNetInterface() {
        return m_netInterface;
    }

    /**
     * @return
     */
    public InetAddress getAddress() {
        return getInterface().getAddress();
    }

    /**
     * @return
     */
    public PollStatus doPoll() {
        if (getContext().isNodeProcessingEnabled())
            return getParent().doPoll(this);
        else
            return poll();
    }
    
    public Event createDownEvent(Date date) {
        return getContext().createEvent(EventConstants.NODE_LOST_SERVICE_EVENT_UEI, getNodeId(), getAddress(), getSvcName(), date);
    }
    
    
    public Event createUpEvent(Date date) {
        return getContext().createEvent(EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI, getNodeId(), getAddress(), getSvcName(), date);
    }
    
    public Event createUnresponsiveEvent(Date date) {
        return getContext().createEvent(EventConstants.SERVICE_UNRESPONSIVE_EVENT_UEI, getNodeId(), getAddress(), getSvcName(), date);
    }

    public Event createResponsiveEvent(Date date) {
        return getContext().createEvent(EventConstants.SERVICE_RESPONSIVE_EVENT_UEI, getNodeId(), getAddress(), getSvcName(), date);
    }

    public void createOutage(PollEvent cause) {
        super.createOutage(cause);
        getContext().openOutage(this, cause.getEvent());
    }
    protected void resolveOutage(PollEvent resolution) {
        super.resolveOutage(resolution);
        getContext().resolveOutage(this, resolution.getEvent());
    }

    public String toString() { return getInterface()+":"+getSvcName(); }

    public void processStatusChange(Date date) {
        if (getContext().isServiceUnresponsiveEnabled()) {
            if (isStatusChanged() && getStatus() == PollStatus.STATUS_UNRESPONSIVE) {
                getContext().sendEvent(createUnresponsiveEvent(date));
                if (m_oldStatus == PollStatus.STATUS_UP)
                    resetStatusChanged();
            }
            else if (isStatusChanged() && m_oldStatus == PollStatus.STATUS_UNRESPONSIVE) {
                getContext().sendEvent(createResponsiveEvent(date));
                if (getStatus() == PollStatus.STATUS_UP)
                    resetStatusChanged();
            }
        }
        super.processStatusChange(date);
    }
    
    public void updateStatus(PollStatus newStatus) {
        
        if (!getContext().isServiceUnresponsiveEnabled()) {
            if (newStatus == PollStatus.STATUS_UNRESPONSIVE)
                newStatus = PollStatus.STATUS_UP;
        }
        
        if (getStatus() != newStatus)
            m_oldStatus = getStatus();
        
        super.updateStatus(newStatus);
    }
}
