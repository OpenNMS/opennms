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

package org.opennms.features.topology.plugins.topo.onmsdao.internal;


import java.util.Collection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.features.topology.api.Graph;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.TopologyProvider;
import org.opennms.features.topology.api.topo.Criteria;
import org.opennms.features.topology.api.topo.Edge;
import org.opennms.features.topology.api.topo.EdgeRef;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.api.topo.VertexRef;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.vaadin.ui.Window;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-mock.xml"
        })

public class OnmsTopologyProviderTest {
    

    @Autowired
    private OnmsTopologyProvider m_topologyProvider;
    
    @Autowired
     private EasyMockDataPopulator m_databasePopulator;
     public class TestVertex {

    }

    
    private class TestGraphContainer implements GraphContainer{
        
        
        private SimpleVertexContainer m_vertContainer;

        public TestGraphContainer(SimpleVertexContainer vertContainer) {
            m_vertContainer = vertContainer;
        }
        
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
	public TopologyProvider getDataSource() {
		throw new UnsupportedOperationException("GraphContainer.getDataSource is not yet implemented.");
	}

	@Override
	public void setDataSource(TopologyProvider topologyProvider) {
		throw new UnsupportedOperationException("GraphContainer.setDataSource is not yet implemented.");
	}

	@Override
	public GraphProvider getBaseTopology() {
		throw new UnsupportedOperationException("GraphContainer.getBaseTopology is not yet implemented.");
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
	public Vertex getVertex(VertexRef ref) {
		throw new UnsupportedOperationException("GraphContainer.getVertex is not yet implemented.");
	}

	@Override
	public Edge getEdge(EdgeRef ref) {
		throw new UnsupportedOperationException("GraphContainer.getEdge is not yet implemented.");
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
	public Vertex getParent(VertexRef child) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setBaseTopology(GraphProvider graphProvider) {
		// TODO Auto-generated method stub
		
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
	public Collection<? extends Vertex> getVertices() {
		throw new UnsupportedOperationException("GraphContainer.getVertices is not yet implemented.");
	}

	@Override
	public Collection<? extends Vertex> getChildren(VertexRef vRef) {
		throw new UnsupportedOperationException("GraphContainer.getChildren is not yet implemented.");
	}

	@Override
	public Collection<? extends Vertex> getRootGroup() {
		throw new UnsupportedOperationException("GraphContainer.getRootGroup is not yet implemented.");
	}

	@Override
	public boolean hasChildren(VertexRef vRef) {
		throw new UnsupportedOperationException("GraphContainer.hasChildren is not yet implemented.");
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
            return null;
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
        
    }

    @Before
    public void setUp() {
        m_databasePopulator.populateDatabase();
    }
    
    @After
    public void tearDown() {
        if(m_topologyProvider != null) {
            m_topologyProvider.resetContainer();
        }
        m_databasePopulator.tearDown();
    }
    
	@Test
	public void testLoad1() {		
		m_topologyProvider.load("1");
	}
	
        @Test
        public void testLoad2() {               
                m_topologyProvider.load("2");
        }

}
