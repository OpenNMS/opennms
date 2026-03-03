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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * JDBC-based implementation of {@link EventStore} that queries the
 * {@code events_archive} table.
 */
public class JdbcEventStore implements EventStore {

    private static final Logger LOG = LoggerFactory.getLogger(JdbcEventStore.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final TypeReference<Map<String, String>> MAP_TYPE = new TypeReference<>() {};

    private static final String BASE_SELECT =
            "SELECT event_tsid, event_uei, event_source, event_severity, event_time, " +
            "node_id, ip_addr, service_name, event_log_msg, event_descr, " +
            "event_display, event_log, event_data, created_at " +
            "FROM events_archive";

    private static final String COUNT_SELECT = "SELECT COUNT(*) FROM events_archive";

    private static final RowMapper<StoredEvent> ROW_MAPPER = (rs, rowNum) -> mapRow(rs);

    private final JdbcTemplate jdbcTemplate;

    public JdbcEventStore(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(Objects.requireNonNull(dataSource));
    }

    JdbcEventStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate);
    }

    @Override
    public Optional<StoredEvent> getByTsid(long tsid) {
        String sql = BASE_SELECT + " WHERE event_tsid = ?";
        List<StoredEvent> results = jdbcTemplate.query(sql, ROW_MAPPER, tsid);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public List<StoredEvent> findByCriteria(EventCriteria criteria) {
        WhereClause where = buildWhereClause(criteria);
        String sortDirection = criteria.getSortOrder() == EventCriteria.SortOrder.ASC ? "ASC" : "DESC";
        String sql = BASE_SELECT + where.sql +
                " ORDER BY event_time " + sortDirection +
                " LIMIT ? OFFSET ?";

        List<Object> params = new ArrayList<>(where.params);
        params.add(criteria.getLimit());
        params.add(criteria.getOffset());

        return jdbcTemplate.query(sql, ROW_MAPPER, params.toArray());
    }

    @Override
    public long count(EventCriteria criteria) {
        WhereClause where = buildWhereClause(criteria);
        String sql = COUNT_SELECT + where.sql;
        Long result = jdbcTemplate.queryForObject(sql, Long.class, where.params.toArray());
        return result != null ? result : 0L;
    }

    private static WhereClause buildWhereClause(EventCriteria criteria) {
        List<String> conditions = new ArrayList<>();
        List<Object> params = new ArrayList<>();

        if (criteria.getUei() != null) {
            conditions.add("event_uei = ?");
            params.add(criteria.getUei());
        }
        if (criteria.getNodeId() != null) {
            conditions.add("node_id = ?");
            params.add(criteria.getNodeId());
        }
        if (criteria.getIpAddress() != null) {
            conditions.add("ip_addr = ?");
            params.add(criteria.getIpAddress());
        }
        if (criteria.getServiceName() != null) {
            conditions.add("service_name = ?");
            params.add(criteria.getServiceName());
        }
        if (criteria.getSeverityGte() != null) {
            conditions.add("event_severity >= ?");
            params.add(criteria.getSeverityGte());
        }
        if (criteria.getSeverityLte() != null) {
            conditions.add("event_severity <= ?");
            params.add(criteria.getSeverityLte());
        }
        if (criteria.getAfterTime() != null) {
            conditions.add("event_time >= ?");
            params.add(Timestamp.from(criteria.getAfterTime()));
        }
        if (criteria.getBeforeTime() != null) {
            conditions.add("event_time <= ?");
            params.add(Timestamp.from(criteria.getBeforeTime()));
        }
        if (criteria.getEventDisplayFilter() != null) {
            conditions.add("event_display = ?");
            params.add(criteria.getEventDisplayFilter());
        }

        String sql = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
        return new WhereClause(sql, params);
    }

    private static StoredEvent mapRow(ResultSet rs) throws SQLException {
        Timestamp eventTime = rs.getTimestamp("event_time");
        Timestamp createdAt = rs.getTimestamp("created_at");
        Long nodeId = rs.getLong("node_id");
        if (rs.wasNull()) {
            nodeId = null;
        }

        return StoredEvent.builder()
                .eventTsid(rs.getLong("event_tsid"))
                .eventUei(rs.getString("event_uei"))
                .eventSource(rs.getString("event_source"))
                .eventSeverity(rs.getInt("event_severity"))
                .eventTime(eventTime != null ? eventTime.toInstant() : Instant.EPOCH)
                .nodeId(nodeId)
                .ipAddress(rs.getString("ip_addr"))
                .serviceName(rs.getString("service_name"))
                .eventLogMsg(rs.getString("event_log_msg"))
                .eventDescr(rs.getString("event_descr"))
                .eventDisplay(rs.getString("event_display"))
                .eventLog(rs.getString("event_log"))
                .eventData(deserializeEventData(rs.getString("event_data")))
                .createdAt(createdAt != null ? createdAt.toInstant() : null)
                .build();
    }

    private static Map<String, String> deserializeEventData(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return OBJECT_MAPPER.readValue(json, MAP_TYPE);
        } catch (JsonProcessingException e) {
            LOG.warn("Failed to deserialize event_data JSON", e);
            return Collections.emptyMap();
        }
    }

    private static class WhereClause {
        final String sql;
        final List<Object> params;

        WhereClause(String sql, List<Object> params) {
            this.sql = sql;
            this.params = params;
        }
    }
}
