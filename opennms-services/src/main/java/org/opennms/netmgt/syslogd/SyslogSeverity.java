/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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
            return null;
        }
        return severities[severity];
    }
    
    public static SyslogSeverity getSeverityForCode(final int code) {
        return getSeverity(code & MASK);
    }
}
