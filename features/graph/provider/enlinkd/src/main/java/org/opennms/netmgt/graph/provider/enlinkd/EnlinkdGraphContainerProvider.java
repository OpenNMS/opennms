/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.graph.provider.enlinkd;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.opennms.netmgt.enlinkd.model.CdpElementTopologyEntity;
import org.opennms.netmgt.enlinkd.model.CdpLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.persistence.api.TopologyEntityCache;
import org.opennms.netmgt.graph.api.GraphContainer;
import org.opennms.netmgt.graph.api.info.DefaultGraphContainerInfo;
import org.opennms.netmgt.graph.api.info.DefaultGraphInfo;
import org.opennms.netmgt.graph.api.info.GraphContainerInfo;
import org.opennms.netmgt.graph.api.service.GraphContainerProvider;
import org.opennms.netmgt.graph.simple.SimpleEdge;
import org.opennms.netmgt.graph.simple.SimpleGraph;
import org.opennms.netmgt.graph.simple.SimpleGraphContainer;
import org.opennms.netmgt.graph.simple.SimpleVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnlinkdGraphContainerProvider implements GraphContainerProvider {
    private static final Logger LOG = LoggerFactory.getLogger(EnlinkdGraphContainerProvider.class);

    protected static final String NAMESPACE = "enlinkd";

    private final GraphContainerInfo containerInfo;
    private TopologyEntityCache topologyEntityCache;

    public EnlinkdGraphContainerProvider(TopologyEntityCache topologyEntityCache) {
        this.topologyEntityCache = Objects.requireNonNull(topologyEntityCache);
        this.containerInfo = createGraphContainerInfo();
    }

    @Override
    public GraphContainer loadGraphContainer() {
        // Hard refresh everything, everytime
        topologyEntityCache.refresh();

        final SimpleGraphContainer simpleGraphContainer = new SimpleGraphContainer(getContainerInfo());
        final SimpleGraph targetGraph = new SimpleGraph(NAMESPACE);
        targetGraph.setLabel(simpleGraphContainer.getLabel());
        targetGraph.setDescription(simpleGraphContainer.getDescription());

        // CDP!
        loadCdpLinks(targetGraph);

        simpleGraphContainer.addGraph(targetGraph);
        return simpleGraphContainer;
    }

    @Override
    public GraphContainerInfo getContainerInfo() {
        return containerInfo;
    }


    private void loadCdpLinks(SimpleGraph graph) {
        List<CdpElementTopologyEntity> cdpElements = topologyEntityCache.getCdpElementTopologyEntities();
        List<CdpLinkTopologyEntity> allLinks = topologyEntityCache.getCdpLinkTopologyEntities();
        List<Pair<CdpLinkTopologyEntity, CdpLinkTopologyEntity>> matchedCdpLinks = matchCdpLinks(cdpElements, allLinks);
        for (Pair<CdpLinkTopologyEntity, CdpLinkTopologyEntity> pair : matchedCdpLinks) {
            connectCdpLinkPair(graph, pair);
        }
    }

    List<Pair<CdpLinkTopologyEntity, CdpLinkTopologyEntity>> matchCdpLinks(final List<CdpElementTopologyEntity> cdpElements, final List<CdpLinkTopologyEntity> allLinks) {

        // 1. create lookup maps:
        Map<Integer, CdpElementTopologyEntity> cdpelementmap = new HashMap<>();
        for (CdpElementTopologyEntity cdpelement: cdpElements) {
            cdpelementmap.put(cdpelement.getNodeId(), cdpelement);
        }
        Map<CompositeKey, CdpLinkTopologyEntity> targetLinkMap = new HashMap<>();
        for (CdpLinkTopologyEntity targetLink : allLinks) {
            CompositeKey key = new CompositeKey(targetLink.getCdpCacheDevicePort(),
                    targetLink.getCdpInterfaceName(),
                    cdpelementmap.get(targetLink.getNodeId()).getCdpGlobalDeviceId(),
                    targetLink.getCdpCacheDeviceId());
            targetLinkMap.put(key, targetLink);
        }
        Set<Integer> parsed = new HashSet<Integer>();

        // 2. iterate
        List<Pair<CdpLinkTopologyEntity, CdpLinkTopologyEntity>> results = new ArrayList<>();
        for (CdpLinkTopologyEntity sourceLink : allLinks) {
            if (parsed.contains(sourceLink.getId())) {
                continue;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("getCdpLinks: source: {} ", sourceLink);
            }
            CdpElementTopologyEntity sourceCdpElement = cdpelementmap.get(sourceLink.getNodeId());

            CdpLinkTopologyEntity targetLink = targetLinkMap.get(new CompositeKey(sourceLink.getCdpInterfaceName(),
                    sourceLink.getCdpCacheDevicePort(),
                    sourceLink.getCdpCacheDeviceId(),
                    sourceCdpElement.getCdpGlobalDeviceId()));

            if (targetLink == null) {
                LOG.debug("getCdpLinks: cannot found target for source: '{}'", sourceLink.getId());
                continue;
            }

            if (sourceLink.getId().equals(targetLink.getId()) || parsed.contains(targetLink.getId())) {
                continue;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("getCdpLinks: cdp: {}, target: {} ", sourceLink.getCdpCacheDevicePort(), targetLink);
            }

            parsed.add(sourceLink.getId());
            parsed.add(targetLink.getId());
            results.add(Pair.of(sourceLink, targetLink));
        }
        return results;
    }

    private void connectCdpLinkPair(SimpleGraph graph, Pair<CdpLinkTopologyEntity, CdpLinkTopologyEntity> pair) {
        CdpLinkTopologyEntity sourceLink = pair.getLeft();
        CdpLinkTopologyEntity targetLink = pair.getRight();
        SimpleVertex sourceVertex = getOrCreateNodeVertex(graph, sourceLink.getNodeIdAsString());
        SimpleVertex targetVertex = getOrCreateNodeVertex(graph, targetLink.getNodeIdAsString());

        // Connect the two
        final SimpleEdge edge = new SimpleEdge(sourceVertex, targetVertex);
        graph.addEdge(edge);
    }

    private SimpleVertex getOrCreateNodeVertex(SimpleGraph graph, String nodeIdAsString) {
        SimpleVertex vertex = graph.getVertex(nodeIdAsString);
        if (vertex != null) {
            return vertex;
        }
        vertex = new NodeVertex(nodeIdAsString);
        vertex.setLabel(nodeIdAsString);
        graph.addVertex(vertex);
        return vertex;
    }

    private GraphContainerInfo createGraphContainerInfo() {
        final DefaultGraphContainerInfo containerInfo = new DefaultGraphContainerInfo(NAMESPACE);
        containerInfo.setLabel("Enlinkd Topology Provider");
        containerInfo.setDescription("This Topology Provider does stuff");

        final DefaultGraphInfo defaultGraphInfo = new DefaultGraphInfo(containerInfo.getId(), SimpleVertex.class);
        defaultGraphInfo.setLabel(containerInfo.getLabel());
        defaultGraphInfo.setDescription(containerInfo.getDescription());

        containerInfo.getGraphInfos().add(defaultGraphInfo);
        return containerInfo;
    }

    public void init() {

    }

    public void destroy() {

    }

}
