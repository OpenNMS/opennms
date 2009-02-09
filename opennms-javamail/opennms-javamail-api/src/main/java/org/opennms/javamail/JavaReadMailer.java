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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;

import org.opennms.netmgt.config.common.JavamailProperty;
import org.opennms.netmgt.config.common.ReadmailConfig;


/*
 * TODO Handy API things I've found that should be implemented
 * 
            Message[] msgs = new Message[mailFolder.getMessageCount()];
            int unReadCnt = mailFolder.getUnreadMessageCount();
            int newCnt = mailFolder.getNewMessageCount();
            int delCnt = mailFolder.getUnreadMessageCount();
            FetchProfile fp = new FetchProfile();
            fp.add(FetchProfile.Item.FLAGS);
            fp.add(FetchProfileItem.FLAGS);
            mailFolder.fetch(msgs, fp);
            SearchTerm st = new SubjectTerm(subjectMatch);
*/            


/**
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 *
 */
public class JavaReadMailer {

    public JavaReadMailer() {
    }

    /*
     * TODO: Need readers that:
     *   - use FetchProfiles
     *   - make use of pre-fetch nature of getMessages()
     *   - A reader for the entire system could be implemented using events/event listeners
     * TODO: Need to make this more efficient... probably needs state so that message contents can be retrieved from the
     *       store after they are read via this message.
     */
    public List<OpenNMSJavaMailMessage> readMessages(ReadmailConfig config, String subjectMatch) throws JavaMailerException {
        Message[] msgs;
        Folder mailFolder = null;
        
        Session sess = Session.getInstance(configureProperties(config), configureAuthenticator(config));
        try {
            Store store = sess.getStore(config.getReadmailHost().getReadmailProtocol().getTransport());
            store.connect(config.getReadmailHost().getHost(), (int)config.getReadmailHost().getPort(), config.getUserAuth().getUserName(), config.getUserAuth().getPassword());
            mailFolder = store.getFolder(config.getMailFolder());
            mailFolder.open(Folder.READ_WRITE);
            msgs = mailFolder.getMessages();
        } catch (NoSuchProviderException e) {
            throw new JavaMailerException("No provider matching:"+config.getReadmailHost().getReadmailProtocol().getTransport()+" from config:"+config.getName(), e);
        } catch (MessagingException e) {
            throw new JavaMailerException("Problem reading messages from configured mail store", e);
        } finally {
            if (mailFolder.isOpen()) {
                try {
                    mailFolder.close(true);
                } catch (MessagingException e) {
                    throw new JavaMailerException("problem closing mail folder:"+config.getMailFolder(), e);
                }
            }
        }
        
        List<OpenNMSJavaMailMessage> omsgs = new ArrayList<OpenNMSJavaMailMessage>(msgs.length);
        for (int i = 0; i < msgs.length; i++) {
            try {
                omsgs.add(new OpenNMSJavaMailMessage(msgs[i], sess));
            } catch (MessagingException e) {
                throw new JavaMailerException("Problem creating message", e);
            } catch (IOException e) {
                throw new JavaMailerException("IO Problem creating message", e);
            }
        }
        return omsgs;
    }

    /**
     * Configures the java mail api properties based on the settings ReadMailConfig
     * @param config
     * @return
     */
    private Properties configureProperties(ReadmailConfig config) {
        Properties props = new Properties();
        
        props.setProperty("mail.debug", String.valueOf(config.isDebug()));
        
        //first set the actual properties defined in the sendmail configuration
        List<JavamailProperty> jmps = config.getJavamailPropertyCollection();
        for (JavamailProperty jmp : jmps) {
            props.setProperty(jmp.getName(), jmp.getValue());
        }
        
        String protocol = config.getReadmailHost().getReadmailProtocol().getTransport();
        props.put("mail." + protocol + ".host", config.getReadmailHost().getHost());
        props.put("mail." + protocol + ".user", config.getUserAuth().getUserName());
        props.put("mail." + protocol + ".port", config.getReadmailHost().getPort());
        props.put("mail." + protocol + ".starttls.enable", config.getReadmailHost().getReadmailProtocol().isStartTls());
        props.put("mail.smtp.auth", "true");

        if (config.getReadmailHost().getReadmailProtocol().isSslEnable()) {
            props.put("mail." + protocol + ".socketFactory.port", config.getReadmailHost().getPort());
            props.put("mail." + protocol + ".socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail." + protocol + ".socketFactory.fallback", "false");
        }

        //FIXME: need config for these
        props.put("mail." + protocol + ".connectiontimeout", 3000);
        props.put("mail." + protocol + ".timeout", 3000);
        props.put("mail.store.protocol", protocol);
        
        return props;
    }

    /**
     * Creates an authenticator using setting from the ReadmailConfig
     * 
     * @param config
     * @return an instance of Authenticator overriding the getPasswordAuthentication attribute
     */
    private static Authenticator configureAuthenticator(final ReadmailConfig config) {
        Authenticator auth = new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(config.getUserAuth().getUserName(), config.getUserAuth().getPassword());
            }
        };
        return auth;
    }


}
