/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 7, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.ackd.readers;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.log4j.Logger;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ackd.AckReader;
import org.opennms.netmgt.dao.AckdConfigurationDao;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;


/**
 * Acknowledgment Reader implementation using Hyperic alert groovy servlet
 * 
 * TODO: Associate Hyperic acks with openNMS user
 * 
 * @author <a href=mailto:david@opennms.org>David Hustace</a>
 * 
 */
public class HypericAckReader implements AckReader, InitializingBean {

    private static final String NAME="HypericAckReader";

    private volatile Future<?> m_future;
    private AckProcessor m_ackProcessor = new AckProcessor() {
        public void afterPropertiesSet() throws Exception {
        }

        public void run() {
            // Parse Hyperic alert data
        }

        public void reloadConfigs() {
        }
    };
    private ReaderSchedule m_schedule;

    private volatile AckReaderState m_state = AckReaderState.STOPPED;

    @Autowired
    private volatile AckdConfigurationDao m_ackdConfigDao;

    public void afterPropertiesSet() throws Exception {
        boolean state = (m_ackProcessor != null);
        Assert.state(state, "Dependency injection failed; one or more fields are null.");
    }

    private synchronized void start(final ScheduledThreadPoolExecutor executor) {
        if (m_schedule == null) {
            m_schedule = ReaderSchedule.createSchedule();
        }
        this.start(executor, m_schedule, true);
    }

    public synchronized void start(final ScheduledThreadPoolExecutor executor, final ReaderSchedule schedule, boolean reloadConfig) throws IllegalStateException {
        if (reloadConfig) {
            //FIXME:The reload of JavaMailConfiguration is made here because the DAO is there. Perhaps that should be changed.
            log().info("start: reloading ack processor configuration...");
            m_ackProcessor.reloadConfigs();
            log().info("start: ack processor configuration reloaded.");
        }

        if (AckReaderState.STOPPED.equals(getState())) {
            this.setState(AckReaderState.START_PENDING);
            this.setSchedule(executor, schedule, false);
            log().info("start: Starting reader...");

            this.scheduleReads(executor);

            this.setState(AckReaderState.STARTED);
            log().info("start: Reader started.");
        } else {
            IllegalStateException e = new IllegalStateException("Reader is not in a stopped state.  Reader state is: "+getState());
            log().error("start: "+e, e);
            throw e;
        }
    }

    public synchronized void pause() throws IllegalStateException {
        if (AckReaderState.STARTED.equals(getState()) || AckReaderState.RESUMED.equals(getState())) {
            log().info("pause: lock acquired; pausing reader...");
            setState(AckReaderState.PAUSE_PENDING);

            if (m_future != null) {
                m_future.cancel(false);
                m_future = null;
            }

            setState(AckReaderState.PAUSED);
            log().info("pause: Reader paused.");
        } else {
            IllegalStateException e = new IllegalStateException("Reader is not in a running state (STARTED or RESUMED).  Reader state is: "+getState());
            log().error("pause: "+e, e);
            throw e;
        }
    }

    public synchronized void resume(final ScheduledThreadPoolExecutor executor) throws IllegalStateException {
        if (AckReaderState.PAUSED.equals(getState())) {
            setState(AckReaderState.RESUME_PENDING);
            log().info("resume: lock acquired; resuming reader...");

            scheduleReads(executor);

            setState(AckReaderState.RESUMED);
            log().info("resume: reader resumed.");
        } else {
            IllegalStateException e = new IllegalStateException("Reader is not in a paused state, cannot resume.  Reader state is: "+getState());
            log().error("resume: "+e, e);
            throw e;
        }
    }

    public synchronized void stop() throws IllegalStateException {
        if (!AckReaderState.STOPPED.equals(getState())) {
            setState(AckReaderState.STOP_PENDING);
            log().info("stop: lock acquired; stopping reader...");

            if (m_future != null) {
                m_future.cancel(false);
                m_future = null;
            }

            setState(AckReaderState.STOPPED);
            log().info("stop: Reader stopped.");
        } else {
            IllegalStateException e = new IllegalStateException("Reader is already stopped.");
            log().error("stop: "+e, e);
            throw e;
        }
    }

    private synchronized void scheduleReads(final ScheduledThreadPoolExecutor executor) {
        log().debug("scheduleReads: acquired lock, creating schedule...");

        executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        m_future = executor.scheduleWithFixedDelay(
                this.getAckProcessor(),
                getSchedule().getInitialDelay(), 
                getSchedule().getInterval(), 
                getSchedule().getUnit()
        );
        log().debug("scheduleReads: exited lock, schedule updated.");
        log().debug("scheduleReads: schedule is:" +
                " attempts remaining: "+getSchedule().getAttemptsRemaining()+
                "; initial delay: "+getSchedule().getInitialDelay()+
                "; interval: "+getSchedule().getInterval()+
                "; unit: "+getSchedule().getUnit());

        log().debug("scheduleReads: executor details:"+
                " active count: "+executor.getActiveCount()+
                "; completed task count: "+executor.getCompletedTaskCount()+
                "; task count: "+executor.getTaskCount()+
                "; queue size: "+executor.getQueue().size());
    }

    private Logger log() {
        return ThreadCategory.getInstance(this.getClass());
    }

    @Override
    public String toString() {
        return getClass().getCanonicalName();
    }

    public void setAckProcessor(AckProcessor ackProcessor) {
        m_ackProcessor = ackProcessor;
    }

    public AckProcessor getAckProcessor() {
        return m_ackProcessor;
    }

    public String getName() {
        return NAME;
    }

    public void setAckdConfigDao(AckdConfigurationDao ackdConfigDao) {
        m_ackdConfigDao = ackdConfigDao;
    }

    public AckdConfigurationDao getAckdConfigDao() {
        return m_ackdConfigDao;
    }

    /**
     * Gets a new schedule and optionally reschedules <code>MailAckProcessor</code>
     * @param schedule
     * @param reschedule
     */
    private synchronized void setSchedule(final ScheduledThreadPoolExecutor executor, ReaderSchedule schedule, boolean reschedule) {
        m_schedule = schedule;

        if (reschedule) {
            stop();
            start(executor);
        }
    }

    private ReaderSchedule getSchedule() {
        if (m_schedule == null) {
            m_schedule = ReaderSchedule.createSchedule();
        }
        return m_schedule;
    }

    /**
     * Anything calling this method should already have the lock.
     * 
     * @param state
     */
    private synchronized void setState(AckReaderState state) {
        m_state = state;
    }

    public AckReaderState getState() {
        return m_state;
    }

    public Future<?> getFuture() {
        return m_future;
    }
}
