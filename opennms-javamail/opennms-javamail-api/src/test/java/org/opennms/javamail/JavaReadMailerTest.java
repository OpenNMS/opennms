/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.javamail;

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Authenticator;
import javax.mail.Flags.Flag;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.search.OrTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.core.mate.api.ContextKey;
import org.opennms.core.mate.api.MapScope;
import org.opennms.core.mate.api.Scope;
import org.opennms.netmgt.config.javamail.ReadmailConfig;
import org.opennms.netmgt.config.javamail.ReadmailHost;
import org.opennms.netmgt.config.javamail.ReadmailProtocol;
import org.opennms.netmgt.config.javamail.SendmailConfig;
import org.opennms.netmgt.config.javamail.SendmailHost;
import org.opennms.netmgt.config.javamail.SendmailMessage;
import org.opennms.netmgt.config.javamail.SendmailProtocol;
import org.opennms.netmgt.config.javamail.UserAuth;

public class JavaReadMailerTest {

    @Before
    public void setup() {
        final Map<ContextKey, String> map = new HashMap<>();
        map.put(new ContextKey("scv","javamailer:username"), "john");
        map.put(new ContextKey("scv","javamailer:password"), "doe");

        JavaMailerConfig.setSecureCredentialsVaultScope(new MapScope(Scope.ScopeName.GLOBAL, map));
    }

    /**
     * Un-ignore this test with a proper gmail account
     * @throws JavaMailerException
     * @throws MessagingException
     * @throws InterruptedException
     */
    @Test
    @Ignore("manual test to run against gmail")
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
        
        assertEquals(3, msgs.size());
        
        st = new OrTerm(new SubjectTerm(".*"+term1+" #.*"), new SubjectTerm(".*"+term2+" #.*"));
        
        try {
            msgs = readMailer.retrieveMessages(st);
        } catch (JavaMailerException e) {
            e.printStackTrace();
        }
        
        //Should find only term1 and term2 messages
        Assert.assertNotNull(msgs);
        assertEquals(2, msgs.size());

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

    @Test
    public void testMetadata() throws Exception {
        final JavaReadMailer javaReadMailer = createGoogleReadMailer(null, null);

        Authenticator authenticator = javaReadMailer.createAuthenticator("${scv:javamailer:username|ABC}", "${scv:javamailer:password|DEF}");
        final Method method = authenticator.getClass().getDeclaredMethod("getPasswordAuthentication");
        method.setAccessible(true);
        final PasswordAuthentication passwordAuthentication = (PasswordAuthentication) method.invoke(authenticator);

        assertEquals("john", passwordAuthentication.getUserName());
        assertEquals("doe", passwordAuthentication.getPassword());
    }
}
