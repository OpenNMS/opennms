/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.snmp.proxy;

import java.util.List;
import java.util.Objects;

import org.opennms.netmgt.snmp.SnmpResult;

public class WalkResponse {

    private final List<SnmpResult> results;
    private final String correlationId;

    public WalkResponse(List<SnmpResult> results) {
        this(results, null);
    }

    public WalkResponse(List<SnmpResult> results, String correlationId) {
        this.results = Objects.requireNonNull(results);
        this.correlationId = correlationId;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public List<SnmpResult> getResults() {
        return results;
    }

    @Override
    public int hashCode() {
        return Objects.hash(results, correlationId);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final WalkResponse other = (WalkResponse) obj;
        return Objects.equals(this.results, other.results)
                && Objects.equals(this.correlationId, other.correlationId);
    }
}
