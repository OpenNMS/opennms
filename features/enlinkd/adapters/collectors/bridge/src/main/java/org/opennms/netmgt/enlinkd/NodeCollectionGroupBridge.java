/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.enlinkd.common.NodeCollector;
import org.opennms.netmgt.enlinkd.common.SchedulableNodeCollectorGroup;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.Node;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.scheduler.LegacyPriorityExecutor;
import org.opennms.netmgt.snmp.proxy.LocationAwareSnmpClient;

public class NodeCollectionGroupBridge extends SchedulableNodeCollectorGroup {

    private final BridgeTopologyService m_bridgeTopologyService;
    private final int m_maxBft;
    private final boolean m_disableBridgeVlanDiscovery;


    public NodeCollectionGroupBridge(long interval, long initial, LegacyPriorityExecutor executor, int priority, NodeTopologyService nodeTopologyService, LocationAwareSnmpClient locationAwareSnmpClient, BridgeTopologyService bridgeTopologyService, int max_bft, boolean disableBridgeVlanDiscovery) {
        super(interval, initial, executor, priority, ProtocolSupported.BRIDGE, nodeTopologyService, locationAwareSnmpClient);
        m_bridgeTopologyService = bridgeTopologyService;
        m_maxBft = max_bft;
        m_disableBridgeVlanDiscovery = disableBridgeVlanDiscovery;
    }


    public BridgeTopologyService getBridgeTopologyService() {
        return m_bridgeTopologyService;
    }

    public int getMaxBft() {
        return m_maxBft;
    }

    public boolean isDisableBridgeVlanDiscovery() {
        return m_disableBridgeVlanDiscovery;
    }

    @Override
    public NodeCollector getNodeCollector(final Node node, final int priority) {
        return new NodeDiscoveryBridge(this, node, priority);
    }
}
