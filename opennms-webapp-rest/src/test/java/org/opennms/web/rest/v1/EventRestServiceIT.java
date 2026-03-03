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
package org.opennms.web.rest.v1;

import static org.junit.Assert.assertTrue;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import javax.sql.DataSource;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.rest.AbstractSpringJerseyRestTestCase;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.dao.mock.EventAnticipator;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Operaction;
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
        "file:src/main/webapp/WEB-INF/applicationContext-cxf-common.xml",
        "classpath:/applicationContext-rest-test.xml"
})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.timeseries.strategy=integration")
@JUnitTemporaryDatabase
@Transactional
public class EventRestServiceIT extends AbstractSpringJerseyRestTestCase {

    private static long tsidCounter = 200_000L;

    @Autowired
    private DataSource m_dataSource;

    @Autowired
    private MockEventIpcManager m_eventMgr;

    private JdbcTemplate m_jdbcTemplate;

    @Override
    protected void afterServletStart() {
        MockLogAppender.setupLogging(true, "DEBUG");
        m_jdbcTemplate = new JdbcTemplate(m_dataSource);

        Instant now = Instant.now();
        insertArchivedEvent(1L, "uei.opennms.org/test/event1", OnmsSeverity.WARNING, now.minus(1, ChronoUnit.HOURS));
        insertArchivedEvent(1L, "uei.opennms.org/test/event2", OnmsSeverity.MAJOR, now);
    }

    @Test
    public void testBetween() throws Exception {
        String xml;
        xml = sendRequest(GET, "/events/between", parseParamData("begin=2010-01-01T00:00:00Z"), 200);
        assertTrue(xml.contains("\"totalCount\""));
    }

    @Test
    public void canPublishEventToBus() throws Exception {
        Event e = new Event();
        e.setUei("some.uei");
        e.setHost("from-some-host");

        EventAnticipator anticipator = m_eventMgr.getEventAnticipator();
        anticipator.anticipateEvent(e);

        sendData(POST, MediaType.APPLICATION_XML, "/events", JaxbUtils.marshal(e), Status.ACCEPTED.getStatusCode());

        m_eventMgr.finishProcessingEvents();
        anticipator.verifyAnticipated(1000, 0, 0, 0, 0);
    }

    @Test
    public void testBadDbidEvent() throws Exception {
        final Event e = new Event();
        e.setDbid(-1L);
        sendData(POST, MediaType.APPLICATION_XML, "/events", JaxbUtils.marshal(e), Status.BAD_REQUEST.getStatusCode());
    }

    @Test
    public void testPostBadStateEvent() throws Exception {
        final Event e = new Event();
        final Operaction action = new Operaction();
        action.setState("monkey");
        e.addOperaction(action);
        sendData(POST, MediaType.APPLICATION_XML, "/events", JaxbUtils.marshal(e), Status.BAD_REQUEST.getStatusCode());
    }

    private void insertArchivedEvent(Long nodeId, String uei, OnmsSeverity severity, Instant eventTime) {
        long tsid = tsidCounter++;
        m_jdbcTemplate.update(
                "INSERT INTO events_archive (event_tsid, event_uei, event_source, event_severity, event_time, " +
                        "node_id, event_log_msg, event_descr, event_display, event_log) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                tsid, uei, "JUnit", severity.getId(), Timestamp.from(eventTime),
                nodeId, "Test event", "Test description", "Y", "Y");
    }
}
