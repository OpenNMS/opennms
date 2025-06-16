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
