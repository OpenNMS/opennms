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

package org.opennms.netmgt.collectd;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.collectd.JMXCollector.JMXCollectionResource;
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
import org.opennms.netmgt.jmx.connection.JmxConnectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.FileInputStream;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 *
 * @author Markus Neumann <Markus@OpenNMS.org>
 */
public class JMXCollectorTest {

    private static Logger logger = LoggerFactory.getLogger(JMXCollectorTest.class);

    private static class DummyCollectionAgent implements CollectionAgent {
        private Map<String, Object> attributes = new HashMap<>();

        @Override
        public Boolean isStoreByForeignSource() {
            return null;
        }

        @Override
        public String getHostAddress() {
            return null;
        }

        @Override
        public void setSavedIfCount(int ifCount) { }

        @Override
        public int getNodeId() {
            return 0;
        }

        @Override
        public String getForeignSource() {
            return null;
        }

        @Override
        public String getForeignId() {
            return null;
        }

        @Override
        public java.io.File getStorageDir() {
            return new java.io.File("");
        }

        @Override
        public String getSysObjectId() {
            return null;
        }

        @Override
        public long getSavedSysUpTime() {
            return 0;
        }

        @Override
        public void setSavedSysUpTime(long sysUpTime) {

        }

        @Override
        public int getType() {
            return 0;
        }

        @Override
        public InetAddress getAddress() {
            try {
                return InetAddress.getLocalHost();
            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public <V> V getAttribute(String property) {
            return (V) attributes.get(property);
        }

        @Override
        public Object setAttribute(String property, Object value) {
            attributes.put(property, value);
            return value;
        }
    }

    private static class CollectionResult {
        private final SingleResourceCollectionSet resourceCollectionSet;
        private final JMXCollectionResource jmxCollectionResource;;

        private CollectionResult(SingleResourceCollectionSet resourceCollectionSet,
                                 JMXCollectionResource jmxCollectionResource) {
            this.resourceCollectionSet = resourceCollectionSet;
            this.jmxCollectionResource = jmxCollectionResource;
        }
    }

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

        collectionAgent = new DummyCollectionAgent();
        collectionAgent.setAttribute("org.opennms.netmgt.collectd.JMXCollector.nodeInfo", jmxNodeInfo);

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
        collectionAgent = null;
        jmxCollector = null;
    }

    @Test
    public void collectSingleMbeanWithSingleAttribute() {
        CollectionResult result = collect("collectSingleMbeanWithSingleAttribute");
        AttributeGroup group = result.jmxCollectionResource.getGroup(new AttributeGroupType("java_lang_type_Compilation", AttributeGroupType.IF_TYPE_ALL));
        assertEquals(1, group.getAttributes().size());
        printDebugAttributeGroup(group);
    }
    
    /**
     * Single attributes not provided by the agent will be ignored 
     */
    @Test
    public void collectSingleMbeanWithOneNotAvailableAttribute() {
        CollectionResult result = collect("collectSingleMbeanWithOneNotAvailableAttribute");
        AttributeGroup group = result.jmxCollectionResource.getGroup(new AttributeGroupType("java_lang_type_Compilation", AttributeGroupType.IF_TYPE_ALL));
        assertEquals(0, group.getAttributes().size());
        printDebugAttributeGroup(group);
    }

    @Test
    public void collectSingleMbeanWithOneNotAvailableAttributesAndOneAvailableAttributes() {
        CollectionResult result = collect("collectSingleMbeanWithOneNotAvailableAttributesAndOneAvailableAttributes");
        AttributeGroup group = result.jmxCollectionResource.getGroup(new AttributeGroupType("java_lang_type_Compilation", AttributeGroupType.IF_TYPE_ALL));
        assertEquals(1, group.getAttributes().size());
        printDebugAttributeGroup(group);
    }
    
    @Test
    public void collectSingleMbeanWithManyNotAvailableAttributesAndManyAvailableAttributes() {
        CollectionResult result = collect("collectSingleMbeanWithManyNotAvailableAttributesAndManyAvailableAttributes");
        AttributeGroup group = result.jmxCollectionResource.getGroup(new AttributeGroupType("java_lang_type_OperatingSystem", AttributeGroupType.IF_TYPE_ALL));
        assertEquals(8, group.getAttributes().size());
        printDebugAttributeGroup(group);
    }
    
    @Test
    public void collectSingleMbeanWithOneCompAttribWithAllItsCompMembers() {
        CollectionResult collectionResult = collect("collectSingleMbeanWithOneCompAttribWithAllItsCompMembers");
        AttributeGroup group = collectionResult.jmxCollectionResource.getGroup(new AttributeGroupType("java_lang_type_Memory", AttributeGroupType.IF_TYPE_ALL));
        assertEquals(4, group.getAttributes().size());
        printDebugAttributeGroup(group);
    }
    
    @Test
    public void collectSingleMbeanWithOneCompAttribWithOneIgnoredCompMembers() {
        CollectionResult collectionResult = collect("collectSingleMbeanWithOneCompAttribWithOneIgnoredCompMembers");
        AttributeGroup group = collectionResult.jmxCollectionResource.getGroup(new AttributeGroupType("java_lang_type_Memory", AttributeGroupType.IF_TYPE_ALL));
        assertEquals(3, group.getAttributes().size());
        printDebugAttributeGroup(group);
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
        dataSourceMap.put(mBeansObjectName + "|LastGcInfo", new JMXDataSource());

        jmxNodeInfo.setDsMap(dataSourceMap);
        CollectionSet collectionSet = jmxCollector.collect(collectionAgent, null, new HashMap<String, Object>());
        assertEquals("Collection of one Jvm default value failed", 1, collectionSet.getStatus());
    }

    private Map<String, JMXDataSource> generateDataSourceMap(final String collectionName, final Map<String, List<Attrib>> attributeMap) {
        return JMXCollector.buildDataSourceList(collectionName, attributeMap);
    }

    private CollectionResult collect(String collectionName) {
        jmxNodeInfo.setMBeans(jmxConfigFactory.getMBeanInfo(collectionName));
        jmxNodeInfo.setDsMap(generateDataSourceMap(collectionName, jmxConfigFactory.getAttributeMap(collectionName, "", "")));
        collectionAgent.setAttribute("collectionName", collectionName);

        //start collection
        final CollectionSet collectionSet = jmxCollector.collect(collectionAgent, null, new HashMap<String, Object>());
        final SingleResourceCollectionSet jmxCollectionSet = (SingleResourceCollectionSet) collectionSet;
        final JMXCollectionResource jmxCollectionResource = (JMXCollectionResource)jmxCollectionSet.getCollectionResource();

        assertEquals("Collection: " + collectionName + " failed", 1, collectionSet.getStatus());

        final CollectionResult collectionResult = new CollectionResult(jmxCollectionSet, jmxCollectionResource);
        return collectionResult;
    }
        
    private void printDebugAttributeGroup(AttributeGroup group) {
        for (CollectionAttribute collectionAttribute : group.getAttributes()) {
            logger.debug("Attribute Type   '{}'", collectionAttribute.getAttributeType());
            logger.debug("Attribute Name   '{}'", collectionAttribute.getName());
            logger.debug("Attribute Number '{}'", collectionAttribute.getNumericValue());
            logger.debug("Attribute Value  '{}'", collectionAttribute.getStringValue());
        }
    }

    public class JMXCollectorImpl extends JMXCollector {
        @Override
        protected String getConnectionName() {
            return JmxConnectors.PLATFORM;
        }
    }
}
