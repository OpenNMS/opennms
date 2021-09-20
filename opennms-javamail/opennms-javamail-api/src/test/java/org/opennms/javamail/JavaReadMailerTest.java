/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2002-2014 The OpenNMS Group, Inc.
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
import org.opennms.netmgt.config.javamail.ReadmailConfig;
import org.opennms.netmgt.config.javamail.ReadmailHost;
import org.opennms.netmgt.config.javamail.ReadmailProtocol;
import org.opennms.netmgt.config.javamail.SendmailConfig;
import org.opennms.netmgt.config.javamail.SendmailHost;
import org.opennms.netmgt.config.javamail.SendmailMessage;
import org.opennms.netmgt.config.javamail.SendmailProtocol;
import org.opennms.netmgt.config.javamail.UserAuth;

public class JavaReadMailerTest {
    
    /**
     * Un-ignore this test with a proper gmail account
     * @throws JavaMailerException
     * @throws MessagingException
     * @throws InterruptedException
     */
    @Test
    @Ignore
    public void testReadMessagesWithSearchTerm() throws JavaMailerException, MessagingException, InterruptedException {
        
        String gmailAccount = getUser();
        String gmailPassword = getPassword();
        
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

    private String getPassword() {
        return "bar";
    }

    private String getUser() {
        return "foo";
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
        
        config.setAttemptInterval(1000l);
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
    
    @Test
    @Ignore
    public void testGetText() throws JavaMailerException, MessagingException, IOException {
        JavaReadMailer rm = createGoogleReadMailer(getUser(), getPassword());
        List<Message> msgs = rm.retrieveMessages();
        for (Message msg : msgs) {
            JavaReadMailer.getText(msg);
            JavaReadMailer.string2Lines("abc\nxyz");
        }
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
