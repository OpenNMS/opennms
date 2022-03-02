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

package org.opennms.netmgt.graph.rest.impl.converter.json;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opennms.netmgt.graph.api.Edge;
import org.opennms.netmgt.graph.api.ImmutableGraph;
import org.opennms.netmgt.graph.api.Vertex;
import org.opennms.netmgt.graph.api.focus.Focus;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.rest.api.Converter;
import org.opennms.netmgt.graph.rest.impl.converter.JsonPropertyConverterService;
import org.osgi.framework.BundleContext;

public class GraphConverter implements Converter<ImmutableGraph<?, ?>, JSONObject> {

    private final BundleContext bundleContext;

    public GraphConverter(BundleContext bundleContext) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    @Override
    public boolean canConvert(Class<ImmutableGraph<?, ?>> type) {
        return ImmutableGraph.class.isAssignableFrom(type);
    }

    @Override
    public JSONObject convert(ImmutableGraph<?, ?> input) {
        final JSONObject jsonGraph = new JSONObject();
        final JSONArray jsonEdgesArray = new JSONArray();
        final JSONArray jsonVerticesArray = new JSONArray();
        jsonGraph.put("edges", jsonEdgesArray);
        jsonGraph.put("vertices", jsonVerticesArray);


        if (input != null) {
            final JsonPropertyConverterService propertyConverterService = new JsonPropertyConverterService(bundleContext);
            final Map<String, Object> properties = input.asGenericGraph().getProperties();
            final JSONObject convertedProperties = propertyConverterService.convert(properties);
            convertedProperties.toMap().forEach(jsonGraph::put);

            // Convert Edges
            input.getEdges().stream()
                    .sorted(Comparator.comparing(Edge::getId))
                    .forEach(edge -> {
                        final GenericEdge genericEdge = edge.asGenericEdge();
                        final Map<String, Object> edgeProperties = new HashMap<>(genericEdge.getProperties());
                        edgeProperties.put("source", genericEdge.getSource());
                        edgeProperties.put("target", genericEdge.getTarget());
                        final JSONObject jsonEdge = propertyConverterService.convert(edgeProperties);
                        jsonEdgesArray.put(jsonEdge);
                    });

            // Convert Vertices
            input.getVertices().stream()
                    .sorted(Comparator.comparing(Vertex::getId))
                    .forEach(vertex -> {
                        final JSONObject jsonVertex = new VertexConverter(bundleContext).convert(vertex);
                        jsonVerticesArray.put(jsonVertex);
                    });

            // Convert the focus
            final Focus defaultFocus = input.getDefaultFocus();
            final JSONObject jsonFocus = new JSONObject();
            jsonFocus.put("type", defaultFocus.getId());
            jsonFocus.put("vertexIds", new JSONArray(defaultFocus.getVertexRefs()));
            jsonGraph.put("defaultFocus", jsonFocus);
        }
        return jsonGraph;
    }
}
