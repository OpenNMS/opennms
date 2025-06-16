/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.poller.pollables;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a PollableNode
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 */
public class PollableNode extends PollableContainer {
    private static final Logger LOG = LoggerFactory.getLogger(PollableNode.class);

    private final int m_nodeId;
    private String m_nodeLabel;
    private final String m_nodeLocation;
    private final ReentrantLock m_lock = new ReentrantLock(true);

    /**
     * <p>Constructor for PollableNode.</p>
     *
     * @param network a {@link org.opennms.netmgt.poller.pollables.PollableNetwork} object.
     * @param nodeId a int.
     * @param nodeLabel a {@link java.lang.String} object.
     * @param nodeLocation a {@link java.lang.String} object.
     */
    public PollableNode(PollableNetwork network, int nodeId, String nodeLabel, String nodeLocation) {
        super(network, Scope.NODE);
        m_nodeId = nodeId;
        m_nodeLabel = nodeLabel;
        m_nodeLocation = nodeLocation;
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

    public void setNodeLabel(String label) {
        m_nodeLabel = label;
    }

    public String getNodeLocation() {
        return m_nodeLocation;
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

    public List<PollableInterface> getInterfaces() {
        final List<PollableInterface> ifaces = new ArrayList<>();
        for (final PollableElement pe : getMembers()) {
            if (pe instanceof PollableInterface) {
                ifaces.add((PollableInterface)pe);
            }
        }
        return ifaces;
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
        final PollableService[] retVal = new PollableService[1];
        
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
    public String toString() { return "PollableNode [" + getNodeId() + "]"; }

    /**
     * <p>getLockRoot</p>
     *
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableElement} object.
     */
    @Override
    protected PollableElement getLockRoot() {
        return this;
    }
    
    /**
     * This method does not have a timeout, it blocks indefinitely
     * until the lock is obtained. 
     */
    @Override
    protected void obtainTreeLock() {
        m_lock.lock();
    }

    /** 
     * This method tries to obtain the lock within the given timeout.
     * @param Timeout in milliseconds
     * @throws LockUnavailable If the lock cannot be acquired before
     * the timeout or the thread is interrupted while trying to acquire the 
     * lock.
     */
    @Override
    protected void obtainTreeLock(long timeout) throws LockUnavailable {
        if (timeout < 1) {
            obtainTreeLock();
        } else {
            try {
                if (m_lock.tryLock(timeout, TimeUnit.MILLISECONDS)) {
                    // Lock was successful
                    return;
                } else {
                    // Lock was unsuccessful
                    throw new LockUnavailable("Unable to obtain lock for " + PollableNode.this + " within " + timeout + " milliseconds");
                }
            } catch (InterruptedException e) {
                throw new LockUnavailable("Interrupted while waiting " + timeout + " milliseconds to obtain lock for " + PollableNode.this);
            }
        }
    }
    
    /**
     * <p>releaseTreeLock</p>
     */
    @Override
    protected void releaseTreeLock() {
        m_lock.unlock();
    }
    
    /** {@inheritDoc} */
    @Override
    public PollStatus doPoll(final PollableElement elem) {
        final PollStatus[] retVal = new PollStatus[1];
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
