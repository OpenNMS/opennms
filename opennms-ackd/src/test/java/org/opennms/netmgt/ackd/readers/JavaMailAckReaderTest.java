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

package org.opennms.netmgt.ackd.readers;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Message.RecipientType;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import org.junit.Assert;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.javamail.JavaMailerException;
import org.opennms.javamail.JavaSendMailer;
import org.opennms.netmgt.ackd.AckReader;
import org.opennms.netmgt.ackd.Ackd;
import org.opennms.netmgt.config.ackd.AckdConfiguration;
import org.opennms.netmgt.config.javamail.End2endMailConfig;
import org.opennms.netmgt.config.javamail.ReadmailConfig;
import org.opennms.netmgt.config.javamail.ReadmailHost;
import org.opennms.netmgt.config.javamail.ReadmailProtocol;
import org.opennms.netmgt.config.javamail.SendmailConfig;
import org.opennms.netmgt.config.javamail.SendmailHost;
import org.opennms.netmgt.config.javamail.SendmailMessage;
import org.opennms.netmgt.config.javamail.SendmailProtocol;
import org.opennms.netmgt.config.javamail.UserAuth;
import org.opennms.netmgt.dao.api.AckdConfigurationDao;
import org.opennms.netmgt.dao.api.JavaMailConfigurationDao;
import org.opennms.netmgt.dao.jaxb.DefaultAckdConfigurationDao;
import org.opennms.netmgt.model.AckAction;
import org.opennms.netmgt.model.AckType;
import org.opennms.netmgt.model.OnmsAcknowledgment;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.ContextConfiguration;

/**
 * Integration test of for the Javamail Acknowledgement Reader Implementation.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-ackd.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class JavaMailAckReaderTest implements InitializingBean {

    @Autowired
    private Ackd m_daemon;
    
    @Autowired
    private JavaMailConfigurationDao m_jmDao;
    
    @Autowired
    private MailAckProcessor m_processor;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    /**
     * tests the ability to detect an aknowledgable ID
     */
    @Test
    public void detectId() {
        String expression = m_daemon.getConfigDao().getConfig().getNotifyidMatchExpression();
        Integer id = MailAckProcessor.detectId("Re: notice #1234", expression);
        Assert.assertEquals(new Integer(1234), id);
    }

    /**
     * tests the ability to create acknowledgments from an email for plain text.  This test
     * creates a message from scratch rather than reading from an inbox. 
     */
    @Test
    public void workingWithSimpleTextMessages() {
        Properties props = new Properties();
        Message msg = new MimeMessage(Session.getDefaultInstance(props));
        try {
            Address[] addrs = new Address[1];
            addrs[0] = new InternetAddress("david@opennms.org");
            msg.addFrom(addrs);
            msg.addRecipient(javax.mail.internet.MimeMessage.RecipientType.TO, addrs[0]);
            msg.setSubject("Re: Notice #1234 JavaMailReaderImplTest Test Message");
            msg.setText("ACK");
        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        }
        List<Message> msgs = new ArrayList<Message>(1);
        msgs.add(msg);
        List<OnmsAcknowledgment> acks = m_processor.createAcks(msgs);
        
        Assert.assertEquals(1, acks.size());
        Assert.assertEquals(AckType.NOTIFICATION, acks.get(0).getAckType());
        Assert.assertEquals("david@opennms.org", acks.get(0).getAckUser());
        Assert.assertEquals(AckAction.ACKNOWLEDGE, acks.get(0).getAckAction());
        Assert.assertEquals(new Integer(1234), acks.get(0).getRefId());
    }
    
    /**
     * tests the ability to create acknowledgments from an email for a multi-part text.  This test
     * creates a message from scratch rather than reading from an inbox.  This message creation
     * may not actually represent what comes from a mail server.
     */
    @Test
    public void workingWithMultiPartMessages() throws JavaMailerException, MessagingException {
        List<Message> msgs = new ArrayList<Message>();
        Properties props = new Properties();
        Message msg = new MimeMessage(Session.getDefaultInstance(props));
        Address[] addrs = new Address[1];
        addrs[0] = new InternetAddress("david@opennms.org");
        msg.addFrom(addrs);
        msg.addRecipient(RecipientType.TO, new InternetAddress("david@opennms.org"));
        msg.setSubject("Re: Notice #1234 JavaMailReaderImplTest Test Message");
        Multipart mpContent = new MimeMultipart();
        BodyPart textBp = new MimeBodyPart();
        BodyPart htmlBp = new MimeBodyPart();
        textBp.setText("ack");
        htmlBp.setContent("<html>\n" + 
        		" <head>\n" + 
        		"  <title>\n" + 
        		"   Acknowledge\n" + 
        		"  </title>\n" + 
        		" </head>\n" + 
        		" <body>\n" + 
        		"  <h1>\n" + 
        		"   ack\n" + 
        		"  </h1>\n" + 
        		" </body>\n" + 
        		"</html>", "text/html");
        
        mpContent.addBodyPart(textBp);
        mpContent.addBodyPart(htmlBp);
        msg.setContent(mpContent);
        
        msgs.add(msg);
        
        List<OnmsAcknowledgment> acks = m_processor.createAcks(msgs);
        Assert.assertEquals(1, acks.size());
        Assert.assertEquals(AckType.NOTIFICATION, acks.get(0).getAckType());
        Assert.assertEquals("david@opennms.org", acks.get(0).getAckUser());
        Assert.assertEquals(AckAction.ACKNOWLEDGE, acks.get(0).getAckAction());
        Assert.assertEquals(new Integer(1234), acks.get(0).getRefId());
    }

    @Ignore
    @Test
    public void findAndProcessAcks() throws InterruptedException {
        AckReader reader = new DefaultAckReader();
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
        reader.setAckProcessor(m_processor);
        Future<?> f = executor.schedule(m_processor, 5, TimeUnit.SECONDS);
        
        m_processor.setJmConfigDao(m_jmDao);
        m_processor.setAckdConfigDao(createAckdConfigDao());
        //m_processor.setAcknowledgmentDao(ackDao);
        //Thread.sleep(20000);
        while (!f.isDone()) {
            Thread.sleep(10);
        }
        Assert.assertTrue(f.isDone());
    }


    private AckdConfigurationDao createAckdConfigDao() {
        
        class AckdConfigDao extends DefaultAckdConfigurationDao {

            @Override
            public AckdConfiguration getConfig() {
                AckdConfiguration config = new AckdConfiguration();
                config.setAckExpression("~(?i)^AcK$");
                config.setAlarmidMatchExpression("~(?i).*alarmid:([0-9]+).*");
                config.setAlarmSync(true);
                config.setClearExpression("~(?i)^(Resolve|cleaR)$");
                config.setEscalateExpression("~(?i)^esc$");
                config.setNotifyidMatchExpression("~(?i).*RE:.*Notice #([0-9]+).*");
                config.setUnackExpression("~(?i)^unAck$");
                return config;
            }

        }
        
        return new AckdConfigDao();
        
    }


    protected class JmCnfDao implements JavaMailConfigurationDao {
        
        ReadmailConfig m_readConfig = createReadMailConfig();
        SendmailConfig m_sendConfig = createSendMailConfig();
        End2endMailConfig m_e2eConfig = createE2Ec();
        

        @Override
        public ReadmailConfig getDefaultReadmailConfig() {
            return m_readConfig;
        }

        private ReadmailConfig createReadMailConfig() {
            ReadmailConfig config = new ReadmailConfig();
            updateConfigWithGoogleReadConfiguration(config, getUser(), getPassword());
            m_readConfig = config;
            return m_readConfig;
        }

        private End2endMailConfig createE2Ec() {
            return new End2endMailConfig();
        }

        private SendmailConfig createSendMailConfig() {
            return new SendmailConfig();
        }

        @Override
        public SendmailConfig getDefaultSendmailConfig() {
            return m_sendConfig;
        }

        @Override
        public End2endMailConfig getEnd2EndConfig(String name) {
            return m_e2eConfig;
        }

        @Override
        public List<End2endMailConfig> getEnd2EndConfigs() {
            List<End2endMailConfig> list = new ArrayList<End2endMailConfig>();
            list.add(m_e2eConfig);
            return list;
        }

        @Override
        public ReadmailConfig getReadMailConfig(String name) {
            return m_readConfig;
        }

        @Override
        public List<ReadmailConfig> getReadmailConfigs() {
            List<ReadmailConfig> list = new ArrayList<ReadmailConfig>();
            list.add(m_readConfig);
            return list;
        }

        @Override
        public SendmailConfig getSendMailConfig(String name) {
            return m_sendConfig;
        }

        @Override
        public List<SendmailConfig> getSendmailConfigs() {
            List<SendmailConfig> list = new ArrayList<SendmailConfig>();
            list.add(m_sendConfig);
            return list;
        }

        @Override
        public void verifyMarshaledConfiguration() throws IllegalStateException {
        }

        @Override
        public void reloadConfiguration()
                throws DataAccessResourceFailureException {
            
        }
        
    }
    
    @Ignore
    public void createAcknowledgment() {
        fail("Not yet implemented");
    }

    @Ignore
    public void determineAckAction() {
        fail("Not yet implemented");
    }

    @Ignore
    public void start() {
        fail("Not yet implemented");
    }

    @Ignore
    public void pause() {
        fail("Not yet implemented");
    }

    @Ignore
    public void resume() {
        fail("Not yet implemented");
    }

    @Ignore
    public void stop() {
        fail("Not yet implemented");
    }

    
    /**
     * This test requires that 4 emails can be read from a Google account.  The mails should be
     * in this order:
     * Subject matching ackd-configuration expression of action type ack
     * Subject matching ackd-configuration expression of action type ack
     * Subject matching ackd-configuration expression of action type ack
     * Subject matching ackd-configuration expression of action type clear
     * 
     * The test has been updated to now include sending an email message to a gmail account.  Just correct
     * the account details for your own local testing.
     * 
     * @throws JavaMailerException 
     * 
     */
    @Ignore
    @Test
    public void testIntegration() throws JavaMailerException {
        
        String gmailAccount = getUser();
        String gmailPassword = getPassword();
        
        JavaSendMailer sendMailer = createSendMailer(gmailAccount, gmailPassword);
        
        SendmailMessage sendMsg = createAckMessage(gmailAccount, "1", "ack");
        sendMailer.setMessage(sendMailer.buildMimeMessage(sendMsg));
        sendMailer.send();
        sendMsg = createAckMessage(gmailAccount, "2", "ack");
        sendMailer.setMessage(sendMailer.buildMimeMessage(sendMsg));
        sendMailer.send();
        sendMsg = createAckMessage(gmailAccount, "3", "ack");
        sendMailer.setMessage(sendMailer.buildMimeMessage(sendMsg));
        sendMailer.send();
        sendMsg = createAckMessage(gmailAccount, "4", "clear");
        sendMailer.setMessage(sendMailer.buildMimeMessage(sendMsg));
        sendMailer.send();

        ReadmailConfig config = m_processor.determineMailReaderConfig();
        
        Assert.assertNotNull(config);
        updateConfigWithGoogleReadConfiguration(config, gmailAccount, gmailPassword);
        
        List<Message> msgs = m_processor.retrieveAckMessages();
        
        List<OnmsAcknowledgment> acks = m_processor.createAcks(msgs);
        
        Assert.assertNotNull(acks);
        Assert.assertEquals(4, acks.size());
        
        Assert.assertEquals(AckType.NOTIFICATION, acks.get(0).getAckType());
        Assert.assertEquals(AckAction.ACKNOWLEDGE, acks.get(0).getAckAction());
        Assert.assertEquals(Integer.valueOf(1), acks.get(0).getRefId());
        Assert.assertEquals(getUser()+"@gmail.com", acks.get(0).getAckUser());
        
        Assert.assertEquals(AckType.NOTIFICATION, acks.get(1).getAckType());
        Assert.assertEquals(AckAction.ACKNOWLEDGE, acks.get(1).getAckAction());
        Assert.assertEquals(Integer.valueOf(2), acks.get(1).getRefId());
        Assert.assertEquals(getUser()+"@gmail.com", acks.get(1).getAckUser());
        
        Assert.assertEquals(AckType.NOTIFICATION, acks.get(2).getAckType());
        Assert.assertEquals(AckAction.ACKNOWLEDGE, acks.get(2).getAckAction());
        Assert.assertEquals(Integer.valueOf(3), acks.get(2).getRefId());
        Assert.assertEquals(getUser()+"@gmail.com", acks.get(2).getAckUser());
        
        Assert.assertEquals(AckType.NOTIFICATION, acks.get(3).getAckType());
        Assert.assertEquals(AckAction.CLEAR, acks.get(3).getAckAction());
        Assert.assertEquals(Integer.valueOf(4), acks.get(3).getRefId());
        Assert.assertEquals(getUser()+"@gmail.com", acks.get(3).getAckUser());
    }

    private String getPassword() {
        return "bar";
    }

    private String getUser() {
        return "foo";
    }

    private SendmailMessage createAckMessage(String gmailAccount, String noticeId, String body) {
        SendmailMessage sendMsg = new SendmailMessage();
        sendMsg.setTo(gmailAccount+"@gmail.com");
        sendMsg.setFrom(gmailAccount+"@gmail.com");
        sendMsg.setSubject("Re: Notice #"+noticeId+":");
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
    
    private void updateConfigWithGoogleReadConfiguration(ReadmailConfig config, String gmailAccount, String gmailPassword) {
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
    }

}
