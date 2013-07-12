/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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
import org.opennms.javamail.JavaMailerException;
import org.opennms.javamail.JavaReadMailer;
import org.opennms.netmgt.config.ackd.Parameter;
import org.opennms.netmgt.config.javamail.ReadmailConfig;
import org.opennms.netmgt.dao.api.AckdConfigurationDao;
import org.opennms.netmgt.dao.api.AcknowledgmentDao;
import org.opennms.netmgt.dao.api.JavaMailConfigurationDao;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.AckType;
import org.opennms.netmgt.model.OnmsAcknowledgment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class uses the JavaMail API to connect to a mail store and retrieve messages, using
 * the configured host and user details, and detects replies to notifications that have
 * an acknowledgment action: acknowledge, unacknowledge, clear, escalate.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
class MailAckProcessor implements AckProcessor {
    private static final Logger LOG = LoggerFactory.getLogger(MailAckProcessor.class);
    
    private static final int LOG_FIELD_WIDTH = 128;
    
    private AckdConfigurationDao m_ackdDao;
    
    private AcknowledgmentDao m_ackDao;
    
    private volatile JavaMailConfigurationDao m_jmConfigDao;
    
    /**
     * <p>afterPropertiesSet</p>
     *
     * @throws java.lang.Exception if any.
     */
    @Override
    public void afterPropertiesSet() throws Exception {
    }

    private MailAckProcessor() {
    }

    /**
     * Retrieve the messages in the configured mail folder, searches for notification replies,
     * and creates and processes the acknowledgments.
     */
    protected void findAndProcessAcks() {
        
        LOG.debug("findAndProcessAcks: checking for acknowledgments...");
        Collection<OnmsAcknowledgment> acks;

        try {
            List<Message> msgs = retrieveAckMessages();  //TODO: need a read *new* messages feature
            acks = createAcks(msgs);
            
            if (acks != null) {
                LOG.debug("findAndProcessAcks: Found {} acks.  Processing...", acks.size());
                m_ackDao.processAcks(acks);
                LOG.debug("findAndProcessAcks: acks processed.");
            }
        } catch (JavaMailerException e) {
            LOG.error("findAndProcessAcks: Exception thrown in JavaMail", e);
        }
        
        LOG.debug("findAndProcessAcks: completed checking for and processing acknowledgments.");
    }

    //should probably be static
    /**
     * Creates <code>OnmsAcknowledgment</code>s for each notification reply email message determined
     * to have an acknowledgment action.
     *
     * @param msgs a {@link java.util.List} object.
     * @return a {@link java.util.List} object.
     */
    protected List<OnmsAcknowledgment> createAcks(final List<Message> msgs) {
        
        LOG.info("createAcks: Detecting and possibly creating acknowledgments from {} messages...", msgs.size());
        List<OnmsAcknowledgment> acks = null;
        
        if (msgs != null && msgs.size() > 0) {
            acks = new ArrayList<OnmsAcknowledgment>();
            
            Iterator<Message> it = msgs.iterator();
            while (it.hasNext()) {
                Message msg = (Message) it.next();
                try {
                    
                    LOG.debug("createAcks: detecting acks in message: {}", msg.getSubject());
                    Integer id = detectId(msg.getSubject(), m_ackdDao.getConfig().getNotifyidMatchExpression());
                    
                    if (id != null) {
                        final OnmsAcknowledgment ack = createAck(msg, id);
                        ack.setAckType(AckType.NOTIFICATION);
                        ack.setLog(createLog(msg));
                        acks.add(ack);
                        msg.setFlag(Flag.DELETED, true);
                        LOG.debug("createAcks: found notification acknowledgment: {}", ack);
                        continue;
                    }
                    
                    id = detectId(msg.getSubject(), m_ackdDao.getConfig().getAlarmidMatchExpression());
                    
                    if (id != null) {
                        final OnmsAcknowledgment ack = createAck(msg, id);
                        ack.setAckType(AckType.ALARM);
                        ack.setLog(createLog(msg));
                        acks.add(ack);
                        msg.setFlag(Flag.DELETED, true);
                        LOG.debug("createAcks: found alarm acknowledgment: {}", ack);
                        continue;
                    }
                    
                } catch (MessagingException e) {
                    LOG.error("createAcks: messaging error", e);
                } catch (IOException e) {
                    LOG.error("createAcks: IO problem", e);
                }
            }
        } else {
            LOG.debug("createAcks: No messages for acknowledgment processing.");
        }
        
        LOG.info("createAcks: Completed detecting and possibly creating acknowledgments.  Created {} acknowledgments.", (acks == null? 0 : acks.size()));
        return acks;
    }

    /**
     * <p>detectId</p>
     *
     * @param subject a {@link java.lang.String} object.
     * @param expression a {@link java.lang.String} object.
     * @return a {@link java.lang.Integer} object.
     */
    protected static Integer detectId(final String subject, final String expression) {
        LOG.debug("detectId: Detecting aknowledgable ID from subject: {} using expression: {}", subject, expression);
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
            LOG.debug("detectId: found acknowledgable ID: {}", id);
        } else {
            LOG.debug("detectId: no acknowledgable ID found.");
        }

        return id;
    }

    /**
     * <p>createAck</p>
     *
     * @param msg a {@link javax.mail.Message} object.
     * @param refId a {@link java.lang.Integer} object.
     * @return a {@link org.opennms.netmgt.model.OnmsAcknowledgment} object.
     * @throws javax.mail.MessagingException if any.
     * @throws java.io.IOException if any.
     */
    protected OnmsAcknowledgment createAck(final Message msg, final Integer refId) throws MessagingException, IOException {
        String ackUser = ((InternetAddress)msg.getFrom()[0]).getAddress();
        Date ackTime = msg.getReceivedDate();
        OnmsAcknowledgment ack = new OnmsAcknowledgment(ackTime, ackUser);
        ack.setAckType(AckType.NOTIFICATION);
        ack.setAckAction(determineAckAction(msg));
        ack.setRefId(refId);
        return ack;
    }

    /**
     * <p>determineAckAction</p>
     *
     * @param msg a {@link javax.mail.Message} object.
     * @return a {@link org.opennms.netmgt.model.AckAction} object.
     * @throws java.io.IOException if any.
     * @throws javax.mail.MessagingException if any.
     */
    protected AckAction determineAckAction(final Message msg) throws IOException, MessagingException {
        LOG.info("determineAckAcktion: evaluating message looking for user specified acktion...");
        
        List<String> messageText = JavaReadMailer.getText(msg);
        
        AckAction action = AckAction.UNSPECIFIED;
        if (messageText != null && messageText.size() > 0) {
            
            LOG.debug("determineAction: message text: {}", messageText);
            
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
            LOG.warn(concern);
            throw new MessagingException(concern);
        }
        LOG.info("determineAckAcktion: evaluated message, {} action determined from message.", action);
        return action;
    }

    /**
     * <p>retrieveAckMessages</p>
     *
     * @return a {@link java.util.List} object.
     * @throws org.opennms.javamail.JavaMailerException if any.
     */
    protected List<Message> retrieveAckMessages() throws JavaMailerException {
        LOG.debug("retrieveAckMessages: Retrieving messages...");
        
        ReadmailConfig readMailConfig = determineMailReaderConfig();
        
        LOG.debug("retrieveAckMessages: creating JavaReadMailer with config: host: {} port: {} ssl: {} transport: {} user: {} password: {}", readMailConfig.getReadmailHost().getHost(), readMailConfig.getReadmailHost().getPort(), readMailConfig.getReadmailHost().getReadmailProtocol().getSslEnable(), readMailConfig.getReadmailHost().getReadmailProtocol().getTransport(), readMailConfig.getUserAuth().getUserName(), readMailConfig.getUserAuth().getPassword());
        
        //TODO: make flag for folder open mode
        //TODO: Make sure configuration supports flag for deleting acknowledgments
        JavaReadMailer readMailer = new JavaReadMailer(readMailConfig, true);

        String notifRe = m_ackdDao.getConfig().getNotifyidMatchExpression();
        notifRe = notifRe.startsWith("~") ? notifRe.substring(1) : notifRe;
        
        String alarmRe = m_ackdDao.getConfig().getAlarmidMatchExpression();
        alarmRe = alarmRe.startsWith("~") ? alarmRe.substring(1) : alarmRe;
        
        Pattern notifPattern = Pattern.compile(notifRe);
        Pattern alarmPattern = Pattern.compile(alarmRe);
        
        List<Message> msgs = readMailer.retrieveMessages();
        LOG.info("retrieveAckMessages: Iterating {} messages with notif expression: {} and alarm expression: {}", msgs.size(), notifRe, alarmRe);
        
        for (Iterator<Message> iterator = msgs.iterator(); iterator.hasNext();) {
            Message msg = iterator.next();
            try {
                String subject = msg.getSubject();
                
                Matcher alarmMatcher = alarmPattern.matcher(subject);
                Matcher notifMatcher = notifPattern.matcher(subject);
                
                LOG.debug("retrieveAckMessages: comparing the subject: {}", subject);
                if (!(notifMatcher.matches() || alarmMatcher.matches())) {
                    
                    LOG.debug("retrieveAckMessages: Subject doesn't match either expression.");
                    iterator.remove();
                } else {
                    //TODO: this just looks wrong
                    //delete this non-ack message because the acks will get deleted later and the config
                    //indicates delete all mail from mailbox
                    LOG.debug("retrieveAckMessages: Subject matched, setting deleted flag");
                    if (readMailConfig.isDeleteAllMail()) {
                        msg.setFlag(Flag.DELETED, true);
                    }
                }
            } catch (Throwable t) {
                LOG.error("retrieveAckMessages: Problem processing message: {}", t);
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

    
    /**
     * <p>run</p>
     */
    @Override
    public void run() {
        try {
            LOG.info("run: Processing mail acknowledgments (opposed to femail acks ;)...");
            findAndProcessAcks();
            LOG.info("run: Finished processing mail acknowledgments.");
        } catch (Throwable e) {
            LOG.debug("run: threw exception", e);
        } finally {
            LOG.debug("run: method completed.");
        }
        
    }
    
    /**
     * <p>determineMailReaderConfig</p>
     *
     * @return a {@link org.opennms.netmgt.config.javamail.ReadmailConfig} object.
     */
    public ReadmailConfig determineMailReaderConfig() {
    	
    	LOG.info("determineMailReaderConfig: determining mail reader configuration...");
    	
        List<Parameter> parms = m_ackdDao.getParametersForReader("JavaMailReader");
        ReadmailConfig config = m_jmConfigDao.getDefaultReadmailConfig();
        
        for (Parameter parameter : parms) {
            if ("readmail-config".equalsIgnoreCase(parameter.getKey())) {
                config = m_jmConfigDao.getReadMailConfig(parameter.getValue());
            }
        }
        
	LOG.info("determinedMailReaderConfig: {}", config);
        return config;
    }
    
    /**
     * <p>setAckdConfigDao</p>
     *
     * @param configDao a {@link org.opennms.netmgt.dao.api.AckdConfigurationDao} object.
     */
    public synchronized void setAckdConfigDao(final AckdConfigurationDao configDao) {
        m_ackdDao = configDao;
    }
    
    /**
     * @param ackDao a {@link org.opennms.netmgt.dao.api.AcknowledgmentDao} object.
     */
    public synchronized void setAcknowledgmentDao(final AcknowledgmentDao ackDao) {
        m_ackDao = ackDao;
    }
    
    /**
     * <p>reloadConfigs</p>
     */
    @Override
    public synchronized void reloadConfigs() {
        LOG.debug("reloadConfigs: lock acquired; reloading configuration...");
        m_jmConfigDao.reloadConfiguration();
        LOG.debug("reloadConfigs: configuration reloaded");
    }
    
    /**
     * <p>getJmConfigDao</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.JavaMailConfigurationDao} object.
     */
    protected JavaMailConfigurationDao getJmConfigDao() {
        return m_jmConfigDao;
    }

    /**
     * <p>setJmConfigDao</p>
     *
     * @param jmConfigDao a {@link org.opennms.netmgt.dao.api.JavaMailConfigurationDao} object.
     */
    public void setJmConfigDao(final JavaMailConfigurationDao jmConfigDao) {
        m_jmConfigDao = jmConfigDao;
    }

}
