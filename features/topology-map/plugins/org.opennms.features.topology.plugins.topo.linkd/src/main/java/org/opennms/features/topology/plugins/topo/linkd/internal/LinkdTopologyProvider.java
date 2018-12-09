/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.plugins.topo.linkd.internal;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionAware;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.topo.AbstractTopologyProvider;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Defaults;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.enlinkd.model.CdpLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.IsIsLink;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.model.OspfLink;
import org.opennms.netmgt.enlinkd.service.api.BridgePort;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyException;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.CdpTopologyService;
import org.opennms.netmgt.enlinkd.service.api.IsisTopologyService;
import org.opennms.netmgt.enlinkd.service.api.LldpTopologyService;
import org.opennms.netmgt.enlinkd.service.api.MacPort;
import org.opennms.netmgt.enlinkd.service.api.NodeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.OspfTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.enlinkd.service.api.Topology;
import org.opennms.netmgt.enlinkd.service.api.TopologyConnection;
import org.opennms.netmgt.enlinkd.service.api.TopologyShared;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

public class LinkdTopologyProvider extends AbstractTopologyProvider implements GraphProvider {

    private static Logger LOG = LoggerFactory.getLogger(LinkdTopologyProvider.class);

    private SnmpInterfaceDao m_snmpInterfaceDao;

    private NodeTopologyService m_nodeTopologyService;
    private BridgeTopologyService m_bridgeTopologyService;
    private CdpTopologyService m_cdpTopologyService;
    private LldpTopologyService m_lldpTopologyService;
    private OspfTopologyService m_ospfTopologyService;
    private IsisTopologyService m_isisTopologyService;

    private Table<Integer, Integer,OnmsSnmpInterface> m_nodeToOnmsSnmpTable = HashBasedTable.create();

    private final Timer m_loadFullTimer;
    private final Timer m_loadSnmpInterfacesTimer;
    private final Timer m_loadLldpLinksTimer;
    private final Timer m_loadOspfLinksTimer;
    private final Timer m_loadCdpLinksTimer;
    private final Timer m_loadIsisLinksTimer;
    private final Timer m_loadBridgeLinksTimer;
    private final Timer m_loadVerticesTimer;
    private final Timer m_loadEdgesTimer;

    public static final String TOPOLOGY_NAMESPACE_LINKD = "nodes";
    
    private SelectionAware selectionAwareDelegate = new LinkdSelectionAware();

    public LinkdTopologyProvider(MetricRegistry registry) {
        super(TOPOLOGY_NAMESPACE_LINKD);
        Objects.requireNonNull(registry);
        m_loadFullTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "full"));
        m_loadSnmpInterfacesTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "snmpinterfaces"));
        m_loadLldpLinksTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "links", "lldp"));
        m_loadOspfLinksTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "links", "ospf"));
        m_loadCdpLinksTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "links", "cdp"));
        m_loadIsisLinksTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "links", "isis"));
        m_loadBridgeLinksTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "links", "bridge"));
        m_loadVerticesTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "vertices", "none"));
        m_loadEdgesTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "edges", "none"));
    }

    private OnmsSnmpInterface getSnmpInterface(Integer nodeid, Integer ifindex) {
        if(m_nodeToOnmsSnmpTable.contains(nodeid,ifindex) ) {
                return m_nodeToOnmsSnmpTable.get(nodeid,ifindex);
        }
        return null;
    }
    
    @Override
    public SelectionChangedListener.Selection getSelection(List<VertexRef> selectedVertices, ContentType type) {
       return selectionAwareDelegate.getSelection(selectedVertices, type);
    }

    @Override
    public boolean contributesTo(ContentType type) {
        return selectionAwareDelegate.contributesTo(type);
    }

    protected void connectVertices(String id,AbstractVertex sourceV, AbstractVertex targetV,  
            OnmsSnmpInterface sourceinterface,
            OnmsSnmpInterface targetInterface,
            String sourceAddr,
            String targetAddr,
            ProtocolSupported discoveredBy) {
        addEdges(LinkdEdge.create(id, sourceV, targetV, sourceinterface, targetInterface,sourceAddr,targetAddr,discoveredBy));
    }
    
    private void loadEdges() {

        Timer.Context context = m_loadLldpLinksTimer.time();
        try{
            getLldpLinks();
            LOG.info("loadEdges: LldpLink loaded");
        } catch (Exception e){
            LOG.error("Loading LldpLink failed: {}",e.getMessage(),e);
        } finally {
            context.stop();
        }

        context = m_loadOspfLinksTimer.time();
        try{
            getOspfLinks();
            LOG.info("loadEdges: OspfLink loaded");
        } catch (Exception e){
            LOG.error("Loading OspfLink failed: {}",e.getMessage(),e);
        } finally {
            context.stop();
        }

        context = m_loadCdpLinksTimer.time();
        try{
            getCdpLinks();
            LOG.info("loadEdges: CdpLink loaded");
        } catch (Exception e){
            LOG.error("Loading CdpLink failed: {}",e.getMessage(),e);
        } finally {
            context.stop();
        }

        context = m_loadIsisLinksTimer.time();
        try{
            getIsIsLinks();
            LOG.info("loadEdges: IsIsLink loaded");
        } catch (Exception e){
            LOG.error("Exception getting IsIs link: "+e.getMessage(),e);
        } finally {
            context.stop();
        }

        context = m_loadBridgeLinksTimer.time();
        try{
            getBridgeLinks();
            LOG.info("loadEdges: BridgeLink loaded");
        } catch (Exception e){
            LOG.error("Loading BridgeLink failed: {}",e.getMessage(),e);
        } finally {
            context.stop();
        }
    }


    private void getLldpLinks() {

        for (TopologyConnection<LldpLink, LldpLink> pair : m_lldpTopologyService.match()) {
            LldpLink sourceLink = pair.getLeft();
            LldpLink targetLink = pair.getRight();
            LinkdVertex source = (LinkdVertex) getVertex(TOPOLOGY_NAMESPACE_LINKD, sourceLink.getNode().getNodeId());
            source.getProtocolSupported().add(ProtocolSupported.LLDP);
            LinkdVertex target = (LinkdVertex) getVertex(TOPOLOGY_NAMESPACE_LINKD, targetLink.getNode().getNodeId());
            target.getProtocolSupported().add(ProtocolSupported.LLDP);
            OnmsSnmpInterface sourceSnmpInterface = getSnmpInterface(sourceLink.getNode().getId(), sourceLink.getLldpPortIfindex());
            OnmsSnmpInterface targetSnmpInterface = getSnmpInterface(targetLink.getNode().getId(), targetLink.getLldpPortIfindex());
            connectVertices(Topology.getDefaultEdgeId(sourceLink.getId(), targetLink.getId()),
                    source,target,
                    sourceSnmpInterface,targetSnmpInterface,
                    sourceLink.getLldpPortDescr(),targetLink.getLldpPortDescr(),
                    ProtocolSupported.LLDP);
        }
    }

    private void getOspfLinks() {

        for (TopologyConnection<OspfLink, OspfLink> pair : m_ospfTopologyService.match()) {
            OspfLink sourceLink = pair.getLeft();
            OspfLink targetLink = pair.getRight();

            LinkdVertex source = (LinkdVertex)getVertex(TOPOLOGY_NAMESPACE_LINKD, sourceLink.getNode().getNodeId());
            source.getProtocolSupported().add(ProtocolSupported.OSPF);
            LinkdVertex target = (LinkdVertex)getVertex(TOPOLOGY_NAMESPACE_LINKD, targetLink.getNode().getNodeId());
            target.getProtocolSupported().add(ProtocolSupported.OSPF);
            OnmsSnmpInterface sourceSnmpInterface = getSnmpInterface(sourceLink.getNode().getId(), sourceLink.getOspfIfIndex());
            OnmsSnmpInterface targetSnmpInterface = getSnmpInterface(targetLink.getNode().getId(), targetLink.getOspfIfIndex());
            connectVertices(Topology.getDefaultEdgeId(sourceLink.getId(), targetLink.getId()),
                    source,target,
                    sourceSnmpInterface,targetSnmpInterface,
                    InetAddressUtils.str(targetLink.getOspfRemIpAddr()),
                    InetAddressUtils.str(sourceLink.getOspfRemIpAddr()),
                    ProtocolSupported.OSPF);
        }
    }

    private void getCdpLinks() {
        for(TopologyConnection<CdpLinkTopologyEntity, CdpLinkTopologyEntity> pair : m_cdpTopologyService.match()) {
            CdpLinkTopologyEntity sourceLink = pair.getLeft();
            CdpLinkTopologyEntity targetLink = pair.getRight();
            LinkdVertex source = (LinkdVertex) getVertex(TOPOLOGY_NAMESPACE_LINKD, sourceLink.getNodeIdAsString());
            source.getProtocolSupported().add(ProtocolSupported.CDP);
            LinkdVertex target = (LinkdVertex) getVertex(TOPOLOGY_NAMESPACE_LINKD, targetLink.getNodeIdAsString());
            target.getProtocolSupported().add(ProtocolSupported.CDP);
            OnmsSnmpInterface sourceSnmpInterface = getSnmpInterface(sourceLink.getNodeId(), sourceLink.getCdpCacheIfIndex());
            OnmsSnmpInterface targetSnmpInterface = getSnmpInterface(targetLink.getNodeId(), targetLink.getCdpCacheIfIndex());
            connectVertices(Topology.getDefaultEdgeId(sourceLink.getId(), targetLink.getId()),
                source, target,
                sourceSnmpInterface, targetSnmpInterface,
                targetLink.getCdpCacheAddress(),
                sourceLink.getCdpCacheAddress(),
                ProtocolSupported.CDP);
        }
    }

    private void getIsIsLinks() {

        for(TopologyConnection<IsIsLink, IsIsLink> pair : m_isisTopologyService.match()) {
            IsIsLink sourceLink = pair.getLeft();
            IsIsLink targetLink = pair.getRight();
            LinkdVertex source = (LinkdVertex) getVertex(TOPOLOGY_NAMESPACE_LINKD, sourceLink.getNode().getNodeId());
            source.getProtocolSupported().add(ProtocolSupported.ISIS);
            LinkdVertex target = (LinkdVertex) getVertex(TOPOLOGY_NAMESPACE_LINKD, targetLink.getNode().getNodeId());
            target.getProtocolSupported().add(ProtocolSupported.ISIS);
            OnmsSnmpInterface sourceSnmpInterface = getSnmpInterface(sourceLink.getNode().getId(), sourceLink.getIsisCircIfIndex());
            OnmsSnmpInterface targetSnmpInterface = getSnmpInterface(targetLink.getNode().getId(), targetLink.getIsisCircIfIndex());
            connectVertices(Topology.getDefaultEdgeId(sourceLink.getId(), targetLink.getId()),
                source, target,
                sourceSnmpInterface, targetSnmpInterface,
                targetLink.getIsisISAdjNeighSNPAAddress(),
                sourceLink.getIsisISAdjNeighSNPAAddress(),
                ProtocolSupported.ISIS);
        }
    }

    
    private void getBridgeLinks() throws BridgeTopologyException {
        for (TopologyShared<BridgePort,MacPort> topologylink: m_bridgeTopologyService.match()) {
            Map<BridgePort,LinkdVertex> portToNodeVertexMap = new HashMap<BridgePort, LinkdVertex>();
            for (BridgePort bp : topologylink.getLeft()) {
                LinkdVertex vertex = (LinkdVertex)getVertex(TOPOLOGY_NAMESPACE_LINKD, bp.getNodeId().toString());
                vertex.getProtocolSupported().add(ProtocolSupported.BRIDGE);
                portToNodeVertexMap.put(bp,vertex);
            }
            
            Map<MacPort,LinkdVertex> macPortToNodeVertexMap = new HashMap<MacPort, LinkdVertex>();
            for (MacPort port : topologylink.getRight()) {
                LinkdVertex vertex;
                if (port.getNodeId() != null) {
                    vertex = (LinkdVertex)getVertex(TOPOLOGY_NAMESPACE_LINKD, port.getNodeId().toString());
                    vertex.getProtocolSupported().add(ProtocolSupported.BRIDGE);
                    continue;
                } else {
                    vertex = LinkdVertex.create(port);
                    addVertices(vertex);
                }
                macPortToNodeVertexMap.put(port,vertex);
            }
        
            if (portToNodeVertexMap.size() == 2 && 
                    macPortToNodeVertexMap.size() == 0) {
                LinkdVertex source = null;
                LinkdVertex target = null;
                BridgePort sourcebp = null;
                BridgePort targetbp = null;
                for (BridgePort bp: portToNodeVertexMap.keySet()) {
                    if (bp.getNodeId() == topologylink.getTop().getNodeId()) {
                        source = portToNodeVertexMap.get(bp);
                        sourcebp = bp;
                        continue;
                    } 
                    target = portToNodeVertexMap.get(bp);
                    targetbp=bp;
                }
                OnmsSnmpInterface sourceSnmpInterface = getSnmpInterface(sourcebp.getNodeId(), sourcebp.getBridgePortIfIndex());
                OnmsSnmpInterface targetSnmpInterface = getSnmpInterface(targetbp.getNodeId(), targetbp.getBridgePortIfIndex());
                connectVertices(Topology.getEdgeId(sourcebp,targetbp),
                                source,target,
                                sourceSnmpInterface,targetSnmpInterface,
                                "bp: "+sourcebp.getBridgePort(),
                                "bp: "+targetbp.getBridgePort(),
                                ProtocolSupported.BRIDGE);
                return;
            }
            if (portToNodeVertexMap.size() == 1 && 
                macPortToNodeVertexMap.size() == 1 ) {
                LinkdVertex sourceVertex = portToNodeVertexMap.values().iterator().next();
                LinkdVertex targetVertex = macPortToNodeVertexMap.values().iterator().next();
                BridgePort sourceBridgePort = portToNodeVertexMap.keySet().iterator().next();
                MacPort targetMacPort = macPortToNodeVertexMap.keySet().iterator().next();
                
                OnmsSnmpInterface sourceinterface = getSnmpInterface(sourceBridgePort.getNodeId(), sourceBridgePort.getBridgePortIfIndex());
                OnmsSnmpInterface targetinterface = getSnmpInterface(targetMacPort.getNodeId(),targetMacPort.getMacPortIfIndex());
                connectVertices(Topology.getEdgeId(sourceBridgePort, targetMacPort), sourceVertex, targetVertex,sourceinterface,targetinterface,
                                "bp: "+sourceBridgePort.getBridgePort(),
                                targetMacPort.toString(),
                                ProtocolSupported.BRIDGE);
                return;
            }
        
            LinkdVertex topVertex = portToNodeVertexMap.get(topologylink.getTop());
            AbstractVertex cloudVertex = addVertex(Topology.getId(topologylink.getTop()), 0, 0);
            cloudVertex.setLabel("Shared Segment");
            cloudVertex.setIconKey("cloud");
            cloudVertex.setTooltipText("'Shared Segment' designated port: " + topologylink.getTop().printTopology());
            if (LOG.isDebugEnabled()) {
                LOG.debug("parseSegment: adding cloud: id: '{}', {}", cloudVertex.getId(), topologylink.getTop().printTopology());
            }
            for (BridgePort bp: portToNodeVertexMap.keySet()) {
                LinkdVertex bpportvertex = portToNodeVertexMap.get(bp);
                OnmsSnmpInterface targetinterface = getSnmpInterface(bp.getNodeId(), bp.getBridgePortIfIndex());
                connectVertices(Topology.getEdgeId(cloudVertex.getId(), bp), cloudVertex, bpportvertex, null, targetinterface, 
                                "shared segment: up bridge " + topVertex.getLabel() + " bp:" +topologylink.getTop().getBridgePort(),
                                "bp: "+bp.getBridgePort(), ProtocolSupported.BRIDGE);
                
            }
            for (MacPort targetMacPort: macPortToNodeVertexMap.keySet()) {
                LinkdVertex target = macPortToNodeVertexMap.get(targetMacPort);
                OnmsSnmpInterface targetinterface = getSnmpInterface(targetMacPort.getNodeId(),targetMacPort.getMacPortIfIndex());
                connectVertices(Topology.getEdgeId(cloudVertex.getId(), targetMacPort), cloudVertex,target, null, 
                                targetinterface,
                                "shared segment: up bridge " + topVertex.getLabel() + " bp:" + topologylink.getTop().getBridgePort(),
                                targetMacPort.printTopology(), ProtocolSupported.BRIDGE);
            }
        
        }
    }
        
    @Override
    public Defaults getDefaults() {
        return new Defaults()
                .withSemanticZoomLevel(Defaults.DEFAULT_SEMANTIC_ZOOM_LEVEL)
                .withPreferredLayout("D3 Layout") // D3 Layout
                .withCriteria(() -> {
                    final NodeTopologyEntity node = m_nodeTopologyService.getDefaultFocusPoint();

                    if (node != null) {
                        final Vertex defaultVertex = getVertex(TOPOLOGY_NAMESPACE_LINKD, node.getId());
                        if (defaultVertex != null) {
                            return Lists.newArrayList(LinkdHopCriteria.createCriteria(node.getId(), node.getLabel()));
                        }
                    }
                    return Lists.newArrayList();
                });
    }
    
    private void doRefresh() {        
        Timer.Context vcontext = m_loadSnmpInterfacesTimer.time();
        try {
            for (OnmsSnmpInterface snmp: m_snmpInterfaceDao.findAll()) {
                if (!m_nodeToOnmsSnmpTable.contains(snmp.getNode().getId(),snmp.getIfIndex())) {
                    m_nodeToOnmsSnmpTable.put(snmp.getNode().getId(),snmp.getIfIndex(),snmp);
                }
            }
            LOG.info("refresh: Snmp Interface loaded");
        } catch (Exception e){
            LOG.error("Loading Snmp Interface failed: {}",e.getMessage(),e);
        } finally {
            vcontext.stop();
        }


        vcontext = m_loadVerticesTimer.time();
        try {
            for (NodeTopologyEntity node : m_nodeTopologyService.findAll()) {
                addVertices(LinkdVertex.create(node));
            }
            LOG.info("refresh: Loaded Vertices");
        } catch (Exception e){
            LOG.error("Exception Loading Vertices: {}",e.getMessage(),e);
        } finally {
            vcontext.stop();
        }
        
        vcontext = m_loadEdgesTimer.time();
        try {
            loadEdges();
            LOG.info("refresh: Loaded Edges");
        } catch (Exception e){
            LOG.error("Exception Loading Edges: {}",e.getMessage(),e);
        } finally {
            vcontext.stop();
        }
    }

    @Override
    public void refresh() {
        final Timer.Context context = m_loadFullTimer.time();
        try {
            resetContainer();
            m_nodeToOnmsSnmpTable.clear();
            doRefresh();
        } finally {
            context.stop();
        }
        
        LOG.info("refresh: Found {} groups", getGroups().size());
        LOG.info("refresh: Found {} vertices", getVerticesWithoutGroups().size());
        LOG.info("refresh: Found {} edges", getEdges().size());
    }
    public CdpTopologyService getCdpTopologyService() {
        return m_cdpTopologyService;
    }
    public void setCdpTopologyService(CdpTopologyService cdpTopologyService) {
        m_cdpTopologyService = cdpTopologyService;
    }
    public LldpTopologyService getLldpTopologyService() {
        return m_lldpTopologyService;
    }
    public void setLldpTopologyService(LldpTopologyService lldpTopologyService) {
        m_lldpTopologyService = lldpTopologyService;
    }
    public OspfTopologyService getOspfTopologyService() {
        return m_ospfTopologyService;
    }
    public void setOspfTopologyService(OspfTopologyService ospfTopologyService) {
        m_ospfTopologyService = ospfTopologyService;
    }
    public IsisTopologyService getIsisTopologyService() {
        return m_isisTopologyService;
    }
    public void setIsisTopologyService(IsisTopologyService isisTopologyService) {
        m_isisTopologyService = isisTopologyService;
    }
    public NodeTopologyService getNodeTopologyService() {
        return m_nodeTopologyService;
    }
    public void setNodeTopologyService(NodeTopologyService nodeTopologyService) {
        m_nodeTopologyService = nodeTopologyService;
    }
    public BridgeTopologyService getBridgeTopologyService() {
        return m_bridgeTopologyService;
    }
    public void setBridgeTopologyService(BridgeTopologyService bridgeTopologyService) {
        m_bridgeTopologyService = bridgeTopologyService;
    }
    public SnmpInterfaceDao getSnmpInterfaceDao() {
        return m_snmpInterfaceDao;
    }
    public void setSnmpInterfaceDao(SnmpInterfaceDao snmpInterfaceDao) {
        m_snmpInterfaceDao = snmpInterfaceDao;
    }
}
