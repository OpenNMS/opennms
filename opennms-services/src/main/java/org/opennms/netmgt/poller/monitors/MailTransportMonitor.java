/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.poller.monitors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.HeaderTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.core.utils.TimeoutTracker;
import org.opennms.javamail.JavaMailer;
import org.opennms.javamail.JavaMailerException;
import org.opennms.netmgt.config.mailtransporttest.JavamailProperty;
import org.opennms.netmgt.config.mailtransporttest.ReadmailTest;
import org.opennms.netmgt.config.mailtransporttest.SendmailTest;
import org.opennms.netmgt.model.PollStatus;
import org.opennms.netmgt.poller.Distributable;
import org.opennms.netmgt.poller.MonitoredService;

//TODO: adjust to use new javamail-configuration.xml

/**
 * This <code>ServiceMonitor</code> is designed to monitor the transport of
 * SMTP email.
 *
 * Use cases:
 *
 * a) Class will test that it can successfully send an email.
 * b) Class will test that it can successfully connect to a mail server and get mailbox contents.
 * c) Class will test that it can successfully read a new email message from a mail server.
 * d) Class will test that it can send an email and read that same email from a mail server.
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
@Distributable
public class MailTransportMonitor extends AbstractServiceMonitor {
    private static final Logger LOG = LoggerFactory.getLogger(MailTransportMonitor.class);

    private static final String MTM_HEADER_KEY = "X-OpenNMS-MTM-ID";
    private final String m_headerValue = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));

	/** {@inheritDoc} */
    @Override
    public PollStatus poll(MonitoredService svc, Map<String, Object> parameters) {
        PollStatus status = null;

        try {
        	final MailTransportParameters mailParms = MailTransportParameters.get(parameters);
            
            try {
                if ("${ipaddr}".equals(mailParms.getReadTestHost())) {
                mailParms.setReadTestHost(svc.getIpAddr());
                }
            } catch (final IllegalStateException ise) {
                //just ignore, don't have to have a both a read and send test configured
            }

            try {
                if ("${ipaddr}".equals(mailParms.getSendTestHost())) {
                    mailParms.setSendTestHost(svc.getIpAddr());
                }
            } catch (final IllegalStateException ise) {
                //just ignore, don't have to have a both a read and send test configured
            }
            
            parseJavaMailProperties(mailParms);
            status = doMailTest(mailParms);
        } catch (final IllegalStateException ise) {
            //ignore this because we don't have to have both a send and read
            
        } catch (final Throwable e) {
            LOG.error("An error occurred while polling.", e);
            status = PollStatus.down("Exception from mailer: " + e.getLocalizedMessage());
        }

        return status;
    }

    private void parseJavaMailProperties(final MailTransportParameters mailParms) {
    	final ReadmailTest readTest = mailParms.getReadTest();

    	List<JavamailProperty> propertyList = new ArrayList<JavamailProperty>();
        if (readTest != null) {
            propertyList = readTest.getJavamailPropertyCollection();
        }

        final SendmailTest sendTest = mailParms.getSendTest();
        if (sendTest != null) {
        	final List<JavamailProperty> sendTestProperties = sendTest.getJavamailPropertyCollection();
            propertyList.addAll(sendTestProperties);
        }
        
        final Properties props = mailParms.getJavamailProperties();
        for (final JavamailProperty property : propertyList) {
            props.setProperty(property.getName(), property.getValue());
        }
        
        mailParms.setJavamailProperties(props);
    }

    /**
     * This method handles all the logic for testing mail.
     * 
     * @param mailParms
     */
    private PollStatus doMailTest(final MailTransportParameters mailParms) {
    	final long beginPoll = System.currentTimeMillis();
        PollStatus status = PollStatus.unknown("Beginning poll.");
        mailParms.setTestSubjectSuffix(Long.toString(beginPoll));

        /*
         * If both a send and receive test are configured, then were testing the
         * throughput (round trip delivery) of mail. This can be configured to
         * send and receive to the same of different hosts.
         */
        if (mailParms.getSendTest() != null && mailParms.getReadTest() != null) {

            /*
             * Doing round-trip mail test so create a unique subject for
             * matching.
             */
            mailParms.setEnd2EndTestInProgress(true);
            status = sendTestMessage(mailParms);

            if (status.isAvailable()) {
                LOG.debug("doMailTest: send test successful.");
                status = readTestMessage(mailParms);
            } else {
                LOG.info("doMailTest: send test unsuccessful... skipping read portion of test.");
            }

        } else if (mailParms.getReadTest() != null) {
            status = readTestMessage(mailParms);
        } else if (mailParms.getSendTest() != null) {
            status = sendTestMessage(mailParms);
        } else {
            throw new IllegalArgumentException("MailTransportMonitor requires either send-host or read-host parameters");
        }

        if (status.isAvailable()) {
            status.setResponseTime(Double.valueOf(String.valueOf(System.currentTimeMillis() - beginPoll)));
        }
        LOG.info("doMailTest: mailtest result: {}", status);
        return status;
    }

    private PollStatus readTestMessage(final MailTransportParameters mailParms) {
        LOG.debug("readTestMessage: Beginning read mail test.");
        PollStatus status = PollStatus.unavailable("Test not completed.");

        final long interval = mailParms.getReadTestAttemptInterval();

        if (mailParms.isEnd2EndTestInProgress()) {
            LOG.debug("Initially delaying read test: {} because end to end test is in progress.", mailParms.getReadTestAttemptInterval());
            
            if (delayTest(status, interval) == PollStatus.SERVICE_UNKNOWN) {
                return status;
            }
        }
        
        Store mailStore = null;
        Folder mailFolder = null;
        try {
        	final JavaMailer readMailer = new JavaMailer(mailParms.getJavamailProperties());
            setReadMailProperties(mailParms, readMailer);

            final TimeoutTracker tracker = new TimeoutTracker(mailParms.getParameterMap(), mailParms.getRetries(), mailParms.getTimeout());
            for (tracker.reset(); tracker.shouldRetry(); tracker.nextAttempt()) {
                tracker.startAttempt();
                
                if (tracker.getAttempt() > 0) {
                    if (delayTest(status, interval) == PollStatus.SERVICE_UNKNOWN) {
                        LOG.warn("readTestMessage: Status set to: {} during delay, exiting test.", status);
                        break;
                    }
                }
                LOG.debug("readTestMessage: reading mail attempt: {}, elapsed time: {}ms.", (tracker.getAttempt()+1), String.format("%.2f", tracker.elapsedTimeInMillis()));
                try {
                    mailStore = readMailer.getSession().getStore();
                    mailFolder = retrieveMailFolder(mailParms, mailStore);
                    mailFolder.open(Folder.READ_WRITE);
                } catch (final MessagingException e) {
                    if (tracker.shouldRetry()) {
                        LOG.warn("readTestMessage: error reading INBOX", e);
                        closeStore(mailStore, mailFolder);
                        continue;  //try again to get mail Folder from Store
                    } else {
                        LOG.warn("readTestMessage: error reading INBOX", e);
                        return PollStatus.down(e.getLocalizedMessage());
                    }
                }
                if (mailFolder.isOpen() && (mailParms.getReadTest().getSubjectMatch() != null || mailParms.isEnd2EndTestInProgress())) {
                    status = processMailSubject(mailParms, mailFolder);
                    if (status.getStatusCode() == PollStatus.SERVICE_AVAILABLE) {
                        break;
                    }
                }
            }

        } catch (final JavaMailerException e) {
            status = PollStatus.down(e.getLocalizedMessage());
        } finally {
            closeStore(mailStore, mailFolder);
        }
        return status;
    }

    /**
     * Handy method to do the try catch try of closing a mail store and folder.
     * @param mailStore
     * @param mailFolder
     */
    private void closeStore(final Store mailStore, final Folder mailFolder)  {
        try {
            if (mailFolder != null && mailFolder.isOpen()) {
                mailFolder.close(true);
            }
        } catch (final MessagingException e) {
		LOG.debug("Unable to close mail folder.", e);
        } finally {
            try {
                if (mailStore != null && mailStore.isConnected()) {
                    mailStore.close();
                }
            } catch (final MessagingException e1) {
		LOG.debug("Unable to close message store.", e1);
            }
        }
    }

    /**
     * After a mailbox has been opened, search through the retrieved messages
     * for a matching subject.
     * 
     * @param mailParms
     * @param mailFolder
     * @return a PollStatus indicative of the success of matching a subject or just retieving
     *         mail folder contents... dependent on configuration.
     */
    private PollStatus processMailSubject(final MailTransportParameters mailParms, final Folder mailFolder) {
        PollStatus status = PollStatus.unknown();
        try {
        	final String subject = computeMatchingSubject(mailParms);
            if (mailFolder.isOpen() && subject != null) {
            	final Message[] mailMessages = mailFolder.getMessages();
                final SearchTerm searchTerm = new SubjectTerm(subject);
                final SearchTerm deleteTerm = new HeaderTerm(MTM_HEADER_KEY, m_headerValue);

                LOG.debug("searchMailSubject: searching {} message(s) for subject '{}'", mailMessages.length, subject);

                boolean delete = false;
                boolean found = false;
                for (int i = 1; i <= mailMessages.length; i++) {
                	final Message mailMessage = mailFolder.getMessage(i);

			LOG.debug("searchMailSubject: retrieved message subject '{}'", mailMessage.getSubject());

                    if (mailMessage.match(searchTerm)) {
                        found = true;
                        LOG.debug("searchMailSubject: message with subject '{}' found.", subject);
                        
                        if (mailParms.isEnd2EndTestInProgress()) {
                            if (!delete) LOG.debug("searchMailSubject: flagging message with subject '{}' for deletion for end2end test.", subject);
                            delete = true;
                        }
                    }

                	final boolean deleteAllMail = mailParms.getReadTest().isDeleteAllMail();
					final boolean foundMTMHeader = mailMessage.match(deleteTerm);
					LOG.debug("searchMailSubject: deleteAllMail = {}, MTM header found = {}", Boolean.toString(deleteAllMail), Boolean.toString(foundMTMHeader));
					
					if (deleteAllMail) {
						if (!delete) LOG.debug("searchMailSubject: flagging message with subject '{}' for deletion because deleteAllMail is set.", subject);
						delete = true;
					} else if (foundMTMHeader) {
						if (!delete) LOG.debug("searchMailSubject: flagging message with subject '{}' for deletion because we sent it (found header {}={})", subject, MTM_HEADER_KEY, m_headerValue);
						delete = true;
					}
					
					if (delete) {
						mailMessage.setFlag(Flag.DELETED, true);
					}
                	
                	// since we want to delete old messages matchin MTM_HEADER_KEY, we can't break early
                	// if (found) break;
                }

                if (!found) {
			LOG.debug("searchMailSubject: message with subject: '{}' NOT found.", subject);
                    status = PollStatus.down("searchMailSubject: matching test message: '"+subject+"', not found.");
                } else {
                    status = PollStatus.available();
                }
            }
        } catch (final MessagingException e) {
            return PollStatus.down(e.getLocalizedMessage());
        }

        return status;
    }

    /**
     * An end2end test has the subject appended with a unique value for matching the read with the send.
     * 
     * @param mailParms
     * @return a computed subject based on the requirements of the service configuration.
     */
    private String computeMatchingSubject(final MailTransportParameters mailParms) {
        String subject = null;
        if (mailParms.isEnd2EndTestInProgress()) {
            subject = mailParms.getComputedTestSubject();
        } else {
            subject = mailParms.getReadTest().getSubjectMatch();
        }
        return subject;
    }

    /**
     * This sets up the properties for the read mail portion of the service poll.  These properties
     * are derived from configuration elements vs. being hardcoded javamail properties.  Elements
     * that conflict with javamail defined properties always win.
     * 
     * @param mailParms
     * @param readMailer
     */
    private void setReadMailProperties(final MailTransportParameters mailParms, final JavaMailer readMailer) {
    	final Properties sendMailProps = readMailer.getSession().getProperties();

    	final String protocol = mailParms.getReadTestProtocol();
        sendMailProps.put("mail." + protocol + ".host", mailParms.getReadTestHost());
        sendMailProps.put("mail." + protocol + ".user", mailParms.getReadTestUserName());
        sendMailProps.put("mail." + protocol + ".port", mailParms.getReadTestPort());
        sendMailProps.put("mail." + protocol + ".starttls.enable", mailParms.isReadTestStartTlsEnabled());
        sendMailProps.put("mail.smtp.auth", "true");

        if (mailParms.isReadTestSslEnabled()) {
            sendMailProps.put("mail." + protocol + ".socketFactory.port", mailParms.getReadTestPort());
            sendMailProps.put("mail." + protocol + ".socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            sendMailProps.put("mail." + protocol + ".socketFactory.fallback", "false");
        }

        sendMailProps.put("mail." + protocol + ".connectiontimeout", mailParms.getTimeout());
        sendMailProps.put("mail." + protocol + ".timeout", mailParms.getTimeout());

        sendMailProps.put("mail.store.protocol", protocol);
    }

    /**
     * Establish connection with mail store and return the configured mail folder.
     * 
     * @param mailParms
     * @param mailStore
     * @return the folder specified in configuration
     * @throws MessagingException
     */
    private Folder retrieveMailFolder(final MailTransportParameters mailParms, final Store mailStore) throws MessagingException {
        mailStore.connect(mailParms.getReadTestHost(), mailParms.getReadTestPort(), mailParms.getReadTestUserName(), mailParms.getReadTestPassword());
        Folder mailFolder = mailStore.getDefaultFolder();
        mailFolder = mailFolder.getFolder(mailParms.getReadTestFolder());
        return mailFolder;
    }

    /**
     * Sends message based on properties and fields configured for the service.
     * 
     * @param mailParms
     * @return a PollStatus
     */
    private PollStatus sendTestMessage(final MailTransportParameters mailParms) {
        PollStatus status = PollStatus.unavailable("Test not completed.");

        final long interval = mailParms.getSendTestAttemptInterval();

        final TimeoutTracker tracker = new TimeoutTracker(mailParms.getParameterMap(), mailParms.getRetries(), mailParms.getTimeout());
        for (tracker.reset(); tracker.shouldRetry(); tracker.nextAttempt()) {
            tracker.startAttempt();
            LOG.debug("sendTestMessage: sending mail attempt: {}, elapsed time: {}ms", (tracker.getAttempt() + 1), String.format("%.2f", tracker.elapsedTimeInMillis()));
            try {
                final JavaMailer sendMailer = createMailer(mailParms);
                overRideDefaultProperties(mailParms, sendMailer);
                sendMailer.mailSend();
                status = PollStatus.available();
                break;
            } catch (final JavaMailerException e) {
                status = PollStatus.unavailable(e.getLocalizedMessage());
            }
            
            if (tracker.shouldRetry()) {
                delayTest(status, interval);
            }
        }
        return status;
    }

    /**
     * Set poll status to unknown indicate an exception occurred while attempting to sleep the thread.
     * @param status
     * @param interval
     * @return returns an unchanged PollStatus unless an exception happens in which case status is changed to unknown. 
     */
    private int delayTest(PollStatus status, final long interval) {
        LOG.debug("delayTest: delaying test for: {}ms. per configuration.", interval);
        try {
            Thread.sleep(interval);
        } catch (final InterruptedException e) {
            LOG.error("delayTest: An interrupt exception occurred while delaying the mail test", e);
            status = PollStatus.unknown(e.getLocalizedMessage());
        }
        return status.getStatusCode();
    }

    /**
     * Override some fields in the JavaMailer class. TODO: This needs
     * re-factoring!

     * @param mailParms
     * @param sendMailer
     */
    private void overRideDefaultProperties(final MailTransportParameters mailParms, final JavaMailer sendMailer) {
        sendMailer.setFrom(mailParms.getSendTestFrom());

        sendMailer.getSession().setDebug(mailParms.isSendTestDebug());
        sendMailer.setDebug(mailParms.isSendTestDebug());
        
        sendMailer.setEncoding(mailParms.getSendTestMessageEncoding());
        sendMailer.setMailer(mailParms.getSendTestMailer());
        sendMailer.setMailHost(mailParms.getSendTestHost());

        //char_set, encoding, m_contentType
        sendMailer.setMessageText(mailParms.getSendTestMessageBody());
        sendMailer.setCharSet(mailParms.getSendTestCharSet());
        sendMailer.setContentType(mailParms.getSendTestMessageContentType());
        
        sendMailer.setSmtpSsl(mailParms.isSendTestIsSslEnable());
        
        
        sendMailer.setSubject(mailParms.getComputedTestSubject());
        sendMailer.setTo(mailParms.getSendTestRecipeint());
        sendMailer.setTransport(mailParms.getSendTestTransport());
        sendMailer.setUseJMTA(mailParms.isSendTestUseJmta());
    }

    private JavaMailer createMailer(final MailTransportParameters mailParms) throws JavaMailerException {
        final JavaMailer sendMailer = new JavaMailer(mailParms.getJavamailProperties());
        final String mailPropsPrefix = new StringBuilder("mail.").append(mailParms.getSendTestTransport()).append('.').toString();
        final Properties props = sendMailer.getSession().getProperties();
        
        //user
        props.setProperty(mailPropsPrefix+"user", mailParms.getSendTestUserName());
        sendMailer.setUser(mailParms.getSendTestUserName());
        sendMailer.setPassword(mailParms.getSendTestPassword());
        
        //host
        props.setProperty(mailPropsPrefix+"host", mailParms.getSendTestHost());
        sendMailer.setMailHost(mailParms.getSendTestHost());
        
        //port
        props.setProperty(mailPropsPrefix+"port", String.valueOf(mailParms.getSendTestPort()));
        sendMailer.setSmtpPort(mailParms.getSendTestPort());

        //connectiontimeout
        //Override this with configured javamail property because this setting is a generic timeout value
        if (!props.containsKey(mailPropsPrefix+"connectiontimeout")) {
            props.setProperty(mailPropsPrefix+"connectiontimeout", String.valueOf(mailParms.getTimeout()));
        }
        
        //timeout
        //Override this with configured javamail property because this setting is a generic timeout value
        if (!props.containsKey(mailPropsPrefix+"timeout")) {
            props.setProperty(mailPropsPrefix+"timeout", String.valueOf(mailParms.getTimeout()));
        }
        
        //from
        props.setProperty(mailPropsPrefix+"from", mailParms.getSendTestFrom());
        sendMailer.setFrom(mailParms.getSendTestFrom());
        
        //auth
        props.setProperty(mailPropsPrefix+"auth", String.valueOf(mailParms.isSendTestUseAuth()));
        sendMailer.setAuthenticate(mailParms.isSendTestUseAuth());

        //quitwait
        props.setProperty(mailPropsPrefix+"quitwait", String.valueOf(mailParms.isSendTestIsQuitWait()));
        sendMailer.setQuitWait(mailParms.isSendTestIsQuitWait());
        
        //socketFactory.class
        //socketFactory.port
        if (mailParms.isSendTestIsSslEnable()) {
            
            //override this hard coded default if this property is specified
            if (!props.containsKey(mailPropsPrefix+"socketFactory.class")) {
                props.setProperty(mailPropsPrefix+"socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            }
            props.setProperty(mailPropsPrefix+"socketFactory.port", String.valueOf(mailParms.getSendTestPort()));
            sendMailer.setSmtpPort(mailParms.getSendTestPort());
        }
        sendMailer.setSmtpSsl(mailParms.isSendTestIsSslEnable());
        
        //starttls.enable
        props.setProperty(mailPropsPrefix+"starttls.enable", String.valueOf(mailParms.isSendTestStartTls()));
        sendMailer.setStartTlsEnabled(mailParms.isSendTestStartTls());
        sendMailer.addExtraHeader(MTM_HEADER_KEY, m_headerValue);
        
        sendMailer.setSession(Session.getInstance(props, sendMailer.createAuthenticator()));
        
        return sendMailer;
    }

}
