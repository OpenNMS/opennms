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

import org.opennms.netmgt.topologies.service.api.OnmsTopologyPort;

public class LinkdPort {

    public static LinkdPort create(OnmsTopologyPort tport, LinkdVertex vertex) {
        LinkdPort port = new LinkdPort(vertex, tport.getIfindex());
        port.setToolTipText(tport.getToolTipText());
        return port;
    }
        
    private final LinkdVertex m_vertex;
    private final Integer m_ifindex;
    private String m_toolTipText;
    
    public LinkdPort(LinkdVertex vertex, Integer ifindex) {
        super();
        m_vertex = vertex;
        m_ifindex = ifindex;
    }
    
    public LinkdPort clone () {
        LinkdPort clone = new LinkdPort(this.getVertex(), this.getIfIndex());
        clone.setToolTipText(this.getToolTipText());
        return clone;
    }

    public LinkdVertex getVertex() {
        return m_vertex;
    }
    public Integer getIfIndex() {
        return m_ifindex;
    }

    public String getToolTipText() {
        return m_toolTipText;
    }

    public void setToolTipText(String port) {
        m_toolTipText = port;
    }

}
