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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.scheduler.Scheduler;

/**
 * <P>
 * The PollableInterface class...
 * </P>
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 */
public class PollableInterface implements Pollable {
    /**
     * node that this interface belongs to
     */
    private PollableNode m_node;

    /**
     * IP address of this interface
     */
    private InetAddress m_address;

    /**
     * Last known/current status
     */
    private int m_status;

    /**
     * Map of 'PollableService' objects keyed by service name
     */
    private Map m_services;

    /**
     * Set by poll() method.
     */
    private boolean m_statusChangedFlag;

    /**
     * Reference to the poller scheduler
     */
    private Scheduler m_scheduler;

    /**
     * Reference to the list of all scheduled services
     */
    private List m_pollableServices;

    /**
     * Constructor.
     */
    public PollableInterface(PollableNode node, InetAddress address) {
        m_node = node;
        m_address = address;
        m_services = Collections.synchronizedMap(new HashMap());
        m_scheduler = getPoller().getScheduler();
        m_pollableServices = getPoller().getPollableServiceList();
        m_statusChangedFlag = false;
        m_status = Pollable.STATUS_UNKNOWN;
    }

    /**
     * Return the node this interface belongs to
     */
    public PollableNode getNode() {
        return m_node;
    }

    /**
     * Return the address of this interface
     */
    public InetAddress getAddress() {
        return m_address;
    }

    public Collection getServices() {
        return m_services.values();
    }

    public PollableService getService(String svcName) {
        // Sanity check
        if (svcName == null)
            return null;

        return (PollableService) m_services.get(svcName);
    }

    /**
     * Add a PollableService object to the services map keyed by service name.
     * 
     * TODO: What's this warning all about
     * WARNING: For node outage processing we are only interested in testing a
     * particular service once regardless of how many times the service is
     * actually scheduled for polling (due to inclusion by multiple packages),
     * therefore note that only the LAST pollable service added to the interface
     * is actually saved in the services map. Any existing entry is simply
     * replaced.
     */
    public synchronized void addService(PollableService service) {
        m_services.put(service.getServiceName(), service);
        this.recalculateStatus();
    }

    public synchronized void removeService(PollableService service) {
        ServiceMonitor sm = getPoller().getServiceMonitor(service.getServiceName());
        sm.release(service);
        m_services.remove(service.getServiceName());
        this.recalculateStatus();
    }

    public synchronized void deleteAllServices() {
        m_services.clear();
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

        PollableService pSvc = (PollableService) m_services.get(svcName);
        if (pSvc != null)
            return true;
        else
            return false;
    }

    public int getStatus() {
        return m_status;
    }

    public boolean statusChanged() {
        return m_statusChangedFlag;
    }

    public synchronized void resetStatusChanged() {
        m_statusChangedFlag = false;

        // Iterate over service list and reset each services's
        // status changed flag
        Iterator i = m_services.values().iterator();
        while (i.hasNext()) {
            PollableService pSvc = (PollableService) i.next();
            pSvc.resetStatusChanged();
        }
    }

    /**
     * Responsible for recalculating the UP/DOWN status of the interface.
     * 
     */
    public synchronized void recalculateStatus() {
        Category log = ThreadCategory.getInstance(getClass());

        if (log.isDebugEnabled())
            log.debug("recaclulateStatus: interface=" + m_address.getHostAddress());

        int status = Pollable.STATUS_UNKNOWN;

        // Get configured critical service
        String criticalSvcName = getPollerConfig().getCriticalService();

        // If critical service defined and supported by this
        // interface then simply need to check the critical
        // service status...
        //
        if (criticalSvcName != null && this.supportsService(criticalSvcName)) {
            PollableService criticalSvc = getService(criticalSvcName);
            if (criticalSvc.getStatus() == Pollable.STATUS_UP)
                status = Pollable.STATUS_UP;
            else
                status = Pollable.STATUS_DOWN;
        } else {
            // No critical service defined so iterate over the
            // interface's services in order to determine interface
            // status
            boolean allServicesDown = true;
            Iterator iter = m_services.values().iterator();
            while (iter.hasNext()) {
                PollableService pSvc = (PollableService) iter.next();
                if (pSvc.getStatus() == Pollable.STATUS_UP) {
                    if (log.isDebugEnabled())
                        log.debug("recalculateStatus: svc=" + pSvc.getServiceName() + " status=UP, atleast one svc is UP!");
                    allServicesDown = false;
                    break;
                }
            }

            if (allServicesDown)
                status = Pollable.STATUS_DOWN;
            else
                status = Pollable.STATUS_UP;
        }

        m_status = status;

        if (log.isDebugEnabled())
            log.debug("recalculateStatus: completed, interface=" + m_address.getHostAddress() + " status=" + Pollable.statusType[m_status]);
    }

    /**
     * <P>
     * Invokes a poll of the service.
     * </P>
     * 
     * If the service changes status then node outage processing will be invoked
     * and the status of the entire interface will be evaluated.
     */
    public synchronized int poll(PollableService pSvc) {
        Category log = ThreadCategory.getInstance(getClass());

        m_statusChangedFlag = false;

        int svcStatus = Pollable.STATUS_UNKNOWN;

        // Get configured critical service
        String criticalSvcName = getPollerConfig().getCriticalService();
        if (log.isDebugEnabled())
            log.debug("poll: polling interface " + m_address.getHostAddress() + " status=" + Pollable.statusType[m_status] + " (criticalSvc= " + criticalSvcName + ")");

        // If no critical service defined then retrieve the
        // value of the 'pollAllIfNoCriticalServiceDefined' flag
        boolean pollAllServices = true;
        if (criticalSvcName == null) {
            pollAllServices = getPollerConfig().pollAllIfNoCriticalServiceDefined();
            if (log.isDebugEnabled())
                log.debug("poll: pollAllServices (since no critical svc defined) flag: " + pollAllServices);
        }

        // Polling logic if interface is currently DOWN
        //
        // Remember if critical service is defined and the interface is DOWN
        // we only poll the critical service (provided that the interface
        // actually supports the critical service).
        //
        if (m_status == Pollable.STATUS_DOWN) {
            // Critical service defined and supported by interface
            if (criticalSvcName != null && this.supportsService(criticalSvcName)) {
                PollableService criticalSvc = null;

                // Service to be polled is critical service?
                if (pSvc.getServiceName().equals(criticalSvcName)) {
                    // Issue poll
                    svcStatus = pSvc.poll();
                    criticalSvc = pSvc;
                } else {
                    // This may be the first time this status has been
                    // polled since the interface was found to be DOWN
                    // (since it only takes the critical service being DOWN
                    // for the entire interface to be seen as DOWN) so go
                    // ahead and set the status on this service to DOWN.
                    pSvc.setStatus(Pollable.STATUS_DOWN);

                    /*
                     * -------------------------------------------- Commenting
                     * this logic out for now, need to implement code to create
                     * PollableServiceProxy objects and this doesn't fit in well
                     * with that...will revisit this afterwards.
                     * -------------------------------------------- // Determine
                     * if we need to repoll the critical // service using the
                     * statement: // NOW - T:critical >= I:svc // where // NOW =
                     * current time // T:critical= time critical svc last polled //
                     * I:svc = current poll interval for scheduled svc // // If
                     * the statement resolves to true we should go // ahead and
                     * repoll the critical svc otherwise // simply
                     * return.criticalSvc.getLastPollTime() criticalSvc =
                     * this.getService(criticalSvcName); if (criticalSvc !=
                     * null) { long timeSinceLastCriticalPoll =
                     * System.currentTimeMillis() -
                     * criticalSvc.getLastPollTime(); if (log.isDebugEnabled())
                     * log.debug("poll: timeSinceLastCriticalPoll=" +
                     * timeSinceLastCriticalPoll + " interval for " +
                     * pSvc.getInterface().getAddress().getHostAddress() + "/" +
                     * pSvc.getServiceName() + " is " +
                     * pSvc.getLastScheduleInterval()); if (
                     * timeSinceLastCriticalPoll >=
                     * pSvc.getLastScheduleInterval()) { // Re-poll critical svc
                     * if (log.isDebugEnabled()) log.debug("poll: re-polling
                     * critical service..."); svcStatus = criticalSvc.poll(); } }
                     * ------------------------------------------------
                     */
                }

                if (svcStatus == Pollable.STATUS_UP && criticalSvc.statusChanged()) {
                    // Mark interface as up and poll all remaining
                    // services on this interface
                    //
                    m_status = Pollable.STATUS_UP;
                    m_statusChangedFlag = true;

                    Iterator iter = m_services.values().iterator();
                    while (iter.hasNext()) {
                        PollableService svc = (PollableService) iter.next();

                        // Skip critical service since already polled
                        if (svc == criticalSvc)
                            continue;

                        // Poll the service
                        int tmpStatus = svc.poll();

                        // If status of non-critical service changes to UP
                        // then create PollableServiceProxy object and
                        // reschedule the proxy service at the
                        // appropriate interval
                        //
                        // PollableServiceProxy is a lightweight object which
                        // encapsulates the PollableService object and allows us
                        // to reschedule the service to be polled at the
                        // appropriate
                        // interval until the scheduler schedules the
                        // PollableService
                        // object and it is updated with an interval based on
                        // its new status.
                        if (tmpStatus == Pollable.STATUS_UP) {
                            long runAt = svc.recalculateInterval() + System.currentTimeMillis();
                            m_scheduler.schedule(new PollableServiceProxy(svc, runAt), svc.recalculateInterval());
                            log.debug("poll: scheduling new PollableServiceProxy for " + m_address.getHostAddress() + "/" + svc.getServiceName() + " at interval= " + svc.recalculateInterval());
                        }
                    }

                    // Iterate over pollable services list, poll
                    // any remaining services which match this node/interface
                    // combination, and then reschedule them via the
                    // PollableServiceProxy.
                    synchronized (m_pollableServices) {
                        iter = m_pollableServices.iterator();
                        while (iter.hasNext()) {
                            PollableService tmp = (PollableService) iter.next();
                            InetAddress addr = (InetAddress) tmp.getAddress();
                            if (addr.equals(m_address)) {
                                if (!m_services.containsValue(tmp)) {
                                    int tmpStatus = tmp.poll();
                                    if (tmpStatus == Pollable.STATUS_UP) {
                                        long runAt = tmp.recalculateInterval() + System.currentTimeMillis();
                                        m_scheduler.schedule(new PollableServiceProxy(tmp, runAt), tmp.recalculateInterval());
                                        log.debug("poll: scheduling new PollableServiceProxy for " + m_address.getHostAddress() + "/" + tmp.getServiceName() + " at interval= " + tmp.recalculateInterval());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            // Critical svc not defined or not supported by interface.
            else {
                // Issue poll
                svcStatus = pSvc.poll();
                if (svcStatus == Pollable.STATUS_UP && pSvc.statusChanged()) {
                    // Mark interface as up
                    m_status = Pollable.STATUS_UP;
                    m_statusChangedFlag = true;

                    // Check flag which controls whether all services
                    // are polled if no critical service is defined
                    if (pollAllServices) {
                        // Poll all remaining services on the interface
                        Iterator iter = m_services.values().iterator();

                        while (iter.hasNext()) {
                            PollableService svc = (PollableService) iter.next();
                            // Skip service that was just polled
                            if (svc == pSvc)
                                continue;
                            svc.poll();
                        }
                    }
                }
            }
        }
        // Polling logic if interface is currently UP
        //
        else if (m_status == Pollable.STATUS_UP) {
            // Issue poll
            svcStatus = pSvc.poll();
            if (svcStatus == Pollable.STATUS_DOWN && pSvc.statusChanged()) {
                // If this is the only service supported by
                // the interface mark it as down
                if (m_services.size() == 1) {
                    m_status = Pollable.STATUS_DOWN;
                    m_statusChangedFlag = true;
                }
                // else if Critical service defined and supported by interface
                // (regardless of pkg)
                else if (criticalSvcName != null && this.supportsService(criticalSvcName)) {
                    // Retrieve critical service PollableService object
                    // and poll it.
                    if (log.isDebugEnabled())
                        log.debug("poll: status changed to DOWN, now polling critical svc...");
                    PollableService criticalSvc = this.getService(criticalSvcName);

                    // If the service we just polled WAS in fact the critical
                    // service
                    // then no need to poll it again...the interface is DOWN!
                    int criticalSvcStatus = Pollable.STATUS_UNKNOWN;
                    if (pSvc == criticalSvc) {
                        criticalSvcStatus = svcStatus;
                    } else {
                        criticalSvcStatus = criticalSvc.poll();
                    }

                    if (criticalSvcStatus == Pollable.STATUS_DOWN) {
                        if (log.isDebugEnabled())
                            log.debug("poll: critical svc DOWN, interface is down!");
                        m_status = Pollable.STATUS_DOWN;
                        m_statusChangedFlag = true;
                    } else {
                        if (log.isDebugEnabled())
                            log.debug("poll: critical svc UP, interface still up!");
                    }
                }
                // else if no critical service or critical service not supported
                else {
                    // Check flag which controls whether all services
                    // are polled if no critical service is defined
                    if (pollAllServices) {
                        // Poll all remaining services on the interface
                        // If all services are DOWN then mark the interface DOWN
                        boolean allSvcDown = true;
                        Iterator iter = m_services.values().iterator();

                        while (iter.hasNext()) {
                            PollableService tmpSvc = (PollableService) iter.next();
                            // Skip service that was already polled
                            if (tmpSvc == pSvc)
                                continue;
                            int tmpStatus = tmpSvc.poll();
                            if (tmpStatus == Pollable.STATUS_UP) {
                                allSvcDown = false;
                            }
                        }

                        if (allSvcDown) {
                            m_status = Pollable.STATUS_DOWN;
                            m_statusChangedFlag = true;
                        }
                    }
                }
            }
        }

        if (log.isDebugEnabled())
            log.debug("poll: poll of interface " + m_address.getHostAddress() + " completed, status= " + Pollable.statusType[m_status]);
        return m_status;
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
    public void setNode(PollableNode newPNode) {
        m_node = newPNode;
    }
}
