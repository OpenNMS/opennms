/**
 * 
 */
package org.opennms.javamail;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.opennms.netmgt.config.common.JavamailProperty;
import org.opennms.netmgt.config.common.SendmailConfig;

public class OpenNMSJavaMailMessage extends MimeMessage {
    Session m_session;
    
    public OpenNMSJavaMailMessage(Session session) {
        super(session);
        m_session = session;
    }
    
    public OpenNMSJavaMailMessage(Message message, Session session) throws MessagingException, IOException {
        super(session, message.getInputStream());
        m_session = session;
    }

    public Session getSession() {
        return m_session;
    }

    public static Authenticator configureAuthenticator(final SendmailConfig config) {
        Authenticator auth;
        if (config.isUseAuthentication()) {
            auth = new Authenticator() {
                
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(config.getUserAuth().getUserName(), config.getUserAuth().getPassword());
                }
            };
        } else {
            auth = null;
        }
        return auth;
    }

    public static Properties configureProps(final SendmailConfig config) {
        
        Properties props = new Properties();
        
        props.setProperty("mail.debug", String.valueOf(config.isDebug()));
        
        //first set the actual properties defined in the sendmail configuration
        List<JavamailProperty> jmps = config.getJavamailPropertyCollection();
        for (JavamailProperty jmp : jmps) {
            props.setProperty(jmp.getName(), jmp.getValue());
        }
    
        
        final String mailPropsPrefix = new StringBuilder("mail.").append(config.getSendmailProtocol().getTransport()).append('.').toString();
        
        //set the convenience properties
        props.setProperty(mailPropsPrefix+"auth", String.valueOf(config.isUseAuthentication()));
        props.setProperty(mailPropsPrefix+"user", String.valueOf(config.getUserAuth().getUserName()));
        props.setProperty(mailPropsPrefix+"password", String.valueOf(config.getUserAuth().getUserName()));
        props.setProperty(mailPropsPrefix+"starttls.enable", String.valueOf(config.getSendmailProtocol().isStartTls()));
        props.setProperty(mailPropsPrefix+"quitwait", String.valueOf(config.getSendmailProtocol().isQuitWait()));
        props.setProperty(mailPropsPrefix+"port", String.valueOf(config.getSendmailHost().getPort()));
        
        if (config.getSendmailProtocol().isSslEnable()) {
            props.setProperty("mail.smtps.auth", String.valueOf(config.isUseAuthentication()));
            if (!props.containsKey("mail.smtps.socketFactory.class")) {
                props.setProperty("mail.smtps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            }
            if (!props.containsKey("mail.smtps.socketFactory.port")) {
                props.setProperty("mail.smtps.socketFactory.port", String.valueOf(config.getSendmailHost().getPort()));
            }
        }
        
        
        return props;
    }

    /**
     * Recommended usage is from the OpenNMSJavaMessageBuilder class.
     * 
     * Create a JavaMail message using all the properties from the @param config object.  An authenticator,
     * content type, from address, reply-to address, subject, and date is set based on the SendmailConfig settings.
     * You probably want to set the subject to something more meaningful.  Javamail properties are created
     * in the message's session object based on the configuration.
     * 
     * @param content
     * @param config
     * @return a JavaMail Message object
     * @throws JavaMailerException
     */
    public static OpenNMSJavaMailMessage createMessage(String content, SendmailConfig config) throws JavaMailerException {
        Session sess = Session.getInstance(OpenNMSJavaMailMessage.configureProps(config), OpenNMSJavaMailMessage.configureAuthenticator(config));
        OpenNMSJavaMailMessage msg = new OpenNMSJavaMailMessage(sess);
        try {
            msg.setContent(content, config.getSendmailProtocol().getMessageContentType());
            msg.setFrom(new InternetAddress(config.getSendmailMessage().getFrom()));
            Address[] addrs = {new InternetAddress(config.getSendmailMessage().getFrom())};
            msg.setReplyTo(addrs);
            msg.setSentDate(new Date());
            msg.setSubject(config.getSendmailMessage().getSubject());
        } catch (MessagingException e) {
            throw new JavaMailerException("An exception occurred creating a Java Mail message from config: "+config.getName(), e);
        }
        return msg;
    }
}
