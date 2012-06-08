package org.opennms.features.topology.plugins.topo.simple.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.opennms.features.topology.plugins.topo.simple.internal.operations.Constants.GROUP_ICON;
import static org.opennms.features.topology.plugins.topo.simple.internal.operations.Constants.SERVER_ICON;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.VertexContainer;
import org.opennms.features.topology.plugins.topo.simple.internal.operations.AddVertexOperation;
import org.opennms.features.topology.plugins.topo.simple.internal.operations.CreateGroupOperation;
import org.opennms.features.topology.plugins.topo.simple.internal.operations.RemoveVertexOperation;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
import com.vaadin.ui.Window;

public class SimpleTopologyProviderTest {
    
    public class TestVertex {

    }

    
    private class TestGraphContainer implements GraphContainer{
        
        
        private SimpleVertexContainer m_vertContainer;

        public TestGraphContainer(SimpleVertexContainer vertContainer) {
            m_vertContainer = vertContainer;
        }
        
        @Override
        public Integer getSemanticZoomLevel() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void setSemanticZoomLevel(Integer level) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void setLayoutAlgorithm(LayoutAlgorithm layoutAlgorithm) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public LayoutAlgorithm getLayoutAlgorithm() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public void redoLayout() {
            // TODO Auto-generated method stub
            
        }

        @Override
        public Property getProperty(String property) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public VertexContainer<?, ?> getVertexContainer() {
            return m_vertContainer;
        }

        @Override
        public BeanContainer<?, ?> getEdgeContainer() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Collection<?> getVertexIds() {
            return null;
        }

        @Override
        public Collection<?> getEdgeIds() {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Item getVertexItem(Object vertexId) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Item getEdgeItem(Object edgeId) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Collection<?> getEndPointIdsForEdge(Object edgeId) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public Collection<?> getEdgeIdsForVertex(Object vertexId) {
            // TODO Auto-generated method stub
            return null;
        }
        
    }
    
    private class TestOperationContext implements OperationContext{
        
        private GraphContainer m_graphContainer;

        public TestOperationContext(GraphContainer graphContainer) {
            m_graphContainer = graphContainer;
        }
        
        @Override
        public Window getMainWindow() {
            return null;
        }

        @Override
        public GraphContainer getGraphContainer() {
            return m_graphContainer;
        }
        
    }
    
    private SimpleTopologyProvider m_topologyProvider;
    
    @Before
    public void setUp() {
        if(m_topologyProvider == null) {
            m_topologyProvider = new SimpleTopologyProvider();
        }
    }
    
    @After
    public void tearDown() {
        if(m_topologyProvider != null) {
            m_topologyProvider.resetContainer();
        }
    }
    
	@Test
	public void test() {
		SimpleTopologyProvider topologyProvider = new SimpleTopologyProvider();
		
		String vertexA = (String) topologyProvider.addVertex(50, 100, SERVER_ICON);
		String vertexB = (String) topologyProvider.addVertex(100, 50, SERVER_ICON);
		String vertexC = (String) topologyProvider.addVertex(100, 150, SERVER_ICON);
		String vertexD = (String) topologyProvider.addVertex(150, 100, SERVER_ICON);
		String vertexE = (String) topologyProvider.addVertex(200, 200, SERVER_ICON);
		String group1 = (String) topologyProvider.addGroup(GROUP_ICON);
		String group2 = (String) topologyProvider.addGroup(GROUP_ICON);
		topologyProvider.getVertexContainer().setParent(vertexA, group1);
		topologyProvider.getVertexContainer().setParent(vertexB, group1);
		topologyProvider.getVertexContainer().setParent(vertexC, group2);
		topologyProvider.getVertexContainer().setParent(vertexD, group2);
		
		topologyProvider.connectVertices(vertexA, vertexB);
		topologyProvider.connectVertices(vertexA, vertexC);
		topologyProvider.connectVertices(vertexB, vertexC);
		topologyProvider.connectVertices(vertexB, vertexD);
		topologyProvider.connectVertices(vertexC, vertexD);
		topologyProvider.connectVertices(vertexA, vertexE);
		topologyProvider.connectVertices(vertexD, vertexE);
		
		topologyProvider.save("test-graph.xml");
		
		topologyProvider.load("test-graph.xml");
		
	}
	
	@Test
	public void testAddVertexWithOperation() {
	    List<Object> targets = Collections.EMPTY_LIST;
	    OperationContext operationContext = getOperationContext();
	    
	    AddVertexOperation addOperation = new AddVertexOperation(GROUP_ICON, m_topologyProvider);
	    addOperation.execute(targets, operationContext);
	    
	    Collection<?> vertIds =  m_topologyProvider.getVertexIds();
	    assertEquals(1, vertIds.size());
	    assertTrue(vertIds.contains("v0"));
	}

	@Test
	public void testAddVertexToAnotherVertexOperation() {
	    //Add existing vertex
	    Object vertexId = addVertexToTopr();
	    
	    List<Object> targets = new ArrayList<Object>();
	    targets.add(vertexId);
	    
	    OperationContext operationContext = getOperationContext();
	    AddVertexOperation addOperation = new AddVertexOperation(SERVER_ICON, m_topologyProvider);
        addOperation.execute(targets, operationContext);
	    
        Collection<?> vertIds = m_topologyProvider.getVertexIds();
        assertEquals(2, vertIds.size());
        
        Collection<?> edgeIds = m_topologyProvider.getEdgeIds();
        assertEquals(1, edgeIds.size());
	    
	}
	
    @Test
	public void testConnectVertices() {
        Object vertexId = m_topologyProvider.addVertex(0, 0, SERVER_ICON);
        
        assertEquals(1, m_topologyProvider.getVertexIds().size());
        Object vertId = m_topologyProvider.getVertexIds().iterator().next();
        assertEquals("v0", vertId);
        
        m_topologyProvider.addVertex(0, 0, SERVER_ICON);
        assertEquals(2, m_topologyProvider.getVertexIds().size());
        
        Object edgeId = m_topologyProvider.connectVertices("v0", "v1");
        assertEquals(1, m_topologyProvider.getEdgeIds().size());
        SimpleLeafVertex sourceLeafVert = (SimpleLeafVertex) m_topologyProvider.getEdgeItem(edgeId).getItemProperty("source").getValue();
        SimpleLeafVertex targetLeafVert = (SimpleLeafVertex) m_topologyProvider.getEdgeItem(edgeId).getItemProperty("target").getValue();
        
        assertEquals("v0", sourceLeafVert.getId());
        assertEquals("v1", targetLeafVert.getId());
        
        Collection<?> edgeIds = m_topologyProvider.getEdgeIdsForVertex(vertexId);
        assertEquals(1, edgeIds.size());
        assertEquals(edgeId, edgeIds.iterator().next());
        
	}
    
    @Test
    public void testRemoveVertexOperation() {
        Object vertexId = addVertexToTopr();
        
        OperationContext operationContext = getOperationContext();
        RemoveVertexOperation removeOperation = new RemoveVertexOperation(m_topologyProvider);
        removeOperation.execute(Arrays.asList(vertexId), operationContext);
        
        assertEquals(0, m_topologyProvider.getVertexIds().size());
        
    }
    
    @Test
    public void testCreateGroupOperation() {
        Object vertexId = addVertexToTopr();
        Object vertexId2 = addVertexToTopr();
        
        CreateGroupOperation groupOperation = new CreateGroupOperation(m_topologyProvider);
        groupOperation.execute(Arrays.asList(vertexId, vertexId2), getOperationContext());
        
        
        Item vertexItem1 = m_topologyProvider.getVertexItem(vertexId);
        SimpleGroup parent = (SimpleGroup) vertexItem1.getItemProperty("parent").getValue();
        assertEquals(2, parent.getMembers().size());
        assertEquals(0, parent.getSemanticZoomLevel());
        
        m_topologyProvider.addGroup(GROUP_ICON);
        
    }
	
	
	private TestOperationContext getOperationContext() {
        return new TestOperationContext(new TestGraphContainer(new SimpleVertexContainer()));
    }
	
	private Object addVertexToTopr() {
	    return m_topologyProvider.addVertex(0, 0, SERVER_ICON);
    }

}
