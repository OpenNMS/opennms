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

package org.opennms.netmgt.model.topology;

import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeMacLink;

public class BridgePort implements Topology {

    private Integer m_node;
    private Integer m_bridgePort;
    private Integer m_bridgePortIfIndex;
    //FIXME a BridgePort is identified by nodeid and port
    //      the vlan is an attribute of the shared segment 
    //      and of the domain must be moved there
    private Integer m_vlan;


    public static BridgePort getFromBridgeForwardingTableEntry(BridgeForwardingTableEntry link) {
        BridgePort bp = new BridgePort();
        bp.setNodeId(link.getNodeId());
        bp.setBridgePort(link.getBridgePort());
        bp.setBridgePortIfIndex(link.getBridgePortIfIndex());
        bp.setVlan(link.getVlan());
        return bp;
    }

    public static BridgePort getFromBridgeMacLink(
            BridgeMacLink link) {
        BridgePort bp = new BridgePort();
        bp.setNodeId(link.getNode().getId());
        bp.setBridgePort(link.getBridgePort());
        bp.setBridgePortIfIndex(link.getBridgePortIfIndex());
        bp.setVlan(link.getVlan());
        return bp;
    }

    public static BridgePort getFromBridgeBridgeLink(BridgeBridgeLink link) {
        BridgePort bp = new BridgePort();
        bp.setNodeId(link.getNode().getId());
        bp.setBridgePort(link.getBridgePort());
        bp.setBridgePortIfIndex(link.getBridgePortIfIndex());
        bp.setVlan(link.getVlan());
        return bp;
    }

    public static BridgePort getFromDesignatedBridgeBridgeLink(BridgeBridgeLink link) {
        BridgePort bp = new BridgePort();
        bp.setNodeId(link.getDesignatedNode().getId());
        bp.setBridgePort(link.getDesignatedPort());
        bp.setBridgePortIfIndex(link.getDesignatedPortIfIndex());
        bp.setVlan(link.getDesignatedVlan());
        return bp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((m_bridgePort == null) ? 0 : m_bridgePort.hashCode());
        result = prime * result + ((m_node == null) ? 0 : m_node.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        BridgePort other = (BridgePort) obj;
        if (m_bridgePort == null) {
            if (other.m_bridgePort != null)
                return false;
        } else if (!m_bridgePort.equals(other.m_bridgePort))
            return false;
        if (m_node == null) {
            if (other.m_node != null)
                return false;
        } else if (!m_node.equals(other.m_node))
            return false;
        return true;
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

        final StringBuffer strbfr = new StringBuffer();
        strbfr.append("nodeid:["); 
        strbfr.append(getNodeId());
        strbfr.append("], bridgeport:[");
        strbfr.append(getBridgePort());
        strbfr.append("], ifindex:[");
        strbfr.append(getBridgePortIfIndex());
        strbfr.append("], vlan:[");
        strbfr.append(getVlan());
        strbfr.append("]");

        return strbfr.toString();
    }

}
