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

import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BridgeOnmsTopologyUpdater extends EnlinkdOnmsTopologyUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(BridgeOnmsTopologyUpdater.class);

    protected final BridgeTopologyService m_bridgeTopologyService;

    public BridgeOnmsTopologyUpdater(EventForwarder eventforwarder,
            OnmsTopologyDao topologyDao, BridgeTopologyService bridgeTopologyService, NodeTopologyService nodeTopologyService,
            long interval, long initialsleeptime) {
        super(eventforwarder, topologyDao,nodeTopologyService,interval, initialsleeptime);
        m_bridgeTopologyService = bridgeTopologyService;
    }            
    
    @Override
    public String getName() {
        return "BridgeTopologyUpdaters";
    }

    @Override
    public OnmsTopology getTopology() {
        return new OnmsTopology();
    }

    @Override
    public String getId() {
        return ProtocolSupported.BRIDGE.name();
    }

    @Override
    public String getProtocol() {
        return ProtocolSupported.CDP.name();
    }
            
}

