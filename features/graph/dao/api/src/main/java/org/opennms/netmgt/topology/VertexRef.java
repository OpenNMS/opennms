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

package org.opennms.netmgt.topology;


import com.google.common.base.Objects;

// TODO: Patrick discuss with mvr if the mapping from and to a property shouldn't happen through the converter service?
public class VertexRef {
    private final String namespace;
    private final String id;


    public VertexRef(String propertyValue) {
        java.util.Objects.requireNonNull(propertyValue);
        String[] string = propertyValue.split(":");
        this.namespace = string[0];
        this.id = string[1];
    }

    public VertexRef(String namespace, String id) {
        this.namespace = namespace;
        this.id = id;
    }

    /** Copy constructor */
    public VertexRef(VertexRef genericVertexRef){
        this(genericVertexRef.namespace, genericVertexRef.id);
    }

    public String getNamespace() {
        return this.namespace;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VertexRef that = (VertexRef) o;
        return Objects.equal(namespace, that.namespace) &&
                Objects.equal(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(namespace, id);
    }

    @Override
    public String toString() {
        return namespace + ":" + id;
    }
}
