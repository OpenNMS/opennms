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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;

import org.opennms.core.utils.JavaMailer;
import org.opennms.core.utils.JavaMailerException;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.netmgt.ackd.AckReader;
import org.opennms.netmgt.ackd.AckService;
import org.opennms.netmgt.config.ackd.AckdConfiguration;
import org.opennms.netmgt.config.common.JavamailProperty;
import org.opennms.netmgt.config.common.ReadmailConfig;
import org.opennms.netmgt.model.Acknowledgment;


/**
 * Acknowledgment Reader implementation using Java Mail
 * 
 * TODO: Set AckdConfiguration in each AckReader impl from Daemon
 * TODO: Identify acknowledgments for sent notifications
 * TODO: Identify acknowledgments for alarm IDs (how the send knows the ID, good question)
 * TODO: Identify escalation reply
 * TODO: Identify clear reply
 * TODO: Identify unacknowledge reply
 * TODO: Configurable Schedule
 * TODO: Identify Java Mail configuration element to use for reading replies
 * TODO: Migrate JavaMailNotificationStrategy to new JavaMail Configuration
 * TODO: Migrate Availability Reports send via JavaMail to new JavaMail Configuration
 * TODO: Move reading email messages from MTM and this class to JavaMailer class
 * 
 * 
 * @author <a href=mailto:david@opennms.org>David Hustace</a>
 * 
 */
public class JavaMailAckReaderImpl implements AckReader {

    private static final int IDLE = 0;
    private static final int RUNNING = 1;
    private static final int FINISHING = 2;
    private int m_status;
    
    private Timer m_timer;
    
    private AckService m_ackService;
    
    private AckdConfiguration m_config;

    public void start(final AckdConfiguration config) {
        m_config = config;
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
        if (m_timer != null) {
            m_status = FINISHING;
            m_timer.cancel();
            m_timer = null;
        } else {
            //TODO: log something
        }
    }
    
    private void scheduleReads() {
        if (m_timer != null) {
            m_status = FINISHING;
            m_timer.cancel();
        }
        m_timer = new Timer("Ackd.JavaMailReader", true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                findAndProcessAcks();  //should be something else, place holder for now
            }

        };
        m_timer.scheduleAtFixedRate(task, 3000, 3000);

    }
    
    private void findAndProcessAcks() {
        
        Collection<Acknowledgment> acks;

        try {
            List<Message> msgs = readMessages();
            acks = detectAcks(msgs);
            m_ackService.proccessAck(acks);
        } catch (JavaMailerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        
        
    }
    
    private Collection<Acknowledgment> detectAcks(List<Message> msgs) {
        Collection<Acknowledgment> acks = null;
        
        if (msgs != null) {
            acks = new ArrayList<Acknowledgment>();
            for (Message msg : msgs) {
                try {
                    if (hasNotifId(msg.getSubject())) {
                        acks.add(createAcknowledgment(msg));
                    }
                } catch (MessagingException e) {
                    //FIXME: do something audit like here
                }
            }
            
        }
        return acks;
    }


    private Acknowledgment createAcknowledgment(Message msg) throws MessagingException {
        Acknowledgment ack = new Acknowledgment();
        ack.setAckTime(msg.getReceivedDate());
        ack.setAckUser(((InternetAddress)msg.getFrom()[0]).getAddress());
        return ack;
    }

    private boolean hasNotifId(String subject) {
        
        return false;
    }

    private List<Message> readMessages() throws JavaMailerException {
        List<Message> messages = null;
        
        //TODO: Need a factory for this
        ReadmailConfig m_config = new ReadmailConfig();
        
        String protocol = m_config.getReadmailHost().getReadmailProtocol().getTransport();
        Properties jmProps = createProperties(m_config.getJavamailPropertyCollection());
        jmProps.put("mail." + protocol + ".host", m_config.getReadmailHost().getHost());
        jmProps.put("mail." + protocol + ".user", m_config.getUserAuth().getUserName());
        jmProps.put("mail." + protocol + ".port", m_config.getReadmailHost().getPort());
        jmProps.put("mail." + protocol + ".starttls.enable", m_config.getReadmailHost().getReadmailProtocol().isStartTls());
        jmProps.put("mail.smtp.auth", "true");

        if (m_config.getReadmailHost().getReadmailProtocol().isSslEnable()) {
            jmProps.put("mail." + protocol + ".socketFactory.port", m_config.getReadmailHost().getPort());
            jmProps.put("mail." + protocol + ".socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            jmProps.put("mail." + protocol + ".socketFactory.fallback", "false");
        }

        //FIXME: need config for these
        jmProps.put("mail." + protocol + ".connectiontimeout", 3000);
        jmProps.put("mail." + protocol + ".timeout", 3000);
        jmProps.put("mail.store.protocol", protocol);

        Store mailStore = null;
        Folder mailFolder = null;
        

        try {
            JavaMailer readMailer = new JavaMailer(jmProps);
            TimeoutTrackerMap map = new TimeoutTrackerMap(Integer.valueOf(3), Integer.valueOf(3000), Boolean.TRUE);
            TimeoutTracker tracker = new TimeoutTracker(map.getParameterMap(), 1, 3000);

            for (tracker.reset(); tracker.shouldRetry(); tracker.nextAttempt()) {
                tracker.startAttempt();
                
                try {
                    mailStore = readMailer.getSession().getStore();
                    mailFolder = retrieveMailFolder(m_config, mailStore);
                    mailFolder.open(Folder.READ_WRITE);  //TODO: Make sure configuration supports flag for deleting acknowledgments
                    
                    if (mailFolder.isOpen()) {
                        Message[] msgs = mailFolder.getMessages();
                        messages = Arrays.asList(msgs);
                    }
                } catch (MessagingException e) {
                    //TODO: something clever here and continue?
                    continue;
                }
            }

            
        } finally {
            
        }

        return messages;
    }

    
    private Boolean searchForReplies(List<Message> messages, ReadmailConfig config) {
        return null;
    }

    /**
     * Establish connection with mail store and return the configured mail folder.
     * 
     * @param mailParms
     * @param mailStore
     * @return the folder specified in configuration
     * @throws MessagingException
     */
    private Folder retrieveMailFolder(final ReadmailConfig config, final Store mailStore) throws MessagingException {
        mailStore.connect(config.getReadmailHost().getHost(), (int)config.getReadmailHost().getPort(), config.getUserAuth().getUserName(), config.getUserAuth().getPassword());
        
        //TODO: figure out the difference between getting a named folder from the store and getting a named folder from a folder (perhaps a heiarchy thing?)
        Folder mailFolder = mailStore.getDefaultFolder();
        mailFolder = mailFolder.getFolder(config.getMailFolder());
        if (!mailFolder.exists()) {
            throw new IllegalArgumentException("The specified mail folder doesn't exist in the store: "+config.getMailFolder());
        }
        return mailFolder;
    }

    
    private class TimeoutTrackerMap {
        Map<String, String> m_map;
        
        TimeoutTrackerMap(Integer retry, Integer timeout, Boolean strict) {
            
            m_map = new HashMap<String, String>();
            m_map.put("timeout", timeout.toString());
            m_map.put("retry", retry.toString());
            m_map.put("strict-timeout", strict.toString());
        }
        
        public Map<String, String> getParameterMap() {
            return m_map;
        }
        
    }

    private Properties createProperties(final List<JavamailProperty> javamailPropertyCollection) {
        Properties props = new Properties();
        
        for (JavamailProperty javamailProperty : javamailPropertyCollection) {
            props.setProperty(javamailProperty.getName(), javamailProperty.getValue());
        }
        
        return props;
    }

    public void setAckService(AckService ackService) {
        m_ackService = ackService;
    }

    public AckService getAckService() {
        return m_ackService;
    }

    public void setAckdConfig(AckdConfiguration config) {
        m_config = config;
    }

}
