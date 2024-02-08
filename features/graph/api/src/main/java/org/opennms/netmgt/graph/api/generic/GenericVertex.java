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

import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.opennms.netmgt.graph.api.NodeRef;
import org.opennms.netmgt.graph.api.Vertex;
import org.opennms.netmgt.graph.api.VertexRef;
import org.opennms.netmgt.graph.api.aware.NodeRefAware;
import org.slf4j.LoggerFactory;

public final class GenericVertex extends GenericElement implements Vertex, NodeRefAware {
    
    private GenericVertex(Map<String, Object> properties) {
        super(properties);
        Objects.requireNonNull(getId(), "id cannot be null");
    }

    public VertexRef getVertexRef() {
        return new VertexRef(this.getNamespace(), this.getId());
    }

    @Override
    public NodeRef getNodeRef() {
        final List<NodeRef> nodeRefs = NodeRef.from(this);
        if (nodeRefs.isEmpty()) {
            return null;
        }
        if (nodeRefs.size() > 1) {
            LoggerFactory.getLogger(getClass()).warn("Vertex has multiple node references: {}. Using first one: {}", nodeRefs, nodeRefs.get(0));
        }
        return nodeRefs.get(0);
    }

    @Override
    public GenericVertex asGenericVertex() {
        return this;
    }

    public static GenericVertexBuilder builder() {
    	return new GenericVertexBuilder();
    }
    
    public final static class GenericVertexBuilder extends GenericElementBuilder<GenericVertexBuilder> {
    	
        private GenericVertexBuilder() {}
        
    	public GenericVertexBuilder id(String id) {
    		property(GenericProperties.ID, id);
    		return this;
    	}

        public GenericVertexBuilder vertex(GenericVertex vertex) {
            Objects.requireNonNull(vertex);
            properties(vertex.getProperties());
            return this;
        }

        public GenericVertexBuilder nodeRef(String foreignSource, String foreignId) {
            property(GenericProperties.FOREIGN_SOURCE, foreignSource);
            property(GenericProperties.FOREIGN_ID, foreignId);
            return this;
        }
    	
    	public GenericVertex build() {
    		return new GenericVertex(properties);
    	}
    }
}
