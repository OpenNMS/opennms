/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.api.topo;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.topo.simple.SimpleGraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractTopologyProvider implements GraphProvider {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractTopologyProvider.class);

    protected final BackendGraph graph;

    protected TopologyProviderInfo topologyProviderInfo = new DefaultTopologyProviderInfo();

    public AbstractTopologyProvider(String namespace) {
        this(new SimpleGraph(Objects.requireNonNull(namespace)));
    }

    public AbstractTopologyProvider(BackendGraph graph) {
        this.graph = Objects.requireNonNull(graph);
        LOG.debug("Creating a new {} with namespace {}", getClass().getSimpleName(), graph.getNamespace());
    }

    @Override
    public String getNamespace() {
        return graph.getNamespace();
    }

    public TopologyProviderInfo getTopologyProviderInfo() {
        return topologyProviderInfo;
    }

    public void setTopologyProviderInfo(TopologyProviderInfo topologyProviderInfo) {
        this.topologyProviderInfo = topologyProviderInfo;
    }

    @Override
    public BackendGraph getCurrentGraph() {
        return graph;
    }

    @Override
    public abstract void refresh();

    protected static SelectionChangedListener.Selection getSelection(String namespace, List<VertexRef> selectedVertices, ContentType type) {
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
}
