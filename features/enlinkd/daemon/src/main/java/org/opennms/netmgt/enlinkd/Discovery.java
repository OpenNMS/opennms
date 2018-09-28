/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd;

import org.opennms.netmgt.enlinkd.scheduler.ReadyRunnable;
import org.opennms.netmgt.enlinkd.scheduler.Scheduler;
import org.opennms.netmgt.model.events.EventBuilder;

/**
 * This class is designed to collect the necessary SNMP information from the
 * target address and store the collected information. When the class is
 * initially constructed no information is collected. The SNMP Session
 * creating and collection occurs in the main run method of the instance. This
 * allows the collection to occur in a thread if necessary.
 */
public abstract class Discovery implements ReadyRunnable {

    /**
     * The scheduler object
     */
    private Scheduler m_scheduler;

    /**
     * The interval, default value 30 minutes
     */
    protected long m_poll_interval = 1800000;
    protected long m_initial_sleep_time = 600000;
    private boolean m_runned = false;

    /**
     * The initial sleep time, default value 5 minutes
     */

    protected boolean m_suspendCollection = false;

    protected final EnhancedLinkd m_linkd;
    
    /**
     * Constructs a new SNMP collector for a node using the passed interface
     * as the collection point. The collection does not occur until the
     * <code>run</code> method is invoked.
     * 
     * @param nodeid
     * @param config
     *            The SnmpPeer object to collect from.
     */
    public Discovery(final EnhancedLinkd linkd, long interval, long initial) {
        m_linkd = linkd;
        m_poll_interval = interval;
        m_initial_sleep_time = initial;
   }

    public abstract String getName();
    public abstract void runDiscovery();
    
    // run is called by a Thread for the runnable
    // execute is where you got the stuff made
    public void run() {
        runDiscovery();
        m_runned = true;
        reschedule();
    }
    /**
     * <p>
     * getScheduler
     * </p>
     * 
     * @return a {@link org.opennms.netmgt.enlinkd.scheduler.Scheduler} object.
     */
    public Scheduler getScheduler() {
        return m_scheduler;
    }

    /**
     * <p>
     * setScheduler
     * </p>
     * 
     * @param scheduler
     *            a {@link org.opennms.netmgt.enlinkd.scheduler.Scheduler}
     *            object.
     */
    public void setScheduler(Scheduler scheduler) {
        m_scheduler = scheduler;
    }

    /**
     * <p>
     * schedule
     * </p>
     */
    public void schedule() {
        if (m_scheduler == null)
            throw new IllegalStateException(
                                            "Cannot schedule a service whose scheduler is set to null");
        m_scheduler.schedule(m_initial_sleep_time, this);
    }

    /**
     * <p>
     * unschedule
     * </p>
     */
    public void unschedule() {
        if (m_scheduler == null)
            throw new IllegalStateException(
                                            "rescedule: Cannot schedule a service whose scheduler is set to null");
        if (m_runned) {
            m_scheduler.unschedule(this, m_poll_interval);
        } else {
            m_scheduler.unschedule(this, m_initial_sleep_time);
        }
    }

    /**
	 * 
	 */
    public void reschedule() {
        if (m_scheduler == null)
            throw new IllegalStateException(
                                            "Cannot schedule a service whose scheduler is set to null");
        m_scheduler.schedule(m_poll_interval, this);
    }

    /**
     * <p>
     * isReady
     * </p>
     * 
     * @return a boolean.
     */
    public boolean isReady() {
        return true;
    }

    /**
     * <p>
     * isSuspended
     * </p>
     * 
     * @return Returns the suspendCollection.
     */
    public boolean isSuspended() {
        return m_suspendCollection;
    }

    /**
     * <p>
     * suspend
     * </p>
     */
    public void suspend() {
        m_suspendCollection = true;
    }

    /**
     * <p>
     * wakeUp
     * </p>
     */
    public void wakeUp() {
        m_suspendCollection = false;
    }

    /**
     * <p>
     * getInfo
     * </p>
     * 
     * @return a {@link java.lang.String} object.
     */
    public String getInfo() {
        return  getName();  
    }

    /**
     * <p>
     * getPollInterval
     * </p>
     * 
     * @return Returns the initial_sleep_time.
     */
    public long getPollInterval() {
        return m_poll_interval;
    }

    /**
     * <p>
     * setPollInterval
     * </p>
     * 
     * @param interval
     *            a long.
     */
    public void setPollInterval(long interval) {
        m_poll_interval = interval;
    }

    /**
     * <p>
     * getInitialSleepTime
     * </p>
     * 
     * @return Returns the initial_sleep_time.
     */
    public long getInitialSleepTime() {
        return m_initial_sleep_time;
    }

    /**
     * <p>
     * setInitialSleepTime
     * </p>
     * 
     * @param initial_sleep_time
     *            The initial_sleep_timeto set.
     */
    public void setInitialSleepTime(long initial_sleep_time) {
        m_initial_sleep_time = initial_sleep_time;
    }

    @Override
    public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                            + (int) (m_initial_sleep_time ^ (m_initial_sleep_time >>> 32));
            result = prime * result
                            + (int) (m_poll_interval ^ (m_poll_interval >>> 32));
            return result;
    }

    @Override
    public boolean equals(Object obj) {
            if (this == obj)
                    return true;
            if (obj == null)
                    return false;
            if (getClass() != obj.getClass())
                    return false;
            Discovery other = (Discovery) obj;
            if (m_initial_sleep_time != other.m_initial_sleep_time)
                    return false;
            if (m_poll_interval != other.m_poll_interval)
                    return false;
            return true;
    }

    protected void sendSuspendedEvent(int nodeid) {
        EventBuilder builder = new EventBuilder(
                                   "uei.opennms.org/internal/linkd/nodeLinkDiscoverySuspended",
                                   "EnhancedLinkd");
                           builder.setNodeid(nodeid);
                           builder.addParam("runnable", getName());
       m_linkd.getEventForwarder().sendNow(builder.getEvent());
    }
    
    protected void sendStartEvent(int nodeid) {
        EventBuilder builder = new EventBuilder(
                                   "uei.opennms.org/internal/linkd/nodeLinkDiscoveryStarted",
                                   "EnhancedLinkd");
                           builder.setNodeid(nodeid);
                           builder.addParam("runnable", getName());
                           m_linkd.getEventForwarder().sendNow(builder.getEvent());
        
    }
    
    protected void sendCompletedEvent(int nodeid) {
        EventBuilder builder = new EventBuilder(
                                   "uei.opennms.org/internal/linkd/nodeLinkDiscoveryCompleted",
                                   "EnhancedLinkd");
                           builder.setNodeid(nodeid);
                           builder.addParam("runnable", getName());
                           m_linkd.getEventForwarder().sendNow(builder.getEvent());
    }


}
