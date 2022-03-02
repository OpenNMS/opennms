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

package org.opennms.features.apilayer.graph.status;

import org.opennms.features.apilayer.utils.InterfaceMapper;
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
