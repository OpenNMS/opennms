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
import java.util.HashMap;
import java.util.Map;

import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.internet.MimeMessage;

import org.junit.Before;
import org.junit.Test;
import org.opennms.core.mate.api.ContextKey;
import org.opennms.core.mate.api.MapScope;
import org.opennms.core.mate.api.Scope;
import org.opennms.netmgt.config.javamail.SendmailConfig;
import org.opennms.netmgt.config.javamail.SendmailHost;
import org.opennms.netmgt.config.javamail.SendmailProtocol;
import org.opennms.netmgt.config.javamail.UserAuth;
import org.opennms.netmgt.config.javamail.SendmailMessage;

public class JavaSendMailerTest {

    @Before
    public void setup() {
        final Map<ContextKey, String> map = new HashMap<>();
        map.put(new ContextKey("scv","javamailer:username"), "john");
        map.put(new ContextKey("scv","javamailer:password"), "doe");

        JavaMailerConfig.setSecureCredentialsVaultScope(new MapScope(Scope.ScopeName.GLOBAL, map));
    }

    private JavaSendMailer createSendMailer() throws JavaMailerException {

        SendmailConfig config = new SendmailConfig();

        config.setAttemptInterval(1000l);
        config.setDebug(true);
        config.setName("test");

        SendmailMessage sendmailMessage = new SendmailMessage();
        sendmailMessage.setBody("some body");
        sendmailMessage.setFrom("from.me@somewhere.com");
        sendmailMessage.setSubject("some message");
        sendmailMessage.setTo("recipient@foo.bar.com");
        config.setSendmailMessage(sendmailMessage);

        SendmailHost host = new SendmailHost();
        host.setHost("smtp.foo.bar.com");
        host.setPort(465);
        config.setSendmailHost(host);

        SendmailProtocol protocol = new SendmailProtocol();
        protocol.setSslEnable(true);
        protocol.setTransport("smtps");
        config.setSendmailProtocol(protocol);

        config.setUseAuthentication(true);
        config.setUseJmta(false);
        UserAuth auth = new UserAuth();
        auth.setUserName("foo");
        auth.setPassword("bar");
        config.setUserAuth(auth);

        return new JavaSendMailer(config);
    }

    @Test
    public void testReplyTo() throws Exception {
        final JavaSendMailer sendMailer = createSendMailer();

        final SendmailMessage sendmailMessage = new SendmailMessage();
        sendmailMessage.setBody("body");
        sendmailMessage.setFrom("root@foo.bar.com");
        sendmailMessage.setSubject("The subject");
        sendmailMessage.setTo("test@gmail.com");
        sendmailMessage.setReplyTo("christian@opennms.org");

        final MimeMessage mimeMessage = sendMailer.buildMimeMessage(sendmailMessage);

        assertEquals(1, mimeMessage.getReplyTo().length);
        assertEquals("christian@opennms.org", mimeMessage.getReplyTo()[0].toString());
    }

    @Test
    public void testEmptyReplyTo() throws Exception {
        final JavaSendMailer sendMailer = createSendMailer();

        final SendmailMessage sendmailMessage = new SendmailMessage();
        sendmailMessage.setBody("body");
        sendmailMessage.setFrom("root@foo.bar.com");
        sendmailMessage.setSubject("The subject");
        sendmailMessage.setTo("test@gmail.com");
        sendmailMessage.setReplyTo("");

        final MimeMessage mimeMessage = sendMailer.buildMimeMessage(sendmailMessage);

        assertEquals(1, mimeMessage.getReplyTo().length);
        assertEquals("root@foo.bar.com", mimeMessage.getReplyTo()[0].toString());
    }

    @Test
    public void testNullReplyTo() throws Exception {
        final JavaSendMailer sendMailer = createSendMailer();

        final SendmailMessage sendmailMessage = new SendmailMessage();
        sendmailMessage.setBody("body");
        sendmailMessage.setFrom("root@foo.bar.com");
        sendmailMessage.setSubject("The subject");
        sendmailMessage.setTo("test@gmail.com");
        sendmailMessage.setReplyTo(null);

        final MimeMessage mimeMessage = sendMailer.buildMimeMessage(sendmailMessage);

        assertEquals(1, mimeMessage.getReplyTo().length);
        assertEquals("root@foo.bar.com", mimeMessage.getReplyTo()[0].toString());
    }

    @Test
    public void testMetadata() throws Exception {
        final JavaSendMailer javaSendMailer = createSendMailer();

        Authenticator authenticator = javaSendMailer.createAuthenticator("${scv:javamailer:username|ABC}", "${scv:javamailer:password|DEF}");
        final Method method = authenticator.getClass().getDeclaredMethod("getPasswordAuthentication");
        method.setAccessible(true);
        final PasswordAuthentication passwordAuthentication = (PasswordAuthentication) method.invoke(authenticator);

        assertEquals("john", passwordAuthentication.getUserName());
        assertEquals("doe", passwordAuthentication.getPassword());
    }
}
