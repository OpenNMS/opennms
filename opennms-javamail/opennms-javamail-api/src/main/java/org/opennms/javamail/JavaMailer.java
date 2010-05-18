/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2005-2007 The OpenNMS Group, Inc. All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * 2010 Feb 11: Added support for attachments from input stream - jonathan@opennms.org.
 * 2008 Jan 06: Indent. - dj@opennms.org
 * 2008 Jan 06: Moved initialization of the mailer session to constructor so
 *              that properties can be overridden by the implementer.
 *              - david@opennms.org
 * 2007 Jun 13: Added support for SSL, proper auth, ports, content-type, and
 *              charsets.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.javamail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.opennms.core.utils.PropertiesUtils;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.util.StringUtils;

/**
 * Sends an email message using the Java Mail API
 * 
 * @author <A HREF="mailto:david@opennms.org">David Hustace </A>
 */
public class JavaMailer {
    private static final String DEFAULT_FROM_ADDRESS = "root@[127.0.0.1]";
//  private static final String DEFAULT_TO_ADDRESS = "root@[127.0.0.1]";
    private static final String DEFAULT_MAIL_HOST = "127.0.0.1";
    private static final boolean DEFAULT_AUTHENTICATE = false;
    private static final String DEFAULT_AUTHENTICATE_USER = "opennms";
    private static final String DEFAULT_AUTHENTICATE_PASSWORD = "opennms";
    private static final String DEFAULT_MAILER = "smtpsend";
    private static final String DEFAULT_TRANSPORT = "smtp";
    private static final boolean DEFAULT_MAILER_DEBUG = true;
    private static final boolean DEFAULT_USE_JMTA = true;
    private static final String DEFAULT_CONTENT_TYPE = "text/plain";
    private static final String DEFAULT_CHARSET = "us-ascii";
    private static final String DEFAULT_ENCODING = "Q"; // I think this means quoted-printable encoding, see bug 2825
    private static final boolean DEFAULT_STARTTLS_ENABLE = false;
    private static final boolean DEFAULT_QUIT_WAIT = true;
    private static final int DEFAULT_SMTP_PORT = 25;
    private static final boolean DEFAULT_SMTP_SSL_ENABLE = false;

    private Session m_session = null;

    /*
     * properties from configuration
     */
    private Properties m_mailProps;
    
    /*
     * fields from properties used for deterministic behavior of the mailer
     */
    private boolean m_debug;
    private String m_mailHost;
    private boolean m_useJMTA;
    private String m_mailer;
    private String m_transport;
    private String m_from;
    private boolean m_authenticate;
    private String m_user;
    private String m_password;
    private String m_contentType;
    private String m_charSet;
    private String m_encoding;
    private boolean m_startTlsEnabled;
    private boolean m_quitWait;
    private int m_smtpPort;
    private boolean m_smtpSsl;

    /*
     * Basic messaging fields
     */
    private String m_to;
    private String m_subject;
    private String m_messageText;
    private String m_fileName;
    private InputStream m_inputStream;
    private String m_inputStreamName;
    private String m_inputStreamContentType;

    
    public JavaMailer(Properties javamailProps) throws JavaMailerException {
        
        try {
            configureProperties(javamailProps);
        } catch (IOException e) {
            throw new JavaMailerException("Failed to construct mailer", e);
        }
        
        //Now set the properties into the session
        m_session = Session.getInstance(getMailProps(), createAuthenticator());
    }

    /**
     * Default constructor.  Default properties from javamailer-properties are set into session.  To change these
     * properties, retrieve the current properties from the session and override as needed.
     * @throws IOException 
     */
    public JavaMailer() throws JavaMailerException {
        this(new Properties());
    }

    /**
     * This method uses a properties file reader to pull in opennms styled javamail properties and sets
     * the actual javamail properties.  This is here to preserve the backwards compatibility but configuration
     * will probably change soon.
     * 
     * @throws IOException
     */
    
    private void configureProperties(Properties javamailProps) throws IOException {
        
        //this loads the opennms defined properties
        m_mailProps = JavaMailerConfig.getProperties();
        
        //this sets any javamail defined properties sent in to the constructor
        m_mailProps.putAll(javamailProps);
        
        /*
         * fields from properties used for deterministic behavior of the mailer
         */
        m_debug = PropertiesUtils.getProperty(m_mailProps, "org.opennms.core.utils.debug", DEFAULT_MAILER_DEBUG);
        m_mailHost = PropertiesUtils.getProperty(m_mailProps, "org.opennms.core.utils.mailHost", DEFAULT_MAIL_HOST);
        m_useJMTA = PropertiesUtils.getProperty(m_mailProps, "org.opennms.core.utils.useJMTA", DEFAULT_USE_JMTA);
        m_mailer = PropertiesUtils.getProperty(m_mailProps, "org.opennms.core.utils.mailer", DEFAULT_MAILER);
        m_transport = PropertiesUtils.getProperty(m_mailProps, "org.opennms.core.utils.transport", DEFAULT_TRANSPORT);
        m_from = PropertiesUtils.getProperty(m_mailProps, "org.opennms.core.utils.fromAddress", DEFAULT_FROM_ADDRESS);
        m_authenticate = PropertiesUtils.getProperty(m_mailProps, "org.opennms.core.utils.authenticate", DEFAULT_AUTHENTICATE);
        m_user = PropertiesUtils.getProperty(m_mailProps, "org.opennms.core.utils.authenticateUser", DEFAULT_AUTHENTICATE_USER);
        m_password = PropertiesUtils.getProperty(m_mailProps, "org.opennms.core.utils.authenticatePassword", DEFAULT_AUTHENTICATE_PASSWORD);
        m_contentType = PropertiesUtils.getProperty(m_mailProps, "org.opennms.core.utils.messageContentType", DEFAULT_CONTENT_TYPE);
        m_charSet = PropertiesUtils.getProperty(m_mailProps, "org.opennms.core.utils.charset", DEFAULT_CHARSET);
        m_encoding = PropertiesUtils.getProperty(m_mailProps, "org.opennms.core.utils.encoding", DEFAULT_ENCODING);
        m_startTlsEnabled = PropertiesUtils.getProperty(m_mailProps, "org.opennms.core.utils.starttls.enable", DEFAULT_STARTTLS_ENABLE);
        m_quitWait = PropertiesUtils.getProperty(m_mailProps, "org.opennms.core.utils.quitwait", DEFAULT_QUIT_WAIT);
        m_smtpPort = PropertiesUtils.getProperty(m_mailProps, "org.opennms.core.utils.smtpport", DEFAULT_SMTP_PORT);
        m_smtpSsl = PropertiesUtils.getProperty(m_mailProps, "org.opennms.core.utils.smtpssl.enable", DEFAULT_SMTP_SSL_ENABLE);

        //Set the actual JavaMailProperties... any that are defined in the file will not be overridden
        //Eventually, all configuration will be defined in properties and this strange parsing will not happen
        //TODO: fix this craziness!
        
        if (!m_mailProps.containsKey("mail.smtp.auth")) {
            m_mailProps.setProperty("mail.smtp.auth", String.valueOf(isAuthenticate()));
        }
        if (!m_mailProps.containsKey("mail.smtp.starttls.enable")) {
            m_mailProps.setProperty("mail.smtp.starttls.enable", String.valueOf(isStartTlsEnabled()));
        }
        if (!m_mailProps.containsKey("mail.smtp.quitwait")) {
            m_mailProps.setProperty("mail.smtp.quitwait", String.valueOf(isQuitWait()));
        }
        if (!m_mailProps.containsKey("mail.smtp.port")) {
            m_mailProps.setProperty("mail.smtp.port", String.valueOf(getSmtpPort()));
        }
        if (isSmtpSsl()) {
            if (!m_mailProps.containsKey("mail.smtps.auth")) {
                m_mailProps.setProperty("mail.smtps.auth", String.valueOf(isAuthenticate()));
            }
            if (!m_mailProps.containsKey("mail.smtps.socketFactory.class")) {
                m_mailProps.setProperty("mail.smtps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            }
            if (!m_mailProps.containsKey("mail.smtps.socketFactory.port")) {
                m_mailProps.setProperty("mail.smtps.socketFactory.port", String.valueOf(getSmtpPort()));
            }
//            if (!getMailProps().containsKey("mail.smtp.socketFactory.fallback")) {
//                getMailProps().setProperty("mail.smtp.socketFactory.fallback", "false");
//            }
        }
        
        if (!m_mailProps.containsKey("mail.smtp.quitwait")) {
            m_mailProps.setProperty("mail.smtp.quitwait", "true");
        }
        //getMailProps().setProperty("mail.store.protocol", "pop3");
        
    }

    /**
     * Sends a message based on properties set on this bean.
     */
    public void mailSend() throws JavaMailerException {
        log().debug(createSendLogMsg());        
        sendMessage(buildMessage());
    }

    /**
     * Helper method to create an Authenticator based on Password Authentication
     * @return
     */
    public Authenticator createAuthenticator() {
        Authenticator auth;
        if (isAuthenticate()) {
            auth = new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(getUser(), getPassword());
                }
            };
        } else {
            auth = null;
        }
        return auth;
    }

    /**
     * Build a complete message ready for sending.
     * 
     * @param session session to use to create a new MimeMessage
     * @return completed message, ready to be passed to Transport.sendMessage
     * @throws JavaMailerException if any of the underlying operations fail
     */
    public Message buildMessage() throws JavaMailerException {
        try {
            checkEnvelopeAndContents();
            Message message = initializeMessage();

            String encodedText = MimeUtility.encodeText(getMessageText(), m_charSet, m_encoding);
            if ((getFileName() == null) && (getInputStream() == null))  {
                message.setContent(encodedText, m_contentType+"; charset="+m_charSet);
            } else if (getFileName() == null) {
                BodyPart streamBodyPart = new MimeBodyPart();
                streamBodyPart.setDataHandler(new DataHandler(new InputStreamDataSource(m_inputStreamName, m_inputStreamContentType, m_inputStream)));
                streamBodyPart.setFileName(m_inputStreamName);
                streamBodyPart.setHeader("Content-Transfer-Encoding", "base64");  
                streamBodyPart.setDisposition(Part.ATTACHMENT); 
                MimeMultipart mp = new MimeMultipart();
                mp.addBodyPart(streamBodyPart);
                message.setContent(mp);
                } else {
                    BodyPart bp = new MimeBodyPart();
                    bp.setContent(encodedText, m_contentType+"; charset="+m_charSet);
                    MimeMultipart mp = new MimeMultipart();
                    mp.addBodyPart(bp);
                    mp = new MimeMultipart();
                    mp.addBodyPart(createFileAttachment(new File(getFileName())));
                    message.setContent(mp);
                }

            message.setHeader("X-Mailer", getMailer());
            message.setSentDate(new Date());

            message.saveChanges();

            return message;
        } catch (AddressException e) {
            log().error("Java Mailer Addressing exception: ", e);
            throw new JavaMailerException("Java Mailer Addressing exception: ", e);
        } catch (MessagingException e) {
            log().error("Java Mailer messaging exception: ", e);
            throw new JavaMailerException("Java Mailer messaging exception: ", e);
        } catch (UnsupportedEncodingException e) {
            log().error("Java Mailer messaging exception: ", e);
            throw new JavaMailerException("Java Mailer encoding exception: ", e);
        }
    }

    /**
     * Helper method to that creates a MIME message.
     * @param session
     * @return
     * @throws MessagingException
     * @throws AddressException
     */
    private Message initializeMessage() throws MessagingException, AddressException {
        Message message;
        message = new MimeMessage(getSession());
        message.setFrom(new InternetAddress(getFrom()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(getTo(), false));
        message.setSubject(getSubject());
        return message;
    }

    /**
     * @return
     */
    private String createSendLogMsg() {
        StringBuffer sb = new StringBuffer();
        sb.append("\n\tTo: ");
        sb.append(getTo());
        sb.append("\n\tFrom: ");
        sb.append(getFrom());
        sb.append("\n\tSubject is: ");
        sb.append(getSubject());
        sb.append("\n\tFile: ");
        sb.append(getFileName()!=null ? getFileName() : "no file attached");
        sb.append("\n\n");
        sb.append(getMessageText());
        sb.append("\n");
        return sb.toString();
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
    public MimeBodyPart createFileAttachment(final File file) throws MessagingException, JavaMailerException {
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
        if ("".equals(getFrom())) {
            throw new JavaMailerException("Cannot have an empty from address.");
        }
        if (getTo() == null) {
            throw new JavaMailerException("Cannot have a null to address.");
        }
        if ("".equals(getTo())) {
            throw new JavaMailerException("Cannot have an empty to address.");
        }
        if (getSubject() == null) {
            throw new JavaMailerException("Cannot have a null subject.");
        }
        if ("".equals(getSubject())) {
            throw new JavaMailerException("Cannot have an empty subject.");
        }
        if (getMessageText() == null) {
            throw new JavaMailerException("Cannot have a null messageText.");
        }
        if ("".equals(getMessageText())) {
            throw new JavaMailerException("Cannot have an empty messageText.");
        }
    }

    /**
     * Send message.
     * 
     * @param session
     * @param message
     * @throws JavaMailerException
     */
    public void sendMessage(Message message) throws JavaMailerException {
        Transport t = null;
        try {
            t = getSession().getTransport(getTransport());
            log().debug("for transport name '" + getTransport() + "' got: " + t.getClass().getName() + "@" + Integer.toHexString(t.hashCode()));

            LoggingTransportListener listener = new LoggingTransportListener(log());
            t.addTransportListener(listener);

            if (t.getURLName().getProtocol().equals("mta")) {
                // JMTA throws an AuthenticationFailedException if we call connect()
                log().debug("transport is 'mta', not trying to connect()");
            } else if (isAuthenticate()) {
                log().debug("authenticating to " + getMailHost());
                t.connect(getMailHost(), getSmtpPort(), getUser(), getPassword());
            } else {
                log().debug("not authenticating to " + getMailHost());
                t.connect(getMailHost(), getSmtpPort(), null, null);
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
    
    private class InputStreamDataSource implements DataSource {  
        
        private String name;  
        private String contentType;  
        private ByteArrayOutputStream baos;  
          
        InputStreamDataSource(String name, String contentType, InputStream inputStream) throws JavaMailerException {  
            this.name = name;  
            this.contentType = contentType;
            
            log().debug("setting contentType " + this.contentType);
              
            baos = new ByteArrayOutputStream();  
              
            int read;  
            byte[] buff = new byte[256];  
            try {
                while((read = inputStream.read(buff)) != -1) {  
                    baos.write(buff, 0, read);  
                }
            } catch (IOException e) {
                log().error("Could not read attachment from input stream: " + e, e);
                throw new JavaMailerException("Could not read attachment from input stream: " + e, e);
            }  
        }  
          
        public String getContentType() {
            log().debug("getContentType: " + contentType);
            return contentType;  
        }  
   
        public InputStream getInputStream() throws IOException {  
            return new ByteArrayInputStream(baos.toByteArray());  
        }  
   
        public String getName() {  
            return name;  
        }  
   
        public OutputStream getOutputStream() throws IOException {  
            throw new IOException("Cannot write to this read-only resource");  
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
     * @return Returns the input stream attachment.
     */
    public InputStream getInputStream() {
        return m_inputStream;
    }

    /**
     * @param inputStream
     *            Sets the input stream to be attached to the message.
     */
    public void setInputStream(InputStream inputStream) {
        m_inputStream = inputStream;
    }
    
    /**
     * @return Returns the name to use for stream attachments..
     */
    public String getInputStreamName() {
        return m_inputStreamName;
    }

    /**
     * @param inputStreamName
     *            Sets the name to use for stream attachments.
     */
    public void setInputStreamName(String inputStreamName) {
        m_inputStreamName = inputStreamName;
    }
    
    /**
     * @return Returns the name to use for stream attachments..
     */
    public String getInputStreamContentType() {
        return m_inputStreamContentType;
    }

    /**
     * @param inputStreamName
     *            Sets the name to use for stream attachments.
     */
    public void setInputStreamContentType(String inputStreamContentType) {
        m_inputStreamContentType = inputStreamContentType;
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
        if (isDebug()) {
            m_session.setDebugOut(new PrintStream(new LoggingByteArrayOutputStream(log()), true));
        }
        m_session.setDebug(isDebug());
    }

    /**
     * @return log4j Category
     */
    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    public static class LoggingByteArrayOutputStream extends ByteArrayOutputStream {
        private ThreadCategory m_category;

        public LoggingByteArrayOutputStream(ThreadCategory threadCategory) {
            m_category = threadCategory;
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
        private ThreadCategory m_category;
        private List<Address> m_invalidAddresses = new ArrayList<Address>();
        private List<Address> m_validSentAddresses = new ArrayList<Address>();
        private List<Address> m_validUnsentAddresses = new ArrayList<Address>();

        public LoggingTransportListener(ThreadCategory threadCategory) {
            m_category = threadCategory;
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

    /**
     * @return the session
     */
    public Session getSession() {
        return m_session;
    }

    /**
     * @param session the session to set
     */
    public void setSession(Session session) {
        m_session = session;
    }

    /**
     * @return the contentType
     */
    public String getContentType() {
        return m_contentType;
    }

    /**
     * @param contentType the contentType to set
     */
    public void setContentType(String contentType) {
        m_contentType = contentType;
    }

    /**
     * @return the charSet
     */
    public String getCharSet() {
        return m_charSet;
    }

    /**
     * @param charSet the charSet to set
     */
    public void setCharSet(String charSet) {
        m_charSet = charSet;
    }

    /**
     * @return the encoding
     */
    public String getEncoding() {
        return m_encoding;
    }

    /**
     * @param encoding the encoding to set
     */
    public void setEncoding(String encoding) {
        m_encoding = encoding;
    }

    /**
     * @return the startTlsEnabled
     */
    public boolean isStartTlsEnabled() {
        return m_startTlsEnabled;
    }

    /**
     * @param startTlsEnabled the startTlsEnabled to set
     */
    public void setStartTlsEnabled(boolean startTlsEnabled) {
        m_startTlsEnabled = startTlsEnabled;
    }

    /**
     * @return the quitWait
     */
    public boolean isQuitWait() {
        return m_quitWait;
    }

    /**
     * @param quitWait the quitWait to set
     */
    public void setQuitWait(boolean quitWait) {
        m_quitWait = quitWait;
    }

    /**
     * @return the smtpPort
     */
    public int getSmtpPort() {
        return m_smtpPort;
    }

    /**
     * @param smtpPort the smtpPort to set
     */
    public void setSmtpPort(int smtpPort) {
        m_smtpPort = smtpPort;
    }

    /**
     * @return the smtpSsl
     */
    public boolean isSmtpSsl() {
        return m_smtpSsl;
    }

    /**
     * @param smtpSsl the smtpSsl to set
     */
    public void setSmtpSsl(boolean smtpSsl) {
        m_smtpSsl = smtpSsl;
    }

    /**
     * This returns the properties configured in the javamail-configuration.properties file.
     * @return
     */
    public Properties getMailProps() {
        return m_mailProps;
    }

}
