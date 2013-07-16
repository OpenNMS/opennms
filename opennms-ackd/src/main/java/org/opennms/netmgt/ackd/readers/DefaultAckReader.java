/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.ackd.readers;

import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.opennms.core.utils.BeanUtils;
import org.opennms.netmgt.ackd.AckReader;
import org.opennms.netmgt.dao.api.AckdConfigurationDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;


/**
 * Acknowledgment Reader implementation using Java Mail
 *
 * DONE: Identify acknowledgments for sent notifications
 * DONE: Identify acknowledgments for alarm IDs (how the send knows the ID, good question)
 * DONE: Persist acknowledgments
 * DONE: Identify escalation reply
 * DONE: Identify clear reply
 * DOND: Identify unacknowledged reply
 * DONE: Formalize Acknowledgment parameters (ack-type, id)
 * DONE: JavaMail configuration factory
 * DONE: Ackd configuration factory
 * TODO: Associate email replies with openNMS user
 * DONE: Finish scheduling component of JavaAckReader
 * DONE: Configurable Schedule
 * DONE: Identify Java Mail configuration element to use for reading replies
 * TODO: Migrate JavaMailNotificationStrategy to new JavaMail Configuration and JavaSendMailer
 * TODO: Migrate Availability Reports send via JavaMail to new JavaMail Configuration and JavaSendMailer
 * TODO: Move reading email messages from MTM to JavaReadMailer class
 * DONE: Need an event to cause re-loading of schedules based on changes to ackd-configuration
 * DONE: Do some proper logging
 * DONE: Handle "enabled" flag of the readers in ackd-configuration
 * DONE: Move executor to Ackd daemon
 *
 * @author <a href=mailto:david@opennms.org>David Hustace</a>
 * @version $Id: $
 */
public class DefaultAckReader implements AckReader, InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultAckReader.class);
    private volatile String m_name;

    private volatile Future<?> m_future;
    private AckProcessor m_ackProcessor;
    private ReaderSchedule m_schedule;

    private volatile AckReaderState m_state = AckReaderState.STOPPED;

    @Autowired
    private volatile AckdConfigurationDao m_ackdConfigDao;

    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
        boolean state = (m_ackProcessor != null);
        Assert.state(state, "Dependency injection failed; one or more fields are null.");
    }

    private synchronized void start(final ScheduledThreadPoolExecutor executor) {
        if (m_schedule == null) {
            m_schedule = ReaderSchedule.createSchedule();
        }
        this.start(executor, m_schedule, true);
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void start(final ScheduledThreadPoolExecutor executor, final ReaderSchedule schedule, boolean reloadConfig) throws IllegalStateException {
        if (reloadConfig) {
            //FIXME:The reload of JavaMailConfiguration is made here because the DAO is there. Perhaps that should be changed.
            LOG.info("start: reloading ack processor configuration...");
            m_ackProcessor.reloadConfigs();
            LOG.info("start: ack processor configuration reloaded.");
        }

        if (AckReaderState.STOPPED.equals(getState())) {
            this.setState(AckReaderState.START_PENDING);
            this.setSchedule(executor, schedule, false);
            LOG.info("start: Starting reader...");

            this.scheduleReads(executor);

            this.setState(AckReaderState.STARTED);
            LOG.info("start: Reader started.");
        } else {
            IllegalStateException e = new IllegalStateException("Reader is not in a stopped state.  Reader state is: "+getState());
            LOG.error("start error", e);
            throw e;
        }
    }

    /**
     * <p>pause</p>
     *
     * @throws java.lang.IllegalStateException if any.
     */
    @Override
    public synchronized void pause() throws IllegalStateException {
        if (AckReaderState.STARTED.equals(getState()) || AckReaderState.RESUMED.equals(getState())) {
            LOG.info("pause: lock acquired; pausing reader...");
            setState(AckReaderState.PAUSE_PENDING);

            if (m_future != null) {
                m_future.cancel(false);
                m_future = null;
            }

            setState(AckReaderState.PAUSED);
            LOG.info("pause: Reader paused.");
        } else {
            IllegalStateException e = new IllegalStateException("Reader is not in a running state (STARTED or RESUMED).  Reader state is: "+getState());
            LOG.error("pause error", e);
            throw e;
        }
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void resume(final ScheduledThreadPoolExecutor executor) throws IllegalStateException {
        if (AckReaderState.PAUSED.equals(getState())) {
            setState(AckReaderState.RESUME_PENDING);
            LOG.info("resume: lock acquired; resuming reader...");

            scheduleReads(executor);

            setState(AckReaderState.RESUMED);
            LOG.info("resume: reader resumed.");
        } else {
            IllegalStateException e = new IllegalStateException("Reader is not in a paused state, cannot resume.  Reader state is: "+getState());
            LOG.error("resume error", e);
            throw e;
        }
    }

    /**
     * <p>stop</p>
     *
     * @throws java.lang.IllegalStateException if any.
     */
    @Override
    public synchronized void stop() throws IllegalStateException {
        if (!AckReaderState.STOPPED.equals(getState())) {
            setState(AckReaderState.STOP_PENDING);
            LOG.info("stop: lock acquired; stopping reader...");

            if (m_future != null) {
                m_future.cancel(false);
                m_future = null;
            }

            setState(AckReaderState.STOPPED);
            LOG.info("stop: Reader stopped.");
        } else {
            IllegalStateException e = new IllegalStateException("Reader is already stopped.");
            LOG.error("stop error", e);
            throw e;
        }
    }

    private synchronized void scheduleReads(final ScheduledThreadPoolExecutor executor) {
        LOG.debug("scheduleReads: acquired lock, creating schedule...");

        executor.setContinueExistingPeriodicTasksAfterShutdownPolicy(false);
        executor.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        m_future = executor.scheduleWithFixedDelay(this.getAckProcessor(), getSchedule().getInitialDelay(), 
                getSchedule().getInterval(), getSchedule().getUnit());
        LOG.debug("scheduleReads: exited lock, schedule updated.");
        LOG.debug("scheduleReads: schedule is: attempts remaining: {}; initial delay: {}; interval: {}; unit: {}",
                  getSchedule().getAttemptsRemaining(),
                  getSchedule().getInitialDelay(),
                  getSchedule().getInterval(),
                  getSchedule().getUnit());

        LOG.debug("scheduleReads: executor details: active count: {}; completed task count: {}; task count: {}; queue size: {}", executor.getActiveCount(), executor.getCompletedTaskCount(), executor.getTaskCount(), executor.getQueue().size());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return getClass().getCanonicalName();
    }

    /** {@inheritDoc} */
    @Override
    public void setAckProcessor(AckProcessor ackProcessor) {
        m_ackProcessor = ackProcessor;
    }

    /**
     * <p>getAckProcessor</p>
     *
     * @return a {@link org.opennms.netmgt.ackd.readers.AckProcessor} object.
     */
    @Override
    public AckProcessor getAckProcessor() {
        return m_ackProcessor;
    }

    /**
     * <p>setAckdConfigDao</p>
     *
     * @param ackdConfigDao a {@link org.opennms.netmgt.dao.api.AckdConfigurationDao} object.
     */
    public void setAckdConfigDao(AckdConfigurationDao ackdConfigDao) {
        m_ackdConfigDao = ackdConfigDao;
    }

    /**
     * <p>getAckdConfigDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.AckdConfigurationDao} object.
     */
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

    /**
     * <p>getState</p>
     *
     * @return a AckReaderState object.
     */
    @Override
    public AckReaderState getState() {
        return m_state;
    }

    /**
     * <p>getFuture</p>
     *
     * @return a {@link java.util.concurrent.Future} object.
     */
    public Future<?> getFuture() {
        return m_future;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String getName() {
        return m_name;
    }

    /** {@inheritDoc} */
    @Override
    public synchronized void setName(String name) {
        m_name = name;
    }

}
