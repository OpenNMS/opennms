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

import java.io.File;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.apache.commons.lang.StringUtils;
import org.opennms.core.utils.LldpUtils.LldpPortIdSubType;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.browsers.ContentType;
import org.opennms.features.topology.api.browsers.SelectionAware;
import org.opennms.features.topology.api.browsers.SelectionChangedListener;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractSearchProvider;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.api.topo.WrappedGraph;
import org.opennms.features.topology.api.topo.WrappedVertex;
import org.opennms.netmgt.dao.api.BridgeBridgeLinkDao;
import org.opennms.netmgt.dao.api.BridgeMacLinkDao;
import org.opennms.netmgt.dao.api.CdpLinkDao;
import org.opennms.netmgt.dao.api.IsIsLinkDao;
import org.opennms.netmgt.dao.api.LldpElementDao;
import org.opennms.netmgt.dao.api.LldpLinkDao;
import org.opennms.netmgt.dao.api.OspfLinkDao;
import org.opennms.netmgt.model.BridgeBridgeLink;
import org.opennms.netmgt.model.LldpElement;
import org.opennms.netmgt.model.LldpLink;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.opennms.netmgt.model.OspfLink;
import org.opennms.netmgt.model.topology.BridgeMacTopologyLink;
import org.opennms.netmgt.model.topology.CdpTopologyLink;
import org.opennms.netmgt.model.topology.EdgeAlarmStatusSummary;
import org.opennms.netmgt.model.topology.IsisTopologyLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

public class EnhancedLinkdTopologyProvider extends AbstractLinkdTopologyProvider {

    private abstract class LinkDetail<K> {
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

    private class LldpLinkDetail extends LinkDetail<LldpLink> {


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

    private class OspfLinkDetail extends LinkDetail<OspfLink>{

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

    private class IsIsLinkDetail extends LinkDetail<Integer>{


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

    public class BridgeLinkDetail extends LinkDetail<Integer> {

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

    public class CdpLinkDetail extends LinkDetail<Integer>{

        private final Integer m_sourceIfIndex;
        private final String m_sourceIfName;
        private final String m_targetIfName;

        public CdpLinkDetail(String id, Vertex source, Integer sourceIfIndex, String sourceIfName, Vertex target, String targetIfName) {
            super(id, source, null, target, null);
            m_sourceIfIndex = sourceIfIndex;
            m_sourceIfName = sourceIfName;
            m_targetIfName = targetIfName;
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
            return m_sourceIfIndex;
        }

        @Override
        public Integer getTargetIfIndex() {
            return null;
        }

        @Override
        public String getType() { return "CDP"; }

        public String getSourceIfName() {
            return m_sourceIfName;
        }

        public String getTargetIfName() {
            return m_targetIfName;
        }
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
    private OspfLinkDao m_ospfLinkDao;
    private IsIsLinkDao m_isisLinkDao;
    private BridgeBridgeLinkDao m_bridgeBridgeLinkDao;
    private BridgeMacLinkDao m_bridgeMacLinkDao;
    private CdpLinkDao m_cdpLinkDao;
    private SelectionAware selectionAwareDelegate = new EnhancedLinkdSelectionAware();
    public final static String LLDP_EDGE_NAMESPACE = TOPOLOGY_NAMESPACE_LINKD + "::LLDP";
    public final static String OSPF_EDGE_NAMESPACE = TOPOLOGY_NAMESPACE_LINKD + "::OSPF";
    public final static String ISIS_EDGE_NAMESPACE = TOPOLOGY_NAMESPACE_LINKD + "::ISIS";
    public final static String BRIDGE_EDGE_NAMESPACE = TOPOLOGY_NAMESPACE_LINKD + "::BRIDGE";
    public final static String CDP_EDGE_NAMESPACE = TOPOLOGY_NAMESPACE_LINKD + "::CDP";

    public EnhancedLinkdTopologyProvider() { }

    /**
     * Used as an init-method in the OSGi blueprint
     * @throws JAXBException
     * @throws MalformedURLException
     */
    public void onInit() throws MalformedURLException, JAXBException {
        LOG.debug("init: loading enlinkd topology.");
        try {
            // @see http://issues.opennms.org/browse/NMS-7835
            getTransactionOperations().execute(new TransactionCallbackWithoutResult() {
                @Override
                public void doInTransactionWithoutResult(TransactionStatus status) {
                    try {
                        load(null);
                    } catch (MalformedURLException | JAXBException e) {
                        throw new UndeclaredThrowableException(e);
                    }
                }
            });
        } catch (UndeclaredThrowableException e) {
            // I'm not sure if there's a more elegant way to do this...
            Throwable t = e.getUndeclaredThrowable();
            if (t instanceof MalformedURLException) {
                throw (MalformedURLException)t;
            } else if (t instanceof JAXBException) {
                throw (JAXBException)t;
            }
        }
    }

    @Override
    @Transactional
    public void load(String filename) throws MalformedURLException, JAXBException {
        if (filename != null) {
            LOG.warn("Filename that was specified for linkd topology will be ignored: " + filename + ", using " + getConfigurationFile() + " instead");
        }
        try{
            //TODO: change to one query from the database that will return all links plus elements joined
            //This reset container is set in here for the demo, don't commit

            resetContainer();
        } catch (Exception e){
            LOG.error("Exception reset Container: "+e.getMessage(),e);
        }
        
        Map<Integer, OnmsNode> nodemap = new HashMap<Integer, OnmsNode>();

        try {
            for (OnmsNode node: m_nodeDao.findAll()) {
                nodemap.put(node.getId(), node);
            }
        } catch (Exception e){
            LOG.error("Exception getting node list: "+e.getMessage(),e);
        }

        try{
            getLldpLinks(nodemap);
        } catch (Exception e){
            LOG.error("Exception getting Lldp link: "+e.getMessage(),e);
        }
        try{
            getOspfLinks(nodemap);
        } catch (Exception e){
            LOG.error("Exception getting Ospf link: "+e.getMessage(),e);
        }
        try{
            getIsIsLinks();
        } catch (Exception e){
            LOG.error("Exception getting IsIs link: "+e.getMessage(),e);
        }
        try{
            getBridgeLinks(nodemap);
        } catch (Exception e){
            LOG.error("Exception getting Bridge link: "+e.getMessage(),e);
        }
        try{
            getCdpLinks();
        } catch (Exception e){
            LOG.error("Exception getting Cdp link: "+e.getMessage(),e);
        }

        LOG.debug("loadtopology: adding nodes without links: " + isAddNodeWithoutLink());
        if (isAddNodeWithoutLink()) {
            addNodesWithoutLinks();
        }

        File configFile = new File(getConfigurationFile());
        if (configFile.exists() && configFile.canRead()) {
            LOG.debug("loadtopology: loading topology from configuration file: " + getConfigurationFile());
            WrappedGraph graph = getGraphFromFile(configFile);

            // Add all groups to the topology
            for (WrappedVertex eachVertexInFile: graph.m_vertices) {
                if (eachVertexInFile.group) {
                    LOG.debug("loadtopology: adding group to topology: " + eachVertexInFile.id);
                    if (eachVertexInFile.namespace == null) {
                        eachVertexInFile.namespace = getVertexNamespace();
                        LoggerFactory.getLogger(this.getClass()).warn("Setting namespace on vertex to default: {}", eachVertexInFile);
                    }
                    if (eachVertexInFile.id == null) {
                        LoggerFactory.getLogger(this.getClass()).warn("Invalid vertex unmarshalled from {}: {}", getConfigurationFile(), eachVertexInFile);
                    }
                    AbstractVertex newGroupVertex = addGroup(eachVertexInFile.id, eachVertexInFile.iconKey, eachVertexInFile.label);
                    newGroupVertex.setIpAddress(eachVertexInFile.ipAddr);
                    newGroupVertex.setLocked(eachVertexInFile.locked);
                    if (eachVertexInFile.nodeID != null) newGroupVertex.setNodeID(eachVertexInFile.nodeID);
                    if (!newGroupVertex.equals(eachVertexInFile.parent)) newGroupVertex.setParent(eachVertexInFile.parent);
                    newGroupVertex.setSelected(eachVertexInFile.selected);
                    newGroupVertex.setStyleName(eachVertexInFile.styleName);
                    newGroupVertex.setTooltipText(eachVertexInFile.tooltipText);
                    if (eachVertexInFile.x != null) newGroupVertex.setX(eachVertexInFile.x);
                    if (eachVertexInFile.y != null) newGroupVertex.setY(eachVertexInFile.y);
                }
            }
            for (Vertex vertex: getVertices()) {
                if (vertex.getParent() != null && !vertex.equals(vertex.getParent())) {
                    LOG.debug("loadtopology: setting parent of " + vertex + " to " + vertex.getParent());
                    setParent(vertex, vertex.getParent());
                }
            }
            // Add all children to the specific group
            // Attention: We ignore all other attributes, they do not need to be merged!
            for (WrappedVertex eachVertexInFile : graph.m_vertices) {
                if (!eachVertexInFile.group && eachVertexInFile.parent != null) {
                    final Vertex child = getVertex(eachVertexInFile);
                    final Vertex parent = getVertex(eachVertexInFile.parent);
                    if (child == null || parent == null) continue;
                    LOG.debug("loadtopology: setting parent of " + child + " to " + parent);
                    if (!child.equals(parent)) setParent(child, parent);
                }
            }
        } else {
            LOG.debug("loadtopology: could not load topology configFile:" + getConfigurationFile());
        }
        LOG.debug("Found " + getGroups().size() + " groups");
        LOG.debug("Found " + getVerticesWithoutGroups().size() + " vertices");
        LOG.debug("Found " + getEdges().size() + " edges");



    }

    private void getOspfLinks(Map<Integer, OnmsNode> nodemap) {
        List<OspfLink> allLinks =  getOspfLinkDao().findAll();
        Set<OspfLinkDetail> combinedLinkDetails = new HashSet<OspfLinkDetail>();
        Set<Integer> parsed = new HashSet<Integer>();
        for(OspfLink sourceLink : allLinks) {
            if (parsed.contains(sourceLink.getId())) {
                LOG.debug("loadtopology: ospf link with id '{}' already parsed, skipping", sourceLink.getId());
                continue;
            }
            LOG.debug("loadtopology: ospf link with id '{}'", sourceLink.getId());
            OnmsNode sourcenode = nodemap.get(sourceLink.getNode().getId());
            Vertex source = getVertex(getVertexNamespace(),sourcenode.getNodeId());
            if (source == null) {
                source = getDefaultVertex(sourceLink.getNode().getId(), sourcenode.getSysObjectId(), sourcenode.getLabel(), 
                                          sourcenode.getSysLocation(), sourcenode.getType());
                addVertices(source);
            }
            for (OspfLink targetLink : allLinks) {
                if (sourceLink.getId().intValue() == targetLink.getId().intValue() || parsed.contains(targetLink.getId())) 
                    continue;
                LOG.debug("loadtopology: checking ospf link with id '{}'", targetLink.getId());
                if(sourceLink.getOspfRemIpAddr().equals(targetLink.getOspfIpAddr()) && targetLink.getOspfRemIpAddr().equals(sourceLink.getOspfIpAddr())) {
                    OnmsNode targetnode = nodemap.get(targetLink.getNode().getId());
                    Vertex target = getVertex(getVertexNamespace(),targetnode.getNodeId());
                                       if (target == null) {
                        target = getDefaultVertex(targetnode.getId(), targetnode.getSysObjectId(), targetnode.getLabel(), 
                                                  targetnode.getSysLocation(), targetnode.getType());
                        addVertices(target);
                    }
                    OspfLinkDetail linkDetail = new OspfLinkDetail(
                            Math.min(sourceLink.getId(), targetLink.getId()) + "|" + Math.max(sourceLink.getId(), targetLink.getId()),
                            source, sourceLink, target, targetLink);
                    combinedLinkDetails.add(linkDetail);
                    parsed.add(sourceLink.getId());
                    parsed.add(targetLink.getId());
                    break;
                }
            }
        }

        for (OspfLinkDetail linkDetail : combinedLinkDetails) {
            AbstractEdge edge = connectVertices(linkDetail.getId(), linkDetail.getSource(), linkDetail.getTarget(), OSPF_EDGE_NAMESPACE);
            edge.setTooltipText(getEdgeTooltipText(linkDetail));
        }
    }

    private void getLldpLinks(Map<Integer, OnmsNode> nodemap) {
        Map<Integer, LldpElement> lldpelementmap = new HashMap<Integer, LldpElement>();
        for (LldpElement lldpelement: m_lldpElementDao.findAll()) {
            lldpelementmap.put(lldpelement.getNode().getId(), lldpelement);
        }
        List<LldpLink> allLinks = m_lldpLinkDao.findAll();
        Set<LldpLinkDetail> combinedLinkDetails = new HashSet<LldpLinkDetail>();
        Set<Integer> parsed = new HashSet<Integer>();
        for (LldpLink sourceLink : allLinks) {
            if (parsed.contains(sourceLink.getId())) {
                LOG.debug("loadtopology: lldp link with id '{}' already parsed, skipping", sourceLink.getId());
                continue;
            }
            LOG.debug("loadtopology: lldp link with id '{}' link '{}' ", sourceLink.getId(), sourceLink);
            OnmsNode sourceNode = nodemap.get(sourceLink.getNode().getId());
            Vertex source = getVertex(getVertexNamespace(), sourceNode.getNodeId());
            if (source == null) {
                source = getDefaultVertex(sourceNode.getId(),
                                      sourceNode.getSysObjectId(),
                                      sourceNode.getLabel(),
                                      sourceNode.getSysLocation(),
                                      sourceNode.getType());
                addVertices(source);
            }

            LldpElement sourceLldpElement = lldpelementmap.get(sourceLink.getNode().getId());
            LldpLink targetLink = null;
            for (LldpLink link : allLinks) {
                if (sourceLink.getId().intValue() == link.getId().intValue()|| parsed.contains(link.getId()))
                    continue;
                LOG.debug("loadtopology: checking lldp link with id '{}' link '{}' ", link.getId(), link);
                LldpElement element = lldpelementmap.get(link.getNode().getId());
                //Compare the remote data to the targetNode element data
                if (!sourceLink.getLldpRemChassisId().equals(element.getLldpChassisId()) || !link.getLldpRemChassisId().equals(sourceLldpElement.getLldpChassisId())) 
                    continue;
                boolean bool1 = sourceLink.getLldpRemPortId().equals(link.getLldpPortId()) && link.getLldpRemPortId().equals(sourceLink.getLldpPortId());
                boolean bool3 = sourceLink.getLldpRemPortIdSubType() == link.getLldpPortIdSubType() && link.getLldpRemPortIdSubType() == sourceLink.getLldpPortIdSubType();

                if (bool1 && bool3) {
                    targetLink=link;
                    LOG.debug("loadtopology: found lldp mutual link: '{}' and '{}' ", sourceLink,targetLink);
                    break;
                }
            }
            
            if (targetLink == null) {
                List<OnmsNode> nodes = new ArrayList<OnmsNode>();
                for (OnmsNode node: nodemap.values()) {
                    if (node.getSysName() != null && sourceLink.getLldpRemSysname() != null && node.getSysName().equals(sourceLink.getLldpRemSysname()))
                        nodes.add(node);
                }
                if (nodes.size() == 1) {
                    targetLink = reverseLldpLink(nodes.get(0), sourceLldpElement, sourceLink); 
                    LOG.debug("loadtopology: found lldp link using lldp rem sysname: '{}' and '{}'", sourceLink, targetLink);
                }
            }
            
            if (targetLink == null) {
                LOG.debug("loadtopology: cannot found target node for link: '{}'", sourceLink);
                continue;
            }
                
            parsed.add(sourceLink.getId());
            parsed.add(targetLink.getId());
            
            OnmsNode targetNode = nodemap.get(targetLink.getNode().getId());
            Vertex target = getVertex(getVertexNamespace(), targetNode.getNodeId());
            if (target == null) {
                target = getDefaultVertex(targetNode.getId(),
                                          targetNode.getSysObjectId(),
                                          targetNode.getLabel(),
                                        targetNode.getSysLocation(),
                                        targetNode.getType());
                addVertices(target);
            }
            combinedLinkDetails.add(new LldpLinkDetail(Math.min(sourceLink.getId(), targetLink.getId()) + "|" + Math.max(sourceLink.getId(), targetLink.getId()),
                                                       source, sourceLink, target, targetLink));

        }

        for (LldpLinkDetail linkDetail : combinedLinkDetails) {
            AbstractEdge edge = connectVertices(linkDetail.getId(), linkDetail.getSource(), linkDetail.getTarget(), LLDP_EDGE_NAMESPACE);
            edge.setTooltipText(getEdgeTooltipText(linkDetail));
        }
    }

    private void getCdpLinks() {
        List<CdpTopologyLink> cdpLinks = m_cdpLinkDao.findLinksForTopology();

        if (cdpLinks != null && cdpLinks.size() > 0) {
            for (CdpTopologyLink link : cdpLinks) {
                LOG.debug("loadtopology: adding cdp link: '{}'", link );
                String id = Math.min(link.getSourceId(), link.getTargetId()) + "|" + Math.max(link.getSourceId(), link.getTargetId());
                Vertex source = getVertex(getVertexNamespace(), link.getSrcNodeId().toString());
                if (source == null) {
                    source = getDefaultVertex(link.getSrcNodeId(),
                                              link.getSrcSysoid(),
                                              link.getSrcLabel(),
                                            link.getSrcLocation(),
                                        link.getSrcNodeType());
                    addVertices(source);
                }
                Vertex target = getVertex(getVertexNamespace(), link.getTargetNodeId().toString());
                if (target == null) {
                    target = getDefaultVertex(link.getTargetNodeId(),
                                              link.getTargetSysoid(),
                                              link.getTargetLabel(),
                                            link.getTargetLocation(),
                                            link.getTargetNodeType());
                    addVertices(target);
                }
                CdpLinkDetail linkDetail = new CdpLinkDetail(id,
                        source,
                        link.getSrcIfIndex(),
                        link.getSrcIfName(),
                        target,
                        link.getTargetIfName());

                AbstractEdge edge = connectVertices(linkDetail.getId(), linkDetail.getSource(), linkDetail.getTarget(), CDP_EDGE_NAMESPACE);
                edge.setTooltipText(getEdgeTooltipText(linkDetail));
            }
        }
    }

    private void getIsIsLinks(){
        List<IsisTopologyLink> isislinks = m_isisLinkDao.getLinksForTopology();

        if (isislinks != null && isislinks.size() > 0) {
            for (IsisTopologyLink link : isislinks) {
                LOG.debug("loadtopology: adding isis link: '{}'", link );
                String id = Math.min(link.getSourceId(), link.getTargetId()) + "|" + Math.max(link.getSourceId(), link.getTargetId());
                Vertex source = getVertex(getVertexNamespace(), link.getSrcNodeId().toString());
                if (source == null) {
                     source = getDefaultVertex(link.getSrcNodeId(),
                                       link.getSrcSysoid(),
                                       link.getSrcLabel(),
                                     link.getSrcLocation(),
                                     link.getSrcNodeType());
                    addVertices(source);

                }
                Vertex target = getVertex(getVertexNamespace(), link.getTargetNodeId().toString());
                if (target == null) {
                    target = getDefaultVertex(link.getTargetNodeId(),
                                       link.getTargetSysoid(),
                                       link.getTargetLabel(),
                                         link.getTargetLocation(),
                                         link.getTargetNodeType());
                    addVertices(target);
                }
                IsIsLinkDetail linkDetail = new IsIsLinkDetail(
                        id,
                        source,
                        link.getSourceId(),
                        link.getSrcIfIndex(),
                        target,
                        link.getTargetId(),
                        link.getTargetIfIndex()
                );

                AbstractEdge edge = connectVertices(linkDetail.getId(), linkDetail.getSource(), linkDetail.getTarget(), ISIS_EDGE_NAMESPACE);
                edge.setTooltipText(getEdgeTooltipText(linkDetail));
            }
        }
    }

    private void getBridgeLinks(Map<Integer, OnmsNode> nodemap){
        // parse bridge bridge link simple connection
        for (BridgeBridgeLink link : m_bridgeBridgeLinkDao.findAll()) {
            OnmsNode sourceNode = nodemap.get(link.getNode().getId());
            Vertex source = getVertex(getVertexNamespace(), sourceNode.getNodeId());
            if (source==null) {
                source = getDefaultVertex(sourceNode.getId(), sourceNode.getSysObjectId(), sourceNode.getLabel(),sourceNode.getSysDescription(),sourceNode.getType());
                addVertices(source);
           }
            OnmsNode targetNode = nodemap.get(link.getDesignatedNode().getId());
            Vertex target = getVertex(getVertexNamespace(), targetNode.getNodeId());
            if (target == null) {
                target = getDefaultVertex(targetNode.getId(), targetNode.getSysObjectId(), targetNode.getLabel(),targetNode.getSysDescription(),targetNode.getType());
                addVertices(target);
            }
            BridgeLinkDetail detail = new BridgeLinkDetail(EnhancedLinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD,source,link.getBridgePortIfIndex(),  target, link.getDesignatedPortIfIndex(), link.getBridgePort(), link.getDesignatedPort(), link.getId(),link.getId() );
           AbstractEdge edge = connectVertices(detail.getId(), detail.getSource(), detail.getTarget(), BRIDGE_EDGE_NAMESPACE);
           edge.setTooltipText(getEdgeTooltipText(detail));
        }

        // parse instead inherited multi bridge links...
        // here we must use a cloud to connect vertexes
        Map<String, String> mactocloud = new HashMap<String, String>(); 
        int cloudindex = 0;
        for (BridgeMacTopologyLink link : m_bridgeMacLinkDao.getAllBridgeLinksToBridgeNodes()) {
            
            String sourceLinkId=link.getSrcNodeId()+":"+link.getBridgePort();
            String targetLinkId=link.getTargetNodeId()+":"+link.getTargetBridgePort();
            if (link.getBridgePortIfIndex() != null)
                    sourceLinkId = link.getSrcNodeId()+":"+link.getBridgePortIfIndex();
            if (link.getTargetIfIndex() != null)
                 targetLinkId=link.getTargetNodeId()+":"+link.getTargetIfIndex();

            if (mactocloud.containsKey(sourceLinkId) && mactocloud.containsKey(targetLinkId)) {
                mactocloud.put(link.getMacAddr(), mactocloud.get(sourceLinkId));
                continue;
            }
            
            if (mactocloud.containsKey(sourceLinkId) && !mactocloud.containsKey(targetLinkId)) {
                Vertex cloudVertex = getVertex(getVertexNamespace(), mactocloud.get(sourceLinkId));
                Vertex target = getVertex(getVertexNamespace(), link.getTargetNodeId().toString());
                if (target == null) {
                    target = getDefaultVertex(link.getTargetNodeId(),
                                       link.getTargetSysoid(),
                                       link.getTargetLabel(),
                                     link.getTargetLocation(),
                                     link.getTargetNodeType());
                    addVertices(target);
                }
                Edge edge = connectVertices(EdgeAlarmStatusSummary.getDefaultEdgeId(link.getTargetId(), link.getTargetId()), cloudVertex, target, BRIDGE_EDGE_NAMESPACE);
                edge.setTooltipText(getEdgeVertexCloudToolTipTextFromTarget(target, link, cloudVertex.getTooltipText()));

                mactocloud.put(link.getMacAddr(), mactocloud.get(sourceLinkId));
                mactocloud.put(targetLinkId, mactocloud.get(sourceLinkId));
                continue;
            }
            
            if (!mactocloud.containsKey(sourceLinkId) && mactocloud.containsKey(targetLinkId)) {
                Vertex cloudVertex = getVertex(getVertexNamespace(), mactocloud.get(targetLinkId));
                Vertex source = getVertex(getVertexNamespace(), link.getSrcNodeId().toString());
                if (source == null) {
                    source = getDefaultVertex(link.getSrcNodeId(),
                                       link.getSrcSysoid(),
                                       link.getSrcLabel(),
                                     link.getSrcLocation(),
                                     link.getSrcNodeType());
                    addVertices(source);
                }
                Edge edge = connectVertices(EdgeAlarmStatusSummary.getDefaultEdgeId(link.getId(), link.getId()), cloudVertex, source, BRIDGE_EDGE_NAMESPACE);
                edge.setTooltipText(getEdgeVertexCloudToolTipTextFromSource(source, link, cloudVertex.getTooltipText()));

                mactocloud.put(link.getMacAddr(), mactocloud.get(targetLinkId));
                mactocloud.put(sourceLinkId, mactocloud.get(targetLinkId));
                continue;
            }

            Vertex source = getVertex(getVertexNamespace(), link.getSrcNodeId().toString());
            if (source == null) {
                source = getDefaultVertex(link.getSrcNodeId(),
                                   link.getSrcSysoid(),
                                   link.getSrcLabel(),
                                 link.getSrcLocation(),
                                 link.getSrcNodeType());
                addVertices(source);
            }
            Vertex target = getVertex(getVertexNamespace(), link.getTargetNodeId().toString());
            if (target == null) {
                target = getDefaultVertex(link.getTargetNodeId(),
                                   link.getTargetSysoid(),
                                   link.getTargetLabel(),
                                 link.getTargetLocation(),
                                 link.getTargetNodeType());
                addVertices(target);
            }
            if (mactocloud.containsKey(link.getMacAddr())) {
                Vertex cloudVertex = getVertex(getVertexNamespace(), mactocloud.get(link.getMacAddr()));
                Edge edge1 = connectVertices(EdgeAlarmStatusSummary.getDefaultEdgeId(link.getId(), link.getId()), cloudVertex, source, BRIDGE_EDGE_NAMESPACE);
                edge1.setTooltipText(getEdgeVertexCloudToolTipTextFromSource(source, link, cloudVertex.getTooltipText()));
                Edge edge2 = connectVertices(EdgeAlarmStatusSummary.getDefaultEdgeId(link.getTargetId(), link.getTargetId()), cloudVertex, target, BRIDGE_EDGE_NAMESPACE);
                edge2.setTooltipText(getEdgeVertexCloudToolTipTextFromTarget(target, link,cloudVertex.getTooltipText()));
                mactocloud.put(sourceLinkId, mactocloud.get(link.getMacAddr()));
                mactocloud.put(targetLinkId, mactocloud.get(link.getMacAddr()));
                continue;
            }
            
            String cloudId = "Cloud:"+cloudindex;
            AbstractVertex cloudVertex = addVertex(cloudId, 0, 0);
            cloudVertex.setLabel("");
            cloudVertex.setIconKey("cloud");
            cloudVertex.setTooltipText("Cloud Representing a Shared Segment connecting switches");
            addVertices(cloudVertex);

            Edge edge1 = connectVertices(EdgeAlarmStatusSummary.getDefaultEdgeId(link.getId(), link.getId()), cloudVertex, source, BRIDGE_EDGE_NAMESPACE);
            edge1.setTooltipText(getEdgeVertexCloudToolTipTextFromSource(source, link,cloudVertex.getTooltipText()));
            Edge edge2 = connectVertices(EdgeAlarmStatusSummary.getDefaultEdgeId(link.getTargetId(), link.getTargetId()), cloudVertex, target, BRIDGE_EDGE_NAMESPACE);
            edge2.setTooltipText(getEdgeVertexCloudToolTipTextFromTarget(target, link, cloudVertex.getTooltipText()));
            mactocloud.put(link.getMacAddr(), cloudId);
            mactocloud.put(sourceLinkId, cloudId);
            mactocloud.put(targetLinkId, cloudId);
            cloudindex++;
        }
        
        // now we parse the linkd from switches to hosts
        Multimap<String, BridgeMacTopologyLink> multimap = HashMultimap.create();
        for (BridgeMacTopologyLink macLink : m_bridgeMacLinkDao.getAllBridgeLinksToIpAddrToNodes()) {
            String sourceid = String.valueOf(macLink.getSrcNodeId()) + ":" +String.valueOf(macLink.getBridgePort());
            if (macLink.getBridgePortIfIndex() != null )
                sourceid = String.valueOf(macLink.getSrcNodeId()) + ":" +String.valueOf(macLink.getBridgePortIfIndex());
            multimap.put(sourceid, macLink);
        }

        
        for (String key : multimap.keySet()){
            Collection<BridgeMacTopologyLink> links = multimap.get(key);
            if (links.size() == 1) {
                BridgeMacTopologyLink link = links.iterator().next();
                String edgeId = String.valueOf(link.getId())+ "|" + String.valueOf(link.getTargetId());
                Vertex target = getVertex(getVertexNamespace(), link.getTargetNodeId().toString());
                if (target == null) {
                    target = getDefaultVertex(link.getTargetNodeId(),
                                       link.getTargetSysoid(),
                                       link.getTargetLabel(),
                                     link.getTargetLocation(),
                                     link.getTargetNodeType());
                    addVertices(target);
                }

                if (mactocloud.containsKey(link.getMacAddr())) {
                    Vertex cloudVertex = getVertex(getVertexNamespace(), mactocloud.get(link.getMacAddr()));
                    Edge edge = connectVertices(edgeId, cloudVertex, target, BRIDGE_EDGE_NAMESPACE);
                    edge.setTooltipText(getEdgeVertexCloudToolTipTextFromTarget(target, link,cloudVertex.getTooltipText()));
                    continue;
                }
                Vertex source = getVertex(getVertexNamespace(), link.getSrcNodeId().toString());
                if (source == null) {
                    source = getDefaultVertex(link.getSrcNodeId(),
                                       link.getSrcSysoid(),
                                       link.getSrcLabel(),
                                     link.getSrcLocation(),
                                     link.getSrcNodeType());
                    addVertices(source);
                }
                BridgeLinkDetail detail = new BridgeLinkDetail(edgeId,EnhancedLinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD,source, link.getBridgePortIfIndex(), target, link.getTargetIfIndex(), link.getBridgePort(), link.getTargetBridgePort(),link.getId(),link.getTargetId());
                AbstractEdge edge = connectVertices(detail.getId(), detail.getSource(), detail.getTarget(), BRIDGE_EDGE_NAMESPACE);
                edge.setTooltipText(getEdgeTooltipText(detail));
                continue;
            }
            // This is a multi link. Means that we have multiple and node on the same bridge port
            // The connection is with a cloud
            if (mactocloud.containsKey(key)) {
                Vertex cloudVertex = getVertex(getVertexNamespace(), mactocloud.get(mactocloud.get(key)));
                for (BridgeMacTopologyLink link : multimap.get(key)) {
                    String edgeId = String.valueOf(link.getId())+ "|" + String.valueOf(link.getTargetId());
                    Vertex target = getVertex(getVertexNamespace(), link.getTargetNodeId().toString());
                    if (target == null) {
                        target = getDefaultVertex(link.getTargetNodeId(),
                                           link.getTargetSysoid(),
                                           link.getTargetLabel(),
                                         link.getTargetLocation(),
                                         link.getTargetNodeType());
                        addVertices(target);
                    }
                    AbstractEdge edge2 = connectVertices(edgeId, cloudVertex, target, BRIDGE_EDGE_NAMESPACE);
                    edge2.setTooltipText(getEdgeVertexCloudToolTipTextFromTarget(target, link,cloudVertex.getTooltipText()));
                }
                continue; // next multimap key
            }

            // well we do not have yet a cloud......for the specified node:bridgeport
            String[] keyParts = key.split(":");
            String sourceNodeId = keyParts[0];
            String bridgePort = keyParts[1];
            BridgeMacTopologyLink link1 = multimap.get(key).iterator().next();
            Vertex parentVertex = getVertex(getVertexNamespace(), sourceNodeId);
            if (parentVertex == null) {
                parentVertex = getDefaultVertex(link1.getSrcNodeId(),
                                   link1.getSrcSysoid(),
                                   link1.getSrcLabel(),
                                 link1.getSrcLocation(),
                                 link1.getSrcNodeType());
                addVertices(parentVertex);
            }
            
            AbstractVertex    cloudVertex = addVertex(key, 0, 0);
            cloudVertex.setLabel("");
            cloudVertex.setIconKey("cloud");
            cloudVertex.setTooltipText("Cloud Representing the Shared Segment connecting to switch: " + parentVertex.getLabel() + " bridge port: " + bridgePort);

            Edge edge = connectVertices(EdgeAlarmStatusSummary.getDefaultEdgeId(link1.getId(), link1.getId()), cloudVertex, parentVertex, BRIDGE_EDGE_NAMESPACE);
            edge.setTooltipText(getEdgeVertexCloudToolTipTextFromSource(parentVertex, link1,cloudVertex.getTooltipText()));
            
            for (BridgeMacTopologyLink link : multimap.get(key)) {
                String edgeId = String.valueOf(link.getId())+ "|" + String.valueOf(link.getTargetId());
                Vertex target = getVertex(getVertexNamespace(), link.getTargetNodeId().toString());
                if (target == null) {
                    target = getDefaultVertex(link.getTargetNodeId(),
                                       link.getTargetSysoid(),
                                       link.getTargetLabel(),
                                     link.getTargetLocation(),
                                     link.getTargetNodeType());
                    addVertices(target);
                }
                AbstractEdge edge2 = connectVertices(edgeId, cloudVertex, target, BRIDGE_EDGE_NAMESPACE);
                edge2.setTooltipText(getEdgeVertexCloudToolTipTextFromTarget(target, link,cloudVertex.getTooltipText()));
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

    private String getEdgeVertexCloudToolTipTextFromSource(Vertex vertex, BridgeMacTopologyLink link, String cloudText) {
        if (link.getBridgePortIfName() != null)
            return getEdgeVertexCloudTooltipText(vertex, " port with ifname: " + link.getBridgePortIfName(), cloudText);
        if (link.getBridgePortIfIndex() != null)
            return getEdgeVertexCloudTooltipText(vertex, " port with ifindex: " + String.valueOf(link.getBridgePortIfIndex()), cloudText);
        return getEdgeVertexCloudTooltipText(vertex, " bridge port : " + String.valueOf(link.getBridgePort()), cloudText);
    }

    private String getEdgeVertexCloudToolTipTextFromTarget(Vertex vertex, BridgeMacTopologyLink link, String cloudText) {
        if (link.getTargetBridgePort() != null && link.getTargetPortIfName() != null)
            return getEdgeVertexCloudTooltipText(vertex, " port with ifname: " + link.getTargetPortIfName(), cloudText);
        if (link.getTargetBridgePort() != null && link.getTargetIfIndex() != null)
            return getEdgeVertexCloudTooltipText(vertex, " port with ifindex: " + String.valueOf(link.getTargetIfIndex()), cloudText);
        if (link.getTargetBridgePort() != null )
            return getEdgeVertexCloudTooltipText(vertex, " bridge port : " + String.valueOf(link.getTargetBridgePort()), cloudText);
        return getEdgeVertexCloudTooltipText(vertex, " ip/mac : "+ link.getBridgePortIfName() + "/"+ link.getMacAddr(), cloudText);
    }

    private String getEdgeVertexCloudTooltipText(Vertex vertex, String vertexPortText, String cloudText) {
        StringBuffer tooltipText = new StringBuffer();

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append("Type of Link: Bridge Layer 2");
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append("Name: &lt;endpoint1 " + vertex.getLabel() + vertexPortText);
        tooltipText.append( " ---- endpoint2 A Shared Segment &gt;");
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append( "End Point 1: " + vertex.getLabel() + ", " + vertex.getIpAddress());
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append( "End Point 1: " + cloudText);
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        return tooltipText.toString();
    }

    private String getEdgeTooltipText(LinkDetail<?> linkDetail) {

        StringBuffer tooltipText = new StringBuffer();
        Vertex source = linkDetail.getSource();
        Vertex target = linkDetail.getTarget();
        OnmsSnmpInterface sourceInterface = getByNodeIdAndIfIndex(linkDetail.getSourceIfIndex(), source);
        OnmsSnmpInterface targetInterface = getByNodeIdAndIfIndex(linkDetail.getTargetIfIndex(), target);

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        if (sourceInterface != null && targetInterface != null
                && sourceInterface.getNetMask() != null && !sourceInterface.getNetMask().isLoopbackAddress()
                && targetInterface.getNetMask() != null && !targetInterface.getNetMask().isLoopbackAddress()) {
            tooltipText.append("Type of Link: " + linkDetail.getType() + " Layer3/Layer2");
        } else {
            tooltipText.append("Type of Link: " + linkDetail.getType() + " Layer2");
        }
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append( "Name: &lt;endpoint1 " + source.getLabel());
        if (sourceInterface != null )
            tooltipText.append( ":"+sourceInterface.getIfName());
        tooltipText.append( " ---- endpoint2 " + target.getLabel());
        if (targetInterface != null)
            tooltipText.append( ":"+targetInterface.getIfName());
        tooltipText.append("&gt;");
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        if ( targetInterface != null) {
            if (targetInterface.getIfSpeed() != null) {
                tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
                tooltipText.append( "Bandwidth: " + getHumanReadableIfSpeed(targetInterface.getIfSpeed()));
                tooltipText.append(HTML_TOOLTIP_TAG_END);
            }
        } else if (sourceInterface != null) {
            if (sourceInterface.getIfSpeed() != null) {
                tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
                tooltipText.append( "Bandwidth: " + getHumanReadableIfSpeed(sourceInterface.getIfSpeed()));
                tooltipText.append(HTML_TOOLTIP_TAG_END);
            }
        }

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append( "End Point 1: " + source.getLabel() + ", " + source.getIpAddress());
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append( "End Point 2: " + target.getLabel() + ", " + target.getIpAddress());
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        return tooltipText.toString();
    }

    private OnmsSnmpInterface getByNodeIdAndIfIndex(Integer ifIndex, Vertex source) {
        if(source.getId() != null && StringUtils.isNumeric(source.getId()) && ifIndex != null)
            return getSnmpInterfaceDao().findByNodeIdAndIfIndex(Integer.parseInt(source.getId()), ifIndex);

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

    public BridgeMacLinkDao getBridgeMacLinkDao() {
        return m_bridgeMacLinkDao;
    }

    public void setBridgeMacLinkDao(BridgeMacLinkDao bridgeMacLinkDao) {
        m_bridgeMacLinkDao = bridgeMacLinkDao;
    }

    public BridgeBridgeLinkDao getBridgeBridgeLinkDao() {
        return m_bridgeBridgeLinkDao;
    }

    public void setBridgeBridgeLinkDao(BridgeBridgeLinkDao bridgeBridgeLinkDao) {
        m_bridgeBridgeLinkDao = bridgeBridgeLinkDao;
    }

    public CdpLinkDao getCdpLinkDao() {
        return m_cdpLinkDao;
    }

    public void setCdpLinkDao(CdpLinkDao cdpLinkDao) {
        m_cdpLinkDao = cdpLinkDao;
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
