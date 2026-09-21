/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.config.api;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.opennms.netmgt.config.datacollection.DatacollectionConfig;
import org.opennms.netmgt.config.datacollection.Group;
import org.opennms.netmgt.config.datacollection.Groups;
import org.opennms.netmgt.config.datacollection.MibObj;
import org.opennms.netmgt.config.datacollection.MibObjProperty;
import org.opennms.netmgt.config.datacollection.MibObject;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.SnmpCollection;
import org.opennms.netmgt.config.datacollection.SystemDef;
import org.opennms.netmgt.config.datacollection.SystemDefChoice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shared utility methods for querying a DatacollectionConfig object tree.
 * Extracted from DefaultDataCollectionConfigDao so the same logic can be
 * used by both the XML-backed and DB-backed implementations.
 */
public final class DataCollectionConfigLookupUtils {

    private static final Logger LOG = LoggerFactory.getLogger(DataCollectionConfigLookupUtils.class);

    private static final String IF_TYPE_ALL = "all";
    private static final String IF_TYPE_IGNORE = "ignore";

    private DataCollectionConfigLookupUtils() {
    }

    /**
     * Check if a SystemDef matches the given sysoid and IP address.
     */
    public static boolean systemDefMatches(final SystemDef system, final String aSysoid, final String anAddress) {
        boolean bMatchSysoid = false;
        boolean isMask = false;
        String currSysoid = null;
        final SystemDefChoice sysChoice = system.getSystemDefChoice();

        if (sysChoice.getSysoid() != null) {
            currSysoid = sysChoice.getSysoid();
        } else if (sysChoice.getSysoidMask() != null) {
            currSysoid = sysChoice.getSysoidMask();
            isMask = true;
        }

        if (currSysoid != null) {
            if (isMask) {
                if (aSysoid.startsWith(currSysoid)) {
                    bMatchSysoid = true;
                }
            } else {
                if (aSysoid.equals(currSysoid)) {
                    bMatchSysoid = true;
                }
            }
        }

        boolean bMatchIPAddress = true;
        if (bMatchSysoid) {
            if (anAddress != null) {
                List<String> addrList = null;
                List<String> maskList = null;
                if (system.getIpList() != null) {
                    addrList = system.getIpList().getIpAddresses();
                    maskList = system.getIpList().getIpAddressMasks();
                }

                if (addrList != null && addrList.size() > 0 || maskList != null && maskList.size() > 0) {
                    bMatchIPAddress = false;
                }

                if (addrList != null && addrList.size() > 0) {
                    if (addrList.contains(anAddress)) {
                        bMatchIPAddress = true;
                    }
                }

                if (!bMatchIPAddress) {
                    if (maskList != null && maskList.size() > 0) {
                        for (final String currMask : maskList) {
                            if (anAddress.indexOf(currMask) == 0) {
                                bMatchIPAddress = true;
                                break;
                            }
                        }
                    }
                }
            }
        }

        return bMatchSysoid && bMatchIPAddress;
    }

    /**
     * Recursively resolve a group name to its MibObjects, applying ifType filtering.
     */
    public static void processGroupName(final Map<String, Group> groupMap,
                                        final String groupName,
                                        final int ifType,
                                        final List<MibObject> mibObjectList,
                                        final Map<String, ResourceType> resourceTypes) {
        processGroupName(groupMap, groupName, ifType, mibObjectList, resourceTypes, new HashSet<>());
    }

    private static void processGroupName(final Map<String, Group> groupMap,
                                         final String groupName,
                                         final int ifType,
                                         final List<MibObject> mibObjectList,
                                         final Map<String, ResourceType> resourceTypes,
                                         final Set<String> visited) {
        if (!visited.add(groupName)) {
            LOG.warn("processGroupName: cycle detected at group '{}'; skipping to prevent infinite recursion.", groupName);
            return;
        }
        final Group group = groupMap.get(groupName);
        if (group == null) {
            LOG.warn("processGroupName: unable to retrieve group '{}': check DataCollection config.", groupName);
            return;
        }

        // Process sub-groups
        for (final String includeGroup : group.getIncludeGroups()) {
            processGroupName(groupMap, includeGroup, ifType, mibObjectList, resourceTypes, visited);
        }

        final String ifTypeStr = String.valueOf(ifType);
        String groupIfType = group.getIfType();
        if (groupIfType == null) {
            // DB column is nullable and the XSD doesn't always require ifType.
            // Treat missing as "ignore" (skip group) rather than NPE on indexOf.
            return;
        }

        boolean addGroupObjects = false;
        if (ifType == DataCollectionConfigDao.NODE_ATTRIBUTES) {
            if (IF_TYPE_IGNORE.equals(groupIfType)) {
                addGroupObjects = true;
            }
        } else {
            if (IF_TYPE_ALL.equals(groupIfType)) {
                addGroupObjects = true;
            } else if (IF_TYPE_IGNORE.equals(groupIfType)) {
                // Do nothing
            } else if (ifType == DataCollectionConfigDao.ALL_IF_ATTRIBUTES) {
                addGroupObjects = true;
            } else {
                boolean isList = groupIfType.indexOf(',') != -1;
                if (!isList) {
                    if (ifTypeStr.equals(groupIfType)) addGroupObjects = true;
                } else {
                    int tmpIndex = groupIfType.indexOf(ifTypeStr);
                    while (tmpIndex != -1) {
                        groupIfType = groupIfType.substring(tmpIndex);
                        final int nextComma = groupIfType.indexOf(',');
                        String parsedType = nextComma == -1 ? groupIfType : groupIfType.substring(0, nextComma);
                        if (ifTypeStr.equals(parsedType)) {
                            addGroupObjects = true;
                            break;
                        }
                        if (nextComma == -1) break;
                        groupIfType = groupIfType.substring(nextComma + 1);
                        tmpIndex = groupIfType.indexOf(ifTypeStr);
                    }
                }
            }
        }

        if (addGroupObjects) {
            processObjectList(groupName, group.getIfType(), group.getMibObjs(), mibObjectList, resourceTypes);
        }
    }

    /**
     * Recursively resolve a group name to its MibObjProperties.
     */
    public static void processGroupForProperties(final Map<String, Group> groupMap,
                                                  final String groupName,
                                                  final List<MibObjProperty> mibObjProperties) {
        processGroupForProperties(groupMap, groupName, mibObjProperties, new HashSet<>());
    }

    private static void processGroupForProperties(final Map<String, Group> groupMap,
                                                  final String groupName,
                                                  final List<MibObjProperty> mibObjProperties,
                                                  final Set<String> visited) {
        if (!visited.add(groupName)) {
            LOG.warn("processGroupForProperties: cycle detected at group '{}'; skipping to prevent infinite recursion.", groupName);
            return;
        }
        final Group group = groupMap.get(groupName);
        if (group == null) {
            LOG.warn("processGroupForProperties: unable to retrieve group '{}': check DataCollection config.", groupName);
            return;
        }
        for (final String includeGroup : group.getIncludeGroups()) {
            processGroupForProperties(groupMap, includeGroup, mibObjProperties, visited);
        }
        group.getProperties().forEach(p -> p.setGroupName(groupName));
        mibObjProperties.addAll(group.getProperties());
    }

    /**
     * Convert MibObj list to MibObject list, resolving resource types.
     */
    public static void processObjectList(final String groupName,
                                         final String groupIfType,
                                         final List<MibObj> objectList,
                                         final List<MibObject> mibObjectList,
                                         final Map<String, ResourceType> resourceTypes) {
        for (final MibObj mibObj : objectList) {
            final MibObject aMibObject = new MibObject();
            aMibObject.setGroupName(groupName);
            aMibObject.setGroupIfType(groupIfType);
            aMibObject.setOid(mibObj.getOid());
            aMibObject.setAlias(mibObj.getAlias());
            aMibObject.setType(mibObj.getType());
            aMibObject.setInstance(mibObj.getInstance());
            aMibObject.setMaxval(mibObj.getMaxval());
            aMibObject.setMinval(mibObj.getMinval());

            final ResourceType resourceType = resourceTypes.get(mibObj.getInstance());
            if (resourceType != null) {
                aMibObject.setResourceType(resourceType);
            }

            if (!mibObjectList.contains(aMibObject)) {
                mibObjectList.add(aMibObject);
            }
        }
    }

    /**
     * Build the collection → group map from a DatacollectionConfig.
     */
    public static Map<String, Map<String, Group>> buildCollectionGroupMap(final DatacollectionConfig config) {
        final Map<String, Map<String, Group>> collectionGroupMap = new HashMap<>();
        for (final SnmpCollection collection : config.getSnmpCollections()) {
            final Map<String, Group> groupMap = new HashMap<>();
            final Groups groups = collection.getGroups();
            if (groups != null) {
                for (final Group group : groups.getGroups()) {
                    groupMap.put(group.getName(), group);
                }
            }
            collectionGroupMap.put(collection.getName(), groupMap);
        }
        return Collections.unmodifiableMap(collectionGroupMap);
    }

    /**
     * Validate that all MibObj instances reference valid resource types.
     */
    public static void validateResourceTypes(final Collection<SnmpCollection> snmpCollections,
                                             final Set<String> allowedResourceTypes) {
        final String configuredString = allowedResourceTypes.isEmpty()
                ? "(none)"
                : StringUtils.join(allowedResourceTypes, ", ");
        final String allowableValues = "any positive number, 'ifIndex', or any of the configured resourceTypes: " + configuredString;

        for (final SnmpCollection collection : snmpCollections) {
            final Groups groups = collection.getGroups();
            if (groups != null) {
                for (final Group group : groups.getGroups()) {
                    for (final MibObj mibObj : group.getMibObjs()) {
                        final String instance = mibObj.getInstance();
                        if (instance == null) continue;
                        if (MibObject.INSTANCE_IFINDEX.equals(instance)) continue;
                        if (allowedResourceTypes.contains(instance)) continue;
                        try {
                            if (Integer.parseInt(instance.trim()) >= 0) continue;
                        } catch (NumberFormatException e) { }
                        throw new IllegalArgumentException("instance '" + instance + "' invalid in mibObj definition for OID '"
                                + mibObj.getOid() + "' in collection '" + collection.getName() + "' for group '"
                                + group.getName() + "'.  Allowable instance values: " + allowableValues);
                    }
                }
            }
        }
    }

    /**
     * Find a named SnmpCollection in a DatacollectionConfig.
     */
    public static SnmpCollection findSnmpCollection(final DatacollectionConfig config, final String collectionName) {
        if (config == null) return null;
        for (final SnmpCollection collection : config.getSnmpCollections()) {
            if (collection.getName().equals(collectionName)) return collection;
        }
        return null;
    }
}
