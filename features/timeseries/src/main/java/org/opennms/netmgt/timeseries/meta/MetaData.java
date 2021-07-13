/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.timeseries.meta;

import java.util.Objects;
import java.util.StringJoiner;

public class MetaData {
    private final String resourceId;
    private final String name;
    private final String value;

    public MetaData(final String resourceId, final String name, final String value) {
        this.resourceId = Objects.requireNonNull(resourceId);
        this.name = Objects.requireNonNull(name);
        this.value = value;
    }

    public String getResourceId() {
        return this.resourceId;
    }

    public String getName() {
        return this.name;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaData metaData = (MetaData) o;
        return Objects.equals(resourceId, metaData.resourceId) &&
                Objects.equals(name, metaData.name) &&
                Objects.equals(value, metaData.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceId, name, value);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", MetaData.class.getSimpleName() + "[", "]")
                .add("resourceId='" + resourceId + "'")
                .add("name='" + name + "'")
                .add("value='" + value + "'")
                .toString();
    }
}
