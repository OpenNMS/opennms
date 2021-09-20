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

package org.opennms.netmgt.graph.api.focus;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.netmgt.graph.api.VertexRef;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

public class Focus {

    private final String id;
    private final List<VertexRef> vertexRefs;

    public Focus(final String id) {
        this(id, Lists.newArrayList());
    }

    public Focus(final String id, final List<VertexRef> vertexRefs) {
        this.id = Objects.requireNonNull(id);
        this.vertexRefs = Objects.requireNonNull(vertexRefs);
    }

    public String getId() {
        return id;
    }

    public List<VertexRef> getVertexRefs() {
        return ImmutableList.copyOf(vertexRefs);
    }

    public List<String> getVertexIds() {
        return vertexRefs.stream().map(v -> v.getId()).collect(Collectors.toList());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Focus focus = (Focus) o;
        return Objects.equals(id, focus.id)
                && Objects.equals(vertexRefs, focus.vertexRefs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, vertexRefs);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("vertexRefs", vertexRefs)
                .toString();
    }
}
