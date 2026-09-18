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

import java.util.ArrayList;
import java.util.List;

import org.opennms.netmgt.model.OnmsSeverity;

/** Win32_NTLogEvent.EventType values and the OpenNMS severity each one maps to. */
public enum EventLogLevel {
    ERROR(1, OnmsSeverity.MAJOR),
    WARNING(2, OnmsSeverity.WARNING),
    INFORMATION(3, OnmsSeverity.NORMAL),
    AUDIT_SUCCESS(4, OnmsSeverity.NORMAL),
    AUDIT_FAILURE(5, OnmsSeverity.MINOR);

    private final int eventType;
    private final OnmsSeverity severity;

    EventLogLevel(int eventType, OnmsSeverity severity) {
        this.eventType = eventType;
        this.severity = severity;
    }

    public int getEventType() {
        return eventType;
    }

    public OnmsSeverity getSeverity() {
        return severity;
    }

    public String getLabel() {
        switch (this) {
            case AUDIT_SUCCESS: return "Audit Success";
            case AUDIT_FAILURE: return "Audit Failure";
            default: return name().charAt(0) + name().substring(1).toLowerCase();
        }
    }

    public static EventLogLevel forEventType(Integer eventType) {
        if (eventType != null) {
            for (EventLogLevel level : values()) {
                if (level.eventType == eventType) {
                    return level;
                }
            }
        }
        return null;
    }

    /** Accepts the names used in the configuration: {@code Error,Warning,Information,AuditSuccess,AuditFailure}. */
    public static List<Integer> parseEventTypes(String levels) {
        final List<Integer> types = new ArrayList<>();
        if (levels == null || levels.trim().isEmpty()) {
            return types;
        }
        for (String token : levels.split(",")) {
            final String key = token.trim().replace(" ", "").replace("_", "").toUpperCase();
            if (key.isEmpty()) {
                continue;
            }
            EventLogLevel match = null;
            for (EventLogLevel level : values()) {
                if (level.name().replace("_", "").equals(key)) {
                    match = level;
                }
            }
            if (match == null) {
                throw new IllegalArgumentException("Unknown event log level '" + token.trim() + "'");
            }
            types.add(match.eventType);
        }
        return types;
    }
}
