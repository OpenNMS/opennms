/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Map;
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
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.core.CollectionSpecification;
import org.opennms.netmgt.collection.test.api.CollectorTestUtils;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.support.FilesystemResourceStorageDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.jrobin.JRobinRrdStrategy;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.opennms.test.mock.MockUtil;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * The Test Class for SnmpCollector with MIB Object Properties.
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
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
@JUnitTemporaryDatabase(reuseDatabase=false) // Relies on records created in @Before so we need a fresh database for each test
public class SnmpCollectorWithMibPropertiesIT implements InitializingBean, TestContextAware {

    /** The Constant TEST_NODE_LABEL. */
    private final static String TEST_NODE_LABEL = "sample.local"; 

    /** The platform transaction manager. */
    @Autowired
    private PlatformTransactionManager m_transactionManager;

    /** The Node DAO. */
    @Autowired
    private NodeDao m_nodeDao;

    /** The IP interface DAO. */
    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    /** The SNMP peer factory. */
    @Autowired
    private SnmpPeerFactory m_snmpPeerFactory;

    /** The context. */
    private TestContext m_context;

    /** The test host name. */
    private String m_testHostName;

    /** The collection specification. */
    private CollectionSpecification m_collectionSpecification;

    /** The collection agent. */
    private SnmpCollectionAgent m_collectionAgent;

    /** The RRD strategy. */
    private RrdStrategy<?, ?> m_rrdStrategy;

    /** The resource storage DAO. */
    private FilesystemResourceStorageDao m_resourceStorageDao;

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.InitializingBean#afterPropertiesSet()
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    /**
     * Sets up the test.
     *
     * @throws Exception the exception
     */
    @Before
    public void setUp() throws Exception {
        MockServiceCollector.setDelegate(null);
        MockLogAppender.setupLogging();

        m_rrdStrategy = new JRobinRrdStrategy();

        m_resourceStorageDao = new FilesystemResourceStorageDao();
        File snmpRrdDirectory = (File)m_context.getAttribute("rrdDirectory");
        m_resourceStorageDao.setRrdDirectory(snmpRrdDirectory.getParentFile());

        m_testHostName = InetAddressUtils.str(InetAddress.getLocalHost());

        OnmsIpInterface iface = null;
        OnmsNode testNode = null;
        Collection<OnmsNode> testNodes = m_nodeDao.findByLabel(TEST_NODE_LABEL);
        if (testNodes == null || testNodes.size() < 1) {
            NetworkBuilder builder = new NetworkBuilder();
            builder.addNode(TEST_NODE_LABEL).setId(1).setSysObjectId(".1.3.6.1.4.1.9.1.9999"); // Fake Cisco SysOID
            builder.addSnmpInterface(1).setIfName("Fa0/0").setPhysAddr("44:33:22:11:00").setIfType(6).setCollectionEnabled(true).addIpInterface(m_testHostName).setIsSnmpPrimary("P");
            builder.addSnmpInterface(18).setIfName("Se1/0.102").setIfAlias("Conexion Valencia").setIfType(32).setCollectionEnabled(true).addIpInterface("10.0.0.1").setIsSnmpPrimary("N");
            testNode = builder.getCurrentNode();
            assertNotNull(testNode);
            m_nodeDao.save(testNode);
            m_nodeDao.flush();
        } else {
            testNode = testNodes.iterator().next();
        }

        Set<OnmsIpInterface> ifaces = testNode.getIpInterfaces();
        assertEquals(2, ifaces.size());
        iface = ifaces.iterator().next();

        SnmpPeerFactory.setInstance(m_snmpPeerFactory);

        SnmpCollector collector = new SnmpCollector();
        collector.initialize();

        m_collectionSpecification = CollectorTestUtils.createCollectionSpec("SNMP", collector, "default");
        m_collectionAgent = DefaultSnmpCollectionAgent.create(iface.getId(), m_ipInterfaceDao, m_transactionManager);
    }

    /**
     * Tears down the test.
     *
     * @throws Exception the exception
     */
    @After
    public void tearDown() throws Exception {
        MockUtil.println("------------ End Test --------------------------");
        MockLogAppender.assertNoWarningsOrGreater();
    }

    /**
     * Test collection with MibObj Properties.
     *
     * @throws Exception the exception
     */
    @Test
    @JUnitCollector(datacollectionType = "snmp", datacollectionConfig = "/org/opennms/netmgt/config/datacollection-config-NMS8484.xml")
    @JUnitSnmpAgent(resource = "/org/opennms/netmgt/snmp/airespace.properties")
    public void testCollect() throws Exception {
        System.setProperty("org.opennms.netmgt.collectd.SnmpCollector.limitCollectionToInstances", "true");

        CollectionSet collectionSet = m_collectionSpecification.collect(m_collectionAgent);
        assertEquals("collection status", CollectionStatus.SUCCEEDED, collectionSet.getStatus());
        CollectorTestUtils.persistCollectionSet(m_rrdStrategy, m_resourceStorageDao, m_collectionSpecification, collectionSet);

        Map<String, String> slot0 = m_resourceStorageDao.getStringAttributes(ResourcePath.get("snmp", "1", "bsnAPIfLoadParametersEntry", "132.178.97.20.31.224.0"));
        assertEquals("AP84b2.6111.29ac", slot0.get("bsnAPName"));
        assertEquals("0", slot0.get("slotNumber"));
        Map<String, String> slot1 = m_resourceStorageDao.getStringAttributes(ResourcePath.get("snmp", "1", "bsnAPIfLoadParametersEntry", "132.178.97.20.31.224.1"));
        assertEquals("AP84b2.6111.29ac", slot1.get("bsnAPName"));
        assertEquals("1", slot1.get("slotNumber"));
    }

    /**
     * Test collection for Cisco QoS with MibObj Properties.
     *
     * @throws Exception the exception
     */
    @Test
    @JUnitCollector(datacollectionType = "snmp", datacollectionConfig = "/org/opennms/netmgt/config/datacollection-config-cisco-qos.xml")
    @JUnitSnmpAgent(resource = "/org/opennms/netmgt/snmp/cisco-qos.properties")
    public void testCollectCiscoQoS() throws Exception {
        System.setProperty("org.opennms.netmgt.collectd.SnmpCollector.limitCollectionToInstances", "true");

        CollectionSet collectionSet = m_collectionSpecification.collect(m_collectionAgent);
        assertEquals("collection status", CollectionStatus.SUCCEEDED, collectionSet.getStatus());
        CollectorTestUtils.persistCollectionSet(m_rrdStrategy, m_resourceStorageDao, m_collectionSpecification, collectionSet);

        Map<String, String> map = m_resourceStorageDao.getStringAttributes(ResourcePath.get("snmp", "1", "cbQosCMStatsEntry", "290.508801"));
        assertEquals("OUTBOUND-LLQ", map.get("cbQosClassMapPolicy"));
        assertEquals("GESTION-ROUTING", map.get("cbQosClassMapName"));
        assertEquals("Conexion Valencia", map.get("ifAlias"));
    }

    /* (non-Javadoc)
     * @see org.opennms.core.test.TestContextAware#setTestContext(org.springframework.test.context.TestContext)
     */
    @Override
    public void setTestContext(TestContext context) {
        m_context = context;
    }
}
