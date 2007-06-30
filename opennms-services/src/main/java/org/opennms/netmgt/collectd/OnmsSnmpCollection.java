
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

import java.util.Collection;
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
import org.opennms.netmgt.model.OnmsIpInterface.CollectionType;

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
    private int m_maxVarsPerPdu;
    private DataCollectionConfig m_dataCollectionConfig;

    public OnmsSnmpCollection(CollectionAgent agent, ServiceParameters params) {
        this(agent, params, null);
    }

    public OnmsSnmpCollection(CollectionAgent agent, ServiceParameters params, DataCollectionConfig config) {
        // Need to set this first before determineMaxVarsPerPdu is called
        m_dataCollectionConfig = config;
        
        m_params = params;
        m_maxVarsPerPdu = determineMaxVarsPerPdu(agent);
    }
    
    public ServiceParameters getServiceParameters() {
        return m_params;
    }

    public String getName() {
        return m_params.getCollectionName();
    }

    public int getSnmpPort() {
        return m_params.getSnmpPort();
    }

    public Category log() {
        return ThreadCategory.getInstance(getClass());
    }

    int getMaxVarsPerPdu() {
        return m_maxVarsPerPdu;
    }

    private int determineMaxVarsPerPdu(CollectionAgent agent) {
        // Retrieve configured value for max number of vars per PDU
        int maxVarsPerPdu = getDataCollectionConfig().getMaxVarsPerPdu(getName());
        if (maxVarsPerPdu == 0) {
            log().info("determineMaxVarsPerPdu: using agent's configured value: "
                       + agent.getMaxVarsPerPdu());
            maxVarsPerPdu = agent.getMaxVarsPerPdu();
        } else {
            log().info("determineMaxVarsPerPdu: using data collection configured value: "
                       + maxVarsPerPdu);
        }
        return maxVarsPerPdu;
    }

    public DataCollectionConfig getDataCollectionConfig() {
        if (m_dataCollectionConfig == null) {
            initializeDataCollectionConfig();
        }
        return m_dataCollectionConfig;
    }

    public void setDataCollectionConfig(DataCollectionConfig config) {
        m_dataCollectionConfig = config;
    }
    
    private void initializeDataCollectionConfig() {
        setDataCollectionConfig(DataCollectionConfigFactory.getInstance());
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

    public CollectionSet createCollectionSet(CollectionAgent agent) {
        return new CollectionSet(agent, this);
    }


    public Collection<AttributeType> getAttributeTypes(CollectionAgent agent, int ifType) {
        String sysObjectId = agent.getSysObjectId();
        String hostAddress = agent.getHostAddress();
        List<MibObject> oidList = getDataCollectionConfig().getMibObjectList(getName(), sysObjectId, hostAddress, ifType);

        Map<String, AttributeGroupType> groupTypes = new HashMap<String, AttributeGroupType>();

        List<AttributeType> typeList = new LinkedList<AttributeType>();
        for (MibObject mibObject : oidList) {
            String instanceName = mibObject.getInstance();
            AttributeGroupType groupType = getGroup(groupTypes, mibObject);
            AttributeType attrType = AttributeType.create(getResourceType(agent, instanceName), getName(), mibObject, groupType);
            groupType.addAttributeType(attrType);
            typeList.add(attrType);
        }
        log().debug("getAttributeTypes(" + agent + ", " + ifType + "): " + typeList);
        return typeList;
    }

    private AttributeGroupType getGroup(Map<String, AttributeGroupType> groupTypes, MibObject mibObject) {
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

    private Map<String, ResourceType> getGenericIndexResourceTypes(CollectionAgent agent) {
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
        return getGenericIndexResourceTypes(agent).get(name);
    }

    private Collection<ResourceType> getResourceTypes(CollectionAgent agent) {
        HashSet<ResourceType> set = new HashSet<ResourceType>(3);
        set.add(getNodeResourceType(agent));
        set.add(getIfResourceType(agent));
        set.add(getIfAliasResourceType(agent));
        set.addAll(getGenericIndexResourceTypes(agent).values());
        return set;
    }

    public Collection<AttributeType> getAttributeTypes(CollectionAgent agent) {
        HashSet<AttributeType> set = new HashSet<AttributeType>();
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

    CollectionType getMinimumCollectionType() {
        if (getStorageFlag().equals(SnmpCollector.SNMP_STORAGE_PRIMARY)) {
            return CollectionType.PRIMARY;
        }
        if (getStorageFlag().equals(SnmpCollector.SNMP_STORAGE_SELECT)) {
            return CollectionType.COLLECT;
        }

        return CollectionType.NO_COLLECT;
    }


}
