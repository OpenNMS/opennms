/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 27, 2009
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.javamail;

import java.util.Calendar;
import java.util.List;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Flags.Flag;
import javax.mail.search.OrTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.netmgt.config.common.ReadmailConfig;
import org.opennms.netmgt.config.common.ReadmailHost;
import org.opennms.netmgt.config.common.ReadmailProtocol;
import org.opennms.netmgt.config.common.SendmailConfig;
import org.opennms.netmgt.config.common.SendmailHost;
import org.opennms.netmgt.config.common.SendmailMessage;
import org.opennms.netmgt.config.common.SendmailProtocol;
import org.opennms.netmgt.config.common.UserAuth;

public class JavaReadMailerTest {
    
    /**
     * This un-ignore this test with a proper gmail account
     * @throws JavaMailerException
     * @throws MessagingException
     * @throws InterruptedException
     */
    @Test
    @Ignore
    public void testReadMessagesWithSearchTerm() throws JavaMailerException, MessagingException, InterruptedException {
        
        String gmailAccount = "foo";
        String gmailPassword = "bar";
        
        JavaSendMailer sendMailer = createSendMailer(gmailAccount, gmailPassword);
        
        String term1 = String.valueOf(Calendar.getInstance().getTimeInMillis());
        Thread.sleep(2);
        String term2 = String.valueOf(Calendar.getInstance().getTimeInMillis());
        Thread.sleep(2);
        String term3 = String.valueOf(Calendar.getInstance().getTimeInMillis());
        Thread.sleep(2);
        
        SendmailMessage sendMsg = createAckMessage(gmailAccount, "1", term1, "ack");
        sendMailer.setMessage(sendMailer.buildMimeMessage(sendMsg));
        sendMailer.send();
        
        sendMsg = createAckMessage(gmailAccount, "2", term2, "ack");
        sendMailer.setMessage(sendMailer.buildMimeMessage(sendMsg));
        sendMailer.send();

        sendMsg = createAckMessage(gmailAccount, "3", term3, "ack");
        sendMailer.setMessage(sendMailer.buildMimeMessage(sendMsg));
        sendMailer.send();
        
        JavaReadMailer readMailer = createGoogleReadMailer(gmailAccount, gmailPassword);
        
        //See if search finds all 3 messages
        SearchTerm st = new OrTerm(new SubjectTerm(".*"+term1+" #.*"), new SubjectTerm(".*"+term2+" #.*"));
        st = new OrTerm(st, new SubjectTerm("*."+term3+" #.*"));

        List<Message> msgs = null;
        try {
            msgs = readMailer.retrieveMessages(st);
        } catch (JavaMailerException e) {
            e.printStackTrace();
        }
        
        Assert.assertEquals(3, msgs.size());
        
        st = new OrTerm(new SubjectTerm(".*"+term1+" #.*"), new SubjectTerm(".*"+term2+" #.*"));
        
        try {
            msgs = readMailer.retrieveMessages(st);
        } catch (JavaMailerException e) {
            e.printStackTrace();
        }
        
        //Should find only term1 and term2 messages
        Assert.assertNotNull(msgs);
        Assert.assertEquals(2, msgs.size());

        //Now cleanup
        //Delete the term1 and term2 messages
        for (Message msg : msgs) {
            msg.setFlag(Flag.DELETED, true);
        }
        
        //Find and delete the term3 messages
        st = new SubjectTerm("*."+term3+" #.*");
        try {
            msgs = readMailer.retrieveMessages(st);
        } catch (JavaMailerException e) {
            e.printStackTrace();
        }
        
        Assert.assertNotNull(msgs);
        Assert.assertTrue(msgs.size() >= 1);
        for (Message eventMsg : msgs) {
            eventMsg.setFlag(Flag.DELETED, true);
        }
        
        //Make sure they're all gone
        st = new OrTerm(new SubjectTerm(".*"+term1+" #.*"), new SubjectTerm(".*"+term2+" #.*"));
        st = new OrTerm(st, new SubjectTerm("*."+term3+" #.*"));

        try {
            msgs = readMailer.retrieveMessages(st);
        } catch (JavaMailerException e) {
            e.printStackTrace();
        }
        Assert.assertTrue(msgs.isEmpty());
        
    }
    
    private SendmailMessage createAckMessage(String gmailAccount, String noticeId, String regards, String body) {
        SendmailMessage sendMsg = new SendmailMessage();
        sendMsg.setTo(gmailAccount+"@gmail.com");
        sendMsg.setFrom(gmailAccount+"@gmail.com");
        sendMsg.setSubject("re:"+regards+" #"+noticeId+":");
        sendMsg.setBody(body);
        return sendMsg;
    }

    private JavaSendMailer createSendMailer(String gmailAccount, String gmailPassword) throws JavaMailerException {
        
        SendmailConfig config = new SendmailConfig();
        
        config.setAttemptInterval(1000);
        config.setDebug(true);
        config.setName("test");
        
        SendmailMessage sendmailMessage = new SendmailMessage();
        sendmailMessage.setBody("service is down");
        sendmailMessage.setFrom("bamboo.opennms@gmail.com");
        sendmailMessage.setSubject("Notice #1234: service down");
        sendmailMessage.setTo("bamboo.opennms@gmail.com");
        config.setSendmailMessage(sendmailMessage);
        
        SendmailHost host = new SendmailHost();
        host.setHost("smtp.gmail.com");
        host.setPort(465);
        config.setSendmailHost(host);
        
        SendmailProtocol protocol = new SendmailProtocol();
        protocol.setSslEnable(true);
        protocol.setTransport("smtps");
        config.setSendmailProtocol(protocol);
        
        config.setUseAuthentication(true);
        config.setUseJmta(false);
        UserAuth auth = new UserAuth();
        auth.setUserName(gmailAccount);
        auth.setPassword(gmailPassword);
        config.setUserAuth(auth);
        
        return new JavaSendMailer(config);
    }

    private JavaReadMailer createGoogleReadMailer(String gmailAccount, String gmailPassword) throws JavaMailerException {
        ReadmailConfig config = new ReadmailConfig();
        config.setDebug(true);
        config.setDeleteAllMail(false);
        config.setMailFolder("INBOX");
        ReadmailHost readmailHost = new ReadmailHost();
        readmailHost.setHost("imap.gmail.com");
        readmailHost.setPort(993);
        ReadmailProtocol readmailProtocol = new ReadmailProtocol();
        readmailProtocol.setSslEnable(true);
        readmailProtocol.setStartTls(false);
        readmailProtocol.setTransport("imaps");
        readmailHost.setReadmailProtocol(readmailProtocol);
        config.setReadmailHost(readmailHost);
        UserAuth userAuth = new UserAuth();
        userAuth.setPassword(gmailPassword);
        userAuth.setUserName(gmailAccount);
        config.setUserAuth(userAuth);
        
        JavaReadMailer mailer = new JavaReadMailer(config, true);
        return mailer;
    }


}
