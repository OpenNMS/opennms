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
