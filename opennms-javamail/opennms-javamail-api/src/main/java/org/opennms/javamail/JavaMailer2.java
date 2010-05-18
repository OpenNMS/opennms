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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.event.TransportEvent;
import javax.mail.event.TransportListener;
import javax.mail.internet.MimeBodyPart;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.util.StringUtils;

/**
 * Sends an email message using the Java Mail API
 * 
 * @author <A HREF="mailto:david@opennms.org">David Hustace </A>
 */
public abstract class JavaMailer2 {

    private Session m_session = null;
    private Properties m_mailProps;
    
    public JavaMailer2(Properties javamailProps) throws JavaMailerException {
    }

    /**
     * Default constructor.  Default properties from javamailer-properties are set into session.  To change these
     * properties, retrieve the current properties from the session and override as needed.
     * @throws IOException 
     */
    public JavaMailer2() throws JavaMailerException {
        this(new Properties());
    }


    /**
     * Helper method to create an Authenticator based on Password Authentication
     * @return
     */
    public Authenticator createAuthenticator(final String user, final String password) {
        Authenticator auth;
        auth = new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(user, password);
            }
        };
        return auth;
    }

    /*
    public Message buildMessage(String m_charSet, String m_encoding, String m_contentType) throws JavaMailerException {
        try {

            String encodedText = MimeUtility.encodeText(getMessageText(), m_charSet, m_encoding);
            if (getFileName() == null) {
                message.setContent(encodedText, m_contentType+"; charset="+m_charSet);
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
    */

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


    public void setDebug(boolean debug) {
        if (debug) {
            m_session.setDebugOut(new PrintStream(new LoggingByteArrayOutputStream(log()), true));
        }
        m_session.setDebug(debug);
    }

    /**
     * @return log4j Category
     */
    protected static ThreadCategory log() {
        return ThreadCategory.getInstance();
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
     * This returns the properties configured in the javamail-configuration.properties file.
     * @return
     */
    public Properties getMailProps() {
        return m_mailProps;
    }

}
