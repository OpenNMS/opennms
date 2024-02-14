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
package org.opennms.netmgt.graph.provider.legacy;

import org.opennms.features.topology.api.topo.Edge;
import org.opennms.netmgt.graph.api.VertexRef;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.domain.AbstractDomainEdge;

public class LegacyEdge extends AbstractDomainEdge {
    public LegacyEdge(GenericEdge genericEdge) {
        super(genericEdge);
    }

    public LegacyEdge(Edge legacyEdge) {
        super(GenericEdge.builder()
                .id(legacyEdge.getId())
                .label(legacyEdge.getLabel())
                .namespace(legacyEdge.getNamespace())
                .source(new VertexRef(legacyEdge.getSource().getNamespace(), legacyEdge.getSource().getVertex().getId()))
                .target(new VertexRef(legacyEdge.getTarget().getNamespace(), legacyEdge.getTarget().getVertex().getId()))
                .build());
    }
}
