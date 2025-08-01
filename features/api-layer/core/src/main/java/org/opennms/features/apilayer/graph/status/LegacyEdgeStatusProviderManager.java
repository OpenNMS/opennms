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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.features.apilayer.common.utils.InterfaceMapper;
import org.opennms.features.apilayer.utils.ModelMappers;
import org.opennms.features.topology.api.topo.AbstractEdgeRef;
import org.opennms.features.topology.api.topo.BackendGraph;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.DefaultStatus;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.EdgeStatusProvider;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.integration.api.v1.graph.immutables.ImmutableEdge;
import org.opennms.integration.api.v1.graph.immutables.ImmutableVertexRef;
import org.opennms.integration.api.v1.graph.status.LegacyStatusProvider;
import org.opennms.integration.api.v1.graph.status.StatusInfo;
import org.opennms.netmgt.graph.provider.topology.LegacyEdge;
import org.opennms.netmgt.model.OnmsSeverity;
import org.osgi.framework.BundleContext;

public class LegacyEdgeStatusProviderManager extends InterfaceMapper<LegacyStatusProvider, EdgeStatusProvider> {

    public LegacyEdgeStatusProviderManager(final BundleContext bundleContext) {
        super(org.opennms.features.topology.api.topo.EdgeStatusProvider.class, bundleContext);
    }

    @Override
    public org.opennms.features.topology.api.topo.EdgeStatusProvider map(LegacyStatusProvider extension) {
        return new org.opennms.features.topology.api.topo.EdgeStatusProvider() {

            @Override
            public String getNamespace() {
                // This is not ideal, but technically the namespace is not required for the StatusProvider
                // So this returns null.
                return null;
            }

            @Override
            public boolean contributesTo(String namespace) {
                return extension.canCalculate(namespace);
            }

            @Override
            public Map<? extends EdgeRef, ? extends Status> getStatusForEdges(BackendGraph graph, Collection<EdgeRef> edges, Criteria[] criteria) {
                final Map<EdgeRef, Status> statusMap = new HashMap<>();
                edges.forEach(edgeRef -> {
                    // edgeRef is an Edge. If that is the case, the EdgeRef is returned casted as an Edge.
                    // However as the edgeRef is not the actual Edge implementation but a cloned version of AbstractEdge, thus not a LegacyEdge.
                    // In order to get the LegacyEdge, the edgeRef must not implement Edge, so here we create a new instanceof EdgeRef
                    // (using AbstractEdgeRef) to get the LegacyEdge reference
                    final EdgeRef actualEdgeRef = new AbstractEdgeRef(edgeRef);
                    final Edge edge = graph.getEdge(actualEdgeRef);
                    if (edge instanceof LegacyEdge) {
                        final LegacyEdge legacyEdge = (LegacyEdge) edge;
                        final VertexRef sourceVertex = legacyEdge.getSource().getVertex();
                        final VertexRef targetVertex = legacyEdge.getTarget().getVertex();
                        final ImmutableEdge apiEdge = ImmutableEdge
                                .newBuilder(
                                        legacyEdge.getNamespace(),
                                        legacyEdge.getId(),
                                        ImmutableVertexRef.newBuilder(sourceVertex.getNamespace(), sourceVertex.getId()).build(),
                                        ImmutableVertexRef.newBuilder(targetVertex.getNamespace(), targetVertex.getId()).build())
                                .properties(legacyEdge.getProperties())
                                .build();
                        final StatusInfo apiStatus = extension.calculateStatus(apiEdge);
                        final Status status = convert(apiStatus);
                        statusMap.put(edgeRef, status);
                    } else {
                        statusMap.put(edgeRef, new DefaultStatus(OnmsSeverity.INDETERMINATE.getLabel(), 0));
                    }
                });
                return statusMap;
            }
        };
    }

    public static Status convert(StatusInfo apiStatus) {
        if (apiStatus == null) {
            return new DefaultStatus(OnmsSeverity.NORMAL.getLabel(), 0);
        }
        final OnmsSeverity onmsSeverity = ModelMappers.fromSeverity(apiStatus.getSeverity());
        return new DefaultStatus(onmsSeverity.getLabel(), apiStatus.getCount());
    }
}
