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

package org.opennms.netmgt.graph.api.generic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.netmgt.graph.api.Graph;
import org.opennms.netmgt.graph.api.Vertex;
import org.opennms.netmgt.graph.api.VertexRef;
import org.opennms.netmgt.graph.api.context.DefaultGraphContext;
import org.opennms.netmgt.graph.api.focus.Focus;
import org.opennms.netmgt.graph.api.info.GraphInfo;

import com.google.common.collect.Lists;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

// TODO MVR enforce namespace
public class GenericGraph extends GenericElement implements Graph<GenericVertex, GenericEdge> {

    private final DirectedSparseGraph<VertexRef, GenericEdge> jungGraph = new DirectedSparseGraph<>();
    private final Map<String, GenericVertex> vertexToIdMap = new HashMap<>();
    private final Map<String, GenericEdge> edgeToIdMap = new HashMap<>();
//    private final Map<NodeRef, V> nodeRefToVertexMap = new HashMap<>();

    // A calculation of the focus
    private Focus focusStrategy;
    protected GraphInfo<GenericVertex> graphInfo;

    public GenericGraph(String namespace) {
        this(new MapBuilder<String, Object>()
                .withProperty(GenericProperties.NAMESPACE, namespace)
                .build());
    }

    /** Copy constructor. */
    public GenericGraph(GenericGraph copyMe) {
        this(new HashMap<>(copyMe.properties));
        this.setFocusStrategy(copyMe.focusStrategy);
        for(GenericVertex originalVertex : copyMe.vertexToIdMap.values()){
            this.addVertex(new GenericVertex(originalVertex));
        }
        for(GenericEdge originalEdge : copyMe.edgeToIdMap.values()){
            this.addEdge(new GenericEdge(originalEdge));
        }
    }

    public GenericGraph(Map<String, Object> properties) {
        super(properties);
        this.graphInfo = new GenericGraphInfo();
    }

    public static GenericGraph fromGraphInfo(GraphInfo graphInfo) {
        // we can't have a constructor GenericGraph(GraphInfo graphInfo) since it conflicts with GenericGraph(GenericGraph graph)
        // that's why we have a factory method instead
        GenericGraph graph = new GenericGraph(graphInfo.getNamespace());
        graph.setDescription(graphInfo.getDescription());
        graph.setLabel(graphInfo.getLabel());
        return graph;
    }

    //    @Override
//    public Vertex getVertex(NodeRef nodeRef) {
//        return nodeRefMap.get(nodeRef);
//    }

    @Override
    public GenericGraph asGenericGraph() {
        return this;
    }

    @Override
    public List<GenericVertex> getVertices() {
        // TODO MVR use junggraph.getVetices instead. However addEdge is adding the edges if not in same namespace
        // We have to figure out a workaround for that somehow
        return new ArrayList<>(vertexToIdMap.values());
    }

    @Override
    public List<GenericEdge> getEdges() {
        // TODO MVR use junggraph.getEdges instead. However addEdge is adding the edges if not in same namespace
        // We have to figure out a workaround for that somehow
        return new ArrayList<>(edgeToIdMap.values());
    }

    public void setDescription(String description) {
        setProperty(GenericProperties.DESCRIPTION, description);
    }

    public GraphInfo getGraphInfo(){
        return graphInfo;
    }

    @Override
    public String getNamespace() {
        return graphInfo.getNamespace();
    }

    @Override
    public String getDescription() {
        return graphInfo.getDescription();
    }

    @Override
    public String getLabel() {
        return graphInfo.getLabel();
    }

    @Override
    public Class<GenericVertex> getVertexType() {
        return GenericVertex.class;
    }

    @Override
    public List<Vertex> getDefaultFocus() {
        if (focusStrategy != null) {
            return focusStrategy.getFocus(new DefaultGraphContext(this)).stream().map(vr -> vertexToIdMap.get(vr.getId())).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }

//    @Override
//    public Vertex getVertex(NodeRef nodeRef) {
//        return nodeRefToVertexMap.get(nodeRef);
//    }

    public void setFocusStrategy(Focus focusStrategy) {
        this.focusStrategy = focusStrategy; // TODO MVR verify persistence of this
    }

    @Override
    public void addEdges(Collection<GenericEdge> edges) {
        for (GenericEdge eachEdge : edges) {
            addEdge(eachEdge);
        }
    }

    @Override
    public void addVertices(Collection<GenericVertex> vertices) {
        for (GenericVertex eachVertex : vertices) {
            addVertex(eachVertex);
        }
    }

    @Override
    public void addVertex(GenericVertex vertex) {
        Objects.requireNonNull(vertex);
        Objects.requireNonNull(vertex.getId());
        if (jungGraph.containsVertex(vertex)) return; // already added
        jungGraph.addVertex(vertex);
        vertexToIdMap.put(vertex.getId(), vertex);

//        if (vertex.getNodeRef() != null) {
//            // TODO MVR implement me
////            nodeRefToVertexMap.put(vertex.getNodeRef(), vertex);
//        }
    }

    @Override
    public void addEdge(GenericEdge edge) {
        Objects.requireNonNull(edge);
        Objects.requireNonNull(edge.getId());
        if (jungGraph.containsEdge(edge)) return; // already added
        if (edge.getSource().getNamespace().equalsIgnoreCase(getNamespace()) && getVertex(edge.getSource().getId()) == null) {
            addVertex((GenericVertex) edge.getSource());
        }
        if (edge.getTarget().getNamespace().equalsIgnoreCase(getNamespace()) && getVertex(edge.getTarget().getId()) == null) {
            addVertex((GenericVertex) edge.getTarget());
        }
        jungGraph.addEdge(edge, edge.getSource(), edge.getTarget());
        edgeToIdMap.put(edge.getId(), edge);
    }

    @Override
    public void removeEdge(GenericEdge edge) {
        Objects.requireNonNull(edge);
        jungGraph.removeEdge(edge);
        edgeToIdMap.remove(edge.getId());
    }

    @Override
    public void removeVertex(GenericVertex vertex) {
        Objects.requireNonNull(vertex);
        jungGraph.removeVertex(vertex);
        vertexToIdMap.remove(vertex.getId());
    }

    @Override
    public GenericVertex getVertex(String id) {
        return vertexToIdMap.get(id);
    }

    @Override
    public GenericEdge getEdge(String id) {
        return edgeToIdMap.get(id);
    }

    @Override
    public List<String> getVertexIds() {
        return vertexToIdMap.keySet().stream().sorted().collect(Collectors.toList());
    }

    @Override
    public List<String> getEdgeIds() {
        return edgeToIdMap.keySet().stream().sorted().collect(Collectors.toList());
    }

    @Override
    public List<GenericVertex> resolveVertices(Collection<String> vertexIds) {
        final List<GenericVertex> collect = vertexIds.stream().map(vid -> vertexToIdMap.get(vid)).filter(v -> v != null).collect(Collectors.toList());
        return collect;
    }

    @Override
    public GenericVertex resolveVertex(VertexRef vertexRef) {
        Objects.requireNonNull(vertexRef);
        if (getNamespace().equalsIgnoreCase(vertexRef.getNamespace())) {
            final GenericVertex resolvedVertex = resolveVertices(Lists.newArrayList(vertexRef.getId())).stream().findAny().orElse(null);
            return resolvedVertex;
        }
        return null;
    }

    public List<GenericVertex> resolveVertexRefs(Collection<VertexRef> vertexRefs) {
        // Determine all vertexId for all vertices with the same namespace as the current graph
        List<String> vertexIds = vertexRefs.stream()
                .filter(ref -> getNamespace().equalsIgnoreCase(ref.getNamespace()))
                .map(ref -> ref.getId())
                .collect(Collectors.toList());
        return resolveVertices(vertexIds);
    }

    @Override
    public List<GenericEdge> resolveEdges(Collection<String> vertexIds) {
        final List<GenericEdge> collect = vertexIds.stream().map(eid -> edgeToIdMap.get(eid)).collect(Collectors.toList());
        return collect;
    }

    @Override
    public Collection<GenericVertex> getNeighbors(GenericVertex eachVertex) {
        return resolveVertexRefs(jungGraph.getNeighbors(eachVertex));
    }

    @Override
    public Collection<GenericEdge> getConnectingEdges(GenericVertex eachVertex) {
        final Set<GenericEdge> edges = new HashSet<>();
        edges.addAll(jungGraph.getInEdges(eachVertex));
        edges.addAll(jungGraph.getOutEdges(eachVertex));
        return edges;
    }

    @Override
    public Graph<GenericVertex, GenericEdge> getSnapshot(Collection<GenericVertex> verticesInFocus, int szl) {
        // TODO MVR implement me
//        return new SemanticZoomLevelTransformer(verticesInFocus, szl).transform(this, () -> {
//            final SimpleGraph<SimpleVertex, SimpleEdge<SimpleVertex>> snapshotGraph = new SimpleGraph<>(getNamespace());
//            applyInfo(SimpleGraph.this);
//            return snapshotGraph;
//        });
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GenericGraph that = (GenericGraph) o;
        return Objects.equals(vertexToIdMap, that.vertexToIdMap)
                && Objects.equals(edgeToIdMap, that.edgeToIdMap)
                && Objects.equals(focusStrategy, that.focusStrategy);
    }

    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(),
                vertexToIdMap, edgeToIdMap, focusStrategy);
    }

    private class GenericGraphInfo implements GraphInfo<GenericVertex> {

        @Override
        public String getNamespace() {
            return (String) properties.get(GenericProperties.NAMESPACE);
        }

        @Override
        public String getDescription() {
            return (String) properties.get(GenericProperties.DESCRIPTION);
        }

        @Override
        public String getLabel() {
            return (String) properties.get(GenericProperties.LABEL);
        }

        @Override
        public Class<GenericVertex> getVertexType() {
            return GenericVertex.class;
        }


    }
}
