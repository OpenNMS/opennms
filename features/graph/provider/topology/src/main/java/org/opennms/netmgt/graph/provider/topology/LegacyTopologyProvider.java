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

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.BackendGraph;
import org.opennms.features.topology.api.topo.Defaults;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.TopologyProviderInfo;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.graph.api.enrichment.EnrichmentService;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.service.GraphService;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class LegacyTopologyProvider implements GraphProvider {

    private final String containerId;
    private final String namespace;
    private final GraphService graphService;
    private final NodeDao nodeDao;
    private final boolean resolveNodeIds;
    private final EnrichmentService enrichmentService;

    private LegacyBackendGraph backendGraph;

    public LegacyTopologyProvider(final LegacyTopologyConfiguration configuration, final NodeDao nodeDao,
                                  final GraphService graphService, final EnrichmentService enrichmentService,
                                  final String containerId, final String graphNamespace) {
        this.containerId = Objects.requireNonNull(containerId);
        this.namespace = Objects.requireNonNull(graphNamespace);
        this.graphService = Objects.requireNonNull(graphService);
        this.enrichmentService = Objects.requireNonNull(enrichmentService);
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.resolveNodeIds = Objects.requireNonNull(configuration).isResolveNodeIds();
    }

    @Override
    public BackendGraph getCurrentGraph() {
        return this.backendGraph;
    }

    @Override
    public void refresh() {
        GenericGraph graph = graphService.getGraph(containerId, namespace);

        // Enrichment
        if (resolveNodeIds) {
            graph = this.enrichmentService.enrich(graph);
        }

        this.backendGraph = new LegacyBackendGraph(graph);
    }

    @Override
    public String getNamespace() {
        return namespace;
    }

    @Override
    public Defaults getDefaults() {
        return new Defaults()
                .withPreferredLayout("D3 Layout")
                .withSemanticZoomLevel(1)
                .withCriteria(() -> {
                    if (backendGraph != null) {
                        return backendGraph.getDefaultCriteria();
                    }
                    return Lists.newArrayList();
                });
    }

    @Override
    public TopologyProviderInfo getTopologyProviderInfo() {
        return new LegacyTopologyProviderInfo(graphService.getGraphInfo(namespace));
    }

    @Override
    public SelectionChangedListener.Selection getSelection(List<VertexRef> selectedVertices, ContentType type) {
        final Set<Integer> nodeIds = selectedVertices.stream()
                .filter(v -> namespace.equals(v.getNamespace()))
                .filter(v -> v instanceof AbstractVertex)
                .map(v -> (AbstractVertex) v)
                .map(v -> v.getNodeID())
                .filter(nodeId -> nodeId != null)
                .collect(Collectors.toSet());
        if (type == ContentType.Alarm) {
            return new SelectionChangedListener.AlarmNodeIdSelection(nodeIds);
        }
        if (type == ContentType.Node) {
            return new SelectionChangedListener.IdSelection<>(nodeIds);
        }
        return SelectionChangedListener.Selection.NONE;
    }

    @Override
    public boolean contributesTo(ContentType type) {
        return Sets.newHashSet(ContentType.Alarm, ContentType.Node).contains(type);
    }
}
