
//This file is part of the OpenNMS(R) Application.

//OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
//OpenNMS(R) is a derivative work, containing both original code, included code and modified
//code that was published under the GNU General Public License. Copyrights for modified 
//and included code are below.

//OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.

//Modifications:
//2006 Aug 15: Javadocs, generic index resource type support, use generics for collections

//Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.

//This program is free software; you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation; either version 2 of the License, or
//(at your option) any later version.

//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU General Public License for more details.

//You should have received a copy of the GNU General Public License
//along with this program; if not, write to the Free Software
//Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

//For more information contact:
//OpenNMS Licensing       <license@opennms.org>
//http://www.opennms.org/
//http://www.opennms.com/


package org.opennms.netmgt.collectd;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.DataCollectionConfig;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.MibObject;

/**
 * Represents SNMP collection data for a single collection period.
 * It is particularly used to create a CollectionSet for a specific
 * remote agent with {@link #createCollectionSet} and to provide
 * data to CollectionSet and other classes that are created during
 * collection.
 */
public class OnmsSnmpCollection {

    private ServiceParameters m_params;
    private NodeResourceType m_nodeResourceType;
    private IfResourceType m_ifResourceType;
    private IfAliasResourceType m_ifAliasResourceType;
    private Map<String, ResourceType> m_genericIndexResourceTypes;
    private DataCollectionConfig m_dataCollectionConfig;
    private List<SnmpAttributeType> m_nodeAttributeTypes;
    private List<SnmpAttributeType> m_indexedAttributeTypes;
    private List<SnmpAttributeType> m_aliasAttributeTypes;

    public OnmsSnmpCollection(CollectionAgent agent, ServiceParameters params) {
        this(agent, params, null);
    }

    public OnmsSnmpCollection(CollectionAgent agent, ServiceParameters params, DataCollectionConfig config) {
        setDataCollectionConfig(config);

        m_params = params;

        if (Boolean.getBoolean("org.opennms.netmgt.collectd.OnmsSnmpCollection.loadResourceTypesInInit")) {
            getResourceTypes(agent);
        }
    }

    public ServiceParameters getServiceParameters() {
        return m_params;
    }

    public String getName() {
        return m_params.getCollectionName();
    }

    public int getSnmpPort(int current) {
        return m_params.getSnmpPort(current);
    }

    public int getSnmpRetries(int current) {
        return m_params.getSnmpRetries(current);
    }

    public int getSnmpTimeout(int current) {
        return m_params.getSnmpTimeout(current);
    }

    public String getSnmpReadCommunity(String current) {
        return m_params.getSnmpReadCommunity(current);
    }

    public String getSnmpWriteCommunity(String current) {
        return m_params.getSnmpWriteCommunity(current);
    }

    public InetAddress getSnmpProxyFor(InetAddress current) {
        return m_params.getSnmpProxyFor(current);
    }

    public int getSnmpVersion(int current) {
        return m_params.getSnmpVersion(current);
    }

    public int getSnmpMaxVarsPerPdu(int current) {
        return m_params.getSnmpMaxVarsPerPdu(current);
    }

    public int getSnmpMaxRepetitions(int current) {
        return m_params.getSnmpMaxRepetitions(current);
    }

    public int getSnmpMaxRequestSize(int current) {
        return m_params.getSnmpMaxRequestSize(current);
    }

    public String getSnmpSecurityName(String current) {
        return m_params.getSnmpSecurityName(current);
    }

    public String getSnmpAuthPassPhrase(String current) {
        return m_params.getSnmpAuthPassPhrase(current);
    }

    public String getSnmpAuthProtocol(String current) {
        return m_params.getSnmpAuthProtocol(current);
    }

    public String getSnmpPrivPassPhrase(String current) {
        return m_params.getSnmpPrivPassPhrase(current);
    }

    public String getSnmpPrivProtocol(String current) {
        return m_params.getSnmpPrivProtocol(current);
    }

    public ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

    private DataCollectionConfig getDataCollectionConfig() {
        if (m_dataCollectionConfig == null) {
            setDataCollectionConfig(DataCollectionConfigFactory.getInstance());
        }
        return m_dataCollectionConfig;
    }

    public void setDataCollectionConfig(DataCollectionConfig config) {
        m_dataCollectionConfig = config;
    }

    public String getStorageFlag() {
        String collectionName = getName();
        String storageFlag = getDataCollectionConfig().getSnmpStorageFlag(collectionName);
        if (storageFlag == null) {
            log().warn("getStorageFlag: Configuration error, failed to "
                    + "retrieve SNMP storage flag for collection: "
                    + collectionName);
            storageFlag = SnmpCollector.SNMP_STORAGE_PRIMARY;
        }
        return storageFlag;
    }

    public String toString() {
        return getName();
    }

    public SnmpCollectionSet createCollectionSet(CollectionAgent agent) {
        return new SnmpCollectionSet(agent, this);
    }
    
    private List<SnmpAttributeType> getIndexedAttributeTypes(CollectionAgent agent) {
        if (m_indexedAttributeTypes == null) {
            m_indexedAttributeTypes = loadAttributeTypes(agent, DataCollectionConfig.ALL_IF_ATTRIBUTES);
        }
        return m_indexedAttributeTypes;
    }
    
    public List<SnmpAttributeType> getIndexedAttributeTypesForResourceType(CollectionAgent agent, ResourceType resourceType) {
        LinkedList<SnmpAttributeType> resAttrTypes = new LinkedList<SnmpAttributeType>();
        for(SnmpAttributeType attrType : getIndexedAttributeTypes(agent)) {
            if (attrType.getResourceType().equals(resourceType)) {
                resAttrTypes.add(attrType);
            }
        }
        return resAttrTypes;
    }

    public List<SnmpAttributeType> getNodeAttributeTypes(CollectionAgent agent) {
        if (m_nodeAttributeTypes == null) {
            m_nodeAttributeTypes = loadAttributeTypes(agent, DataCollectionConfig.NODE_ATTRIBUTES);
        }
        return m_nodeAttributeTypes;
    }

    public List<SnmpAttributeType> loadAttributeTypes(CollectionAgent agent, int ifType) {
        String sysObjectId = agent.getSysObjectId();
        String hostAddress = agent.getHostAddress();
        List<MibObject> oidList = getDataCollectionConfig().getMibObjectList(getName(), sysObjectId, hostAddress, ifType);

        Map<String, AttributeGroupType> groupTypes = new HashMap<String, AttributeGroupType>();

        List<SnmpAttributeType> typeList = new LinkedList<SnmpAttributeType>();
        for (MibObject mibObject : oidList) {
            String instanceName = mibObject.getInstance();
            AttributeGroupType groupType = findGroup(groupTypes, mibObject);
            SnmpAttributeType attrType = SnmpAttributeType.create(getResourceType(agent, instanceName), getName(), mibObject, groupType);
            groupType.addAttributeType(attrType);
            typeList.add(attrType);
        }
        log().debug("getAttributeTypes(" + agent + ", " + ifType + "): " + typeList);
        return typeList;
    }

    private AttributeGroupType findGroup(Map<String, AttributeGroupType> groupTypes, MibObject mibObject) {
        AttributeGroupType groupType = groupTypes.get(mibObject.getGroupName());
        if (groupType == null) {
            groupType = new AttributeGroupType(mibObject.getGroupName(), mibObject.getGroupIfType());
            groupTypes.put(mibObject.getGroupName(), groupType);
        }
        return groupType;
    }

    public ResourceType getResourceType(CollectionAgent agent, String instanceName) {
        if (MibObject.INSTANCE_IFINDEX.equals(instanceName)) {
            return getIfResourceType(agent);
        } else if (getGenericIndexResourceType(agent, instanceName) != null) {
            return getGenericIndexResourceType(agent, instanceName);
        } else {
            return getNodeResourceType(agent);
        }
    }

    public NodeResourceType getNodeResourceType(CollectionAgent agent) {
        if (m_nodeResourceType == null)
            m_nodeResourceType = new NodeResourceType(agent, this);
        return m_nodeResourceType;
    }

    public IfResourceType getIfResourceType(CollectionAgent agent) {
        if (m_ifResourceType == null) {
            m_ifResourceType = new IfResourceType(agent, this);
        }
        return m_ifResourceType;
    }

    public IfAliasResourceType getIfAliasResourceType(CollectionAgent agent) {
        if (m_ifAliasResourceType == null) {
            m_ifAliasResourceType = new IfAliasResourceType(agent, this, m_params, getIfResourceType(agent));            
        }
        return m_ifAliasResourceType;

    }
    
    public Collection<ResourceType> getGenericIndexResourceTypes(CollectionAgent agent) {
        return Collections.unmodifiableCollection(getGenericIndexResourceTypeMap(agent).values());
    }

    private Map<String, ResourceType> getGenericIndexResourceTypeMap(CollectionAgent agent) {
        if (m_genericIndexResourceTypes == null) {
            Collection<org.opennms.netmgt.config.datacollection.ResourceType> configuredResourceTypes =
                getDataCollectionConfig().getConfiguredResourceTypes().values();
            Map<String,ResourceType> resourceTypes = new HashMap<String,ResourceType>();
            for (org.opennms.netmgt.config.datacollection.ResourceType configuredResourceType : configuredResourceTypes) {
                resourceTypes.put(configuredResourceType.getName(), new GenericIndexResourceType(agent, this, configuredResourceType));
            }
            m_genericIndexResourceTypes = resourceTypes;
        }
        return m_genericIndexResourceTypes;
    }
    
    private ResourceType getGenericIndexResourceType(CollectionAgent agent, String name) {
        return getGenericIndexResourceTypeMap(agent).get(name);
    }

    private Collection<ResourceType> getResourceTypes(CollectionAgent agent) {
        HashSet<ResourceType> set = new HashSet<ResourceType>(3);
        set.add(getNodeResourceType(agent));
        set.add(getIfResourceType(agent));
        set.add(getIfAliasResourceType(agent));
        set.addAll(getGenericIndexResourceTypeMap(agent).values());
        return set;
    }

    public Collection<SnmpAttributeType> getAttributeTypes(CollectionAgent agent) {
        HashSet<SnmpAttributeType> set = new HashSet<SnmpAttributeType>();
        for (ResourceType resourceType : getResourceTypes(agent)) {
            set.addAll(resourceType.getAttributeTypes());
        }
        return set;

    }

    public Collection<? extends CollectionResource> getResources(CollectionAgent agent) {
        LinkedList<CollectionResource> resources = new LinkedList<CollectionResource>();
        for (ResourceType resourceType : getResourceTypes(agent)) {
            resources.addAll(resourceType.getResources());
        }
        return resources;
    }

    boolean isSelectCollectionOnly() {
        if (getStorageFlag().equals(SnmpCollector.SNMP_STORAGE_PRIMARY) || getStorageFlag().equals(SnmpCollector.SNMP_STORAGE_SELECT)) {
            return true;
        }

        return false;
    }

    public List<SnmpAttributeType> loadAliasAttributeTypes(CollectionAgent agent) {
        IfAliasResourceType resType = getIfAliasResourceType(agent);
        MibObject ifAliasMibObject = new MibObject();
        ifAliasMibObject.setOid(".1.3.6.1.2.1.31.1.1.1.18");
        ifAliasMibObject.setAlias("ifAlias");
        ifAliasMibObject.setType("string");
        ifAliasMibObject.setInstance("ifIndex");
        
        ifAliasMibObject.setGroupName("aliasedResource");
        ifAliasMibObject.setGroupIfType("all");
    
        AttributeGroupType groupType = new AttributeGroupType(ifAliasMibObject.getGroupName(), ifAliasMibObject.getGroupIfType());
    
        SnmpAttributeType type = SnmpAttributeType.create(resType, resType.getCollectionName(), ifAliasMibObject, groupType);
        return Collections.singletonList(type);
    }

    public List<SnmpAttributeType> getAliasAttributeTypes(CollectionAgent agent) {
        if (m_aliasAttributeTypes == null) {
            m_aliasAttributeTypes = loadAliasAttributeTypes(agent);
        }
        return m_aliasAttributeTypes;
    }


}
