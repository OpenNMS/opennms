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

import org.opennms.core.utils.InetAddressUtils;

import java.net.InetAddress;

public class BridgeMacTopologyLink {

    private final int m_id;
    private final Integer m_nodeId;
    private final Integer m_bridgePort;
    private final Integer m_bridgePortIfIndex;
    private final Integer m_bridgePortIfName;
    private final Integer m_vlan;
    private final String m_macAddr;
    private final InetAddress m_netaddress;
    private final InetAddress m_ipAddr;
    private final Integer m_targetNodeId;
    private final String m_nodeLabel;
    private final Integer m_sourceIfIndex;

    public BridgeMacTopologyLink(Integer id, Integer nodeId, Integer bridgePort, Integer bridgePortIfIndex,
                                 Integer bridgePortIfName, Integer vlan, String macAddr, String netaddress,
                                 String ipAddr, Integer targetNodeId, String nodeLabel, Integer sourceIfIndex) {
        m_id = id;
        m_nodeId = nodeId;
        m_bridgePort = bridgePort;
        m_bridgePortIfIndex = bridgePortIfIndex;
        m_bridgePortIfName = bridgePortIfName;
        m_vlan = vlan;
        m_macAddr = macAddr;
        m_netaddress = InetAddressUtils.getInetAddress(netaddress);
        m_ipAddr = InetAddressUtils.getInetAddress(ipAddr);
        m_targetNodeId = targetNodeId;
        m_nodeLabel = nodeLabel;
        m_sourceIfIndex = sourceIfIndex;
    }

    public int getId() {
        return m_id;
    }

    public Integer getNodeId() {
        return m_nodeId;
    }

    public Integer getBridgePort() {
        return m_bridgePort;
    }

    public Integer getBridgePortIfIndex() {
        return m_bridgePortIfIndex;
    }

    public Integer getBridgePortIfName() {
        return m_bridgePortIfName;
    }

    public Integer getVlan() {
        return m_vlan;
    }

    public String getMacAddr() {
        return m_macAddr;
    }

    public InetAddress getNetaddress() {
        return m_netaddress;
    }

    public InetAddress getIpAddr() {
        return m_ipAddr;
    }

    public Integer getTargetNodeId() {
        return m_targetNodeId;
    }

    public String getNodeLabel() {
        return m_nodeLabel;
    }

    public Integer getSourceIfIndex(){
        return m_sourceIfIndex;
    }
}
