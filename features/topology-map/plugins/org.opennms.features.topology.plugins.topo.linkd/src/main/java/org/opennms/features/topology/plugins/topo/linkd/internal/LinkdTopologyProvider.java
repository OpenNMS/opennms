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


import java.io.File;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.opennms.core.criteria.CriteriaBuilder;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.support.VertexHopGraphProvider;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.FocusNodeHopCriteria;
import org.opennms.features.topology.api.support.VertexHopGraphProvider.VertexHopCriteria;
import org.opennms.features.topology.api.topo.AbstractEdge;
import org.opennms.features.topology.api.topo.AbstractSearchProvider;
import org.opennms.features.topology.api.topo.AbstractTopologyProvider;
import org.opennms.features.topology.api.topo.AbstractVertex;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.SearchProvider;
import org.opennms.features.topology.api.topo.SearchQuery;
import org.opennms.features.topology.api.topo.SearchResult;
import org.opennms.features.topology.api.topo.SimpleLeafVertex;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.api.topo.WrappedEdge;
import org.opennms.features.topology.api.topo.WrappedGraph;
import org.opennms.features.topology.api.topo.WrappedGroup;
import org.opennms.features.topology.api.topo.WrappedLeafVertex;
import org.opennms.features.topology.api.topo.WrappedVertex;
import org.opennms.netmgt.dao.api.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.SnmpInterfaceDao;
import org.opennms.netmgt.dao.api.TopologyDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.FilterManager;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsNode.NodeType;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;

public class LinkdTopologyProvider extends AbstractTopologyProvider implements GraphProvider, SearchProvider {
	
	private static Logger LOG = LoggerFactory.getLogger(LinkdTopologyProvider.class);

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
    
    public static final String TOPOLOGY_NAMESPACE_LINKD = "nodes";
    public static final String GROUP_ICON_KEY = "linkd:group";
    public static final String SERVER_ICON_KEY = "linkd:system";
    
    private static final String HTML_TOOLTIP_TAG_OPEN = "<p>";
    private static final String HTML_TOOLTIP_TAG_END  = "</p>";
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
    private static final EnumMap<NodeType, String> m_nodeStatusMap;

    static {
        m_nodeStatusMap = new EnumMap<NodeType, String>(NodeType.class);
        m_nodeStatusMap.put(NodeType.ACTIVE, "Active");
        m_nodeStatusMap.put(NodeType.UNKNOWN, "Unknown");
        m_nodeStatusMap.put(NodeType.DELETED, "Deleted");                        
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

    private boolean addNodeWithoutLink = false;
    
    private DataLinkInterfaceDao m_dataLinkInterfaceDao;

    private NodeDao m_nodeDao;

    private SnmpInterfaceDao m_snmpInterfaceDao;

    private IpInterfaceDao m_ipInterfaceDao;

    private TopologyDao m_topologyDao;

    private String m_configurationFile;

    private final boolean m_aclEnabled;

    private FilterManager m_filterManager;
    
    private LinkdHopCriteriaFactory m_criteriaHopFactory;

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
    
    public boolean isAddNodeWithoutLink() {
        return addNodeWithoutLink;
    }

    public void setAddNodeWithoutLink(boolean addNodeWithoutLink) {
        this.addNodeWithoutLink = addNodeWithoutLink;
    }

    public DataLinkInterfaceDao getDataLinkInterfaceDao() {
        return m_dataLinkInterfaceDao;
    }

    public void setDataLinkInterfaceDao(DataLinkInterfaceDao dataLinkInterfaceDao) {
        m_dataLinkInterfaceDao = dataLinkInterfaceDao;
    }

    public void setTopologyDao(TopologyDao topologyDao) {
        m_topologyDao = topologyDao;
    }

    public void setFilterManager(FilterManager filterManager){
        m_filterManager = filterManager;
    }

    public FilterManager getFilterManager() {
        return m_filterManager;
    }
    
    public LinkdHopCriteriaFactory getLinkdHopCriteriaFactory() {
        return m_criteriaHopFactory;
    }
    
    public void setLinkdHopCriteriaFactory(LinkdHopCriteriaFactory factory) {
        m_criteriaHopFactory = factory;
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
    
    public LinkdTopologyProvider() {
    	super(TOPOLOGY_NAMESPACE_LINKD);
        String aclsProp = System.getProperty("org.opennms.web.aclsEnabled");
        m_aclEnabled = aclsProp != null ? aclsProp.equals("true") : false;
    }

    private static WrappedGraph getGraphFromFile(File file) throws JAXBException, MalformedURLException {
        JAXBContext jc = JAXBContext.newInstance(WrappedGraph.class);
        Unmarshaller u = jc.createUnmarshaller();
        return (WrappedGraph) u.unmarshal(file.toURI().toURL());
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
    public void load(String filename) throws MalformedURLException, JAXBException {
        if (filename != null) {
            LOG.warn("Filename that was specified for linkd topology will be ignored: " + filename + ", using " + m_configurationFile + " instead");
        }
        LOG.debug("loadtopology: resetContainer ");
        resetContainer();

        for (DataLinkInterface link: m_dataLinkInterfaceDao.findAll()) {
            LOG.debug("loadtopology: parsing link: " + link.getDataLinkInterfaceId());

            OnmsNode node = m_nodeDao.get(link.getNode().getId());
            LOG.debug("loadtopology: found source node: " + node.getLabel());
            String sourceId = node.getNodeId();
            Vertex source = getVertex(getVertexNamespace(), sourceId);
            if (source == null) {
                LOG.debug("loadtopology: adding source node as vertex: " + node.getLabel());
                source = getVertex(node);
                addVertices(source);
            }

            OnmsNode parentNode = m_nodeDao.get(link.getNodeParentId());
            LOG.debug("loadtopology: found target node: " + parentNode.getLabel());
            String targetId = parentNode.getNodeId();
            Vertex target = getVertex(getVertexNamespace(), targetId);
            if (target == null) {
                LOG.debug("loadtopology: adding target as vertex: " + parentNode.getLabel());
                target = getVertex(parentNode);
                addVertices(target);
            }
            
            // Create a new edge that connects the vertices
            // TODO: Make sure that all properties are set on this object
            AbstractEdge edge = connectVertices(link.getDataLinkInterfaceId(), source, target); 
            edge.setTooltipText(getEdgeTooltipText(link, source, target));
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
    

    private String getEdgeTooltipText(DataLinkInterface link,
            Vertex source, Vertex target) {
        StringBuffer tooltipText = new StringBuffer();

        OnmsSnmpInterface sourceInterface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(Integer.parseInt(source.getId()), link.getIfIndex());
        OnmsSnmpInterface targetInterface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(Integer.parseInt(target.getId()), link.getParentIfIndex());
        
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

    private static String getNodeTooltipText(OnmsNode node, AbstractVertex vertex, OnmsIpInterface ip) {
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
    
    public static String getIconName(OnmsNode node) {
        return node.getSysObjectId() == null ? "linkd:system" : "linkd:system:snmp:"+node.getSysObjectId();
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
    public String getSearchProviderNamespace() {
        return TOPOLOGY_NAMESPACE_LINKD;
    }


    //@Override
    public List<SearchResult> slowQuery(SearchQuery searchQuery, GraphContainer graphContainer) {
        LOG.debug("SearchProvider->query: called with search query: '{}'", searchQuery);
        List<SearchResult> searchResults = Lists.newArrayList();
        
        CriteriaBuilder cb = new CriteriaBuilder(OnmsNode.class);
        String ilike = "%"+searchQuery.getQueryString()+"%";  //check this for performance reasons
//        cb.alias("assetRecord", "asset").match("any").ilike("label", ilike).ilike("sysDescription", ilike).ilike("asset.comment", ilike);
        cb.match("any").ilike("label", ilike).ilike("sysDescription", ilike);
        List<OnmsNode> nodes = m_nodeDao.findMatching(cb.toCriteria());
        
        if (nodes.size() == 0) {
            return searchResults;
        }
        
        for (OnmsNode node : nodes) {
            searchResults.add(createSearchResult(node, searchQuery.getQueryString()));
        }
        
        return searchResults;
    }

    private SearchResult createSearchResult(OnmsNode node, String queryString) {
        SearchResult result = new SearchResult("node", node.getId().toString(), node.getLabel(), queryString);
        return result;
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

    @Override
    public void onFocusSearchResult(SearchResult searchResult, OperationContext operationContext) {
        LOG.debug("SearchProvider->onFocusSearchResult: called with search result: '{}'", searchResult);
    }

    @Override
    public void onDefocusSearchResult(SearchResult searchResult, OperationContext operationContext) {
        LOG.debug("SearchProvider->onDefocusSearchResult: called with search result: '{}'", searchResult);
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
    
    private org.opennms.features.topology.api.topo.Criteria findCriterion(String resultId, GraphContainer container) {
        
        org.opennms.features.topology.api.topo.Criteria[] criteria = container.getCriteria();
        for (org.opennms.features.topology.api.topo.Criteria criterion : criteria) {
            if (criterion instanceof LinkdHopCriteria ) {
                
                String id = ((LinkdHopCriteria) criterion).getId();
                
                if (id.equals(resultId)) {
                    return criterion;
                }
            }
            
            if (criterion instanceof FocusNodeHopCriteria) {
                String id = ((FocusNodeHopCriteria)criterion).getId();
                
                if (id.equals(resultId)) {
                    return criterion;
                }
            }
            
        }
        return null;
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
    public void addVertexHopCriteria(SearchResult searchResult, GraphContainer container) {
        LOG.debug("SearchProvider->addVertexHopCriteria: called with search result: '{}'", searchResult);
        
        VertexHopCriteria criterion = LinkdHopCriteriaFactory.createCriteria(searchResult.getId(), searchResult.getLabel());
        container.addCriteria(criterion);
        
        LOG.debug("SearchProvider->addVertexHop: adding hop criteria {}.", criterion);
        
        logCriteriaInContainer(container);
    }

    private void logCriteriaInContainer(GraphContainer container) {
        Criteria[] criteria = container.getCriteria();
        LOG.debug("SearchProvider->addVertexHopCriteria: there are now {} criteria in the GraphContainer.", criteria.length);
        for (Criteria crit : criteria) {
            LOG.debug("SearchProvider->addVertexHopCriteria: criterion: '{}' is in the GraphContainer.", crit);
        }
    }
    

    @Override
    public VertexHopCriteria getDefaultCriteria() {
        LOG.debug("SearchProvider->getDefaultCriteria called.");

        final OnmsNode node = m_topologyDao.getDefaultFocusPoint();
                
        VertexHopCriteria criterion = null;
        
        if (node != null) {
            final Vertex defaultVertex = getVertex(TOPOLOGY_NAMESPACE_LINKD, node.getNodeId());
            if (defaultVertex != null) {
                VertexHopGraphProvider.FocusNodeHopCriteria hopCriteria = new VertexHopGraphProvider.FocusNodeHopCriteria();
                hopCriteria.add(defaultVertex);
                return hopCriteria;
            }
        }
        
        LOG.debug("SearchProvider->getDefaultCriteria:returning hop criteria: '{}'.", criterion);
        return criterion;
    }


    private static String getIfStatusString(int ifStatusNum) {
          if (ifStatusNum < OPER_ADMIN_STATUS.length) {
              return OPER_ADMIN_STATUS[ifStatusNum];
          } else {
              return "Unknown (" + ifStatusNum + ")";
          }
      }
      
    /**
     * Return the human-readable name for a interface status character, may be
     * null.
     *
     * @param c a char.
     * @return a {@link java.lang.String} object.
     */
    private static String getNodeStatusString(NodeType c) {
        return m_nodeStatusMap.get(c);
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

    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }
    
}
