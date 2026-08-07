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
package org.opennms.web.rest.v1;


import java.io.File;
import java.nio.charset.Charset;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations={
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-mockConfigManager.xml",
        "classpath*:/META-INF/opennms/component-service.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment(systemProperties="org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase
public class NotificationConfigRestServiceIT extends AbstractSpringJerseyRestTestCase {


    private String m_onmsHome;

    private static String s_previousOpennmsHome;


    @Override
    protected void beforeServletStart() throws Exception {
        MockLogAppender.setupLogging();
        File etc = new File("target/test-work-dir/etc");
        etc.mkdirs();
        m_onmsHome = etc.getParent();
        if (s_previousOpennmsHome == null) {
            s_previousOpennmsHome = System.getProperty("opennms.home", "");
        }
        System.setProperty("opennms.home", m_onmsHome);
        ConfigurationTestUtils.setRelativeHomeDirectory(m_onmsHome);

        FileUtils.writeStringToFile(new File(etc, "notifd-configuration.xml"), "<?xml version=\"1.0\"?>"
                + "<notifd-configuration status=\"off\" match-all=\"true\">"
                + "<queue><queue-id>default</queue-id><interval>20s</interval>"
                + "<handler-class><name>org.opennms.netmgt.notifd.DefaultQueueHandler</name></handler-class>"
                + "</queue>"
                + "</notifd-configuration>", Charset.defaultCharset());
        // NotifdConfigFactory.init() is deliberately not called here; the
        // service initializes the factories itself

        FileUtils.writeStringToFile(new File(etc, "notifications.xml"), "<?xml version=\"1.0\"?>"
                + "<notifications xmlns=\"http://xmlns.opennms.org/xsd/notifications\">"
                + "<header><rev>1.2</rev><created>Wednesday, February 6, 2002 10:10:00 AM EST</created><mstation>localhost</mstation></header>"
                + "<notification name=\"junitNotification\" status=\"on\">"
                + "<uei>uei.opennms.org/nodes/nodeDown</uei>"
                + "<rule>IPADDR != '0.0.0.0'</rule>"
                + "<destinationPath>Email-Admin</destinationPath>"
                + "<text-message>node down</text-message>"
                + "</notification>"
                + "</notifications>", Charset.defaultCharset());

        FileUtils.writeStringToFile(new File(etc, "destinationPaths.xml"), "<?xml version=\"1.0\"?>"
                + "<destinationPaths>"
                + "<header><rev>1.2</rev><created>Wednesday, February 6, 2002 10:10:00 AM EST</created><mstation>localhost</mstation></header>"
                + "<path name=\"Email-Admin\">"
                + "<target><name>Admin</name><command>javaEmail</command></target>"
                + "</path>"
                + "</destinationPaths>", Charset.defaultCharset());

        FileUtils.writeStringToFile(new File(etc, "notificationCommands.xml"), "<?xml version=\"1.0\"?>"
                + "<notification-commands xmlns=\"http://xmlns.opennms.org/xsd/notificationCommands\">"
                + "<header><ver>.9</ver><created>Wednesday, February 6, 2002 10:10:00 AM EST</created><mstation>localhost</mstation></header>"
                + "<command binary=\"false\">"
                + "<name>javaEmail</name>"
                + "<execute>org.opennms.netmgt.notifd.JavaMailNotificationStrategy</execute>"
                + "<comment>send an email</comment>"
                + "</command>"
                + "</notification-commands>", Charset.defaultCharset());

        FileUtils.writeStringToFile(new File(etc, "groups.xml"), "<?xml version=\"1.0\"?>"
                + "<groupinfo xmlns=\"http://xmlns.opennms.org/xsd/groups\">"
                + "<header><rev>1.3</rev><created>Wednesday, February 6, 2002 10:10:00 AM EST</created><mstation>localhost</mstation></header>"
                + "<groups><group><name>Admin</name><user>admin</user></group></groups>"
                + "<roles><role name=\"junit-oncall\" supervisor=\"admin\" membership-group=\"Admin\"/></roles>"
                + "</groupinfo>", Charset.defaultCharset());

    }

    // Required so context initialization can't repoint opennms.home at
    // opennms-base-assembly between servlet start and the first request.
    @Override
    public void afterServletStart() throws Exception {
        System.setProperty("opennms.home", m_onmsHome);
        ConfigurationTestUtils.setRelativeHomeDirectory(m_onmsHome);
    }

    // Restore opennms.home for later suites sharing this JVM fork.
    // ConfigurationTestUtils.setRelativeHomeDirectory holds no state of its own —
    // it only writes opennms.home (via setAbsoluteHomeDirectory) — so resetting
    // the property here fully undoes the setRelativeHomeDirectory calls too.
    @org.junit.AfterClass
    public static void restoreOpennmsHome() {
        if (s_previousOpennmsHome != null) {
            if (s_previousOpennmsHome.isEmpty()) {
                System.clearProperty("opennms.home");
            } else {
                System.setProperty("opennms.home", s_previousOpennmsHome);
            }
        }
    }

    @Test
    public void testNotifdStatus() throws Exception {
        JSONObject status = new JSONObject(getJson("/notification-config/status"));
        Assert.assertEquals("off", status.getString("status"));

        sendData(PUT, MediaType.APPLICATION_JSON, "/notification-config/status", "{\"status\":\"on\"}", 204);
        status = new JSONObject(getJson("/notification-config/status"));
        Assert.assertEquals("on", status.getString("status"));

        // only on/off are valid
        sendData(PUT, MediaType.APPLICATION_JSON, "/notification-config/status", "{\"status\":\"maybe\"}", 400);
    }

    @Test
    public void testDestinationPathsReadable() throws Exception {
        // the read side ships in the base PR: the event-notification editor
        // needs the path list for its destination picker
        // no assumptions about what other suites left in the shared config
        JSONObject list = new JSONObject(getJson("/notification-config/destination-paths"));
        Assert.assertTrue(list.getJSONArray("path").length() >= 1);
        final String firstName = list.getJSONArray("path").getJSONObject(0).getString("name");
        JSONObject path = new JSONObject(getJson("/notification-config/destination-paths/"
                + java.net.URLEncoder.encode(firstName, java.nio.charset.StandardCharsets.UTF_8)));
        Assert.assertEquals(firstName, path.getString("name"));
    }

    @Test
    public void testForbiddenForNonAdmin() throws Exception {
        setUser("nobody", new String[]{ "ROLE_USER" });
        try {
            sendRequest(GET, "/notification-config/status", 403);
            sendData(PUT, MediaType.APPLICATION_JSON, "/notification-config/status", "{\"status\":\"on\"}", 403);
        } finally {
            setUser("admin", new String[]{ "ROLE_ADMIN" });
        }
    }

    private String getJson(final String url) throws Exception {
        // the instance createRequest carries the setUser() user AND roles
        final MockHttpServletRequest request = createRequest(GET, url);
        request.addHeader("Accept", MediaType.APPLICATION_JSON);
        return sendRequest(request, 200);
    }
}
