package org.opennms.features.topology.app.internal.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.HierarchicalBeanContainer;
import org.opennms.features.topology.api.TopologyProvider;
import org.opennms.features.topology.api.VertexContainer;
import org.opennms.features.topology.api.support.FilterableHierarchicalContainer;
import org.opennms.features.topology.api.support.VertexProviderFilterableHierarchicalContainer;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.app.internal.SimpleGraphContainer;
import org.opennms.features.topology.app.internal.TestTopologyProvider;
import org.opennms.features.topology.plugins.topo.adapter.TPGraphProvider;



public class FilterableHeirarchicalContainerTest {
    
    private class DefaultTestVertexContainer extends VertexContainer<String, Vertex>{

        public DefaultTestVertexContainer(Class<? super Vertex> type) {
            super(type);
        }

        @Override
        public Collection<?> getChildren(Object itemId) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Object getParent(Object itemId) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Collection<?> rootItemIds() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public boolean setParent(Object itemId, Object newParentId)
                throws UnsupportedOperationException {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean areChildrenAllowed(Object itemId) {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean setChildrenAllowed(Object itemId,
                boolean areChildrenAllowed)
                throws UnsupportedOperationException {
            // TODO Auto-generated method stub
            return false;
        }
        
    }
    
    
    private FilterableHierarchicalContainer m_container;
    private VertexProviderFilterableHierarchicalContainer m_topologyProvidercontainer;
    
    HierarchicalBeanContainer<String, Vertex> m_beanContainer;
    VertexProvider m_vertexProvider;
    GraphContainer m_graphContainer;
    TopologyProvider m_topologyProvider;
    
    @Before
    public void setUp() {
        m_topologyProvider = new TestTopologyProvider("test");
        m_graphContainer = new SimpleGraphContainer(m_topologyProvider);
        m_beanContainer = new DefaultTestVertexContainer(Vertex.class);
        m_beanContainer.setBeanIdProperty("id");
        m_vertexProvider = new TPGraphProvider(m_topologyProvider);
        
    }
    
    @Test
    public void testFilterableHeirarchicalContainer() {
        FilterableHierarchicalContainer container = new FilterableHierarchicalContainer(m_beanContainer);
        VertexProviderFilterableHierarchicalContainer vertexContainer = new VertexProviderFilterableHierarchicalContainer(m_vertexProvider);
        
        assertNotNull(vertexContainer.getItemIds());
        assertEquals(2, vertexContainer.getItemIds().size());
        
        Collection<?> itemIds = vertexContainer.getItemIds();
        assertEquals(2, itemIds.size());
        String firstItem = (String) itemIds.iterator().next();
        assertEquals("v1", vertexContainer.getContainerProperty(firstItem, "id").getValue());
        assertEquals(String.class, vertexContainer.getType("id"));
        assertEquals(String.class, vertexContainer.getType("label"));
        
        assertEquals(2, vertexContainer.rootItemIds().size());
        assertTrue(vertexContainer.isRoot("v1"));
        
        Collection<?> propertyIds = vertexContainer.getContainerPropertyIds();
        assertEquals(2, propertyIds.size());
        Iterator it = propertyIds.iterator();
        assertEquals("id", it.next());
        assertEquals("label", it.next());
    }

}
