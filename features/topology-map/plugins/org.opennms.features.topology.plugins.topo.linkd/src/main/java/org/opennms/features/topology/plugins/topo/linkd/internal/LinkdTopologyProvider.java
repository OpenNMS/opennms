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

import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.dao.DataLinkInterfaceDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.SnmpInterfaceDao;
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsSeverity;
import org.opennms.netmgt.model.OnmsSnmpInterface;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;

public class LinkdTopologyProvider implements TopologyProvider {
    public static final String GROUP_ICON_KEY = "linkd-group";
    public static final String SERVER_ICON_KEY = "linkd-server";

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

    private boolean addNodeWithoutLink = true;
    
    private DataLinkInterfaceDao m_dataLinkInterfaceDao;
    
    private NodeDao m_nodeDao;
    
    private IpInterfaceDao m_ipInterfaceDao;
    
    private SnmpInterfaceDao m_snmpInterfaceDao;

    private AlarmDao m_alarmDao;

    private String m_configurationFile;
    
    
    private Map<Integer, OnmsSeverity> m_nodeToSeveritymap;
    
    public String getConfigurationFile() {
        return m_configurationFile;
    }

    public void setConfigurationFile(String configurationFile) {
        m_configurationFile = configurationFile;
    }

    public AlarmDao getAlarmDao() {
        return m_alarmDao;
    }

    public void setAlarmDao(AlarmDao alarmDao) {
        m_alarmDao = alarmDao;
    }

    public SnmpInterfaceDao getSnmpInterfaceDao() {
        return m_snmpInterfaceDao;
    }

    public void setSnmpInterfaceDao(SnmpInterfaceDao snmpInterfaceDao) {
        m_snmpInterfaceDao = snmpInterfaceDao;
    }

    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
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

    private LinkdVertexContainer m_vertexContainer;
    private BeanContainer<String, LinkdEdge> m_edgeContainer;

    private int m_groupCounter = 0;
    
    public DataLinkInterfaceDao getDataLinkInterfaceDao() {
        return m_dataLinkInterfaceDao;
    }

    public void setDataLinkInterfaceDao(DataLinkInterfaceDao dataLinkInterfaceDao) {
        m_dataLinkInterfaceDao = dataLinkInterfaceDao;
    }

    public void onInit() {
        log("init: loading topology v1.2");
        loadtopology();
    }
    
    public LinkdTopologyProvider() {
        m_vertexContainer = new LinkdVertexContainer();
        m_edgeContainer = new BeanContainer<String, LinkdEdge>(LinkdEdge.class);
        m_edgeContainer.setBeanIdProperty("id");
        m_nodeToSeveritymap = new HashMap<Integer, OnmsSeverity>();
    }

    @Override
    public Object addGroup(String groupIcon) {
        return addGroup(getNextGroupId(), groupIcon);
    }

    private Item addGroup(String groupId, String icon) {
        if (m_vertexContainer.containsId(groupId)) {
            throw new IllegalArgumentException("A vertex or group with id " + groupId + " already exists!");
        }
        System.err.println("Adding a group: " + groupId);
        LinkdVertex vertex = new LinkdGroup(groupId, "Group " + groupId);
        vertex.setIconKey(icon);
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

    private void loadseveritymap() {
        m_nodeToSeveritymap.clear();
        for (OnmsAlarm alarm: m_alarmDao.findAll()) {
            if (m_nodeToSeveritymap.containsKey(alarm.getNodeId())
                    && alarm.getSeverity().isLessThan(m_nodeToSeveritymap.get(alarm.getNodeId())))
                return;
            m_nodeToSeveritymap.put(alarm.getNodeId(), alarm.getSeverity());
        }
    }

    private void loadtopology() {
        log("loadtopology: loading topology: configFile:" + m_configurationFile);
        
        log("loadtopology: Clean Vertexcontainer");
        getVertexContainer().removeAllItems();
        log("loadtopology: Clean EdgeContainer");
        getEdgeContainer().removeAllItems();

        log("loadtopology: loading node to severity map");
        loadseveritymap();
        
        Map<String, LinkdVertex> vertexes = new HashMap<String, LinkdVertex>();
        Collection<LinkdEdge> edges = new ArrayList<LinkdEdge>();
        for (DataLinkInterface link: m_dataLinkInterfaceDao.findAll()) {
            log("loadtopology: parsing link: " + link.getDataLinkInterfaceId());

            OnmsNode node = m_nodeDao.get(link.getNode().getId());
            log("loadtopology: found node: " + node.getLabel());
            String sourceId = node.getNodeId();
            LinkdVertex source;
            if ( vertexes.containsKey(sourceId)) {
                source = vertexes.get(sourceId);
            } else {
                log("loadtopology: adding source as vertex: " + node.getLabel());
                OnmsIpInterface ip = getAddress(node);
                source = new LinkdNodeVertex(node.getNodeId(), 0, 0, getIconName(node), node.getLabel(), ( ip == null ? null : ip.getIpAddress().getHostAddress()));
                source.setTooltipText(getNodeTooltipText(node, source, ip));
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
                OnmsIpInterface ip = getAddress(parentNode);
                target = new LinkdNodeVertex(parentNode.getNodeId(), 0, 0, getIconName(parentNode), parentNode.getLabel(), ( ip == null ? null : ip.getIpAddress().getHostAddress()));
                target.setTooltipText(getNodeTooltipText(parentNode, target, ip));                
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
                LinkdVertex linklessnode;
                if (!vertexes.containsKey(nodeId)) {
                    log("loadtopology: adding link less node: " + onmsnode.getLabel());
                    OnmsIpInterface ip = getAddress(onmsnode);
                    linklessnode = new LinkdNodeVertex(onmsnode.getNodeId(), 0, 0, getIconName(onmsnode), onmsnode.getLabel(), ( ip == null ? null : ip.getIpAddress().getHostAddress()));
                    linklessnode.setTooltipText(getNodeTooltipText(onmsnode, linklessnode, ip));
                    vertexes.put(nodeId,linklessnode);
                }                
            }
        }
        
        File configFile = new File(m_configurationFile);

        if (configFile.exists() && configFile.canRead()) {
            log("loadtopology: loading topology from configuration file: " + m_configurationFile);
            m_groupCounter=0;
            SimpleGraph graph = getGraphFromFile(configFile);
            for (LinkdVertex vertex: graph.m_vertices) {
                if (!vertex.isLeaf()) {
                    m_groupCounter++;
                    LinkdGroup group = (LinkdGroup) vertex;
                    log("loadtopology: found group: " + group.getId());
                    for (LinkdVertex vx: group.getMembers()) {
                        log("loadtopology: found group/member: " + group.getId()+"/"+ vx.getId());
                        if (vx.isLeaf() && !vertexes.containsKey(vx.getId()))
                            group.removeMember(vx);
                    }
                    vertexes.put(group.getId(), group);
                }
            }
        }
        
        log("Found Vertexes: #" + vertexes.size());        
        log("Found Edges: #" + edges.size());
        log("Found Groups: #" + m_groupCounter);
        
        m_vertexContainer.addAll(vertexes.values());
        m_edgeContainer.addAll(edges);        
    }

    private String getEdgeTooltipText(DataLinkInterface link,
            LinkdVertex source, LinkdVertex target) {
        String tooltipText="";

        OnmsSnmpInterface sourceInterface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(Integer.parseInt(source.getId()), link.getIfIndex());
        OnmsSnmpInterface targetInterface = m_snmpInterfaceDao.findByNodeIdAndIfIndex(Integer.parseInt(target.getId()), link.getParentIfIndex());
        
        if (sourceInterface == null || targetInterface == null) {
            tooltipText+= "Type of the Link: Layer2";
        } else if (sourceInterface.getNetMask() != null && !sourceInterface.getNetMask().isLoopbackAddress() 
                && targetInterface.getNetMask() != null && !targetInterface.getNetMask().isLoopbackAddress()) {
            tooltipText+= "Type of the Link: Layer3/Layer2";
        }
        tooltipText +="\n";

        tooltipText += "Name: <endpoint1 " + source.getLabel() ;
        if (sourceInterface != null ) 
            tooltipText += ":"+sourceInterface.getIfName();
        
        tooltipText += " ---- endpoint2 " + target.getLabel();
        if (targetInterface != null) 
            tooltipText += ":"+targetInterface.getIfName();
        tooltipText +=">\n";

        
        if (targetInterface != null) {
            tooltipText += "Bandwidth: " + getHumanReadableIfSpeed(targetInterface.getIfSpeed());
            tooltipText +="\n";
            tooltipText += "Link status: " + getIfStatusString(targetInterface.getIfAdminStatus()) + "/" + getIfStatusString(targetInterface.getIfOperStatus());
            tooltipText +="\n";
        } else if (sourceInterface != null) {
            tooltipText += "Bandwidth: " + getHumanReadableIfSpeed(sourceInterface.getIfSpeed());
            tooltipText +="\n";
            tooltipText += "Link status: " + getIfStatusString(sourceInterface.getIfAdminStatus()) + "/" + getIfStatusString(sourceInterface.getIfOperStatus());
            tooltipText +="\n";
        }

        tooltipText += "EndPoint1: " + source.getLabel() + ", " + source.getIpAddr();
        tooltipText +="\n";
        
        tooltipText += "EndPoint2: " + target.getLabel() + ", " + target.getIpAddr();
        tooltipText +="\n";
        log("getEdgeTooltipText\n" + tooltipText);
        return tooltipText;
    }

    private String getNodeTooltipText(OnmsNode node, LinkdVertex vertex, OnmsIpInterface ip) {
        String tooltipText="";
        if (node.getSysDescription() != null && node.getSysDescription().length() >0) {
            tooltipText +=node.getSysDescription();
            tooltipText +="\n";
        }
        tooltipText += vertex.getIpAddr();
        tooltipText +="\n";
        
        tooltipText += vertex.getLabel();
        tooltipText +="\n";
        
        if (node.getSysLocation() != null && node.getSysLocation().length() >0) {
            tooltipText +=node.getSysLocation();
            tooltipText +="\n";
        }
        
        OnmsSeverity severity = OnmsSeverity.NORMAL;
        if (m_nodeToSeveritymap.containsKey(node.getId()))
                severity = m_nodeToSeveritymap.get(node.getId());
        tooltipText += "Alarm Status: " + severity.getLabel();
        tooltipText +="\n";
        
        tooltipText += getNodeStatusString(node.getType().charAt(0));
        if (ip.isManaged()) {
            tooltipText += "/Managed";
        } else {
            tooltipText += "/UnManaged";
        }
        tooltipText +="\n";

        log("getNodeTooltipText:\n" + tooltipText);
        
        return tooltipText;

    }
    
    protected String getIconName(OnmsNode node) {
        String iconName = SERVER_ICON_KEY;
        
        if (node.getSysObjectId() != null)
            iconName = "snmp:"+node.getSysObjectId();
        return iconName;
       
    }
    
    private OnmsIpInterface getAddress(OnmsNode node) {
        return m_ipInterfaceDao.findPrimaryInterfaceByNodeId(node.getId());
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

    private <T> List<T> getBeans(BeanContainer<?, T> container) {
        Collection<?> itemIds = container.getItemIds();
        List<T> beans = new ArrayList<T>(itemIds.size());
        
        for(Object itemId : itemIds) {
            beans.add(container.getItem(itemId).getBean());
        }
        
        return beans;
    }

    @Override
    public void setParent(Object vertexId, Object parentId) {
        m_vertexContainer.setParent(vertexId, parentId);
    }
    
      private String getIfStatusString(int ifStatusNum) {
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
    private String getHumanReadableIfSpeed(long ifSpeed) {
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
        System.err.println("LinkdTopologyProvider: "+ string);
    }

}
