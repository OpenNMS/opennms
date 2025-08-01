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

import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.netmgt.graph.api.generic.GenericVertex;
import org.opennms.netmgt.graph.domain.AbstractDomainVertex;

public class LegacyVertex extends AbstractDomainVertex {

    public LegacyVertex(GenericVertex genericVertex) {
        super(genericVertex);
    }

    public LegacyVertex(Vertex legacyVertex) {
        super(GenericVertex.builder()
                .id(legacyVertex.getId())
                .label(legacyVertex.getLabel())
                .namespace(legacyVertex.getNamespace())
                .property("nodeID", legacyVertex.getNodeID())
                .property("iconKey", legacyVertex.getIconKey())
                .property("ipAddress", legacyVertex.getIpAddress())
                .property("styleName", legacyVertex.getStyleName())
                .property("tooltipText", legacyVertex.getTooltipText())
                .property("x", legacyVertex.getX())
                .property("y", legacyVertex.getY())
                .build());
    }
}
