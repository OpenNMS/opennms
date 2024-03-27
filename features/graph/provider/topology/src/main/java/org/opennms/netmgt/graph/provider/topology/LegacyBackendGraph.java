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
package org.opennms.netmgt.graph.provider.topology;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.features.topology.api.support.hops.DefaultVertexHopCriteria;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.DefaultVertexRef;
import org.opennms.features.topology.api.topo.simple.SimpleGraph;
import org.opennms.netmgt.graph.api.generic.GenericGraph;

public class LegacyBackendGraph extends SimpleGraph {
    private final GenericGraph delegate;

    public LegacyBackendGraph(GenericGraph genericGraph) {
        super(Objects.requireNonNull(genericGraph.getNamespace()));
        genericGraph.getVertices().forEach(genericVertex -> addVertices(new LegacyVertex(genericVertex)));
        genericGraph.getEdges().forEach(genericEdge -> addEdges(new LegacyEdge(genericEdge)));
        this.delegate = genericGraph;
    }

    public List<Criteria> getDefaultCriteria() {
        return delegate.getDefaultFocus().getVertexIds().stream()
                .map(vertexId -> new DefaultVertexHopCriteria(new DefaultVertexRef(getNamespace(), vertexId)))
                .collect(Collectors.toList());
    }

    @Override
    public LegacyVertex getVertex(String namespace, String id) {
        return (LegacyVertex) super.getVertex(namespace, id);
    }
}
