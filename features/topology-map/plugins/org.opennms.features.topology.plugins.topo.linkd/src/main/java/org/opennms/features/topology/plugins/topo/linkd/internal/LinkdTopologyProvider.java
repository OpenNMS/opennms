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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXB;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.opennms.features.topology.api.TopologyProvider;

import org.opennms.netmgt.dao.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSnmpInterface;
import org.slf4j.LoggerFactory;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.transaction.support.TransactionOperations;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;

public class LinkdTopologyProvider implements TopologyProvider {
    public static final String GROUP_ICON_KEY = "linkd:group";
    public static final String SERVER_ICON_KEY = "linkd:system";
    public static final String ROOT_GROUP_ID = "Network";
    
    private static final String HTML_TOOLTIP_TAG_OPEN = "<p>";
    private static final String HTML_TOOLTIP_TAG_END  = "";
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
     * Do not use directly. Call {@link #getNodeStatusMap 
     * getInterfaceStatusMap} instead.
     */
    private static final Map<Character, String> m_nodeStatusMap;

    static {
        m_nodeStatusMap = new HashMap<Character, String>();
        m_nodeStatusMap.put('A', "Active");
        m_nodeStatusMap.put(' ', "Unknown");
        m_nodeStatusMap.put('D', "Deleted");                        
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

    private String m_configurationFile;

//    private TransactionOperations m_transactionTemplate;
    
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

    private final LinkdVertexContainer m_vertexContainer;
    private final BeanContainer<String, LinkdEdge> m_edgeContainer;

    private int m_groupCounter = 0;
    
    public DataLinkInterfaceDao getDataLinkInterfaceDao() {
        return m_dataLinkInterfaceDao;
    }

    public void setDataLinkInterfaceDao(DataLinkInterfaceDao dataLinkInterfaceDao) {
        m_dataLinkInterfaceDao = dataLinkInterfaceDao;
    }

    public void onInit() {
        log("init: loading topology v1.3");
        loadtopology();
    }
    
    public LinkdTopologyProvider() {
        m_vertexContainer = new LinkdVertexContainer();
        m_edgeContainer = new BeanContainer<String, LinkdEdge>(LinkdEdge.class);
        m_edgeContainer.setBeanIdProperty("id");
    }

    @Override
    public Object addGroup(String groupIconKey) {
        String nextGroupId = getNextGroupId();
        addGroup(nextGroupId, groupIconKey, "Group " + nextGroupId);
        return nextGroupId;
    }

    private Item addGroup(String groupId, String iconKey, String label) {
        if (m_vertexContainer.containsId(groupId)) {
            throw new IllegalArgumentException("A vertex or group with id " + groupId + " already exists!");
        }
        log("Adding a group: " + groupId);
        LinkdVertex vertex = new LinkdGroup(groupId, label);
        vertex.setIconKey(iconKey);
        return m_vertexContainer.addBean(vertex);        
    }
    
    public String getNextGroupId() {
        return "linkdg" + m_groupCounter++;
    }

    
    @Override
    public boolean containsVertexId(Object vertexId) {
        return m_vertexContainer.containsId(vertexId);    
    }

    @Override
    public BeanContainer<String, LinkdEdge> getEdgeContainer() {
        return m_edgeContainer;
    }

    @Override
    public Collection<String> getEdgeIds() {
        return m_edgeContainer.getItemIds();
    }

    @Override
    public Collection<String> getEdgeIdsForVertex(Object vertexId) {
        
        LinkdVertex vertex = getRequiredVertex(vertexId);
        
        List<String> edges = new ArrayList<String>(vertex.getEdges().size());
        
        for(LinkdEdge e : vertex.getEdges()) {
            
            String edgeId = e.getId();
            
            edges.add(edgeId);

        }
        
        return edges;
    }

    
    private LinkdVertex getRequiredVertex(Object vertexId) {
        return getVertex(vertexId, true);
    }

    private LinkdVertex getVertex(Object vertexId, boolean required) {
        BeanItem<LinkdVertex> item = m_vertexContainer.getItem(vertexId);
        if (required && item == null) {
            throw new IllegalArgumentException("required vertex " + vertexId + " not found.");
        }
        
        return item == null ? null : item.getBean();
    }

    @Override
    public Item getEdgeItem(Object edgeId) {
        return m_edgeContainer.getItem(edgeId);
    }

    @Override
    public Collection<String> getEndPointIdsForEdge(Object edgeId) {
        LinkdEdge edge= getRequiredEdge(edgeId);

        List<String> endPoints = new ArrayList<String>(2);
        
        endPoints.add(edge.getSource().getId());
        endPoints.add(edge.getTarget().getId());

        return endPoints;

    }

    private LinkdEdge getRequiredEdge(Object edgeId) {
        return getEdge(edgeId, true);
    }

    private LinkdEdge getEdge(Object edgeId, boolean required) {
        BeanItem<LinkdEdge> item = m_edgeContainer.getItem(edgeId);
        if (required && item == null) {
            throw new IllegalArgumentException("required edge " + edgeId + " not found.");
        }
        
        return item == null ? null : item.getBean();
    }

    @Override
    public LinkdVertexContainer getVertexContainer() {
        return m_vertexContainer;
    }

    @Override
    public Collection<String> getVertexIds() {
        return m_vertexContainer.getItemIds();
    }

    @Override
    public Item getVertexItem(Object vertexId) {
        return m_vertexContainer.getItem(vertexId);
    }

    @Override
    public void load(String filename) {
        if (filename == null) {
            loadtopology();
        } else {
            loadfromfile(filename);
        }
    }

    @XmlRootElement(name="graph")
    @XmlAccessorType(XmlAccessType.FIELD)
    private static class SimpleGraph {
        
        @XmlElements({
                @XmlElement(name="vertex", type=LinkdNodeVertex.class),
                @XmlElement(name="group", type=LinkdGroup.class)
        })
        List<LinkdVertex> m_vertices = new ArrayList<LinkdVertex>();
        
        @XmlElement(name="edge")
        List<LinkdEdge> m_edges = new ArrayList<LinkdEdge>();
        
        @SuppressWarnings("unused")
        public SimpleGraph() {}

        public SimpleGraph(List<LinkdVertex> vertices, List<LinkdEdge> edges) {
            m_vertices = vertices;
            m_edges = edges;
        }

    }

    private SimpleGraph getGraphFromFile(File file) {
        return JAXB.unmarshal(file, SimpleGraph.class);
    }

    private void loadfromfile(String filename) {
        File file = new File(filename);
        if (file.exists() && file.canRead()) {
            SimpleGraph graph = getGraphFromFile(file);        
            m_vertexContainer.addAll(graph.m_vertices);        
            m_edgeContainer.addAll(graph.m_edges);
        }
    }

    //@Transactional
    private void loadtopology() {
        log("loadtopology: loading topology: configFile:" + m_configurationFile);
        
        log("loadtopology: Clean Vertexcontainer");
        getVertexContainer().removeAllItems();
        log("loadtopology: Clean EdgeContainer");
        getEdgeContainer().removeAllItems();

        Map<String, LinkdVertex> vertexes = new HashMap<String, LinkdVertex>();
        Collection<LinkdEdge> edges = new ArrayList<LinkdEdge>();
        for (DataLinkInterface link: m_dataLinkInterfaceDao.findAll()) {
            log("loadtopology: parsing link: " + link.getDataLinkInterfaceId());

            OnmsNode node = m_nodeDao.get(link.getNode().getId());
            //OnmsNode node = link.getNode();
            log("loadtopology: found node: " + node.getLabel());
            String sourceId = node.getNodeId();
            LinkdVertex source;
            if ( vertexes.containsKey(sourceId)) {
                source = vertexes.get(sourceId);
            } else {
                log("loadtopology: adding source as vertex: " + node.getLabel());
                source = getVertex(node);
                vertexes.put(sourceId, source);
            }

            OnmsNode parentNode = m_nodeDao.get(link.getNodeParentId());
            log("loadtopology: found parentnode: " + parentNode.getLabel());
                       String targetId = parentNode.getNodeId();
            LinkdVertex target;
            if (vertexes.containsKey(targetId)) {
                target = vertexes.get(targetId);
            } else {
                log("loadtopology: adding target as vertex: " + parentNode.getLabel());
                target = getVertex(parentNode);
                vertexes.put(targetId, target);
            }
            LinkdEdge edge = new LinkdEdge(link.getDataLinkInterfaceId(),source,target); 
            edge.setTooltipText(getEdgeTooltipText(link,source,target));
            edges.add(edge);
        }
        
        log("loadtopology: isAddNodeWithoutLink: " + isAddNodeWithoutLink());
        if (isAddNodeWithoutLink()) {
            for (OnmsNode onmsnode: m_nodeDao.findAll()) {
                log("loadtopology: parsing link less node: " + onmsnode.getLabel());
                String nodeId = onmsnode.getNodeId();
                if (!vertexes.containsKey(nodeId)) {
                    log("loadtopology: adding link less node: " + onmsnode.getLabel());
                    vertexes.put(nodeId,getVertex(onmsnode));
                }                
            }
        }
        
        log("Found Vertexes: #" + vertexes.size());        
        log("Found Edges: #" + edges.size());

                
        m_vertexContainer.addAll(vertexes.values());
        m_edgeContainer.addAll(edges);        
 
        File configFile = new File(m_configurationFile);

        if (configFile.exists() && configFile.canRead()) {
            log("loadtopology: loading topology from configuration file: " + m_configurationFile);
            m_groupCounter=0;
            SimpleGraph graph = getGraphFromFile(configFile);
            for (LinkdVertex vertex: graph.m_vertices) {
                if (!vertex.isLeaf()) {
                    log("loadtopology: adding group to topology: " + vertex.getId());
                    m_groupCounter++;
                    addGroup(vertex.getId(), vertex.getIconKey(), vertex.getLabel());
                }
            }
            
            for (LinkdVertex vertex: graph.m_vertices) {
                log("loadtopology: found vertex: " + vertex.getId());
                if (vertex.isRoot()) {
                    if (!vertex.isLeaf())
                        setParent(vertex.getId(), ROOT_GROUP_ID);
                } else {
                    setParent(vertex.getId(), vertex.getParent().getId());
                }
            }

        }
        log("Found Groups: #" + m_groupCounter);


    }

    private LinkdVertex getVertex(OnmsNode onmsnode) {
        OnmsIpInterface ip = getAddress(onmsnode);
        LinkdVertex vertex = new LinkdNodeVertex(onmsnode.getNodeId(), 0, 0, getIconName(onmsnode), onmsnode.getLabel(), ( ip == null ? null : ip.getIpAddress().getHostAddress()));
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
            LinkdVertex source, LinkdVertex target) {
        String tooltipText="";

        OnmsSnmpInterface sourceInterface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(Integer.parseInt(source.getId()), link.getIfIndex());
        OnmsSnmpInterface targetInterface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(Integer.parseInt(target.getId()), link.getParentIfIndex());
        
        tooltipText +=HTML_TOOLTIP_TAG_OPEN;
        if (sourceInterface != null && targetInterface != null
         && sourceInterface.getNetMask() != null && !sourceInterface.getNetMask().isLoopbackAddress() 
         && targetInterface.getNetMask() != null && !targetInterface.getNetMask().isLoopbackAddress()) {
            tooltipText+= "Type of the Link: Layer3/Layer2";
        } else {
            tooltipText+= "Type of the Link: Layer2";            
        }
        tooltipText +=HTML_TOOLTIP_TAG_END;

        tooltipText +=HTML_TOOLTIP_TAG_OPEN;
        tooltipText += "Name: &lt;endpoint1 " + source.getLabel() ;
        if (sourceInterface != null ) 
            tooltipText += ":"+sourceInterface.getIfName();
        tooltipText += " ---- endpoint2 " + target.getLabel();
        if (targetInterface != null) 
            tooltipText += ":"+targetInterface.getIfName();
        tooltipText +="&gt;";
        tooltipText +=HTML_TOOLTIP_TAG_END;
        
        if ( targetInterface != null) {
            if (targetInterface.getIfSpeed() != null) {
                tooltipText +=HTML_TOOLTIP_TAG_OPEN;
                tooltipText += "Bandwidth: " + getHumanReadableIfSpeed(targetInterface.getIfSpeed());
                tooltipText +=HTML_TOOLTIP_TAG_END;
            }
            if (targetInterface.getIfOperStatus() != null) {
                tooltipText +=HTML_TOOLTIP_TAG_OPEN;
                tooltipText += "Link status: " + getIfStatusString(targetInterface.getIfOperStatus());
                tooltipText +=HTML_TOOLTIP_TAG_END;
            }
        } else if (sourceInterface != null) {
            if (sourceInterface.getIfSpeed() != null) {
                tooltipText +=HTML_TOOLTIP_TAG_OPEN;
                tooltipText += "Bandwidth: " + getHumanReadableIfSpeed(sourceInterface.getIfSpeed());
                tooltipText +=HTML_TOOLTIP_TAG_END;
            }
            if (sourceInterface.getIfOperStatus() != null) {
                tooltipText +=HTML_TOOLTIP_TAG_OPEN;
                tooltipText += "Link status: " + getIfStatusString(sourceInterface.getIfOperStatus());
                tooltipText +=HTML_TOOLTIP_TAG_END;
            }
        }

        tooltipText +=HTML_TOOLTIP_TAG_OPEN;
        tooltipText += "EndPoint1: " + source.getLabel() + ", " + source.getIpAddr();
        tooltipText +=HTML_TOOLTIP_TAG_END;
        
        tooltipText +=HTML_TOOLTIP_TAG_OPEN;
        tooltipText += "EndPoint2: " + target.getLabel() + ", " + target.getIpAddr();
        tooltipText +=HTML_TOOLTIP_TAG_END;

        log("getEdgeTooltipText\n" + tooltipText);
        return tooltipText;
    }

    private String getNodeTooltipText(OnmsNode node, LinkdVertex vertex, OnmsIpInterface ip) {
        String tooltipText="";
        if (node.getSysDescription() != null && node.getSysDescription().length() >0) {
            tooltipText +=HTML_TOOLTIP_TAG_OPEN;
            tooltipText +="Description: " + node.getSysDescription();
            tooltipText +=HTML_TOOLTIP_TAG_END;
        }
        tooltipText +=HTML_TOOLTIP_TAG_OPEN;
        tooltipText += "Mngt ip: " + vertex.getIpAddr();
        tooltipText +=HTML_TOOLTIP_TAG_END;
        
        tooltipText +=HTML_TOOLTIP_TAG_OPEN;
        tooltipText += "Name: " + vertex.getLabel();
        tooltipText +=HTML_TOOLTIP_TAG_END;
        
        if (node.getSysLocation() != null && node.getSysLocation().length() >0) {
            tooltipText +=HTML_TOOLTIP_TAG_OPEN;
            tooltipText +="Location: " + node.getSysLocation();
            tooltipText +=HTML_TOOLTIP_TAG_END;
        }
        
        tooltipText +=HTML_TOOLTIP_TAG_OPEN;
        tooltipText += "Status: " +getNodeStatusString(node.getType().charAt(0));
        if (ip != null && ip.isManaged()) {
            tooltipText += "/Managed";
        } else {
            tooltipText += "/UnManaged";
        }
        tooltipText +=HTML_TOOLTIP_TAG_END;

        log("getNodeTooltipText:\n" + tooltipText);
        
        return tooltipText;

    }
    
    protected String getIconName(OnmsNode node) {
        return node.getSysObjectId() == null ? "linkd:system" : "linkd:system:snmp:"+node.getSysObjectId();
    }
    
    @Override
    public void save(String filename) {
        if (filename == null) 
            filename=m_configurationFile;
        List<LinkdVertex> vertices = getBeans(m_vertexContainer);
        List<LinkdEdge> edges = getBeans(m_edgeContainer);

        SimpleGraph graph = new SimpleGraph(vertices, edges);
        
        JAXB.marshal(graph, new File(filename));
    }

    private static <T> List<T> getBeans(BeanContainer<?, T> container) {
        Collection<?> itemIds = container.getItemIds();
        List<T> beans = new ArrayList<T>(itemIds.size());
        
        for(Object itemId : itemIds) {
            beans.add(container.getItem(itemId).getBean());
        }
        
        return beans;
    }

    @Override
    public void setParent(Object vertexId, Object parentId) {
        boolean addedparent = m_vertexContainer.setParent(vertexId, parentId);
        log("setParent for vertex:" + vertexId + " parent: " + parentId + ": "+ addedparent);
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
    private String getNodeStatusString(char c) {
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
            displaySpeed = ((double) ifSpeed) / 1000000000;
            units = "Gbps";
        } else if (ifSpeed >= 1000000L) {
            if ((ifSpeed % 1000000L) == 0) {
                formatter = s_noDigitsAfterDecimal;
            } else {
                formatter = s_oneDigitAfterDecimal;
            }
            displaySpeed = ((double) ifSpeed) / 1000000;
            units = "Mbps";
        } else if (ifSpeed >= 1000L) {
            if ((ifSpeed % 1000L) == 0) {
                formatter = s_noDigitsAfterDecimal;
            } else {
                formatter = s_oneDigitAfterDecimal;
            }
            displaySpeed = ((double) ifSpeed) / 1000;
            units = "kbps";
        } else {
            formatter = s_noDigitsAfterDecimal;
            displaySpeed = (double) ifSpeed;
            units = "bps";
        }
        
        return formatter.format(displaySpeed) + " " + units;
    }

    private void log(final String string) {
        LoggerFactory.getLogger(getClass()).debug(string);
    }
/*
    public TransactionOperations getTransactionTemplate() {
        return m_transactionTemplate;
    }

    public void setTransactionTemplate(TransactionOperations transactionTemplate) {
        m_transactionTemplate = transactionTemplate;
    }
*/

    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }

	@Override
	public String getNamespace() {
		return "node";
	}
    
    
}
