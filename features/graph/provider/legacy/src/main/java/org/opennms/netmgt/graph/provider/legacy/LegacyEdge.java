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
import org.opennms.features.topology.api.topo.LinkDetailsAware;
import org.opennms.netmgt.graph.api.VertexRef;
import org.opennms.netmgt.graph.api.generic.GenericEdge;
import org.opennms.netmgt.graph.domain.AbstractDomainEdge;

public class LegacyEdge extends AbstractDomainEdge {
    public LegacyEdge(GenericEdge genericEdge) {
        super(genericEdge);
    }

    public LegacyEdge(Edge legacyEdge) {
        super(toGenericEdge(legacyEdge));
    }

    /** Provider-local: an ifIndex means nothing on a business-service edge. */
    public interface Properties {
        String SOURCE_IFINDEX = "sourceIfIndex";
        String TARGET_IFINDEX = "targetIfIndex";
        String DISCOVERY_PROTOCOL = "discoveryProtocol";
    }

    /**
     * Only the endpoint node refs used to survive here, dropping the interfaces
     * discovery had already resolved.
     */
    private static GenericEdge toGenericEdge(Edge legacyEdge) {
        final GenericEdge.GenericEdgeBuilder builder = GenericEdge.builder()
                .id(legacyEdge.getId())
                .label(legacyEdge.getLabel())
                .namespace(legacyEdge.getNamespace())
                .source(new VertexRef(legacyEdge.getSource().getNamespace(), legacyEdge.getSource().getVertex().getId()))
                .target(new VertexRef(legacyEdge.getTarget().getNamespace(), legacyEdge.getTarget().getVertex().getId()));
        if (legacyEdge instanceof LinkDetailsAware) {
            final LinkDetailsAware link = (LinkDetailsAware) legacyEdge;
            // Strings because the REST converters have no case for a boxed
            // Integer and fall back to toString() anyway, as nodeID already does.
            builder.property(Properties.SOURCE_IFINDEX, asString(link.getSourceIfIndex()));
            builder.property(Properties.TARGET_IFINDEX, asString(link.getTargetIfIndex()));
            // The builder drops nulls, so an unresolved end is absent.
            builder.property(Properties.DISCOVERY_PROTOCOL, link.getDiscoveryProtocol());
        }
        return builder.build();
    }

    private static String asString(final Integer ifIndex) {
        return ifIndex == null ? null : String.valueOf(ifIndex);
    }
}
