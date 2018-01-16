/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.filter.api;

import java.util.Objects;

public class SnmpInterfaceIdFilter implements Filter {

    private final int snmpInterfaceId;

    public SnmpInterfaceIdFilter(int snmpInterfaceId) {
        this.snmpInterfaceId = snmpInterfaceId;
    }

    public int getSnmpInterfaceId() {
        return snmpInterfaceId;
    }

    @Override
    public <T> T visit(FilterVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SnmpInterfaceIdFilter that = (SnmpInterfaceIdFilter) o;
        return snmpInterfaceId == that.snmpInterfaceId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(snmpInterfaceId);
    }
}
