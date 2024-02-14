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
package org.opennms.netmgt.graph.enrichment;

import static org.opennms.netmgt.graph.enrichment.EnrichmentUtils.parseBoolean;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.netmgt.graph.api.NodeRef;
import org.opennms.netmgt.graph.api.NodeService;
import org.opennms.netmgt.graph.api.enrichment.EnrichedProperties;
import org.opennms.netmgt.graph.api.enrichment.EnrichmentGraphBuilder;
import org.opennms.netmgt.graph.api.enrichment.EnrichmentProcessor;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.opennms.netmgt.graph.api.generic.GenericProperties;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.api.info.StatusInfo;

public class StatusEnrichmentProcessor implements EnrichmentProcessor {

    private final NodeService nodeService;

    public StatusEnrichmentProcessor(final NodeService nodeService) {
        this.nodeService = Objects.requireNonNull(nodeService);
    }

    @Override
    public boolean canEnrich(GenericGraph graph) {
        return parseBoolean(graph.getProperties(), GenericProperties.Enrichment.DEFAULT_STATUS);
    }

    @Override
    public void enrich(EnrichmentGraphBuilder graphBuilder) {
        final List<NodeRef> nodeRefs = graphBuilder.getVertices().stream().map(GenericVertex::getNodeRef).filter(Objects::nonNull).collect(Collectors.toList());
        final Map<NodeRef, StatusInfo> statusInfos = nodeService.resolveStatus(nodeRefs);
        for (Map.Entry<NodeRef, StatusInfo> eachEntry : statusInfos.entrySet()) {
            graphBuilder.resolveVertices(eachEntry.getKey())
                .forEach(vertex -> graphBuilder.property(vertex, EnrichedProperties.STATUS, eachEntry.getValue()));
        }
    }
}
