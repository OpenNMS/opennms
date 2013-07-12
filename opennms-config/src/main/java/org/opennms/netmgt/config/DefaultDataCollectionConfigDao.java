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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.utils.FileReloadContainer;
import org.opennms.core.xml.AbstractJaxbConfigDao;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.Groups;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.config.datacollection.SystemDef;
import org.opennms.netmgt.config.datacollection.SystemDefChoice;
import org.opennms.netmgt.config.datacollection.Systems;
import org.opennms.netmgt.model.RrdRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

/**
 * DefaultDataCollectionConfigDao
 * 
 * <p>This class is the main repository for SNMP data collection configuration
 * information used by the SNMP service monitor. When this class is loaded it
 * reads the SNNMP data collection configuration into memory.</p>
 *
 * @author <a href="mail:agalue@opennms.org">Alejandro Galue</a>
 */
public class DefaultDataCollectionConfigDao extends AbstractJaxbConfigDao<DatacollectionConfig, DatacollectionConfig> implements DataCollectionConfigDao {
    
    public static final Logger LOG = LoggerFactory.getLogger(DefaultDataCollectionConfigDao.class);
    
    private String m_configDirectory;

    // have we validated the config since last reloading?
    private boolean m_validated = false;
    private RuntimeException m_validationException = null;

    private List<String> dataCollectionGroups = new ArrayList<String>();

    public DefaultDataCollectionConfigDao() {
        super(DatacollectionConfig.class, "data-collection");
    }

    @Override
    protected DatacollectionConfig loadConfig(final Resource resource) {
        m_validated = false;
        m_validationException = null;
        return super.loadConfig(resource);
    }
    
    @Override
    protected DatacollectionConfig translateConfig(final DatacollectionConfig config) {
        final DataCollectionConfigParser parser = new DataCollectionConfigParser(getConfigDirectory());

        // Updating Configured Collections
        for (final SnmpCollection collection : config.getSnmpCollectionCollection()) {
            parser.parseCollection(collection);
        }

        // Create a special collection to hold all resource types, because they should be defined only once.
        final SnmpCollection resourceTypeCollection = new SnmpCollection();
        resourceTypeCollection.setName("__resource_type_collection");
        for (final ResourceType rt : parser.getAllResourceTypes()) {
            resourceTypeCollection.addResourceType(rt);
        }
        resourceTypeCollection.setGroups(new Groups());
        resourceTypeCollection.setSystems(new Systems());
        config.getSnmpCollectionCollection().add(0, resourceTypeCollection);
        dataCollectionGroups.clear();
        dataCollectionGroups.addAll(parser.getExternalGroupMap().keySet());
        return config;
    }

    public void setConfigDirectory(String configDirectory) {
        this.m_configDirectory = configDirectory;
    }

    public String getConfigDirectory() {
        if (m_configDirectory == null) {
            final StringBuffer sb = new StringBuffer(ConfigFileConstants.getHome());
            sb.append(File.separator);
            sb.append("etc");
            sb.append(File.separator);
            sb.append("datacollection");
            sb.append(File.separator);
            m_configDirectory = sb.toString();
        }
        return m_configDirectory;
    }

    @Override
    public String getSnmpStorageFlag(final String collectionName) {
        final SnmpCollection collection = getSnmpCollection(getContainer(), collectionName);
        return collection == null ? null : collection.getSnmpStorageFlag();
    }

    @Override
    public List<MibObject> getMibObjectList(final String cName, final String aSysoid, final String anAddress, final int ifType) {
        LOG.debug("getMibObjectList: collection: {} sysoid: {} address: {} ifType: {}", cName, aSysoid, anAddress, ifType);

        if (aSysoid == null) {
            LOG.debug("getMibObjectList: aSysoid parameter is NULL...");
            return new ArrayList<MibObject>();
        }

        // Retrieve the appropriate Collection object
        final SnmpCollection collection = getSnmpCollection(getContainer(), cName);
        if (collection == null) {
            return Collections.emptyList();
        }

        // First build a list of SystemDef objects which "match" the passed
        // sysoid and IP address parameters. The SystemDef object must match
        // on both the sysoid AND the IP address.
        //
        // SYSOID MATCH
        //
        // A SystemDef object's sysoid value may be a complete system object
        // identifier or it may be a mask (a partial sysoid).
        //
        // If the sysoid is not a mask, the 'aSysoid' string must equal the
        // sysoid value exactly in order to match.
        //
        // If the sysoid is a mask, the 'aSysoid' string need only start with
        // the sysoid mask value in order to match
        //
        // For example, a sysoid mask of ".1.3.6.1.4.1.11." would match any
        // Hewlett-Packard product which had this sysoid prefix (which should
        // include all of them).
        //
        // IPADDRESS MATCH
        //
        // In order to match on IP Address one of the following must be true:
        // 
        // The SystemDef's IP address list (ipList) must contain the 'anAddress'
        // parm (must be an exact match)
        //
        // OR
        //
        // The 'anAddress' parm must have the same prefix as one of the
        // SystemDef's IP address mask list (maskList) entries.
        //
        // NOTE: A SystemDef object which contains an empty IP list and
        // an empty Mask list matches ALL IP addresses (default is INCLUDE).

        final List<SystemDef> systemList = new ArrayList<SystemDef>();

        for (final SystemDef system : collection.getSystems().getSystemDefCollection()) {
            // Match on sysoid?
            boolean bMatchSysoid = false;

            // Retrieve sysoid for this SystemDef and/ set the isMask boolean.
            boolean isMask = false;
            String currSysoid = null;
            SystemDefChoice sysChoice = system.getSystemDefChoice();

            if (sysChoice.getSysoid() != null) {
                currSysoid = sysChoice.getSysoid();
            } else if (sysChoice.getSysoidMask() != null) {
                currSysoid = sysChoice.getSysoidMask();
                isMask = true;
            }

            if (currSysoid != null) {
                if (isMask) {
                    // SystemDef's sysoid is a mask, 'aSysoid' need only
                    // start with the sysoid mask in order to match
                    if (aSysoid.startsWith(currSysoid)) {
                        LOG.debug("getMibObjectList: includes sysoid {} for system <name>: {}", aSysoid, system.getName());
                        bMatchSysoid = true;
                    }
                } else {
                    // System's sysoid is not a mask, 'aSysoid' must
                    // match the sysoid exactly.
                    if (aSysoid.equals(currSysoid)) {
                        LOG.debug("getMibObjectList: includes sysoid {} for system <name>: {}", aSysoid, system.getName());
                        bMatchSysoid = true;
                    }
                }
            }

            // Match on ipAddress?
            boolean bMatchIPAddress = true; // default is INCLUDE
            if (bMatchSysoid == true) {
                if (anAddress != null) {
                    List<String> addrList = null;
                    List<String> maskList = null;
                    if (system.getIpList() != null) {
                        addrList = system.getIpList().getIpAddrCollection();
                        maskList = system.getIpList().getIpAddrMaskCollection();
                    }

                    // If either Address list or Mask list exist then 'anAddress'
                    // must be included by one of them
                    if (addrList != null && addrList.size() > 0 || maskList != null && maskList.size() > 0) {
                        bMatchIPAddress = false;
                    }

                    // First see if address is in list of specific addresses
                    if (addrList != null && addrList.size() > 0) {
                        if (addrList.contains(anAddress)) {
                            LOG.debug("getMibObjectList: addrList exists and does include IP address {} for system <name>: {}", anAddress, system.getName());
                            bMatchIPAddress = true;
                        }
                    }

                    // If still no match, see if address matches any of the masks
                    if (bMatchIPAddress == false) {

                        if (maskList != null && maskList.size() > 0) {
                            for (final String currMask : maskList) {
                                if (anAddress.indexOf(currMask) == 0) {
                                    LOG.debug("getMibObjectList: anAddress '{}' matches mask '{}'", anAddress, currMask);
                                    bMatchIPAddress = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if (bMatchSysoid && bMatchIPAddress) {
                LOG.debug("getMibObjectList: MATCH!! adding system '{}'", system.getName());
                systemList.add(system);
            }
        }

        // Next build list of Mib objects to collect from the list of matching SystemDefs
        final List<MibObject> mibObjectList = new ArrayList<MibObject>();

        for (final SystemDef system : systemList) {
            // Next process each of the SystemDef's groups
            for (final String grpName : system.getCollect().getIncludeGroupCollection()) {
                processGroupName(cName, grpName, ifType, mibObjectList);
            }
        }

        return mibObjectList;
    }

    @Override
    public Map<String, ResourceType> getConfiguredResourceTypes() {
        final Map<String,ResourceType> map = new HashMap<String,ResourceType>();

        final Collection<SnmpCollection> snmpCollections = getContainer().getObject().getSnmpCollectionCollection();
        for (final SnmpCollection collection : snmpCollections) {
            for (final ResourceType resourceType : collection.getResourceTypeCollection()) {
                map.put(resourceType.getName(), resourceType);
            }
        }

        // FIXME: I guarantee there's a cleaner way to do this, but I didn't want to refactor everything
        // that calls this just to optimize out validation.
        if (!m_validated) {
            try {
                validateResourceTypes(getContainer(), map.keySet());
            } catch (final RuntimeException e) {
                m_validationException = e;
                throw e;
            }
        } else {
            if (m_validationException != null) {
                throw m_validationException;
            }
        }
        return Collections.unmodifiableMap(map);
    }

    @Override
    public RrdRepository getRrdRepository(final String collectionName) {
        final RrdRepository repo = new RrdRepository();
        repo.setRrdBaseDir(new File(getRrdPath()));
        repo.setRraList(getRRAList(collectionName));
        repo.setStep(getStep(collectionName));
        repo.setHeartBeat((2 * getStep(collectionName)));
        return repo;
    }

    @Override
    public int getStep(final String collectionName) {
        final SnmpCollection collection = getSnmpCollection(getContainer(), collectionName);
        return collection == null ? -1 : collection.getRrd().getStep();
    }

    @Override
    public List<String> getRRAList(final String collectionName) {
        final SnmpCollection collection = getSnmpCollection(getContainer(), collectionName);
        return collection == null ? null : collection.getRrd().getRraCollection();
    }

    @Override
    public String getRrdPath() {
        final String rrdPath = getContainer().getObject().getRrdRepository();
        if (rrdPath == null) {
            throw new RuntimeException("Configuration error, failed to retrieve path to RRD repository.");
        }

        /*
         * TODO: make a path utils class that has the below in it strip the
         * File.separator char off of the end of the path.
         */
        if (rrdPath.endsWith(File.separator)) {
            return rrdPath.substring(0, (rrdPath.length() - File.separator.length()));
        }
        return rrdPath;
    }

    /* Private Methods */

    private static SnmpCollection getSnmpCollection(final FileReloadContainer<DatacollectionConfig> container, final String collectionName) {
        for (final SnmpCollection collection : container.getObject().getSnmpCollection()) {
            if (collection.getName().equals(collectionName)) return collection;
        }
        return null;
    }

    /**
     * Private utility method used by the getMibObjectList() method. This method
     * takes a group name and a list of MibObject objects as arguments and adds
     * all of the MibObjects associated with the group to the object list. If
     * the passed group consists of any additional sub-groups, then this method
     * will be called recursively for each sub-group until the entire
     * log.debug("processGroupName: adding MIB objects from group: " +
     * groupName); group is processed.
     * 
     * @param cName
     *            Collection name
     * @param groupName
     *            Name of the group to process
     * @param ifType
     *            Interface type
     * @param mibObjectList
     *            List of MibObject objects being built.
     */
    private void processGroupName(final String cName, final String groupName, final int ifType, final List<MibObject> mibObjectList) {
        // Using the collector name retrieve the group map
        final Map<String, Group> groupMap = getCollectionGroupMap(getContainer()).get(cName);

        // Next use the groupName to access the Group object
        final Group group = groupMap.get(groupName);

        // Verify that we have a valid Group object...generate
        // warning message if not...
        if (group == null) {
            LOG.warn("DataCollectionConfigFactory.processGroupName: unable to retrieve group information for group name '{}': check DataCollection.xml file.", groupName);
            return;
        }

        LOG.debug("processGroupName:  processing group: {} groupIfType: {} ifType: {}", groupName, group.getIfType(), ifType);

        // Process any sub-groups contained within this group
        for (final String includeGroup : group.getIncludeGroupCollection()) {
            processGroupName(cName, includeGroup, ifType, mibObjectList);
        }

        // Add this group's objects to the object list provided
        // that the group's ifType string does not exclude the
        // provided ifType parm.
        //
        // ifType parm of -1 indicates that only node-level
        // objects are to be added
        //
        // Any other ifType parm value must be compared with
        // the group's ifType value to verify that they match
        // (if group's ifType is "all" then the objects will
        // automatically be added.
        final String ifTypeStr = String.valueOf(ifType);
        String groupIfType = group.getIfType();

        boolean addGroupObjects = false;
        if (ifType == NODE_ATTRIBUTES) {
            if (groupIfType.equals("ignore")) {
                addGroupObjects = true;
            }
        } else {
            if (groupIfType.equals("all")) {
                addGroupObjects = true;
            } else if ("ignore".equals(groupIfType)) {
                // Do nothing
            } else if (ifType == ALL_IF_ATTRIBUTES) {
                addGroupObjects = true;
            } else {
                // First determine if the group's ifType value contains
                // a single type value or a list of values. In the case
                // of a list the ifType values will be delimited by commas.
                boolean isList = false;
                if (groupIfType.indexOf(',') != -1) isList = true;

                // Next compare the provided ifType parameter with the
                // group's ifType value to determine if the group's OIDs
                // should be added to the MIB object list.
                //
                // If the group ifType value is a single value then only
                // a simple comparison is needed to see if there is an
                // exact match.
                //
                // In the case of the group ifType value being a list
                // of ifType values it is more complicated...each comma
                // delimited substring which starts with the provided
                // ifType parm must be extracted and compared until an
                // EXACT match is found..
                if (!isList) {
                    if (ifTypeStr.equals(groupIfType)) addGroupObjects = true;
                } else {
                    int tmpIndex = groupIfType.indexOf(ifTypeStr);
                    while (tmpIndex != -1) {
                        groupIfType = groupIfType.substring(tmpIndex);

                        // get substring starting at tmpIndex to
                        // either the end of the groupIfType string
                        // or to the first comma after tmpIndex
                        final int nextComma = groupIfType.indexOf(',');

                        String parsedType = null;
                        if (nextComma == -1) // No comma, this is last type
                            // value
                        {
                            parsedType = groupIfType;
                        } else // Found comma
                        {
                            parsedType = groupIfType.substring(0, nextComma);
                        }
                        if (ifTypeStr.equals(parsedType)) {
                            addGroupObjects = true;
                            break;
                        }

                        // No more commas indicates no more ifType values to
                        // compare...we're done
                        if (nextComma == -1) break;

                        // Get next substring and reset tmpIndex to
                        // once again point to the first occurrence of
                        // the ifType string parm.
                        groupIfType = groupIfType.substring(nextComma + 1);
                        tmpIndex = groupIfType.indexOf(ifTypeStr);
                    }
                }
            }
        }

        if (addGroupObjects) {
            LOG.debug("processGroupName: OIDs from group '{}:{}' are included for ifType: {}", group.getName(), group.getIfType(), ifType);
            processObjectList(groupName, groupIfType, group.getMibObjCollection(), mibObjectList);
        } else {
            LOG.debug("processGroupName: OIDs from group '{}:{}' are excluded for ifType: {}", group.getName(), group.getIfType(), ifType);
        }
    }

    /**
     * Takes a list of castor generated MibObj objects iterates over them
     * creating corresponding MibObject objects and adding them to the supplied
     * MibObject list.
     * @param groupName TODO
     * @param groupIfType TODO
     * @param objectList
     *            List of MibObject objects parsed from
     *            'datacollection-config.xml'
     * @param mibObjectList
     *            List of MibObject objects currently being built
     */
    private void processObjectList(final String groupName, final String groupIfType, final List<MibObj> objectList, final List<MibObject> mibObjectList) {
        for (final MibObj mibObj : objectList) {
            // Create a MibObject from the castor MibObj
            final MibObject aMibObject = new MibObject();
            aMibObject.setGroupName(groupName);
            aMibObject.setGroupIfType(groupIfType);
            aMibObject.setOid(mibObj.getOid());
            aMibObject.setAlias(mibObj.getAlias());
            aMibObject.setType(mibObj.getType());
            aMibObject.setInstance(mibObj.getInstance());
            aMibObject.setMaxval(mibObj.getMaxval());
            aMibObject.setMinval(mibObj.getMinval());

            final ResourceType resourceType = getConfiguredResourceTypes().get(mibObj.getInstance());
            if (resourceType != null) {
                aMibObject.setResourceType(resourceType);
            }

            // Add the MIB object provided it isn't already in the list
            if (!mibObjectList.contains(aMibObject)) {
                mibObjectList.add(aMibObject);
            }
        }
    }

    private static Map<String,Map<String,Group>> getCollectionGroupMap(FileReloadContainer<DatacollectionConfig> container) {
        // Build collection map which is a hash map of Collection
        // objects indexed by collection name...also build
        // collection group map which is a hash map indexed
        // by collection name with a hash map as the value
        // containing a map of the collections's group names
        // to the Group object containing all the information
        // for that group. So the associations are:
        //
        // CollectionMap
        // collectionName -> Collection
        //
        // CollectionGroupMap
        // collectionName -> groupMap
        // 
        // GroupMap
        // groupMapName -> Group
        //
        // This is parsed and built at initialization for
        // faster processing at run-timne.
        // 
        final Map<String,Map<String,Group>> collectionGroupMap = new HashMap<String,Map<String,Group>>();

        for (final SnmpCollection collection : container.getObject().getSnmpCollectionCollection()) {
            // Build group map for this collection
            final Map<String,Group> groupMap = new HashMap<String,Group>();

            final Groups groups = collection.getGroups();
            if (groups != null) {
                for (final Group group : groups.getGroupCollection()) {
                    groupMap.put(group.getName(), group);
                }
            }
            collectionGroupMap.put(collection.getName(), groupMap);
        }
        return Collections.unmodifiableMap(collectionGroupMap);
    }

    private static void validateResourceTypes(final FileReloadContainer<DatacollectionConfig> container, final Set<String> allowedResourceTypes) {
        final String configuredString;
        if (allowedResourceTypes.size() == 0) {
            configuredString = "(none)";
        } else {
            configuredString = StringUtils.join(allowedResourceTypes, ", ");
        }

        final String allowableValues = "any positive number, 'ifIndex', or any of the configured resourceTypes: " + configuredString;
        for (final SnmpCollection collection : container.getObject().getSnmpCollectionCollection()) {
            final Groups groups = collection.getGroups();
            if (groups != null) {
				for (final Group group : groups.getGroupCollection()) {
	                for (final MibObj mibObj : group.getMibObjCollection()) {
	                    final String instance = mibObj.getInstance();
	                    if (instance == null)                            continue;
                        if (MibObject.INSTANCE_IFINDEX.equals(instance)) continue;
                        if (allowedResourceTypes.contains(instance))     continue;
	                    try {
	                        // Check to see if the value is a non-negative integer
	                        if (Integer.parseInt(instance.trim()) >= 0) {
	                            continue;
	                        }
	                    } catch (NumberFormatException e) {}

	                    // XXX this should be a better exception
	                    throw new IllegalArgumentException("instance '" + instance + "' invalid in mibObj definition for OID '" + mibObj.getOid() + "' in collection '" + collection.getName() + "' for group '" + group.getName() + "'.  Allowable instance values: " + allowableValues);
	                }
				}
            }
        }
    }

    @Override
    public DatacollectionConfig getRootDataCollection() {
        return getContainer().getObject();
    }
    
    @Override
    public List<String> getAvailableDataCollectionGroups() {
        return dataCollectionGroups;
    }

    @Override
    public List<String> getAvailableSystemDefs() {
        List<String> systemDefs = new ArrayList<String>();
        for (final SnmpCollection collection : getContainer().getObject().getSnmpCollectionCollection()) {
            if (collection.getSystems() != null) {
                for (final SystemDef systemDef : collection.getSystems().getSystemDefCollection()) {
                    systemDefs.add(systemDef.getName());
                }
            }
        }
        return systemDefs;
    }

    @Override
    public List<String> getAvailableMibGroups() {
        List<String> groups = new ArrayList<String>();
        for (final SnmpCollection collection : getContainer().getObject().getSnmpCollectionCollection()) {
            if (collection.getGroups() != null) {
                for (final Group group : collection.getGroups().getGroupCollection()) {
                    groups.add(group.getName());
                }
            }
        }
        return groups;
    }

}
