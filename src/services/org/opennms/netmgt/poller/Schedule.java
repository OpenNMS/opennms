//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2004 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
// OpenNMS Licensing       <license@opennms.org>
//     http://www.opennms.org/
//     http://www.opennms.com/
//
package org.opennms.netmgt.poller;

import java.util.Enumeration;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Downtime;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Scheduler;


/**
 * Represents a Poll of a single service.  
 * @author brozow
 */
public class Schedule implements ReadyRunnable {

    PollableService m_svc;
    
    /**
     * The last time the service was scheduled for a poll.
     */
    private long m_lastScheduledPoll = 0L;

    /**
     * This was the interval to use when the node was last rescheduled. This
     * must be used or it could block a queue! (i.e. its ready time gets longer
     * while the elements behind it are ready to go!)
     */
    private long m_lastInterval = 0L;

    /**
     * The last time the service was polled...whether due to a scheduled poll or
     * node outage processing.
     */
    private long m_lastPoll;
    
    /**
     * Set to true when service is first constructed which will cause the
     * recalculateInterval() method to return 0 resulting in an immediate poll.
     */
    private boolean m_pollImmediate = true;
    private ServiceConfig m_svcConfig;
    private Scheduler m_scheduler;



    public Schedule(PollableService svc, ServiceConfig config) {
        m_svc = svc;
        m_svcConfig = config;
        m_scheduler = m_svcConfig.getPoller().getScheduler();
    }
    
    public ServiceConfig getServiceConfig() {
        return m_svcConfig;
    }

    Scheduler getScheduler() {
        return m_scheduler;
    }
    /**
     * @param lastScheduledPoll
     */
    public void setLastScheduledPoll(long lastScheduledPoll) {
        m_lastScheduledPoll = lastScheduledPoll;
    }

    /**
     * @return
     */
    public long getLastScheduledPoll() {
        return m_lastScheduledPoll;
    }

    /**
     * @return
     */
    public boolean isReady() {
        long when = getLastInterval();
        boolean ready = false;
    
        if (when < 1) {
            ready = true;
        } else {
            ready = ((when - (System.currentTimeMillis() - getLastScheduledPoll())) < 1);
        }
    
        return ready;
    }

    public void run() {
        Category log = ThreadCategory.getInstance(m_svc.getClass());
        
        try {
            runAndReschedule();
        } catch (LockUnavailableException e) {
            // failed to acquire lock, just reschedule on 10 second queue
            if (log.isDebugEnabled())
                log.debug("Lock unavailable, rescheduling on 10 sec queue, reason: " + e.getMessage());
            reschedule(10000);
        } catch (InterruptedException e) {
            // The thread was interrupted; reschedule on 10 second queue
            if (log.isDebugEnabled())
                log.debug(e);
            reschedule(10000);
        }
    }
    
    private void runAndReschedule() throws InterruptedException {
        doRun(true);
    }

    public void runButDontReschedule(boolean resched) throws LockUnavailableException, InterruptedException {
        doRun(false);
    }

    /**
     * @param interval
     */
    public void schedulePoll(long interval) {
        m_scheduler.schedule(this, interval);
    }

    /**
     * @param pollImmediate
     */
    public void setPollImmediate(boolean pollImmediate) {
        m_pollImmediate = pollImmediate;
    }

    /**
     * @return
     */
    public boolean isPollImmediate() {
        return m_pollImmediate;
    }

    /**
     * @return
     */
    long recalculateInterval() {
        Category log = ThreadCategory.getInstance(getClass());
    
        // If poll immediate flag is set the service hasn't
        // been polled yet. Return 0 to cause an immediate
        // poll of the interface.
        if (isPollImmediate()) {
            return 0;
        }
    
        long when = getServiceConfig().getService().getInterval();
        long downSince = 0;
        if (m_svc.getStatus() == ServiceMonitor.SERVICE_UNAVAILABLE)
            downSince = System.currentTimeMillis() - m_svc.getStatusChangeTime();
    
        if (log.isDebugEnabled())
            log.debug("recalculateInterval for " + m_svc + " : " + " status= " + Pollable.statusType[m_svc.getStatus()] + " downSince= " + downSince);
    
        switch (m_svc.getStatus()) {
        case ServiceMonitor.SERVICE_AVAILABLE:
            break;
    
        case ServiceMonitor.SERVICE_UNAVAILABLE:
            boolean matched = false;
            Enumeration edowntime = m_svcConfig.getPackage().enumerateDowntime();
            while (edowntime.hasMoreElements()) {
                Downtime dt = (Downtime) edowntime.nextElement();
                if (dt.getBegin() <= downSince) {
                    if (dt.getDelete() != null && (dt.getDelete().equals("yes") || dt.getDelete().equals("true"))) {
                        when = -1;
                        matched = true;
                    }
                    // FIXME: the below is a subtle bug... should be downSince
                    // not m_statusChangeTime it is masked by the fact we go thru to 
                    // loop more than once and reset the values
                    else if (dt.hasEnd() && dt.getEnd() > m_svc.getStatusChangeTime()) {
                        // in this interval
                        //
                        when = dt.getInterval();
                        matched = true;
                    } else // no end
                    {
                        when = dt.getInterval();
                        matched = true;
                    }
                }
            }
            if (!matched) {
                log.warn("recalculateInterval: Could not locate downtime model, throwing runtime exception");
                throw new RuntimeException("Downtime model is invalid, cannot schedule service " + m_svc);
            }
    
            break;
    
        default:
            log.warn("recalculateInterval: invalid status found, downtime model lookup failed. throwing runtime exception");
            throw new RuntimeException("Invalid Polling Status for service " + m_svc + ", status = " + m_svc.getStatus());
    
        } // end switch()
    
        if (log.isDebugEnabled())
            log.debug("recalculateInterval: new scheduling interval for " + m_svc + " = " + when);
        return when;
    }

    /**
     * @param lastInterval
     */
    public void setLastInterval(long lastInterval) {
        m_lastInterval = lastInterval;
    }

    /**
     * @return
     */
    public long getLastInterval() {
        return m_lastInterval;
    }

    /**
     * @param interval
     */
    void reschedule(long interval) {
        
        // Update m_lastInterval
        // 
        // NOTE: Never want to reschedule at less than 1 milliscond interval
        //
        if (interval <= 0) {
            setLastInterval(getServiceConfig().getService().getInterval());
        }
        else {
            setLastInterval(interval);
        }
    
        // Reschedule the service
        schedulePoll(interval);
    }

    /**
     * @param reuseInterval
     * @param svc
     */
    void reschedule(boolean reuseInterval) {
        // Determine interval at which to reschedule the interface
        // 
        long interval = 0L;
    
        if (reuseInterval) {
            interval = getLastInterval();
        } else {
            // Recalculate polling interval
            // 
            // NOTE: interval of -1 indicates interface/service
            // pair has exceeded the downtime model and
            // is to be deleted.
            interval = recalculateInterval();
    
            if (interval < 0) {
                m_svc.deleteService();
    
                return; // Return without rescheduling
            } // end delete event
        }
        reschedule(interval);
    }


    public String toString() {
        return m_svc + m_svcConfig.getPackageName();
    }

    /**
     * @param service
     */
    void adjustSchedule() {
        long runAt = recalculateInterval() + System.currentTimeMillis();
        getScheduler().schedule(new ScheduleProxy(this, runAt), recalculateInterval());
        ThreadCategory.getInstance().debug("poll: scheduling new PollableServiceProxy for " + m_svc + " at interval= " + recalculateInterval());
    }

    /**
     * @return
     */
    public long getScheduledRuntime() {
        return (getLastPoll() + getLastInterval());
    }

    /**
     * @param allowedToRescheduleMyself
     * @param m_svc
     * @throws LockUnavailableException
     * @throws InterruptedException
     */
    void doRun(boolean allowedToRescheduleMyself) throws LockUnavailableException, InterruptedException {
        // Update last scheduled poll time if allowedToRescheduleMyself
        // flag is true
        if (allowedToRescheduleMyself)
            setLastScheduledPoll(System.currentTimeMillis());
        
        Category log = ThreadCategory.getInstance(m_svc.getClass());
        
        // Is the service marked for deletion? If so simply return.
        //
        if (m_svc.isDeleted()) {
            if (log.isDebugEnabled()) {
                log.debug("PollableService doRun: Skipping service marked as deleted on " + m_svc + ", status = " + m_svc.getStatus());
            }
        } else {
            
            // Check scheduled outages to see if any apply indicating
            // that the poll should be skipped
            //
            if (getServiceConfig().scheduledOutage(m_svc)) {
                // Outage applied...reschedule the service and return
                if (allowedToRescheduleMyself) {
                    reschedule(true);
                }
                
            } else {
                
                doPoll();
                setPollImmediate(false);

                // reschedule the service for polling
                if (allowedToRescheduleMyself) {
                    reschedule(false);
                }
                
            }
        }
    }

    /**
     * 
     */
    public void schedulePoll() {
        schedulePoll(recalculateInterval());
    }

    void doPoll() throws InterruptedException {
        Category log = ThreadCategory.getInstance(m_svc.getClass());
        // NodeId
        PollableNode pNode = m_svc.getNode();
        int nodeId = pNode.getNodeId();
    
        // Is node outage processing enabled?
        if (getServiceConfig().getPollerConfig().nodeOutageProcessingEnabled()) {
    
            /*
             * Acquire lock to 'PollableNode'
             */
            boolean ownLock = false;
            try {
                // Attempt to obtain node lock...wait no longer than 500ms
                // We don't want to tie up the thread for long periods of time
                // waiting for the lock on the PollableNode to be released.
                if (log.isDebugEnabled())
                    log.debug("run: ------------- requesting node lock for nodeid: " + nodeId + " -----------");
    
                if (!(ownLock = pNode.getNodeLock(500)))
                    throw new LockUnavailableException("failed to obtain lock on nodeId " + nodeId);
            } catch (InterruptedException iE) {
                // failed to acquire lock
                throw new InterruptedException("failed to obtain lock on nodeId " + nodeId + ": " + iE.getMessage());
            }
            // Now we have a lock
    
            if (ownLock) // This is probably redundant, but better to be
                            // sure.
            {
                try {
                    // Make sure the node hasn't been deleted.
                    if (!pNode.isDeleted()) {
                        if (log.isDebugEnabled())
                            log.debug("run: calling poll() for " + m_svc);
    
                        pNode.poll(m_svc);
    
                        if (log.isDebugEnabled())
                            log.debug("run: call to poll() finished for " + m_svc);
                    }
                } finally {
                    if (log.isDebugEnabled())
                        log.debug("run: ----------- releasing node lock for nodeid: " + nodeId + " ----------");
                    try {
                        pNode.releaseNodeLock();
                    } catch (InterruptedException iE) {
                        log.error("run: thread interrupted...failed to release lock on nodeId " + nodeId);
                    }
                }
            }
        } else {
            // Node outage processing disabled so simply poll the service
            if (log.isDebugEnabled())
                log.debug("run: node outage processing disabled, polling: " + m_svc);
            m_svc.poll();
        }
        
        
    }

    public String getPackageName() {
        return m_svcConfig.getPackageName();
    }
    
    public String getServiceName() {
        return m_svcConfig.getServiceName();
    }
    
    public Package getPackage() {
        return m_svcConfig.getPackage();
    }
    
    public Map getPropertyMap() {
        return m_svcConfig.getPropertyMap();
    }
    
    public PollerConfig getPollerConfig() {
        return m_svcConfig.getPollerConfig();
    }
    
    public ServiceMonitor getServiceMonitor() {
        return m_svcConfig.getServiceMonitor();
    }
    
    public long getLastPoll() {
        return m_lastPoll;
    }
    public void setLastPoll(long lastPoll) {
        m_lastPoll = lastPoll;
    }

    public int poll() {
        Category log = ThreadCategory.getInstance(m_svc.getClass());
    
        setLastPoll(System.currentTimeMillis());
        m_svc.resetStatusChanged();
        if (log.isDebugEnabled())
            log.debug("poll: starting new poll for " + m_svc + ":" + getPackageName());
    
        // Poll the interface/service pair via the service monitor
        //
        int status = ServiceMonitor.SERVICE_UNAVAILABLE;
        Map propertiesMap = getPropertyMap();
        try {
            status = getServiceMonitor().poll(m_svc.getNetInterface(), propertiesMap, getPackage());
            if (log.isDebugEnabled())
                log.debug("poll: polled for " + m_svc + ":" + getPackageName()+" with result: " + Pollable.statusType[status]);
        } catch (NetworkInterfaceNotSupportedException ex) {
            log.error("poll: Interface " + m_svc.getIpAddr() + " Not Supported!", ex);
        } catch (Throwable t) {
            log.error("poll: An undeclared throwable was caught polling interface " + m_svc.getIpAddr(), t);
        }
    
        // serviceUnresponsive behavior disabled?
        //
        if (!getPollerConfig().serviceUnresponsiveEnabled()) {
            // serviceUnresponsive behavior is disabled, a status
            // of SERVICE_UNRESPONSIVE is treated as SERVICE_UNAVAILABLE
            if (status == ServiceMonitor.SERVICE_UNRESPONSIVE)
                status = ServiceMonitor.SERVICE_UNAVAILABLE;
        } else {
            // Update unresponsive flag based on latest status
            // returned by the monitor and generate serviceUnresponsive
            // or serviceResponsive event if necessary.
            //
            switch (status) {
            case ServiceMonitor.SERVICE_UNRESPONSIVE:
                // Check unresponsive flag to determine if we need
                // to generate a 'serviceUnresponsive' event.
                //
                if (!m_svc.isUnresponsive()) {
                    m_svc.setUnresponsive(true);
                    m_svc.sendEvent(EventConstants.SERVICE_UNRESPONSIVE_EVENT_UEI, propertiesMap);
    
                    // Set status back to available, don't want unresponsive
                    // service to generate outage
                    status = ServiceMonitor.SERVICE_AVAILABLE;
                }
                break;
    
            case ServiceMonitor.SERVICE_AVAILABLE:
                // Check unresponsive flag to determine if we
                // need to generate a 'serviceResponsive' event
                if (m_svc.isUnresponsive()) {
                    m_svc.setUnresponsive(false);
                    m_svc.sendEvent(EventConstants.SERVICE_RESPONSIVE_EVENT_UEI, propertiesMap);
                }
                break;
    
            case ServiceMonitor.SERVICE_UNAVAILABLE:
                // Clear unresponsive flag
                m_svc.setUnresponsive(false);
                break;
    
            default:
                break;
            }
        }
    
        // Any change in status?
        //
        if (status != m_svc.getStatus()) {
            // get the time of the status change
            //
            m_svc.setStatusChanged();
            m_svc.setStatusChangeTime(System.currentTimeMillis());
    
            // Is node outage processing disabled?
            if (!getPollerConfig().nodeOutageProcessingEnabled()) {
                // node outage processing disabled, go ahead and generate
                // transition events.
                if (log.isDebugEnabled())
                    log.debug("poll: node outage disabled, status change will trigger event.");
    
                // Send the appropriate event
                //
                switch (status) {
                case ServiceMonitor.SERVICE_AVAILABLE: // service up!
                    m_svc.sendEvent(EventConstants.NODE_REGAINED_SERVICE_EVENT_UEI, propertiesMap);
                    break;
    
                case ServiceMonitor.SERVICE_UNAVAILABLE: // service down!
                    m_svc.sendEvent(EventConstants.NODE_LOST_SERVICE_EVENT_UEI, propertiesMap);
                    break;
    
                default:
                    break;
                }
            }
        }
    
        // Set status
        m_svc.setStatus(status);
    
        // Reschedule the interface
        // 
        // NOTE: rescheduling now handled by PollableService.run()
        // reschedule(false);
    
        return m_svc.getStatus();
    }
}
