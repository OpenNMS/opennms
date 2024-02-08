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
package org.opennms.features.topology.plugins.topo.graphml.status;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.StatusProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.api.topo.BackendGraph;
import org.opennms.features.topology.plugins.topo.graphml.GraphMLMetaTopologyProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

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
    public Map<? extends VertexRef, ? extends Status> getStatusForVertices(final BackendGraph graph,
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
                                                  this.provider.getGraphProviderBy(e.getKey()).getCurrentGraph(),
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
