/*******************************************************************************
 * This file is part of OpenNMS(R).
 * <p>
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 * <p>
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 * <p>
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 * http://www.gnu.org/licenses/
 * <p>
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.app.internal.jung;


import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.LevelAware;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

import edu.uci.ics.jung.graph.DirectedOrderedSparseMultigraph;

/**
 * <p>
 * Algorithm to transform a graph of vertices and edges into a hierarchical structure of a 2D plane.
 * It is a geometric transformation which does not change the graph but the x and y position of its vertices.
 * </p>
 * <p>
 * The algorithm generates the layout correctly if only one root element exists or if there are no vertices at all
 * If no root element exists, a random vertex from the graph is picked and defined as root.
 * If multiple root elements exist, a parent of those is created and called dummy root element. This ensures that there is only one root element.
 * </p>
 * <p>
 * The current graph to be transformed is extracted from the {@link org.opennms.features.topology.api.GraphContainer}.
 * Basically all its vertices are transformed and then the new layout is set back to the GraphContainer by executing
 * the {@link #updateLayout(GraphContainer) updateLayout} method.
 * </p>
 *
 * The Algorithm uses the JUNG library.
 * @see <a href="http://jung.sourceforge.net">JUNG Framework (Java Universal Network/Graph Framework)</a>
 *
 *
 */
public class HierarchyLayoutAlgorithm extends AbstractLayoutAlgorithm {

    /**
     * Updates the current layout by extracting the containers graph and then perform a (x,y) tranformation
     * of all vertices.
     *
     * @param graphContainer The container of the current graph. Contains all relevant information to perform the transformation
     *                       of the {@link org.opennms.features.topology.api.Graph} by changing its {@link org.opennms.features.topology.api.Layout}
     */
    @Override
    public void updateLayout(final GraphContainer graphContainer) {
        final Graph graph = graphContainer.getGraph();
        final Layout graphLayout = graph.getLayout();
        final edu.uci.ics.jung.algorithms.layout.Layout<VertexRef, Edge> treeLayout = createTreeLayout(graph);

        applyLayoutPositions(graph.getDisplayVertices(), treeLayout, graphLayout);
    }

    private edu.uci.ics.jung.graph.DirectedGraph<VertexRef, Edge> convert(final Graph g) {
        // We need to sort the elements. For this purpose we use the DirectedOrderedSparseMultigraph
        final edu.uci.ics.jung.graph.DirectedGraph<VertexRef, Edge> jungGraph = new DirectedOrderedSparseMultigraph<>();
        final Collection<Vertex> displayVertices = g.getDisplayVertices();

        // Sort by level
        final List<Vertex> sortedVertices = displayVertices.stream().filter(v -> v instanceof LevelAware).sorted(new Comparator<Vertex>() {
            @Override
            public int compare(Vertex o1, Vertex o2) {
                return Integer.compare(((LevelAware) o1).getLevel(), ((LevelAware) o2).getLevel());
            }
        }).collect(Collectors.toList());

        // Add all non LevelAware Vertices
        final List<Vertex> notLevelAwareVertices = displayVertices.stream().filter(v -> !sortedVertices.contains(v)).collect(Collectors.toList());
        sortedVertices.addAll(notLevelAwareVertices);

        // Build the graph
        for(VertexRef v : sortedVertices) {
            jungGraph.addVertex(v);
        }

        // The order of edges does not matter
        for(Edge e : g.getDisplayEdges()) {
            jungGraph.addEdge(e, e.getSource().getVertex(), e.getTarget().getVertex());
        }
        return jungGraph;
    }

    private void applyLayoutPositions(final Collection<? extends Vertex> vertices, final edu.uci.ics.jung.algorithms.layout.Layout<VertexRef, Edge> layout, final Layout graphLayout) {
        for(VertexRef v : vertices) {
            Point2D p = layout.transform(v);
            graphLayout.setLocation(v, new Point(p.getX(), p.getY()));
        }
    }

    private edu.uci.ics.jung.algorithms.layout.Layout<VertexRef, Edge> createTreeLayout(final Graph g) {
        final edu.uci.ics.jung.graph.DirectedGraph<VertexRef, Edge> jungGraph = convert(g);
        HierarchyLayout<VertexRef, Edge> layout = new HierarchyLayout<>(jungGraph, ELBOW_ROOM * 2, ELBOW_ROOM * 2);
        return layout;
    }

    private List<Edge> createRootDummyEdges(List<Vertex> realRootVertices, Vertex dummyRootVertex) {
        List<Edge> edges = new ArrayList<>();
        for (Vertex eachRealRootVertex : realRootVertices) {
            Edge edge = new AbstractEdge(
                    getClass().getSimpleName(),
                    dummyRootVertex.getId() + ":" + eachRealRootVertex.getId(),
                    dummyRootVertex, eachRealRootVertex);
            edges.add(edge);
        }
        return edges;
    }
}
