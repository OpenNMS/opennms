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

import static org.opennms.features.apilayer.graph.status.LegacyEdgeStatusProviderManager.convert;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.features.apilayer.common.utils.InterfaceMapper;
import org.opennms.features.topology.api.topo.BackendGraph;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.DefaultStatus;
import org.opennms.features.topology.api.topo.Status;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.integration.api.v1.graph.immutables.ImmutableVertex;
import org.opennms.integration.api.v1.graph.status.LegacyStatusProvider;
import org.opennms.integration.api.v1.graph.status.StatusInfo;
import org.opennms.netmgt.graph.provider.topology.LegacyVertex;
import org.opennms.netmgt.model.OnmsSeverity;
import org.osgi.framework.BundleContext;

public class LegacyVertexStatusProviderManager extends InterfaceMapper<LegacyStatusProvider, org.opennms.features.topology.api.topo.StatusProvider> {

    public LegacyVertexStatusProviderManager(final BundleContext bundleContext) {
        super(org.opennms.features.topology.api.topo.StatusProvider.class, bundleContext);
    }

    @Override
    public org.opennms.features.topology.api.topo.StatusProvider map(LegacyStatusProvider extension) {
        return new org.opennms.features.topology.api.topo.StatusProvider() {

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
            public Map<? extends VertexRef, ? extends Status> getStatusForVertices(BackendGraph graph, Collection<VertexRef> vertices, Criteria[] criteria) {
                final Map<VertexRef, Status> statusMap = new HashMap<>();
                vertices.forEach(vertexRef -> {
                    final Vertex vertex = graph.getVertex(vertexRef, criteria);
                    if (vertex instanceof LegacyVertex) {
                        final LegacyVertex legacyVertex = (LegacyVertex) vertex;
                        final ImmutableVertex apiVertex = ImmutableVertex
                                .newBuilder(legacyVertex.getNamespace(), legacyVertex.getId())
                                .properties(legacyVertex.getProperties())
                                .build();
                        final StatusInfo apiStatus = extension.calculateStatus(apiVertex);
                        final Status status = convert(apiStatus);
                        statusMap.put(vertexRef, status);
                    } else {
                        statusMap.put(vertexRef, new DefaultStatus(OnmsSeverity.INDETERMINATE.getLabel(), 0));
                    }
                });
                return statusMap;
            }
        };
    }
}
