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
 * Created: February 4, 2009
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
package org.opennms.javamail;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import org.opennms.netmgt.config.common.SendmailConfig;

/**
 * Builder pattern for creating Java Mail Messages.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class OpenNMSJavaMailMessageBuilder {
    OpenNMSJavaMailMessage m_msg;
    
    public OpenNMSJavaMailMessageBuilder(SendmailConfig config) throws JavaMailerException {
        m_msg = OpenNMSJavaMailMessage.createMessage(null, config);
    }
    
    public OpenNMSJavaMailMessageBuilder(SendmailConfig config, String body) throws JavaMailerException {
        m_msg = OpenNMSJavaMailMessage.createMessage(body, config);
    }
    
    public OpenNMSJavaMailMessageBuilder addToRecipient(String emailAddr) throws JavaMailerException {
        try {
            m_msg.addRecipient(Message.RecipientType.TO, new InternetAddress(emailAddr));
        } catch (AddressException e) {
            throw new JavaMailerException("Could not add To recipient", e);
        } catch (MessagingException e) {
            throw new JavaMailerException("Could not add To recipient", e);
        }
        return this;
    }
    
    public OpenNMSJavaMailMessageBuilder addPlainTextContent(String content) throws JavaMailerException {
        try {
            m_msg.setText(content);
        } catch (MessagingException e) {
            throw new JavaMailerException("Could not plain text content to message.", e);
        }
        return this;
    }
    
    public OpenNMSJavaMailMessageBuilder addHtmlContent(String content) throws JavaMailerException {
        try {
            m_msg.setContent(content, "text/html");
        } catch (MessagingException e) {
            throw new JavaMailerException("Could not add Html content to message", e);
        }
        return this;
    }
    
    public Message toMessage() {
        return m_msg;
    }
    
}
