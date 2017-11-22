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

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.OnmsNode;

public class SharedSegment implements BridgeTopology{
    
    public static List<BridgeBridgeLink> getBridgeBridgeLinks(SharedSegment segment) throws BridgeTopologyException {
        BridgePort designatedPort = segment.getDesignatedPort();
        OnmsNode designatedNode = new OnmsNode();
        designatedNode.setId(designatedPort.getNodeId());
        List<BridgeBridgeLink> links = new ArrayList<BridgeBridgeLink>();
        for (BridgePort port:segment.getBridgePortsOnSegment()) {
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
            link.setDesignatedPort(designatedPort.getBridgePort());
            link.setDesignatedPortIfIndex(designatedPort.getBridgePortIfIndex());
            link.setDesignatedVlan(designatedPort.getVlan());
            links.add(link);
        }
        return links;

    }

    public static List<BridgeMacLink> getBridgeMacLinks(SharedSegment segment) throws BridgeTopologyException {
        List<BridgeMacLink> maclinks = new ArrayList<BridgeMacLink>();
        for (String mac: segment.getMacsOnSegment()) {
                for (BridgePort bp: segment.getBridgePortsOnSegment()) {
                    if (bp == null) {
                        throw new BridgeTopologyException("BridgePort on segment should not be null", segment);
                    }
                    maclinks.add(BridgeForwardingTableEntry.getBridgeMacLinkFromBridgePort(bp, mac));
                }
        }
        return maclinks;
    }

    public static SharedSegment createFrom(BridgeMacLink link) throws BridgeTopologyException {
        SharedSegment segment = new SharedSegment();
        segment.getBridgePortsOnSegment().add(BridgePort.getFromBridgeMacLink(link));
        segment.getMacsOnSegment().add(link.getMacAddress());
        segment.setDesignatedBridge(link.getNode().getId());
        segment.setCreateTime(link.getBridgeMacLinkCreateTime());
        segment.setPollTime(link.getBridgeMacLinkLastPollTime());
        
        return segment;
    }

    public static SharedSegment createFrom(BridgeBridgeLink link) throws BridgeTopologyException {
        SharedSegment segment = new SharedSegment();
        segment.getBridgePortsOnSegment().add(BridgePort.getFromBridgeBridgeLink(link));
        segment.getBridgePortsOnSegment().add(BridgePort.getFromDesignatedBridgeBridgeLink(link));
        segment.setDesignatedBridge(link.getDesignatedNode().getId());
        segment.setCreateTime(link.getBridgeBridgeLinkCreateTime());
        segment.setPollTime(link.getBridgeBridgeLinkLastPollTime());

        return segment;
    }

    public static SharedSegment createAndAddToBroadcastDomain(BroadcastDomain domain, BridgePort port, Set<String> macs) 
            throws BridgeTopologyException {
        SharedSegment segment = new SharedSegment();
        segment.getBridgePortsOnSegment().add(port);
        segment.getMacsOnSegment().addAll(macs);
        segment.setDesignatedBridge(port.getNodeId());
        domain.getSharedSegments().add(segment);
        return segment;
    }
    
    public static SharedSegment createAndAddToBroadcastDomain(BroadcastDomain domain, 
            Set<BridgePort> ports, Set<String> macs, 
            Integer designatedBridge) throws BridgeTopologyException {
        SharedSegment segment = new SharedSegment();
        segment.getBridgePortsOnSegment().addAll(ports);
        segment.getMacsOnSegment().addAll(macs);
        segment.setDesignatedBridge(designatedBridge);
        domain.getSharedSegments().add(segment);
        return segment;
    }
    
    public static SharedSegment create() {
        return new SharedSegment();
                
    }
    
    Integer m_designatedBridgeId;
    Set<String> m_macsOnSegment = new HashSet<String>();
    Set<BridgePort> m_portsOnSegment = new HashSet<BridgePort>();
    Date m_createTime;
    Date m_pollTime;
    
    public Date getCreateTime() {
        return m_createTime;
    }

    public void setCreateTime(Date createTime) {
        m_createTime = createTime;
    }

    public Date getPollTime() {
        return m_pollTime;
    }

    public void setPollTime(Date pollTime) {
        m_pollTime = pollTime;
    }

    private SharedSegment() {
        
    }
                    
    public boolean setDesignatedBridge(Integer designatedBridge) throws BridgeTopologyException {
        if (designatedBridge == null) { 
            throw new BridgeTopologyException("Designated Bridge NodeId cannot be null", this);
        }
        m_designatedBridgeId = designatedBridge;
            return true;
    }

    public Integer getDesignatedBridge() {
        return m_designatedBridgeId;
    }

    public BridgePort getDesignatedPort() throws BridgeTopologyException {
        if (m_designatedBridgeId == null) {
            throw new BridgeTopologyException("Designated Bridge NodeId cannot be null", this);
        }
        BridgePort designatedbridge= getBridgePort(m_designatedBridgeId);
        if (designatedbridge == null) {
            throw new BridgeTopologyException("Designated BridgePort cannot be null", this);
        }
        return designatedbridge;
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
        Set<Integer> nodes = new HashSet<Integer>();
        for (BridgePort link : m_portsOnSegment) {
            if (link == null || link.getNodeId() == null)
                continue;
            nodes.add(link.getNodeId());
        }
        return nodes;
    }

    public void retain(Set<String> macs, BridgePort dlink) {
        m_portsOnSegment.add(dlink);
        m_macsOnSegment.retainAll(macs);
    }
    
    public void assign(Set<String> macs, BridgePort dlink) {
        m_portsOnSegment.add(dlink);
        m_macsOnSegment = macs;
    }

    //FIXME what happens when i remove designated bridge?
    //FIXME and also if the shared is empty
    public boolean removeBridge(Integer bridgeId) throws BridgeTopologyException {
        if (bridgeId == null ) {
            return false;
        }
        if (m_portsOnSegment.isEmpty()) {
            return false;
        }
        BridgePort bridgetoremove = getBridgePort(bridgeId);
        if ( bridgetoremove == null ) {
            return false;
        }
        if (m_designatedBridgeId != null && m_designatedBridgeId.intValue() == bridgeId.intValue()) {
            m_designatedBridgeId = null;
        }
        return m_portsOnSegment.remove(bridgetoremove);
    }
        
    public Set<String> getMacsOnSegment() {
        return m_macsOnSegment;
    }

    public boolean containsMac(String mac) {
        return m_macsOnSegment.contains(mac);
    }

    public BridgePort getBridgePort(Integer nodeid) throws BridgeTopologyException {
        if (nodeid == null)
            return null;
        for (BridgePort port: m_portsOnSegment) {
            if (port == null) {
                throw new BridgeTopologyException("Shared Segment: BridgePort cannot be null.", this);
            }
            if (port.getNodeId() == null) {
                throw new BridgeTopologyException("Shared Segment: BridgePort nodeid cannot be null.", this);
            }
            if (port.getBridgePort() == null) {
                throw new BridgeTopologyException("Shared Segment: BridgePort bridgeport cannot be null.", this);
            }
            if ( port.getNodeId().intValue() == nodeid.intValue()) {
                return port;
            }
        }
        return null;        
    }
    
    public boolean containsPort(BridgePort port) {
        return m_portsOnSegment.contains(port);
    }
    
    public String printTopology() {
    	StringBuffer strbfr = new StringBuffer();
        strbfr.append("segment -> designated bridge:[");
        strbfr.append(getDesignatedBridge());
        strbfr.append("]\n");
        for (BridgePort blink:  m_portsOnSegment) {
            if (blink == null) {
                strbfr.append("       -> port:[null]");
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
