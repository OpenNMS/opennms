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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
import org.opennms.netmgt.model.DataLinkInterface;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;

public class LinkdTopologyProvider implements TopologyProvider {
    public static final String GROUP_ICON_KEY = "linkd-group";
    public static final String SERVER_ICON_KEY = "linkd-server";

    DataLinkInterfaceDao m_dataLinkInterfaceDao;
    
    NodeDao m_nodeDao;
    
    IpInterfaceDao m_ipInterfaceDao;
    
    public IpInterfaceDao getIpInterfaceDao() {
        return m_ipInterfaceDao;
    }

    public void setIpInterfaceDao(IpInterfaceDao ipInterfaceDao) {
        m_ipInterfaceDao = ipInterfaceDao;
    }

    boolean addNodeWithoutLink = true;
    
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

    public NodeDao getNodeDao() {
        return m_nodeDao;
    }

    public void setNodeDao(NodeDao nodeDao) {
        m_nodeDao = nodeDao;
    }

    public void onInit() {
        log("init: loading topology");
        loadtopology();
    }
    
    public LinkdTopologyProvider() {
        m_vertexContainer = new LinkdVertexContainer();
        m_edgeContainer = new BeanContainer<String, LinkdEdge>(LinkdEdge.class);
        m_edgeContainer.setBeanIdProperty("id");
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
    private void loadfromfile(String filename) {
        SimpleGraph graph = JAXB.unmarshal(new File(filename), SimpleGraph.class);
        
        m_vertexContainer.addAll(graph.m_vertices);        
        m_edgeContainer.addAll(graph.m_edges);
    }
    
    private void loadtopology() {
        log("loadtopology: loading topology");
        LinkdVertexContainer vertexContainer = new LinkdVertexContainer();
        BeanContainer<String, LinkdEdge> edgeContainer = new BeanContainer<String, LinkdEdge>(LinkdEdge.class);
        edgeContainer.setBeanIdProperty("id");
        for (DataLinkInterface link: m_dataLinkInterfaceDao.findAll()) {
            OnmsNode node = m_nodeDao.get(link.getNode().getId());
            log("found node: " + node.getLabel());
            String sourceId = node.getNodeId();
            LinkdVertex source;
            BeanItem<LinkdVertex> item = vertexContainer.getItem(sourceId);
            if (item == null) {
                source = new LinkdNodeVertex(node.getNodeId(), 0, 0, getIconName(node), node.getLabel(), getAddress(node));
                vertexContainer.addBean( source);
            }
            else {
                source = item.getBean();
            }

            OnmsNode parentNode = m_nodeDao.get(link.getNodeParentId());
            log("found parentnode: " + parentNode.getLabel());
                       String targetId = parentNode.getNodeId();
            LinkdVertex target;
            item = vertexContainer.getItem(targetId);
            if (item == null) {
                target = new LinkdNodeVertex(parentNode.getNodeId(), 0, 0, getIconName(parentNode), parentNode.getLabel(), getAddress(parentNode));
                vertexContainer.addBean(target);                    
            } else {
                target = item.getBean();
            }            
            edgeContainer.addBean(new LinkdEdge(link.getDataLinkInterfaceId(),source,target));
        }
        
        if (isAddNodeWithoutLink()) {
            for (OnmsNode node: m_nodeDao.findAll()) {
                log("parsing node: " + node.getLabel());
                String nodeId = node.getNodeId();
                LinkdVertex linklessnode;
                BeanItem<LinkdVertex> item = vertexContainer.getItem(nodeId);
                if (item == null) {
                    log("adding linklessnode: " + node.getLabel());
                    linklessnode = new LinkdNodeVertex(node.getNodeId(), 0, 0, getIconName(node), node.getLabel(), getAddress(node));
                    vertexContainer.addBean(linklessnode);
                }                
            }
        }
        m_vertexContainer.removeAllItems();
        m_vertexContainer=vertexContainer;
        m_edgeContainer.removeAllItems();
        m_edgeContainer=edgeContainer;
    }

    protected String getIconName(OnmsNode node) {
        String iconName = SERVER_ICON_KEY;
        
        if (node.getSysObjectId() != null)
            iconName = "snmp:"+node.getSysObjectId();
        return iconName;
       
    }
    
    private String getAddress(OnmsNode node) {
        OnmsIpInterface primary = m_ipInterfaceDao.findPrimaryInterfaceByNodeId(node.getId());
	return primary == null ? null : primary.getIpAddress().getHostAddress();
    }
    
    @Override
    public void save(String filename) {
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

    
    private void log(final String string) {
        System.err.println("LinkdTopologyProvider: "+ string);
    }

}
