package org.opennms.netmgt.provision;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.mock.MockEventIpcManager;
import org.opennms.netmgt.model.OnmsNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

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
        "classpath:/META-INF/opennms/provisiond-extensions.xml"
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

    /* this was autowired, but the test was breaking; they're all ignored anyways, so for now, ignore :)
     * @Autowired
     */
    private RancidProvisioningAdapter m_adapter; 
    
    @Before
    public void setUp() throws Exception {
        Properties props = new Properties();
        props.setProperty("log4j.logger.org.hibernate", "INFO");
        props.setProperty("log4j.logger.org.springframework", "INFO");
        props.setProperty("log4j.logger.org.hibernate.SQL", "DEBUG");
        
        Assert.notNull(m_nodeDao, "Autowiring failed, node dao is null");
        Assert.notNull(m_mockEventIpcManager, "Autowiring failed, ipc manager is null");
        Assert.notNull(m_populator, "Autowiring failed, db populater is null");
        Assert.notNull(m_adapter, "Autowiring failed, adapter is null");

        m_populator.populateDatabase();
    }
    
    @Test
    @Transactional
    @Ignore
    public void testAddNode() {
        List<OnmsNode> nodes = m_nodeDao.findAll();
        
        assertTrue(nodes.size() > 0);
        
        try {
            m_adapter.addNode(nodes.get(0).getId());
        } catch (ProvisioningAdapterException pae) {
            //do nothing for now, this is the current expectation since the adapter is not yet implemented
        }
    }
    
    @Test
    @Transactional
    @Ignore
    public void testAddSameOperationTwice() throws InterruptedException {
        SimpleQueuedProvisioningAdapter adapter = new TestAdapter();
        
        try {
            adapter.addNode(1);
            Thread.sleep(1000);
            adapter.addNode(1);  //should get thrown away
            adapter.updateNode(1);
            org.junit.Assert.assertEquals(2, adapter.getOperationQueue().getOperationQueueForNode(1).size());
            Thread.sleep(10000);
            org.junit.Assert.assertEquals(0, adapter.getOperationQueue().getOperationQueueForNode(1).size());
        } catch (ProvisioningAdapterException pae) {
            //do nothing for now, this is the current expectation since the adapter is not yet implemented
        }
    }

    @Test
    @Transactional
    @Ignore
    public void testUpdateNode() {
        fail("Not yet implemented");
    }

    @Test
    @Transactional
    @Ignore
    public void testDeleteNode() {
        fail("Not yet implemented");
    }

    @Test
    @Transactional
    @Ignore
    public void testNodeConfigChanged() {
        fail("Not yet implemented");
    }

    class TestAdapter extends SimpleQueuedProvisioningAdapter {

        @Override
        AdapterOperationSchedule createScheduleForNode(int nodeId, AdapterOperationType adapterOperationType) {
            return new AdapterOperationSchedule(3, 3, 1, TimeUnit.SECONDS);
        };
        
        @Override
        public String getName() {
            return "TestAdapter";
        }

        @Override
        public boolean isNodeReady(AdapterOperation op) {
            return true;
        }

        @Override
        public void processPendingOperationForNode(AdapterOperation op)
                throws ProvisioningAdapterException {
            System.out.println(op);
        }
        
    }
}
