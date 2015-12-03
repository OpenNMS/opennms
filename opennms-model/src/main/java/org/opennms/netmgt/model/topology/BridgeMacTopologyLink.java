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

import org.opennms.netmgt.model.OnmsNode.NodeType;


public class BridgeMacTopologyLink extends TopologyLink {

    private final int m_id;
    private final Integer m_bridgePort;
    private final Integer m_bridgePortIfIndex;
    private final String m_bridgePortIfName;
    private final Integer m_vlan;
    private final Integer m_targetBridgePort;
    private final Integer m_targetIfIndex;
    private final String m_targetPortIfName;
    private final int m_targetid;

    private final String m_macAddr;

    public BridgeMacTopologyLink(Integer id, 
            Integer nodeId, 
            String srcLabel, String srcSysoid, String srcLocation,
            NodeType srcNodeType,
            Integer bridgePort, Integer bridgePortIfIndex,
            String bridgePortIfName, 
            Integer vlan, 
            Integer targetNodeId, 
            String targetLabel, String targetSysoid, String targetLocation,
            NodeType targetNodeType,
            String macAddr,
            Integer targetIfIndex, String targetPortIfName, 
            Integer targetBridgePort, Integer targetid, Date lastPollTime) {
        super(lastPollTime,nodeId,srcLabel,srcSysoid,srcLocation,srcNodeType,targetNodeId,targetLabel,targetSysoid,targetLocation,targetNodeType);
        m_id = id;
        m_bridgePort = bridgePort;
        m_bridgePortIfIndex = bridgePortIfIndex;
        m_bridgePortIfName = bridgePortIfName;
        m_vlan = vlan;
        m_macAddr = macAddr;
        m_targetIfIndex = targetIfIndex;
        m_targetPortIfName = targetPortIfName;
        m_targetBridgePort = targetBridgePort;
        m_targetid = targetid;
    }

    public int getId() {
        return m_id;
    }

    public int getTargetId() {
        return m_targetid;
    }

    public Integer getBridgePort() {
        return m_bridgePort;
    }

    public Integer getBridgePortIfIndex() {
        return m_bridgePortIfIndex;
    }

    public String getBridgePortIfName() {
        return m_bridgePortIfName;
    }

    public Integer getVlan() {
        return m_vlan;
    }

    public String getMacAddr() {
        return m_macAddr;
    }
    
    public Integer getTargetIfIndex() {
        return m_targetIfIndex;
    }

    public String getTargetPortIfName() {
        return m_targetPortIfName;
    }

    public Integer getTargetBridgePort() {
        return m_targetBridgePort;
    }

}
