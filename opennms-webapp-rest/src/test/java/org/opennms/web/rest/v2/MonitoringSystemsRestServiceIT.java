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

import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Collections;
import java.util.UUID;

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
@JUnitConfigurationEnvironment(systemProperties="org.apache.cxf.Logger=org.apache.cxf.common.logging.Slf4jLogger")
@JUnitTemporaryDatabase
@Transactional
public class MonitoringSystemsRestServiceIT extends AbstractSpringJerseyRestTestCase {
    public static final String DEFAULT_SYSTEM_LABEL = "localhost";
    public static final String DEFAULT_SYSTEM_LOCATION = "Default";
    private String testSystemId;
    private static final Logger LOG = LoggerFactory.getLogger(MonitoringSystemsRestServiceIT.class);

    @Autowired
    private DatabasePopulator m_databasePopulator;

    public MonitoringSystemsRestServiceIT () {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");

        testSystemId = UUID.randomUUID().toString().toLowerCase();

        // force test database to have our main monitoring system (DistPoller is actually a Monitoring System)
        m_databasePopulator.resetDatabase(true);
        m_databasePopulator.populateMainDistPoller(testSystemId, DEFAULT_SYSTEM_LABEL, DEFAULT_SYSTEM_LOCATION);
        m_databasePopulator.populateDatabase();
    }

    @Override
    protected void beforeServletDestroy() throws Exception {
        m_databasePopulator.resetDatabase(true);
    }

    @Test
    public void testGetMainMonitoringSystem() throws Exception {
        final String jsonResponse = sendRequest(GET, "/monitoringSystems/main", Collections.emptyMap(), 200);

        assertNotNull(jsonResponse);

        final JSONObject object = new JSONObject(jsonResponse);

        assertEquals(testSystemId, object.getString("id"));
        assertEquals("localhost", object.getString("label"));
        assertEquals("Default", object.getString("location"));
        assertEquals("OpenNMS", object.getString("type"));
    }
}
