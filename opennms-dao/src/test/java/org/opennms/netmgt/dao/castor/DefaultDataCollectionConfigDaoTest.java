/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.dao.castor;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.opennms.netmgt.config.MibObject;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.config.datacollection.SystemDef;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.mock.MockLogAppender;
import org.springframework.core.io.InputStreamResource;

/**
 * DefaultDataCollectionConfigDaoTest
 *
 * @author <a href="mail:agalue@opennms.org">Alejandro Galue</a>
 */
public class DefaultDataCollectionConfigDaoTest {
    
    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
        System.setProperty("opennms.home", "src/test/opennms-home");
    }
    
    @After
    public void tearDown() {
        MockLogAppender.assertNoWarningsOrGreater();
    }

/*
    @Test
    public void testNewStyle() throws Exception {
        DefaultDataCollectionConfigDao dao = instantiateDao("datacollection-config.xml", true);
        executeTests(dao);
    }
*/

    @Test
    public void testOldStyle() throws Exception {
        DefaultDataCollectionConfigDao oldDao = instantiateDao("datacollection-config.xml", false);
        executeTests(oldDao);
    }
    
/*
    @Test
    public void testCompareOldAndNewStyles() throws Exception {
        DefaultDataCollectionConfigDao newDao = instantiateDao("datacollection-config.xml", true);
        DefaultDataCollectionConfigDao oldDao = instantiateDao("examples/old-datacollection-config.xml", false);
        compareContent(oldDao.getContainer().getObject(), newDao.getContainer().getObject());
    }
*/

    private void executeTests(DefaultDataCollectionConfigDao dao) {
        // Expected Values
        int netsnmpObjectsCount = 70;
        int ciscoObjectsCount = 44;
        int resourceTypesCount = 69;
        int systemDefCount = 126;

        // Execute Tests
        executeRepositoryTest(dao);
        executeMibObjectsTest(dao, ".1.3.6.1.4.1.8072.3.2.255", netsnmpObjectsCount);
        executeMibObjectsTest(dao, ".1.3.6.1.4.1.9.1.222", ciscoObjectsCount);
        executeResourceTypesTest(dao, resourceTypesCount);
        executeSystemDefCount(dao, systemDefCount);
    }

    private void executeRepositoryTest(DefaultDataCollectionConfigDao dao) {
        Assert.assertEquals("select", dao.getSnmpStorageFlag("default"));

        // Test Repository
        RrdRepository repository = dao.getRrdRepository("default");
        Assert.assertNotNull(repository);
        Assert.assertEquals(300, repository.getStep());

        // Test Step
        Assert.assertEquals(repository.getStep(), dao.getStep("default"));

        // Test RRA List
        List<String> rras = dao.getRRAList("default");
        Assert.assertEquals(repository.getRraList().size(), rras.size());
    }

    private DefaultDataCollectionConfigDao instantiateDao(String fileName, boolean setConfigDirectory) throws Exception {
        DefaultDataCollectionConfigDao dao = new DefaultDataCollectionConfigDao();
        File configFile = ConfigurationTestUtils.getFileForConfigFile(fileName);
        if (setConfigDirectory) {
            File configFolder = new File(configFile.getParentFile(), "datacollection");
            // Assert.assertTrue(configFolder.isDirectory());
            dao.setConfigDirectory(configFolder.getAbsolutePath());
        }
        dao.setConfigResource(new InputStreamResource(new FileInputStream(configFile)));
        dao.afterPropertiesSet();
        return dao;
    }

    private void executeSystemDefCount(DefaultDataCollectionConfigDao dao, int expectedCount) {
        DatacollectionConfig config = dao.getContainer().getObject();
        int systemDefCount = 0;
        for (SnmpCollection collection : config.getSnmpCollectionCollection()) {
            systemDefCount += collection.getSystems().getSystemDefCount();
        }
        Assert.assertEquals(expectedCount, systemDefCount);
    }

    private void executeResourceTypesTest(DefaultDataCollectionConfigDao dao, int expectedCount) {
        Map<String,ResourceType> resourceTypesMap = dao.getConfiguredResourceTypes();
        Assert.assertNotNull(resourceTypesMap);
        Assert.assertEquals(expectedCount, resourceTypesMap.size());
        Assert.assertTrue(resourceTypesMap.containsKey("frCircuitIfIndex")); // Used resource type
        Assert.assertTrue(resourceTypesMap.containsKey("wmiTcpipNetworkInterface")); // Unused resource type
        Assert.assertTrue(resourceTypesMap.containsKey("xmpFilesys")); // Unused resource type
    }

    private void executeMibObjectsTest(DefaultDataCollectionConfigDao dao, String systemOid, int expectedCount) {
        List<MibObject> mibObjects = dao.getMibObjectList("default", systemOid, "127.0.0.1", -1);
        Assert.assertNotNull(mibObjects);
        Assert.assertEquals(expectedCount, mibObjects.size());
    }

    private void compareContent(DatacollectionConfig refObj, DatacollectionConfig newObj) {
        Set<String> resourceTypes = new HashSet<String>();
        Set<String> systemDefs = new HashSet<String>();
        Set<String> groups = new HashSet<String>();

        for (SnmpCollection collection : refObj.getSnmpCollectionCollection()) {
            for (SystemDef sd : collection.getSystems().getSystemDefCollection()) {
                systemDefs.add(sd.getName());
                for (String group : sd.getCollect().getIncludeGroupCollection()) {
                    groups.add(group);
                }
            }
            for (Group g : collection.getGroups().getGroupCollection()) {
                if (groups.contains(g.getName())) {
                    for (MibObj mo : g.getMibObjCollection()) {
                        String i = mo.getInstance();
                        if (!i.matches("\\d+") && !i.equals("ifIndex"))
                            resourceTypes.add(mo.getInstance());
                    }
                }
            }
        }

        for (SnmpCollection collection : newObj.getSnmpCollectionCollection()) {
            for (Group g : collection.getGroups().getGroupCollection()) {
                for (MibObj mo : g.getMibObjCollection()) {
                    String i = mo.getInstance();
                    if (!i.matches("\\d+") && !i.equals("ifIndex"))
                        resourceTypes.remove(mo.getInstance());
                }
            }
            for (SystemDef sd : collection.getSystems().getSystemDefCollection()) {
                systemDefs.remove(sd.getName());
                for (String group : sd.getCollect().getIncludeGroupCollection()) {
                    groups.remove(group);
                }
            }
        }

        if (systemDefs.size() > 0) {
            Assert.fail("There are un-configured system definitions on the new datacollection: " + systemDefs);
        }

        if (groups.size() > 0) {
            Assert.fail("There are un-configured groups on the new datacollection: " + groups);
        }

        if (resourceTypes.size() > 0) {
            Assert.fail("There are un-configured resourceTypes on the new datacollection: " + resourceTypes);
        }
    }

}
