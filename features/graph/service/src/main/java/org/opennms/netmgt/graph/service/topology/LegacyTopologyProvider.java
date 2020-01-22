/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020-2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.service.topology;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.support.hops.DefaultVertexHopCriteria;
import org.opennms.features.topology.api.topo.BackendGraph;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.Defaults;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.TopologyProviderInfo;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.service.GraphService;

import com.google.common.collect.Lists;

public class LegacyTopologyProvider implements GraphProvider {

    private final String containerId;
    private final String namespace;
    private final GraphService graphService;

    private LegacyBackendGraph backendGraph;

    public LegacyTopologyProvider(final GraphService graphService, final String containerId, final String graphNamespace) {
        this.containerId = Objects.requireNonNull(containerId);
        this.namespace = Objects.requireNonNull(graphNamespace);
        this.graphService = Objects.requireNonNull(graphService);
    }

    @Override
    public BackendGraph getCurrentGraph() {
        return this.backendGraph;
    }

    @Override
    public void refresh() {
        final GenericGraph graph = graphService.getGraph(containerId, namespace);
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
                    if (backendGraph != null && !backendGraph.getVertices().isEmpty()) {
                        return Lists.newArrayList(
                                new DefaultVertexHopCriteria(
                                        new DefaultVertexRef(namespace, backendGraph.getVertices().get(0).getId())));
                    }
                    return Collections.emptyList();
                });
    }

    @Override
    public TopologyProviderInfo getTopologyProviderInfo() {
        return new LegacyTopologyProviderInfo(graphService.getGraphInfo(namespace));
    }

    @Override
    public SelectionChangedListener.Selection getSelection(List<VertexRef> selectedVertices, ContentType type) {
        return SelectionChangedListener.Selection.NONE;
    }

    @Override
    public boolean contributesTo(ContentType type) {
        return false;
    }
}
