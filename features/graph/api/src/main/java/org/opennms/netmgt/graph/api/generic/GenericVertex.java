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
