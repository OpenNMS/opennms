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
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.features.distributed.kvstore.api.JsonStore;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.wsman.eventlog.EventLogStatusStore;
import org.opennms.netmgt.wsman.eventlog.EventLogTarget;
import org.springframework.beans.factory.annotation.Autowired;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Drives the Event Logs REST resource against a scratch opennms.home holding a copy of
 * the shipped wsman-eventlog-configuration.xml.
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
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml"
})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase
public class WsmanEventLogConfigRestServiceIT extends AbstractSpringJerseyRestTestCase {

    private static final String URL = "/wsman-config/event-log";

    @Autowired
    private JsonStore m_jsonStore;

    @Autowired
    private DatabasePopulator m_populator;

    // the temporary database is reused across the methods of this class, so populate once
    private static boolean s_populated;

    private File m_home;
    private File m_file;
    private String m_previousHome;

    public WsmanEventLogConfigRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Before
    public void useScratchHome() throws Exception {
        MockLogAppender.setupLogging(true);
        m_previousHome = System.getProperty("opennms.home");
        m_home = new File("target/test-work-dir/wsman-eventlog-home-" + UUID.randomUUID());
        final File etc = new File(m_home, "etc");
        assertTrue(etc.mkdirs());
        m_file = new File(etc, WsmanEventLogConfigRestService.FILE_NAME);
        Files.copy(new File("../opennms-base-assembly/src/main/filtered/etc/" + WsmanEventLogConfigRestService.FILE_NAME).toPath(), m_file.toPath());
        System.setProperty("opennms.home", m_home.getAbsolutePath());
        if (!s_populated) {
            m_populator.populateDatabase();
            s_populated = true;
        }
    }

    @After
    public void restoreHome() {
        if (m_previousHome != null) {
            System.setProperty("opennms.home", m_previousHome);
        }
    }

    @Test
    public void readsTheShippedConfiguration() throws Exception {
        final JSONObject config = new JSONObject(getJson(URL, 200));
        assertEquals(4, config.getInt("threads"));
        assertEquals("5m", config.getString("targetRefreshInterval"));
        assertEquals(64, config.getString("version").length());
        final JSONArray packages = config.getJSONArray("packages");
        assertEquals(1, packages.length());
        final JSONObject pkg = packages.getJSONObject(0);
        assertEquals("windows-servers", pkg.getString("name"));
        final JSONArray logs = pkg.getJSONArray("logs");
        assertEquals("System", logs.getJSONObject(0).getString("name"));
        assertTrue(logs.getJSONObject(0).getBoolean("enabled"));
        assertEquals(60000, logs.getJSONObject(0).getLong("interval"));
        assertEquals("wql", logs.getJSONObject(0).getString("mode"));
        assertFalse(logs.getJSONObject(2).getBoolean("enabled"));
        assertEquals(3, logs.length());
        assertEquals(6008, pkg.getJSONArray("eventMappings").getJSONObject(0).getInt("eventId"));
    }

    @Test
    public void savesChangesAndBumpsTheVersion() throws Exception {
        final JSONObject config = new JSONObject(getJson(URL, 200));
        final String version = config.getString("version");
        final JSONObject security = config.getJSONArray("packages").getJSONObject(0).getJSONArray("logs").getJSONObject(2);
        security.put("enabled", true);
        security.put("interval", 300000);

        final JSONObject saved = new JSONObject(sendData(PUT, MediaType.APPLICATION_JSON, URL, config.toString(), 200).getContentAsString());
        assertFalse(version.equals(saved.getString("version")));
        final JSONObject savedSecurity = saved.getJSONArray("packages").getJSONObject(0).getJSONArray("logs").getJSONObject(2);
        assertTrue(savedSecurity.getBoolean("enabled"));
        assertEquals(300000, savedSecurity.getLong("interval"));

        final String xml = new String(Files.readAllBytes(m_file.toPath()), StandardCharsets.UTF_8);
        assertTrue(xml, xml.contains("name=\"Security\""));
        assertTrue(xml, xml.contains("interval=\"300000\""));
        assertTrue(xml, xml.contains("event-id=\"6008\""));

        // the version the page loaded before the save is stale now
        sendData(PUT, MediaType.APPLICATION_JSON, URL, config.toString(), 409);
    }

    @Test
    public void rejectsInvalidValuesWithoutTouchingTheFile() throws Exception {
        final long before = m_file.lastModified();
        final byte[] bytes = Files.readAllBytes(m_file.toPath());
        final JSONObject config = new JSONObject(getJson(URL, 200));
        final JSONObject log = config.getJSONArray("packages").getJSONObject(0).getJSONArray("logs").getJSONObject(0);

        log.put("interval", 10);
        String reason = sendData(PUT, MediaType.APPLICATION_JSON, URL, config.toString(), 400).getContentAsString();
        assertTrue(reason, reason.contains("at least 1000 ms"));

        log.put("interval", 60000);
        log.put("mode", "shell");
        reason = sendData(PUT, MediaType.APPLICATION_JSON, URL, config.toString(), 400).getContentAsString();
        assertTrue(reason, reason.contains("Get-WinEvent"));
        log.put("mode", "wql");
        log.put("lookback", "soon");
        reason = sendData(PUT, MediaType.APPLICATION_JSON, URL, config.toString(), 400).getContentAsString();
        assertTrue(reason, reason.contains("lookback"));

        log.put("lookback", "1h");
        log.put("levels", "Critical");
        reason = sendData(PUT, MediaType.APPLICATION_JSON, URL, config.toString(), 400).getContentAsString();
        assertTrue(reason, reason.contains("Unknown event log level"));

        log.put("levels", "Error");
        config.getJSONArray("packages").getJSONObject(0).put("filter", "IPADDR IPLIKE");
        sendData(PUT, MediaType.APPLICATION_JSON, URL, config.toString(), 400);

        config.getJSONArray("packages").getJSONObject(0).put("filter", "IPADDR != '0.0.0.0'");
        config.remove("version");
        sendData(PUT, MediaType.APPLICATION_JSON, URL, config.toString(), 400);

        assertEquals(before, m_file.lastModified());
        assertEquals(new String(bytes, StandardCharsets.UTF_8), new String(Files.readAllBytes(m_file.toPath()), StandardCharsets.UTF_8));
    }

    @Test
    public void rejectsDuplicateMappingsAndOverlappingIds() throws Exception {
        final long before = m_file.lastModified();
        final byte[] bytes = Files.readAllBytes(m_file.toPath());
        final JSONObject config = new JSONObject(getJson(URL, 200));
        final JSONObject pkg = config.getJSONArray("packages").getJSONObject(0);
        final JSONArray mappings = pkg.getJSONArray("eventMappings");
        final JSONObject shutdown = mappings.getJSONObject(0);

        final JSONObject duplicate = new JSONObject(shutdown.toString()).put("logfile", "system").put("uei", "uei.opennms.org/wsman/eventlog/other");
        mappings.put(duplicate);
        String reason = sendData(PUT, MediaType.APPLICATION_JSON, URL, config.toString(), 400).getContentAsString();
        assertTrue(reason, reason.contains("maps Event ID 6008 twice"));

        mappings.remove(mappings.length() - 1);
        shutdown.put("eventId", 0);
        reason = sendData(PUT, MediaType.APPLICATION_JSON, URL, config.toString(), 400).getContentAsString();
        assertTrue(reason, reason.contains("Event ID of 1 or more"));

        shutdown.put("eventId", 6008);
        final JSONObject log = pkg.getJSONArray("logs").getJSONObject(0);
        log.put("includeEventIds", "1,2");
        log.put("excludeEventIds", "2");
        reason = sendData(PUT, MediaType.APPLICATION_JSON, URL, config.toString(), 400).getContentAsString();
        assertTrue(reason, reason.contains("Event ID 2 is both included and excluded"));

        assertEquals(before, m_file.lastModified());
        assertEquals(new String(bytes, StandardCharsets.UTF_8), new String(Files.readAllBytes(m_file.toPath()), StandardCharsets.UTF_8));
    }

    @Test
    public void previewsAFilter() throws Exception {
        JSONObject preview = new JSONObject(sendData(POST, MediaType.APPLICATION_JSON, URL + "/preview-filter", "{\"filter\":\"IPADDR != '0.0.0.0'\"}", 200).getContentAsString());
        assertTrue(preview.getBoolean("valid"));
        assertEquals("", preview.optString("error", ""));
        // the populated database has nodes but none carries the WS-Man service
        assertTrue(preview.getInt("matchedNodes") > 0);
        assertEquals(0, preview.getInt("readableNodes"));

        preview = new JSONObject(sendData(POST, MediaType.APPLICATION_JSON, URL + "/preview-filter", "{\"filter\":\"IPADDR IPLIKE\"}", 200).getContentAsString());
        assertFalse(preview.getBoolean("valid"));
        assertFalse(preview.optString("error", "").isEmpty());

        preview = new JSONObject(sendData(POST, MediaType.APPLICATION_JSON, URL + "/preview-filter", "{\"filter\":\"\"}", 200).getContentAsString());
        assertFalse(preview.getBoolean("valid"));
    }

    @Test
    public void reportsTheReadStatusRecordedByTheDaemon() throws Exception {
        assertEquals(0, new JSONObject(getJson(URL + "/status", 200)).getJSONArray("rows").length());
        final EventLogStatusStore store = new EventLogStatusStore(m_jsonStore);
        final EventLogTarget target = new EventLogTarget(42, "win-12", InetAddress.getByName("10.0.0.12"), "Default");
        store.update(target, "windows-servers", "System", st -> { st.lastSuccess = 1_700_000_000_000L; st.recordsRead = 7; st.cursor = 1003L; });
        store.update(target, "windows-servers", "Security", st -> { st.lastFailure = 1_700_000_100_000L; st.lastError = "401"; st.consecutiveFailures = 2; st.backingOff = true; });
        try {
            final JSONArray rows = new JSONObject(getJson(URL + "/status", 200)).getJSONArray("rows");
            assertEquals(2, rows.length());
            final JSONObject system = rows.getJSONObject(0);
            assertEquals("win-12", system.getString("nodeLabel"));
            assertEquals("System", system.getString("log"));
            assertEquals(7, system.getLong("recordsRead"));
            assertEquals(1003, system.getLong("cursor"));
            final JSONObject security = rows.getJSONObject(1);
            assertTrue(security.getBoolean("backingOff"));
            assertEquals("401", security.getString("lastError"));
        } finally {
            store.clear();
        }
    }

    @Test
    public void listsTheDefinitionsBehindTheMappings() throws Exception {
        final JSONArray rows = new JSONObject(getJson(URL + "/definitions", 200)).getJSONArray("rows");
        JSONObject shutdown = null;
        for (int i = 0; i < rows.length(); i++) {
            if ("uei.opennms.org/wsman/eventlog/unexpectedShutdown".equals(rows.getJSONObject(i).getString("uei"))) {
                shutdown = rows.getJSONObject(i);
            }
        }
        assertNotNull(rows.toString(), shutdown);
        assertTrue(shutdown.getBoolean("exists"));
        assertEquals("Windows unexpected shutdown", shutdown.getString("label"));
        assertEquals("Major", shutdown.getString("severity"));
        assertTrue(shutdown.getBoolean("alarm"));
        assertEquals(3, shutdown.getInt("alarmType"));
        assertEquals("opennms.wsman.eventlog.events", shutdown.getString("sourceName"));

        final JSONObject unknown = new JSONObject(getJson(URL + "/definition?uei=uei.opennms.org/wsman/eventlog/nothing", 200));
        assertFalse(unknown.getBoolean("exists"));
    }

    @Test
    public void createsThenUpdatesADefinitionForAMappedUei() throws Exception {
        final String uei = "uei.opennms.org/wsman/eventlog/diskFull";
        final JSONObject create = new JSONObject()
                .put("uei", uei).put("label", "Windows disk full").put("severity", "Major")
                .put("logMessage", "Disk full on %parm[computerName]%").put("alarm", true).put("alarmType", 3);
        final JSONObject created = new JSONObject(sendData(PUT, MediaType.APPLICATION_JSON, URL + "/definition", create.toString(), 200).getContentAsString());
        assertTrue(created.getBoolean("exists"));
        assertTrue(created.getBoolean("editable"));
        assertEquals("opennms.wsman.eventlog.events", created.getString("sourceName"));
        assertEquals("Windows disk full", created.getString("label"));
        assertEquals("Windows disk full", created.getString("description"));
        assertEquals("%uei%:%dpname%:%nodeid%", created.getString("reductionKey"));
        final long eventId = created.getLong("eventId");

        final JSONObject update = new JSONObject()
                .put("uei", uei).put("label", "Windows volume full").put("severity", "Minor").put("alarm", false);
        final JSONObject updated = new JSONObject(sendData(PUT, MediaType.APPLICATION_JSON, URL + "/definition", update.toString(), 200).getContentAsString());
        assertEquals(eventId, updated.getLong("eventId"));
        assertEquals("Windows volume full", updated.getString("label"));
        assertEquals("Minor", updated.getString("severity"));
        assertFalse(updated.getBoolean("alarm"));
        assertTrue(updated.getBoolean("editable"));

        sendData(PUT, MediaType.APPLICATION_JSON, URL + "/definition", new JSONObject().put("uei", uei).put("severity", "Major").toString(), 400);
        sendData(PUT, MediaType.APPLICATION_JSON, URL + "/definition", new JSONObject().put("uei", "nope").put("label", "x").put("severity", "Major").toString(), 400);
        sendData(PUT, MediaType.APPLICATION_JSON, URL + "/definition", create.put("severity", "Loud").toString(), 400);
        create.put("severity", "Major");
        final String longUei = "uei.opennms.org/wsman/eventlog/" + "x".repeat(300 - "uei.opennms.org/wsman/eventlog/".length());
        assertEquals(300, longUei.length());
        String reason = sendData(PUT, MediaType.APPLICATION_JSON, URL + "/definition", create.put("uei", longUei).toString(), 400).getContentAsString();
        assertTrue(reason, reason.contains("256"));
        reason = sendData(PUT, MediaType.APPLICATION_JSON, URL + "/definition", create.put("uei", "uei.opennms.org/wsman/eventlog/disk full").toString(), 400).getContentAsString();
        assertTrue(reason, reason.contains("whitespace"));
    }

    @Test
    public void refusesToRewriteADefinitionOfAnotherSource() throws Exception {
        final String uei = "uei.opennms.org/nodes/nodeDown";
        final JSONObject definition = new JSONObject(getJson(URL + "/definition?uei=" + uei, 200));
        assertTrue(definition.getBoolean("exists"));
        assertFalse(definition.getBoolean("editable"));
        assertFalse("opennms.wsman.eventlog.events".equals(definition.getString("sourceName")));

        final JSONObject update = new JSONObject()
                .put("uei", uei).put("label", "Node down, rewritten").put("severity", "Major").put("alarm", true);
        final String reason = sendData(PUT, MediaType.APPLICATION_JSON, URL + "/definition", update.toString(), 409).getContentAsString();
        assertTrue(reason, reason.contains("is defined in source '" + definition.getString("sourceName") + "'"));

        final JSONObject after = new JSONObject(getJson(URL + "/definition?uei=" + uei, 200));
        assertEquals(definition.getString("label"), after.getString("label"));
        assertEquals(definition.getString("sourceName"), after.getString("sourceName"));
    }

    @Test
    public void forbiddenForNonAdmin() throws Exception {
        setUser("user", new String[] { "ROLE_USER" });
        getJson(URL, 403);
        sendData(PUT, MediaType.APPLICATION_JSON, URL, "{}", 403);
        sendData(POST, MediaType.APPLICATION_JSON, URL + "/preview-filter", "{}", 403);
        getJson(URL + "/status", 403);
        getJson(URL + "/definitions", 403);
        getJson(URL + "/definition?uei=x", 403);
        sendData(PUT, MediaType.APPLICATION_JSON, URL + "/definition", "{}", 403);
    }

    private String getJson(final String url, final int expectedStatus) throws Exception {
        final MockHttpServletRequest request = createRequest(GET, url);
        request.addHeader("Accept", MediaType.APPLICATION_JSON);
        return sendRequest(request, expectedStatus);
    }
}
