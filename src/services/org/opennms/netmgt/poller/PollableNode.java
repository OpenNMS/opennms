//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2003 Jan 31: Cleaned up some unused imports.
// 2002 Nov 13: Added the nodelabel and interfaceresolve variables to notifications.
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
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.netmgt.poller;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.xml.event.Event;

/**
 * <P>
 * The PollableNode class...
 * </P>
 * 
 * @author <A HREF="mailto:jamesz@opennms.com">James Zuo </A>
 * @author <A HREF="mailto:tarus@opennms.org">Tarus Balog </A>
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 */
public class PollableNode extends PollableAggregate {
    /**
     * nodeId
     */
    private int m_nodeId;

    /**
     * Used to lock access to the PollableNode during a poll()
     */
    private Object m_lock;

    private boolean m_isLocked;

    private boolean m_isDeleted;

    private Poller m_poller;

    /**
     * Constructor.
     */
    public PollableNode(int nodeId, Poller poller) {
        super(PollStatus.STATUS_UNKNOWN);
        m_poller = poller;

        m_nodeId = nodeId;

        m_lock = new Object();
        m_isLocked = false;

        m_isDeleted = false;
    }

    public int getNodeId() {
        return m_nodeId;
    }

    public Collection getInterfaces() {
        return getMembers();
    }

    public synchronized void addInterface(PollableInterface pInterface) {
        addMember(pInterface.getAddress().getHostAddress(), pInterface);

        PollStatus oldStatus = getStatus();
        this.recalculateStatus();
        PollStatus newStatus = getStatus();
        if (oldStatus != newStatus)
            setStatusChanged(true);
    }

    public synchronized void deleteAllInterfaces() {
        deleteMembers();
    }

    public PollableInterface findInterface(String ipAddress) {
        return (PollableInterface) findMember(ipAddress);
    }

    public synchronized void removeInterface(PollableInterface pInterface) {
        String key = pInterface.getAddress().getHostAddress();
        removeMember(key);
        PollStatus oldStatus = getStatus();
        this.recalculateStatus();
        PollStatus newStatus = getStatus();
        if (oldStatus != newStatus)
            setStatusChanged(true);
        
    }

    public void markAsDeleted() {
        m_isDeleted = true;
    }

    public boolean isDeleted() {
        return m_isDeleted;
    }

    /**
     * Responsible for recalculating this node's UP/DOWN status.
     */
    public synchronized void recalculateStatus() {
        Category log = ThreadCategory.getInstance(getClass());

        if (log.isDebugEnabled())
            log.debug("recalculateStatus: nodeId=" + m_nodeId);

        PollStatus status = PollStatus.STATUS_UNKNOWN;

        // Inspect status of each of the node's interfaces
        // in order to determine the current status of the node.
        //
        boolean allInterfacesDown = true;
        Iterator iter = getMembers().iterator();
        while (iter.hasNext()) {
            PollableInterface pIf = (PollableInterface) iter.next();
            if (pIf.getStatus() == PollStatus.STATUS_UNKNOWN)
                pIf.recalculateStatus();

            if (pIf.getStatus() == PollStatus.STATUS_UP) {
                if (log.isDebugEnabled())
                    log.debug("recalculateStatus: interface=" + pIf.getAddress().getHostAddress() + " status=Up, atleast one interface is UP!");
                allInterfacesDown = false;
                break;
            }
        }

        if (allInterfacesDown)
            status = PollStatus.STATUS_DOWN;
        else
            status = PollStatus.STATUS_UP;

        setStatus(status);

        if (log.isDebugEnabled())
            log.debug("recalculateStatus: completed, nodeId=" + m_nodeId + " status=" + getStatus());

    }

    public boolean getNodeLock(long timeout) throws InterruptedException {
        boolean ownLock = false;

        synchronized (m_lock) {
            // Is the node currently locked?
            if (!m_isLocked) {
                // Now it is...
                m_isLocked = true;
                ownLock = true;
            } else {
                // Someone else has the lock, wait
                // for the specified timeout...
                m_lock.wait(timeout);

                // Was the lock released?
                if (!m_isLocked) {
                    // Yep...
                    m_isLocked = true;
                    ownLock = true;
                }
            }
        }

        return ownLock;
    }

    public void releaseNodeLock() throws InterruptedException {
        synchronized (m_lock) {
            if (m_isLocked) {
                m_isLocked = false;
                m_lock.notifyAll();
            }
        }
    }

    public Event createUpEvent(Date date) {
        return getPoller().createEvent(EventConstants.NODE_UP_EVENT_UEI, m_nodeId, null, null, date);
    }

    public Event createDownEvent(Date date) {
        return getPoller().createEvent(EventConstants.NODE_DOWN_EVENT_UEI, m_nodeId, null, null, date);
    }

    /**
     * Invokes a poll of the remote interface.
     * 
     * If the interface changes status then node outage processing will be
     * invoked and the status of the entire node will be evaluated.
     */
    public synchronized PollStatus poll(PollableService pSvc) {
        Category log = ThreadCategory.getInstance(getClass());

        if (log.isDebugEnabled())
            log.debug("poll: polling nodeid " + m_nodeId + " status=" + getStatus());

        setStatusChanged(false);

        // Retrieve PollableInterface object from the NIF
        PollableInterface pInterface = pSvc.getInterface();
        
        // Poll the service via the PollableInterface object
        PollStatus ifStatus = pInterface.poll(pSvc);
        
        // If interface status changed and is different from the node status
        if (ifStatus != getStatus() && pInterface.statusChanged()) {
        
            log.debug("poll: requested interface is "+ifStatus+"; testing remaining interfaces");
        
            // update the nodes status
            PollStatus newStatus = pollRemainingInterfaces(pInterface);
            updateStatus(newStatus);
        
        }

        // Call generateEvents() which will inspect the current status
        // of the N/I/S tree and generate any events necessary to keep
        // RTC and OutageManager in sync.
        generateEvents(new Date());

        if (log.isDebugEnabled())
            log.debug("poll: poll of nodeid " + m_nodeId + " completed, status=" + getStatus());

        return getStatus();
    }

    private PollStatus pollRemainingInterfaces(PollableInterface pInterface) {
        
        Category log = ThreadCategory.getInstance(getClass());

        PollStatus allIfStatus = pInterface.getStatus();

        // Iterate over list of interfaces
        Iterator iter = getMembers().iterator();
        while (iter.hasNext()) {
            PollableInterface pIf = (PollableInterface) iter.next();
            
            // Skip the interface that was already polled
            if (pIf != pInterface) {
                log.debug("poll: (node outage) testing interface " + pIf.getAddress().getHostAddress());
                
                PollStatus ifStatus = pollInterface(pIf);
                
                if (ifStatus == PollStatus.STATUS_UP) {
                    // we don't return early because we want to poll ALL the interfaces
                    allIfStatus = PollStatus.STATUS_UP;
                    log.debug("poll: (node outage) not a node outage - at least one interface is up");
                }
            }
        }

        return allIfStatus;
    }

    private PollStatus pollInterface(PollableInterface pIf) {
        
        // Get critical service
        String critSvcName = getPollerConfig().getCriticalService();

        PollableService svc;
        if (critSvcName != null && pIf.supportsService(critSvcName)) {
            // poll the critical service if the interface has one
            svc = pIf.findService(critSvcName);
        } else {
            // can't find a critical service to just pick a service
            svc = (PollableService) pIf.getServices().iterator().next();
        }
        return pIf.poll(svc);
    }

    Poller getPoller() {
        return m_poller;
    }

    private PollerConfig getPollerConfig() {
        return getPoller().getPollerConfig();
    }
}
