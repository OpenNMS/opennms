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

import static org.opennms.netmgt.graph.enrichment.EnrichmentUtils.*;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.netmgt.graph.api.NodeRef;
import org.opennms.netmgt.graph.api.NodeService;
import org.opennms.netmgt.graph.api.enrichment.EnrichmentGraphBuilder;
import org.opennms.netmgt.graph.api.enrichment.EnrichmentProcessor;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericProperties;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.info.NodeInfo;

public class NodeEnrichmentProcessor implements EnrichmentProcessor {

    private final NodeService nodeService;

    public NodeEnrichmentProcessor(NodeService nodeService) {
        this.nodeService = Objects.requireNonNull(nodeService);
    }

    @Override
    public boolean canEnrich(final GenericGraph graph) {
        return parseBoolean(graph.getProperties(), GenericProperties.Enrichment.RESOLVE_NODES);
    }

    @Override
    public void enrich(EnrichmentGraphBuilder graphBuilder) {
        final List<NodeRef> nodeRefs = graphBuilder.getVertices().stream().map(GenericVertex::getNodeRef).filter(Objects::nonNull).collect(Collectors.toList());
        final List<NodeInfo> nodeInfos = nodeService.resolveNodes(nodeRefs);
        nodeInfos.forEach(ni -> {
            final List<GenericVertex> vertices = graphBuilder.resolveVertices(ni.getNodeRef());
            for (GenericVertex eachVertex : vertices) {
                graphBuilder.property(eachVertex, GenericProperties.NODE_INFO, ni);
            }
        });
    }
}
