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

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * Represents a Collection to be persisted as Json string.
 * This class is the Java representation of the Json string.
 * @see org.opennms.netmgt.graph.persistence.converter.CollectionConverter
 */
public class PersistedCollection {
    private Class type;
    private List<PersistedCollectionEntry> entries;

    public Class getType() {
        return type;
    }

    public void setType(Class type) {
        this.type = type;
    }

    public List<PersistedCollectionEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<PersistedCollectionEntry> entries) {
        this.entries = entries;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PersistedCollection that = (PersistedCollection) o;
        return Objects.equal(type, that.type) &&
                Objects.equal(entries, that.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type, entries);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("type", type)
                .add("entries", entries)
                .toString();
    }
}
