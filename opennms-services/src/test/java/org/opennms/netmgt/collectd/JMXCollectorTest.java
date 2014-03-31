/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R). If not, see:
 * http://www.gnu.org/licenses/
 *
 * For more information contact:
 * OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/
 * http://www.opennms.com/
 ******************************************************************************
 */
package org.opennms.netmgt.collectd;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.opennms.core.spring.BeanUtils;
import org.opennms.core.test.OpenNMSJUnit4ClassRunner;
import org.opennms.core.test.TestContextAware;
import org.opennms.core.test.db.annotations.JUnitTemporaryDatabase;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.collectd.jmxhelper.JmxTest;
import org.opennms.netmgt.collectd.jmxhelper.JmxTestMBean;
import org.opennms.netmgt.collection.api.AttributeGroup;
import org.opennms.netmgt.collection.api.AttributeGroupType;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.support.SingleResourceCollectionSet;
import org.opennms.netmgt.config.BeanInfo;
import org.opennms.netmgt.config.JMXDataCollectionConfigFactory;
import org.opennms.netmgt.config.collectd.jmx.Attrib;
import org.opennms.netmgt.dao.api.IpInterfaceDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.dao.api.ServiceTypeDao;
import org.opennms.netmgt.model.NetworkBuilder;
import org.opennms.netmgt.model.OnmsDistPoller;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.OnmsServiceType;
import org.opennms.netmgt.rrd.RrdUtils;
import org.opennms.protocols.jmx.connectors.ConnectionWrapper;
import org.opennms.test.FileAnticipator;
import org.opennms.test.JUnitConfigurationEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.transaction.PlatformTransactionManager;

/**
 *
 * @author Markus Neumann <Markus@OpenNMS.org>
 */
@RunWith(OpenNMSJUnit4ClassRunner.class)
@TestExecutionListeners({
    JUnitCollectorExecutionListener.class
})
@ContextConfiguration(locations = {
    "classpath:/META-INF/opennms/applicationContext-soa.xml",
    "classpath:/META-INF/opennms/applicationContext-dao.xml",
    "classpath*:/META-INF/opennms/component-dao.xml",
    "classpath:/META-INF/opennms/applicationContext-daemon.xml",
    "classpath:/META-INF/opennms/applicationContext-collectdTest.xml",
    "classpath:/META-INF/opennms/mockEventIpcManager.xml",
    "classpath*:/META-INF/opennms/applicationContext-minimal-conf.xml"
})
@JUnitConfigurationEnvironment(systemProperties = "org.opennms.rrd.storeByGroup=false")
@JUnitTemporaryDatabase
public class JMXCollectorTest implements TestContextAware, InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(JMXCollectorTest.class);

    @Autowired
    private PlatformTransactionManager m_transactionManager;

    @Autowired
    private NodeDao m_nodeDao;

    @Autowired
    private IpInterfaceDao m_ipInterfaceDao;

    @Autowired
    private ServiceTypeDao m_serviceTypeDao;

    @Autowired
    private Collectd m_collectd;

    private JMXCollector m_collector;

    private MBeanServer platformMBeanServer;

    private JMXNodeInfo jmxNodeInfo;

    private CollectionSpecification m_collectionSpecification;

    private TestContext m_context;

    private final OnmsDistPoller m_distPoller = new OnmsDistPoller("localhost", "127.0.0.1");

    private final String m_testHostName = "127.0.0.1";
    private CollectionAgent m_collectionAgent;

    @Override
    public void afterPropertiesSet() throws Exception {
        BeanUtils.assertAutowiring(this);
    }

    @Override
    public void setTestContext(TestContext t) {
        m_context = t;
    }

    private OnmsServiceType getServiceType(String name) {
        OnmsServiceType serviceType = m_serviceTypeDao.findByName(name);
        if (serviceType == null) {
            serviceType = new OnmsServiceType(name);
            m_serviceTypeDao.save(serviceType);
            m_serviceTypeDao.flush();
        }
        return serviceType;
    }

    @Rule
    public TestName m_testName = new TestName();

    @Before
    public void setUp() throws Exception {
        logger.debug("\n\n<<<< start of test: {} >>>>\n", m_testName.getMethodName());
        jmxNodeInfo = new JMXNodeInfo(1);
        platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = new ObjectName("org.opennms.netmgt.collectd.jmxhelper:type=JmxTest");
        JmxTestMBean testMBean = new JmxTest();
        platformMBeanServer.registerMBean(testMBean, objectName);

        if (m_nodeDao.findByLabel("testnode").isEmpty()) {
            NetworkBuilder builder = new NetworkBuilder(m_distPoller);
            builder.addNode("testnode");
            builder.addInterface(InetAddressUtils.normalize(m_testHostName)).setIsManaged("M").setIsSnmpPrimary("P");
            builder.addService(getServiceType("ICMP"));
            builder.addService(getServiceType("JMX"));
            OnmsNode n = builder.getCurrentNode();
            assertNotNull(n);
            m_nodeDao.save(n);
            m_nodeDao.flush();
        }

        m_collector = new JMXCollectorImpl();
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("collection", "default");
        m_collector.initialize(parameters);

        Collection<OnmsIpInterface> ifaces = m_ipInterfaceDao.findByIpAddress(m_testHostName);
        assertEquals(1, ifaces.size());
        OnmsIpInterface iface = ifaces.iterator().next();

        m_collectionSpecification = CollectorTestUtils.createCollectionSpec("JMX", m_collector, "default");
        m_collectionAgent = DefaultCollectionAgent.create(iface.getId(), m_ipInterfaceDao, m_transactionManager);
        m_collectionAgent.setAttribute(JMXCollector.NODE_INFO_KEY, jmxNodeInfo);

    }

    @After
    public void tearDown() throws Exception {
        jmxNodeInfo = null;
        if (platformMBeanServer != null) {
            platformMBeanServer.unregisterMBean(new ObjectName("org.opennms.netmgt.collectd.jmxhelper:type=JmxTest"));
            platformMBeanServer = null;
        }
        logger.debug("\n\n<<<< end of test: {} >>>>\n", m_testName.getMethodName());
    }

    /**
     * This test is just a prove of concept.
     */
    @Test
    @JUnitCollector(
      datacollectionConfig = "/etc/JmxCollectorConfigTest.xml",
      datacollectionType = "jsr160"
    )
    public void collectTwoBasicValues() {
        String mBeansObjectName = "org.opennms.netmgt.collectd.jmxhelper:type=JmxTest";
        Map<String, BeanInfo> mBeans = new HashMap<String, BeanInfo>();
        BeanInfo beanInfo = new BeanInfo();
        beanInfo.setObjectName(mBeansObjectName);
        List<String> attributes = new ArrayList<String>();
        attributes.add("X");
        attributes.add("Name");
//TODO Tak: Test attributes that will return null is the next step
//        attributes.add("NullString");
        beanInfo.setAttributes(attributes);
        mBeans.put("first", beanInfo);
        jmxNodeInfo.setMBeans(mBeans);
        Map<String, JMXDataSource> dataSourceMap = new HashMap<String, JMXDataSource>();
        dataSourceMap.put(mBeansObjectName + "|X", new JMXDataSource());
        dataSourceMap.put(mBeansObjectName + "|Name", new JMXDataSource());
//        dataSourceMap.put("org.opennms.netmgt.collectd.jmxhelper:type=JmxTest|NullString", new JMXDataSource());
        jmxNodeInfo.setDsMap(dataSourceMap);
        CollectionSet collectionSet = m_collector.collect(m_collectionAgent, null, null);
        assertEquals("Collection of two dummy values run successfully", 1, collectionSet.getStatus());
    }

    @Test
    @JUnitCollector(
      datacollectionConfig = "/etc/JmxCollectorConfigTest.xml",
      datacollectionType = "jsr160"
    )
    public void collectSingleMbeanWithSingleAttribute() {
        String collectionName = "collectSingleMbeanWithSingleAttribute";
        jmxNodeInfo.setMBeans(JMXDataCollectionConfigFactory.getInstance().getMBeanInfo(collectionName));
        jmxNodeInfo.setDsMap(generateDataSourceMap(collectionName, JMXDataCollectionConfigFactory.getInstance().getAttributeMap(collectionName, "", "")));

        //start collection
        CollectionSet collectionSet = m_collector.collect(m_collectionAgent, null, null);
        JMXCollectionSet jmxCollectionSet = (JMXCollectionSet) collectionSet;
        List<JMXCollectionResource> jmxCollectionResources = jmxCollectionSet.getCollectionResources();
        System.err.println("jmxCollectionResources: " + jmxCollectionResources);
        JMXCollectionResource jmxCollectionResource = jmxCollectionResources.get(0);
        AttributeGroup group = jmxCollectionResource.getGroup(new AttributeGroupType("java_lang_type_Compilation", AttributeGroupType.IF_TYPE_ALL));
        assertEquals(1, group.getAttributes().size());
        printDebugAttributeGroup(group);

        //ToDo Tak how to check if all metrics where collected?
        assertEquals("Collection: " + collectionName + " run successfully", 1, collectionSet.getStatus());
    }

    /**
     * Single attributes not provided by the agent will be ignored
     */
    @Test
    @JUnitCollector(
      datacollectionConfig = "/etc/JmxCollectorConfigTest.xml",
      datacollectionType = "jsr160"
    )
    public void collectSingleMbeanWithOneNotAvailableAttribute() {
        String collectionName = "collectSingleMbeanWithOneNotAvailableAttribute";
        jmxNodeInfo.setMBeans(JMXDataCollectionConfigFactory.getInstance().getMBeanInfo(collectionName));
        jmxNodeInfo.setDsMap(generateDataSourceMap(collectionName, JMXDataCollectionConfigFactory.getInstance().getAttributeMap(collectionName, "", "")));

        //start collection
        CollectionSet collectionSet = m_collector.collect(m_collectionAgent, null, null);
        JMXCollectionSet jmxCollectionSet = (JMXCollectionSet) collectionSet;
        List<JMXCollectionResource> jmxCollectionResources = jmxCollectionSet.getCollectionResources();
        assertEquals(1, jmxCollectionResources.size());
        JMXCollectionResource jmxCollectionResource = jmxCollectionResources.get(0);
        AttributeGroup group = jmxCollectionResource.getGroup(new AttributeGroupType("java_lang_type_Compilation", AttributeGroupType.IF_TYPE_ALL));
        assertEquals(0, group.getAttributes().size());
        printDebugAttributeGroup(group);

        assertEquals("Collection: " + collectionName + " run successfully", 1, collectionSet.getStatus());
    }

    @Test
    @JUnitCollector(
      datacollectionConfig = "/etc/JmxCollectorConfigTest.xml",
      datacollectionType = "jsr160"
    )
    public void collectSingleMbeanWithOneNotAvailableAttributesAndOneAvailableAttributes() {
        String collectionName = "collectSingleMbeanWithOneNotAvailableAttributesAndOneAvailableAttributes";
        jmxNodeInfo.setMBeans(JMXDataCollectionConfigFactory.getInstance().getMBeanInfo(collectionName));
        jmxNodeInfo.setDsMap(generateDataSourceMap(collectionName, JMXDataCollectionConfigFactory.getInstance().getAttributeMap(collectionName, "", "")));

        //start collection
        CollectionSet collectionSet = m_collector.collect(m_collectionAgent, null, null);
        JMXCollectionSet jmxCollectionSet = (JMXCollectionSet) collectionSet;
        List<JMXCollectionResource> jmxCollectionResources = jmxCollectionSet.getCollectionResources();
        assertEquals(1, jmxCollectionResources.size());
        JMXCollectionResource jmxCollectionResource = jmxCollectionResources.get(0);
        AttributeGroup group = jmxCollectionResource.getGroup(new AttributeGroupType("java_lang_type_Compilation", AttributeGroupType.IF_TYPE_ALL));
        assertEquals(1, group.getAttributes().size());
        printDebugAttributeGroup(group);

        assertEquals("Collection: " + collectionName + " run successfully", 1, collectionSet.getStatus());
    }

    @Test
    @JUnitCollector(
      datacollectionConfig = "/etc/JmxCollectorConfigTest.xml",
      datacollectionType = "jsr160"
    )
    public void collectSingleMbeanWithManyNotAvailableAttributesAndManyAvailableAttributes() {
        String collectionName = "collectSingleMbeanWithManyNotAvailableAttributesAndManyAvailableAttributes";
        jmxNodeInfo.setMBeans(JMXDataCollectionConfigFactory.getInstance().getMBeanInfo(collectionName));
        jmxNodeInfo.setDsMap(generateDataSourceMap(collectionName, JMXDataCollectionConfigFactory.getInstance().getAttributeMap(collectionName, "", "")));

        //start collection
        CollectionSet collectionSet = m_collector.collect(m_collectionAgent, null, null);
        JMXCollectionSet jmxCollectionSet = (JMXCollectionSet) collectionSet;
        List<JMXCollectionResource> jmxCollectionResources = jmxCollectionSet.getCollectionResources();
        assertEquals(1, jmxCollectionResources.size());
        JMXCollectionResource jmxCollectionResource = jmxCollectionResources.get(0);
        AttributeGroup group = jmxCollectionResource.getGroup(new AttributeGroupType("java_lang_type_OperatingSystem", AttributeGroupType.IF_TYPE_ALL));
        assertEquals(8, group.getAttributes().size());
        printDebugAttributeGroup(group);

        assertEquals("Collection: " + collectionName + " run successfully", 1, collectionSet.getStatus());
    }

    @Test
    @JUnitCollector(
      datacollectionConfig = "/etc/JmxCollectorConfigTest.xml",
      datacollectionType = "jsr160"
    )
    public void collectSingleMbeanWithOneCompAttribWithAllItsCompMembers() {
        String collectionName = "collectSingleMbeanWithOneCompAttribWithAllItsCompMembers";
        logger.debug("{} starting...", collectionName);
        jmxNodeInfo.setMBeans(JMXDataCollectionConfigFactory.getInstance().getMBeanInfo(collectionName));
        jmxNodeInfo.setDsMap(generateDataSourceMap(collectionName, JMXDataCollectionConfigFactory.getInstance().getAttributeMap(collectionName, "", "")));
        //printDataSourceMap(jmxNodeInfo.getDsMap());

        //start collection
        CollectionSet collectionSet = m_collector.collect(m_collectionAgent, null, null);
        JMXCollectionSet jmxCollectionSet = (JMXCollectionSet) collectionSet;
        List<JMXCollectionResource> jmxCollectionResources = jmxCollectionSet.getCollectionResources();
        //logger.debug("jmxCollectionResources: {}", jmxCollectionResources);
        assertEquals(1, jmxCollectionResources.size());
        JMXCollectionResource jmxCollectionResource = jmxCollectionResources.get(0);
        logger.debug("jmxCollectionResource: {}", jmxCollectionResource);
        logger.debug("jmxCollectionResource.groups: {}", jmxCollectionResource.getGroups());
        AttributeGroup group = jmxCollectionResource.getGroup(new AttributeGroupType("java_lang_type_Memory", AttributeGroupType.IF_TYPE_ALL));
        printDebugAttributeGroup(group);
        assertEquals(4, group.getAttributes().size());

        assertEquals("Collection: " + collectionName + " run successfully", 1, collectionSet.getStatus());
    }

    @Test
    @JUnitCollector(
      datacollectionConfig = "/etc/JmxCollectorConfigTest.xml",
      datacollectionType = "jsr160"
    )
    public void collectSingleMbeanWithOneCompAttribWithOneIgnoredCompMembers() {
        String collectionName = "collectSingleMbeanWithOneCompAttribWithOneIgnoredCompMembers";
        jmxNodeInfo.setMBeans(JMXDataCollectionConfigFactory.getInstance().getMBeanInfo(collectionName));
        jmxNodeInfo.setDsMap(generateDataSourceMap(collectionName, JMXDataCollectionConfigFactory.getInstance().getAttributeMap(collectionName, "", "")));

        //start collection
        CollectionSet collectionSet = m_collector.collect(m_collectionAgent, null, null);
        JMXCollectionSet jmxCollectionSet = (JMXCollectionSet) collectionSet;
        List<JMXCollectionResource> jmxCollectionResources = jmxCollectionSet.getCollectionResources();
        assertEquals(1, jmxCollectionResources.size());
        JMXCollectionResource jmxCollectionResource = jmxCollectionResources.get(0);
        AttributeGroup group = jmxCollectionResource.getGroup(new AttributeGroupType("java_lang_type_Memory", AttributeGroupType.IF_TYPE_ALL));
        assertEquals(3, group.getAttributes().size());
        printDebugAttributeGroup(group);

        assertEquals("Collection: " + collectionName + " run successfully", 1, collectionSet.getStatus());
    }

    /**
     * Check if CompositeAttributes will be collected
     */
    @Test
    @JUnitCollector(
      datacollectionConfig = "/etc/JmxCollectorConfigTest.xml",
      datacollectionType = "jsr160"
    )
    public void collectJvmDefaultComposites() {
        String mBeansObjectName = "java.lang:type=GarbageCollector,name=PS MarkSweep";
        Map<String, BeanInfo> mBeans = new HashMap<String, BeanInfo>();
        BeanInfo beanInfo = new BeanInfo();
        beanInfo.setObjectName(mBeansObjectName);

        List<String> attributes = new ArrayList<String>();
        attributes.add("CollectionCount");
        attributes.add("LastGcInfo");
        beanInfo.setAttributes(attributes);

        Map<String, List<String>> compositeAttributes = new HashMap<String, List<String>>();
        compositeAttributes.put("LastGcInfo", null);
        beanInfo.setCompositeAttributes(compositeAttributes);

        mBeans.put("first", beanInfo);
        jmxNodeInfo.setMBeans(mBeans);
        Map<String, JMXDataSource> dataSourceMap = new HashMap<String, JMXDataSource>();
        dataSourceMap.put(mBeansObjectName + "|CollectionCount", new JMXDataSource());
        //ToDo Tak set the JmxDataSource type to composite?
        dataSourceMap.put(mBeansObjectName + "|LastGcInfo", new JMXDataSource());

        jmxNodeInfo.setDsMap(dataSourceMap);
        CollectionSet collectionSet = m_collector.collect(m_collectionAgent, null, null);
        assertEquals("Collection of one Jvm default value run successfully", 1, collectionSet.getStatus());
    }

    /**
     * Check if CompositeAttributes will be collected
     */
    @Test
    @JUnitCollector(
      datacollectionConfig = "/etc/JmxCollectorConfigTest.xml",
      datacollectionType = "jsr160"
    )
    public void collectJvmDefaultComposites2() {
        String mBeansObjectName = "java.lang:type=MemoryPool,name=*";
        Map<String, BeanInfo> mBeans = new HashMap<String, BeanInfo>();
        BeanInfo beanInfo = new BeanInfo();
        beanInfo.setObjectName(mBeansObjectName);
        beanInfo.setKeyField("name");

        List<String> attributes = new ArrayList<String>();
        attributes.add("CollectionUsageThresholdCount");
        attributes.add("UsageThresholdCount");
        beanInfo.setAttributes(attributes);

        Map<String, List<String>> compositeAttributes = new HashMap<String, List<String>>();
        List<String> cuList = new ArrayList<String>();
        cuList.add("UsageThresholdCount");
        compositeAttributes.put("CollectionUsage", cuList);
        beanInfo.setCompositeAttributes(compositeAttributes);

        mBeans.put("first", beanInfo);
        jmxNodeInfo.setMBeans(mBeans);
        Map<String, JMXDataSource> dataSourceMap = new HashMap<String, JMXDataSource>();
        dataSourceMap.put(mBeansObjectName + "|CollectionCount", new JMXDataSource());
        //ToDo Tak set the JmxDataSource type to composite?
        dataSourceMap.put(mBeansObjectName + "|LastGcInfo", new JMXDataSource());
        dataSourceMap.put(mBeansObjectName + "|CollectionUsageThresholdCount", new JMXDataSource());
        dataSourceMap.put(mBeansObjectName + "|UsageThresholdCount", new JMXDataSource());

        jmxNodeInfo.setDsMap(dataSourceMap);
        CollectionSet collectionSet = m_collector.collect(m_collectionAgent, null, null);
        logger.debug("collectionSet: {}", collectionSet);
        assertEquals("Collection of one Jvm default value run successfully", 1, collectionSet.getStatus());
    }

    @Test
    @JUnitCollector(
      datacollectionConfig = "/org/opennms/netmgt/config/jmx-datacollection-test-persist.xml",
      datacollectionType = "jsr160",
      anticipateRrds = {"1/A", "1/B", "1/C", "1/D",
          "1/jvm-memory-pool/Code_Cache/committed",
          "1/jvm-memory-pool/Code_Cache/init",
          "1/jvm-memory-pool/Code_Cache/max",
          "1/jvm-memory-pool/Code_Cache/peak-committed",
          "1/jvm-memory-pool/Code_Cache/peak-init",
          "1/jvm-memory-pool/Code_Cache/peak-max",
          "1/jvm-memory-pool/Code_Cache/peak-used",
          "1/jvm-memory-pool/Code_Cache/used",
          "1/jvm-memory-pool/PS_Eden_Space/committed",
          "1/jvm-memory-pool/PS_Eden_Space/init",
          "1/jvm-memory-pool/PS_Eden_Space/max",
          "1/jvm-memory-pool/PS_Eden_Space/peak-committed",
          "1/jvm-memory-pool/PS_Eden_Space/peak-init",
          "1/jvm-memory-pool/PS_Eden_Space/peak-max",
          "1/jvm-memory-pool/PS_Eden_Space/peak-used",
          "1/jvm-memory-pool/PS_Eden_Space/used",
          "1/jvm-memory-pool/PS_Old_Gen/committed",
          "1/jvm-memory-pool/PS_Old_Gen/init",
          "1/jvm-memory-pool/PS_Old_Gen/max",
          "1/jvm-memory-pool/PS_Old_Gen/peak-committed",
          "1/jvm-memory-pool/PS_Old_Gen/peak-init",
          "1/jvm-memory-pool/PS_Old_Gen/peak-max",
          "1/jvm-memory-pool/PS_Old_Gen/peak-used",
          "1/jvm-memory-pool/PS_Old_Gen/used",
          "1/jvm-memory-pool/PS_Perm_Gen/committed",
          "1/jvm-memory-pool/PS_Perm_Gen/init",
          "1/jvm-memory-pool/PS_Perm_Gen/max",
          "1/jvm-memory-pool/PS_Perm_Gen/peak-committed",
          "1/jvm-memory-pool/PS_Perm_Gen/peak-init",
          "1/jvm-memory-pool/PS_Perm_Gen/peak-max",
          "1/jvm-memory-pool/PS_Perm_Gen/peak-used",
          "1/jvm-memory-pool/PS_Perm_Gen/used",
          "1/jvm-memory-pool/PS_Survivor_Space/committed",
          "1/jvm-memory-pool/PS_Survivor_Space/init",
          "1/jvm-memory-pool/PS_Survivor_Space/max",
          "1/jvm-memory-pool/PS_Survivor_Space/peak-committed",
          "1/jvm-memory-pool/PS_Survivor_Space/peak-init",
          "1/jvm-memory-pool/PS_Survivor_Space/peak-max",
          "1/jvm-memory-pool/PS_Survivor_Space/peak-used",
          "1/jvm-memory-pool/PS_Survivor_Space/used",},
      anticipateFiles = {"1/strings.properties", "1/jvm-memory-pool",
          "1/jvm-memory-pool/Code_Cache", "1/jvm-memory-pool/Code_Cache",
          "1/jvm-memory-pool/PS_Eden_Space", "1/jvm-memory-pool/PS_Old_Gen",
          "1/jvm-memory-pool/PS_Perm_Gen", "1/jvm-memory-pool/PS_Survivor_Space"
      }
    )
    public final void testPersistJmxStats() throws Exception {
        File snmpRrdDirectory = (File) m_context.getAttribute("rrdDirectory");
        FileAnticipator anticipator = (FileAnticipator) m_context.getAttribute("fileAnticipator");

        int numUpdates = 2;
        int stepSizeInSecs = 1;

        int stepSizeInMillis = stepSizeInSecs * 1000;

        m_collectionSpecification.initialize(m_collectionAgent);

        CollectorTestUtils.collectNTimes(m_collectionSpecification, m_collectionAgent, numUpdates);

        // node level collection
        File nodeDir = CollectorTestUtils.anticipatePath(anticipator, snmpRrdDirectory, "1");
        assertTrue(nodeDir.exists());
        listAllFiles(nodeDir);

        File aRrdFile = new File(nodeDir, CollectorTestUtils.rrd("A"));
        File bRrdFile = new File(nodeDir, CollectorTestUtils.rrd("B"));
        File cRrdFile = new File(nodeDir, CollectorTestUtils.rrd("C"));

        assertEquals("A", Double.valueOf(1.0), RrdUtils.fetchLastValueInRange(aRrdFile.getAbsolutePath(), "A", stepSizeInMillis, stepSizeInMillis));
        assertEquals("B", Double.valueOf(2.0), RrdUtils.fetchLastValueInRange(bRrdFile.getAbsolutePath(), "B", stepSizeInMillis, stepSizeInMillis));
        assertEquals("C", Double.valueOf(3.0), RrdUtils.fetchLastValueInRange(cRrdFile.getAbsolutePath(), "C", stepSizeInMillis, stepSizeInMillis));

        m_collectionSpecification.release(m_collectionAgent);
        //assertEquals("force-fail", true, false);
    }

    private void listAllFiles(File directory) {
        File[] files = directory.listFiles();
        for (File f : files) {
            if (f.isDirectory()) {
                listAllFiles(f);
            } else {
                logger.debug("file: {}", f);
            }
        }
    }

    private Map<String, JMXDataSource> generateDataSourceMap(String collectionName, Map<String, List<Attrib>> attributeMap) {
        return JMXCollector.buildDataSourceList(collectionName, attributeMap);
    }

    private void printDebugAttributeGroup(AttributeGroup group) {
        logger.debug("<<<<<< printDebugAttributeGroup >>>>>>");
        for (CollectionAttribute collectionAttribute : group.getAttributes()) {
            logger.debug("Attribute Type   '{}'", collectionAttribute.getAttributeType());
            logger.debug("Attribute Name   '{}'", collectionAttribute.getName());
            logger.debug("Attrubute Number '{}'", collectionAttribute.getNumericValue());
            logger.debug("Attrubute Value  '{}'", collectionAttribute.getStringValue());
        }
    }

    public class JMXCollectorImpl extends JMXCollector {

        public JMXCollectorImpl() {
            super();
            setServiceName("JMXCollectorTest");
            setUseFriendlyName(true);
        }

        @Override
        public ConnectionWrapper getMBeanServerConnection(Map<String, Object> map, InetAddress address) {
            return new ConnectionWrapper() {

                @Override
                public MBeanServerConnection getMBeanServer() {
                    return platformMBeanServer;
                }

                @Override
                public void close() {
                }
            };
        }
    }
}
