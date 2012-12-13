package org.opennms.features.topology.app.internal.support;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.features.topology.api.HierarchicalBeanContainer;
import org.opennms.features.topology.api.SimpleVertexContainer;
import org.opennms.features.topology.api.support.FilterableHierarchicalContainer;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.app.internal.TestTopologyProvider;



public class FilterableHeirarchicalContainerTest {

    private FilterableHierarchicalContainer m_container;
    
    HierarchicalBeanContainer<VertexRef, Vertex> m_beanContainer;
    VertexProvider m_vertexProvider;
    GraphProvider m_topologyProvider;
    
    @Before
    public void setUp() {
        m_topologyProvider = new TestTopologyProvider("test");
        m_beanContainer = new SimpleVertexContainer();
        m_beanContainer.setBeanIdProperty("id");
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
