/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
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
		resetContainer();
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
					Map<VertexRef, Status> resultMap = statusProvider.getStatusForVertices(this, Lists.newArrayList(this.getVertices()), new Criteria[0]);
					Optional<Map.Entry<VertexRef, Status>> max = resultMap.entrySet().stream().max(Comparator.comparing(e -> OnmsSeverity.get(e.getValue().computeStatus())));
					if (max.isPresent()) {
						return Lists.newArrayList(new VertexHopGraphProvider.DefaultVertexHopCriteria(max.get().getKey()));
					} else if (this.getVertexTotalCount() > 0) {
						return Lists.newArrayList(new VertexHopGraphProvider.DefaultVertexHopCriteria(this.getVertices().get(0)));
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
		for (PathOutageVertex customVertex : tempVertices) {
			this.addVertices(customVertex);
		}

		for (AbstractEdge abstractEdge : this.sparseGraph.getEdges()) {
			this.addEdges(abstractEdge);
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