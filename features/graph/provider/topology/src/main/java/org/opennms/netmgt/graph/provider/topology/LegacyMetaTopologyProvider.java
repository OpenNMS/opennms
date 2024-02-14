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
package org.opennms.netmgt.graph.provider.topology;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.support.breadcrumbs.BreadcrumbStrategy;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.MetaTopologyProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.graph.api.enrichment.EnrichmentService;
import org.opennms.netmgt.graph.api.service.GraphService;

public class LegacyMetaTopologyProvider implements MetaTopologyProvider {

    private final GraphService graphService;
    private final EnrichmentService enrichmentService;
    private final String containerId;
    private final Map<String, GraphProvider> providers;

    public LegacyMetaTopologyProvider(final LegacyTopologyConfiguration configuration, final NodeDao nodeDao,
                                      final GraphService graphService,
                                      final EnrichmentService enrichmentService,
                                      final String containerId) {
        this.graphService = Objects.requireNonNull(graphService);
        this.enrichmentService = Objects.requireNonNull(enrichmentService);
        this.containerId = Objects.requireNonNull(containerId);

        // Build TopologyProvider delegations
        this.providers = graphService.getGraphContainerInfo(containerId).getNamespaces().stream()
                .map(namespace -> new LegacyTopologyProvider(configuration, nodeDao, graphService, enrichmentService, containerId, namespace))
                .collect(Collectors.toMap(LegacyTopologyProvider::getNamespace, Function.identity()));
    }

    @Override
    public GraphProvider getDefaultGraphProvider() {
        final String defaultNamespace = graphService.getGraphContainerInfo(containerId).getPrimaryGraphInfo().getNamespace();
        return providers.get(defaultNamespace);
    }

    @Override
    public Collection<GraphProvider> getGraphProviders() {
        return providers.values();
    }

    @Override
    public Collection<VertexRef> getOppositeVertices(VertexRef vertexRef) {
        Objects.requireNonNull(vertexRef);
        final GraphProvider graphProvider = providers.get(vertexRef.getNamespace());
        if (graphProvider.getCurrentGraph() != null) {
            final EdgeRef[] referencingEdges = graphProvider.getCurrentGraph().getEdgeIdsForVertex(vertexRef);
            final List<VertexRef> oppositeVertices = graphProvider.getCurrentGraph().getEdges(Arrays.asList(referencingEdges)) // resolve edges
                    .stream()
                    // select edges which point to another namespace
                    .filter(edge -> !edge.getSource().getVertex().getNamespace().equals(vertexRef.getNamespace()) || !edge.getTarget().getVertex().getNamespace().equals(vertexRef.getNamespace()))
                    // get the "other" vertex (the one where the namespace does not match)
                    .map(edge -> edge.getSource().getVertex().getNamespace().equals(vertexRef.getNamespace()) ? edge.getTarget().getVertex() : edge.getSource().getVertex())
                    .collect(Collectors.toList());
            return oppositeVertices;
        }
        return Collections.EMPTY_LIST;
    }

    @Override
    public GraphProvider getGraphProviderBy(String namespace) {
        return providers.get(namespace);
    }

    @Override
    public BreadcrumbStrategy getBreadcrumbStrategy() {
        return BreadcrumbStrategy.NONE;
    }

    @Override
    public String getId() {
        return containerId;
    }
}
