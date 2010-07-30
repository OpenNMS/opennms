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
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.IncludeCollection;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.config.datacollection.SystemDef;

import org.springframework.core.io.InputStreamResource;
import org.springframework.dao.DataAccessResourceFailureException;

/**
 * NEWDataCollectionTest
 * 
 * @author <a href="mail:agalue@opennms.org">Alejandro Galue</a>
 */
public class NEWDataCollectionTest {
    
    // IDEAS
    // It could be useful to change systemDefRef to use something more generic like:
    // <include-collection systemDef="Cisco Routers"/>
    // <include-collection dataCollectionGroup="Nortel"/>
    // The first one will add only systemDef Cisco Routers, but the second one will add all systemDef from Nortel's
    // Datacollection Group.
    
    // Validations
    // If there is no systemDefRef, groups and systems can't be empty
    @Test
    public void testLoad() throws Exception {
        DefaultDataCollectionConfigDao dao = new DefaultDataCollectionConfigDao();
        File file = new File("src/test/opennms-home/etc/datacollection-config.xml");
        System.err.println(file.getAbsolutePath());
        InputStream in = new FileInputStream("src/test/opennms-home/etc/datacollection-config.xml");
        dao.setConfigResource(new InputStreamResource(in));
        dao.afterPropertiesSet();
        
        // Get Current Configuration
        DatacollectionConfig config = dao.getContainer().getObject();
        Assert.assertNotNull(config); // FIXME
        
        // Create the global data collection group, which will contain all resource types, mib object groups and system definitions.
        DatacollectionGroup globalContainer = new DatacollectionGroup();
        globalContainer.setName("__global__");
        
        // Add all data from current defined SNMP collection to global container
        for (SnmpCollection collection : config.getSnmpCollectionCollection()) {
            merge(globalContainer, collection);
        }
        
        Assert.assertEquals(0, globalContainer.getResourceTypeCount()); // FIXME
        Assert.assertEquals(0, globalContainer.getSystemDefCount()); // FIXME
        Assert.assertEquals(0, globalContainer.getGroupCount()); // FIXME

        // Parse external files and add data to global container
        List<DatacollectionGroup> externalGroups = parseExternalResources();
        Assert.assertEquals(2, externalGroups.size()); // FIXME
        for (DatacollectionGroup externalGroup : externalGroups) {
            merge(globalContainer, externalGroup);
        }
        
        Assert.assertEquals(14, globalContainer.getResourceTypeCount()); // FIXME
        Assert.assertEquals(69, globalContainer.getSystemDefCount()); // FIXME
        Assert.assertEquals(44, globalContainer.getGroupCount()); // FIXME
        
        // Validate Global Container
        if (!validateGlobalContainer(globalContainer)) {
            // FIXME throw an exception here
        }
        
        // Build content for all SNMP Collections
        for (SnmpCollection collection : config.getSnmpCollectionCollection()) {
            if (collection.getIncludeCollectionCount() > 0) {
                for (IncludeCollection include : collection.getIncludeCollection()) {
                    if (include.getDataCollectionGroup() != null) {
                        // Include All system definitions from a specific datacollection group
                        mergeGroup(collection, globalContainer, include.getDataCollectionGroup());
                    } else {
                        if (include.getSystemDef() == null) {
                            throw new DataAccessResourceFailureException("You must specify at least the data collection group name or system definition name for the include-collection attribute");
                        } else {
                            // Include One system definition
                            mergeSystemDef(collection, globalContainer, include.getSystemDef());
                        }
                    }
                }
            }
        }
    }

    /**
     * Verify if the DatacollectionGroup object contains a specific resourceType.
     * <p>One resource type will be considered the same as another one, if they have the same name and refers to the same storage strategy.</p>
     * 
     * @param globalContainer
     * @param resourceType
     * 
     * @return true, if the datacollection group contains the resourceType
     */
    private boolean contains(DatacollectionGroup globalContainer, ResourceType resourceType) {
        for (ResourceType rt : globalContainer.getResourceTypeCollection()) {
            if (resourceType.getName().equals(rt.getName())
                    && resourceType.getStorageStrategy().getClass().equals(rt.getStorageStrategy().getClazz())
                    && resourceType.getPersistenceSelectorStrategy().getClazz().equals(rt.getPersistenceSelectorStrategy().getClazz()))
                return true;
        }
        return false;
    }
    
    /**
     * Verify if the DatacollectionGroup object contains a specific group.
     * <p>One group will be considered the same as another one, if they have the same name and contain the same mib objects.</p>
     * 
     * @param globalContainer
     * @param group
     * @return true, if the datacollection group contains the mib object group
     */
    private boolean contains(DatacollectionGroup globalContainer, Group group) {
        for (Group g : globalContainer.getGroupCollection()) {
            if (group.getName().equals(g.getName()) && group.getMibObjCount() == g.getMibObjCount()) {
                int count = 0;
                for (MibObj mo : g.getMibObjCollection()) {
                    if (contains(group, mo))
                        count++;
                }
                if (count == g.getMibObjCount())
                    return true;
            }
        }
        return false;
    }
    
    /**
     * Verify if the mib group contains a specific mib object.
     * <p>One mib object will be considered the same as another one, if they have the same alias, instance, oid and type.</p>
     * 
     * @param group
     * @param mibObj
     * 
     * @return true, if the mib group contains the mib object
     */
    private boolean contains(Group group, MibObj mibObj) {
        for (MibObj mo : group.getMibObjCollection()) {
            if (mo.getAlias().equals(mibObj.getAlias())
                    && mo.getInstance().equals(mibObj.getInstance())
                    && mo.getOid().equals(mibObj.getOid())
                    && mo.getType().equals(mibObj.getType()))
                return true;
        }
        return false;
    }


    /**
     * Verify if the DatacollectionGroup object contains a specific system definition.
     * <p>One system definition will be considered the same as another one, if they have the same name and contain the same groups.
     * @param globalContainer
     * @param systemDef
     * 
     * @return true, if the datacollection group contains the system definition
     */
    private boolean contains(DatacollectionGroup globalContainer, SystemDef systemDef) {
        for (SystemDef sd : globalContainer.getSystemDefCollection()) {
            if (systemDef.getName().equals(sd.getName()) && systemDef.getCollect().getIncludeGroupCount() == sd.getCollect().getIncludeGroupCount()) {
                int count = 0;
                for (String group : sd.getCollect().getIncludeGroupCollection()) {
                    if (systemDef.getCollect().getIncludeGroupCollection().contains(group))
                        count++;
                }
                if (count == sd.getCollect().getIncludeGroupCount())
                    return true;
            }
        }
        return false;
    }

    /**
     * Read all XML files from datacollection directory and parse them to create a list of DatacollectionGroup objects.
     * 
     * @return a list of datacollection group
     * 
     * @throws Exception
     */
    private List<DatacollectionGroup> parseExternalResources() throws Exception {
        List<DatacollectionGroup> groups = new LinkedList<DatacollectionGroup>();
        File folder = new File("src/test/opennms-home/etc/datacollection/");
        if (!folder.exists()) {
            throw new DataAccessResourceFailureException("Directory " + folder + " does not exist");
        }
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                InputStream in;
                DatacollectionGroup group;
                try {
                    in = new FileInputStream(file);
                } catch (IOException e) {
                    throw new DataAccessResourceFailureException("Could not get an input stream for resource '" + file + "'; nested exception: " + e, e);
                }
                try {
                    log().info("Parsing " + file);
                    group = CastorUtils.unmarshalWithTranslatedExceptions(DatacollectionGroup.class, in);
                    groups.add(group);
                } finally {
                    IOUtils.closeQuietly(in);
                }
            }
        }
        return groups;
    }

    /**
     * Merge sourceGroup into destinationGroup
     * 
     * @param sourceGroup
     * @param destinationGroup
     */
    private void merge(DatacollectionGroup destinationGroup, DatacollectionGroup sourceGroup) {
        // Add Resource Types
        for (ResourceType rt : sourceGroup.getResourceTypeCollection()) {
            if (!contains(destinationGroup, rt)) {
                log().debug("merge: adding resource type " + rt.getName() + " from " + sourceGroup.getName() + " into " + destinationGroup.getName());
                destinationGroup.addResourceType(rt);
            } else {
                log().warn("merge: resource type " + rt.getName() + " already exist on " + destinationGroup.getName());
            }
        }
        
        // Add Groups
        for (Group g : sourceGroup.getGroupCollection()) {
            if (!contains(destinationGroup, g)) {
                log().debug("merge: adding mib object group " + g.getName() + " from " + sourceGroup.getName() + " into " + destinationGroup.getName());
                destinationGroup.addGroup(g);
            } else {
                log().warn("merge: mib object group " + g.getName() + " already exist on " + destinationGroup.getName());
            }
        }
        
        // Add System Definitions
        for (SystemDef sd : sourceGroup.getSystemDefCollection()) {
            if (!contains(destinationGroup, sd)) {
                log().debug("merge: adding system definition " + sd.getName() + " from " + sourceGroup.getName() + " into " + destinationGroup.getName());
                destinationGroup.addSystemDef(sd);
            } else {
                log().warn("merge: system definition " + sd.getName() + " already exist on " + destinationGroup.getName());
            }
        }        
    }

    /**
     * Merge an snmp collection into destinationGroup
     * 
     * @param destinationGroup
     * @param collection
     */
    private void merge(DatacollectionGroup destinationGroup, SnmpCollection collection) {
        // Add Resource Types
        for (ResourceType rt : collection.getResourceTypeCollection()) {
            if (!contains(destinationGroup, rt)) {
                log().debug("merge: adding resource type " + rt.getName() + " from snmp-collection " + collection.getName() + " into " + destinationGroup.getName());
                destinationGroup.addResourceType(rt);
            } else {
                log().warn("merge: resource type " + rt.getName() + " already exist on " + destinationGroup.getName());                
            }
        }
        
        // Add Groups
        if (collection.getGroups() != null) {
            for (Group g : collection.getGroups().getGroupCollection()) {
                if (!contains(destinationGroup, g)) {
                    log().debug("merge: adding mib object group " + g.getName() + " from snmp-collection " + collection.getName() + " into " + destinationGroup.getName());
                    destinationGroup.addGroup(g);
                } else {
                    log().warn("merge: mib object group " + g.getName() + " already exist on " + destinationGroup.getName());
                }
            }
        }
        
        // Add System Definitions
        if (collection.getSystems() != null) {
            for (SystemDef sd : collection.getSystems().getSystemDefCollection()) {
                if (!contains(destinationGroup, sd)) {
                    log().debug("merge: adding system definition " + sd.getName() + " from snmp-collection " + collection.getName() + " into " + destinationGroup.getName());
                    destinationGroup.addSystemDef(sd);
                } else {
                    log().warn("merge: system definition " + sd.getName() + " already exist on " + destinationGroup.getName());
                }
            }
        }
    }

    private boolean validateGlobalContainer(DatacollectionGroup globalContainer) {
        // TODO Auto-generated method stub
        return false;
    }

    private void mergeSystemDef(SnmpCollection collection, DatacollectionGroup globalContainer, String systemDef) {
        // TODO Auto-generated method stub
        
    }

    private void mergeGroup(SnmpCollection collection, DatacollectionGroup globalContainer, String dataCollectionGroup) {
        // TODO Auto-generated method stub
        
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

/*
    private void mergeGroup(DatacollectionConfig config, SnmpCollection collection, DatacollectionGroup datacollectionGroup) {
        for (SystemDef systemDef : datacollectionGroup.getSystemDefCollection()) {
            // Search if systemDef already exist on the snmp collection object
            boolean found = false;
            for (SystemDef configuredSystemDef : collection.getSystems().getSystemDefCollection()) {
                if (configuredSystemDef.getName().equals(systemDef.getName())) {
                    found = true;
                    continue;
                }
            }
            if (found) {
                // Duplicated items are not allowed
                throw new DataAccessResourceFailureException("The systemDef " + systemDef.getName() + " is duplicated on snmp collection " + collection.getName() + " and data group " + datacollectionGroup.getName());
            }
            // Validate systemDef
            if (!isValidSystemDef(datacollectionGroup, systemDef)) {
                throw new DataAccessResourceFailureException("Invalid systemDef. May contain groups which does not exist.");
            }
            
            
            
        }
    }

    private boolean isValidSystemDef(DatacollectionGroup datacollectionGroup, SystemDef systemDef) {
        for (String groupName : systemDef.getCollect().getIncludeGroupCollection()) {
            // Find Group
            boolean found = false;
            for (Group group : datacollectionGroup.getGroupCollection()) {
                if (group.getName().equals(groupName)) {
                    // Validate Resource Type
                }
            }
        }
        return true;
    }

    private void mergeSystemDef(DatacollectionConfig config, SnmpCollection collection, String systemDef, Map<String, DatacollectionGroup> groupMap) {
        // TODO Auto-generated method stub
        
    }

*/
}
