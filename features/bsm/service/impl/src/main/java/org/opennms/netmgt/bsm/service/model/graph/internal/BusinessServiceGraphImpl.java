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
package org.opennms.netmgt.bsm.service.model.graph.internal;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.opennms.netmgt.bsm.service.model.BusinessService;
import org.opennms.netmgt.bsm.service.model.IpService;
import org.opennms.netmgt.bsm.service.model.edge.ApplicationEdge;
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
    private final Map<Integer, GraphVertex> m_verticesByApplicationId = Maps.newHashMap();
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
                        addReductionKeyVerticesToVertex(vertexForEdge[0], edge.getReductionKeys());
                        return null;
                    }

                    @Override
                    public Void visit(ApplicationEdge edge) {
                        vertexForEdge[0] = new GraphVertexImpl(REDUCE_HIGHEST_SEVERITY, edge.getApplication());
                        addVertex(vertexForEdge[0]);
                        m_verticesByApplicationId.put(vertexForEdge[0].getApplication().getId(), vertexForEdge[0]);

                        for (IpService ipService : edge.getApplication().getIpServices()) {
                            final GraphVertex ipServiceVertex = new GraphVertexImpl(REDUCE_HIGHEST_SEVERITY, ipService);
                            final GraphEdgeImpl ipVertexEdgeEdge = new GraphEdgeImpl(MAP_IDENTITY);
                            addVertex(ipServiceVertex);
                            m_verticesByIpServiceId.put(ipServiceVertex.getIpService().getId(), ipServiceVertex);
                            addEdge(ipVertexEdgeEdge, vertexForEdge[0], ipServiceVertex);
                            addReductionKeyVerticesToVertex(ipServiceVertex, ipService.getReductionKeys());
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

    private void addReductionKeyVerticesToVertex(GraphVertex vertex, Set<String> reductionKeys) {
        for (String reductionKey : reductionKeys) {
            GraphVertex reductionKeyVertex = m_verticesByReductionKey.get(reductionKey);
            if (reductionKeyVertex == null) {
                reductionKeyVertex = new GraphVertexImpl(REDUCE_HIGHEST_SEVERITY, reductionKey);
                addVertex(reductionKeyVertex);
                m_verticesByReductionKey.put(reductionKey, reductionKeyVertex);
            }
            GraphEdgeImpl intermediaryEdge = new GraphEdgeImpl(MAP_IDENTITY);
            addEdge(intermediaryEdge, vertex, reductionKeyVertex);
        }
    }

    private GraphVertex getExistingVertex(Edge edge) {
        return edge.accept(new EdgeVisitor<GraphVertex>() {

            @Override
            public GraphVertex visit(IpServiceEdge edge) {
                return m_verticesByIpServiceId.get(edge.getIpService().getId());
            }

            @Override
            public GraphVertex visit(ApplicationEdge edge) {
                return m_verticesByApplicationId.get(edge.getApplication().getId());
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
    public GraphVertex getVertexByApplicationId(Integer id) {
        return m_verticesByApplicationId.get(id);
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
