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

package org.opennms.features.topology.plugins.topo.simple.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.features.topology.api.Constants;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.SimpleLeafVertex;
import org.opennms.features.topology.api.topo.AbstractVertexRef;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexListener;
import org.opennms.features.topology.api.topo.VertexProvider;
import org.opennms.features.topology.api.topo.VertexRef;
import org.opennms.features.topology.plugins.topo.simple.internal.operations.AddVertexOperation;
import org.opennms.features.topology.plugins.topo.simple.internal.operations.ConnectOperation;
import org.opennms.features.topology.plugins.topo.simple.internal.operations.RemoveVertexOperation;

import com.vaadin.ui.Window;

public class SimpleTopologyProviderTest {

    private class TestGraphContainer implements GraphContainer {

        @Override
        public int getSemanticZoomLevel() {
        	return 0;
        }

        @Override
        public void setSemanticZoomLevel(int level) {
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
        public GraphProvider getBaseTopology() {
            throw new UnsupportedOperationException("GraphContainer.getBaseTopology is not yet implemented.");
        }

        @Override
        public void setBaseTopology(GraphProvider graphProvider) {
            throw new UnsupportedOperationException("GraphContainer.setBaseTopology is not yet implemented.");
        }

        @Override
        public SelectionManager getSelectionManager() {
            throw new UnsupportedOperationException("GraphContainer.getSelectionManager is not yet implemented.");
        }

        @Override
        public Graph getGraph() {
            throw new UnsupportedOperationException("GraphContainer.getGraph is not yet implemented.");
        }

        @Override
        public Vertex getParent(VertexRef child) {
            throw new UnsupportedOperationException("GraphContainer.getParent is not yet implemented.");
        }

        @Override
        public Criteria getCriteria(String namespace) {
            throw new UnsupportedOperationException("GraphContainer.getCriteria is not yet implemented.");
        }

        @Override
        public void setCriteria(Criteria critiera) {
            throw new UnsupportedOperationException("GraphContainer.setCriteria is not yet implemented.");
        }

        @Override
        public double getScale() {
            throw new UnsupportedOperationException("DisplayState.getScale is not yet implemented.");
        }

        @Override
        public void setScale(double scale) {
            throw new UnsupportedOperationException("DisplayState.setScale is not yet implemented.");
        }

        @Override
        public void addChangeListener(ChangeListener listener) {
            // TODO Auto-generated method stub
        }

        @Override
        public void removeChangeListener(ChangeListener listener) {
            // TODO Auto-generated method stub
        }

        @Override
        public Collection<VertexRef> getVertexRefForest(Collection<? extends VertexRef> vertexRefs) {
            throw new UnsupportedOperationException("GraphContainer.getVertexRefForest is not yet implemented.");
        }

        @Override
        public void setDataSource(GraphProvider graphProvider) {
            // TODO Auto-generated method stub
        }
    }

    private class TestOperationContext implements OperationContext{
        
        private GraphContainer m_graphContainer;

        public TestOperationContext(GraphContainer graphContainer) {
            m_graphContainer = graphContainer;
        }
        
        @Override
        public Window getMainWindow() {
            return EasyMock.createMock(Window.class);
        }

        @Override
        public GraphContainer getGraphContainer() {
            return m_graphContainer;
        }

        @Override
        public boolean isChecked() {
            // TODO Auto-generated method stub
            return false;
        }

		@Override
		public DisplayLocation getDisplayLocation() {
			return DisplayLocation.MENUBAR;
		}
        
    }
    
    private GraphProvider m_topologyProvider;
    
    @Before
    public void setUp() {
        if(m_topologyProvider == null) {
            m_topologyProvider = new SimpleTopologyProvider();
        }
        
		m_topologyProvider.resetContainer();
		
		MockLogAppender.setupLogging();
    }
    
    @After
    public void tearDown() {
        if(m_topologyProvider != null) {
            m_topologyProvider.resetContainer();
        }
    }
    
	@Test
	public void test() {
		m_topologyProvider.resetContainer();

		assertTrue(m_topologyProvider.getVertices().size() == 0);

		Vertex vertexA = m_topologyProvider.addVertex(50, 100);
		assertTrue(m_topologyProvider.getVertices().size() == 1);
		//LoggerFactory.getLogger(this.getClass()).debug(m_topologyProvider.getVertices().get(0).toString());
		assertTrue(m_topologyProvider.containsVertexId(vertexA));
		assertTrue(m_topologyProvider.containsVertexId("v0"));
		assertFalse(m_topologyProvider.containsVertexId("v1"));
		VertexRef ref0 = new AbstractVertexRef(m_topologyProvider.getVertexNamespace(), "v0");
		VertexRef ref1 = new AbstractVertexRef(m_topologyProvider.getVertexNamespace(), "v1");
		assertTrue(m_topologyProvider.getVertices(Arrays.asList(new VertexRef[] {ref0})).size() == 1);
		assertTrue(m_topologyProvider.getVertices(Arrays.asList(new VertexRef[] {ref1})).size() == 0);

		Vertex vertexB = m_topologyProvider.addVertex(100, 50);
		assertTrue(m_topologyProvider.containsVertexId(vertexB));
		assertTrue(m_topologyProvider.containsVertexId("v1"));
		assertTrue(m_topologyProvider.getVertices(Arrays.asList(new VertexRef[] {ref1})).size() == 1);

		Vertex vertexC = m_topologyProvider.addVertex(100, 150);
		Vertex vertexD = m_topologyProvider.addVertex(150, 100);
		Vertex vertexE = m_topologyProvider.addVertex(200, 200);
		assertTrue(m_topologyProvider.getVertices().size() == 5);

		Vertex group1 = m_topologyProvider.addGroup("Group 1", Constants.GROUP_ICON_KEY);
		Vertex group2 = m_topologyProvider.addGroup("Group 2", Constants.GROUP_ICON_KEY);
		assertTrue(m_topologyProvider.getVertices().size() == 7);

		m_topologyProvider.setParent(vertexA, group1);
		m_topologyProvider.setParent(vertexB, group1);
		m_topologyProvider.setParent(vertexC, group2);
		m_topologyProvider.setParent(vertexD, group2);
		
		m_topologyProvider.connectVertices(vertexA, vertexB);
		m_topologyProvider.connectVertices(vertexA, vertexC);
		m_topologyProvider.connectVertices(vertexB, vertexC);
		m_topologyProvider.connectVertices(vertexB, vertexD);
		m_topologyProvider.connectVertices(vertexC, vertexD);
		m_topologyProvider.connectVertices(vertexA, vertexE);
		m_topologyProvider.connectVertices(vertexD, vertexE);
		
		m_topologyProvider.save("target/test-classes/test-graph.xml");
		
		m_topologyProvider.load("target/test-classes/test-graph.xml");
		
	}
	
	@Test
	public void loadSampleGraph() {
		GraphProvider topologyProvider = new SimpleTopologyProvider();
		topologyProvider.load("saved-vmware-graph.xml");
		
		System.err.println("Vertex Count: " + topologyProvider.getVertices().size());
		System.err.println("Edge Count: " + topologyProvider.getEdges().size());
	}
	
	@Test
	public void testAddVertexWithOperation() {
	    
	    GraphContainer graphContainer = EasyMock.createMock(GraphContainer.class);
	    
	    EasyMock.expect(graphContainer.getBaseTopology()).andReturn(m_topologyProvider).anyTimes();
	    graphContainer.redoLayout();
	    
	    EasyMock.replay(graphContainer);

	    List<VertexRef> targets = Collections.emptyList();
	    OperationContext operationContext = getOperationContext(graphContainer);
	    
	    AddVertexOperation addOperation = new AddVertexOperation(Constants.GROUP_ICON_KEY);
	    addOperation.execute(targets, operationContext);
	    
	    Collection<? extends Vertex> vertIds =  m_topologyProvider.getVertices();
	    assertEquals(1, vertIds.size());
	    assertTrue("v0".equals(vertIds.iterator().next().getId()));
	}

	@Test
	public void testAddVertexToAnotherVertexOperation() {

		m_topologyProvider.resetContainer();

		//Add existing vertex
        VertexRef vertexRef = addVertexToTopr();
	    
	    GraphContainer graphContainer = EasyMock.createMock(GraphContainer.class);
	    
	    EasyMock.expect(graphContainer.getBaseTopology()).andReturn(m_topologyProvider).anyTimes();
	    graphContainer.redoLayout();
	    
	    EasyMock.replay(graphContainer);
	    
	    
	    List<VertexRef> targets = new ArrayList<VertexRef>();
	    targets.add(vertexRef);
	    
	    OperationContext operationContext = getOperationContext(graphContainer);
	    AddVertexOperation addOperation = new AddVertexOperation(Constants.SERVER_ICON_KEY);
        addOperation.execute(targets, operationContext);
	    
        Collection<? extends Vertex> vertIds = m_topologyProvider.getVertices();
        assertEquals(2, vertIds.size());
        
        Collection<? extends Edge> edgeIds = m_topologyProvider.getEdges();
        assertEquals(1, edgeIds.size());
        
        EasyMock.verify(graphContainer);
	    
	}
	
    @Test
	public void testConnectVertices() {
		m_topologyProvider.resetContainer();

		Vertex vertexId = m_topologyProvider.addVertex(0, 0);
        
        assertEquals(1, m_topologyProvider.getVertices().size());
        Vertex vertex0 = m_topologyProvider.getVertices().iterator().next();
        assertEquals("v0", vertex0.getId());
        
        Vertex vertex1 = m_topologyProvider.addVertex(0, 0);
        assertEquals(2, m_topologyProvider.getVertices().size());
        
        Edge edgeId = m_topologyProvider.connectVertices(vertex0, vertex1);
        assertEquals(1, m_topologyProvider.getEdges().size());
        SimpleLeafVertex sourceLeafVert = (SimpleLeafVertex) edgeId.getSource().getVertex();
        SimpleLeafVertex targetLeafVert = (SimpleLeafVertex) edgeId.getTarget().getVertex();
        
        assertEquals("v0", sourceLeafVert.getId());
        assertEquals("v1", targetLeafVert.getId());
        
        EdgeRef[] edgeIds = m_topologyProvider.getEdgeIdsForVertex(vertexId);
        assertEquals(1, edgeIds.length);
        assertEquals(edgeId, edgeIds[0]);
        
	}
    
    @Test
    public void testRemoveVertexOperation() {
		m_topologyProvider.resetContainer();

	    GraphContainer graphContainer = EasyMock.createMock(GraphContainer.class);
	    
	    EasyMock.expect(graphContainer.getBaseTopology()).andReturn(m_topologyProvider).anyTimes();
	    graphContainer.redoLayout();
	    
	    EasyMock.replay(graphContainer);

        VertexRef vertexRef = addVertexToTopr();
        
        OperationContext operationContext = getOperationContext(graphContainer);
        RemoveVertexOperation removeOperation = new RemoveVertexOperation();
        removeOperation.execute(Arrays.asList(vertexRef), operationContext);
        
        assertEquals(0, m_topologyProvider.getVertices().size());
        
    }
    
    /**
     * TODO Refactor this test into the app bundle.
     */
    @Test
    @Ignore("Since this operation is now interactive, we need to change this unit test")
    public void testCreateGroupOperation() {
        VertexRef vertexId = addVertexToTopr();
        VertexRef vertexId2 = addVertexToTopr();
        
        GraphContainer graphContainer = EasyMock.createMock(GraphContainer.class);
        
        EasyMock.replay(graphContainer);
        
        /*
        CreateGroupOperation groupOperation = new CreateGroupOperation(m_topologyProvider);
        groupOperation.execute(Arrays.asList((Object)"1", (Object)"2"), getOperationContext(graphContainer));
        
        Item vertexItem1 = m_topologyProvider.getVertexContainer().getItem(vertexId);
        SimpleGroup parent = (SimpleGroup) vertexItem1.getItemProperty("parent").getValue();
        assertEquals(2, parent.getMembers().size());
        
        m_topologyProvider.addGroup("Test Group", Constants.GROUP_ICON_KEY);
        
        EasyMock.verify(graphContainer);
        */
    }
    
    @Test
    public void testTopoProviderSetParent() {
        VertexRef vertexId1 = addVertexToTopr();
        VertexRef vertexId2 = addVertexToTopr();
        
        final AtomicInteger eventsReceived = new AtomicInteger(0);
        
        m_topologyProvider.addVertexListener(new VertexListener() {

            @Override
            public void vertexSetChanged(VertexProvider provider,
                    Collection<? extends Vertex> added,
                    Collection<? extends Vertex> update,
                    Collection<String> removedVertexIds) {
                eventsReceived.incrementAndGet();
            }

            @Override
            public void vertexSetChanged(VertexProvider provider) {
                eventsReceived.incrementAndGet();
            }
        });

        Vertex groupId = m_topologyProvider.addGroup("Test Group", "groupIcon.jpg");
        assertEquals(1, eventsReceived.get());
        eventsReceived.set(0);
        
        m_topologyProvider.setParent(vertexId1, groupId);
        m_topologyProvider.setParent(vertexId2, groupId);
        
        assertEquals(2, eventsReceived.get());
    }
    
    @Test
    public void testConnectVerticesOperation() {
    	
		m_topologyProvider.resetContainer();

        VertexRef vertexId1 = addVertexToTopr();
        VertexRef vertexId2 = addVertexToTopr();
        
        GraphContainer graphContainer = EasyMock.createMock(GraphContainer.class);

        EasyMock.expect(graphContainer.getBaseTopology()).andReturn(m_topologyProvider).anyTimes();

        EasyMock.replay(graphContainer);
        
        List<VertexRef> targets = new ArrayList<VertexRef>();
        targets.add(vertexId1);
        targets.add(vertexId2);
        
        ConnectOperation connectOperation = new ConnectOperation();
        connectOperation.execute(targets, getOperationContext(graphContainer));
        
        Collection<? extends Edge> edgeIds = m_topologyProvider.getEdges();
        assertEquals(1, edgeIds.size());
        
        for(Edge edgeId : edgeIds) {
            SimpleLeafVertex source = (SimpleLeafVertex) edgeId.getSource().getVertex();
            SimpleLeafVertex target = (SimpleLeafVertex) edgeId.getTarget().getVertex();
            assertNotNull(source);
            assertNotNull(target);
            assertEquals(vertexId1.getId(), source.getId());
            assertEquals(vertexId2.getId(), target.getId());
        }
        
        EasyMock.verify(graphContainer);
    }
	
	
	private TestOperationContext getOperationContext(GraphContainer mockedContainer) {
        return new TestOperationContext(mockedContainer);
    }
	
	private VertexRef addVertexToTopr() {
	    return m_topologyProvider.addVertex(0, 0);
    }
}
