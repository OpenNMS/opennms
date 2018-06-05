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
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang.StringUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.LldpUtils.LldpPortIdSubType;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionAware;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractSearchProvider;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.SimpleConnector;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.netmgt.dao.api.BridgeTopologyDao;
import org.opennms.netmgt.dao.api.CdpElementDao;
import org.opennms.netmgt.dao.api.CdpLinkDao;
import org.opennms.netmgt.dao.api.IpNetToMediaDao;
import org.opennms.netmgt.dao.api.IsIsElementDao;
import org.opennms.netmgt.dao.api.IsIsLinkDao;
import org.opennms.netmgt.dao.api.LldpElementDao;
import org.opennms.netmgt.dao.api.LldpLinkDao;
import org.opennms.netmgt.dao.api.OspfLinkDao;
import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.CdpElement;
import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.model.CdpLink.CiscoNetworkProtocolType;
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
import org.opennms.netmgt.model.topology.EdgeAlarmStatusSummary;
import org.opennms.netmgt.model.topology.SharedSegment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.Lists;

public class EnhancedLinkdTopologyProvider extends AbstractLinkdTopologyProvider {

    abstract class LinkDetail<K> {
        private final String m_id;
        private final Vertex m_source;
        private final K m_sourceLink;
        private final Vertex m_target;
        private final K m_targetLink;

        public LinkDetail(String id, Vertex source, K sourceLink, Vertex target, K targetLink){
            m_id = id;
            m_source = source;
            m_sourceLink = sourceLink;
            m_target = target;
            m_targetLink = targetLink;
        }

        public abstract int hashCode();

        public abstract boolean equals(Object obj);

        public abstract Integer getSourceIfIndex();

        public abstract Integer getTargetIfIndex();

        public abstract String getType();

        public String getId() {
            return m_id;
        }

        public Vertex getSource() {
            return m_source;
        }

        public Vertex getTarget() {
            return m_target;
        }

        public K getSourceLink() {
            return m_sourceLink;
        }

        public K getTargetLink() {
            return m_targetLink;
        }
    }

    class LldpLinkDetail extends LinkDetail<LldpLink> {


        public LldpLinkDetail(String id, Vertex source, LldpLink sourceLink, Vertex target, LldpLink targetLink) {
            super(id, source, sourceLink, target, targetLink);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((getSourceLink() == null) ? 0 : getSourceLink().getId().hashCode()) + ((getTargetLink() == null) ? 0 : getTargetLink().getId().hashCode());
            result = prime * result
                    + ((getNamespace() == null) ? 0 : getNamespace().hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof LldpLinkDetail){
                LldpLinkDetail objDetail = (LldpLinkDetail)obj;

                return getId().equals(objDetail.getId());
            } else  {
                return false;
            }

        }

        @Override
        public Integer getSourceIfIndex() {
            return getSourceLink().getLldpPortIfindex();
        }

        @Override
        public Integer getTargetIfIndex() {
            return getTargetLink().getLldpPortIfindex();
        }

        @Override
        public String getType() {
            return "LLDP";
        }
    }

    class OspfLinkDetail extends LinkDetail<OspfLink>{

        public OspfLinkDetail(String id, Vertex source, OspfLink sourceLink, Vertex target, OspfLink targetLink) {
            super(id, source, sourceLink, target, targetLink);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((getSourceLink() == null) ? 0 : getSourceLink().getId().hashCode()) + ((getTargetLink() == null) ? 0 : getTargetLink().getId().hashCode());
            result = prime * result
                    + ((getNamespace() == null) ? 0 : getNamespace().hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof OspfLinkDetail){
                OspfLinkDetail objDetail = (OspfLinkDetail)obj;

                return getId().equals(objDetail.getId());
            } else  {
                return false;
            }
        }

        @Override
        public Integer getSourceIfIndex() {
            return getSourceLink().getOspfIfIndex();
        }

        @Override
        public Integer getTargetIfIndex() {
            return getTargetLink().getOspfIfIndex();
        }

        @Override
        public String getType() {
            return "OSPF";
        }
    }

    class IsIsLinkDetail extends LinkDetail<IsIsLink>{


        public IsIsLinkDetail(String id, Vertex source, IsIsLink sourceLink, Vertex target, IsIsLink targetLink) {
            super(id, source, sourceLink, target, targetLink);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((getSourceLink() == null) ? 0 : getSourceLink().getId()) + ((getTargetLink() == null) ? 0 : getTargetLink().getId());
            result = prime * result
                    + ((getNamespace() == null) ? 0 : getNamespace().hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof IsIsLinkDetail){
                IsIsLinkDetail objDetail = (IsIsLinkDetail)obj;

                return getId().equals(objDetail.getId());
            } else  {
                return false;
            }
        }

        @Override
        public Integer getSourceIfIndex() {
            return getSourceLink().getIsisCircIfIndex();
        }

        @Override
        public Integer getTargetIfIndex() {
            return getTargetLink().getIsisCircIfIndex();
        }

        @Override
        public String getType() {
            return "IsIs";
        }
    }

    class BridgeLinkDetail extends LinkDetail<Integer> {

        private final String m_vertexNamespace;
        private final Integer m_sourceBridgePort;
        private final Integer m_targetBridgePort;
        private final Integer m_sourceIfIndex;
        private final Integer m_targetifIndex;

        public BridgeLinkDetail(String vertexNamespace, Vertex source, Integer sourceIfIndex, Vertex target, Integer targetIfIndex, Integer sourceBridgePort, Integer targetBridgePort,Integer sourceLink, Integer targetLink) {
            super(EdgeAlarmStatusSummary.getDefaultEdgeId(sourceLink, targetLink), source, sourceLink, target, targetLink);
            m_vertexNamespace = vertexNamespace;
            m_sourceBridgePort = sourceBridgePort;
            m_targetBridgePort = targetBridgePort;
            m_sourceIfIndex = sourceIfIndex;
            m_targetifIndex = targetIfIndex;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((getSourceLink() == null) ? 0 : getSource().getNodeID().hashCode()) + ((getTargetLink() == null) ? 0 : getTarget().getNodeID().hashCode());
            result = prime * result
                    + ((getVertexNamespace() == null) ? 0 : getVertexNamespace().hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof BridgeLinkDetail){
                BridgeLinkDetail objDetail = (BridgeLinkDetail)obj;

                return getId().equals(objDetail.getId());
            } else  {
                return false;
            }
        }
        
        public Integer getSourceBridgePort() {
            return m_sourceBridgePort;
        }

        public Integer getTargetBridgePort() {
            return m_targetBridgePort;
        }

        @Override
        public String getType() {
            return "Bridge";
        }

        public String getVertexNamespace() {
            return m_vertexNamespace;
        }

        @Override
        public Integer getSourceIfIndex() {
            return m_sourceIfIndex;
        }

        @Override
        public Integer getTargetIfIndex() {
            return m_targetifIndex;
        }
    }

    public class CdpLinkDetail extends LinkDetail<CdpLink> {

        public CdpLinkDetail(String id, Vertex source, CdpLink sourceLink, Vertex target, CdpLink targetLink) {
            super(id, source, sourceLink, target, targetLink);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((getSourceLink() == null) ? 0 : getSource().getNodeID().hashCode()) + ((getTargetLink() == null) ? 0 : getTarget().getNodeID().hashCode());
            result = prime * result
                    + ((getNamespace() == null) ? 0 : getNamespace().hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if(obj instanceof CdpLinkDetail){
                CdpLinkDetail objDetail = (CdpLinkDetail)obj;

                return getId().equals(objDetail.getId());
            } else  {
                return false;
            }
        }

        @Override
        public Integer getSourceIfIndex() {
            return getSourceLink().getCdpCacheIfIndex();
        }

        @Override
        public Integer getTargetIfIndex() {
            return getTargetLink().getCdpCacheIfIndex();
        }

        @Override
        public String getType() { return "CDP"; }

    }

    private static Logger LOG = LoggerFactory.getLogger(EnhancedLinkdTopologyProvider.class);

    static final String[] OPER_ADMIN_STATUS = new String[] {
            "&nbsp;",          //0 (not supported)
            "Up",              //1
            "Down",            //2
            "Testing",         //3
            "Unknown",         //4
            "Dormant",         //5
            "NotPresent",      //6
            "LowerLayerDown"   //7
    };

    private LldpLinkDao m_lldpLinkDao;
    private LldpElementDao m_lldpElementDao;
    private CdpLinkDao m_cdpLinkDao;
    private CdpElementDao m_cdpElementDao;
    private OspfLinkDao m_ospfLinkDao;
    private IsIsLinkDao m_isisLinkDao;
    private IsIsElementDao m_isisElementDao;
    private BridgeTopologyDao m_bridgeTopologyDao;
    private IpNetToMediaDao m_ipNetToMediaDao;

    private SelectionAware selectionAwareDelegate = new EnhancedLinkdSelectionAware();
    
    public final static String LLDP_EDGE_NAMESPACE = TOPOLOGY_NAMESPACE_LINKD + "::LLDP";
    public final static String OSPF_EDGE_NAMESPACE = TOPOLOGY_NAMESPACE_LINKD + "::OSPF";
    public final static String ISIS_EDGE_NAMESPACE = TOPOLOGY_NAMESPACE_LINKD + "::ISIS";
    public final static String BRIDGE_EDGE_NAMESPACE = TOPOLOGY_NAMESPACE_LINKD + "::BRIDGE";
    public final static String CDP_EDGE_NAMESPACE = TOPOLOGY_NAMESPACE_LINKD + "::CDP";

    private final Timer m_loadFullTimer;
    private final Timer m_loadNodesTimer;
    private final Timer m_loadIpInterfacesTimer;
    private final Timer m_loadSnmpInterfacesTimer;
    private final Timer m_loadIpNetToMediaTimer;
    private final Timer m_loadLldpLinksTimer;
    private final Timer m_loadOspfLinksTimer;
    private final Timer m_loadCdpLinksTimer;
    private final Timer m_loadIsisLinksTimer;
    private final Timer m_loadBridgeLinksTimer;
    private final Timer m_loadNoLinksTimer;
    private final Timer m_loadManualLinksTimer;

    public EnhancedLinkdTopologyProvider(MetricRegistry registry) {
        Objects.requireNonNull(registry);
        m_loadFullTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "full"));
        m_loadNodesTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "nodes"));
        m_loadIpInterfacesTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "ipinterfaces"));
        m_loadSnmpInterfacesTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "snmpinterfaces"));
        m_loadIpNetToMediaTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "ipnettomedia"));
        m_loadLldpLinksTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "links", "lldp"));
        m_loadOspfLinksTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "links", "ospf"));
        m_loadCdpLinksTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "links", "cdp"));
        m_loadIsisLinksTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "links", "isis"));
        m_loadBridgeLinksTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "links", "bridge"));
        m_loadNoLinksTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "links", "none"));
        m_loadManualLinksTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "links", "manual"));
    }

    private void loadCompleteTopology() {
        try{
            resetContainer();
        } catch (Exception e){
            LOG.error("Exception reset Container: "+e.getMessage(),e);
        }

        Map<Integer, OnmsNode> nodemap = new HashMap<Integer, OnmsNode>();
        Map<Integer, List<OnmsIpInterface>> nodeipmap = new HashMap<Integer,  List<OnmsIpInterface>>();
        Map<Integer, OnmsIpInterface> nodeipprimarymap = new HashMap<Integer, OnmsIpInterface>();
        Map<String, List<OnmsIpInterface>> macipmap = new HashMap<String, List<OnmsIpInterface>>();
        Map<InetAddress, OnmsIpInterface> ipmap = new HashMap<InetAddress,  OnmsIpInterface>();
        Map<Integer,List<OnmsSnmpInterface>> nodesnmpmap = new HashMap<Integer, List<OnmsSnmpInterface>>();

        Timer.Context context = m_loadNodesTimer.time();
        try {
            LOG.info("Loading nodes");
            for (OnmsNode node: m_nodeDao.findAll()) {
                nodemap.put(node.getId(), node);
            }
            LOG.info("Nodes loaded");
        } catch (Exception e){
            LOG.error("Exception getting node list: "+e.getMessage(),e);
        } finally {
            context.stop();
        }

        context = m_loadIpInterfacesTimer.time();
        try {
            LOG.info("Loading Ip Interface");
            Set<InetAddress> duplicatedips = new HashSet<InetAddress>();
            for (OnmsIpInterface ip: m_ipInterfaceDao.findAll()) {
                if (!nodeipmap.containsKey(ip.getNode().getId())) {
                    nodeipmap.put(ip.getNode().getId(), new ArrayList<OnmsIpInterface>());
                    nodeipprimarymap.put(ip.getNode().getId(), ip);
                }
                nodeipmap.get(ip.getNode().getId()).add(ip);
                if (ip.getIsSnmpPrimary().equals(PrimaryType.PRIMARY)) {
                    nodeipprimarymap.put(ip.getNode().getId(), ip);
                }

                if (duplicatedips.contains(ip.getIpAddress())) {
                    LOG.info("Loading ip Interface, found duplicated ip {}, skipping ", InetAddressUtils.str(ip.getIpAddress()));
                    continue;
                }
                if (ipmap.containsKey(ip.getIpAddress())) {
                    LOG.info("Loading ip Interface, found duplicated ip {}, skipping ", InetAddressUtils.str(ip.getIpAddress()));
                    duplicatedips.add(ip.getIpAddress());
                    continue;
                }
                ipmap.put(ip.getIpAddress(), ip);
            }
            for (InetAddress duplicated: duplicatedips)
                ipmap.remove(duplicated);
            LOG.info("Ip Interface loaded");
        } catch (Exception e){
            LOG.error("Exception getting ip list: "+e.getMessage(),e);
        } finally {
            context.stop();
        }

        context = m_loadSnmpInterfacesTimer.time();
        try {
            LOG.info("Loading Snmp Interface");
            for (OnmsSnmpInterface snmp: m_snmpInterfaceDao.findAll()) {
                // Index the SNMP interfaces by node id
                final int nodeId = snmp.getNode().getId();
                List<OnmsSnmpInterface> snmpinterfaces = nodesnmpmap.get(nodeId);
                if (snmpinterfaces == null) {
                    snmpinterfaces = new ArrayList<>();
                    nodesnmpmap.put(nodeId, snmpinterfaces);
                }
                snmpinterfaces.add(snmp);
            }
            LOG.info("Snmp Interface loaded");
        } catch (Exception e){
            LOG.error("Exception getting snmp interface list: "+e.getMessage(),e);
        } finally {
            context.stop();
        }

        context = m_loadIpNetToMediaTimer.time();
        try {
            Set<String> duplicatednodemac = new HashSet<String>();
            Map<String, Integer> mactonodemap = new HashMap<String, Integer>();
            LOG.info("Loading ip net to media");
            for (IpNetToMedia ipnettomedia: m_ipNetToMediaDao.findAll()) {
                if (duplicatednodemac.contains(ipnettomedia.getPhysAddress())) {
                    LOG.info("load ip net media: different nodeid found for ip: {} mac: {}. Skipping...",InetAddressUtils.str(ipnettomedia.getNetAddress()), ipnettomedia.getPhysAddress());
                    continue;
                }
                OnmsIpInterface ip = ipmap.get(ipnettomedia.getNetAddress());
                if (ip == null) {
                    LOG.info("load ip net media: no nodeid found for ip: {} mac: {}. Skipping...",InetAddressUtils.str(ipnettomedia.getNetAddress()), ipnettomedia.getPhysAddress());
                    continue;
                }
                if (mactonodemap.containsKey(ipnettomedia.getPhysAddress())) {
                    if (mactonodemap.get(ipnettomedia.getPhysAddress()).intValue() != ip.getNode().getId().intValue()) {
                        LOG.info("load ip net media: different nodeid found for ip: {} mac: {}. Skipping...",InetAddressUtils.str(ipnettomedia.getNetAddress()), ipnettomedia.getPhysAddress());
                        duplicatednodemac.add(ipnettomedia.getPhysAddress());
                        continue;
                    }
                }

                if (!macipmap.containsKey(ipnettomedia.getPhysAddress())) {
                    macipmap.put(ipnettomedia.getPhysAddress(), new ArrayList<OnmsIpInterface>());
                    mactonodemap.put(ipnettomedia.getPhysAddress(), ip.getNode().getId());
                }
                macipmap.get(ipnettomedia.getPhysAddress()).add(ip);
            }
            for (String dupmac: duplicatednodemac)
                macipmap.remove(dupmac);
            
            LOG.info("Ip net to media loaded");
        } catch (Exception e){
            LOG.error("Exception getting ip net to media list: "+e.getMessage(),e);
        } finally {
            context.stop();
        }

        context = m_loadLldpLinksTimer.time();
        try{
            LOG.info("Loading Lldp link");
            getLldpLinks(nodemap, nodesnmpmap,nodeipprimarymap);
            LOG.info("Lldp link loaded");
        } catch (Exception e){
            LOG.error("Exception getting Lldp link: "+e.getMessage(),e);
        } finally {
            context.stop();
        }

        context = m_loadOspfLinksTimer.time();
        try{
            LOG.info("Loading Ospf link");
            getOspfLinks(nodemap,nodesnmpmap,nodeipprimarymap);
            LOG.info("Ospf link loaded");
        } catch (Exception e){
            LOG.error("Exception getting Ospf link: "+e.getMessage(),e);
        } finally {
            context.stop();
        }

        context = m_loadIsisLinksTimer.time();
        try{
            LOG.info("Loading Cdp link");
            getCdpLinks(nodemap,nodesnmpmap,nodeipprimarymap,ipmap);
            LOG.info("Cdp link loaded");
        } catch (Exception e){
            LOG.error("Exception getting Cdp link: "+e.getMessage(),e);
        } finally {
            context.stop();
        }

        context = m_loadCdpLinksTimer.time();
        try{
            LOG.info("Loading IsIs link");
            getIsIsLinks(nodemap,nodesnmpmap,nodeipprimarymap);
            LOG.info("IsIs link loaded");
        } catch (Exception e){
            LOG.error("Exception getting IsIs link: "+e.getMessage(),e);
        } finally {
            context.stop();
        }

        context = m_loadBridgeLinksTimer.time();
        try{
            LOG.info("Loading Bridge link");
            getBridgeLinks(nodemap, nodesnmpmap,macipmap,nodeipmap,nodeipprimarymap);
            LOG.info("Bridge link loaded");
        } catch (Exception e){
            LOG.error("Exception getting Bridge link: "+e.getMessage(),e);
        } finally {
            context.stop();
        }

        context = m_loadNoLinksTimer.time();
        try {
            LOG.debug("loadtopology: adding nodes without links: " + isAddNodeWithoutLink());
            if (isAddNodeWithoutLink()) {
                addNodesWithoutLinks(nodemap,nodeipmap,nodeipprimarymap);
            }
        } finally {
            context.stop();
        }

        LOG.debug("Found {} groups", getGroups().size());
        LOG.debug("Found {} vertices", getVerticesWithoutGroups().size());
        LOG.debug("Found {} edges", getEdges().size());
    }

    protected final Vertex getOrCreateVertex(OnmsNode sourceNode,OnmsIpInterface primary) {
        Vertex source = getVertex(getNamespace(), sourceNode.getNodeId());
        if (source == null) {
            source = getDefaultVertex(sourceNode.getId(),
                                  sourceNode.getSysObjectId(),
                                  sourceNode.getLabel(),
                                  sourceNode.getSysLocation(),
                                  sourceNode.getType(),
                                  primary.isManaged(),
                                  InetAddressUtils.str(primary.getIpAddress()));
            addVertices(source);
        }
        
        return source;
    }

    protected final LinkdEdge connectCloudMacVertices(String targetmac, VertexRef sourceRef, VertexRef targetRef,String nameSpace) {
        SimpleConnector source = new SimpleConnector(sourceRef.getNamespace(), sourceRef.getId()+"-"+targetRef.getId()+"-connector", sourceRef);
        SimpleConnector target = new SimpleConnector(targetRef.getNamespace(), targetRef.getId()+"-"+sourceRef.getId()+"-connector", targetRef);

        LinkdEdge edge = new LinkdEdge(nameSpace, targetRef.getId()+":"+targetmac, source, target);
        edge.setTargetEndPoint(targetmac);
        addEdges(edge);
        
        return edge;
    }

    protected final LinkdEdge connectVertices(BridgePort targetport, VertexRef sourceRef, VertexRef targetRef,String nameSpace) {
        SimpleConnector source = new SimpleConnector(sourceRef.getNamespace(), sourceRef.getId()+"-"+targetRef.getId()+"-connector", sourceRef);
        SimpleConnector target = new SimpleConnector(targetRef.getNamespace(), targetRef.getId()+"-"+sourceRef.getId()+"-connector", targetRef);

        LinkdEdge edge = new LinkdEdge(nameSpace, targetRef.getId()+":"+targetport.getBridgePort(), source, target);
        edge.setTargetNodeid(targetport.getNodeId());
        if (targetport.getBridgePortIfIndex() != null)
            edge.setTargetEndPoint(String.valueOf(targetport.getBridgePortIfIndex()));
        addEdges(edge);
        
        return edge;
    }

    protected final LinkdEdge connectVertices(BridgeMacLink link, VertexRef sourceRef, VertexRef targetRef,String nameSpace) {
        SimpleConnector source = new SimpleConnector(sourceRef.getNamespace(), sourceRef.getId()+"-"+link.getId()+"-connector", sourceRef);
        SimpleConnector target = new SimpleConnector(targetRef.getNamespace(), targetRef.getId()+"-"+link.getId()+"-connector", targetRef);

        LinkdEdge edge = new LinkdEdge(nameSpace, String.valueOf(link.getId()), source, target);
        edge.setSourceNodeid(link.getNode().getId());
        if (link.getBridgePortIfIndex() != null)
            edge.setSourceEndPoint(String.valueOf(link.getBridgePortIfIndex()));
        edge.setTargetEndPoint(String.valueOf(link.getMacAddress()));
        addEdges(edge);
        
        return edge;
    }

    
    protected final LinkdEdge connectVertices(LinkDetail<?> linkdetail, String nameSpace) {
        SimpleConnector source = new SimpleConnector(linkdetail.getSource().getNamespace(), linkdetail.getSource().getId()+"-"+linkdetail.getId()+"-connector", linkdetail.getSource());
        SimpleConnector target = new SimpleConnector(linkdetail.getTarget().getNamespace(), linkdetail.getTarget().getId()+"-"+linkdetail.getId()+"-connector", linkdetail.getTarget());

        LinkdEdge edge = new LinkdEdge(nameSpace, linkdetail.getId(), source, target);
        try {
            edge.setSourceNodeid(Integer.parseInt(linkdetail.getSource().getId()));
        } catch (NumberFormatException e) {
            
        }
        try {
            edge.setTargetNodeid(Integer.parseInt(linkdetail.getTarget().getId()));
        } catch (NumberFormatException e) {
            
        }
        if (linkdetail.getSourceIfIndex() != null)
            edge.setSourceEndPoint(String.valueOf(linkdetail.getSourceIfIndex()));
        if (linkdetail.getTargetIfIndex() != null)
            edge.setTargetEndPoint(String.valueOf(linkdetail.getTargetIfIndex()));
        addEdges(edge);
        
        return edge;
    }

    private void getLldpLinks(Map<Integer, OnmsNode> nodemap, Map<Integer, List<OnmsSnmpInterface>> nodesnmpmap, Map<Integer, OnmsIpInterface> ipprimarymap) {
        // Index the nodes by sysName
        final Map<String, OnmsNode> nodesbysysname = new HashMap<>();
        for (OnmsNode node: nodemap.values()) {
            if (node.getSysName() != null) {
                nodesbysysname.putIfAbsent(node.getSysName(), node);
            }
        }

        // Index the LLDP elements by node id
        Map<Integer, LldpElement> lldpelementmap = new HashMap<Integer, LldpElement>();
        for (LldpElement lldpelement: m_lldpElementDao.findAll()) {
            lldpelementmap.put(lldpelement.getNode().getId(), lldpelement);
        }

        // Pull all of the LLDP links and index them by remote chassis id
        List<LldpLink> allLinks = m_lldpLinkDao.findAll();
        Map<String, List<LldpLink>> linksByRemoteChassisId = new HashMap<>();
        for (LldpLink link : allLinks) {
            final String remoteChassisId = link.getLldpRemChassisId();
            List<LldpLink> linksWithRemoteChassisId = linksByRemoteChassisId.get(remoteChassisId);
            if (linksWithRemoteChassisId == null) {
                linksWithRemoteChassisId = new ArrayList<>();
                linksByRemoteChassisId.put(remoteChassisId, linksWithRemoteChassisId);
            }
            linksWithRemoteChassisId.add(link);
        }

        Set<LldpLinkDetail> combinedLinkDetails = new HashSet<LldpLinkDetail>();
        Set<Integer> parsed = new HashSet<Integer>();
        for (LldpLink sourceLink : allLinks) {
            if (parsed.contains(sourceLink.getId())) {
                continue;
            }
            LOG.debug("loadtopology: lldp link with id '{}' link '{}' ", sourceLink.getId(), sourceLink);
            LldpElement sourceLldpElement = lldpelementmap.get(sourceLink.getNode().getId());
            LldpLink targetLink = null;

            // Limit the candidate links by only choosing those have a remote chassis id matching the chassis id of the source link
            for (LldpLink link : linksByRemoteChassisId.getOrDefault(sourceLldpElement.getLldpChassisId(), Collections.emptyList())) {
                if (parsed.contains(link.getId())) {
                    continue;
                }

                if (sourceLink.getId().intValue() == link.getId().intValue()) {
                    continue;
                }
                LOG.debug("loadtopology: checking lldp link with id '{}' link '{}' ", link.getId(), link);
                LldpElement element = lldpelementmap.get(link.getNode().getId());
                // Compare the chassis id on the other end of the link
                if (!sourceLink.getLldpRemChassisId().equals(element.getLldpChassisId())) {
                    continue;
                }
                boolean bool1 = sourceLink.getLldpRemPortId().equals(link.getLldpPortId()) && link.getLldpRemPortId().equals(sourceLink.getLldpPortId());
                boolean bool3 = sourceLink.getLldpRemPortIdSubType() == link.getLldpPortIdSubType() && link.getLldpRemPortIdSubType() == sourceLink.getLldpPortIdSubType();

                if (bool1 && bool3) {
                    targetLink=link;
                    LOG.info("loadtopology: found lldp mutual link: '{}' and '{}' ", sourceLink,targetLink);
                    break;
                }
            }

            if (targetLink == null && sourceLink.getLldpRemSysname() != null) {
                final OnmsNode node = nodesbysysname.get(sourceLink.getLldpRemSysname());
                if (node != null) {
                    targetLink = reverseLldpLink(node, sourceLldpElement, sourceLink);
                    LOG.info("loadtopology: found lldp link using lldp rem sysname: '{}' and '{}'", sourceLink, targetLink);
                }
            }

            if (targetLink == null) {
                LOG.info("loadtopology: cannot found target node for link: '{}'", sourceLink);
                continue;
            }
                
            parsed.add(sourceLink.getId());
            parsed.add(targetLink.getId());
            Vertex source =  getOrCreateVertex(nodemap.get(sourceLink.getNode().getId()),ipprimarymap.get(sourceLink.getNode().getId()));
            Vertex target = getOrCreateVertex(nodemap.get(targetLink.getNode().getId()),ipprimarymap.get(targetLink.getNode().getId()));
            combinedLinkDetails.add(new LldpLinkDetail(Math.min(sourceLink.getId(), targetLink.getId()) + "|" + Math.max(sourceLink.getId(), targetLink.getId()),
                                                       source, sourceLink, target, targetLink));

        }

        for (LldpLinkDetail linkDetail : combinedLinkDetails) {
            LinkdEdge edge = connectVertices(linkDetail, LLDP_EDGE_NAMESPACE);
            edge.setTooltipText(getEdgeTooltipText(linkDetail,nodesnmpmap));
        }
    }

    private void getOspfLinks(Map<Integer, OnmsNode> nodemap,Map<Integer, List<OnmsSnmpInterface>> nodesnmpmap, Map<Integer, OnmsIpInterface> ipprimarymap) {
        List<OspfLink> allLinks =  getOspfLinkDao().findAll();
        Set<OspfLinkDetail> combinedLinkDetails = new HashSet<OspfLinkDetail>();
        Set<Integer> parsed = new HashSet<Integer>();
        for(OspfLink sourceLink : allLinks) {
            if (parsed.contains(sourceLink.getId())) 
                continue;
            LOG.debug("loadtopology: ospf link with id '{}'", sourceLink.getId());
            for (OspfLink targetLink : allLinks) {
                if (sourceLink.getId().intValue() == targetLink.getId().intValue() || parsed.contains(targetLink.getId())) 
                    continue;
                LOG.debug("loadtopology: checking ospf link with id '{}'", targetLink.getId());
                if(sourceLink.getOspfRemIpAddr().equals(targetLink.getOspfIpAddr()) && targetLink.getOspfRemIpAddr().equals(sourceLink.getOspfIpAddr())) {
                    LOG.info("loadtopology: found ospf mutual link: '{}' and '{}' ", sourceLink,targetLink);
                    parsed.add(sourceLink.getId());
                    parsed.add(targetLink.getId());
                    Vertex source =  getOrCreateVertex(nodemap.get(sourceLink.getNode().getId()),ipprimarymap.get(sourceLink.getNode().getId()));
                    Vertex target = getOrCreateVertex(nodemap.get(targetLink.getNode().getId()),ipprimarymap.get(targetLink.getNode().getId()));
                    OspfLinkDetail linkDetail = new OspfLinkDetail(
                            Math.min(sourceLink.getId(), targetLink.getId()) + "|" + Math.max(sourceLink.getId(), targetLink.getId()),
                            source, sourceLink, target, targetLink);
                    combinedLinkDetails.add(linkDetail);
                    break;
                }
            }
        }

        for (OspfLinkDetail linkDetail : combinedLinkDetails) {
            LinkdEdge edge = connectVertices(linkDetail, OSPF_EDGE_NAMESPACE);
            edge.setTooltipText(getEdgeTooltipText(linkDetail,nodesnmpmap));
        }
    }

    private void getCdpLinks(Map<Integer,OnmsNode> nodemap,Map<Integer, List<OnmsSnmpInterface>> nodesnmpmap, 
            Map<Integer, OnmsIpInterface> ipprimarymap, Map<InetAddress,OnmsIpInterface> ipmap) {
        Map<Integer, CdpElement> cdpelementmap = new HashMap<Integer, CdpElement>();
        for (CdpElement cdpelement: m_cdpElementDao.findAll()) {
            cdpelementmap.put(cdpelement.getNode().getId(), cdpelement);
        }

        List<CdpLink> allLinks = m_cdpLinkDao.findAll();
        Set<CdpLinkDetail> combinedLinkDetails = new HashSet<CdpLinkDetail>();
        Set<Integer> parsed = new HashSet<Integer>();

        for (CdpLink sourceLink : allLinks) {
            if (parsed.contains(sourceLink.getId())) { 
                continue;
            }
            LOG.debug("loadtopology: cdp link with id '{}' link '{}' ", sourceLink.getId(), sourceLink);
            CdpElement sourceCdpElement = cdpelementmap.get(sourceLink.getNode().getId());
            CdpLink targetLink = null;
            for (CdpLink link : allLinks) {
                if (sourceLink.getId().intValue() == link.getId().intValue()|| parsed.contains(link.getId()))
                    continue;
                LOG.debug("loadtopology: checking cdp link with id '{}' link '{}' ", link.getId(), link);
                CdpElement element = cdpelementmap.get(link.getNode().getId());
                //Compare the remote data to the targetNode element data
                if (!sourceLink.getCdpCacheDeviceId().equals(element.getCdpGlobalDeviceId()) || !link.getCdpCacheDeviceId().equals(sourceCdpElement.getCdpGlobalDeviceId())) 
                    continue;

                if (sourceLink.getCdpInterfaceName().equals(link.getCdpCacheDevicePort()) && link.getCdpInterfaceName().equals(sourceLink.getCdpCacheDevicePort())) {
                    targetLink=link;
                    LOG.info("loadtopology: found cdp mutual link: '{}' and '{}' ", sourceLink,targetLink);
                    break;
                }
            }
            
            if (targetLink == null) {
                if (sourceLink.getCdpCacheAddressType() == CiscoNetworkProtocolType.ip) {
                    try {
                        InetAddress targetAddress = InetAddressUtils.addr(sourceLink.getCdpCacheAddress());
                        if (ipmap.containsKey(targetAddress)) {
                            targetLink = reverseCdpLink(ipmap.get(targetAddress), sourceCdpElement, sourceLink ); 
                            LOG.info("loadtopology: found cdp link using cdp cache address: '{}' and '{}'", sourceLink, targetLink);
                        }
                    } catch (Exception e) {
                        LOG.warn("loadtopology: cannot convert ip address: {}", sourceLink.getCdpCacheAddress(), e);
                    }
                }
            }
            
            if (targetLink == null) {
                LOG.info("loadtopology: cannot found target node for link: '{}'", sourceLink);
                continue;
            }
                
            parsed.add(sourceLink.getId());
            parsed.add(targetLink.getId());
            Vertex source =  getOrCreateVertex(nodemap.get(sourceLink.getNode().getId()),ipprimarymap.get(sourceLink.getNode().getId()));
            Vertex target = getOrCreateVertex(nodemap.get(targetLink.getNode().getId()),ipprimarymap.get(targetLink.getNode().getId()));
            combinedLinkDetails.add(new CdpLinkDetail(Math.min(sourceLink.getId(), targetLink.getId()) + "|" + Math.max(sourceLink.getId(), targetLink.getId()),
                                                       source, sourceLink, target, targetLink));

        }
        
        for (CdpLinkDetail linkDetail : combinedLinkDetails) {
            LinkdEdge edge = connectVertices(linkDetail, CDP_EDGE_NAMESPACE);
            edge.setTooltipText(getEdgeTooltipText(linkDetail,nodesnmpmap));
        }
    }
    private void getIsIsLinks(Map<Integer,OnmsNode> nodemap,Map<Integer, List<OnmsSnmpInterface>> nodesnmpmap, Map<Integer, OnmsIpInterface> ipprimarymap){

        Map<Integer, IsIsElement> elementmap = new HashMap<Integer, IsIsElement>();
        for (IsIsElement element: m_isisElementDao.findAll()) {
            elementmap.put(element.getNode().getId(), element);
        }

        List<IsIsLink> isislinks = m_isisLinkDao.findAll();
        Set<IsIsLinkDetail> combinedLinkDetails = new HashSet<IsIsLinkDetail>();
        Set<Integer> parsed = new HashSet<Integer>();

        for (IsIsLink sourceLink : isislinks) {
            if (parsed.contains(sourceLink.getId())) { 
                continue;
            }
            LOG.debug("loadtopology: isis link with id '{}' link '{}' ", sourceLink.getId(), sourceLink);
            IsIsElement sourceElement = elementmap.get(sourceLink.getNode().getId());
            IsIsLink targetLink = null;
            for (IsIsLink link : isislinks) {
                if (sourceLink.getId().intValue() == link.getId().intValue()|| parsed.contains(link.getId()))
                    continue;
                LOG.debug("loadtopology: checking isis link with id '{}' link '{}' ", link.getId(), link);
                IsIsElement targetElement = elementmap.get(link.getNode().getId());
                //Compare the remote data to the targetNode element data
                if (!sourceLink.getIsisISAdjNeighSysID().equals(targetElement.getIsisSysID())  
                        || !link.getIsisISAdjNeighSysID().equals(sourceElement.getIsisSysID())) { 
                    continue;
                }

                if (sourceLink.getIsisISAdjIndex().intValue() == 
                        link.getIsisISAdjIndex().intValue()  ) {
                    targetLink=link;
                    LOG.info("loadtopology: found isis mutual link: '{}' and '{}' ", sourceLink,targetLink);
                    break;
                }
            }
            
            if (targetLink == null) {
                LOG.info("loadtopology: cannot found isis target node for link: '{}'", sourceLink);
                continue;
            }

            parsed.add(sourceLink.getId());
            parsed.add(targetLink.getId());
            Vertex source =  getOrCreateVertex(nodemap.get(sourceLink.getNode().getId()),
                                               ipprimarymap.get(sourceLink.getNode().getId()));
            Vertex target = getOrCreateVertex(nodemap.get(targetLink.getNode().getId()),ipprimarymap.get(targetLink.getNode().getId()));
            combinedLinkDetails.add(new IsIsLinkDetail(Math.min(sourceLink.getId(), targetLink.getId()) + "|" + Math.max(sourceLink.getId(), targetLink.getId()),
                                                       source, sourceLink, target, targetLink));
        }

        for (IsIsLinkDetail linkDetail : combinedLinkDetails) {
            LinkdEdge edge = connectVertices(linkDetail, ISIS_EDGE_NAMESPACE);
            edge.setTooltipText(getEdgeTooltipText(linkDetail,nodesnmpmap));
        }
    }

    
    private void getBridgeLinks(Map<Integer, OnmsNode> nodemap, Map<Integer, List<OnmsSnmpInterface>> nodesnmpmap,Map<String, List<OnmsIpInterface>> macToIpMap,Map<Integer, List<OnmsIpInterface>> ipmap, Map<Integer, OnmsIpInterface> ipprimarymap) throws BridgeTopologyException {
        for (BroadcastDomain domain: m_bridgeTopologyDao.load()) {
            LOG.info("loadtopology: parsing broadcast Domain: ", domain.getBridgeNodesOnDomain());
            for (SharedSegment segment: domain.getSharedSegments()) {
                if (segment.noMacsOnSegment() && SharedSegment.getBridgeBridgeLinks(segment).size() == 1) {
                    for (BridgeBridgeLink link : SharedSegment.getBridgeBridgeLinks(segment)) {
                        Vertex source = getOrCreateVertex(nodemap.get(link.getNode().getId()), ipprimarymap.get(link.getNode().getId()));
                        Vertex target = getOrCreateVertex(nodemap.get(link.getDesignatedNode().getId()), ipprimarymap.get(link.getDesignatedNode().getId()));
                        BridgeLinkDetail detail = new BridgeLinkDetail(EnhancedLinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD,source,link.getBridgePortIfIndex(),  target, link.getDesignatedPortIfIndex(), link.getBridgePort(), link.getDesignatedPort(), link.getId(),link.getId() );
                        LinkdEdge edge = connectVertices(detail, BRIDGE_EDGE_NAMESPACE);
                        edge.setTooltipText(getEdgeTooltipText(detail,nodesnmpmap));
                    }
                    continue;
                } 
                if (SharedSegment.getBridgeMacLinks(segment).size() == 1 && SharedSegment.getBridgeBridgeLinks(segment).size() == 0) {
                    for (BridgeMacLink sourcelink: SharedSegment.getBridgeMacLinks(segment)) {
                        if (macToIpMap.containsKey(sourcelink.getMacAddress()) && macToIpMap.get(sourcelink.getMacAddress()).size() > 0) {
                           List<OnmsIpInterface> targetInterfaces = macToIpMap.get(sourcelink.getMacAddress());
                           OnmsIpInterface targetIp = targetInterfaces.get(0);
                           if (segment.getBridgeIdsOnSegment().contains(targetIp.getNode().getId()))
                               continue;
                           Vertex source = getOrCreateVertex(nodemap.get(sourcelink.getNode().getId()), ipprimarymap.get(sourcelink.getNode().getId()));
                           Vertex target = getOrCreateVertex(nodemap.get(targetIp.getNode().getId()), ipprimarymap.get(targetIp.getNode().getId()));
                           LinkdEdge edge = connectVertices(sourcelink, source, target, BRIDGE_EDGE_NAMESPACE);
                           edge.setTooltipText(getEdgeTooltipText(sourcelink,source,target,targetInterfaces,nodesnmpmap));
                        }
                    }
                    continue;    
                }
                String cloudId = segment.getDesignatedBridge()+":"+segment.getDesignatedPort().getBridgePort();
                AbstractVertex cloudVertex = addVertex(cloudId, 0, 0);
                cloudVertex.setLabel("");
                cloudVertex.setIconKey("cloud");
                cloudVertex.setTooltipText("Shared Segment: " + nodemap.get(segment.getDesignatedBridge()).getLabel() + " port: " + segment.getDesignatedPort().getBridgePort());
                addVertices(cloudVertex);
                LOG.info("loadtopology: adding cloud: id: '{}', {}", cloudId, cloudVertex.getTooltipText() );
                for (BridgePort targetport: segment.getBridgePortsOnSegment()) {
                    Vertex target = getOrCreateVertex(nodemap.get(targetport.getNodeId()), ipprimarymap.get(targetport.getNodeId()));
                    LinkdEdge edge = connectVertices(targetport, cloudVertex, target, BRIDGE_EDGE_NAMESPACE);
                    edge.setTooltipText(getEdgeTooltipText(targetport,target,nodesnmpmap));
                }
                for (String targetmac: segment.getMacsOnSegment()) {
                    if (macToIpMap.containsKey(targetmac) && macToIpMap.get(targetmac).size() > 0) {
                        List<OnmsIpInterface> targetInterfaces = macToIpMap.get(targetmac);
                        OnmsIpInterface targetIp = targetInterfaces.get(0);
                        if (segment.getBridgeIdsOnSegment().contains(targetIp.getNode().getId()))
                                continue;
                        Vertex target = getOrCreateVertex(nodemap.get(targetIp.getNode().getId()), ipprimarymap.get(targetIp.getNode().getId()));
                        LinkdEdge edge = connectCloudMacVertices(targetmac, cloudVertex, target, BRIDGE_EDGE_NAMESPACE);
                        edge.setTooltipText(getEdgeTooltipText(targetmac,target,targetInterfaces));
                    }
                }
            }
        }
    }

    private void addNodesWithoutLinks(Map<Integer, OnmsNode> nodemap, Map<Integer, List<OnmsIpInterface>> nodeipmap, Map<Integer, OnmsIpInterface> nodeipprimarymap) {
        for (Entry<Integer, OnmsNode> entry: nodemap.entrySet()) {
            Integer nodeId = entry.getKey();
            OnmsNode node = entry.getValue();
            if (getVertex(getNamespace(), nodeId.toString()) == null) {
                LOG.debug("Adding link-less node: {}", node.getLabel());
                // Use the primary interface, if set
                OnmsIpInterface ipInterface = nodeipprimarymap.get(nodeId);
                if (ipInterface == null) {
                    // Otherwise fall back to the first interface defined
                    List<OnmsIpInterface> ipInterfaces = nodeipmap.getOrDefault(nodeId, Collections.emptyList());
                    if (ipInterfaces.size() > 0) {
                        ipInterfaces.get(0);
                    }
                }
                addVertices(createVertexFor(node, ipInterface));
            }
        }
    }

    @Override
    @Transactional
    public void refresh() {
        final Timer.Context context = m_loadFullTimer.time();
        try {
            loadCompleteTopology();
        } finally {
            context.stop();
        }
    }

    private String getEdgeTooltipText(BridgeMacLink sourcelink,
            Vertex source, Vertex target,
            List<OnmsIpInterface> targetInterfaces,
            Map<Integer, List<OnmsSnmpInterface>> snmpmap) {
        final StringBuilder tooltipText = new StringBuilder();
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append("Bridge Layer2");
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        OnmsSnmpInterface sourceInterface = getByNodeIdAndIfIndex(sourcelink.getBridgePortIfIndex(), target,snmpmap);
        
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append(source.getLabel());
        if (sourceInterface != null) {
            tooltipText.append("(");
            tooltipText.append(sourceInterface.getIfName());
            tooltipText.append(")");
        }
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append(target.getLabel());
        tooltipText.append("(");
        tooltipText.append(sourcelink.getMacAddress());
        tooltipText.append(")");
        tooltipText.append("(");
        if (targetInterfaces.size() == 1) {
            tooltipText.append(InetAddressUtils.str(targetInterfaces.get(0).getIpAddress()));
        } else if (targetInterfaces.size() > 1) {
            tooltipText.append("Multiple ip Addresses ");
        } else {
            tooltipText.append("No ip Address found");
        }
        tooltipText.append(")");
        tooltipText.append(HTML_TOOLTIP_TAG_END);        

        if ( sourceInterface != null) {
            if (sourceInterface.getIfSpeed() != null) {
                tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
                tooltipText.append(getHumanReadableIfSpeed(sourceInterface.getIfSpeed()));
                tooltipText.append(HTML_TOOLTIP_TAG_END);
            }
        }


        return tooltipText.toString();
    }

    private String getEdgeTooltipText(String mac, Vertex target, List<OnmsIpInterface> ipifaces) {
        final StringBuilder tooltipText = new StringBuilder();
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append("Bridge Layer2");
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append(target.getLabel());
        tooltipText.append("(");
        tooltipText.append(mac);
        tooltipText.append(")");
        tooltipText.append("(");
        if (ipifaces.size() == 1) {
            tooltipText.append(InetAddressUtils.str(ipifaces.get(0).getIpAddress()));
        } else if (ipifaces.size() > 1) {
            tooltipText.append("Multiple ip Addresses ");
        } else {
            tooltipText.append("No ip Address found");
        }
        tooltipText.append(")");
        tooltipText.append(HTML_TOOLTIP_TAG_END);        
        
        return tooltipText.toString();
    }


    private String getEdgeTooltipText(BridgePort port, Vertex target, Map<Integer,List<OnmsSnmpInterface>> snmpmap) {
        final StringBuilder tooltipText = new StringBuilder();
        OnmsSnmpInterface targetInterface = getByNodeIdAndIfIndex(port.getBridgePortIfIndex(), target,snmpmap);
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append("Bridge Layer2");
        tooltipText.append(HTML_TOOLTIP_TAG_END);
        
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append(target.getLabel());
        if (targetInterface != null) {
            tooltipText.append("(");
            tooltipText.append(targetInterface.getIfName());
            tooltipText.append(")");
        }
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        if ( targetInterface != null) {
            if (targetInterface.getIfSpeed() != null) {
                tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
                tooltipText.append(getHumanReadableIfSpeed(targetInterface.getIfSpeed()));
                tooltipText.append(HTML_TOOLTIP_TAG_END);
            }
        }
        
        return tooltipText.toString();
    }

    private String getEdgeTooltipText(LinkDetail<?> linkDetail,Map<Integer,List<OnmsSnmpInterface>> snmpmap) {

        final StringBuilder tooltipText = new StringBuilder();
        Vertex source = linkDetail.getSource();
        Vertex target = linkDetail.getTarget();
        OnmsSnmpInterface sourceInterface = getByNodeIdAndIfIndex(linkDetail.getSourceIfIndex(), source,snmpmap);
        OnmsSnmpInterface targetInterface = getByNodeIdAndIfIndex(linkDetail.getTargetIfIndex(), target,snmpmap);

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append(linkDetail.getType());
        String layerText = " Layer 2";
        if (sourceInterface != null && targetInterface != null) {
            final List<OnmsIpInterface> sourceNonLoopback = sourceInterface.getIpInterfaces().stream().filter(iface -> {
                return !iface.getNetMask().isLoopbackAddress();
            }).collect(Collectors.toList());
            final List<OnmsIpInterface> targetNonLoopback = targetInterface.getIpInterfaces().stream().filter(iface -> {
                return !iface.getNetMask().isLoopbackAddress();
            }).collect(Collectors.toList());

            if (!sourceNonLoopback.isEmpty() && !targetNonLoopback.isEmpty()) {
                // if both the source and target have non-loopback IP interfaces, assume this is a layer3 edge
                layerText = " Layer3/Layer2";
            }
        }
        tooltipText.append(layerText);
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append( source.getLabel());
        if (sourceInterface != null ) {
            tooltipText.append("(");
            tooltipText.append(sourceInterface.getIfName());
            tooltipText.append(")");
        }
        tooltipText.append(HTML_TOOLTIP_TAG_END);
        
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append(target.getLabel());
        if (targetInterface != null) {
            tooltipText.append("(");
            tooltipText.append(targetInterface.getIfName());
            tooltipText.append(")");
        }
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        if ( targetInterface != null) {
            if (targetInterface.getIfSpeed() != null) {
                tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
                tooltipText.append(getHumanReadableIfSpeed(targetInterface.getIfSpeed()));
                tooltipText.append(HTML_TOOLTIP_TAG_END);
            }
        } else if (sourceInterface != null) {
            if (sourceInterface.getIfSpeed() != null) {
                tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
                tooltipText.append(getHumanReadableIfSpeed(sourceInterface.getIfSpeed()));
                tooltipText.append(HTML_TOOLTIP_TAG_END);
            }
        }
        return tooltipText.toString();
    }

    private OnmsSnmpInterface getByNodeIdAndIfIndex(Integer ifIndex, Vertex source, Map<Integer,List<OnmsSnmpInterface>> snmpmap) {
        if(source.getId() != null && StringUtils.isNumeric(source.getId()) && ifIndex != null 
                && snmpmap.containsKey(Integer.parseInt(source.getId()))) {
            for (OnmsSnmpInterface snmpiface: snmpmap.get(Integer.parseInt(source.getId()))) {
                if (ifIndex.intValue() == snmpiface.getIfIndex().intValue())
                    return snmpiface;
            }
        }
        return null;
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

    //Search Provider methods
    @Override
    public String getSearchProviderNamespace() {
        return TOPOLOGY_NAMESPACE_LINKD;
    }

    @Override
    public List<SearchResult> query(SearchQuery searchQuery, GraphContainer graphContainer) {
        //LOG.debug("SearchProvider->query: called with search query: '{}'", searchQuery);

        List<Vertex> vertices = getFilteredVertices();
        List<SearchResult> searchResults = Lists.newArrayList();

        for(Vertex vertex : vertices){
            if(searchQuery.matches(vertex.getLabel())) {
                searchResults.add(new SearchResult(vertex, false, false));
            }
        }

        //LOG.debug("SearchProvider->query: found {} search results.", searchResults.size());
        return searchResults;
    }

    @Override
    public void onFocusSearchResult(SearchResult searchResult, OperationContext operationContext) {

    }

    @Override
    public void onDefocusSearchResult(SearchResult searchResult, OperationContext operationContext) {

    }

    @Override
    public boolean supportsPrefix(String searchPrefix) {
        return AbstractSearchProvider.supportsPrefix("nodes=", searchPrefix);
    }

    @Override
    public Set<VertexRef> getVertexRefsBy(SearchResult searchResult, GraphContainer container) {
        LOG.debug("SearchProvider->getVertexRefsBy: called with search result: '{}'", searchResult);
        org.opennms.features.topology.api.topo.Criteria criterion = findCriterion(searchResult.getId(), container);

        Set<VertexRef> vertices = ((VertexHopCriteria)criterion).getVertices();
        LOG.debug("SearchProvider->getVertexRefsBy: found '{}' vertices.", vertices.size());

        return vertices;
    }

    @Override
    public void addVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        LOG.debug("SearchProvider->addVertexHopCriteria: called with search result: '{}'", searchResult);

        VertexHopCriteria criterion = LinkdHopCriteriaFactory.createCriteria(searchResult.getId(), searchResult.getLabel());
        container.addCriteria(criterion);

        LOG.debug("SearchProvider->addVertexHop: adding hop criteria {}.", criterion);

        logCriteriaInContainer(container);
    }

    @Override
    public void removeVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        LOG.debug("SearchProvider->removeVertexHopCriteria: called with search result: '{}'", searchResult);

        Criteria criterion = findCriterion(searchResult.getId(), container);

        if (criterion != null) {
            LOG.debug("SearchProvider->removeVertexHopCriteria: found criterion: {} for searchResult {}.", criterion, searchResult);
            container.removeCriteria(criterion);
        } else {
            LOG.debug("SearchProvider->removeVertexHopCriteria: did not find criterion for searchResult {}.", searchResult);
        }

        logCriteriaInContainer(container);
    }

    @Override
    public void onCenterSearchResult(SearchResult searchResult, GraphContainer graphContainer) {
        LOG.debug("SearchProvider->onCenterSearchResult: called with search result: '{}'", searchResult);
    }

    @Override
    public void onToggleCollapse(SearchResult searchResult, GraphContainer graphContainer) {
        LOG.debug("SearchProvider->onToggleCollapse: called with search result: '{}'", searchResult);
    }

    @Override
    public SelectionChangedListener.Selection getSelection(List<VertexRef> selectedVertices, ContentType type) {
       return selectionAwareDelegate.getSelection(selectedVertices, type);
    }

    @Override
    public boolean contributesTo(ContentType type) {
        return selectionAwareDelegate.contributesTo(type);
    }

    private org.opennms.features.topology.api.topo.Criteria findCriterion(String resultId, GraphContainer container) {

        org.opennms.features.topology.api.topo.Criteria[] criteria = container.getCriteria();
        for (org.opennms.features.topology.api.topo.Criteria criterion : criteria) {
            if (criterion instanceof LinkdHopCriteria ) {
                String id = ((LinkdHopCriteria) criterion).getId();
                if (id.equals(resultId)) {
                    return criterion;
                }
            }
        }
        return null;
    }

    private void logCriteriaInContainer(GraphContainer container) {
        Criteria[] criteria = container.getCriteria();
        LOG.debug("SearchProvider->addVertexHopCriteria: there are now {} criteria in the GraphContainer.", criteria.length);
        for (Criteria crit : criteria) {
            LOG.debug("SearchProvider->addVertexHopCriteria: criterion: '{}' is in the GraphContainer.", crit);
        }
    }

    private CdpLink reverseCdpLink(OnmsIpInterface iface, CdpElement element, CdpLink link) {
        CdpLink reverseLink = new CdpLink();
        reverseLink.setId(-link.getId());
        reverseLink.setNode(iface.getNode());
        reverseLink.setCdpCacheIfIndex(iface.getIfIndex());
        reverseLink.setCdpInterfaceName(link.getCdpCacheDevicePort());
        reverseLink.setCdpCacheDeviceId(element.getCdpGlobalDeviceId());
        reverseLink.setCdpCacheDevicePort(link.getCdpInterfaceName());
        return reverseLink;
    }
    
    private LldpLink reverseLldpLink(OnmsNode sourcenode, LldpElement element, LldpLink link) {
        LldpLink reverseLink = new LldpLink();
        reverseLink.setId(-link.getId());
        reverseLink.setNode(sourcenode);
        
        reverseLink.setLldpLocalPortNum(0);
        reverseLink.setLldpPortId(link.getLldpRemPortId());
        reverseLink.setLldpPortIdSubType(link.getLldpRemPortIdSubType());
        reverseLink.setLldpPortDescr(link.getLldpRemPortDescr());
        if (link.getLldpRemPortIdSubType() == LldpPortIdSubType.LLDP_PORTID_SUBTYPE_LOCAL) {
            try {
                reverseLink.setLldpPortIfindex(Integer.getInteger(link.getLldpRemPortId()));
            } catch (Exception e) {
                LOG.debug("reverseLldpLink: cannot create ifindex from  LldpRemPortId '{}'", link.getLldpRemPortId());
            }
        }

        reverseLink.setLldpRemChassisId(element.getLldpChassisId());
        reverseLink.setLldpRemChassisIdSubType(element.getLldpChassisIdSubType());
        reverseLink.setLldpRemSysname(element.getLldpSysname());
        
        reverseLink.setLldpRemPortId(link.getLldpPortId());
        reverseLink.setLldpRemPortIdSubType(link.getLldpPortIdSubType());
        reverseLink.setLldpRemPortDescr(link.getLldpPortDescr());
        
        reverseLink.setLldpLinkCreateTime(link.getLldpLinkCreateTime());
        reverseLink.setLldpLinkLastPollTime(link.getLldpLinkLastPollTime());
        
        return reverseLink;
    }
}
