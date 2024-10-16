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
import java.util.concurrent.Callable;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.xml.event.Event;

/**
 * Represents a PollableInterface
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public class PollableInterface extends PollableContainer {

    private final InetAddress m_addr;

    /**
     * <p>Constructor for PollableInterface.</p>
     *
     * @param node a {@link org.opennms.netmgt.poller.pollables.PollableNode} object.
     * @param addr a {@link java.net.InetAddress} object.
     */
    public PollableInterface(PollableNode node, InetAddress addr) {
        super(node, Scope.INTERFACE);
        m_addr = addr;
    }

    /**
     * <p>getNode</p>
     *
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableNode} object.
     */
    public PollableNode getNode() {
        return (PollableNode)getParent();
    }
    
    private void setNode(PollableNode newNode) {
        setParent(newNode);
    }

    /**
     * <p>getNetwork</p>
     *
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableNetwork} object.
     */
    public PollableNetwork getNetwork() {
        return getNode().getNetwork();
    }
    
    /**
     * <p>getContext</p>
     *
     * @return a {@link org.opennms.netmgt.poller.pollables.PollContext} object.
     */
    @Override
    public PollContext getContext() {
        return getNode().getContext();
    }

    /**
     * <p>getIpAddr</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getIpAddr() {
        return InetAddressUtils.str(m_addr);
    }
    
    /**
     * <p>getAddress</p>
     *
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getAddress() {
        return m_addr;
    }

    /**
     * <p>getNodeId</p>
     *
     * @return a int.
     */
    public int getNodeId() {
        return getNode().getNodeId();
    }

    /**
     * <p>getNodeLabel</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getNodeLabel() {
        return getNode().getNodeLabel();
    }

    public String getNodeLocation() {
        return getNode().getNodeLocation();
    }

    /**
     * <p>createService</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableService} object.
     */
    public PollableService createService(final String svcName) {
        return withTreeLock(new Callable<PollableService>() {
            @Override
            public PollableService call() {

                PollableService svc = new PollableService(PollableInterface.this, svcName);
                addMember(svc);
                return svc;

            }
            
        });
        
    }

    /**
     * <p>getService</p>
     *
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableService} object.
     */
    public PollableService getService(String svcName) {
        return (PollableService)getMember(svcName);
    }
    
    public List<PollableService> getServices() {
        final List<PollableService> services = new ArrayList<>();
        for (final PollableElement pe : getMembers()) {
            if (pe instanceof PollableService) {
                services.add((PollableService)pe);
            }
        }
        return services;
    }

    /** {@inheritDoc} */
    @Override
    protected Object createMemberKey(PollableElement member) {
        PollableService svc = (PollableService)member;
        return svc.getSvcName();
    }
    
    /** {@inheritDoc} */
    @Override
    protected void visitThis(PollableVisitor v) {
        super.visitThis(v);
        v.visitInterface(this);
    }
    
    /**
     * <p>recalculateStatus</p>
     */
    @Override
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


    /**
     * <p>selectPollElement</p>
     *
     * @return a {@link org.opennms.netmgt.poller.pollables.PollableElement} object.
     */
    @Override
    public PollableElement selectPollElement() {
        PollableService critSvc = getCriticalService();
        return (critSvc != null ? critSvc : super.selectPollElement());
    }
    
    /** {@inheritDoc} */
    @Override
    protected PollStatus poll(PollableElement elem) {
        PollableService critSvc = getCriticalService();
        if (getStatus().isUp() || critSvc == null || elem == critSvc)
            return super.poll(elem);
    
        return PollStatus.down();
    }
    
    /** {@inheritDoc} */
    @Override
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
    /** {@inheritDoc} */
    @Override
    public Event createDownEvent(Date date) {
        return getContext().createEvent(EventConstants.INTERFACE_DOWN_EVENT_UEI, getNodeId(), getAddress(), null, date, getStatus().getReason());
    }
    
    
    /** {@inheritDoc} */
    @Override
    public Event createUpEvent(Date date) {
        return getContext().createEvent(EventConstants.INTERFACE_UP_EVENT_UEI, getNodeId(), getAddress(), null, date, getStatus().getReason());
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() { return "PollableInterface [" + getNode()+":"+getIpAddr() + "]"; }

    /**
     * <p>reparentTo</p>
     *
     * @param newNode a {@link org.opennms.netmgt.poller.pollables.PollableNode} object.
     */
    public void reparentTo(final PollableNode newNode) {
        final PollableNode oldNode = getNode();
        
        if (oldNode.equals(newNode)) return;
        
        // always lock the nodes in nodeId order so deadlock is not possible
        final PollableNode firstNode = (oldNode.getNodeId() <= newNode.getNodeId() ? oldNode : newNode);
        final PollableNode secondNode = (oldNode.getNodeId() <= newNode.getNodeId() ? newNode : oldNode);
        
        final Runnable reparent = new Runnable() {
            @Override
            public void run() {
                oldNode.resetStatusChanged();
                newNode.resetStatusChanged();
                
                oldNode.removeMember(PollableInterface.this);
                newNode.addMember(PollableInterface.this);
                setNode(newNode);
                
                if (getCause() == null || getCause().equals(oldNode.getCause())) {
                    // the current interface outage is a node outage or no outage at all
                    if (newNode.getCause() != null) {
                        // if the new Node has a node outage then we recursively set the 
                        // causes so when process events we properly handle the causes
                        PollableVisitor visitor = new PollableVisitorAdaptor() {
                            @Override
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
            @Override
            public void run() {
                secondNode.withTreeLock(reparent);
            }
        };
        
        firstNode.withTreeLock(lockSecondNodeAndRun);
        
    }

}
