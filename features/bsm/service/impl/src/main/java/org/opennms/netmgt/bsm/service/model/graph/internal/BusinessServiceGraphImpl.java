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

import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.ReadOnlyBusinessService;
import org.opennms.netmgt.bsm.service.model.edge.ro.ReadOnlyChildEdge;
import org.opennms.netmgt.bsm.service.model.edge.ro.ReadOnlyEdge;
import org.opennms.netmgt.bsm.service.model.edge.ro.ReadOnlyIpServiceEdge;
import org.opennms.netmgt.bsm.service.model.edge.ro.ReadOnlyReductionKeyEdge;
import org.opennms.netmgt.bsm.service.model.functions.map.Identity;
import org.opennms.netmgt.bsm.service.model.functions.reduce.MostCritical;
import org.opennms.netmgt.bsm.service.model.graph.BusinessServiceGraph;
import org.opennms.netmgt.bsm.service.model.graph.GraphEdge;
import org.opennms.netmgt.bsm.service.model.graph.GraphVertex;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import edu.uci.ics.jung.algorithms.shortestpath.BFSDistanceLabeler;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;

public class BusinessServiceGraphImpl extends DirectedSparseMultigraph<GraphVertex, GraphEdge> implements BusinessServiceGraph  {
    private static final long serialVersionUID = -7575071727895407844L;

    private final static Identity MAP_IDENTITY = new Identity();
    private final static MostCritical REDUCE_MOST_CRITICAL = new MostCritical();
    private final Map<Long, GraphVertex> m_verticesByBusinessServiceId = Maps.newHashMap();
    private final Map<Long, GraphVertex> m_verticesByIpServiceId = Maps.newHashMap();
    private final Map<String, GraphVertex> m_verticesByReductionKey = Maps.newHashMap();
    private final Map<Long, GraphVertex> m_verticesByEdgeId = Maps.newHashMap();
    private final Map<Integer, Set<GraphVertex>> m_verticesByLevel = Maps.newHashMap();

    public BusinessServiceGraphImpl(final List<? extends ReadOnlyBusinessService> businessServices) {
        // Build the graph
        Objects.requireNonNull(businessServices).stream()
            .forEach(b -> addBusinessServiceVertex(b));

        // Calculate and index the hierarchical levels
        calculateAndIndexLevels();
    }

    private GraphVertex addBusinessServiceVertex(ReadOnlyBusinessService businessService) {
        // Use an existing vertex if we already created one
        GraphVertex businessServiceVertex = m_verticesByBusinessServiceId.get(businessService.getId());
        if (businessServiceVertex != null) {
            return businessServiceVertex;
        }

        // Create
        businessServiceVertex = new GraphVertexImpl(businessService);
        // Add
        addVertex(businessServiceVertex);
        // Index
        m_verticesByBusinessServiceId.put(businessService.getId(), businessServiceVertex);

        for (ReadOnlyEdge edge : businessService.getEdges()) {
            // Create the edge
            GraphEdge graphEdge = new GraphEdgeImpl(edge);

            // Use an existing vertex if we already created one
            GraphVertex vertexForEdge = m_verticesByEdgeId.get(Objects.requireNonNull(edge.getId(), "Edges must have ids."));
            if (vertexForEdge == null && edge instanceof ReadOnlyChildEdge) {
                vertexForEdge = m_verticesByBusinessServiceId.get(((ReadOnlyChildEdge)edge).getChild().getId());
            }

            // If we couldn't find an existing vertex, create one
            if (vertexForEdge == null) {
                if (edge instanceof ReadOnlyChildEdge) {
                    vertexForEdge = addBusinessServiceVertex(((ReadOnlyChildEdge)edge).getChild());
                } else if (edge instanceof ReadOnlyReductionKeyEdge) {
                    String reductionKey = ((ReadOnlyReductionKeyEdge)edge).getReductionKey();
                    vertexForEdge = new GraphVertexImpl(REDUCE_MOST_CRITICAL, reductionKey, edge);
                    addVertex(vertexForEdge);
                    m_verticesByReductionKey.put(reductionKey, vertexForEdge);
                } else if (edge instanceof ReadOnlyIpServiceEdge) {
                    // There are multiple reductions keys for this edge
                    // Create an intermediary vertex using the Most Critical reduction function
                    IpService ipService = ((ReadOnlyIpServiceEdge)edge).getIpService();
                    vertexForEdge = new GraphVertexImpl(REDUCE_MOST_CRITICAL, null, edge);
                    addVertex(vertexForEdge);
                    m_verticesByIpServiceId.put(Long.valueOf(ipService.getId()), vertexForEdge);

                    // Map the reductions keys to the intermediary vertex using the Identity map
                    for (String reductionKey : edge.getReductionKeys()) {
                        GraphEdgeImpl intermediaryEdge = new GraphEdgeImpl(MAP_IDENTITY);
                        GraphVertexImpl reductionKeyVertex = new GraphVertexImpl(REDUCE_MOST_CRITICAL, reductionKey, edge);
                        addVertex(reductionKeyVertex);
                        m_verticesByReductionKey.put(reductionKey, reductionKeyVertex);
                        addEdge(intermediaryEdge, vertexForEdge, reductionKeyVertex);
                    }
                } else {
                    throw new IllegalArgumentException("Unsupported edge of type: " + edge.getClass().getCanonicalName());
                }
            }

            // Link and index
            addEdge(graphEdge, businessServiceVertex, vertexForEdge);
            m_verticesByEdgeId.put(edge.getId(), vertexForEdge);
        }
        return businessServiceVertex;
    }

    private void calculateAndIndexLevels() {
        // Start by finding the root vertices
        // These are the vertices with no incoming edges
        final Set<GraphVertexImpl> rootVertices = Sets.newHashSet();
        for (GraphVertex vertex : getVertices()) {
            if (getInEdges(vertex).size() == 0) {
                rootVertices.add((GraphVertexImpl)vertex);
            }
        }

        // Now calculate the distance of every node to each of the root nodes
        for (GraphVertexImpl rootVertex : rootVertices) {
            final BFSDistanceLabeler<GraphVertex, GraphEdge> bfsDistance = new BFSDistanceLabeler<>();
            bfsDistance.labelDistances(this, rootVertex);
            for (Entry<GraphVertex, Number> entry : bfsDistance.getDistanceDecorator().entrySet()) {
                final int level = entry.getValue().intValue();
                final GraphVertexImpl vertex = (GraphVertexImpl)entry.getKey();

                // Store the maximum level within the vertex
                vertex.setLevel(Math.max(level, vertex.getLevel()));
            }
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
    public GraphVertex getVertexByIpServiceId(Long id) {
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
    public Set<String> getReductionKeys() {
        return m_verticesByReductionKey.keySet();
    }

    @Override
    public Set<GraphVertex> getVerticesByLevel(int level) {
        final Set<GraphVertex> verticesAtLevel = m_verticesByLevel.get(level);
        return verticesAtLevel != null ? verticesAtLevel : Collections.emptySet();
    }
}
