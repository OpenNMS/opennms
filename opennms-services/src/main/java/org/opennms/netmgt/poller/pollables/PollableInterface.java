/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2004-2007 The OpenNMS Group, Inc.  All rights reserved.
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
 * Copyright (C) 2004-2007 The OpenNMS Group, Inc.  All rights reserved.
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

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.xml.event.Event;

/**
 * Represents a PollableInterface 
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class PollableInterface extends PollableContainer {

    private InetAddress m_addr;

    public PollableInterface(PollableNode node, InetAddress addr) {
        super(node);
        m_addr = addr;
    }

    public PollableNode getNode() {
        return (PollableNode)getParent();
    }
    
    private void setNode(PollableNode newNode) {
        setParent(newNode);
    }

    public PollableNetwork getNetwork() {
        return getNode().getNetwork();
    }
    
    public PollContext getContext() {
        return getNode().getContext();
    }

    public String getIpAddr() {
        return m_addr.getHostAddress();
    }
    
    public InetAddress getAddress() {
        return m_addr;
    }

    public int getNodeId() {
        return getNode().getNodeId();
    }

    public String getNodeLabel() {
        return getNode().getNodeLabel();
    }

    public PollableService createService(final String svcName) {
        
        final PollableService[] retVal = new PollableService[1];
        Runnable r = new Runnable() {
            public void run() {
                PollableService svc = new PollableService(PollableInterface.this, svcName);
                addMember(svc);
                retVal[0] = svc;
            }
        };
        withTreeLock(r);
        return retVal[0];
        
    }

    public PollableService getService(String svcName) {
        return (PollableService)getMember(svcName);
    }

    protected Object createMemberKey(PollableElement member) {
        PollableService svc = (PollableService)member;
        return svc.getSvcName();
    }
    
    protected void visitThis(PollableVisitor v) {
        super.visitThis(v);
        v.visitInterface(this);
    }
    
    public void recalculateStatus() {
        PollableService criticalSvc = getCriticalService();
        if (criticalSvc != null) {
            criticalSvc.recalculateStatus();
            updateStatus(criticalSvc.getStatus().isUp() ? PollStatus.up() : PollStatus.down());
        } else {
            super.recalculateStatus();
        }
    }

    /**
     * @return
     */
    private PollableService getCriticalService() {
        return getService(getContext().getCriticalServiceName());
    }


    public PollableElement selectPollElement() {
        PollableService critSvc = getCriticalService();
        return (critSvc != null ? critSvc : super.selectPollElement());
    }
    
    protected PollStatus poll(PollableElement elem) {
        PollableService critSvc = getCriticalService();
        if (getStatus().isUp() || critSvc == null || elem == critSvc)
            return super.poll(elem);
    
        return PollStatus.down();
    }
    
    public PollStatus pollRemainingMembers(PollableElement member) {
        PollableService critSvc = getCriticalService();
        
        
        if (critSvc != null && getStatus().isUp()) {
            if (member != critSvc)
                critSvc.poll();

            return critSvc.getStatus().isUp() ? PollStatus.up() : PollStatus.down();
        }
        
        if (getContext().isPollingAllIfCritServiceUndefined())
            return super.pollRemainingMembers(member);
        else {
            return getMemberStatus();
        }
            
    }
    public Event createDownEvent(Date date) {
        return getContext().createEvent(EventConstants.INTERFACE_DOWN_EVENT_UEI, getNodeId(), getAddress(), null, date, getStatus().getReason());
    }
    
    
    public Event createUpEvent(Date date) {
        return getContext().createEvent(EventConstants.INTERFACE_UP_EVENT_UEI, getNodeId(), getAddress(), null, date, getStatus().getReason());
    }
    
    public String toString() { return getNode()+":"+getIpAddr(); }

    public void reparentTo(final PollableNode newNode) {
        final PollableNode oldNode = getNode();
        
        if (oldNode.equals(newNode)) return;
        
        // always lock the nodes in nodeId order so deadlock is not possible
        final PollableNode firstNode = (oldNode.getNodeId() <= newNode.getNodeId() ? oldNode : newNode);
        final PollableNode secondNode = (oldNode.getNodeId() <= newNode.getNodeId() ? newNode : oldNode);
        
        final Runnable reparent = new Runnable() {
            public void run() {
                oldNode.resetStatusChanged();
                newNode.resetStatusChanged();
              
                int oldNodeId = getNodeId();
                String oldIp = getIpAddr();
                int newNodeId = newNode.getNodeId();
                
                oldNode.removeMember(PollableInterface.this);
                newNode.addMember(PollableInterface.this);
                setNode(newNode);

                getContext().reparentOutages(oldIp, oldNodeId, newNodeId);
                
                if (getCause() == null || getCause().equals(oldNode.getCause())) {
                    // the current interface outage is a node outage or no outage at all
                    if (newNode.getCause() != null) {
                        // if the new Node has a node outage then we recursively set the 
                        // causes so when process events we properly handle the causes
                        PollableVisitor visitor = new PollableVisitorAdaptor() {
                            public void visitElement(PollableElement element) {
                                boolean matches = (element.getCause() == null ? oldNode.getCause() == null : element.getCause().equals(oldNode.getCause()));
                                if (matches) {
                                    element.setCause(newNode.getCause());
                                }
                            }
                        };
                        visit(visitor);
                    } 
                }
                
                
                // process the status changes related to the 
                Date date = new Date();
                oldNode.recalculateStatus();
                oldNode.processStatusChange(date);
                newNode.recalculateStatus();
                newNode.processStatusChange(date);

            }
        };
        
        Runnable lockSecondNodeAndRun = new Runnable() {
            public void run() {
                secondNode.withTreeLock(reparent);
            }
        };
        
        firstNode.withTreeLock(lockSecondNodeAndRun);
        
    }



}
