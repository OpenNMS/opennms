/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019-2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api.topo;

import static org.opennms.features.topology.api.support.hops.CriteriaUtils.getCollapsedCriteria;
import static org.opennms.features.topology.api.support.hops.CriteriaUtils.getCollapsibleCriteria;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.support.SemanticZoomLevelCriteria;
import org.opennms.features.topology.api.support.hops.VertexHopCriteria;

public class CollapsibleGraph implements BackendGraph {

    private final Map<VertexRef,Integer> m_semanticZoomLevels = new LinkedHashMap<>();
    private final BackendGraph m_delegate;

    public CollapsibleGraph(BackendGraph delegate) {
        m_delegate = Objects.requireNonNull(delegate);
    }

    @Override
    public boolean containsVertexId(String id) {
        return containsVertexId(new DefaultVertexRef(getNamespace(), id));
    }

    @Override
    public boolean containsVertexId(VertexRef id, Criteria... criteria) {
        for (CollapsibleCriteria criterium : getCollapsedCriteria(criteria)) {
            Vertex collapsed = criterium.getCollapsedRepresentation();
            if (new RefComparator().compare(collapsed, id) == 0) {
                return true;
            }
        }
        return m_delegate.containsVertexId(id, criteria);
    }

    @Override
    public Vertex getVertex(String namespace, String id) {
        return m_delegate.getVertex(namespace, id);
    }

    @Override
    public Vertex getVertex(VertexRef reference, Criteria... criteria) {
        for (CollapsibleCriteria criterium : getCollapsedCriteria(criteria)) {
            Vertex collapsed = criterium.getCollapsedRepresentation();
            if (new RefComparator().compare(collapsed, reference) == 0) {
                return collapsed;
            }
        }
        return m_delegate.getVertex(reference, criteria);
    }

    public Collection<Vertex> getVertices(int semanticZoomLevel, Criteria... criteria) {
        final List<Vertex> displayVertices = new ArrayList<>();
        for (Vertex v : getVertices(criteria)) {
            int vzl = getSemanticZoomLevel(v);
            if (vzl <= semanticZoomLevel) {
                displayVertices.add(v);
            }
        }
        return displayVertices;
    }

    public int getSemanticZoomLevel(VertexRef vertex) {
        Integer szl = m_semanticZoomLevels.get(vertex);
        return szl == null ? 0 : szl;
    }

    public Set<VertexRef> getFocusNodes(Criteria... criteria) {
        Set<VertexRef> focusNodes = new HashSet<VertexRef>();
        for(Criteria criterium : criteria) {
            try {
                VertexHopCriteria hopCriterium = (VertexHopCriteria)criterium;
                focusNodes.addAll(hopCriterium.getVertices());
            } catch (ClassCastException e) {}
        }
        return focusNodes;
    }

    public int getMaxSemanticZoomLevel(Criteria... criteria) {
        for (Criteria criterium : criteria) {
            // See if there is a SemanticZoomLevelCriteria set and if so, use it
            // to restrict the number of times we iterate over the graph
            try {
                SemanticZoomLevelCriteria szlCriteria = (SemanticZoomLevelCriteria)criterium;
                return szlCriteria.getSemanticZoomLevel();
            } catch (ClassCastException e) {}
        }
        return 100;
    }

    @Override
    public List<Vertex> getVertices(Criteria... criteria) {
        // Otherwise consider vertices szl and focus nodes
        Set<VertexRef> focusNodes = getFocusNodes(criteria);
        int maxSemanticZoomLevel = getMaxSemanticZoomLevel(criteria);

        // Clear the existing semantic zoom level values
        m_semanticZoomLevels.clear();
        int semanticZoomLevel = 0;

        // If we didn't find any matching nodes among the focus nodes...
        if (focusNodes.size() < 1) {
            // ...then return an empty list of vertices, but include collapsed vertices
            collapseVertices(Collections.emptySet(), getCollapsibleCriteria(criteria, false));
        }


        Map<VertexRef, Set<VertexRef>> neighborMap = new HashMap<>();
        List<Edge> edges = m_delegate.getEdges(criteria);
        for(Edge edge : edges) {
            VertexRef src = edge.getSource().getVertex();
            VertexRef tgt = edge.getTarget().getVertex();
            Set<VertexRef> srcNeighbors = neighborMap.get(src);
            if (srcNeighbors == null) {
                srcNeighbors = new HashSet<>();
                neighborMap.put(src, srcNeighbors);
            }
            srcNeighbors.add(tgt);

            Set<VertexRef> tgtNeighbors = neighborMap.get(tgt);
            if (tgtNeighbors == null) {
                tgtNeighbors = new HashSet<>();
                neighborMap.put(tgt, tgtNeighbors);
            }
            tgtNeighbors.add(src);
        }

        Set<Vertex> processed = new HashSet<>();
        Set<VertexRef> neighbors = new HashSet<>();
        Set<VertexRef> workingSet = new HashSet<>(focusNodes);
        // Put a limit on the SZL in case we infinite loop for some reason
        while (semanticZoomLevel <= maxSemanticZoomLevel && workingSet.size() > 0) {
            neighbors.clear();

            for(VertexRef vertexRef : workingSet) {
                // Only consider Vertex if it is actually not filtered by the criteria (which it might)
                Vertex vertex = getVertex(vertexRef, criteria);
                if (vertex != null) {
                    if (m_semanticZoomLevels.containsKey(vertexRef)) {
                        throw new IllegalStateException("Calculating semantic zoom level for vertex that has already been calculated: " + vertexRef.toString());
                    }
                    m_semanticZoomLevels.put(vertexRef, semanticZoomLevel);
                    Set<VertexRef> refs = neighborMap.get(vertexRef);
                    if (refs != null) {
                        neighbors.addAll(refs);
                    }
                    processed.add(vertex);
                }
            }

            neighbors.removeAll(processed);

            workingSet.clear();
            workingSet.addAll(neighbors);

            // Increment the semantic zoom level
            semanticZoomLevel++;
        }

        processed = collapseVertices(processed, getCollapsedCriteria(criteria));

        return new ArrayList<Vertex>(processed);
    }

    @Override
    public List<Vertex> getVertices(Collection<? extends VertexRef> references, Criteria... criteria) {
        return m_delegate.getVertices(references, criteria);
    }

    @Override
    public void addVertexListener(VertexListener vertexListener) {
        m_delegate.addVertexListener(vertexListener);
    }

    @Override
    public void removeVertexListener(VertexListener vertexListener) {
        m_delegate.removeVertexListener(vertexListener);
    }

    @Override
    public void clearVertices() {
        m_delegate.clearVertices();
    }

    @Override
    public int getVertexTotalCount() {
        return m_delegate.getVertexTotalCount();
    }

    @Override
    public void addVertices(Vertex... vertices) {
        m_delegate.addVertices(vertices);
    }

    @Override
    public void removeVertex(VertexRef... vertexId) {
        m_delegate.removeVertex(vertexId);
    }

    @Override
    public Edge getEdge(String namespace, String id) {
        return m_delegate.getEdge(namespace, id);
    }

    @Override
    public Edge getEdge(EdgeRef reference) {
        return m_delegate.getEdge(reference);
    }

    public static Set<Vertex> collapseVertices(Set<Vertex> vertices, CollapsibleCriteria[] criteria) {
        final Set<Vertex> retval = new HashSet<>();
        final Set<Vertex> verticesToProcess = new HashSet<>(vertices);

        // Replace all vertices by its collapsed representation
        for (CollapsibleCriteria collapsibleCriteria : criteria) {
            if (collapsibleCriteria.isCollapsed()) {
                final Set<VertexRef> verticesRepresentedByCollapsible = collapsibleCriteria.getVertices().stream()
                        .filter(vertices::contains)
                        .collect(Collectors.toSet());
                verticesToProcess.removeAll(verticesRepresentedByCollapsible);
                retval.add(collapsibleCriteria.getCollapsedRepresentation());
            }
        }

        // Not all vertices may be represented by a collapsed version - either their criteria is not collapsed
        // or it is a pure vertex - therefore those are added afterwards
        retval.addAll(verticesToProcess);
        verticesToProcess.clear();

        return retval;
    }

    public static Map<VertexRef,Set<Vertex>> getMapOfVerticesToCollapsedVertices(CollapsibleCriteria[] criteria) {
        // Make a map of all of the vertices to their new collapsed representations
        Map<VertexRef,Set<Vertex>> vertexToCollapsedVertices = new TreeMap<>(new RefComparator());
        for (CollapsibleCriteria criterium : criteria) {
            Set<VertexRef> criteriaVertices = criterium.getVertices();
            if (criteriaVertices.size() > 0) {
                Vertex collapsedVertex = criterium.getCollapsedRepresentation();
                for (VertexRef criteriaVertex : criteriaVertices) {
                    Set<Vertex> collapsedVertices = vertexToCollapsedVertices.get(criteriaVertex);
                    if (collapsedVertices == null) {
                        collapsedVertices = new HashSet<>();
                        vertexToCollapsedVertices.put(criteriaVertex, collapsedVertices);
                    }
                    collapsedVertices.add(collapsedVertex);
                }
            }
        }
        return vertexToCollapsedVertices;
    }

    /**
     * This function assumes that all criteria passed in are marked as collapsed.
     * @param edges
     * @param criteria
     * @return
     */
    public static Set<Edge> collapseEdges(Set<Edge> edges, CollapsibleCriteria[] criteria) {

        // Make a map of all of the vertices to their new collapsed representations
        Map<VertexRef,Set<Vertex>> vertexToCollapsedVertices = getMapOfVerticesToCollapsedVertices(criteria);

        if (vertexToCollapsedVertices.size() > 0) {
            Set<Edge> retval = new HashSet<>();
            for (Edge edge : edges) {
                // Add the original edge to retval unless we replace it with an edge that points to a
                // collapsed vertex
                boolean addOriginalEdge = true;

                // If the source vertex is in the collapsed list...
                Set<Vertex> collapsedSources = vertexToCollapsedVertices.get(edge.getSource().getVertex());
                if (collapsedSources != null) {
                    for (VertexRef collapsedSource : collapsedSources) {
                        // Add a new edge with the source as the collapsed vertex
                        Edge newCollapsedEdge = edge.clone();
                        newCollapsedEdge.setId("collapsedSource-" + newCollapsedEdge.getId());
                        newCollapsedEdge.getSource().setVertex(collapsedSource);
                        retval.add(newCollapsedEdge);
                    }
                    // Since we just added a replacement edge, don't add the original
                    addOriginalEdge = false;
                }

                Set<Vertex> collapsedTargets = vertexToCollapsedVertices.get(edge.getTarget().getVertex());
                if (collapsedTargets != null) {
                    for (VertexRef collapsedTarget : collapsedTargets) {
                        // Add a new edge with the target as the collapsed vertex
                        Edge newCollapsedEdge = edge.clone();
                        newCollapsedEdge.setId("collapsedTarget-" + newCollapsedEdge.getId());
                        newCollapsedEdge.getTarget().setVertex(collapsedTarget);
                        retval.add(newCollapsedEdge);
                    }
                    // Since we just added a replacement edge, don't add the original
                    addOriginalEdge = false;
                }

                // If both the source and target have been collapsed, connect all of the collapsed
                // representations to each other. This will allow collapsed groups to connect to one
                // another.
                //
                if (collapsedSources != null && collapsedTargets != null) {
                    for (VertexRef collapsedEndpoint : collapsedSources) {
                        for (VertexRef collapsedTarget : collapsedTargets) {
                            // Add a new edge with the target as the collapsed vertex
                            Edge newCollapsedEdge = edge.clone();
                            newCollapsedEdge.setId("collapsed-" + newCollapsedEdge.getId());
                            newCollapsedEdge.getSource().setVertex(collapsedEndpoint);
                            newCollapsedEdge.getTarget().setVertex(collapsedTarget);
                            retval.add(newCollapsedEdge);
                        }
                    }
                    // Since we just added a replacement edge, don't add the original
                    addOriginalEdge = false;
                }

                // Add the original edge if it wasn't replaced with an edge to a collapsed vertex
                if (addOriginalEdge) {
                    retval.add(edge);
                }
            }
            return retval;
        } else {
            return edges;
        }
    }

    // Returns the content of the collapsible
    public List<Vertex> getVertices(CollapsibleRef collapsibleRef, Criteria... criteria) {
        for (CollapsibleCriteria criterium : getCollapsedCriteria(criteria)) {
            if (new RefComparator().compare(criterium.getCollapsedRepresentation(), collapsibleRef) == 0) {
                return getVertices(criterium.getVertices());
            }
        }
        return Collections.emptyList();
    }

    @Override
    public List<Edge> getEdges(Criteria... criteria) {
        Set<Edge> retval = new HashSet<>(m_delegate.getEdges(criteria));
        retval = collapseEdges(retval, getCollapsedCriteria(criteria));
        return new ArrayList<>(retval);
    }

    @Override
    public List<Edge> getEdges(Collection<? extends EdgeRef> references) {
        return m_delegate.getEdges(references);
    }

    @Override
    public void addEdgeListener(EdgeListener listener) {
        m_delegate.addEdgeListener(listener);
    }

    @Override
    public void removeEdgeListener(EdgeListener listener) {
        m_delegate.removeEdgeListener(listener);
    }

    @Override
    public void clearEdges() {
        m_delegate.clearEdges();
    }

    @Override
    public int getEdgeTotalCount() {
        return m_delegate.getEdgeTotalCount();
    }

    @Override
    public EdgeRef[] getEdgeIdsForVertex(VertexRef vertex) {
        return m_delegate.getEdgeIdsForVertex(vertex);
    }

    @Override
    public Map<VertexRef, Set<EdgeRef>> getEdgeIdsForVertices(VertexRef... vertices) {
        return m_delegate.getEdgeIdsForVertices(vertices);
    }

    @Override
    public void addEdges(Edge... edges) {
        m_delegate.addEdges(edges);
    }

    @Override
    public void removeEdges(EdgeRef... edges) {
        m_delegate.removeEdges(edges);
    }

    @Override
    public Edge connectVertices(String edgeId, VertexRef sourceVertextId, VertexRef targetVertextId) {
        return m_delegate.connectVertices(edgeId, sourceVertextId, targetVertextId);
    }

    @Override
    public void resetContainer() {
        m_delegate.resetContainer();
    }

    @Override
    public String getNamespace() {
        return m_delegate.getNamespace();
    }

    @Override
    public boolean contributesTo(String namespace) {
        return m_delegate.contributesTo(namespace);
    }
}
