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

package org.opennms.netmgt.enlinkd.common;

import org.opennms.netmgt.scheduler.LegacyScheduler;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is designed to collect the necessary SNMP information from the
 * target address and store the collected information. When the class is
 * initially constructed no information is collected. The SNMP Session
 * creating and collection occurs in the main run method of the instance. This
 * allows the collection to occur in a thread if necessary.
 */
public abstract class Discovery implements ReadyRunnable {

    private static final Logger LOG = LoggerFactory.getLogger(Discovery.class);

    /**
     * The scheduler object
     */
    private LegacyScheduler m_scheduler;

    /**
     * The interval, default value 30 minutes
     */
    private long m_poll_interval = 1800000;
    private long m_initial_sleep_time = 600000;

    /**
     * The initial sleep time, default value 5 minutes
     */

    private boolean m_suspendCollection = false;
    private boolean m_unschedule = false;
    
    /**
     * Constructs a new SNMP collector for a node using the passed interface
     * as the collection point. The collection does not occur until the
     * <code>run</code> method is invoked.
     * 
     * @param nodeid
     * @param config
     *            The SnmpPeer object to collect from.
     */
    public Discovery(long interval, long initial) {
        m_poll_interval = interval;
        m_initial_sleep_time = initial;
    }

    public Discovery() {
    }

    public abstract String getName();
    public abstract void runDiscovery();
    
    // run is called by a Thread for the runnable
    // execute is where you got the stuff made
    public void run() {
        //if unscheduled return
        // not scheduling itself anymore
        if (m_unschedule) {
            LOG.info( "run: unscheduled {}", 
                      getInfo());
            return;
        }
        //if collection is suspended then
        // schedule the collection
        if (m_suspendCollection) {
            LOG.info( "run: suspended {}", 
                      getInfo());
            schedule();
            return;
        }
        LOG.info( "run: running {}", 
                      getInfo());
        runDiscovery();            
        reschedule();
    }
    /**
     * <p>
     * getScheduler
     * </p>
     * 
     * @return a {@link org.opennms.netmgt.enlinkd.scheduler.Scheduler} object.
     */
    public LegacyScheduler getScheduler() {
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
    public void setScheduler(LegacyScheduler scheduler) {
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
	 * 
	 */
    private void reschedule() {
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

    public void unschedule() {
        m_unschedule = true;
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
        return  getName() + " initial:" + m_initial_sleep_time + " interval:" + m_poll_interval;  
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

}
