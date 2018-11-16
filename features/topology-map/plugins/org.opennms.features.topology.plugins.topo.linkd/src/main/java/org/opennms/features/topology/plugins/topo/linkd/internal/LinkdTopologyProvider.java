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

import java.net.InetAddress;
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
import org.opennms.netmgt.enlinkd.service.api.OspfTopologyService;
import org.opennms.netmgt.enlinkd.service.api.SharedSegment;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
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

    private Map<Integer, OnmsIpInterface> m_nodeToOnmsIpPrimaryMap =new HashMap<>();
    private Table<Integer, Integer,OnmsSnmpInterface> m_nodeToOnmsSnmpMap = HashBasedTable.create();
    private Map<String, Integer> m_macToNodeidMap = new HashMap<>();
    private Map<String, OnmsIpInterface> m_macToOnmsIpMap = new HashMap<>();
    private Map<String, OnmsSnmpInterface> m_macToOnmsSnmpMap = new HashMap<>();

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
        if(m_nodeToOnmsSnmpMap.contains(nodeid,ifindex) ) {
                return m_nodeToOnmsSnmpMap.get(nodeid,ifindex);
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
        Map<BridgePort,LinkdVertex> portToVertexMap = new HashMap<BridgePort, LinkdVertex>();
        for (BridgePort bp : segment.getBridgePortsOnSegment()) {
            LinkdVertex vertex = (LinkdVertex)getVertex(TOPOLOGY_NAMESPACE_LINKD, bp.getNodeId().toString());
            vertex.getProtocolSupported().add(ProtocolSupported.BRIDGE);
            portToVertexMap.put(bp,vertex);
        }
        
        Set<String> nomappedmacs = new HashSet<String>();
        Map<String,LinkdVertex> macToVertexMap = new HashMap<String, LinkdVertex>();
        for (String mac: segment.getMacsOnSegment()) {
           OnmsSnmpInterface targetsnmpIface = m_macToOnmsSnmpMap.get(mac);
           if (targetsnmpIface != null) {
               LinkdVertex vertex = (LinkdVertex)getVertex(TOPOLOGY_NAMESPACE_LINKD, targetsnmpIface.getNode().getNodeId());
               vertex.getProtocolSupported().add(ProtocolSupported.BRIDGE);
               macToVertexMap.put(mac,vertex);
               continue;
           }
           OnmsIpInterface targetipIface = m_macToOnmsIpMap.get(mac);
           if (targetipIface != null) {
               LinkdVertex vertex = (LinkdVertex)getVertex(TOPOLOGY_NAMESPACE_LINKD, targetipIface.getNode().getNodeId());
               vertex.getProtocolSupported().add(ProtocolSupported.BRIDGE);
               macToVertexMap.put(mac,vertex);
               continue;
           }
           if (m_macToNodeidMap.containsKey(mac)) {
               Integer nodeid = m_macToNodeidMap.get(mac);
               LinkdVertex vertex = (LinkdVertex)getVertex(TOPOLOGY_NAMESPACE_LINKD, nodeid.toString());
               vertex.getProtocolSupported().add(ProtocolSupported.BRIDGE);
               macToVertexMap.put(mac,vertex);
               continue;
           }
           nomappedmacs.add(mac);
        }
        
        if (portToVertexMap.size() == 2 && 
            segment.getMacsOnSegment().size() == 0) {
            LinkdVertex source = null;
            LinkdVertex target = null;
            BridgePort sourcebp = null;
            BridgePort targetbp = null;
            for (BridgePort bp: portToVertexMap.keySet()) {
                if (bp.getNodeId() == segment.getDesignatedBridge()) {
                    source = portToVertexMap.get(bp);
                    sourcebp = bp;
                    continue;
                } 
                target = portToVertexMap.get(bp);
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
        if (portToVertexMap.size() == 1 && 
                macToVertexMap.size() == 1 && 
                segment.getMacsOnSegment().size() == 1) {
            LinkdVertex source = portToVertexMap.values().iterator().next();
            LinkdVertex target = macToVertexMap.values().iterator().next();
            BridgePort sourcebp = portToVertexMap.keySet().iterator().next();
            String targetmac = macToVertexMap.keySet().iterator().next();
            
            OnmsSnmpInterface sourceinterface = getSnmpInterface(sourcebp.getNodeId(), sourcebp.getBridgePortIfIndex());
            OnmsSnmpInterface targetinterface = m_macToOnmsSnmpMap.get(targetmac);
            OnmsIpInterface targetipinterface = m_macToOnmsIpMap.get(targetmac);
            StringBuffer targetAddr = new StringBuffer();
            targetAddr.append(targetmac); 
            if (targetipinterface != null) {
                targetAddr.append(":");
                targetAddr.append(InetAddressUtils.str(targetipinterface.getIpAddress()));
            } else {
                targetAddr.append("Multiple ip addresses");                
            }
            connectVertices(getEdgeId(sourcebp, targetmac), source, target,sourceinterface,targetinterface,
                            "bp: "+sourcebp.getBridgePort(),
                            targetAddr.toString(),
                            ProtocolSupported.BRIDGE);
            return;
        }
        LinkdVertex topVertex = portToVertexMap.get(segment.getDesignatedPort());
        AbstractVertex cloudVertex = addVertex(getEdgeId(segment), 0, 0);
        if (nomappedmacs.size() > 0) {
            cloudVertex.setLabel("Multiple Mac Addresses");
        } else {
            cloudVertex.setLabel("");            
        }
        cloudVertex.setIconKey("cloud");
        cloudVertex.setTooltipText("'Shared Segment' with designated up bridge: " + topVertex.getLabel() + " port: " + segment.getDesignatedPort().getBridgePort());
        addVertices(cloudVertex);
        LOG.debug("parseSegment: adding cloud: id: '{}', {}", cloudVertex.getId(), segment.printTopology() );
        for (BridgePort bp: portToVertexMap.keySet()) {
            LinkdVertex bpportvertex = portToVertexMap.get(bp);
            OnmsSnmpInterface targetinterface = getSnmpInterface(bp.getNodeId(), bp.getBridgePortIfIndex());
            connectVertices(getEdgeId(cloudVertex, bp), cloudVertex, bpportvertex, null, targetinterface, 
                            "shared segment: up bridge " + topVertex.getLabel() + " bp:" + segment.getDesignatedPort().getBridgePort(),
                            "bp: "+bp.getBridgePort(), ProtocolSupported.BRIDGE);
            
        }
        for (String mac: macToVertexMap.keySet()) {
            LinkdVertex target = macToVertexMap.get(mac);
            OnmsSnmpInterface targetiface = m_macToOnmsSnmpMap.get(mac);
            OnmsIpInterface targetipinterface = m_macToOnmsIpMap.get(mac);
            StringBuffer targetAddr = new StringBuffer();
            targetAddr.append(mac); 
            if (targetipinterface != null) {
                targetAddr.append(":");
                targetAddr.append(InetAddressUtils.str(targetipinterface.getIpAddress()));
            } else {
                targetAddr.append("Multiple ip addresses");                
            }
            connectVertices(getEdgeId(cloudVertex, mac), cloudVertex,target, null, 
                            targetiface,
                            "shared segment: up bridge " + topVertex.getLabel() + " bp:" + segment.getDesignatedPort().getBridgePort(),
                            targetAddr.toString(), ProtocolSupported.BRIDGE);
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
        Map<InetAddress, OnmsIpInterface>  ipToOnmsIpMap = new HashMap<InetAddress, OnmsIpInterface>();
        Set<InetAddress> duplicated = new HashSet<InetAddress>();
        try {
            for (OnmsIpInterface ip: m_ipInterfaceDao.findAll()) {
                if (ip.getIsSnmpPrimary().equals(PrimaryType.PRIMARY)) {
                    m_nodeToOnmsIpPrimaryMap.put(ip.getNode().getId(), ip);
                } else {
                    m_nodeToOnmsIpPrimaryMap.putIfAbsent(ip.getNode().getId(), ip);
                }

                if (!ipToOnmsIpMap.containsKey(ip.getIpAddress())) {
                    ipToOnmsIpMap.put(ip.getIpAddress(), ip);
                } else {
                    duplicated.add(ip.getIpAddress());
                    LOG.debug("refresh: found duplicated ip {}", InetAddressUtils.str(ip.getIpAddress()));
                }
            }
            for (InetAddress ipdup: duplicated) {
                ipToOnmsIpMap.remove(ipdup);
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
                if (!m_nodeToOnmsSnmpMap.contains(snmp.getNode().getId(),snmp.getIfIndex())) {
                    m_nodeToOnmsSnmpMap.put(snmp.getNode().getId(),snmp.getIfIndex(),snmp);
                }
            }
            LOG.info("refresh: Snmp Interface loaded");
        } catch (Exception e){
            LOG.error("Loading Snmp Interface failed: {}",e.getMessage(),e);
        } finally {
            vcontext.stop();
        }

        vcontext = m_loadIpNetToMediaTimer.time();

        // mac -> ip[]  ->  snmp[]    —> macToNodeMap
        // mac -> ip    ->  snmp      —> macToNodeMap, macToIpMap, macToSnmpMap
        // mac -> ip    ->  no snmp   —> macToNodeMap, macToIpMap
        // mac -> ip[]  ->  no snmp   —> macToNodeMap
        // mac -> ip[]  ->  snmp      —> macToSnmpMap
        Map<InetAddress, String> iptoMacMap = m_ipNetToMediaTopologyService.getIpMacMap();
        try {
            for (InetAddress ipAddr: iptoMacMap.keySet()) {
                OnmsIpInterface onmsip = ipToOnmsIpMap.get(ipAddr);
                String mac = iptoMacMap.get(ipAddr);
                if (onmsip == null) {
                    LOG.debug("refresh: ipNetToMedia: {}:{}. No OnmsIpInterface found.", mac,InetAddressUtils.str(ipAddr));
                    continue;
                }
                LOG.debug("refresh: ipNetToMedia: {}:{}. OnmsIpInterface found node:[{}].", mac,
                          InetAddressUtils.str(ipAddr),onmsip.getNodeId());

                if (!m_macToNodeidMap.containsKey(mac)) {
                    m_macToNodeidMap.put(mac, onmsip.getNodeId());
                }

                if (!m_macToOnmsIpMap.containsKey(mac)) {
                    m_macToOnmsIpMap.put(mac, onmsip);
                } else {
                    LOG.debug("refresh: ipNetToMedia: {}:{}. Multiple OnmsIpInterface found.", mac,InetAddressUtils.str(ipAddr));
                }
                if (m_nodeToOnmsSnmpMap.containsRow(onmsip.getNodeId())) {
                    for (OnmsSnmpInterface onmssnmp : m_nodeToOnmsSnmpMap.row(onmsip.getNodeId()).values() ) {
                        if (!m_macToOnmsSnmpMap.containsKey(mac)) {
                            m_macToOnmsSnmpMap.put(mac, onmssnmp);
                        } else if (m_macToOnmsSnmpMap.get(mac).getId().intValue() == onmssnmp.getId() ) {
                            continue;
                        } else {
                            LOG.debug("refresh: ipNetToMedia: {}:{}. Multiple OnmsSnmpInterface found.", mac,InetAddressUtils.str(ipAddr));                                
                        }
                    }
                } else {
                    LOG.debug("refresh: ipNetToMedia: {}:{}. No OnmsSnmpInterface found.", mac,InetAddressUtils.str(ipAddr));
                }
            }
            LOG.info("refresh: IpNetToMedia loaded");
        } catch (Exception e){
            LOG.error("Loading ipNetToMedia failed: {}",e.getMessage(),e);
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
            m_nodeToOnmsSnmpMap.clear();
            m_nodeToOnmsIpPrimaryMap.clear();
            m_macToNodeidMap.clear();
            m_macToOnmsIpMap.clear();
            m_macToOnmsSnmpMap.clear();
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
