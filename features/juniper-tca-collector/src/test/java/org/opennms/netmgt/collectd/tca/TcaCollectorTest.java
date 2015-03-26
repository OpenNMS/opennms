/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd.tca;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.easymock.EasyMock;
import org.jrobin.core.Robin;
import org.jrobin.core.RrdDb;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.netmgt.collectd.DefaultCollectionAgent;
import org.opennms.netmgt.collectd.SnmpCollectionAgent;
import org.opennms.netmgt.collectd.tca.config.TcaDataCollection;
import org.opennms.netmgt.collectd.tca.config.TcaDataCollectionConfig;
import org.opennms.netmgt.collectd.tca.config.TcaRrd;
import org.opennms.netmgt.collectd.tca.dao.TcaDataCollectionConfigDao;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.ServiceParameters;
import org.opennms.netmgt.collection.persistence.rrd.OneToOnePersister;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.NetworkBuilder.InterfaceBuilder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.netmgt.snmp.SnmpValue;
import org.opennms.netmgt.snmp.SnmpValueFactory;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

/**
 * The Class TcaCollectorTest.
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@ContextConfiguration(locations={
		"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/junit-component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment
@JUnitTemporaryDatabase(reuseDatabase=false)
@JUnitSnmpAgent(host = TcaCollectorTest.TEST_NODE_IP, port = 9161, resource = "classpath:juniperTcaSample.properties")
@Transactional
public class TcaCollectorTest implements InitializingBean {

	/** The Constant TEST_NODE_IP. */
	public final static String TEST_NODE_IP = "127.0.0.1"; 

	/** The Constant TEST_NODE_LABEL. */
	public final static String TEST_NODE_LABEL = "TestNode"; 
	
	/** The Constant TEST_SNMP_DIR. */
	public final static String TEST_SNMP_DIR = "target/snmp";

	/** The collection agent. */
	private SnmpCollectionAgent m_collectionAgent;

	/** The Node DAO. */
	@Autowired
	private NodeDao m_nodeDao;

	/** The IP Interface DAO. */
	@Autowired
	private IpInterfaceDao m_ipInterfaceDao;

	/** The SNMP peer factory. */
	@Autowired
	private SnmpPeerFactory m_snmpPeerFactory;

	/** The transaction manager. */
	@Autowired
	private PlatformTransactionManager m_transactionManager;
	
	@Autowired
	private TcaDataCollectionConfigDao m_configDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

	/**
	 * Sets the up.
	 *
	 * @throws Exception the exception
	 */
	@Before
	public void setUp() throws Exception {
		MockLogAppender.setupLogging();

		FileUtils.deleteDirectory(new File(TEST_SNMP_DIR));

		// These tests rely on JRobin to verify the values
		RrdUtils.setStrategy(new JRobinRrdStrategy());

		OnmsIpInterface iface = null;
		OnmsNode testNode = null;
		Collection<OnmsNode> testNodes = m_nodeDao.findByLabel(TEST_NODE_LABEL);
		if (testNodes == null || testNodes.size() < 1) {
			NetworkBuilder builder = new NetworkBuilder();
			builder.addNode(TEST_NODE_LABEL).setId(1).setSysObjectId(".1.3.6.1.4.1.1588.2.1.1.1");
			InterfaceBuilder ifBldr = builder.addInterface(TEST_NODE_IP).setIsSnmpPrimary("P");
			ifBldr.addSnmpInterface(6).setIfName("fw0").setPhysAddr("44:33:22:11:00").setIfType(144).setCollectionEnabled(true);
			testNode = builder.getCurrentNode();
			Assert.assertNotNull(testNode);
			m_nodeDao.save(testNode);
			m_nodeDao.flush();
		} else {
			testNode = testNodes.iterator().next();
		}

		Set<OnmsIpInterface> ifaces = testNode.getIpInterfaces();
		Assert.assertEquals(1, ifaces.size());
		iface = ifaces.iterator().next();

		SnmpPeerFactory.setInstance(m_snmpPeerFactory);

		m_collectionAgent = DefaultCollectionAgent.create(iface.getId(), m_ipInterfaceDao, m_transactionManager);
		
		TcaRrd rrd = new TcaRrd();
		rrd.addRra("RRA:AVERAGE:0.5:1:3600");
		rrd.addRra("RRA:AVERAGE:0.5:300:288");
		rrd.addRra("RRA:MIN:0.5:300:288");
		rrd.addRra("RRA:MAX:0.5:300:288");
		rrd.addRra("RRA:AVERAGE:0.5:900:2880");
		rrd.addRra("RRA:MIN:0.5:900:2880");
		rrd.addRra("RRA:MAX:0.5:900:2880");
		rrd.addRra("RRA:AVERAGE:0.5:3600:4300");
		rrd.addRra("RRA:MIN:0.5:3600:4300");
		rrd.addRra("RRA:MAX:0.5:3600:4300");
		TcaDataCollection tcadc = new TcaDataCollection();
		tcadc.setName("default");
		tcadc.setRrd(rrd);
		TcaDataCollectionConfig tcadcc = new TcaDataCollectionConfig();
		tcadcc.addDataCollection(tcadc);
		tcadcc.setRrdRepository(TEST_SNMP_DIR);
		EasyMock.expect(m_configDao.getConfig()).andReturn(tcadcc).atLeastOnce();
		EasyMock.replay(m_configDao);
	}

	/**
	 * Tear down.
	 *
	 * @throws Exception the exception
	 */
	@After
	public void tearDown() throws Exception {
		EasyMock.verify(m_configDao);
		MockLogAppender.assertNoWarningsOrGreater();
	}

	/**
	 * Test collector.
	 *
	 * @throws Exception the exception
	 */
	@Test
	public void testCollector() throws Exception {
		Map<String,Object> parameters = new HashMap<String,Object>();
		parameters.put("collection", "default");

		// Create Collection Set
		TcaCollector collector = new TcaCollector();
		collector.setConfigDao(m_configDao);
		collector.initialize(new HashMap<String,String>());
		collector.initialize(m_collectionAgent, parameters);
		OneToOnePersister persister = new OneToOnePersister(new ServiceParameters(parameters), collector.getRrdRepository("default"));

		// Setup SNMP Value Handling
		SnmpValueFactory valFac = SnmpUtils.getValueFactory();
		SnmpObjId peer1 = SnmpObjId.get(".1.3.6.1.4.1.27091.3.1.6.1.2.171.19.37.60");
		SnmpObjId peer2 = SnmpObjId.get(".1.3.6.1.4.1.27091.3.1.6.1.2.171.19.38.70");

		// Collect and Persist Data - Step 1
		CollectionSet collectionSet = collector.collect(m_collectionAgent, null, parameters);
		validateCollectionSet(collectionSet);
		collectionSet.visit(persister);

		// Generate new SNMP Data
		StringBuffer sb = new StringBuffer("|25|");
		long ts = 1327451787l;
		for (int i = 0; i < 25; i++) {
			sb.append(ts++);
			sb.append(",12,-1,12,-2,1|");
		}

		// Get Current Values
		SnmpValue v1a = SnmpUtils.get(m_collectionAgent.getAgentConfig(), peer1);
		SnmpValue v2a = SnmpUtils.get(m_collectionAgent.getAgentConfig(), peer2);

		// Set New Values
		SnmpUtils.set(m_collectionAgent.getAgentConfig(), peer1, valFac.getOctetString(sb.toString().getBytes()));
		SnmpUtils.set(m_collectionAgent.getAgentConfig(), peer2, valFac.getOctetString(sb.toString().getBytes()));

		// Validate New Values
		SnmpValue v1b = SnmpUtils.get(m_collectionAgent.getAgentConfig(), peer1);
		SnmpValue v2b = SnmpUtils.get(m_collectionAgent.getAgentConfig(), peer2);
		Assert.assertFalse(v1a.toDisplayString().equals(v1b.toDisplayString()));
		Assert.assertFalse(v2a.toDisplayString().equals(v2b.toDisplayString()));

		// Collect and Persist Data - Step 2
		collectionSet = collector.collect(m_collectionAgent, null, parameters);
		validateCollectionSet(collectionSet);
		collectionSet.visit(persister);

		// Validate Persisted Data
		RrdDb jrb = new RrdDb(TEST_SNMP_DIR + "/1/" + TcaCollectionResource.RESOURCE_TYPE_NAME + "/171.19.37.60/" + TcaCollectionSet.INBOUND_DELAY + RrdUtils.getExtension());

		// According with the Fixed Step
		Assert.assertEquals(1, jrb.getArchive(0).getArcStep());

		// According with the Sample Data
		Assert.assertEquals(ts - 1, jrb.getArchive(0).getEndTime());
		Robin inboundDelay = jrb.getArchive(0).getRobin(0);
		for (int i = inboundDelay.getSize() - 49; i < inboundDelay.getSize() - 25; i++) {
			Assert.assertEquals(new Double(11), Double.valueOf(inboundDelay.getValue(i)));
		}
		for (int i = inboundDelay.getSize() - 24; i < inboundDelay.getSize(); i++) {
			Assert.assertEquals(new Double(12), Double.valueOf(inboundDelay.getValue(i)));
		}
	}

	/**
	 * Validate collection set.
	 * <p>Each collection set must contain:<br>
	 * 25 Samples of each of 2 peers = 50 resources</p>
	 * 
	 * @param collectionSet the collection set
	 */
	private void validateCollectionSet(CollectionSet collectionSet) {
		Assert.assertTrue(collectionSet instanceof TcaCollectionSet);
		TcaCollectionSet tcaCollection = (TcaCollectionSet) collectionSet;
		Assert.assertFalse(tcaCollection.getCollectionResources().isEmpty());
		Assert.assertEquals(50, tcaCollection.getCollectionResources().size());
	}

}
