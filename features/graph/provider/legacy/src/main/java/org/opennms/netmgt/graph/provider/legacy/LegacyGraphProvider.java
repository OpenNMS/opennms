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

package org.opennms.netmgt.graph.provider.legacy;

import java.util.Objects;

import org.opennms.features.topology.api.topo.BackendGraph;
import org.opennms.features.topology.api.topo.MetaTopologyProvider;
import org.opennms.features.topology.api.topo.TopologyProviderInfo;
import org.opennms.netmgt.graph.api.ImmutableGraph;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.info.DefaultGraphInfo;
import org.opennms.netmgt.graph.api.info.GraphInfo;
import org.opennms.netmgt.graph.api.service.GraphProvider;

public class LegacyGraphProvider implements GraphProvider {

    private MetaTopologyProvider delegate;

    public LegacyGraphProvider(final MetaTopologyProvider metaTopologyProvider) {
        this.delegate = Objects.requireNonNull(metaTopologyProvider);
    }

    @Override
    public ImmutableGraph<?, ?> loadGraph() {
        final GraphInfo graphInfo = getGraphInfo();
        final BackendGraph currentGraph = delegate.getDefaultGraphProvider().getCurrentGraph();
        final GenericGraph.GenericGraphBuilder builder = GenericGraph.builder();
        builder.graphInfo(graphInfo).id(currentGraph.getNamespace());

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

        final GenericGraph graph = builder.build();
        return graph;
    }

    @Override
    public GraphInfo<?> getGraphInfo() {
        final org.opennms.features.topology.api.topo.GraphProvider defaultGraphProvider = delegate.getDefaultGraphProvider();
        final TopologyProviderInfo delegateInfo = defaultGraphProvider.getTopologyProviderInfo();
        final DefaultGraphInfo graphInfo = new DefaultGraphInfo(defaultGraphProvider.getNamespace(), LegacyVertex.class);
        graphInfo.setDescription(delegateInfo.getDescription());
        graphInfo.setLabel(delegateInfo.getName());
        return graphInfo;
    }
}
