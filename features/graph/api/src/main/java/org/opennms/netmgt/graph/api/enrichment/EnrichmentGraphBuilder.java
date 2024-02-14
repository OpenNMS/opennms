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
