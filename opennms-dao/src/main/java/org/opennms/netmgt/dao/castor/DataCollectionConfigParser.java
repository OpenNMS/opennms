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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.Groups;
import org.opennms.netmgt.config.datacollection.IncludeCollection;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.config.datacollection.SystemDef;
import org.opennms.netmgt.config.datacollection.Systems;

import org.springframework.dao.DataAccessResourceFailureException;

/**
 * DataCollectionConfigParser
 * 
 * @author <a href="mail:agalue@opennms.org">Alejandro Galue</a>
 */
public class DataCollectionConfigParser {
    
    private String configDirectory;
    
    private DatacollectionGroup globalContainer;
    
    private Map<String,DatacollectionGroup> externalGroups;
    
    public DataCollectionConfigParser(String configDirectory) {
        this.configDirectory = configDirectory;
        this.globalContainer = new DatacollectionGroup();
        this.globalContainer.setName("__global__");
    }
    
    protected DatacollectionGroup getGlobalContainer() {
        return globalContainer;
    }
    
    public void parse(DatacollectionConfig config) throws Exception {
        // Parse External Configuration Files
        parseExternalResources();
        
        // Add all data from current defined SNMP collection to global container
        for (SnmpCollection collection : config.getSnmpCollectionCollection()) {
            merge(collection);
        }
        
        // Parse external files and add data to global container
        for (DatacollectionGroup externalGroup : externalGroups.values()) {
            merge(externalGroup);
        }
        
        // Validate Global Container
        validateGlobalContainer();
        
        // Build content for all SNMP Collections
        for (SnmpCollection collection : config.getSnmpCollectionCollection()) {
            if (collection.getIncludeCollectionCount() > 0) {
                // Prepare SNMP Collection
                if (collection.getSystems() == null)
                    collection.setSystems(new Systems());
                if (collection.getGroups() == null)
                    collection.setGroups(new Groups());
                for (IncludeCollection include : collection.getIncludeCollection()) {
                    if (include.getDataCollectionGroup() != null) {
                        // Include All system definitions from a specific datacollection group
                        mergeGroup(collection, include.getDataCollectionGroup());
                    } else {
                        if (include.getSystemDef() == null) {
                            throw new DataAccessResourceFailureException("You must specify at least the data collection group name or system definition name for the include-collection attribute");
                        } else {
                            // Include One system definition
                            mergeSystemDef(collection, include.getSystemDef());
                        }
                    }
                }
            } else {
                log().info("parse: snmp collection " + collection.getName() + " doesn't have any external reference.");
            }
        }
    }

    /**
     * Verify if the resourceTypes list contains a specific resourceType.
     * <p>One resource type will be considered the same as another one, if they have the same name and refers to the same storage strategy.</p>
     * 
     * @param globalContainer
     * @param resourceType
     * 
     * @return true, if the list contains the resourceType
     */
    private boolean contains(Collection<ResourceType> resourceTypes, ResourceType resourceType) {
        for (ResourceType rt : resourceTypes) {
            if (resourceType.getName().equals(rt.getName())
                    && resourceType.getStorageStrategy().getClass().equals(rt.getStorageStrategy().getClazz())
                    && resourceType.getPersistenceSelectorStrategy().getClazz().equals(rt.getPersistenceSelectorStrategy().getClazz()))
                return true;
        }
        return false;
    }
    
    /**
     * Verify if the groups list contains a specific group.
     * <p>One group will be considered the same as another one, if they have the same name and contain the same mib objects.</p>
     * 
     * @param globalContainer
     * @param group
     * @return true, if the list contains the mib object group
     */
    private boolean contains(Collection<Group> groups, Group group) {
        for (Group g : groups) {
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
     * Verify if the systemDefs list contains a specific system definition.
     * <p>One system definition will be considered the same as another one, if they have the same name and contain the same groups.
     * @param globalContainer
     * @param systemDef
     * 
     * @return true, if the list contains the system definition
     */
    private boolean contains(List<SystemDef> systemDefs, SystemDef systemDef) {
        for (SystemDef sd : systemDefs) {
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
    private void parseExternalResources() throws Exception {
        externalGroups = new HashMap<String, DatacollectionGroup>();
        File folder = new File(configDirectory);
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
                    log().info("parseExternalResources: parsing " + file);
                    group = CastorUtils.unmarshalWithTranslatedExceptions(DatacollectionGroup.class, in);
                    externalGroups.put(group.getName(), group);
                } finally {
                    IOUtils.closeQuietly(in);
                }
            }
        }
    }

    /**
     * Merge sourceGroup into global container
     * 
     * @param sourceGroup
     */
    private void merge(DatacollectionGroup sourceGroup) {
        // Add Resource Types
        for (ResourceType rt : sourceGroup.getResourceTypeCollection()) {
            if (!contains(globalContainer.getResourceTypeCollection(), rt)) {
                log().debug("merge: adding resource type " + rt.getName() + " from " + sourceGroup.getName() + " into global container.");
                globalContainer.addResourceType(rt);
            } else {
                log().warn("merge: resource type " + rt.getName() + " already exist on global container.");
            }
        }
        
        // Add Groups
        for (Group g : sourceGroup.getGroupCollection()) {
            if (!contains(globalContainer.getGroupCollection(), g)) {
                log().debug("merge: adding mib object group " + g.getName() + " from " + sourceGroup.getName() + " into global container.");
                globalContainer.addGroup(g);
            } else {
                log().warn("merge: mib object group " + g.getName() + " already exist on global container.");
            }
        }
        
        // Add System Definitions
        for (SystemDef sd : sourceGroup.getSystemDefCollection()) {
            if (!contains(globalContainer.getSystemDefCollection(), sd)) {
                log().debug("merge: adding system definition " + sd.getName() + " from " + sourceGroup.getName() + " into global container.");
                globalContainer.addSystemDef(sd);
            } else {
                log().warn("merge: system definition " + sd.getName() + " already exist on global container.");
            }
        }        
    }

    /**
     * Merge an SNMP collection into global container
     * 
     * @param the SNMP collection
     */
    private void merge(SnmpCollection collection) {
        // Add Resource Types
        for (ResourceType rt : collection.getResourceTypeCollection()) {
            if (!contains(globalContainer.getResourceTypeCollection(), rt)) {
                log().debug("merge: adding resource type " + rt.getName() + " from snmp-collection " + collection.getName() + " into global container.");
                globalContainer.addResourceType(rt);
            } else {
                log().warn("merge: resource type " + rt.getName() + " already exist on global container.");                
            }
        }
        
        // Add Groups
        if (collection.getGroups() != null) {
            for (Group g : collection.getGroups().getGroupCollection()) {
                if (!contains(globalContainer.getGroupCollection(), g)) {
                    log().debug("merge: adding mib object group " + g.getName() + " from snmp-collection " + collection.getName() + " into global container.");
                    globalContainer.addGroup(g);
                } else {
                    log().warn("merge: mib object group " + g.getName() + " already exist on global container.");
                }
            }
        }
        
        // Add System Definitions
        if (collection.getSystems() != null) {
            for (SystemDef sd : collection.getSystems().getSystemDefCollection()) {
                if (!contains(globalContainer.getSystemDefCollection(), sd)) {
                    log().debug("merge: adding system definition " + sd.getName() + " from snmp-collection " + collection.getName() + " into global container.");
                    globalContainer.addSystemDef(sd);
                } else {
                    log().warn("merge: system definition " + sd.getName() + " already exist on global container.");
                }
            }
        }
    }

    private void validateGlobalContainer() {
        // TODO Auto-generated method stub
    }

    private void mergeSystemDef(SnmpCollection collection, String systemDefName) {
        log().debug("mergeSystemDef: merging system defintion " + systemDefName + " into snmp-collection " + collection.getName());
        // Find System Definition
        SystemDef systemDef = null;
        for (SystemDef sd : globalContainer.getSystemDefCollection()) {
            if (sd.getName().equals(systemDefName)) {
                systemDef = sd;
                continue;
            }
        }
        if (systemDef == null) {
            log().error("Can't find system definition " + systemDefName);
            return;
        }
        Set<String> resourceTypes = new HashSet<String>();
        // Add System Definition to target snmp collection
        if (systemDef != null && !contains(collection.getSystems().getSystemDefCollection(), systemDef)) {
            log().debug("mergeSystemDef: adding system definition " + systemDef.getName() + " to snmp-collection " + collection.getName());
            collection.getSystems().addSystemDef(systemDef);
            // Add Groups
            for (String groupName : systemDef.getCollect().getIncludeGroupCollection()) {
                Group group = null;
                for (Group g : globalContainer.getGroupCollection()) {
                    if (g.getName().equals(groupName)) {
                        group = g;
                        continue;
                    }
                }
                if (group != null && !contains(collection.getGroups().getGroupCollection(), group)) {
                    log().debug("mergeSystemDef: adding mib object group " + group.getName() + " to snmp-collection " + collection.getName());
                    collection.getGroups().addGroup(group);
                    // Extract Resource Types
                    for (MibObj mo : group.getMibObjCollection()) {
                        if (!mo.getInstance().matches("^\\d+"))
                            resourceTypes.add(mo.getInstance());
                    }
                } else {
                    log().warn("Group " + groupName + " does not exist on global container, or snmp collection " + collection.getName() + " already has it.");
                }
            }
        }
        // Add Resource Types
        for (String resourceTypeName : resourceTypes) {
            for (ResourceType rt : globalContainer.getResourceTypeCollection()) {
                if (rt.getName().equals(resourceTypeName) && !contains(collection.getResourceTypeCollection(), rt)) {
                    log().debug("mergeSystemDef: adding resource type " + rt.getName() + " to snmp-collection " + collection.getName());
                    collection.addResourceType(rt);
                    continue;
                }
            }
        }
    }

    private void mergeGroup(SnmpCollection collection, String dataCollectionGroupName) {
        DatacollectionGroup group = externalGroups.get(dataCollectionGroupName);
        if (group != null) {
            log().debug("mergeGroup: adding all definitions from group " + group.getName() + " to snmp-collection " + collection.getName());
            System.err.println(group.getSystemDefCount());
            for (SystemDef systemDef : group.getSystemDefCollection()) {
                mergeSystemDef(collection, systemDef.getName());
            }
        } else {
            log().error("Group " + dataCollectionGroupName + " does not exist.");
        }
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

}
