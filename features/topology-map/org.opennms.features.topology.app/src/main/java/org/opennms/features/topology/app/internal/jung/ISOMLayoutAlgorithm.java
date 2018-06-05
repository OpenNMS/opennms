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

import java.awt.geom.Point2D;
import java.util.Collection;

import org.apache.commons.collections15.Transformer;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.util.RandomLocationTransformer;
import edu.uci.ics.jung.graph.SparseGraph;

public class ISOMLayoutAlgorithm extends AbstractLayoutAlgorithm {

	public static class NonStupidISOMLayout extends ISOMLayout<VertexRef,EdgeRef> {
		
		protected final Layout m_layout;

        public NonStupidISOMLayout(edu.uci.ics.jung.graph.Graph<VertexRef,EdgeRef> g, Layout graphLayout) {
			super(g);
			m_layout = graphLayout;
		}

		/**
		 * Override this method so that the initialize() method cannot set the initializer to
		 * {@link RandomLocationTransformer}.
		 */
		@Override
		public void setInitializer(Transformer<VertexRef,Point2D> ignoreMe) {
			super.setInitializer(initializer(m_layout));
		}

        @Override
        public boolean done() {
            return getGraph().getVertexCount() > 0 ? super.done() : true;
        }
    }

	@Override
	public void updateLayout(final Graph graph) {

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

		NonStupidISOMLayout layout = new NonStupidISOMLayout(jungGraph, graphLayout);
		layout.setInitializer(initializer(graphLayout));
		layout.setSize(selectLayoutSize(graph));

		while(!layout.done()) {
			layout.step();
		}

		for(Vertex v : vertices) {
			graphLayout.setLocation(v, new Point(layout.getX(v), layout.getY(v)));
		}
	}
}
