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

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.core.test.ConfigurationTestUtils;
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
public class ChartRestServiceIT extends AbstractSpringJerseyRestTestCase {

    // the shipped configuration, so the three legacy charts are what gets exercised
    private static final File SHIPPED_CHART_CONFIG = new File("../opennms-base-assembly/src/main/filtered/etc/chart-configuration.xml");

    @Autowired
    private DatabasePopulator m_databasePopulator;

    private static boolean s_populated = false;
    private String m_onmsHome;

    public ChartRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Override
    protected void beforeServletStart() throws Exception {
        MockLogAppender.setupLogging();
        final File etc = new File("target/test-work-dir-charts/etc");
        etc.mkdirs();
        m_onmsHome = etc.getParent();
        FileUtils.copyFile(SHIPPED_CHART_CONFIG, new File(etc, "chart-configuration.xml"));
        System.setProperty("opennms.home", m_onmsHome);
        ConfigurationTestUtils.setRelativeHomeDirectory(m_onmsHome);
    }

    // context initialization can repoint opennms.home at opennms-base-assembly
    @Override
    protected void afterServletStart() throws Exception {
        System.setProperty("opennms.home", m_onmsHome);
        ConfigurationTestUtils.setRelativeHomeDirectory(m_onmsHome);
        if (!s_populated) {
            m_databasePopulator.populateDatabase();
            s_populated = true;
        }
        seedNode();
    }

    private static int jdbcNodeCount() throws Exception {
        try (Connection conn = DataSourceFactory.getInstance().getConnection();
             Statement statement = conn.createStatement();
             ResultSet rs = statement.executeQuery("select count(*) from node")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    // the chart queries run on the service's own JDBC connection, which does not
    // see the populator's uncommitted Hibernate rows; commit one node through JDBC,
    // once per temporary database
    private static void seedNode() throws Exception {
        try (Connection conn = DataSourceFactory.getInstance().getConnection();
             Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("select count(*) from node where nodelabel = 'chart-it'")) {
                rs.next();
                if (rs.getInt(1) > 0) {
                    return;
                }
            }
            statement.executeUpdate("insert into node (nodeid, location, nodecreatetime, nodelabel, nodetype)"
                    + " values (nextval('nodenxtid'), 'Default', now(), 'chart-it', 'A')");
        }
    }

    private String getJson(final String url) throws Exception {
        final MockHttpServletRequest request = createRequest(GET, url);
        request.addHeader("Accept", MediaType.APPLICATION_JSON);
        return sendRequest(request, 200);
    }

    @Test
    public void testListsTheShippedCharts() throws Exception {
        final JSONArray charts = new JSONArray(getJson("/charts"));
        final List<String> names = new ArrayList<>();
        final List<String> titles = new ArrayList<>();
        for (int i = 0; i < charts.length(); i++) {
            names.add(charts.getJSONObject(i).getString("name"));
            titles.add(charts.getJSONObject(i).getString("title"));
        }
        assertTrue(names.toString(), names.containsAll(List.of("sample-bar-chart", "sample-bar-chart2", "sample-bar-chart3")));
        assertTrue(titles.toString(), titles.containsAll(List.of("Alarms", "Last 7 Days Outages", "Node Inventory")));
        final JSONObject alarms = charts.getJSONObject(names.indexOf("sample-bar-chart"));
        assertEquals("Severity Chart", alarms.getString("subTitle"));
        assertEquals("Severity", alarms.getString("domainAxisLabel"));
        assertEquals("Count", alarms.getString("rangeAxisLabel"));
    }

    @Test
    public void testNodeInventoryCountsThePopulatedDatabase() throws Exception {
        final JSONObject chart = new JSONObject(getJson("/charts/sample-bar-chart3"));
        assertEquals("Node Inventory", chart.getString("title"));
        final JSONArray categories = chart.getJSONArray("categories");
        final List<String> keys = new ArrayList<>();
        for (int i = 0; i < categories.length(); i++) {
            keys.add(categories.getJSONObject(i).getString("key"));
            assertEquals(keys.get(i), categories.getJSONObject(i).getString("label"));
        }
        assertEquals(List.of("Nodes", "Interfaces", "Services"), keys);

        // each series populates only its own category; the other slots are null
        final JSONArray series = chart.getJSONArray("series");
        assertEquals(3, series.length());
        final JSONObject nodes = series.getJSONObject(0);
        assertEquals("Nodes", nodes.getString("name"));
        assertEquals("#ff0000", nodes.getString("color"));
        final JSONArray values = nodes.getJSONArray("values");
        assertEquals(3, values.length());
        assertTrue("expected the seeded node to be counted, JDBC sees " + jdbcNodeCount() + " nodes but the chart reported " + values.get(0), values.getInt(0) > 0);
        assertTrue(values.isNull(1));
        assertTrue(values.isNull(2));
    }

    @Test
    public void testSeverityChartLabelsItsCategories() throws Exception {
        final JSONObject chart = new JSONObject(getJson("/charts/sample-bar-chart"));
        final JSONArray series = chart.getJSONArray("series");
        assertEquals(2, series.length());
        assertEquals("Events", series.getJSONObject(0).getString("name"));
        assertEquals("Alarms", series.getJSONObject(1).getString("name"));
        final JSONArray categories = chart.getJSONArray("categories");
        for (int i = 0; i < categories.length(); i++) {
            final JSONObject category = categories.getJSONObject(i);
            final int severity = Integer.parseInt(category.getString("key"));
            assertTrue(severity > 4);
            assertNotNull(category.getString("label"));
            assertTrue(category.getString("label"), !category.getString("label").equals(category.getString("key")));
        }
    }

    @Test
    public void testUnknownChartIs404() throws Exception {
        sendRequest(GET, "/charts/no-such-chart", 404);
    }
}
