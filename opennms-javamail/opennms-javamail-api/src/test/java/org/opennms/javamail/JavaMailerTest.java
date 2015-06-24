/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
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

import java.io.File;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.test.ThrowableAnticipator;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.test.annotation.IfProfileValue;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Test the JavaMailer class.
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({}) 
public class JavaMailerTest {
    private static final String TEST_ADDRESS = "test@opennms.org";

    @Before
    public void setUp() throws IOException {
        MockLogAppender.setupLogging();

        Resource resource = new ClassPathResource("/etc/javamail-configuration.properties");

        File homeDir = resource.getFile().getParentFile().getParentFile();
        System.out.println("homeDir: "+homeDir.getAbsolutePath());

        System.setProperty("opennms.home", homeDir.getAbsolutePath());
    }

    @After
    public void tearDown() throws Throwable {
       // MockLogAppender.assertNoWarningsOrGreater();
    }

    @Test
    @IfProfileValue(name="runMailTests", value="true")
    @Ignore
    public final void testJavaMailerWithDefaults() throws Exception {
        JavaMailer jm = createMailer("Test message from testJavaMailer using details");

        jm.mailSend();
    }

    @Test
    @Ignore
    public final void testJavaMailerWithNullTo() throws Exception {
        JavaMailer jm = createMailer("Test message from testJavaMailer without MTA");

        jm.setTo(null);

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new JavaMailerException("Cannot have a null to address."));
        try {
            jm.mailSend();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    @Ignore
    public final void testJavaMailerWithEmptyTo() throws Exception {
        JavaMailer jm = createMailer("Test message from testJavaMailer without MTA");

        jm.setTo("");

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new JavaMailerException("Cannot have an empty to address."));
        try {
            jm.mailSend();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    @Ignore
    public final void testJavaMailerWithNullFrom() throws Exception {
        JavaMailer jm = createMailer("Test message from testJavaMailer without MTA");

        jm.setFrom(null);

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new JavaMailerException("Cannot have a null from address."));
        try {
            jm.mailSend();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    @Ignore
    public final void testJavaMailerWithEmptyFrom() throws Exception {
        JavaMailer jm = createMailer("Test message from testJavaMailer without MTA");

        jm.setFrom("");

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new JavaMailerException("Cannot have an empty from address."));
        try {
            jm.mailSend();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    @Ignore
    public final void testJavaMailerWithNullSubject() throws Exception {
        JavaMailer jm = createMailer("Test message from testJavaMailer without MTA");

        jm.setSubject(null);

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new JavaMailerException("Cannot have a null subject."));
        try {
            jm.mailSend();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    @Ignore
    public final void testJavaMailerWithEmptySubject() throws Exception {
        JavaMailer jm = createMailer("Test message from testJavaMailer without MTA");

        jm.setSubject("");

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new JavaMailerException("Cannot have an empty subject."));
        try {
            jm.mailSend();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    @Ignore
    public final void testJavaMailerWithNullMessageText() throws Exception {
        JavaMailer jm = createMailer("Test message from testJavaMailer without MTA");

        jm.setMessageText(null);

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new JavaMailerException("Cannot have a null messageText."));
        try {
            jm.mailSend();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    @Test
    @Ignore
    public final void testJavaMailerWithEmptyMessageText() throws Exception {
        JavaMailer jm = createMailer("Test message from testJavaMailer without MTA");

        jm.setMessageText("");

        ThrowableAnticipator ta = new ThrowableAnticipator();
        ta.anticipate(new JavaMailerException("Cannot have an empty messageText."));
        try {
            jm.mailSend();
        } catch (Throwable t) {
            ta.throwableReceived(t);
        }
        ta.verifyAnticipated();
    }

    // FIXME: took this out a david's suggestion
    @Ignore
    public final void testJavaMailerUsingMTAExplicitly() throws Exception {
        JavaMailer jm = createMailer("Test message from testJavaMailer using MTA explicitly");

        if (jm.isSmtpSsl()) {
            return;
        }
        jm.setUseJMTA(true);

        jm.mailSend();
    }

    // FIXME: took this out a david's suggestion
    @Ignore
    public final void testJavaMailerUsingMTAByTransport() throws Exception {
        JavaMailer jm = createMailer("Test message from testJavaMailer using MTA by transport");

        if (jm.isSmtpSsl()) {
            return;
        }

        jm.setUseJMTA(false);
        jm.setTransport("mta");

        jm.mailSend();
    }

    @Ignore
    @IfProfileValue(name="runMailTests", value="true")
    public final void testJavaMailerWithFileAttachment() throws Exception {
        JavaMailer jm = createMailer("Test message with file attachment from testJavaMailer");

        jm.setFileName("/etc/shells");

        jm.mailSend();
    }

    private JavaMailer createMailer(String subject) throws Exception {
        JavaMailer jm = new JavaMailer();

        jm.setFrom(TEST_ADDRESS);
        jm.setMessageText(subject + ": " + InetAddressUtils.getLocalHostAddress());
        jm.setSubject("Testing JavaMailer");
        jm.setTo(TEST_ADDRESS);

        return jm;
    }
}
