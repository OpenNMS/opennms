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
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.enlinkd.model.CdpElementTopologyEntity;
import org.opennms.netmgt.enlinkd.model.CdpLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.IpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.model.IpNetToMedia;
import org.opennms.netmgt.enlinkd.model.IsIsElementTopologyEntity;
import org.opennms.netmgt.enlinkd.model.IsIsLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.LldpElementTopologyEntity;
import org.opennms.netmgt.enlinkd.model.LldpLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.NodeTopologyEntity;
import org.opennms.netmgt.enlinkd.model.OspfLinkTopologyEntity;
import org.opennms.netmgt.enlinkd.model.SnmpInterfaceTopologyEntity;
import org.opennms.netmgt.enlinkd.persistence.api.IpNetToMediaDao;
import org.opennms.netmgt.enlinkd.persistence.api.TopologyEntityCache;
import org.opennms.netmgt.enlinkd.service.api.BridgePort;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyException;
import org.opennms.netmgt.enlinkd.service.api.BridgeTopologyService;
import org.opennms.netmgt.enlinkd.service.api.BroadcastDomain;
import org.opennms.netmgt.enlinkd.service.api.ProtocolSupported;
import org.opennms.netmgt.enlinkd.service.api.SharedSegment;
import org.opennms.netmgt.model.FilterManager;
import org.opennms.netmgt.model.OnmsNode;
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
    private FilterManager m_filterManager;

    private BridgeTopologyService m_bridgeTopologyService;
    private IpNetToMediaDao m_ipNetToMediaDao;

    private Map<Integer, IpInterfaceTopologyEntity> m_nodeToOnmsIpPrimaryMap =new HashMap<>();
    private Map<Integer, Map<Integer,SnmpInterfaceTopologyEntity>> m_nodeToOnmsSnmpMap = new HashMap<>();
    private Map<String, Integer> m_macToNodeidMap = new HashMap<>();
    private Map<String, IpInterfaceTopologyEntity> m_macToOnmsIpMap = new HashMap<>();
    private Map<String, SnmpInterfaceTopologyEntity> m_macToOnmsSnmpMap = new HashMap<>();

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

    private SnmpInterfaceTopologyEntity getSnmpInterface(Integer nodeid, Integer ifindex) {
        if(m_nodeToOnmsSnmpMap.containsKey(nodeid)) {
            if (m_nodeToOnmsSnmpMap.get(nodeid).containsKey(ifindex)) {
                return m_nodeToOnmsSnmpMap.get(nodeid).get(ifindex);
            }
        }
        return new SnmpInterfaceTopologyEntity(null, ifindex, "No Interface Found", 0l, nodeid);
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
            SnmpInterfaceTopologyEntity sourceinterface,
            SnmpInterfaceTopologyEntity targetInterface,
            String sourceAddr,
            String targetAddr,
            ProtocolSupported discoveredBy) {
        addEdges(LinkdEdge.create(id, sourceV, targetV, sourceinterface, targetInterface,sourceAddr,targetAddr,discoveredBy));
    }
    
    private void loadVertices() {
        for (NodeTopologyEntity nodeTopologyEntity : m_topologyEntityCache.getNodeTopolgyEntities()) {
            IpInterfaceTopologyEntity primary = m_nodeToOnmsIpPrimaryMap.get(nodeTopologyEntity.getId());
            addVertices(LinkdVertex.create(nodeTopologyEntity,primary));
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

        List<LldpLinkTopologyEntity> allLinks = m_topologyEntityCache.getLldpLinkTopologyEntities();
        // Index the LLDP elements by node id
        Map<Integer, LldpElementTopologyEntity> nodelldpelementidMap = new HashMap<>();
        Map<Integer, LinkdVertex> nodeVertexMap = new HashMap<Integer, LinkdVertex>();
        for (LldpElementTopologyEntity lldpelement: m_topologyEntityCache.getLldpElementTopologyEntities()) {
            nodelldpelementidMap.put(lldpelement.getNodeId(), lldpelement);
            LinkdVertex vertex = (LinkdVertex)getVertex(TOPOLOGY_NAMESPACE_LINKD, lldpelement.getNodeIdAsString());
            vertex.getProtocolSupported().add(ProtocolSupported.LLDP);
            nodeVertexMap.put(lldpelement.getNodeId(), vertex);
            System.err.println(vertex.getId());
        }

        List<Pair<LldpLinkTopologyEntity, LldpLinkTopologyEntity>> matchedLinks = matchLldpLinks(nodelldpelementidMap, allLinks);

        for (Pair<LldpLinkTopologyEntity, LldpLinkTopologyEntity> pair : matchedLinks) {
            LldpLinkTopologyEntity sourceLink = pair.getLeft();
            LldpLinkTopologyEntity targetLink = pair.getRight();
            LinkdVertex source = nodeVertexMap.get(sourceLink.getNodeId());
            LinkdVertex target = nodeVertexMap.get(targetLink.getNodeId());
            SnmpInterfaceTopologyEntity sourceSnmpInterface = getSnmpInterface(sourceLink.getNodeId(), sourceLink.getLldpPortIfindex());
            SnmpInterfaceTopologyEntity targetSnmpInterface = getSnmpInterface(targetLink.getNodeId(), targetLink.getLldpPortIfindex());
            connectVertices(getDefaultEdgeId(sourceLink.getId(), targetLink.getId()),
                    source,target,
                    sourceSnmpInterface,targetSnmpInterface,
                    sourceLink.getLldpPortDescr(),targetLink.getLldpPortDescr(),
                    ProtocolSupported.LLDP);
        }
    }

    List<Pair<LldpLinkTopologyEntity, LldpLinkTopologyEntity>> matchLldpLinks(Map<Integer, LldpElementTopologyEntity> nodelldpelementidMap, List<LldpLinkTopologyEntity> allLinks) {
        List<Pair<LldpLinkTopologyEntity, LldpLinkTopologyEntity>> results = new ArrayList<>();

        // 1.) create mapping
        Map<CompositeKey, LldpLinkTopologyEntity> targetLinkMap = new HashMap<>();
        for(LldpLinkTopologyEntity targetLink : allLinks){

            CompositeKey key = new CompositeKey(
                    targetLink.getLldpRemChassisId(),
                    nodelldpelementidMap.get(targetLink.getNodeId()).getLldpChassisId(),
                    targetLink.getLldpPortId(),
                    targetLink.getLldpPortIdSubType(),
                    targetLink.getLldpRemPortId(),
                    targetLink.getLldpRemPortIdSubType());
            targetLinkMap.put(key, targetLink);
        }

        // 2.) iterate
        Set<Integer> parsed = new HashSet<Integer>();
        for (LldpLinkTopologyEntity sourceLink : allLinks) {
            if (parsed.contains(sourceLink.getId())) {
                continue;
            }
            String sourceLldpChassisId = nodelldpelementidMap.get(sourceLink.getNodeId()).getLldpChassisId();
            if (sourceLldpChassisId.equals(sourceLink.getLldpRemChassisId())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getLldpLinks: self link not adding source: {}",sourceLink);
                }
                parsed.add(sourceLink.getId());
                continue;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("getLldpLinks: source: {}",sourceLink);
            }

            CompositeKey key = new CompositeKey(
                    nodelldpelementidMap.get(sourceLink.getNodeId()).getLldpChassisId(),
                    sourceLink.getLldpRemChassisId(),
                    sourceLink.getLldpRemPortId(),
                    sourceLink.getLldpRemPortIdSubType(),
                    sourceLink.getLldpPortId(),
                    sourceLink.getLldpPortIdSubType());
            LldpLinkTopologyEntity targetLink = targetLinkMap.get(key);

            if (targetLink == null) {
                LOG.debug("getLldpLinks: cannot found target for source: '{}'", sourceLink.getId());
                continue;
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("getLldpLinks: lldp: {} target: {}", sourceLink.getLldpRemChassisId(), targetLink);
            }

            parsed.add(sourceLink.getId());
            parsed.add(targetLink.getId());
            results.add(Pair.of(sourceLink, targetLink));
        }
        return results;
    }

    private void getOspfLinks() {

        List<OspfLinkTopologyEntity> allLinks = m_topologyEntityCache.getOspfLinkTopologyEntities();
        List<Pair<OspfLinkTopologyEntity, OspfLinkTopologyEntity>> matchedLinks = matchOspfLinks(allLinks);

        for (Pair<OspfLinkTopologyEntity, OspfLinkTopologyEntity> pair : matchedLinks) {
            OspfLinkTopologyEntity sourceLink = pair.getLeft();
            OspfLinkTopologyEntity targetLink = pair.getRight();

            LinkdVertex source = (LinkdVertex)getVertex(TOPOLOGY_NAMESPACE_LINKD, sourceLink.getNodeIdAsString());
            source.getProtocolSupported().add(ProtocolSupported.OSPF);
            LinkdVertex target = (LinkdVertex)getVertex(TOPOLOGY_NAMESPACE_LINKD, targetLink.getNodeIdAsString());
            target.getProtocolSupported().add(ProtocolSupported.OSPF);
            SnmpInterfaceTopologyEntity sourceSnmpInterface = getSnmpInterface(sourceLink.getNodeId(), sourceLink.getOspfIfIndex());
            SnmpInterfaceTopologyEntity targetSnmpInterface = getSnmpInterface(targetLink.getNodeId(), targetLink.getOspfIfIndex());
            connectVertices(getDefaultEdgeId(sourceLink.getId(), targetLink.getId()),
                    source,target,
                    sourceSnmpInterface,targetSnmpInterface,
                    InetAddressUtils.str(targetLink.getOspfRemIpAddr()),
                    InetAddressUtils.str(sourceLink.getOspfRemIpAddr()),
                    ProtocolSupported.OSPF);
        }
    }

    List<Pair<OspfLinkTopologyEntity, OspfLinkTopologyEntity>> matchOspfLinks(List<OspfLinkTopologyEntity> allLinks){
        List<Pair<OspfLinkTopologyEntity, OspfLinkTopologyEntity>> results = new ArrayList<>();
        Set<Integer> parsed = new HashSet<Integer>();

        // build mapping:
        Map<CompositeKey, OspfLinkTopologyEntity> targetLinks = new HashMap<>();
        for(OspfLinkTopologyEntity targetLink : allLinks){
            targetLinks.put(new CompositeKey(targetLink.getOspfIpAddr(), targetLink.getOspfRemIpAddr()) , targetLink);
        }

        for(OspfLinkTopologyEntity sourceLink : allLinks) {
            if (parsed.contains(sourceLink.getId())) {
                continue;
            }
            parsed.add(sourceLink.getId());
            if (LOG.isDebugEnabled()) {
                LOG.debug("getOspfLinks: source: {}", sourceLink);
            }
            OspfLinkTopologyEntity targetLink = targetLinks.get(new CompositeKey(sourceLink.getOspfRemIpAddr() , sourceLink.getOspfIpAddr()));
            if(targetLink == null) {
                LOG.debug("getOspfLinks: cannot find target for source: '{}'", sourceLink.getId());
                continue;
            }

            if (sourceLink.getId().equals(targetLink.getId()) || parsed.contains(targetLink.getId())) {
                    continue;
            }

            LOG.debug("getOspfLinks: target: {}", targetLink);
            parsed.add(targetLink.getId());
           results.add(Pair.of(sourceLink, targetLink));
        }
        return results;
    }




    private void getCdpLinks() {
        List<CdpElementTopologyEntity> cdpElements = m_topologyEntityCache.getCdpElementTopologyEntities();
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
        SnmpInterfaceTopologyEntity sourceSnmpInterface = getSnmpInterface(sourceLink.getNodeId(), sourceLink.getCdpCacheIfIndex());
        SnmpInterfaceTopologyEntity targetSnmpInterface = getSnmpInterface(targetLink.getNodeId(), targetLink.getCdpCacheIfIndex());
        connectVertices(getDefaultEdgeId(sourceLink.getId(), targetLink.getId()),
                source, target,
                sourceSnmpInterface, targetSnmpInterface,
                targetLink.getCdpCacheAddress(),
                sourceLink.getCdpCacheAddress(),
                ProtocolSupported.CDP);
    }

    List<Pair<CdpLinkTopologyEntity, CdpLinkTopologyEntity>> matchCdpLinks(final List<CdpElementTopologyEntity> cdpElements, final List<CdpLinkTopologyEntity> allLinks) {

        // 1. create lookup maps:
        Map<Integer, CdpElementTopologyEntity> cdpelementmap = new HashMap<>();
        for (CdpElementTopologyEntity cdpelement: cdpElements) {
            cdpelementmap.put(cdpelement.getNodeId(), cdpelement);
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
                LOG.debug("getCdpLinks: source: {} ", sourceLink);
            }
            CdpElementTopologyEntity sourceCdpElement = cdpelementmap.get(sourceLink.getNodeId());

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
                LOG.debug("getCdpLinks: cdp: {}, target: {} ", sourceLink.getCdpCacheDevicePort(), targetLink);
            }

            parsed.add(sourceLink.getId());
            parsed.add(targetLink.getId());
            results.add(Pair.of(sourceLink, targetLink));
        }
        return results;
    }

    private void getIsIsLinks() {

        List<IsIsElementTopologyEntity> elements = m_topologyEntityCache.getIsIsElementTopologyEntities();
        List<IsIsLinkTopologyEntity> allLinks = m_topologyEntityCache.getIsIsLinkTopologyEntities();

        List<Pair<IsIsLinkTopologyEntity, IsIsLinkTopologyEntity>> results = matchIsIsLinks(elements, allLinks);

        for(Pair<IsIsLinkTopologyEntity, IsIsLinkTopologyEntity> pair : results) {
            IsIsLinkTopologyEntity sourceLink = pair.getLeft();
            IsIsLinkTopologyEntity targetLink = pair.getRight();
            LinkdVertex source = (LinkdVertex) getVertex(TOPOLOGY_NAMESPACE_LINKD, sourceLink.getNodeIdAsString());
            source.getProtocolSupported().add(ProtocolSupported.ISIS);
            LinkdVertex target = (LinkdVertex) getVertex(TOPOLOGY_NAMESPACE_LINKD, targetLink.getNodeIdAsString());
            target.getProtocolSupported().add(ProtocolSupported.ISIS);
            SnmpInterfaceTopologyEntity sourceSnmpInterface = getSnmpInterface(sourceLink.getNodeId(), sourceLink.getIsisCircIfIndex());
            SnmpInterfaceTopologyEntity targetSnmpInterface = getSnmpInterface(targetLink.getNodeId(), targetLink.getIsisCircIfIndex());
            connectVertices(getDefaultEdgeId(sourceLink.getId(), targetLink.getId()),
                source, target,
                sourceSnmpInterface, targetSnmpInterface,
                targetLink.getIsisISAdjNeighSNPAAddress(),
                sourceLink.getIsisISAdjNeighSNPAAddress(),
                ProtocolSupported.ISIS);
        }
    }

    List<Pair<IsIsLinkTopologyEntity, IsIsLinkTopologyEntity>> matchIsIsLinks(final List<IsIsElementTopologyEntity> elements, final List<IsIsLinkTopologyEntity> allLinks) {

        // 1.) create lookupMaps
        Map<Integer, IsIsElementTopologyEntity> elementmap = new HashMap<>();
        for (IsIsElementTopologyEntity element: elements) {
            elementmap.put(element.getNodeId(), element);
        }

        Map<CompositeKey, IsIsLinkTopologyEntity> targetLinkMap = new HashMap<>();
        for (IsIsLinkTopologyEntity targetLink : allLinks) {
          IsIsElementTopologyEntity targetElement = elementmap.get(targetLink.getNodeId());
            targetLinkMap.put(new CompositeKey(targetLink.getIsisISAdjIndex(),
                      targetElement.getIsisSysID(),
                      targetLink.getIsisISAdjNeighSysID()), targetLink);
        }

        // 2. iterate
        Set<Integer> parsed = new HashSet<Integer>();
        List<Pair<IsIsLinkTopologyEntity, IsIsLinkTopologyEntity>> results = new ArrayList<>();

        for (IsIsLinkTopologyEntity sourceLink : allLinks) {
            if (parsed.contains(sourceLink.getId())) {
                continue;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("getIsIsLinks: source: {}", sourceLink);
            }
            IsIsElementTopologyEntity sourceElement = elementmap.get(sourceLink.getNodeId());
            IsIsLinkTopologyEntity targetLink = targetLinkMap.get(new CompositeKey(sourceLink.getIsisISAdjIndex(),
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
                LOG.debug("getIsIsLinks: target: {}", targetLink);
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
           SnmpInterfaceTopologyEntity targetsnmpIface = m_macToOnmsSnmpMap.get(mac);
           if (targetsnmpIface != null) {
               LinkdVertex vertex = (LinkdVertex)getVertex(TOPOLOGY_NAMESPACE_LINKD, targetsnmpIface.getNodeIdAsString());
               vertex.getProtocolSupported().add(ProtocolSupported.BRIDGE);
               macToVertexMap.put(mac,vertex);
               continue;
           }
           IpInterfaceTopologyEntity targetipIface = m_macToOnmsIpMap.get(mac);
           if (targetipIface != null) {
               LinkdVertex vertex = (LinkdVertex)getVertex(TOPOLOGY_NAMESPACE_LINKD, targetipIface.getNodeIdAsString());
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
            SnmpInterfaceTopologyEntity sourceSnmpInterface = getSnmpInterface(sourcebp.getNodeId(), sourcebp.getBridgePortIfIndex());
            SnmpInterfaceTopologyEntity targetSnmpInterface = getSnmpInterface(targetbp.getNodeId(), targetbp.getBridgePortIfIndex());
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

            SnmpInterfaceTopologyEntity sourceinterface = getSnmpInterface(sourcebp.getNodeId(), sourcebp.getBridgePortIfIndex());
            SnmpInterfaceTopologyEntity targetinterface = m_macToOnmsSnmpMap.get(targetmac);
            IpInterfaceTopologyEntity targetipinterface = m_macToOnmsIpMap.get(targetmac);
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
            SnmpInterfaceTopologyEntity targetinterface = getSnmpInterface(bp.getNodeId(), bp.getBridgePortIfIndex());
            connectVertices(getEdgeId(cloudVertex, bp), cloudVertex, bpportvertex, null, targetinterface, 
                            "shared segment: up bridge " + topVertex.getLabel() + " bp:" + segment.getDesignatedPort().getBridgePort(),
                            "bp: "+bp.getBridgePort(), ProtocolSupported.BRIDGE);
            
        }
        for (String mac: macToVertexMap.keySet()) {
            LinkdVertex target = macToVertexMap.get(mac);
            SnmpInterfaceTopologyEntity targetiface = m_macToOnmsSnmpMap.get(mac);
            IpInterfaceTopologyEntity targetipinterface = m_macToOnmsIpMap.get(mac);
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

    public void setFilterManager(FilterManager filterManager) {
        m_filterManager = filterManager;
    }

    public FilterManager getFilterManager() {
        return m_filterManager;
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
        Map<InetAddress, IpInterfaceTopologyEntity>  ipToOnmsIpMap = new HashMap<InetAddress, IpInterfaceTopologyEntity>();
        Set<InetAddress> duplicated = new HashSet<InetAddress>();
        try {
            for (IpInterfaceTopologyEntity ip: m_topologyEntityCache.getIpInterfaceTopologyEntities()) {
                if (ip.getIsSnmpPrimary().equals(PrimaryType.PRIMARY)) {
                    m_nodeToOnmsIpPrimaryMap.put(ip.getNodeId(), ip);
                } else {
                    m_nodeToOnmsIpPrimaryMap.putIfAbsent(ip.getNodeId(), ip);
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
            for (SnmpInterfaceTopologyEntity snmp: m_topologyEntityCache.getSnmpInterfaceTopologyEntities()) {
                // Index the SNMP interfaces by node id
                final int nodeId = snmp.getNodeId();
                if (!m_nodeToOnmsSnmpMap.containsKey(nodeId)) {
                    m_nodeToOnmsSnmpMap.put(nodeId, new HashMap<>());
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
                IpInterfaceTopologyEntity onmsip = ipToOnmsIpMap.get(ipnettomedia.getNetAddress());
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
                    for (SnmpInterfaceTopologyEntity onmssnmp : m_nodeToOnmsSnmpMap.get(onmsip.getNodeId()).values() ) {
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
