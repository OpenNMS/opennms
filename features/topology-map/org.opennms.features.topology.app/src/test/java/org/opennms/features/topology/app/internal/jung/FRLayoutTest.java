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

import edu.uci.ics.jung.graph.SparseGraph;
import org.apache.commons.collections15.Transformer;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.api.*;
import org.opennms.features.topology.api.topo.*;
import org.opennms.features.topology.app.internal.ProviderManager;
import org.opennms.features.topology.app.internal.VEProviderGraphContainer;
import org.opennms.features.topology.plugins.topo.simple.SimpleGraphBuilder;

import java.awt.*;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class FRLayoutTest {

    private static final double ELBOW_ROOM = 50.0;
    GraphContainer m_graphContainer;
    GraphProvider m_graphProvider;

    @Before
    public void setUp(){

        m_graphProvider = new SimpleGraphBuilder("nodes")
                .vertex("v1").vLabel("vertex1").vIconKey("server").vTooltip("tooltip").vStyleName("vertex")
                .vertex("v2").vLabel("vertex2").vIconKey("server").vTooltip("tooltip").vStyleName("vertex")
                .vertex("v3").vLabel("vertex3").vIconKey("server").vTooltip("tooltip").vStyleName("vertex")
                .vertex("v4").vLabel("vertex4").vIconKey("server").vTooltip("tooltip").vStyleName("vertex")
                .vertex("v5").vLabel("vertex5").vIconKey("server").vTooltip("tooltip").vStyleName("vertex")
                .vertex("v6").vLabel("vertex6").vIconKey("server").vTooltip("tooltip").vStyleName("vertex")
                .vertex("v7").vLabel("vertex7").vIconKey("server").vTooltip("tooltip").vStyleName("vertex")
                .vertex("v8").vLabel("vertex8").vIconKey("server").vTooltip("tooltip").vStyleName("vertex")

                .edge("e1", "v1", "v2").eStyleName("edge")
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
                .edge("e14", "v7", "v8").eStyleName("edge")
                .get();

        ProviderManager providerManager = new ProviderManager();
        m_graphContainer = new VEProviderGraphContainer(m_graphProvider, providerManager);
    }

    @Test
    public void testFRLayout() {
        Graph g = m_graphContainer.getGraph();

        List<Vertex> vertices = new ArrayList<Vertex>(g.getDisplayVertices());

        TopoFRLayout<VertexRef, EdgeRef> layout = runFRLayout(g, g.getLayout(), vertices);

        Vertex v1 = vertices.get(0);
        Vertex v2 = vertices.get(1);
        Vertex v3 = vertices.get(2);

        double distance = calcDistance(layout, v1, v2);
        double distance2 = calcDistance(layout, v2, v3);
        double distance3 = calcDistance(layout, v1, v3);
        System.out.println("distance: " + distance);
        System.out.println("distance2: " + distance2);
        System.out.println("distance3: " + distance3);



        //Run again then refactor
        TopoFRLayout<VertexRef, EdgeRef> layout2 = runFRLayout(g, g.getLayout(), vertices);

        distance = calcDistance(layout2, v1, v2);
        distance2 = calcDistance(layout2, v2, v3);
        distance3 = calcDistance(layout2, v1, v3);
        System.out.println("distance: " + distance);
        System.out.println("distance2: " + distance2);
        System.out.println("distance3: " + distance3);

        TopoFRLayout<VertexRef, EdgeRef> layout3 = runFRLayout(g, g.getLayout(), vertices);

        distance = calcDistance(layout3, v1, v2);
        distance2 = calcDistance(layout3, v2, v3);
        distance3 = calcDistance(layout3, v1, v3);
        System.out.println("distance: " + distance);
        System.out.println("distance2: " + distance2);
        System.out.println("distance3: " + distance3);

    }

    private TopoFRLayout<VertexRef, EdgeRef> runFRLayout(Graph g, Layout graphLayout, List<Vertex> vertices) {
        TopoFRLayout<VertexRef, EdgeRef> layout = new TopoFRLayout<VertexRef, EdgeRef>(createJungGraph(g));
        Dimension size = selectLayoutSize(m_graphContainer);
        //layout.setRepulsionMultiplier(3/8.0);
        //layout.setAttractionMultiplier(3/8.0);
        layout.setInitializer(initializer(graphLayout, size));
        layout.setSize(size);

        int count = 0;
//        System.out.println("[");
        while (!layout.done()) {
//            System.out.println("[");

            for(int i = 0; i < vertices.size(); i++) {
                Vertex v = vertices.get(i);
                if(i + 1 == vertices.size()){
//                    System.out.println("{ x:" + layout.getX(v) + ", y: " + layout.getY(v) + " }");
                } else{
//                    System.out.println("{ x:" + layout.getX(v) + ", y: " + layout.getY(v) + " },");
                }

            }

            layout.step();
//            System.out.println("],");
        }
//        System.out.println("]");

        System.out.println("/******** FRLayout Run **********/");

        for(Vertex v : vertices) {
            graphLayout.setLocation(v, layout.getX(v) - size.getWidth()/2.0, layout.getY(v) - size.getHeight()/2.0 );
            System.out.println("layout.getX(): " + layout.getX(v) + " layout.getY(): " + layout.getY(v));
        }
        System.out.println("/******** End FRLayout Run **********/");

        return layout;
    }

    private double calcDistance(TopoFRLayout<VertexRef, EdgeRef> layout2, Vertex v1, Vertex v2) {
        double dx = Math.abs(layout2.getX(v1) - layout2.getX(v2));
        double dy = Math.abs(layout2.getY(v1) - layout2.getY(v2));

        return Math.sqrt(dx * dx + dy * dy);
    }

    private SparseGraph<VertexRef, EdgeRef> createJungGraph(Graph g) {
        SparseGraph<VertexRef, EdgeRef> jungGraph = new SparseGraph<VertexRef, EdgeRef>();

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

    protected Transformer<VertexRef, Point2D> initializer(final Layout graphLayout, final Dimension dim) {
        return new Transformer<VertexRef, Point2D>() {
            @Override
            public Point2D transform(VertexRef v) {
                if (v == null) {
                    System.out.println("Algorithm tried to layout a null vertex");
                    return new Point(0,0);
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

        System.out.printf("selectLayoutSize: vertexCount=%s, returm dim=%s \n", vertexCount, dim);

        return dim;
    }
}
