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
package org.opennms.web.rest.v2;


import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.TreeMap;
import java.util.SortedMap;
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
import org.opennms.netmgt.config.GroupFactory;
import org.opennms.netmgt.filter.FilterDaoFactory;
import org.opennms.netmgt.filter.api.FilterDao;
import org.opennms.netmgt.filter.api.FilterParseException;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.netmgt.dao.api.MonitoringLocationDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SessionUtils;
import org.opennms.netmgt.model.OnmsNode;
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

    public NotificationConfigRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    private static final String INVALID_RULE = "this is not a rule";

    private String m_onmsHome;

    private FilterDao m_filterDao;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private MonitoringLocationDao m_locationDao;

    @Autowired
    private SessionUtils m_sessionUtils;

    // the node the mocked filter matches; seeded via Hibernate so the service's
    // PathOutageDao/NodeDao (which read through Hibernate) can see it
    private int m_pathNodeId;

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

        // target-reference validation resolves user names via UserFactory
        FileUtils.writeStringToFile(new File(etc, "users.xml"), "<?xml version=\"1.0\"?>"
                + "<userinfo xmlns=\"http://xmlns.opennms.org/xsd/users\">"
                + "<header><rev>.9</rev><created>Wednesday, February 6, 2002 10:10:00 AM EST</created><mstation>localhost</mstation></header>"
                + "<users><user><user-id>admin</user-id><full-name>Administrator</full-name></user></users>"
                + "</userinfo>", Charset.defaultCharset());


        m_filterDao = mock(FilterDao.class);
        // the *.*.*.* -> node stub is added in afterServletStart, once the node
        // has been seeded through Hibernate and its generated id is known
        // a rule that matches nothing, for the zero-match apply case
        when(m_filterDao.getNodeMap("IPADDR IPLIKE 1.1.1.1")).thenReturn(new TreeMap<>());
        doThrow(new FilterParseException("invalid rule")).when(m_filterDao).validateRule(INVALID_RULE);
        // getNodeMap now parses the rule itself (the redundant validateRule was dropped)
        doThrow(new FilterParseException("invalid rule")).when(m_filterDao).getNodeMap(INVALID_RULE);
        FilterDaoFactory.setInstance(m_filterDao);
    }

    @org.junit.After
    public void restoreFilterDao() {
        // the mock was pushed into the static factory in setUp; don't leak it
        // into other ITs sharing this JVM
        FilterDaoFactory.setInstance(null);
    }

    // Required so context initialization can't repoint opennms.home at
    // opennms-base-assembly between servlet start and the first request.
    @Override
    public void afterServletStart() throws Exception {
        System.setProperty("opennms.home", m_onmsHome);
        ConfigurationTestUtils.setRelativeHomeDirectory(m_onmsHome);
        // Webapp beans may have initialized GroupFactory while context startup
        // had opennms.home pointed elsewhere; pin a factory built against the
        // test home so the seeded roles are visible.
        GroupFactory.setInstance(new GroupFactory());
        // Seed the node the mocked filter matches through Hibernate (a committed
        // transaction), so the service's PathOutageDao/NodeDao — which read via
        // Hibernate, not DataSourceFactory — can see it for the foreign key.
        m_pathNodeId = m_sessionUtils.withTransaction(() -> {
            final OnmsNode node = new OnmsNode(m_locationDao.getDefaultLocation(), "node1");
            m_nodeDao.save(node);
            return node.getId();
        });
        final SortedMap<Integer, String> nodeMap = new TreeMap<>();
        nodeMap.put(m_pathNodeId, "node1");
        when(m_filterDao.getNodeMap("IPADDR IPLIKE *.*.*.*")).thenReturn(nodeMap);
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
    public void testEventNotificationLifecycle() throws Exception {
        JSONObject list = new JSONObject(getJson("/notification-config/event-notifications"));
        Assert.assertEquals(1, list.getJSONArray("notification").length());

        final String newNotification = "{\"name\":\"junit-added\",\"status\":\"off\","
                + "\"uei\":\"uei.opennms.org/nodes/nodeUp\","
                + "\"rule\":{\"value\":\"IPADDR IPLIKE *.*.*.*\"},"
                + "\"destinationPath\":\"Email-Admin\","
                + "\"text-message\":\"node up\"}";
        sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/event-notifications", newNotification, 204);

        JSONObject added = new JSONObject(getJson("/notification-config/event-notifications/junit-added"));
        Assert.assertEquals("uei.opennms.org/nodes/nodeUp", added.getString("uei"));

        // duplicate name is rejected
        sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/event-notifications", newNotification, 400);

        sendData(PUT, MediaType.APPLICATION_JSON, "/notification-config/event-notifications/junit-added/status", "{\"status\":\"on\"}", 204);
        added = new JSONObject(getJson("/notification-config/event-notifications/junit-added"));
        Assert.assertEquals("on", added.getString("status"));

        sendRequest(DELETE, "/notification-config/event-notifications/junit-added", 204);
        sendRequest(GET, "/notification-config/event-notifications/junit-added", 404);
    }

    @Test
    public void testEventNotificationUpdateCarriesEventSeverity() throws Exception {
        // replaceNotification copies field-by-field; a missing setEventSeverity made
        // severity edits a silent no-op on PUT while POST persisted them.
        final String withSeverity = "{\"name\":\"junit-severity\",\"status\":\"off\","
                + "\"uei\":\"uei.opennms.org/nodes/nodeUp\","
                + "\"rule\":{\"value\":\"IPADDR IPLIKE *.*.*.*\"},"
                + "\"destinationPath\":\"Email-Admin\","
                + "\"text-message\":\"node up\","
                + "\"event-severity\":\"Minor\"}";
        sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/event-notifications", withSeverity, 204);

        JSONObject added = new JSONObject(getJson("/notification-config/event-notifications/junit-severity"));
        Assert.assertEquals("Minor", added.getString("event-severity"));

        final String changedSeverity = withSeverity.replace("\"Minor\"", "\"Major\"");
        sendData(PUT, MediaType.APPLICATION_JSON, "/notification-config/event-notifications/junit-severity", changedSeverity, 204);
        JSONObject updated = new JSONObject(getJson("/notification-config/event-notifications/junit-severity"));
        Assert.assertEquals("Major", updated.getString("event-severity"));

        // clearing severity on update must also stick
        final String noSeverity = withSeverity.replace(",\"event-severity\":\"Minor\"", "");
        sendData(PUT, MediaType.APPLICATION_JSON, "/notification-config/event-notifications/junit-severity", noSeverity, 204);
        updated = new JSONObject(getJson("/notification-config/event-notifications/junit-severity"));
        Assert.assertFalse(updated.has("event-severity"));

        sendRequest(DELETE, "/notification-config/event-notifications/junit-severity", 204);
    }

    @Test
    public void testEventNotificationUnknownReferencesRejected() throws Exception {
        // destinationPath must exist; otherwise delivery fails silently
        final String badPath = "{\"name\":\"junit-bad-path\",\"status\":\"off\","
                + "\"uei\":\"uei.opennms.org/nodes/nodeUp\","
                + "\"rule\":{\"value\":\"IPADDR IPLIKE *.*.*.*\"},"
                + "\"destinationPath\":\"no-such-path\","
                + "\"text-message\":\"x\"}";
        sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/event-notifications", badPath, 400);

        // severity outside the canonical names can never match an event
        final String badSeverity = "{\"name\":\"junit-bad-sev\",\"status\":\"off\","
                + "\"uei\":\"uei.opennms.org/nodes/nodeUp\","
                + "\"rule\":{\"value\":\"IPADDR IPLIKE *.*.*.*\"},"
                + "\"destinationPath\":\"Email-Admin\","
                + "\"text-message\":\"x\","
                + "\"event-severity\":\"Bananas\"}";
        sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/event-notifications", badSeverity, 400);
    }

    @Test
    public void testDestinationPathUnknownReferencesRejected() throws Exception {
        // a target that is not a user, group, role, or email address
        final String ghostTarget = "{\"name\":\"junit-ghost\","
                + "\"target\":[{\"name\":\"ghost\",\"command\":[\"javaEmail\"]}],\"escalate\":[]}";
        sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/destination-paths", ghostTarget, 400);

        // a command that is not defined in notificationCommands.xml
        final String ghostCommand = "{\"name\":\"junit-ghost-cmd\","
                + "\"target\":[{\"name\":\"Admin\",\"command\":[\"carrierPigeon\"]}],\"escalate\":[]}";
        sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/destination-paths", ghostCommand, 400);

        // known user, on-call role, and email targets all pass
        final String validTargets = "{\"name\":\"junit-valid-targets\","
                + "\"target\":[{\"name\":\"admin\",\"command\":[\"javaEmail\"]},"
                + "{\"name\":\"junit-oncall\",\"command\":[\"javaEmail\"]},"
                + "{\"name\":\"noc@example.com\",\"command\":[\"javaEmail\"]}],\"escalate\":[]}";
        sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/destination-paths", validTargets, 204);
        sendRequest(DELETE, "/notification-config/destination-paths/junit-valid-targets", 204);
    }

    @Test
    public void testInvalidDelaysRejected() throws Exception {
        // TimeConverter.convertToMillis parses these after the notice row is
        // inserted, so a bad value poisons eventd dispatch; reject up front.
        final String badInitial = "{\"name\":\"junit-delay\",\"initial-delay\":\"5min\","
                + "\"target\":[{\"name\":\"Admin\",\"command\":[\"javaEmail\"]}],\"escalate\":[]}";
        sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/destination-paths", badInitial, 400);

        final String badEscalation = "{\"name\":\"junit-delay\","
                + "\"target\":[{\"name\":\"Admin\",\"command\":[\"javaEmail\"]}],"
                + "\"escalate\":[{\"delay\":\"1h30m\",\"target\":[{\"name\":\"Admin\",\"command\":[\"javaEmail\"]}]}]}";
        sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/destination-paths", badEscalation, 400);

        final String badInterval = "{\"name\":\"junit-delay\","
                + "\"target\":[{\"name\":\"Admin\",\"command\":[\"javaEmail\"],\"interval\":\"soon\"}],\"escalate\":[]}";
        sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/destination-paths", badInterval, 400);

        // convertToMillis alone would take these (Float.parseFloat allows
        // signs, NaN and Infinity), so the grammar is enforced by regex too
        for (final String sneaky : new String[]{ "-5s", "+5s", "NaN", "Infinity", "1e3s" }) {
            final String body = "{\"name\":\"junit-delay\",\"initial-delay\":\"" + sneaky + "\","
                    + "\"target\":[{\"name\":\"Admin\",\"command\":[\"javaEmail\"]}],\"escalate\":[]}";
            sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/destination-paths", body, 400);
        }

        // the full accepted grammar passes
        final String good = "{\"name\":\"junit-delay\",\"initial-delay\":\"30s\","
                + "\"target\":[{\"name\":\"Admin\",\"command\":[\"javaEmail\"],\"interval\":\"15m\"}],"
                + "\"escalate\":[{\"delay\":\"1h\",\"target\":[{\"name\":\"Admin\",\"command\":[\"javaEmail\"]}]}]}";
        sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/destination-paths", good, 204);
        sendRequest(DELETE, "/notification-config/destination-paths/junit-delay", 204);
    }

    @Test
    public void testDeleteReferencedDestinationPathRejected() throws Exception {
        // junitNotification references Email-Admin; delete must refuse (409)
        // rather than leave the notification pointing at a missing path
        final String spare = "{\"name\":\"junit-spare\","
                + "\"target\":[{\"name\":\"Admin\",\"command\":[\"javaEmail\"]}],\"escalate\":[]}";
        sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/destination-paths", spare, 204);

        sendRequest(DELETE, "/notification-config/destination-paths/Email-Admin", 409);
        // the unreferenced one deletes fine
        sendRequest(DELETE, "/notification-config/destination-paths/junit-spare", 204);
    }

    @Test
    public void testEventNotificationValidation() throws Exception {
        // missing text-message must be rejected before it can poison the config
        sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/event-notifications",
                "{\"name\":\"junit-invalid\",\"status\":\"on\",\"uei\":\"uei.opennms.org/nodes/nodeUp\","
                        + "\"rule\":{\"value\":\"IPADDR IPLIKE *.*.*.*\"},\"destinationPath\":\"Email-Admin\"}", 400);
        sendRequest(GET, "/notification-config/event-notifications/junit-invalid", 404);
    }

    @Test
    public void testDeleteLastEventNotificationRejected() throws Exception {
        // reduce to a single notification, deterministically
        final JSONObject list = new JSONObject(getJson("/notification-config/event-notifications"));
        final org.json.JSONArray items = list.getJSONArray("notification");
        for (int i = 0; i < items.length(); i++) {
            final String name = items.getJSONObject(i).getString("name");
            if (!"junitNotification".equals(name)) {
                sendRequest(DELETE, "/notification-config/event-notifications/"
                        + java.net.URLEncoder.encode(name, java.nio.charset.StandardCharsets.UTF_8), 204);
            }
        }
        // notifications.xsd requires at least one notification element
        sendRequest(DELETE, "/notification-config/event-notifications/junitNotification", 400);
        Assert.assertEquals(1, new JSONObject(getJson("/notification-config/event-notifications"))
                .getJSONArray("notification").length());
    }

    @Test
    public void testEventNotificationMissingStatusRejected() throws Exception {
        // a missing status would die inside the factory's in-place setter
        // chain after several fields were already written to the live object
        sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/event-notifications",
                "{\"name\":\"junit-nostatus\",\"uei\":\"uei.opennms.org/nodes/nodeUp\","
                        + "\"rule\":{\"value\":\"IPADDR IPLIKE *.*.*.*\"},\"destinationPath\":\"Email-Admin\",\"text-message\":\"x\"}", 400);
        sendRequest(GET, "/notification-config/event-notifications/junit-nostatus", 404);

        sendData(PUT, MediaType.APPLICATION_JSON, "/notification-config/event-notifications/junitNotification",
                "{\"name\":\"junitNotification\",\"uei\":\"uei.opennms.org/nodes/nodeDown\","
                        + "\"rule\":{\"value\":\"IPADDR != '0.0.0.0'\"},\"destinationPath\":\"Email-Admin\",\"text-message\":\"node down\"}", 400);
        final JSONObject unchanged = new JSONObject(getJson("/notification-config/event-notifications/junitNotification"));
        Assert.assertEquals("on", unchanged.getString("status"));
    }

    @Test
    public void testEventNotificationRenameCollisionRejected() throws Exception {
        final String other = "{\"name\":\"junit-other\",\"status\":\"off\",\"uei\":\"uei.opennms.org/nodes/nodeUp\","
                + "\"rule\":{\"value\":\"IPADDR IPLIKE *.*.*.*\"},\"destinationPath\":\"Email-Admin\",\"text-message\":\"x\"}";
        sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/event-notifications", other, 204);

        // renaming junitNotification onto the existing junit-other must not create a duplicate name
        final String renamed = "{\"name\":\"junit-other\",\"status\":\"on\",\"uei\":\"uei.opennms.org/nodes/nodeDown\","
                + "\"rule\":{\"value\":\"IPADDR != '0.0.0.0'\"},\"destinationPath\":\"Email-Admin\",\"text-message\":\"node down\"}";
        sendData(PUT, MediaType.APPLICATION_JSON, "/notification-config/event-notifications/junitNotification", renamed, 400);
        sendRequest(GET, "/notification-config/event-notifications/junitNotification", 200);
    }

    @Test
    public void testDestinationPathLifecycle() throws Exception {
        JSONObject list = new JSONObject(getJson("/notification-config/destination-paths"));
        Assert.assertEquals(1, list.getJSONArray("path").length());

        final String newPath = "{\"name\":\"junit-path\",\"initial-delay\":\"30s\","
                + "\"target\":[{\"name\":\"noc@example.com\",\"command\":[\"javaEmail\"]}],"
                + "\"escalate\":[{\"delay\":\"15m\",\"target\":[{\"name\":\"Admin\",\"command\":[\"javaEmail\"]}]}]}";
        sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/destination-paths", newPath, 204);

        JSONObject added = new JSONObject(getJson("/notification-config/destination-paths/junit-path"));
        Assert.assertEquals("30s", added.getString("initial-delay"));
        Assert.assertEquals(1, added.getJSONArray("escalate").length());

        final String updatedPath = "{\"name\":\"junit-path\",\"initial-delay\":\"1m\","
                + "\"target\":[{\"name\":\"Admin\",\"command\":[\"javaEmail\"]}],\"escalate\":[]}";
        sendData(PUT, MediaType.APPLICATION_JSON, "/notification-config/destination-paths/junit-path", updatedPath, 204);
        added = new JSONObject(getJson("/notification-config/destination-paths/junit-path"));
        Assert.assertEquals("1m", added.getString("initial-delay"));

        sendRequest(DELETE, "/notification-config/destination-paths/junit-path", 204);
        sendRequest(GET, "/notification-config/destination-paths/junit-path", 404);
    }

    @Test
    public void testDeleteLastDestinationPathRejected() throws Exception {
        // Whittle down to a single path, then confirm the last one is rejected
        // (destinationPaths.xsd requires at least one) rather than 500ing.
        final JSONArray paths = new JSONObject(getJson("/notification-config/destination-paths")).getJSONArray("path");
        for (int i = 1; i < paths.length(); i++) {
            sendRequest(DELETE, "/notification-config/destination-paths/"
                    + java.net.URLEncoder.encode(paths.getJSONObject(i).getString("name"), java.nio.charset.StandardCharsets.UTF_8), 204);
        }
        final String last = paths.getJSONObject(0).getString("name");
        sendRequest(DELETE, "/notification-config/destination-paths/"
                + java.net.URLEncoder.encode(last, java.nio.charset.StandardCharsets.UTF_8), 400);
    }

    @Test
    public void testCommands() throws Exception {
        JSONArray commands = new JSONArray(getJson("/notification-config/commands"));
        Assert.assertEquals(1, commands.length());
        Assert.assertEquals("javaEmail", commands.getJSONObject(0).getString("name"));
    }

    @Test
    public void testDestinationPathInvalidUpdateKeepsExistingPath() throws Exception {
        // a target with no commands would fail the schema-validated save AFTER
        // the factory already removed the old entry — must be rejected up front
        sendData(PUT, MediaType.APPLICATION_JSON, "/notification-config/destination-paths/Email-Admin",
                "{\"name\":\"Email-Admin\",\"target\":[{\"name\":\"Admin\",\"command\":[]}]}", 400);
        sendData(PUT, MediaType.APPLICATION_JSON, "/notification-config/destination-paths/Email-Admin",
                "{\"name\":\"Email-Admin\",\"target\":[]}", 400);
        JSONObject unchanged = new JSONObject(getJson("/notification-config/destination-paths/Email-Admin"));
        Assert.assertEquals(1, unchanged.getJSONArray("target").length());
    }

    @Test
    public void testDestinationPathRenameCollisionRejected() throws Exception {
        final String other = "{\"name\":\"junit-p2\",\"target\":[{\"name\":\"Admin\",\"command\":[\"javaEmail\"]}]}";
        sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/destination-paths", other, 204);

        sendData(PUT, MediaType.APPLICATION_JSON, "/notification-config/destination-paths/Email-Admin",
                "{\"name\":\"junit-p2\",\"target\":[{\"name\":\"Admin\",\"command\":[\"javaEmail\"]}]}", 400);
        // both paths still intact
        sendRequest(GET, "/notification-config/destination-paths/Email-Admin", 200);
        sendRequest(GET, "/notification-config/destination-paths/junit-p2", 200);
    }

    @Test
    public void testDestinationPathRenameUpdatesReferences() throws Exception {
        sendData(PUT, MediaType.APPLICATION_JSON, "/notification-config/destination-paths/Email-Admin",
                "{\"name\":\"Email-Ops\",\"target\":[{\"name\":\"Admin\",\"command\":[\"javaEmail\"]}]}", 204);
        sendRequest(GET, "/notification-config/destination-paths/Email-Admin", 404);
        sendRequest(GET, "/notification-config/destination-paths/Email-Ops", 200);
        // the notification that referenced the old name follows the rename
        org.opennms.netmgt.config.NotificationFactory.init();
        Assert.assertEquals("Email-Ops", org.opennms.netmgt.config.NotificationFactory.getInstance()
                .getNotification("junitNotification").getDestinationPath());
    }

    @Test
    public void testOnCallRoles() throws Exception {
        JSONArray roles = new JSONArray(getJson("/notification-config/on-call-roles"));
        Assert.assertEquals(1, roles.length());
        Assert.assertEquals("junit-oncall", roles.getString(0));
    }

    @Test
    public void testOnCallRolesFollowFileChanges() throws Exception {
        Assert.assertEquals(1, new JSONArray(getJson("/notification-config/on-call-roles")).length());
        // removing the last role from groups.xml must not leave it cached
        final File groupsFile = new File("target/test-work-dir/etc/groups.xml");
        FileUtils.writeStringToFile(groupsFile, "<?xml version=\"1.0\"?>"
                + "<groupinfo xmlns=\"http://xmlns.opennms.org/xsd/groups\">"
                + "<header><rev>1.3</rev><created>Wednesday, February 6, 2002 10:10:00 AM EST</created><mstation>localhost</mstation></header>"
                + "<groups><group><name>Admin</name><user>admin</user></group></groups>"
                + "</groupinfo>", Charset.defaultCharset());
        groupsFile.setLastModified(System.currentTimeMillis() + 1000);
        Assert.assertEquals(0, new JSONArray(getJson("/notification-config/on-call-roles")).length());
    }

    @Test
    public void testDestinationPathTargetsFollowRoleFileChanges() throws Exception {
        // a role added behind the manager's back (hand edit, or the on-call
        // roles API) must be a valid target without an unrelated read first
        final File groupsFile = new File("target/test-work-dir/etc/groups.xml");
        FileUtils.writeStringToFile(groupsFile, "<?xml version=\"1.0\"?>"
                + "<groupinfo xmlns=\"http://xmlns.opennms.org/xsd/groups\">"
                + "<header><rev>1.3</rev><created>Wednesday, February 6, 2002 10:10:00 AM EST</created><mstation>localhost</mstation></header>"
                + "<groups><group><name>Admin</name><user>admin</user></group></groups>"
                + "<roles><role name=\"junit-oncall\" supervisor=\"admin\" membership-group=\"Admin\"/>"
                + "<role name=\"junit-fresh-role\" supervisor=\"admin\" membership-group=\"Admin\"/></roles>"
                + "</groupinfo>", Charset.defaultCharset());
        groupsFile.setLastModified(System.currentTimeMillis() + 1000);
        sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/destination-paths",
                "{\"name\":\"junit-fresh-target\",\"target\":[{\"name\":\"junit-fresh-role\",\"command\":[\"javaEmail\"]}],\"escalate\":[]}", 204);
        sendRequest(DELETE, "/notification-config/destination-paths/junit-fresh-target", 204);
    }

    @Test
    public void testUnaddressableNamesRejected() throws Exception {
        // '/', '\\', '%' and control characters cannot be addressed as the
        // {name} path segment of PUT/DELETE, so such an entry could never be
        // edited or deleted again
        for (final String bad : new String[] {"junit/slash", "junit\\\\back", "junit%pct", "junit\\u0001ctl"}) {
            sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/event-notifications",
                    "{\"name\":\"" + bad + "\",\"status\":\"off\",\"uei\":\"uei.opennms.org/nodes/nodeUp\","
                    + "\"rule\":{\"value\":\"IPADDR IPLIKE *.*.*.*\"},\"destinationPath\":\"Email-Admin\",\"text-message\":\"x\"}", 400);
            sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/destination-paths",
                    "{\"name\":\"" + bad + "\",\"target\":[{\"name\":\"admin\",\"command\":[\"javaEmail\"]}],\"escalate\":[]}", 400);
        }
        // a rename to such a name is rejected the same way, and the entry keeps its name
        sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/destination-paths",
                "{\"name\":\"junit-renamable\",\"target\":[{\"name\":\"admin\",\"command\":[\"javaEmail\"]}],\"escalate\":[]}", 204);
        sendData(PUT, MediaType.APPLICATION_JSON, "/notification-config/destination-paths/junit-renamable",
                "{\"name\":\"junit/renamed\",\"target\":[{\"name\":\"admin\",\"command\":[\"javaEmail\"]}],\"escalate\":[]}", 400);
        sendRequest(GET, "/notification-config/destination-paths/junit-renamable", 200);
        sendRequest(DELETE, "/notification-config/destination-paths/junit-renamable", 204);
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
    public void testPathOutageLifecycle() throws Exception {
        JSONArray outages = new JSONArray(getJson("/notification-config/path-outages"));
        Assert.assertEquals(0, outages.length());

        JSONObject preview = new JSONObject(getJson("/notification-config/path-outages/preview?rule=IPADDR%20IPLIKE%20*.*.*.*"));
        Assert.assertEquals(1, preview.getInt("totalCount"));
        Assert.assertEquals("node1", preview.getJSONArray("nodes").getJSONObject(0).getString("nodeLabel"));

        sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/path-outages",
                "{\"rule\":\"IPADDR IPLIKE *.*.*.*\",\"criticalIp\":\"192.168.1.1\",\"criticalSvc\":\"ICMP\"}", 204);
        outages = new JSONArray(getJson("/notification-config/path-outages"));
        Assert.assertEquals(1, outages.length());
        Assert.assertEquals("192.168.1.1", outages.getJSONObject(0).getString("criticalPathIp"));

        sendRequest(DELETE, "/notification-config/path-outages/" + m_pathNodeId, 204);
        outages = new JSONArray(getJson("/notification-config/path-outages"));
        Assert.assertEquals(0, outages.length());
    }

    @Test
    public void testPathOutageClearsWithBlankIp() throws Exception {
        sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/path-outages",
                "{\"rule\":\"IPADDR IPLIKE *.*.*.*\",\"criticalIp\":\"192.168.1.1\"}", 204);
        Assert.assertEquals(1, new JSONArray(getJson("/notification-config/path-outages")).length());

        // blank critical IP clears the path for the matching nodes
        sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/path-outages",
                "{\"rule\":\"IPADDR IPLIKE *.*.*.*\"}", 204);
        Assert.assertEquals(0, new JSONArray(getJson("/notification-config/path-outages")).length());
    }

    @Test
    public void testPathOutageValidation() throws Exception {
        sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/path-outages",
                "{\"rule\":\"" + INVALID_RULE + "\",\"criticalIp\":\"192.168.1.1\"}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/path-outages", "{}", 400);
        sendRequest(GET, "/notification-config/path-outages/preview", 400);
        // a rule matching no nodes is rejected rather than silently succeeding
        sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/path-outages",
                "{\"rule\":\"IPADDR IPLIKE 1.1.1.1\",\"criticalIp\":\"192.168.1.1\"}", 400);
        // only ICMP is supported as the critical path service
        sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/path-outages",
                "{\"rule\":\"IPADDR IPLIKE *.*.*.*\",\"criticalIp\":\"192.168.1.1\",\"criticalSvc\":\"HTTP\"}", 400);
        // deleting a critical path that isn't configured is a 404, not a silent 204
        sendRequest(DELETE, "/notification-config/path-outages/999", 404);
    }

    @Test
    public void testForbiddenForNonAdmin() throws Exception {
        setUser("nobody", new String[]{ "ROLE_USER" });
        try {
            sendRequest(GET, "/notification-config/status", 403);
            sendRequest(GET, "/notification-config/event-notifications", 403);
            sendRequest(GET, "/notification-config/services", 403);
            sendData(PUT, MediaType.APPLICATION_JSON, "/notification-config/status", "{\"status\":\"on\"}", 403);
            sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/rule/validate",
                    "{\"rule\":\"IPADDR IPLIKE *.*.*.*\"}", 403);
            sendData(POST, MediaType.APPLICATION_JSON, "/notification-config/path-outages",
                    "{\"rule\":\"IPADDR IPLIKE *.*.*.*\"}", 403);
            sendRequest(DELETE, "/notification-config/event-notifications/junitNotification", 403);
        } finally {
            setUser("admin", new String[]{ "ROLE_ADMIN" });
        }
    }

    @Test
    public void testGetServiceNames() throws Exception {
        // Admin can read the service list; the value depends on the fixture, so
        // assert only that the endpoint returns a well-formed JSON array.
        final JSONArray services = new JSONArray(getJson("/notification-config/services"));
        Assert.assertNotNull(services);
    }

    @Test
    public void testValidateRule() throws Exception {
        // Valid rule, default (no preview): valid true and the expensive match
        // map is not built.
        final JSONObject validated = new JSONObject(sendData(POST, MediaType.APPLICATION_JSON,
                "/notification-config/rule/validate", "{\"rule\":\"IPADDR IPLIKE *.*.*.*\"}", 200)
                .getContentAsString());
        Assert.assertTrue(validated.getBoolean("valid"));
        Assert.assertEquals(0, validated.optInt("matchCount", 0));

        // Same rule with preview requested: still valid, and the match count is
        // now populated (>= 0).
        final JSONObject previewed = new JSONObject(sendData(POST, MediaType.APPLICATION_JSON,
                "/notification-config/rule/validate", "{\"rule\":\"IPADDR IPLIKE *.*.*.*\",\"preview\":true}", 200)
                .getContentAsString());
        Assert.assertTrue(previewed.getBoolean("valid"));
        Assert.assertTrue(previewed.getInt("matchCount") >= 0);

        // A blank rule is rejected as invalid input, not an error.
        final JSONObject blank = new JSONObject(sendData(POST, MediaType.APPLICATION_JSON,
                "/notification-config/rule/validate", "{\"rule\":\"\"}", 200)
                .getContentAsString());
        Assert.assertFalse(blank.getBoolean("valid"));
    }

    private String getJson(final String url) throws Exception {
        // the instance createRequest carries the setUser() user AND roles
        final MockHttpServletRequest request = createRequest(GET, url);
        request.addHeader("Accept", MediaType.APPLICATION_JSON);
        return sendRequest(request, 200);
    }
}
