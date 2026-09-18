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

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Reads a log through {@code Get-WinEvent}, which is the only way to reach the
 * "Applications and Services" logs that Win32_NTLogEvent does not expose.
 *
 * The script emits one tab-separated line per record with the message base64
 * encoded, so neither newlines in messages nor JSON parsing are needed on the
 * Minion. Oldest first and one more than the cap, so the caller can tell a
 * full page from a truncated one.
 */
public final class EventLogPowerShell {

    /** Windows Level values: 1 Critical, 2 Error, 3 Warning, 4 Information, 5 Verbose, 0 LogAlways. */
    private static final long KEYWORD_AUDIT_SUCCESS = 0x8020000000000000L;
    private static final long KEYWORD_AUDIT_FAILURE = 0x8010000000000000L;

    private EventLogPowerShell() {
    }

    public static String command() {
        return "powershell.exe";
    }

    public static String[] arguments(EventLogQueryDTO query) {
        final String script = script(query);
        final String encoded = Base64.getEncoder().encodeToString(script.getBytes(StandardCharsets.UTF_16LE));
        return new String[] { "-NoProfile", "-NonInteractive", "-OutputFormat", "Text", "-EncodedCommand", encoded };
    }

    static String script(EventLogQueryDTO query) {
        final String log = query.getLogfile().replace("'", "''");
        final int limit = Math.max(1, query.getMaxRecords()) + 1;
        return "$ErrorActionPreference = 'Stop'\n"
                + "try { $events = Get-WinEvent -LogName '" + log + "' -FilterXPath '" + xpath(query) + "' -Oldest -MaxEvents " + limit + " -ErrorAction Stop }"
                + " catch [Exception] { if ($_.Exception.Message -match 'No events were found') { $events = @() } else { throw } }\n"
                + "foreach ($e in $events) {\n"
                + "  $m = if ($e.Message) { [Convert]::ToBase64String([Text.Encoding]::UTF8.GetBytes($e.Message)) } else { '' }\n"
                + "  $t = $e.TimeCreated.ToUniversalTime().ToString('yyyyMMddHHmmss.ffffff') + '+000'\n"
                + "  $k = if ($e.Keywords) { [uint64]$e.Keywords } else { 0 }\n"
                + "  Write-Output ([string]$e.RecordId + \"`t\" + [string]$e.Id + \"`t\" + [string]$e.Level + \"`t\" + $k + \"`t\" + $e.ProviderName + \"`t\" + $t + \"`t\" + $e.MachineName + \"`t\" + $m)\n"
                + "}\n";
    }

    /** The XPath Get-WinEvent filters on server side: cursor or time window, plus the level set. */
    static String xpath(EventLogQueryDTO query) {
        final List<String> terms = new ArrayList<>();
        if (query.getAfterRecordNumber() != null) {
            terms.add("EventRecordID > " + query.getAfterRecordNumber());
        } else if (query.getSinceTime() != null) {
            final long ageMs = Math.max(0L, Instant.now().toEpochMilli() - EventLogWql.fromDmtf(query.getSinceTime()).toEpochMilli());
            terms.add("TimeCreated[timediff(@SystemTime) <= " + ageMs + "]");
        }
        final List<Integer> types = query.getEventTypes();
        if (types != null && !types.isEmpty()) {
            final List<String> alternatives = new ArrayList<>();
            for (Integer type : types) {
                switch (type) {
                    case 1: alternatives.add("Level = 1"); alternatives.add("Level = 2"); break;
                    case 2: alternatives.add("Level = 3"); break;
                    case 3: alternatives.add("Level = 4"); alternatives.add("Level = 0"); alternatives.add("Level = 5"); break;
                    case 4: alternatives.add("band(Keywords, " + KEYWORD_AUDIT_SUCCESS + ")"); break;
                    case 5: alternatives.add("band(Keywords, " + KEYWORD_AUDIT_FAILURE + ")"); break;
                    default: break;
                }
            }
            if (!alternatives.isEmpty()) {
                terms.add("(" + String.join(" or ", alternatives) + ")");
            }
        }
        return terms.isEmpty() ? "*" : "*[System[" + String.join(" and ", terms) + "]]";
    }

    /** Parses the script's output; malformed lines are skipped rather than failing the batch. */
    public static List<EventLogRecordDTO> parse(String stdout, String logfile) {
        final List<EventLogRecordDTO> records = new ArrayList<>();
        if (stdout == null) {
            return records;
        }
        for (String line : stdout.split("\\r?\\n")) {
            if (line.trim().isEmpty()) {
                continue;
            }
            final String[] f = line.split("\\t", -1);
            if (f.length < 8) {
                continue;
            }
            try {
                final EventLogRecordDTO record = new EventLogRecordDTO();
                record.setLogfile(logfile);
                record.setRecordNumber(Long.parseLong(f[0].trim()));
                record.setEventCode(Integer.valueOf(f[1].trim()));
                record.setEventType(eventType(Integer.parseInt(f[2].trim()), Long.parseUnsignedLong(f[3].trim().isEmpty() ? "0" : f[3].trim())));
                record.setSourceName(f[4]);
                record.setTimeGenerated(f[5].trim());
                record.setComputerName(f[6]);
                record.setMessage(f[7].isEmpty() ? "" : new String(Base64.getDecoder().decode(f[7].trim()), StandardCharsets.UTF_8));
                records.add(record);
            } catch (RuntimeException e) {
                continue;
            }
        }
        return records;
    }

    /** Maps the modern Level/Keywords pair onto the classic EventType values the rest of the pipeline uses. */
    static int eventType(int level, long keywords) {
        if ((keywords & KEYWORD_AUDIT_FAILURE) == KEYWORD_AUDIT_FAILURE) {
            return 5;
        }
        if ((keywords & KEYWORD_AUDIT_SUCCESS) == KEYWORD_AUDIT_SUCCESS) {
            return 4;
        }
        switch (level) {
            case 1:
            case 2: return 1;
            case 3: return 2;
            default: return 3;
        }
    }

    static String describe(EventLogQueryDTO query) {
        return query.getLogfile() + " via Get-WinEvent (" + xpath(query) + ", max " + query.getMaxRecords() + ")"
                + (query.getEventTypes().isEmpty() ? "" : " types " + query.getEventTypes().stream().map(String::valueOf).collect(Collectors.joining(",")));
    }
}
