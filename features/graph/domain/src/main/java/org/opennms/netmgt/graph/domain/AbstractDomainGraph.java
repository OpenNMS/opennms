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

package org.opennms.netmgt.graph.domain;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opennms.netmgt.graph.api.ImmutableGraph;
import org.opennms.netmgt.graph.api.NodeRef;
import org.opennms.netmgt.graph.api.VertexRef;
import org.opennms.netmgt.graph.api.focus.Focus;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericGraph.GenericGraphBuilder;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.info.GraphInfo;

import com.google.common.base.MoreObjects;

/**
* Acts as a domain specific view on a {@link GenericGraph}.
* Can be extended by a domain specific graph class.
* It contains no data of it's own but operates on the data of it's wrapped {@link GenericGraph}.
**/
public abstract class AbstractDomainGraph<V extends AbstractDomainVertex, E extends AbstractDomainEdge> implements ImmutableGraph<V, E> {

    private final GenericGraph delegate;

    public AbstractDomainGraph(GenericGraph genericGraph) {
        this.delegate = genericGraph;
    }

    @Override
    public List<V> getVertices() {
        return this.delegate.getVertices().stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public List<E> getEdges() {
        return this.delegate.getEdges().stream().map(this::convert).collect(Collectors.toList());
    }

    protected abstract ImmutableGraph<V, E> convert(GenericGraph graph);
    protected abstract V convert(GenericVertex vertex);
    protected abstract E convert(GenericEdge edge);

    @Override
    public V getVertex(String id) {
        Objects.requireNonNull(id);
        return Optional.ofNullable(this.delegate.getVertex(id)).map(this::convert).orElse(null);
    }

    @Override
    public E getEdge(String id) {
        Objects.requireNonNull(id);
        return Optional.ofNullable(this.delegate.getEdge(id)).map(this::convert).orElse(null);
    }

    @Override
    public List<String> getVertexIds() {
        return this.delegate.getVertexIds();
    }

    @Override
    public List<String> getEdgeIds() {
        return this.delegate.getEdgeIds();
    }

    @Override
    public ImmutableGraph<V, E> getView(Collection<V> verticesInFocus, int szl) {
        Objects.requireNonNull(verticesInFocus);
        Collection<GenericVertex> genericVerticesInFocus = verticesInFocus.stream()
                .map(AbstractDomainVertex::asGenericVertex).collect(Collectors.toList());
        GenericGraph genericGraph = this.delegate.getView(genericVerticesInFocus, szl).asGenericGraph();
        return convert(genericGraph);
    }

    @Override
    public List<V> resolveVertices(NodeRef nodeRef) {
        Objects.requireNonNull(nodeRef);
        return delegate.resolveVertices(nodeRef).stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public List<V> resolveVertices(Collection<String> vertexIds) {
        return delegate.resolveVertices(vertexIds).stream()
                .map(this::convert).collect(Collectors.toList());
    }

    @Override
    public V resolveVertex(VertexRef vertexRef) {
       final GenericVertex vertex = delegate.resolveVertex(vertexRef);
       if (vertex != null) {
           return convert(vertex);
       }
       return null;
    }

    @Override
    public List<E> resolveEdges(Collection<String> edgeIds) {
        return delegate.resolveEdges(edgeIds).stream()
                .map(this::convert).collect(Collectors.toList());
    }

    @Override
    public Collection<V> getNeighbors(V vertex) {
        Objects.requireNonNull(vertex);
        return delegate.getNeighbors(vertex.asGenericVertex()).stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public Collection<E> getConnectingEdges(V vertex) {
        Objects.requireNonNull(vertex);
        return delegate.getConnectingEdges(vertex.asGenericVertex()).stream().map(this::convert).collect(Collectors.toList());
    }

    @Override
    public Focus getDefaultFocus() {
        return delegate.getDefaultFocus();
    }

    @Override
    public GenericGraph asGenericGraph() {
        return delegate;
    }

    @Override
    public String getNamespace() {
        return delegate.getNamespace();
    }

    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    @Override
    public String getLabel() {
        return delegate.getLabel();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AbstractDomainGraph<?, ?> that = (AbstractDomainGraph<?, ?>) o;
        return Objects.equals(delegate, that.delegate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(delegate);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("delegate", delegate)
                .toString();
    }

    public static class AbstractDomainGraphBuilder<
        T extends AbstractDomainGraphBuilder,
        V extends AbstractDomainVertex,
        E extends AbstractDomainEdge> {
       
        protected GenericGraphBuilder delegate = GenericGraph.builder();
        
        protected AbstractDomainGraphBuilder() {}

        public T id(String id) {
            delegate.id(id);
            return (T) this;
        }
        
        public T label(String label){
            delegate.label(label);
            return (T) this;
        }
        
        public T namespace(String namespace){
            delegate.namespace(namespace);
            return (T) this;
        }
        
        public T property(String name, String value){
            delegate.property(name, value);
            return (T) this;
        }
        
        
        public T  description(String description) {
            delegate.description(description);
            return (T) this;
        }
        
        public T addEdges(Collection<E> edges) {
            Objects.requireNonNull(edges);
            edges.forEach(this::addEdge);
            return (T) this;
        }

        public T addVertices(Collection<V> vertices) {
            Objects.requireNonNull(vertices);
            vertices.forEach(this::addVertex);
            return (T) this;
        }
        
        public T addVertex(V vertex) {
            Objects.requireNonNull(vertex);
            this.delegate.addVertex(vertex.asGenericVertex());
            return (T) this;
        }

        public T addEdge(E edge) {
            Objects.requireNonNull(edge);
            this.delegate.addEdge(edge.asGenericEdge());
            return (T) this;
        }

        public T removeEdge(E edge) {
            Objects.requireNonNull(edge);
            this.delegate.removeEdge(edge.asGenericEdge());
            return (T) this;
        }

        public T removeVertex(V vertex) {
            Objects.requireNonNull(vertex);
            this.delegate.removeVertex(vertex.asGenericVertex());
            return (T) this;
        }
        
        public T graphInfo(GraphInfo graphInfo) {
            delegate.namespace(graphInfo.getNamespace())
                .label(graphInfo.getLabel())
                .description(graphInfo.getDescription())
                .build();
            return (T) this;
        }

        public GenericGraphBuilder.FocusBuilder focus() {
            return delegate.focus();
        }

        public VertexRef getVertexRef(String vertexId) {
            return delegate.getVertex(vertexId).getVertexRef();
        }
    }
}
