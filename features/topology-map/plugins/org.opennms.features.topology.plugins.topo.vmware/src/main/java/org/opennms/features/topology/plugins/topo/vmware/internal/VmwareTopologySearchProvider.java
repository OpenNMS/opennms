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
package org.opennms.features.topology.plugins.topo.vmware.internal;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.simple.SimpleSearchProvider;
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
        final List<Vertex> vertices = vmwareTopologyProvider.getCurrentGraph().getVertices();
        return vertices.stream().filter(v -> searchQuery.matches(v.getLabel())).collect(Collectors.toList());
    }
}
