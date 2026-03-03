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

import java.sql.Timestamp;
import java.time.Instant;

import javax.sql.DataSource;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.test.JUnitConfigurationEnvironment;
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
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-svclayer.xml",
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml"
})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase
@Transactional
public class EventRestServiceIT extends AbstractSpringJerseyRestTestCase {

    private static long tsidCounter = 100_000L;

    public EventRestServiceIT() {
        super(CXF_REST_V2_CONTEXT_PATH);
    }

    @Autowired
    private DataSource m_dataSource;

    private JdbcTemplate m_jdbcTemplate;

    @Override
    protected void afterServletStart() throws Exception {
        MockLogAppender.setupLogging(true, "DEBUG");
        m_jdbcTemplate = new JdbcTemplate(m_dataSource);

        insertArchivedEvent(1L, "uei.opennms.org/test/somethingWentWrong", OnmsSeverity.MAJOR, "192.168.1.1");
        insertArchivedEvent(1L, "uei.opennms.org/test/somethingIsStillHappening", OnmsSeverity.WARNING, "192.168.1.1");
        insertArchivedEvent(1L, "uei.opennms.org/test/somethingIsOkNow", OnmsSeverity.NORMAL, "192.168.1.1");
        insertArchivedEvent(2L, "uei.opennms.org/test/somethingWentWrong", OnmsSeverity.MAJOR, "192.168.1.2");
        insertArchivedEvent(2L, "uei.opennms.org/test/somethingIsStillHappening", OnmsSeverity.WARNING, "192.168.1.2");
        insertArchivedEvent(2L, "uei.opennms.org/test/somethingIsOkNow", OnmsSeverity.NORMAL, "192.168.1.2");
    }

    @Test
    @JUnitTemporaryDatabase
    public void testEvents() throws Exception {
        String url = "/events";

        JSONObject object = new JSONObject(sendRequest(GET, url, 200));
        Assert.assertEquals(6, object.getInt("totalCount"));

        // Filter by node ID
        object = new JSONObject(sendRequest(GET, url, parseParamData("_s=node.id==1"), 200));
        Assert.assertEquals(3, object.getInt("totalCount"));

        // Filter by UEI
        object = new JSONObject(sendRequest(GET, url, parseParamData("_s=eventUei==uei.opennms.org/test/somethingWentWrong"), 200));
        Assert.assertEquals(2, object.getInt("totalCount"));
    }

    @Test
    @JUnitTemporaryDatabase
    public void testGetSingleEvent() throws Exception {
        long tsid = insertArchivedEvent(1L, "uei.opennms.org/test/singleEventTest", OnmsSeverity.CRITICAL, "10.0.0.1");
        JSONObject object = new JSONObject(sendRequest(GET, "/events/" + tsid, 200));
        Assert.assertEquals("uei.opennms.org/test/singleEventTest", object.getString("uei"));
    }

    @Test
    @JUnitTemporaryDatabase
    public void testAddEvent() throws Exception {
        String url = "/events";
        String event = "<event><uei>uei.opennms.org/testEvent</uei></event>";
        sendPost(url, event, 204);
    }

    private long insertArchivedEvent(Long nodeId, String uei, OnmsSeverity severity, String ipAddr) {
        long tsid = tsidCounter++;
        m_jdbcTemplate.update(
                "INSERT INTO events_archive (event_tsid, event_uei, event_source, event_severity, event_time, " +
                        "node_id, ip_addr, event_log_msg, event_descr, event_display, event_log) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                tsid, uei, "JUnit", severity.getId(), Timestamp.from(Instant.now()),
                nodeId, ipAddr, "Test log message", "Test description", "Y", "Y");
        return tsid;
    }
}
