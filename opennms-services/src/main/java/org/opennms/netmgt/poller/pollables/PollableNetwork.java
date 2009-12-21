/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2004-2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created December 31, 2004
 *
 * Copyright (C) 2004-2006 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.poller.pollables;

import java.net.InetAddress;
import java.util.Date;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.xml.event.Event;




/**
 * Represents a PollableNetwork 
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class PollableNetwork extends PollableContainer {
    
    private final PollContext m_context;

    public PollableNetwork(PollContext context) {
        super(null, Scope.NETWORK);
        m_context = context;
    }
    
    public PollContext getContext() {
        return m_context;
    }

    public PollableNode createNode(int nodeId, String nodeLabel) {
        PollableNode node = new PollableNode(this, nodeId, nodeLabel);
        addMember(node);
        return node;
    }
    
    public PollableNode createNodeIfNecessary(int nodeId, String nodeLabel) {
        synchronized (this) {
            PollableNode node = getNode(nodeId);
            return (node != null ? node : createNode(nodeId, nodeLabel));
        }

    }
    
    public PollableNode getNode(int nodeId) {
        return (PollableNode)getMember(new Integer(nodeId));
    }

    public int getNodeCount() {
        return getMemberCount();
    }
    
    public PollableInterface createInterface(int nodeId, String nodeLabel, InetAddress addr) {
        return createNodeIfNecessary(nodeId, nodeLabel).createInterface(addr);
    }

    public PollableInterface getInterface(int nodeId, InetAddress addr) {
        PollableNode node = getNode(nodeId);
        return (node == null ? null : node.getInterface(addr));
    }

    public PollableService createService(int nodeId, String nodeLabel, InetAddress addr, String svcName) {
        return createNodeIfNecessary(nodeId, nodeLabel).createService(addr, svcName);
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
        throw new UnsupportedOperationException("No down event for the network");
    }
    
    
    public Event createUpEvent(Date date) {
        throw new UnsupportedOperationException("No up event for the network");
    }
    
    class DumpVisitor extends PollableVisitorAdaptor {
        
        private Category m_log;

        public DumpVisitor(Category log) {
            m_log = log;
        }
        public void visitNode(PollableNode pNode) {
            m_log.debug(" nodeid=" + pNode.getNodeId() + " status=" + getStatusString(pNode));
        }

        public void visitInterface(PollableInterface pIf) {;
            m_log.debug("     interface=" + pIf.getIpAddr() + " status=" + getStatusString(pIf));
        }

        public void visitService(PollableService pSvc) {
            m_log.debug("         service=" + pSvc.getSvcName() + " status=" + getStatusString(pSvc));
        }
        
        private String getStatusString(PollableElement e) {
            PollStatus status = e.getStatus();
            boolean up = status.isUp();
            String statusDesc = status.toString();
            PollEvent cause = e.getCause();
            int eventId = cause == null ? 0 : cause.getEventId();
            return (up ? statusDesc : statusDesc+"("+eventId+")");
        }
    }


    
    public void dump() {
        final Category log = ThreadCategory.getInstance(getClass());

        DumpVisitor dumper = new DumpVisitor(log);
        visit(dumper);
        
    }

    public void delete() {
        throw new UnsupportedOperationException("Can't delete the entire network");
    }
    public PollStatus poll(PollableElement elem) {
        PollableElement member = findMemberWithDescendent(elem);
        return member.poll(elem);
    }

    public void processStatusChange(Date date) {
        // no need to process status changes for the network itself
        processMemberStatusChanges(date);
    }
    public void recalculateStatus() {
        Iter iter = new Iter() {
            public void forEachElement(PollableElement elem) {
                elem.recalculateStatus();
            }
        };
        forEachMember(iter);
    }
    public void resetStatusChanged() {
        super.resetStatusChanged();
        Iter iter = new Iter() {
            public void forEachElement(PollableElement elem) {
                elem.resetStatusChanged();
            }
        };
        forEachMember(iter);
    }
    public PollableElement getLockRoot() {
        return this;
    }
    
    public void obtainTreeLock(long timeout) {
    }
    public void releaseTreeLock() {
    }

    @Override
    public PollEvent extrapolateCause() {

        Iter iter = new Iter() {
            public void forEachElement(PollableElement elem) {
                elem.extrapolateCause();
            }
        };
        forEachMember(iter);
        return null;

    }
    
    public void propagateInitialCause() {
        extrapolateCause();
        inheritParentalCause();
    }
    
    
}
