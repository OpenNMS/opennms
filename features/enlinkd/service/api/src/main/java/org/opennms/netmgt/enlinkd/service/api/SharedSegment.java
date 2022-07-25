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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.enlinkd.model.BridgeBridgeLink;
import org.opennms.netmgt.enlinkd.model.BridgeMacLink;
import org.opennms.netmgt.model.OnmsNode;

public class SharedSegment implements Topology{

    private Integer m_designatedBridgeId;
    private final Set<String> m_macsOnSegment = new HashSet<>();
    private final Set<BridgePort> m_portsOnSegment = new HashSet<>();
    private Date m_createTime;
    private Date m_lastPollTime;

    public Date getCreateTime() {
        return m_createTime;
    }

    public void setCreateTime(Date createTime) {
        m_createTime = createTime;
    }

    public Date getLastPollTime() {
        return m_lastPollTime;
    }

    public void setLastPollTime(Date lastPollTime) {
        m_lastPollTime = lastPollTime;
    }

    public void setDesignatedBridge(Integer designatedBridge) {
        m_designatedBridgeId = designatedBridge;
    }

    public Integer getDesignatedBridge() {
        return m_designatedBridgeId;
    }

    public BridgePort getDesignatedPort() {
        if (m_designatedBridgeId == null) {
            return null;
        }
       return getBridgePort(m_designatedBridgeId);
    }

    public boolean isEmpty() {
        return m_portsOnSegment.isEmpty();
    }

    public Set<BridgePort> getBridgePortsOnSegment() {
        return m_portsOnSegment;
    }        
    
    public boolean noMacsOnSegment() {
        return m_macsOnSegment.isEmpty();
    }

    public Set<Integer> getBridgeIdsOnSegment() {
        Set<Integer> nodes = new HashSet<>();
        for (BridgePort link : m_portsOnSegment) {
            if (link == null || link.getNodeId() == null)
                continue;
            nodes.add(link.getNodeId());
        }
        return nodes;
    }

    public Set<String> getMacsOnSegment() {
        return m_macsOnSegment;
    }

    public boolean containsMac(String mac) {
        return m_macsOnSegment.contains(mac);
    }

    public BridgePort getBridgePort(Integer nodeid) {
        if (nodeid == null)
            return null;
        for (BridgePort port: m_portsOnSegment) {
            if ( port.getNodeId().intValue() == nodeid.intValue()) {
                return port;
            }
        }
        return null;        
    }

    public List<BridgeMacLink> getBridgeMacLinks() {
        final List<BridgeMacLink> links = new ArrayList<>();
        if (m_designatedBridgeId == null) {
            return links;
        }
        BridgePort bridgePort=getDesignatedPort();
        m_macsOnSegment.forEach(mac -> {
            BridgeMacLink maclink = new BridgeMacLink();
            OnmsNode node = new OnmsNode();
            node.setId(bridgePort.getNodeId());
            maclink.setNode(node);
            maclink.setBridgePort(bridgePort.getBridgePort());
            maclink.setBridgePortIfIndex(bridgePort.getBridgePortIfIndex());
            maclink.setMacAddress(mac);
            maclink.setVlan(bridgePort.getVlan());
            maclink.setLinkType(BridgeMacLink.BridgeMacLinkType.BRIDGE_LINK);
            links.add(maclink);
        });
        return links;
    }

    public List<BridgeBridgeLink> getBridgeBridgeLinks() {
        List<BridgeBridgeLink> links = new ArrayList<>();
        if (m_designatedBridgeId == null) {
            return links;
        }
        BridgePort designatedPort = getDesignatedPort();
        OnmsNode designatedNode = new OnmsNode();
        designatedNode.setId(designatedPort.getNodeId());
        for (BridgePort port : m_portsOnSegment) {
            if (port.equals(designatedPort)) {
                continue;
            }
            BridgeBridgeLink link = new BridgeBridgeLink();
            OnmsNode node = new OnmsNode();
            node.setId(port.getNodeId());
            link.setNode(node);
            link.setBridgePort(port.getBridgePort());
            link.setBridgePortIfIndex(port.getBridgePortIfIndex());
            link.setVlan(port.getVlan());
            link.setDesignatedNode(designatedNode);
            link.setDesignatedPort(getDesignatedPort().getBridgePort());
            link.setDesignatedPortIfIndex(designatedPort.getBridgePortIfIndex());
            link.setDesignatedVlan(designatedPort.getVlan());
            links.add(link);
        }
        return links;
    }

    public boolean containsPort(BridgePort port) {
        return m_portsOnSegment.contains(port);
    }
    
    public String printTopology() {
        StringBuilder strbfr = new StringBuilder();
        strbfr.append("segment -> designated bridge:[");
        strbfr.append(getDesignatedBridge());
        strbfr.append("]\n");
        for (BridgePort blink:  m_portsOnSegment) {
            strbfr.append("        -> port:");            
            if (blink == null) {
                strbfr.append("[null]");
            } else {
                strbfr.append(blink.printTopology());
            }
            strbfr.append("\n");
        }
        strbfr.append("        -> macs:");
        strbfr.append(getMacsOnSegment());
        return strbfr.toString();       
    }

}
