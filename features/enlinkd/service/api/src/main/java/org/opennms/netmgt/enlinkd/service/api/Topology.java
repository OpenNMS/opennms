/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

import java.net.InetAddress;
import java.util.List;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.enlinkd.model.CdpLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.model.IsIsLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.LldpLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.model.OspfLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.SnmpInterfaceTopologyEntity;
import org.opennms.netmgt.model.OnmsNode;

public interface Topology {

    String printTopology();
    
    public static String getPortTextString(SnmpInterfaceTopologyEntity snmpiface) {
        final StringBuilder port = new StringBuilder();
        port.append(snmpiface.getIfName());
        if (!"".equals(snmpiface.getIfAlias()) ) {
            port.append("(");
            port.append(snmpiface.getIfAlias());
            port.append(")");
        } 
        if ( snmpiface.getIfSpeed() > 0) {
            port.append("(");
            port.append(InetAddressUtils.getHumanReadableIfSpeed(snmpiface.getIfSpeed()));
            port.append(")");
        }
        port.append("(ifIndex:");
        port.append(snmpiface.getIfIndex());
        port.append(")");
       
        return port.toString();
    }

    public static String getPortTextString(String label, Integer ifindex, String address, SnmpInterfaceTopologyEntity snmpiface) {
        if (snmpiface == null) {
            return getPortTextString(label, ifindex, address);
        }
        final StringBuilder tooltipText = new StringBuilder();
        tooltipText.append(label);
        tooltipText.append(" ");
        tooltipText.append(getPortTextString(snmpiface));
        if (address != null ) {
            tooltipText.append("(");
            tooltipText.append(address);
            tooltipText.append(")");
        }
        return tooltipText.toString();        
    }
    
    public static String getPortTextString(String label, Integer ifindex, String address ) {
        final StringBuilder tooltipText = new StringBuilder();
        tooltipText.append(label);
        tooltipText.append(" ");
        if (ifindex  != null ) {
            tooltipText.append("(ifIndex:");
            tooltipText.append(ifindex);
            tooltipText.append(")");
        }
        if (address != null ) {
            tooltipText.append("(");
            tooltipText.append(address);
            tooltipText.append(")");
        }
        return tooltipText.toString();
    }

    public static String getSharedSegmentId(BridgePort bp) {
        StringBuffer id = new StringBuffer();
        id.append("s:");
        id.append(Topology.getId(bp));
        return id.toString();
    }

    public static String getSharedSegmentId(TopologyShared designated) {
        StringBuffer id = new StringBuffer();
        id.append("s:");
        id.append(Topology.getId(designated));
        return id.toString();
    }

    public static String getMacsCloudId(TopologyShared designated) {
        StringBuffer id = new StringBuffer();
        id.append("m:");
        id.append(Topology.getId(designated));
        return id.toString();
    }
    
    public static String getMacsIpLabel() {
        return "Macs/ip addresses on Segment without node";
    }

    public static String getSharedSegmentLabel() {
        return "Segment";
    }
    
    public static String getAddress(InetAddress address) {
        return InetAddressUtils.str(address);
    }
    
    public static String getAddress(IpInterfaceTopologyEntity ip) {
        if (ip == null) {
            return null;
        }
        return getAddress(ip.getIpAddress());
    }
    public static String getAddress(MacPort port ) {
        return port.getPortMacInfo();
    }
    public static String getAddress(MacCloud cloud ) {
        return cloud.getMacsInfo();
    }
    
    public static String getAddress(BridgePort bp) {
        return String.format("bridge port %d vlan %d",bp.getBridgePort(),bp.getVlan());
    }
    
    public static String getAddress(CdpLinkTopologyEntity cdplink) {
        return cdplink.getCdpCacheAddress();
    }

    public static String getRemoteAddress(LldpLinkTopologyEntity lldplink) {
        return String.format("%s type %s", lldplink.getLldpRemPortId(),lldplink.getLldpRemPortIdSubType().name());
    }

    public static String getRemoteAddress(OspfLinkTopologyEntity ospflink) {
        return InetAddressUtils.str(ospflink.getOspfRemIpAddr());
    }

    public static String getAddress(OspfLinkTopologyEntity ospflink) {
        return String.format("%s mask %s", InetAddressUtils.str(ospflink.getOspfIpAddr()), InetAddressUtils.str(ospflink.getOspfIpMask()));
    }

    public static String getRemoteAddress(IsIsLinkTopologyEntity isislink) {
        return isislink.getIsisISAdjNeighSNPAAddress();
    }

    public static String getAddress(MacCloud cloud, List<MacPort> ports) {
        StringBuffer ip = new StringBuffer();
        if (cloud!= null) {
            ip.append(cloud.getMacsInfo());
        }
        for (MacPort port :ports) {
            ip.append(port.getPortMacInfo());
        }
        return ip.toString();
    }

    public static String getMacsIpTextString(MacCloud cloud, List<MacPort> ports) {
        final StringBuilder tooltipText = new StringBuilder();
        tooltipText.append("shared addresses: ");
        tooltipText.append("(");
        tooltipText.append(getAddress(cloud, ports));
        tooltipText.append(")");
        tooltipText.append("(");
        tooltipText.append(getNodeStatus(OnmsNode.NodeType.UNKNOWN));
        tooltipText.append("/");
        tooltipText.append("Not an OpenNMS Node");
        tooltipText.append(")");        
        return tooltipText.toString();
    
    }

    public static String getMacsCloudIpTextString(TopologyShared shared, List<MacPort> ports) {
        final StringBuilder tooltipText = new StringBuilder();
        tooltipText.append("shared addresses: ");
        tooltipText.append("(");
        tooltipText.append(getAddress(shared.getCloud(), ports));
        tooltipText.append(")");
        tooltipText.append("(");
        tooltipText.append(getNodeStatus(OnmsNode.NodeType.UNKNOWN));
        tooltipText.append("/");
        tooltipText.append("Not an OpenNMS Node");
        tooltipText.append(")");        
        return tooltipText.toString();
    
    }

    public static String getNodeTextString(NodeTopologyEntity node, IpInterfaceTopologyEntity primary) {
        final StringBuilder tooltipText = new StringBuilder();
        tooltipText.append(node.getLabel());
        tooltipText.append(": ");
        if (primary != null) {
            tooltipText.append("(");
            tooltipText.append(InetAddressUtils.str(primary.getIpAddress()));
            tooltipText.append(")");
        }
        tooltipText.append("(");
        tooltipText.append(getNodeStatus(node.getType()));
        if (primary != null) {
            tooltipText.append("/");
           tooltipText.append(getIsManaged(primary.isManaged()));
        }
        tooltipText.append(")");
        
        if (node.getLocation() != null && node.getLocation().trim().length() > 0) {
                tooltipText.append(node.getLocation());
        }
        
        return tooltipText.toString();
    
    }

    public static String getSharedSegmentTextString(BridgePort bp) {
        return String.format("'Shared Segment': %s" ,
                bp.printTopology());
    }

    public static String getSharedSegmentTextString(TopologyShared segment) {
        return String.format("'Shared Segment': %s" ,
                segment.getUpPort().printTopology());
    }

    public static String getIsManaged(boolean isManaged) {
        if (isManaged) {
            return "Managed";
        }
        return "Unmanaged";

    }

    public static String getDefaultIconKey() {
        return "linkd.system";        
    }

    public static String getCloudIconKey() {
        return "cloud";
    }
    public static String getIconKey(NodeTopologyEntity node) {
        if (node.getSysObjectId() == null) {
            return "linkd.system";
        }
        if (node.getSysObjectId().startsWith(".")) {
            return "linkd.system.snmp" +node.getSysObjectId();
        }
        return "linkd.system.snmp." + node.getSysObjectId();
        
    }
    public static String getNodeStatus(OnmsNode.NodeType nodeType) {
        if (nodeType == null) {
            return "undefined";
        }
            
        String status;
        switch (nodeType) {
            case ACTIVE:
                status = "Active";
                break;
            case UNKNOWN:
                status = "Unknown";
                break;
            case DELETED:
                status = "Deleted";
                break;
            default:
                status = "undefined";
                break;
        }
        return status;
    }

    public static String getDefaultEdgeId(int sourceId,int targetId) {
        return Math.min(sourceId, targetId) + "|" + Math.max(sourceId, targetId);
    }  
    public static String getId(TopologyShared segment) {
        return  getId(segment.getUpPort());
    }
    
    public static String getId(MacCloud macCloud) {
        return macCloud.getMacs().toString();
    }
    public static String getId(MacPort macPort) {
        if (macPort.getNodeId() == null) {
            return macPort.getMacPortMap().keySet().toString();
        }
        return Integer.toString(macPort.getNodeId());
    }
    public static String getId(BridgePort bp) {
        return bp.getNodeId()+":"+bp.getBridgePort();
    }
    public static String getEdgeId(BridgePort bp, MacPort macport ) {
            return getId(bp)+"|"+getId(macport);
    }
    public static String getEdgeId(BridgePort bp, MacCloud macport ) {
        return getId(bp)+"|"+getId(macport);
    }
    public static String getEdgeId(BridgePort sourcebp, BridgePort targetbp ) {
        if (sourcebp.getNodeId().intValue() < targetbp.getNodeId().intValue()) {
            return getId(sourcebp)+"|"+getId(targetbp);
        }
        return getId(targetbp)+"|"+getId(sourcebp);
    }
    public static String getEdgeId(String id, MacPort macport) {
        return id + "|" + getId(macport);
    }
    public static String getEdgeId(String id, BridgePort bp) {
        return id + "|" + bp.getNodeId() + ":" + bp.getBridgePort();
    }
    static String getDefaultEdgeId(String id1, String id2) {
        return id1+"|"+id2;
    }

    static String getPortId(String id) {
        return "p:"+id;
    }
}
