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

package org.opennms.netmgt.poller.pollables;

import java.net.InetAddress;
import java.util.Date;

import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;




/**
 * Represents a PollableNetwork
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class PollableNetwork extends PollableContainer {
    
    private final PollContext m_context;

    /**
     * <p>Constructor for PollableNetwork.</p>
     *
     * @param context a {@link org.opennms.netmgt.poller.pollables.PollContext} object.
     */
    public PollableNetwork(PollContext context) {
        super(null, Scope.NETWORK);
        m_context = context;
    }
    
    /**
     * <p>getContext</p>
     *
     * @return a {@link org.opennms.netmgt.poller.pollables.PollContext} object.
     */
    @Override
    public PollContext getContext() {
        return m_context;
    }

    /**
     * <p>createNode</p>
     *
     * @param nodeId a int.
     * @param nodeLabel a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableNode} object.
     */
    public PollableNode createNode(int nodeId, String nodeLabel) {
        PollableNode node = new PollableNode(this, nodeId, nodeLabel);
        addMember(node);
        return node;
    }
    
    /**
     * <p>createNodeIfNecessary</p>
     *
     * @param nodeId a int.
     * @param nodeLabel a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableNode} object.
     */
    public PollableNode createNodeIfNecessary(int nodeId, String nodeLabel) {
        synchronized (this) {
            PollableNode node = getNode(nodeId);
            return (node != null ? node : createNode(nodeId, nodeLabel));
        }

    }
    
    /**
     * <p>getNode</p>
     *
     * @param nodeId a int.
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableNode} object.
     */
    public PollableNode getNode(int nodeId) {
        return (PollableNode)getMember(Integer.valueOf(nodeId));
    }

    /**
     * <p>getNodeCount</p>
     *
     * @return a int.
     */
    public int getNodeCount() {
        return getMemberCount();
    }
    
    /**
     * <p>createInterface</p>
     *
     * @param nodeId a int.
     * @param nodeLabel a {@link java.lang.String} object.
     * @param addr a {@link java.net.InetAddress} object.
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableInterface} object.
     */
    public PollableInterface createInterface(int nodeId, String nodeLabel, InetAddress addr) {
        return createNodeIfNecessary(nodeId, nodeLabel).createInterface(addr);
    }

    /**
     * <p>getInterface</p>
     *
     * @param nodeId a int.
     * @param addr a {@link java.net.InetAddress} object.
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableInterface} object.
     */
    public PollableInterface getInterface(int nodeId, InetAddress addr) {
        PollableNode node = getNode(nodeId);
        return (node == null ? null : node.getInterface(addr));
    }

    /**
     * <p>createService</p>
     *
     * @param nodeId a int.
     * @param nodeLabel a {@link java.lang.String} object.
     * @param addr a {@link java.net.InetAddress} object.
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableService} object.
     */
    public PollableService createService(int nodeId, String nodeLabel, InetAddress addr, String svcName) {
        return createNodeIfNecessary(nodeId, nodeLabel).createService(addr, svcName);
    }

    /**
     * <p>getService</p>
     *
     * @param nodeId a int.
     * @param addr a {@link java.net.InetAddress} object.
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableService} object.
     */
    public PollableService getService(int nodeId, InetAddress addr, String svcName) {
        PollableNode node = getNode(nodeId);
        return (node == null ? null : node.getService(addr, svcName));
    }

    /** {@inheritDoc} */
    @Override
    protected Object createMemberKey(PollableElement member) {
        PollableNode node = (PollableNode)member;
        return Integer.valueOf(node.getNodeId());
    }
    
    /** {@inheritDoc} */
    @Override
    protected void visitThis(PollableVisitor v) {
        super.visitThis(v);
        v.visitNetwork(this);
    }


    /** {@inheritDoc} */
    @Override
    public PollStatus pollRemainingMembers(PollableElement member) {
        return getMemberStatus();
    }

    /** {@inheritDoc} */
    @Override
    public Event createDownEvent(Date date) {
        throw new UnsupportedOperationException("No down event for the network");
    }
    
    
    /** {@inheritDoc} */
    @Override
    public Event createUpEvent(Date date) {
        throw new UnsupportedOperationException("No up event for the network");
    }
    
    static class DumpVisitor extends PollableVisitorAdaptor {
        
        private static final Logger LOG = LoggerFactory.getLogger(PollableNetwork.DumpVisitor.class);
        
        @Override
        public void visitNode(PollableNode pNode) {
            LOG.debug(" nodeid={} status={}", pNode.getNodeId(), getStatusString(pNode));
        }

        @Override
        public void visitInterface(PollableInterface pIf) {;
            LOG.debug("     interface={} status={}", pIf.getIpAddr(), getStatusString(pIf));
        }

        @Override
        public void visitService(PollableService pSvc) {
            LOG.debug("         service={} status={}", pSvc.getSvcName(), getStatusString(pSvc));
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


    
    /**
     * <p>dump</p>
     */
    public void dump() {

        DumpVisitor dumper = new DumpVisitor();
        visit(dumper);
        
    }

    /**
     * <p>delete</p>
     */
    @Override
    public void delete() {
        throw new UnsupportedOperationException("Can't delete the entire network");
    }
    /** {@inheritDoc} */
    @Override
    public PollStatus poll(PollableElement elem) {
        PollableElement member = findMemberWithDescendent(elem);
        return member.poll(elem);
    }

    /** {@inheritDoc} */
    @Override
    public void processStatusChange(Date date) {
        // no need to process status changes for the network itself
        processMemberStatusChanges(date);
    }
    /**
     * <p>recalculateStatus</p>
     */
    @Override
    public void recalculateStatus() {
        Iter iter = new Iter() {
            @Override
            public void forEachElement(PollableElement elem) {
                elem.recalculateStatus();
            }
        };
        forEachMember(iter);
    }
    /**
     * <p>resetStatusChanged</p>
     */
    @Override
    public void resetStatusChanged() {
        super.resetStatusChanged();
        Iter iter = new Iter() {
            @Override
            public void forEachElement(PollableElement elem) {
                elem.resetStatusChanged();
            }
        };
        forEachMember(iter);
    }
    /**
     * <p>getLockRoot</p>
     *
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableElement} object.
     */
    @Override
    public PollableElement getLockRoot() {
        return this;
    }
    
    /** {@inheritDoc} */
    @Override
    public void obtainTreeLock(long timeout) {
    }
    /**
     * <p>releaseTreeLock</p>
     */
    @Override
    public void releaseTreeLock() {
    }

    /** {@inheritDoc} */
    @Override
    public PollEvent extrapolateCause() {

        Iter iter = new Iter() {
            @Override
            public void forEachElement(PollableElement elem) {
                elem.extrapolateCause();
            }
        };
        forEachMember(iter);
        return null;

    }
    
    /**
     * <p>propagateInitialCause</p>
     */
    public void propagateInitialCause() {
        extrapolateCause();
        inheritParentalCause();
    }
    
    
}
