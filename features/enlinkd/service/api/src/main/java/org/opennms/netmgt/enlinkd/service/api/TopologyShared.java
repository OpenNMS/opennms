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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TopologyShared {
    
    private final static Logger LOG = LoggerFactory.getLogger(TopologyShared.class);

    public static TopologyShared   of(SharedSegment shs, List<MacPort> macPortMap) throws BridgeTopologyException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("matchBridgeLinks: \n{}", shs.printTopology());
        }
        Set<String> macsOnMacPorts = new HashSet<String>();
        List<MacPort> macPortsOnSegment = new ArrayList<MacPort>();
        macPortMap.stream().filter( mp -> 
            shs.getMacsOnSegment().containsAll(mp.getMacPortMap().keySet())).
            forEach(mp -> {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("matchBridgeLinks: add mac port \n{}", mp.printTopology());
                }
                macPortsOnSegment.add(mp);
                macsOnMacPorts.addAll(mp.getMacPortMap().keySet());
        });
        TopologyShared tps = new TopologyShared(new ArrayList<BridgePort>(shs.getBridgePortsOnSegment()), 
                                                   macPortsOnSegment, shs.getDesignatedPort());
        
        Set<String>  noMacPortMacs = new HashSet<String>(shs.getMacsOnSegment());
        noMacPortMacs.removeAll(macsOnMacPorts);
        if (noMacPortMacs.size() >0) {
            tps.setCloud(MacCloud.create(noMacPortMacs));
        }
        return tps;
    }

    private TopologyShared(List<BridgePort> left, List<MacPort> right,BridgePort top ) {
        this.top = top;
        this.left = left;
        this.right = right;
    }

    private MacCloud cloud;
    private BridgePort top;
    private List<BridgePort> left;
    private List<MacPort> right;

    public List<BridgePort> getBridgePorts() {
        return left;
    }

    public List<MacPort> getMacPorts() {
        return right;
    }

    public BridgePort getUpPort() {
        return top;
    }

    public MacCloud getCloud() {
        return cloud;
    }

    public void setCloud(MacCloud cloud) {
        this.cloud = cloud;
    }
        
}
