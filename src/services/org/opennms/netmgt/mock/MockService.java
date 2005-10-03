package org.opennms.netmgt.mock;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.poller.ServiceMonitor;
import org.opennms.netmgt.xml.event.Event;

/**
 * @author brozow
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class MockService extends MockElement {

    private int m_pollCount;
    
    private Set m_pollingPkgNames = new HashSet();

    private int m_pollStatus;

    private int m_serviceId;

    private String m_svcName;

    private List m_triggers = new ArrayList();

    private Event m_outageEvent;

   public MockService(MockInterface iface, String svcName, int serviceId) {
        super(iface);
        m_svcName = svcName;
        m_serviceId = serviceId;
        m_pollStatus = ServiceMonitor.SERVICE_AVAILABLE;
        m_pollCount = 0;

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
    public String getName() {
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
    
    public Set getPollingPackages() {
        return Collections.unmodifiableSet(m_pollingPkgNames);
    }

    // test
    public int getPollStatus() {
        return m_pollStatus;
    }

    // test
    public int poll(Package pkg) {
        m_pollCount++;
        m_pollingPkgNames.add(pkg.getName());
        
        Iterator it = m_triggers.iterator();
        while (it.hasNext()) {
            PollAnticipator trigger = (PollAnticipator) it.next();
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
    public void setPollStatus(int status) {
        m_pollStatus = status;
    }

    // impl
    public void visit(MockVisitor v) {
        super.visit(v);
        v.visitService(this);
    }
    
    public String toString() {
        return "Svc["+getNodeLabel()+"/"+getIpAddr()+"/"+getName()+"]";
    }

    /**
     * @return
     */
    public Event createDownEvent() {
        return MockUtil.createNodeLostServiceEvent("Test", this, "Service Not Responding.");
    }

    /**
     * @return
     */
    public Event createUpEvent() {
        return MockUtil.createNodeRegainedServiceEvent("Test", this);
    }

    /**
     * @param outageOpened
     */
    public void setOutageEvent(Event outageOpened) {
        m_outageEvent = outageOpened;
    }

    /**
     * @return
     */
    public Event createUnresponsiveEvent() {
        return MockUtil.createServiceUnresponsiveEvent("Test", this, String.valueOf(ServiceMonitor.SERVICE_UNAVAILABLE));
    }

    /**
     * @return
     */
    public Event createResponsiveEvent() {
        return MockUtil.createServiceResponsiveEvent("Test", this);
    }
    
    public Event createDeleteEvent() {
        return MockUtil.createServiceDeletedEvent("Test", this);
    }

}
