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

package org.opennms.features.topology.plugins.topo.graphml.status;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLMetaTopologyProvider;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLTopologyProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class GraphMLPropagateVertexStatusProvider implements StatusProvider {

    private static final Logger LOG = LoggerFactory.getLogger(GraphMLPropagateVertexStatusProvider.class);

    private static class LoopDetectionCriteria extends Criteria {
        private final Set<VertexRef> seen;

        private LoopDetectionCriteria(final LoopDetectionCriteria other,
                                     final VertexRef vertex) {
            this();
            this.seen.addAll(other.seen);
            this.seen.add(vertex);
        }

        public LoopDetectionCriteria() {
            this.seen = new HashSet<>();
        }

        public boolean contains(final VertexRef vertex) {
            return this.seen.contains(vertex);
        }


        @Override
        public ElementType getType() {
            return ElementType.VERTEX;
        }

        @Override
        public String getNamespace() {
            return null;
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            final LoopDetectionCriteria that = (LoopDetectionCriteria) o;

            return seen.equals(that.seen);
        }

        @Override
        public int hashCode() {
            return seen.hashCode();
        }

        public LoopDetectionCriteria with(final VertexRef vertex) {
            return new LoopDetectionCriteria(this, vertex);
        }
    }

    private final String namespace;
    private final GraphMLMetaTopologyProvider provider;
    private final BundleContext bundleContext;

    public GraphMLPropagateVertexStatusProvider(final String namespace,
                                                final GraphMLMetaTopologyProvider provider,
                                                final BundleContext bundleContext) {
        this.namespace = Objects.requireNonNull(namespace);
        this.provider = Objects.requireNonNull(provider);
        this.bundleContext = Objects.requireNonNull(bundleContext);
    }

    @Override
    public Map<? extends VertexRef, ? extends Status> getStatusForVertices(final VertexProvider vertexProvider,
                                                                           final Collection<VertexRef> vertices,
                                                                           final Criteria[] criteria) {

        final LoopDetectionCriteria loopDetectionCriteria = (LoopDetectionCriteria) Iterables.find(Arrays.asList(criteria),
                                                                                                   c -> c instanceof LoopDetectionCriteria,
                                                                                                   new LoopDetectionCriteria());

        final Map<VertexRef, GraphMLVertexStatus> statuses = Maps.newHashMap();

        try {
            final Collection<ServiceReference<StatusProvider>> statusProviderReferences = this.bundleContext.getServiceReferences(StatusProvider.class, null);

            for (final ServiceReference<StatusProvider> statusProviderReference : statusProviderReferences) {
                try {
                    final StatusProvider statusProvider = bundleContext.getService(statusProviderReference);

                    for (final VertexRef vertex : vertices) {
                        if (loopDetectionCriteria.contains(vertex)) {
                            LOG.error("Loop detected with: {}:{}", vertex.getNamespace(), vertex.getId());
                            continue;
                        }

                        GraphMLVertexStatus mergedStatus = statuses.getOrDefault(vertex, new GraphMLVertexStatus());
                        for (VertexRef childVertex : this.provider.getOppositeVertices(vertex)) {
                            if (statusProvider.contributesTo(childVertex.getNamespace())) {
                                final GraphMLVertexStatus childStatus = (GraphMLVertexStatus) statusProvider.getStatusForVertices(
                                        this.provider.getGraphProviderBy(childVertex.getNamespace()),
                                        Collections.singleton(childVertex),
                                        new Criteria[] {loopDetectionCriteria.with(vertex)}).get(childVertex);
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
        return this.namespace;
    }

    @Override
    public boolean contributesTo(String namespace) {
        return this.getNamespace().equals(namespace);
    }
}
