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
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.collection.test.CollectionSetUtils;
import org.opennms.core.collection.test.MockCollectionAgent;
import org.opennms.core.mate.api.EmptyScope;
import org.opennms.core.mate.api.Interpolator;
import org.opennms.netmgt.collectd.jmxhelper.JmxTest;
import org.opennms.netmgt.collectd.jmxhelper.JmxTestMBean;
import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionAttribute;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.CollectionStatus;
import org.opennms.netmgt.collection.api.ResourceType;
import org.opennms.netmgt.collection.api.ResourceTypeMapper;
import org.opennms.netmgt.collection.api.ServiceParameters.ParameterName;
import org.opennms.netmgt.config.BeanInfo;
import org.opennms.netmgt.config.JMXDataCollectionConfigDao;
import org.opennms.netmgt.dao.jmx.JmxConfigDaoJaxb;
import org.opennms.netmgt.jmx.connection.JmxConnectors;

/**
 *
 * @author Markus Neumann <Markus@OpenNMS.org>
 */
public class JMXCollectorTest {

    private JMXCollector jmxCollector;

    private MBeanServer platformMBeanServer;

    private CollectionAgent collectionAgent;

    private JMXNodeInfo jmxNodeInfo;

    private JMXDataCollectionConfigDao jmxDataCollectionConfigDao;

    @Before
    public void setUp() throws Exception {
        System.setProperty("opennms.home", new File("src/test/resources").getAbsolutePath());
        jmxDataCollectionConfigDao = new JMXDataCollectionConfigDao();

        jmxNodeInfo = new JMXNodeInfo(0);
        jmxCollector = new JMXCollectorImpl();
        jmxCollector.setJmxConfigDao(new JmxConfigDaoJaxb());
        jmxCollector.setJmxDataCollectionConfigDao(jmxDataCollectionConfigDao);
        platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = new ObjectName("org.opennms.netmgt.collectd.jmxhelper:type=JmxTest");
        JmxTestMBean testMBean = new JmxTest();
        platformMBeanServer.registerMBean(testMBean, objectName);

        collectionAgent = new MockCollectionAgent(1, "node", "fs", "fid", InetAddress.getLoopbackAddress());
    }

    @After
    public void tearDown() throws Exception {
        jmxNodeInfo = null;
        jmxCollector = null;
        platformMBeanServer.unregisterMBean(new ObjectName("org.opennms.netmgt.collectd.jmxhelper:type=JmxTest"));
        platformMBeanServer = null;
        collectionAgent = null;
        jmxCollector = null;
    }

    @Test
    public void collectSingleMbeanWithSingleAttribute() {
        Map<String, Map<String, CollectionAttribute>> attributesByNameByGroup = collect("collectSingleMbeanWithSingleAttribute");
        assertEquals(1, attributesByNameByGroup.get("java_lang_type_Compilation").size());
    }

    /**
     * Single attributes not provided by the agent will be ignored 
     */
    @Test
    public void collectSingleMbeanWithOneNotAvailableAttribute() {
        Map<String, Map<String, CollectionAttribute>> attributesByNameByGroup = collect("collectSingleMbeanWithOneNotAvailableAttribute");
        System.err.println(attributesByNameByGroup);
        assertFalse("No attributes should present in group.", attributesByNameByGroup.containsKey("java_lang_type_Compilation"));
    }

    @Test
    public void collectSingleMbeanWithOneNotAvailableAttributesAndOneAvailableAttributes() {
        Map<String, Map<String, CollectionAttribute>> attributesByNameByGroup = collect("collectSingleMbeanWithOneNotAvailableAttributesAndOneAvailableAttributes");
        assertEquals(1, attributesByNameByGroup.get("java_lang_type_Compilation").size());
    }

    @Test
    public void collectSingleMbeanWithManyNotAvailableAttributesAndManyAvailableAttributes() {
        Map<String, Map<String, CollectionAttribute>> attributesByNameByGroup = collect("collectSingleMbeanWithManyNotAvailableAttributesAndManyAvailableAttributes");
        assertEquals(8, attributesByNameByGroup.get("java_lang_type_OperatingSystem").size());
    }

    @Test
    public void collectSingleMbeanWithOneCompAttribWithAllItsCompMembers() {
        Map<String, Map<String, CollectionAttribute>> attributesByNameByGroup = collect("collectSingleMbeanWithOneCompAttribWithAllItsCompMembers");
        assertEquals(4, attributesByNameByGroup.get("java_lang_type_Memory").size());
    }

    @Test
    public void collectSingleMbeanWithOneCompAttribWithOneIgnoredCompMembers() {
        Map<String, Map<String, CollectionAttribute>> attributesByNameByGroup = collect("collectSingleMbeanWithOneCompAttribWithOneIgnoredCompMembers");
        assertEquals(3, attributesByNameByGroup.get("java_lang_type_Memory").size());
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

        List<String> attributes = new ArrayList<>();
        attributes.add("CollectionCount");
        attributes.add("LastGcInfo");
        beanInfo.setAttributes(attributes);

        List<String> compositeAttributes = new ArrayList<>();
        compositeAttributes.add("LastGcInfo");
        beanInfo.setCompositeAttributes(compositeAttributes);

        mBeans.put("first", beanInfo);
        jmxNodeInfo.setMBeans(mBeans);
        Map<String, JMXDataSource> dataSourceMap = new HashMap<String, JMXDataSource>();
        dataSourceMap.put(mBeansObjectName + "|CollectionCount", new JMXDataSource());
        dataSourceMap.put(mBeansObjectName + "|LastGcInfo", new JMXDataSource());

        jmxNodeInfo.setDsMap(dataSourceMap);
        CollectionSet collectionSet = jmxCollector.collect(collectionAgent, Collections.emptyMap());
        assertEquals("Collection of one Jvm default value failed", CollectionStatus.SUCCEEDED, collectionSet.getStatus());
    }

    @Test
    public void collectJvmMbeansWithWildCard() {

        final Map<String, Object> parms = new HashMap<String, Object>();
        parms.put(ParameterName.COLLECTION.toString(), "collectBasicJvmValues");
        parms.putAll(jmxCollector.getRuntimeAttributes(collectionAgent, parms));
        CollectionSet collectionSet = jmxCollector.collect(collectionAgent, Interpolator.interpolateAttributes(parms, EmptyScope.EMPTY));
        assertEquals("Collection of jvm values failed", CollectionStatus.SUCCEEDED, collectionSet.getStatus());

        ResourceType rt = mock(ResourceType.class, RETURNS_DEEP_STUBS);
        when(rt.getName()).thenReturn("jvm");
        when(rt.getStorageStrategy().getClazz()).thenReturn(MockStorageStrategy.class.getCanonicalName());
        when(rt.getPersistenceSelectorStrategy().getClazz())
                .thenReturn(MockPersistenceSelectorStrategy.class.getCanonicalName());
        ResourceTypeMapper.getInstance().setResourceTypeMapper((name) -> rt);
        Map<String, Map<String, CollectionAttribute>> attributesByNameByGroup = CollectionSetUtils
                .getAttributesByNameByGroup(collectionSet);
        // 2 attributes from the defined Numeric attributes and 3 string
        // attributes, domain, type, name
        assertEquals(5, attributesByNameByGroup.get("java_lang_type_GarbageCollector_name__").size());
        Map<String, CollectionAttribute> attributes = attributesByNameByGroup
                .get("java_lang_type_GarbageCollector_name__");
        CollectionAttribute attribute1 = attributes.get("domain");
        CollectionAttribute attribute2 = attributes.get("type");
        assertEquals("java.lang", attribute1.getStringValue());
        assertEquals("GarbageCollector", attribute2.getStringValue());
    }

    private Map<String, Map<String, CollectionAttribute>> collect(String collectionName) {
        final Map<String, Object> parms = new HashMap<String, Object>();
        parms.put(ParameterName.COLLECTION.toString(), collectionName);
        parms.putAll(jmxCollector.getRuntimeAttributes(collectionAgent, parms));

        //start collection
        final CollectionSet collectionSet = jmxCollector.collect(collectionAgent, Interpolator.interpolateAttributes(parms, EmptyScope.EMPTY));

        assertEquals("Collection: " + collectionName + " failed", CollectionStatus.SUCCEEDED, collectionSet.getStatus());

        return CollectionSetUtils.getAttributesByNameByGroup(collectionSet);
    }

    public class JMXCollectorImpl extends JMXCollector {
        @Override
        protected JmxConnectors getConnectionName() {
            return JmxConnectors.platform;
        }

        @Override
        public String serviceName() {
            return "platform";
        }
    }
}
