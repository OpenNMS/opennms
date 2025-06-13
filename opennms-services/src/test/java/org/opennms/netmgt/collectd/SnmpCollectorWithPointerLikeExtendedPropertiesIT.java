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
        "classpath:/META-INF/opennms/applicationContext-pinger.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml",
        "classpath:/META-INF/opennms/applicationContext-testPollerConfigDaos.xml"
})
@JUnitConfigurationEnvironment(systemProperties="org.opennms.rrd.storeByGroup=false")
@JUnitTemporaryDatabase(reuseDatabase=false) // Relies on records created in @Before so we need a fresh database for each test
public class SnmpCollectorWithPointerLikeExtendedPropertiesIT implements InitializingBean, TestContextAware {

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
            builder.addNode(TEST_NODE_LABEL).setId(1).setSysObjectId(".1.3.6.1.4.1.9.1.1208");
            builder.addSnmpInterface(1).setIfName("Vl1").setPhysAddr("de:ad:be:ef:ca:01").setIfType(6).setCollectionEnabled(true).addIpInterface(m_testHostName).setIsSnmpPrimary("P");
            builder.addSnmpInterface(10146).setIfName("Gi1/0/46").setPhysAddr("de:ad:be:ef:ca:32").setIfType(6).setCollectionEnabled(true).addIpInterface("10.0.46.1").setIsSnmpPrimary("N");
            builder.addSnmpInterface(10152).setIfName("Gi1/0/52").setPhysAddr("de:ad:be:ef:ca:38").setIfType(6).setCollectionEnabled(true).addIpInterface("10.0.52.1").setIsSnmpPrimary("N");
            testNode = builder.getCurrentNode();
            assertNotNull(testNode);
            m_nodeDao.save(testNode);
            m_nodeDao.flush();
        } else {
            testNode = testNodes.iterator().next();
        }

        Set<OnmsIpInterface> ifaces = testNode.getIpInterfaces();
        assertEquals(3, ifaces.size());
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
     * Test collection for dot1d-bridge base port entry with indirectly referenced property
     * pulled over from ifTable, using the value of dot1dBasePortIfIndex as a pointer.
     * 
     * MIB dump provided by Jean-Marie Kubek
     * 
     * @throws Exception the exception
     */
    @Test
    @JUnitCollector(datacollectionType = "snmp", datacollectionConfig = "/org/opennms/netmgt/config/datacollection-config-dot1d-bridge-base-iftable.xml")
    @JUnitSnmpAgent(resource = "/org/opennms/netmgt/snmp/cisco-dot1dbridge-iftable-system-snmpwalk.properties")
    public void testCollectionDot1dBasePortIfIndexVsIfTable() throws Exception {
        System.setProperty("org.opennms.netmgt.collectd.SnmpCollector.limitCollectionToInstances", "true");
        
        CollectionSet collectionSet = m_collectionSpecification.collect(m_collectionAgent);
        assertEquals("collection status", CollectionStatus.SUCCEEDED, collectionSet.getStatus());
        CollectorTestUtils.persistCollectionSet(m_rrdStrategy, m_resourceStorageDao, m_collectionSpecification, collectionSet);

        Map<String, String> map = m_resourceStorageDao.getStringAttributes(ResourcePath.get("snmp", "1", "dot1dBasePortEntry", "46"));
        assertEquals("GigabitEthernet1/0/46", map.get("dot1dBasePortIfDescr"));
        
        map = m_resourceStorageDao.getStringAttributes(ResourcePath.get("snmp", "1", "dot1dBasePortEntry", "52"));
        assertEquals("GigabitEthernet1/0/52", map.get("dot1dBasePortIfDescr"));
        assertEquals("Gi1/0/52", map.get("dot1dBasePortIfName"));
        assertEquals("This is an ifAlias", map.get("dot1dBasePortIfAlias"));
    }
    
    /**
     * Test collection for cpmCPUTotalTable with indirectly-referenced string properties
     * pulled over from the entPhysicalTable, using the value of cpmCPUTotalPhysicalIndex
     * as a pointer
     * 
     * MIB dump provided by David Schlenk
     */
    @Test
    @JUnitCollector(datacollectionType = "snmp", datacollectionConfig = "/org/opennms/netmgt/config/datacollection-config-cisco-xe-cpmcputotal-entity.xml")
    @JUnitSnmpAgent(resource = "/org/opennms/netmgt/snmp/cisco-xe-snmpwalk.properties")
    public void testCollectionCpmCPUTotalEntryVsEntPhysicalEntry() throws Exception {
        System.setProperty("org.opennms.netmgt.collectd.SnmpCollector.limitCollectionToInstances", "true");
        
        CollectionSet collectionSet = m_collectionSpecification.collect(m_collectionAgent);
        assertEquals("collection status", CollectionStatus.SUCCEEDED, collectionSet.getStatus());
        CollectorTestUtils.persistCollectionSet(m_rrdStrategy, m_resourceStorageDao, m_collectionSpecification, collectionSet);

        Map<String, String> map = m_resourceStorageDao.getStringAttributes(ResourcePath.get("snmp", "1", "cpmCPUTotalEntry", "7"));
        assertEquals("cpu R0/0", map.get("cpmCPUTotalName"));
        assertEquals("CPU 0 of module R0", map.get("cpmCPUTotalDescr"));
    }

    /* (non-Javadoc)
     * @see org.opennms.core.test.TestContextAware#setTestContext(org.springframework.test.context.TestContext)
     */
    @Override
    public void setTestContext(TestContext context) {
        m_context = context;
    }
}
