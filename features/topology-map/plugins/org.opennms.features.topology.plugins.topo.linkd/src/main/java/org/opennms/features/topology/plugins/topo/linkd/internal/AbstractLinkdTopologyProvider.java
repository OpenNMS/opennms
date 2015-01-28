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

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.topo.*;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.dao.api.TopologyDao;
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
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class AbstractLinkdTopologyProvider extends AbstractTopologyProvider implements GraphProvider,  SearchProvider {

    private static Logger LOG = LoggerFactory.getLogger(AbstractLinkdTopologyProvider.class);

    public static final String TOPOLOGY_NAMESPACE_LINKD = "nodes";
    protected static final String HTML_TOOLTIP_TAG_OPEN = "<p>";
    protected static final String HTML_TOOLTIP_TAG_END  = "</p>";
    /**
     * Always print at least one digit after the decimal point,
     * and at most three digits after the decimal point.
     */
    protected static final DecimalFormat s_oneDigitAfterDecimal = new DecimalFormat("0.0##");
    /**
     * Print no digits after the decimal point (heh, nor a decimal point).
     */
    protected static final DecimalFormat s_noDigitsAfterDecimal = new DecimalFormat("0");

    /**
     * Do not use directly. Call {@link #getNodeStatusString(org.opennms.netmgt.model.OnmsNode.NodeType)}
     * getInterfaceStatusMap} instead.
     */
    protected static final EnumMap<OnmsNode.NodeType, String> m_nodeStatusMap;

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

    protected final boolean m_aclEnabled;
    protected String m_configurationFile;
    protected NodeDao m_nodeDao;
    protected SnmpInterfaceDao m_snmpInterfaceDao;
    protected IpInterfaceDao m_ipInterfaceDao;
    protected TopologyDao m_topologyDao;
    protected FilterManager m_filterManager;
    protected boolean m_addNodeWithoutLink = false;
    protected LinkdHopCriteriaFactory m_criteriaHopFactory;

    protected AbstractLinkdTopologyProvider() {
        super(TOPOLOGY_NAMESPACE_LINKD);
        String aclsProp = System.getProperty("org.opennms.web.aclsEnabled");
        m_aclEnabled = aclsProp != null ? aclsProp.equals("true") : false;
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
    protected static String getHumanReadableIfSpeed(long ifSpeed) {
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

    protected static WrappedGraph getGraphFromFile(File file) throws JAXBException, MalformedURLException {
        JAXBContext jc = JAXBContext.newInstance(WrappedGraph.class);
        Unmarshaller u = jc.createUnmarshaller();
        return (WrappedGraph) u.unmarshal(file.toURI().toURL());
    }

    public static String getIconName(OnmsNode node) {
        return node.getSysObjectId() == null ? "linkd:system" : "linkd:system:snmp:"+node.getSysObjectId();
    }

    /**
     * Return the human-readable name for a interface status character, may be
     * null.
     *
     * @param c a char.
     * @return a {@link String} object.
     */
    protected static String getNodeStatusString(OnmsNode.NodeType c) {
        return m_nodeStatusMap.get(c);
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
        tooltipText.append( "Management IP and Name: " + vertex.getIpAddress() + " (" + vertex.getLabel() + ")");
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

    public String getConfigurationFile() {
        return m_configurationFile;
    }

    public void setConfigurationFile(String configurationFile) {
        m_configurationFile = configurationFile;
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

    protected List<Vertex> getFilteredVertices() {
        if(isAclEnabled()){
            //Get All nodes when called should filter with ACL
            List<OnmsNode> onmsNodes = getNodeDao().findAll();

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

    public void setAddNodeWithoutLink(boolean addNodeWithoutLink) { m_addNodeWithoutLink = addNodeWithoutLink; }

    public boolean isAddNodeWithoutLink(){ return m_addNodeWithoutLink; }

    public boolean isAclEnabled() {
        return m_aclEnabled;
    }

    protected Map<Integer, String> getAllNodesNoACL() {
        if(getFilterManager().isEnabled()){
            String[] userGroups = getFilterManager().getAuthorizationGroups();
            Map<Integer, String> nodeLabelsById = null;
            try{
                getFilterManager().disableAuthorizationFilter();
                nodeLabelsById = getNodeDao().getAllLabelsById();

            } finally {
                // Make sure that we re-enable the authorization filter
                if(userGroups != null){
                    getFilterManager().enableAuthorizationFilter(userGroups);
                }
            }
            return nodeLabelsById != null ? nodeLabelsById : new HashMap<Integer, String>();
        } else {
            return getNodeDao().getAllLabelsById();
        }
    }

    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
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

        JAXB.marshal(graph, new File(getConfigurationFile()));
    }

    public LinkdHopCriteriaFactory getLinkdHopCriteriaFactory() {
        return m_criteriaHopFactory;
    }

    public void setLinkdHopCriteriaFactory(LinkdHopCriteriaFactory criteriaHopFactory) {
        m_criteriaHopFactory = criteriaHopFactory;
    }

    protected OnmsIpInterface getAddress(OnmsNode node) {
        //OnmsIpInterface ip = node.getPrimaryInterface();
        OnmsIpInterface ip = getIpInterfaceDao().findPrimaryInterfaceByNodeId(node.getId());
        if ( ip == null) {
//            for (OnmsIpInterface iterip: node.getIpInterfaces()) {
            for (OnmsIpInterface iterip: getIpInterfaceDao().findByNodeId(node.getId())) {
                ip = iterip;
                break;
            }
        }
        return ip;
    }

    protected AbstractVertex getVertex(OnmsNode onmsnode) {
        OnmsIpInterface ip = getAddress(onmsnode);
        AbstractVertex vertex = new SimpleLeafVertex(TOPOLOGY_NAMESPACE_LINKD, onmsnode.getNodeId(), 0, 0);
        vertex.setIconKey(getIconName(onmsnode));
        vertex.setLabel(onmsnode.getLabel());
        vertex.setIpAddress(ip == null ? null : ip.getIpAddress().getHostAddress());
        vertex.setNodeID(Integer.parseInt(onmsnode.getNodeId()));
        vertex.setTooltipText(getNodeTooltipText(onmsnode, vertex, ip));
        return vertex;
    }

    protected String getEdgeTooltipText(DataLinkInterface link,
                                        Vertex source, Vertex target) {
        StringBuffer tooltipText = new StringBuffer();

        OnmsSnmpInterface sourceInterface = getSnmpInterfaceDao().findByNodeIdAndIfIndex(Integer.parseInt(source.getId()), link.getIfIndex());
        OnmsSnmpInterface targetInterface = getSnmpInterfaceDao().findByNodeIdAndIfIndex(Integer.parseInt(target.getId()), link.getParentIfIndex());

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

    @Override
    public abstract void load(String filename) throws MalformedURLException, JAXBException;

    @Override
    public VertexHopGraphProvider.VertexHopCriteria getDefaultCriteria() {
        final OnmsNode node = m_topologyDao.getDefaultFocusPoint();

        VertexHopGraphProvider.VertexHopCriteria criterion = null;

        if (node != null) {
            final Vertex defaultVertex = getVertex(node);
            if (defaultVertex != null) {
                criterion = new LinkdHopCriteria(node.getNodeId(), node.getLabel(), m_nodeDao);
            }
        }

        return criterion;
    }

    protected void addNodesWithoutLinks() {
        Map<Integer, String> nodeLabelsById = getAllNodesNoACL();
        for (Entry<Integer, String> nodeIdAndLabel: nodeLabelsById.entrySet()) {
            Integer nodeId = nodeIdAndLabel.getKey();
            String nodeLabel = nodeIdAndLabel.getValue();
            if (getVertex(getVertexNamespace(), nodeId.toString()) == null) {
                LOG.debug("Adding link-less node: " + nodeLabel);
                addVertices(new DeferedNodeLeafVertex(TOPOLOGY_NAMESPACE_LINKD, nodeId, nodeLabel, this));
            }
        }
    }

    private interface LinkState {
        void setParentInterfaces(OnmsSnmpInterface sourceInterface, OnmsSnmpInterface targetInterface);
        String getLinkStatus();
    }

    protected class LinkStateMachine {
        LinkState m_upState;
        LinkState m_downState;
        LinkState m_unknownState;
        LinkState m_state;

        public LinkStateMachine() {
            m_upState = new AbstractLinkdTopologyProvider.LinkUpState(this);
            m_downState = new AbstractLinkdTopologyProvider.LinkDownState(this);
            m_unknownState = new AbstractLinkdTopologyProvider.LinkUnknownState(this);
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
}
