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

import java.text.DecimalFormat;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opennms.features.topology.api.topo.AbstractTopologyProvider;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Defaults;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.SearchProvider;
import org.opennms.features.topology.api.topo.SimpleLeafVertex;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.dao.api.TopologyDao;
import org.opennms.netmgt.model.FilterManager;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.springframework.transaction.support.TransactionOperations;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public abstract class AbstractLinkdTopologyProvider extends AbstractTopologyProvider implements GraphProvider,  SearchProvider {

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

    protected static final EnumMap<OnmsNode.NodeType, String> m_nodeStatusMap;

    static {
        m_nodeStatusMap = new EnumMap<>(OnmsNode.NodeType.class);
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
    protected TransactionOperations m_transactionOperations;
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
     * interface speeds. Feel free to expand it as necessary to accommodate
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

    public static String getIconName(String nodeSysObjectId) {
        if (nodeSysObjectId == null) {
            return "linkd.system";
        }
        if (nodeSysObjectId.startsWith(".")) {
            return "linkd.system.snmp" + nodeSysObjectId;
        }
        return "linkd.system.snmp." + nodeSysObjectId;
    }

    protected static String getNodeTooltipDefaultText(String ip, String label, boolean isManaged, String location,NodeType nodeType) {
        final StringBuilder tooltipText = new StringBuilder();
        tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
        tooltipText.append(label);
        tooltipText.append(": ");
        if (ip != null) {
            tooltipText.append("(");
            tooltipText.append(ip);
            tooltipText.append(")");
        }
        tooltipText.append("(");
        tooltipText.append(m_nodeStatusMap.get(nodeType));
        if (ip != null) {
            if (isManaged) {
                tooltipText.append( "/Managed");
            } else {
                tooltipText.append( "/Unmanaged");
            }
        }
        tooltipText.append(")");
        tooltipText.append(HTML_TOOLTIP_TAG_END);
        
        if (location != null && location.length() > 0) {
                tooltipText.append(HTML_TOOLTIP_TAG_OPEN);
                tooltipText.append(location);
                tooltipText.append(HTML_TOOLTIP_TAG_END);
        }
        return tooltipText.toString();

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

    public LinkdHopCriteriaFactory getLinkdHopCriteriaFactory() {
        return m_criteriaHopFactory;
    }

    public void setLinkdHopCriteriaFactory(LinkdHopCriteriaFactory criteriaHopFactory) {
        m_criteriaHopFactory = criteriaHopFactory;
    }

    protected OnmsIpInterface getAddress(Integer nodeId) {
        //OnmsIpInterface ip = node.getPrimaryInterface();
        OnmsIpInterface ip = getIpInterfaceDao().findPrimaryInterfaceByNodeId(nodeId);
        if ( ip == null) {
//            for (OnmsIpInterface iterip: node.getIpInterfaces()) {
            for (OnmsIpInterface iterip: getIpInterfaceDao().findByNodeId(nodeId)) {
                ip = iterip;
                break;
            }
        }
        return ip;
    }

    protected AbstractVertex getDefaultVertex(Integer nodeId, String sysobjectId, String nodeLabel, String location, NodeType nodeType, boolean isManaged, String ip) {
        return getVertex(nodeId,
                           ip,
                           sysobjectId,
                           nodeLabel,
                           getNodeTooltipDefaultText(ip,
                                                     nodeLabel,
                                                     isManaged,
                                                     location,
                                                     nodeType));
    }

    protected AbstractVertex getVertex(Integer nodeId, String ip, String sysobjectId, String nodeLabel, String tooltipText) {
        AbstractVertex vertex = new SimpleLeafVertex(TOPOLOGY_NAMESPACE_LINKD, nodeId.toString(), 0, 0);
        vertex.setIconKey(getIconName(sysobjectId));
        vertex.setLabel(nodeLabel);
        vertex.setIpAddress(ip);
        vertex.setNodeID(nodeId);
        vertex.setTooltipText(tooltipText);
        return vertex;
    }

    @Override
    public Defaults getDefaults() {
        return new Defaults()
                .withSemanticZoomLevel(Defaults.DEFAULT_SEMANTIC_ZOOM_LEVEL)
                .withPreferredLayout("D3 Layout") // D3 Layout
                .withCriteria(() -> {
                    final OnmsNode node = m_topologyDao.getDefaultFocusPoint();

                    if (node != null) {
                        final Vertex defaultVertex = createVertexFor(node, getAddress(node.getId()));
                        if (defaultVertex != null) {
                            return Lists.newArrayList(new LinkdHopCriteria(node.getNodeId(), node.getLabel(), m_nodeDao));
                        }
                    }

                    return Lists.newArrayList();
                });
    }

    protected Vertex createVertexFor(OnmsNode node, OnmsIpInterface ipInterface) {
        String ip = null;
        boolean isManaged= false;
        if (ipInterface != null && ipInterface.getIpAddress() != null) {
            ip = ipInterface.getIpAddress().getHostAddress();
            isManaged = ipInterface.isManaged();
        }
        return getVertex(node.getId(),ip,node.getSysObjectId(),node.getLabel(),getNodeTooltipDefaultText(ip,node.getLabel(),isManaged,node.getSysLocation(),node.getType()));
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
