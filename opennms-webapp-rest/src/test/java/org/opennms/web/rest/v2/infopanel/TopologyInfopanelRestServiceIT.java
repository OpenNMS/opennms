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
package org.opennms.web.rest.v2.infopanel;

import static org.junit.Assert.assertEquals;

import org.json.JSONArray;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;

/**
 * Integration test for {@code GET /api/v2/topology/infopanel}. Exercises the
 * resource through the REST stack: that it is wired (beans autowire), validates
 * its input (400 without a nodeId), 404s an unknown node, and returns a JSON
 * array for a real node. The rendering of {@code etc/infopanel} templates is
 * covered by {@code InfoPanelRendererTest}; a stock test environment has no
 * such templates, so a real node yields an empty array here.
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
public class TopologyInfopanelRestServiceIT extends AbstractSpringJerseyRestTestCase {

    public TopologyInfopanelRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
    }

    @Test
    @JUnitTemporaryDatabase
    public void infopanelEndpoint() throws Exception {
        // A nodeId is required.
        sendRequest(GET, "/topology/infopanel", 400);

        // Unknown node -> 404 (before any template is created).
        sendRequest(GET, "/topology/infopanel", parseParamData("nodeId=999999"), 404);

        // Create a node, then the endpoint returns a JSON array for it. With no
        // etc/infopanel templates in the test environment, the array is empty.
        final String node = "<node type=\"A\" label=\"TestMachine1\" foreignSource=\"JUnit\" foreignId=\"TestMachine1\">"
                + "<labelSource>H</labelSource>"
                + "<sysName>TestMachine1</sysName>"
                + "<sysObjectId>.1.3.6.1.4.1.8072.3.2.255</sysObjectId>"
                + "</node>";
        sendPost("/nodes", node, 201);

        final String body = sendRequest(GET, "/topology/infopanel", parseParamData("nodeId=1"), 200);
        assertEquals(0, new JSONArray(body).length());
    }

    @Test
    @JUnitTemporaryDatabase
    public void edgeEndpointValidatesAndRenders() throws Exception {
        // Both node ids are required.
        sendRequest(GET, "/topology/infopanel/edge", 400);
        sendRequest(GET, "/topology/infopanel/edge", parseParamData("sourceNodeId=1"), 400);

        // Unknown nodes are a 404.
        sendRequest(GET, "/topology/infopanel/edge", parseParamData("sourceNodeId=1&targetNodeId=999999"), 404);

        // Happy path: create a node, then the endpoint renders for a link with
        // that node on both ends. With no etc/infopanel templates in the test
        // environment the rendered array is empty.
        final String node = "<node type=\"A\" label=\"TestMachine1\" foreignSource=\"JUnit\" foreignId=\"TestMachine1\">"
                + "<labelSource>H</labelSource>"
                + "<sysName>TestMachine1</sysName>"
                + "<sysObjectId>.1.3.6.1.4.1.8072.3.2.255</sysObjectId>"
                + "</node>";
        sendPost("/nodes", node, 201);
        final String body = sendRequest(GET, "/topology/infopanel/edge",
                parseParamData("sourceNodeId=1&targetNodeId=1&sourcePort=eth0&targetPort=eth1&protocol=lldp"), 200);
        assertEquals(0, new JSONArray(body).length());
    }
}
