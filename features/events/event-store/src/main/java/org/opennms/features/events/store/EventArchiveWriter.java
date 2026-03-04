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

import java.net.InetAddress;
import java.sql.Timestamp;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.sql.DataSource;

import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.model.IParm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Listens for fault events from the Kafka consumer and writes them
 * into the {@code events_archive} table for query access.
 *
 * <p>This is registered as an {@link EventListener} on the
 * {@code KafkaFaultEventConsumer}. Inserts use {@code ON CONFLICT DO NOTHING}
 * for idempotency since Kafka provides at-least-once delivery.</p>
 */
public class EventArchiveWriter implements EventListener {

    private static final Logger LOG = LoggerFactory.getLogger(EventArchiveWriter.class);

    private static final String INSERT_SQL =
            "INSERT INTO events_archive " +
            "(event_tsid, event_uei, event_source, event_severity, event_time, " +
            " node_id, ip_addr, service_name, ifindex, event_log_msg, event_descr, " +
            " event_display, event_log, event_data) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) " +
            "ON CONFLICT (event_tsid) DO NOTHING";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final Map<String, Integer> SEVERITY_MAP = Map.of(
            "Indeterminate", 1,
            "Cleared", 2,
            "Normal", 3,
            "Warning", 4,
            "Minor", 5,
            "Major", 6,
            "Critical", 7
    );

    private final JdbcTemplate jdbcTemplate;

    public EventArchiveWriter(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(Objects.requireNonNull(dataSource));
    }

    EventArchiveWriter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = Objects.requireNonNull(jdbcTemplate);
    }

    @Override
    public String getName() {
        return "EventArchiveWriter";
    }

    @Override
    public void onEvent(IEvent event) {
        if (event == null) {
            return;
        }

        Long tsid = event.getDbid();
        if (tsid == null) {
            LOG.warn("Skipping event without TSID: uei={}", event.getUei());
            return;
        }

        try {
            jdbcTemplate.update(INSERT_SQL,
                    tsid,
                    event.getUei(),
                    event.getSource(),
                    parseSeverity(event.getSeverity()),
                    event.getTime() != null ? new Timestamp(event.getTime().getTime()) : null,
                    event.getNodeid(),
                    formatIpAddress(event.getInterfaceAddress()),
                    event.getService(),
                    event.getIfIndex(),
                    event.getLogmsg() != null ? event.getLogmsg().getContent() : null,
                    event.getDescr(),
                    getEventDisplay(event),
                    getEventLog(event),
                    serializeParameters(event.getParmCollection()));
        } catch (Exception e) {
            LOG.error("Failed to archive event tsid={} uei={}", tsid, event.getUei(), e);
        }
    }

    static int parseSeverity(String severity) {
        if (severity == null) {
            return 1; // Indeterminate
        }
        Integer mapped = SEVERITY_MAP.get(severity);
        if (mapped != null) {
            return mapped;
        }
        try {
            return Integer.parseInt(severity);
        } catch (NumberFormatException e) {
            return 1; // Indeterminate
        }
    }

    private static String formatIpAddress(InetAddress address) {
        if (address == null) {
            return null;
        }
        return address.getHostAddress();
    }

    private static String getEventDisplay(IEvent event) {
        if (event.getLogmsg() != null && event.getLogmsg().getContent() != null) {
            return "Y";
        }
        return "N";
    }

    private static String getEventLog(IEvent event) {
        if (event.getLogmsg() != null && event.getLogmsg().getContent() != null) {
            return "Y";
        }
        return "N";
    }

    static String serializeParameters(List<IParm> parms) {
        if (parms == null || parms.isEmpty()) {
            return null;
        }
        Map<String, String> map = new LinkedHashMap<>();
        for (IParm parm : parms) {
            if (parm.getParmName() != null && parm.getValue() != null) {
                map.put(parm.getParmName(), parm.getValue().getContent());
            }
        }
        if (map.isEmpty()) {
            return null;
        }
        try {
            return OBJECT_MAPPER.writeValueAsString(map);
        } catch (JsonProcessingException e) {
            LOG.warn("Failed to serialize event parameters", e);
            return null;
        }
    }
}
