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
 * Created: January 28, 2009
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

import java.util.List;

import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Transport;

import org.opennms.netmgt.config.common.SendmailConfig;

/**
 * Use this class for sending OpenNMSJavaMailMessages within OpenNMS
 * 
 * TODO: Needs testing
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class JavaSendMailer {
    
    public JavaSendMailer() {
    }
    
    /**
     * This needs to be improved to send messages without making multiple connections.
     * 
     * @param msgs
     * @param config
     * @throws JavaMailerException
     */
    public static void sendMessages(List<OpenNMSJavaMailMessage> msgs, SendmailConfig config) throws JavaMailerException {
        for (OpenNMSJavaMailMessage msg : msgs) {
            sendMessage(msg, config);
        }
    }
    
    private static void sendMessage(OpenNMSJavaMailMessage msg, SendmailConfig config) throws JavaMailerException {
        try {
            Transport t = msg.getSession().getTransport(config.getSendmailProtocol().getTransport());
            t.connect();
            t.sendMessage(msg, msg.getAllRecipients());
            t.close();
        } catch (NoSuchProviderException e) {
            throw new JavaMailerException("Could not find transport for specified protocol:"+config.getSendmailProtocol().getTransport(), e);
        } catch (MessagingException e) {
            throw new JavaMailerException("Could not connect to transport host via protocol:"+config.getSendmailProtocol().getTransport(), e);
        }
    }


}
