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

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.poller.Downtime;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Scheduler;


/**
 * Represents a Poll of a single service.  
 * @author brozow
 */
public class Schedule implements ReadyRunnable {

    private PollableService m_svc;
    
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
     * Set to true when service is first constructed which will cause the
     * recalculateInterval() method to return 0 resulting in an immediate poll.
     */
    private boolean m_pollImmediate = true;
    private ServiceConfig m_svcConfig;
    private Scheduler m_scheduler;



    public Schedule(PollableService svc, Scheduler scheduler, ServiceConfig config) {
        m_scheduler = scheduler;
        m_svc = svc;
        m_svcConfig = config;
    }
    
    public ServiceConfig getServiceConfig() {
        return m_svcConfig;
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
            m_svc.doRun(true);
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

}
