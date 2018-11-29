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
import java.util.ArrayList;
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
import org.opennms.netmgt.enlinkd.persistence.api.TopologyEntityCache;
import org.opennms.netmgt.enlinkd.model.CdpElement;
import org.opennms.netmgt.enlinkd.model.IpNetToMedia;
import org.opennms.netmgt.enlinkd.model.IsIsElement;
import org.opennms.netmgt.enlinkd.model.IsIsLink;
import org.opennms.netmgt.enlinkd.model.LldpElement;
import org.opennms.netmgt.enlinkd.model.LldpLink;
import org.opennms.netmgt.enlinkd.model.OspfLink;
import org.opennms.netmgt.enlinkd.persistence.api.CdpElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.IpNetToMediaDao;
import org.opennms.netmgt.enlinkd.persistence.api.IsIsElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.IsIsLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.LldpElementDao;
import org.opennms.netmgt.enlinkd.persistence.api.LldpLinkDao;
import org.opennms.netmgt.enlinkd.persistence.api.OspfLinkDao;
import org.opennms.netmgt.enlinkd.service.api.BridgePort;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyException;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.BroadcastDomain;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.enlinkd.service.api.SharedSegment;
import org.opennms.netmgt.enlinkd.model.CdpLinkTopologyEntity;
import org.opennms.netmgt.model.FilterManager;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.PrimaryType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionOperations;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.Lists;

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
    private TopologyEntityCache m_topologyEntityCache;
    private SnmpInterfaceDao m_snmpInterfaceDao;
    private IpInterfaceDao m_ipInterfaceDao;
    private FilterManager m_filterManager;

    private LldpLinkDao m_lldpLinkDao;
    private LldpElementDao m_lldpElementDao;
    private CdpElementDao m_cdpElementDao;
    private OspfLinkDao m_ospfLinkDao;
    private IsIsLinkDao m_isisLinkDao;
    private IsIsElementDao m_isisElementDao;
    private BridgeTopologyService m_bridgeTopologyService;
    private IpNetToMediaDao m_ipNetToMediaDao;

    private Map<Integer, OnmsIpInterface> m_nodeToOnmsIpPrimaryMap =new HashMap<>();
    private Map<Integer, Map<Integer,OnmsSnmpInterface>> m_nodeToOnmsSnmpMap = new HashMap<>();
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
        if(m_nodeToOnmsSnmpMap.containsKey(nodeid)) {
            if (m_nodeToOnmsSnmpMap.get(nodeid).containsKey(ifindex)) {
                return m_nodeToOnmsSnmpMap.get(nodeid).get(ifindex);
            }
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
        for (NodeTopologyEntity vertex : m_topologyEntityCache.getNodeTopolgyEntities()) {
            OnmsIpInterface primary = m_nodeToOnmsIpPrimaryMap.get(vertex.getId());
            addVertices(LinkdVertex.create(vertex,primary));
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

        List<LldpLink> allLinks = m_lldpLinkDao.findAll();
        // Index the LLDP elements by node id
        Map<Integer, LldpElement> nodelldpelementidMap = new HashMap<Integer, LldpElement>();
        Map<Integer, LinkdVertex> nodeVertexMap = new HashMap<Integer, LinkdVertex>();
        for (LldpElement lldpelement: m_lldpElementDao.findAll()) {
            nodelldpelementidMap.put(lldpelement.getNode().getId(), lldpelement);
            LinkdVertex vertex = (LinkdVertex)getVertex(TOPOLOGY_NAMESPACE_LINKD, lldpelement.getNode().getNodeId());
            vertex.getProtocolSupported().add(ProtocolSupported.LLDP);
            nodeVertexMap.put(lldpelement.getNode().getId(), vertex);
            System.err.println(vertex.getId());
        }

        List<Pair<LldpLink, LldpLink>> matchedLinks = matchLldpLinks(nodelldpelementidMap, allLinks);

        for (Pair<LldpLink, LldpLink> pair : matchedLinks) {
            LldpLink sourceLink = pair.getLeft();
            LldpLink targetLink = pair.getRight();
            LinkdVertex source = nodeVertexMap.get(sourceLink.getNode().getId());
            LinkdVertex target = nodeVertexMap.get(targetLink.getNode().getId());
            OnmsSnmpInterface sourceSnmpInterface = getSnmpInterface(sourceLink.getNode().getId(), sourceLink.getLldpPortIfindex());
            OnmsSnmpInterface targetSnmpInterface = getSnmpInterface(targetLink.getNode().getId(), targetLink.getLldpPortIfindex());
            connectVertices(getDefaultEdgeId(sourceLink.getId(), targetLink.getId()),
                    source,target,
                    sourceSnmpInterface,targetSnmpInterface,
                    sourceLink.getLldpPortDescr(),targetLink.getLldpPortDescr(),
                    ProtocolSupported.LLDP);
        }
    }

    List<Pair<LldpLink, LldpLink>> matchLldpLinks(Map<Integer, LldpElement> nodelldpelementidMap, List<LldpLink> allLinks) {
        List<Pair<LldpLink, LldpLink>> results = new ArrayList<>();

        // 1.) create mapping
        Map<CompositeKey, LldpLink> targetLinkMap = new HashMap<>();
        for(LldpLink targetLink : allLinks){

            CompositeKey key = new CompositeKey(
                    targetLink.getLldpRemChassisId(),
                    nodelldpelementidMap.get(targetLink.getNode().getId()).getLldpChassisId(),
                    targetLink.getLldpPortId(),
                    targetLink.getLldpPortIdSubType(),
                    targetLink.getLldpRemPortId(),
                    targetLink.getLldpRemPortIdSubType());
            targetLinkMap.put(key, targetLink);
        }

        // 2.) iterate
        Set<Integer> parsed = new HashSet<Integer>();
        for (LldpLink sourceLink : allLinks) {
            if (parsed.contains(sourceLink.getId())) {
                continue;
            }
            String sourceLldpChassisId = nodelldpelementidMap.get(sourceLink.getNode().getId()).getLldpChassisId();
            if (sourceLldpChassisId.equals(sourceLink.getLldpRemChassisId())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getLldpLinks: self link not adding source: {}",sourceLink.toString());
                }
                parsed.add(sourceLink.getId());
                continue;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("getLldpLinks: source: {}",sourceLink.toString());
            }

            CompositeKey key = new CompositeKey(
                    nodelldpelementidMap.get(sourceLink.getNode().getId()).getLldpChassisId(),
                    sourceLink.getLldpRemChassisId(),
                    sourceLink.getLldpRemPortId(),
                    sourceLink.getLldpRemPortIdSubType(),
                    sourceLink.getLldpPortId(),
                    sourceLink.getLldpPortIdSubType());
            LldpLink targetLink = targetLinkMap.get(key);

            if (targetLink == null) {
                LOG.debug("getLldpLinks: cannot found target for source: '{}'", sourceLink.getId());
                continue;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("getLldpLinks: lldp: {} target: {}", sourceLink.getLldpRemChassisId(), targetLink.toString());
            }

            parsed.add(sourceLink.getId());
            parsed.add(targetLink.getId());
            results.add(Pair.of(sourceLink, targetLink));
        }
        return results;
    }

    private void getOspfLinks() {

        List<OspfLink> allLinks = getOspfLinkDao().findAll();
        List<Pair<OspfLink, OspfLink>> matchedLinks = matchOspfLinks(allLinks);

        for (Pair<OspfLink, OspfLink> pair : matchedLinks) {
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

    List<Pair<OspfLink, OspfLink>> matchOspfLinks(List<OspfLink> allLinks){
        List<Pair<OspfLink, OspfLink>> results = new ArrayList<>();
        Set<Integer> parsed = new HashSet<Integer>();

        // build mapping:
        Map<CompositeKey, OspfLink> targetLinks = new HashMap<>();
        for(OspfLink targetLink : allLinks){
            targetLinks.put(new CompositeKey(targetLink.getOspfIpAddr(), targetLink.getOspfRemIpAddr()) , targetLink);
        }

        for(OspfLink sourceLink : allLinks) {
            if (parsed.contains(sourceLink.getId())) {
                continue;
            }
            parsed.add(sourceLink.getId());
            if (LOG.isDebugEnabled()) {
                LOG.debug("getOspfLinks: source: {}", sourceLink.toString());
            }
            OspfLink targetLink = targetLinks.get(new CompositeKey(sourceLink.getOspfRemIpAddr() , sourceLink.getOspfIpAddr()));
            if(targetLink == null) {
                LOG.debug("getOspfLinks: cannot find target for source: '{}'", sourceLink.getId());
                continue;
            }

            if (sourceLink.getId().equals(targetLink.getId()) || parsed.contains(targetLink.getId())) {
                    continue;
            }

            LOG.debug("getOspfLinks: target: {}", targetLink.toString());
            parsed.add(targetLink.getId());
           results.add(Pair.of(sourceLink, targetLink));
        }
        return results;
    }




    private void getCdpLinks() {
        List<CdpElement> cdpElements = m_cdpElementDao.findAll();
        List<CdpLinkTopologyEntity> allLinks = m_topologyEntityCache.getCdpLinkTopologyEntities();
        List<Pair<CdpLinkTopologyEntity, CdpLinkTopologyEntity>> matchedCdpLinks = matchCdpLinks(cdpElements, allLinks);
        for (Pair<CdpLinkTopologyEntity, CdpLinkTopologyEntity> pair : matchedCdpLinks) {
            connectCdpLinkPair(pair);
        }
    }

    private void connectCdpLinkPair(Pair<CdpLinkTopologyEntity, CdpLinkTopologyEntity> pair){
        CdpLinkTopologyEntity sourceLink = pair.getLeft();
        CdpLinkTopologyEntity targetLink = pair.getRight();
        LinkdVertex source = (LinkdVertex) getVertex(TOPOLOGY_NAMESPACE_LINKD, sourceLink.getNodeIdAsString());
        source.getProtocolSupported().add(ProtocolSupported.CDP);
        LinkdVertex target = (LinkdVertex) getVertex(TOPOLOGY_NAMESPACE_LINKD, targetLink.getNodeIdAsString());
        target.getProtocolSupported().add(ProtocolSupported.CDP);
        OnmsSnmpInterface sourceSnmpInterface = getSnmpInterface(sourceLink.getNodeId(), sourceLink.getCdpCacheIfIndex());
        OnmsSnmpInterface targetSnmpInterface = getSnmpInterface(targetLink.getNodeId(), targetLink.getCdpCacheIfIndex());
        connectVertices(getDefaultEdgeId(sourceLink.getId(), targetLink.getId()),
                source, target,
                sourceSnmpInterface, targetSnmpInterface,
                targetLink.getCdpCacheAddress(),
                sourceLink.getCdpCacheAddress(),
                ProtocolSupported.CDP);
    }

    List<Pair<CdpLinkTopologyEntity, CdpLinkTopologyEntity>> matchCdpLinks(final List<CdpElement> cdpElements, final List<CdpLinkTopologyEntity> allLinks) {

        // 1. create lookup maps:
        Map<Integer, CdpElement> cdpelementmap = new HashMap<Integer, CdpElement>();
        for (CdpElement cdpelement: cdpElements) {
            cdpelementmap.put(cdpelement.getNode().getId(), cdpelement);
        }
        Map<CompositeKey, CdpLinkTopologyEntity> targetLinkMap = new HashMap<>();
        for (CdpLinkTopologyEntity targetLink : allLinks) {
            CompositeKey key = new CompositeKey(targetLink.getCdpCacheDevicePort(),
                    targetLink.getCdpInterfaceName(),
                    cdpelementmap.get(targetLink.getNodeId()).getCdpGlobalDeviceId(),
                    targetLink.getCdpCacheDeviceId());
            targetLinkMap.put(key, targetLink);
        }
        Set<Integer> parsed = new HashSet<Integer>();

        // 2. iterate
        List<Pair<CdpLinkTopologyEntity, CdpLinkTopologyEntity>> results = new ArrayList<>();
        for (CdpLinkTopologyEntity sourceLink : allLinks) {
            if (parsed.contains(sourceLink.getId())) {
                continue;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("getCdpLinks: source: {} ", sourceLink.toString());
            }
            CdpElement sourceCdpElement = cdpelementmap.get(sourceLink.getNodeId());

            CdpLinkTopologyEntity targetLink = targetLinkMap.get(new CompositeKey(sourceLink.getCdpInterfaceName(),
                    sourceLink.getCdpCacheDevicePort(),
                    sourceLink.getCdpCacheDeviceId(),
                    sourceCdpElement.getCdpGlobalDeviceId()));

            if (targetLink == null) {
                LOG.debug("getCdpLinks: cannot found target for source: '{}'", sourceLink.getId());
                continue;
            }

            if (sourceLink.getId().equals(targetLink.getId()) || parsed.contains(targetLink.getId())) {
                continue;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("getCdpLinks: cdp: {}, target: {} ", sourceLink.getCdpCacheDevicePort(), targetLink.toString());
            }

            parsed.add(sourceLink.getId());
            parsed.add(targetLink.getId());
            results.add(Pair.of(sourceLink, targetLink));
        }
        return results;
    }

    private void getIsIsLinks() {

        List<IsIsElement> elements = m_isisElementDao.findAll();
        List<IsIsLink> allLinks = m_isisLinkDao.findAll();

        List<Pair<IsIsLink, IsIsLink>> results = matchIsIsLinks(elements, allLinks);

        for(Pair<IsIsLink, IsIsLink> pair : results) {
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

    List<Pair<IsIsLink, IsIsLink>> matchIsIsLinks(final List<IsIsElement> elements, final List<IsIsLink> allLinks) {

        // 1.) create lookupMaps
        Map<Integer, IsIsElement> elementmap = new HashMap<Integer, IsIsElement>();
        for (IsIsElement element: elements) {
            elementmap.put(element.getNode().getId(), element);
        }

        Map<CompositeKey, IsIsLink> targetLinkMap = new HashMap<>();
        for (IsIsLink targetLink : allLinks) {
            IsIsElement targetElement = elementmap.get(targetLink.getNode().getId());
            targetLinkMap.put(new CompositeKey(targetLink.getIsisISAdjIndex(),
                      targetElement.getIsisSysID(),
                      targetLink.getIsisISAdjNeighSysID()), targetLink);
        }

        // 2. iterate
        Set<Integer> parsed = new HashSet<Integer>();
        List<Pair<IsIsLink, IsIsLink>> results = new ArrayList<>();

        for (IsIsLink sourceLink : allLinks) {
            if (parsed.contains(sourceLink.getId())) {
                continue;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("getIsIsLinks: source: {}", sourceLink.toString());
            }
            IsIsElement sourceElement = elementmap.get(sourceLink.getNode().getId());
            IsIsLink targetLink = targetLinkMap.get(new CompositeKey(sourceLink.getIsisISAdjIndex(),
                    sourceLink.getIsisISAdjNeighSysID(),
                    sourceElement.getIsisSysID()));

            if (targetLink == null) {
                LOG.debug("getIsIsLinks: cannot found target for source: '{}'", sourceLink.getId());
                continue;
            }
            if (sourceLink.getId().intValue() == targetLink.getId().intValue()|| parsed.contains(targetLink.getId())) {
                continue;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("getIsIsLinks: target: {}", targetLink.toString());
            }
            results.add(Pair.of(sourceLink, targetLink));
            parsed.add(sourceLink.getId());
            parsed.add(targetLink.getId());
        }
        return results;
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

    public TopologyEntityCache getTopologyEntityCache() {
        return m_topologyEntityCache;
    }

    public void setTopologyEntityCache(TopologyEntityCache topologyEntityCache) {
        m_topologyEntityCache = topologyEntityCache;
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

    public void setLldpLinkDao(LldpLinkDao lldpLinkDao) {
        m_lldpLinkDao = lldpLinkDao;
    }

    public LldpLinkDao getLldpLinkDao() {
        return m_lldpLinkDao;
    }

    public void setLldpElementDao(LldpElementDao lldpElementDao) {
        m_lldpElementDao = lldpElementDao;
    }

    public LldpElementDao getLldpElementDao() {
        return m_lldpElementDao;
    }

    public void setOspfLinkDao(OspfLinkDao ospfLinkDao) {
        m_ospfLinkDao = ospfLinkDao;
    }

    public OspfLinkDao getOspfLinkDao(){
        return m_ospfLinkDao;
    }

    public IsIsLinkDao getIsisLinkDao() {
        return m_isisLinkDao;
    }

    public void setIsisLinkDao(IsIsLinkDao isisLinkDao) {
        m_isisLinkDao = isisLinkDao;
    }

    public IsIsElementDao getIsisElementDao() {
        return m_isisElementDao;
    }

    public void setIsisElementDao(IsIsElementDao isisElementDao) {
        m_isisElementDao = isisElementDao;
    }

    public BridgeTopologyService getBridgeTopologyService() {
        return m_bridgeTopologyService;
    }

    public void setBridgeTopologyService(BridgeTopologyService bridgeTopologyService) {
        m_bridgeTopologyService = bridgeTopologyService;
    }

    public IpNetToMediaDao getIpNetToMediaDao() {
        return m_ipNetToMediaDao;
    }
    
    public void setIpNetToMediaDao(IpNetToMediaDao ipNetToMediaDao) {
        m_ipNetToMediaDao = ipNetToMediaDao;
    }

    public CdpElementDao getCdpElementDao() {
        return m_cdpElementDao;
    }

    public void setCdpElementDao(CdpElementDao cdpElementDao) {
        m_cdpElementDao = cdpElementDao;
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
                // Index the SNMP interfaces by node id
                final int nodeId = snmp.getNode().getId();
                if (!m_nodeToOnmsSnmpMap.containsKey(nodeId)) {
                    m_nodeToOnmsSnmpMap.put(nodeId, new HashMap<Integer, OnmsSnmpInterface>());
                }
                m_nodeToOnmsSnmpMap.get(nodeId).put(snmp.getIfIndex(), snmp);
            }
            LOG.info("refresh: Snmp Interface loaded");
        } catch (Exception e){
            LOG.error("Loading Snmp Interface failed: {}",e.getMessage(),e);
        } finally {
            vcontext.stop();
        }

        vcontext = m_loadIpNetToMediaTimer.time();

        Set<String> multiIpMacs = new HashSet<String>();
        Set<String> multiSnmpMacs = new HashSet<String>();

        // mac -> ip[]  ->  snmp[]    —> macToNodeMap
        // mac -> ip    ->  snmp      —> macToNodeMap, macToIpMap, macToSnmpMap
        // mac -> ip    ->  no snmp   —> macToNodeMap, macToIpMap
        // mac -> ip[]  ->  no snmp   —> macToNodeMap
        // mac -> ip[]  ->  snmp      —> macToSnmpMap
        try {
            for (IpNetToMedia ipnettomedia: m_ipNetToMediaDao.findAll()) {
                OnmsIpInterface onmsip = ipToOnmsIpMap.get(ipnettomedia.getNetAddress());
                if (onmsip == null) {
                    LOG.debug("refresh: ipNetToMedia: {}:{}. No OnmsIpInterface found.", ipnettomedia.getPhysAddress(),InetAddressUtils.str(ipnettomedia.getNetAddress()));
                    continue;
                }
                LOG.debug("refresh: ipNetToMedia: {}:{}. OnmsIpInterface found node:[{}].", ipnettomedia.getPhysAddress(),
                          InetAddressUtils.str(ipnettomedia.getNetAddress()),onmsip.getNodeId());
                if (!m_macToNodeidMap.containsKey(ipnettomedia.getPhysAddress())) {
                    m_macToNodeidMap.put(ipnettomedia.getPhysAddress(), onmsip.getNodeId());
                }
                if (!m_macToOnmsIpMap.containsKey(ipnettomedia.getPhysAddress())) {
                    m_macToOnmsIpMap.put(ipnettomedia.getPhysAddress(), onmsip);
                } else {
                    multiIpMacs.add(ipnettomedia.getPhysAddress());
                    LOG.debug("refresh: ipNetToMedia: {}:{}. Multiple OnmsIpInterface found.", ipnettomedia.getPhysAddress(),InetAddressUtils.str(ipnettomedia.getNetAddress()));
                }
                if (m_nodeToOnmsSnmpMap.containsKey(onmsip.getNodeId())) {
                for (OnmsSnmpInterface onmssnmp : m_nodeToOnmsSnmpMap.get(onmsip.getNodeId()).values() ) {
                    if (onmssnmp.getId().intValue() == onmssnmp.getId().intValue()) {
                        if (!m_macToOnmsSnmpMap.containsKey(ipnettomedia.getPhysAddress())) {
                            m_macToOnmsSnmpMap.put(ipnettomedia.getPhysAddress(), onmssnmp);
                        } else if (m_macToOnmsSnmpMap.get(ipnettomedia.getPhysAddress()).getId().intValue() == onmssnmp.getId() ) {
                            continue;
                        } else {
                            multiSnmpMacs.add(ipnettomedia.getPhysAddress());
                            LOG.debug("refresh: ipNetToMedia: {}:{}. Multiple OnmsSnmpInterface found.", ipnettomedia.getPhysAddress(),InetAddressUtils.str(ipnettomedia.getNetAddress()));                                
                        }
                    }
                }
                } else {
                    LOG.debug("refresh: ipNetToMedia: {}:{}. No OnmsSnmpInterface found.", ipnettomedia.getPhysAddress(),InetAddressUtils.str(ipnettomedia.getNetAddress()));
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
}
