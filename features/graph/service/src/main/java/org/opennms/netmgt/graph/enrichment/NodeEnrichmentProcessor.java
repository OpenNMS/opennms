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

package org.opennms.netmgt.graph.enrichment;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.netmgt.graph.api.NodeRef;
import org.opennms.netmgt.graph.api.NodeService;
import org.opennms.netmgt.graph.api.enrichment.EnrichmentProcessor;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericProperties;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.info.NodeInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NodeEnrichmentProcessor implements EnrichmentProcessor {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final NodeService nodeService;

    public NodeEnrichmentProcessor(NodeService nodeService) {
        this.nodeService = Objects.requireNonNull(nodeService);
    }

    @Override
    public boolean canEnrich(GenericGraph graph) {
        // TODO MVR make it a constant
        final Boolean value = graph.getProperty("enrichment.resolveNodes", false);
        return value;
    }

    @Override
    public GenericGraph enrich(GenericGraph graph) {
        final List<NodeRef> nodeRefs = graph.getVertices().stream().map(v -> v.getNodeRef()).filter(ref -> ref != null).collect(Collectors.toList());
        final List<NodeInfo> nodeInfos = nodeService.resolveNodes(nodeRefs);
        final GenericGraph.GenericGraphBuilder enrichedGraphBuilder = GenericGraph.builder().graph(graph);
        nodeInfos.forEach(ni -> {
            final List<GenericVertex> vertices = enrichedGraphBuilder.resolveVertices(ni.getNodeRef());
            for (GenericVertex eachVertex : vertices) {
                GenericVertex enrichedVertex = GenericVertex.builder()
                        .vertex(eachVertex)
                        .property(GenericProperties.NODE_INFO, ni)
                        .build();
                enrichedGraphBuilder.removeVertex(eachVertex);
                enrichedGraphBuilder.addVertex(enrichedVertex);
            }
        });
        return enrichedGraphBuilder.build();
    }
}
