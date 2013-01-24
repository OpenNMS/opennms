package org.opennms.features.topology.app.internal.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.HierarchicalBeanContainer;
import org.opennms.features.topology.api.TopologyProvider;
import org.opennms.features.topology.api.VertexContainer;
import org.opennms.features.topology.api.support.FilterableHierarchicalContainer;
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
        m_beanContainer.addAll(m_vertexProvider.getVertices());
        
    }
    
    @Test
    @Ignore("This seems not to be finished")
    public void testFilterableHeirarchicalContainer() {
        FilterableHierarchicalContainer container = new FilterableHierarchicalContainer(m_beanContainer);
        
        System.err.println(m_beanContainer.getItemIds());
        assertNotNull(container.getItemIds());
        System.err.println(container.getItemIds());
        assertEquals(2, container.getItemIds().size());
        
        Collection<?> itemIds = container.getItemIds();
        assertEquals(2, itemIds.size());
        String firstItem = (String) itemIds.iterator().next();
        assertEquals("v0", container.getContainerProperty(firstItem, "id").getValue());
        assertEquals(String.class, container.getType("id"));
        assertEquals(String.class, container.getType("label"));
        
        assertEquals(2, container.rootItemIds().size());
        assertTrue(container.isRoot("v0"));
        
        Collection<?> propertyIds = container.getContainerPropertyIds();
        assertEquals(2, propertyIds.size());
        Iterator it = propertyIds.iterator();
        assertEquals("id", it.next());
        assertEquals("label", it.next());
    }

}
