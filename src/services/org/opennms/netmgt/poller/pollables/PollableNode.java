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
package org.opennms.netmgt.poller.pollables;

import java.net.InetAddress;
import java.util.Date;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.xml.event.Event;

/**
 * Represents a PollableNode 
 *
 * @author brozow
 */
public class PollableNode extends PollableContainer {

    private int m_nodeId;

    public PollableNode(PollableNetwork network, int nodeId) {
        super(network);
        m_nodeId = nodeId;
    }

    public int getNodeId() {
        return m_nodeId;
    }
    
    public PollableInterface createInterface(InetAddress addr) {
        PollableInterface iface =  new PollableInterface(this, addr);
        addMember(iface);
        return iface;
    }

    public PollableInterface getInterface(InetAddress addr) {
        return (PollableInterface)getMember(addr);
    }

    public PollableNetwork getNetwork() {
        return (PollableNetwork)getParent();
    }
    
    public PollContext getContext() {
        return getNetwork().getContext();
    }
    
    protected Object createMemberKey(PollableElement member) {
        PollableInterface iface = (PollableInterface)member;
        return iface.getAddress();
    }

    /**
     * @param ipAddr
     * @param svcName
     * @return
     */
    public PollableService createService(InetAddress addr, String svcName) {
        PollableInterface iface = getInterface(addr);
        if (iface == null)
            iface = createInterface(addr);
        return iface.createService(svcName);
    }

    /**
     * @param ipAddr
     * @param svcName
     * @return
     */
    public PollableService getService(InetAddress addr, String svcName) {
        PollableInterface iface = getInterface(addr);
        return (iface == null ? null : iface.getService(svcName));
    }


    protected void visitThis(PollableVisitor v) {
        super.visitThis(v);
        v.visitNode(this);
    }
    
    public Event createDownEvent(Date date) {
        return getContext().createEvent(EventConstants.NODE_DOWN_EVENT_UEI, getNodeId(), null, null, date);
    }
    
    public Event createUpEvent(Date date) {
        return getContext().createEvent(EventConstants.NODE_UP_EVENT_UEI, getNodeId(), null, null, date);
    }
    
    public String toString() { return String.valueOf(getNodeId()); }


}
