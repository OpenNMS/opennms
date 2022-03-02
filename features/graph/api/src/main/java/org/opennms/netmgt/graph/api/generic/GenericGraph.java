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

import static com.google.common.base.Preconditions.checkArgument;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.netmgt.graph.api.Edge;
import org.opennms.netmgt.graph.api.ImmutableGraph;
import org.opennms.netmgt.graph.api.NodeRef;
import org.opennms.netmgt.graph.api.VertexRef;
import org.opennms.netmgt.graph.api.focus.Focus;
import org.opennms.netmgt.graph.api.focus.FocusStrategy;
import org.opennms.netmgt.graph.api.info.GraphInfo;
import org.opennms.netmgt.graph.api.transformer.SemanticZoomLevelTransformer;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

import edu.uci.ics.jung.graph.DirectedSparseGraph;

/**
 * Immutable generic graph.
 */
public final class GenericGraph extends GenericElement implements ImmutableGraph<GenericVertex, GenericEdge> {

    private final DirectedSparseGraph<VertexRef, GenericEdge> jungGraph;
    private final Map<String, GenericVertex> vertexToIdMap;
    private final Map<String, GenericEdge> edgeToIdMap;
    private final Map<NodeRef, List<GenericVertex>> nodeRefToVertexMap;

    // A calculation of the focus
    private final Focus defaultFocus;
    private final GraphInfo graphInfo;

    private GenericGraph(GenericGraphBuilder builder) {
        super(builder.properties);
        this.jungGraph = builder.jungGraph;
        this.vertexToIdMap = builder.vertexToIdMap;
        this.edgeToIdMap = builder.edgeToIdMap;
        this.nodeRefToVertexMap = builder.nodeRefToVertexMap;
        this.defaultFocus = builder.defaultFocus;
        this.graphInfo = new GenericGraphInfo();
    }

    @Override
    public GenericGraph asGenericGraph() {
        return this;
    }

    @Override
    public List<GenericVertex> getVertices() {
        return new ArrayList<>(vertexToIdMap.values());
    }

    @Override
    public List<GenericEdge> getEdges() {
        return new ArrayList<>(edgeToIdMap.values());
    }

    public GraphInfo getGraphInfo(){
        return graphInfo;
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
    public Focus getDefaultFocus() {
        return defaultFocus;
    }

    @Override
    public List<GenericVertex> resolveVertices(NodeRef nodeRef) {
        Objects.requireNonNull(nodeRef);
        final List<GenericVertex> resolvedVertices = Lists.newArrayList();
        for (NodeRef eachVariant : nodeRef.getVariants()) {
            if (nodeRefToVertexMap.containsKey(eachVariant)) {
                resolvedVertices.addAll(nodeRefToVertexMap.get(eachVariant));
            }
        }
        return resolvedVertices;
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
        if (getNamespace().equals(vertexRef.getNamespace())) {
            final GenericVertex resolvedVertex = resolveVertices(Lists.newArrayList(vertexRef.getId())).stream().findAny().orElse(null);
            return resolvedVertex;
        }
        return null;
    }

    public List<GenericVertex> resolveVertexRefs(Collection<VertexRef> vertexRefs) {
        // Determine all vertexId for all vertices with the same namespace as the current graph
        List<String> vertexIds = vertexRefs.stream()
                .filter(ref -> getNamespace().equals(ref.getNamespace()))
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
        return resolveVertexRefs(jungGraph.getNeighbors(eachVertex.getVertexRef()));
    }

    @Override
    public Collection<GenericEdge> getConnectingEdges(GenericVertex eachVertex) {
        final Set<GenericEdge> edges = new HashSet<>();
        if (eachVertex != null) {
            final VertexRef genericVertexRef = eachVertex.getVertexRef();
            edges.addAll(jungGraph.getInEdges(genericVertexRef));
            edges.addAll(jungGraph.getOutEdges(genericVertexRef));
        }
        return edges;
    }

    @Override
    public ImmutableGraph<GenericVertex, GenericEdge> getView(Collection<GenericVertex> verticesInFocus, int szl) {
        return new SemanticZoomLevelTransformer(verticesInFocus, szl).transform(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        GenericGraph that = (GenericGraph) o;
        return Objects.equals(vertexToIdMap, that.vertexToIdMap)
                && Objects.equals(edgeToIdMap, that.edgeToIdMap)
                && Objects.equals(defaultFocus, that.defaultFocus);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(),
                vertexToIdMap, edgeToIdMap, getDefaultFocus());
    }
    
    public static GenericGraphBuilder builder() {
        return new GenericGraphBuilder();
    }

    public static GenericGraphBuilder from(GenericGraph graph) {
        Objects.requireNonNull(graph);
        return new GenericGraphBuilder().graph(graph);
    }
    
    public final static class GenericGraphBuilder extends GenericElementBuilder<GenericGraphBuilder> {

        private final DirectedSparseGraph<VertexRef, GenericEdge> jungGraph = new DirectedSparseGraph<>();
        private final Map<String, GenericVertex> vertexToIdMap = new HashMap<>();
        private final Map<String, GenericEdge> edgeToIdMap = new HashMap<>();
        private final Map<NodeRef, List<GenericVertex>> nodeRefToVertexMap = new HashMap<>();

        // A calculation of the focus
        private Focus defaultFocus = new Focus(FocusStrategy.EMPTY);
        
        private GenericGraphBuilder() {}
     
        public GenericGraphBuilder graph(GenericGraph graph) {
            this.properties(graph.getProperties());
            this.addVertices(graph.getVertices());
            this.addEdges(graph.getEdges());
            this.defaultFocus = graph.defaultFocus;
            return this;
        }
        
        public GenericGraphBuilder description(String description) {
            property(GenericProperties.DESCRIPTION, description);
            return this;
        }
        
        public GenericGraphBuilder graphInfo(GraphInfo graphInfo) {
            namespace(graphInfo.getNamespace());
            description(graphInfo.getDescription());
            label(graphInfo.getLabel());
            return this;
        }
        
        public GenericGraphBuilder focus(Focus defaultFocus) {
            Objects.requireNonNull(defaultFocus);
            this.defaultFocus = defaultFocus;
            return this;
        }

        public FocusBuilder focus() {
            return new FocusBuilder();
        }

        public GenericGraphBuilder addEdges(Collection<GenericEdge> edges) {
            for (GenericEdge eachEdge : edges) {
                addEdge(eachEdge);
            }
            return this;
        }

        public GenericGraphBuilder addVertices(Collection<GenericVertex> vertices) {
            for (GenericVertex eachVertex : vertices) {
                addVertex(eachVertex);
            }
            return this;
        }

        public GenericGraphBuilder addVertex(GenericVertex vertex) {
            Objects.requireNonNull(getNamespace(), "Please set a namespace before adding elements to this graph.");
            Objects.requireNonNull(vertex, "GenericVertex can not be null");
            checkArgument(!Strings.isNullOrEmpty(vertex.getId()) , "GenericVertex.getId() can not be empty or null. Vertex= %s", vertex);
            if (!this.getNamespace().equals(vertex.getNamespace())) {
                throw new IllegalArgumentException(
                    String.format("The namespace of the vertex (%s) doesn't match the namespace of this graph (%s). Vertex: %s ",
                        vertex.getNamespace(), this.getNamespace(), vertex.toString()));
            }
            if (vertexToIdMap.containsKey(vertex.getId())) return this; // already added
            jungGraph.addVertex(vertex.getVertexRef());
            vertexToIdMap.put(vertex.getId(), vertex);
            if (vertex.getNodeRef() != null) {
                nodeRefToVertexMap.putIfAbsent(vertex.getNodeRef(), new ArrayList<>());
                nodeRefToVertexMap.get(vertex.getNodeRef()).add(vertex);
            }
            return this; 
        }

        public GenericGraphBuilder addEdge(GenericEdge edge) {
            Objects.requireNonNull(getNamespace(), "Please set a namespace before adding elements to this graph.");
            Objects.requireNonNull(edge, "GenericEdge cannot be null");
            checkArgument(!Strings.isNullOrEmpty(edge.getId()) , "GenericEdge.getId() can not be empty or null. Vertex= %s", edge);
            if(!this.getNamespace().equals(edge.getNamespace())){
                throw new IllegalArgumentException(
                        String.format("The namespace of the edge (%s) doesn't match the namespace of this graph (%s). Edge: %s ",
                        edge.getNamespace(), this.getNamespace(), edge.toString()));
            }
            assertEdgeContainsAtLeastOneKnownVertex(edge);
            if (edgeToIdMap.containsKey(edge.getId())) return this; // already added
            jungGraph.addEdge(edge, edge.getSource(), edge.getTarget());
            edgeToIdMap.put(edge.getId(), edge);
            return this;
        }

        // Verifies that either the source or target vertex are known by the graph
        private void assertEdgeContainsAtLeastOneKnownVertex(Edge edge) {
            Objects.requireNonNull(edge.getSource(), "Source vertex must be provided");
            Objects.requireNonNull(edge.getTarget(), "Target vertex must be provided");
            final VertexRef sourceRef = edge.getSource();
            final VertexRef targetRef = edge.getTarget();

            // neither source nor target share the same namespace => both unknown => not valid
            if (!sourceRef.getNamespace().equals(getNamespace()) && !targetRef.getNamespace().equals(getNamespace())) {
                throw new IllegalArgumentException(
                        String.format("Adding an Edge with two vertices of unknown namespace. Either the source or target vertex must match the graph's namespace (%s). But got: (%s, %s)",
                                getNamespace(), sourceRef.getNamespace(), targetRef.getNamespace()));
            }
            // source vertex is shared -> check if known
            if (sourceRef.getNamespace().equals(getNamespace())) {
                assertVertexFromSameNamespaceIsKnown(sourceRef);
            }
            // target vertex is shared -> check if known
            if (targetRef.getNamespace().equals(getNamespace())) {
                assertVertexFromSameNamespaceIsKnown(targetRef);
            }
        }

        private void assertVertexFromSameNamespaceIsKnown(VertexRef vertex) {
            if (vertex.getNamespace().equals(getNamespace()) && getVertex(vertex.getId()) == null) {
                throw new IllegalArgumentException(
                        String.format("Adding a VertexRef to an unknown Vertex with id=%s in our namespace (%s). Please add the Vertex first to the graph",
                                vertex.getId(), this.getNamespace()));
            }
        }
        
        public void removeEdge(GenericEdge edge) {
            Objects.requireNonNull(edge);
            jungGraph.removeEdge(edge);
            edgeToIdMap.remove(edge.getId());
        }
        
        public void removeVertex(GenericVertex vertex) {
            Objects.requireNonNull(vertex);
            jungGraph.removeVertex(vertex.getVertexRef());
            vertexToIdMap.remove(vertex.getId());
        }
        
        public String getNamespace() {
            return Objects.requireNonNull((String)this.properties.get(GenericProperties.NAMESPACE), "Namespace is not set yet. Please call namespace(...) first.");
        }
        
        public GenericVertex getVertex(String id) {
            return vertexToIdMap.get(id);
        }

        public List<GenericVertex> resolveVertices(NodeRef nodeRef) {
            Objects.requireNonNull(nodeRef);
            final List<GenericVertex> resolvedVertices = Lists.newArrayList();
            for (NodeRef eachVariant : nodeRef.getVariants()) {
                if (nodeRefToVertexMap.containsKey(eachVariant)) {
                    resolvedVertices.addAll(nodeRefToVertexMap.get(eachVariant));
                }
            }
            return resolvedVertices;
        }

        public GenericGraphBuilder namespace(String namespace) {
            checkIfNamespaceChangeIsAllowed(namespace);
            return super.namespace(namespace);
        }
    
        public GenericGraphBuilder property(String name, Object value) {
            if(GenericProperties.NAMESPACE.equals(name)) {
                checkIfNamespaceChangeIsAllowed((String)value);
            }
            return super.property(name, value);
        }
        
        public GenericGraphBuilder properties(Map<String, Object> properties) {
            if(properties != null && properties.containsKey(GenericProperties.NAMESPACE)) {
                checkIfNamespaceChangeIsAllowed((String)properties.get(GenericProperties.NAMESPACE));
            }
            return super.properties(properties);
        }
        
        private void checkIfNamespaceChangeIsAllowed(String newNamespace) {
            if(!this.vertexToIdMap.isEmpty() && !this.edgeToIdMap.isEmpty() && !Objects.equals(getNamespace(), newNamespace)) {
                throw new IllegalStateException("Cannot change namespace after adding Elements to Graph.");
            }
        }
        
        public GenericGraph build() {
            return new GenericGraph(this);
        }

        public List<GenericVertex> getVertices() {
            return Lists.newArrayList(vertexToIdMap.values());
        }

        /**
         * Helper to build a focus based on various strategies we support.
         */
        public class FocusBuilder {

            private String focusStrategy = FocusStrategy.EMPTY;
            private List<VertexRef> focusSelection = new ArrayList<>();

            public FocusBuilder first() {
                focusStrategy = FocusStrategy.FIRST;
                return this;
            }

            public FocusBuilder empty() {
                focusStrategy = FocusStrategy.EMPTY;
                return this;
            }

            public FocusBuilder all() {
                focusStrategy = FocusStrategy.ALL;
                return this;
            }

            public FocusBuilder selection(String vertexNamespace, List<String> vertexIds) {
                Objects.requireNonNull(vertexNamespace);
                Objects.requireNonNull(vertexIds);
                final List<VertexRef> vertexRefs = vertexIds.stream().map(id -> new VertexRef(vertexNamespace, id)).collect(Collectors.toList());
                return selection(vertexRefs);
            }

            public FocusBuilder selection(List<VertexRef> vertexRefs) {
                Objects.requireNonNull(vertexRefs);
                focusStrategy = FocusStrategy.SELECTION;
                focusSelection = new ArrayList<>(vertexRefs);
                return this;
            }

            public FocusBuilder selection(VertexRef vertexRef) {
                Objects.requireNonNull(vertexRef);
                return selection(Lists.newArrayList(vertexRef));
            }

            public Focus build() {
                switch(focusStrategy) {
                    case FocusStrategy.FIRST:
                        final List<VertexRef> list = Lists.newArrayList();
                        if (!vertexToIdMap.isEmpty()) {
                            list.add(vertexToIdMap.values().iterator().next().getVertexRef());
                        }
                        return new Focus(FocusStrategy.FIRST, list);
                    case FocusStrategy.ALL:
                        return new Focus(FocusStrategy.ALL, vertexToIdMap.values().stream().map(GenericVertex::getVertexRef).collect(Collectors.toList()));
                    case FocusStrategy.EMPTY:
                        return new Focus(FocusStrategy.EMPTY);
                    case FocusStrategy.SELECTION:
                        // Only use selections, which actually exist in the graph
                        final List<VertexRef> existingVertexRefs = focusSelection.stream()
                                .filter(v -> v.getNamespace().equals(getNamespace()) && vertexToIdMap.containsKey(v.getId()))
                                .collect(Collectors.toList());
                        return new Focus(FocusStrategy.SELECTION, existingVertexRefs);
                    default:
                        final String[] validValues = new String[]{ FocusStrategy.ALL, FocusStrategy.EMPTY, FocusStrategy.FIRST, FocusStrategy.SELECTION };
                        throw new IllegalStateException("Focus Strategy '" + focusStrategy + "' not supported. Supported values are: " + Arrays.toString(validValues));
                }
            }

            // Convenient method to build the focus and apply
            // it to the GenericGraphBuilder afterwards
            public GenericGraphBuilder apply() {
                final Focus focus = build();
                return focus(focus);
            }
        }
    }
    
    private class GenericGraphInfo implements GraphInfo {

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

    }
}
