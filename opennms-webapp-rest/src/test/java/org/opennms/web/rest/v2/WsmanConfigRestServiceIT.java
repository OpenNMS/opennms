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
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.UUID;

import javax.ws.rs.core.MediaType;

import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.netmgt.dao.WSManConfigDao;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
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
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml"
})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase
public class WsmanConfigRestServiceIT extends AbstractSpringJerseyRestTestCase {

    private static final String DEFINITION_10 = "{\"ranges\":[{\"begin\":\"10.0.0.1\",\"end\":\"10.0.0.50\"}],\"specifics\":[],\"ipMatches\":[],"
            + "\"username\":\"monitor\",\"password\":\"secret-one\",\"clearPassword\":false,\"ssl\":false,\"port\":5985}";

    @Autowired
    private WSManConfigDao m_wsManConfigDao;

    private Resource m_originalResource;
    private File m_workingCopy;
    private String m_shippedContent;

    public WsmanConfigRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
    }

    // The service reads and rewrites the DAO's config resource, so point the
    // shared DAO at a scratch copy of the shipped file for the duration of a test.
    @Before
    public void useWorkingCopy() throws Exception {
        final AbstractJaxbConfigDao<?, ?> dao = (AbstractJaxbConfigDao<?, ?>) m_wsManConfigDao;
        m_originalResource = dao.getConfigResource();
        m_shippedContent = new String(Files.readAllBytes(m_originalResource.getFile().toPath()), StandardCharsets.UTF_8);
        m_workingCopy = new File("target/test-work-dir", "wsman-config-" + UUID.randomUUID() + ".xml");
        m_workingCopy.getParentFile().mkdirs();
        Files.write(m_workingCopy.toPath(), m_shippedContent.getBytes(StandardCharsets.UTF_8));
        dao.setConfigResource(new FileSystemResource(m_workingCopy));
    }

    @After
    public void restoreShippedFile() throws Exception {
        ((AbstractJaxbConfigDao<?, ?>) m_wsManConfigDao).setConfigResource(m_originalResource);
        // never let a test leave the shipped file modified
        final String now = new String(Files.readAllBytes(m_originalResource.getFile().toPath()), StandardCharsets.UTF_8);
        if (!now.equals(m_shippedContent)) {
            Files.write(m_originalResource.getFile().toPath(), m_shippedContent.getBytes(StandardCharsets.UTF_8));
            throw new AssertionError("the shipped wsman-config.xml was modified by the test; restored");
        }
    }

    @Test
    public void testReadsShippedDefaultsWithoutExposingThePassword() throws Exception {
        // the shipped wsman-config.xml sets username/password and ssl/path on the root element
        final String body = getJson("/wsman-config", 200);
        final JSONObject defaults = new JSONObject(body).getJSONObject("defaults");
        assertEquals("root", defaults.getString("username"));
        assertTrue(defaults.getBoolean("hasPassword"));
        assertTrue(defaults.getBoolean("ssl"));
        assertEquals("/wsman", defaults.getString("path"));
        assertEquals(0, new JSONObject(body).getJSONArray("definitions").length());
        assertFalse("the password value must never be returned", body.contains("calvin"));
        assertFalse(body.contains("\"password\""));
    }

    @Test
    public void testForbiddenForNonAdmin() throws Exception {
        setUser("user", new String[]{ "ROLE_USER" });
        try {
            getJson("/wsman-config", 403);
            sendData(PUT, MediaType.APPLICATION_JSON, "/wsman-config", "{\"defaults\":{},\"definitions\":[]}", 403);
        } finally {
            setUser("admin", new String[]{ "ROLE_ADMIN" });
        }
    }

    @Test
    public void testUpdateKeepsReplacesAndClearsPasswords() throws Exception {
        // editing the defaults without a password keeps the stored one
        String body = sendData(PUT, MediaType.APPLICATION_JSON, "/wsman-config",
                "{\"defaults\":{\"username\":\"operator\",\"ssl\":true,\"path\":\"/wsman\",\"timeout\":15000,\"retry\":2},\"definitions\":[]}", 200)
                .getContentAsString();
        JSONObject defaults = new JSONObject(body).getJSONObject("defaults");
        assertEquals("operator", defaults.getString("username"));
        assertEquals(15000, defaults.getInt("timeout"));
        assertTrue(defaults.getBoolean("hasPassword"));
        assertTrue(fileContent().contains("password=\"calvin\""));
        assertFalse(body.contains("calvin"));

        // a new definition with its own password
        body = sendData(PUT, MediaType.APPLICATION_JSON, "/wsman-config",
                "{\"defaults\":{\"username\":\"operator\"},\"definitions\":[" + DEFINITION_10 + "]}", 200).getContentAsString();
        final JSONObject def = new JSONObject(body).getJSONArray("definitions").getJSONObject(0);
        assertTrue(def.getBoolean("hasPassword"));
        assertEquals("10.0.0.1", def.getJSONArray("ranges").getJSONObject(0).getString("begin"));
        assertFalse(body.contains("secret-one"));
        assertTrue(fileContent().contains("secret-one"));

        // re-saving it by sourceIndex without a password keeps it; a second new one goes in front of it
        body = sendData(PUT, MediaType.APPLICATION_JSON, "/wsman-config",
                "{\"defaults\":{\"username\":\"operator\"},\"definitions\":["
                + "{\"specifics\":[\"192.168.1.9\"],\"clearPassword\":false},"
                + "{\"sourceIndex\":0,\"ranges\":[{\"begin\":\"10.0.0.1\",\"end\":\"10.0.0.50\"}],\"username\":\"monitor\",\"clearPassword\":false}]}", 200)
                .getContentAsString();
        assertEquals(2, new JSONObject(body).getJSONArray("definitions").length());
        assertFalse(new JSONObject(body).getJSONArray("definitions").getJSONObject(0).getBoolean("hasPassword"));
        assertTrue(new JSONObject(body).getJSONArray("definitions").getJSONObject(1).getBoolean("hasPassword"));
        assertTrue(fileContent().contains("secret-one"));

        // clearing removes it from the file
        sendData(PUT, MediaType.APPLICATION_JSON, "/wsman-config",
                "{\"defaults\":{\"username\":\"operator\",\"clearPassword\":true},\"definitions\":["
                + "{\"sourceIndex\":1,\"ranges\":[{\"begin\":\"10.0.0.1\",\"end\":\"10.0.0.50\"}],\"clearPassword\":true}]}", 200);
        body = getJson("/wsman-config", 200);
        assertFalse(new JSONObject(body).getJSONObject("defaults").getBoolean("hasPassword"));
        assertFalse(new JSONObject(body).getJSONArray("definitions").getJSONObject(0).getBoolean("hasPassword"));
        assertFalse(fileContent().contains("secret-one"));
        assertFalse(fileContent().contains("calvin"));
    }

    @Test
    public void testInvalidUpdatesAreRejectedAndLeaveTheFileAlone() throws Exception {
        final String before = fileContent();
        final String[] bad = {
            // end before begin
            "{\"defaults\":{},\"definitions\":[{\"ranges\":[{\"begin\":\"10.0.0.50\",\"end\":\"10.0.0.1\"}]}]}",
            // mixed families
            "{\"defaults\":{},\"definitions\":[{\"ranges\":[{\"begin\":\"10.0.0.1\",\"end\":\"fe80::1\"}]}]}",
            // no criteria
            "{\"defaults\":{},\"definitions\":[{\"username\":\"x\"}]}",
            // malformed IPLIKE and specific
            "{\"defaults\":{},\"definitions\":[{\"ipMatches\":[\"10.0.*\"]}]}",
            "{\"defaults\":{},\"definitions\":[{\"specifics\":[\"not-an-ip\"]}]}",
            // out-of-range settings and a bad path
            "{\"defaults\":{\"port\":70000},\"definitions\":[]}",
            "{\"defaults\":{\"timeout\":0},\"definitions\":[]}",
            "{\"defaults\":{\"path\":\"wsman path\"},\"definitions\":[]}",
            // a stale or duplicated source index
            "{\"defaults\":{},\"definitions\":[{\"sourceIndex\":3,\"specifics\":[\"10.0.0.1\"]}]}",
            "{\"defaults\":{},\"definitions\":[]}" // placeholder replaced below
        };
        for (int i = 0; i < bad.length - 1; i++) {
            final String response = sendData(PUT, MediaType.APPLICATION_JSON, "/wsman-config", bad[i], 400).getContentAsString();
            assertFalse("expected a plain-text reason for: " + bad[i], response.isBlank());
        }
        assertEquals("a rejected update must not touch the file", before, fileContent());

        sendData(PUT, MediaType.APPLICATION_JSON, "/wsman-config",
                "{\"defaults\":{},\"definitions\":[" + DEFINITION_10 + "]}", 200);
        sendData(PUT, MediaType.APPLICATION_JSON, "/wsman-config",
                "{\"defaults\":{},\"definitions\":[{\"sourceIndex\":0,\"specifics\":[\"10.0.0.1\"]},{\"sourceIndex\":0,\"specifics\":[\"10.0.0.2\"]}]}", 400);
    }

    private String fileContent() throws Exception {
        return new String(Files.readAllBytes(m_workingCopy.toPath()), StandardCharsets.UTF_8);
    }

    private String getJson(final String url, final int expectedStatus) throws Exception {
        final MockHttpServletRequest request = createRequest(GET, url);
        request.addHeader("Accept", MediaType.APPLICATION_JSON);
        return sendRequest(request, expectedStatus);
    }
}
