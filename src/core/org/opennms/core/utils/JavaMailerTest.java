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
import junit.framework.TestCase;

/**
 * @author david
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class JavaMailerTest extends TestCase {
    
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
*/
}
