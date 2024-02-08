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
package org.opennms.features.topology.app.internal.support;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.VertexRef;

import com.google.common.base.Throwables;

import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.visualization.VisualizationImageServer;

public class GraphUtils {

    public static void renderGraphToFile(Graph<VertexRef, Edge> jungGraph, File file) {
        final edu.uci.ics.jung.algorithms.layout.Layout<VertexRef, Edge> jungLayout = new KKLayout<>(jungGraph);
        jungLayout.setSize(new Dimension(1800,1800)); // Size of the layout

        final Set<VertexRef> roots = jungGraph.getVertices().stream()
                .filter(v -> jungGraph.getInEdges(v).isEmpty())
                .collect(Collectors.toSet());

        VisualizationImageServer<VertexRef, Edge> vv = new VisualizationImageServer<>(jungLayout, jungLayout.getSize());
        vv.setPreferredSize(new Dimension(2000,2000)); // Viewing area size
        vv.getRenderContext().setVertexLabelTransformer(VertexRef::getLabel);
        vv.getRenderContext().setEdgeLabelTransformer(Edge::getLabel);
        vv.getRenderContext().setVertexFillPaintTransformer(vertexRef -> {
            if (roots.contains(vertexRef)) {
                return Color.RED;
            }
            return Color.BLUE;
        });

        // Draw vertices according to in/out edge count. The more edges, the bigger the vertex
        vv.getRenderContext().setVertexShapeTransformer(vertexRef -> {
            Collection<Edge> inEdges = jungGraph.getInEdges(vertexRef);
            Collection<Edge> outEdges = jungGraph.getOutEdges(vertexRef);
            int edgeCount = inEdges.size() + outEdges.size();
            int widthHeight = (edgeCount / 4 + 1) * 20;
            return new Ellipse2D.Float(-1 * widthHeight / 2,-1 * widthHeight / 2,widthHeight,widthHeight);
        });

        // Create the buffered image
        BufferedImage image = (BufferedImage) vv.getImage(
                new Point2D.Double(vv.getGraphLayout().getSize().getWidth() / 2,
                        vv.getGraphLayout().getSize().getHeight() / 2),
                new Dimension(vv.getGraphLayout().getSize()));

        // Render
        try {
            ImageIO.write(image, "png", file);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }
}
