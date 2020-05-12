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

package org.opennms.netmgt.graph.api.enrichment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

import org.opennms.netmgt.graph.api.NodeRef;
import org.opennms.netmgt.graph.api.VertexRef;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericElement;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericVertex;

import com.google.common.collect.Maps;

public final class EnrichmentGraphBuilder {

    private final GenericGraph view;
    private final Map<VertexRef, Map<String, Object>> vertexRefToPropertiesMap = Maps.newHashMap();
    private final Map<String, Map<String, Object>> edgeIdToPropertiesMap = Maps.newHashMap();
    private final Map<String, Object> graphProperties = Maps.newHashMap();

    public EnrichmentGraphBuilder(final GenericGraph input) {
        this.view = Objects.requireNonNull(input);
    }

    public GenericGraph getView() {
        return view;
    }

    public List<GenericVertex> getVertices() {
        return getView().getVertices();
    }

    public List<GenericEdge> getEdges() {
        return getView().getEdges();
    }

    public List<GenericVertex> resolveVertices(final NodeRef nodeRef) {
        return getView().resolveVertices(nodeRef);
    }

    public  <T> EnrichmentGraphBuilder property(final String name, final T value) {
        property(view, name, value, () -> graphProperties);
        return this;
    }

    public <T> EnrichmentGraphBuilder property(final GenericVertex vertex, final String name, final T value) {
        Objects.requireNonNull(vertex);
        property(vertex, name, value, () -> getVertexProperties(vertex.getVertexRef()));
        return this;
    }

    public <T> EnrichmentGraphBuilder property(final GenericEdge edge, final String name, final T value) {
        property(edge, name, value, () -> getEdgeProperties(edge));
        return this;
    }

    private <T> EnrichmentGraphBuilder property(final GenericElement element, final String name, final T value, Supplier<Map<String, Object>> enrichedPropertiesMapSupplier) {
        Objects.requireNonNull(element);
        Objects.requireNonNull(name);
        Objects.requireNonNull(value);
        if (element.getProperty(name) != null) {
            throw new IllegalArgumentException(
                    String.format("Cannot change existing property on '%s' graph element with id '%s'", element.getClass().getSimpleName(), element.getId()));
        }
        final Map<String, Object> enrichedProperties = enrichedPropertiesMapSupplier.get();
        if (enrichedProperties != null) {
            enrichedProperties.put(name, value);
        }
        return this;
    }

    private Map<String, Object> getVertexProperties(VertexRef vertexRef) {
        Objects.requireNonNull(vertexRef);
        if (view.resolveVertex(vertexRef) == null) {
            throw new IllegalArgumentException(
                    String.format("Vertex with id '%s' does not exist", vertexRef.getId()));
        }
        if (!vertexRefToPropertiesMap.containsKey(vertexRef)) {
            vertexRefToPropertiesMap.put(vertexRef, new HashMap<>());
        }
        return vertexRefToPropertiesMap.get(vertexRef);
    }

    private Map<String, Object> getEdgeProperties(GenericEdge edge) {
        Objects.requireNonNull(edge);
        final String edgeId = edge.getId();
        if (view.getEdge(edgeId) == null) {
            throw new IllegalArgumentException(
                    String.format("Edge with id '%s' does not exist", edgeId));
        }
        if (!edgeIdToPropertiesMap.containsKey(edgeId)) {
            edgeIdToPropertiesMap.put(edgeId, new HashMap<>());
        }
        return edgeIdToPropertiesMap.get(edgeId);
    }

    public GenericGraph build() {
        final GenericGraph.GenericGraphBuilder genericGraphBuilder = GenericGraph.builder()
                .properties(view.getProperties())
                .properties(graphProperties)
                .focus(view.getDefaultFocus());
        view.getVertices().forEach(vertex -> {
            final GenericVertex enrichedVertex = GenericVertex.builder().vertex(vertex).properties(getVertexProperties(vertex.getVertexRef())).build();
            genericGraphBuilder.addVertex(enrichedVertex);
        });
        view.getEdges().forEach(edge -> {
            final GenericEdge enrichedEdge = GenericEdge.builder().edge(edge).properties(getEdgeProperties(edge)).build();
            genericGraphBuilder.addEdge(enrichedEdge);
        });
        return genericGraphBuilder.build();
    }

}
