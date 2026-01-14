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

import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.web.rest.support.SearchProperties;
import org.opennms.web.rest.support.SearchProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.Transactional;

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
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml"
})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase
@Transactional
public class SearchPropertiesIT extends AbstractSpringJerseyRestTestCase {

    @Autowired
    private JdbcTemplate m_jdbcTemplate;

    public SearchPropertiesIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Before
    public void setUp() throws Throwable {
        super.setUp();
        // Clear out the monitoringlocations table so that it returns HTTP 204 like the other services
        m_jdbcTemplate.execute("DELETE FROM monitoringlocations");
    }

    /**
     * Test searching and ordering by every {@link SearchProperty} listed in
     * {@link SearchProperties#ALARM_SERVICE_PROPERTIES}.
     * 
     * @throws Exception
     */
    @Test
    public void testAlarmSearchProperties() throws Exception {
        testAllSearchParameters("/alarms", SearchProperties.ALARM_SERVICE_PROPERTIES);
    }

    @Test
    public void testApplicationSearchProperties() throws Exception {
        testAllSearchParameters("/applications", SearchProperties.APPLICATION_SERVICE_PROPERTIES);
    }

    @Test
    public void testEventSearchProperties() throws Exception {
        testAllSearchParameters("/events", SearchProperties.EVENT_SERVICE_PROPERTIES);
    }

    @Test
    public void testIfServiceSearchProperties() throws Exception {
        testAllSearchParameters("/ifservices", SearchProperties.IF_SERVICE_SERVICE_PROPERTIES);
    }

    @Test
    public void testLocationSearchProperties() throws Exception {
        testAllSearchParameters("/monitoringLocations", SearchProperties.LOCATION_SERVICE_PROPERTIES);
    }

    @Test
    public void testMinionSearchProperties() throws Exception {
        testAllSearchParameters("/minions", SearchProperties.MINION_SERVICE_PROPERTIES);
    }

    @Test
    public void testNodesSearchProperties() throws Exception {
        testAllSearchParameters("/nodes", SearchProperties.NODE_SERVICE_PROPERTIES);
    }

    @Test
    public void testNotificationSearchProperties() throws Exception {
        testAllSearchParameters("/notifications", SearchProperties.NOTIFICATION_SERVICE_PROPERTIES);
    }

    @Test
    public void testOutageSearchProperties() throws Exception {
        testAllSearchParameters("/outages", SearchProperties.OUTAGE_SERVICE_PROPERTIES);
    }

    protected void testAllSearchParameters(String url, Set<SearchProperty> properties) throws Exception {
        for (SearchProperty prop : properties) {
            System.err.println("Testing " + prop.getId());
            switch(prop.type) {
            case BOOLEAN:
                sendRequest(GET, url, parseParamData(String.format("_s=%s==true;%s==false", prop.getId(), prop.getId())), 204);
                break;
            case FLOAT:
                sendRequest(GET, url, parseParamData(String.format("_s=%s==1.0;%s!=1.0", prop.getId(), prop.getId())), 204);
                break;
            case INTEGER:
                sendRequest(GET, url, parseParamData(String.format("_s=%s==1;%s!=1", prop.getId(), prop.getId())), 204);
                break;
            case IP_ADDRESS:
                sendRequest(GET, url, parseParamData(String.format("_s=%s==127.0.0.1;%s!=127.0.0.1", prop.getId(), prop.getId())), 204);
                break;
            case LONG:
                sendRequest(GET, url, parseParamData(String.format("_s=%s==1;%s!=1", prop.getId(), prop.getId())), 204);
                break;
            case STRING:
                sendRequest(GET, url, parseParamData(String.format("_s=%s==A;%s!=A", prop.getId(), prop.getId())), 204);
                break;
            case TIMESTAMP:
                sendRequest(GET, url, parseParamData(String.format(
                    "_s=%s==%s;%s!=%s", 
                    prop.getId(), 
                    "1970-01-01T00:00:00.000-0000",
                    prop.getId(), 
                    "1970-01-01T00:00:00.000%252B0000"
                )), 204);
                break;
            default:
                throw new IllegalArgumentException();
            }
            if (prop.orderBy) {
                sendRequest(GET, url, parseParamData("orderBy=" + prop.getId()), 204);
            }
        }
    }
}
