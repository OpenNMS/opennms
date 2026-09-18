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
package org.opennms.netmgt.wsman.eventlog.rpc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

import org.junit.Test;

public class EventLogPowerShellTest {

    @Test
    public void xpathUsesTheCursorAndLevels() {
        final EventLogQueryDTO q = new EventLogQueryDTO("Microsoft-Windows-TaskScheduler/Operational");
        q.setAfterRecordNumber(500L);
        q.setEventTypes(List.of(1, 2));
        assertEquals("*[System[EventRecordID > 500 and (Level = 1 or Level = 2 or Level = 3)]]", EventLogPowerShell.xpath(q));
    }

    @Test
    public void xpathUsesATimeWindowOnTheFirstPoll() {
        final EventLogQueryDTO q = new EventLogQueryDTO("System");
        q.setSinceTime(EventLogWql.toDmtf(java.time.Instant.now().minusSeconds(3600)));
        final String xpath = EventLogPowerShell.xpath(q);
        assertTrue(xpath, xpath.startsWith("*[System[TimeCreated[timediff(@SystemTime) <= 36"));
    }

    @Test
    public void xpathMapsAuditLevelsToKeywords() {
        final EventLogQueryDTO q = new EventLogQueryDTO("Security");
        q.setEventTypes(List.of(5));
        assertEquals("*[System[(band(Keywords, -9218868437227405312))]]", EventLogPowerShell.xpath(q));
        assertEquals("*", EventLogPowerShell.xpath(new EventLogQueryDTO("System")));
    }

    @Test
    public void scriptIsHandedToPowerShellEncoded() {
        final EventLogQueryDTO q = new EventLogQueryDTO("Microsoft-Windows-TaskScheduler/Operational");
        q.setMaxRecords(200);
        final String[] args = EventLogPowerShell.arguments(q);
        assertEquals("powershell.exe", EventLogPowerShell.command());
        assertEquals("-EncodedCommand", args[4]);
        final String script = new String(Base64.getDecoder().decode(args[5]), StandardCharsets.UTF_16LE);
        assertTrue(script, script.contains("Get-WinEvent -LogName 'Microsoft-Windows-TaskScheduler/Operational' -FilterXPath '*' -Oldest -MaxEvents 201"));
        assertTrue(script, script.contains("ToBase64String"));
    }

    @Test
    public void parsesTabSeparatedLinesAndDecodesMessages() {
        final String message = "Task Scheduler failed to start\r\n\"Backup\" task.";
        final String b64 = Base64.getEncoder().encodeToString(message.getBytes(StandardCharsets.UTF_8));
        final String stdout = "12\t101\t2\t0\tMicrosoft-Windows-TaskScheduler\t20260918120000.000000+000\tWIN-12\t" + b64 + "\r\n"
                + "13\t4625\t0\t9227875636482146304\tMicrosoft-Windows-Security-Auditing\t20260918120001.000000+000\tWIN-12\t\r\n"
                + "garbage line\r\n";
        final List<EventLogRecordDTO> records = EventLogPowerShell.parse(stdout, "Security");

        assertEquals(2, records.size());
        assertEquals(12L, records.get(0).getRecordNumber());
        assertEquals(Integer.valueOf(101), records.get(0).getEventCode());
        assertEquals(Integer.valueOf(1), records.get(0).getEventType());
        assertEquals("Microsoft-Windows-TaskScheduler", records.get(0).getSourceName());
        assertEquals(message, records.get(0).getMessage());
        assertEquals("Security", records.get(0).getLogfile());
        // an audit-failure keyword wins over the level
        assertEquals(Integer.valueOf(5), records.get(1).getEventType());
        assertEquals("", records.get(1).getMessage());
    }

    @Test
    public void mapsModernLevelsOntoClassicEventTypes() {
        assertEquals(1, EventLogPowerShell.eventType(1, 0));
        assertEquals(1, EventLogPowerShell.eventType(2, 0));
        assertEquals(2, EventLogPowerShell.eventType(3, 0));
        assertEquals(3, EventLogPowerShell.eventType(4, 0));
        assertEquals(3, EventLogPowerShell.eventType(0, 0));
        assertEquals(4, EventLogPowerShell.eventType(0, 0x8020000000000000L));
        assertEquals(5, EventLogPowerShell.eventType(0, 0x8010000000000000L));
    }
}
