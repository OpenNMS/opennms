/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
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

import java.io.File;
import java.io.FileInputStream;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.collectd.JMXCollector.JMXCollectionResource;
import org.opennms.netmgt.collectd.JMXCollector.JMXCollectionSet;
import org.opennms.netmgt.collectd.jmxhelper.JmxTest;
import org.opennms.netmgt.collectd.jmxhelper.JmxTestMBean;
import org.opennms.netmgt.config.BeanInfo;
import org.opennms.netmgt.config.JMXDataCollectionConfigFactory;
import org.opennms.netmgt.config.collectd.jmx.Attrib;
import org.opennms.netmgt.config.collector.AttributeGroup;
import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.config.collector.CollectionAttribute;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.protocols.jmx.connectors.ConnectionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Markus Neumann <Markus@OpenNMS.org>
 */
public class JMXCollectorTest {

    private static Logger logger = LoggerFactory.getLogger(JMXCollectorTest.class);

    private JMXCollector jmxCollector;

    private MBeanServer platformMBeanServer;

    private CollectionAgent collectionAgent;

    private JMXNodeInfo jmxNodeInfo;

    private JMXDataCollectionConfigFactory jmxConfigFactory;

    @Before
    public void setUp() throws Exception {
        jmxNodeInfo = new JMXNodeInfo(0);
        jmxCollector = new JMXCollectorImpl();
        platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = new ObjectName("org.opennms.netmgt.collectd.jmxhelper:type=JmxTest");
        JmxTestMBean testMBean = new JmxTest();
        platformMBeanServer.registerMBean(testMBean, objectName);

        collectionAgent = EasyMock.createMock(CollectionAgent.class);
        EasyMock.expect(collectionAgent.getAddress()).andReturn(InetAddress.getLocalHost()).anyTimes();
        EasyMock.expect(collectionAgent.getAttribute("org.opennms.netmgt.collectd.JMXCollector.nodeInfo")).andReturn(jmxNodeInfo).anyTimes();
        EasyMock.expect(collectionAgent.getNodeId()).andReturn(0).anyTimes();
        EasyMock.expect(collectionAgent.getStorageDir()).andReturn(new File("")).anyTimes();

        EasyMock.replay(collectionAgent);

        FileInputStream configFileStream = new FileInputStream("src/test/resources/etc/JmxCollectorConfigTest.xml");
        logger.debug("ConfigFileStream check '{}'", configFileStream.available());
        jmxConfigFactory = new JMXDataCollectionConfigFactory(configFileStream);
        JMXDataCollectionConfigFactory.setInstance(jmxConfigFactory);
    }

    @After
    public void tearDown() throws Exception {
        jmxNodeInfo = null;
        jmxCollector.release();
        jmxCollector = null;
        platformMBeanServer.unregisterMBean(new ObjectName("org.opennms.netmgt.collectd.jmxhelper:type=JmxTest"));
        platformMBeanServer = null;
        EasyMock.verify(collectionAgent);
        EasyMock.reset(collectionAgent);
        jmxCollector = null;
    }

    /**
     * This test is just a prove of concept.
     */
    @Test
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
        CollectionSet collectionSet = jmxCollector.collect(collectionAgent, null, null);
        assertEquals("Collection of two dummy values run successfully", 1, collectionSet.getStatus());
    }

    @Test
    public void collectSingleMbeanWithSingleAttribute() {
        String collectionName = "collectSingleMbeanWithSingleAttribute";
        jmxNodeInfo.setMBeans(jmxConfigFactory.getMBeanInfo(collectionName));
        jmxNodeInfo.setDsMap(generateDataSourceMap(jmxConfigFactory.getAttributeMap(collectionName, "", "")));
        
        //start collection
        CollectionSet collectionSet = jmxCollector.collect(collectionAgent, null, null);
        JMXCollectionSet jmxCollectionSet = (JMXCollectionSet) collectionSet;
        JMXCollectionResource jmxCollectionResource = jmxCollectionSet.getResource();
        AttributeGroup group = jmxCollectionResource.getGroup(new AttributeGroupType("java_lang_type_Compilation", "all"));
        assertEquals(1, group.getAttributes().size());
        printDebugAttributeGroup(group);
        
        //ToDo Tak how to check if all metrics where collected?
        assertEquals("Collection: " + collectionName + " run successfully", 1, collectionSet.getStatus());
    }
    
    /**
     * Single attributes not provided by the agent will be ignored 
     */
    @Test
    public void collectSingleMbeanWithOneNotAvailableAttribute() {
        String collectionName = "collectSingleMbeanWithOneNotAvailableAttribute";
        jmxNodeInfo.setMBeans(jmxConfigFactory.getMBeanInfo(collectionName));
        jmxNodeInfo.setDsMap(generateDataSourceMap(jmxConfigFactory.getAttributeMap(collectionName, "", "")));
        
        //start collection
        CollectionSet collectionSet = jmxCollector.collect(collectionAgent, null, null);
        JMXCollectionSet jmxCollectionSet = (JMXCollectionSet) collectionSet;
        JMXCollectionResource jmxCollectionResource = jmxCollectionSet.getResource();
        AttributeGroup group = jmxCollectionResource.getGroup(new AttributeGroupType("java_lang_type_Compilation", "all"));
        assertEquals(0, group.getAttributes().size());
        printDebugAttributeGroup(group);
        
        assertEquals("Collection: " + collectionName + " run successfully", 1, collectionSet.getStatus());
    }

    @Test
    public void collectSingleMbeanWithOneNotAvailableAttributesAndOneAvailableAttributes() {
        String collectionName = "collectSingleMbeanWithOneNotAvailableAttributesAndOneAvailableAttributes";
        jmxNodeInfo.setMBeans(jmxConfigFactory.getMBeanInfo(collectionName));
        jmxNodeInfo.setDsMap(generateDataSourceMap(jmxConfigFactory.getAttributeMap(collectionName, "", "")));
        
        //start collection
        CollectionSet collectionSet = jmxCollector.collect(collectionAgent, null, null);
        JMXCollectionSet jmxCollectionSet = (JMXCollectionSet) collectionSet;
        JMXCollectionResource jmxCollectionResource = jmxCollectionSet.getResource();
        AttributeGroup group = jmxCollectionResource.getGroup(new AttributeGroupType("java_lang_type_Compilation", "all"));
        assertEquals(1, group.getAttributes().size());
        printDebugAttributeGroup(group);
        
        assertEquals("Collection: " + collectionName + " run successfully", 1, collectionSet.getStatus());
    }
    
    @Test
    public void collectSingleMbeanWithManyNotAvailableAttributesAndManyAvailableAttributes() {
        String collectionName = "collectSingleMbeanWithManyNotAvailableAttributesAndManyAvailableAttributes";
        jmxNodeInfo.setMBeans(jmxConfigFactory.getMBeanInfo(collectionName));
        jmxNodeInfo.setDsMap(generateDataSourceMap(jmxConfigFactory.getAttributeMap(collectionName, "", "")));
        
        //start collection
        CollectionSet collectionSet = jmxCollector.collect(collectionAgent, null, null);
        JMXCollectionSet jmxCollectionSet = (JMXCollectionSet) collectionSet;
        JMXCollectionResource jmxCollectionResource = jmxCollectionSet.getResource();
        AttributeGroup group = jmxCollectionResource.getGroup(new AttributeGroupType("java_lang_type_OperatingSystem", "all"));
        assertEquals(8, group.getAttributes().size());
        printDebugAttributeGroup(group);
        
        assertEquals("Collection: " + collectionName + " run successfully", 1, collectionSet.getStatus());
    }
    
    @Test
    public void collectSingleMbeanWithOneCompAttribWithAllItsCompMembers() {
        String collectionName = "collectSingleMbeanWithOneCompAttribWithAllItsCompMembers";
        jmxNodeInfo.setMBeans(jmxConfigFactory.getMBeanInfo(collectionName));
        jmxNodeInfo.setDsMap(generateDataSourceMap(jmxConfigFactory.getAttributeMap(collectionName, "", "")));
        
        //start collection
        CollectionSet collectionSet = jmxCollector.collect(collectionAgent, null, null);
        JMXCollectionSet jmxCollectionSet = (JMXCollectionSet) collectionSet;
        JMXCollectionResource jmxCollectionResource = jmxCollectionSet.getResource();
        AttributeGroup group = jmxCollectionResource.getGroup(new AttributeGroupType("java_lang_type_Memory", "all"));
        assertEquals(4, group.getAttributes().size());
        printDebugAttributeGroup(group);
        
        assertEquals("Collection: " + collectionName + " run successfully", 1, collectionSet.getStatus());
    }        
    
    @Test
    public void collectSingleMbeanWithOneCompAttribWithOneIgnoredCompMembers() {
        String collectionName = "collectSingleMbeanWithOneCompAttribWithOneIgnoredCompMembers";
        jmxNodeInfo.setMBeans(jmxConfigFactory.getMBeanInfo(collectionName));
        jmxNodeInfo.setDsMap(generateDataSourceMap(jmxConfigFactory.getAttributeMap(collectionName, "", "")));
        
        //start collection
        CollectionSet collectionSet = jmxCollector.collect(collectionAgent, null, null);
        JMXCollectionSet jmxCollectionSet = (JMXCollectionSet) collectionSet;
        JMXCollectionResource jmxCollectionResource = jmxCollectionSet.getResource();
        AttributeGroup group = jmxCollectionResource.getGroup(new AttributeGroupType("java_lang_type_Memory", "all"));
        assertEquals(3, group.getAttributes().size());
        printDebugAttributeGroup(group);
        
        assertEquals("Collection: " + collectionName + " run successfully", 1, collectionSet.getStatus());
    }        
    /**
     * Check if CompositeAttributes will be collected
     */
    @Test
    public void collectJvmDefaultComposites() {
        String mBeansObjectName = "java.lang:type=GarbageCollector,name=PS MarkSweep";
        Map<String, BeanInfo> mBeans = new HashMap<String, BeanInfo>();
        BeanInfo beanInfo = new BeanInfo();
        beanInfo.setObjectName(mBeansObjectName);

        List<String> attributes = new ArrayList<String>();
        attributes.add("CollectionCount");
        attributes.add("LastGcInfo");
        beanInfo.setAttributes(attributes);

        List<String> compositeAttributes = new ArrayList<String>();
        compositeAttributes.add("LastGcInfo");
        beanInfo.setCompositeAttributes(compositeAttributes);

        mBeans.put("first", beanInfo);
        jmxNodeInfo.setMBeans(mBeans);
        Map<String, JMXDataSource> dataSourceMap = new HashMap<String, JMXDataSource>();
        dataSourceMap.put(mBeansObjectName + "|CollectionCount", new JMXDataSource());
        //ToDo Tak set the JmxDataSource type to composite?
        dataSourceMap.put(mBeansObjectName + "|LastGcInfo", new JMXDataSource());

        jmxNodeInfo.setDsMap(dataSourceMap);
        CollectionSet collectionSet = jmxCollector.collect(collectionAgent, null, null);
        assertEquals("Collection of one Jvm default value run successfully", 1, collectionSet.getStatus());
    }

    private Map<String, JMXDataSource> generateDataSourceMap(Map<String, List<Attrib>> attributeMap) {
        return jmxCollector.buildDataSourceList("foo", attributeMap);
    }
        
    private void printDebugAttributeGroup(AttributeGroup group) {
        for (CollectionAttribute collectionAttribute : group.getAttributes()) {
            logger.debug("Attribute Type   '{}'", collectionAttribute.getAttributeType());
            logger.debug("Attribute Name   '{}'", collectionAttribute.getName());
            logger.debug("Attrubute Number '{}'", collectionAttribute.getNumericValue());
            logger.debug("Attrubute Value  '{}'", collectionAttribute.getStringValue());
        }
    }
    
    public class JMXCollectorImpl extends JMXCollector {

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
