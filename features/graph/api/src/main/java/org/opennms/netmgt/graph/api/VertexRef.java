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

package org.opennms.netmgt.graph.api;

import java.util.Objects;

import com.google.common.base.MoreObjects;
import com.google.common.base.Strings;

/** immutable composite key that is unique over all graphs / graph containers */
public final class VertexRef {

    private final String namespace;
    private final String id;

    public VertexRef(String namespace, String id) {
        this.namespace = requireNotEmpty(namespace, "namespace");
        this.id = requireNotEmpty(id, "id");
    }

    public String getNamespace(){
        return namespace;
    }

    public String getId(){
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VertexRef vertexRef = (VertexRef) o;
        return Objects.equals(namespace, vertexRef.namespace) &&
                Objects.equals(id, vertexRef.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(namespace, id);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("namespace", namespace)
                .add("id", id)
                .toString();
    }

    private static String requireNotEmpty(String stringToAssert, String attributeName) {
        if (Strings.isNullOrEmpty(stringToAssert)) {
            throw new IllegalArgumentException(String.format("%s cannot be null or empty", attributeName));
        }
        return stringToAssert;
    }
}
