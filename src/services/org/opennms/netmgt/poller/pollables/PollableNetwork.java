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

import org.opennms.netmgt.xml.event.Event;




/**
 * Represents a PollableNetwork 
 *
 * @author brozow
 */
public class PollableNetwork extends PollableContainer {
    
    private PollContext m_context;

    public PollableNetwork(PollContext context) {
        super(null);
        m_context = context;
    }
    
    public PollContext getContext() {
        return m_context;
    }

    public PollableNode createNode(int nodeId) {
        PollableNode node = new PollableNode(this, nodeId);
        addMember(node);
        return node;
    }
    
    public PollableNode getNode(int nodeId) {
        return (PollableNode)getMember(new Integer(nodeId));
    }

    public int getNodeCount() {
        return getMemberCount();
    }
    
    public PollableInterface createInterface(int nodeId, InetAddress addr) {
        PollableNode node = getNode(nodeId);
        if (node == null)
            node = createNode(nodeId);
        return node.createInterface(addr);
    }

    public PollableInterface getInterface(int nodeId, InetAddress addr) {
        PollableNode node = getNode(nodeId);
        return (node == null ? null : node.getInterface(addr));
    }

    public PollableService createService(int nodeId, InetAddress addr, String svcName) {
        PollableNode node = getNode(nodeId);
        if (node == null)
            node = createNode(nodeId);
        return node.createService(addr, svcName);
        
    }

    public PollableService getService(int nodeId, InetAddress addr, String svcName) {
        PollableNode node = getNode(nodeId);
        return (node == null ? null : node.getService(addr, svcName));
    }

    protected Object createMemberKey(PollableElement member) {
        PollableNode node = (PollableNode)member;
        return new Integer(node.getNodeId());
    }
    
    protected void visitThis(PollableVisitor v) {
        super.visitThis(v);
        v.visitNetwork(this);
    }


    public PollStatus pollRemainingMembers(PollableElement member) {
        return getMemberStatus();
    }

    public Event createDownEvent(Date date) {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    public Event createUpEvent(Date date) {
        // TODO Auto-generated method stub
        return null;
    }

}
