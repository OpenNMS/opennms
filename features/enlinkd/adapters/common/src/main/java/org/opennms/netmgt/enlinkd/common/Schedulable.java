/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

public abstract class Schedulable implements ReadyRunnable {

    private static final Logger LOG = LoggerFactory.getLogger(Schedulable.class);
    /**
     * The scheduler object
     * This will schedule Schedulable
     *
     */
    private LegacyScheduler m_scheduler;

    /**
     * The interval, default value 30 minutes
     */
    private long m_poll_interval = 1800000;

    /**
     * The initial sleep time, default value 5 minutes
     */
    private long m_initial_sleep_time = 600000;

    private boolean m_unschedule = false;


    public long getPollInterval() {
        return m_poll_interval;
    }

    public void setPollInterval(long m_poll_interval) {
        this.m_poll_interval = m_poll_interval;
    }

    public long getInitialSleepTime() {
        return m_initial_sleep_time;
    }

    public void setInitialSleepTime(long m_initial_sleep_time) {
        this.m_initial_sleep_time = m_initial_sleep_time;
    }

    /**
     * Constructs a new Schedulable.
     *
     * @param interval the time in msec between group of collections
     * @param initial the time in msec wait before performing a collection at all
     */
    public Schedulable(long interval, long initial) {
        m_poll_interval = interval;
        m_initial_sleep_time = initial;
    }

    public Schedulable() {
    }

    public abstract String getName();
    public abstract void runSchedulable();

    @Override
    public void run() {
        //if unscheduled return
        // not scheduling itself anymore
        if (m_unschedule) {
            LOG.info( "run: unscheduled {}",
                    getInfo());
            return;
        }
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
        m_unschedule=false;
        if (m_scheduler == null)
            throw new IllegalStateException(
                    "Cannot schedule a service whose scheduler is set to null");
        m_scheduler.schedule(m_initial_sleep_time, this);
    }

    public void unschedule() {
        m_unschedule=true;
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
    @Override
    public boolean isReady() {
        return true;
    }

    /**
     * <p>
     * getInfo
     * </p>
     *
     * @return a {@link String} object.
     */
    public String getInfo() {
        return  getName() + " initial:" + m_initial_sleep_time + " interval:" + m_poll_interval;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Schedulable that = (Schedulable) o;

        if (m_poll_interval != that.m_poll_interval) return false;
        if (m_initial_sleep_time != that.m_initial_sleep_time) return false;
        return getName().equals(that.getName());
    }

    @Override
    public int hashCode() {
        int result = getName().hashCode();
        result = 31 * result + (int) (m_poll_interval ^ (m_poll_interval >>> 32));
        result = 31 * result + (int) (m_initial_sleep_time ^ (m_initial_sleep_time >>> 32));
        return result;
    }
}
