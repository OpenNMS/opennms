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

import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.model.OnmsNode;

public interface Topology {

    String printTopology();

    public static String getToolTipText(MacPort port) {
        final StringBuilder tooltipText = new StringBuilder();
        tooltipText.append(getId(port));
        tooltipText.append(": ");
        tooltipText.append("(");
        if (port.hasInetAddresses()) {
            tooltipText.append(port.getIpMacInfo());
        } else {
            tooltipText.append("no ip address");
        }
        tooltipText.append(")");
        tooltipText.append("(");
        tooltipText.append(getNodeStatus(OnmsNode.NodeType.UNKNOWN));
        tooltipText.append("/");
        tooltipText.append("Not an OpenNMS Node");
        tooltipText.append(")");
        
        
        return tooltipText.toString();
    
    }

    public static String getToolTipText(NodeTopologyEntity node) {
        final StringBuilder tooltipText = new StringBuilder();
        tooltipText.append(node.getLabel());
        tooltipText.append(": ");
        tooltipText.append("(");
        tooltipText.append(node.getAddress());
        tooltipText.append(")");
        tooltipText.append("(");
        tooltipText.append(getNodeStatus(node.getType()));
        tooltipText.append("/");
        tooltipText.append(getIsManaged(node.isManaged()));
        tooltipText.append(")");
        
        if (node.getLocation() != null && node.getLocation().trim().length() > 0) {
                tooltipText.append(node.getLocation());
        }
        
        return tooltipText.toString();
    
    }
    
    public static String getIsManaged(boolean isManaged) {
        if (isManaged) {
            return "Managed";
        }
        return "Unmanaged";

    }
    
    public static String getIconKey(MacPort macPort) {
        return "linkd.system";        
    }
    public static String getIconKey(NodeTopologyEntity node) {
        if (node.getSysoid() == null) {
            return "linkd.system";
        }
        if (node.getSysoid().startsWith(".")) {
            return "linkd.system.snmp" +node.getSysoid();
        }
        return "linkd.system.snmp." + node.getSysoid();
        
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
    public static String getId(BridgePort designated) {
        return  designated.getNodeId()+":"+designated.getBridgePort();
    }
    public static String getId(MacPort macPort) {
        if (macPort.getNodeId() == null) {
            return macPort.getMacPortMap().keySet().toString();
        }
        return Integer.toString(macPort.getNodeId());
    }
    public static String getEdgeId(BridgePort bp, MacPort macport ) {
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
    
    
}
