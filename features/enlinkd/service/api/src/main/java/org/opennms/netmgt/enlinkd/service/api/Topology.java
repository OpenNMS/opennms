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
    
    static String getPortTextString(SnmpInterfaceTopologyEntity snmpiface) {
        final StringBuilder port = new StringBuilder();
        port.append(snmpiface.getIfName());
        if (!"".equals(snmpiface.getIfAlias()) ) {
            port.append("(");
            port.append(snmpiface.getIfAlias());
            port.append(")");
        } 
        if ( snmpiface.getIfSpeed() != null && snmpiface.getIfSpeed() > 0) {
            port.append("(");
            port.append(InetAddressUtils.getHumanReadableIfSpeed(snmpiface.getIfSpeed()));
            port.append(")");
        }
        port.append("(ifIndex:");
        port.append(snmpiface.getIfIndex());
        port.append(")");
       
        return port.toString();
    }

    static String getPortTextString(String label, Integer ifindex, String address, SnmpInterfaceTopologyEntity snmpiface) {
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
    
    static String getPortTextString(String label, Integer ifindex, String address) {
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

    static String getSharedSegmentId(TopologyShared designated) {
        return "s:" +
                Topology.getId(designated);
    }

    static String getMacsCloudId(TopologyShared designated) {
        return "m:" +
                Topology.getId(designated);
    }
    
    static String getMacsIpLabel() {
        return "Macs/ip addresses on Segment without node";
    }

    static String getSharedSegmentLabel() {
        return "Segment";
    }
    
    static String getAddress(InetAddress address) {
        return InetAddressUtils.str(address);
    }
    
    static String getAddress(IpInterfaceTopologyEntity ip) {
        if (ip == null) {
            return null;
        }
        return getAddress(ip.getIpAddress());
    }
    static String getAddress(MacPort port) {
        return port.getPortMacInfo();
    }
    static String getAddress(MacCloud cloud) {
        return cloud.getMacsInfo();
    }
    
    static String getAddress(BridgePort bp) {
        return String.format("bridge port %d vlan %d",bp.getBridgePort(),bp.getVlan());
    }
    
    static String getAddress(CdpLinkTopologyEntity cdplink) {
        return cdplink.getCdpCacheAddress();
    }

    static String getRemoteAddress(LldpLinkTopologyEntity lldplink) {
        return String.format("%s type %s", lldplink.getLldpRemPortId(),lldplink.getLldpRemPortIdSubType().name());
    }

    static String getRemoteAddress(OspfLinkTopologyEntity ospflink) {
        return InetAddressUtils.str(ospflink.getOspfRemIpAddr());
    }

    static String getAddress(OspfLinkTopologyEntity ospflink) {
        return String.format("%s mask %s", InetAddressUtils.str(ospflink.getOspfIpAddr()), InetAddressUtils.str(ospflink.getOspfIpMask()));
    }

    static String getRemoteAddress(IsIsLinkTopologyEntity isislink) {
        return isislink.getIsisISAdjNeighSNPAAddress();
    }

    static String getAddress(MacCloud cloud, List<MacPort> ports) {
        StringBuilder ip = new StringBuilder();
        if (cloud!= null) {
            ip.append(cloud.getMacsInfo());
        }
        for (MacPort port :ports) {
            ip.append(port.getPortMacInfo());
        }
        return ip.toString();
    }

    static String getMacsCloudIpTextString(TopologyShared shared, List<MacPort> ports) {
        return "shared addresses: " +
                "(" +
                getAddress(shared.getCloud(), ports) +
                ")" +
                "(" +
                getNodeStatus(OnmsNode.NodeType.UNKNOWN) +
                "/" +
                "Not an OpenNMS Node" +
                ")";
    
    }

    static String getNodeTextString(NodeTopologyEntity node, IpInterfaceTopologyEntity primary) {
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

    static String getSharedSegmentTextString(TopologyShared segment) {
        return String.format("'Shared Segment': %s" ,
                segment.getUpPort().printTopology());
    }

    static String getIsManaged(boolean isManaged) {
        if (isManaged) {
            return "Managed";
        }
        return "Unmanaged";

    }

    static String getDefaultIconKey() {
        return "linkd.system";        
    }

    static String getCloudIconKey() {
        return "cloud";
    }
    static String getIconKey(NodeTopologyEntity node) {
        if (node.getSysObjectId() == null) {
            return "linkd.system";
        }
        if (node.getSysObjectId().startsWith(".")) {
            return "linkd.system.snmp" +node.getSysObjectId();
        }
        return "linkd.system.snmp." + node.getSysObjectId();
        
    }
    static String getNodeStatus(OnmsNode.NodeType nodeType) {
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

    static String getDefaultEdgeId(int sourceId, int targetId) {
        return Math.min(sourceId, targetId) + "|" + Math.max(sourceId, targetId);
    }  
    static String getId(TopologyShared segment) {
        return  getId(segment.getUpPort());
    }
    
    static String getId(MacCloud macCloud) {
        return macCloud.getMacs().toString();
    }
    static String getId(MacPort macPort) {
        if (macPort.getNodeId() == null) {
            return macPort.getMacPortMap().keySet().toString();
        }
        return Integer.toString(macPort.getNodeId());
    }
    static String getId(BridgePort bp) {
        return bp.getNodeId()+":"+bp.getBridgePort();
    }
    static String getEdgeId(BridgePort bp, MacPort macport) {
            return getId(bp)+"|"+getId(macport);
    }

    static String getEdgeId(BridgePort sourcebp, BridgePort targetbp) {
        if (sourcebp.getNodeId() < targetbp.getNodeId()) {
            return getId(sourcebp)+"|"+getId(targetbp);
        }
        return getId(targetbp)+"|"+getId(sourcebp);
    }
    static String getEdgeId(String id, MacPort macport) {
        return id + "|" + getId(macport);
    }
    static String getEdgeId(String id, BridgePort bp) {
        return id + "|" + bp.getNodeId() + ":" + bp.getBridgePort();
    }
    static String getDefaultEdgeId(String id1, String id2) {
        return id1+"|"+id2;
    }

    static String getPortId(String id) {
        return "p:"+id;
    }
}
