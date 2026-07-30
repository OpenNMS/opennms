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
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.mate.api.EmptyScope;
import org.opennms.netmgt.config.javamail.ReadmailConfig;
import org.opennms.netmgt.config.javamail.ReadmailHost;
import org.opennms.netmgt.config.javamail.ReadmailProtocol;
import org.opennms.netmgt.config.javamail.SendmailConfig;
import org.opennms.netmgt.config.javamail.SendmailHost;
import org.opennms.netmgt.config.javamail.SendmailMessage;
import org.opennms.netmgt.config.javamail.SendmailProtocol;
import org.opennms.netmgt.config.javamail.UserAuth;
import org.springframework.core.io.ClassPathResource;

import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.GreenMailUtil;
import com.icegreen.greenmail.util.ServerSetup;

import jakarta.mail.Message;

/**
 * Wire-level tests against an embedded GreenMail server: these exercise the
 * real Angus SMTP/IMAP providers rather than mocking the session.
 */
public class JavaMailerWireTest {

    private GreenMail greenMail;
    private int smtpPort;
    private int imapPort;

    @Before
    public void setUp() throws IOException {
        final File homeDir = new ClassPathResource("/etc/javamail-configuration.properties")
                .getFile().getParentFile().getParentFile();
        System.setProperty("opennms.home", homeDir.getAbsolutePath());

        // no Spring context registry in this test; seed the interpolation scopes
        JavaMailerConfig.setSecureCredentialsVaultScope(EmptyScope.EMPTY);
        JavaMailerConfig.setTokenScope(EmptyScope.EMPTY);

        greenMail = new GreenMail(new ServerSetup[] {
                new ServerSetup(0, "127.0.0.1", ServerSetup.PROTOCOL_SMTP).dynamicPort(),
                new ServerSetup(0, "127.0.0.1", ServerSetup.PROTOCOL_IMAP).dynamicPort(),
        });
        greenMail.start();
        smtpPort = greenMail.getSmtp().getPort();
        imapPort = greenMail.getImap().getPort();
    }

    @After
    public void tearDown() {
        if (greenMail != null) {
            greenMail.stop();
        }
    }

    private Properties smtpProps() {
        final Properties props = new Properties();
        props.setProperty("org.opennms.core.utils.mailHost", "127.0.0.1");
        props.setProperty("org.opennms.core.utils.smtpport", String.valueOf(smtpPort));
        props.setProperty("org.opennms.core.utils.useJMTA", "false");
        // fail fast instead of hanging the build on a protocol mismatch
        props.setProperty("mail.smtp.connectiontimeout", "5000");
        props.setProperty("mail.smtp.timeout", "5000");
        return props;
    }

    @Test
    public void canSendPlainSmtp() throws Exception {
        final JavaMailer jm = new JavaMailer(smtpProps());
        jm.setFrom("sender@opennms.org");
        jm.setTo("receiver@opennms.org");
        jm.setSubject("wire test");
        jm.setMessageText("plain body");
        jm.mailSend();

        assertTrue(greenMail.waitForIncomingEmail(5000, 1));
        final Message received = greenMail.getReceivedMessages()[0];
        assertEquals("wire test", received.getSubject());
        assertEquals("plain body", GreenMailUtil.getBody(received));
    }

    @Test
    public void canSendAuthenticatedSmtp() throws Exception {
        greenMail.setUser("authuser@opennms.org", "authuser", "secret");

        final Properties props = smtpProps();
        props.setProperty("org.opennms.core.utils.authenticate", "true");
        props.setProperty("org.opennms.core.utils.authenticateUser", "authuser");
        props.setProperty("org.opennms.core.utils.authenticatePassword", "secret");

        final JavaMailer jm = new JavaMailer(props);
        jm.setFrom("sender@opennms.org");
        jm.setTo("authuser@opennms.org");
        jm.setSubject("authenticated wire test");
        jm.setMessageText("authenticated body");
        jm.mailSend();

        assertTrue(greenMail.waitForIncomingEmail(5000, 1));
        assertEquals("authenticated wire test", greenMail.getReceivedMessages()[0].getSubject());
    }

    @Test
    public void canSendViaJavaSendMailerFromXmlStyleConfig() throws Exception {
        final SendmailConfig config = new SendmailConfig();
        config.setName("wire-test");
        config.setUseAuthentication(false);
        config.setUseJmta(false);

        final SendmailHost host = new SendmailHost();
        host.setHost("127.0.0.1");
        host.setPort(smtpPort);
        config.setSendmailHost(host);

        final SendmailProtocol protocol = new SendmailProtocol();
        protocol.setTransport("smtp");
        config.setSendmailProtocol(protocol);

        final SendmailMessage message = new SendmailMessage();
        message.setFrom("sender@opennms.org");
        message.setTo("receiver@opennms.org");
        message.setSubject("sendmailer wire test");
        message.setBody("sendmailer body");
        config.setSendmailMessage(message);

        // useJmProps=false: do not overlay javamail-configuration.properties
        final JavaSendMailer sendMailer = new JavaSendMailer(config, false);
        sendMailer.send();

        assertTrue(greenMail.waitForIncomingEmail(5000, 1));
        final Message received = greenMail.getReceivedMessages()[0];
        assertEquals("sendmailer wire test", received.getSubject());
        assertEquals("sender@opennms.org", received.getFrom()[0].toString());
    }

    @Test
    public void canReadViaImap() throws Exception {
        greenMail.setUser("reader@opennms.org", "reader", "readersecret");
        GreenMailUtil.sendTextEmail("reader@opennms.org", "sender@opennms.org",
                "imap read test", "imap body", greenMail.getSmtp().getServerSetup());
        assertTrue(greenMail.waitForIncomingEmail(5000, 1));

        final ReadmailConfig config = new ReadmailConfig();
        config.setName("wire-read");
        final ReadmailHost host = new ReadmailHost();
        host.setHost("127.0.0.1");
        host.setPort(imapPort);
        final ReadmailProtocol protocol = new ReadmailProtocol();
        protocol.setTransport("imap");
        host.setReadmailProtocol(protocol);
        config.setReadmailHost(host);
        final UserAuth auth = new UserAuth();
        auth.setUserName("reader");
        auth.setPassword("readersecret");
        config.setUserAuth(auth);

        final JavaReadMailer readMailer = new JavaReadMailer(config, false);
        final List<Message> messages = readMailer.retrieveMessages();
        assertEquals(1, messages.size());
        assertEquals("imap read test", messages.get(0).getSubject());
    }

    /**
     * Angus implements XOAUTH2 natively; this asserts the exact SASL initial
     * response bytes reach the wire (base64 of "user=U\1auth=Bearer T\1\1"),
     * which is the contract Microsoft 365 and Gmail validate.
     */
    @Test
    public void xoauth2AuthCommandReachesTheWire() throws Exception {
        final AtomicReference<String> authLine = new AtomicReference<>();
        final List<String> transcript = java.util.Collections.synchronizedList(new java.util.ArrayList<>());
        try (ServerSocket server = new ServerSocket(0)) {
            final Thread fake = new Thread(() -> {
                try (Socket socket = server.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
                     PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                    out.print("220 fake ESMTP\r\n"); out.flush();
                    String line;
                    while ((line = in.readLine()) != null) {
                        transcript.add(line);
                        final String upper = line.toUpperCase();
                        if (upper.startsWith("EHLO")) {
                            out.print("250-fake\r\n250 AUTH XOAUTH2\r\n");
                        } else if (upper.startsWith("AUTH XOAUTH2")) {
                            authLine.set(line);
                            out.print("235 2.7.0 accepted\r\n");
                        } else if (upper.startsWith("MAIL") || upper.startsWith("RCPT") || upper.startsWith("NOOP") || upper.startsWith("RSET")) {
                            out.print("250 OK\r\n");
                        } else if (upper.startsWith("DATA")) {
                            out.print("354 go\r\n"); out.flush();
                            // swallow message content until the terminating dot
                            while ((line = in.readLine()) != null && !line.equals(".")) {
                                // DATA payload, no response expected
                            }
                            out.print("250 queued\r\n");
                        } else if (upper.startsWith("QUIT")) {
                            out.print("221 bye\r\n"); out.flush();
                            break;
                        } else {
                            out.print("250 OK\r\n");
                        }
                        out.flush();
                    }
                } catch (final IOException e) {
                    // socket teardown races are fine; the assertion below decides
                }
            });
            fake.start();

            final Properties props = smtpProps();
            props.setProperty("org.opennms.core.utils.smtpport", String.valueOf(server.getLocalPort()));
            props.setProperty("org.opennms.core.utils.authenticate", "true");
            props.setProperty("org.opennms.core.utils.authenticateUser", "svc@example.com");
            props.setProperty("org.opennms.core.utils.authenticatePassword", "test-access-token");
            props.setProperty("mail.smtp.auth.mechanisms", "XOAUTH2");

            final JavaMailer jm = new JavaMailer(props);
            jm.setFrom("svc@example.com");
            jm.setTo("someone@example.com");
            jm.setSubject("xoauth2 wire test");
            jm.setMessageText("body");
            try {
                jm.mailSend();
            } catch (final JavaMailerException e) {
                throw new AssertionError("send failed; transcript so far: " + transcript, e);
            }
            fake.join(5000);
        }

        final String line = authLine.get();
        assertTrue("no AUTH XOAUTH2 seen on the wire; transcript: " + transcript, line != null);
        final String decoded = new String(Base64.getDecoder().decode(line.substring("AUTH XOAUTH2 ".length())), StandardCharsets.US_ASCII);
        assertEquals("user=svc@example.com\u0001auth=Bearer test-access-token\u0001\u0001", decoded);
    }
}
