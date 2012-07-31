package org.opennms.features.topology.plugins.topo.onmsdao.internal;


import java.util.Collection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.features.topology.api.GraphContainer;
import org.opennms.features.topology.api.LayoutAlgorithm;
import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.api.VertexContainer;
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

        @Override
        public Object getVertexItemIdForVertexKey(Object key) {
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
