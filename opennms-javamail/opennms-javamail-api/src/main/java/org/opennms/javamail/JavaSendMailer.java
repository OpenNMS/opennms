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
import java.nio.charset.Charset;
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
import org.opennms.netmgt.config.javamail.SendmailHost;
import org.opennms.netmgt.config.javamail.SendmailMessage;
import org.opennms.netmgt.config.javamail.SendmailProtocol;
import org.opennms.netmgt.config.javamail.UserAuth;
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
            if (config.getSendmailMessage() != null) {
                m_message = buildMimeMessage(config.getSendmailMessage());
            } else {
                throw new JavaMailerException("Unable to build mime message: sendmail-message missing from config!");
            }
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
    public MimeMessage buildMimeMessage(final SendmailMessage msg) {
        MimeMessage mimeMsg = new MimeMessage(m_session);
        if (m_config.getSendmailMessage() != msg) {
            m_config.setSendmailMessage(msg);
        }

        if (m_config.getSendmailMessage() != null) {
            final SendmailMessage configMsg = m_config.getSendmailMessage();

            try {
                final String charset = m_config.getSendmailProtocol() != null? m_config.getSendmailProtocol().getCharSet() : Charset.defaultCharset().name();
                final MimeMessageHelper helper = new MimeMessageHelper(mimeMsg, false, charset);
                helper.setFrom(configMsg.getFrom());
                helper.setTo(configMsg.getTo());
                helper.setSubject(configMsg.getSubject());
            } catch (final MessagingException e) {
                LOG.warn("found a problem building message: {}", e.getMessage());
            }
        } else {
            LOG.warn("Missing sendmail message configuration. This MIME message will probably be wrong.");
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
                    if (m_config.getUserAuth() != null) {
                        final UserAuth userAuth = m_config.getUserAuth();
                        return new PasswordAuthentication(userAuth.getUserName(), userAuth.getPassword());
                    }
                    LOG.debug("No user authentication configured.");
                    return new PasswordAuthentication(null,null);
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

        Properties props = generatePropsFromConfig(m_config.getJavamailProperties());
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
                if (m_config.getSendmailHost() != null) {
                    final SendmailHost sendmailHost = m_config.getSendmailHost();
                    sendmailHost.setHost(PropertiesUtils.getProperty(props, "org.opennms.core.utils.mailHost", sendmailHost.getHost()));
                    sendmailHost.setPort(PropertiesUtils.getProperty(props, "org.opennms.core.utils.smtpport", sendmailHost.getPort()));
                }
                if (m_config.getSendmailProtocol() != null) {
                    final SendmailProtocol sendmailProtocol = m_config.getSendmailProtocol();
                    sendmailProtocol.setMailer(PropertiesUtils.getProperty(props, "org.opennms.core.utils.mailer", sendmailProtocol.getMailer()));
                    sendmailProtocol.setTransport(PropertiesUtils.getProperty(props, "org.opennms.core.utils.transport", sendmailProtocol.getTransport()));
                    sendmailProtocol.setMessageContentType(PropertiesUtils.getProperty(props, "org.opennms.core.utils.messageContentType", sendmailProtocol.getMessageContentType()));
                    sendmailProtocol.setCharSet(PropertiesUtils.getProperty(props, "org.opennms.core.utils.charset", sendmailProtocol.getCharSet()));
                    sendmailProtocol.setMessageEncoding(PropertiesUtils.getProperty(props, "org.opennms.core.utils.encoding", sendmailProtocol.getMessageEncoding()));
                    sendmailProtocol.setStartTls(PropertiesUtils.getProperty(props, "org.opennms.core.utils.starttls.enable", sendmailProtocol.isStartTls()));
                    sendmailProtocol.setQuitWait(PropertiesUtils.getProperty(props, "org.opennms.core.utils.quitwait", sendmailProtocol.isQuitWait()));
                    sendmailProtocol.setSslEnable(PropertiesUtils.getProperty(props, "org.opennms.core.utils.smtpssl.enable", sendmailProtocol.isSslEnable()));
                }
                if (m_config.getUserAuth() != null) {
                    final UserAuth userAuth = m_config.getUserAuth();
                    userAuth.setUserName(PropertiesUtils.getProperty(props, "org.opennms.core.utils.authenticateUser", userAuth.getUserName()));
                    userAuth.setPassword(PropertiesUtils.getProperty(props, "org.opennms.core.utils.authenticatePassword", userAuth.getPassword()));
                }
                if (m_config.getSendmailMessage() != null) {
                    final SendmailMessage sendmailMessage = m_config.getSendmailMessage();
                    sendmailMessage.setFrom(PropertiesUtils.getProperty(props, "org.opennms.core.utils.fromAddress", sendmailMessage.getFrom()));
                }
                m_config.setUseJmta(PropertiesUtils.getProperty(props, "org.opennms.core.utils.useJMTA", m_config.isUseJmta()));
                m_config.setUseAuthentication(PropertiesUtils.getProperty(props, "org.opennms.core.utils.authenticate", m_config.isUseAuthentication()));
            }
        } catch (IOException e) {
            LOG.info("configureProperties: could not load javamail.properties, continuing for is no longer required", e);
        }

        //this sets any javamail properties that were set in the SendmailConfig object
        if (props == null) {
            props = new Properties();
        }

        props.putAll(sendmailConfigDefinedProps);

        if (m_config.getSendmailProtocol() != null) {
            final SendmailProtocol sendmailProtocol = m_config.getSendmailProtocol();
            if (!props.containsKey("mail.smtp.starttls.enable")) {
                props.setProperty("mail.smtp.starttls.enable", String.valueOf(sendmailProtocol.isStartTls()));
            }
            if (!props.containsKey("mail.smtp.quitwait")) {
                props.setProperty("mail.smtp.quitwait", String.valueOf(sendmailProtocol.isQuitWait()));
            }
            if (!props.containsKey("mail.smtp.quitwait")) {
                props.setProperty("mail.smtp.quitwait", String.valueOf(sendmailProtocol.isQuitWait()));
            }
            if (sendmailProtocol.isSslEnable()) {
                if (!props.containsKey("mail.smtps.auth")) {
                    props.setProperty("mail.smtps.auth", String.valueOf(m_config.isUseAuthentication()));
                }
                if (!props.containsKey("mail.smtps.socketFactory.class")) {
                    props.setProperty("mail.smtps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
                }
                if (!props.containsKey("mail.smtps.socketFactory.port") && m_config.getSendmailHost() != null) {
                    props.setProperty("mail.smtps.socketFactory.port", String.valueOf(m_config.getSendmailHost().getPort()));
                }
            }
        }

        if (!props.containsKey("mail.smtp.auth")) {
            props.setProperty("mail.smtp.auth", String.valueOf(m_config.isUseAuthentication()));
        }
        if (!props.containsKey("mail.smtp.port") && m_config.getSendmailHost() != null) {
            props.setProperty("mail.smtp.port", String.valueOf(m_config.getSendmailHost().getPort()));
        }


    }

    /**
     * Send.
     *
     * @throws JavaMailerException the java mailer exception
     */
    public void send() throws JavaMailerException {
        if (m_config.getSendmailProtocol() == null || m_config.getSendmailMessage() == null) {
            throw new JavaMailerException("sendmail-protocol or sendmail-message are not configured!");
        }
        try {
            final SendmailProtocol sendmailProtocol = m_config.getSendmailProtocol();
            final String body = m_config.getSendmailMessage().getBody();
            if ("text/plain".equals(sendmailProtocol.getMessageContentType().toLowerCase())) {
                m_message.setText(body);
            } else {
                m_message.setContent(body, sendmailProtocol.getMessageContentType());
            }
        } catch (final MessagingException e) {
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
        if (m_config.getSendmailProtocol() == null || m_config.getSendmailHost() == null) {
            throw new JavaMailerException("sendmail-protocol or sendmail-host are not configured!");
        }
        try {
            SendmailProtocol protoConfig = m_config.getSendmailProtocol();
            t = m_session.getTransport(protoConfig.getTransport());
            LOG.debug("for transport name '{}' got: {}@{}", protoConfig.getTransport(), t.getClass().getName(), Integer.toHexString(t.hashCode()));

            LoggingTransportListener listener = new LoggingTransportListener();
            t.addTransportListener(listener);

            if ("mta".equals(t.getURLName().getProtocol())) {
                // JMTA throws an AuthenticationFailedException if we call connect()
                LOG.debug("transport is 'mta', not trying to connect()");
            } else {
                final SendmailHost sendmailHost = m_config.getSendmailHost();
                if (m_config.isUseAuthentication() && m_config.getUserAuth() != null) {
                    LOG.debug("authenticating to {}", sendmailHost.getHost());
                    final UserAuth userAuth = m_config.getUserAuth();
                    t.connect(sendmailHost.getHost(), sendmailHost.getPort(), userAuth.getUserName(), userAuth.getPassword());
                } else {
                    LOG.debug("not authenticating to {}", sendmailHost.getHost());
                    t.connect(sendmailHost.getHost(), sendmailHost.getPort(), null, null);
                }
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
