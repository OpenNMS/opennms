/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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
import org.opennms.netmgt.config.dao.outages.api.ReadablePollOutagesDao;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.support.FilesystemResourceStorageDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.rrd.RrdStrategy;
import org.opennms.netmgt.rrd.rrdtool.MultithreadedJniRrdStrategy;
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
        "classpath:/META-INF/opennms/applicationContext-pinger.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/applicationContext-testPollerConfigDaos.xml"
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

    @Autowired
    private ReadablePollOutagesDao m_pollOutagesDao;

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

        m_rrdStrategy = new MultithreadedJniRrdStrategy();

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
            builder.addSnmpInterface(1).setIfName("Fa0/0").setPhysAddr("44:33:22:11:00").setIfType(6).setCollectionEnabled(true).setIfOperStatus(1).addIpInterface(m_testHostName).setIsSnmpPrimary("P");
            builder.addSnmpInterface(18).setIfName("Se1/0.102").setIfAlias("Conexion Valencia").setIfType(32).setCollectionEnabled(true).setIfOperStatus(1).addIpInterface("10.0.0.1").setIsSnmpPrimary("N");
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

        m_collectionSpecification = CollectorTestUtils.createCollectionSpec("SNMP", collector, "default",
                m_pollOutagesDao, collector.getClass().getCanonicalName());
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
    
    /**
     * Test collection for Cisco memory-pool entry with indirectly referenced property
     * pulled over from entPhysicalTable
     * 
     * @throwsException the exception
     */
    @Test
    @JUnitCollector(datacollectionType = "snmp", datacollectionConfig = "/org/opennms/netmgt/config/datacollection-config-cisco-mempool.xml")
    @JUnitSnmpAgent(resource = "/org/opennms/netmgt/snmp/cisco-mempool-snmpwalk.properties")
    public void testCollectionCiscoMemPoolVsEntPhysical() throws Exception {
        System.setProperty("org.opennms.netmgt.collectd.SnmpCollector.limitCollectionToInstances", "true");
        
        CollectionSet collectionSet = m_collectionSpecification.collect(m_collectionAgent);
        assertEquals("collection status", CollectionStatus.SUCCEEDED, collectionSet.getStatus());
        CollectorTestUtils.persistCollectionSet(m_rrdStrategy, m_resourceStorageDao, m_collectionSpecification, collectionSet);
        
        Map<String, String> map = m_resourceStorageDao.getStringAttributes(ResourcePath.get("snmp", "1", "cempMemoryPool", "1.1"));
        assertEquals("Processor", map.get("cempMemoryPoolName"));
        assertEquals("CISCO2911/K9", map.get("cempMemoryPoolPhysName"));
        assertEquals("CISCO2911/K9 chassis, Hw Serial#: FCZ161870SC, Hw Revision: 1.0", map.get("cempMemoryPoolPhysDescr"));
        
        map = m_resourceStorageDao.getStringAttributes(ResourcePath.get("snmp", "1", "cempMemoryPool", "1.2"));
        assertEquals("CISCO2911/K9", map.get("cempMemoryPoolPhysName"));
        assertEquals("CISCO2911/K9 chassis, Hw Serial#: FCZ161870SC, Hw Revision: 1.0", map.get("cempMemoryPoolPhysDescr"));
    }

    /**
     * Test enum-lookup property extender against values of dot1dStpPortState
     *
     * @throws Exception the exception
     */
    @Test
    @JUnitCollector(datacollectionType = "snmp", datacollectionConfig = "/org/opennms/netmgt/config/datacollection-config-dot1d-bridge-base-iftable.xml")
    @JUnitSnmpAgent(resource = "/org/opennms/netmgt/snmp/cisco-dot1dbridge-iftable-system-snmpwalk.properties")
    public void testEnumLookupPropertyExtenderVsDot1dStpPortState() throws Exception {
        System.setProperty("org.opennms.netmgt.collectd.SnmpCollector.limitCollectionToInstances", "true");
        
        CollectionSet collectionSet = m_collectionSpecification.collect(m_collectionAgent);
        assertEquals("collection status", CollectionStatus.SUCCEEDED, collectionSet.getStatus());
        CollectorTestUtils.persistCollectionSet(m_rrdStrategy, m_resourceStorageDao, m_collectionSpecification, collectionSet);
        
        Map<String, String> map = m_resourceStorageDao.getStringAttributes(ResourcePath.get("snmp", "1", "dot1dStpPortEntry", "46"));
        assertEquals("forwarding(5)", map.get("dot1dStpPortStateText"));
        assertEquals("testDefaultValue", map.get("dot1dStpPortEnableText"));
    }

    /**
     * Test basic enum-lookup property extender with properties defined against standard ifTable entries
     *
     * @throws Exception
     */
    @Test
    @JUnitCollector(
            datacollectionType = "snmp",
            datacollectionConfig = "/org/opennms/netmgt/config/datacollection-config-NMS15342.xml",
            anticipateFiles = {
                    "1",
                    "1/Fa0_0",
                    "1/Fa0_0/strings.properties"
            },
            anticipateRrds = {
                    "1/Fa0_0/ifOperStatus",
                    "1/Fa0_0/ifAdminStatus"
            }
    )
    @JUnitSnmpAgent(resource = "/org/opennms/netmgt/snmp/snmpTestData1.properties")
    public void testPropertyExtenderIndexedByIf() throws Exception {
        System.setProperty("org.opennms.netmgt.collectd.SnmpCollector.limitCollectionToInstances", "true");

        CollectionSet collectionSet = m_collectionSpecification.collect(m_collectionAgent);
        assertEquals("collection status", CollectionStatus.SUCCEEDED, collectionSet.getStatus());
        CollectorTestUtils.persistCollectionSet(m_rrdStrategy, m_resourceStorageDao, m_collectionSpecification, collectionSet);

        Map<String, String> map = m_resourceStorageDao.getStringAttributes(ResourcePath.get("snmp", "1", "Fa0_0"));
        assertEquals("UP", map.get("ifAdminStatusTXT"));
        assertEquals("UP", map.get("ifOperStatusTXT"));
    }

    /* (non-Javadoc)
     * @see org.opennms.core.test.TestContextAware#setTestContext(org.springframework.test.context.TestContext)
     */
    @Override
    public void setTestContext(TestContext context) {
        m_context = context;
    }
}
