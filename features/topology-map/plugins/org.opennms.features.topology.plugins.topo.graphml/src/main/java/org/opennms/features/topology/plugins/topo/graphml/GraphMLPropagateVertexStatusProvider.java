/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.graphml;

import com.google.common.collect.Maps;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.graphml.internal.AlarmSummaryWrapper;
import org.opennms.features.topology.plugins.topo.graphml.internal.GraphMLServiceAccessor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class GraphMLPropagateVertexStatusProvider implements StatusProvider {

    private final GraphMLTopologyProvider provider;
    private final BundleContext bundleContext;
    private final AlarmSummaryWrapper alarmSummaryWrapper;

    private final GraphMLServiceAccessor serviceAccessor;

    public GraphMLPropagateVertexStatusProvider(final GraphMLTopologyProvider provider,
                                                final BundleContext bundleContext,
                                                final AlarmSummaryWrapper alarmSummaryWrapper,
                                                final GraphMLServiceAccessor serviceAccessor) {
        this.provider = Objects.requireNonNull(provider);
        this.bundleContext = Objects.requireNonNull(bundleContext);
        this.alarmSummaryWrapper = Objects.requireNonNull(alarmSummaryWrapper);

        this.serviceAccessor = Objects.requireNonNull(serviceAccessor);
    }

    @Override
    public Map<? extends VertexRef, ? extends Status> getStatusForVertices(final VertexProvider vertexProvider,
                                                                           final Collection<VertexRef> vertices,
                                                                           final Criteria[] criteria) {

        final Map<VertexRef, GraphMLVertexStatus> statuses = Maps.newHashMap();

        try {
            final Collection<ServiceReference<StatusProvider>> statusProviderReferences = this.bundleContext.getServiceReferences(StatusProvider.class, null);

            for (final ServiceReference<StatusProvider> statusProviderReference : statusProviderReferences) {
                try {
                    final StatusProvider statusProvider = bundleContext.getService(statusProviderReference);

                    for (final VertexRef vertex : vertices) {
                        GraphMLVertexStatus mergedStatus = statuses.getOrDefault(vertex, new GraphMLVertexStatus());
                        for (VertexRef childVertex : this.provider.getMetaTopologyProvider().getOppositeVertices(vertex)) {
                            if (statusProvider.contributesTo(childVertex.getNamespace())) {
                                final GraphMLVertexStatus childStatus = (GraphMLVertexStatus) statusProvider.getStatusForVertices(
                                        this.provider.getMetaTopologyProvider().getGraphProviderBy(childVertex.getNamespace()),
                                        Collections.singleton(childVertex),
                                        new Criteria[0]).get(childVertex);
                                mergedStatus = GraphMLVertexStatus.merge(mergedStatus, childStatus);
                            }
                        }
                        statuses.put(vertex, mergedStatus);
                    }

                } finally {
                    bundleContext.ungetService(statusProviderReference);
                }
            }

        } catch (final InvalidSyntaxException e) {
        }

        return statuses;
    }

    @Override
    public String getNamespace() {
        return provider.getVertexNamespace();
    }

    @Override
    public boolean contributesTo(String namespace) {
        return getNamespace().equals(namespace);
    }
}
