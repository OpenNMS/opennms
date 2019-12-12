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

import org.opennms.netmgt.graph.api.NodeRef;
import org.opennms.netmgt.graph.api.VertexRef;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericVertex;

import com.google.common.collect.Maps;

public final class EnrichmentGraphBuilder {

    private final GenericGraph view;
    private final Map<VertexRef, Map<String, Object>> vertexRefToPropertiesMap = Maps.newHashMap();
    private final Map<String, Map<String, Object>> edgeIdToPropertiesMap = Maps.newHashMap();

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

    public <T> EnrichmentGraphBuilder property(final GenericVertex vertex, final String name, final T value) {
        Objects.requireNonNull(vertex);
        Objects.requireNonNull(name);
        final Map<String, Object> vertexProperties = getVertexProperties(vertex);
        if (vertex.getProperty(name) != null) {
            throw new IllegalArgumentException("Cannot change existing property");
        }
        vertexProperties.put(name, value);
        return this;
    }

    public <T> EnrichmentGraphBuilder property(final GenericEdge edge, final String name, final T value) {
        Objects.requireNonNull(edge);
        Objects.requireNonNull(name);
        final Map<String, Object> edgeProperties = getEdgeProperties(edge);
        if (edge.getProperty(name) != null) {
            throw new IllegalArgumentException("Cannot change existing property");
        }
        edgeProperties.put(name, value);
        return this;
    }

    private Map<String, Object> getVertexProperties(GenericVertex vertex) {
        Objects.requireNonNull(vertex);
        final VertexRef vertexRef = vertex.getVertexRef();
        if (view.resolveVertex(vertexRef) == null) {
            throw new IllegalArgumentException("Vertex is unknown"); // TODO MVR ...
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
            throw new IllegalArgumentException("Edge is unknown"); // TODO MVR ...
        }
        if (!edgeIdToPropertiesMap.containsKey(edgeId)) {
            edgeIdToPropertiesMap.put(edgeId, new HashMap<>());
        }
        return edgeIdToPropertiesMap.get(edgeId);
    }

    public GenericGraph build() {
        final GenericGraph.GenericGraphBuilder genericGraphBuilder = GenericGraph.builder().properties(view.getProperties());
        view.getVertices().forEach(vertex -> GenericVertex.builder().vertex(vertex).properties(getVertexProperties(vertex)));
        view.getEdges().forEach(edge -> GenericEdge.builder().edge(edge).properties(getEdgeProperties(edge)));
        return genericGraphBuilder.build();
    }

}
