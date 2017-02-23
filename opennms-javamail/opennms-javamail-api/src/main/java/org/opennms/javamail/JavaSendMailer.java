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
import java.io.PrintStream;
import java.util.List;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import org.opennms.core.utils.PropertiesUtils;
import org.opennms.netmgt.config.javamail.JavamailProperty;
import org.opennms.netmgt.config.javamail.SendmailConfig;
import org.opennms.netmgt.config.javamail.SendmailMessage;
import org.opennms.netmgt.config.javamail.SendmailProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.MimeMessageHelper;

/**
 * Use this class for sending e-mails.
 * <p>Crude extension of JavaMailer</p>
 * <p>TODO: Improve class hierarchy</p>
 * <p>TODO: Needs testing</p>
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class JavaSendMailer extends JavaMailer2 {

    /** The Constant LOG. */
    private static final Logger LOG = LoggerFactory.getLogger(JavaSendMailer.class);

    /** The properties. */
    private Properties m_properties;

    /** The sendmail configuration. */
    private SendmailConfig m_config;

    /** The MIME message. */
    private MimeMessage m_message;

    /** The session. */
    private Session m_session;

    /**
     * Instantiates a new java send mailer.
     * <p>Constructs everything required to call send().</p>
     *
     * @param config the sendmail configuration
     * @param useJmProps a boolean representing the handling of the deprecated javamail-configuration.properties file.
     * @throws JavaMailerException the java mailer exception
     */
    public JavaSendMailer(SendmailConfig config, boolean useJmProps) throws JavaMailerException {
        m_config = config;
        try {
            m_session = Session.getInstance(createProps(useJmProps), createAuthenticator());
            m_message = buildMimeMessage(config.getSendmailMessage());
            if (m_config.isDebug()) {
                m_session.setDebugOut(new PrintStream(new LoggingByteArrayOutputStream()));
            }
            m_session.setDebug(m_config.isDebug());

        } catch (IOException e) {
            throw new JavaMailerException("IO problem creating session", e);
        }
    }

    /**
     * Instantiates a new java send mailer.
     * <p>Using this constructor implies overriding sendmail configuration with properties
     * from the deprecated javamail-configuration.properties file.</p>
     *
     * @param config the sendmail configuration
     * @throws JavaMailerException the java mailer exception
     */
    public JavaSendMailer(SendmailConfig config) throws JavaMailerException {
        this(config, true);
    }

    /**
     * Builds the mime message.
     *
     * @param msg the sendmail message
     * @return the mime message
     */
    public MimeMessage buildMimeMessage(SendmailMessage msg) {
        //no need to set the same object again
        if (m_config.getSendmailMessage() != msg) {
            m_config.setSendmailMessage(msg);
        }
        MimeMessage mimeMsg = new MimeMessage(m_session);
        try {
            MimeMessageHelper helper = new MimeMessageHelper(mimeMsg, false, m_config.getSendmailProtocol().getCharSet());
            helper.setFrom(m_config.getSendmailMessage().getFrom());
            helper.setTo(m_config.getSendmailMessage().getTo());
            helper.setSubject(m_config.getSendmailMessage().getSubject());
        } catch (MessagingException e) {
            LOG.warn("found a problem building message: {}", e.getMessage());
        }
        return mimeMsg;
    }

    /**
     * Creates the authenticator.
     *
     * @return the authenticator
     */
    public Authenticator createAuthenticator() {
        Authenticator auth;
        if (m_config.isUseAuthentication()) {
            auth = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(m_config.getUserAuth().getUserName(), m_config.getUserAuth().getPassword());
                }
            };
        } else {
            auth = null;
        }
        return auth;
    }

    /**
     * Creates the props.
     *
     * @param useJmProps a boolean representing the handling of the deprecated javamail-configuration.properties file.
     * @return the properties
     * @throws IOException Signals that an I/O exception has occurred.
     */
    private Properties createProps(boolean useJmProps) throws IOException {

        Properties props = generatePropsFromConfig(m_config.getJavamailPropertyCollection());
        configureProperties(props, useJmProps);

        //get rid of this
        return Session.getDefaultInstance(new Properties()).getProperties();
    }

    /**
     * Generate props from configuration.
     *
     * @param javamailPropertyCollection the javamail property collection
     * @return the properties
     */
    private Properties generatePropsFromConfig(List<JavamailProperty> javamailPropertyCollection) {
        Properties props = new Properties();
        for (JavamailProperty property : javamailPropertyCollection) {
            props.put(property.getName(), property.getValue());
        }
        return props;
    }

    /**
     * Configure properties.
     * <p>This method uses a properties file reader to pull in opennms styled javamail properties and sets
     * the actual javamail properties.  This is here to preserve the backwards compatibility but configuration
     * will probably change soon.</p>
     * <p>FIXME definitely will change soon, will be deprecated.</p>
     *
     * @param sendmailConfigDefinedProps the sendmail configuration defined properties
     * @param useJmProps a boolean representing the handling of the deprecated javamail-configuration.properties file.
     */
    private void configureProperties(Properties sendmailConfigDefinedProps, boolean useJmProps) {

        //this loads the properties from the old style javamail-configuration.properties
        //TODO: deprecate this
        Properties props = null;
        try {
            props = JavaMailerConfig.getProperties();

            /* These strange properties from javamail-configuration.properties need to be translated into actual javax.mail properties
             * FIXME: The precedence of the properties file vs. the SendmailConfiguration should probably be addressed here
             * FIXME: if using a valid sendmail config, it probably doesn't make sense to use any of these properties
             */
            if (useJmProps) {
                m_config.setDebug(PropertiesUtils.getProperty(props, "org.opennms.core.utils.debug", m_config.isDebug()));
                m_config.getSendmailHost().setHost(PropertiesUtils.getProperty(props, "org.opennms.core.utils.mailHost", m_config.getSendmailHost().getHost()));
                m_config.setUseJmta(PropertiesUtils.getProperty(props, "org.opennms.core.utils.useJMTA", m_config.isUseJmta()));
                m_config.getSendmailProtocol().setMailer(PropertiesUtils.getProperty(props, "org.opennms.core.utils.mailer", m_config.getSendmailProtocol().getMailer()));
                m_config.getSendmailProtocol().setTransport(PropertiesUtils.getProperty(props, "org.opennms.core.utils.transport", m_config.getSendmailProtocol().getTransport()));
                m_config.getSendmailMessage().setFrom(PropertiesUtils.getProperty(props, "org.opennms.core.utils.fromAddress", m_config.getSendmailMessage().getFrom()));
                m_config.setUseAuthentication(PropertiesUtils.getProperty(props, "org.opennms.core.utils.authenticate", m_config.isUseAuthentication()));
                m_config.getUserAuth().setUserName(PropertiesUtils.getProperty(props, "org.opennms.core.utils.authenticateUser", m_config.getUserAuth().getUserName()));
                m_config.getUserAuth().setPassword(PropertiesUtils.getProperty(props, "org.opennms.core.utils.authenticatePassword", m_config.getUserAuth().getPassword()));
                m_config.getSendmailProtocol().setMessageContentType(PropertiesUtils.getProperty(props, "org.opennms.core.utils.messageContentType", m_config.getSendmailProtocol().getMessageContentType()));
                m_config.getSendmailProtocol().setCharSet(PropertiesUtils.getProperty(props, "org.opennms.core.utils.charset", m_config.getSendmailProtocol().getCharSet()));
                m_config.getSendmailProtocol().setMessageEncoding(PropertiesUtils.getProperty(props, "org.opennms.core.utils.encoding", m_config.getSendmailProtocol().getMessageEncoding()));
                m_config.getSendmailProtocol().setStartTls(PropertiesUtils.getProperty(props, "org.opennms.core.utils.starttls.enable", m_config.getSendmailProtocol().isStartTls()));
                m_config.getSendmailProtocol().setQuitWait(PropertiesUtils.getProperty(props, "org.opennms.core.utils.quitwait", m_config.getSendmailProtocol().isQuitWait()));
                m_config.getSendmailHost().setPort(PropertiesUtils.getProperty(props, "org.opennms.core.utils.smtpport", m_config.getSendmailHost().getPort()));
                m_config.getSendmailProtocol().setSslEnable(PropertiesUtils.getProperty(props, "org.opennms.core.utils.smtpssl.enable", m_config.getSendmailProtocol().isSslEnable()));
            }
        } catch (IOException e) {
            LOG.info("configureProperties: could not load javamail.properties, continuing for is no longer required", e);
        }

        //this sets any javamail properties that were set in the SendmailConfig object
        if (props == null) {
            props = new Properties();
        }

        props.putAll(sendmailConfigDefinedProps);

        if (!props.containsKey("mail.smtp.auth")) {
            props.setProperty("mail.smtp.auth", String.valueOf(m_config.isUseAuthentication()));
        }
        if (!props.containsKey("mail.smtp.starttls.enable")) {
            props.setProperty("mail.smtp.starttls.enable", String.valueOf(m_config.getSendmailProtocol().isStartTls()));
        }
        if (!props.containsKey("mail.smtp.quitwait")) {
            props.setProperty("mail.smtp.quitwait", String.valueOf(m_config.getSendmailProtocol().isQuitWait()));
        }
        if (!props.containsKey("mail.smtp.port")) {
            props.setProperty("mail.smtp.port", String.valueOf(m_config.getSendmailHost().getPort()));
        }
        if (m_config.getSendmailProtocol().isSslEnable()) {
            if (!props.containsKey("mail.smtps.auth")) {
                props.setProperty("mail.smtps.auth", String.valueOf(m_config.isUseAuthentication()));
            }
            if (!props.containsKey("mail.smtps.socketFactory.class")) {
                props.setProperty("mail.smtps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            }
            if (!props.containsKey("mail.smtps.socketFactory.port")) {
                props.setProperty("mail.smtps.socketFactory.port", String.valueOf(m_config.getSendmailHost().getPort()));
            }
        }

        if (!props.containsKey("mail.smtp.quitwait")) {
            props.setProperty("mail.smtp.quitwait", String.valueOf(m_config.getSendmailProtocol().isQuitWait()));
        }

    }

    /**
     * Send.
     *
     * @throws JavaMailerException the java mailer exception
     */
    public void send() throws JavaMailerException {
        try {
            if ("text/plain".equals(m_config.getSendmailProtocol().getMessageContentType().toLowerCase())) {
                m_message.setText(m_config.getSendmailMessage().getBody());
            } else {
                m_message.setContent(m_config.getSendmailMessage().getBody(), m_config.getSendmailProtocol().getMessageContentType());
            }
        } catch (MessagingException e) {
            LOG.error("Java Mailer messaging exception: {}", e, e);
            throw new JavaMailerException("Java Mailer messaging exception: " + e, e);
        }
        send(m_message);
    }

    /**
     * Send.
     *
     * @param message the message
     * @throws JavaMailerException the java mailer exception
     */
    public void send(MimeMessage message) throws JavaMailerException {
        Transport t = null;
        try {
            SendmailProtocol protoConfig = m_config.getSendmailProtocol();
            t = m_session.getTransport(protoConfig.getTransport());
            LOG.debug("for transport name '{}' got: {}@{}", protoConfig.getTransport(), t.getClass().getName(), Integer.toHexString(t.hashCode()));

            LoggingTransportListener listener = new LoggingTransportListener();
            t.addTransportListener(listener);

            if (t.getURLName().getProtocol().equals("mta")) {
                // JMTA throws an AuthenticationFailedException if we call connect()
                LOG.debug("transport is 'mta', not trying to connect()");
            } else if (m_config.isUseAuthentication()) {
                LOG.debug("authenticating to {}", m_config.getSendmailHost().getHost());
                t.connect(m_config.getSendmailHost().getHost(), m_config.getSendmailHost().getPort(), m_config.getUserAuth().getUserName(), m_config.getUserAuth().getPassword());
            } else {
                LOG.debug("not authenticating to {}", m_config.getSendmailHost().getHost());
                t.connect(m_config.getSendmailHost().getHost(), m_config.getSendmailHost().getPort(), null, null);
            }

            t.sendMessage(message, message.getAllRecipients());
            listener.assertAllMessagesDelivered();
        } catch (NoSuchProviderException e) {
            LOG.error("Couldn't get a transport: {}", e, e);
            throw new JavaMailerException("Couldn't get a transport: " + e, e);
        } catch (MessagingException e) {
            LOG.error("Java Mailer messaging exception: {}", e, e);
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
     * Sets the sendmail configuration.
     *
     * @param config the new sendmail configuration
     */
    public void setConfig(SendmailConfig config) {
        m_config = config;
    }


    /**
     * Gets the sendmail configuration.
     *
     * @return the sendmail configuration
     */
    public SendmailConfig getConfig() {
        return m_config;
    }


    /**
     * Sets the message.
     *
     * @param message the new message
     */
    public void setMessage(MimeMessage message) {
        m_message = message;
    }


    /**
     * Gets the message.
     *
     * @return the message
     */
    public MimeMessage getMessage() {
        return m_message;
    }


    /**
     * Sets the properties.
     *
     * @param properties the new properties
     */
    public void setProperties(Properties properties) {
        m_properties = properties;
    }


    /**
     * Gets the properties.
     *
     * @return the properties
     */
    public Properties getProperties() {
        return m_properties;
    }

}
