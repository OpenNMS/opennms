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
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

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
		"classpath:/META-INF/opennms/applicationContext-setupIpLike-enabled.xml",
		"classpath*:/META-INF/opennms/provisiond-extensions.xml",
		"classpath*:/META-INF/opennms/component-dao.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase
public class SnmpAssetProvisioningAdapterTest implements InitializingBean {

	@Autowired
	private SnmpAssetProvisioningAdapter m_adapter;

	@Autowired
	private NodeDao m_nodeDao;

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
	@JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
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
	@JUnitTemporaryDatabase // Relies on records created in @Before so we need a fresh database
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
