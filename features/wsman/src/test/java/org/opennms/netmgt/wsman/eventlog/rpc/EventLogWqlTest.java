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

import java.time.Instant;
import java.util.List;

import org.junit.Test;

public class EventLogWqlTest {

    @Test
    public void buildsCursorQuery() {
        final EventLogQueryDTO q = new EventLogQueryDTO("System");
        q.setAfterRecordNumber(184233L);
        assertEquals("SELECT " + EventLogWql.COLUMNS + " FROM Win32_NTLogEvent WHERE Logfile = 'System' AND RecordNumber > 184233",
                EventLogWql.forQuery(q));
    }

    @Test
    public void buildsFirstPollQueryWithLevels() {
        final EventLogQueryDTO q = new EventLogQueryDTO("Application");
        q.setSinceTime("20260918120000.000000+000");
        q.setEventTypes(List.of(1, 2));
        assertEquals("SELECT " + EventLogWql.COLUMNS + " FROM Win32_NTLogEvent WHERE Logfile = 'Application'"
                + " AND TimeGenerated >= '20260918120000.000000+000' AND (EventType = 1 OR EventType = 2)",
                EventLogWql.forQuery(q));
    }

    // a cursor wins over the lookback, so a node with history never replays it
    @Test
    public void cursorTakesPrecedenceOverSinceTime() {
        final EventLogQueryDTO q = new EventLogQueryDTO("System");
        q.setAfterRecordNumber(5L);
        q.setSinceTime("20260918120000.000000+000");
        assertEquals("SELECT " + EventLogWql.COLUMNS + " FROM Win32_NTLogEvent WHERE Logfile = 'System' AND RecordNumber > 5", EventLogWql.forQuery(q));
    }

    @Test
    public void escapesQuotesInLogNames() {
        assertEquals("SELECT " + EventLogWql.COLUMNS + " FROM Win32_NTLogEvent WHERE Logfile = 'O''Brien'", EventLogWql.forQuery(new EventLogQueryDTO("O'Brien")));
    }

    @Test
    public void convertsDmtfTimestampsBothWays() {
        final Instant t = Instant.parse("2026-09-18T12:34:56.123456Z");
        assertEquals("20260918123456.123456+000", EventLogWql.toDmtf(t));
        assertEquals(t, EventLogWql.fromDmtf("20260918123456.123456+000"));
        // 08:34 at UTC-4 is 12:34 UTC
        assertEquals(t, EventLogWql.fromDmtf("20260918083456.123456-240"));
        assertEquals(t, EventLogWql.fromDmtf("20260918123456.123456***"));
    }
}
