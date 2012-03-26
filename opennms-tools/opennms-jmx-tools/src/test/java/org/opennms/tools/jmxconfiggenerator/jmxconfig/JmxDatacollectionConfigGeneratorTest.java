/**
 * *****************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc. OpenNMS(R) is Copyright (C)
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
package org.opennms.tools.jmxconfiggenerator.jmxconfig;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import javax.management.*;
import org.junit.*;
import org.opennms.tools.jmxconfiggenerator.graphs.SnmpGraphConfigGenerator;

/**
 * @author Simon Walter <simon.walter@hp-factory.de>
 * @author Markus Neumann <markus@opennms.com>
 */
public class JmxDatacollectionConfigGeneratorTest {

    JmxDatacollectionConfigGenerator jmxConfigGen;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        jmxConfigGen = new JmxDatacollectionConfigGenerator();
    }

    @After
    public void tearDown() throws Exception {
        jmxConfigGen = null;
    }

    @Test
    public void testGenerateJmxConfig() throws AttributeNotFoundException, MBeanException, MalformedObjectNameException, ReflectionException, InstanceAlreadyExistsException, MBeanRegistrationException, NotCompliantMBeanException {
        MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        ObjectName objectName = new ObjectName("org.opennms.tools.jmxconfiggenerator.jmxconfig:type=JmxTest");
        JmxTestMBean testMBean = new JmxTest();
        platformMBeanServer.registerMBean(testMBean, objectName);
        jmxConfigGen.setJmxServerConnection(platformMBeanServer);
        jmxConfigGen.generateJmxConfig("testService", "localhost", "0000", null, null, true, "testOutFile.xml");
    }

    @Ignore
    @Test
    public void testGenerateJmxConfigCassandraLocal() throws AttributeNotFoundException, MBeanException {
        jmxConfigGen.setJmxServerConnection(null);
        jmxConfigGen.generateJmxConfig("cassandra", "localhost", "7199", null, null, true, "test.xml");
    }

    @Ignore
    @Test
    public void testGenerateGraphs() throws IOException {
        SnmpGraphConfigGenerator.generateGraphs("cassandra", "test.xml", "wtf.properties");
    }
}
