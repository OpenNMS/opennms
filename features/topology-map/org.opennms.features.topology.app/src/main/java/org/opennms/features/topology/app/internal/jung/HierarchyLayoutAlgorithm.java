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
import java.util.Collection;

import edu.uci.ics.jung.algorithms.layout.TreeLayout;
import edu.uci.ics.jung.algorithms.shortestpath.MinimumSpanningForest;
import edu.uci.ics.jung.graph.DelegateForest;
import edu.uci.ics.jung.graph.Forest;
import edu.uci.ics.jung.graph.SparseGraph;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

public class HierarchyLayoutAlgorithm extends AbstractLayoutAlgorithm {

    @Override
    public void updateLayout(final GraphContainer graphContainer) {
        final Graph g = graphContainer.getGraph();
        final Layout graphLayout = g.getLayout();
        final edu.uci.ics.jung.algorithms.layout.Layout<VertexRef, Edge> treeLayout = createTreeLayout(g);

        treeLayout.setInitializer(initializer(g.getLayout()));

        transformLayout(g.getDisplayVertices(), treeLayout, graphLayout);
    }

    private edu.uci.ics.jung.graph.Graph<VertexRef, Edge> convert(final Graph g) {
        final SparseGraph<VertexRef, Edge> sparseGraph = new SparseGraph<VertexRef, Edge>();

        for(VertexRef v : g.getDisplayVertices()) {
            sparseGraph.addVertex(v);
        }
        for(Edge e : g.getDisplayEdges()) {
            sparseGraph.addEdge(e, e.getSource().getVertex(), e.getTarget().getVertex());
        }
        return sparseGraph;
    }

    private void transformLayout(final Collection<? extends Vertex> vertices, final edu.uci.ics.jung.algorithms.layout.Layout<VertexRef, Edge> layout, final Layout graphLayout) {
        for(VertexRef v : vertices) {
            Point2D p = layout.transform(v);
            graphLayout.setLocation(v, new Point(p.getX(), p.getY()));
        }
    }

    private Vertex getRoot(Graph g) {
        for (Vertex eachVertex : g.getDisplayVertices()) {
            if (eachVertex.getParent() == null) {
                return eachVertex;
            }
        }
        return null;
    }

    public Forest createMinForest(final Graph g) {
        return new MinimumSpanningForest(convert(g), new DelegateForest(), getRoot(g)).getForest();
    }

    public edu.uci.ics.jung.algorithms.layout.Layout<VertexRef, Edge> createTreeLayout(final Graph g) {
        return new TreeLayout<>(createMinForest(g));
    }
}
