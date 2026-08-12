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
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Drives the {@code /api/v2/topology/views} resource end to end through the
 * CXF/Jersey test harness, asserting the CRUD lifecycle, that the canvas
 * {@code definition} round-trips as nested JSON (not an escaped string), and
 * the error semantics (409 on duplicate name, 400 on missing fields, 404 on a
 * missing id).
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
public class TopologyViewRestServiceIT extends AbstractSpringJerseyRestTestCase {

    private static final String BASE = "/topology/views";

    private final ObjectMapper m_mapper = new ObjectMapper();

    public TopologyViewRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "WARN");
        // Stamp a principal: the resource records the authenticated user as the
        // view's owner (and rejects requests without one).
        setUser("admin", new String[] { "ROLE_ADMIN" });
    }

    @Test
    @JUnitTemporaryDatabase
    public void canCreateReadUpdateDeleteView() throws Exception {
        // The catalog starts with just the seeded "Default" view.
        final JsonNode initial = m_mapper.readTree(sendRequest(GET, BASE, 200));
        assertEquals(1, initial.size());
        assertEquals("Default", initial.get(0).get("name").asText());

        // Create with a nested-JSON definition
        final String create = "{\"name\":\"Core DC\","
                + "\"definition\":{\"nodes\":[{\"id\":\"n1\",\"label\":\"core-sw1\",\"x\":120,\"y\":80}],"
                + "\"edges\":[],\"labels\":[],\"viewport\":{\"zoom\":1,\"panX\":0,\"panY\":0}}}";
        final MockHttpServletResponse post = sendData(POST, MediaType.APPLICATION_JSON, BASE, create, 201);
        final String id = lastPathSegment(post.getHeader("Location"));
        assertNotNull("POST should return a Location with an id", id);

        // Read back: definition is real nested JSON, owner stamped from the principal
        final JsonNode view = m_mapper.readTree(sendRequest(GET, BASE + "/" + id, 200));
        assertEquals("Core DC", view.get("name").asText());
        assertEquals("admin", view.get("owner").asText());
        assertTrue("definition should be a JSON object, not an escaped string",
                view.get("definition").isObject());
        assertTrue(view.get("definition").get("nodes").isArray());
        assertEquals(1, view.get("definition").get("nodes").size());
        assertTrue(view.get("lastModified").isNull());

        // Update (rename + change the canvas)
        final String update = "{\"name\":\"Core DC (edited)\","
                + "\"definition\":{\"nodes\":[{\"id\":\"n1\",\"label\":\"core-sw1\",\"x\":200,\"y\":200}],"
                + "\"edges\":[],\"labels\":[],\"viewport\":{\"zoom\":2,\"panX\":10,\"panY\":10}}}";
        sendData(PUT, MediaType.APPLICATION_JSON, BASE + "/" + id, update, 204);

        final JsonNode updated = m_mapper.readTree(sendRequest(GET, BASE + "/" + id, 200));
        assertEquals("Core DC (edited)", updated.get("name").asText());
        assertEquals(2, updated.get("definition").get("viewport").get("zoom").asInt());
        assertTrue("lastModified should be set after an update", !updated.get("lastModified").isNull());

        // Delete, then it is gone; the seeded "Default" view remains.
        sendRequest(DELETE, BASE + "/" + id, 204);
        sendRequest(GET, BASE + "/" + id, 404);
        final JsonNode remaining = m_mapper.readTree(sendRequest(GET, BASE, 200));
        assertEquals(1, remaining.size());
        assertEquals("Default", remaining.get(0).get("name").asText());
    }

    @Test
    @JUnitTemporaryDatabase
    public void rejectsDuplicateName() throws Exception {
        final String body = "{\"name\":\"dup\",\"definition\":{}}";
        sendData(POST, MediaType.APPLICATION_JSON, BASE, body, 201);
        sendData(POST, MediaType.APPLICATION_JSON, BASE, body, 409);
    }

    @Test
    @JUnitTemporaryDatabase
    public void rejectsMissingNameOrDefinition() throws Exception {
        sendData(POST, MediaType.APPLICATION_JSON, BASE, "{\"definition\":{}}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, BASE, "{\"name\":\"no-def\"}", 400);
    }

    @Test
    @JUnitTemporaryDatabase
    public void getMissingReturnsNotFound() throws Exception {
        sendRequest(GET, BASE + "/99999999", 404);
    }

    private static String lastPathSegment(final Object location) {
        if (location == null) {
            return null;
        }
        final String s = location.toString();
        return s.substring(s.lastIndexOf('/') + 1);
    }

    @Test
    @JUnitTemporaryDatabase
    public void normalizesNamesAndEnforcesUniquenessOnTrimmedValue() throws Exception {
        // Stored name is trimmed, and a differently-padded duplicate conflicts.
        final MockHttpServletResponse post =
                sendData(POST, MediaType.APPLICATION_JSON, BASE, "{\"name\":\" Core \",\"definition\":{}}", 201);
        final String id = lastPathSegment(post.getHeader("Location"));
        final JsonNode view = m_mapper.readTree(sendRequest(GET, BASE + "/" + id, 200));
        assertEquals("Core", view.get("name").asText());
        sendData(POST, MediaType.APPLICATION_JSON, BASE, "{\"name\":\"Core\",\"definition\":{}}", 409);
    }

    @Test
    @JUnitTemporaryDatabase
    public void ownerComesFromThePrincipalNotTheBody() throws Exception {
        // Role enforcement for /api/v2/** lives in the servlet security filter
        // chain, which this harness does not run -- but the resource's own
        // guarantee is testable: a client-supplied owner is never trusted.
        setUser("alice", new String[] { "ROLE_ADMIN" });
        final MockHttpServletResponse post = sendData(POST, MediaType.APPLICATION_JSON, BASE,
                "{\"name\":\"owned\",\"definition\":{},\"owner\":\"mallory\"}", 201);
        final String id = lastPathSegment(post.getHeader("Location"));
        final JsonNode view = m_mapper.readTree(sendRequest(GET, BASE + "/" + id, 200));
        assertEquals("alice", view.get("owner").asText());
        setUser("admin", new String[] { "ROLE_ADMIN" }); // restore for other tests
    }
}
