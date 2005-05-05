/*
 * Created on Jan 24, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.core.utils;

/*
import java.util.Calendar;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import alt.dev.jmta.JMTA;
*/
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Category;
import org.opennms.netmgt.mock.OpenNMSTestCase;

/**
 * @author david hustace
 */
public class JavaMailerTest extends OpenNMSTestCase {
    
    public void testNothing() throws Exception {
        
    }
	    
 /*   public void testMTA() throws Exception {
        
        Properties props = new Properties();
        Session session = Session.getInstance(props, null);
        MimeMessage message = new MimeMessage(session);
        message.setContent("Hello", "text/plain");
        message.setText("Hello");
        message.setSubject(Calendar.getInstance().getTime() + ": testMTA message");
        
        String to = "david@opennms.org";
        String from = "david@opennms.org";
        

        Address address = new InternetAddress(from);
        message.setFrom(address);
        address = new InternetAddress(to);
        message.setRecipients(Message.RecipientType.TO, to);
        message.saveChanges();
        
        JMTA.send(message);
        
    }

    
    public final void testJavaMailerWithoutFileAttachment()  {
        
        JavaMailer jm = new JavaMailer();
        
        jm.setFrom("david@opennms.org");
        try {
            jm.setMessageText("Test message from testJavaMailer: "+InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            log().error("Host Name exception: "+e.getMessage());
        }
        jm.setSubject("Testing JavaMailer");
        jm.setTo("david@opennms.org");
        try {
            jm.mailSend();
        } catch (JavaMailerException e) {
            log().error("JavaMailerException exception: "+e.getMessage());
        }
        
    }
    
    public final void testJavaMailerWithFileAttachment() {
        
        JavaMailer jm = new JavaMailer();

        jm.setFrom("david@opennms.org");
        try {
            jm.setMessageText("Test message with file attachment from testJavaMailer: "+InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            log().error("Host Name exception: "+e.getMessage());
        }
        jm.setSubject("Testing JavaMailer");
        jm.setTo("david@opennms.org");
        
        jm.setFileName("/etc/motd");
        try {
            jm.mailSend();
        } catch (JavaMailerException e) {
            log().error("JavaMailerException exception: "+e.getMessage());
        }
        
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }
*/
}
