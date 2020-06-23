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

package org.opennms.features.jmxconfiggenerator.jmxconfig;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.features.jmxconfiggenerator.jmxconfig.query.MBeanServerQueryException;
import org.opennms.features.jmxconfiggenerator.log.Slf4jLogAdapter;
import org.opennms.netmgt.config.collectd.jmx.JmxDatacollectionConfig;
import org.opennms.netmgt.config.collectd.jmx.Mbean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Throwables;

/**
 * @author Simon Walter <simon.walter@hp-factory.de>
 * @author Markus Neumann <markus@opennms.com>
 */
public class JmxDatacollectionConfiggeneratorTest {

    private static final Logger LOG = LoggerFactory.getLogger(JmxDatacollectionConfiggeneratorTest.class);

    private JmxDatacollectionConfiggenerator jmxConfiggenerator;
    private MBeanServer platformMBeanServer;
    private JmxTestDummyMBean testMBean;
    private ObjectName testObjectName;
    private Map<String, String> dictionary = new HashMap<String, String>();

    @Before
    public void setUp() throws Exception {
        jmxConfiggenerator = new JmxDatacollectionConfiggenerator(new Slf4jLogAdapter(JmxDatacollectionConfiggenerator.class));
        platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
        testObjectName = new ObjectName("org.opennms.tools.jmxconfiggenerator.jmxconfig:type=JmxTest");
        testMBean = new JmxTestDummy();
        platformMBeanServer.registerMBean(testMBean, testObjectName);
    }

    @After
    public void tearDown() throws Exception {
        jmxConfiggenerator = null;
        platformMBeanServer.unregisterMBean(testObjectName);
        platformMBeanServer = null;
    }

    @Test
    public void testGenerateJmxConfigModelSkipNonNumber() throws MBeanServerQueryException, IOException, JMException {
        JmxDatacollectionConfig jmxConfigModel = jmxConfiggenerator.generateJmxConfigModel(platformMBeanServer, "testService", true, false, dictionary);
        Assert.assertEquals(1, jmxConfigModel.getJmxCollectionList().size());
        Assert.assertTrue(10 < jmxConfigModel.getJmxCollectionList().get(0).getMbeans().size());

        Mbean mbean = findMbean(jmxConfigModel, "org.opennms.tools.jmxconfiggenerator.jmxconfig.JmxTest");
        Assert.assertNotNull(mbean);
        Assert.assertEquals(5, mbean.getAttribList().size());
        LOG.info(prettyPrint(jmxConfigModel));
    }

    @Test
    public void testGenerateJmxConfigModelSkipJvmMBeans() throws MBeanServerQueryException, IOException, JMException {
        JmxDatacollectionConfig jmxConfigModel = jmxConfiggenerator.generateJmxConfigModel(platformMBeanServer, "testService", false, true, dictionary);
        Assert.assertEquals(1, jmxConfigModel.getJmxCollectionList().size());
        Assert.assertEquals(1, jmxConfigModel.getJmxCollectionList().get(0).getMbeans().size());
        Assert.assertEquals("org.opennms.tools.jmxconfiggenerator.jmxconfig.JmxTest", jmxConfigModel.getJmxCollectionList().get(0).getMbeans().get(0).getName());
        Assert.assertEquals(4, jmxConfigModel.getJmxCollectionList().get(0).getMbeans().get(0).getAttribList().size());
        LOG.info(prettyPrint(jmxConfigModel));
    }

    @Test
    public void testGenerateJmxConfigModelRunJvmMBeans() throws MBeanServerQueryException, IOException, JMException {
        JmxDatacollectionConfig jmxConfigModel = jmxConfiggenerator.generateJmxConfigModel(platformMBeanServer, "testService", true, true, dictionary);
        Assert.assertEquals(1, jmxConfigModel.getJmxCollectionList().size());
        Assert.assertTrue(10 < jmxConfigModel.getJmxCollectionList().get(0).getMbeans().size());

        Mbean mbean = findMbean(jmxConfigModel, "org.opennms.tools.jmxconfiggenerator.jmxconfig.JmxTest");
        Assert.assertNotNull(mbean);
        Assert.assertEquals(4, mbean.getAttribList().size());
        LOG.info(prettyPrint(jmxConfigModel));
    }

    @Test
    public void testGenerateJmxConfigModelUsingMbeanFilter() throws MBeanServerQueryException, IOException, JMException {
        List<String> mbeanIds = new ArrayList<>();
        mbeanIds.add("java.lang:type=GarbageCollector,name=PS MarkSweep");
        mbeanIds.add("java.lang:type=GarbageCollector,name=PS Scavenge");
        JmxDatacollectionConfig jmxConfigModel = jmxConfiggenerator.generateJmxConfigModel(mbeanIds, platformMBeanServer, "testService", true, true, dictionary);
        Assert.assertNotNull(jmxConfigModel);
        LOG.info(prettyPrint(jmxConfigModel));

        Assert.assertEquals(2, jmxConfigModel.getJmxCollectionList().get(0).getMbeans().size());
        for (Mbean eachMbean : jmxConfigModel.getJmxCollectionList().get(0).getMbeans()) {
            Assert.assertEquals(2, eachMbean.getAttribList().size());
            Assert.assertEquals(0, eachMbean.getCompAttribList().size());
        }
    }

    @Test
    public void testGenerateJmxConfigModelUsingIdFilter() throws MBeanServerQueryException, IOException, JMException {
        List<String> mbeanIds = new ArrayList<>();
        mbeanIds.add("java.lang:type=GarbageCollector,name=PS MarkSweep:CollectionCount");
        mbeanIds.add("java.lang:type=GarbageCollector,name=PS Scavenge:CollectionTime");
        JmxDatacollectionConfig jmxConfigModel = jmxConfiggenerator.generateJmxConfigModel(mbeanIds, platformMBeanServer, "testService", true, true, dictionary);
        Assert.assertNotNull(jmxConfigModel);
        LOG.info(prettyPrint(jmxConfigModel));

        Assert.assertEquals(2, jmxConfigModel.getJmxCollectionList().get(0).getMbeans().size());
        for (Mbean eachMbean : jmxConfigModel.getJmxCollectionList().get(0).getMbeans()) {
            Assert.assertEquals(1, eachMbean.getAttribList().size());
            Assert.assertEquals(0, eachMbean.getCompAttribList().size());
        }
    }

    /**
     * Converts the given object to a pretty formatted XML string.
     * @param object The object to pretty print.
     * @param <T> The type of the object to pretty print.
     * @return The given object as a pretty formatted XML string.
     */
    private <T> String prettyPrint(T object) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            JAXBContext jaxbContext = JAXBContext.newInstance(object.getClass());
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(object, out);

            return out.toString();
        } catch (IOException | JAXBException je) {
            throw Throwables.propagate(je);
        }
    }

    /**
     * Finds the given mbeanName in the given jmxConfigModel.
     *
     * @param jmxConfigModel The Model to search the MBean.
     * @param mbeanName The name of the MBean to find.
     * @return The Mbean when names are matching, null otherwise.
     */
    private Mbean findMbean(JmxDatacollectionConfig jmxConfigModel, String mbeanName) {
        for (Mbean eachMbean : jmxConfigModel.getJmxCollectionList().get(0).getMbeans()) {
            if (Objects.equals(eachMbean.getName(), mbeanName)) {
                return eachMbean;
            }
        }
        return null;
    }
    
    @Test
    public void testRunMultipleTimes() throws MBeanServerQueryException, IOException, JMException {
        jmxConfiggenerator.generateJmxConfigModel(platformMBeanServer, "testService", true, true, dictionary);
        HashMap<String, Integer> aliasMapCopy = new HashMap<>(jmxConfiggenerator.aliasMap);
        ArrayList<String> aliasListCopy = new ArrayList<>(jmxConfiggenerator.aliasList);
        jmxConfiggenerator.generateJmxConfigModel(platformMBeanServer, "testService", true, true, dictionary);

        Assert.assertEquals(aliasMapCopy, jmxConfiggenerator.aliasMap);
        Assert.assertEquals(aliasListCopy, jmxConfiggenerator.aliasList);
    }

    @Test
    public void testCreateAndRegisterUniqueAlias() throws IOException {
        Assert.assertEquals("0alias1", jmxConfiggenerator.createAndRegisterUniqueAlias("alias1"));
        Assert.assertEquals("1alias1", jmxConfiggenerator.createAndRegisterUniqueAlias("alias1"));

        String someAlias = StringUtils.rightPad("X", 20, "X") + "YYY";
        String someOtherAlias = StringUtils.rightPad("X", 20, "X") + "XXX";
        Assert.assertEquals("0XXXXXXXXXXXXXXXXXX", jmxConfiggenerator.createAndRegisterUniqueAlias(someAlias));
        Assert.assertEquals("0XXXXXXXXXXXXXXXXXXXXXXX_NAME_CRASH_AS_19_CHAR_VALUE", jmxConfiggenerator.createAndRegisterUniqueAlias(someOtherAlias));

    }
}
