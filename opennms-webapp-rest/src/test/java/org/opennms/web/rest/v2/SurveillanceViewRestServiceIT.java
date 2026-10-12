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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Drives the {@code /api/v2/surveillance/views} resource end to end through
 * the CXF/Jersey test harness: the CRUD lifecycle and its validation/error
 * semantics, the in-resource ROLE_ADMIN write guard, per-user default-view
 * resolution, and the computed status/drill-down endpoints against a
 * populated database.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(locations = {
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
        "classpath:/META-INF/opennms/applicationContext-postgresJsonStore.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase
public class SurveillanceViewRestServiceIT extends AbstractSpringJerseyRestTestCase {

    private static final String BASE = "/surveillance/views";

    private final ObjectMapper m_mapper = new ObjectMapper();

    @Autowired
    private DatabasePopulator m_databasePopulator;

    public SurveillanceViewRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "WARN");
        setUser("admin", new String[] { "ROLE_ADMIN" });
    }

    private static String viewBody(final String name) {
        return "{\"name\":\"" + name + "\",\"refreshSeconds\":120,"
                + "\"rows\":[{\"label\":\"Routers\",\"categories\":[\"Routers\"]}],"
                + "\"columns\":[{\"label\":\"DEV\",\"categories\":[\"DEV_AC\"]}]}";
    }

    private String create(final String name) throws Exception {
        final MockHttpServletResponse post = sendData(POST, MediaType.APPLICATION_JSON, BASE, viewBody(name), 201);
        return lastPathSegment(post.getHeader("Location"));
    }

    @Test
    @JUnitTemporaryDatabase
    public void canCreateReadUpdateDeleteView() throws Exception {
        // The DAO's init() seeds the stock "default" view into the empty store
        // at context start.
        final JsonNode initial = m_mapper.readTree(sendRequest(GET, BASE, 200));
        assertEquals(1, initial.size());
        assertEquals("default", initial.get(0).get("name").asText());
        assertEquals(true, initial.get(0).get("isDefault").asBoolean());
        assertEquals("system", initial.get(0).get("owner").asText());

        final String id = create("Ops");
        assertNotNull("POST should return a Location with an id", id);

        final JsonNode view = m_mapper.readTree(sendRequest(GET, BASE + "/" + id, 200));
        assertEquals("Ops", view.get("name").asText());
        assertEquals(120, view.get("refreshSeconds").asInt());
        assertEquals("admin", view.get("owner").asText());
        assertEquals(false, view.get("isDefault").asBoolean());
        assertEquals(1, view.get("rows").size());
        assertEquals("Routers", view.get("rows").get(0).get("label").asText());
        assertEquals("Routers", view.get("rows").get(0).get("categories").get(0).asText());
        assertEquals(1, view.get("columns").size());
        assertTrue(view.get("lastModified").isNull());

        // Full-replace update: rename, retune, reshape
        final String update = "{\"name\":\"Ops (edited)\",\"refreshSeconds\":60,"
                + "\"rows\":[{\"label\":\"Servers\",\"categories\":[\"Servers\"]}],"
                + "\"columns\":[{\"label\":\"DEV\",\"categories\":[\"DEV_AC\"]},{\"label\":\"PROD\",\"categories\":[\"Production\"]}]}";
        sendData(PUT, MediaType.APPLICATION_JSON, BASE + "/" + id, update, 204);

        final JsonNode updated = m_mapper.readTree(sendRequest(GET, BASE + "/" + id, 200));
        assertEquals("Ops (edited)", updated.get("name").asText());
        assertEquals(60, updated.get("refreshSeconds").asInt());
        assertEquals("Servers", updated.get("rows").get(0).get("label").asText());
        assertEquals(2, updated.get("columns").size());
        assertEquals("admin", updated.get("owner").asText());
        assertTrue("lastModified should be set after an update", !updated.get("lastModified").isNull());

        sendRequest(DELETE, BASE + "/" + id, 204);
        sendRequest(GET, BASE + "/" + id, 404);
        // ...leaving just the seeded "default" view
        assertEquals(1, m_mapper.readTree(sendRequest(GET, BASE, 200)).size());
    }

    @Test
    @JUnitTemporaryDatabase
    public void validatesTheViewShape() throws Exception {
        // name, at least one row and one column, labels and categories are all required
        sendData(POST, MediaType.APPLICATION_JSON, BASE,
                "{\"rows\":[{\"label\":\"r\",\"categories\":[\"c\"]}],\"columns\":[{\"label\":\"c\",\"categories\":[\"c\"]}]}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, BASE,
                "{\"name\":\"x\",\"columns\":[{\"label\":\"c\",\"categories\":[\"c\"]}]}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, BASE,
                "{\"name\":\"x\",\"rows\":[{\"label\":\"r\",\"categories\":[]}],\"columns\":[{\"label\":\"c\",\"categories\":[\"c\"]}]}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, BASE,
                "{\"name\":\"x\",\"refreshSeconds\":0,\"rows\":[{\"label\":\"r\",\"categories\":[\"c\"]}],\"columns\":[{\"label\":\"c\",\"categories\":[\"c\"]}]}", 400);
        // duplicate row labels within a view
        sendData(POST, MediaType.APPLICATION_JSON, BASE,
                "{\"name\":\"x\",\"rows\":[{\"label\":\"r\",\"categories\":[\"c\"]},{\"label\":\"r\",\"categories\":[\"c\"]}],\"columns\":[{\"label\":\"c\",\"categories\":[\"c\"]}]}", 400);
    }

    @Test
    @JUnitTemporaryDatabase
    public void rejectsDuplicateNames() throws Exception {
        create("dup");
        sendData(POST, MediaType.APPLICATION_JSON, BASE, viewBody("dup"), 409);

        // Renaming onto an existing name conflicts, too
        final String otherId = create("other");
        sendData(PUT, MediaType.APPLICATION_JSON, BASE + "/" + otherId, viewBody("dup"), 409);
    }

    @Test
    @JUnitTemporaryDatabase
    public void writesRequireRoleAdmin() throws Exception {
        setUser("bob", new String[] { "ROLE_USER" });
        sendData(POST, MediaType.APPLICATION_JSON, BASE, viewBody("nope"), 403);
        setUser("admin", new String[] { "ROLE_ADMIN" });

        final String id = create("guarded");
        setUser("bob", new String[] { "ROLE_USER" });
        sendData(PUT, MediaType.APPLICATION_JSON, BASE + "/" + id, viewBody("guarded"), 403);
        sendRequest(DELETE, BASE + "/" + id, 403);
        // reads stay open to any authenticated user
        sendRequest(GET, BASE + "/" + id, 200);
        setUser("admin", new String[] { "ROLE_ADMIN" });
    }

    @Test
    @JUnitTemporaryDatabase
    public void resolvesTheDefaultViewByUserGroupThenSetting() throws Exception {
        // The seeded stock view is the configured default
        assertEquals("default", m_mapper.readTree(sendRequest(GET, BASE + "/default", 200)).get("name").asText());

        // Repointing the setting changes what resolves
        final String opsId = create("Ops");
        sendData(PUT, MediaType.APPLICATION_JSON, BASE + "/default/" + opsId, "", 204);
        assertEquals("Ops", m_mapper.readTree(sendRequest(GET, BASE + "/default", 200)).get("name").asText());
        assertEquals(true, m_mapper.readTree(sendRequest(GET, BASE + "/" + opsId, 200)).get("isDefault").asBoolean());

        // A view named like the user beats the configured default
        final String adminId = create("admin");
        assertEquals("admin", m_mapper.readTree(sendRequest(GET, BASE + "/default", 200)).get("name").asText());

        // ...and deleting it falls back to the configured default again
        sendRequest(DELETE, BASE + "/" + adminId, 204);
        assertEquals("Ops", m_mapper.readTree(sendRequest(GET, BASE + "/default", 200)).get("name").asText());

        // Deleting the default view clears the setting: nothing resolves, even
        // though the seeded "default" view still exists (no silent fallback)
        sendRequest(DELETE, BASE + "/" + opsId, 204);
        sendRequest(GET, BASE + "/default", 404);
    }

    @Test
    @JUnitTemporaryDatabase
    public void computesStatusAndDrillDowns() throws Exception {
        m_databasePopulator.populateDatabase();

        final String id = create("grid");

        final JsonNode status = m_mapper.readTree(sendRequest(GET, BASE + "/" + id + "/status", 200));
        assertEquals(id, status.get("viewId").asText());
        assertEquals("grid", status.get("viewName").asText());
        assertEquals(120, status.get("refreshSeconds").asInt());
        assertEquals(1, status.get("rows").size());
        assertEquals("Routers", status.get("rows").get(0).asText());
        assertEquals(1, status.get("columns").size());
        assertEquals(1, status.get("cells").size());
        final JsonNode cell = status.get("cells").get(0).get(0);
        assertNotNull(cell.get("status").asText());
        assertTrue("node1 is in Routers and DEV_AC", cell.get("total").asInt() >= 1);
        assertTrue(cell.get("down").asInt() <= cell.get("total").asInt());

        // The populated database seeds one unacknowledged alarm on node1
        final JsonNode alarms = m_mapper.readTree(sendRequest(GET, BASE + "/" + id + "/alarms", 200));
        assertEquals(1, alarms.size());
        assertNotNull(alarms.get(0).get("nodeLabel").asText());

        final JsonNode rtc = m_mapper.readTree(sendRequest(GET, BASE + "/" + id + "/rtc", 200));
        assertTrue(rtc.size() >= 1);
        assertTrue(rtc.get(0).get("availability").asDouble() <= 1.0);

        // Notifications: the populator seeds none, so this is just a clean 200
        assertEquals(0, m_mapper.readTree(sendRequest(GET, BASE + "/" + id + "/notifications", 200)).size());

        // Drill-down selection: a known label narrows, an unknown one is a 400
        sendRequest(GET, BASE + "/" + id + "/alarms?row=Routers&column=DEV", 200);
        sendRequest(GET, BASE + "/" + id + "/alarms?row=NoSuchRow", 400);

        // A view referencing a category that does not exist fails loudly
        final String brokenId = create("broken-" + System.currentTimeMillis());
        final String broken = "{\"name\":\"broken\",\"rows\":[{\"label\":\"r\",\"categories\":[\"NoSuchCategory\"]}],"
                + "\"columns\":[{\"label\":\"c\",\"categories\":[\"DEV_AC\"]}]}";
        sendData(PUT, MediaType.APPLICATION_JSON, BASE + "/" + brokenId, broken, 204);
        sendRequest(GET, BASE + "/" + brokenId + "/status", 400);
    }

    private static String lastPathSegment(final Object location) {
        if (location == null) {
            return null;
        }
        final String s = location.toString();
        return s.substring(s.lastIndexOf('/') + 1);
    }
}
