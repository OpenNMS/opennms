/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.topology.plugins.topo.linkd.internal;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.*;
import org.opennms.netmgt.dao.api.*;
import org.opennms.netmgt.dao.hibernate.HibernateFilterManager;
import org.opennms.netmgt.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.*;

public class EnhancedLinkdTopologyProvider extends AbstractTopologyProvider implements GraphProvider, SearchProvider {

    private class LldpLinkDetail{

        private final String m_id;
        private final Vertex m_source;
        private final LldpLink m_sourceLink;
        private final Vertex m_target;
        private final LldpLink m_targetLink;

        public LldpLinkDetail(String id, Vertex source, LldpLink sourceLink, Vertex target, LldpLink targetLink){
            m_id = id;
            m_source = source;
            m_sourceLink = sourceLink;
            m_target = target;
            m_targetLink = targetLink;
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

        public String getId() {
            return m_id;
        }

        public Vertex getSource() {
            return m_source;
        }

        public Vertex getTarget() {
            return m_target;
        }

        public LldpLink getSourceLink() {
            return m_sourceLink;
        }

        public LldpLink getTargetLink() {
            return m_targetLink;
        }
    }

    private class LinkStateMachine {
        LinkState m_upState;
        LinkState m_downState;
        LinkState m_unknownState;
        LinkState m_state;

        public LinkStateMachine() {
            m_upState = new LinkUpState(this);
            m_downState = new LinkDownState(this);
            m_unknownState = new LinkUnknownState(this);
            m_state = m_upState;
        }

        public void setParentInterfaces(OnmsSnmpInterface sourceInterface, OnmsSnmpInterface targetInterface) {
            m_state.setParentInterfaces(sourceInterface, targetInterface);
        }

        public String getLinkStatus() {
            return m_state.getLinkStatus();
        }

        public LinkState getUpState() {
            return m_upState;
        }

        public LinkState getDownState() {
            return m_downState;
        }

        public LinkState getUnknownState() {
            return m_unknownState;
        }

        public void setState(LinkState state) {
            m_state = state;
        }
    }

    private interface LinkState {
        void setParentInterfaces(OnmsSnmpInterface sourceInterface, OnmsSnmpInterface targetInterface);
        String getLinkStatus();
    }

    private abstract class AbstractLinkState implements LinkState {

        private LinkStateMachine m_linkStateMachine;

        public AbstractLinkState(LinkStateMachine linkStateMachine) {
            m_linkStateMachine = linkStateMachine;
        }

        protected LinkStateMachine getLinkStateMachine() {
            return m_linkStateMachine;
        }
    }

    private class LinkUpState extends AbstractLinkState {

        public LinkUpState(LinkStateMachine linkStateMachine) {
            super(linkStateMachine);
        }

        @Override
        public void setParentInterfaces(OnmsSnmpInterface sourceInterface, OnmsSnmpInterface targetInterface) {
            if(sourceInterface != null && sourceInterface.getIfOperStatus() != null) {
                if(sourceInterface.getIfOperStatus() != 1) {
                    getLinkStateMachine().setState( getLinkStateMachine().getDownState() );
                }
            }

            if(targetInterface != null && targetInterface.getIfOperStatus() != null) {
                if(targetInterface.getIfOperStatus() != 1) {
                    getLinkStateMachine().setState( getLinkStateMachine().getDownState() );
                }
            }

            if(sourceInterface == null && targetInterface == null) {
                getLinkStateMachine().setState( getLinkStateMachine().getUnknownState() );
            }

        }

        @Override
        public String getLinkStatus() {
            return OPER_ADMIN_STATUS[1];
        }

    }

    private class LinkDownState extends AbstractLinkState {

        public LinkDownState(LinkStateMachine linkStateMachine) {
            super(linkStateMachine);
        }

        @Override
        public void setParentInterfaces(OnmsSnmpInterface sourceInterface, OnmsSnmpInterface targetInterface) {
            if(targetInterface != null && targetInterface.getIfOperStatus() != null) {
                if(sourceInterface != null) {
                    if(sourceInterface.getIfOperStatus() == 1 && targetInterface.getIfOperStatus() == 1) {
                        getLinkStateMachine().setState( getLinkStateMachine().getUpState() );
                    }
                }
            } else if(sourceInterface == null) {
                getLinkStateMachine().setState( getLinkStateMachine().getUnknownState() );
            }
        }

        @Override
        public String getLinkStatus() {
            return OPER_ADMIN_STATUS[2];
        }

    }

    private class LinkUnknownState extends AbstractLinkState{

        public LinkUnknownState(LinkStateMachine linkStateMachine) {
            super(linkStateMachine);
        }


        @Override
        public void setParentInterfaces(OnmsSnmpInterface sourceInterface, OnmsSnmpInterface targetInterface) {
            if(targetInterface != null && targetInterface.getIfOperStatus() != null) {
                if(sourceInterface != null) {
                    if(sourceInterface.getIfOperStatus() == 1 && targetInterface.getIfOperStatus() == 1) {
                        getLinkStateMachine().setState( getLinkStateMachine().getUpState() );
                    } else {
                        getLinkStateMachine().setState( getLinkStateMachine().getDownState() );
                    }
                }
            }

        }

        @Override
        public String getLinkStatus() {
            return OPER_ADMIN_STATUS[4];
        }

    }

    private static Logger LOG = LoggerFactory.getLogger(EnhancedLinkdTopologyProvider.class);
    public static final String TOPOLOGY_NAMESPACE_LINKD = "nodes";

    protected static final String HTML_TOOLTIP_TAG_OPEN = "<p>";
    protected static final String HTML_TOOLTIP_TAG_END  = "</p>";

    /**
     * Always print at least one digit after the decimal point,
     * and at most three digits after the decimal point.
     */
    private static final DecimalFormat s_oneDigitAfterDecimal = new DecimalFormat("0.0##");

    /**
     * Print no digits after the decimal point (heh, nor a decimal point).
     */
    private static final DecimalFormat s_noDigitsAfterDecimal = new DecimalFormat("0");

    /**
     * Do not use directly. Call {@link #getNodeStatusString(org.opennms.netmgt.model.OnmsNode.NodeType)}
     * getInterfaceStatusMap} instead.
     */
    private static final EnumMap<OnmsNode.NodeType, String> m_nodeStatusMap;

    static {
        m_nodeStatusMap = new EnumMap<OnmsNode.NodeType, String>(OnmsNode.NodeType.class);
        m_nodeStatusMap.put(OnmsNode.NodeType.ACTIVE, "Active");
        m_nodeStatusMap.put(OnmsNode.NodeType.UNKNOWN, "Unknown");
        m_nodeStatusMap.put(OnmsNode.NodeType.DELETED, "Deleted");
    }

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
    private NodeDao m_nodeDao;
    private SnmpInterfaceDao m_snmpInterfaceDao;
    private IpInterfaceDao m_ipInterfaceDao;
    private TopologyDao m_topologyDao;
    private String m_configurationFile;
    private FilterManager m_filterManager;
    private LinkdHopCriteriaFactory m_criteriaHopFactory;
    private boolean m_addNodeWithoutLink = false;
    private final boolean m_aclEnabled;

    public EnhancedLinkdTopologyProvider() {
        super(TOPOLOGY_NAMESPACE_LINKD);
        String aclsProp = System.getProperty("org.opennms.web.aclsEnabled");
        m_aclEnabled = aclsProp != null ? aclsProp.equals("true") : false;
    }

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
    public void save() {
        List<WrappedVertex> vertices = new ArrayList<WrappedVertex>();
        for (Vertex vertex : getVertices()) {
            if (vertex.isGroup()) {
                vertices.add(new WrappedGroup(vertex));
            } else {
                vertices.add(new WrappedLeafVertex(vertex));
            }
        }
        List<WrappedEdge> edges = new ArrayList<WrappedEdge>();
        for (Edge edge : getEdges()) {
            WrappedEdge newEdge = new WrappedEdge(edge, new WrappedLeafVertex(m_vertexProvider.getVertex(edge.getSource().getVertex())), new WrappedLeafVertex(m_vertexProvider.getVertex(edge.getTarget().getVertex())));
            edges.add(newEdge);
        }

        WrappedGraph graph = new WrappedGraph(getEdgeNamespace(), vertices, edges);

        JAXB.marshal(graph, new File(m_configurationFile));
    }

    @Override
    public void load(String filename) throws MalformedURLException, JAXBException {
        if (filename != null) {
            LOG.warn("Filename that was specified for linkd topology will be ignored: " + filename + ", using " + m_configurationFile + " instead");
        }

        //TODO: change to one query from the database that will return all links plus elements joined
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

        //Adding all deduplicated links
        for (LldpLinkDetail linkDetail : combinedLinkDetails) {
            AbstractEdge edge = connectVertices(linkDetail.getId(), linkDetail.getSource(), linkDetail.getTarget());
            edge.setTooltipText(getEdgeTooltipText(linkDetail.getSourceLink(), linkDetail.getTargetLink(), linkDetail.getSource(), linkDetail.getTarget()));
        }

        LOG.debug("loadtopology: adding nodes without links: " + isAddNodeWithoutLink());
        if (isAddNodeWithoutLink()) {

            List<OnmsNode> allNodes;
            allNodes = getAllNodesNoACL();

            for (OnmsNode onmsnode: allNodes) {
                String nodeId = onmsnode.getNodeId();
                if (getVertex(getVertexNamespace(), nodeId) == null) {
                    LOG.debug("loadtopology: adding link-less node: " + onmsnode.getLabel());
                    addVertices(getVertex(onmsnode));
                }
            }

        }

        File configFile = new File(m_configurationFile);
        if (configFile.exists() && configFile.canRead()) {
            LOG.debug("loadtopology: loading topology from configuration file: " + m_configurationFile);
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
                        LoggerFactory.getLogger(this.getClass()).warn("Invalid vertex unmarshalled from {}: {}", m_configurationFile, eachVertexInFile);
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
            LOG.debug("loadtopology: could not load topology configFile:" + m_configurationFile);
        }
        LOG.debug("Found " + getGroups().size() + " groups");
        LOG.debug("Found " + getVerticesWithoutGroups().size() + " vertices");
        LOG.debug("Found " + getEdges().size() + " edges");



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

    @Override
    public VertexHopCriteria getDefaultCriteria() {
        return null;
    }

    private AbstractVertex getVertex(OnmsNode onmsnode) {
        OnmsIpInterface ip = getAddress(onmsnode);
        AbstractVertex vertex = new SimpleLeafVertex(TOPOLOGY_NAMESPACE_LINKD, onmsnode.getNodeId(), 0, 0);
        vertex.setIconKey(getIconName(onmsnode));
        vertex.setLabel(onmsnode.getLabel());
        vertex.setIpAddress(ip == null ? null : ip.getIpAddress().getHostAddress());
        vertex.setNodeID(Integer.parseInt(onmsnode.getNodeId()));
        vertex.setTooltipText(getNodeTooltipText(onmsnode, vertex, ip));
        return vertex;
    }

    protected static String getNodeTooltipText(OnmsNode node, AbstractVertex vertex, OnmsIpInterface ip) {
        StringBuffer tooltipText = new StringBuffer();

        /*
        if (node.getSysDescription() != null && node.getSysDescription().length() >0) {
            tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
            tooltipText.append("Description: " + node.getSysDescription());
            tooltipText.append(HTML_TOOLTIP_TAG_END);
        }
        */

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append("Management IP and Name: " + vertex.getIpAddress() + " (" + vertex.getLabel() + ")");
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        if (node.getSysLocation() != null && node.getSysLocation().length() >0) {
            tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
            tooltipText.append("Location: " + node.getSysLocation());
            tooltipText.append(HTML_TOOLTIP_TAG_END);
        }

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append( "Status: " +getNodeStatusString(node.getType()));
        if (ip != null && ip.isManaged()) {
            tooltipText.append( " / Managed");
        } else {
            tooltipText.append( " / Unmanaged");
        }
        tooltipText.append(HTML_TOOLTIP_TAG_END);

        return tooltipText.toString();

    }

    private String getEdgeTooltipText(LldpLink sourceLink,
                                      LldpLink targetLink, Vertex source, Vertex target) {
        StringBuffer tooltipText = new StringBuffer();

        OnmsSnmpInterface sourceInterface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(Integer.parseInt(source.getId()), sourceLink.getLldpPortIfindex());
        OnmsSnmpInterface targetInterface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(Integer.parseInt(target.getId()), targetLink.getLldpPortIfindex());

        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        if (sourceInterface != null && targetInterface != null
                && sourceInterface.getNetMask() != null && !sourceInterface.getNetMask().isLoopbackAddress()
                && targetInterface.getNetMask() != null && !targetInterface.getNetMask().isLoopbackAddress()) {
            tooltipText.append("Type of Link: Layer3/Layer2");
        } else {
            tooltipText.append("Type of Link: Layer2");
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

        LinkStateMachine stateMachine = new LinkStateMachine();
        stateMachine.setParentInterfaces(sourceInterface, targetInterface);
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append("Link status: " + stateMachine.getLinkStatus());
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

    /**
     * Method used to convert an integer bits-per-second value to a more
     * readable vale using commonly recognized abbreviation for network
     * interface speeds. Feel free to expand it as necessary to accomodate
     * different values.
     *
     * @param ifSpeed
     *            The bits-per-second value to be converted into a string
     *            description
     * @return A string representation of the speed (&quot;100 Mbps&quot; for
     *         example)
     */
    private static String getHumanReadableIfSpeed(long ifSpeed) {
        DecimalFormat formatter;
        double displaySpeed;
        String units;

        if (ifSpeed >= 1000000000L) {
            if ((ifSpeed % 1000000000L) == 0) {
                formatter = s_noDigitsAfterDecimal;
            } else {
                formatter = s_oneDigitAfterDecimal;
            }
            displaySpeed = ((double) ifSpeed) / 1000000000.0;
            units = "Gbps";
        } else if (ifSpeed >= 1000000L) {
            if ((ifSpeed % 1000000L) == 0) {
                formatter = s_noDigitsAfterDecimal;
            } else {
                formatter = s_oneDigitAfterDecimal;
            }
            displaySpeed = ((double) ifSpeed) / 1000000.0;
            units = "Mbps";
        } else if (ifSpeed >= 1000L) {
            if ((ifSpeed % 1000L) == 0) {
                formatter = s_noDigitsAfterDecimal;
            } else {
                formatter = s_oneDigitAfterDecimal;
            }
            displaySpeed = ((double) ifSpeed) / 1000.0;
            units = "kbps";
        } else {
            formatter = s_noDigitsAfterDecimal;
            displaySpeed = (double) ifSpeed;
            units = "bps";
        }

        return formatter.format(displaySpeed) + " " + units;
    }

    private static WrappedGraph getGraphFromFile(File file) throws JAXBException, MalformedURLException {
        JAXBContext jc = JAXBContext.newInstance(WrappedGraph.class);
        Unmarshaller u = jc.createUnmarshaller();
        return (WrappedGraph) u.unmarshal(file.toURI().toURL());
    }

    /**
     * Return the human-readable name for a interface status character, may be
     * null.
     *
     * @param c a char.
     * @return a {@link java.lang.String} object.
     */
    private static String getNodeStatusString(OnmsNode.NodeType c) {
        return m_nodeStatusMap.get(c);
    }

    private OnmsIpInterface getAddress(OnmsNode node) {
        //OnmsIpInterface ip = node.getPrimaryInterface();
        OnmsIpInterface ip = m_ipInterfaceDao.findPrimaryInterfaceByNodeId(node.getId());
        if ( ip == null) {
//            for (OnmsIpInterface iterip: node.getIpInterfaces()) {
            for (OnmsIpInterface iterip: m_ipInterfaceDao.findByNodeId(node.getId())) {
                ip = iterip;
                break;
            }
        }
        return ip;
    }

    public void setLldpLinkDao(LldpLinkDao lldpLinkDao) {
        m_lldpLinkDao = lldpLinkDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public void setSnmpInterfaceDao(SnmpInterfaceDao snmpInterfaceDao) {
        m_snmpInterfaceDao = snmpInterfaceDao;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }

    public void setTopologyDao(TopologyDao topologyDao) {
        m_topologyDao = topologyDao;
    }

    public void setConfigurationFile(String configurationFile) {
        m_configurationFile = configurationFile;
    }

    public String getConfigurationFile() {
        return m_configurationFile;
    }

    public static String getIconName(OnmsNode node) {
        return node.getSysObjectId() == null ? "linkd:system" : "linkd:system:snmp:"+node.getSysObjectId();
    }

    public LldpLinkDao getLldpLinkDao() {
        return m_lldpLinkDao;
    }

    public void setAddNodeWithoutLink(boolean addNodeWithoutLink) { m_addNodeWithoutLink = addNodeWithoutLink; }

    public boolean isAddNodeWithoutLink(){ return m_addNodeWithoutLink; }

    public void setFilterManager(FilterManager filterManager) {
        m_filterManager = filterManager;
    }

    public FilterManager getFilterManager() {
        return m_filterManager;
    }

    public LinkdHopCriteriaFactory getLinkdHopCriteriaFactory() {
        return m_criteriaHopFactory;
    }

    public void setLinkdHopCriteriaFactory(LinkdHopCriteriaFactory criteriaHopFactory) {
        m_criteriaHopFactory = criteriaHopFactory;
    }

    private List<OnmsNode> getAllNodesNoACL() {
        if(getFilterManager().isEnabled()){
            String[] userGroups = getFilterManager().getAuthorizationGroups();
            List<OnmsNode> nodeList = null;
            try{
                getFilterManager().disableAuthorizationFilter();
                nodeList = m_nodeDao.findAll();

            } finally {
                // Make sure that we re-enable the authorization filter
                if(userGroups != null){
                    getFilterManager().enableAuthorizationFilter(userGroups);
                }
            }
            return nodeList != null ? nodeList : Collections.<OnmsNode>emptyList();
        } else {
            return m_nodeDao.findAll();
        }


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

    private List<Vertex> getFilteredVertices() {
        if(m_aclEnabled){
            //Get All nodes when called should filter with ACL
            List<OnmsNode> onmsNodes = m_nodeDao.findAll();

            //Transform the onmsNodes list to a list of Ids
            final List<Integer> nodes = Lists.transform(onmsNodes, new Function<OnmsNode, Integer>() {
                @Override
                public Integer apply(OnmsNode node) {
                    return node.getId();
                }
            });


            //Filter out the nodes that are not viewable by the user.
            return Lists.newArrayList(Collections2.filter(m_vertexProvider.getVertices(), new Predicate<Vertex>() {
                @Override
                public boolean apply(Vertex vertex) {
                    return nodes.contains(vertex.getNodeID());
                }
            }));
        } else{
            return m_vertexProvider.getVertices();
        }

    }
}
