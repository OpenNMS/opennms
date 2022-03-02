/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.persistence.converter.collection;

import java.util.List;
import java.util.Objects;

import com.google.common.base.MoreObjects;

/**
 * Represents a {@link java.util.Collection} to be persisted as Json string.
 * This class contains the serialized objects of the original Java object,
 * as well as the collection type, to later de-serialize accordingly.
 *
 * @see org.opennms.netmgt.graph.persistence.converter.CollectionConverter
 */
public class SerializedCollection {
    // The type of the collection
    private Class type;
    // Each entry of the original collection, but serialized
    private List<SerializedCollectionEntry> entries;

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public List<SerializedCollectionEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<SerializedCollectionEntry> entries) {
        this.entries = entries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SerializedCollection that = (SerializedCollection) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(entries, that.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, entries);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .add("entries", entries)
                .toString();
    }
}
