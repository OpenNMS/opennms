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

package org.opennms.features.topology.plugins.topo.vmware.internal;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.SimpleSearchProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;

public class VmwareTopologySearchProvider extends SimpleSearchProvider {

    private VmwareTopologyProvider vmwareTopologyProvider;

    public VmwareTopologySearchProvider(VmwareTopologyProvider vmwareTopologyProvider) {
        this.vmwareTopologyProvider = Objects.requireNonNull(vmwareTopologyProvider);
    }

    @Override
    public String getSearchProviderNamespace() {
        return VmwareTopologyProvider.TOPOLOGY_NAMESPACE_VMWARE;
    }

    @Override
    public List<? extends VertexRef> queryVertices(SearchQuery searchQuery, GraphContainer container) {
        final List<Vertex> vertices = vmwareTopologyProvider.getVertices();
        return vertices.stream().filter(v -> searchQuery.matches(v.getLabel())).collect(Collectors.toList());
    }
}
