package org.opennms.netmgt.provision;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.test.annotations.JUnitHttpServer;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
    OpenNMSConfigurationExecutionListener.class,
    TemporaryDatabaseExecutionListener.class,
    DependencyInjectionTestExecutionListener.class,
    DirtiesContextTestExecutionListener.class,
    TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations= {
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
        "classpath*:/META-INF/opennms/provisiond-extensions.xml"
        })
@JUnitTemporaryDatabase()
/**
 * Test class for Rancid Provisioning
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
public class RancidProvisioningAdapterIntegrationTest {

    @Autowired
    private NodeDao m_nodeDao;
    
    @Autowired
    private MockEventIpcManager m_mockEventIpcManager;
    
    @Autowired
    private DatabasePopulator m_populator;

    @Autowired
    private RancidProvisioningAdapter m_adapter; 
    
    private static final int NODE_ID = 1;
    
    @Before
    public void setUp() throws Exception {
        Properties props = new Properties();
        props.setProperty("log4j.logger.org.hibernate", "INFO");
        props.setProperty("log4j.logger.org.springframework", "INFO");
        props.setProperty("log4j.logger.org.hibernate.SQL", "DEBUG");
        MockLogAppender.setupLogging(props);
        
        assertNotNull("Autowiring failed, node dao is null", m_nodeDao);
        assertNotNull("Autowiring failed, IPC manager is null", m_mockEventIpcManager);
        assertNotNull("Autowiring failed, DB populator is null", m_populator);
        assertNotNull("Autowiring failed, adapter is null", m_adapter);

        m_populator.populateDatabase();
    }
    
    /**
     * TODO: This test needs to be updated so that it properly connects to the JUnitHttpServer
     * for simulated RANCID REST operations.
     */
    @Test
    @JUnitHttpServer(port=7081,basicAuth=true)
    public void testAddNode() {
        List<OnmsNode> nodes = m_nodeDao.findAll();
        
        assertTrue(nodes.size() > 0);
        
        m_adapter.addNode(nodes.get(0).getId());
    }
    
    /**
     * TODO: This test needs to be updated so that it properly connects to the JUnitHttpServer
     * for simulated RANCID REST operations.
     */
    @Test
    @JUnitHttpServer(port=7081,basicAuth=true)
    @Ignore
    public void testAddSameOperationTwice() throws InterruptedException {
        // AdapterOperationChecker verifyOperations = new AdapterOperationChecker(2);
        // m_adapter.getOperationQueue().addListener(verifyOperations);
        OnmsNode node = m_nodeDao.get(NODE_ID);
        assertNotNull(node);
        int firstNodeId = node.getId();

        m_adapter.addNode(firstNodeId);
        m_adapter.addNode(firstNodeId); // should get deduplicated
        m_adapter.updateNode(firstNodeId);

        // assertTrue(verifyOperations.enqueueLatch.await(4, TimeUnit.SECONDS));
        // assertTrue(verifyOperations.dequeueLatch.await(4, TimeUnit.SECONDS));
        // assertTrue(verifyOperations.executeLatch.await(4, TimeUnit.SECONDS));
        assertEquals(0, m_adapter.getOperationQueue().getOperationQueueForNode(firstNodeId).size());
    }

    @Test
    public void testUpdateNode() {
        // TODO: Add some tests
    }

    @Test
    public void testDeleteNode() {
        // TODO: Add some tests
    }

    @Test
    public void testNodeConfigChanged() {
        // TODO: Add some tests
    }
}
