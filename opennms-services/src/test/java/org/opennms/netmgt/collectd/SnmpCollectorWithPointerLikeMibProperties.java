/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

import static org.junit.Assert.*;

import java.io.File;
import java.net.InetAddress;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.TestContextAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.test.snmp.annotations.JUnitSnmpAgent;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.config.SnmpPeerFactory;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.support.FilesystemResourceStorageDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.RrdGraphAttribute;
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
@ContextConfiguration(locations = {
        "classpath:/META-INF/opennms/applicationContext-commonConfigs.xml",
        "classpath:/META-INF/opennms/applicationContext-minimal-conf.xml",
        "classpath:/META-INF/opennms/applicationContext-soa.xml",
        "classpath:/META-INF/opennms/applicationContext-mockDao.xml",
        "classpath*:/META-INF/opennms/component-dao.xml",
        "classpath:/META-INF/opennms/applicationContext-daemon.xml",
        "classpath:/META-INF/opennms/mockEventIpcManager.xml",
        "classpath:/META-INF/opennms/applicationContext-proxy-snmp.xml"
})
@JUnitConfigurationEnvironment(systemProperties = {
        "org.opennms.rrd.storeByGroup=false",
})
@JUnitTemporaryDatabase(reuseDatabase = false)
// Relies on records created in @Before so we need a fresh database for each test
public class SnmpCollectorWithPointerLikeMibProperties implements InitializingBean, TestContextAware {

    /**
     * The Constant TEST_NODE_LABEL.
     */
    private final static String TEST_NODE_LABEL = "sample.local";

    /**
     * The platform transaction manager.
     */
    @Autowired
    private PlatformTransactionManager m_transactionManager;

    /**
     * The Node DAO.
     */
    @Autowired
    private NodeDao m_nodeDao;

    /**
     * The IP interface DAO.
     */
    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    /**
     * The SNMP peer factory.
     */
    @Autowired
    private SnmpPeerFactory m_snmpPeerFactory;

    /**
     * The context.
     */
    private TestContext m_context;

    /**
     * The test host name.
     */
    private String m_testHostName;

    /**
     * The collection specification.
     */
    private CollectionSpecification m_collectionSpecification;

    /**
     * The collection agent.
     */
    private CollectionAgent m_collectionAgent;

    /**
     * The RRD strategy.
     */
    private RrdStrategy<?, ?> m_rrdStrategy;

    /**
     * The resource storage DAO.
     */
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
        MockLogAppender.setupLogging();

        m_rrdStrategy = new JRobinRrdStrategy();

        m_resourceStorageDao = new FilesystemResourceStorageDao();
        File snmpRrdDirectory = (File) m_context.getAttribute("rrdDirectory");
        m_resourceStorageDao.setRrdDirectory(snmpRrdDirectory.getParentFile());

        m_testHostName = InetAddressUtils.str(InetAddress.getLocalHost());

        OnmsIpInterface iface = null;
        OnmsNode testNode = null;
        Collection<OnmsNode> testNodes = m_nodeDao.findByLabel(TEST_NODE_LABEL);
        if (testNodes == null || testNodes.size() < 1) {
            NetworkBuilder builder = new NetworkBuilder();
            builder.addNode(TEST_NODE_LABEL).setId(1).setSysObjectId(".1.3.6.1.4.1.25506.11.1.34"); // Fake Cisco SysOID
            builder.addSnmpInterface(52).setIfName("GigabitEthernet1/0/52").setPhysAddr("b8af6729be61").setIfType(6).setCollectionEnabled(true).addIpInterface(m_testHostName).setIsSnmpPrimary("P");
            builder.addSnmpInterface(56).setIfName("Ten-GigabitEthernet1/1/1").setPhysAddr("b8af6729be62").setIfType(6).setCollectionEnabled(true).addIpInterface(m_testHostName).setIsSnmpPrimary("P");
            //builder.addSnmpInterface(18).setIfName("Se1/0.102").setIfAlias("Conexion Valencia").setIfType(32).setCollectionEnabled(true).addIpInterface("10.0.0.1").setIsSnmpPrimary("N");
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
        collector.initialize(null);

        m_collectionSpecification = CollectorTestUtils.createCollectionSpec("SNMP", collector, "default");
        m_collectionAgent = DefaultCollectionAgent.create(iface.getId(), m_ipInterfaceDao, m_transactionManager);
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
     * Test collection with MibObj and indirection mapping
     * indexEntry -mapsto--> IfIndex.
     *
     * @throws Exception the exception
     */
    @Test
    @JUnitCollector(datacollectionType = "snmp", datacollectionConfig = "/org/opennms/netmgt/config/datacollection-config-NMS8969.xml")
    @JUnitSnmpAgent(resource = "/org/opennms/netmgt/snmp/dot1dBasePortTable.properties")
    public void testCollectIndirection() throws Exception {
        System.setProperty("org.opennms.netmgt.collectd.SnmpCollector.limitCollectionToInstances", "true");

        m_collectionSpecification.initialize(m_collectionAgent);

        CollectionSet collectionSet = m_collectionSpecification.collect(m_collectionAgent);
        assertEquals("collection status", ServiceCollector.COLLECTION_SUCCEEDED, collectionSet.getStatus());
        CollectorTestUtils.persistCollectionSet(m_rrdStrategy, m_resourceStorageDao, m_collectionSpecification, collectionSet);

        m_collectionSpecification.release(m_collectionAgent);
        assertNotNull(m_resourceStorageDao);

        // we expect the two interfaces to be persisted

        ResourcePath rpath52 = ResourcePath.get("snmp", "1", "ifEntry","52");
        assertNotNull(rpath52);
        Map<String, String> itfReified52 = m_resourceStorageDao.getStringAttributes(rpath52);
        assertEquals("GigabitEthernet1/0/52", itfReified52.get("ifDescr"));
        ResourcePath rpath56 = ResourcePath.get("snmp", "1", "ifEntry","56");
        
        Map<String, String> itfReified56 = 
        		m_resourceStorageDao.getStringAttributes(rpath56);

        /*
        Map<String, String> itf52 = 
        		m_resourceStorageDao.getStringAttributes(ResourcePath.get("snmp", "1", "GigabitEthernet1_0_52-b8af6729be61"));
        */
        assertEquals("GigabitEthernet1/0/52", itfReified52.get("ifDescr"));
        assertEquals("Ten-GigabitEthernet1/1/1", itfReified56.get("ifDescr"));
        // each interface should have an ifIndex String Property
        // Not strictly necessary for ifIndex (we know this from the db) 
        // but we should be more generic to adapt to other use cases
        assertEquals("52", itfReified52.get("ifIndex"));
        assertEquals("56", itfReified56.get("ifIndex"));

        // we expect dot1dBasePort data  to be persisted
        // thanks to sibling storage strategy they are persisted under ifIndex
        // and not under dot1dBasePortIndex
        Map<String, String> dot1dPort52 = 
        		m_resourceStorageDao.getStringAttributes(ResourcePath.get("snmp", "1", "dot1dBasePortEntry", "52"));
        assertEquals("52", dot1dPort52.get("dot1dBasePortString"));
        assertEquals("52", dot1dPort52.get("dot1dBasePortIfIdx"));

        Map<String, String> dot1dPort53 = 
        		m_resourceStorageDao.getStringAttributes(ResourcePath.get("snmp", "1", "dot1dBasePortEntry", "56"));
        assertEquals("53", dot1dPort53.get("dot1dBasePortString"));
        assertEquals("56", dot1dPort53.get("dot1dBasePortIfIdx"));


        // at this point we are able to display each ifIndex in the GUI (in resource graph)
        // but it is hard to correlate them to other ifIndex data  as they are stored under the 
        // name (ifDescr) of the interface...


        // So as we would like to  be able to use the *name* (or ifDescr)  
        // we have to relate this in the string.properties associated to each
        // dot1dbasePort
        assertEquals("GigabitEthernet1/0/52", dot1dPort52.get("associatedIfDescr"));
        assertEquals("Ten-GigabitEthernet1/1/1", dot1dPort53.get("associatedIfDescr"));


        // Remark : Do we have to take multivalued backereferences into account (e.g. many dot1dPorts associated to one ifIndex).
    }
    
    /**
     * Test collection with MibObj and indirection mapping
     * indexEntry -mapsto--> IfIndex.
     *
     * @throws Exception the exception
     */
    @Test
    @JUnitCollector(datacollectionType = "snmp", datacollectionConfig = "/org/opennms/netmgt/config/datacollection-config-NMS8969-sibling.xml")
    @JUnitSnmpAgent(resource = "/org/opennms/netmgt/snmp/dot1dBasePortTable.properties")
    public void testCollectIndirectionSibling() throws Exception {
        System.setProperty("org.opennms.netmgt.collectd.SnmpCollector.limitCollectionToInstances", "true");

        m_collectionSpecification.initialize(m_collectionAgent);

        CollectionSet collectionSet = m_collectionSpecification.collect(m_collectionAgent);
        assertEquals("collection status", ServiceCollector.COLLECTION_SUCCEEDED, collectionSet.getStatus());
        CollectorTestUtils.persistCollectionSet(m_rrdStrategy, m_resourceStorageDao, m_collectionSpecification, collectionSet);

        m_collectionSpecification.release(m_collectionAgent);
        assertNotNull(m_resourceStorageDao);

        // we expect the two interfaces to be persisted

        ResourcePath rpath52 = ResourcePath.get("snmp", "1", "ifEntry","52");
        assertNotNull(rpath52);
        Map<String, String> itfReified52 = m_resourceStorageDao.getStringAttributes(rpath52);
        assertEquals("GigabitEthernet1/0/52", itfReified52.get("ifDescr"));
        ResourcePath rpath56 = ResourcePath.get("snmp", "1", "ifEntry","56");
        
        Map<String, String> itfReified56 = 
        		m_resourceStorageDao.getStringAttributes(rpath56);

        /*
        Map<String, String> itf52 = 
        		m_resourceStorageDao.getStringAttributes(ResourcePath.get("snmp", "1", "GigabitEthernet1_0_52-b8af6729be61"));
        */
        assertEquals("GigabitEthernet1/0/52", itfReified52.get("ifDescr"));
        assertEquals("Ten-GigabitEthernet1/1/1", itfReified56.get("ifDescr"));
        // each interface should have an ifIndex String Property
        // Not strictly necessary for ifIndex (we know this from the db) 
        // but we should be more generic to adapt to other use cases
        assertEquals("52", itfReified52.get("ifIndex"));
        assertEquals("56", itfReified56.get("ifIndex"));

        // we expect dot1dBasePort data  to be persisted
        // thanks to sibling storage strategy they are persisted under ifIndex
        // and not under dot1dBasePortIndex
        Map<String, String> dot1dPort52 = 
        		m_resourceStorageDao.getStringAttributes(ResourcePath.get("snmp", "1", "dot1dBasePortEntry", "GigabitEthernet1-0-52"));
        assertEquals("52", dot1dPort52.get("dot1dBasePortString"));
        assertEquals("52", dot1dPort52.get("dot1dBasePortIfIdx"));

        Map<String, String> dot1dPort53 = 
        		m_resourceStorageDao.getStringAttributes(ResourcePath.get("snmp", "1", "dot1dBasePortEntry", "Ten-GigabitEthernet1-1-1"));
        assertEquals("53", dot1dPort53.get("dot1dBasePortString"));
        assertEquals("56", dot1dPort53.get("dot1dBasePortIfIdx"));


        // at this point we are able to display each ifIndex in the GUI (in resource graph)
        // but it is hard to correlate them to other ifIndex data  as they are stored under the 
        // name (ifDescr) of the interface...


        // So as we would like to  be able to use the *name* (or ifDescr)  
        // we have to relate this in the string.properties associated to each
        // dot1dbasePort
        assertEquals("GigabitEthernet1/0/52", dot1dPort52.get("associatedIfDescr"));
        assertEquals("Ten-GigabitEthernet1/1/1", dot1dPort53.get("associatedIfDescr"));


        // Remark : Do we have to take multivalued backereferences into account (e.g. many dot1dPorts associated to one ifIndex).
    }
    
    
    
    @Test
    @JUnitCollector(datacollectionType = "snmp", datacollectionConfig = "/org/opennms/netmgt/config/datacollection-config-NMS8969IfIndex.xml")
    @JUnitSnmpAgent(resource = "/org/opennms/netmgt/snmp/dot1dBasePortTable.properties")
    public void testCollectIndirectionIfIndex() throws Exception {
        System.setProperty("org.opennms.netmgt.collectd.SnmpCollector.limitCollectionToInstances", "true");

        m_collectionSpecification.initialize(m_collectionAgent);
        m_resourceStorageDao.setRrdExtension(".jrb");

        CollectionSet collectionSet = m_collectionSpecification.collect(m_collectionAgent);
        assertEquals("collection status", ServiceCollector.COLLECTION_SUCCEEDED, collectionSet.getStatus());
        CollectorTestUtils.persistCollectionSet(m_rrdStrategy, m_resourceStorageDao, m_collectionSpecification, collectionSet);
        m_collectionSpecification.release(m_collectionAgent);
        assertNotNull(m_resourceStorageDao);

        // we expect the two interfaces to be persisted

        

        Map<String, String> itf52 = m_resourceStorageDao.getStringAttributes(ResourcePath.get("snmp", "1", "GigabitEthernet1_0_52-b8af6729be61"));
        assertEquals("GigabitEthernet1/0/52", itf52.get("ifDescr"));
        Map<String, String> itf56 = m_resourceStorageDao.getStringAttributes(ResourcePath.get("snmp", "1", "Ten_GigabitEthernet1_1_1-b8af6729be62"));
        assertEquals("Ten-GigabitEthernet1/1/1", itf56.get("ifDescr"));
        // each interface should have an ifIndex String Property
        // Not strictly necessary for ifIndex (we know this from the db) 
        // but we should be more generic to adapt to other use cases
        //assertEquals("52", itf52.get("ifIndex"));
        //assertEquals("56", itf56.get("ifIndex"));

        // we expect dot1dBasePort data  to be persisted
        // thanks to sibling storage strategy they are persisted under ifIndex
        // and not under dot1dBasePortIndex
        Map<String, String> dot1dPort52 = m_resourceStorageDao.getStringAttributes(ResourcePath.get("snmp", "1", "dot1dBasePortEntry", "GigabitEthernet1_0_52-b8af6729be61"));
        assertEquals("52", dot1dPort52.get("dot1dBasePortString"));
        assertEquals("52", dot1dPort52.get("dot1dBasePortIfIdx"));
        ResourcePath rpath53 = ResourcePath.get("snmp", "1", "dot1dBasePortEntry", "Ten_GigabitEthernet1_1_1-b8af6729be62");
        assertNotNull(rpath53);
        Map<String, String> dot1dPort53 = m_resourceStorageDao.getStringAttributes(rpath53);
        assertEquals("53", dot1dPort53.get("dot1dBasePortString"));
        assertEquals("56", dot1dPort53.get("dot1dBasePortIfIdx"));


        // at this point we are able to display each ifIndex in the GUI (in resource graph)
        // but it is hard to correlate them to other ifIndex data  as they are stored under the 
        // name (ifDescr) of the interface...


        // So as we would like to  be able to use the *name* (or ifDescr)  
        // we have to relate this in the string.properties associated to each
        // dot1dbasePort
        assertEquals("GigabitEthernet1_0_52-b8af6729be61", dot1dPort52.get("associatedIfDescr"));
        assertEquals("Ten_GigabitEthernet1_1_1-b8af6729be62", dot1dPort53.get("associatedIfDescr"));
        //590096 182148
        Set<OnmsAttribute> dot1dPort53Atts = m_resourceStorageDao.getAttributes(rpath53);
        // Remark : Do we have to take multivalued backereferences into account (e.g. many dot1dPorts associated to one ifIndex).

        
    }
    
    

    /* (non-Javadoc)
     * @see org.opennms.core.test.TestContextAware#setTestContext(org.springframework.test.context.TestContext)
     */
    @Override
    public void setTestContext(TestContext context) {
        m_context = context;
    }
}
