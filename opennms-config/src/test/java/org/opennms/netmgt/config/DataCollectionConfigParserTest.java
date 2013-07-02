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

package org.opennms.netmgt.config;

import java.io.File;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.ConfigurationTestUtils;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.config.datacollection.SystemDef;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

/**
 * DataCollectionConfigParserTest
 * 
 * @author <a href="mail:agalue@opennms.org">Alejandro Galue</a>
 */
public class DataCollectionConfigParserTest {

    private static final int resourceTypesCount = 141;
    private static final int systemDefCount = 149;
    private static final int groupsCount = 228;

    @Before
    public void setUp() {
        MockLogAppender.setupLogging();
    }

    @After
    public void tearDown() {
        MockLogAppender.assertNoErrorOrGreater();
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
        File configFolder = getDatacollectionDirectory();
        DataCollectionConfigParser parser = new DataCollectionConfigParser(configFolder.getAbsolutePath());
        parser.parseCollection(collection);

        // Validate SNMP Collection
        Assert.assertEquals(0, collection.getResourceTypeCount()); 
        Assert.assertNull(collection.getSystems());
        Assert.assertNull(collection.getGroups());
    }

    @Test
    public void testLoadWithOnlyExternalReferences() throws Exception {
        // Create DatacollectionConfig
        Resource resource = new InputStreamResource(this.getClass().getResourceAsStream("datacollection-config-onlyimports.xml"));
        DatacollectionConfig config = CastorUtils.unmarshal(DatacollectionConfig.class, resource, false);

        // Validate default datacollection content
        SnmpCollection collection = config.getSnmpCollection(0);
        Assert.assertEquals(49, collection.getIncludeCollectionCount());
        Assert.assertEquals(0, collection.getResourceTypeCount()); 
        Assert.assertNull(collection.getSystems());
        Assert.assertNull(collection.getGroups());

        // Execute Parser
        executeParser(collection);

        // Validate SNMP Collection
        Assert.assertEquals(0, collection.getResourceTypeCount()); // Resource Types should live on a special collection
        Assert.assertEquals(142, collection.getSystems().getSystemDefCount());
        Assert.assertEquals(167, collection.getGroups().getGroupCount()); // Unused groups will be ignored
    }

    @Test
    public void testLoadHybridConfiguration() throws Exception {
        // Create DatacollectionConfig
        Resource resource = new InputStreamResource(this.getClass().getResourceAsStream("datacollection-config-hybrid.xml"));
        DatacollectionConfig config = CastorUtils.unmarshal(DatacollectionConfig.class, resource, false);

        // Validate default datacollection content
        SnmpCollection collection = config.getSnmpCollection(0);
        Assert.assertEquals(12, collection.getIncludeCollectionCount());
        Assert.assertEquals(0, collection.getResourceTypeCount()); 
        Assert.assertEquals(1, collection.getSystems().getSystemDefCount());
        Assert.assertEquals(1, collection.getGroups().getGroupCount());

        // Execute Parser
        executeParser(collection);

        // Validate SNMP Collection
        Assert.assertEquals(0, collection.getResourceTypeCount()); // Resource Types should live on a special collection
        Assert.assertEquals(17, collection.getSystems().getSystemDefCount());
        Assert.assertEquals(64, collection.getGroups().getGroupCount());
    }

    @Test
    public void testLoadSimple() throws Exception {
        // Create DatacollectionConfig
        Resource resource = new InputStreamResource(this.getClass().getResourceAsStream("datacollection-config-simple.xml"));
        DatacollectionConfig config = CastorUtils.unmarshal(DatacollectionConfig.class, resource, false);

        // Validate default datacollection content
        SnmpCollection collection = config.getSnmpCollection(0);
        Assert.assertEquals(1, collection.getIncludeCollectionCount());
        Assert.assertEquals(0, collection.getResourceTypeCount()); 
        Assert.assertNull(collection.getSystems());
        Assert.assertNull(collection.getGroups());

        // Execute Parser
        executeParser(collection);

        // Validate SNMP Collection
        Assert.assertEquals(0, collection.getResourceTypeCount()); // Resource Types should live on a special collection
        Assert.assertEquals(71, collection.getSystems().getSystemDefCount());
        Assert.assertEquals(27, collection.getGroups().getGroupCount());
    }

    @Test
    public void testLoadSimpleWithExclusions() throws Exception {
        // Create DatacollectionConfig
        Resource resource = new InputStreamResource(this.getClass().getResourceAsStream("datacollection-config-excludes.xml"));
        DatacollectionConfig config = CastorUtils.unmarshal(DatacollectionConfig.class, resource, false);

        // Validate default datacollection content
        SnmpCollection collection = config.getSnmpCollection(0);
        Assert.assertEquals(1, collection.getIncludeCollectionCount());
        Assert.assertEquals(0, collection.getResourceTypeCount()); 
        Assert.assertNull(collection.getSystems());
        Assert.assertNull(collection.getGroups());

        // Execute Parser
        executeParser(collection);

        // Validate SNMP Collection
        Assert.assertEquals(0, collection.getResourceTypeCount()); // Resource Types should live on a special collection
        Assert.assertEquals(41, collection.getSystems().getSystemDefCount()); // 48 systemDef to exclude
        Assert.assertEquals(26, collection.getGroups().getGroupCount()); //  1 group to exclude (used only on Cisco PIX or Cisco AS)
    }

    @Test
    public void testSingleSystemDefs() throws Exception {
        // Create DatacollectionConfig
        Resource resource = new InputStreamResource(this.getClass().getResourceAsStream("datacollection-config-single-systemdef.xml"));
        DatacollectionConfig config = CastorUtils.unmarshal(DatacollectionConfig.class, resource, false);

        // Validate default datacollection content
        SnmpCollection collection = config.getSnmpCollection(0);
        Assert.assertEquals(2, collection.getIncludeCollectionCount());
        Assert.assertEquals(0, collection.getResourceTypeCount()); 
        Assert.assertNull(collection.getSystems());
        Assert.assertNull(collection.getGroups());

        // Execute Parser
        executeParser(collection);

        // Validate SNMP Collection
        Assert.assertEquals(0, collection.getResourceTypeCount()); // Resource Types should live on a special collection
        Assert.assertEquals(2, collection.getSystems().getSystemDefCount());
        Assert.assertEquals(31, collection.getGroups().getGroupCount());
    }

    @Test
    public void testPrecedence() throws Exception {
        // Create DatacollectionConfig
        Resource resource = new InputStreamResource(this.getClass().getResourceAsStream("datacollection-config-hybrid-precedence.xml"));
        DatacollectionConfig config = CastorUtils.unmarshal(DatacollectionConfig.class, resource, false);
        
        // Validate default datacollection content
        SnmpCollection collection = config.getSnmpCollection(0);
        Assert.assertEquals(1, collection.getIncludeCollectionCount());
        Assert.assertEquals(0, collection.getResourceTypeCount());
        Assert.assertEquals(1, collection.getSystems().getSystemDefCount());
        Assert.assertEquals(1, collection.getGroups().getGroupCount());

        // Execute Parser
        executeParser(collection);

        // Validate SNMP Collection
        Assert.assertEquals(0, collection.getResourceTypeCount()); // Resource Types should live on a special collection
        Assert.assertEquals(71, collection.getSystems().getSystemDefCount());
        Assert.assertEquals(14, collection.getGroups().getGroupCount());

        // Test Precedence ~ any group/systemDef directly defined inside the SNMP collection will have precedence over any definition
        // from external files. That means, the definitions from external files will be ignored and won't be included in the collection.
        // This is a way to "override" the content of a specific datacollection-group.
        for (Group group : collection.getGroups().getGroupCollection()) {
            if (group.getName().equals("cisco-frame-relay")) {
                Assert.assertEquals(4, group.getMibObjCount());
            }
        }
        for (SystemDef systemDef : collection.getSystems().getSystemDefCollection()) {
            if (systemDef.getName().equals("Cisco Routers")) {
                Assert.assertEquals(3, systemDef.getCollect().getIncludeGroupCount());
            }
        }
    }

    private static File getDatacollectionDirectory() {
        File configFile = ConfigurationTestUtils.getFileForConfigFile("datacollection-config.xml");
        File configFolder = new File(configFile.getParentFile(), "datacollection");
        System.err.println(configFolder.getAbsolutePath());
        Assert.assertTrue(configFolder.isDirectory());
        return configFolder;
    }

    private static void executeParser(SnmpCollection collection) {
        File configFolder = getDatacollectionDirectory();
        DataCollectionConfigParser parser = new DataCollectionConfigParser(configFolder.getAbsolutePath());
        parser.parseCollection(collection);
        validateParser(parser);
    }

    private static void validateParser(DataCollectionConfigParser parser) {
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
