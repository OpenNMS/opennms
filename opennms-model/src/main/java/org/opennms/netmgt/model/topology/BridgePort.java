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

import java.util.Date;

import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.BridgeMacLink.BridgeDot1qTpFdbStatus;

public class BridgePort {

    private OnmsNode m_node;
    private Integer m_bridgePort;
    private Integer m_bridgePortIfIndex;
    private String  m_bridgePortIfName;
    private Integer m_vlan;
    private Date m_createTime;
    private Date m_pollTime;

    public static BridgeMacLink getBridgeMacLink(BridgePort bp, String mac) {
        BridgeMacLink maclink = new BridgeMacLink();
        maclink.setNode(bp.getNode());
        maclink.setBridgePort(bp.getBridgePort());
        maclink.setBridgePortIfIndex(bp.getBridgePortIfIndex());
        maclink.setBridgePortIfName(bp.getBridgePortIfName());
        maclink.setMacAddress(mac);
        maclink.setBridgeDot1qTpFdbStatus(BridgeDot1qTpFdbStatus.DOT1D_TP_FDB_STATUS_LEARNED);
        maclink.setVlan(bp.getVlan());
        maclink.setBridgeMacLinkCreateTime(bp.getCreateTime());
        maclink.setBridgeMacLinkLastPollTime(bp.getPollTime());
        return maclink;
    }

    public static BridgePort getBridgeFromBridgeMacLink(BridgeMacLink link) {
        BridgePort bp = new BridgePort();
        bp.setNode(link.getNode());
        bp.setBridgePort(link.getBridgePort());
        bp.setBridgePortIfIndex(link.getBridgePortIfIndex());
        bp.setBridgePortIfName(link.getBridgePortIfName());
        bp.setVlan(link.getVlan());
        bp.setCreateTime(link.getBridgeMacLinkCreateTime());
        bp.setPollTime(link.getBridgeMacLinkLastPollTime());
        return bp;
    }

    public static BridgePort getFromBridgeBridgeLink(BridgeBridgeLink link) {
        BridgePort bp = new BridgePort();
        bp.setNode(link.getNode());
        bp.setBridgePort(link.getBridgePort());
        bp.setBridgePortIfIndex(link.getBridgePortIfIndex());
        bp.setBridgePortIfName(link.getBridgePortIfName());
        bp.setVlan(link.getVlan());
        bp.setCreateTime(link.getBridgeBridgeLinkCreateTime());
        bp.setPollTime(link.getBridgeBridgeLinkLastPollTime());
        return bp;
    }

    public static BridgePort getFromDesignatedBridgeBridgeLink(BridgeBridgeLink link) {
        BridgePort bp = new BridgePort();
        bp.setNode(link.getDesignatedNode());
        bp.setBridgePort(link.getDesignatedPort());
        bp.setBridgePortIfIndex(link.getDesignatedPortIfIndex());
        bp.setBridgePortIfName(link.getDesignatedPortIfName());
        bp.setVlan(link.getDesignatedVlan());
        bp.setCreateTime(link.getBridgeBridgeLinkCreateTime());
        bp.setPollTime(link.getBridgeBridgeLinkLastPollTime());
        return bp;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((m_bridgePort == null) ? 0 : m_bridgePort.hashCode());
        result = prime * result + ((m_node == null) ? 0 : m_node.getId().hashCode());
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
        } else if (!m_node.getId().equals(other.m_node.getId()))
            return false;
        return true;
    }

    public OnmsNode getNode() {
        return m_node;
    }
    public void setNode(OnmsNode node) {
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
    public String getBridgePortIfName() {
        return m_bridgePortIfName;
    }
    public void setBridgePortIfName(String bridgePortIfName) {
        m_bridgePortIfName = bridgePortIfName;
    }
    public Integer getVlan() {
        return m_vlan;
    }
    public void setVlan(Integer vlan) {
        m_vlan = vlan;
    }
    
    public Date getCreateTime() {
        return m_createTime;
    }
    public void setCreateTime(Date time) {
        m_createTime = time;
    }
    public Date getPollTime() {
        return m_pollTime;
    }
    public void setPollTime(Date time) {
        m_pollTime = time;
    }
    
    public String printTopology() {
        final StringBuilder strbfr = new StringBuilder();

        strbfr.append("bridge port:[nodeid:["); 
        strbfr.append(getNode().getId());
        strbfr.append("], bridgeport:");
        strbfr.append(getBridgePort());
        strbfr.append("]\n");

        return strbfr.toString();
        }

}
