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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.servlet.ServletContext;
import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.config.GraphCollectionConfigFactory;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
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
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase
public class GraphCollectionRestServiceIT extends AbstractSpringJerseyRestTestCase {

    private static final String BASE = "/graph-collections";

    @Autowired
    private ServletContext m_servletContext;

    private final File m_configFile = new File("target/test-classes/graph-collections-it.xml");

    public GraphCollectionRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Override
    protected void beforeServletStart() throws Exception {
        // Reset the file every time so the tests do not depend on each other's writes.
        Files.copy(new File("src/test/resources/ksc-performance-reports.xml").toPath(), m_configFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        GraphCollectionConfigFactory.setConfigFile(m_configFile);
        GraphCollectionConfigFactory.getInstance().reload();
    }

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
        setUser("admin", new String[] { "ROLE_ADMIN" });
    }

    @Test
    public void testListAndGet() throws Exception {
        final JSONArray list = new JSONArray(getJson(BASE, 200));
        assertEquals(2, list.length());
        // Sorted by title: "Test" before "Test 2".
        assertEquals("Test", list.getJSONObject(0).getString("title"));
        assertEquals("Test 2", list.getJSONObject(1).getString("title"));
        assertEquals(1, list.getJSONObject(0).getJSONArray("graphs").length());

        final JSONObject one = new JSONObject(getJson(BASE + "/0", 200));
        assertEquals(0, one.getInt("id"));
        assertEquals("Test", one.getString("title"));
        assertFalse(one.getBoolean("showTimespanButton"));
        assertEquals(0, one.getInt("graphsPerLine"));
        final JSONObject graph = one.getJSONArray("graphs").getJSONObject(0);
        assertEquals("prefab", graph.getString("kind"));
        assertEquals("blah", graph.getString("title"));
        assertEquals("ssh", graph.getString("graphtype"));
        assertEquals("7_day", graph.getString("timespan"));
        assertEquals("node[8].responseTime[216.216.217.254]", graph.getString("resourceId"));

        sendRequest(GET, BASE + "/99", 404);
    }

    @Test
    public void testTimespans() throws Exception {
        final JSONArray timespans = new JSONArray(getJson(BASE + "/timespans", 200));
        assertEquals(GraphCollectionConfigFactory.TIMESPAN_OPTIONS.length, timespans.length());
        assertEquals("1_hour", timespans.getJSONObject(0).getString("id"));
        assertEquals("1 hour", timespans.getJSONObject(0).getString("label"));
        assertEquals("Yesterday 9am-5pm", timespans.getJSONObject(15).getString("label"));
    }

    @Test
    public void testCreate() throws Exception {
        final String body = "{\"title\":\"Created\",\"showTimespanButton\":true,\"graphsPerLine\":3,"
                + "\"graphs\":[{\"title\":\"First\",\"resourceId\":\"node[1].nodeSnmp[]\",\"graphtype\":\"mib2.tcpopen\",\"timespan\":\"1_hour\"}]}";
        final MockHttpServletResponse response = sendData(POST, MediaType.APPLICATION_JSON, BASE, body, 201);
        assertTrue(response.getHeader("Location").toString().endsWith(BASE + "/2"));
        final JSONObject created = new JSONObject(response.getContentAsString());
        assertEquals(2, created.getInt("id"));
        assertEquals("Created", created.getString("title"));
        assertTrue(created.getBoolean("showTimespanButton"));
        assertEquals(3, created.getInt("graphsPerLine"));
        assertEquals("First", created.getJSONArray("graphs").getJSONObject(0).getString("title"));

        final String xml = slurp();
        assertTrue(xml, xml.contains("title=\"Created\""));
        assertTrue(xml, xml.contains("graphtype=\"mib2.tcpopen\""));

        // The id in a create body is ignored, so the next one is assigned too.
        final MockHttpServletResponse again = sendData(POST, MediaType.APPLICATION_JSON, BASE, "{\"id\":0,\"title\":\"Another\"}", 201);
        assertEquals(3, new JSONObject(again.getContentAsString()).getInt("id"));
        assertEquals(4, new JSONArray(getJson(BASE, 200)).length());
    }

    @Test
    public void testCreateRejectsBadInput() throws Exception {
        sendData(POST, MediaType.APPLICATION_JSON, BASE, "{\"title\":\"  \"}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, BASE, "{\"title\":\"x\",\"graphsPerLine\":-1}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, BASE,
                "{\"title\":\"x\",\"graphs\":[{\"resourceId\":\"node[1].nodeSnmp[]\",\"graphtype\":\"mib2.tcpopen\",\"timespan\":\"fortnight\"}]}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, BASE,
                "{\"title\":\"x\",\"graphs\":[{\"resourceId\":\"node[1].nodeSnmp[]\",\"timespan\":\"1_hour\"}]}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, BASE,
                "{\"title\":\"x\",\"graphs\":[{\"graphtype\":\"mib2.tcpopen\",\"timespan\":\"1_hour\"}]}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, BASE,
                "{\"title\":\"x\",\"graphs\":[{\"resourceId\":\"not a resource id\",\"graphtype\":\"mib2.tcpopen\",\"timespan\":\"1_hour\"}]}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, BASE,
                "{\"title\":\"x\",\"graphs\":[{\"resourceId\":\"node[1].nodeSnmp[]\",\"graphtype\":\"mib2.tcpopen\",\"timespan\":\"1_hour\",\"extlink\":\"javascript:alert(1)\"}]}", 400);
        // kind: adhoc is reserved and rejected; anything else unknown is rejected too.
        sendData(POST, MediaType.APPLICATION_JSON, BASE,
                "{\"title\":\"x\",\"graphs\":[{\"kind\":\"adhoc\",\"title\":\"a\",\"timespan\":\"1_hour\"}]}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, BASE,
                "{\"title\":\"x\",\"graphs\":[{\"kind\":\"sparkline\",\"resourceId\":\"node[1].nodeSnmp[]\",\"graphtype\":\"mib2.tcpopen\",\"timespan\":\"1_hour\"}]}", 400);
        // Protocol-relative links leave the site without a scheme; a missing timespan says so plainly.
        sendData(POST, MediaType.APPLICATION_JSON, BASE,
                "{\"title\":\"x\",\"graphs\":[{\"resourceId\":\"node[1].nodeSnmp[]\",\"graphtype\":\"mib2.tcpopen\",\"timespan\":\"1_hour\",\"extlink\":\"//evil.example/x\"}]}", 400);
        final MockHttpServletResponse noTimespan = sendData(POST, MediaType.APPLICATION_JSON, BASE,
                "{\"title\":\"x\",\"graphs\":[{\"resourceId\":\"node[1].nodeSnmp[]\",\"graphtype\":\"mib2.tcpopen\"}]}", 400);
        assertEquals("Graph 1: a timespan is required.", noTimespan.getContentAsString());
        final String longTitle = "x".repeat(256);
        sendData(POST, MediaType.APPLICATION_JSON, BASE, "{\"title\":\"" + longTitle + "\"}", 400);
        assertEquals(2, new JSONArray(getJson(BASE, 200)).length());
        assertFalse(slurp().contains("title=\"x\""));
    }

    @Test
    public void testCreateToleratesUnknownFieldsAndStoresLinks() throws Exception {
        // A client posting back a resolved graph, or a newer client, must not be rejected for extra fields.
        final String body = "{\"title\":\"Tolerant\",\"someFutureField\":true,"
                + "\"graphs\":[{\"kind\":\"prefab\",\"title\":\"g\",\"index\":0,\"valid\":true,\"resourceId\":\"node%5B1%5D.nodeSnmp%5B%5D\","
                + "\"graphtype\":\"mib2.tcpopen\",\"timespan\":\"1_hour\",\"extlink\":\" https://example.org/dash \"}]}";
        final JSONObject created = new JSONObject(sendData(POST, MediaType.APPLICATION_JSON, BASE, body, 201).getContentAsString());
        final JSONObject graph = created.getJSONArray("graphs").getJSONObject(0);
        assertEquals("prefab", graph.getString("kind"));
        assertEquals("https://example.org/dash", graph.getString("extlink"));
        // URL-encoded ids are accepted as legacy files carry them; they are stored as given.
        assertEquals("node%5B1%5D.nodeSnmp%5B%5D", graph.getString("resourceId"));

        // Relative links, as hand-edited files carry, round-trip too.
        final String relative = "{\"title\":\"r\",\"resourceId\":\"node[1].nodeSnmp[]\",\"graphtype\":\"mib2.tcpopen\",\"timespan\":\"1_hour\",\"extlink\":\"element/node.jsp?node=1\"}";
        final JSONObject withRelative = new JSONObject(sendData(POST, MediaType.APPLICATION_JSON, BASE + "/0/graphs", relative, 200).getContentAsString());
        assertEquals("element/node.jsp?node=1", withRelative.getJSONArray("graphs").getJSONObject(1).getString("extlink"));
        // The single-graph error message carries no "Graph N:" prefix.
        final String bad = "{\"title\":\"r\",\"resourceId\":\"node[1].nodeSnmp[]\",\"graphtype\":\"mib2.tcpopen\",\"timespan\":\"nope\"}";
        final MockHttpServletResponse badResponse = sendData(POST, MediaType.APPLICATION_JSON, BASE + "/0/graphs", bad, 400);
        assertEquals("unknown timespan 'nope'.", badResponse.getContentAsString());
    }

    @Test
    public void testUpdateReplacesEverything() throws Exception {
        final String body = "{\"id\":42,\"title\":\"Renamed\",\"showTimespanButton\":true,\"showGraphtypeButton\":true,\"graphsPerLine\":2,"
                + "\"graphs\":["
                + "{\"title\":\"Second\",\"resourceId\":\"node[2].nodeSnmp[]\",\"graphtype\":\"mib2.tcpopen\",\"timespan\":\"1_day\"},"
                + "{\"title\":\"blah\",\"resourceId\":\"node[8].responseTime[216.216.217.254]\",\"graphtype\":\"ssh\",\"timespan\":\"7_day\"}"
                + "]}";
        final JSONObject saved = new JSONObject(sendData(PUT, MediaType.APPLICATION_JSON, BASE + "/1", body, 200).getContentAsString());
        // The path id wins over the body id.
        assertEquals(1, saved.getInt("id"));
        assertEquals("Renamed", saved.getString("title"));
        assertEquals(2, saved.getJSONArray("graphs").length());
        assertEquals("Second", saved.getJSONArray("graphs").getJSONObject(0).getString("title"));

        final JSONObject reread = new JSONObject(getJson(BASE + "/1", 200));
        assertEquals("Renamed", reread.getString("title"));
        assertTrue(reread.getBoolean("showGraphtypeButton"));
        assertEquals(2, reread.getInt("graphsPerLine"));
        assertEquals("1_day", reread.getJSONArray("graphs").getJSONObject(0).getString("timespan"));

        final String xml = slurp();
        assertTrue(xml, xml.contains("title=\"Renamed\""));
        assertFalse(xml, xml.contains("title=\"Test 2\""));

        sendData(PUT, MediaType.APPLICATION_JSON, BASE + "/99", body, 404);
        sendData(PUT, MediaType.APPLICATION_JSON, BASE + "/1", "{\"title\":\"\"}", 400);
    }

    @Test
    public void testDelete() throws Exception {
        sendRequest(DELETE, BASE + "/1", 204);
        sendRequest(GET, BASE + "/1", 404);
        sendRequest(DELETE, BASE + "/1", 404);
        assertEquals(1, new JSONArray(getJson(BASE, 200)).length());
        assertFalse(slurp().contains("title=\"Test 2\""));
    }

    @Test
    public void testAddGraph() throws Exception {
        final String graph = "{\"title\":\"Added\",\"resourceId\":\"node[1].nodeSnmp[]\",\"graphtype\":\"mib2.tcpopen\",\"timespan\":\"2_day\"}";
        JSONObject saved = new JSONObject(sendData(POST, MediaType.APPLICATION_JSON, BASE + "/0/graphs", graph, 200).getContentAsString());
        assertEquals(2, saved.getJSONArray("graphs").length());
        assertEquals("Added", saved.getJSONArray("graphs").getJSONObject(1).getString("title"));

        final String first = "{\"title\":\"Inserted\",\"resourceId\":\"node[1].nodeSnmp[]\",\"graphtype\":\"mib2.tcpopen\",\"timespan\":\"2_day\"}";
        saved = new JSONObject(sendData(POST, MediaType.APPLICATION_JSON, BASE + "/0/graphs?index=0", first, 200).getContentAsString());
        assertEquals(3, saved.getJSONArray("graphs").length());
        assertEquals("Inserted", saved.getJSONArray("graphs").getJSONObject(0).getString("title"));
        assertEquals("Added", saved.getJSONArray("graphs").getJSONObject(2).getString("title"));

        sendData(POST, MediaType.APPLICATION_JSON, BASE + "/0/graphs?index=9", first, 400);
        sendData(POST, MediaType.APPLICATION_JSON, BASE + "/0/graphs", "{\"title\":\"no timespan\",\"resourceId\":\"node[1].nodeSnmp[]\",\"graphtype\":\"x\"}", 400);
        sendData(POST, MediaType.APPLICATION_JSON, BASE + "/99/graphs", graph, 404);

        final String xml = slurp();
        assertTrue(xml, xml.contains("title=\"Inserted\""));
        assertTrue(xml, xml.contains("title=\"Added\""));
    }

    @Test
    public void testReload() throws Exception {
        // Edit the file behind the API's back, then ask for a reload.
        final String xml = slurp().replace("title=\"Test 2\"", "title=\"Edited on disk\"");
        Files.write(m_configFile.toPath(), xml.getBytes(StandardCharsets.UTF_8));
        assertEquals("Test 2", new JSONObject(getJson(BASE + "/1", 200)).getString("title"));
        sendRequest(POST, BASE + "/reload", 204);
        assertEquals("Edited on disk", new JSONObject(getJson(BASE + "/1", 200)).getString("title"));
    }

    @Test
    public void testReadOnlyUserCanReadButNotWrite() throws Exception {
        setUser("viewer", new String[] { "ROLE_USER", "ROLE_READONLY" });
        try {
            getJson(BASE, 200);
            getJson(BASE + "/0", 200);
            sendData(POST, MediaType.APPLICATION_JSON, BASE, "{\"title\":\"nope\"}", 403);
            sendData(PUT, MediaType.APPLICATION_JSON, BASE + "/0", "{\"title\":\"nope\"}", 403);
            sendData(POST, MediaType.APPLICATION_JSON, BASE + "/0/graphs",
                    "{\"title\":\"nope\",\"resourceId\":\"node[1].nodeSnmp[]\",\"graphtype\":\"x\",\"timespan\":\"1_hour\"}", 403);
            sendRequest(DELETE, BASE + "/0", 403);
            sendRequest(POST, BASE + "/reload", 403);
        } finally {
            setUser("admin", new String[] { "ROLE_ADMIN" });
        }
        assertEquals("Test", new JSONObject(getJson(BASE + "/0", 200)).getString("title"));
        assertFalse(slurp().contains("nope"));
    }

    @Test
    public void testPlainUserCanWrite() throws Exception {
        setUser("someone", new String[] { "ROLE_USER" });
        try {
            sendData(POST, MediaType.APPLICATION_JSON, BASE, "{\"title\":\"Mine\"}", 201);
        } finally {
            setUser("admin", new String[] { "ROLE_ADMIN" });
        }
        assertTrue(slurp().contains("title=\"Mine\""));
    }

    @Test
    public void testResolvedCustomCollection() throws Exception {
        final JSONObject resolved = new JSONObject(getJson(BASE + "/0/resolved", 200));
        assertEquals(0, resolved.getInt("id"));
        assertEquals("custom", resolved.getString("type"));
        assertEquals("Test", resolved.getString("title"));
        assertTrue(resolved.getInt("graphsPerLine") >= 1);
        assertTrue(resolved.isNull("timespan"));
        final JSONObject graph = resolved.getJSONArray("graphs").getJSONObject(0);
        assertEquals(0, graph.getInt("index"));
        assertEquals("prefab", graph.getString("kind"));
        assertEquals("7_day", graph.getString("timespan"));
        assertEquals("ssh", graph.getString("graphtype"));
        assertTrue(graph.getLong("end") > graph.getLong("start"));
        // Node 8 is not in the test database, so the graph is reported, not dropped.
        assertFalse(graph.getBoolean("valid"));
        assertNotNull(graph.getString("error"));
        assertEquals(1, new JSONObject(getJson(BASE + "/0", 200)).getJSONArray("graphs").length());

        final JSONObject overridden = new JSONObject(getJson(BASE + "/0/resolved?timespan=1_hour&graphtype=mib2.bits", 200));
        assertEquals("1_hour", overridden.getString("timespan"));
        assertEquals("mib2.bits", overridden.getString("graphtype"));
        final JSONObject graph2 = overridden.getJSONArray("graphs").getJSONObject(0);
        assertEquals("1_hour", graph2.getString("timespan"));
        assertEquals("mib2.bits", graph2.getString("graphtype"));
        final long hour = graph2.getLong("end") - graph2.getLong("start");
        assertTrue(Long.toString(hour), hour >= 3_500_000L && hour <= 3_700_000L);

        // "none" means no override, as the legacy page sent it.
        assertTrue(new JSONObject(getJson(BASE + "/0/resolved?timespan=none&graphtype=none", 200)).isNull("timespan"));
        sendRequest(GET, BASE + "/0/resolved?timespan=fortnight", 400);
        sendRequest(GET, BASE + "/99/resolved", 404);
    }

    @Test
    public void testResolvedNodeCollectionErrors() throws Exception {
        // Building a node's collection walks the timeseries storage for every interface, which this
        // harness does not provide, so only the paths that fail before that lookup are checked here:
        // the override is validated first, and an unknown node never reaches the storage.
        sendRequest(GET, BASE + "/resolved/node/999999?timespan=fortnight", 400);
        sendRequest(GET, BASE + "/resolved/node/999999", 404);
    }

    private String getJson(final String url, final int expectedStatus) throws Exception {
        final MockHttpServletRequest request = createRequest(m_servletContext, GET, url, getUser(), getUserRoles());
        request.addHeader("Accept", MediaType.APPLICATION_JSON);
        return sendRequest(request, expectedStatus);
    }

    private String slurp() throws Exception {
        return new String(Files.readAllBytes(m_configFile.toPath()), StandardCharsets.UTF_8);
    }
}
