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
package org.opennms.netmgt.enlinkd.common;

import java.net.InetAddress;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.model.SnmpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.enlinkd.service.api.Topology;
import org.opennms.netmgt.enlinkd.service.api.TopologyService;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.scheduler.Schedulable;
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

public abstract class TopologyUpdater extends Schedulable implements OnmsTopologyUpdater {

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

    private final OnmsTopologyDao m_topologyDao;
    private final NodeTopologyService m_nodeTopologyService;
    private final TopologyService m_topologyService;

    private final Object m_lock = new Object();
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
    public synchronized void runSchedulable() {
        LOG.info("run: start {}", getName());
        final OnmsTopology oldTopology = m_topology.clone();
        final OnmsTopology newTopology = runDiscoveryInternally(oldTopology);
        if (oldTopology != newTopology) {
            synchronized (m_lock) {
                m_topology = newTopology;
                setDefaultVertex();
            }
        }
        LOG.info("run: end {}", getName());
    }

    public void setDefaultVertex() {
        if (m_topology.getVertices().isEmpty()) {
            LOG.info("setDefaultVertex: {}: topology is empty", getName());
            return;
        }
        NodeTopologyEntity defaultFocusPoint = getDefaultFocusPoint();
        if (defaultFocusPoint != null) {
            OnmsTopologyVertex dv = create(defaultFocusPoint,getIpPrimaryMap().get(defaultFocusPoint.getId()));
            if (m_topology.hasVertex(dv.getId())) {
                m_topology.setDefaultVertex(dv);
                LOG.info("setDefaultVertex: {}: set default: {}", getName(), dv.getLabel());
            } else  {
                m_topology.setDefaultVertex(m_topology.getVertices().iterator().next());
                LOG.info("setDefaultVertex: {}: set first item: {}",getName(), m_topology.getDefaultVertex().getLabel());
            }
        }
    }

    protected OnmsTopology runDiscoveryInternally(OnmsTopology oldTopology) {
        if (!m_runned) {
            try {
                OnmsTopology newTopology = buildTopology();
                m_runned = true;
                m_topologyService.parseUpdates();
                newTopology.getVertices().forEach(this::update);
                newTopology.getEdges().forEach(this::update);
                LOG.info("run: {} first run topology calculated", getName());
                return newTopology;
            } catch (Exception e) {
                LOG.error("run: {} first run: cannot build topology", getName(), e);
                return oldTopology;
            }
        } else if (m_topologyService.parseUpdates() || m_forceRun) {
            m_forceRun = false;
            m_topologyService.refresh();
            LOG.info("run: updates {}, recalculating topology ", getName());
            OnmsTopology newTopology;
            try {
                newTopology = buildTopology();
            } catch (Exception e) {
                LOG.error("cannot build topology", e);
                return oldTopology;
            }
            oldTopology.getVertices().stream().filter(v -> !newTopology.hasVertex(v.getId())).forEach(this::delete);
            oldTopology.getEdges().stream().filter(g -> !newTopology.hasEdge(g.getId())).forEach(this::delete);

            newTopology.getVertices().stream().filter(v -> !m_topology.hasVertex(v.getId())).forEach(this::update);
            newTopology.getEdges().stream().filter(g -> !m_topology.hasEdge(g.getId())).forEach(this::update);
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
        return m_nodeTopologyService.findAllNode().stream().collect(Collectors.toMap(NodeTopologyEntity::getId, node -> node, (n1, n2) ->n1));
    }

    public Map<Integer, IpInterfaceTopologyEntity> getIpPrimaryMap() {
        return m_nodeTopologyService.findAllIp().
                stream().
                collect
                (
                      Collectors.toMap
                      (
                              IpInterfaceTopologyEntity::getNodeId,
                           ip -> ip,
                              TopologyUpdater::getPrimary
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

    public Map<Integer, SnmpInterfaceTopologyEntity> getSnmpInterfaceMap() {
        return
                m_nodeTopologyService.
                        findAllSnmp().
                        stream().
                        collect(Collectors.toMap(SnmpInterfaceTopologyEntity::getId, Function.identity()));
    }

    public Table<Integer, InetAddress, IpInterfaceTopologyEntity> getIpInterfaceTable() {
        Table<Integer, InetAddress,IpInterfaceTopologyEntity> nodeToOnmsIpTable = HashBasedTable.create();
        for (IpInterfaceTopologyEntity ip: m_nodeTopologyService.findAllIp()) {
            if (!nodeToOnmsIpTable.contains(ip.getNodeId(),ip.getIpAddress())) {
                nodeToOnmsIpTable.put(ip.getNodeId(),ip.getIpAddress(),ip);
            }
        }
        return nodeToOnmsIpTable;
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
        synchronized (m_lock) {
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
        synchronized (m_lock) {
            m_topology = topology;
        }
    }

    public boolean isRunned() {
        return m_runned;
    }

    public void setRunned(boolean runned) {
        m_runned = runned;
    }

    public void forceRun() {
        m_forceRun = true;
    }
                
}

