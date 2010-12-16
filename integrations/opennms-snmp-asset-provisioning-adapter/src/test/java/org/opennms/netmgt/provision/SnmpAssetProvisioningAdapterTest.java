package org.opennms.netmgt.provision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.mock.snmp.JUnitSnmpAgent;
import org.opennms.netmgt.dao.NodeDao;
import org.opennms.netmgt.dao.db.JUnitTemporaryDatabase;
import org.opennms.netmgt.dao.db.OpenNMSConfigurationExecutionListener;
import org.opennms.netmgt.dao.db.TemporaryDatabaseExecutionListener;
import org.opennms.netmgt.dao.support.ProxySnmpAgentConfigFactory;
import org.opennms.netmgt.model.NetworkBuilder;
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
		"classpath:/META-INF/opennms/applicationContext-daemon.xml",
		"classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
		"classpath:/META-INF/opennms/mockEventIpcManager.xml",
		"classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
		"classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
		"classpath*:/META-INF/opennms/provisiond-extensions.xml",
		"classpath*:/META-INF/opennms/component-dao.xml"
})
@JUnitTemporaryDatabase()
public class SnmpAssetProvisioningAdapterTest {

	@Autowired
	private SnmpAssetProvisioningAdapter m_adapter;

	@Autowired
	private NodeDao m_nodeDao;

	@Before
	public void setUp() throws Exception {
		// Use the mock.logLevel system property to control the log level
		MockLogAppender.setupLogging(true);

		// Set the operation delay to 1 second so that queued operations execute immediately
		m_adapter.setDelay(1);
		m_adapter.setTimeUnit(TimeUnit.SECONDS);

		NetworkBuilder nb = new NetworkBuilder();
		nb.addNode("test.example.com").setForeignSource("rancid").setForeignId("1").setSysObjectId(".1.3");
		nb.addInterface("192.168.0.1");
		m_nodeDao.save(nb.getCurrentNode());
		m_nodeDao.flush();

		// Make sure that the localhost SNMP connection config factory has overridden
		// the normal config factory
		assertTrue(m_adapter.getSnmpPeerFactory() instanceof ProxySnmpAgentConfigFactory);
	}

	@Test
	@JUnitSnmpAgent(resource = "snmpAssetTestData.properties")
	public void testAdd() throws Exception {
		AdapterOperationChecker verifyOperations = new AdapterOperationChecker(1);
		m_adapter.getOperationQueue().addListener(verifyOperations);
		OnmsNode n = m_nodeDao.findByForeignId("rancid", "1");
		assertNotNull(n);
		m_adapter.addNode(n.getId());

		assertTrue(verifyOperations.enqueueLatch.await(4, TimeUnit.SECONDS));
		assertTrue(verifyOperations.dequeueLatch.await(4, TimeUnit.SECONDS));
		assertTrue(verifyOperations.executeLatch.await(4, TimeUnit.SECONDS));
		assertEquals(0, m_adapter.getOperationQueue().getOperationQueueForNode(n.getId()).size());

		// TODO: Add assertions to check that the addNode() adapter call updated the asset record
	}

	@Test
	@JUnitSnmpAgent(resource = "snmpAssetTestData.properties")
	public void testDelete() throws Exception {
		AdapterOperationChecker verifyOperations = new AdapterOperationChecker(1);
		m_adapter.getOperationQueue().addListener(verifyOperations);
		OnmsNode n = m_nodeDao.findByForeignId("rancid", "1");
		assertNotNull(n);
		m_adapter.deleteNode(n.getId());

		assertTrue(verifyOperations.enqueueLatch.await(4, TimeUnit.SECONDS));
		assertTrue(verifyOperations.dequeueLatch.await(4, TimeUnit.SECONDS));
		assertTrue(verifyOperations.executeLatch.await(4, TimeUnit.SECONDS));
		assertEquals(0, m_adapter.getOperationQueue().getOperationQueueForNode(n.getId()).size());

		// TODO: Add assertions to check that the deleteNode() adapter call updated the asset record
	}
}
