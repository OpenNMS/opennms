/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc. OpenNMS(R) is Copyright (C)
 * 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OpenNMS(R). If not, see: http://www.gnu.org/licenses/
 *
 * For more information contact: OpenNMS(R) Licensing <license@opennms.org>
 * http://www.opennms.org/ http://www.opennms.com/
 ******************************************************************************
 */
package org.opennms.netmgt.collectd;

import java.io.File;
import org.easymock.EasyMock;

import java.lang.management.ManagementFactory;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import org.junit.*;
import org.opennms.netmgt.collectd.jmxhelper.JmxTest;
import org.opennms.netmgt.collectd.jmxhelper.JmxTestMBean;
import org.opennms.netmgt.config.BeanInfo;
import org.opennms.netmgt.config.collector.CollectionSet;
import org.opennms.protocols.jmx.connectors.ConnectionWrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.*;

/**
 *
 * @author Markus Neumann <Markus@OpenNMS.org>
 */
public class JMXCollectorTest {

    private static Logger logger = LoggerFactory.getLogger(JMXCollectorTest.class);

    private JMXCollector jmxCollector;

    private MBeanServer platformMBeanServer;

    private CollectionAgent collectionAgent;

    private JMXNodeInfo jmxNodeInfo = new JMXNodeInfo(0);

    @Before
    public void setUp() throws Exception {
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
    }

    @After
    public void tearDown() throws Exception {
        jmxCollector.release();
        jmxCollector = null;
        platformMBeanServer.unregisterMBean(new ObjectName("org.opennms.netmgt.collectd.jmxhelper:type=JmxTest"));
        platformMBeanServer = null;
        EasyMock.verify(collectionAgent);
    }

    /**
     * This test is just a prove of concept.
     */
    @Test
    public void closingIn() {
        Map<String, BeanInfo> mBeans = new HashMap<String, BeanInfo>();
        BeanInfo beanInfo = new BeanInfo();
        beanInfo.setObjectName("org.opennms.netmgt.collectd.jmxhelper:type=JmxTest");
        List<String> attributes = new ArrayList<String>();
        attributes.add("X");
        attributes.add("Name");
//TODO Tak: Test attributes that will return null is the next step        
//        attributes.add("NullString");
        beanInfo.setAttributes(attributes);
        mBeans.put("first", beanInfo);
        jmxNodeInfo.setMBeans(mBeans);
        Map<String, JMXDataSource> dataSourceMap = new HashMap<String, JMXDataSource>();
        dataSourceMap.put("org.opennms.netmgt.collectd.jmxhelper:type=JmxTest|X", new JMXDataSource());
        dataSourceMap.put("org.opennms.netmgt.collectd.jmxhelper:type=JmxTest|Name", new JMXDataSource());
//        dataSourceMap.put("org.opennms.netmgt.collectd.jmxhelper:type=JmxTest|NullString", new JMXDataSource());
        jmxNodeInfo.setDsMap(dataSourceMap);
        CollectionSet collectionSet = jmxCollector.collect(collectionAgent, null, null);
        assertEquals("Collection of two dummy values run successfully", 1, collectionSet.getStatus());
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
