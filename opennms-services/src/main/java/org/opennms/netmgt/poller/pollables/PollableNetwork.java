/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2004-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletionStage;

import org.opennms.core.utils.AsyncReentrantLock;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a PollableNetwork
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class PollableNetwork extends PollableContainer {
    private static final Logger LOG = LoggerFactory.getLogger(PollableNetwork.class);

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
     * @param nodeLocation a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableNode} object.
     */
    public PollableNode createNode(int nodeId, String nodeLabel, String nodeLocation) {
        PollableNode node = new PollableNode(this, nodeId, nodeLabel, nodeLocation);
        addMember(node);
        return node;
    }

    /**
     * <p>createNodeIfNecessary</p>
     *
     * @param nodeId a int.
     * @param nodeLabel a {@link java.lang.String} object.
     * @param nodeLocation a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableNode} object.
     */
    public PollableNode createNodeIfNecessary(int nodeId, String nodeLabel, String nodeLocation) {
        synchronized (this) {
            PollableNode node = getNode(nodeId);
            return (node != null ? node : createNode(nodeId, nodeLabel, nodeLocation));
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

    public List<Long> getNodeIds() {
        List<Long> nodeIds = new ArrayList<>();
        for (PollableElement e : getMembers()) {
            int nodeId = ((PollableNode)e).getNodeId();
            nodeIds.add(new Long(nodeId));
        }
        return nodeIds;
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
     * @param nodeLocation a {@link java.lang.String} object.
     * @param addr a {@link java.net.InetAddress} object.
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableInterface} object.
     */
    public PollableInterface createInterface(AsyncReentrantLock.Locker locker, int nodeId, String nodeLabel, String nodeLocation, InetAddress addr) {
        return createNodeIfNecessary(nodeId, nodeLabel, nodeLocation).createInterface(locker, addr);
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
     * @param nodeLocation a {@link java.lang.String} object.
     * @param addr a {@link java.net.InetAddress} object.
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableService} object.
     */
    public PollableService createService(AsyncReentrantLock.Locker locker, int nodeId, String nodeLabel, String nodeLocation, InetAddress addr, String svcName) {
        return createNodeIfNecessary(nodeId, nodeLabel, nodeLocation).createService(locker, addr, svcName);
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
    public CompletionStage<PollStatus> pollRemainingMembers(AsyncReentrantLock.Locker locker, PollableElement member) {
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
    public void delete(AsyncReentrantLock.Locker locker) {
        LOG.debug("Can't delete the entire network.");
    }

    /** {@inheritDoc} */
    @Override
    public CompletionStage<PollStatus> poll(AsyncReentrantLock.Locker locker, PollableElement elem) {
        PollableElement member = findMemberWithDescendent(elem);
        return member.poll(locker, elem);
    }

    /** {@inheritDoc} */
    @Override
    public void processStatusChange(AsyncReentrantLock.Locker locker, Date date) {
        // no need to process status changes for the network itself
        processMemberStatusChanges(locker, date);
    }
    /**
     * <p>recalculateStatus</p>
     */
    @Override
    public void recalculateStatus(AsyncReentrantLock.Locker locker) {
        Iter iter = new Iter() {
            @Override
            public void forEachElement(PollableElement elem) {
                elem.recalculateStatus(locker);
            }
        };
        forEachMember(iter);
    }
    /**
     * <p>resetStatusChanged</p>
     */
    @Override
    public void resetStatusChanged(AsyncReentrantLock.Locker locker) {
        super.resetStatusChanged(locker);
        Iter iter = new Iter() {
            @Override
            public void forEachElement(PollableElement elem) {
                elem.resetStatusChanged(locker);
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
    protected PollableElement getLockRoot() {
        return this;
    }
    
    /** {@inheritDoc} */
    @Override
    protected void obtainTreeLock(AsyncReentrantLock.Locker locker) {
    }

    /** {@inheritDoc} */
    @Override
    protected void obtainTreeLock(AsyncReentrantLock.Locker locker, long timeout) {
    }

    /**
     * <p>releaseTreeLock</p>
     */
    @Override
    protected void releaseTreeLock(AsyncReentrantLock.Locker locker) {
    }

    /** {@inheritDoc} */
    @Override
    public PollEvent extrapolateCause(AsyncReentrantLock.Locker locker) {

        Iter iter = new Iter() {
            @Override
            public void forEachElement(PollableElement elem) {
                elem.extrapolateCause(locker);
            }
        };
        forEachMember(iter);
        return null;

    }
    
    /**
     * <p>propagateInitialCause</p>
     */
    public void propagateInitialCause(AsyncReentrantLock.Locker locker) {
        extrapolateCause(locker);
        inheritParentalCause(locker);
    }
    
    
}
