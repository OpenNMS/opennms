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
package org.opennms.netmgt.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class Schedulable implements ReadyRunnable {

    private static final Logger LOG = LoggerFactory.getLogger(Schedulable.class);

    /**
     * The scheduler object
     */
    private LegacyScheduler m_scheduler;

    /**
     * The interval, default value 30 minutes
     */
    private long m_poll_interval = 1800000;
    /**
     * The initial sleep time, default value 10 minutes
     */
    private long m_initial_sleep_time = 600000;

    private boolean m_suspendCollection = false;
    private boolean m_unschedule = false;
    
    /**
     * Constructs a new Schedulable
     *
     * @param interval the time in msec between collections
     * @param initial the time in msec wait before performing a collection at all
     *
     */
    public Schedulable(long interval, long initial) {
        m_poll_interval = interval;
        m_initial_sleep_time = initial;
    }

    public Schedulable() {
    }

    public abstract String getName();
    public abstract void runSchedulable();
    
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
        runSchedulable();
        reschedule();
    }

    public LegacyScheduler getScheduler() {
        return m_scheduler;
    }

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
            Schedulable other = (Schedulable) obj;
            if (m_initial_sleep_time != other.m_initial_sleep_time)
                    return false;
        return m_poll_interval == other.m_poll_interval;
    }

}
