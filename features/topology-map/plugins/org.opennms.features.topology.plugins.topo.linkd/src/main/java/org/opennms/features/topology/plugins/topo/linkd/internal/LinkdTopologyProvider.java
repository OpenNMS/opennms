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
import org.opennms.netmgt.model.DataLinkInterface;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;

public class LinkdTopologyProvider implements TopologyProvider {
    public static final String GROUP_ICON = "VAADIN/widgetsets/org.opennms.features.topology.widgetset.gwt.TopologyWidgetset/topologywidget/images/group.png";
    public static final String SERVER_ICON = "VAADIN/widgetsets/org.opennms.features.topology.widgetset.gwt.TopologyWidgetset/topologywidget/images/server.png";
    public static final String SWITCH_ICON = "VAADIN/widgetsets/org.opennms.features.topology.widgetset.gwt.TopologyWidgetset/topologywidget/images/srx100.png";

    DataLinkInterfaceDao m_dataLinkInterfaceDao;
    
    private LinkdVertexContainer m_vertexContainer;
    private BeanContainer<String, LinkdEdge> m_edgeContainer;

    private int m_groupCounter = 0;
    
    public DataLinkInterfaceDao getDataLinkInterfaceDao() {
        return m_dataLinkInterfaceDao;
    }

    public void setDataLinkInterfaceDao(DataLinkInterfaceDao dataLinkInterfaceDao) {
        m_dataLinkInterfaceDao = dataLinkInterfaceDao;
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
        LinkdVertex vertex = new LinkdGroup(groupId);
        vertex.setIcon(icon);
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
        reset();
        if (filename == null) {
            loadtopology();
        } else {
            loadfromfile(filename);
        }
    }

    private void reset() {
        m_vertexContainer.removeAllItems();
        m_edgeContainer.removeAllItems();        
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
        for (DataLinkInterface link: m_dataLinkInterfaceDao.findAll()) {
            String sourceId = link.getNode().getNodeId();
            LinkdVertex source;
            BeanItem<LinkdVertex> item = m_vertexContainer.getItem(sourceId);
            if (item == null) {
                source = new LinkdNodeVertex(link.getNode().getNodeId(), 0, 0, SWITCH_ICON);
                m_vertexContainer.addBean( source);
            }
            else {
                source = item.getBean();
            }

            String targetId = link.getNodeParentId().toString();
            LinkdVertex target;
            item = m_vertexContainer.getItem(targetId);
            if (item == null) {
                target = new LinkdNodeVertex(targetId, 0, 0, SWITCH_ICON);
                m_vertexContainer.addBean( target);                    
            }
            else {
                target = item.getBean();
            }
            
            m_edgeContainer.addBean(new LinkdEdge(link.getId().toString(),source,target));
        }        
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

}
