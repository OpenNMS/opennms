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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.net.InetAddress;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Test;
import org.opennms.netmgt.config.wsman.eventlog.EventMapping;
import org.opennms.netmgt.wsman.eventlog.rpc.EventLogRecordDTO;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;

public class EventLogEventMapperTest {

    private final EventLogEventMapper mapper = new EventLogEventMapper("WsManEventLogd");
    private final EventLogTarget target = new EventLogTarget(42, "win-12", addr("10.0.0.12"), "Default");

    @Test
    public void mapsARecordToAnEventWithParms() {
        final EventLogRecordDTO record = record("System", 11, 7036, 3, "Service Control Manager", "The Spooler service entered the running state.");
        record.setInsertionStrings(List.of("Spooler", "running"));
        final Event event = mapper.toEvent(target, record, List.of());

        assertEquals("uei.opennms.org/wsman/eventlog/System", event.getUei());
        assertEquals(Long.valueOf(42), event.getNodeid());
        assertEquals("10.0.0.12", event.getInterface());
        assertEquals("Normal", event.getSeverity());
        assertEquals("WsManEventLogd", event.getSource());
        assertEquals(java.time.Instant.parse("2026-09-21T10:00:00Z").toEpochMilli(), event.getTime().getTime());
        final Map<String, String> parms = parms(event);
        assertEquals("System", parms.get("logfile"));
        assertEquals("7036", parms.get("eventId"));
        assertEquals("3", parms.get("eventType"));
        assertEquals("Information", parms.get("level"));
        assertEquals("Service Control Manager", parms.get("sourceName"));
        assertEquals("11", parms.get("recordNumber"));
        assertEquals("WIN-12", parms.get("computerName"));
        assertEquals("The Spooler service entered the running state.", parms.get("message"));
        assertEquals("Spooler", parms.get("insertionString1"));
        assertEquals("running", parms.get("insertionString2"));
    }

    @Test
    public void severityFollowsTheLevel() {
        assertEquals("Major", mapper.toEvent(target, record("System", 1, 1, 1, "s", "m"), List.of()).getSeverity());
        assertEquals("Warning", mapper.toEvent(target, record("System", 1, 1, 2, "s", "m"), List.of()).getSeverity());
        assertEquals("Normal", mapper.toEvent(target, record("Security", 1, 1, 4, "s", "m"), List.of()).getSeverity());
        assertEquals("Minor", mapper.toEvent(target, record("Security", 1, 1, 5, "s", "m"), List.of()).getSeverity());
        assertEquals("Indeterminate", mapper.toEvent(target, record("Security", 1, 1, 9, "s", "m"), List.of()).getSeverity());
    }

    @Test
    public void unknownLogsShareTheOtherUei() {
        assertEquals("uei.opennms.org/wsman/eventlog/other", mapper.toEvent(target, record("Windows PowerShell", 1, 400, 3, "s", "m"), List.of()).getUei());
        assertEquals("uei.opennms.org/wsman/eventlog/Application", mapper.toEvent(target, record("application", 1, 400, 3, "s", "m"), List.of()).getUei());
    }

    @Test
    public void mappingOverridesUeiAndSeverity() {
        final EventMapping shutdown = mapping("System", null, 6008, "uei.opennms.org/wsman/eventlog/unexpectedShutdown", "Major");
        final EventMapping bySource = mapping(null, "Application Error", 1000, "uei.opennms.org/x/appCrash", null);
        final List<EventMapping> mappings = List.of(shutdown, bySource);

        final Event mapped = mapper.toEvent(target, record("System", 1, 6008, 3, "EventLog", "m"), mappings);
        assertEquals("uei.opennms.org/wsman/eventlog/unexpectedShutdown", mapped.getUei());
        assertEquals("Major", mapped.getSeverity());

        // same id in another log does not match a log-scoped mapping
        assertEquals("uei.opennms.org/wsman/eventlog/Application", mapper.toEvent(target, record("Application", 1, 6008, 3, "EventLog", "m"), mappings).getUei());

        // a source-scoped mapping without a severity keeps the level's severity
        final Event crash = mapper.toEvent(target, record("Application", 1, 1000, 1, "Application Error", "m"), mappings);
        assertEquals("uei.opennms.org/x/appCrash", crash.getUei());
        assertEquals("Major", crash.getSeverity());
        assertEquals("uei.opennms.org/wsman/eventlog/Application", mapper.toEvent(target, record("Application", 1, 1000, 1, "Other", "m"), mappings).getUei());
    }

    @Test
    public void truncatesLongMessages() {
        final String big = "x".repeat(EventLogEventMapper.MAX_MESSAGE_LENGTH + 100);
        assertEquals(EventLogEventMapper.MAX_MESSAGE_LENGTH, parms(mapper.toEvent(target, record("System", 1, 1, 1, "s", big), List.of())).get("message").length());
    }

    @Test
    public void truncatedEventNamesTheLogAndCap() {
        final Event event = mapper.truncatedEvent(target, "Security", 500);
        assertEquals(EventLogEventMapper.UEI_TRUNCATED, event.getUei());
        assertEquals("Security", parms(event).get("logfile"));
        assertEquals("500", parms(event).get("maxRecords"));
        assertNull(event.getSeverity());
    }

    static EventLogRecordDTO record(String logfile, long recordNumber, int eventCode, int eventType, String source, String message) {
        final EventLogRecordDTO r = new EventLogRecordDTO();
        r.setLogfile(logfile);
        r.setRecordNumber(recordNumber);
        r.setEventCode(eventCode);
        r.setEventType(eventType);
        r.setSourceName(source);
        r.setTimeGenerated("20260921100000.000000+000");
        r.setComputerName("WIN-12");
        r.setMessage(message);
        return r;
    }

    static EventMapping mapping(String logfile, String source, int id, String uei, String severity) {
        final EventMapping m = new EventMapping();
        m.setLogfile(logfile);
        m.setSource(source);
        m.setEventId(id);
        m.setUei(uei);
        m.setSeverity(severity);
        return m;
    }

    static Map<String, String> parms(Event event) {
        return event.getParmCollection().stream().collect(Collectors.toMap(Parm::getParmName, p -> p.getValue().getContent()));
    }

    static InetAddress addr(String ip) {
        try {
            return InetAddress.getByName(ip);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
