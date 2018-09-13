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

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opennms.netmgt.dao.api.CdpElementDao;
import org.opennms.netmgt.dao.api.CdpLinkDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.TopologyDao;
import org.opennms.netmgt.model.CdpElement;
import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsTopology;
import org.opennms.netmgt.model.OnmsTopologyEdge;
import org.opennms.netmgt.model.OnmsTopologyMessage;
import org.opennms.netmgt.model.OnmsTopologyProtocol;
import org.opennms.netmgt.model.OnmsTopologyUpdater;
import org.opennms.netmgt.model.OnmsTopologyVertex;
import org.opennms.netmgt.model.topology.Topology;
import org.opennms.netmgt.model.topology.Topology.ProtocolSupported;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

public class DiscoveryCdpTopology extends Discovery implements OnmsTopologyUpdater, InitializingBean {

    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryCdpTopology.class);
    final int m_maxthreads;

    @Autowired
    NodeDao m_nodeDao;
   
    @Autowired
    CdpElementDao m_cdpElementDao;

    @Autowired
    CdpLinkDao m_cdpLinkDao;
    
    @Autowired
    TopologyDao m_topologyDao;
    
    public DiscoveryCdpTopology(EnhancedLinkd linkd) {
        super(linkd, linkd.getBridgeTopologyInterval(), linkd.getInitialSleepTime()+linkd.getBridgeTopologyInterval());
        m_maxthreads=linkd.getDiscoveryBridgeThreads();
    }
            
    
    @Override
    public void runDiscovery() {
        OnmsTopology topo = getTopology();
        topo.getVertices().stream().forEach(vertex -> m_topologyDao.update(this, OnmsTopologyMessage.update(vertex)));
        topo.getEdges().stream().forEach(edge -> m_topologyDao.update(this, OnmsTopologyMessage.update(edge)));
    }

    @Override
    public String getName() {
        return "DiscoveryCdpTopology";
    }

    @Override
    public OnmsTopology getTopology() {
        Map<Integer, OnmsNode> nodeMap=new HashMap<Integer, OnmsNode>();
        m_nodeDao.findAll().stream().forEach(node -> nodeMap.put(node.getId(), node));
        Map<Integer, CdpElement> cdpelementmap = new HashMap<Integer, CdpElement>();
        OnmsTopology topology = new OnmsTopology();
        m_cdpElementDao.findAll().stream().forEach(cdpelement -> {
            cdpelementmap.put(cdpelement.getNode().getId(), cdpelement);
            OnmsTopologyVertex vertex = OnmsTopologyVertex.create(nodeMap.get(cdpelement.getNode().getId()));
            vertex.getProtocolSupported().add(OnmsTopologyProtocol.createFromTopologySupportedProtocol(ProtocolSupported.CDP));
            topology.getVertices().add(vertex);
        });
        List<CdpLink> allLinks = m_cdpLinkDao.findAll();
        Set<Integer> parsed = new HashSet<Integer>();

        for (CdpLink sourceLink : allLinks) {
            if (parsed.contains(sourceLink.getId())) { 
                continue;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("getCdpTopology: source: {} ", sourceLink.printTopology());
            }
            CdpElement sourceCdpElement = cdpelementmap.get(sourceLink.getNode().getId());
            CdpLink targetLink = null;
            for (CdpLink link : allLinks) {
                if (sourceLink.getId().intValue() == link.getId().intValue()|| parsed.contains(link.getId())) {
                    continue;
                }
                CdpElement element = cdpelementmap.get(link.getNode().getId());
                //Compare the remote data to the targetNode element data
                if (!sourceLink.getCdpCacheDeviceId().equals(element.getCdpGlobalDeviceId()) || !link.getCdpCacheDeviceId().equals(sourceCdpElement.getCdpGlobalDeviceId())) {
                    continue;
                }

                if (sourceLink.getCdpInterfaceName().equals(link.getCdpCacheDevicePort()) && link.getCdpInterfaceName().equals(sourceLink.getCdpCacheDevicePort())) {
                    targetLink=link;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getCdpLinks: cdp: {}, target: {} ", link.getCdpCacheDevicePort(), targetLink.printTopology());
                    }
                    break;
                }
            }
                        
            if (targetLink == null) {
                LOG.debug("getCdpLinks: cannot found target for source: '{}'", sourceLink.getId());
                continue;
            }
                
            parsed.add(sourceLink.getId());
            parsed.add(targetLink.getId());
            OnmsTopologyVertex source = topology.getVertex(sourceLink.getNode().getNodeId());
            OnmsTopologyVertex target = topology.getVertex(targetLink.getNode().getNodeId());
            OnmsTopologyEdge edge = OnmsTopologyEdge.create(source, target);
            edge.setSourcePort(sourceLink.getCdpInterfaceName());
            edge.setSourceIfIndex(sourceLink.getCdpCacheIfIndex());
            edge.setSourceAddr(targetLink.getCdpCacheAddress());
            edge.setTargetPort(targetLink.getCdpInterfaceName());
            edge.setTargetIfIndex(targetLink.getCdpCacheIfIndex());
            edge.setTargetAddr(sourceLink.getCdpCacheAddress());
            edge.setDiscoveredBy(OnmsTopologyProtocol.createFromTopologySupportedProtocol(ProtocolSupported.CDP));
            topology.getEdges().add(edge);
       }
        
        return topology;
    }

    @Override
    public String getId() {
        return Topology.ProtocolSupported.CDP.name();
    }

    @Override
    public OnmsTopologyProtocol getProtocol() {
        return OnmsTopologyProtocol.createFromTopologySupportedProtocol(Topology.ProtocolSupported.CDP);
    }


    public NodeDao getNodeDao() {
        return m_nodeDao;
    }


    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }


    public CdpElementDao getCdpElementDao() {
        return m_cdpElementDao;
    }


    public void setCdpElementDao(CdpElementDao cdpElementDao) {
        m_cdpElementDao = cdpElementDao;
    }


    public CdpLinkDao getCdpLinkDao() {
        return m_cdpLinkDao;
    }


    public void setCdpLinkDao(CdpLinkDao cdpLinkDao) {
        m_cdpLinkDao = cdpLinkDao;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        m_topologyDao.register(this);
    }
            
}

