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

package org.opennms.netmgt.enlinkd.common;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.model.SnmpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.enlinkd.service.api.Topology;
import org.opennms.netmgt.enlinkd.service.api.TopologyService;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.topologies.service.api.OnmsTopology;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyDao;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyMessage;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyProtocol;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyRef;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyUpdater;
import org.opennms.netmgt.topologies.service.api.OnmsTopologyVertex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

public abstract class TopologyUpdater extends Discovery implements OnmsTopologyUpdater {

    public static OnmsTopologyProtocol create(ProtocolSupported protocol) {
            return OnmsTopologyProtocol.create(protocol.name());
    }
    
    public static OnmsTopologyVertex create(NodeTopologyEntity node, IpInterfaceTopologyEntity primary) {
        Objects.requireNonNull(node);
        Objects.requireNonNull(node.getId());
        OnmsTopologyVertex vertex = 
                OnmsTopologyVertex.create(node.getId().toString(), 
                                          node.getLabel(), 
                                          Topology.getAddress(primary), 
                                          Topology.getIconKey(node)
                                          );
        vertex.setNodeid(node.getId());
        vertex.setToolTipText(Topology.getNodeTextString(node, primary));
        return vertex;
    }

    private static final Logger LOG = LoggerFactory.getLogger(TopologyUpdater.class);

    private OnmsTopologyDao m_topologyDao;
    private NodeTopologyService m_nodeTopologyService;
    private TopologyService m_topologyService;

    private OnmsTopology m_topology;
    private boolean m_runned = false;
    private boolean m_registered = false;
    private boolean m_forceRun = false;

    public TopologyUpdater(
            TopologyService topologyService,
            OnmsTopologyDao topologyDao, NodeTopologyService nodeTopologyService) {
        super();
        m_topologyDao = topologyDao;
        m_topologyService = topologyService;
        m_nodeTopologyService = nodeTopologyService;
        m_topology = new OnmsTopology();
    }            
    
    public void register() {
        if (m_registered) {
            return;
        }
        try {
            m_topologyDao.register(this);
            m_registered  = true;
            LOG.info("register: protocol:{}", getProtocol().getId());
        } catch (Exception e) {
            LOG.error("register", e);
        }
    }
    
    public void unregister() {
        if (!m_registered) {
            return;
        }
        try {
            m_topologyDao.unregister(this);
            m_registered = false;
            LOG.info("unregister: protocol:{}", getProtocol().getId());
        } catch (Exception e) {
            LOG.error("unregister", e);
        }
    }
    
    private <T extends OnmsTopologyRef>void update(T topoObject) {
        try {
            m_topologyDao.update(this, OnmsTopologyMessage.update(topoObject,getProtocol()));
        } catch (Exception e) {
            LOG.error("Exception while updating", e);
        }
    }

    private <T extends OnmsTopologyRef>void delete(T topoObject) {
        try {
            m_topologyDao.update(this, OnmsTopologyMessage.delete(topoObject,getProtocol()));
        } catch (Exception e) {
            LOG.error("Exception while deleting", e);
        }
    }

    @Override
    public synchronized void runDiscovery() {
        LOG.debug("run: start {}", getName());
        final OnmsTopology oldTopology = m_topology.clone();
        final OnmsTopology newTopology = runDiscoveryInternally(oldTopology);
        if (oldTopology != newTopology) {
            synchronized (m_topology) {
                m_topology = newTopology;
            }
        }
        LOG.debug("run: end {}", getName());
    }

    protected OnmsTopology runDiscoveryInternally(OnmsTopology oldTopology) {
        if (!m_runned) {
            try {
                OnmsTopology newTopology = buildTopology();
                m_runned = true;
                m_topologyService.parseUpdates();
                newTopology.getVertices().stream().forEach(v -> update(v));
                newTopology.getEdges().stream().forEach(g -> update(g));
                LOG.debug("run: {} first run topology calculated", getName());
                return newTopology;
            } catch (Exception e) {
                LOG.error("run: {} first run: cannot build topology", getName(), e);
                return oldTopology;
            }
        } else if (m_topologyService.parseUpdates() || m_forceRun) {
            m_forceRun = false;
            m_topologyService.refresh();
            LOG.debug("run: updates {}, recalculating topology ", getName());
            OnmsTopology newTopology;
            try {
                newTopology = buildTopology();
            } catch (Exception e) {
                LOG.error("cannot build topology", e);
                return oldTopology;
            }
            oldTopology.getVertices().stream().filter(v -> !newTopology.hasVertex(v.getId())).forEach(v -> delete(v));
            oldTopology.getEdges().stream().filter(g -> !newTopology.hasEdge(g.getId())).forEach(g -> delete(g));

            newTopology.getVertices().stream().filter(v -> !m_topology.hasVertex(v.getId())).forEach(v -> update(v));
            newTopology.getEdges().stream().filter(g -> !m_topology.hasEdge(g.getId())).forEach(g -> update(g));
            return newTopology;
        }
        return oldTopology;
    }

    public OnmsTopologyDao getTopologyDao() {
        return m_topologyDao;
    }

    public NodeTopologyService getNodeTopologyService() {
        return m_nodeTopologyService;
    }

    public Map<Integer, NodeTopologyEntity> getNodeMap() {
        return m_nodeTopologyService.findAllNode().stream().collect(Collectors.toMap(node -> node.getId(), node -> node, (n1,n2) ->n1));
    }
    
    public Map<Integer, IpInterfaceTopologyEntity> getIpPrimaryMap() {
        return m_nodeTopologyService.findAllIp().
                stream().
                collect
                (
                      Collectors.toMap
                      (
                           ip -> ip.getNodeId(), 
                           ip -> ip, 
                           (ip1,ip2) -> getPrimary(ip1, ip2)
                      )
                );
    }
    
    public Table<Integer, Integer,SnmpInterfaceTopologyEntity> getSnmpInterfaceTable() {
        Table<Integer, Integer,SnmpInterfaceTopologyEntity> nodeToOnmsSnmpTable = HashBasedTable.create();
        for (SnmpInterfaceTopologyEntity snmp: m_nodeTopologyService.findAllSnmp()) {
            if (!nodeToOnmsSnmpTable.contains(snmp.getNodeId(),snmp.getIfIndex())) {
                nodeToOnmsSnmpTable.put(snmp.getNodeId(),snmp.getIfIndex(),snmp);
            }
        }
        return nodeToOnmsSnmpTable;
    }
    
    private static IpInterfaceTopologyEntity getPrimary(IpInterfaceTopologyEntity n1, IpInterfaceTopologyEntity n2) {
        if (PrimaryType.PRIMARY.equals(n2.getIsSnmpPrimary()) ) {
            return n2;
        }
        return n1;
    }
    public abstract OnmsTopology buildTopology();

    @Override
    public OnmsTopology getTopology() {
        synchronized(m_topology) {
            return m_topology.clone();
        }
    }

    public NodeTopologyEntity getDefaultFocusPoint() { 
        return m_nodeTopologyService.getDefaultFocusPoint();
    }

    public boolean isRegistered() {
        return m_registered;
    }

    public void setRegistered(boolean registered) {
        m_registered = registered;
    }

    public void setTopology(OnmsTopology topology) {
        synchronized (m_topology) {
            m_topology = topology;
        }
    }

    public boolean isRunned() {
        return m_runned;
    }

    public void setRunned(boolean runned) {
        m_runned = runned;
    }

    public boolean isForceRun() {
        return m_forceRun;
    }

    public void forceRun() {
        m_forceRun = true;
    }
                
}

