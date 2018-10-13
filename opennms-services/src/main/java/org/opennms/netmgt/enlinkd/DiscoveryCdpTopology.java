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

import org.opennms.netmgt.model.CdpElement;
import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsTopology;
import org.opennms.netmgt.model.OnmsTopologyEdge;
import org.opennms.netmgt.model.OnmsTopologyException;
import org.opennms.netmgt.model.OnmsTopologyMessage;
import org.opennms.netmgt.model.OnmsTopologyUpdater;
import org.opennms.netmgt.model.OnmsTopologyVertex;
import org.opennms.netmgt.model.topology.Topology;
import org.opennms.netmgt.model.topology.Topology.ProtocolSupported;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscoveryCdpTopology extends Discovery implements OnmsTopologyUpdater {

    public static DiscoveryCdpTopology createAndRegister(EnhancedLinkd linkd) throws OnmsTopologyException {
        DiscoveryCdpTopology discoveryCdpTopology = new DiscoveryCdpTopology(linkd);
        linkd.getQueryManager().register(discoveryCdpTopology);
        return discoveryCdpTopology;
    }

    private static final Logger LOG = LoggerFactory.getLogger(DiscoveryCdpTopology.class);
        
    private DiscoveryCdpTopology(EnhancedLinkd linkd) {
        super(linkd, linkd.getBridgeTopologyInterval(), linkd.getInitialSleepTime()+linkd.getBridgeTopologyInterval());
    }
            
    
    @Override
    public void runDiscovery() {
        LOG.debug("run: start");
        OnmsTopology topo = getTopology();
        topo.getVertices().stream().forEach(vertex -> {
                m_linkd.getQueryManager().update(this, OnmsTopologyMessage.update(vertex));
        });
        topo.getEdges().stream().forEach(edge -> {
                m_linkd.getQueryManager().update(this, OnmsTopologyMessage.update(edge));
        });
        LOG.debug("run: end");
    }

    @Override
    public String getName() {
        return "DiscoveryCdpTopology";
    }

    @Override
    public OnmsTopology getTopology() {
        Map<Integer, OnmsNode> nodeMap=new HashMap<Integer, OnmsNode>();
        m_linkd.getQueryManager().getAllOnmsNodes().stream().forEach(node -> nodeMap.put(node.getId(), node));
        Map<Integer, CdpElement> cdpelementmap = new HashMap<Integer, CdpElement>();
        OnmsTopology topology = new OnmsTopology();
        m_linkd.getQueryManager().getAllCdpElements().stream().forEach(cdpelement -> {
            cdpelementmap.put(cdpelement.getNode().getId(), cdpelement);
            OnmsTopologyVertex vertex = OnmsTopologyVertex.create(nodeMap.get(cdpelement.getNode().getId()));
            vertex.getProtocolSupported().add(ProtocolSupported.CDP.name());
            topology.getVertices().add(vertex);
        });
        
        List<CdpLink> allLinks = m_linkd.getQueryManager().getAllCdpLinks();
        Set<Integer> parsed = new HashSet<Integer>();

        for (CdpLink sourceLink : allLinks) {
            if (parsed.contains(sourceLink.getId())) { 
                continue;
            }
            LOG.warn("getCdpTopology: source: {} ", sourceLink.printTopology());
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
                    LOG.warn("getCdpLinks: cdp: {}, target: {} ", link.getCdpCacheDevicePort(), targetLink.printTopology());
                    break;
                }
            }
                        
            if (targetLink == null) {
                LOG.warn("getCdpLinks: cannot found target for source: '{}'", sourceLink.getId());
                continue;
            }
                
            parsed.add(sourceLink.getId());
            parsed.add(targetLink.getId());
            OnmsTopologyVertex source = topology.getVertex(sourceLink.getNode().getId().toString());
            OnmsTopologyVertex target = topology.getVertex(targetLink.getNode().getId().toString());
            OnmsTopologyEdge edge = OnmsTopologyEdge.create(source, target, sourceLink.getCdpCacheIfIndex(),targetLink.getCdpCacheIfIndex());
            edge.setSourcePort(sourceLink.getCdpInterfaceName());
            edge.setSourceIfIndex(sourceLink.getCdpCacheIfIndex());
            edge.setSourceAddr(targetLink.getCdpCacheAddress());
            edge.setTargetPort(targetLink.getCdpInterfaceName());
            edge.setTargetIfIndex(targetLink.getCdpCacheIfIndex());
            edge.setTargetAddr(sourceLink.getCdpCacheAddress());
            edge.setDiscoveredBy(ProtocolSupported.CDP.name());
            topology.getEdges().add(edge);
       }
        
        return topology;
    }

    @Override
    public String getId() {
        return Topology.ProtocolSupported.CDP.name();
    }

    @Override
    public String getProtocol() {
        return Topology.ProtocolSupported.CDP.name();
    }
            
}

