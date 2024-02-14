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
package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.util.EnumSet;
import java.util.Set;

import org.opennms.features.topology.api.topo.simple.SimpleLeafVertex;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;

public class LinkdVertex extends SimpleLeafVertex {

    public static LinkdVertex create(OnmsTopologyVertex tvertex, String namespace) {
        LinkdVertex vertex = new LinkdVertex(tvertex.getId(), namespace);
        vertex.setNodeID(tvertex.getNodeid());
        vertex.setLabel(tvertex.getLabel());
        vertex.setIpAddress(tvertex.getAddress());
        vertex.setIconKey(tvertex.getIconKey());
        vertex.setTooltipText(tvertex.getToolTipText());
        return vertex;
    }

    private final Set<ProtocolSupported> m_protocolSupported = EnumSet.noneOf(ProtocolSupported.class);

    public LinkdVertex(String id, String namespace) {
        super(namespace, id, 0, 0);
    }
    
    
    @Override
    public String getTooltipText() {
        StringBuilder tooltipText = new StringBuilder();
        tooltipText.append("<p>");
        tooltipText.append(super.getTooltipText());
        tooltipText.append("</p>");
        if (m_protocolSupported.size() > 0) {
            tooltipText.append("<p>");
            tooltipText.append(m_protocolSupported);
            tooltipText.append("</p>");
        }
        return tooltipText.toString();
    }

    public Set<ProtocolSupported> getProtocolSupported() {
        return m_protocolSupported;
    }

}
