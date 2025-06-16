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
package org.opennms.features.apilayer.graph.status;

import org.opennms.features.apilayer.common.utils.InterfaceMapper;
import org.opennms.features.apilayer.utils.ModelMappers;
import org.opennms.integration.api.v1.graph.immutables.ImmutableEdge;
import org.opennms.integration.api.v1.graph.immutables.ImmutableVertex;
import org.opennms.integration.api.v1.graph.immutables.ImmutableVertexRef;
import org.opennms.integration.api.v1.graph.status.StatusInfo;
import org.opennms.integration.api.v1.graph.status.StatusProvider;
import org.opennms.integration.api.v1.model.Severity;
import org.opennms.netmgt.graph.api.enrichment.EnrichedProperties;
import org.opennms.netmgt.graph.api.enrichment.EnrichmentGraphBuilder;
import org.opennms.netmgt.graph.api.enrichment.EnrichmentProcessor;
import org.opennms.netmgt.graph.api.generic.GenericGraph;
import org.osgi.framework.BundleContext;

public class StatusProviderManager extends InterfaceMapper<StatusProvider, org.opennms.netmgt.graph.api.enrichment.EnrichmentProcessor> {

    public StatusProviderManager(final BundleContext bundleContext) {
        super(org.opennms.netmgt.graph.api.enrichment.EnrichmentProcessor.class, bundleContext);
    }

    @Override
    public EnrichmentProcessor map(final StatusProvider extension) {
        return new EnrichmentProcessor() {

            @Override
            public boolean canEnrich(GenericGraph graph) {
                return extension.canCalculate(graph.getNamespace());
            }

            @Override
            public void enrich(EnrichmentGraphBuilder graphBuilder) {
                graphBuilder.getVertices().forEach(vertex -> {
                    final ImmutableVertex apiVertex = ImmutableVertex
                            .newBuilder(vertex.getNamespace(), vertex.getId())
                            .properties(vertex.getProperties())
                            .build();
                    final StatusInfo apiStatus = extension.calculateStatus(apiVertex);
                    graphBuilder.property(vertex, EnrichedProperties.STATUS, convert(apiStatus));
                });
                graphBuilder.getEdges().forEach(edge -> {
                    final ImmutableEdge apiEdge = ImmutableEdge.newBuilder(edge.getNamespace(), edge.getId(),
                            ImmutableVertexRef.newBuilder(edge.getSource().getNamespace(), edge.getSource().getId()).build(),
                            ImmutableVertexRef.newBuilder(edge.getTarget().getNamespace(), edge.getTarget().getId()).build()
                    ).build();
                    final StatusInfo apiStatus = extension.calculateStatus(apiEdge);
                    graphBuilder.property(edge, EnrichedProperties.STATUS, convert(apiStatus));
                });
            }
        };
    }

    private static org.opennms.netmgt.graph.api.info.StatusInfo convert(final StatusInfo apiStatus) {
        if (apiStatus == null) {
            return org.opennms.netmgt.graph.api.info.StatusInfo.builder(org.opennms.netmgt.graph.api.info.Severity.Normal).build();
        }
        final org.opennms.netmgt.graph.api.info.StatusInfo.StatusInfoBuilder statusBuilder = org.opennms.netmgt.graph.api.info.StatusInfo.builder(convert(apiStatus.getSeverity()));
        if (apiStatus.getCount() != 0) {
            statusBuilder.count(apiStatus.getCount());
        }
        return statusBuilder.build();
    }

    private static org.opennms.netmgt.graph.api.info.Severity convert(final Severity apiSeverity) {
        return org.opennms.netmgt.graph.api.info.Severity.createFrom(ModelMappers.fromSeverity(apiSeverity));
    }

}
