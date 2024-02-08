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
