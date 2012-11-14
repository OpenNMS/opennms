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
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.SelectionManager;
import org.opennms.features.topology.api.TopologyProvider;
import org.opennms.features.topology.api.VertexContainer;
import org.opennms.features.topology.api.topo.GraphProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.vaadin.data.Item;
import com.vaadin.data.Property;
import com.vaadin.data.util.BeanContainer;
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

        @Override
        public Object getVertexItemIdForVertexKey(Object key) {
            // TODO Auto-generated method stub
            return null;
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
	public void setBaseTopology(GraphProvider graphProvider) {
		throw new UnsupportedOperationException("GraphContainer.setBaseTopology is not yet implemented.");
	}

	@Override
	public int getX(Object vertexId) {
		throw new UnsupportedOperationException("GraphContainer.getX is not yet implemented.");
	}

	@Override
	public void setX(Object vertexId, int x) {
		throw new UnsupportedOperationException("GraphContainer.setX is not yet implemented.");
	}

	@Override
	public int getY(Object vertexId) {
		throw new UnsupportedOperationException("GraphContainer.getY is not yet implemented.");
	}

	@Override
	public void setY(Object vertexId, int y) {
		throw new UnsupportedOperationException("GraphContainer.setY is not yet implemented.");
	}

	@Override
	public int getSemanticZoomLevel(Object vertexId) {
		throw new UnsupportedOperationException("GraphContainer.getSemanticZoomLevel is not yet implemented.");
	}
	@Override
	public Object getGroupId(Object vertexId) {
		throw new UnsupportedOperationException("GraphContainer.getGroupId is not yet implemented.");
	}

	@Override
	public Object getDisplayVertexId(Object vertexId, int semanticZoomLevel) {
		throw new UnsupportedOperationException("GraphContainer.getDisplayVertexId is not yet implemented.");
	}

	@Override
	public Collection<Object> getDisplayVertexIds(int semanticZoomLevel) {
		throw new UnsupportedOperationException("GraphContainer.getDisplayVertices is not yet implemented.");
	}

	@Override
	public Collection<?> getVertexIds() {
		throw new UnsupportedOperationException("GraphContainer.getVertexIds is not yet implemented.");
	}

	@Override
	public boolean hasChildren(Object itemId) {
		throw new UnsupportedOperationException("GraphContainer.hasChildren is not yet implemented.");
	}

	@Override
	public Collection<?> getChildren(Object itemId) {
		throw new UnsupportedOperationException("GraphContainer.getChildren is not yet implemented.");
	}

	@Override
	public Object getParentId(Object itemId) {
		throw new UnsupportedOperationException("GraphContainer.getParentId is not yet implemented.");
	}

	@Override
	public boolean containsVertexId(Object vertexId) {
		throw new UnsupportedOperationException("GraphContainer.containsVertexId is not yet implemented.");
	}

	@Override
	public boolean containsEdgeId(Object edgeId) {
		throw new UnsupportedOperationException("GraphContainer.containsEdgeId is not yet implemented.");
	}

	@Override
	public SelectionManager getSelectionManager() {
		throw new UnsupportedOperationException("GraphContainer.getSelectionManager is not yet implemented.");
	}

	@Override
	public Collection<?> getVertexForest(Collection<?> vertexIds) {
		throw new UnsupportedOperationException("GraphContainer.getVertexForest is not yet implemented.");
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
