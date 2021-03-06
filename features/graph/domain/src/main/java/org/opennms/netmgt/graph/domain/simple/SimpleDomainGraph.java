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

package org.opennms.netmgt.graph.domain.simple;

import org.opennms.netmgt.graph.api.ImmutableGraph;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.domain.AbstractDomainGraph;

/**
 * Acts as a domain specific view on a {@link GenericGraph}.
 * This is the most basic concrete subclass of {@link AbstractDomainGraph} and can be used as a reference for your own
 * domain graph. It is a final class. If you need more functionality please extend {@link AbstractDomainGraph}.
 */
public final class SimpleDomainGraph extends AbstractDomainGraph<SimpleDomainVertex, SimpleDomainEdge> {

    public SimpleDomainGraph(GenericGraph graph) {
        super(graph);
    }

    @Override
    public SimpleDomainVertex convert(GenericVertex vertex) {
        return new SimpleDomainVertex(vertex);
    }

    @Override
    public SimpleDomainEdge convert(GenericEdge edge) {
        return new SimpleDomainEdge(edge);
    }

    @Override
    protected ImmutableGraph<SimpleDomainVertex, SimpleDomainEdge> convert(GenericGraph graph) {
        return new SimpleDomainGraph(graph);
    }

    public static SimpleDomainGraphBuilder builder() {
        return new SimpleDomainGraphBuilder();
    }
    
    public static SimpleDomainGraph from(GenericGraph genericGraph) {
        return new SimpleDomainGraph(genericGraph);
    }
    
    public final static class SimpleDomainGraphBuilder extends AbstractDomainGraphBuilder<SimpleDomainGraphBuilder, SimpleDomainVertex, SimpleDomainEdge> {
        
        private SimpleDomainGraphBuilder() {}

        public SimpleDomainGraph build() {
            return new SimpleDomainGraph(delegate.build());
        }
    }
}
