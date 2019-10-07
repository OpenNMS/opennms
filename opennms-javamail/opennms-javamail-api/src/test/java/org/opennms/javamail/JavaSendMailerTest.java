/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

import static org.junit.Assert.assertEquals;

import javax.mail.internet.MimeMessage;

import org.junit.Test;
import org.opennms.netmgt.config.javamail.SendmailConfig;
import org.opennms.netmgt.config.javamail.SendmailHost;
import org.opennms.netmgt.config.javamail.SendmailProtocol;
import org.opennms.netmgt.config.javamail.UserAuth;
import org.opennms.netmgt.config.javamail.SendmailMessage;

public class JavaSendMailerTest {

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
}
