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
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.xml.event.Event;

/**
 * Represents a PollableNode 
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class PollableNode extends PollableContainer {

    /**
     * Represents a Lock 
     *
     * @author brozow
     */
    public class Lock {
        private Thread m_owner = null;
        private int m_obtainCount = 0;
        
        public synchronized void obtain() {
            ThreadCategory log = ThreadCategory.getInstance(getClass());
            
            if (m_owner != Thread.currentThread()) {
                log.debug("Trying to obtain lock for "+PollableNode.this);
                while (m_owner != null) {
                    try { wait();} catch (InterruptedException e) { throw new ThreadInterrupted("Lock for "+PollableNode.this+" is unavailable", e);}
                }
                m_owner = Thread.currentThread();
                log.debug("Obtained lock for "+PollableNode.this);
            }
            m_obtainCount++;
        }
        
        public synchronized void obtain(long timeout) {
            ThreadCategory log = ThreadCategory.getInstance(getClass());
            
            if (m_owner != Thread.currentThread()) {
                log.debug("Trying to obtain lock for "+PollableNode.this);
                long now = System.currentTimeMillis();
                long endTime = (timeout == 0 ? Long.MAX_VALUE : now+timeout);
                while (m_owner != null) {
                    try { wait(endTime-now);} catch (InterruptedException e) { throw new ThreadInterrupted("Lock for "+PollableNode.this+" is unavailable", e);}
                    now = System.currentTimeMillis();
                    if (now >= endTime)
                        throw new LockUnavailable("Unable to obtain lock for "+PollableNode.this+" before timeout");
                }
                m_owner = Thread.currentThread();
                log.debug("Obtained lock for "+PollableNode.this);
            }
            m_obtainCount++;
        }
        
        public synchronized void release() {
            if (m_owner == Thread.currentThread()) {
                m_obtainCount--;
                if (m_obtainCount == 0) {
                    ThreadCategory.getInstance(getClass()).debug("Releasing lock for "+PollableNode.this);
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
    private final String m_nodeLabel;
    private final Lock m_lock = new Lock();

    public PollableNode(PollableNetwork network, int nodeId, String nodeLabel) {
        super(network, Scope.NODE);
        m_nodeId = nodeId;
        m_nodeLabel = nodeLabel;
    }

    public int getNodeId() {
        return m_nodeId;
    }
    
    public String getNodeLabel() {
        return m_nodeLabel;
    }

    
    public PollableInterface createInterface(final InetAddress addr) {
        final PollableInterface[] retVal = new PollableInterface[1];
        Runnable r = new Runnable() {
            public void run() {
                PollableInterface iface =  new PollableInterface(PollableNode.this, addr);
                addMember(iface);
                retVal[0] = iface;
            }
        };
        withTreeLock(r);
        return retVal[0];
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
    public PollableService createService(final InetAddress addr, final String svcName) {
        final PollableService retVal[] = new PollableService[1];
        
        Runnable r = new Runnable() {
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
        return getContext().createEvent(EventConstants.NODE_DOWN_EVENT_UEI, getNodeId(), null, null, date, getStatus().getReason());
    }
    
    public Event createUpEvent(Date date) {
        return getContext().createEvent(EventConstants.NODE_UP_EVENT_UEI, getNodeId(), null, null, date, getStatus().getReason());
    }
    
    public String toString() { return String.valueOf(getNodeId()); }

    public PollableElement getLockRoot() {
        return this;
    }
    
    public boolean isTreeLockAvailable() {
        return m_lock.isLockAvailable();
    }
    
    public void obtainTreeLock(long timeout) {
        if (timeout == 0)
            m_lock.obtain();
        else
            m_lock.obtain(timeout);
    }
    
    public void releaseTreeLock() {
        m_lock.release();
    }
    
    public PollStatus doPoll(final PollableElement elem) {
        final PollStatus retVal[] = new PollStatus[1];
        Runnable r = new Runnable() {
            public void run() {
                resetStatusChanged();
                retVal[0] =  poll(elem);
            }
        };
        withTreeLock(r);
        return retVal[0];
    }

}
