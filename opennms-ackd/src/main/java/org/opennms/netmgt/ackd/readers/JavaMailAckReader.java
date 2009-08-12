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

import org.apache.log4j.Logger;
import org.opennms.core.concurrent.PausibleScheduledThreadPoolExecutor;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ackd.AckReader;
import org.opennms.netmgt.dao.AckdConfigurationDao;
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
 * TODO: Move reading email messages from MTM and this class to JavaReadMailer class
 * DONE: Need an event to cause re-loading of schedules based on changes to ackd-configuration
 * TODO: Need an opennms.property or flag in each config to control auto reloading of configurations in new ConfigDaos
 * DONE: Do some proper logging
 * DONE: Handle "enabled" flag of the readers in ackd-configuration
 * 
 * 
 * @author <a href=mailto:david@opennms.org>David Hustace</a>
 * 
 */
public class JavaMailAckReader implements AckReader, InitializingBean {

    private static final String NAME="JavaMailReader";
    
    private Object m_lock = new Object();
    private PausibleScheduledThreadPoolExecutor m_executor;
    private MailAckProcessor m_mailAckProcessor;
    private ReaderSchedule m_schedule;
    
    private AckReaderState m_state = AckReaderState.STOPPED;
    
    @Autowired
    private AckdConfigurationDao m_ackdConfigDao;
    
    public void afterPropertiesSet() throws Exception {
        boolean state = (m_executor != null && m_mailAckProcessor != null);
        Assert.state(state, "Dependency injection failed; one or more fields are null.");
    }
    
    public void start(ReaderSchedule schedule) throws IllegalStateException {
        synchronized (m_lock) {
            if (AckReaderState.STOPPED.equals(getState())) {
            setState(AckReaderState.START_PENDING);
            setSchedule(schedule, false);
            log().info("start: Starting reader...");
            scheduleReads();
            setState(AckReaderState.STARTED);
            log().info("start: Reader started.");
            } else {
                IllegalStateException e = new IllegalStateException("Reader is not in a stopped state.  Reader state is: "+getState());
                log().error("start: "+e, e);
                throw e;
            }
        }
    }

    public void pause() throws IllegalStateException {
        log().debug("pause: acquiring lock...");
        synchronized (m_lock) {
            if (AckReaderState.STARTED.equals(getState()) || AckReaderState.RESUMED.equals(getState())) {
                log().info("pause: lock acquired; pausing reader...");
                setState(AckReaderState.PAUSE_PENDING);
                unScheduleReads();
                setState(AckReaderState.PAUSED);
                log().info("pause: Reader paused.");
            } else {
                IllegalStateException e = new IllegalStateException("Reader is not in a running state (STARTED or RESUMED).  Reader state is: "+getState());
                log().error("pause: "+e, e);
                throw e;
            }
        }
        log().debug("pause: lock released.");
    }

    public void resume() throws IllegalStateException {
        log().debug("resume: acquiring lock...");
        synchronized (m_lock) {
            if (AckReaderState.PAUSED.equals(getState())) {
                setState(AckReaderState.RESUME_PENDING);
                log().info("resume: lock acquired; resuming reader...");
                scheduleReads();
                setState(AckReaderState.RESUMED);
                log().info("resume: reader resumed.");
            } else {
                IllegalStateException e = new IllegalStateException("Reader is not in a paused state, cannot resume.  Reader state is: "+getState());
                log().error("resume: "+e, e);
                throw e;
            }
        }
        log().debug("resume: lock released.");
    }

    public void stop() throws IllegalStateException {
        log().debug("resume: acquiring lock...");
        synchronized (m_lock) {
            if (!AckReaderState.STOPPED.equals(getState())) {
                log().info("stop: lock acquired; stopping reader...");
                unScheduleReads();
                m_executor.remove(m_mailAckProcessor);
                log().info("stop: Reader stopped.");
            } else {
                IllegalStateException e = new IllegalStateException("Reader is already stopped.");
                log().error("stop: "+e, e);
                throw e;
            }
        }
    }

    private void unScheduleReads() {
        log().debug("unscheduleReades: acquiring lock...");
        synchronized (m_lock) {
            log().debug("unscheduleReades: lock acquired.  Pausing schedule...");
            //this isn't probably the right way to handle this
            getExecutor().pause();
            log().debug("unscheduleReads: schedule paused.");
        }
        log().debug("unscheduleReads: lock released.");
    }
    
    private void scheduleReads() {
        
        log().debug("scheduleReads: attempting to acquire lock...");

        synchronized (m_lock) {
            if (getState().equals(AckReaderState.PAUSED)) {
                getExecutor().resume();
                return;
            }

            log().debug("scheduleReads: acquired lock, creating schedule...");

            m_executor.scheduleWithFixedDelay(getMailAckProcessor(), getSchedule().getInitialDelay(), 
                                              getSchedule().getInterval(), getSchedule().getUnit());
            log().debug("scheduleReads: exited lock, schedule updated.");
            log().debug("scheduleReads: schedule is:" +
                        " attempts remaining: "+getSchedule().getAttemptsRemaining()+
                        "; initial delay: "+getSchedule().getInitialDelay()+
                        "; interval: "+getSchedule().getInterval()+
                        "; unit: "+getSchedule().getUnit());

            log().debug("scheduleReads: executor details:"+
                        " active count: "+m_executor.getActiveCount()+
                        "; completed task count: "+m_executor.getCompletedTaskCount()+
                        "; task count: "+m_executor.getTaskCount()+
                        "; queue size: "+m_executor.getQueue().size());
        }
        
    }
    
    private Logger log() {
        return ThreadCategory.getInstance();
    }

    public PausibleScheduledThreadPoolExecutor getExecutor() {
        return m_executor;
    }

    public void setExecutor(PausibleScheduledThreadPoolExecutor executor) {
        synchronized (m_lock) {
            m_executor = executor;
        }
    }

    @Override
    public String toString() {
        return getClass().getCanonicalName();
    }
    
    public void setMailAckProcessor(MailAckProcessor mailAckProcessor) {
        m_mailAckProcessor = mailAckProcessor;
    }

    public MailAckProcessor getMailAckProcessor() {
        return m_mailAckProcessor;
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
    public void setSchedule(ReaderSchedule schedule, boolean reschedule) {
        synchronized (m_lock) {
            m_schedule = schedule;
            if (reschedule) {
                m_executor.remove(m_mailAckProcessor);
                scheduleReads();
            }
        }
    }

    public ReaderSchedule getSchedule() {
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
    private void setState(AckReaderState state) {
        synchronized (m_lock) {
            m_state = state;
        }
    }
    
    public AckReaderState getState() {
        return m_state;
    }

    
}
