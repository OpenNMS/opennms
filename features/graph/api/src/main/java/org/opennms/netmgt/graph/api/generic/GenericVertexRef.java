/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
<<<<<<< HEAD
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
=======
 * Copyright (C) 2019 The OpenNMS Group, Inc.
>>>>>>> HZN-1521: Edges should allow to contain vertices of different namespaces: introduce VertexRef
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

package org.opennms.netmgt.graph.api.generic;

import org.opennms.netmgt.graph.api.VertexRef;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * An immutable composite key to reference a vertex.
 */
public class GenericVertexRef implements VertexRef {

    private final String namespace;
    private final String id;

    public GenericVertexRef(String namespace, String id) {
        this.namespace = namespace;
        this.id = id;
    }

    @Override
    public String getNamespace() {
        return this.namespace;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GenericVertexRef that = (GenericVertexRef) o;
        return Objects.equal(namespace, that.namespace) &&
                Objects.equal(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(namespace, id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("namespace", namespace)
                .add("id", id)
                .toString();
    }
}
