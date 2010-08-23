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
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.test.ConfigurationTestUtils;
import org.opennms.test.mock.MockLogAppender;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

/**
 * DataCollectionConfigParserTest
 * 
 * @author <a href="mail:agalue@opennms.org">Alejandro Galue</a>
 */
public class DataCollectionConfigParserTest {

    private static final int resourceTypesCount = 69;
    private static final int systemDefCount = 125;
    private static final int groupsCount = 183;

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
    }

    @After
    public void tearDown() {
        MockLogAppender.assertNoWarningsOrGreater();
    }

    @Test
    public void testLoadWithEmptyConfig() throws Exception {
        // Create a SNMP collection
        SnmpCollection collection = new SnmpCollection();
        collection.setName("default");

        // Validate default datacollection content
        Assert.assertEquals(0, collection.getIncludeCollectionCount());
        Assert.assertEquals(0, collection.getResourceTypeCount()); 
        Assert.assertNull(collection.getSystems());
        Assert.assertNull(collection.getGroups());

        // Execute Parser
        executeParser(collection);

        // Validate SNMP Collection
        Assert.assertEquals(0, collection.getResourceTypeCount()); 
        Assert.assertNull(collection.getSystems());
        Assert.assertNull(collection.getGroups());
    }

    @Test
    public void testLoadWithOnlyExternalReferences() throws Exception {
        // Create DatacollectionConfig
        Resource resource = new FileSystemResource("src/test/resources/datacollection/datacollection-config-onlyimports.xml");
        DatacollectionConfig config = CastorUtils.unmarshal(DatacollectionConfig.class, resource);

        // Validate default datacollection content
        SnmpCollection collection = config.getSnmpCollection(0);
        Assert.assertEquals(42, collection.getIncludeCollectionCount());
        Assert.assertEquals(0, collection.getResourceTypeCount()); 
        Assert.assertNull(collection.getSystems());
        Assert.assertNull(collection.getGroups());

        // Execute Parser
        executeParser(collection);

        // Validate SNMP Collection
        Assert.assertEquals(resourceTypesCount, collection.getResourceTypeCount()); 
        Assert.assertEquals(systemDefCount, collection.getSystems().getSystemDefCount());
        Assert.assertEquals(144, collection.getGroups().getGroupCount()); // Unused groups will be ignored
    }

    @Test
    public void testLoadHybridConfiguration() throws Exception {
        // Create DatacollectionConfig
        Resource resource = new FileSystemResource("src/test/resources/datacollection/datacollection-config-hybrid.xml");
        DatacollectionConfig config = CastorUtils.unmarshal(DatacollectionConfig.class, resource);

        // Validate default datacollection content
        SnmpCollection collection = config.getSnmpCollection(0);
        Assert.assertEquals(12, collection.getIncludeCollectionCount());
        Assert.assertEquals(0, collection.getResourceTypeCount()); 
        Assert.assertEquals(1, collection.getSystems().getSystemDefCount());
        Assert.assertEquals(1, collection.getGroups().getGroupCount());

        // Execute Parser
        executeParser(collection);

        // Validate SNMP Collection
        Assert.assertEquals(resourceTypesCount, collection.getResourceTypeCount()); 
        Assert.assertEquals(15, collection.getSystems().getSystemDefCount());
        Assert.assertEquals(61, collection.getGroups().getGroupCount());
    }

    @Test
    public void testLoadSimple() throws Exception {
        // Create DatacollectionConfig
        Resource resource = new FileSystemResource("src/test/resources/datacollection/datacollection-config-simple.xml");
        DatacollectionConfig config = CastorUtils.unmarshal(DatacollectionConfig.class, resource);

        // Validate default datacollection content
        SnmpCollection collection = config.getSnmpCollection(0);
        Assert.assertEquals(1, collection.getIncludeCollectionCount());
        Assert.assertEquals(0, collection.getResourceTypeCount()); 
        Assert.assertNull(collection.getSystems());
        Assert.assertNull(collection.getGroups());

        // Execute Parser
        executeParser(collection);

        // Validate SNMP Collection
        Assert.assertEquals(resourceTypesCount, collection.getResourceTypeCount()); 
        Assert.assertEquals(68, collection.getSystems().getSystemDefCount());
        Assert.assertEquals(26, collection.getGroups().getGroupCount());
    }

    @Test
    public void testLoadSimpleWithExclusions() throws Exception {
        // Create DatacollectionConfig
        Resource resource = new FileSystemResource("src/test/resources/datacollection/datacollection-config-excludes.xml");
        DatacollectionConfig config = CastorUtils.unmarshal(DatacollectionConfig.class, resource);

        // Validate default datacollection content
        SnmpCollection collection = config.getSnmpCollection(0);
        Assert.assertEquals(1, collection.getIncludeCollectionCount());
        Assert.assertEquals(0, collection.getResourceTypeCount()); 
        Assert.assertNull(collection.getSystems());
        Assert.assertNull(collection.getGroups());

        // Execute Parser
        executeParser(collection);

        // Validate SNMP Collection
        Assert.assertEquals(resourceTypesCount, collection.getResourceTypeCount()); 
        Assert.assertEquals(38, collection.getSystems().getSystemDefCount()); // 30 systemDef to exclude
        Assert.assertEquals(25, collection.getGroups().getGroupCount()); //  1 group to exclude (used only on Cisco PIX or Cisco AS)
    }

    private File getDatacollectionDirectory() {
        File configFile = ConfigurationTestUtils.getFileForConfigFile("datacollection-config.xml");
        File configFolder = new File(configFile.getParentFile(), "datacollection");
        System.err.println(configFolder.getAbsolutePath());
        Assert.assertTrue(configFolder.isDirectory());
        return configFolder;
    }

    private void executeParser(SnmpCollection collection) {
        File configFolder = getDatacollectionDirectory();
        DataCollectionConfigParser parser = new DataCollectionConfigParser(configFolder.getAbsolutePath());
        parser.parseCollection(collection);
        validateParser(parser);
    }

    private void validateParser(DataCollectionConfigParser parser) {
        Map<String,DatacollectionGroup> groupMap = parser.getExternalGroupMap();
        int currentResourceTypes = 0;
        int currentSystemDefs = 0;
        int currentMibGroups = 0;
        for (DatacollectionGroup group : groupMap.values()) {
            currentResourceTypes += group.getResourceTypeCount();
        }
        for (DatacollectionGroup group : groupMap.values()) {
            currentSystemDefs += group.getSystemDefCount();
        }
        for (DatacollectionGroup group : groupMap.values()) {
            currentMibGroups += group.getGroupCount();
        }
        Assert.assertEquals(resourceTypesCount, currentResourceTypes);
        Assert.assertEquals(systemDefCount, currentSystemDefs);
        Assert.assertEquals(groupsCount, currentMibGroups);
    }

}
