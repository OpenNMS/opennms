/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.javamail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;
import javax.mail.search.SearchTerm;

import org.apache.commons.lang.StringUtils;
import org.opennms.netmgt.config.javamail.JavamailProperty;
import org.opennms.netmgt.config.javamail.ReadmailConfig;
import org.opennms.netmgt.config.javamail.ReadmailHost;
import org.opennms.netmgt.config.javamail.UserAuth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


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
 * JavaMail implementation for reading electronic mail.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class JavaReadMailer extends JavaMailer2 {
	
	private static final Logger LOG = LoggerFactory.getLogger(JavaReadMailer.class);

    
    private List<Message> m_messages;
    final private ReadmailConfig m_config;
    private Session m_session;
    private Boolean m_deleteOnClose = false;
    private Store m_store;


    /**
     * {@inheritDoc}
     *
     * Finalizer to be sure and close with the appropriate mode
     * any open folders
     */
    @Override
    protected void finalize() throws Throwable {
        LOG.debug("finalize: cleaning up mail folder an store connections...");
        if (m_messages != null && !m_messages.isEmpty() && m_messages.get(0).getFolder() != null && m_messages.get(0).getFolder().isOpen()) {
            m_messages.get(0).getFolder().close(m_deleteOnClose);
        }
        
        if (m_store.isConnected()) {
            m_store.close();
        }
        LOG.debug("finalize: Mail folder and store connections closed.");
        super.finalize();
    }
    
    //TODO figure out why need this throws here
    /**
     * <p>Constructor for JavaReadMailer.</p>
     *
     * @param config a {@link org.opennms.netmgt.config.javamail.ReadmailConfig} object.
     * @param closeOnDelete a {@link java.lang.Boolean} object.
     * @throws org.opennms.javamail.JavaMailerException if any.
     */
    public JavaReadMailer(final ReadmailConfig config, Boolean closeOnDelete) throws JavaMailerException {
        if (closeOnDelete != null) {
            m_deleteOnClose = closeOnDelete;
        }
        m_config = config;
        
        final UserAuth userAuth = getUserAuth(config);
        m_session = Session.getInstance(configureProperties(), createAuthenticator(userAuth.getUserName(), userAuth.getPassword()));
    }
    
    /**
     * <p>retrieveMessages</p>
     *
     * @return a {@link java.util.List} object.
     * @throws org.opennms.javamail.JavaMailerException if any.
     */
    public List<Message> retrieveMessages() throws JavaMailerException {
        Message[] msgs;
        Folder mailFolder = null;
        
        final ReadmailHost readmailHost = getReadmailHost(m_config);
        final UserAuth userAuth = getUserAuth(m_config);

        try {
            m_store = m_session.getStore(readmailHost.getReadmailProtocol().getTransport());
            m_store.connect(readmailHost.getHost(), (int)readmailHost.getPort(), userAuth.getUserName(), userAuth.getPassword());
            mailFolder = m_store.getFolder(m_config.getMailFolder());
            mailFolder.open(Folder.READ_WRITE);
            msgs = mailFolder.getMessages();
        } catch (NoSuchProviderException e) {
            throw new JavaMailerException("No provider matching:"+readmailHost.getReadmailProtocol().getTransport()+" from config:"+m_config.getName(), e);
        } catch (MessagingException e) {
            throw new JavaMailerException("Problem reading messages from configured mail store", e);
        }
        
        return new ArrayList<Message>(Arrays.asList(msgs));
    }
    

    /*
     * TODO: Need readers that:
     *   - use FetchProfiles
     *   - make use of pre-fetch nature of getMessages()
     *   - A reader for the entire system could be implemented using events/event listeners
     * TODO: Need to make this more efficient... probably needs state so that message contents can be retrieved from the
     *       store after they are read via this message.
     */
    
    /**
     * <p>retrieveMessages</p>
     *
     * @param term a {@link javax.mail.search.SearchTerm} object.
     * @return a {@link java.util.List} object.
     * @throws org.opennms.javamail.JavaMailerException if any.
     */
    public List<Message> retrieveMessages(SearchTerm term) throws JavaMailerException {
        Message[] msgs;
        Folder mailFolder = null;
        
        final ReadmailHost readmailHost = getReadmailHost(m_config);
        final UserAuth userAuth = getUserAuth(m_config);

        try {
            Store store = m_session.getStore(readmailHost.getReadmailProtocol().getTransport());
            store.connect(readmailHost.getHost(), (int)readmailHost.getPort(), userAuth.getUserName(), userAuth.getPassword());
            mailFolder = store.getFolder(m_config.getMailFolder());
            mailFolder.open(Folder.READ_WRITE);
            msgs = mailFolder.search(term);
        } catch (NoSuchProviderException e) {
            throw new JavaMailerException("No provider matching:"+readmailHost.getReadmailProtocol().getTransport()+" from config:"+m_config.getName(), e);
        } catch (MessagingException e) {
            throw new JavaMailerException("Problem reading messages from configured mail store", e);
        }
        
        List<Message> msgList = Arrays.asList(msgs);
        
        return msgList;
    }

    /**
     * Configures the java mail api properties based on the settings ReadMailConfig
     * @return A set of javamail properties based on the mail configuration 
     * @throws JavaMailerException 
     */
    private Properties configureProperties() throws JavaMailerException {
        Properties props = new Properties();
        
        props.setProperty("mail.debug", String.valueOf(m_config.isDebug()));
        
        //first set the actual properties defined in the sendmail configuration
        List<JavamailProperty> jmps = m_config.getJavamailPropertyCollection();
        for (JavamailProperty jmp : jmps) {
            props.setProperty(jmp.getName(), jmp.getValue());
        }
        
        final ReadmailHost readmailHost = getReadmailHost(m_config);
        final UserAuth userAuth = getUserAuth(m_config);

        String protocol = readmailHost.getReadmailProtocol().getTransport();
        props.put("mail." + protocol + ".host", readmailHost.getHost());
        props.put("mail." + protocol + ".user", userAuth.getUserName());
        props.put("mail." + protocol + ".port", readmailHost.getPort());
        props.put("mail." + protocol + ".starttls.enable", readmailHost.getReadmailProtocol().isStartTls());
        props.put("mail.smtp.auth", "true");

        if (readmailHost.getReadmailProtocol().isSslEnable()) {
            props.put("mail." + protocol + ".socketFactory.port", readmailHost.getPort());
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
     * <p>getMessages</p>
     *
     * @return a {@link java.util.List} object.
     */
    public List<Message> getMessages() {
        return m_messages;
    }

    /**
     * Attempts to reteive the string portion of a message... tries to handle
     * multipart messages as well.  This seems to be working so far with my tests
     * but could use some tweaking later as more types of mail servers are used
     * with this feature.
     *
     * @param msg a {@link javax.mail.Message} object.
     * @return The text portion of an email with each line being an element of the list.
     * @throws javax.mail.MessagingException if any.
     * @throws java.io.IOException if any.
     */
    public static List<String> getText(Message msg) throws MessagingException, IOException {
        
        Object content = null;
        String text = null;
        
        LOG.debug("getText: getting text of message from MimeType: text/*");

        try {
            text = (String)msg.getContent();

        } catch (ClassCastException cce) {
            content = msg.getContent();

            if (content instanceof MimeMultipart) {

                LOG.debug("getText: content is MimeMultipart, checking for text from each part...");

                for (int cnt = 0; cnt < ((MimeMultipart)content).getCount(); cnt++) {
                    BodyPart bp = ((MimeMultipart)content).getBodyPart(cnt);
                    if (bp.isMimeType("text/*")) {
                        text = (String)bp.getContent();
                        LOG.debug("getText: found text MIME type: {}", text);
                        break;
                    }
                }
                LOG.debug("getText: did not find text within MimeMultipart message.");
            }
        }
        return string2Lines(text);
    }
    
    /**
     * <p>isDeleteOnClose</p>
     *
     * @return a {@link java.lang.Boolean} object.
     */
    public Boolean isDeleteOnClose() {
        return m_deleteOnClose;
    }

    /**
     * <p>setDeleteOnClose</p>
     *
     * @param deleteOnClose a {@link java.lang.Boolean} object.
     */
    public void setDeleteOnClose(Boolean deleteOnClose) {
        m_deleteOnClose = deleteOnClose;
    }

    /**
     * <p>string2Lines</p>
     *
     * @param text a {@link java.lang.String} object.
     * @return a {@link java.util.List} object.
     */
    public static List<String> string2Lines(String text) {
        if (text == null) {
            return null;
        }
        String[] linea = StringUtils.split(text, "\n");
        for (int i = 0; i < linea.length; i++) {
            linea[i] = StringUtils.chomp(linea[i]);
        }
        return Arrays.asList(linea);
    }

    private UserAuth getUserAuth(final ReadmailConfig config) throws JavaMailerException {
        if (!config.getUserAuth().isPresent()) {
            throw new JavaMailerException("user-auth is not configured!");
        }
        return config.getUserAuth().get();
    }

    private ReadmailHost getReadmailHost(final ReadmailConfig config) throws JavaMailerException {
        if (!config.getReadmailHost().isPresent()) {
            throw new JavaMailerException("readmail-host is not configured!");
        }
        return config.getReadmailHost().get();
    }

}
