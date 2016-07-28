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

package org.opennms.features.topology.plugins.topo.linkd.internal.providers;

import java.net.InetAddress;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionAware;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractSearchProvider;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.SimpleConnector;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.linkd.internal.EnhancedLinkdSelectionAware;
import org.opennms.features.topology.plugins.topo.linkd.internal.LinkdEdge;
import org.opennms.features.topology.plugins.topo.linkd.internal.LinkdHopCriteria;
import org.opennms.features.topology.plugins.topo.linkd.internal.LinkdHopCriteriaFactory;
import org.opennms.netmgt.model.BridgeMacLink;
import org.opennms.netmgt.model.CdpLink;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OspfLink;
import org.opennms.netmgt.model.PrimaryType;
import org.opennms.netmgt.model.topology.BridgePort;
import org.opennms.netmgt.model.topology.EdgeAlarmStatusSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.collect.Lists;

public abstract class EnhancedLinkdTopologyProvider extends AbstractLinkdTopologyProvider {

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
                    + ((getVertexNamespace() == null) ? 0 : getVertexNamespace().hashCode());
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

    protected class OspfLinkDetail extends LinkDetail<OspfLink>{

        public OspfLinkDetail(String id, Vertex source, OspfLink sourceLink, Vertex target, OspfLink targetLink) {
            super(id, source, sourceLink, target, targetLink);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((getSourceLink() == null) ? 0 : getSourceLink().getId().hashCode()) + ((getTargetLink() == null) ? 0 : getTargetLink().getId().hashCode());
            result = prime * result
                    + ((getVertexNamespace() == null) ? 0 : getVertexNamespace().hashCode());
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

    class IsIsLinkDetail extends LinkDetail<Integer>{


        private final int m_sourceIfindex;
        private final int m_targetIfindex;
        private final int m_sourceLinkId;
        private final int m_targetLinkId;

        public IsIsLinkDetail(String id, Vertex source, int sourceLinkId, Integer sourceIfIndex, Vertex target, int targetLinkId, Integer targetIfIndex) {
            super(id, source, null, target, null);
            m_sourceLinkId = sourceLinkId;
            m_targetLinkId = targetLinkId;
            m_sourceIfindex = sourceIfIndex;
            m_targetIfindex = targetIfIndex;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((getSourceLink() == null) ? 0 : m_sourceLinkId) + ((getTargetLink() == null) ? 0 : m_targetLinkId);
            result = prime * result
                    + ((getVertexNamespace() == null) ? 0 : getVertexNamespace().hashCode());
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
            return m_sourceIfindex;
        }

        @Override
        public Integer getTargetIfIndex() {
            return m_targetIfindex;
        }

        @Override
        public String getType() {
            return "IsIs";
        }
    }

    protected class BridgeLinkDetail extends LinkDetail<Integer> {

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

        public BridgeLinkDetail(String id,String vertexNamespace, Vertex source, Integer sourceIfIndex, Vertex target, Integer targetIfIndex, Integer sourceBridgePort, Integer targetBridgePort,Integer sourceLink, Integer targetLink) {
            super(id, source, sourceLink, target, targetLink);
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
                    + ((getVertexNamespace() == null) ? 0 : getVertexNamespace().hashCode());
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

    protected static Logger LOG = LoggerFactory.getLogger(EnhancedLinkdTopologyProvider.class);

    private SelectionAware selectionAwareDelegate = new EnhancedLinkdSelectionAware();


    private final Timer m_loadFullTimer;
    private final Timer m_loadNodesTimer;
    private final Timer m_loadIpInterfacesTimer;
    private final Timer m_loadSnmpInterfacesTimer;
    private final Timer m_loadLldpLinksTimer;
    private final Timer m_loadNoLinksTimer;

    public EnhancedLinkdTopologyProvider(MetricRegistry registry, String namespace) {
        super(namespace);
        Objects.requireNonNull(registry);
        m_loadFullTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "full"));
        m_loadNodesTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "nodes"));
        m_loadIpInterfacesTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "ipinterfaces"));
        m_loadSnmpInterfacesTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "snmpinterfaces"));
        m_loadLldpLinksTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "links", "lldp"));
        m_loadNoLinksTimer = registry.timer(MetricRegistry.name("enlinkd", "load", "links", "none"));
    }

    @Override
    @Transactional
    public void load(String filename) throws MalformedURLException, JAXBException {
        final Timer.Context context = m_loadFullTimer.time();
        if (filename != null) {
            LOG.warn("Filename that was specified for linkd topology will be ignored: " + filename + ", using " + getConfigurationFile() + " instead");
        }
        try {
            loadCompleteTopology();
        } finally {
            context.stop();
        }
    }

    protected abstract void loadTopology(Map<Integer, OnmsNode> nodemap, Map<Integer, List<OnmsSnmpInterface>> nodesnmpmap, Map<Integer, OnmsIpInterface> nodeipprimarymap, Map<InetAddress, OnmsIpInterface> ipmap);

    private void loadCompleteTopology() throws MalformedURLException, JAXBException {
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

        context = m_loadLldpLinksTimer.time();
        try{
            LOG.info("Loading " + getClass().getSimpleName() + " links");
            loadTopology(nodemap, nodesnmpmap,nodeipprimarymap,ipmap);
            LOG.info("Lldp link loaded");
        } catch (Exception e){
            LOG.error("Exception getting " + getClass().getSimpleName() + " links: " + e.getMessage(),e);
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
        Vertex source = getVertex(getVertexNamespace(), sourceNode.getNodeId());
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

    protected final LinkdEdge connectCloudMacVertices(String targetmac, VertexRef sourceRef, VertexRef targetRef, String nameSpace) {
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
        edge.setTargetNodeid(targetport.getNode().getId());
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

    private void addNodesWithoutLinks(Map<Integer, OnmsNode> nodemap, Map<Integer, List<OnmsIpInterface>> nodeipmap, Map<Integer, OnmsIpInterface> nodeipprimarymap) {
        for (Entry<Integer, OnmsNode> entry: nodemap.entrySet()) {
            Integer nodeId = entry.getKey();
            OnmsNode node = entry.getValue();
            if (getVertex(getVertexNamespace(), nodeId.toString()) == null) {
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
    public void refresh() {
        try {
            load(null);
        } catch (MalformedURLException e) {
            LOG.error(e.getMessage(), e);
        } catch (JAXBException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    protected String getEdgeTooltipText(BridgeMacLink sourcelink,
            Vertex source, Vertex target,
            List<OnmsIpInterface> targetInterfaces,
            Map<Integer, List<OnmsSnmpInterface>> snmpmap) {
        StringBuffer tooltipText = new StringBuffer();
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

    protected String getEdgeTooltipText(String mac, Vertex target, List<OnmsIpInterface> ipifaces) {
        StringBuffer tooltipText = new StringBuffer();
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


    protected String getEdgeTooltipText(BridgePort port, Vertex target, Map<Integer,List<OnmsSnmpInterface>> snmpmap) {
        StringBuffer tooltipText = new StringBuffer();
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

    protected String getEdgeTooltipText(LinkDetail<?> linkDetail,Map<Integer,List<OnmsSnmpInterface>> snmpmap) {

        StringBuffer tooltipText = new StringBuffer();
        Vertex source = linkDetail.getSource();
        Vertex target = linkDetail.getTarget();
        OnmsSnmpInterface sourceInterface = getByNodeIdAndIfIndex(linkDetail.getSourceIfIndex(), source,snmpmap);
        OnmsSnmpInterface targetInterface = getByNodeIdAndIfIndex(linkDetail.getTargetIfIndex(), target,snmpmap);

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append(linkDetail.getType());
        if (sourceInterface != null && targetInterface != null
                && sourceInterface.getNetMask() != null && !sourceInterface.getNetMask().isLoopbackAddress()
                && targetInterface.getNetMask() != null && !targetInterface.getNetMask().isLoopbackAddress()) {
            tooltipText.append(" Layer3/Layer2");
        } else {
            tooltipText.append(" Layer2");
        }
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
                searchResults.add(new SearchResult(vertex));
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
            if (criterion instanceof LinkdHopCriteria) {
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

}
