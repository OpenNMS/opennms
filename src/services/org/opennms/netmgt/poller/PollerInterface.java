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
// 2003 Oct 08: Implemented the poller release function.
// 2003 Jan 31: Cleaned up some unused imports.
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

import java.net.InetAddress;
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
 * The PollableInterface class...
 * </P>
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 */
public class PollerInterface extends PollerContainer {

    /**
     * node that this interface belongs to
     */
    private PollerNode m_node;

    /**
     * IP address of this interface
     */
    private InetAddress m_address;

    /**
     * Constructor.
     */
    public PollerInterface(PollerNode node, InetAddress address) {
        super(PollStatus.STATUS_UNKNOWN);
        m_node = node;
        m_address = address;
    }

    /**
     * Return the node this interface belongs to
     */
    public PollerNode getNode() {
        return m_node;
    }

    /**
     * Return the address of this interface
     */
    public InetAddress getAddress() {
        return m_address;
    }

    public Collection getServices() {
        return getMembers();
    }

    public PollerService findService(String svcName) {
        // Sanity check
        if (svcName == null)
            return null;

        return (PollerService) findMember(svcName);
    }

    /**
     * Add a PollableService object to the services map keyed by service name.
     */
    public synchronized void addService(PollerService service) {
        addMember(service.getServiceName(), service);
        this.recalculateStatus();
    }

    public synchronized void removeService(PollerService service) {
        ServiceMonitor sm = getPoller().getServiceMonitor(service.getServiceName());
        service.releaseMonitor(sm);
        removeMember(service.getServiceName());
        this.recalculateStatus();
    }

    public synchronized void deleteAllServices() {
        deleteMembers();
    }

    /**
     * Takes a service and returns true if this interface supports the service.
     * Returns false otherwise.
     * 
     */
    public boolean supportsService(String svcName) {
        // Sanity check
        if (svcName == null)
            return false;

        PollerService pSvc = findService(svcName);
        if (pSvc != null)
            return true;
        else
            return false;
    }

    /**
     * Responsible for recalculating the UP/DOWN status of the interface.
     * 
     */
    public synchronized void recalculateStatus() {
        Category log = ThreadCategory.getInstance(getClass());

        if (log.isDebugEnabled())
            log.debug("recaclulateStatus: interface=" + m_address.getHostAddress());

        PollStatus status = PollStatus.STATUS_UNKNOWN;

        // Get configured critical service
        String criticalSvcName = getPollerConfig().getCriticalService();

        // If critical service defined and supported by this
        // interface then simply need to check the critical
        // service status...
        //
        if (hasCriticalService()) {
            PollerService criticalSvc = findService(criticalSvcName);
            if (criticalSvc.getStatus() == PollStatus.STATUS_UP)
                status = PollStatus.STATUS_UP;
            else
                status = PollStatus.STATUS_DOWN;
        } else {
            // No critical service defined so iterate over the
            // interface's services in order to determine interface
            // status
            boolean allServicesDown = true;
            Iterator iter = m_members.values().iterator();
            while (iter.hasNext()) {
                PollerService pSvc = (PollerService) iter.next();
                if (pSvc.getStatus() == PollStatus.STATUS_UP) {
                    if (log.isDebugEnabled())
                        log.debug("recalculateStatus: svc=" + pSvc.getServiceName() + " status=UP, atleast one svc is UP!");
                    allServicesDown = false;
                    break;
                }
            }

            if (allServicesDown)
                status = PollStatus.STATUS_DOWN;
            else
                status = PollStatus.STATUS_UP;
        }

        setStatus(status);

        if (log.isDebugEnabled())
            log.debug("recalculateStatus: completed, interface=" + m_address.getHostAddress() + " status=" + getStatus());
    }

    /**
     * <P>
     * Invokes a poll of the service.
     * </P>
     * 
     * If the service changes status then node outage processing will be invoked
     * and the status of the entire interface will be evaluated.
     */
    public synchronized PollStatus poll(PollerService pSvc) {
        Category log = ThreadCategory.getInstance(getClass());

        resetStatusChanged();

        // Critical service defined and supported by interface
        if (isEssential(pSvc)) {
            // Issue poll
            PollStatus svcStatus = pSvc.poll();
            if (svcStatus != getStatus() && pSvc.statusChanged()) {
                // Mark interface as up
                PollStatus newStatus = pollRemainingEssentialServices(pSvc);
                updateStatus(newStatus);
                
            }
        } else {
            // This may be the first time this status has been
            // polled since the interface was found to be DOWN
            // (since it only takes the critical service being DOWN
            // for the entire interface to be seen as DOWN) so go
            // ahead and set the status on this service to DOWN.
            pSvc.updateStatus(PollStatus.STATUS_DOWN);
            // markRemainingServices(pSvc, PollStatus.STATUS_DOWN);
        }

        if (log.isDebugEnabled())
            log.debug("poll: poll of interface " + m_address.getHostAddress() + " completed, status= " + getStatus());
        return getStatus();
    }

    private PollStatus pollRemainingEssentialServices(PollerService pSvc) {
        return (hasCriticalService() && getStatus() == PollStatus.STATUS_UP ? pollCriticalService(pSvc) : pollRemainingServices(pSvc));
    }

    private PollStatus pollCriticalService(PollerService pSvc) {

        // Get configured critical service
        String criticalSvcName = getPollerConfig().getCriticalService();
        PollerService criticalSvc = this.findService(criticalSvcName);

        // If the service we just polled WAS in fact the critical
        // service then no need to poll it again...the interface is DOWN!
        return (pSvc == criticalSvc ? pSvc.getStatus() : criticalSvc.poll());
    }


    private boolean isEssential(PollerService pSvc) {
        return !hasCriticalService() || isTheCriticalService(pSvc) || getStatus() == PollStatus.STATUS_UP;
    }

    /**
     * @param svc
     * @return
     */
    private boolean isTheCriticalService(PollerService svc) {
        String criticalSvcName = getPollerConfig().getCriticalService();
        return hasCriticalService() && svc.getServiceName().equals(criticalSvcName);
    }
    
    private boolean hasCriticalService() {
        String criticalSvcName = getPollerConfig().getCriticalService();
        return criticalSvcName != null && this.supportsService(criticalSvcName);
    }

    private PollStatus pollRemainingServices(PollerService pSvc) {
        
        // If no critical service defined then retrieve the
        // value of the 'pollAllIfNoCriticalServiceDefined' flag
        boolean doPoll = (isTheCriticalService(pSvc) ? true : getPollerConfig().pollAllIfNoCriticalServiceDefined());

        PollStatus allSvcStatus = pSvc.getStatus();
        Iterator iter = m_members.values().iterator();
        while (iter.hasNext()) {
            PollerService svc = (PollerService) iter.next();
            // Skip service that was already polled
            if (svc != pSvc) {
                PollStatus svcStatus = (doPoll ? svc.poll() : svc.getStatus());
                if (svcStatus == PollStatus.STATUS_UP) {
                    allSvcStatus = PollStatus.STATUS_UP;
                }
            } 
        }
        return allSvcStatus;
    }

    /**
     * @return
     */
    private PollerConfig getPollerConfig() {
        return getPoller().getPollerConfig();
    }

    /**
     * 
     */
    Poller getPoller() {
        return m_node.getPoller();
    }

    /**
     * @param newPNode
     */
    public void setNode(PollerNode newPNode) {
        m_node = newPNode;
    }

    /**
     * @return
     */
    public int getNodeId() {
        return getNode().getNodeId();
    }

    /**
     * @param date
     * @param node
     * @return
     */
    public Event createDownEvent(Date date) {
        return getPoller().createEvent(EventConstants.INTERFACE_DOWN_EVENT_UEI, getNodeId(), getAddress(), null, date);
    }

    /**
     * @param date
     * @return
     */
    public Event createUpEvent(Date date) {
        return getPoller().createEvent(EventConstants.INTERFACE_UP_EVENT_UEI, getNodeId(), getAddress(), null, date);
    }

    public void visit(PollerVisitor v) {
        super.visit(v);
        v.visitInterface(this);
        visitMembers(v);
    }

}
