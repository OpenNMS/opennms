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
package org.opennms.netmgt.notifd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.http.JUnitHttpServerExecutionListener;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.core.test.http.annotations.Webapp;
import org.opennms.netmgt.model.notifd.Argument;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath:/META-INF/opennms/applicationContext-pinger.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class WebhookNotificationStrategyIT {

    @Before
    public void resetServlet() {
        WebhookNotificationStrategyTestServlet.reset();
    }

    private static String url(final String query) {
        final int port = JUnitHttpServerExecutionListener.getPort();
        assertTrue(port > 0);
        return "http://localhost:" + port + "/hooks/webhook" + query;
    }

    private static Argument arg(final String argSwitch, final String value) {
        return new Argument(argSwitch, null, value, false);
    }

    private static Argument substituted(final String argSwitch, final String substitution) {
        return new Argument(argSwitch, substitution, "", false);
    }

    @Test
    @JUnitHttpServer(webapps={
            @Webapp(context="/hooks", path="src/test/resources/WebhookNotificationStrategyTest")
    })
    public void testSlackStyleJsonPost() {
        final List<Argument> arguments = new ArrayList<>();
        arguments.add(arg("-url", url("")));
        arguments.add(substituted("-body", "{\"text\": \"${subject}\\n${textMessage}\"}"));
        arguments.add(arg("-subject", "Node down"));
        arguments.add(arg("-tm", "webServer1 is down"));

        assertEquals(0, new WebhookNotificationStrategy().send(arguments));

        assertEquals("POST", WebhookNotificationStrategyTestServlet.getMethod());
        assertEquals("application/json", WebhookNotificationStrategyTestServlet.getContentType());
        assertEquals("{\"text\": \"Node down\\nwebServer1 is down\"}",
                WebhookNotificationStrategyTestServlet.getBody());
    }

    @Test
    @JUnitHttpServer(webapps={
            @Webapp(context="/hooks", path="src/test/resources/WebhookNotificationStrategyTest")
    })
    public void testQuotedSubjectStaysValidJson() {
        final List<Argument> arguments = new ArrayList<>();
        arguments.add(arg("-url", url("")));
        arguments.add(substituted("-body", "{\"content\": \"${subject}\"}"));
        arguments.add(arg("-subject", "Interface \"eth0\" down"));

        assertEquals(0, new WebhookNotificationStrategy().send(arguments));

        final String body = WebhookNotificationStrategyTestServlet.getBody();
        assertEquals("{\"content\": \"Interface \\\"eth0\\\" down\"}", body);
        assertTrue(WebhookNotificationStrategy.isWellFormedJson(body));
    }

    @Test
    @JUnitHttpServer(webapps={
            @Webapp(context="/hooks", path="src/test/resources/WebhookNotificationStrategyTest")
    })
    public void testCustomHeadersAreSent() {
        final List<Argument> arguments = new ArrayList<>();
        arguments.add(arg("-url", url("")));
        arguments.add(substituted("-body", "{\"text\": \"hi\"}"));
        arguments.add(arg("-header-Authorization", "Bearer sekrit"));
        arguments.add(arg("-header-X-Route", "ops"));

        assertEquals(0, new WebhookNotificationStrategy().send(arguments));

        assertEquals("Bearer sekrit", WebhookNotificationStrategyTestServlet.getHeader("Authorization"));
        assertEquals("ops", WebhookNotificationStrategyTestServlet.getHeader("X-Route"));
    }

    @Test
    @JUnitHttpServer(webapps={
            @Webapp(context="/hooks", path="src/test/resources/WebhookNotificationStrategyTest")
    })
    public void testEmptyTwoOhFourCountsAsSuccess() {
        // Discord replies 204 with no body. The strategy this replaces only ever
        // accepted a literal "ok" body, so this case used to be reported as failed.
        final List<Argument> arguments = new ArrayList<>();
        arguments.add(arg("-url", url("?status=204")));
        arguments.add(substituted("-body", "{\"content\": \"${subject}\"}"));
        arguments.add(arg("-subject", "Node down"));

        assertEquals(0, new WebhookNotificationStrategy().send(arguments));
    }

    @Test
    @JUnitHttpServer(webapps={
            @Webapp(context="/hooks", path="src/test/resources/WebhookNotificationStrategyTest")
    })
    public void testServerErrorFails() {
        final List<Argument> arguments = new ArrayList<>();
        arguments.add(arg("-url", url("?status=500&respond=nope")));
        arguments.add(substituted("-body", "{\"text\": \"hi\"}"));

        assertEquals(1, new WebhookNotificationStrategy().send(arguments));
    }

    @Test
    @JUnitHttpServer(webapps={
            @Webapp(context="/hooks", path="src/test/resources/WebhookNotificationStrategyTest")
    })
    public void testSuccessMatchCatchesAnErrorBehindATwoHundred() {
        final List<Argument> arguments = new ArrayList<>();
        arguments.add(arg("-url", url("?respond=channel_not_found")));
        arguments.add(substituted("-body", "{\"text\": \"hi\"}"));
        arguments.add(arg("-success-match", "^ok$"));

        assertEquals(1, new WebhookNotificationStrategy().send(arguments));
    }

    @Test
    @JUnitHttpServer(webapps={
            @Webapp(context="/hooks", path="src/test/resources/WebhookNotificationStrategyTest")
    })
    public void testFormEncodedPutIsNotEscapedAsJson() {
        final List<Argument> arguments = new ArrayList<>();
        arguments.add(arg("-url", url("")));
        arguments.add(arg("-method", "put"));
        arguments.add(arg("-content-type", "application/x-www-form-urlencoded"));
        arguments.add(substituted("-body", "text=${subject}"));
        arguments.add(arg("-subject", "He said \"down\""));

        assertEquals(0, new WebhookNotificationStrategy().send(arguments));

        assertEquals("PUT", WebhookNotificationStrategyTestServlet.getMethod());
        assertEquals("text=He said \"down\"", WebhookNotificationStrategyTestServlet.getBody());
    }

    @Test
    @JUnitHttpServer(webapps={
            @Webapp(context="/hooks", path="src/test/resources/WebhookNotificationStrategyTest")
    })
    public void testUrlFromSystemProperty() {
        System.setProperty("org.opennms.netmgt.notifd.webhook.teams.url", url(""));
        try {
            final List<Argument> arguments = new ArrayList<>();
            arguments.add(arg("-name", "teams"));
            arguments.add(arg("-url", ""));
            arguments.add(substituted("-body", "{\"text\": \"hi\"}"));

            assertEquals(0, new WebhookNotificationStrategy().send(arguments));
            assertEquals("POST", WebhookNotificationStrategyTestServlet.getMethod());
        } finally {
            System.clearProperty("org.opennms.netmgt.notifd.webhook.teams.url");
        }
    }
}
