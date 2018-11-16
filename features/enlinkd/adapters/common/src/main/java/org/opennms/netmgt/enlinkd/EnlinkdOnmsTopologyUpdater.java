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
import java.util.stream.Collectors;

import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyException;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyMessage;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyUpdater;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class EnlinkdOnmsTopologyUpdater extends Discovery implements OnmsTopologyUpdater {

    private static final Logger LOG = LoggerFactory.getLogger(EnlinkdOnmsTopologyUpdater.class);

    private final OnmsTopologyDao m_topologyDao;
    private final NodeTopologyService m_nodeTopologyService;

    public EnlinkdOnmsTopologyUpdater(EventForwarder eventforwarder,
            OnmsTopologyDao topologyDao, NodeTopologyService nodeTopologyService,
            long interval, long initialsleeptime) {
        super(eventforwarder, interval, initialsleeptime);
        m_topologyDao = topologyDao;
        m_nodeTopologyService = nodeTopologyService;
    }            
    
    @Override
    public void runDiscovery() {
        LOG.debug("run: start");
        OnmsTopology topo = getTopology();
        topo.getVertices().stream().forEach(vertex -> {
            try {
                m_topologyDao.update(this, OnmsTopologyMessage.update(vertex));
            } catch (OnmsTopologyException e) {
                LOG.error("update: cannot {}: {} {} {}", e.getMessageStatus(), e.getId(), e.getProtocol(),e.getMessage());
            }
        });
        topo.getEdges().stream().forEach(edge -> {
            try {
                m_topologyDao.update(this, OnmsTopologyMessage.update(edge));
            } catch (OnmsTopologyException e) {
                LOG.error("update: cannot {}: {} {} {}", e.getMessageStatus(), e.getId(), e.getProtocol(),e.getMessage());
            }
        });
        LOG.debug("run: end");
    }

    public OnmsTopologyDao getTopologyDao() {
        return m_topologyDao;
    }

    public NodeTopologyService getNodeTopologyService() {
        return m_nodeTopologyService;
    }

    public Map<Integer, OnmsNode> getNodeMap() {
        return m_nodeTopologyService.findAll().stream().collect(Collectors.toMap(node -> node.getId(), node -> node));

    }
            
}

