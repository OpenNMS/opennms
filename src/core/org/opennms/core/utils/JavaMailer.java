/*
 * Created on Sep 13, 2004
 *
 * Copyright (C) 2005 The OpenNMS Group, Inc.
 * 
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.core.utils;

import java.util.Date;
import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.apache.log4j.Category;

import com.sun.mail.smtp.SMTPTransport;

/**
 * Sends an email message using the Java Mail API
 * 
 * @author <A HREF="mailto:david@opennms.org">David Hustace </A>
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class JavaMailer {

    private final String DEFAULT_FROM_ADDRESS = JavaMailerConfig.getProperty("org.opennms.core.utils.fromAddress", "root@127.0.0.1");

    private final String DEFAULT_MAIL_HOST = JavaMailerConfig.getProperty("org.opennms.core.utils.mailHost", "127.0.0.1");

    private final boolean DEFAULT_AUTHENTICATE = JavaMailerConfig.getProperty("org.opennms.core.utils.authenticate", false);

    private final String DEFAULT_AUTHENTICATE_USER = JavaMailerConfig.getProperty("org.opennms.core.utils.authenticateUser", "opennms");

    private final String DEFAULT_AUTHENTICATE_PASSWORD = JavaMailerConfig.getProperty("org.opennms.core.utils.authenticatePassword", "opennms");

    private final String DEFAULT_MAILER = JavaMailerConfig.getProperty("org.opennms.core.utils.mailer", "smtpsend");

    private final String DEFAULT_TRANSPORT = JavaMailerConfig.getProperty("org.opennms.core.utils.transport", "smtp");

    private final boolean DEFAULT_MAILER_DEBUG = JavaMailerConfig.getProperty("org.opennms.core.utils.debug", true);

    private String _mailHost = DEFAULT_MAIL_HOST;

    private String _mailer = DEFAULT_MAILER;

    private String _transport = DEFAULT_TRANSPORT;

    private String _to;

    private String _from = DEFAULT_FROM_ADDRESS;

    private boolean _authenticate = DEFAULT_AUTHENTICATE;

    private String _user = DEFAULT_AUTHENTICATE_USER;

    private String _password = DEFAULT_AUTHENTICATE_PASSWORD;

    private String _subject;

    private String _messageText;

    private String _fileName;

    /**
     * @return Returns the from address.
     */
    public String getFrom() {
        return _from;
    }

    /**
     * @param from
     *            The from address to set.
     */
    public void setFrom(String from) {
        _from = from;
    }

    /**
     * @return Returns the authenticate boolean.
     */
    public boolean isAuthenticate() {
        return _authenticate;
    }

    /**
     * @param authenticate
     *            The authenticate boolean to set.
     */
    public void setAuthenticate(boolean authenticate) {
        _authenticate = authenticate;
    }

    /**
     * @return Returns the file name attachment.
     */
    public String getFileName() {
        return _fileName;
    }

    /**
     * @param file
     *            Sets the file name to be attached to the messaget.
     */
    public void setFileName(String fileName) {
        this._fileName = fileName;
    }

    /**
     * @return Returns the mail host.
     */
    public String getMailHost() {
        return _mailHost;
    }

    /**
     * @param mail_host
     *            Sets the mail host.
     */
    public void setMailHost(String mail_host) {
        _mailHost = mail_host;
    }

    /**
     * @return Returns the mailer.
     */
    public String getMailer() {
        return _mailer;
    }

    /**
     * @param mailer
     *            Sets the mailer.
     */
    public void setMailer(String mailer) {
        _mailer = mailer;
    }

    /**
     * @return Returns the message text.
     */
    public String getMessageText() {
        return _messageText;
    }

    /**
     * @param messageText
     *            Sets the message text.
     */
    public void setMessageText(String messageText) {
        _messageText = messageText;
    }

    /**
     * @return Returns the message Subject.
     */
    public String getSubject() {
        return _subject;
    }

    /**
     * @param subject
     *            Sets the message Subject.
     */
    public void setSubject(String subject) {
        _subject = subject;
    }

    /**
     * @return Returns the To address.
     */
    public String getTo() {
        return _to;
    }

    /**
     * @param to
     *            Sets the To address.
     */
    public void setTo(String to) {
        _to = to;
    }

    public JavaMailer() {

    }

    /**
     * @param text
     * @param subject
     * @param to
     * 
     */
    public void mailSend() throws JavaMailerException {

        Category log = ThreadCategory.getInstance(getClass());

        Properties props = System.getProperties();

        if (_mailHost != null)
            props.put("mail.smtp.host", _mailHost);

        if (_authenticate)
            props.put("mail.smtp.auth", "true");

        // Get a Session object
        Session session = Session.getInstance(props, null);

        if (DEFAULT_MAILER_DEBUG)
            session.setDebug(true);

        // construct the message
        Message _msg = new MimeMessage(session);
        MimeBodyPart mbp1 = new MimeBodyPart(); // for message text
        MimeBodyPart mbp2 = null; // for file attachment if necessary

        try {
            if (_from != null)
                _msg.setFrom(new InternetAddress(_from));
            else
                _msg.setFrom();

            if (_to == null) {
                log.debug("_to is null");
                _to = "root@127.0.0.1";
            }
            log.debug("To is: " + _to);
            _msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(_to, false));

            if (_subject == null) {
                log.debug("_subject is null");
                _subject = "Subject was null";
            }
            log.debug("Subject is: " + _subject);
            _msg.setSubject(_subject);

            if (_messageText == null) {
                log.debug("_messageText is null");
                _messageText = "Message Text was null";
            }
            log.debug("Subject is: " + _subject);

            // _msg.setText(_messageText);
            mbp1.setText(_messageText);

            MimeMultipart mp = new MimeMultipart();
            mp.addBodyPart(mbp1);

            if (_fileName != null) {
                log.debug("_file is not null");
                mbp2 = new MimeBodyPart();
                FileDataSource fds = new FileDataSource(_fileName);
                mbp2.setDataHandler(new DataHandler(fds));
                mbp2.setFileName(fds.getName());
                mp.addBodyPart(mbp2);
            }

            _msg.setHeader("X-Mailer", _mailer);
            _msg.setSentDate(new Date());
            _msg.setContent(mp);

            SMTPTransport t = null;
            try {
                t = (SMTPTransport) session.getTransport(_transport);
                if (_authenticate)
                    t.connect(_mailHost, _user, _password);
                else
                    t.connect();

                t.sendMessage(_msg, _msg.getAllRecipients());
            } catch (NoSuchProviderException e) {
                throw new JavaMailerException("Couldn't get a transport: ", e);
            } catch (MessagingException e) {
                throw new JavaMailerException("Java Mailer messaging exception: ", e);
            } finally {
                System.out.println("Response: " + t.getLastServerResponse());
                try {
                    t.close();
                } catch (MessagingException e1) {
                    throw new JavaMailerException("Java Mailer messaging exception on transport close: ", e1);
                }
            }

            System.out.println("\nMail was sent successfully.");

        } catch (AddressException e) {
            throw new JavaMailerException("Java Mailer Addressing exception: ", e);
        } catch (MessagingException e) {
            throw new JavaMailerException("Java Mailer messaging exception: ", e);
        } finally {

        }
    }

    /**
     * @return Returns the password.
     */
    public String getPassword() {
        return _password;
    }

    /**
     * @param password
     *            The password to set.
     */
    public void setPassword(String password) {
        _password = password;
    }

    /**
     * @return Returns the user.
     */
    public String getUser() {
        return _user;
    }

    /**
     * @param user
     *            The user to set.
     */
    public void setUser(String user) {
        _user = user;
    }
}