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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.xml.event.Event;

/**
 * Represents a PollableNode
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class PollableNode extends PollableContainer {
    private static final Logger LOG = LoggerFactory.getLogger(PollableNode.class);

    /**
     * Represents a Lock 
     *
     * @author brozow
     */
    public class Lock {
        private Thread m_owner = null;
        private int m_obtainCount = 0;
        
        public synchronized void obtain() {
            
            if (m_owner != Thread.currentThread()) {
                LOG.debug("Trying to obtain lock for {}", PollableNode.this);
                while (m_owner != null) {
                    try { wait();} catch (InterruptedException e) { throw new ThreadInterrupted("Lock for "+PollableNode.this+" is unavailable", e);}
                }
                m_owner = Thread.currentThread();
                LOG.debug("Obtained lock for {}", PollableNode.this);
            }
            m_obtainCount++;
        }
        
        public synchronized void obtain(long timeout) {
            
            if (m_owner != Thread.currentThread()) {
                LOG.debug("Trying to obtain lock for {}", PollableNode.this);
                long now = System.currentTimeMillis();
                long endTime = (timeout == 0 ? Long.MAX_VALUE : now+timeout);
                while (m_owner != null) {
                    try { wait(endTime-now);} catch (InterruptedException e) { throw new ThreadInterrupted("Lock for "+PollableNode.this+" is unavailable", e);}
                    now = System.currentTimeMillis();
                    if (now >= endTime)
                        throw new LockUnavailable("Unable to obtain lock for "+PollableNode.this+" before timeout");
                }
                m_owner = Thread.currentThread();
                LOG.debug("Obtained lock for {}", PollableNode.this);
            }
            m_obtainCount++;
        }
        
        public synchronized void release() {
            if (m_owner == Thread.currentThread()) {
                m_obtainCount--;
                if (m_obtainCount == 0) {
                    LOG.debug("Releasing lock for {}", PollableNode.this);
                    m_owner = null;
                    notifyAll();
                }
            }
        }

        /**
         * @return
         */
        public synchronized boolean isLockAvailable() {
            return m_owner == null;
        }

    }
    
    private final int m_nodeId;
    private String m_nodeLabel;
    private final Lock m_lock = new Lock();

    /**
     * <p>Constructor for PollableNode.</p>
     *
     * @param network a {@link org.opennms.netmgt.poller.pollables.PollableNetwork} object.
     * @param nodeId a int.
     * @param nodeLabel a {@link java.lang.String} object.
     */
    public PollableNode(PollableNetwork network, int nodeId, String nodeLabel) {
        super(network, Scope.NODE);
        m_nodeId = nodeId;
        m_nodeLabel = nodeLabel;
    }

    /**
     * <p>getNodeId</p>
     *
     * @return a int.
     */
    public int getNodeId() {
        return m_nodeId;
    }
    
    /**
     * <p>getNodeLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNodeLabel() {
        return m_nodeLabel;
    }

    /**
     * <p>setNodeLabel</p>
     *
     * @param nodeLabel a {@link java.lang.String} object.
     */
    public void setNodeLabel(String nodeLabel) {
        m_nodeLabel = nodeLabel;
    }

    /**
     * <p>createInterface</p>
     *
     * @param addr a {@link java.net.InetAddress} object.
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableInterface} object.
     */
    public PollableInterface createInterface(final InetAddress addr) {
        final PollableInterface[] retVal = new PollableInterface[1];
        Runnable r = new Runnable() {
            @Override
            public void run() {
                PollableInterface iface =  new PollableInterface(PollableNode.this, addr);
                addMember(iface);
                retVal[0] = iface;
            }
        };
        withTreeLock(r);
        return retVal[0];
    }

    /**
     * <p>getInterface</p>
     *
     * @param addr a {@link java.net.InetAddress} object.
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableInterface} object.
     */
    public PollableInterface getInterface(InetAddress addr) {
        return (PollableInterface)getMember(addr);
    }

    /**
     * <p>getNetwork</p>
     *
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableNetwork} object.
     */
    public PollableNetwork getNetwork() {
        return (PollableNetwork)getParent();
    }
    
    /**
     * <p>getContext</p>
     *
     * @return a {@link org.opennms.netmgt.poller.pollables.PollContext} object.
     */
    @Override
    public PollContext getContext() {
        return getNetwork().getContext();
    }
    
    /** {@inheritDoc} */
    @Override
    protected Object createMemberKey(PollableElement member) {
        PollableInterface iface = (PollableInterface)member;
        return iface.getAddress();
    }

    /**
     * <p>createService</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @param addr a {@link java.net.InetAddress} object.
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableService} object.
     */
    public PollableService createService(final InetAddress addr, final String svcName) {
        final PollableService retVal[] = new PollableService[1];
        
        Runnable r = new Runnable() {
            @Override
            public void run() {
                PollableInterface iface = getInterface(addr);
                if (iface == null)
                    iface = createInterface(addr);
                retVal[0] = iface.createService(svcName);
            }
        };
        withTreeLock(r);
        return retVal[0];
    }

    /**
     * <p>getService</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @param addr a {@link java.net.InetAddress} object.
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableService} object.
     */
    public PollableService getService(InetAddress addr, String svcName) {
        PollableInterface iface = getInterface(addr);
        return (iface == null ? null : iface.getService(svcName));
    }


    /** {@inheritDoc} */
    @Override
    protected void visitThis(PollableVisitor v) {
        super.visitThis(v);
        v.visitNode(this);
    }
    
    /** {@inheritDoc} */
    @Override
    public Event createDownEvent(Date date) {
        return getContext().createEvent(EventConstants.NODE_DOWN_EVENT_UEI, getNodeId(), null, null, date, getStatus().getReason());
    }
    
    /** {@inheritDoc} */
    @Override
    public Event createUpEvent(Date date) {
        return getContext().createEvent(EventConstants.NODE_UP_EVENT_UEI, getNodeId(), null, null, date, getStatus().getReason());
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() { return String.valueOf(getNodeId()); }

    /**
     * <p>getLockRoot</p>
     *
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableElement} object.
     */
    @Override
    public PollableElement getLockRoot() {
        return this;
    }
    
    /**
     * <p>isTreeLockAvailable</p>
     *
     * @return a boolean.
     */
    @Override
    public boolean isTreeLockAvailable() {
        return m_lock.isLockAvailable();
    }
    
    /** {@inheritDoc} */
    @Override
    public void obtainTreeLock(long timeout) {
        if (timeout == 0)
            m_lock.obtain();
        else
            m_lock.obtain(timeout);
    }
    
    /**
     * <p>releaseTreeLock</p>
     */
    @Override
    public void releaseTreeLock() {
        m_lock.release();
    }
    
    /** {@inheritDoc} */
    @Override
    public PollStatus doPoll(final PollableElement elem) {
        final PollStatus retVal[] = new PollStatus[1];
        Runnable r = new Runnable() {
            @Override
            public void run() {
                resetStatusChanged();
                retVal[0] =  poll(elem);
            }
        };
        withTreeLock(r);
        return retVal[0];
    }

}
