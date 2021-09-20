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

import java.util.Map;
import java.util.Objects;

import org.opennms.netmgt.graph.api.Edge;
import org.opennms.netmgt.graph.api.VertexRef;

import com.google.common.base.MoreObjects;

public final class GenericEdge extends GenericElement implements Edge {

    private final VertexRef source;
    private final VertexRef target;

    private GenericEdge(VertexRef source, VertexRef target, Map<String, Object> properties) {
        super(new MapBuilder<String, Object>()
                .withProperties(properties)
                .withProperty(GenericProperties.ID, properties.getOrDefault(GenericProperties.ID, source.getNamespace() + ":" + source.getId() + "->" + target.getNamespace() + ":" + target.getId()))
                .build());
        this.source = Objects.requireNonNull(source);
        this.target = Objects.requireNonNull(target);
        if (!source.getNamespace().equals(getNamespace()) && !target.getNamespace().equals(getNamespace())) {
            throw new IllegalArgumentException (
                    String.format("Neither the namespace of the source VertexRef(namespace=%s) nor the target VertexRef(%s) matches our namespace=%s",
                            source.getNamespace(), target.getNamespace(), getNamespace()));
        }
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
    
    public static GenericEdgeBuilder builder() {
        return new GenericEdgeBuilder();
    }
    
    public final static class GenericEdgeBuilder extends GenericElementBuilder<GenericEdgeBuilder> {
        
        private VertexRef source;
        private VertexRef target;
        
        private GenericEdgeBuilder() {}

        public GenericEdgeBuilder edge(GenericEdge edge) {
            Objects.requireNonNull(edge);
            properties(edge.getProperties());
            source(edge.getSource());
            target(edge.getTarget());
            return this;
        }

        public GenericEdgeBuilder source(String namespace, String id) {
            source(new VertexRef(namespace, id));
            return this;
        }

        public GenericEdgeBuilder source(VertexRef source) {
            Objects.requireNonNull(source);
            this.source = source;
            return this;
        }

        public GenericEdgeBuilder target(String namespace, String id) {
            target(new VertexRef(namespace, id));
            return this;
        }
        
        public GenericEdgeBuilder target(VertexRef target) {
            Objects.requireNonNull(target);
            this.target = target;
            return this;
        }
        
        public GenericEdge build() {
            return new GenericEdge(source, target, properties);
        }
    }
}
