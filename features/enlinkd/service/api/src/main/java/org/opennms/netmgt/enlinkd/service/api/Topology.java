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

public interface Topology {

    String printTopology();

    public static String getDefaultEdgeId(int sourceId,int targetId) {
        return Math.min(sourceId, targetId) + "|" + Math.max(sourceId, targetId);
    }  
    public static String getId(BridgePort designated) {
        return  designated.getNodeId()+":"+designated.getBridgePort();
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

    static String getDefaultEdgeId(String id, String id2) {
        return id+"|"+id2;
    }
}
