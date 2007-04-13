/*
 * Created on Sep 13, 2004
 *
 * Copyright (C) 2005 The OpenNMS Group, Inc.
 */
package org.opennms.netmgt.utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.util.StringUtils;

/**
 * Sends an email message using the Java Mail API
 * 
 * @author <A HREF="mailto:david@opennms.org">David Hustace </A>
 */
public class JavaMailer {
    private static final String DEFAULT_FROM_ADDRESS = "root@[127.0.0.1]";
//    private static final String DEFAULT_TO_ADDRESS = "root@[127.0.0.1]";
    private static final String DEFAULT_MAIL_HOST = "127.0.0.1";
    private static final boolean DEFAULT_AUTHENTICATE = false;
    private static final String DEFAULT_AUTHENTICATE_USER = "opennms";
    private static final String DEFAULT_AUTHENTICATE_PASSWORD = "opennms";
    private static final String DEFAULT_MAILER = "smtpsend";
    private static final String DEFAULT_TRANSPORT = "smtp";
    private static final boolean DEFAULT_MAILER_DEBUG = true;
    private static final boolean DEFAULT_USE_JMTA = true;

    private boolean m_debug = JavaMailerConfig.getProperty("org.opennms.core.utils.debug", DEFAULT_MAILER_DEBUG);
    private String m_mailHost = JavaMailerConfig.getProperty("org.opennms.core.utils.mailHost", DEFAULT_MAIL_HOST);
    private boolean m_useJMTA = JavaMailerConfig.getProperty("org.opennms.core.utils.useJMTA", DEFAULT_USE_JMTA);
    private String m_mailer = JavaMailerConfig.getProperty("org.opennms.core.utils.mailer", DEFAULT_MAILER);
    private String m_transport = JavaMailerConfig.getProperty("org.opennms.core.utils.transport", DEFAULT_TRANSPORT);
    private String m_from = JavaMailerConfig.getProperty("org.opennms.core.utils.fromAddress", DEFAULT_FROM_ADDRESS);
    private boolean m_authenticate = JavaMailerConfig.getProperty("org.opennms.core.utils.authenticate", DEFAULT_AUTHENTICATE);
    private String m_user = JavaMailerConfig.getProperty("org.opennms.core.utils.authenticateUser", DEFAULT_AUTHENTICATE_USER);
    private String m_password = JavaMailerConfig.getProperty("org.opennms.core.utils.authenticatePassword", DEFAULT_AUTHENTICATE_PASSWORD);

    private String m_to;// = DEFAULT_TO_ADDRESS;
    private String m_subject;
    private String m_messageText;
    private String m_fileName;

    public JavaMailer() {

    }

    /**
     * Sends a message based on properties set on this bean.
     */
    public void mailSend() throws JavaMailerException {
        checkEnvelopeAndContents();
        
        Properties props = System.getProperties();
        
        props.put("mail.smtp.auth", new Boolean(isAuthenticate()).toString());
        
        Session session = Session.getInstance(props, null);
        session.setDebugOut(new PrintStream(new LoggingByteArrayOutputStream(log()), true));
        session.setDebug(isDebug());
        
        sendMessage(session, buildMessage(session));
    }

    /**
     * Build a complete message ready for sending.
     * 
     * @param session session to use to create a new MimeMessage
     * @return completed message, ready to be passed to Transport.sendMessage
     * @throws JavaMailerException if any of the underlying operations fail
     */
    private Message buildMessage(Session session) throws JavaMailerException {
        try {
            Message message = new MimeMessage(session);
            
            log().debug("From is: " + getFrom());
            message.setFrom(new InternetAddress(getFrom()));
            
            log().debug("To is: " + getTo());
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(getTo(), false));
            
            log().debug("Subject is: " + getSubject());
            message.setSubject(getSubject());
            
            log().debug("Message text is: " + getMessageText());
            if (getFileName() != null) {
                log().debug("fileName is non-null--creating a MIME multipart message with file '" + getFileName() + "' as an attachment.");

                /*
                 * Create a MIME multipart message to hold our message text
                 * and the attachment
                 */
                MimeMultipart mp = new MimeMultipart();
                mp.addBodyPart(createTextPart(getMessageText()));
                mp.addBodyPart(createFileAttachment(new File(getFileName())));
                message.setContent(mp);
            } else {
                message.setText(getMessageText());
            }
            
            message.setHeader("X-Mailer", getMailer());
            message.setSentDate(new Date());
            
            message.saveChanges();
            
            return message;
        } catch (AddressException e) {
            log().error("Java Mailer Addressing exception: " + e, e);
            throw new JavaMailerException("Java Mailer Addressing exception: " + e, e);
        } catch (MessagingException e) {
            log().error("Java Mailer messaging exception: " + e, e);
            throw new JavaMailerException("Java Mailer messaging exception: " + e, e);
        }
    }

    /**
     * Create a MimeBodyPart containing plain text.
     * 
     * @param messageText plain text message to include
     * @return plain text body part
     * @throws MessagingException
     */
    private MimeBodyPart createTextPart(String messageText) throws MessagingException {
        MimeBodyPart messageTextPart = new MimeBodyPart();
        messageTextPart.setText(messageText);
        return messageTextPart;
    }
    
    /**
     * Create a file attachment as a MimeBodyPart, checking to see if the file
     * exists before we create the attachment.
     * 
     * @param file file to attach
     * @return attachment body part
     * @throws MessagingException if we can't set the data handler or
     *      the file name on the MimeBodyPart
     * @throws JavaMailerException if the file does not exist or is not
     *      readable
     */
    private MimeBodyPart createFileAttachment(File file) throws MessagingException, JavaMailerException {
        if (!file.exists()) {
            log().error("File attachment '" + file.getAbsolutePath() + "' does not exist.");
            throw new JavaMailerException("File attachment '" + file.getAbsolutePath() + "' does not exist.");
        }
        if (!file.canRead()) {
            log().error("File attachment '" + file.getAbsolutePath() + "' is not readable.");
            throw new JavaMailerException("File attachment '" + file.getAbsolutePath() + "' is not readable.");
        }
        
        MimeBodyPart bodyPart = new MimeBodyPart();
        FileDataSource fds = new FileDataSource(file);
        bodyPart.setDataHandler(new DataHandler(fds));
        bodyPart.setFileName(fds.getName());
        return bodyPart;
    }
    
    /**
     * Check that required envelope and message contents properties have been
     * set. 
     * 
     * @throws JavaMailerException if any of the required properties have not
     *      been set 
     */
    private void checkEnvelopeAndContents() throws JavaMailerException {
        if (getFrom() == null) {
            throw new JavaMailerException("Cannot have a null from address.");
        }
        if (getTo() == null) {
            throw new JavaMailerException("Cannot have a null to address.");
        }
        if (getSubject() == null) {
            throw new JavaMailerException("Cannot have a null subject.");
        }
        if (getMessageText() == null) {
            throw new JavaMailerException("Cannot have a null messageText.");
        }
    }

    /**
     * Send message.
     * 
     * @param session
     * @param message
     * @throws JavaMailerException
     */
    private void sendMessage(Session session, Message message) throws JavaMailerException {
        Transport t = null;
        try {
            t = session.getTransport(getTransport());
            log().debug("for transport name '" + getTransport() + "' got: " + t.getClass().getName() + "@" + Integer.toHexString(t.hashCode()));
            
            LoggingTransportListener listener = new LoggingTransportListener(log());
            t.addTransportListener(listener);
            
            if (t.getURLName().getProtocol().equals("mta")) {
                // JMTA throws an AuthenticationFailedException if we call connect()
                log().debug("transport is 'mta', not trying to connect()");
            } else if (isAuthenticate()) {
                log().debug("authenticating to " + getMailHost());
                t.connect(getMailHost(), getUser(), getPassword());
            } else {
                log().debug("not authenticating to " + getMailHost());
                t.connect(getMailHost(), null, null);
            }
            
            t.sendMessage(message, message.getAllRecipients());
            listener.assertAllMessagesDelivered();
        } catch (NoSuchProviderException e) {
            log().error("Couldn't get a transport: " + e, e);
            throw new JavaMailerException("Couldn't get a transport: " + e, e);
        } catch (MessagingException e) {
            log().error("Java Mailer messaging exception: " + e, e);
            throw new JavaMailerException("Java Mailer messaging exception: " + e, e);
        } finally {
            try {
                if (t != null && t.isConnected()) {
                    t.close();
                }
            } catch (MessagingException e) {
                throw new JavaMailerException("Java Mailer messaging exception on transport close: " + e, e);
            }
        }
    }

    /**
     * @return Returns the password.
     */
    public String getPassword() {
        return m_password;
    }

    /**
     * @param password
     *            The password to set.
     */
    public void setPassword(String password) {
        m_password = password;
    }

    /**
     * @return Returns the user.
     */
    public String getUser() {
        return m_user;
    }

    /**
     * @param user
     *            The user to set.
     */
    public void setUser(String user) {
        m_user = user;
    }
    
	/**
	 * @return Returns the _useMailHost.
	 */
	public boolean isUseJMTA() {
		return m_useJMTA;
	}
    
	/**
	 * @param mailHost The _useMailHost to set.
	 */
	public void setUseJMTA(boolean useMTA) {
		m_useJMTA = useMTA;
	}

    /**
     * @return Returns the from address.
     */
    public String getFrom() {
        return m_from;
    }

    /**
     * @param from
     *            The from address to set.
     */
    public void setFrom(String from) {
        m_from = from;
    }

    /**
     * @return Returns the authenticate boolean.
     */
    public boolean isAuthenticate() {
        return m_authenticate;
    }

    /**
     * @param authenticate
     *            The authenticate boolean to set.
     */
    public void setAuthenticate(boolean authenticate) {
        m_authenticate = authenticate;
    }

    /**
     * @return Returns the file name attachment.
     */
    public String getFileName() {
        return m_fileName;
    }

    /**
     * @param file
     *            Sets the file name to be attached to the messaget.
     */
    public void setFileName(String fileName) {
        m_fileName = fileName;
    }

    /**
     * @return Returns the mail host.
     */
    public String getMailHost() {
        return m_mailHost;
    }

    /**
     * @param mail_host
     *            Sets the mail host.
     */
    public void setMailHost(String mail_host) {
        m_mailHost = mail_host;
    }

    /**
     * @return Returns the mailer.
     */
    public String getMailer() {
        return m_mailer;
    }

    /**
     * @param mailer
     *            Sets the mailer.
     */
    public void setMailer(String mailer) {
        m_mailer = mailer;
    }

    /**
     * @return Returns the message text.
     */
    public String getMessageText() {
        return m_messageText;
    }

    /**
     * @param messageText
     *            Sets the message text.
     */
    public void setMessageText(String messageText) {
        m_messageText = messageText;
    }

    /**
     * @return Returns the message Subject.
     */
    public String getSubject() {
        return m_subject;
    }

    /**
     * @param subject
     *            Sets the message Subject.
     */
    public void setSubject(String subject) {
        m_subject = subject;
    }

    /**
     * @return Returns the To address.
     */
    public String getTo() {
        return m_to;
    }

    /**
     * @param to
     *            Sets the To address.
     */
    public void setTo(String to) {
        m_to = to;
    }

    public String getTransport() {
        if (isUseJMTA()) {
            return "mta";
        } else {
            return m_transport;
        }
    }

    public void setTransport(String transport) {
        m_transport = transport;
    }

    public boolean isDebug() {
        return m_debug;
    }

    public void setDebug(boolean debug) {
        m_debug = debug;
    }

    /**
     * @return log4j Category
     */
    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
    
    public static class LoggingByteArrayOutputStream extends ByteArrayOutputStream {
        private Category m_category;
        
        public LoggingByteArrayOutputStream(Category category) {
            m_category = category;
        }

        @Override
        public void flush() throws IOException {
            super.flush();
            
            String buffer = toString().replaceAll("\n", "");
            if (buffer.length() > 0) {
                m_category.debug(buffer);   
            }
            
            reset();
        }
    }
    
    public static class LoggingTransportListener implements TransportListener {
        private Category m_category;
        private List<Address> m_invalidAddresses = new ArrayList<Address>();
        private List<Address> m_validSentAddresses = new ArrayList<Address>();
        private List<Address> m_validUnsentAddresses = new ArrayList<Address>();
        
        public LoggingTransportListener(Category category) {
            m_category = category;
        }

        public void messageDelivered(TransportEvent event) {
            logEvent("message delivered", event);
        }

        public void messageNotDelivered(TransportEvent event) {
            logEvent("message not delivered", event);
        }

        public void messagePartiallyDelivered(TransportEvent event) {
            logEvent("message partially delivered", event);
        }

        private void logEvent(String message, TransportEvent event) {
            if (event.getInvalidAddresses() != null && event.getInvalidAddresses().length > 0) {
                m_invalidAddresses.addAll(Arrays.asList(event.getInvalidAddresses()));
                m_category.error(message + ": invalid addresses: " + StringUtils.arrayToDelimitedString(event.getInvalidAddresses(), ", "));
            }
            if (event.getValidSentAddresses() != null && event.getValidSentAddresses().length > 0) {
                m_validSentAddresses.addAll(Arrays.asList(event.getValidSentAddresses()));
                m_category.debug(message + ": valid sent addresses: " + StringUtils.arrayToDelimitedString(event.getValidSentAddresses(), ", "));
            }
            if (event.getValidUnsentAddresses() != null && event.getValidUnsentAddresses().length > 0) {
                m_validUnsentAddresses.addAll(Arrays.asList(event.getValidUnsentAddresses()));
                m_category.error(message + ": valid unsent addresses: " + StringUtils.arrayToDelimitedString(event.getValidUnsentAddresses(), ", "));
            }
        }
        
        public boolean hasAnythingBeenReceived() {
            return m_invalidAddresses.size() != 0 || m_validSentAddresses.size() != 0 || m_validUnsentAddresses.size() != 0;
        }
        
        /**
         * We sleep up to ten times for 10ms, checking to see if anything has
         * been received because the notifications are done by a separate
         * thread.  We also wait another 50ms after we see the first
         * notification come in, just to see if anything else trickles in.
         * This isn't perfect, but it's somewhat of a shot in the dark to
         * hope that we catch most things, to try to catch as many errors
         * as possible so we can fairly reliably report if anything had
         * problems. 
         * 
         * @throws JavaMailerException
         */
        public void assertAllMessagesDelivered() throws JavaMailerException {
            for (int i = 0; i < 10; i++) {
                if (hasAnythingBeenReceived()) {
                    break;
                }
                
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    // Do nothing
                }
            }
            
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                // Do nothing
            }

            if (m_invalidAddresses.size() == 0 && m_validUnsentAddresses.size() == 0) {
                // Nothing failed, so just return
                return;
            }
            
            throw new JavaMailerException("Not all messages delivered:\n"
                    + "\t" + m_validSentAddresses.size() + " messages were sent to valid addresses: " + StringUtils.collectionToDelimitedString(m_validSentAddresses, ", ") + "\n"
                    + "\t" + m_validUnsentAddresses.size() + " messages were not sent to valid addresses: " + StringUtils.collectionToDelimitedString(m_validUnsentAddresses, ", ") + "\n"
                    + "\t" + m_invalidAddresses.size() + " messages had invalid addresses: " + StringUtils.collectionToDelimitedString(m_invalidAddresses, ", "));
        }

    }

}
