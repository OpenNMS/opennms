/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.provision;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.snmp.ProxySnmpAgentConfigFactory;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgents;
import org.opennms.netmgt.dao.DatabasePopulator;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.mock.MockEventIpcManager;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

/**
 * Test class for SNMP asset provisioning
 * 
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @author <a href="mailto:antonio@opennms.it">Antonio Russo</a>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations= {
		"classpath:/META-INF/opennms/applicationContext-soa.xml",
		"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
		"classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
		"classpath:/META-INF/opennms/applicationContext-mockDao.xml",
		"classpath:/META-INF/opennms/applicationContext-daemon.xml",
		"classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
		"classpath:/META-INF/opennms/mockEventIpcManager.xml",
		"classpath:/META-INF/opennms/applicationContext-databasePopulator.xml",
		"classpath*:/META-INF/opennms/provisiond-extensions.xml",
		"classpath*:/META-INF/opennms/component-dao.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
@JUnitSnmpAgents(value={
		@JUnitSnmpAgent(host="192.168.1.1", resource = "classpath:snmpAssetTestData.properties"),
		@JUnitSnmpAgent(host="172.20.1.201", resource = "classpath:snmpAssetTestData.properties"),
		@JUnitSnmpAgent(host="172.20.1.204", resource = "classpath:snmpAssetTestData.properties")
})
public class SnmpAssetProvisioningAdapterIntegrationTest implements InitializingBean {

	@Autowired
	private NodeDao m_nodeDao;

	@Autowired
	private MockEventIpcManager m_mockEventIpcManager;

	@Autowired
	private DatabasePopulator m_populator;

	@Autowired
	private SnmpAssetProvisioningAdapter m_adapter;

	private static final String EXPECTED_COMMENT_FIELD = "OS Type: Linux\nOS Version: 2.6.20-1.2316.fc5smp\nSystem Type: IBM ThinkCentre M52\nProcessor Type: Intel(R) Pentium(R) 4 CPU 3.00GHz Intel(R) Pentium(R) 4 CPU 3.00GHz\nProcessor Speed: 2992.863 2992.863\nPhysical Memory: 2109865984\nHard Drive Type: ATA WDC WD1600AAJS-2\nTape Drive Type: None\nOptical Drive Type: HL-DT-ST DVDRAM_GSA-H10N\nEquinox Type: Equinox Systems ESP\nModem 1 Type: ttyS0 - 2949C - Yes\nModem 2 Type: N/A\nModem Error Count: ttyS0 - 0 ttyQ01e0 - 0\nDatabase Dump Log: 1\nMonetra Version: Monetra 7.3.0 BUILD 20254\nMonetra Store ID: 0000001751\nOven Status: Oven not installed\nRAID Status: Cannot find raid file.\nMonetra Unsettled Batch Timestamp: None\nMonetra Key Timestamp: 05/25/2010\nUnsettled Batch: 00:50:56:E7:A7:2F\nSecondary Password Algorithm Version: 2.00\nProfit Database Version: V6.29R3\nJava Version: 1.6.0_06\nProfit API Version: ProfitApi-2-0-57";
	private static final int NODE_ID = 1;

	@Override
	public void afterPropertiesSet() throws Exception {
		BeanUtils.assertAutowiring(this);
	}

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
	@JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
	public void testAddNode() throws InterruptedException {
		AdapterOperationChecker verifyOperations = new AdapterOperationChecker(1);
		m_adapter.getOperationQueue().addListener(verifyOperations);
		
		try {
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
		} finally {
			m_adapter.getOperationQueue().removeListener(verifyOperations);
		}
	}

	@Test
	@JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
	@Transactional
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
	@JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
	public void testAddSameOperationTwice() throws InterruptedException {
		AdapterOperationChecker verifyOperations = new AdapterOperationChecker(2);
		m_adapter.getOperationQueue().addListener(verifyOperations);
		
		try {
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
		} finally {
			m_adapter.getOperationQueue().removeListener(verifyOperations);
		}
	}

	@Test
	@JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
	public void testUpdateNode() throws InterruptedException {
		AdapterOperationChecker verifyOperations = new AdapterOperationChecker(2);
		m_adapter.getOperationQueue().addListener(verifyOperations);
		
		try {
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
		} finally {
			m_adapter.getOperationQueue().removeListener(verifyOperations);
		}
	}

	@Test
	@JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
	public void testNodeConfigChanged() throws InterruptedException {
		AdapterOperationChecker verifyOperations = new AdapterOperationChecker(1);
		m_adapter.getOperationQueue().addListener(verifyOperations);
		
		try {
			OnmsNode node = m_nodeDao.get(NODE_ID);
			assertNotNull(node);
			int firstNodeId = node.getId();
	
			m_adapter.nodeConfigChanged(firstNodeId);
		} finally {
			m_adapter.getOperationQueue().removeListener(verifyOperations);
		}
	}

	@Test
	@JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
	public void testDeleteNode() throws InterruptedException {
		AdapterOperationChecker verifyOperations = new AdapterOperationChecker(1);
		m_adapter.getOperationQueue().addListener(verifyOperations);
		
		try {
			OnmsNode node = m_nodeDao.get(NODE_ID);
			assertNotNull(node);
			int firstNodeId = node.getId();
	
			m_adapter.deleteNode(firstNodeId);
	
			assertTrue(verifyOperations.enqueueLatch.await(4, TimeUnit.SECONDS));
			assertTrue(verifyOperations.dequeueLatch.await(4, TimeUnit.SECONDS));
			assertTrue(verifyOperations.executeLatch.await(4, TimeUnit.SECONDS));
			assertEquals(0, m_adapter.getOperationQueue().getOperationQueueForNode(firstNodeId).size());
		} finally {
			m_adapter.getOperationQueue().removeListener(verifyOperations);
		}
	}
}
