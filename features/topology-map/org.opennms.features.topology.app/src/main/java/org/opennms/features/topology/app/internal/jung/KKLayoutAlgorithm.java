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

import java.util.Collection;

import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.Layout;
import org.opennms.features.topology.api.Point;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.graph.SparseGraph;

public class KKLayoutAlgorithm extends AbstractLayoutAlgorithm {

	@Override
	public void updateLayout(final Graph graph) {

		final Layout graphLayout = graph.getLayout();

		SparseGraph<VertexRef, Edge> jungGraph = new SparseGraph<VertexRef, Edge>();

		Collection<? extends Vertex> vertices = graph.getDisplayVertices();

		for(Vertex v : vertices) {
			jungGraph.addVertex(v);
		}

		Collection<? extends Edge> edges = graph.getDisplayEdges();

		for(Edge e : edges) {
			jungGraph.addEdge(e, e.getSource().getVertex(), e.getTarget().getVertex());
		}

		KKLayout<VertexRef, Edge> layout = new KKLayout<VertexRef, Edge>(jungGraph);
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
