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
