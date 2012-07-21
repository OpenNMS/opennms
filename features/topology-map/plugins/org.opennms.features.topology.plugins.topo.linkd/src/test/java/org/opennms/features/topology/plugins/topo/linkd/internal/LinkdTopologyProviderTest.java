package org.opennms.features.topology.plugins.topo.linkd.internal;



import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.opennms.features.topology.api.OperationContext;
import org.opennms.features.topology.plugins.topo.linkd.internal.LinkdTopologyProvider;
import org.opennms.features.topology.plugins.topo.linkd.internal.operations.OpenOperation;
import org.opennms.features.topology.plugins.topo.linkd.internal.operations.SaveOperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-mock.xml"
        })

public class LinkdTopologyProviderTest {
    @Autowired
    private OpenOperation m_openOperation;
    
    @Autowired
    private SaveOperation m_saveOperation;
    
    @Autowired
    private OperationContext m_operationContext;

    @Autowired
    private LinkdTopologyProvider m_topologyProvider;
    
    @Autowired
     private EasyMockDataPopulator m_databasePopulator;
     public class TestVertex {

    }

    
    @Before
    public void setUp() {
        m_databasePopulator.populateDatabase();
    }
    
    @After
    public void tearDown() {
        m_databasePopulator.tearDown();
    }
    
	@Test
	public void testLoad() {		
		m_topologyProvider.load(null);
		m_databasePopulator.check(m_topologyProvider);
	}
	
	@Test
	public void testSave() {
	    m_topologyProvider.save("target/test-map.xml");	    
            m_databasePopulator.check(m_topologyProvider);
            

	}
	
	@Test
	public void testOperationOpen() {
	    m_openOperation.execute(null, m_operationContext);
            m_databasePopulator.check(m_topologyProvider);

	}

	@Test
	public void testOperationSave() {
            List<Object> targets = new ArrayList<Object>(1);
            targets.add("target/test-graph.xml");
            m_saveOperation.execute(targets, m_operationContext);	            
	}

	@Test
	public void testOperationOpenExistingFile() {
	    List<Object> targets = new ArrayList<Object>(1);
            targets.add("target/test-map.xml");
            m_openOperation.execute(targets, m_operationContext);
	    
	}
	
}
