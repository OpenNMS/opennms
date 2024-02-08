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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.json.simple.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.http.JUnitHttpServerExecutionListener;
import org.opennms.core.test.http.annotations.JUnitHttpServer;
import org.opennms.core.test.http.annotations.Webapp;
import org.opennms.netmgt.model.notifd.Argument;
import org.opennms.netmgt.model.notifd.NotificationStrategy;
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
public class MattermostNotificationStrategyIT {
    /*
     * Test method for 'org.opennms.netmgt.notifd.MattermostNotificationStrategy.send(List)'
     */
    @Test
    @JUnitHttpServer(webapps={
            @Webapp(context="/hooks", path="src/test/resources/MattermostNotificationStrategyTest")
    })
    public void testSendValidJustArgs() {
        final int port = JUnitHttpServerExecutionListener.getPort();
        assertTrue(port > 0);

        final NotificationStrategy ns = new MattermostNotificationStrategy();
        final List<Argument> arguments = new ArrayList<Argument>();

        // Set these properties. We will override them with Args on the first run.
        System.setProperty("org.opennms.netmgt.notifd.mattermost.webhookURL", "http://localhost:" + port + "/hooks/abunchofstuffthatidentifiesawebhook");
        System.setProperty("org.opennms.netmgt.notifd.mattermost.channel", "integrationtestsXX");
        System.setProperty("org.opennms.netmgt.notifd.mattermost.iconURL", "http://opennms.org/logo.pngXX");
        System.setProperty("org.opennms.netmgt.notifd.mattermost.iconEmoji", ":shipitXX:");
        System.setProperty("org.opennms.netmgt.notifd.mattermost.username", "opennmsXX");

        arguments.add(new Argument("-url", null, "http://localhost:" + port + "/hooks/abunchofstuffthatidentifiesawebhook", false));
        arguments.add(new Argument("-channel", null, "integrationtests", false));
        arguments.add(new Argument("-username", null, "opennms", false));
        arguments.add(new Argument("-iconurl", null, "http://opennms.org/logo.png", false));
        arguments.add(new Argument("-iconemoji", null, ":shipit:", false));
        arguments.add(new Argument("-subject", null, "Test", false));
        arguments.add(new Argument("-tm", null, "This is only a test", false));

        int statusCode = ns.send(arguments);
        assertEquals(0, statusCode);

        JSONObject inputJson = MattermostNotificationStrategyTestServlet.getInputJson();
        assertNotNull(inputJson);
        assertEquals("opennms", inputJson.get("username"));
        assertEquals("**Test**\nThis is only a test", inputJson.get("text"));
        assertEquals("integrationtests", inputJson.get("channel"));
        assertEquals("http://opennms.org/logo.png", inputJson.get("icon_url"));
        assertEquals(":shipit:", inputJson.get("icon_emoji"));
        assertEquals(5, inputJson.size());

        // Now do it again, without the Args, and verify that the property values come out
        arguments.clear();
        arguments.add(new Argument("-subject", null, "Test again", false));
        arguments.add(new Argument("-tm", null, "This is only a second test", false));

        statusCode = ns.send(arguments);
        assertEquals(0, statusCode);

        inputJson = MattermostNotificationStrategyTestServlet.getInputJson();
        assertNotNull(inputJson);
        assertEquals("opennmsXX", inputJson.get("username"));
        assertEquals("**Test again**\nThis is only a second test", inputJson.get("text"));
        assertEquals("integrationtestsXX", inputJson.get("channel"));
        assertEquals("http://opennms.org/logo.pngXX", inputJson.get("icon_url"));
        assertEquals(":shipitXX:", inputJson.get("icon_emoji"));
        assertEquals(5, inputJson.size());

        // Now do it again without the leading - in switch names
        arguments.clear();
        arguments.add(new Argument("url", null, "http://localhost:" + port + "/hooks/abunchofstuffthatidentifiesawebhook", false));
        arguments.add(new Argument("channel", null, "integrationtests", false));
        arguments.add(new Argument("username", null, "opennms", false));
        arguments.add(new Argument("iconurl", null, "http://opennms.org/logo.png", false));
        arguments.add(new Argument("iconemoji", null, ":shipit:", false));
        arguments.add(new Argument("subject", null, "Test", false));
        arguments.add(new Argument("tm", null, "This is only a test", false));

        statusCode = ns.send(arguments);
        assertEquals(0, statusCode);

        inputJson = MattermostNotificationStrategyTestServlet.getInputJson();
        assertNotNull(inputJson);
        assertEquals("opennms", inputJson.get("username"));
        assertEquals("**Test**\nThis is only a test", inputJson.get("text"));
        assertEquals("integrationtests", inputJson.get("channel"));
        assertEquals("http://opennms.org/logo.png", inputJson.get("icon_url"));
        assertEquals(":shipit:", inputJson.get("icon_emoji"));
        assertEquals(5, inputJson.size());
    }

}
