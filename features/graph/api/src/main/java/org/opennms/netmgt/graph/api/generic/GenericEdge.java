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

package org.opennms.netmgt.graph.api.generic;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.opennms.netmgt.graph.api.Edge;
import org.opennms.netmgt.graph.api.VertexRef;

import com.google.common.base.MoreObjects;

public class GenericEdge extends GenericElement implements Edge {

    private final GenericVertexRef source;
    private final GenericVertexRef target;

    public GenericEdge(String namespace, GenericVertexRef source, GenericVertexRef target) {
        this(source, target, new MapBuilder<String, Object>()
                .withProperty(GenericProperties.NAMESPACE, namespace)
                .build());
    }

    public GenericEdge(String namespace, GenericVertexRef source, GenericVertexRef target, Map<String, Object> properties) {
        this(source, target, new MapBuilder<String, Object>()
                .withProperties(properties)
                .withProperty(GenericProperties.NAMESPACE, namespace)
                .build());
    }

    private GenericEdge(GenericVertexRef source, GenericVertexRef target, Map<String, Object> properties) {
        super(properties);
        this.source = Objects.requireNonNull(source);
        this.target = Objects.requireNonNull(target);
        if(!source.getNamespace().equals(getNamespace()) && !target.getNamespace().equals(getNamespace())) {
            throw new IllegalArgumentException(
                    String.format("Neither the namespace of the source VertexRef(namespace=%s) nor the target VertexRef(%s) matches our namespace=%s",
                            source.getNamespace(), target.getNamespace(), getNamespace()));
        }
        this.setId(source.getNamespace() + ":" + source.getId() + "->" + target.getNamespace() + ":" + target.getId());
    }

    /** Copy constructor with same namespace */
    public GenericEdge(GenericEdge copyMe) {
        this(copyMe, copyMe.getNamespace());
    }

    /** Copy constructor with new namespace */
    public GenericEdge(GenericEdge copyMe, String newNamespace) {
        this(new GenericVertexRef(newNamespace, copyMe.source.getId()),
                new GenericVertexRef(newNamespace, copyMe.target.getId()),
                new MapBuilder<String, Object>()
                .withProperties(copyMe.getProperties())
                .withProperty(GenericProperties.NAMESPACE, newNamespace)
                .build());
    }

    public Map<String, Object> getProperties() {
        return new HashMap<>(properties);
    }

    @Override
    public VertexRef getSource() {
        return source;
    }

    @Override
    public VertexRef getTarget() {
        return target;
    }

    @Override
    public GenericEdge asGenericEdge() {
        return this;
    }

    @Override
    // TODO MVR remove again after endless recursion is fixed
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("source", source)
                .add("target", target)
                .add("properties", properties)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GenericEdge that = (GenericEdge) o;
        return Objects.equals(source, that.source) &&
                Objects.equals(target, that.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), source, target);
    }
}
