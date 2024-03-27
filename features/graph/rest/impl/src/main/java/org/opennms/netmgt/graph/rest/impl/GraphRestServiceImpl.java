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
package org.opennms.netmgt.graph.rest.impl;

import java.util.List;
import java.util.Objects;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;
import org.opennms.netmgt.graph.api.ImmutableGraph;
import org.opennms.netmgt.graph.api.ImmutableGraphContainer;
import org.opennms.netmgt.graph.api.enrichment.EnrichmentService;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.service.GraphService;
import org.opennms.netmgt.graph.rest.api.GraphRestService;
import org.opennms.netmgt.graph.rest.api.Query;
import org.opennms.netmgt.graph.rest.impl.converter.JsonConverterService;
import org.opennms.netmgt.graph.rest.impl.renderer.JsonGraphRenderer;
import org.osgi.framework.BundleContext;

public class GraphRestServiceImpl implements GraphRestService {

    private final BundleContext bundleContext;
    private final GraphService graphService;
    private final EnrichmentService enrichmentService;

    public GraphRestServiceImpl(final GraphService graphService, final EnrichmentService enrichmentService, final BundleContext bundleContext) {
        this.graphService = Objects.requireNonNull(graphService);
        this.enrichmentService = Objects.requireNonNull(enrichmentService);
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    @Override
    public Response listContainerInfo() {
        final List<GraphContainerInfo> graphContainerInfos = graphService.getGraphContainerInfos();
        if (graphContainerInfos.isEmpty()) {
            return Response.noContent().build();
        }
        final String rendered = render(graphContainerInfos);
        return Response.ok(rendered).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public Response getContainer(String containerId) {
        final ImmutableGraphContainer container = graphService.getGraphContainer(containerId);
        if (container == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        final String rendered = render(container);
        return Response.ok(rendered).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public Response getGraph(String containerId, String namespace) {
        final GenericGraph graph = graphService.getGraph(containerId, namespace);
        if (graph == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        final String rendered = render(graph);
        return Response.ok(rendered).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    @Override
    public Response getView(String containerId, String namespace, Query query) {
        final GenericGraph graph = graphService.getGraph(containerId, namespace);
        if (graph == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if (query.getSemanticZoomLevel() == null) {
            query.setSemanticZoomLevel(Query.DEFAULT_SEMANTIC_ZOOM_LEVEL);
        }
        if (query.getSemanticZoomLevel() < 0) {
            return Response
                .status(Response.Status.BAD_REQUEST)
                .entity(new JSONObject().put("error", "SemanticZoomLevel must be >= 0 but was " + query.getSemanticZoomLevel()).toString())
                .build();
        }
        if (query.getVerticesInFocus() == null || query.getVerticesInFocus().isEmpty()) {
            query.setVerticesInFocus(graph.getDefaultFocus().getVertexIds());
        }
        final List<GenericVertex> focussedVertices = graph.resolveVertices(query.getVerticesInFocus());
        final GenericGraph view = graph.getView(focussedVertices, query.getSemanticZoomLevel()).asGenericGraph();

        // Apply enrichment
        final GenericGraph enrichedView = enrichmentService.enrich(view);
        final JSONObject jsonView = new JsonConverterService(bundleContext).convert(enrichedView);
        jsonView.put("focus", convert(query));
        jsonView.remove("defaultFocus"); // There shouldn't be a default focus
        return Response.ok(jsonView.toString()).type(MediaType.APPLICATION_JSON_TYPE).build();
    }

    private static JSONObject convert(Query query) {
        final JSONObject jsonQuery = new JSONObject();
        jsonQuery.put("semanticZoomLevel", query.getSemanticZoomLevel());
        jsonQuery.put("vertices", query.getVerticesInFocus());
        return jsonQuery;
    }

    private String render(List<GraphContainerInfo> infos) {
        return new JsonGraphRenderer(bundleContext).render(infos);
    }

    private String render(ImmutableGraphContainer graphContainer) {
        return new JsonGraphRenderer(bundleContext).render(graphContainer);
    }

    private String render(ImmutableGraph graph) {
        return new JsonGraphRenderer(bundleContext).render(graph);
    }
}
