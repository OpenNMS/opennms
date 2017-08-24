/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.Collection;

import edu.uci.ics.jung.graph.SparseGraph;
import org.apache.commons.collections15.Transformer;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

public class TopoFRLayoutAlgorithm extends AbstractLayoutAlgorithm {

    @Override
    public void updateLayout(final GraphContainer graphContainer) {

        Graph g = graphContainer.getGraph();

        final Layout graphLayout = g.getLayout();

        SparseGraph<VertexRef, EdgeRef> jungGraph = new SparseGraph<VertexRef, EdgeRef>();

        Collection<Vertex> vertices = g.getDisplayVertices();

        for(Vertex v : vertices) {
            jungGraph.addVertex(v);
        }

        Collection<Edge> edges = g.getDisplayEdges();

        for(Edge e : edges) {
            jungGraph.addEdge(e, e.getSource().getVertex(), e.getTarget().getVertex());
        }

        TopoFRLayout<VertexRef, EdgeRef> layout = new TopoFRLayout<VertexRef, EdgeRef>(jungGraph);
        // Initialize the vertex positions to the last known positions from the layout
        Dimension size = selectLayoutSize(graphContainer);
        layout.setInitializer(initializer(graphLayout, (int)size.getWidth()/2, (int)size.getHeight()/2));
        // Resize the graph to accommodate the number of vertices
        layout.setSize(size);

        while(!layout.done()) {
            layout.step();
        }

        // Store the new positions in the layout
        for(Vertex v : vertices) {
            graphLayout.setLocation(v, new Point(layout.getX(v) - (size.getWidth()/2), (int)layout.getY(v) - (size.getHeight()/2)));
        }
    }

    protected static Transformer<VertexRef, Point2D> initializer(final Layout graphLayout, final int xOffset, final int yOffset) {
        return new Transformer<VertexRef, Point2D>() {
            @Override
            public Point2D transform(VertexRef v) {
                org.opennms.features.topology.api.Point location = graphLayout.getLocation(v);
                return new Point2D.Double(location.getX()+xOffset, location.getY()+yOffset);
            }
        };
    }
}
