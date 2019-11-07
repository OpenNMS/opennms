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

package org.opennms.netmgt.graph.rest.impl.renderer;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opennms.netmgt.graph.api.Edge;
import org.opennms.netmgt.graph.api.ImmutableGraph;
import org.opennms.netmgt.graph.api.ImmutableGraphContainer;
import org.opennms.netmgt.graph.api.Vertex;
import org.opennms.netmgt.graph.api.focus.Focus;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.info.GraphInfo;

public class JsonGraphRenderer implements GraphRenderer {

    @Override
    public String render(List<GraphContainerInfo> containerInfos) {
        final JSONArray graphContainerJsonArray = new JSONArray();
        containerInfos.stream()
            .sorted(Comparator.comparing(GraphContainerInfo::getId))
            .forEach(containerInfo -> {
                final JSONObject jsonGraphContainerInfoObject = new JSONObject();
                jsonGraphContainerInfoObject.put("id", containerInfo.getId());
                jsonGraphContainerInfoObject.put("label", containerInfo.getLabel());
                jsonGraphContainerInfoObject.put("description", containerInfo.getDescription());

                final JSONArray graphInfoArray = new JSONArray();
                containerInfo.getGraphInfos().stream()
                    .sorted(Comparator.comparing(GraphInfo::getNamespace))
                    .forEach(graphInfo -> {
                        final JSONObject jsonGraphInfoObject = new JSONObject();
                        jsonGraphInfoObject.put("namespace", graphInfo.getNamespace());
                        jsonGraphInfoObject.put("label", graphInfo.getLabel());
                        jsonGraphInfoObject.put("description", graphInfo.getDescription());
                        graphInfoArray.put(jsonGraphInfoObject);
                });
                jsonGraphContainerInfoObject.put("graphs", graphInfoArray);
                graphContainerJsonArray.put(jsonGraphContainerInfoObject);
        });
        return graphContainerJsonArray.toString();
    }

    @Override
    public String render(ImmutableGraphContainer<?> graphContainer) {
        final JSONObject jsonContainer = new JSONObject();
        final JSONArray jsonGraphArray = new JSONArray();
        jsonContainer.put("graphs", jsonGraphArray);

        final GenericGraphContainer genericGraphContainer = graphContainer.asGenericGraphContainer();
        genericGraphContainer.getProperties().forEach((key, value) -> jsonContainer.put(key, value));
        graphContainer.getGraphs()
            .stream()
            .sorted(Comparator.comparing(GraphInfo::getNamespace))
            .forEach(graph -> {
               final JSONObject jsonGraph = convert(graph);
               jsonGraphArray.put(jsonGraph);
            }
        );
        return jsonContainer.toString();
    }

    @Override
    public String render(ImmutableGraph<?, ?> graph) {
        final JSONObject jsonGraph = convert(graph);
        return jsonGraph.toString();
    }

    public static JSONObject convert(ImmutableGraph<?, ?> graph) {
        final JSONObject jsonGraph = new JSONObject();
        final JSONArray jsonEdgesArray = new JSONArray();
        final JSONArray jsonVerticesArray = new JSONArray();
        jsonGraph.put("edges", jsonEdgesArray);
        jsonGraph.put("vertices", jsonVerticesArray);

        if (graph != null) {
            graph.asGenericGraph().getProperties().forEach((key, value) -> jsonGraph.put(key, value));

            // Convert Edges
            graph.getEdges().stream()
                .sorted(Comparator.comparing(Edge::getId))
                .forEach(edge -> {
                    final GenericEdge genericEdge = edge.asGenericEdge();
                    final Map<String, Object> edgeProperties = new HashMap<>(genericEdge.getProperties());
                    edgeProperties.put("source", genericEdge.getSource().getId());
                    edgeProperties.put("target", genericEdge.getTarget().getId());
                    final JSONObject jsonEdge = new JSONObject(edgeProperties);
                    jsonEdgesArray.put(jsonEdge);
                });

            // Convert Vertices
            graph.getVertices().stream()
                .sorted(Comparator.comparing(Vertex::getId))
                .forEach(vertex -> {
                    final JSONObject jsonVertex = new JSONObject(vertex.asGenericVertex().getProperties());
                    jsonVerticesArray.put(jsonVertex);
                });

            // Convert the focus
            final Focus defaultFocus = graph.getDefaultFocus();
            final JSONObject jsonFocus = new JSONObject();
            jsonFocus.put("type", defaultFocus.getId());
            jsonFocus.put("vertexIds", new JSONArray(defaultFocus.getVertexRefs()));
            jsonGraph.put("defaultFocus", jsonFocus);
        }
        return jsonGraph;
    }
}
