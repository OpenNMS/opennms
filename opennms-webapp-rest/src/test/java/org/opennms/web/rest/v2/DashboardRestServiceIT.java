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
import static org.junit.Assert.assertTrue;

import javax.ws.rs.core.MediaType;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

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
public class DashboardRestServiceIT extends AbstractSpringJerseyRestTestCase {

    @Autowired
    private DatabasePopulator m_databasePopulator;

    public DashboardRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    private static boolean s_populated = false;

    @Override
    protected void afterServletStart() {
        MockLogAppender.setupLogging();
        // the temp database is shared across the methods of this class;
        // repopulating trips unique constraints
        if (!s_populated) {
            m_databasePopulator.populateDatabase();
            s_populated = true;
        }
    }

    @Test
    public void testLayoutLifecycle() throws Exception {
        // the document round-trips as nested JSON, not an escaped string
        final String layout = "{\"scope\":\"SYSTEM\",\"version\":1,"
                + "\"panels\":[{\"id\":\"a\",\"type\":\"notes\",\"x\":0,\"y\":0,\"w\":6,\"h\":200,"
                + "\"options\":{\"text\":\"hello\"}}],"
                + "\"refresh\":{\"seconds\":120,\"paused\":false}}";
        sendData(PUT, MediaType.APPLICATION_JSON, "/dashboard/system", layout, 204);

        final JSONObject read = new JSONObject(getJson("/dashboard/system"));
        assertEquals("SYSTEM", read.getString("scope"));
        assertEquals(1, read.getJSONArray("panels").length());
        assertEquals("hello", read.getJSONArray("panels").getJSONObject(0).getJSONObject("options").getString("text"));

        // saving again replaces the document
        sendData(PUT, MediaType.APPLICATION_JSON, "/dashboard/system",
                "{\"scope\":\"SYSTEM\",\"version\":1,\"panels\":[],\"refresh\":{\"seconds\":60,\"paused\":true}}", 204);
        final JSONObject replaced = new JSONObject(getJson("/dashboard/system"));
        assertEquals(0, replaced.getJSONArray("panels").length());
        assertEquals(60, replaced.getJSONObject("refresh").getInt("seconds"));
    }

    @Test
    public void testNullBodyRejected() throws Exception {
        sendData(PUT, MediaType.APPLICATION_JSON, "/dashboard/system", "null", 400);
    }

    @Test
    public void testServiceTypesSortedCaseInsensitively() throws Exception {
        final JSONArray types = new JSONArray(getJson("/dashboard/service-types"));
        assertTrue(types.length() > 0);
        String previous = null;
        for (int i = 0; i < types.length(); i++) {
            final JSONObject type = types.getJSONObject(i);
            assertTrue(type.has("id"));
            final String name = type.getString("name");
            if (previous != null) {
                assertTrue(previous + " <= " + name, previous.compareToIgnoreCase(name) <= 0);
            }
            previous = name;
        }
    }

    @Test
    public void testLayoutWriteRequiresAdmin() throws Exception {
        // ensure a layout exists so the read below is a definite 200
        sendData(PUT, MediaType.APPLICATION_JSON, "/dashboard/system",
                "{\"scope\":\"SYSTEM\",\"panels\":[]}", 204);
        setUser("nobody", new String[]{ "ROLE_USER" });
        try {
            // every user may read the layout, only admins may write it
            sendRequest(GET, "/dashboard/system", 200);
            sendData(PUT, MediaType.APPLICATION_JSON, "/dashboard/system",
                    "{\"scope\":\"SYSTEM\",\"panels\":[]}", 403);
        } finally {
            setUser("admin", new String[]{ "ROLE_ADMIN" });
        }
    }

    private String getJson(final String url) throws Exception {
        final MockHttpServletRequest request = createRequest(GET, url);
        request.addHeader("Accept", MediaType.APPLICATION_JSON);
        return sendRequest(request, 200);
    }
}
