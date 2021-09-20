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
import java.util.List;
import java.util.Objects;

import org.json.JSONArray;
import org.json.JSONObject;
import org.opennms.netmgt.graph.api.ImmutableGraph;
import org.opennms.netmgt.graph.api.ImmutableGraphContainer;
import org.opennms.netmgt.graph.api.Vertex;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.renderer.GraphRenderer;
import org.opennms.netmgt.graph.rest.impl.converter.JsonConverterService;
import org.osgi.framework.BundleContext;

public class JsonGraphRenderer implements GraphRenderer {

    private final BundleContext bundleContext;

    public JsonGraphRenderer(BundleContext bundleContext) {
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    @Override
    public String getContentType() {
        return "application/json";
    }

    @Override
    public String render(int identation, List<GraphContainerInfo> containerInfos) {
        final JSONArray graphContainerJsonArray = new JSONArray();
        containerInfos.stream()
            .sorted(Comparator.comparing(GraphContainerInfo::getId))
            .forEach(containerInfo -> {
                final JSONObject jsonGraphContainerInfoObject = new JsonConverterService(bundleContext).convert(containerInfo);
                graphContainerJsonArray.put(jsonGraphContainerInfoObject);
        });
        return graphContainerJsonArray.toString(identation);
    }

    @Override
    public String render(int identation, ImmutableGraphContainer<?> graphContainer) {
        final JSONObject jsonGraphContainer = new JsonConverterService(bundleContext).convert(graphContainer);
        return jsonGraphContainer.toString(identation);
    }

    @Override
    public String render(int identation, ImmutableGraph<?, ?> graph) {
        final JSONObject jsonGraph = new JsonConverterService(bundleContext).convert(graph);
        return jsonGraph.toString(identation);
    }

    @Override
    public String render(int identation, Vertex vertex) {
        final JSONObject jsonVertex = new JsonConverterService(bundleContext).convert(vertex);
        return jsonVertex.toString(identation);
    }

}
