//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2006 Aug 15: Formatting, rganize imports, use generics for Collections, resource type support, validate instance values. - dj@opennms.org
// 2003 Oct 20: Added minval and maxval parameters to mibObj for RRDs.
// 2003 Jan 31: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//
// Tab Size = 8
//

package org.opennms.netmgt.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.ConfigFileConstants;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.Groups;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.config.datacollection.SystemDef;
import org.opennms.netmgt.config.datacollection.SystemDefChoice;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.netmgt.model.RrdRepository;

/**
 * This class is the main respository for SNMP data collection configuration
 * information used by the SNMP service monitor. When this class is loaded it
 * reads the snmp data collection configuration into memory.
 * 
 * <strong>Note: </strong>Users of this class should make sure the
 * <em>init()</em> is called before calling any other method to ensure the
 * config is loaded before accessing other convenience methods.
 * 
 * @author <a href="mailto:weave@oculan.com">Weave </a>
 * @author <a href="http://www.opennms.org/">OpenNMS </a>
 * 
 */
public final class DataCollectionConfigFactory implements DataCollectionConfig {
	private static final Pattern s_digitsPattern = Pattern.compile("\\d+"); 
	
    /**
     * The singleton instance of this factory
     */
    private static DataCollectionConfig m_singleton = null;

    /**
     * The config class loaded from the config file
     */
    private DatacollectionConfig m_config;

    /**
     * This member is set to true if the configuration file has been loaded.
     */
    private static boolean m_loaded = false;

    /**
     * Map of group maps indexed by SNMP collection name.
     */
    private Map<String,Map<String,Group>> m_collectionGroupMap;

    /**
     * Map of SnmpCollection objects indexed by data collection name
     */
    private Map<String,SnmpCollection> m_collectionMap;

	private Map<String, ResourceType> m_configuredResourceTypes;

    /**
     * Private constructor
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    private DataCollectionConfigFactory(String configFile) throws IOException, MarshalException, ValidationException {
        InputStream stream = new FileInputStream(configFile);
        try {
            m_config = CastorUtils.unmarshal(DatacollectionConfig.class, stream);        
            setUpFactory();
        } finally {
            if (stream != null) {
                IOUtils.closeQuietly(stream);
            }
        }
    }
    
    @Deprecated
    public DataCollectionConfigFactory(Reader rdr) throws MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(DatacollectionConfig.class, rdr);        
        setUpFactory();
    }

    public DataCollectionConfigFactory(InputStream stream) throws MarshalException, ValidationException {
        m_config = CastorUtils.unmarshal(DatacollectionConfig.class, stream);
        setUpFactory();
    }

    protected void setUpFactory() {
        buildCollectionMap();
        processConfiguredResourceTypes();
        validateResourceTypes();
    }
    
    public Map<String,ResourceType> getConfiguredResourceTypes() {
    	return m_configuredResourceTypes;
    }
    
    public void processConfiguredResourceTypes() {
    	Map<String,ResourceType> map = new HashMap<String,ResourceType>();
    	Collection<SnmpCollection> snmpCollections = getSnmpCollections();
		for (SnmpCollection collection : snmpCollections) {
			Collection<ResourceType> resourceTypes = getResourceTypesForCollection(collection);
			for (ResourceType resourceType : resourceTypes) {
				map.put(resourceType.getName(), resourceType);
			}
		}
    	m_configuredResourceTypes = map;
    }

    private List<ResourceType> getResourceTypesForCollection(SnmpCollection collection) {
        return (List<ResourceType>) collection.getResourceTypeCollection();
    }

    private List<SnmpCollection> getSnmpCollections() {
        return (List<SnmpCollection>) m_config.getSnmpCollectionCollection();
    }
    
    private static String join(String separator, Collection<String> collection) {
    	StringBuffer buffer = new StringBuffer();
    	
    	Iterator<String> iterator = collection.iterator();
    	if (iterator.hasNext()) {
    		buffer.append(iterator.next());
    	}
    	
    	while (iterator.hasNext()) {
    		buffer.append(separator);
    		buffer.append(iterator.next());
    	}
    	
    	return buffer.toString();
    }

    private void validateResourceTypes() {
    	Set<String> allowedResourceTypes = getConfiguredResourceTypes().keySet();
    	String configuredString;
    	if (allowedResourceTypes.size() == 0) {
    		configuredString = "(none)";
    	} else {
    		configuredString = join(", ", allowedResourceTypes);
    	}
    	
    	String allowableValues = "any positive number, 'ifIndex', or any of the configured resourceTypes: " + configuredString;
		Collection<SnmpCollection> snmpCollections = getSnmpCollections();
		for (SnmpCollection collection : snmpCollections) {
			Collection<Group> groups = getGroupsForCollection(collection);
			for (Group group : groups) {
				Collection<MibObj> mibObjs = getMibObjsForGroup(group);
				for (MibObj mibObj : mibObjs) {
					String instance = mibObj.getInstance();
					if (instance == null) {
						continue;
					}
					if (s_digitsPattern.matcher(instance).matches()) {
						continue;
					}
					if (MibObject.INSTANCE_IFINDEX.equals(instance)) {
						continue;
					}
					if (allowedResourceTypes.contains(instance)) {
						continue;
					}
					// XXX this should be a better exception
					throw new IllegalArgumentException("instance '" + instance + "' invalid in mibObj definition for OID '" + mibObj.getOid() + "' in collection '" + collection.getName() + "' for group '" + group.getName() + "'.  Allowable instance values: " + allowableValues);
				}
			}
		}
	}

    private List<MibObj> getMibObjsForGroup(Group group) {
        return (List<MibObj>) group.getMibObjCollection();
    }

    private List<Group> getGroupsForCollection(SnmpCollection collection) {
        return (List<Group>) collection.getGroups().getGroupCollection();
    }

	public static void setInstance(DataCollectionConfig instance) {
        m_singleton = instance;
        m_loaded = true;
    }

    /**
     * Load the config from the default config file and create the singleton
     * instance of this factory.
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    public static synchronized void init() throws IOException, MarshalException, ValidationException {
        if (m_loaded) {
            // init already called - return
            // to reload, reload() will need to be called
            return;
        }

        File cfgFile = ConfigFileConstants.getFile(ConfigFileConstants.DATA_COLLECTION_CONF_FILE_NAME);

        ThreadCategory.getInstance(DataCollectionConfigFactory.class).debug("init: config file path: " + cfgFile.getPath());

        m_singleton = new DataCollectionConfigFactory(cfgFile.getPath());

        m_loaded = true;
    }

    /**
     * Reload the config from the default config file
     * 
     * @exception java.io.IOException
     *                Thrown if the specified config file cannot be read/loaded
     * @exception org.exolab.castor.xml.MarshalException
     *                Thrown if the file does not conform to the schema.
     * @exception org.exolab.castor.xml.ValidationException
     *                Thrown if the contents do not match the required schema.
     */
    public static synchronized void reload() throws IOException, MarshalException, ValidationException {
        m_singleton = null;
        m_loaded = false;

        init();
    }

    /**
     * Return the singleton instance of this factory.
     * 
     * @return The current factory instance.
     * 
     * @throws java.lang.IllegalStateException
     *             Thrown if the factory has not yet been initialized.
     */
    public static synchronized DataCollectionConfig getInstance() {
        if (!m_loaded)
            throw new IllegalStateException("The factory has not been initialized");

        return m_singleton;
    }

    /**
     * This method returns the list of MIB objects associated with a particular
     * system object id, IP address, and ifType for the specified collection.
     * 
     * @param cName
     *            name of the data collection from which to retrieve oid
     *            information.
     * @param aSysoid
     *            system object id to look up in the collection
     * @param anAddress
     *            IP address to look up in the collection
     * @param ifType
     *            Interface type (-1 indicates that only node-level objects
     *            should be retrieved.
     * 
     * @return a list of MIB objects
     */
    public List<MibObject> getMibObjectList(String cName, String aSysoid, String anAddress, int ifType) {
        if (log().isDebugEnabled())
            log().debug("getMibObjectList: collection: " + cName + " sysoid: " + aSysoid + " address: " + anAddress + " ifType: " + ifType);

        if (aSysoid == null) {
            if (log().isDebugEnabled())
                log().debug("getMibObjectList: aSysoid parameter is NULL...");
            return new ArrayList<MibObject>();
        }

        // Retrieve the appropriate Collection object
        // 
        SnmpCollection collection = m_collectionMap.get(cName);
        if (collection == null) {
            return new ArrayList<MibObject>();
        }

        // 
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
        //
        List<SystemDef> systemList = new ArrayList<SystemDef>();
        Enumeration<SystemDef> e = collection.getSystems().enumerateSystemDef();

        SystemDef system = null;
        while (e.hasMoreElements()) {
            system = e.nextElement();

            // Match on sysoid?
            boolean bMatchSysoid = false;

            // Retrieve sysoid for this SystemDef and/ set the isMask boolean.
            boolean isMask = false;
            String currSysoid = null;
            SystemDefChoice sysChoice = system.getSystemDefChoice();

            if (sysChoice.getSysoid() != null)
                currSysoid = sysChoice.getSysoid();
            else if (sysChoice.getSysoidMask() != null) {
                currSysoid = sysChoice.getSysoidMask();
                isMask = true;
            }

            if (currSysoid != null) {
                if (isMask) {
                    // SystemDef's sysoid is a mask, 'aSysoid' need only
                    // start with the sysoid mask in order to match
                    if (aSysoid.startsWith(currSysoid)) {
                        if (log().isDebugEnabled())
                            log().debug("getMibObjectList: includes sysoid " + aSysoid + " for system <name>: " + system.getName());
                        bMatchSysoid = true;
                    }
                } else {
                    // System's sysoid is not a mask, 'aSysoid' must
                    // match the sysoid exactly.
                    if (aSysoid.equals(currSysoid)) {
                        if (log().isDebugEnabled())
                            log().debug("getMibObjectList: includes sysoid " + aSysoid + " for system <name>: " + system.getName());
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

                    // If either Address list or Mask list exist then
                    // 'anAddress'
                    // must be included by one of them
                    if (addrList != null && addrList.size() > 0 || maskList != null && maskList.size() > 0)
                        bMatchIPAddress = false;

                    // First see if address is in list of specific addresses
                    if (addrList != null && addrList.size() > 0) {
                        if (addrList.contains(anAddress)) {
                            if (log().isDebugEnabled())
                                log().debug("getMibObjectList: addrList exists and does include IP address " + anAddress + " for system <name>: " + system.getName());
                            bMatchIPAddress = true;
                        }
                    }

                    // If still no match, see if address matches any of the
                    // masks
                    if (bMatchIPAddress == false) {

                        if (maskList != null && maskList.size() > 0) {
                            Iterator<String> iter = maskList.iterator();
                            while (iter.hasNext()) {
                                String currMask = iter.next();
                                if (anAddress.indexOf(currMask) == 0) {
                                    if (log().isDebugEnabled())
                                        log().debug("getMibObjectList: anAddress '" + anAddress + "' matches mask '" + currMask + "'");
                                    bMatchIPAddress = true;
                                    break;
                                }
                            }
                        }
                    }
                }
            }

            if (bMatchSysoid && bMatchIPAddress) {
                if (log().isDebugEnabled())
                    log().debug("getMibObjectList: MATCH!! adding system '" + system.getName() + "'");
                systemList.add(system);
            }
        }

        // Next build list of Mib objects to collect from the list of matching
        // SystemDefs
        List<MibObject> mibObjectList = new ArrayList<MibObject>();

        Iterator<SystemDef> i = systemList.iterator();
        while (i.hasNext()) {
            system = i.next();

            // Next process each of the SystemDef's groups
            List<String> groupList = system.getCollect().getIncludeGroupCollection();
            Iterator<String> j = groupList.iterator();
            while (j.hasNext()) {
                // Call processGroupName on each group within the SystemDef
                String grpName = j.next();
                processGroupName(cName, grpName, ifType, mibObjectList);
            }
        }

        return mibObjectList;
    }

	private Category log() {
		return ThreadCategory.getInstance(getClass());
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
    private void processGroupName(String cName, String groupName, int ifType, List<MibObject> mibObjectList) {
        Category log = log();

        // Using the collector name retrieve the group map
        Map<String, Group> groupMap = m_collectionGroupMap.get(cName);

        // Next use the groupName to access the Group object
        Group group = groupMap.get(groupName);

        // Verify that we have a valid Group object...generate
        // warning message if not...
        if (group == null) {
            log.warn("DataCollectionConfigFactory.processGroupName: unable to retrieve group information for group name '" + groupName + "': check DataCollection.xml file.");
            return;
        }

        if (log.isDebugEnabled()) {
            log.debug("processGroupName:  processing group: " + groupName + " groupIfType: " + group.getIfType() + " ifType: " + ifType);
        }

        // Process any sub-groups contained within this group
        List<String> groupNameList = group.getIncludeGroupCollection();
        Iterator<String> i = groupNameList.iterator();
        while (i.hasNext()) {
            // Recursive call to process sub-groups
            processGroupName(cName, i.next(), ifType, mibObjectList);
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
        String ifTypeStr = String.valueOf(ifType);
        String groupIfType = group.getIfType();

        boolean addGroupObjects = false;
        if (ifType == NODE_ATTRIBUTES) {
            if (groupIfType.equals("ignore")) {
                addGroupObjects = true;
            }
        } else {
            if (groupIfType.equals("all")) {
                addGroupObjects = true;
            } else if (groupIfType.equals("ignore")) {
                // Do nothing
            } else if (ifType == ALL_IF_ATTRIBUTES) {
                addGroupObjects = true;
            } else {
                // First determine if the group's ifType value contains
                // a single type value or a list of values. In the case
                // of a list the ifType values will be delimited by commas.
                boolean isList = false;
                if (groupIfType.indexOf(',') != -1)
                    isList = true;

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
                    if (ifTypeStr.equals(groupIfType))
                        addGroupObjects = true;
                } else {
                    int tmpIndex = groupIfType.indexOf(ifTypeStr);
                    while (tmpIndex != -1) {
                        groupIfType = groupIfType.substring(tmpIndex);

                        // get substring starting at tmpIndex to
                        // either the end of the groupIfType string
                        // or to the first comma after tmpIndex
                        int nextComma = groupIfType.indexOf(',');

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
                        if (nextComma == -1)
                            break;

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
            if (log.isDebugEnabled()) {
                log.debug("processGroupName: OIDs from group '" + group.getName() + ":" + group.getIfType() + "' are included for ifType: " + ifType);
            }
            List<MibObj> objectList = group.getMibObjCollection();
            processObjectList(groupName, groupIfType, objectList, mibObjectList);
        } else {
            if (log.isDebugEnabled())
                log.debug("processGroupName: OIDs from group '" + group.getName() + ":" + group.getIfType() + "' are excluded for ifType: " + ifType);
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
    private void processObjectList(String groupName, String groupIfType, List<MibObj> objectList, List<MibObject> mibObjectList) {
        Iterator<MibObj> i = objectList.iterator();
        while (i.hasNext()) {
            MibObj mibObj = i.next();

            // Create a MibObject from the castor MibObj
            MibObject aMibObject = new MibObject();
            aMibObject.setGroupName(groupName);
            aMibObject.setGroupIfType(groupIfType);
            aMibObject.setOid(mibObj.getOid());
            aMibObject.setAlias(mibObj.getAlias());
            aMibObject.setType(mibObj.getType());
            aMibObject.setInstance(mibObj.getInstance());
            aMibObject.setMaxval(mibObj.getMaxval());
            aMibObject.setMinval(mibObj.getMinval());
            
            ResourceType resourceType = getConfiguredResourceTypes().get(mibObj.getInstance());
            if (resourceType != null) {
            	aMibObject.setResourceType(resourceType);
            }

            // Add the MIB object provided it isn't already in the list
            if (!mibObjectList.contains(aMibObject)) {
                mibObjectList.add(aMibObject);
            }
        }
    }

    /**
     * Retrieves configured RRD step size.
     * 
     * @param cName
     *            Name of the data collection
     * 
     * @return RRD step size for the specified collection
     */
    public int getStep(String cName) {
        SnmpCollection collection = m_collectionMap.get(cName);
        if (collection != null)
            return collection.getRrd().getStep();
        else
            return -1;
    }

    /**
     * Retrieves configured list of RoundRobin Archive statements.
     * 
     * @param cName
     *            Name of the data collection
     * 
     * @return list of RRA strings.
     */
    public List<String> getRRAList(String cName) {
        SnmpCollection collection = m_collectionMap.get(cName);
        if (collection != null)
            return collection.getRrd().getRraCollection();
        else
            return null;

    }

    /**
     * Retrieves the configured value of the SNMP storage flag.
     * 
     * @param cName
     *            Name of the data collection
     * 
     * @return SNMP storage flag
     */
    public String getSnmpStorageFlag(String cName) {
        SnmpCollection collection = m_collectionMap.get(cName);
        if (collection != null)
            return collection.getSnmpStorageFlag();
        else
            return null;
    }

    public RrdRepository getRrdRepository(String collectionName) {
            RrdRepository repo = new RrdRepository();
            repo.setRrdBaseDir(new File(getRrdPath()));
            repo.setRraList(getRRAList(collectionName));
            repo.setStep(getStep(collectionName));
            repo.setHeartBeat((2 * getStep(collectionName)));
            return repo;
    }
    
    public String getRrdPath() {
        String rrdPath = m_config.getRrdRepository();
    	if (rrdPath == null) {
    		throw new RuntimeException("Configuration error, failed to "
    				+ "retrieve path to RRD repository.");
    	}
    
    	/*
    	 * TODO: make a path utils class that has the below in it strip the
    	 * File.separator char off of the end of the path.
    	 */
    	if (rrdPath.endsWith(File.separator)) {
    		rrdPath = rrdPath.substring(0, (rrdPath.length() - File.separator.length()));
    	}
        
        return rrdPath;
    }

    private void buildCollectionMap() {
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
        m_collectionMap = new HashMap<String,SnmpCollection>();
        m_collectionGroupMap = new HashMap<String,Map<String,Group>>();

        java.util.Collection<SnmpCollection> collections = m_config.getSnmpCollectionCollection();
        Iterator<SnmpCollection> citer = collections.iterator();
        while (citer.hasNext()) {
            SnmpCollection collection = citer.next();

            // Build group map for this collection
            Map<String,Group> groupMap = new HashMap<String,Group>();

            Groups groups = collection.getGroups();
            java.util.Collection<Group> groupList = groups.getGroupCollection();
            Iterator<Group> giter = groupList.iterator();
            while (giter.hasNext()) {
                Group group = giter.next();
                groupMap.put(group.getName(), group);
            }
            m_collectionGroupMap.put(collection.getName(), groupMap);
            m_collectionMap.put(collection.getName(), collection);
        }
    }

}
