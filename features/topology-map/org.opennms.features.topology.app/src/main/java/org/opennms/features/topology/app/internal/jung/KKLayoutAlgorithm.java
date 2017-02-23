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

import java.util.Collection;

import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.graph.SparseGraph;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

public class KKLayoutAlgorithm extends AbstractLayoutAlgorithm {

	@Override
	public void updateLayout(final GraphContainer graphContainer) {

		Graph g = graphContainer.getGraph();

		final Layout graphLayout = g.getLayout();

		SparseGraph<VertexRef, Edge> jungGraph = new SparseGraph<VertexRef, Edge>();

		Collection<? extends Vertex> vertices = g.getDisplayVertices();

		for(Vertex v : vertices) {
			jungGraph.addVertex(v);
		}

		Collection<? extends Edge> edges = g.getDisplayEdges();

		for(Edge e : edges) {
			jungGraph.addEdge(e, e.getSource().getVertex(), e.getTarget().getVertex());
		}

		KKLayout<VertexRef, Edge> layout = new KKLayout<VertexRef, Edge>(jungGraph);
		layout.setInitializer(initializer(graphLayout));
		layout.setSize(selectLayoutSize(graphContainer));

		while(!layout.done()) {
			layout.step();
		}

		for(Vertex v : vertices) {
			graphLayout.setLocation(v, new Point(layout.getX(v), layout.getY(v)));
		}
	}
}
