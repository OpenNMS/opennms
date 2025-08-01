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
