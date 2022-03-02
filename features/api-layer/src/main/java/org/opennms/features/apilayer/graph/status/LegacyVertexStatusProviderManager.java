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

import static org.opennms.features.apilayer.graph.status.LegacyEdgeStatusProviderManager.convert;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.features.apilayer.utils.InterfaceMapper;
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
