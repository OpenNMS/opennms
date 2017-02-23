/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.Optional;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opennms.core.test.MockLogAppender;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.config.datacollection.SystemDef;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;

/**
 * DataCollectionConfigParserTest
 * 
 * @author <a href="mail:agalue@opennms.org">Alejandro Galue</a>
 */
public class DataCollectionConfigParserTest {

    private static final int resourceTypesCount = 45;
    private static final int systemDefCount = 89;
    private static final int groupsCount = 103;

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
        Assert.assertEquals(0, collection.getIncludeCollections().size());
        Assert.assertEquals(0, collection.getResourceTypes().size()); 
        Assert.assertNull(collection.getSystems());
        Assert.assertNull(collection.getGroups());

        // Execute Parser
        final DataCollectionConfigParser parser = new DataCollectionConfigParser("target");
        parser.parseCollection(collection);

        // Validate SNMP Collection
        Assert.assertEquals(0, collection.getResourceTypes().size()); 
        Assert.assertNull(collection.getSystems());
        Assert.assertNull(collection.getGroups());
    }

    @Test
    public void testLoadWithOnlyExternalReferences() throws Exception {
        // Create DatacollectionConfig
        Resource resource = new InputStreamResource(this.getClass().getResourceAsStream("datacollection-config-onlyimports.xml"));
        DatacollectionConfig config = JaxbUtils.unmarshal(DatacollectionConfig.class, resource, false);

        // Validate default datacollection content
        SnmpCollection collection = config.getSnmpCollections().get(0);
        Assert.assertEquals(3, collection.getIncludeCollections().size());
        Assert.assertEquals(0, collection.getResourceTypes().size()); 
        Assert.assertNull(collection.getSystems());
        Assert.assertNull(collection.getGroups());

        // Execute Parser
        executeParser(collection);

        // Validate SNMP Collection
        Assert.assertEquals(0, collection.getResourceTypes().size()); // Resource Types should live on a special collection
        Assert.assertEquals(2, collection.getSystems().getSystemDefs().size());
        Assert.assertEquals(6, collection.getGroups().getGroups().size()); // Unused groups will be ignored
    }

    @Test
    public void testLoadHybridConfiguration() throws Exception {
        // Create DatacollectionConfig
        Resource resource = new InputStreamResource(this.getClass().getResourceAsStream("datacollection-config-hybrid.xml"));
        DatacollectionConfig config = JaxbUtils.unmarshal(DatacollectionConfig.class, resource, false);

        // Validate default datacollection content
        SnmpCollection collection = config.getSnmpCollections().get(0);
        Assert.assertEquals(13, collection.getIncludeCollections().size());
        Assert.assertEquals(0, collection.getResourceTypes().size()); 
        Assert.assertEquals(1, collection.getSystems().getSystemDefs().size());
        Assert.assertEquals(1, collection.getGroups().getGroups().size());

        // Execute Parser
        executeParser(collection);

        // Validate SNMP Collection
        Assert.assertEquals(0, collection.getResourceTypes().size()); // Resource Types should live on a special collection
        Assert.assertEquals(17, collection.getSystems().getSystemDefs().size());
        Assert.assertEquals(65, collection.getGroups().getGroups().size());
    }

    @Test
    public void testLoadSimple() throws Exception {
        // Create DatacollectionConfig
        Resource resource = new InputStreamResource(this.getClass().getResourceAsStream("datacollection-config-simple.xml"));
        DatacollectionConfig config = JaxbUtils.unmarshal(DatacollectionConfig.class, resource, false);

        // Validate default datacollection content
        SnmpCollection collection = config.getSnmpCollections().get(0);
        Assert.assertEquals(1, collection.getIncludeCollections().size());
        Assert.assertEquals(0, collection.getResourceTypes().size()); 
        Assert.assertNull(collection.getSystems());
        Assert.assertNull(collection.getGroups());

        // Execute Parser
        executeParser(collection);

        // Validate SNMP Collection
        Assert.assertEquals(0, collection.getResourceTypes().size()); // Resource Types should live on a special collection
        Assert.assertEquals(71, collection.getSystems().getSystemDefs().size());
        Assert.assertEquals(28, collection.getGroups().getGroups().size());
    }

    @Test
    public void testLoadSimpleWithExclusions() throws Exception {
        // Create DatacollectionConfig
        Resource resource = new InputStreamResource(this.getClass().getResourceAsStream("datacollection-config-excludes.xml"));
        DatacollectionConfig config = JaxbUtils.unmarshal(DatacollectionConfig.class, resource, false);

        // Validate default datacollection content
        SnmpCollection collection = config.getSnmpCollections().get(0);
        Assert.assertEquals(1, collection.getIncludeCollections().size());
        Assert.assertEquals(0, collection.getResourceTypes().size()); 
        Assert.assertNull(collection.getSystems());
        Assert.assertNull(collection.getGroups());

        // Execute Parser
        executeParser(collection);

        // Validate SNMP Collection
        Assert.assertEquals(0, collection.getResourceTypes().size()); // Resource Types should live on a special collection
        Assert.assertEquals(41, collection.getSystems().getSystemDefs().size()); // 48 systemDef to exclude
        Assert.assertEquals(27, collection.getGroups().getGroups().size()); //  1 group to exclude (used only on Cisco PIX or Cisco AS)
    }

    @Test
    public void testSingleSystemDefs() throws Exception {
        // Create DatacollectionConfig
        Resource resource = new InputStreamResource(this.getClass().getResourceAsStream("datacollection-config-single-systemdef.xml"));
        DatacollectionConfig config = JaxbUtils.unmarshal(DatacollectionConfig.class, resource, false);

        // Validate default datacollection content
        SnmpCollection collection = config.getSnmpCollections().get(0);
        Assert.assertEquals(2, collection.getIncludeCollections().size());
        Assert.assertEquals(0, collection.getResourceTypes().size()); 
        Assert.assertNull(collection.getSystems());
        Assert.assertNull(collection.getGroups());

        // Execute Parser
        executeParser(collection);

        // Validate SNMP Collection
        Assert.assertEquals(0, collection.getResourceTypes().size()); // Resource Types should live on a special collection
        Assert.assertEquals(2, collection.getSystems().getSystemDefs().size());
        Assert.assertEquals(32, collection.getGroups().getGroups().size());
    }

    @Test
    public void testPrecedence() throws Exception {
        // Create DatacollectionConfig
        Resource resource = new InputStreamResource(this.getClass().getResourceAsStream("datacollection-config-hybrid-precedence.xml"));
        DatacollectionConfig config = JaxbUtils.unmarshal(DatacollectionConfig.class, resource, false);
        
        // Validate default datacollection content
        SnmpCollection collection = config.getSnmpCollections().get(0);
        Assert.assertEquals(1, collection.getIncludeCollections().size());
        Assert.assertEquals(0, collection.getResourceTypes().size());
        Assert.assertEquals(1, collection.getSystems().getSystemDefs().size());
        Assert.assertEquals(1, collection.getGroups().getGroups().size());

        // Execute Parser
        executeParser(collection);

        // Validate SNMP Collection
        Assert.assertEquals(0, collection.getResourceTypes().size()); // Resource Types should live on a special collection
        Assert.assertEquals(71, collection.getSystems().getSystemDefs().size());
        Assert.assertEquals(14, collection.getGroups().getGroups().size());

        // Test Precedence ~ any group/systemDef directly defined inside the SNMP collection will have precedence over any definition
        // from external files. That means, the definitions from external files will be ignored and won't be included in the collection.
        // This is a way to "override" the content of a specific datacollection-group.
        for (Group group : collection.getGroups().getGroups()) {
            if (group.getName().equals("cisco-frame-relay")) {
                Assert.assertEquals(4, group.getMibObjs().size());
            }
        }
        for (SystemDef systemDef : collection.getSystems().getSystemDefs()) {
            if (systemDef.getName().equals("Cisco Routers")) {
                Assert.assertEquals(3, systemDef.getCollect().getIncludeGroups().size());
            }
        }
    }

    @Test
    public void testsForNMS8030() throws Exception {
        File home = new File("src/test/resources/NMS8030");
        Assert.assertTrue(home.exists());
        File configFile = new File(home, "etc/datacollection-config.xml");
        DefaultDataCollectionConfigDao dao = new DefaultDataCollectionConfigDao();
        dao.setConfigResource(new FileSystemResource(configFile));
        dao.setConfigDirectory(new File(home, "etc/datacollection").getAbsolutePath());
        dao.setReloadCheckInterval(0l);
        dao.afterPropertiesSet();

        // Use case 1

        Optional<SystemDef> systemDef = dao.getRootDataCollection().getSnmpCollections().stream()
                .filter(sc -> sc.getName().equals("sample1"))
                .flatMap(sc -> sc.getSystems().getSystemDefs().stream())
                .filter(s -> s.getName().equals("Cisco Routers"))
                .findFirst();
        Assert.assertTrue(systemDef.isPresent());
        Assert.assertEquals(5, systemDef.get().getCollect().getIncludeGroups().size());

        // Use Case 2

        Optional<Group> group = dao.getRootDataCollection().getSnmpCollections().stream()
                .filter(sc -> sc.getName().equals("sample2"))
                .flatMap(sc -> sc.getGroups().getGroups().stream())
                .filter(s -> s.getName().equals("cisco-memory-pool"))
                .findFirst();
        Assert.assertTrue(group.isPresent());
        Assert.assertEquals(3, group.get().getMibObjs().size());

        // Use case 3

        systemDef = dao.getRootDataCollection().getSnmpCollections().stream()
                .filter(sc -> sc.getName().equals("sample2"))
                .flatMap(sc -> sc.getSystems().getSystemDefs().stream())
                .filter(s -> s.getName().equals("Cisco Routers"))
                .findFirst();
        Assert.assertTrue(systemDef.isPresent());
        Assert.assertEquals(5, systemDef.get().getCollect().getIncludeGroups().size());

        // Use Case 4

        group = dao.getRootDataCollection().getSnmpCollections().stream()
                .filter(sc -> sc.getName().equals("sample2"))
                .flatMap(sc -> sc.getGroups().getGroups().stream())
                .filter(s -> s.getName().equals("cisco-memory-pool"))
                .findFirst();
        Assert.assertTrue(group.isPresent());
        Assert.assertEquals(3, group.get().getMibObjs().size());

    }
    
    private static File getDatacollectionDirectory() throws URISyntaxException {
        final File configFolder = new File("target/test-classes/org/opennms/netmgt/config/datacollection-config-parser-test/datacollection");
        Assert.assertTrue(configFolder.exists());
        Assert.assertTrue(configFolder.isDirectory());
        return configFolder;
    }

    private static void executeParser(SnmpCollection collection) throws URISyntaxException {
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
            currentResourceTypes += group.getResourceTypes().size();
        }
        for (DatacollectionGroup group : groupMap.values()) {
            currentSystemDefs += group.getSystemDefs().size();
        }
        for (DatacollectionGroup group : groupMap.values()) {
            currentMibGroups += group.getGroups().size();
        }
        Assert.assertEquals(resourceTypesCount, currentResourceTypes);
        Assert.assertEquals(systemDefCount, currentSystemDefs);
        Assert.assertEquals(groupsCount, currentMibGroups);
    }

}
