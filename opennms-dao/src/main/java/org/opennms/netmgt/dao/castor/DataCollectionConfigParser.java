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
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.io.IOUtils;

import org.opennms.core.utils.ThreadCategory;
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
    
    private Map<String,DatacollectionGroup> externalGroupsMap;
    
    public DataCollectionConfigParser(String configDirectory) {
        this.configDirectory = configDirectory;
    }
    
    protected Map<String,DatacollectionGroup> getExternalGroupMap() {
        return externalGroupsMap;
    }
    
    /**
     * Update/Validate SNMP collection
     * 
     * @param collection
     */
    public void parseCollection(SnmpCollection collection) {
        parseExternalResources();

        if (collection.getIncludeCollectionCount() > 0) {
            checkCollection(collection);
            // Add all Resource Types from cache. That because a resourceType is a shared object across all datacollection configuration.
            for (ResourceType rt : getAllResourceTypes()) {
                if (!contains(collection.getResourceTypeCollection(), rt))
                    collection.addResourceType(rt);
            }
            // Add systemDefs and dependencies
            for (IncludeCollection include : collection.getIncludeCollection()) {
                if (include.getDataCollectionGroup() != null) {
                    // Include All system definitions from a specific datacollection group
                    addDatacollectionGroup(collection, include.getDataCollectionGroup(), include.getExcludeFilterCollection());
                } else {
                    if (include.getSystemDef() == null) {
                        throwException("You must specify at least the data collection group name or system definition name for the include-collection attribute", null);
                    } else {
                        // Include One system definition
                        addSystemDef(collection, include.getSystemDef());
                    }
                }
            }
        } else {
            log().info("parse: snmp collection " + collection.getName() + " doesn't have any external reference.");
        }
    }
    
    /**
     * Verify the sub-groups of SNMP collection.
     * 
     * @param collection
     */
    private void checkCollection(SnmpCollection collection) {
        if (collection.getSystems() == null)
            collection.setSystems(new Systems());
        if (collection.getGroups() == null)
            collection.setGroups(new Groups());
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
     * Verify if the MIB group contains a specific MIB object.
     * <p>One MIB object will be considered the same as another one, if they have the same alias, instance, oid and type.</p>
     * 
     * @param group
     * @param mibObj
     * 
     * @return true, if the MIB group contains the MIB object
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
     * <p>One system definition will be considered the same as another one, if they have the same name and contain the same groups.</p>
     * 
     * @param globalContainer
     * @param systemDef
     * 
     * @return true, if the list contains the system definition
     */
    // TODO Include sysoid and sysoidMask on validation process
    private boolean contains(List<SystemDef> systemDefs, SystemDef systemDef) {
        for (SystemDef sd : systemDefs) {
            if (systemDef.getName().equals(sd.getName())
                    && systemDef.getCollect().getIncludeGroupCount() == sd.getCollect().getIncludeGroupCount()) {
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
     */
    private void parseExternalResources() {
        // Ensure that this is called only once.
        if (externalGroupsMap != null && externalGroupsMap.size() > 0) {
            log().info("parseExternalResources: external data collection groups are already parsed");
            return;
        }
        
        // Create external groups map
        externalGroupsMap = new HashMap<String, DatacollectionGroup>();
        
        // Check configuration files repository
        File folder = new File(configDirectory);
        if (!folder.exists() || !folder.isDirectory()) {
            log().info("parseExternalResources: directory " + folder + " does not exist or is not a folder.");
            return;
        }
        
        // Get external configuration files
        File[] listOfFiles = folder.listFiles(new FilenameFilter() {
            public boolean accept(File file, String name) {
                return name.endsWith(".xml");
            }
        });
        
        // Parse configuration files (populate external groups map)
        for (File file : listOfFiles) {
            InputStream in = null;
            try {
                in = new FileInputStream(file);
            } catch (IOException e) {
                throwException("Could not get an input stream for resource '" + file + "'; nested exception: " + e.getMessage(), e);
            }
            try {
                log().debug("parseExternalResources: parsing " + file);
                DatacollectionGroup group = CastorUtils.unmarshalWithTranslatedExceptions(DatacollectionGroup.class, in);
                group.validate();
                externalGroupsMap.put(group.getName(), group);
            } catch (Exception e) {
                throwException("Can't parse XML file " + file + "; nested exception: " + e.getMessage(), e);
            } finally {
                IOUtils.closeQuietly(in);
            }
        }
    }

    private Set<ResourceType> getAllResourceTypes() {
        Set<ResourceType> resourceTypes = new HashSet<ResourceType>();
        for (DatacollectionGroup group : externalGroupsMap.values()) {
            for (ResourceType rt : group.getResourceTypeCollection()) {
                if (!contains(resourceTypes, rt))
                    resourceTypes.add(rt);
            }
        }
        return resourceTypes;
    }

    private SystemDef getSystemDef(String systemDefName) {
        for (DatacollectionGroup group : externalGroupsMap.values()) {
            for (SystemDef sd : group.getSystemDefCollection()) {
                if (sd.getName().equals(systemDefName)) {
                    return sd;
                }
            }
        }
        return null;
    }

    private Group getMibObjectGroup(String groupName) {
        for (DatacollectionGroup group : externalGroupsMap.values()) {
            for (Group g : group.getGroupCollection()) {
                if (g.getName().equals(groupName)) {
                    return g;
                }
            }
        }
        return null;
    }

    /**
     * Add a specific system definition into a SNMP collection.
     * 
     * @param collection the target SNMP collection object.
     * @param systemDefName the system definition name.
     */
    private void addSystemDef(SnmpCollection collection, String systemDefName) {
        log().debug("addSystemDef: merging system defintion " + systemDefName + " into snmp-collection " + collection.getName());
        // Find System Definition
        SystemDef systemDef = getSystemDef(systemDefName);
        if (systemDef == null) {
            throwException("Can't find system definition " + systemDefName, null);
        }
        // Add System Definition to target SNMP collection
        if (contains(collection.getSystems().getSystemDefCollection(), systemDef)) {
            log().warn("addSystemDef: system definition " + systemDefName + " already exist on snmp collection " + collection.getName());
        } else {
            log().debug("addSystemDef: adding system definition " + systemDef.getName() + " to snmp-collection " + collection.getName());
            collection.getSystems().addSystemDef(systemDef);
            // Add Groups
            for (String groupName : systemDef.getCollect().getIncludeGroupCollection()) {
                Group group = getMibObjectGroup(groupName);
                if (group == null) {
                    log().warn("addSystemDef: group " + groupName + " does not exist on global container");
                } else {
                    if (contains(collection.getGroups().getGroupCollection(), group)) {
                        log().debug("addSystemDef: group " + groupName + " already exist on snmp collection " + collection.getName());
                    } else {
                        log().debug("addSystemDef: adding mib object group " + group.getName() + " to snmp-collection " + collection.getName());
                        collection.getGroups().addGroup(group);
                    }
                }
            }
        }
    }

    /**
     * Add all system definitions defined on a specific data collection group, into a SNMP collection.
     * 
     * @param collection the target SNMP collection object.
     * @param dataCollectionGroupName the data collection group name.
     * @param excludeList the list of regular expression to exclude certain system definitions.
     */
    private void addDatacollectionGroup(SnmpCollection collection, String dataCollectionGroupName, List<String> excludeList) {
        DatacollectionGroup group = externalGroupsMap.get(dataCollectionGroupName);
        if (group == null) {
            throwException("Group " + dataCollectionGroupName + " does not exist.", null);
        }
        log().debug("addDatacollectionGroup: adding all definitions from group " + group.getName() + " to snmp-collection " + collection.getName());
        for (SystemDef systemDef : group.getSystemDefCollection()) {
            String sysDef = systemDef.getName();
            if (shouldAdd(sysDef, excludeList)) {
                addSystemDef(collection, sysDef);
            }
        }
    }

    private boolean shouldAdd(String sysDef, List<String> excludeList) {
        if (excludeList != null) {
            for (String re : excludeList) {
                try {
                    final Pattern p = Pattern.compile(re);
                    final Matcher m = p.matcher(sysDef);
                    if (m.matches()) {
                        log().info("addDatacollectionGroup: system definition " + sysDef + " is blacklisted by filter " + re);
                        return false;
                    }
                } catch (PatternSyntaxException e) {
                    log().warn("the regular expression " + re + " is invalid: " + e.getMessage(), e);
                }
            }
        }
        return true;
    }

    private void throwException(String msg, Exception e) {
        if (e == null) {
            log().error(msg);
            throw new DataAccessResourceFailureException(msg);
        } else {
            log().error(msg, e);
            throw new DataAccessResourceFailureException(msg, e);            
        }
    }
    
    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

}
