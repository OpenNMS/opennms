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

package org.opennms.netmgt.graph.provider.topology;

import java.util.List;
import java.util.Map;
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
import org.opennms.netmgt.graph.api.NodeRef;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.service.GraphService;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class LegacyTopologyProvider implements GraphProvider {

    private final String containerId;
    private final String namespace;
    private final GraphService graphService;
    private final NodeDao nodeDao;
    private final boolean resolveNodeIds;

    private LegacyBackendGraph backendGraph;

    public LegacyTopologyProvider(final LegacyTopologyConfiguration configuration, final NodeDao nodeDao, final GraphService graphService, final String containerId, final String graphNamespace) {
        this.containerId = Objects.requireNonNull(containerId);
        this.namespace = Objects.requireNonNull(graphNamespace);
        this.graphService = Objects.requireNonNull(graphService);
        this.nodeDao = Objects.requireNonNull(nodeDao);
        this.resolveNodeIds = Objects.requireNonNull(configuration).isResolveNodeIds();
    }

    @Override
    public BackendGraph getCurrentGraph() {
        return this.backendGraph;
    }

    @Override
    public void refresh() {
        final GenericGraph graph = graphService.getGraph(containerId, namespace);
        this.backendGraph = new LegacyBackendGraph(graph);

        // Optionally resolve the nodIds of vertices providing information related to nodes
        if (resolveNodeIds) {
            // Update nodeId information as enrichment is not implemented at the moment
            final Map<String, Map<String, Integer>> nodeIdMap = getNodeIdMap(graph);
            graph.getVertices().stream()
                .filter(v -> v.getNodeRef() != null && v.getNodeRef().getNodeId() == null)
                .forEach(vertex -> {
                    final NodeRef nodeRef = vertex.getNodeRef();
                    final Map<String, Integer> foreignIdNodeIdMap = nodeIdMap.get(nodeRef.getForeignSource());
                    final Integer nodeId = foreignIdNodeIdMap.get(nodeRef.getForeignId());
                    if (nodeId != null) {
                        backendGraph.getVertex(getNamespace(), vertex.getId()).setNodeID(nodeId);
                    }
                });
        }
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

    private Map<String, Map<String, Integer>> getNodeIdMap(GenericGraph graph) {
        final Set<String> foreignSources = graph.getVertices().stream()
                .filter(v -> v.getNodeRef() != null && v.getNodeRef().getNodeId() == null)
                .map(v -> v.getNodeRef().getForeignSource())
                .collect(Collectors.toSet());
        final Map<String, Map<String, Integer>> foreignSourceMap = Maps.newHashMap();
        for (String eachForeignSource : foreignSources) {
            final Map<String, Integer> foreignIdToNodeIdMap = nodeDao.getForeignIdToNodeIdMap(eachForeignSource);
            foreignSourceMap.put(eachForeignSource, foreignIdToNodeIdMap);
        }
        return foreignSourceMap;
    }
}
