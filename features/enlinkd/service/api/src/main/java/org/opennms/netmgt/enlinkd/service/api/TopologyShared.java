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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TopologyShared implements Topology {
    
    public static TopologyShared   of(SharedSegment shs, List<MacPort> macPortsOnSegment) throws BridgeTopologyException {
        TopologyShared tps = new TopologyShared(new ArrayList<BridgePort>(shs.getBridgePortsOnSegment()), 
                                                macPortsOnSegment, shs.getDesignatedPort());
        

        final Set<String>  noPortMacs = new HashSet<String>(shs.getMacsOnSegment());
        macPortsOnSegment.stream().forEach(mp -> noPortMacs.removeAll(mp.getMacPortMap().keySet()));
        
        if (noPortMacs.size() >0) {
            tps.setCloud(MacCloud.create(noPortMacs));
        }
        return tps;
    }

    private TopologyShared(List<BridgePort> left, List<MacPort> right,BridgePort top ) {
        this.designated = top;
        this.left = left;
        this.right = right;
    }

    private MacCloud cloud;
    private BridgePort designated;
    private List<BridgePort> left;
    private List<MacPort> right;

    public List<BridgePort> getBridgePorts() {
        return left;
    }

    public List<MacPort> getMacPorts() {
        return right;
    }

    public BridgePort getUpPort() {
        return designated;
    }

    public MacCloud getCloud() {
        return cloud;
    }

    public void setCloud(MacCloud cloud) {
        this.cloud = cloud;
    }

    @Override
    public String printTopology() {
        final StringBuffer strbfr = new StringBuffer();
        strbfr.append("shared -> designated bridge:[");
        strbfr.append(designated.printTopology());
        strbfr.append("]\n");
        for (BridgePort blink:  left) {
            strbfr.append("        -> port:");            
            if (blink == null) {
                strbfr.append("[null]");
            } else {
                strbfr.append(blink.printTopology());
            }
            strbfr.append("\n");
        }
        for (MacPort port: right) {
            strbfr.append("        -> macs:");
            strbfr.append(port.printTopology());
        }
        if (cloud != null) {
            strbfr.append("        -> macs:");
            strbfr.append(cloud.printTopology());
        }
        return strbfr.toString();
    }
        
}
