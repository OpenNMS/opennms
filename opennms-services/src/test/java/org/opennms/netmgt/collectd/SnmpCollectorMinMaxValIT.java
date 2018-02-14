/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;
import java.util.Properties;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.collection.test.JUnitCollector;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.TestContextAware;
import org.opennms.core.test.TestContextAwareExecutionListener;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.snmp.ProxySnmpAgentConfigFactory;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.core.CollectionSpecification;
import org.opennms.netmgt.collection.test.api.CollectorTestUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.support.FilesystemResourceStorageDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.NetworkBuilder.InterfaceBuilder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy;
import org.opennms.netmgt.snmp.SnmpAgentConfig;
import org.opennms.netmgt.snmp.SnmpObjId;
import org.opennms.netmgt.snmp.SnmpUtils;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.mock.MockUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

@RunWith(OpenNMSJUnit4ClassRunner.class)
@TestExecutionListeners({
    TestContextAwareExecutionListener.class
})
@ContextConfiguration(locations={
		"classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-pinger.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml"
})
@JUnitConfigurationEnvironment(systemProperties="org.opennms.rrd.storeByGroup=false")
@JUnitTemporaryDatabase
@Transactional
public class SnmpCollectorMinMaxValIT implements TestContextAware, InitializingBean {
    private static final Logger LOG = LoggerFactory.getLogger(SnmpCollectorMinMaxValIT.class);
    private static final String TEST_HOST_ADDRESS = "192.0.2.205";
    private static final String TEST_NODE_LABEL = "TestNode"; 

    @Autowired
    private PlatformTransactionManager m_transactionManager;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    @Autowired
    private SnmpPeerFactory m_snmpPeerFactory;

    private TestContext m_context;

    private CollectionSpecification m_collectionSpecification;

    private SnmpCollectionAgent m_collectionAgent;

	private SnmpAgentConfig m_agentConfig;

	private RrdStrategy<?, ?> m_rrdStrategy;

    private FilesystemResourceStorageDao m_resourceStorageDao;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Before
    public void setUp() throws Exception {
        final Properties p = new Properties();
    	p.setProperty("log4j.logger.org.opennms.netmgt.snmp.SnmpUtils", "DEBUG");
        MockLogAppender.setupLogging(p);

        assertTrue(m_snmpPeerFactory instanceof ProxySnmpAgentConfigFactory);

        SnmpPeerFactory.setInstance(m_snmpPeerFactory);
        m_agentConfig = m_snmpPeerFactory.getAgentConfig(InetAddressUtils.addr(TEST_HOST_ADDRESS));

        m_rrdStrategy = new JRobinRrdStrategy();

        m_resourceStorageDao = new FilesystemResourceStorageDao();
        File snmpRrdDirectory = (File)m_context.getAttribute("rrdDirectory");
        m_resourceStorageDao.setRrdDirectory(snmpRrdDirectory.getParentFile());

        OnmsIpInterface iface = null;
        OnmsNode testNode = null;
        Collection<OnmsNode> testNodes = m_nodeDao.findByLabel(TEST_NODE_LABEL);
        if (testNodes == null || testNodes.size() < 1) {
            NetworkBuilder builder = new NetworkBuilder();
            builder.addNode(TEST_NODE_LABEL).setId(1).setSysObjectId(".1.3.6.1.4.1.1588.2.1.1.1");

            builder.addSnmpInterface(1).setIfName("lo0").setPhysAddr("00:11:22:33:44");
            builder.addSnmpInterface(2).setIfName("gif0").setPhysAddr("00:11:22:33:45").setIfType(55);
            builder.addSnmpInterface(3).setIfName("stf0").setPhysAddr("00:11:22:33:46").setIfType(57);

            InterfaceBuilder ifBldr = builder.addInterface(TEST_HOST_ADDRESS).setIsSnmpPrimary("P");
            ifBldr.addSnmpInterface(6).setIfName("fw0").setPhysAddr("44:33:22:11:00").setIfType(144).setCollectionEnabled(true);

            testNode = builder.getCurrentNode();
            assertNotNull(testNode);
            m_nodeDao.save(testNode);
            m_nodeDao.flush();
        } else {
            testNode = testNodes.iterator().next();
        }

        final Set<OnmsIpInterface> ifaces = testNode.getIpInterfaces();
        assertEquals(1, ifaces.size());
        iface = ifaces.iterator().next();
        
        LOG.debug("iface = {}", iface);

        final SnmpCollector collector = new SnmpCollector();
        collector.initialize();

        m_collectionSpecification = CollectorTestUtils.createCollectionSpec("SNMP", collector, "default");
        m_collectionAgent = DefaultSnmpCollectionAgent.create(iface.getId(), m_ipInterfaceDao, m_transactionManager);
    }

    @After
    public void tearDown() throws Exception {
        MockUtil.println("------------ End Test --------------------------");
        // MockLogAppender.assertNoWarningsOrGreater();
    }


    @Test
    @JUnitCollector(
            datacollectionConfig = "/org/opennms/netmgt/config/datacollection-minmax-persistTest-config.xml", 
            datacollectionType = "snmp",
            anticipateFiles = {
                    "1",
                    "1/fw0"
            },
            anticipateRrds = {
                    "1/tcpCurrEstab",
                    "1/fw0/ifInOctets"
            }
    )
    @JUnitSnmpAgent(host=TEST_HOST_ADDRESS, resource="/org/opennms/netmgt/snmp/snmpTestData1.properties")
    public void testPersist() throws Exception {
    	final File snmpRrdDirectory = (File)m_context.getAttribute("rrdDirectory");

        // node level collection
    	final File nodeDir = new File(snmpRrdDirectory, "1");
    	final File rrdFile = new File(nodeDir, rrd("tcpCurrEstab"));

        // interface level collection
    	final File ifDir = new File(nodeDir, "fw0");
    	final File ifRrdFile = new File(ifDir, rrd("ifInOctets"));

    	final int numUpdates = 2;
    	final int stepSizeInSecs = 1;

    	final int stepSizeInMillis = stepSizeInSecs*1000;
        final int rangeSizeInMillis = stepSizeInMillis + 20000;

        CollectorTestUtils.collectNTimes(m_rrdStrategy, m_resourceStorageDao, m_collectionSpecification, m_collectionAgent, numUpdates);

        // This is the value from snmpTestData1.properties
        //.1.3.6.1.2.1.6.9.0 = Gauge32: 123
        assertEquals(Double.valueOf(123.0), m_rrdStrategy.fetchLastValueInRange(rrdFile.getAbsolutePath(), "tcpCurrEstab", stepSizeInMillis, rangeSizeInMillis));

        // This is the value from snmpTestData1.properties
        // .1.3.6.1.2.1.2.2.1.10.6 = Counter32: 1234567
        assertEquals(Double.valueOf(1234567.0), m_rrdStrategy.fetchLastValueInRange(ifRrdFile.getAbsolutePath(), "ifInOctets", stepSizeInMillis, rangeSizeInMillis));

        // now update the data in the agent
		SnmpUtils.set(m_agentConfig, SnmpObjId.get(".1.3.6.1.2.1.6.9.0"), SnmpUtils.getValueFactory().getInt32(456));
		SnmpUtils.set(m_agentConfig, SnmpObjId.get(".1.3.6.1.2.1.2.2.1.10.6"), SnmpUtils.getValueFactory().getCounter32(7654321));

        CollectorTestUtils.collectNTimes(m_rrdStrategy, m_resourceStorageDao, m_collectionSpecification, m_collectionAgent, numUpdates);

        // by now the values should be the new values
        assertEquals(Double.valueOf(456.0), m_rrdStrategy.fetchLastValueInRange(rrdFile.getAbsolutePath(), "tcpCurrEstab", stepSizeInMillis, rangeSizeInMillis));
        assertEquals(Double.valueOf(1234567.0), m_rrdStrategy.fetchLastValueInRange(ifRrdFile.getAbsolutePath(), "ifInOctets", stepSizeInMillis, rangeSizeInMillis));

     // now update the data in the agent
		SnmpUtils.set(m_agentConfig, SnmpObjId.get(".1.3.6.1.2.1.6.9.0"), SnmpUtils.getValueFactory().getInt32(456));
		SnmpUtils.set(m_agentConfig, SnmpObjId.get(".1.3.6.1.2.1.2.2.1.10.6"), SnmpUtils.getValueFactory().getCounter32(1234567));

        CollectorTestUtils.collectNTimes(m_rrdStrategy, m_resourceStorageDao, m_collectionSpecification, m_collectionAgent, numUpdates);

        // by now the values should be the new values
        assertEquals(Double.valueOf(456.0), m_rrdStrategy.fetchLastValueInRange(rrdFile.getAbsolutePath(), "tcpCurrEstab", stepSizeInMillis, rangeSizeInMillis));
        assertEquals(Double.valueOf(1234567.0), m_rrdStrategy.fetchLastValueInRange(ifRrdFile.getAbsolutePath(), "ifInOctets", stepSizeInMillis, rangeSizeInMillis));
    }

    private String rrd(final String file) {
        return file + m_rrdStrategy.getDefaultFileExtension();
    }

    @Override
    public void setTestContext(final TestContext context) {
        m_context = context;
    }
}
