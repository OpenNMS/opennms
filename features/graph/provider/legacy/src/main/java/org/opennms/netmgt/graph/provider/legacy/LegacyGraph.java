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
package org.opennms.netmgt.graph.provider.legacy;

import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.support.hops.VertexHopCriteria;
import org.opennms.features.topology.api.topo.BackendGraph;
import org.opennms.features.topology.api.topo.TopologyProviderInfo;
import org.opennms.netmgt.graph.api.ImmutableGraph;
import org.opennms.netmgt.graph.api.VertexRef;
import org.opennms.netmgt.graph.api.focus.Focus;
import org.opennms.netmgt.graph.api.focus.FocusStrategy;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericProperties;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.info.DefaultGraphInfo;
import org.opennms.netmgt.graph.api.info.GraphInfo;
import org.opennms.netmgt.graph.domain.AbstractDomainGraph;

import com.google.common.collect.Lists;

public class LegacyGraph extends AbstractDomainGraph<LegacyVertex, LegacyEdge> {

    public static GraphInfo getGraphInfo(org.opennms.features.topology.api.topo.GraphProvider topoGraphProvider) {
        final TopologyProviderInfo delegateInfo = topoGraphProvider.getTopologyProviderInfo();
        final DefaultGraphInfo graphInfo = new DefaultGraphInfo(topoGraphProvider.getNamespace());
        graphInfo.setDescription(delegateInfo.getDescription());
        graphInfo.setLabel(delegateInfo.getName());
        return graphInfo;
    }

    public static LegacyGraph getLegacyGraphFromTopoGraphProvider(org.opennms.features.topology.api.topo.GraphProvider topoGraphProvider) {
        return new LegacyGraph(getImmutableGraphFromTopoGraphProvider(topoGraphProvider).asGenericGraph());
    }

    public static ImmutableGraph<?,?> getImmutableGraphFromTopoGraphProvider(org.opennms.features.topology.api.topo.GraphProvider topoGraphProvider) {
        topoGraphProvider.refresh();
        final BackendGraph currentGraph = topoGraphProvider.getCurrentGraph();
        final GenericGraph.GenericGraphBuilder builder = GenericGraph.builder();
        builder.graphInfo(getGraphInfo(topoGraphProvider))
                .id(currentGraph.getNamespace())
                .property(GenericProperties.Enrichment.RESOLVE_NODES, true)
                .property(GenericProperties.Enrichment.DEFAULT_STATUS, true);

        currentGraph.getVertices().forEach(legacyVertex -> {
            final LegacyVertex domainVertex = new LegacyVertex(legacyVertex);
            final GenericVertex genericVertex = domainVertex.asGenericVertex();
            builder.addVertex(genericVertex);
        });

        currentGraph.getEdges().forEach(legacyEdge -> {
            final LegacyEdge domainEdge = new LegacyEdge(legacyEdge);
            final GenericEdge genericEdge = domainEdge.asGenericEdge();
            builder.addEdge(genericEdge);
        });

        final Set<VertexRef> focus = topoGraphProvider.getDefaults().getCriteria().stream()
                .filter(c -> VertexHopCriteria.class.isAssignableFrom(c.getClass()))
                .map(c -> (VertexHopCriteria) c)
                .flatMap(c -> c.getVertices().stream())
                .map(v -> new org.opennms.netmgt.graph.api.VertexRef(v.getNamespace(), v.getId()))
                .collect(Collectors.toSet());
        builder.focus(new Focus(FocusStrategy.SELECTION, Lists.newArrayList(focus)));
        return builder.build();
    }

    public LegacyGraph(GenericGraph genericGraph) {
        super(genericGraph);
    }

    @Override
    protected ImmutableGraph<LegacyVertex, LegacyEdge> convert(GenericGraph graph) {
        return new LegacyGraph(graph);
    }

    @Override
    protected LegacyVertex convert(GenericVertex vertex) {
        return new LegacyVertex(vertex);
    }

    @Override
    protected LegacyEdge convert(GenericEdge edge) {
        return new LegacyEdge(edge);
    }

}
