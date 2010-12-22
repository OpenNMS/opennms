package org.opennms.netmgt.provision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.mock.snmp.JUnitSnmpAgent;
import org.opennms.mock.snmp.JUnitSnmpAgentExecutionListener;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.dao.support.ProxySnmpAgentConfigFactory;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

@RunWith(SpringJUnit4ClassRunner.class)
@TestExecutionListeners({
	OpenNMSConfigurationExecutionListener.class,
	TemporaryDatabaseExecutionListener.class,
	JUnitSnmpAgentExecutionListener.class,
	DependencyInjectionTestExecutionListener.class,
	DirtiesContextTestExecutionListener.class,
	TransactionalTestExecutionListener.class
})
@ContextConfiguration(locations= {
		"classpath:/META-INF/opennms/applicationContext-dao.xml",
		"classpath:/META-INF/opennms/applicationContext-daemon.xml",
		"classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
		"classpath:/META-INF/opennms/mockEventIpcManager.xml",
		"classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
		"classpath*:/META-INF/opennms/provisiond-extensions.xml",
		"classpath*:/META-INF/opennms/component-dao.xml"
})
@JUnitTemporaryDatabase()
/**
 * Test class for SNMP asset provisioning
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
public class SnmpAssetProvisioningAdapterIntegrationTest {

	@Autowired
	private NodeDao m_nodeDao;

	@Autowired
	private MockEventIpcManager m_mockEventIpcManager;

	@Autowired
	private DatabasePopulator m_populator;

	@Autowired
	private SnmpAssetProvisioningAdapter m_adapter;

	private static final String EXPECTED_COMMENT_FIELD = "OS Type: \"Linux\"\nOS Version: \"2.6.20-1.2316.fc5smp\"\nSystem Type: \"IBM ThinkCentre M52\"\nProcessor Type: \"Intel(R) Pentium(R) 4 CPU 3.00GHz Intel(R) Pentium(R) 4 CPU 3.00GHz\"\nProcessor Speed: \"2992.863 2992.863\"\nPhysical Memory: \"2109865984\"\nHard Drive Type: \"ATA WDC WD1600AAJS-2\"\nTape Drive Type: \"None\"\nOptical Drive Type: \"HL-DT-ST DVDRAM_GSA-H10N\"\nEquinox Type: \"Equinox Systems ESP\"\nModem 1 Type: \"ttyS0 - 2949C - Yes\"\nModem 2 Type: \"N/A\"\nModem Error Count: \"ttyS0 - 0 ttyQ01e0 - 0\"\nDatabase Dump Log: \"1\"\nMonetra Version: \"Monetra 7.3.0 BUILD 20254\"\nMonetra Store ID: \"0000001751\"\nOven Status: \"Oven not installed\"\nRAID Status: \"Cannot find raid file.\"\nMonetra Unsettled Batch Timestamp: \"None\"\nMonetra Key Timestamp: \"05/25/2010\"\nUnsettled Batch: 00:50:56:E7:A7:2F\nSecondary Password Algorithm Version: \"2.00\"\nProfit Database Version: \"V6.29R3\"\nJava Version: \"1.6.0_06\"\nProfit API Version: \"ProfitApi-2-0-57\"";
	private static final int NODE_ID = 1;

	@Before
	public void setUp() throws Exception {
		// Use the mock.logLevel system property to control the log level
		MockLogAppender.setupLogging(true);

		// Set the operation delay to 1 second so that queued operations execute immediately
		m_adapter.setDelay(1);
		m_adapter.setTimeUnit(TimeUnit.SECONDS);

		Assert.notNull(m_nodeDao, "Autowiring failed, NodeDao is null");
		Assert.notNull(m_mockEventIpcManager, "Autowiring failed, IPC manager is null");
		Assert.notNull(m_populator, "Autowiring failed, DB populator is null");
		Assert.notNull(m_adapter, "Autowiring failed, adapter is null");

		// Make sure that the localhost SNMP connection config factory has overridden
		// the normal config factory
		assertTrue(m_adapter.getSnmpPeerFactory() instanceof ProxySnmpAgentConfigFactory);

		m_populator.populateDatabase();

		OnmsNode node = m_nodeDao.get(NODE_ID);
		assertNotNull(node);
		node.setSysObjectId(".1.3");
		m_nodeDao.saveOrUpdate(node);
	}

	@Test
	@JUnitSnmpAgent(resource = "snmpAssetTestData.properties")
	public void testAddNode() throws InterruptedException {
		AdapterOperationChecker verifyOperations = new AdapterOperationChecker(1);
		m_adapter.getOperationQueue().addListener(verifyOperations);
		OnmsNode node = m_nodeDao.get(NODE_ID);
		assertNotNull(node);
		int firstNodeId = node.getId();

		m_adapter.addNode(firstNodeId);

		assertTrue(verifyOperations.enqueueLatch.await(4, TimeUnit.SECONDS));
		assertTrue(verifyOperations.dequeueLatch.await(4, TimeUnit.SECONDS));
		assertTrue(verifyOperations.executeLatch.await(4, TimeUnit.SECONDS));
		assertEquals(0, m_adapter.getOperationQueue().getOperationQueueForNode(firstNodeId).size());

		node = m_nodeDao.get(firstNodeId);
		assertNotNull(node);
		System.out.println("ID: " + node.getAssetRecord().getId());
		System.out.println("Comment: " + node.getAssetRecord().getComment());
		assertNotNull("AssetRecord comment is null", node.getAssetRecord().getComment());
		assertEquals(EXPECTED_COMMENT_FIELD, node.getAssetRecord().getComment());
	}

	@Test
	@Transactional
	@JUnitSnmpAgent(resource = "snmpAssetTestData.properties")
	public void testAddNodeDirectly() throws InterruptedException {
		OnmsNode node = m_nodeDao.get(NODE_ID);
		assertNotNull(node);
		int firstNodeId = node.getId();

		m_adapter.doAddNode(firstNodeId);

		node = m_nodeDao.get(firstNodeId);
		assertNotNull(node);
		assertNotNull("AssetRecord comment is null", node.getAssetRecord().getComment());
		assertEquals(EXPECTED_COMMENT_FIELD, node.getAssetRecord().getComment());
	}

	@Test
	@JUnitSnmpAgent(resource = "snmpAssetTestData.properties")
	public void testAddSameOperationTwice() throws InterruptedException {
		AdapterOperationChecker verifyOperations = new AdapterOperationChecker(2);
		m_adapter.getOperationQueue().addListener(verifyOperations);
		OnmsNode node = m_nodeDao.get(NODE_ID);
		assertNotNull(node);
		int firstNodeId = node.getId();

		m_adapter.addNode(firstNodeId);
		m_adapter.addNode(firstNodeId); // should get deduplicated
		m_adapter.updateNode(firstNodeId);

		assertTrue(verifyOperations.enqueueLatch.await(4, TimeUnit.SECONDS));
		assertTrue(verifyOperations.dequeueLatch.await(4, TimeUnit.SECONDS));
		assertTrue(verifyOperations.executeLatch.await(4, TimeUnit.SECONDS));
		assertEquals(0, m_adapter.getOperationQueue().getOperationQueueForNode(firstNodeId).size());

		node = m_nodeDao.get(firstNodeId);
		assertNotNull(node);
		System.out.println("ID: " + node.getAssetRecord().getId());
		System.out.println("Comment: " + node.getAssetRecord().getComment());
		assertNotNull("AssetRecord comment is null", node.getAssetRecord().getComment());
		assertEquals(EXPECTED_COMMENT_FIELD, node.getAssetRecord().getComment());
	}

	@Test
	@JUnitSnmpAgent(resource = "snmpAssetTestData.properties")
	public void testUpdateNode() throws InterruptedException {
		AdapterOperationChecker verifyOperations = new AdapterOperationChecker(2);
		m_adapter.getOperationQueue().addListener(verifyOperations);
		OnmsNode node = m_nodeDao.get(NODE_ID);
		assertNotNull(node);
		int firstNodeId = node.getId();

		assertNull(node.getAssetRecord().getComment());
		m_adapter.addNode(firstNodeId);
		m_adapter.updateNode(firstNodeId);

		assertTrue(verifyOperations.enqueueLatch.await(4, TimeUnit.SECONDS));
		assertTrue(verifyOperations.dequeueLatch.await(4, TimeUnit.SECONDS));
		assertTrue(verifyOperations.executeLatch.await(4, TimeUnit.SECONDS));
		assertEquals(0, m_adapter.getOperationQueue().getOperationQueueForNode(firstNodeId).size());

		node = m_nodeDao.get(firstNodeId);
		assertNotNull(node);
		System.out.println("ID: " + node.getAssetRecord().getId());
		System.out.println("Comment: " + node.getAssetRecord().getComment());
		assertNotNull("AssetRecord comment is null", node.getAssetRecord().getComment());
		assertEquals(EXPECTED_COMMENT_FIELD, node.getAssetRecord().getComment());
	}

	@Test
	public void testNodeConfigChanged() throws InterruptedException {
		AdapterOperationChecker verifyOperations = new AdapterOperationChecker(1);
		m_adapter.getOperationQueue().addListener(verifyOperations);
		OnmsNode node = m_nodeDao.get(NODE_ID);
		assertNotNull(node);
		int firstNodeId = node.getId();

		m_adapter.nodeConfigChanged(firstNodeId);
	}

	@Test
	public void testDeleteNode() throws InterruptedException {
		AdapterOperationChecker verifyOperations = new AdapterOperationChecker(1);
		m_adapter.getOperationQueue().addListener(verifyOperations);
		OnmsNode node = m_nodeDao.get(NODE_ID);
		assertNotNull(node);
		int firstNodeId = node.getId();

		m_adapter.deleteNode(firstNodeId);

		assertTrue(verifyOperations.enqueueLatch.await(4, TimeUnit.SECONDS));
		assertTrue(verifyOperations.dequeueLatch.await(4, TimeUnit.SECONDS));
		assertTrue(verifyOperations.executeLatch.await(4, TimeUnit.SECONDS));
		assertEquals(0, m_adapter.getOperationQueue().getOperationQueueForNode(firstNodeId).size());
	}
}
