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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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
import org.opennms.netmgt.dao.api.BridgeTopologyDao;
import org.opennms.netmgt.dao.api.CdpElementDao;
import org.opennms.netmgt.dao.api.CdpLinkDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.IpNetToMediaDao;
import org.opennms.netmgt.dao.api.IsIsElementDao;
import org.opennms.netmgt.dao.api.IsIsLinkDao;
import org.opennms.netmgt.dao.api.LldpElementDao;
import org.opennms.netmgt.dao.api.LldpLinkDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.OspfLinkDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.dao.api.TopologyDao;
import org.opennms.netmgt.model.CdpElement;
import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.model.FilterManager;
import org.opennms.netmgt.model.IpNetToMedia;
import org.opennms.netmgt.model.IsIsElement;
import org.opennms.netmgt.model.IsIsLink;
import org.opennms.netmgt.model.LldpElement;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OspfLink;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.topology.BridgePort;
import org.opennms.netmgt.model.topology.BridgeTopologyException;
import org.opennms.netmgt.model.topology.BroadcastDomain;
import org.opennms.netmgt.model.topology.SharedSegment;
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
    private SnmpInterfaceDao m_snmpInterfaceDao;
    private IpInterfaceDao m_ipInterfaceDao;
    private TopologyDao m_topologyDao;
    private FilterManager m_filterManager;

    private LldpLinkDao m_lldpLinkDao;
    private LldpElementDao m_lldpElementDao;
    private CdpLinkDao m_cdpLinkDao;
    private CdpElementDao m_cdpElementDao;
    private OspfLinkDao m_ospfLinkDao;
    private IsIsLinkDao m_isisLinkDao;
    private IsIsElementDao m_isisElementDao;
    private BridgeTopologyDao m_bridgeTopologyDao;
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
    
    public enum ProtocolSupported {
        LLDP,
        OSPF,
        ISIS,
        BRIDGE,
        CDP
    }

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

        // Pull all of the LLDP links and index them by remote chassis id
        Map<String, List<LldpLink>> lldpRemoteIdLinksMap = new HashMap<>();
        List<LldpLink> allLinks = m_lldpLinkDao.findAll();
        for (LldpLink link : allLinks) {
            final String remoteChassisId = link.getLldpRemChassisId();
            if (!lldpRemoteIdLinksMap.containsKey(remoteChassisId)) {
                lldpRemoteIdLinksMap.put(remoteChassisId, new ArrayList<>());
            }
            lldpRemoteIdLinksMap.get(remoteChassisId).add(link);
        }

        Set<Integer> parsed = new HashSet<Integer>();
        for (LldpLink sourceLink : allLinks) {
            if (parsed.contains(sourceLink.getId())) {
                continue;
            }
            String sourceLldpChassisId = nodelldpelementidMap.get(sourceLink.getNode().getId()).getLldpChassisId();
            if (sourceLldpChassisId.equals(sourceLink.getLldpRemChassisId())) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getLldpLinks: self link not adding source: {}",sourceLink.printTopology());
                }
                parsed.add(sourceLink.getId());
                continue;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("getLldpLinks: source: {}",sourceLink.printTopology());
            }
            LldpLink targetLink = null;

            // Limit the candidate links by only choosing those have a remote chassis id matching the chassis id of the source link
            for (LldpLink link : lldpRemoteIdLinksMap.getOrDefault(sourceLldpChassisId, Collections.emptyList())) {
                if (parsed.contains(link.getId())) {
                    continue;
                }

                String targetchassisId = nodelldpelementidMap.get(link.getNode().getId()).getLldpChassisId();
                // Compare the chassis id on the other end of the link
                if (!sourceLink.getLldpRemChassisId().equals(targetchassisId)) {
                    continue;
                }
                // no match if source Rem Port is not 'link' Local Port
                if (!(sourceLink.getLldpRemPortId().equals(link.getLldpPortId())) || !(sourceLink.getLldpRemPortIdSubType() == link.getLldpPortIdSubType())) {
                    continue;
                }
                // no match if source Local Port is not 'link Remote Port
                if (!(link.getLldpRemPortId().equals(sourceLink.getLldpPortId()))|| !(link.getLldpRemPortIdSubType() == sourceLink.getLldpPortIdSubType())) {
                    continue;
                }
                // biderection link found 
                targetLink=link;
                if (LOG.isDebugEnabled()) {
                    LOG.debug("getLldpLinks: lldp: {} target: {}", targetchassisId, link.printTopology());
                }
                break;
            }

            if (targetLink == null) {
                LOG.debug("getLldpLinks: cannot found target for source: '{}'", sourceLink.getId());
                continue;
            }
            parsed.add(sourceLink.getId());
            parsed.add(targetLink.getId());
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

    private void getOspfLinks() {
        List<OspfLink> allLinks =  getOspfLinkDao().findAll();
        Set<Integer> parsed = new HashSet<Integer>();
SOURCE:        for(OspfLink sourceLink : allLinks) {
            if (parsed.contains(sourceLink.getId())) {
                continue;
            }
            parsed.add(sourceLink.getId());
            if (LOG.isDebugEnabled()) {
                LOG.debug("getOspfLinks: source: {}", sourceLink.printTopology());
            }
            for (OspfLink targetLink : allLinks) {
                if (sourceLink.getId().intValue() == targetLink.getId().intValue() || parsed.contains(targetLink.getId())) { 
                    continue;
                }
                if(sourceLink.getOspfRemIpAddr().equals(targetLink.getOspfIpAddr()) && targetLink.getOspfRemIpAddr().equals(sourceLink.getOspfIpAddr())) {
                    LOG.info("getOspfLinks: target: {}", targetLink.printTopology());
                    parsed.add(targetLink.getId());
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
                    continue SOURCE;
                }
            }
            LOG.debug("getOspfLinks: cannot found target for source: '{}'", sourceLink.getId());
        }
    }

    private void getCdpLinks() {
        Map<Integer, CdpElement> cdpelementmap = new HashMap<Integer, CdpElement>();
        for (CdpElement cdpelement: m_cdpElementDao.findAll()) {
            cdpelementmap.put(cdpelement.getNode().getId(), cdpelement);
        }

        List<CdpLink> allLinks = m_cdpLinkDao.findAll();
        Set<Integer> parsed = new HashSet<Integer>();

        for (CdpLink sourceLink : allLinks) {
            if (parsed.contains(sourceLink.getId())) { 
                continue;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("getCdpLinks: source: {} ", sourceLink.printTopology());
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
            LinkdVertex source = (LinkdVertex)getVertex(TOPOLOGY_NAMESPACE_LINKD, sourceLink.getNode().getNodeId());
            source.getProtocolSupported().add(ProtocolSupported.CDP);
            LinkdVertex target = (LinkdVertex)getVertex(TOPOLOGY_NAMESPACE_LINKD, targetLink.getNode().getNodeId());
            target.getProtocolSupported().add(ProtocolSupported.CDP);
            OnmsSnmpInterface sourceSnmpInterface = getSnmpInterface(sourceLink.getNode().getId(), sourceLink.getCdpCacheIfIndex());
            OnmsSnmpInterface targetSnmpInterface = getSnmpInterface(targetLink.getNode().getId(), targetLink.getCdpCacheIfIndex());
            connectVertices(getDefaultEdgeId(sourceLink.getId(), targetLink.getId()),
                            source,target,
                            sourceSnmpInterface,targetSnmpInterface,
                            targetLink.getCdpCacheAddress(),
                            sourceLink.getCdpCacheAddress(),
                            ProtocolSupported.CDP);
        }
    }
    
    private void getIsIsLinks(){

        Map<Integer, IsIsElement> elementmap = new HashMap<Integer, IsIsElement>();
        for (IsIsElement element: m_isisElementDao.findAll()) {
            elementmap.put(element.getNode().getId(), element);
        }

        List<IsIsLink> isislinks = m_isisLinkDao.findAll();
        Set<Integer> parsed = new HashSet<Integer>();

        for (IsIsLink sourceLink : isislinks) {
            if (parsed.contains(sourceLink.getId())) { 
                continue;
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("getIsIsLinks: source: {}", sourceLink.printTopology());
            }
            IsIsElement sourceElement = elementmap.get(sourceLink.getNode().getId());
            IsIsLink targetLink = null;
            for (IsIsLink link : isislinks) {
                if (sourceLink.getId().intValue() == link.getId().intValue()|| parsed.contains(link.getId())) {
                    continue;
                }
                IsIsElement targetElement = elementmap.get(link.getNode().getId());
                //Compare the remote data to the targetNode element data
                if (!sourceLink.getIsisISAdjNeighSysID().equals(targetElement.getIsisSysID())  
                        || !link.getIsisISAdjNeighSysID().equals(sourceElement.getIsisSysID())) { 
                    continue;
                }

                if (sourceLink.getIsisISAdjIndex().intValue() == 
                        link.getIsisISAdjIndex().intValue()  ) {
                    targetLink=link;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("getIsIsLinks: target: {}", targetLink.printTopology());
                    }
                    break;
                }
            }
            
            if (targetLink == null) {
                LOG.debug("getIsIsLinks: cannot found target for source: '{}'", sourceLink.getId());
                continue;
            }

            parsed.add(sourceLink.getId());
            parsed.add(targetLink.getId());
            LinkdVertex source = (LinkdVertex)getVertex(TOPOLOGY_NAMESPACE_LINKD, sourceLink.getNode().getNodeId());
            source.getProtocolSupported().add(ProtocolSupported.ISIS);
            LinkdVertex target = (LinkdVertex)getVertex(TOPOLOGY_NAMESPACE_LINKD, targetLink.getNode().getNodeId());
            target.getProtocolSupported().add(ProtocolSupported.ISIS);
            OnmsSnmpInterface sourceSnmpInterface = getSnmpInterface(sourceLink.getNode().getId(), sourceLink.getIsisCircIfIndex());
            OnmsSnmpInterface targetSnmpInterface = getSnmpInterface(targetLink.getNode().getId(), targetLink.getIsisCircIfIndex());
            connectVertices(getDefaultEdgeId(sourceLink.getId(), targetLink.getId()),
                            source,target,
                            sourceSnmpInterface,targetSnmpInterface,
                            targetLink.getIsisISAdjNeighSNPAAddress(),
                            sourceLink.getIsisISAdjNeighSNPAAddress(),
                            ProtocolSupported.ISIS);
        }

    }
    
    private void getBridgeLinks() throws BridgeTopologyException {
        
        for (BroadcastDomain domain: m_bridgeTopologyDao.load()) {
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

    public void setTopologyDao(TopologyDao topologyDao) {
        m_topologyDao = topologyDao;
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

    public BridgeTopologyDao getBridgeTopologyDao() {
        return m_bridgeTopologyDao;
    }

    public void setBridgeTopologyDao(BridgeTopologyDao bridgeTopologyDao) {
        m_bridgeTopologyDao = bridgeTopologyDao;
    }

    public IpNetToMediaDao getIpNetToMediaDao() {
        return m_ipNetToMediaDao;
    }
    
    public void setIpNetToMediaDao(IpNetToMediaDao ipNetToMediaDao) {
        m_ipNetToMediaDao = ipNetToMediaDao;
    }

    public CdpLinkDao getCdpLinkDao() {
        return m_cdpLinkDao;
    }

    public void setCdpLinkDao(CdpLinkDao cdpLinkDao) {
        m_cdpLinkDao = cdpLinkDao;
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
                    final OnmsNode node = m_topologyDao.getDefaultFocusPoint();

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
                if (!m_macToNodeidMap.containsKey(ipnettomedia.getPhysAddress())) {
                    m_macToNodeidMap.put(ipnettomedia.getPhysAddress(), onmsip.getNodeId());
                }
                if (!m_macToOnmsIpMap.containsKey(ipnettomedia.getPhysAddress())) {
                    m_macToOnmsIpMap.put(ipnettomedia.getPhysAddress(), onmsip);
                } else {
                    multiIpMacs.add(ipnettomedia.getPhysAddress());
                    LOG.debug("refresh: ipNetToMedia: {}:{}. Multiple OnmsIpInterface found.", ipnettomedia.getPhysAddress(),InetAddressUtils.str(ipnettomedia.getNetAddress()));
                }
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
