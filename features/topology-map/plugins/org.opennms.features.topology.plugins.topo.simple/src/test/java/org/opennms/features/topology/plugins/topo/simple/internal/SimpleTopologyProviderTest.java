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
import org.opennms.features.topology.api.Constants;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.SimpleLeafVertex;
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
	public Collection<VertexRef> getVertexRefForest(
			Collection<? extends VertexRef> vertexRefs) {
		throw new UnsupportedOperationException("GraphContainer.getVertexRefForest is not yet implemented.");
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
		

    }
    
    @After
    public void tearDown() {
        if(m_topologyProvider != null) {
            m_topologyProvider.resetContainer();
        }
    }
    
	@Test
	public void test() {
		GraphProvider topologyProvider = new SimpleTopologyProvider();
		topologyProvider.resetContainer();
		
		Vertex vertexA = topologyProvider.addVertex(50, 100);
		Vertex vertexB = topologyProvider.addVertex(100, 50);
		Vertex vertexC = topologyProvider.addVertex(100, 150);
		Vertex vertexD = topologyProvider.addVertex(150, 100);
		Vertex vertexE = topologyProvider.addVertex(200, 200);
		Vertex group1 = topologyProvider.addGroup("Group 1", Constants.GROUP_ICON_KEY);
		Vertex group2 = topologyProvider.addGroup("Group 2", Constants.GROUP_ICON_KEY);
		topologyProvider.setParent(vertexA, group1);
		topologyProvider.setParent(vertexB, group1);
		topologyProvider.setParent(vertexC, group2);
		topologyProvider.setParent(vertexD, group2);
		
		topologyProvider.connectVertices(vertexA, vertexB);
		topologyProvider.connectVertices(vertexA, vertexC);
		topologyProvider.connectVertices(vertexB, vertexC);
		topologyProvider.connectVertices(vertexB, vertexD);
		topologyProvider.connectVertices(vertexC, vertexD);
		topologyProvider.connectVertices(vertexA, vertexE);
		topologyProvider.connectVertices(vertexD, vertexE);
		
		topologyProvider.save("target/test-classes/test-graph.xml");
		
		topologyProvider.load("target/test-classes/test-graph.xml");
		
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
	    
	    List<VertexRef> targets = Collections.emptyList();
	    OperationContext operationContext = getOperationContext(new TestGraphContainer());
	    
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

        Object groupId = m_topologyProvider.addGroup("Test Group", "groupIcon.jpg");
        assertEquals(1, eventsReceived.get());
        eventsReceived.set(0);
        
        m_topologyProvider.setParent(vertexId1.getId(), groupId);
        m_topologyProvider.setParent(vertexId2.getId(), groupId);
        
        assertEquals(2, eventsReceived.get());
    }
    
    @Test
    public void testConnectVerticesOperation() {
    	
		m_topologyProvider.resetContainer();

        VertexRef vertexId1 = addVertexToTopr();
        VertexRef vertexId2 = addVertexToTopr();
        
        GraphContainer graphContainer = EasyMock.createMock(GraphContainer.class);
        
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
