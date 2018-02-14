/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.notifd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-pinger.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class SlackNotificationStrategyIT {
    /*
     * Test method for 'org.opennms.netmgt.notifd.MattermostNotificationStrategy.send(List)'
     */
    @Test
    @JUnitHttpServer(webapps={
            @Webapp(context="/hooks", path="src/test/resources/MattermostNotificationStrategyTest")
    })
    public void testSendValidMessage() {
        final int port = JUnitHttpServerExecutionListener.getPort();
        assertTrue(port > 0);
        try {

            final NotificationStrategy ns = new SlackNotificationStrategy();
            final List<Argument> arguments = new ArrayList<>();

            // Set these properties. We will override them with Args on the first run.
            System.setProperty("org.opennms.netmgt.notifd.slack.webhookURL", "http://localhost:" + port + "/hooks/abunchofstuffthatidentifiesawebhook");
            System.setProperty("org.opennms.netmgt.notifd.slack.channel", "integrationtestsXX");
            System.setProperty("org.opennms.netmgt.notifd.slack.iconURL", "http://opennms.org/logo.pngXX");
            System.setProperty("org.opennms.netmgt.notifd.slack.iconEmoji", ":shipitXX:");
            System.setProperty("org.opennms.netmgt.notifd.slack.username", "opennmsXX");

            arguments.add(new Argument("url", null, "http://localhost:" + port + "/hooks/abunchofstuffthatidentifiesawebhook", false));
            arguments.add(new Argument("channel", null, "integrationtests", false));
            arguments.add(new Argument("username", null, "opennms", false));
            arguments.add(new Argument("iconurl", null, "http://opennms.org/logo.png", false));
            arguments.add(new Argument("iconemoji", null, ":shipit:", false));
            arguments.add(new Argument("-subject", null, "Test", false));
            arguments.add(new Argument("-tm", null, "This is only a test", false));
            
            int statusCode = ns.send(arguments);
            assertEquals(0, statusCode);
            
            JSONObject inputJson = MattermostNotificationStrategyTestServlet.getInputJson();
            assertNotNull(inputJson);
            assertEquals("opennms", inputJson.get("username"));
            assertEquals("*Test*\nThis is only a test", inputJson.get("text"));
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
            assertEquals("*Test again*\nThis is only a second test", inputJson.get("text"));
            assertEquals("integrationtestsXX", inputJson.get("channel"));
            assertEquals("http://opennms.org/logo.pngXX", inputJson.get("icon_url"));
            assertEquals(":shipitXX:", inputJson.get("icon_emoji"));
            assertEquals(5, inputJson.size());

        } catch (Throwable e) {
            e.printStackTrace();
            fail("Caught Exception: " + e.getMessage());
        }
    }

}
