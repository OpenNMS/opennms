/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
