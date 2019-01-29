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

package org.opennsm.features.graphs.simple;

import java.util.Objects;

import org.opennms.features.graph.api.AbstractGraph;
import org.opennms.features.graph.api.Edge;
import org.opennms.features.graph.api.Graph;
import org.opennms.features.graph.api.Vertex;
import org.opennms.features.graph.api.generic.GenericGraph;
import org.opennms.features.graph.api.generic.GenericProperties;
import org.opennms.features.graph.api.info.DefaultGraphInfo;
import org.opennms.features.graph.api.info.GraphInfo;

// TODO MVR enforce namespace
// TODO MVR this is basically a copy of GenericGraph :'(
// TODO MVR implement duplication detection (e.g. adding same vertex twice
// and as well as adding different edges with different source/target vertices, should add each vertex only once,
// maybe not here, but at some point that check should be implemented)
public class SimpleGraph extends AbstractGraph<SimpleVertex, SimpleEdge> implements Graph<SimpleVertex, SimpleEdge> {

    public SimpleGraph(String namespace) {
        this(namespace, SimpleVertex.class);
    }

    public SimpleGraph(String namespace, Class<SimpleVertex> vertexType) {
        super(new DefaultGraphInfo(namespace, vertexType));
    }

    // Copy constructor.
    public SimpleGraph(SimpleGraph copyMe) {
        super(new DefaultGraphInfo(Objects.requireNonNull(copyMe)));
        // TODO MVR copy focus strategy? :(

        copyMe.getVertices().forEach(v -> {
            final SimpleVertex clonedVertex = new SimpleVertex(v);
            clonedVertex.setNamespace(copyMe.getNamespace());
            addVertex(clonedVertex);
        });

        copyMe.getEdges().forEach(e -> {
            final SimpleEdge clonedEdge = new SimpleEdge(e);
            clonedEdge.setNamespace(getNamespace());
            addEdge(clonedEdge); // TODO MVR ... gnaaa, this is wrong
        });
    }

    public SimpleGraph(GraphInfo graphInfo) {
        super(graphInfo);
    }

    @Override
    public GenericGraph asGenericGraph() {
        final GenericGraph graph = new GenericGraph();
        graph.setProperty(GenericProperties.NAMESPACE, getNamespace());
        graph.setProperty(GenericProperties.LABEL, getLabel());
        graph.setProperty(GenericProperties.DESCRIPTION, getDescription());
        getVertices().stream().map(Vertex::asGenericVertex).forEach(graph::addVertex);
        getEdges().stream().map(Edge::asGenericEdge).forEach(graph::addEdge);
        return graph;
    }

    public void setLabel(String label) {
        ((DefaultGraphInfo) graphInfo).setLabel(label);
    }

    public void setDescription(String description) {
        ((DefaultGraphInfo) graphInfo).setDescription(description);
    }

    public void setNamespace(String namespace) {
        ((DefaultGraphInfo) graphInfo).setNamespace(namespace);
        getVertices().forEach(v -> v.setNamespace(namespace));
        getEdges().forEach(e -> {
            if (e.getSource().getNamespace().equalsIgnoreCase(e.getNamespace())) {
                ((SimpleVertex) e.getSource()).setNamespace(namespace);
            }
            if (e.getTarget().getNamespace().equalsIgnoreCase(e.getNamespace())) {
                ((SimpleVertex) e.getTarget()).setNamespace(namespace);
            }
            e.setNamespace(namespace);
        });
    }

    public SimpleVertex createVertex(String id) {
        final SimpleVertex vertex = new SimpleVertex(getNamespace(), id);
        addVertex(vertex);
        return vertex;
    }

    public SimpleEdge createEdge(SimpleVertex sourceVertex, SimpleVertex targetVertex) {
        final SimpleEdge edge = new SimpleEdge(sourceVertex, targetVertex);
        edge.setNamespace(getNamespace());
        addEdge(edge);
        return edge;
    }

}
