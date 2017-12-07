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

package org.opennms.netmgt.bsm.service.model.graph.internal;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.edge.ChildEdge;
import org.opennms.netmgt.bsm.service.model.edge.Edge;
import org.opennms.netmgt.bsm.service.model.edge.EdgeVisitor;
import org.opennms.netmgt.bsm.service.model.edge.IpServiceEdge;
import org.opennms.netmgt.bsm.service.model.edge.ReductionKeyEdge;
import org.opennms.netmgt.bsm.service.model.functions.map.Identity;
import org.opennms.netmgt.bsm.service.model.functions.reduce.HighestSeverity;
import org.opennms.netmgt.bsm.service.model.graph.BusinessServiceGraph;
import org.opennms.netmgt.bsm.service.model.graph.GraphEdge;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

public class BusinessServiceGraphImpl extends DirectedSparseMultigraph<GraphVertex, GraphEdge> implements BusinessServiceGraph  {
    private static final long serialVersionUID = -7575071727895407844L;

    private final static Identity MAP_IDENTITY = new Identity();
    private final static HighestSeverity REDUCE_HIGHEST_SEVERITY = new HighestSeverity();
    private final Map<Long, GraphVertex> m_verticesByBusinessServiceId = Maps.newHashMap();
    private final Map<Integer, GraphVertex> m_verticesByIpServiceId = Maps.newHashMap();
    private final Map<String, GraphVertex> m_verticesByReductionKey = Maps.newHashMap();
    private final Map<Long, GraphVertex> m_verticesByEdgeId = Maps.newHashMap();
    private final Map<Integer, Set<GraphVertex>> m_verticesByLevel = Maps.newHashMap();
    private final Map<Long, GraphEdge> m_edgesByEdgeId = Maps.newHashMap();

    public BusinessServiceGraphImpl(final List<? extends BusinessService> businessServices) {
        // Build the graph
        Objects.requireNonNull(businessServices).stream()
            .forEach(this::addBusinessServiceVertex);

        // Calculate and index the hierarchical levels
        calculateAndIndexLevels();
    }

    private GraphVertex addBusinessServiceVertex(BusinessService businessService) {
        // Use an existing vertex if we already created one
        GraphVertex businessServiceVertex = m_verticesByBusinessServiceId.get(businessService.getId());
        if (businessServiceVertex != null) {
            return businessServiceVertex;
        }

        // Create
        businessServiceVertex = new GraphVertexImpl(businessService.getReduceFunction(), businessService);
        // Add
        addVertex(businessServiceVertex);
        // Index
        m_verticesByBusinessServiceId.put(businessService.getId(), businessServiceVertex);

        for (Edge edge : businessService.getEdges()) {
            // Create the edge
            GraphEdge graphEdge = new GraphEdgeImpl(edge);

            // Use an existing vertex if we already created one
            final GraphVertex[] vertexForEdge = {getExistingVertex(edge)};

            // If we couldn't find an existing vertex, create one
            if (vertexForEdge[0] == null) {
                edge.accept(new EdgeVisitor<Void>() {

                    @Override
                    public Void visit(ChildEdge edge) {
                        vertexForEdge[0] = addBusinessServiceVertex(edge.getChild());
                        return null;
                    }

                    @Override
                    public Void visit(IpServiceEdge edge) {
                        // There are multiple reductions keys for this edge
                        // Create an intermediary vertex using the Most Critical reduction function
                        vertexForEdge[0] = new GraphVertexImpl(REDUCE_HIGHEST_SEVERITY, edge.getIpService());
                        addVertex(vertexForEdge[0]);
                        m_verticesByIpServiceId.put(vertexForEdge[0].getIpService().getId(), vertexForEdge[0]);

                        // SPECIAL CASE: Map the reductions keys to the intermediary vertex using the Identity map
                        for (String reductionKey : edge.getReductionKeys()) {
                            GraphVertex reductionKeyVertex = m_verticesByReductionKey.get(reductionKey);
                            if (reductionKeyVertex == null) { // not already added
                                reductionKeyVertex = new GraphVertexImpl(REDUCE_HIGHEST_SEVERITY, reductionKey);
                                addVertex(reductionKeyVertex);
                                m_verticesByReductionKey.put(reductionKey, reductionKeyVertex);
                            }
                            // Always add an edge
                            GraphEdgeImpl intermediaryEdge = new GraphEdgeImpl(MAP_IDENTITY);
                            addEdge(intermediaryEdge, vertexForEdge[0], reductionKeyVertex);
                        }
                        return null;
                    }

                    @Override
                    public Void visit(ReductionKeyEdge edge) {
                        String reductionKey = edge.getReductionKey();
                        vertexForEdge[0] = new GraphVertexImpl(REDUCE_HIGHEST_SEVERITY, edge.getReductionKey());
                        addVertex(vertexForEdge[0]);
                        m_verticesByReductionKey.put(reductionKey, vertexForEdge[0]);
                        return null;
                    }
                });
            }

            // Link and index
            addEdge(graphEdge, businessServiceVertex, vertexForEdge[0]);
            m_verticesByEdgeId.put(edge.getId(), vertexForEdge[0]);
            m_edgesByEdgeId.put(edge.getId(), graphEdge);
        }
        return businessServiceVertex;
    }

    private GraphVertex getExistingVertex(Edge edge) {
        return edge.accept(new EdgeVisitor<GraphVertex>() {

            @Override
            public GraphVertex visit(IpServiceEdge edge) {
                return m_verticesByIpServiceId.get(edge.getIpService().getId());
            }

            @Override
            public GraphVertex visit(ReductionKeyEdge edge) {
                return m_verticesByReductionKey.get(edge.getReductionKey());
            }

            @Override
            public GraphVertex visit(ChildEdge edge) {
                return m_verticesByBusinessServiceId.get(edge.getChild().getId());
            }
        });
    }

    private void calculateAndIndexLevels() {
        // Start by finding the root vertices
        // These are the vertices with no incoming edges
        final Set<GraphVertex> rootVertices = Sets.newHashSet();
        for (GraphVertex vertex : getVertices()) {
            if (getInEdges(vertex).size() == 0) {
                rootVertices.add(vertex);
            }
        }

        // Now calculate the distance of every node to each of the root nodes
        final GraphLevelIndexer<GraphVertex, GraphEdge> levelIndexer = new GraphLevelIndexer<>();
        levelIndexer.indexLevel(this, rootVertices);
        for (Entry<GraphVertex, Integer> entry : levelIndexer.getLevelMap().entrySet()) {
            final int level = entry.getValue().intValue();
            final GraphVertexImpl vertex = (GraphVertexImpl)entry.getKey();

            // Store the maximum level within the vertex
            vertex.setLevel(Math.max(level, vertex.getLevel()));
        }

        // Index the vertices by level
        for (GraphVertex vertex : getVertices()) {
            Set<GraphVertex> verticesAtLevel = m_verticesByLevel.get(vertex.getLevel());
            if (verticesAtLevel == null) {
                verticesAtLevel = Sets.newHashSet();
                m_verticesByLevel.put(vertex.getLevel(), verticesAtLevel);
            }
            verticesAtLevel.add(vertex);
        }
    }

    @Override
    public GraphVertex getVertexByBusinessServiceId(Long id) {
        return m_verticesByBusinessServiceId.get(id);
    }

    @Override
    public GraphVertex getVertexByIpServiceId(Integer id) {
        return m_verticesByIpServiceId.get(id);
    }

    @Override
    public GraphVertex getVertexByReductionKey(String reductionKey) {
        return m_verticesByReductionKey.get(reductionKey);
    }

    @Override
    public GraphVertex getVertexByEdgeId(Long id) {
        return m_verticesByEdgeId.get(id);
    }

    @Override
    public GraphEdge getGraphEdgeByEdgeId(Long id) {
        return m_edgesByEdgeId.get(id);
    }

    @Override
    public Set<String> getReductionKeys() {
        return m_verticesByReductionKey.keySet();
    }

    @Override
    public Set<GraphVertex> getVerticesByLevel(int level) {
        final Set<GraphVertex> verticesAtLevel = m_verticesByLevel.get(level);
        return verticesAtLevel != null ? verticesAtLevel : Collections.emptySet();
    }
}
