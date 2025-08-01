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
