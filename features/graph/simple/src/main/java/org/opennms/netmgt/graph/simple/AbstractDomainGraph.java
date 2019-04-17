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

package org.opennms.netmgt.graph.simple;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opennms.netmgt.graph.api.Graph;
import org.opennms.netmgt.graph.api.Vertex;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericVertex;

import com.google.common.base.MoreObjects;

/**
 * Acts as a domain specific view on a graph.
 * Can be extended by a domain specific graph class.
 * It contains no data of it's own but operates on the data of it's wrapped Graph.
 */
// TODO MVR enforce namespace
// TODO MVR implement duplication detection (e.g. adding same vertex twice
// and as well as adding different edges with different source/target vertices, should add each vertex only once,
// maybe not here, but at some point that check should be implemented)
public abstract class AbstractDomainGraph<V extends SimpleVertex, E extends SimpleEdge> implements Graph<V, E> {

    private final GenericGraph delegate;

    public AbstractDomainGraph(String namespace) {
        this.delegate = new GenericGraph();
        this.delegate.setNamespace(namespace);
    }

    public AbstractDomainGraph(GenericGraph genericGraph) {
        this.delegate = genericGraph;
    }

    @Override
    public List<V> getVertices() {
        return this.delegate.getVertices().stream().map(this::convert).collect(Collectors.toList());
    }

    protected abstract V convert(GenericVertex vertex);

    @Override
    public List<E> getEdges() {
        return this.delegate.getEdges().stream().map(this::convert).collect(Collectors.toList());
    }

    protected abstract E convert(GenericEdge edge);

    protected abstract Graph<V, E> convert(GenericGraph graph);

    @Override
    public void addEdges(Collection<E> edges) {
        Objects.requireNonNull(edges);
        edges.forEach(this::addEdge);
    }

    @Override
    public void addVertices(Collection<V> vertices) {
        Objects.requireNonNull(vertices);
        vertices.forEach(this::addVertex);
    }

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
    public void addVertex(V vertex) {
        Objects.requireNonNull(vertex);
        this.delegate.addVertex(vertex.asGenericVertex());
    }

    @Override
    public void addEdge(E edge) {
        Objects.requireNonNull(edge);
        this.delegate.addEdge(edge.asGenericEdge());
    }

    @Override
    public void removeEdge(E edge) {
        Objects.requireNonNull(edge);
        this.delegate.removeEdge(edge.asGenericEdge());
    }

    @Override
    public void removeVertex(V vertex) {
        Objects.requireNonNull(vertex);
        this.delegate.removeVertex(vertex.asGenericVertex());
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
    public Graph<V, E> getSnapshot(Collection<V> verticesInFocus, int szl) {
        Objects.requireNonNull(verticesInFocus);
        Collection<GenericVertex> genericVerticesInFocus = verticesInFocus.stream()
                .map(SimpleVertex::asGenericVertex).collect(Collectors.toList());
        GenericGraph genericGraph = this.delegate.getSnapshot(genericVerticesInFocus, szl).asGenericGraph();
        return convert(genericGraph);
    }

    @Override
    public List<V> resolveVertices(Collection<String> vertexIds) {
        return delegate.resolveVertices(vertexIds).stream()
                .map(this::convert).collect(Collectors.toList());
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
    public List<Vertex> getDefaultFocus() {
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

    public void setNamespace(String namespace) {
        delegate.setNamespace(namespace);
    }

    public void setDescription(String description) {
        delegate.setDescription(description);
    }

    @Override
    public String getDescription() {
        return delegate.getDescription();
    }

    @Override
    public String getLabel() {
        return delegate.getLabel();
    }

    public void setLabel(String label){
        delegate.setLabel(label);
    }

    @Override
    public abstract Class getVertexType();

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
}
