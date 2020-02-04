/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.graph.api.info;

import java.util.Objects;

import org.opennms.netmgt.model.OnmsSeverity;

/**
 * Graph API internal Severity enum.
 */
public enum Severity {
    Unknown,
    Normal,
    Warning,
    Minor,
    Major,
    Critical;

    public static Severity createFrom(final OnmsSeverity severity) {
        Objects.requireNonNull(severity);
        switch(severity) {
            case INDETERMINATE: return Severity.Unknown;
            case NORMAL: return Severity.Normal;
            case WARNING: return Severity.Warning;
            case MINOR: return Severity.Minor;
            case MAJOR: return Severity.Major;
            case CRITICAL: return Severity.Critical;
            default:
                throw new IllegalStateException("Cannot convert OnmsSeverity to Severity due to unknown severity '" + severity.name() + "'");
        }
    }

    public boolean isLessThan(Severity other) {
        Objects.requireNonNull(other);
        return ordinal() < other.ordinal();
    }

    public boolean isEqual(Severity other) {
        Objects.requireNonNull(other);
        return ordinal() == other.ordinal();
    }
}
