/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.linkd.internal;

import org.opennms.netmgt.enlinkd.model.SnmpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.service.api.Topology;

public class LinkdPort {

    public static LinkdPort create(LinkdVertex vertex) {
        LinkdPort port = new LinkdPort(vertex, -1);
        port.setPort(vertex.getTooltipText());
        return port;
    }
    
    public static LinkdPort create(LinkdVertex vertex, Integer ifindex, String addr, SnmpInterfaceTopologyEntity iface) {
        if (ifindex == null ) {
            LinkdPort port = new LinkdPort(vertex, -1);
            port.setPort(Topology.getPortTextString(vertex.getLabel(), null, addr));
            return port;
        }
        LinkdPort port = new LinkdPort(vertex, iface.getIfIndex());
        port.setPort(Topology.getPortTextString(vertex.getLabel(), ifindex, addr,iface));
        return port;
        
    }
    
    private final LinkdVertex m_vertex;
    private final Integer m_ifindex;
    private String m_port;
    
    public LinkdPort(LinkdVertex vertex, Integer ifindex) {
        super();
        m_vertex = vertex;
        m_ifindex = ifindex;
    }
    
    public LinkdPort clone () {
        LinkdPort clone = new LinkdPort(this.getVertex(), this.getIfIndex());
        clone.setPort(this.getPort());
        return clone;
    }

    public LinkdVertex getVertex() {
        return m_vertex;
    }
    public Integer getIfIndex() {
        return m_ifindex;
    }

    public String getPort() {
        return m_port;
    }

    public void setPort(String port) {
        m_port = port;
    }

}
