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
package org.opennms.netmgt.syslogd;

public enum SyslogSeverity {
    EMERGENCY(0, "system is unusable"),
    ALERT(1, "action must be taken immediately"),
    CRITICAL(2, "critical conditions"),
    ERROR(3, "error conditions"),
    WARNING(4, "warning conditions"),
    NOTICE(5, "normal but significant condition"),
    INFORMATIONAL(6, "informational messages"),
    DEBUG(7, "debug-level messages"),
    ALL(8, "all levels"),
    UNKNOWN(99, "unknown");

    public static final int MASK = 0x0007;

    private final int m_severity;
    private final String m_name;
    private final String m_description;

    SyslogSeverity(final int severity, final String description) {
        m_severity = severity;
        m_name = (name().substring(0, 1) + name().substring(1).toLowerCase()).intern();
        m_description = description.intern();
    }
    
    public int getSeverityNumber() {
        return m_severity;
    }
    
    public String getDescription() {
        return m_description;
    }

    public int getPriority(final SyslogFacility facility) {
        if (facility == null) {
            return 0 | m_severity;
        }
        return (facility.getFacilityNumber() & SyslogFacility.MASK) | m_severity;
    }
    
    @Override
    public String toString() {
        return m_name;
    }

    public static SyslogSeverity getSeverity(final int severity) {
        final SyslogSeverity[] severities = SyslogSeverity.values();
        if (severities.length < severity) {
            return SyslogSeverity.UNKNOWN;
        }
        return severities[severity];
    }
    
    public static SyslogSeverity getSeverityForCode(final int code) {
        return getSeverity(code & MASK);
    }
}
