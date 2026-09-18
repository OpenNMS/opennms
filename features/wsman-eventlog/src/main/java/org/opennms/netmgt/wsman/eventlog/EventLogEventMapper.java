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
package org.opennms.netmgt.wsman.eventlog;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.config.wsman.eventlog.EventMapping;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.wsman.eventlog.rpc.EventLogRecordDTO;
import org.opennms.netmgt.wsman.eventlog.rpc.EventLogWql;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Builds the OpenNMS event for one record, applying the package's mapping rules. */
public class EventLogEventMapper {

    private static final Logger LOG = LoggerFactory.getLogger(EventLogEventMapper.class);

    public static final String UEI_PREFIX = "uei.opennms.org/wsman/eventlog/";
    public static final String UEI_OTHER = UEI_PREFIX + "other";
    public static final String UEI_TRUNCATED = UEI_PREFIX + "pollTruncated";

    /** Logs with a shipped event definition of their own; anything else uses UEI_OTHER. */
    private static final List<String> CLASSIC_LOGS = List.of("Application", "System", "Security");

    /** Messages longer than this are cut so one chatty source cannot bloat the events table. */
    public static final int MAX_MESSAGE_LENGTH = 4000;

    private final String source;

    public EventLogEventMapper(String source) {
        this.source = Objects.requireNonNull(source);
    }

    public Event toEvent(EventLogTarget target, EventLogRecordDTO record, List<EventMapping> mappings) {
        final EventMapping mapping = findMapping(record, mappings);
        final EventLogLevel level = EventLogLevel.forEventType(record.getEventType());
        final String uei = mapping != null ? mapping.getUei() : defaultUei(record.getLogfile());

        final EventBuilder builder = new EventBuilder(uei, source);
        builder.setNodeid(target.getNodeId());
        builder.setInterface(target.getAddress());
        builder.setTime(eventTime(record));
        builder.setSeverity(severity(mapping, level).getLabel());
        builder.addParam("logfile", nullSafe(record.getLogfile()));
        builder.addParam("eventId", record.getEventCode() == null ? "" : Integer.toString(record.getEventCode()));
        builder.addParam("eventType", record.getEventType() == null ? "" : Integer.toString(record.getEventType()));
        builder.addParam("level", level == null ? "Unknown" : level.getLabel());
        builder.addParam("sourceName", nullSafe(record.getSourceName()));
        builder.addParam("recordNumber", Long.toString(record.getRecordNumber()));
        builder.addParam("computerName", nullSafe(record.getComputerName()));
        builder.addParam("timeGenerated", nullSafe(record.getTimeGenerated()));
        builder.addParam("message", truncate(record.getMessage()));
        int i = 1;
        for (String insertionString : record.getInsertionStrings()) {
            builder.addParam("insertionString" + i++, truncate(insertionString));
        }
        return builder.getEvent();
    }

    public Event truncatedEvent(EventLogTarget target, String logfile, int maxRecords) {
        final EventBuilder builder = new EventBuilder(UEI_TRUNCATED, source);
        builder.setNodeid(target.getNodeId());
        builder.setInterface(target.getAddress());
        builder.addParam("logfile", logfile);
        builder.addParam("maxRecords", Integer.toString(maxRecords));
        return builder.getEvent();
    }

    static EventMapping findMapping(EventLogRecordDTO record, List<EventMapping> mappings) {
        if (record.getEventCode() == null || mappings == null) {
            return null;
        }
        for (EventMapping mapping : mappings) {
            if (mapping.getEventId() != record.getEventCode()) {
                continue;
            }
            if (mapping.getLogfile() != null && !mapping.getLogfile().equalsIgnoreCase(record.getLogfile())) {
                continue;
            }
            if (mapping.getSource() != null && !mapping.getSource().equalsIgnoreCase(record.getSourceName())) {
                continue;
            }
            return mapping;
        }
        return null;
    }

    static String defaultUei(String logfile) {
        for (String classic : CLASSIC_LOGS) {
            if (classic.equalsIgnoreCase(logfile)) {
                return UEI_PREFIX + classic;
            }
        }
        return UEI_OTHER;
    }

    static OnmsSeverity severity(EventMapping mapping, EventLogLevel level) {
        if (mapping != null && mapping.getSeverity() != null && !mapping.getSeverity().trim().isEmpty()) {
            return OnmsSeverity.get(mapping.getSeverity().trim());
        }
        return level == null ? OnmsSeverity.INDETERMINATE : level.getSeverity();
    }

    static Date eventTime(EventLogRecordDTO record) {
        if (record.getTimeGenerated() != null) {
            try {
                return Date.from(EventLogWql.fromDmtf(record.getTimeGenerated()));
            } catch (RuntimeException e) {
                LOG.debug("Unparseable TimeGenerated '{}' on {}", record.getTimeGenerated(), record);
            }
        }
        return Date.from(Instant.now());
    }

    private static String truncate(String value) {
        if (value == null) {
            return "";
        }
        return value.length() <= MAX_MESSAGE_LENGTH ? value : value.substring(0, MAX_MESSAGE_LENGTH);
    }

    private static String nullSafe(String value) {
        return value == null ? "" : value;
    }
}
