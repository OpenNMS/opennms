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

package org.opennms.features.jmxconfiggenerator.jmxconfig;

import org.opennms.features.jmxconfiggenerator.jmxconfig.JmxDatacollectionConfiggenerator;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;
import javax.management.MBeanServer;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.xmlns.xsd.config.jmx_datacollection.JmxDatacollectionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Simon Walter <simon.walter@hp-factory.de>
 * @author Markus Neumann <markus@opennms.com>
 */
public class JmxDatacollectionConfiggeneratorTest {

    private static Logger logger = LoggerFactory.getLogger(JmxDatacollectionConfiggenerator.class);
    private JmxDatacollectionConfiggenerator jmxConfiggenerator;
    private MBeanServer platformMBeanServer;
    private Map<String, String> dictionary = new HashMap<String, String>();

    @Before
    public void setUp() throws Exception {
        jmxConfiggenerator = new JmxDatacollectionConfiggenerator();
        platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = new ObjectName("org.opennms.tools.jmxconfiggenerator.jmxconfig:type=JmxTest");
        JmxTestDummyMBean testMBean = new JmxTestDummy();
        platformMBeanServer.registerMBean(testMBean, objectName);
    }

    @After
    public void tearDown() throws Exception {
        jmxConfiggenerator = null;
        platformMBeanServer.unregisterMBean(new ObjectName("org.opennms.tools.jmxconfiggenerator.jmxconfig:type=JmxTest"));
        platformMBeanServer = null;
    }

    @Test
    public void testGenerateJmxConfigModelSkipJvmMBeans() {
        JmxDatacollectionConfig jmxConfigModel = jmxConfiggenerator.generateJmxConfigModel(platformMBeanServer, "testService", false, false, dictionary);
        Assert.assertEquals(1, jmxConfigModel.getJmxCollection().size());
        Assert.assertEquals(1, jmxConfigModel.getJmxCollection().get(0).getMbeans().getMbean().size());
        Assert.assertEquals("org.opennms.tools.jmxconfiggenerator.jmxconfig.JmxTest", jmxConfigModel.getJmxCollection().get(0).getMbeans().getMbean().get(0).getName());
        Assert.assertEquals(3, jmxConfigModel.getJmxCollection().get(0).getMbeans().getMbean().get(0).getAttrib().size());
    }

    @Test
    public void testGenerateJmxConfigModelRunWritableMBeans() {
        JmxDatacollectionConfig jmxConfigModel = jmxConfiggenerator.generateJmxConfigModel(platformMBeanServer, "testService", false, true, dictionary);
        Assert.assertEquals(1, jmxConfigModel.getJmxCollection().size());
        Assert.assertEquals(1, jmxConfigModel.getJmxCollection().get(0).getMbeans().getMbean().size());
        Assert.assertEquals("org.opennms.tools.jmxconfiggenerator.jmxconfig.JmxTest", jmxConfigModel.getJmxCollection().get(0).getMbeans().getMbean().get(0).getName());
        Assert.assertEquals(4, jmxConfigModel.getJmxCollection().get(0).getMbeans().getMbean().get(0).getAttrib().size());
    }

    @Test
    public void testGenerateJmxConfigModelRunJvmMBeans() {
        JmxDatacollectionConfig jmxConfigModel = jmxConfiggenerator.generateJmxConfigModel(platformMBeanServer, "testService", true, false, dictionary);
        Assert.assertEquals(1, jmxConfigModel.getJmxCollection().size());
        Assert.assertTrue(10 < jmxConfigModel.getJmxCollection().get(0).getMbeans().getMbean().size());
        Assert.assertEquals("org.opennms.tools.jmxconfiggenerator.jmxconfig.JmxTest", jmxConfigModel.getJmxCollection().get(0).getMbeans().getMbean().get(0).getName());
        Assert.assertEquals(3, jmxConfigModel.getJmxCollection().get(0).getMbeans().getMbean().get(0).getAttrib().size());
    }

    //@Test
    public void testGenerateJmxConfigCassandraLocal() throws MalformedURLException, IOException {
        JmxDatacollectionConfig jmxConfigModel = jmxConfiggenerator.generateJmxConfigModel(platformMBeanServer, "cassandra", false, false, dictionary);
        Assert.assertEquals(1, jmxConfigModel.getJmxCollection().size());
        Assert.assertEquals(35, jmxConfigModel.getJmxCollection().get(0).getMbeans().getMbean().size());
        Assert.assertEquals("org.apache.cassandra.internal.MemtablePostFlusher", jmxConfigModel.getJmxCollection().get(0).getMbeans().getMbean().get(0).getName());
    }

    //@Test
    public void testGenerateJmxConfigJmxMp() throws MalformedURLException, IOException {
    	
    	JMXServiceURL url = jmxConfiggenerator.getJmxServiceURL(false, "connect.opennms-edu.net", "9998");
    	JMXConnector jmxConnector = jmxConfiggenerator.getJmxConnector(null, null, url);
        MBeanServerConnection mBeanServerConnection = jmxConfiggenerator.createMBeanServerConnection(jmxConnector);
        logger.debug("MBeanServerConnection: '{}'",mBeanServerConnection);
        JmxDatacollectionConfig jmxConfigModel = jmxConfiggenerator.generateJmxConfigModel(mBeanServerConnection, "RemoteRepository", true, true, dictionary);
        Assert.assertEquals(1, jmxConfigModel.getJmxCollection().size());
        Assert.assertEquals(35, jmxConfigModel.getJmxCollection().get(0).getMbeans().getMbean().size());
        Assert.assertEquals("org.apache.cassandra.internal.MemtablePostFlusher", jmxConfigModel.getJmxCollection().get(0).getMbeans().getMbean().get(0).getName());
    }
}
