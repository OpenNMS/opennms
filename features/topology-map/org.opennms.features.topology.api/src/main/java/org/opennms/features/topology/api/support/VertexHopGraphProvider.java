/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api.support;

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

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionAware;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.topo.CollapsibleCriteria;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.Defaults;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeListener;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.RefComparator;
import org.opennms.features.topology.api.topo.TopologyProviderInfo;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexListener;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Sets;

/**
 * This class will be used to filter a topology so that the semantic zoom level is
 * interpreted as a hop distance away from a set of selected vertices. The vertex 
 * selection is specified using sets of {@link VertexHopCriteria} filters.
 * 
 * @author Seth
 */
public class VertexHopGraphProvider implements GraphProvider, SelectionAware {
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(VertexHopGraphProvider.class);

    public static WrappedVertexHopCriteria getWrappedVertexHopCriteria(GraphContainer graphContainer) {
        final Set<VertexHopCriteria> vertexHopCriterias = Criteria.getCriteriaForGraphContainer(graphContainer, VertexHopCriteria.class);
        return new WrappedVertexHopCriteria(vertexHopCriterias);
    }

    public static CollapsibleCriteria[] getCollapsedCriteriaForContainer(GraphContainer graphContainer) {
        return getCollapsedCriteria(graphContainer.getCriteria());
    }

    public static CollapsibleCriteria[] getCollapsedCriteria(Criteria[] criteria) {
        return getCollapsibleCriteria(criteria, true);
    }

    public static CollapsibleCriteria[] getCollapsibleCriteriaForContainer(GraphContainer graphContainer) {
        return getCollapsibleCriteria(graphContainer.getCriteria());
    }

    public static CollapsibleCriteria[] getCollapsibleCriteria(Criteria[] criteria) {
        return getCollapsibleCriteria(criteria, false);
    }

    public static CollapsibleCriteria[] getCollapsibleCriteria(Criteria[] criteria, boolean onlyCollapsed) {
        List<CollapsibleCriteria> retval = new ArrayList<CollapsibleCriteria>();
        if (criteria != null) {
            for (Criteria criterium : criteria) {
                try {
                    CollapsibleCriteria hopCriteria = (CollapsibleCriteria)criterium;
                    if (onlyCollapsed) {
                        if (hopCriteria.isCollapsed()) {
                            retval.add(hopCriteria);
                        }
                    } else {
                        retval.add(hopCriteria);
                    }
                } catch (ClassCastException e) {}
            }
        }
        return retval.toArray(new CollapsibleCriteria[0]);
    }

    public abstract static class VertexHopCriteria extends Criteria {
        private String m_label = "";
        private String m_id = "";
        
        @Override
        public String toString() {
            return "Namespace:"+getNamespace()+", ID:"+getId()+", Label:"+getLabel();
        }

        //Adding explicit constructor because I found that this label must be set
        //for the focus list to have meaningful information in the focus list.
        public VertexHopCriteria(String label) {
        	m_label = label;
        }
        
        public VertexHopCriteria(String id, String label) {
        	m_id = id;
        	m_label = label;
        }

        @Override
        public ElementType getType() {
            return ElementType.VERTEX;
        }

        public abstract Set<VertexRef> getVertices();

        public String getLabel() {
            return m_label;
        }

        public void setLabel(String label) {
            m_label = label;
        }

        public void setId(String id){
            m_id = id;
        }

        public String getId(){
            return m_id;
        }

        public boolean isEmpty() {
            Set<VertexRef> vertices = getVertices();
            if (vertices == null) {
                return false;
            }
            return vertices.isEmpty();
        }
    }

    /**
     * Wrapper class to wrap a bunch of {@link VertexHopCriteria}.
     * There may be multiple {@link VertexHopCriteria} objects available.
     * However in the end it is easier to use this criteria object to wrap all available {@link VertexHopCriteria}
     * instead of iterating over all all the time and determine all vertices.
     */
    public static class WrappedVertexHopCriteria extends VertexHopCriteria {

        private final Set<VertexHopCriteria> criteriaList;

        public WrappedVertexHopCriteria(Set<VertexHopCriteria> vertexHopCriterias) {
            super("Wrapped Vertex Hop Criteria for all VertexHopCriteria in the currently selected GraphProvider");
            criteriaList = Objects.requireNonNull(vertexHopCriterias);
        }

        public void addCriteria(VertexHopCriteria criteria) {
            this.criteriaList.add(criteria);
        }

        @Override
        public Set<VertexRef> getVertices() {
            Set<VertexRef> vertices = criteriaList.stream()
                    .flatMap(criteria -> criteria.getVertices().stream())
                    .collect(Collectors.toSet());
            return vertices;
        }

        @Override
        public String getNamespace() {
            return "$wrapped$";
        }

        @Override
        public int hashCode() {
            return Objects.hash(criteriaList);
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (obj instanceof WrappedVertexHopCriteria) {
                WrappedVertexHopCriteria other = (WrappedVertexHopCriteria) obj;
                return Objects.equals(criteriaList, other.criteriaList);
            }
            return false;
        }

        public boolean contains(VertexRef vertexRef) {
            return getVertices().contains(vertexRef);
        }
    }

    /**
     * Helper criteria class to reference to existing VertexRefs.
     * This should be used anytime you want to add a vertex to the current focus (e.g. from the mouse context menu).
     */
    public static class DefaultVertexHopCriteria extends VertexHopCriteria {

        private final VertexRef vertexRef;

        public DefaultVertexHopCriteria(VertexRef vertexRef) {
            super(vertexRef.getId(), vertexRef.getLabel());
            this.vertexRef = vertexRef;
        }

        @Override
        public Set<VertexRef> getVertices() {
            return Sets.newHashSet(vertexRef);
        }

        @Override
        public String getNamespace() {
            return vertexRef.getNamespace();
        }

        @Override
        public int hashCode() {
            return vertexRef.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            if (obj instanceof DefaultVertexHopCriteria) {
                return Objects.equals(vertexRef, ((DefaultVertexHopCriteria) obj).vertexRef);
            }
            return false;
        }
    }

    private final GraphProvider m_delegate;
    private final Map<VertexRef,Integer> m_semanticZoomLevels = new LinkedHashMap<VertexRef,Integer>();

    public VertexHopGraphProvider(GraphProvider delegate) {
        m_delegate = delegate;
    }

    @Override
    public void refresh() {
        m_delegate.refresh();
    }

    @Override
    public String getNamespace() {
        return m_delegate.getNamespace();
    }

    @Override
    public boolean contributesTo(String namespace) {
        return m_delegate.contributesTo(namespace);
    }

    @Deprecated
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

    @Override
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
        // If we have a IgnoreHopCriteria, just return all existing vertices
        for (Criteria criterium : criteria) {
            try {
                IgnoreHopCriteria ignoreHopCriteria = (IgnoreHopCriteria)criterium;
                return m_delegate.getVertices();
            } catch (ClassCastException e) {}
        }

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


        Map<VertexRef, Set<VertexRef>> neighborMap = new HashMap<VertexRef, Set<VertexRef>>();
        List<Edge> edges = m_delegate.getEdges(criteria);
        for(Edge edge : edges) {
            VertexRef src = edge.getSource().getVertex();
            VertexRef tgt = edge.getTarget().getVertex();
            Set<VertexRef> srcNeighbors = neighborMap.get(src);
            if (srcNeighbors == null) {
                srcNeighbors = new HashSet<VertexRef>();
                neighborMap.put(src, srcNeighbors);
            }
            srcNeighbors.add(tgt);

            Set<VertexRef> tgtNeighbors = neighborMap.get(tgt);
            if (tgtNeighbors == null) {
                tgtNeighbors = new HashSet<VertexRef>();
                neighborMap.put(tgt, tgtNeighbors);
            }
            tgtNeighbors.add(src);
        }

        Set<Vertex> processed = new HashSet<Vertex>();
        Set<VertexRef> neighbors = new HashSet<VertexRef>();
        Set<VertexRef> workingSet = new HashSet<VertexRef>(focusNodes);
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
        Map<VertexRef,Set<Vertex>> vertexToCollapsedVertices = new TreeMap<VertexRef,Set<Vertex>>(new RefComparator());
        for (CollapsibleCriteria criterium : criteria) {
            Set<VertexRef> criteriaVertices = criterium.getVertices();
            if (criteriaVertices.size() > 0) {
                Vertex collapsedVertex = criterium.getCollapsedRepresentation();
                for (VertexRef criteriaVertex : criteriaVertices) {
                    Set<Vertex> collapsedVertices = vertexToCollapsedVertices.get(criteriaVertex);
                    if (collapsedVertices == null) {
                        collapsedVertices = new HashSet<Vertex>();
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
            Set<Edge> retval = new HashSet<Edge>();
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

    @Override
    public List<Vertex> getVertices(Collection<? extends VertexRef> references, Criteria... criteria) {
        return m_delegate.getVertices(references, criteria);
    }

    /**
     * TODO: Is this correct?
     */
    @Override
    public List<Vertex> getRootGroup() {
        return getVertices();
    }

    @Override
    public boolean hasChildren(VertexRef group) {	
        return false;
    }

    @Override
    public Vertex getParent(VertexRef vertex) {
        // throw new UnsupportedOperationException("Grouping is unsupported by " + getClass().getName());
        return null;
    }

    @Override
    public boolean setParent(VertexRef child, VertexRef parent) {
        // throw new UnsupportedOperationException("Grouping is unsupported by " + getClass().getName());
        return false;
    }

    @Override
    public List<Vertex> getChildren(VertexRef group, Criteria... criteria) {
        for (CollapsibleCriteria criterium : getCollapsedCriteria(criteria)) {
            if (new RefComparator().compare(criterium.getCollapsedRepresentation(), group) == 0) {
                return getVertices(criterium.getVertices());
            }
        }
        return Collections.emptyList();
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
    public int getEdgeTotalCount() {
        return m_delegate.getEdgeTotalCount();
    }

    @Override
    public Edge getEdge(String namespace, String id) {
        return m_delegate.getEdge(namespace, id);
    }

    @Override
    public Edge getEdge(EdgeRef reference) {
        return m_delegate.getEdge(reference);
    }

    @Override
    public List<Edge> getEdges(Criteria... criteria) {
        Set<Edge> retval = new HashSet<Edge>(m_delegate.getEdges(criteria));
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
    public void resetContainer() {
        m_delegate.resetContainer();
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
    public Vertex addVertex(int x, int y) {
        return m_delegate.addVertex(x, y);
    }

    @Override
    public Vertex addGroup(String label, String iconKey) {
        throw new UnsupportedOperationException("Grouping is unsupported by " + getClass().getName());
    }

    @Override
    public EdgeRef[] getEdgeIdsForVertex(VertexRef vertex) {
        return m_delegate.getEdgeIdsForVertex(vertex);
    }

    /**
     * TODO This will miss edges provided by auxiliary edge providers
     */
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
    public Edge connectVertices(VertexRef sourceVertextId, VertexRef targetVertextId) {
        return m_delegate.connectVertices(sourceVertextId, targetVertextId);
    }

    @Override
    public Defaults getDefaults() {
        return m_delegate.getDefaults();
    }

    @Override
    public SelectionChangedListener.Selection getSelection(List<VertexRef> selectedVertices, ContentType type) {
        return m_delegate.getSelection(selectedVertices, type);
    }

    @Override
    public boolean contributesTo(ContentType type) {
        return m_delegate.contributesTo(type);
    }

    public TopologyProviderInfo getTopologyProviderInfo() {
        return m_delegate.getTopologyProviderInfo();
    }
}
