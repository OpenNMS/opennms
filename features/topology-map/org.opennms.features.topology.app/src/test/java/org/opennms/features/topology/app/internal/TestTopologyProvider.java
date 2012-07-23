package org.opennms.features.topology.app.internal;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.opennms.features.topology.api.TopologyProvider;
import org.opennms.features.topology.api.VertexContainer;

import com.vaadin.data.Item;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.data.util.BeanItem;

public class TestTopologyProvider implements TopologyProvider{
    private TestVertexContainer m_vertexContainer;
    private BeanContainer<String, TestEdge> m_edgeContainer;
    private int m_vertexCounter = 0;
    private int m_edgeCounter = 0;
    private int m_groupCounter = 0;
    
    public TestTopologyProvider() {
        m_vertexContainer = new TestVertexContainer();
        m_edgeContainer = new BeanContainer<String, TestEdge>(TestEdge.class);
        m_edgeContainer.setBeanIdProperty("id");
        
        String vId1 = getNextVertexId();
        TestVertex v1 = new TestLeafVertex(vId1, 0, 0);
        v1.setIcon("icon.jpg");
        Item beanItem = m_vertexContainer.addBean(v1);
        
        String vId2 = getNextVertexId();
        TestVertex v2 = new TestLeafVertex(vId2, 0, 0);
        v2.setIcon("icon.jpg");
        Item beanItem2 = m_vertexContainer.addBean(v2);
        
        String edgeId = getNextEdgeId();
        TestEdge edge = new TestEdge(edgeId, v1, v2);
        m_edgeContainer.addBean(edge);
        
    }
    
    public Object addVertex() {
        String id = getNextVertexId();
        TestVertex vert = new TestLeafVertex(id, 0, 0);
        vert.setIcon("icon.jpb");
        m_vertexContainer.addBean(vert);
        return id;
        
    }
    
    private String getNextEdgeId() {
        return "e" + m_edgeCounter++;
    }

    private String getNextVertexId() {
        return "v" + m_vertexCounter++;
    }

    @Override
    public void setParent(Object vertexId, Object parentId) {
        assertVertex(vertexId);
        assertGroup(parentId);
        
        m_vertexContainer.setParent(vertexId, parentId);
    }

    @Override
    public Object addGroup(String groupIcon) {
        String nextGroupId = getNextGroupId();
        addGroup(nextGroupId, groupIcon);
        return nextGroupId;
    }

    private Item addGroup(String groupId, String groupIcon) {
        if(m_vertexContainer.containsId(groupId)) {
            throw new IllegalArgumentException("A vertex or group with id " + groupId + " already exists!");
        }
        TestVertex vertex = new TestGroup(groupId);
        vertex.setIcon(groupIcon);
        return m_vertexContainer.addBean(vertex);
    }

    private String getNextGroupId() {
        return "g" + m_groupCounter++;
    }

    @Override
    public boolean containsVertexId(Object vertexId) {
        return m_vertexContainer.containsId(vertexId);
    }

    @Override
    public void save(String filename) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void load(String filename) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public VertexContainer<?, ?> getVertexContainer() {
        return m_vertexContainer;
    }

    @Override
    public BeanContainer<?, ?> getEdgeContainer() {
        return m_edgeContainer;
    }

    @Override
    public Collection<?> getVertexIds() {
        return m_vertexContainer.getItemIds();
    }

    @Override
    public Collection<?> getEdgeIds() {
        return m_edgeContainer.getItemIds();
    }

    @Override
    public Item getVertexItem(Object vertexId) {
        return m_vertexContainer.getItem(vertexId);
    }

    @Override
    public Item getEdgeItem(Object edgeId) {
        assertEdge(edgeId);
        return m_edgeContainer.getItem(edgeId);
    }

    @Override
    public Collection<?> getEdgeIdsForVertex(Object vertexId) {
        TestVertex vertex = getRequiresVertex(vertexId);
        List<Object> edges = new ArrayList<Object>(vertex.getEdges().size());
        
        for(TestEdge e : vertex.getEdges()) {
            Object edgeId = e.getId();
            edges.add(edgeId);
        }
        return edges;
    }

    @Override
    public Collection<?> getEndPointIdsForEdge(Object edgeId) {
        TestEdge edge = getRequiredEdge(edgeId);
        List<Object> endPoints = new ArrayList<Object>(2);
        endPoints.add(edge.getSource().getId());
        endPoints.add(edge.getTarget().getId());
        return endPoints;
    }
    
    private TestEdge getRequiredEdge(Object edgeId) {
        return getEdge(edgeId, true);
    }

    private TestEdge getEdge(Object edgeId, boolean required) {
        BeanItem<TestEdge> item = m_edgeContainer.getItem(edgeId);
        if(required && item == null) {
            throw new IllegalArgumentException("required edge " + edgeId + " not found");
        }
        return item == null ? null : item.getBean();
    }

    private TestVertex getRequiresVertex(Object vertexId) {
        return getVertex(vertexId, true);
    }

    private TestVertex getVertex(Object vertexId, boolean required) {
        BeanItem<TestVertex> item = m_vertexContainer.getItem(vertexId);
        if(required && item == null) {
            throw new IllegalArgumentException("required vertex " + vertexId + " not found");
        }
        return item == null ? null : item.getBean();
    }
    
    private void assertGroup(Object parentId) {
        assertTrue(m_vertexContainer.containsId(parentId));
        TestVertex parentItem = m_vertexContainer.getItem(parentId).getBean();
        assertFalse(parentItem.isLeaf());
    }

    private void assertVertex(Object vertexId) {
        assertTrue(m_vertexContainer.containsId(vertexId));
    }

    private void assertEdge(Object edgeId) {
        assertTrue(m_edgeContainer.containsId(edgeId));
    }



}