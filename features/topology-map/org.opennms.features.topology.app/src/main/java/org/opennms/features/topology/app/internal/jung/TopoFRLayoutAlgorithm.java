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
package org.opennms.features.topology.app.internal.jung;

import java.awt.Dimension;
import java.awt.geom.Point2D;
import java.util.Collection;

import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

import com.google.common.base.Function;

import edu.uci.ics.jung.graph.SparseGraph;

public class TopoFRLayoutAlgorithm extends AbstractLayoutAlgorithm {

    @Override
    public void updateLayout(final Graph graph) {

        final Layout graphLayout = graph.getLayout();

        SparseGraph<VertexRef, EdgeRef> jungGraph = new SparseGraph<>();

        Collection<Vertex> vertices = graph.getDisplayVertices();

        for(Vertex v : vertices) {
            jungGraph.addVertex(v);
        }

        Collection<Edge> edges = graph.getDisplayEdges();

        for(Edge e : edges) {
            jungGraph.addEdge(e, e.getSource().getVertex(), e.getTarget().getVertex());
        }

        TopoFRLayout<VertexRef, EdgeRef> layout = new TopoFRLayout<>(jungGraph);
        // Initialize the vertex positions to the last known positions from the layout
        Dimension size = selectLayoutSize(graph);
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

    protected static Function<VertexRef, Point2D> initializer(final Layout graphLayout, final int xOffset, final int yOffset) {
        return (VertexRef v) -> {
            org.opennms.features.topology.api.Point location = graphLayout.getLocation(v);
            return new Point2D.Double(location.getX()+xOffset, location.getY()+yOffset);
        };
    }
}
