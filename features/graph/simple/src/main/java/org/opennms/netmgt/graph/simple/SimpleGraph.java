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

package org.opennms.netmgt.graph.simple;

import org.opennms.netmgt.graph.api.ImmutableGraph;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericVertex;

/**
 * Acts as a domain specific view on a GenericGraph.
 * This is the most basic concrete subclass of {@link AbstractDomainGraph} and can be used as a reference for your own
 * domain graph. It is a final class. If you need more functionality please extend AbstractDomainGraph.
 */
// TODO MVR rename to domain and this makes more sense
public final class SimpleGraph extends AbstractDomainGraph<SimpleVertex, SimpleEdge> {

    public SimpleGraph(GenericGraph graph) {
        super(graph);
    }

    @Override
    public SimpleVertex convert(GenericVertex vertex) {
        return new SimpleVertex(vertex);
    }

    @Override
    public SimpleEdge convert(GenericEdge edge) {
        return new SimpleEdge(edge);
    }

    @Override
    protected ImmutableGraph<SimpleVertex, SimpleEdge> convert(GenericGraph graph) {
        return new SimpleGraph(graph);
    }

    @Override
    public Class getVertexType() {
        return SimpleVertex.class;
    }
    
    public static SimpleGraphBuilder builder() {
        return new SimpleGraphBuilder();
    }
    
    public static SimpleGraph from(GenericGraph genericGraph) {
        return new SimpleGraph(genericGraph);
    }
    
    public final static class SimpleGraphBuilder extends AbstractDomainGraphBuilder<SimpleGraphBuilder, SimpleVertex, SimpleEdge> {
        
        private SimpleGraphBuilder() {
            domainVertexType(SimpleVertex.class);
        }

        public SimpleGraph build() {
            return new SimpleGraph(delegate.build());
        }
    }
}
