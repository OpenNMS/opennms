//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//
// For more information contact:
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
/*
 * Created on Jan 24, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.opennms.netmgt.utils;


import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import junit.framework.TestCase;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import alt.dev.jmta.JMTA;
/**
 * @author david hustace
 */
public class JavaMailerTest extends TestCase {
    
    protected void setUp() throws IOException {
        MockLogAppender.setupLogging();
        
    	Resource resource = new ClassPathResource("/etc/javamail-configuration.properties");
    	
    	File homeDir = resource.getFile().getParentFile().getParentFile();

        System.setProperty("opennms.home", homeDir.getAbsolutePath());
    }
    
    protected void tearDown() {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    public void testNothing() throws Exception {
        
    }
	    
    public void testMTA() throws Exception {
        
        Properties props = new Properties();
        Session session = Session.getInstance(props, null);
        MimeMessage message = new MimeMessage(session);
        message.setContent("Hello", "text/plain");
        message.setText("Hello");
        message.setSubject(Calendar.getInstance().getTime() + ": testMTA message");
        
        String to = "brozow@opennms.org";
        String from = "brozow@opennms.org";
        

        Address address = new InternetAddress(from);
        message.setFrom(address);
        address = new InternetAddress(to);
        message.setRecipients(Message.RecipientType.TO, to);
        message.saveChanges();
        
        Transport  aTransport = session.getTransport( "mta" );
        
        aTransport.sendMessage( message, null );
//        JMTA.send(message);
        
    }

    
    public final void testJavaMailerWithoutFileAttachment()  {
        
        JavaMailer jm = new JavaMailer();
        
        jm.setFrom("brozow@opennms.org");
        try {
            jm.setMessageText("Test message from testJavaMailer: "+InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            log().error("Host Name exception: "+e.getMessage());
        }
        jm.setSubject("Testing JavaMailer");
        jm.setTo("brozow@opennms.org");
        try {
            jm.mailSend();
        } catch (JavaMailerException e) {
            log().error("JavaMailerException exception: "+e.getMessage());
        }
        
    }
    
    public final void testJavaMailerWithFileAttachment() {
        
        JavaMailer jm = new JavaMailer();

        jm.setFrom("brozow@opennms.org");
        try {
            jm.setMessageText("Test message with file attachment from testJavaMailer: "+InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            log().error("Host Name exception: "+e.getMessage());
        }
        jm.setSubject("Testing JavaMailer");
        jm.setTo("brozow@opennms.org");
        
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

}
