/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import javax.mail.Message;

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

    private JavaMailer createMailer(String subject) throws Exception {
        JavaMailer jm = new JavaMailer();

        jm.setFrom(TEST_ADDRESS);
        jm.setMessageText(subject + ": " + InetAddressUtils.getLocalHostAddress());
        jm.setSubject("Testing JavaMailer");
        jm.setTo(TEST_ADDRESS);

        return jm;
    }

    @Test
    public void testReplyTo() throws Exception {
        final Properties p = new Properties();
        p.setProperty("org.opennms.core.utils.replyToAddress", "christian@opennms.org");
        final JavaMailer jm = new JavaMailer(p);

        jm.setFrom(TEST_ADDRESS);
        jm.setMessageText("The subject");
        jm.setSubject("Testing JavaMailer");
        jm.setTo(TEST_ADDRESS);

        final Message message = jm.buildMessage();
        assertEquals(1, message.getReplyTo().length);
        assertEquals("christian@opennms.org", jm.buildMessage().getReplyTo()[0].toString());
    }

    @Test
    public void testEmptyReplyTo() throws Exception {
        final Properties p = new Properties();
        p.setProperty("org.opennms.core.utils.replyToAddress", "");
        final JavaMailer jm = new JavaMailer(p);

        jm.setFrom(TEST_ADDRESS);
        jm.setMessageText("The subject");
        jm.setSubject("Testing JavaMailer");
        jm.setTo(TEST_ADDRESS);

        final Message message = jm.buildMessage();
        assertEquals(1, message.getReplyTo().length);
        assertEquals("test@opennms.org", jm.buildMessage().getReplyTo()[0].toString());
    }

    @Test
    public void testNullReplyTo() throws Exception {
        final JavaMailer jm = new JavaMailer();

        jm.setFrom(TEST_ADDRESS);
        jm.setMessageText("The subject");
        jm.setSubject("Testing JavaMailer");
        jm.setTo(TEST_ADDRESS);

        final Message message = jm.buildMessage();
        assertEquals(1, message.getReplyTo().length);
        assertEquals("test@opennms.org", jm.buildMessage().getReplyTo()[0].toString());
    }
}
