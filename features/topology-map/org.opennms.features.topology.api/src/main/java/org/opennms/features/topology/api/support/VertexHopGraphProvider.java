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

import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.bind.JAXBException;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.CollapsibleCriteria;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeListener;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.RefComparator;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexListener;
import org.opennms.features.topology.api.topo.VertexRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class will be used to filter a topology so that the semantic zoom level is
 * interpreted as a hop distance away from a set of selected vertices. The vertex 
 * selection is specified using sets of {@link VertexHopCriteria} filters.
 * 
 * @author Seth
 */
public class VertexHopGraphProvider implements GraphProvider {
    @SuppressWarnings("unused")
    private static final Logger LOG = LoggerFactory.getLogger(VertexHopGraphProvider.class);

    public static FocusNodeHopCriteria getFocusNodeHopCriteriaForContainer(GraphContainer graphContainer) {
        return getFocusNodeHopCriteriaForContainer(graphContainer, true);
    }

    public static FocusNodeHopCriteria getFocusNodeHopCriteriaForContainer(GraphContainer graphContainer, boolean createIfAbsent) {
        return Criteria.getSingleCriteriaForGraphContainer(graphContainer, FocusNodeHopCriteria.class, createIfAbsent);
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
        	super();
        	m_label = label;
        }
        
        public VertexHopCriteria(String id, String label) {
        	super();
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
    }

    public static class FocusNodeHopCriteria extends VertexHopCriteria {
        private final Set<VertexRef> m_vertices = new TreeSet<VertexRef>(new RefComparator());

        //FIXME: Since the criteria must have a label set, this constructor is being deprecated
        //Be sure that your constructing class calls setLabel()
        public FocusNodeHopCriteria() {
        	super(null);
        }
        
        public FocusNodeHopCriteria(String label) {
        	super(label);
        }
        
        public FocusNodeHopCriteria(String id, String label) {
        	super(id, label);
        }
        
        /**
         * FIXME: I think it does matter ;)
         * TODO: This return value doesn't matter since we just delegate
         * to the m_delegate provider.
         */
        @Override
        public String getNamespace() {
            return "nodes";
        }

        public void add(VertexRef ref) {
            m_vertices.add(ref);
            setDirty(true);
        }

        public void remove(VertexRef ref) {
            m_vertices.remove(ref);
            setDirty(true);
        }

        public void clear() {
            m_vertices.clear();
            setDirty(true);
        }

        public boolean contains(VertexRef ref) {
            return m_vertices.contains(ref);
        }

        public int size() {
            return m_vertices.size();
        }

        public boolean isEmpty() {
            return m_vertices.isEmpty();
        }

        @Override
        public Set<VertexRef> getVertices() {
            return Collections.unmodifiableSet(m_vertices);
        }

        public void addAll(Collection<VertexRef> refs) {
            m_vertices.addAll(refs);
            setDirty(true);
        }

        public void removeAll(Collection<VertexRef> refs) {
            m_vertices.removeAll(refs);
            setDirty(true);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((m_vertices == null) ? 0 : m_vertices.hashCode());
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (!(obj instanceof FocusNodeHopCriteria)) {
                return false;
            }
            final FocusNodeHopCriteria other = (FocusNodeHopCriteria) obj;
            
            if ((other.getNamespace() != null && other.getNamespace().equals(getNamespace()))
                    &&  (other.getId() != null && other.getId().equals(getId()))
                    && (other.getLabel() != null && other.getLabel().equals(getLabel()))) {
               return true; 
            }
            
//            if (m_vertices == null) {
//                if (other.m_vertices != null) {
//                    return false;
//                }
//            } else if (!m_vertices.equals(other.m_vertices)) {
//                return false;
//            }
            
            return false;
        }
    }

    private final GraphProvider m_delegate;
    private final Map<VertexRef,Integer> m_semanticZoomLevels = new LinkedHashMap<VertexRef,Integer>();

    public VertexHopGraphProvider(GraphProvider delegate) {
        m_delegate = delegate;
    }

    @Override
    public void save() {
        m_delegate.save();
    }

    @Override
    public void load(String filename) throws MalformedURLException, JAXBException {
        m_delegate.load(filename);
    }

    @Override
    public void refresh() {
        m_delegate.refresh();
    }

    @Override
    public String getVertexNamespace() {
        return m_delegate.getVertexNamespace();
    }

    @Override
    public boolean contributesTo(String namespace) {
        return m_delegate.contributesTo(namespace);
    }

    @Deprecated
    @Override
    public boolean containsVertexId(String id) {
        return containsVertexId(new DefaultVertexRef(getVertexNamespace(), id));
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

        Set<VertexRef> focusNodes = getFocusNodes(criteria);
        int maxSemanticZoomLevel = getMaxSemanticZoomLevel(criteria);

        // Clear the existing semantic zoom level values
        m_semanticZoomLevels.clear();
        int semanticZoomLevel = 0;

        // If we didn't find any matching nodes among the focus nodes...
        if (focusNodes.size() < 1) {
            // ...then return an empty list of vertices
            return Collections.emptyList();
            // return allVertices;
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
                if (m_semanticZoomLevels.containsKey(vertexRef)) {
                    throw new IllegalStateException("Calculating semantic zoom level for vertex that has already been calculated: " + vertexRef.toString());
                }
                m_semanticZoomLevels.put(vertexRef, semanticZoomLevel);
                Set<VertexRef> refs = neighborMap.get(vertexRef);
                if (refs != null) {
                    neighbors.addAll(refs);
                }
                Vertex vertex = getVertex(vertexRef, criteria);
                if (vertex != null) {
                    processed.add(getVertex(vertexRef, criteria));
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
        // Make a map of all of the vertices to their new collapsed representations
        Map<VertexRef,Set<Vertex>> vertexToCollapsedVertices = getMapOfVerticesToCollapsedVertices(criteria);

        if (vertexToCollapsedVertices.size() > 0) {
            Set<Vertex> retval = new HashSet<Vertex>();
            for (Vertex vertex : vertices) {
                // If the source vertex is in the collapsed list...
                Set<Vertex> collapsedVertices = vertexToCollapsedVertices.get(vertex);
                if (collapsedVertices != null) {
                    for (Vertex collapsedEndpoint : collapsedVertices) {
                        retval.add(collapsedEndpoint);
                    }
                } else {
                    retval.add(vertex);
                }
            }
            return retval;
        } else {
            return vertices;
        }
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

    /**
     * TODO: OVERRIDE THIS FUNCTION?
     */
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
        //LOG.warn("Called getChildren() on a vertex {} that is not currently collapsible", group);
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
    public String getEdgeNamespace() {
        return m_delegate.getEdgeNamespace();
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
        return new ArrayList<Edge>(retval);
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
    public boolean groupingSupported() {
        return false;
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
    public Criteria getDefaultCriteria() {
        return m_delegate.getDefaultCriteria();
    }
    
    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return super.toString();
    }
}
