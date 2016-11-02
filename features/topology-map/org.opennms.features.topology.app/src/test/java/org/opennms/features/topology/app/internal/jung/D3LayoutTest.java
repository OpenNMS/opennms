/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections15.Transformer;
import org.junit.Test;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.support.SimpleGraphBuilder;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

import edu.uci.ics.jung.graph.SparseGraph;

public class D3LayoutTest extends AbstractLayoutTest {

    private static final double ELBOW_ROOM = 50.0;

    @Override
    protected GraphProvider getGraphProvider() {
        final SimpleGraphBuilder bldr = new SimpleGraphBuilder("nodes");
        for(int i = 0; i < 100; i++) {
            bldr.vertex("v"+i).vLabel("vertex"+i).vIconKey("server").vTooltip("tooltip").vStyleName("vertex");
        }
        bldr.edge("e1", "v1", "v2").eStyleName("edge")
                .edge("e2", "v1", "v3").eStyleName("edge")
                .edge("e3", "v1", "v4").eStyleName("edge")
                .edge("e4", "v1", "v5").eStyleName("edge")
                .edge("e5", "v1", "v6").eStyleName("edge")
                .edge("e6", "v1", "v7").eStyleName("edge")
                .edge("e7", "v1", "v8").eStyleName("edge")
                .edge("e8", "v1", "v8").eStyleName("edge")
                .edge("e9", "v2", "v8").eStyleName("edge")
                .edge("e10", "v2", "v7").eStyleName("edge")
                .edge("e11", "v3", "v8").eStyleName("edge")
                .edge("e12", "v5", "v8").eStyleName("edge")
                .edge("e13", "v6", "v8").eStyleName("edge")
                .edge("e14", "v7", "v8").eStyleName("edge");

        return bldr.get();
    }

    @Test
    public void testD3Layout() throws IOException {
        Graph g = m_graphContainer.getGraph();

        List<Vertex> vertices = new ArrayList<>(g.getDisplayVertices());

        D3TopoLayout<VertexRef, EdgeRef> layout = runD3Layout(1, g, g.getLayout(), vertices);

        Vertex v1 = vertices.get(0);
        Vertex v2 = vertices.get(1);
        Vertex v3 = vertices.get(2);

        double distance = calcDistance(layout, v1, v2);
        double distance2 = calcDistance(layout, v2, v3);
        double distance3 = calcDistance(layout, v1, v3);
        LOG.info("distance: " + distance);
        LOG.info("distance2: " + distance2);
        LOG.info("distance3: " + distance3);

        D3TopoLayout<VertexRef, EdgeRef> layout2 = runD3Layout(2, g, g.getLayout(), vertices);

        distance = calcDistance(layout2, v1, v2);
        distance2 = calcDistance(layout2, v2, v3);
        distance3 = calcDistance(layout2, v1, v3);
        LOG.info("distance: " + distance);
        LOG.info("distance2: " + distance2);
        LOG.info("distance3: " + distance3);

        D3TopoLayout<VertexRef, EdgeRef> layout3 = runD3Layout(3, g, g.getLayout(), vertices);

        distance = calcDistance(layout3, v1, v2);
        distance2 = calcDistance(layout3, v2, v3);
        distance3 = calcDistance(layout3, v1, v3);
        LOG.info("distance: " + distance);
        LOG.info("distance2: " + distance2);
        LOG.info("distance3: " + distance3);
    }

    private double calcDistance(D3TopoLayout<VertexRef, EdgeRef> layout2, Vertex v1, Vertex v2) {
        double dx = Math.abs(layout2.getX(v1) - layout2.getX(v2));
        double dy = Math.abs(layout2.getY(v1) - layout2.getY(v2));

        return Math.sqrt(dx * dx + dy * dy);
    }

    private D3TopoLayout<VertexRef, EdgeRef> runD3Layout(int count, Graph g, Layout graphLayout, List<Vertex> vertices) throws IOException {
        D3TopoLayout<VertexRef, EdgeRef> layout = new D3TopoLayout<>(createJungGraph(g));
        Dimension size = selectLayoutSize(m_graphContainer);

        layout.setInitializer(initializer(graphLayout, size));
        layout.setSize(size);
        
        try (PrintWriter out = new PrintWriter(new FileWriter("target/data"+count+".js"))) {
            out.println("var gCenter = { x: " + size.getWidth()/2.0 + ", y: " + size.getHeight()/2.0 + "};");
            out.println("var data = [");
            while (!layout.done()) {
                out.println("[");

                for (int i = 0; i < vertices.size(); i++) {
                    Vertex v = vertices.get(i);
                    if (i + 1 == vertices.size()) {
                        out.println("{ x:" + layout.getX(v) + ", y:" + layout.getY(v) + " }");
                    } else {
                        out.println("{ x:" + layout.getX(v) + ", y:" + layout.getY(v) + " },");
                    }
                }

                layout.step();
                out.println("],");
            }
            out.println("];");
            LOG.info("/******** D3Layout Run **********/");
        }

        for (Vertex v : vertices) {
            graphLayout.setLocation(v, new Point(layout.getX(v) - size.getWidth()/2.0, layout.getY(v) - size.getHeight()/2.0));
            LOG.info("layout.getX(): " + layout.getX(v) + " layout.getY(): " + layout.getY(v));
        }
        LOG.info("/******** End D3Layout Run **********/");
        return layout;
    }

    protected Transformer<VertexRef, Point2D> initializer(final Layout graphLayout, final Dimension dim) {
        return new Transformer<VertexRef, Point2D>() {
            @Override
            public Point2D transform(VertexRef v) {
                if (v == null) {
                    LOG.info("Algorithm tried to layout a null vertex");
                    return new java.awt.Point(0,0);
                }
                org.opennms.features.topology.api.Point location = graphLayout.getLocation(v);
                return new Point2D.Double(location.getX() + dim.getWidth()/2.0 , location.getY() + dim.getHeight()/2.0 );
            }
        };
    }

    protected Dimension selectLayoutSize(GraphContainer g) {
        int vertexCount = g.getGraph().getDisplayVertices().size();

        double height = 3*Math.sqrt(vertexCount)*ELBOW_ROOM;
        double width = height*16.0/9.0;

        Dimension dim = new Dimension((int)width, (int)height);

        LOG.info("selectLayoutSize: vertexCount={}, return dim={}", vertexCount, dim);

        return dim;
    }

    private SparseGraph<VertexRef, EdgeRef> createJungGraph(Graph g) {
        SparseGraph<VertexRef, EdgeRef> jungGraph = new SparseGraph<>();

        Collection<Vertex> vertices = g.getDisplayVertices();

        for (Vertex v : vertices) {
            jungGraph.addVertex(v);
        }

        Collection<Edge> edges = g.getDisplayEdges();
        for (Edge e : edges) {
            jungGraph.addEdge(e, e.getSource().getVertex(), e.getTarget().getVertex());
        }
        return jungGraph;
    }
}
