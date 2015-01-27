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

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

import org.apache.commons.lang.StringUtils;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.*;
import org.opennms.netmgt.dao.api.*;
import org.opennms.netmgt.model.*;
import org.opennms.netmgt.model.topology.BridgeMacTopologyLink;
import org.opennms.netmgt.model.topology.CdpTopologyLink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import javax.xml.bind.JAXBException;

import java.io.File;
import java.net.MalformedURLException;
import java.util.*;

public class EnhancedLinkdTopologyProvider extends AbstractLinkdTopologyProvider {

    public CdpLinkDao getCdpLinkDao() {
        return m_cdpLinkDao;
    }

    public void setCdpLinkDao(CdpLinkDao cdpLinkDao) {
        m_cdpLinkDao = cdpLinkDao;
    }

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


        public BridgeLinkDetail(String id, String vertexNamespace, Vertex source, Integer sourceLink, Vertex target, Integer targetLink) {
            super(id, source, sourceLink, target, targetLink);
            m_vertexNamespace = vertexNamespace;
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

        @Override
        public Integer getSourceIfIndex() {
            return 0;
        }

        @Override
        public Integer getTargetIfIndex() {
            return 0;
        }

        @Override
        public String getType() {
            return "Bridge";
        }

        public String getVertexNamespace() {
            return m_vertexNamespace;
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
    }

    private interface LinkState {
        void setParentInterfaces(OnmsSnmpInterface sourceInterface, OnmsSnmpInterface targetInterface);
        String getLinkStatus();
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
    private OspfLinkDao m_ospfLinkDao;
    private IsIsLinkDao m_isisLinkDao;
    private BridgeBridgeLinkDao m_bridgeBridgeLinkDao;
    private BridgeMacLinkDao m_bridgeMacLinkDao;
    private CdpLinkDao m_cdpLinkDao;
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
        LOG.debug("init: loading topology.");
        load(null);
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

            getLldpLinks();
            getOspfLinks();
            getIsIsLinks();
            getBridgeLinks();
            getCdpLinks();


        } catch (Exception e){
            LOG.debug(e.getStackTrace().toString());
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

    private void getOspfLinks() {
        List<OspfLink> allLinks =  getOspfLinkDao().findAll();
        Set<OspfLinkDetail> combinedLinkDetails = new HashSet<OspfLinkDetail>();
        for(OspfLink sourceLink : allLinks) {

            for (OspfLink targetLink : allLinks) {
                boolean ipAddrCheck = sourceLink.getOspfRemIpAddr().equals(targetLink.getOspfIpAddr()) && targetLink.getOspfRemIpAddr().equals(sourceLink.getOspfIpAddr());
                if(ipAddrCheck) {
                    String id = "ospf::" + Math.min(sourceLink.getId(), targetLink.getId()) + "||" + Math.max(sourceLink.getId(), targetLink.getId());
                    AbstractVertex source = new AbstractVertex(AbstractLinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD, sourceLink.getNode().getNodeId(), sourceLink.getNode().getLabel());
                    source.setIpAddress(sourceLink.getOspfIpAddr().getHostAddress());

                    AbstractVertex target = new AbstractVertex(AbstractLinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD, targetLink.getNode().getNodeId(), targetLink.getNode().getLabel());
                    target.setIpAddress(targetLink.getOspfIpAddr().getHostAddress());

                    OspfLinkDetail linkDetail = new OspfLinkDetail(
                            Math.min(sourceLink.getId(), targetLink.getId()) + "|" + Math.max(sourceLink.getId(), targetLink.getId()),
                            source, sourceLink, target, targetLink);
                    combinedLinkDetails.add(linkDetail);
                }
            }
        }

        for (OspfLinkDetail linkDetail : combinedLinkDetails) {
            AbstractEdge edge = connectVertices(linkDetail.getId(), linkDetail.getSource(), linkDetail.getTarget(), OSPF_EDGE_NAMESPACE);
            edge.setTooltipText(getEdgeTooltipText(linkDetail));
        }
    }

    private void getLldpLinks() {
        List<LldpLink> allLinks = m_lldpLinkDao.findAll();
        Set<LldpLinkDetail> combinedLinkDetails = new HashSet<LldpLinkDetail>();
        for (LldpLink sourceLink : allLinks) {
            LOG.debug("loadtopology: parsing link: " + sourceLink);
            OnmsNode sourceNode = sourceLink.getNode();
            LldpElement sourceElement = sourceNode.getLldpElement();
            LOG.debug("loadtopology: found source node: " + sourceNode.getLabel());
            Vertex source = getVertex(getVertexNamespace(), sourceNode.getNodeId());
            if (source == null) {
                LOG.debug("loadtopology: adding source node as vertex: " + sourceNode.getLabel());
                source = getVertex(sourceNode);
                addVertices(source);
            }

            for (LldpLink targetLink : allLinks) {
                OnmsNode targetNode = targetLink.getNode();
                LldpElement targetLldpElement = targetNode.getLldpElement();

                //Compare the remote data to the targetNode element data
                boolean bool1 = sourceLink.getLldpRemPortId().equals(targetLink.getLldpPortId()) && targetLink.getLldpRemPortId().equals(sourceLink.getLldpPortId());
                boolean bool2 = sourceLink.getLldpRemPortDescr().equals(targetLink.getLldpPortDescr()) && targetLink.getLldpRemPortDescr().equals(sourceLink.getLldpPortDescr());
                boolean bool3 = sourceLink.getLldpRemChassisId().equals(targetLldpElement.getLldpChassisId()) && targetLink.getLldpRemChassisId().equals(sourceElement.getLldpChassisId());
                boolean bool4 = sourceLink.getLldpRemSysname().equals(targetLldpElement.getLldpSysname()) && targetLink.getLldpRemSysname().equals(sourceElement.getLldpSysname());
                boolean bool5 = sourceLink.getLldpRemPortIdSubType() == targetLink.getLldpPortIdSubType() && targetLink.getLldpRemPortIdSubType() == sourceLink.getLldpPortIdSubType();

                if (bool1 && bool2 && bool3 && bool4 && bool5) {
                    Vertex target = getVertex(getVertexNamespace(), targetNode.getNodeId());
                    if (target == null) {
                        target = getVertex(targetNode);
                    }

                    LldpLinkDetail linkDetail = new LldpLinkDetail(
                            Math.min(sourceLink.getId(), targetLink.getId()) + "|" + Math.max(sourceLink.getId(), targetLink.getId()),
                            source, sourceLink, target, targetLink);
                    combinedLinkDetails.add(linkDetail);
                }
            }

        }

        for (LldpLinkDetail linkDetail : combinedLinkDetails) {
            AbstractEdge edge = connectVertices(linkDetail.getId(), linkDetail.getSource(), linkDetail.getTarget(), LLDP_EDGE_NAMESPACE);
            edge.setTooltipText(getEdgeTooltipText(linkDetail));
        }
    }

    private void getCdpLinks() {
        List<CdpTopologyLink> cdpLinks = m_cdpLinkDao.findLinksForTopology();

        for (CdpTopologyLink link : cdpLinks) {
            String id = Math.min(link.getSourceId(), link.getTargetId()) + "|" + Math.max(link.getSourceId(), link.getTargetId());
            CdpLinkDetail linkDetail = new CdpLinkDetail(id,
                    getVertex(m_nodeDao.get(link.getSrcNodeId())),
                    link.getSrcIfIndex(),
                    link.getSrcIfName(),
                    getVertex(m_nodeDao.get(link.getTargetNodeId())),
                    link.getTargetIfName());

            AbstractEdge edge = connectVertices(linkDetail.getId(), linkDetail.getSource(), linkDetail.getTarget(), CDP_EDGE_NAMESPACE);
            edge.setTooltipText(getEdgeTooltipText(linkDetail));

        }
    }

    private void getIsIsLinks(){
        List<Object[]> isislinks = m_isisLinkDao.getLinksForTopology();

        for (Object[] linkObj : isislinks) {
            Integer link1Id = (Integer) linkObj[1];
            Integer link1Nodeid = (Integer) linkObj[2];
            Integer link1IfIndex = (Integer) linkObj[3];
            Integer link2Id = (Integer) linkObj[4];
            Integer link2Nodeid = (Integer) linkObj[5];
            Integer link2IfIndex = (Integer) linkObj[6];
            IsIsLinkDetail linkDetail = new IsIsLinkDetail(
                    Math.min(link1Id, link2Id) + "|" + Math.max(link1Id, link2Id),
                    getVertex(m_nodeDao.get(link1Nodeid)),
                    link1Id,
                    link1IfIndex,
                    getVertex(m_nodeDao.get(link2Nodeid)),
                    link2Id,
                    link2IfIndex
            );

            AbstractEdge edge = connectVertices(linkDetail.getId(), linkDetail.getSource(), linkDetail.getTarget(), ISIS_EDGE_NAMESPACE);
            edge.setTooltipText(getEdgeTooltipText(linkDetail));
        }
    }

    private void getBridgeLinks(){

        List<BridgeMacTopologyLink> bridgeMacLinks = m_bridgeMacLinkDao.getAllBridgeLinksToIpAddrToNodes();

        Multimap<String, BridgeMacTopologyLink> multimap = HashMultimap.create();
        for (BridgeMacTopologyLink macLink : bridgeMacLinks) {
            multimap.put(String.valueOf(macLink.getNodeId()) + "|" +String.valueOf(macLink.getBridgePort()), macLink);
        }

        //if multimap entry has more than one item, check bridgeBridgeLink and add cloud vertex
        for (String key : multimap.keySet()){
            Collection<BridgeMacTopologyLink> links = multimap.get(key);
            if (links.size() > 1) {
                //process link with cloud
                processMultipleBridgeLinks(key, links);
            } else{
                //add single connection
                BridgeMacTopologyLink topoLink = links.iterator().next();
                String id = Math.min(topoLink.getNodeId(), topoLink.getTargetNodeId()) + "|" + Math.max(topoLink.getNodeId(), topoLink.getTargetNodeId());
                BridgeLinkDetail detail = new BridgeLinkDetail(id, EnhancedLinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD,
                        getVertex(m_nodeDao.get(topoLink.getNodeId())), topoLink.getId(), getVertex(m_nodeDao.get(topoLink.getTargetNodeId())), topoLink.getId());

                AbstractEdge edge = connectVertices(detail.getId(), detail.getSource(), detail.getTarget(), BRIDGE_EDGE_NAMESPACE);
                //TODO: fix tooltip for bridge topology
                edge.setTooltipText(getEdgeTooltipText(detail));
            }

        }
    }

    private void processMultipleBridgeLinks(String bridgeLinkKey, Collection<BridgeMacTopologyLink> topoLinks) {
        //TODO: When making the links for bridge links make sure that check against bridgebridge link table
        String[] keyParts = bridgeLinkKey.split("\\|");

        int parentNodeId = Integer.parseInt(keyParts[0]);
        String bridgePort = keyParts[1];

        AbstractVertex parentVertex = getVertex(m_nodeDao.get(parentNodeId));

        AbstractVertex cloudVertex = addVertex(bridgeLinkKey, 0, 0);
        cloudVertex.setLabel("");
        cloudVertex.setIconKey("cloud");
        cloudVertex.setTooltipText(parentVertex.getLabel() + " bridge port: " + bridgePort);

        for (BridgeMacTopologyLink topoLink : topoLinks) {
            if(topoLink.getTargetNodeId() != null) {

                //Check to see if there are any edges with the cloudVertex, if not add it
                if (getEdgeIdsForVertex(cloudVertex).length == 0) {
                    Edge edge = connectVertices(bridgeLinkKey, cloudVertex, parentVertex, BRIDGE_EDGE_NAMESPACE);
                    edge.setTooltipText(getBridgeCloudTooltip(parentVertex, bridgePort));
                }

                String edgeId = Math.min(topoLink.getNodeId(), topoLink.getTargetNodeId()) + "|" + Math.max(topoLink.getNodeId(), topoLink.getTargetNodeId());
                AbstractVertex target = getVertex(m_nodeDao.get(topoLink.getTargetNodeId()));
                AbstractEdge edge = connectVertices(edgeId, cloudVertex, target, BRIDGE_EDGE_NAMESPACE);


                //Creating just for tooltip text,
                AbstractVertex tooltipCloudVertex = new SimpleLeafVertex(TOPOLOGY_NAMESPACE_LINKD, null, 0,0);
                tooltipCloudVertex.setLabel(parentVertex.getLabel() + " bridge port: " + bridgePort);
                tooltipCloudVertex.setIpAddress("");

                BridgeLinkDetail detail = new BridgeLinkDetail(edgeId, EnhancedLinkdTopologyProvider.TOPOLOGY_NAMESPACE_LINKD,
                        tooltipCloudVertex, topoLink.getId(), target, topoLink.getId());

                edge.setTooltipText(getEdgeTooltipText(detail));
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

    private String getBridgeCloudTooltip(Vertex parentVertex, String bridgePort) {
        StringBuffer tooltipText = new StringBuffer();

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append("Type of Link: Bridge Layer 2");
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append("Name: &lt;endpoint1 " + parentVertex.getLabel() + " bridge port: " + bridgePort);
        tooltipText.append( " ---- endpoint2 " + parentVertex.getLabel() + "&gt;");
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append( "End Point 1: " + parentVertex.getLabel() + " bridge port: " + bridgePort);
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append( "End Point 2: " + parentVertex.getLabel() + ", " + parentVertex.getIpAddress());
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        return tooltipText.toString();
    }

    private String getEdgeTooltipText(LinkDetail linkDetail) {

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

    private org.opennms.features.topology.api.topo.Criteria findCriterion(String resultId, GraphContainer container) {

        org.opennms.features.topology.api.topo.Criteria[] criteria = container.getCriteria();
        for (org.opennms.features.topology.api.topo.Criteria criterion : criteria) {
            if (criterion instanceof LinkdHopCriteria ) {

                String id = ((LinkdHopCriteria) criterion).getId();

                if (id.equals(resultId)) {
                    return criterion;
                }
            }

            if (criterion instanceof VertexHopGraphProvider.FocusNodeHopCriteria) {
                String id = ((VertexHopGraphProvider.FocusNodeHopCriteria)criterion).getId();

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
