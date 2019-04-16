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

import org.opennms.netmgt.graph.api.Graph;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.info.GraphInfo;

/**
 * Acts as a domain specific view on a Graph.
 * This is the most basic concrete subclass of {@link AbstractDomainGraph} and can be used as a reference for your own
 * domain graph. It is a final class since it exposes class information about it's associated vertex and edge types.
 * If you need more functionality please extend AbstractDomainGraph.
 */
public final class SimpleGraph extends AbstractDomainGraph<SimpleVertex, SimpleEdge> {


    public SimpleGraph(String namespace) {
        super(namespace);
    }

    public static SimpleGraph fromGraphInfo(GraphInfo graphInfo) {
        // we can't have a constructor SimpleGraph(GraphInfo graphInfo) since it conflicts with SimpleGraph(GenericGraph graph)
        // that's why we have a factory method instead
        GenericGraph graph = new GenericGraph();
        graph.setNamespace(graphInfo.getNamespace());
        graph.setLabel(graphInfo.getLabel());
        graph.setDescription(graphInfo.getDescription());
        return new SimpleGraph(graph);
    }

    public SimpleGraph(GenericGraph graph) {
        super(graph);
    }

    /** copy constructor */
    public SimpleGraph(SimpleGraph graph) {
        this(new GenericGraph(graph.asGenericGraph()));
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
    protected Graph<SimpleVertex, SimpleEdge> convert(GenericGraph graph) {
        return new SimpleGraph(graph);
    }

    @Override
    public Class getVertexType() {
        return SimpleVertex.class;
    }
}
