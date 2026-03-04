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
package org.opennms.features.events.store;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;

public class JdbcEventStoreTest {

    private static final String CREATE_TABLE_SQL =
            "CREATE TABLE events_archive (" +
            "  event_tsid BIGINT PRIMARY KEY," +
            "  event_uei VARCHAR(256) NOT NULL," +
            "  event_source VARCHAR(256)," +
            "  event_severity INTEGER," +
            "  event_time TIMESTAMP NOT NULL," +
            "  node_id BIGINT," +
            "  ip_addr VARCHAR(64)," +
            "  service_name VARCHAR(256)," +
            "  ifindex INTEGER," +
            "  event_log_msg TEXT," +
            "  event_descr TEXT," +
            "  event_display VARCHAR(1) DEFAULT 'Y'," +
            "  event_log VARCHAR(1) DEFAULT 'Y'," +
            "  event_data TEXT," +
            "  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
            ")";

    private static final String INSERT_SQL =
            "INSERT INTO events_archive " +
            "(event_tsid, event_uei, event_source, event_severity, event_time, " +
            " node_id, ip_addr, service_name, ifindex, event_log_msg, event_descr, " +
            " event_display, event_log, event_data) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private JdbcTemplate jdbcTemplate;
    private JdbcEventStore store;

    private final Instant baseTime = Instant.parse("2026-03-01T12:00:00Z");

    @Before
    public void setUp() {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute(CREATE_TABLE_SQL);

        store = new JdbcEventStore(jdbcTemplate);

        insertTestEvent(1001L, "uei.opennms.org/nodes/nodeDown", "pollerd", 6,
                baseTime, 10L, "192.168.1.1", "ICMP", "Node down", "Node 10 is down",
                "Y", "Y", "{\"nodeLabel\":\"router-01\"}");

        insertTestEvent(1002L, "uei.opennms.org/nodes/nodeUp", "pollerd", 3,
                baseTime.plus(1, ChronoUnit.HOURS), 10L, "192.168.1.1", "ICMP",
                "Node up", "Node 10 is back up", "Y", "Y", null);

        insertTestEvent(1003L, "uei.opennms.org/nodes/nodeDown", "pollerd", 7,
                baseTime.plus(2, ChronoUnit.HOURS), 20L, "10.0.0.1", "SNMP",
                "Node critical", "Node 20 is critically down", "Y", "Y", null);

        insertTestEvent(1004L, "uei.opennms.org/threshold/exceeded", "threshd", 5,
                baseTime.minus(1, ChronoUnit.DAYS), 10L, "192.168.1.1", "ICMP",
                "Threshold exceeded", "CPU > 90%", "N", "Y", null);
    }

    @After
    public void tearDown() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS events_archive");
    }

    @Test
    public void shouldGetByTsid() {
        Optional<StoredEvent> result = store.getByTsid(1001L);
        assertThat(result).isPresent();
        StoredEvent event = result.get();
        assertThat(event.getEventTsid()).isEqualTo(1001L);
        assertThat(event.getEventUei()).isEqualTo("uei.opennms.org/nodes/nodeDown");
        assertThat(event.getEventSource()).isEqualTo("pollerd");
        assertThat(event.getEventSeverity()).isEqualTo(6);
        assertThat(event.getNodeId()).isEqualTo(10L);
        assertThat(event.getIpAddress()).isEqualTo("192.168.1.1");
        assertThat(event.getServiceName()).isEqualTo("ICMP");
        assertThat(event.getEventLogMsg()).isEqualTo("Node down");
        assertThat(event.getEventDescr()).isEqualTo("Node 10 is down");
        assertThat(event.getEventData()).containsEntry("nodeLabel", "router-01");
    }

    @Test
    public void shouldReturnEmptyForMissingTsid() {
        assertThat(store.getByTsid(9999L)).isEmpty();
    }

    @Test
    public void shouldFindByUei() {
        EventCriteria criteria = EventCriteria.builder()
                .uei("uei.opennms.org/nodes/nodeDown")
                .build();
        List<StoredEvent> results = store.findByCriteria(criteria);
        assertThat(results).hasSize(2);
        assertThat(results).extracting(StoredEvent::getEventTsid).containsExactly(1003L, 1001L);
    }

    @Test
    public void shouldFindByNodeId() {
        EventCriteria criteria = EventCriteria.builder()
                .nodeId(10L)
                .build();
        List<StoredEvent> results = store.findByCriteria(criteria);
        assertThat(results).hasSize(3);
    }

    @Test
    public void shouldFindBySeverityRange() {
        EventCriteria criteria = EventCriteria.builder()
                .severityGte(6)
                .build();
        List<StoredEvent> results = store.findByCriteria(criteria);
        assertThat(results).hasSize(2);
        assertThat(results).extracting(StoredEvent::getEventSeverity)
                .allMatch(s -> s >= 6);
    }

    @Test
    public void shouldFindByTimeRange() {
        EventCriteria criteria = EventCriteria.builder()
                .afterTime(baseTime)
                .beforeTime(baseTime.plus(3, ChronoUnit.HOURS))
                .build();
        List<StoredEvent> results = store.findByCriteria(criteria);
        assertThat(results).hasSize(3); // events at baseTime, +1h, +2h
    }

    @Test
    public void shouldFilterByEventDisplay() {
        EventCriteria criteria = EventCriteria.builder()
                .eventDisplayFilter("Y")
                .build();
        List<StoredEvent> results = store.findByCriteria(criteria);
        assertThat(results).hasSize(3); // event 1004 has display=N
    }

    @Test
    public void shouldRespectLimitAndOffset() {
        EventCriteria criteria = EventCriteria.builder()
                .limit(2)
                .offset(0)
                .build();
        List<StoredEvent> results = store.findByCriteria(criteria);
        assertThat(results).hasSize(2);

        EventCriteria page2 = EventCriteria.builder()
                .limit(2)
                .offset(2)
                .build();
        List<StoredEvent> page2Results = store.findByCriteria(page2);
        assertThat(page2Results).hasSize(2);
    }

    @Test
    public void shouldSortAscending() {
        EventCriteria criteria = EventCriteria.builder()
                .sortOrder(EventCriteria.SortOrder.ASC)
                .build();
        List<StoredEvent> results = store.findByCriteria(criteria);
        assertThat(results).hasSize(4);
        assertThat(results.get(0).getEventTsid()).isEqualTo(1004L); // earliest
        assertThat(results.get(3).getEventTsid()).isEqualTo(1003L); // latest
    }

    @Test
    public void shouldCountByCriteria() {
        EventCriteria allCriteria = EventCriteria.builder().build();
        assertThat(store.count(allCriteria)).isEqualTo(4L);

        EventCriteria nodeDownCriteria = EventCriteria.builder()
                .uei("uei.opennms.org/nodes/nodeDown")
                .build();
        assertThat(store.count(nodeDownCriteria)).isEqualTo(2L);
    }

    @Test
    public void shouldCombineMultipleFilters() {
        EventCriteria criteria = EventCriteria.builder()
                .uei("uei.opennms.org/nodes/nodeDown")
                .nodeId(10L)
                .build();
        List<StoredEvent> results = store.findByCriteria(criteria);
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getEventTsid()).isEqualTo(1001L);
    }

    private void insertTestEvent(long tsid, String uei, String source, int severity,
                                  Instant time, Long nodeId, String ipAddr,
                                  String service, String logMsg, String descr,
                                  String display, String log, String eventData) {
        insertTestEvent(tsid, uei, source, severity, time, nodeId, ipAddr,
                service, null, logMsg, descr, display, log, eventData);
    }

    private void insertTestEvent(long tsid, String uei, String source, int severity,
                                  Instant time, Long nodeId, String ipAddr,
                                  String service, Integer ifIndex, String logMsg, String descr,
                                  String display, String log, String eventData) {
        jdbcTemplate.update(INSERT_SQL,
                tsid, uei, source, severity,
                java.sql.Timestamp.from(time),
                nodeId, ipAddr, service, ifIndex, logMsg, descr,
                display, log, eventData);
    }
}
