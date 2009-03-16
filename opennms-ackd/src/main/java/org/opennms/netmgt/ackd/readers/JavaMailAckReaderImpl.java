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

import org.opennms.core.concurrent.PausibleScheduledThreadPoolExecutor;
import org.opennms.netmgt.ackd.AckReader;
import org.springframework.beans.factory.InitializingBean;


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
 * TODO: Finish scheduling component of JavaAckReader
 * TODO: Configurable Schedule
 * DONE: Identify Java Mail configuration element to use for reading replies
 * TODO: Migrate JavaMailNotificationStrategy to new JavaMail Configuration and JavaSendMailer
 * TODO: Migrate Availability Reports send via JavaMail to new JavaMail Configuration and JavaSendMailer
 * TODO: Move reading email messages from MTM and this class to JavaReadMailer class
 * TODO: Do some proper logging
 * 
 * 
 * @author <a href=mailto:david@opennms.org>David Hustace</a>
 * 
 */
public class JavaMailAckReaderImpl implements AckReader, InitializingBean {

    private int m_status;
    private PausibleScheduledThreadPoolExecutor m_executor;
    private ReaderSchedule m_schedule;
    
    public void start() {
        scheduleReads();
    }
    
    public void pause() {
        unScheduleReads();
    }

    public void resume() {
        scheduleReads();
    }

    public void stop() {
        unScheduleReads();
    }

    private void unScheduleReads() {
        throw new IllegalStateException("Method not yet implemented");
    }
    
    private void scheduleReads() {
        
        if (m_schedule == null) {
            m_schedule = ReaderSchedule.createSchedule();
        }
        
        m_executor.scheduleWithFixedDelay(MailAckProcessor.getInstance(), m_schedule.getInitialDelay(), 
                                          m_schedule.getInterval(), m_schedule.getUnit());
    }
    
    public void setReaderSchedule(ReaderSchedule schedule) {
        m_schedule = schedule;
    }
    
    public int getStatus() {
        return m_status;
    }

    public void setStatus(int status) {
        m_status = status;
    }

    public PausibleScheduledThreadPoolExecutor getExecutor() {
        return m_executor;
    }

    public void setExecutor(PausibleScheduledThreadPoolExecutor executor) {
        m_executor = executor;
    }

    public void afterPropertiesSet() throws Exception {
    }
    
}
