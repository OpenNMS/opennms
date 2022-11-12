/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

import java.util.List;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.MetaTopologyProvider;
import org.opennms.netmgt.graph.api.ImmutableGraph;
import org.opennms.netmgt.graph.api.ImmutableGraphContainer;
import org.opennms.netmgt.graph.api.generic.GenericGraphContainer;
import org.opennms.netmgt.graph.api.info.GraphInfo;

public class LegacyGraphContainer implements ImmutableGraphContainer<ImmutableGraph<?,?>> {
    private final MetaTopologyProvider delegate;

    public LegacyGraphContainer(MetaTopologyProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public List<ImmutableGraph<?, ?>> getGraphs() {
        return delegate.getGraphProviders().stream().map(LegacyGraph::getImmutableGraphFromTopoGraphProvider).collect(Collectors.toList());
    }

    @Override
    public ImmutableGraph<?, ?> getGraph(String namespace) {
        return LegacyGraph.getImmutableGraphFromTopoGraphProvider(delegate.getGraphProviderBy(namespace));
    }

    @Override
    public GenericGraphContainer asGenericGraphContainer() {
        GenericGraphContainer.GenericGraphContainerBuilder builder = GenericGraphContainer.builder()
            .id(delegate.getId())
            .label(delegate.getClass().getSimpleName())
            .description(delegate.getClass().getCanonicalName());
        for (ImmutableGraph<?,?> graph: getGraphs()) {
            builder.addGraph(graph.asGenericGraph());
        }
        builder.applyContainerInfo(this);
        return builder.build();
    }

    @Override
    public String getId() {
        return delegate.getId();
    }

    @Override
    public List<String> getNamespaces() {
        return delegate.getGraphProviders().stream().map(GraphProvider::getNamespace).collect(Collectors.toList());
    }

    @Override
    public String getDescription() {
        return delegate.getId();
    }

    @Override
    public String getLabel() {
        return delegate.getId();
    }

    @Override
    public GraphInfo getGraphInfo(String namespace) {
        return LegacyGraph.getGraphInfo(delegate.getGraphProviderBy(namespace));
    }

    @Override
    public GraphInfo getPrimaryGraphInfo() {
        return LegacyGraph.getGraphInfo(delegate.getDefaultGraphProvider());
    }

    @Override
    public List<GraphInfo> getGraphInfos() {
        return delegate.getGraphProviders().stream().map(LegacyGraph::getGraphInfo).collect(Collectors.toList());
    }
}
