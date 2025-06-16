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
package org.opennms.features.topology.plugins.topo.pathoutage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.support.hops.DefaultVertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractTopologyProvider;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Defaults;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;

import com.google.common.collect.Lists;

import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;

/**
 * This provider allows us to build a vertex hierarchy, based on parent-child relations between nodes
 */
public class PathOutageProvider extends AbstractTopologyProvider {

	public static final String NAMESPACE = "pathoutage";

	private NodeDao nodeDao;
	private PathOutageStatusProvider statusProvider;
	private DirectedSparseGraph<PathOutageVertex, AbstractEdge> sparseGraph;

	public PathOutageProvider(NodeDao nodeDao, PathOutageStatusProvider pathOutageStatusProvider) {
		super(NAMESPACE);
		this.nodeDao = nodeDao;
		this.statusProvider = pathOutageStatusProvider;
		this.sparseGraph = null;
	}

	@Override
	public void refresh() {
		load();
	}

	@Override
	public Defaults getDefaults() {
		// Current implementation of default focus strategy either creates a DefaultVertexHopCriteria for
		// semantic zoom level 0 and the node with the worst state; or for the first node (if all nodes have state NORMAL)
		return new Defaults()
				.withSemanticZoomLevel(0)
				.withPreferredLayout("Hierarchy Layout")
				.withCriteria(() -> {
					final Map<? extends VertexRef, ? extends Status> resultMap = statusProvider.getStatusForVertices(graph, Lists.newArrayList(graph.getVertices()), new Criteria[0]);
					final Optional<? extends Map.Entry<? extends VertexRef, ? extends Status>> max = resultMap.entrySet().stream().max(Comparator.comparing(e -> OnmsSeverity.get(e.getValue().computeStatus())));
					if (max.isPresent()) {
						return Lists.newArrayList(new DefaultVertexHopCriteria(max.get().getKey()));
					} else if (graph.getVertexTotalCount() > 0) {
						return Lists.newArrayList(new DefaultVertexHopCriteria(graph.getVertices().get(0)));
					} else {
						return Lists.newArrayList();
					}
				});
	}

	@Override
	public SelectionChangedListener.Selection getSelection(List<VertexRef> selectedVertices, ContentType type) {
		// only consider vertices of this namespace and of the correct type
		final List<Integer> nodeIds = selectedVertices.stream()
				.filter(v -> NAMESPACE.equals((v.getNamespace())))
				.filter(v -> v instanceof AbstractVertex)
				.map(v -> (AbstractVertex) v)
				.map(v -> v.getNodeID())
				.filter(id -> id != null)
				.collect(Collectors.toList());

		if (type == ContentType.Alarm) {
			return new SelectionChangedListener.AlarmNodeIdSelection(nodeIds);
		}
		if (type == ContentType.Node) {
			return new SelectionChangedListener.IdSelection<>(nodeIds);
		}
		return SelectionChangedListener.Selection.NONE;
	}

	@Override
	public boolean contributesTo(ContentType type) {
		return ContentType.Alarm == type || ContentType.Node == type;
	}

	private void load() {
		List<OnmsNode> nodes = nodeDao.findAll();

		this.sparseGraph = new DirectedSparseGraph();

		// Add vertices and edges to graph
		int edgeID = 0;
		for (OnmsNode node : nodes) {
			PathOutageVertex vertexChild = new PathOutageVertex(node);
			if (node.getParent() == null) {
				this.sparseGraph.addVertex(vertexChild);
			} else {
				PathOutageVertex vertexParent = new PathOutageVertex(node.getParent());
				vertexChild.setParent(vertexParent);
				this.sparseGraph.addVertex(vertexChild);
				this.sparseGraph.addVertex(vertexParent);
				this.sparseGraph.addEdge(new AbstractEdge(NAMESPACE, String.valueOf(edgeID), vertexParent, vertexChild),
						vertexParent, vertexChild, EdgeType.DIRECTED);
				edgeID++;
			}
		}

		// Index vertices, store indexed vertices in a separate list
		List<PathOutageVertex> tempVertices = new ArrayList<>();
		for (PathOutageVertex vertex : this.sparseGraph.getVertices()) {
			Collection<PathOutageVertex> predecessors = this.sparseGraph.getPredecessors(vertex);
			if (predecessors.isEmpty()) {
				vertex.setLevel(0);
				tempVertices.add(vertex);
				tempVertices.addAll(setLevel(this.sparseGraph, vertex, 1));
			}
		}

		// Initialize vertices and edges of a base topology provider
		graph.resetContainer();
		for (PathOutageVertex customVertex : tempVertices) {
			graph.addVertices(customVertex);
		}

		for (AbstractEdge abstractEdge : this.sparseGraph.getEdges()) {
			graph.addEdges(abstractEdge);
		}
	}

	/**
	 * Recursive method for calculating the level of a given vertex
	 * @param graph Graph with all vertices
	 * @param vertex Current vertex
	 * @param newLevel New level value
	 * @return List with vertices with calculated level values
	 */
	private List<PathOutageVertex> setLevel(DirectedSparseGraph<PathOutageVertex, AbstractEdge> graph, PathOutageVertex vertex, int newLevel) {
		List<PathOutageVertex> result = new ArrayList<>();
		for (PathOutageVertex cVertex : graph.getSuccessors(vertex)) {
			cVertex.setLevel(newLevel);
			result.add(cVertex);
			result.addAll(setLevel(graph, cVertex, newLevel + 1));
		}
		return result;
	}
}