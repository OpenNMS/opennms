/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.enlinkd.service.api;

import java.util.Objects;

public class BridgePort implements Topology {

    private Integer m_node;
    private Integer m_bridgePort;
    private Integer m_bridgePortIfIndex;
    //FIXME a BridgePort is identified by nodeid and port
    //      the vlan is an attribute of the shared segment 
    //      and of the domain must be moved there
    private Integer m_vlan;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BridgePort that = (BridgePort) o;
        return Objects.equals(m_node, that.m_node) &&
                Objects.equals(m_bridgePort, that.m_bridgePort);
    }

    @Override
    public int hashCode() {
        return Objects.hash(m_node, m_bridgePort);
    }

    public Integer getNodeId() {
        return m_node;
    }

    public void setNodeId(Integer node) {
        m_node = node;
    }
    public Integer getBridgePort() {
        return m_bridgePort;
    }
    public void setBridgePort(Integer bridgePort) {
        m_bridgePort = bridgePort;
    }
    public Integer getBridgePortIfIndex() {
        return m_bridgePortIfIndex;
    }
    public void setBridgePortIfIndex(Integer bridgePortIfIndex) {
        m_bridgePortIfIndex = bridgePortIfIndex;
    }

    public Integer getVlan() {
        return m_vlan;
    }
    public void setVlan(Integer vlan) {
        m_vlan = vlan;
    }
        
    public String printTopology() {

            return "nodeid:[" +
                getNodeId() +
                "], bridgeport:[" +
                getBridgePort() +
                "], ifindex:[" +
                getBridgePortIfIndex() +
                "], vlan:[" +
                getVlan() +
                "]";
    }

}
