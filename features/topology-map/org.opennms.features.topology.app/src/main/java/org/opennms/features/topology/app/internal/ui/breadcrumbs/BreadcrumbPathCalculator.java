/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.app.internal.ui.breadcrumbs;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.opennms.features.topology.api.TopologyServiceClient;
import org.opennms.features.topology.api.support.IgnoreHopCriteria;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

import com.google.common.collect.Lists;

import edu.uci.ics.jung.algorithms.shortestpath.UnweightedShortestPath;
import edu.uci.ics.jung.graph.DirectedSparseGraph;

public class BreadcrumbPathCalculator {

    private static class IdGenerator {
        private long counter = 0;
        public long nextId() {
            return ++counter;
        }
    }

    protected static final Vertex rootVertex = new AbstractVertex("$$outer-space$$", "$$pathService.root$$");

    static PathTree findPath(TopologyServiceClient topologyServiceClient, Collection<VertexRef> vertices) {
        Objects.requireNonNull(topologyServiceClient);
        Objects.requireNonNull(vertices);

        final Map<VertexRef, EdgeRef> incomingEdgeMap = getIncomingEdgeMap(topologyServiceClient);
        final PathTree pathTree = new PathTree();
        for (VertexRef eachVertex : vertices) {
            List<VertexRef> path = findPath(incomingEdgeMap, eachVertex);
            pathTree.addPath(path);
        }
        return pathTree;
    }

    static List<VertexRef> findPath(Map<VertexRef, EdgeRef> incomingEdgeMap, VertexRef vertexToFind) {
        Objects.requireNonNull(incomingEdgeMap);
        Objects.requireNonNull(vertexToFind);

        List<VertexRef> vertexRefs = Lists.newArrayList();
        if (incomingEdgeMap.get(vertexToFind) != null) {
            addPathRecursively(vertexRefs, vertexToFind, incomingEdgeMap);

            if (vertexRefs.size() >= 2) {
                Iterator<VertexRef> it = vertexRefs.iterator();
                VertexRef left = it.next();
                while (it.hasNext()) {
                    VertexRef right = it.next();
                    if (left.getNamespace().equals(right.getNamespace())) {
                        it.remove();
                    }
                    left = right;
                }
            }
        }
        return vertexRefs;
    }

    private static void addPathRecursively(List<VertexRef> vertexRefs, VertexRef vertexToFind, Map<VertexRef, EdgeRef> incomingEdgeMap) {
        if (incomingEdgeMap.get(vertexToFind) != null) {
            vertexRefs.add(0, vertexToFind);
            addPathRecursively(vertexRefs, ((Edge) incomingEdgeMap.get(vertexToFind)).getSource().getVertex(), incomingEdgeMap);
        }
    }

    static Map<VertexRef, EdgeRef> getIncomingEdgeMap(TopologyServiceClient topologyServiceClient) {
        // Convert to JUNG graph
        // We build one big graph out of all graph providers in order to determine the shortest path between each vertex
        // when we want to calculate the SHORTEST_PATH_TO_ROOT
        final DirectedSparseGraph<VertexRef, EdgeRef> sparseGraph = new DirectedSparseGraph<>();
        topologyServiceClient.getGraphProviders().forEach(eachGraph -> {
            for (Vertex eachVertex : eachGraph.getVertices(new IgnoreHopCriteria())) {
                sparseGraph.addVertex(eachVertex);
            }
            for (EdgeRef eachEdge : eachGraph.getEdges()) {
                sparseGraph.addEdge(eachEdge, ((Edge) eachEdge).getSource().getVertex(), ((Edge) eachEdge).getTarget().getVertex());
            }
        });

        // Link the layers
        final IdGenerator idGenerator = new IdGenerator();
        sparseGraph.getVertices().forEach(eachVertex -> {
            topologyServiceClient.getOppositeVertices(eachVertex).forEach(oppositeVertex -> {
                sparseGraph.addEdge(new AbstractEdge("$$outer-space$$", "" + idGenerator.nextId(), eachVertex, oppositeVertex), eachVertex, oppositeVertex);
            });
        });

        // Create dummy root
        sparseGraph.addVertex(rootVertex);
        for (Vertex eachVertex : topologyServiceClient.getDefaultGraphProvider().getVertices(new IgnoreHopCriteria())) {
            sparseGraph.addEdge(new AbstractEdge("$$outer-space$$", "" + idGenerator.nextId(), rootVertex, eachVertex), rootVertex, eachVertex);
        }

        // Build shortest path for graph
        final UnweightedShortestPath<VertexRef, EdgeRef> shortestPath = new UnweightedShortestPath<>(sparseGraph);
        Map<VertexRef, EdgeRef> incomingEdgeMap = shortestPath.getIncomingEdgeMap(rootVertex);
        return incomingEdgeMap;
    }
}
