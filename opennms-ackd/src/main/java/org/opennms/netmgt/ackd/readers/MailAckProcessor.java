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
 * Created: March 15, 2009
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Header;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Flags.Flag;
import javax.mail.internet.InternetAddress;

import org.opennms.core.utils.StringUtils;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.javamail.JavaMailerException;
import org.opennms.javamail.JavaReadMailer;
import org.opennms.netmgt.config.ackd.Parameter;
import org.opennms.netmgt.config.common.ReadmailConfig;
import org.opennms.netmgt.dao.AckdConfigurationDao;
import org.opennms.netmgt.dao.JavaMailConfigurationDao;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.AckType;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.netmgt.model.acknowledgments.AckService;

/**
 * This class uses the JavaMail API to connect to a mail store and retrieve messages, using
 * the configured host and user details, and detects replies to notifications that have
 * an acknowledgment action: acknowledge, unacknowledge, clear, escalate.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
class MailAckProcessor implements AckProcessor {
    
    private static final int LOG_FIELD_WIDTH = 128;
    
    private AckdConfigurationDao m_ackdDao;
    
    private AckService m_ackService;
    
    private volatile JavaMailConfigurationDao m_jmConfigDao;
    
    public void afterPropertiesSet() throws Exception {
    }

    private MailAckProcessor() {
    }

    /**
     * Retrieve the messages in the configured mail folder, searches for notification replies,
     * and creates and processes the acknowledgments.
     */
    protected void findAndProcessAcks() {
        
        log().debug("findAndProcessAcks: checking for acknowledgments...");
        Collection<OnmsAcknowledgment> acks;

        try {
            List<Message> msgs = retrieveAckMessages();  //TODO: need a read *new* messages feature
            acks = createAcks(msgs);
            
            if (acks != null) {
                log().debug("findAndProcessAcks: Found "+acks.size()+" acks.  Processing...");
                m_ackService.processAcks(acks);
                log().debug("findAndProcessAcks: acks processed.");
            }
        } catch (JavaMailerException e) {
            log().error("findAndProcessAcks: Exception thrown in JavaMail: "+e, e);
        }
        
        log().debug("findAndProcessAcks: completed checking for and processing acknowledgments.");
    }

    private static ThreadCategory log() {
        return ThreadCategory.getInstance(MailAckProcessor.class);
    }

    //should probably be static
    /**
     * Creates <code>OnmsAcknowledgment</code>s for each notification reply email message determined
     * to have an acknowledgment action.
     */
    protected List<OnmsAcknowledgment> createAcks(final List<Message> msgs) {
        
        log().info("createAcks: Detecting and possibly creating acknowledgments from "+msgs.size()+" messages...");
        List<OnmsAcknowledgment> acks = null;
        
        if (msgs != null && msgs.size() > 0) {
            acks = new ArrayList<OnmsAcknowledgment>();
            
            Iterator<Message> it = msgs.iterator();
            while (it.hasNext()) {
                Message msg = (Message) it.next();
                try {
                    
                    log().debug("createAcks: detecting acks in message: "+msg.getSubject());
                    Integer id = detectId(msg.getSubject(), m_ackdDao.getConfig().getNotifyidMatchExpression());
                    
                    if (id != null) {
                        final OnmsAcknowledgment ack = createAck(msg, id);
                        ack.setAckType(AckType.NOTIFICATION);
                        ack.setLog(createLog(msg));
                        acks.add(ack);
                        msg.setFlag(Flag.DELETED, true);
                        log().debug("createAcks: found notification acknowledgment: "+ack);
                        continue;
                    }
                    
                    id = detectId(msg.getSubject(), m_ackdDao.getConfig().getAlarmidMatchExpression());
                    
                    if (id != null) {
                        final OnmsAcknowledgment ack = createAck(msg, id);
                        ack.setAckType(AckType.ALARM);
                        ack.setLog(createLog(msg));
                        acks.add(ack);
                        msg.setFlag(Flag.DELETED, true);
                        log().debug("createAcks: found alarm acknowledgment."+ack);
                        continue;
                    }
                    
                } catch (MessagingException e) {
                    log().error("createAcks: messaging error: "+e);
                } catch (IOException e) {
                    log().error("createAcks: IO problem: "+e);
                }
            }
        } else {
            log().debug("createAcks: No messages for acknowledgment processing.");
        }
        
        log().info("createAcks: Completed detecting and possibly creating acknowledgments.  Created "+
                   (acks == null? 0 : acks.size())+" acknowledgments.");
        return acks;
    }

    protected static Integer detectId(final String subject, final String expression) {
        log().debug("detectId: Detecting aknowledgable ID from subject: "+subject+" using expression: "+expression);
        Integer id = null;

        //TODO: force opennms config '~' style regex attribute identity because this is the only way for this to work
        String ackExpression = null;
        
        if (expression.startsWith("~")) {
            ackExpression = expression.substring(1);
        } else {
            ackExpression = expression;
        }
        Pattern pattern = Pattern.compile(ackExpression);
        Matcher matcher = pattern.matcher(subject);

        if (matcher.matches() && matcher.groupCount() > 0) {
            id = Integer.valueOf(matcher.group(1));
            log().debug("detectId: found acknowledgable ID: "+id);
        } else {
            log().debug("detectId: no acknowledgable ID found.");
        }

        return id;
    }

    protected OnmsAcknowledgment createAck(final Message msg, final Integer refId) throws MessagingException, IOException {
        String ackUser = ((InternetAddress)msg.getFrom()[0]).getAddress();
        Date ackTime = msg.getReceivedDate();
        OnmsAcknowledgment ack = new OnmsAcknowledgment(ackTime, ackUser);
        ack.setAckType(AckType.NOTIFICATION);
        ack.setAckAction(determineAckAction(msg));
        ack.setRefId(refId);
        return ack;
    }

    protected AckAction determineAckAction(final Message msg) throws IOException, MessagingException {
        log().info("determineAckAcktion: evaluating message looking for user specified acktion...");
        
        List<String> messageText = JavaReadMailer.getText(msg);
        
        AckAction action = AckAction.UNSPECIFIED;
        if (messageText != null && messageText.size() > 0) {
            
            log().debug("determineAction: message text: "+messageText);
            
            if (m_ackdDao.acknowledgmentMatch(messageText)) {
                action = AckAction.ACKNOWLEDGE;
            } else if (m_ackdDao.clearMatch(messageText)) {
                action = AckAction.CLEAR;
            } else if (m_ackdDao.escalationMatch(messageText)) {
                action = AckAction.ESCALATE;
            } else if (m_ackdDao.unAcknowledgmentMatch(messageText)) {
                action = AckAction.UNACKNOWLEDGE;
            } else {
                action = AckAction.UNSPECIFIED;
            }
            
        } else {
            String concern = "determineAckAction: a reply message to a notification has no text to evaluate.  " +
            		"No action can be determined.";
            log().warn(concern);
            throw new MessagingException(concern);
        }
        log().info("determineAckAcktion: evaluated message, "+action+" action determined from message.");
        return action;
    }

    protected List<Message> retrieveAckMessages() throws JavaMailerException {
        log().debug("retrieveAckMessages: Retrieving messages...");
        
        ReadmailConfig readMailConfig = determineMailReaderConfig();
        
        log().debug("retrieveAckMessages: creating JavaReadMailer with config: " +
        		"host: " + readMailConfig.getReadmailHost().getHost() + 
        		" port: " + readMailConfig.getReadmailHost().getPort() +
        		" ssl: " + readMailConfig.getReadmailHost().getReadmailProtocol().getSslEnable() +
        		" transport: " + readMailConfig.getReadmailHost().getReadmailProtocol().getTransport() +
        		" user: "+readMailConfig.getUserAuth().getUserName() +
        		" password: "+readMailConfig.getUserAuth().getPassword());
        
        //TODO: make flag for folder open mode
        //TODO: Make sure configuration supports flag for deleting acknowledgments
        JavaReadMailer readMailer = new JavaReadMailer(readMailConfig, true);

        String notifRe = m_ackdDao.getConfig().getNotifyidMatchExpression();
        notifRe = notifRe.startsWith("~") ? notifRe.substring(1) : notifRe;
        
        String alarmRe = m_ackdDao.getConfig().getAlarmidMatchExpression();
        alarmRe = alarmRe.startsWith("~") ? alarmRe.substring(1) : alarmRe;
        
        List<Message> msgs = readMailer.retrieveMessages();
        log().info("retrieveAckMessages: Iterating "+msgs.size()+" messages with notif expression: "+notifRe+
                   " and alarm expression: "+alarmRe);
        
        for (Iterator<Message> iterator = msgs.iterator(); iterator.hasNext();) {
            Message msg = iterator.next();
            try {
                String subject = msg.getSubject();
                
                log().debug("retrieveAckMessages: comparing the subject: "+subject);
                if (!(subject.matches(notifRe) || subject.matches(alarmRe))) {
                    
                    log().debug("retrieveAckMessages: Subject doesn't match either expression.");
                    iterator.remove();
                } else {
                    //TODO: this just looks wrong
                    //delete this non-ack message because the acks will get deleted later and the config
                    //indicates delete all mail from mailbox
                    log().debug("retrieveAckMessages: Subject matched, setting deleted flag");
                    if (readMailConfig.isDeleteAllMail()) {
                        msg.setFlag(Flag.DELETED, true);
                    }
                }
            } catch (Throwable t) {
                log().error("retrieveAckMessages: Problem processing message: "+t);
            }
        }
        return msgs;
    }

    @SuppressWarnings("unchecked")
    private static String createLog(final Message msg) {
        StringBuilder bldr = new StringBuilder();
        Enumeration<Header> allHeaders;
        try {
            allHeaders = msg.getAllHeaders();
        } catch (MessagingException e) {
            return null;
        }
        while (allHeaders.hasMoreElements()) {
            Header header = allHeaders.nextElement();
            String name = header.getName();
            String value = header.getValue();
            bldr.append(name);
            bldr.append(":");
            bldr.append(value);
            bldr.append("\n");
        }
        return StringUtils.truncate(bldr.toString(), LOG_FIELD_WIDTH);
    }

    
    public void run() {
        try {
            log().info("run: Processing mail acknowledgments (opposed to femail acks ;)..." );
            findAndProcessAcks();
            log().info("run: Finished processing mail acknowledgments." );
        } catch (Exception e) {
            log().debug("run: threw exception: "+e, e);
        } finally {
            log().debug("run: method completed.");
        }
        
    }
    
    public ReadmailConfig determineMailReaderConfig() {
    	
    	log().info("determineMailReaderConfig: determining mail reader configuration...");
    	
        List<Parameter> parms = m_ackdDao.getParametersForReader("JavaMailReader");
        ReadmailConfig config = m_jmConfigDao.getDefaultReadmailConfig();
        
        for (Parameter parameter : parms) {
            if ("readmail-config".equalsIgnoreCase(parameter.getKey())) {
                config = m_jmConfigDao.getReadMailConfig(parameter.getValue());
            }
        }
        
    	log().info("determinedMailReaderConfig: "+config);
        return config;
    }
    
    public synchronized void setAckdConfigDao(final AckdConfigurationDao configDao) {
        m_ackdDao = configDao;
    }
    
    public synchronized void setAckService(final AckService ackService) {
        m_ackService = ackService;
    }
    
    public synchronized void reloadConfigs() {
        log().debug("reloadConfigs: lock acquired; reloading configuration...");
        m_jmConfigDao.reloadConfiguration();
        log().debug("reloadConfigs: configuration reloaded");
    }
    
    protected JavaMailConfigurationDao getJmConfigDao() {
        return m_jmConfigDao;
    }

    public void setJmConfigDao(final JavaMailConfigurationDao jmConfigDao) {
        m_jmConfigDao = jmConfigDao;
    }

}