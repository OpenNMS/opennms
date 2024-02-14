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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.mail.Message;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.mate.api.ContextKey;
import org.opennms.core.mate.api.MapScope;
import org.opennms.core.mate.api.Scope;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.utils.InetAddressUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
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

        final Map<ContextKey, String> map = new HashMap<>();
        map.put(new ContextKey("scv","javamailer:username"), "john");
        map.put(new ContextKey("scv","javamailer:password"), "doe");

        JavaMailerConfig.setSecureCredentialsVaultScope(new MapScope(Scope.ScopeName.GLOBAL, map));
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

    @Test
    public void testMetadata() throws Exception {
        final JavaMailer jm = new JavaMailer();
        assertEquals("john", jm.getUser());
        assertEquals("doe", jm.getPassword());
    }
}
