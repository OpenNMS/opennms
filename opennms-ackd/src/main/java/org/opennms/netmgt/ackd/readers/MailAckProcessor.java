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

import org.opennms.javamail.JavaMailerException;
import org.opennms.javamail.JavaReadMailer;
import org.opennms.netmgt.ackd.AckService;
import org.opennms.netmgt.config.common.ReadmailConfig;
import org.opennms.netmgt.dao.AckdConfigurationDao;
import org.opennms.netmgt.dao.JavaMailConfigurationDao;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.AckType;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.springframework.beans.factory.InitializingBean;

class MailAckProcessor implements Runnable, InitializingBean {
    
    private static AckdConfigurationDao m_daemonConfigDao;
    private static AckService m_ackService;
    private static JavaMailConfigurationDao m_jmConfigDao;
    private static MailAckProcessor m_instance;

    public void afterPropertiesSet() throws Exception {
        m_instance = this;
    }

    private MailAckProcessor() {
    }

    public synchronized static MailAckProcessor getInstance() {
        return m_instance;
    }

    protected void findAndProcessAcks() {
        
        Collection<OnmsAcknowledgment> acks;

        try {
            List<Message> msgs = retrieveAckMessages();  //TODO: need a read *new* messages feature
            acks = createAcks(msgs);
            m_ackService.processAcks(acks);
        } catch (JavaMailerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //should probably be static
    protected List<OnmsAcknowledgment> createAcks(List<Message> msgs) {
        List<OnmsAcknowledgment> acks = null;
        
        if (msgs != null && msgs.size() > 0) {
            acks = new ArrayList<OnmsAcknowledgment>();
            for (Message msg : msgs) {
                try {
                    Integer id = detectId(msg.getSubject(), m_daemonConfigDao.getConfig().getNotifyidMatchExpression());
                    
                    if (id != null) {
                        final OnmsAcknowledgment ack = createAcknowledgment(msg, id);
                        ack.setAckType(AckType.NOTIFICATION);
                        ack.setLog(createLog(msg));
                        acks.add(ack);
                        msg.setFlag(Flag.DELETED, true);
                        continue;
                    }
                    id = detectId(msg.getSubject(), m_daemonConfigDao.getConfig().getAlarmidMatchExpression());
                    
                    if (id != null) {
                        final OnmsAcknowledgment ack = createAcknowledgment(msg, id);
                        ack.setAckType(AckType.ALARM);
                        ack.setLog(createLog(msg));
                        acks.add(ack);
                        msg.setFlag(Flag.DELETED, true);
                        continue;
                    }
                    
                } catch (MessagingException e) {
                    //FIXME: do something audit like here
                    e.printStackTrace();
                    continue;
                } catch (IOException e) {
                    // FIXME: ditto
                    e.printStackTrace();
                    continue;
                }
            }
        }
        return acks;
    }

    protected static Integer detectId(final String subject, final String expression) {
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
        }

        return id;
    }

    //should probably be static method
    protected OnmsAcknowledgment createAcknowledgment(Message msg, Integer refId) throws MessagingException, IOException {
        String ackUser = ((InternetAddress)msg.getFrom()[0]).getAddress();
        Date ackTime = msg.getReceivedDate();
        OnmsAcknowledgment ack = new OnmsAcknowledgment(ackTime, ackUser);
        ack.setAckType(AckType.NOTIFICATION);
        ack.setAckAction(determineAckAction(msg));
        ack.setRefId(refId);
        return ack;
    }

    //should probably be static method
    protected AckAction determineAckAction(Message msg) throws IOException, MessagingException {
        
        List<String> messageText = JavaReadMailer.getText(msg);
        
        AckAction action = AckAction.UNSPECIFIED;
        if (messageText != null && messageText.size() > 0) {
            
            if (m_daemonConfigDao.acknowledgmentMatch(messageText)) {
                action = AckAction.ACKNOWLEDGE;
            } else if (m_daemonConfigDao.clearMatch(messageText)) {
                action = AckAction.CLEAR;
            } else if (m_daemonConfigDao.escalationMatch(messageText)) {
                action = AckAction.ESCALATE;
            } else if (m_daemonConfigDao.unAcknowledgmentMatch(messageText)) {
                action = AckAction.UNACKNOWLEDGE;
            } else {
                action = AckAction.UNSPECIFIED;
            }
            
        } else {
            //TODO something smart
        }
        return action;
    }

    //should probably be static method
    protected List<Message> retrieveAckMessages() throws JavaMailerException {
        ReadmailConfig config = m_jmConfigDao.getReadMailConfig(m_daemonConfigDao.getConfig().getReadmailConfig());
        
        //TODO: make flag for folder open mode
        //TODO: Make sure configuration supports flag for deleting acknowledgments
        JavaReadMailer readMailer = new JavaReadMailer(config, true);

        String notifRe = m_daemonConfigDao.getConfig().getNotifyidMatchExpression();
        notifRe = notifRe.startsWith("~") ? notifRe.substring(1) : notifRe;
        
        String alarmRe = m_daemonConfigDao.getConfig().getAlarmidMatchExpression();
        alarmRe = alarmRe.startsWith("~") ? alarmRe.substring(1) : alarmRe;
        
        List<Message> msgs = readMailer.retrieveMessages();
        for (Iterator<Message> iterator = msgs.iterator(); iterator.hasNext();) {
            Message msg = iterator.next();
            
            try {
                String subject = msg.getSubject();
                if (!(subject.matches(notifRe) || subject.matches(alarmRe))) {
                    iterator.remove();
                } else {
                    //delete this non-ack message because the acks will get deleted later and the config
                    //indicates delete all mail from mailbox
                    if (config.isDeleteAllMail()) {
                        msg.setFlag(Flag.DELETED, true);
                    }
                }
            } catch (MessagingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return msgs;
    }

    private String createLog(Message msg) {
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
        return bldr.toString();
    }

    
    public void run() {
        findAndProcessAcks();
    }
    
    
    public synchronized void setAckdConfigDao(AckdConfigurationDao configDao) {
        m_daemonConfigDao = configDao;
    }
    
    public synchronized void setAckService(AckService ackService) {
        m_ackService = ackService;
    }
    
    public void setJmConfigDao(JavaMailConfigurationDao jmConfigDao) {
        m_jmConfigDao = jmConfigDao;
    }

}