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
import java.util.Collection;

import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.jung.ISOMLayoutAlgorithm.NonStupidISOMLayout;

import edu.uci.ics.jung.algorithms.layout.FRLayout;
import edu.uci.ics.jung.algorithms.layout.SpringLayout;
import edu.uci.ics.jung.graph.SparseGraph;

public class RealUltimateLayoutAlgorithm extends AbstractLayoutAlgorithm {

	@Override
	public void updateLayout(Graph graph) {

		final Layout graphLayout = graph.getLayout();

		SparseGraph<VertexRef, EdgeRef> jungGraph = new SparseGraph<VertexRef, EdgeRef>();

		Collection<? extends Vertex> vertices = graph.getDisplayVertices();

		for(Vertex v : vertices) {
			jungGraph.addVertex(v);
		}

		Collection<? extends Edge> edges = graph.getDisplayEdges();

		for(Edge e : edges) {
			jungGraph.addEdge(e, e.getSource().getVertex(), e.getTarget().getVertex());
		}

		Dimension size = selectLayoutSize(graph);
		Dimension paddedSize = new Dimension((int)(size.getWidth()*.75), (int)(size.getHeight()*.75));

		doISOMLayout(graphLayout, jungGraph, size);
		doSpringLayout(graphLayout, jungGraph, size, SPRING_LAYOUT_REPULSION);
		doFRLayout(graphLayout, jungGraph, paddedSize, (int)(size.getWidth()/8.0), (int)(size.getHeight()/8.0));
		doSpringLayout(graphLayout, jungGraph, size, SPRING_LAYOUT_REPULSION);
	}

	private static void doSpringLayout(final Layout graphLayout, SparseGraph<VertexRef, EdgeRef> jungGraph, Dimension size, int repulsion) {
		SpringLayout<VertexRef, EdgeRef> layout = new SpringLayout<VertexRef, EdgeRef>(jungGraph);
		layout.setForceMultiplier(SPRING_FORCE_MULTIPLIER);
		layout.setRepulsionRange(repulsion);
		layout.setInitializer(initializer(graphLayout));
		layout.setSize(size);

		int count = 0;
		while(!layout.done() && count < 700) {
			layout.step();
			count++;
		}

		for(VertexRef v : jungGraph.getVertices()) {
			graphLayout.setLocation(v, new Point(layout.getX(v), layout.getY(v)));
		}
	}

	private static void doFRLayout(final Layout graphLayout, SparseGraph<VertexRef, EdgeRef> jungGraph, Dimension size, final int xOffset, final int yOffset) {
		FRLayout<VertexRef, EdgeRef> layout = new FRLayout<VertexRef, EdgeRef>(jungGraph);
		layout.setInitializer(initializer(graphLayout, xOffset, yOffset));
		layout.setSize(size);

		while(!layout.done()) {
			layout.step();
		}

		for(VertexRef v : jungGraph.getVertices()) {
			graphLayout.setLocation(v, new Point(layout.getX(v)+xOffset, layout.getY(v)+yOffset));
		}

	}

	private static void doISOMLayout(final Layout graphLayout, SparseGraph<VertexRef, EdgeRef> jungGraph, Dimension size) {
		NonStupidISOMLayout layout = new NonStupidISOMLayout(jungGraph, graphLayout);
		layout.setInitializer(initializer(graphLayout));
		layout.setSize(size);

		while(!layout.done()) {
			layout.step();
		}

		for(VertexRef v : jungGraph.getVertices()) {
			graphLayout.setLocation(v, new Point(layout.getX(v), layout.getY(v)));
		}

	}
}
