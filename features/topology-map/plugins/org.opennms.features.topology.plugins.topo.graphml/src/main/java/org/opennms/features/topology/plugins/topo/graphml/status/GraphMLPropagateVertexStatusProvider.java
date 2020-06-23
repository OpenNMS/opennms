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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLMetaTopologyProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class GraphMLPropagateVertexStatusProvider implements StatusProvider {

    private static final Logger LOG = LoggerFactory.getLogger(GraphMLPropagateVertexStatusProvider.class);

    private static class LoopDetectionCriteria extends Criteria {
        private final Set<VertexRef> seen;

        private LoopDetectionCriteria(final LoopDetectionCriteria that,
                                      final Collection<VertexRef> vertices) {
            this();
            this.seen.addAll(that.seen);
            this.seen.addAll(vertices);
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
            if (!(o instanceof LoopDetectionCriteria)) {
                return false;
            }

            final LoopDetectionCriteria that = (LoopDetectionCriteria) o;

            return seen.equals(that.seen);
        }

        @Override
        public int hashCode() {
            return seen.hashCode();
        }

        public LoopDetectionCriteria with(final Collection<VertexRef> vertices) {
            return new LoopDetectionCriteria(this, Objects.requireNonNull(vertices));
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
        final List<Criteria> criteriaList = Lists.newArrayList(criteria);

        final LoopDetectionCriteria loopDetectionCriteria = Iterables.tryFind(criteriaList,
                                                                              c -> c instanceof LoopDetectionCriteria)
                                                                     .transform(c -> (LoopDetectionCriteria) c)
                                                                     .or(LoopDetectionCriteria::new);

        // Build map from namespace to opposite vertices
        final Multimap<String, VertexRef> oppositeVertices = HashMultimap.create();
        for (final VertexRef sourceVertex : vertices) {
            // Filter out loops
            if (loopDetectionCriteria.contains(sourceVertex)) {
                LOG.error("Loop detected with: {}:{}", sourceVertex.getNamespace(), sourceVertex.getId());
                continue;
            }

            for (VertexRef targetVertex : this.provider.getOppositeVertices(sourceVertex)) {
                oppositeVertices.put(targetVertex.getNamespace(), targetVertex);
            }
        }

        // Replace loop detection criteria with extended one
        criteriaList.remove(loopDetectionCriteria);
        criteriaList.add(loopDetectionCriteria.with(vertices));

        // Find and call status provider for each namespace and get result per opposite vertex
        final Map<VertexRef, Status> targetStatuses = Maps.newHashMap();
        try {
            final Collection<ServiceReference<StatusProvider>> statusProviderReferences = this.bundleContext.getServiceReferences(StatusProvider.class, null);
            for (final ServiceReference<StatusProvider> statusProviderReference : statusProviderReferences) {
                try {
                    final StatusProvider statusProvider = bundleContext.getService(statusProviderReference);

                    for (final Map.Entry<String, Collection<VertexRef>> e : oppositeVertices.asMap().entrySet()) {
                        if (statusProvider.contributesTo(e.getKey())) {
                            targetStatuses.putAll(statusProvider.getStatusForVertices(
                                                  this.provider.getGraphProviderBy(e.getKey()),
                                                  e.getValue(),
                                                  criteriaList.toArray(new Criteria[0])));
                        }
                    }

                } finally {
                    bundleContext.ungetService(statusProviderReference);
                }
            }
        } catch (final InvalidSyntaxException e) {
        }

        // Merge statuses from targets to sources
        final Map<VertexRef, GraphMLVertexStatus> statuses = Maps.newHashMap();
        for (final VertexRef sourceVertex : vertices) {
            GraphMLVertexStatus mergedStatus = new GraphMLVertexStatus();

            for (VertexRef targetVertex : this.provider.getOppositeVertices(sourceVertex)) {
                if (targetStatuses.containsKey(targetVertex)) {
                    mergedStatus = GraphMLVertexStatus.merge(mergedStatus, (GraphMLVertexStatus) targetStatuses.get(targetVertex));
                }
            }
            statuses.put(sourceVertex, mergedStatus);
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
