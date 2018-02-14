/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.jung;


import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.LevelAware;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * the {@link LayoutAlgorithm#updateLayout(Graph) updateLayout} method.
 * </p>
 *
 * The Algorithm uses the JUNG library.
 * @see <a href="http://jung.sourceforge.net">JUNG Framework (Java Universal Network/Graph Framework)</a>
 *
 *
 */
public class HierarchyLayoutAlgorithm extends AbstractLayoutAlgorithm {

    private static Logger LOG = LoggerFactory.getLogger(HierarchyLayoutAlgorithm.class);

    /**
     * Updates the current layout by extracting the containers graph and then perform a (x,y) tranformation
     * of all vertices.
     *
     * @param graph The container of the current graph. Contains all relevant information to perform the transformation
     *                       of the {@link Graph} by changing its {@link Layout}
     */
    @Override
    public void updateLayout(final Graph graph) {
        final Layout graphLayout = graph.getLayout();

        // Only apply if fully level aware.
        // This should fix rendering if selected "Hierarchical Layout" is already selected, but the graph is not
        // fully level aware. See NMS-8703
        if (isFullyLevelAware(graph)) {
            final HierarchyLayout<VertexRef, Edge> treeLayout = createTreeLayout(graph);
            applyLayoutPositions(graph.getDisplayVertices(), treeLayout, graphLayout);
        } else {
            // SEE NMS-8703
            LOG.warn("The selected graph is not fully level aware. Cannot layout hierarchical. Falling back to D3 Layout");
            new D3TopoLayoutAlgorithm().updateLayout(graph);
        }
    }

    /**
     * Verifies that all vertices in the graph are implementing {@link LevelAware}.
     *
     * @param graph The graph to verify.
     * @return True if all vertices implement {@link LevelAware}. False otherwise.
     */
    private boolean isFullyLevelAware(Graph graph) {
        long levelAwareCount = graph.getDisplayVertices().stream().filter(v -> v instanceof LevelAware).count();
        return levelAwareCount == graph.getDisplayVertices().size();
    }

    private edu.uci.ics.jung.graph.DirectedGraph<VertexRef, Edge> convert(final Graph g) {
        if (!isFullyLevelAware(g)) {
            throw new IllegalStateException("The graph is not LevelAware. Cannot apply Hierarchy Layout. Aborting");
        }

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

    private void applyLayoutPositions(final Collection<? extends Vertex> vertices, final HierarchyLayout<VertexRef, Edge> layout, final Layout graphLayout) {
        final List<VertexRef> displayVertices = vertices.stream()
                .map(v -> (VertexRef)v)
                .collect(Collectors.toList());
        layout.horizontalSqueeze(displayVertices);

        for(VertexRef v : displayVertices) {
            Point2D p = layout.transform(v);
            graphLayout.setLocation(v, new Point(p.getX(), p.getY()));
        }
    }

    private HierarchyLayout<VertexRef, Edge> createTreeLayout(final Graph g) {
        final edu.uci.ics.jung.graph.DirectedGraph<VertexRef, Edge> jungGraph = convert(g);
        HierarchyLayout<VertexRef, Edge> layout = new HierarchyLayout<>(jungGraph, ELBOW_ROOM * 2, ELBOW_ROOM * 2);
        return layout;
    }
}
