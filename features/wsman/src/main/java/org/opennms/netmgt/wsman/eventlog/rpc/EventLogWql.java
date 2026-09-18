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

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Builds the WQL for one log and converts the DMTF timestamps WMI uses
 * ({@code yyyyMMddHHmmss.ffffff+UTC-offset-in-minutes}).
 */
public final class EventLogWql {

    public static final String CLASS_NAME = "Win32_NTLogEvent";

    public static final String COLUMNS = "RecordNumber,Logfile,EventCode,EventType,SourceName,TimeGenerated,ComputerName,Message,InsertionStrings";

    private static final DateTimeFormatter DMTF = DateTimeFormatter.ofPattern("yyyyMMddHHmmss.SSSSSS");

    private EventLogWql() {
    }

    public static String forQuery(EventLogQueryDTO query) {
        final StringBuilder sb = new StringBuilder("SELECT ").append(COLUMNS)
                .append(" FROM ").append(CLASS_NAME)
                .append(" WHERE Logfile = '").append(escape(query.getLogfile())).append("'");
        if (query.getAfterRecordNumber() != null) {
            sb.append(" AND RecordNumber > ").append(query.getAfterRecordNumber());
        } else if (query.getSinceTime() != null) {
            sb.append(" AND TimeGenerated >= '").append(escape(query.getSinceTime())).append("'");
        }
        final List<Integer> types = query.getEventTypes();
        if (types != null && !types.isEmpty()) {
            sb.append(" AND (")
              .append(types.stream().map(t -> "EventType = " + t).collect(Collectors.joining(" OR ")))
              .append(")");
        }
        return sb.toString();
    }

    /** WQL string literals escape a single quote by doubling it. */
    public static String escape(String value) {
        return value == null ? "" : value.replace("'", "''");
    }

    public static String toDmtf(Instant instant) {
        return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC).format(DMTF) + "+000";
    }

    /**
     * Parses a DMTF timestamp; the trailing offset is minutes east of UTC and may be
     * {@code ***} on agents that do not report one, which is read as UTC.
     */
    public static Instant fromDmtf(String dmtf) {
        if (dmtf == null || dmtf.length() < 21) {
            throw new IllegalArgumentException("Not a DMTF timestamp: " + dmtf);
        }
        final String base = dmtf.substring(0, 21);
        final String offset = dmtf.substring(21).trim();
        int offsetMinutes = 0;
        if (!offset.isEmpty() && !offset.contains("*")) {
            offsetMinutes = Integer.parseInt(offset.startsWith("+") ? offset.substring(1) : offset);
        }
        return ZonedDateTime.parse(base, DMTF.withZone(ZoneOffset.UTC)).toInstant().minusSeconds(offsetMinutes * 60L);
    }
}
