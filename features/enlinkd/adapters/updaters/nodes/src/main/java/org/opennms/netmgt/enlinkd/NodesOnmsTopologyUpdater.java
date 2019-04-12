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

package org.opennms.netmgt.enlinkd;

import java.util.Map;

import org.opennms.netmgt.enlinkd.common.TopologyUpdater;
import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;

public class NodesOnmsTopologyUpdater extends TopologyUpdater {

    public static NodesOnmsTopologyUpdater clone (NodesOnmsTopologyUpdater bpu) {
        NodesOnmsTopologyUpdater update = new NodesOnmsTopologyUpdater(bpu.getTopologyDao(), bpu.getNodeTopologyService());
        update.setRunned(bpu.isRunned());
        update.setTopology(bpu.getTopology());
        return update;
 
    }
    
    public NodesOnmsTopologyUpdater(
            OnmsTopologyDao topologyDao, NodeTopologyService nodeTopologyService) {
        super(nodeTopologyService, topologyDao,nodeTopologyService);
    }            
    
    @Override
    public String getName() {
        return "NodesTopologyUpdater";
    }

    @Override
    public OnmsTopology buildTopology() {
        Map<Integer, IpInterfaceTopologyEntity> ipMap= getIpPrimaryMap();
        OnmsTopology topology = new OnmsTopology();
        for (NodeTopologyEntity element: getNodeMap().values()) {
            topology.getVertices().add(create(element,ipMap.get(element.getId())));
        }
        NodeTopologyEntity defaultFocusPoint = getDefaultFocusPoint();
        if (defaultFocusPoint != null) {
            topology.setDefaultVertex(create(defaultFocusPoint,ipMap.get(defaultFocusPoint.getId())));
        }
        return topology;
    }

    @Override
    public OnmsTopologyProtocol getProtocol() {
        return create(ProtocolSupported.NODES);
    }

}

