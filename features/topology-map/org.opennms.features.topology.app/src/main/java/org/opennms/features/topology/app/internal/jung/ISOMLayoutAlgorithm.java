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

import java.awt.geom.Point2D;
import java.util.Collection;
import com.google.common.base.Function;

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
		public void setInitializer(final Function<VertexRef,Point2D> ignoreMe) {
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
