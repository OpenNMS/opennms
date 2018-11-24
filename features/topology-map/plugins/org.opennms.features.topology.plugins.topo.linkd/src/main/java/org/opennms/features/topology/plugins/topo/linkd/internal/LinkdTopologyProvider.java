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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
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
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.enlinkd.model.CdpLink;
import org.opennms.netmgt.enlinkd.model.IsIsLink;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.enlinkd.model.OspfLink;
import org.opennms.netmgt.enlinkd.service.api.BridgePort;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyException;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.BroadcastDomain;
import org.opennms.netmgt.enlinkd.service.api.CdpTopologyService;
import org.opennms.netmgt.enlinkd.service.api.IpNetToMediaTopologyService;
import org.opennms.netmgt.enlinkd.service.api.IsisTopologyService;
import org.opennms.netmgt.enlinkd.service.api.LldpTopologyService;
import org.opennms.netmgt.enlinkd.service.api.MacPort;
import org.opennms.netmgt.enlinkd.service.api.OspfTopologyService;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.enlinkd.service.api.SharedSegment;
import org.opennms.netmgt.model.FilterManager;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PrimaryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionOperations;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Lists;
import com.google.common.collect.Table;

public class LinkdTopologyProvider extends AbstractTopologyProvider implements GraphProvider {

    static final String getEdgeId(AbstractVertex vertex, BridgePort bp) {
        return vertex.getId() + "|" + bp.getNodeId() + ":" + bp.getBridgePort();
    }
    static final String getEdgeId(AbstractVertex vertex, String mac) {
        return vertex.getId() + "|" + mac;
    }

    static final String getEdgeId(SharedSegment segment) throws BridgeTopologyException {
        return  segment.getDesignatedBridge()+":"+segment.getDesignatedPort().getBridgePort();
    }
    
    static final String getDefaultEdgeId(int sourceId,int targetId) {
        return Math.min(sourceId, targetId) + "|" + Math.max(sourceId, targetId);
    }
    
    static final String getEdgeId(BridgePort sourcebp, BridgePort targetbp ) {
        if (sourcebp.getNodeId().intValue() < targetbp.getNodeId().intValue()) {
            return sourcebp.getNodeId()+":"+sourcebp.getBridgePort()+"|"+targetbp.getNodeId()+":"+targetbp.getBridgePort();
        }
        return  targetbp.getNodeId()+":"+targetbp.getBridgePort()+"|"+sourcebp.getNodeId()+":"+sourcebp.getBridgePort();
    }

    static final String getEdgeId(BridgePort sourcebp, String targetmac ) {
            return sourcebp.getNodeId()+":"+sourcebp.getBridgePort()+"|"+targetmac;
    }

    private static Logger LOG = LoggerFactory.getLogger(LinkdTopologyProvider.class);

    private TransactionOperations m_transactionOperations;
    private NodeDao m_nodeDao;
    private SnmpInterfaceDao m_snmpInterfaceDao;
    private IpInterfaceDao m_ipInterfaceDao;
    private FilterManager m_filterManager;

    private BridgeTopologyService m_bridgeTopologyService;
    private CdpTopologyService m_cdpTopologyService;
    private LldpTopologyService m_lldpTopologyService;
    private OspfTopologyService m_ospfTopologyService;
    private IsisTopologyService m_isisTopologyService;
    private IpNetToMediaTopologyService m_ipNetToMediaTopologyService;

    Map<String,MacPort> m_macPorts;

    private Map<Integer, OnmsIpInterface> m_nodeToOnmsIpPrimaryMap =new HashMap<>();
    private Table<Integer, Integer,OnmsSnmpInterface> m_nodeToOnmsSnmpTable = HashBasedTable.create();

    private final Timer m_loadFullTimer;
    private final Timer m_loadIpInterfacesTimer;
    private final Timer m_loadSnmpInterfacesTimer;
    private final Timer m_loadIpNetToMediaTimer;
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
        m_loadIpInterfacesTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "ipinterfaces"));
        m_loadSnmpInterfacesTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "snmpinterfaces"));
        m_loadIpNetToMediaTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "ipnettomedia"));
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
        OnmsSnmpInterface snmpiface = new OnmsSnmpInterface();
        OnmsNode node = new OnmsNode();
        node.setId(nodeid);
        snmpiface.setNode(node);
        snmpiface.setIfIndex(ifindex);
        snmpiface.setIfName("No Interface Found");
        return snmpiface;
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
    
    private void loadVertices() {
        for (OnmsNode node : m_nodeDao.findAll()) {
            OnmsIpInterface primary = m_nodeToOnmsIpPrimaryMap.get(node.getId());
            addVertices(LinkdVertex.create(node,primary));
        }
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

        context = m_loadIpNetToMediaTimer.time();
        try { 
            m_ipNetToMediaTopologyService.getMacPorts().stream().forEach( mp -> {
               mp.getMacPortMap().keySet().stream().forEach( m -> m_macPorts.put(m, mp)); 
            });            
            LOG.info("loadEdges: IpNetToMedia loaded");
        } catch (Exception e){
            LOG.error("Loading ipNetToMedia failed: {}",e.getMessage(),e);
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

        for (Pair<LldpLink, LldpLink> pair : m_lldpTopologyService.matchLldpLinks()) {
            LldpLink sourceLink = pair.getLeft();
            LldpLink targetLink = pair.getRight();
            LinkdVertex source = (LinkdVertex) getVertex(TOPOLOGY_NAMESPACE_LINKD, sourceLink.getNode().getNodeId());
            source.getProtocolSupported().add(ProtocolSupported.LLDP);
            LinkdVertex target = (LinkdVertex) getVertex(TOPOLOGY_NAMESPACE_LINKD, targetLink.getNode().getNodeId());
            target.getProtocolSupported().add(ProtocolSupported.LLDP);
            OnmsSnmpInterface sourceSnmpInterface = getSnmpInterface(sourceLink.getNode().getId(), sourceLink.getLldpPortIfindex());
            OnmsSnmpInterface targetSnmpInterface = getSnmpInterface(targetLink.getNode().getId(), targetLink.getLldpPortIfindex());
            connectVertices(getDefaultEdgeId(sourceLink.getId(), targetLink.getId()),
                    source,target,
                    sourceSnmpInterface,targetSnmpInterface,
                    sourceLink.getLldpPortDescr(),targetLink.getLldpPortDescr(),
                    ProtocolSupported.LLDP);
        }
    }

    private void getOspfLinks() {

        for (Pair<OspfLink, OspfLink> pair : m_ospfTopologyService.matchOspfLinks()) {
            OspfLink sourceLink = pair.getLeft();
            OspfLink targetLink = pair.getRight();

            LinkdVertex source = (LinkdVertex)getVertex(TOPOLOGY_NAMESPACE_LINKD, sourceLink.getNode().getNodeId());
            source.getProtocolSupported().add(ProtocolSupported.OSPF);
            LinkdVertex target = (LinkdVertex)getVertex(TOPOLOGY_NAMESPACE_LINKD, targetLink.getNode().getNodeId());
            target.getProtocolSupported().add(ProtocolSupported.OSPF);
            OnmsSnmpInterface sourceSnmpInterface = getSnmpInterface(sourceLink.getNode().getId(), sourceLink.getOspfIfIndex());
            OnmsSnmpInterface targetSnmpInterface = getSnmpInterface(targetLink.getNode().getId(), targetLink.getOspfIfIndex());
            connectVertices(getDefaultEdgeId(sourceLink.getId(), targetLink.getId()),
                    source,target,
                    sourceSnmpInterface,targetSnmpInterface,
                    InetAddressUtils.str(targetLink.getOspfRemIpAddr()),
                    InetAddressUtils.str(sourceLink.getOspfRemIpAddr()),
                    ProtocolSupported.OSPF);
        }
    }

    private void getCdpLinks() {

        for(Pair<CdpLink, CdpLink> pair : m_cdpTopologyService.matchCdpLinks()) {
            CdpLink sourceLink = pair.getLeft();
            CdpLink targetLink = pair.getRight();
            LinkdVertex source = (LinkdVertex) getVertex(TOPOLOGY_NAMESPACE_LINKD, sourceLink.getNode().getNodeId());
            source.getProtocolSupported().add(ProtocolSupported.CDP);
            LinkdVertex target = (LinkdVertex) getVertex(TOPOLOGY_NAMESPACE_LINKD, targetLink.getNode().getNodeId());
            target.getProtocolSupported().add(ProtocolSupported.CDP);
            OnmsSnmpInterface sourceSnmpInterface = getSnmpInterface(sourceLink.getNode().getId(), sourceLink.getCdpCacheIfIndex());
            OnmsSnmpInterface targetSnmpInterface = getSnmpInterface(targetLink.getNode().getId(), targetLink.getCdpCacheIfIndex());
            connectVertices(getDefaultEdgeId(sourceLink.getId(), targetLink.getId()),
                source, target,
                sourceSnmpInterface, targetSnmpInterface,
                targetLink.getCdpCacheAddress(),
                sourceLink.getCdpCacheAddress(),
                ProtocolSupported.CDP);
        }
    }

    private void getIsIsLinks() {

        for(Pair<IsIsLink, IsIsLink> pair : m_isisTopologyService.matchIsIsLinks()) {
            IsIsLink sourceLink = pair.getLeft();
            IsIsLink targetLink = pair.getRight();
            LinkdVertex source = (LinkdVertex) getVertex(TOPOLOGY_NAMESPACE_LINKD, sourceLink.getNode().getNodeId());
            source.getProtocolSupported().add(ProtocolSupported.ISIS);
            LinkdVertex target = (LinkdVertex) getVertex(TOPOLOGY_NAMESPACE_LINKD, targetLink.getNode().getNodeId());
            target.getProtocolSupported().add(ProtocolSupported.ISIS);
            OnmsSnmpInterface sourceSnmpInterface = getSnmpInterface(sourceLink.getNode().getId(), sourceLink.getIsisCircIfIndex());
            OnmsSnmpInterface targetSnmpInterface = getSnmpInterface(targetLink.getNode().getId(), targetLink.getIsisCircIfIndex());
            connectVertices(getDefaultEdgeId(sourceLink.getId(), targetLink.getId()),
                source, target,
                sourceSnmpInterface, targetSnmpInterface,
                targetLink.getIsisISAdjNeighSNPAAddress(),
                sourceLink.getIsisISAdjNeighSNPAAddress(),
                ProtocolSupported.ISIS);
        }
    }

    
    private void getBridgeLinks() throws BridgeTopologyException {
        for (BroadcastDomain domain: m_bridgeTopologyService.findAll()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("getBridgeLinks:\n {}", domain.printTopology());
            }
            parseDomain(domain);
        }
    }
    
    private void parseDomain(BroadcastDomain domain) throws BridgeTopologyException {
        for (SharedSegment segment: domain.getSharedSegments()) {
            LOG.debug("parseDomain: \n{}", segment.printTopology());
            parseSegment(segment);
        }
    }
    
    private void parseSegment(SharedSegment segment) throws BridgeTopologyException {
        Map<BridgePort,LinkdVertex> portToNodeVertexMap = new HashMap<BridgePort, LinkdVertex>();
        for (BridgePort bp : segment.getBridgePortsOnSegment()) {
            LinkdVertex vertex = (LinkdVertex)getVertex(TOPOLOGY_NAMESPACE_LINKD, bp.getNodeId().toString());
            vertex.getProtocolSupported().add(ProtocolSupported.BRIDGE);
            portToNodeVertexMap.put(bp,vertex);
        }
        
        Set<String> noMappedToIpMacs = new HashSet<String>();
        Set<MacPort> noNodeMacPorts = new HashSet<MacPort>();
        Map<MacPort,LinkdVertex> macPortToNodeVertexMap = new HashMap<MacPort, LinkdVertex>();
        for (String mac: segment.getMacsOnSegment()) {
           MacPort port = m_macPorts.get(mac);
           if (port == null) {
                noMappedToIpMacs.add(mac);
                continue;
           }
           if (port.getNodeId() != null) {
               LinkdVertex vertex = (LinkdVertex)getVertex(TOPOLOGY_NAMESPACE_LINKD, port.getNodeId().toString());
               vertex.getProtocolSupported().add(ProtocolSupported.BRIDGE);
               macPortToNodeVertexMap.put(port,vertex);
               continue;
           }
           noNodeMacPorts.add(port);
        }
        
        if (portToNodeVertexMap.size() == 2 && 
            segment.getMacsOnSegment().size() == 0) {
            LinkdVertex source = null;
            LinkdVertex target = null;
            BridgePort sourcebp = null;
            BridgePort targetbp = null;
            for (BridgePort bp: portToNodeVertexMap.keySet()) {
                if (bp.getNodeId() == segment.getDesignatedBridge()) {
                    source = portToNodeVertexMap.get(bp);
                    sourcebp = bp;
                    continue;
                } 
                target = portToNodeVertexMap.get(bp);
                targetbp=bp;
            }
            OnmsSnmpInterface sourceSnmpInterface = getSnmpInterface(sourcebp.getNodeId(), sourcebp.getBridgePortIfIndex());
            OnmsSnmpInterface targetSnmpInterface = getSnmpInterface(targetbp.getNodeId(), targetbp.getBridgePortIfIndex());
            connectVertices(getEdgeId(sourcebp,targetbp),
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
            connectVertices(getEdgeId(sourceBridgePort, targetMacPort.getMacPortMap().keySet().toString()), sourceVertex, targetVertex,sourceinterface,targetinterface,
                            "bp: "+sourceBridgePort.getBridgePort(),
                            targetMacPort.toString(),
                            ProtocolSupported.BRIDGE);
            return;
        }
        
        LinkdVertex topVertex = portToNodeVertexMap.get(segment.getDesignatedPort());
        AbstractVertex cloudVertex = addVertex(getEdgeId(segment), 0, 0);
        cloudVertex.setLabel("Shared Segment");
        cloudVertex.setIconKey("cloud");
        cloudVertex.setTooltipText("'Shared Segment' designated port: " + segment.getDesignatedPort().printTopology());
        LOG.debug("parseSegment: adding cloud: id: '{}', {}", cloudVertex.getId(), segment.printTopology() );

        for (BridgePort bp: portToNodeVertexMap.keySet()) {
            LinkdVertex bpportvertex = portToNodeVertexMap.get(bp);
            OnmsSnmpInterface targetinterface = getSnmpInterface(bp.getNodeId(), bp.getBridgePortIfIndex());
            connectVertices(getEdgeId(cloudVertex, bp), cloudVertex, bpportvertex, null, targetinterface, 
                            "shared segment: up bridge " + topVertex.getLabel() + " bp:" + segment.getDesignatedPort().getBridgePort(),
                            "bp: "+bp.getBridgePort(), ProtocolSupported.BRIDGE);
            
        }
        for (MacPort targetMacPort: macPortToNodeVertexMap.keySet()) {
            LinkdVertex target = macPortToNodeVertexMap.get(targetMacPort);
            OnmsSnmpInterface targetinterface = getSnmpInterface(targetMacPort.getNodeId(),targetMacPort.getMacPortIfIndex());
            connectVertices(getEdgeId(cloudVertex, targetMacPort.getMacPortMap().keySet().toString()), cloudVertex,target, null, 
                            targetinterface,
                            "shared segment: up bridge " + topVertex.getLabel() + " bp:" + segment.getDesignatedPort().getBridgePort(),
                            targetMacPort.printTopology(), ProtocolSupported.BRIDGE);
        }
        
        for (MacPort targetMacPort: noNodeMacPorts) {
            AbstractVertex macPortVertex = addVertex(targetMacPort.getMacPortMap().toString(), 0, 0);
            macPortVertex.setLabel(targetMacPort.getIpMacInfo()+"....");
            macPortVertex.setIconKey("generic");
            macPortVertex.setTooltipText(targetMacPort.printTopology());
            connectVertices(getEdgeId(cloudVertex, targetMacPort.getMacPortMap().keySet().toString()), cloudVertex,macPortVertex, null, 
                            null,
                            "shared segment: up bridge " + topVertex.getLabel() + " bp:" + segment.getDesignatedPort().getBridgePort(),
                            targetMacPort.printTopology(), ProtocolSupported.BRIDGE);
        }
        
        if (noMappedToIpMacs.size() > 0) {
            AbstractVertex macPortVertex = addVertex(noMappedToIpMacs.toString(), 0, 0);
            macPortVertex.setLabel(noMappedToIpMacs.iterator().next()+ "....");
            macPortVertex.setIconKey("generic");
            macPortVertex.setTooltipText("Shared Segment macs: " + noMappedToIpMacs.toString());
            connectVertices(getEdgeId(cloudVertex, noMappedToIpMacs.toString()), cloudVertex,macPortVertex, null, 
                            null,
                            "shared segment: up bridge " + topVertex.getLabel() + " bp:" + segment.getDesignatedPort().getBridgePort(),
                            noMappedToIpMacs.toString(), ProtocolSupported.BRIDGE);
            
        }
    }

    public TransactionOperations getTransactionOperations() {
        return m_transactionOperations;
    }

    public void setTransactionOperations(TransactionOperations transactionOperations) {
        m_transactionOperations = transactionOperations;
    }

    public SnmpInterfaceDao getSnmpInterfaceDao() {
        return m_snmpInterfaceDao;
    }

    public void setSnmpInterfaceDao(SnmpInterfaceDao snmpInterfaceDao) {
        m_snmpInterfaceDao = snmpInterfaceDao;
    }

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }

    public void setFilterManager(FilterManager filterManager) {
        m_filterManager = filterManager;
    }

    public FilterManager getFilterManager() {
        return m_filterManager;
    }

    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }

    public BridgeTopologyService getBridgeTopologyService() {
        return m_bridgeTopologyService;
    }

    public void setBridgeTopologyService(BridgeTopologyService bridgeTopologyService) {
        m_bridgeTopologyService = bridgeTopologyService;
    }

        
    @Override
    public Defaults getDefaults() {
        return new Defaults()
                .withSemanticZoomLevel(Defaults.DEFAULT_SEMANTIC_ZOOM_LEVEL)
                .withPreferredLayout("D3 Layout") // D3 Layout
                .withCriteria(() -> {
                    final OnmsNode node = m_nodeDao.getDefaultFocusPoint();

                    if (node != null) {
                        final Vertex defaultVertex = getVertex(TOPOLOGY_NAMESPACE_LINKD, node.getNodeId());
                        if (defaultVertex != null) {
                            return Lists.newArrayList(LinkdHopCriteria.createCriteria(node.getNodeId(), node.getLabel()));
                        }
                    }
                    return Lists.newArrayList();
                });
    }
    
    private void doRefresh() {        
        Timer.Context vcontext = m_loadIpInterfacesTimer.time();
        try {
            for (OnmsIpInterface ip: m_ipInterfaceDao.findAll()) {
                if (ip.getIsSnmpPrimary().equals(PrimaryType.PRIMARY)) {
                    m_nodeToOnmsIpPrimaryMap.put(ip.getNode().getId(), ip);
                } else {
                    m_nodeToOnmsIpPrimaryMap.putIfAbsent(ip.getNode().getId(), ip);
                }

            }
            LOG.info("refresh: Ip Interface loaded");
        } catch (Exception e){
            LOG.error("Loading Ip Interface failed: {}", e.getMessage(), e);
        } finally {
            vcontext.stop();
        }

        vcontext = m_loadSnmpInterfacesTimer.time();
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
            loadVertices();
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
            m_nodeToOnmsIpPrimaryMap.clear();
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
    public IpNetToMediaTopologyService getIpNetToMediaTopologyService() {
        return m_ipNetToMediaTopologyService;
    }
    public void setIpNetToMediaTopologyService(
            IpNetToMediaTopologyService ipNetToMediaTopologyService) {
        m_ipNetToMediaTopologyService = ipNetToMediaTopologyService;
    }
}
