
/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collectd;

import java.net.InetAddress;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.opennms.netmgt.config.DataCollectionConfigDao;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.config.MibObject;
import org.opennms.netmgt.config.collector.AttributeGroupType;
import org.opennms.netmgt.config.collector.CollectionResource;
import org.opennms.netmgt.config.collector.ServiceParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents SNMP collection data for a single collection period.
 * It is particularly used to create a CollectionSet for a specific
 * remote agent with {@link #createCollectionSet} and to provide
 * data to CollectionSet and other classes that are created during
 * collection.
 *
 * @author ranger
 * @version $Id: $
 */
public class OnmsSnmpCollection {
    
    private static final Logger LOG = LoggerFactory.getLogger(OnmsSnmpCollection.class);

    private ServiceParameters m_params;
    private NodeResourceType m_nodeResourceType;
    private IfResourceType m_ifResourceType;
    private IfAliasResourceType m_ifAliasResourceType;
    private Map<String, ResourceType> m_genericIndexResourceTypes;
    private DataCollectionConfigDao m_dataCollectionConfigDao;
    private List<SnmpAttributeType> m_nodeAttributeTypes;
    private List<SnmpAttributeType> m_indexedAttributeTypes;
    private List<SnmpAttributeType> m_aliasAttributeTypes;

    /**
     * <p>Constructor for OnmsSnmpCollection.</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @param params a {@link org.opennms.netmgt.config.collector.ServiceParameters} object.
     */
    public OnmsSnmpCollection(CollectionAgent agent, ServiceParameters params) {
        this(agent, params, null);
    }

    /**
     * <p>Constructor for OnmsSnmpCollection.</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @param params a {@link org.opennms.netmgt.config.collector.ServiceParameters} object.
     * @param config a {@link org.opennms.netmgt.config.DataCollectionConfigDao} object.
     */
    public OnmsSnmpCollection(CollectionAgent agent, ServiceParameters params, DataCollectionConfigDao config) {
        setDataCollectionConfigDao(config);

        m_params = params;

        if (Boolean.getBoolean("org.opennms.netmgt.collectd.OnmsSnmpCollection.loadResourceTypesInInit")) {
            getResourceTypes(agent);
        }
    }

    /**
     * <p>getServiceParameters</p>
     *
     * @return a {@link org.opennms.netmgt.config.collector.ServiceParameters} object.
     */
    public ServiceParameters getServiceParameters() {
        return m_params;
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_params.getCollectionName();
    }

    /**
     * <p>getSnmpPort</p>
     *
     * @param current a int.
     * @return a int.
     */
    public int getSnmpPort(int current) {
        return m_params.getSnmpPort(current);
    }

    /**
     * <p>getSnmpRetries</p>
     *
     * @param current a int.
     * @return a int.
     */
    public int getSnmpRetries(int current) {
        return m_params.getSnmpRetries(current);
    }

    /**
     * <p>getSnmpTimeout</p>
     *
     * @param current a int.
     * @return a int.
     */
    public int getSnmpTimeout(int current) {
        return m_params.getSnmpTimeout(current);
    }

    /**
     * <p>getSnmpReadCommunity</p>
     *
     * @param current a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getSnmpReadCommunity(String current) {
        return m_params.getSnmpReadCommunity(current);
    }

    /**
     * <p>getSnmpWriteCommunity</p>
     *
     * @param current a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getSnmpWriteCommunity(String current) {
        return m_params.getSnmpWriteCommunity(current);
    }

    /**
     * <p>getSnmpProxyFor</p>
     *
     * @param current a {@link java.net.InetAddress} object.
     * @return a {@link java.net.InetAddress} object.
     */
    public InetAddress getSnmpProxyFor(InetAddress current) {
        return m_params.getSnmpProxyFor(current);
    }

    /**
     * <p>getSnmpVersion</p>
     *
     * @param current a int.
     * @return a int.
     */
    public int getSnmpVersion(int current) {
        return m_params.getSnmpVersion(current);
    }

    /**
     * <p>getSnmpMaxVarsPerPdu</p>
     *
     * @param current a int.
     * @return a int.
     */
    public int getSnmpMaxVarsPerPdu(int current) {
        return m_params.getSnmpMaxVarsPerPdu(current);
    }

    /**
     * <p>getSnmpMaxRepetitions</p>
     *
     * @param current a int.
     * @return a int.
     */
    public int getSnmpMaxRepetitions(int current) {
        return m_params.getSnmpMaxRepetitions(current);
    }

    /**
     * <p>getSnmpMaxRequestSize</p>
     *
     * @param current a int.
     * @return a int.
     */
    public int getSnmpMaxRequestSize(int current) {
        return m_params.getSnmpMaxRequestSize(current);
    }

    /**
     * <p>getSnmpSecurityName</p>
     *
     * @param current a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getSnmpSecurityName(String current) {
        return m_params.getSnmpSecurityName(current);
    }

    /**
     * <p>getSnmpAuthPassPhrase</p>
     *
     * @param current a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getSnmpAuthPassPhrase(String current) {
        return m_params.getSnmpAuthPassPhrase(current);
    }

    /**
     * <p>getSnmpAuthProtocol</p>
     *
     * @param current a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getSnmpAuthProtocol(String current) {
        return m_params.getSnmpAuthProtocol(current);
    }

    /**
     * <p>getSnmpPrivPassPhrase</p>
     *
     * @param current a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getSnmpPrivPassPhrase(String current) {
        return m_params.getSnmpPrivPassPhrase(current);
    }

    /**
     * <p>getSnmpPrivProtocol</p>
     *
     * @param current a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String getSnmpPrivProtocol(String current) {
        return m_params.getSnmpPrivProtocol(current);
    }

    private DataCollectionConfigDao getDataCollectionConfigDao() {
        if (m_dataCollectionConfigDao == null) {
            setDataCollectionConfigDao(DataCollectionConfigFactory.getInstance());
        }
        return m_dataCollectionConfigDao;
    }

    /**
     * <p>setDataCollectionConfig</p>
     *
     * @param config a {@link org.opennms.netmgt.config.DataCollectionConfigDao} object.
     */
    public void setDataCollectionConfigDao(DataCollectionConfigDao config) {
        m_dataCollectionConfigDao = config;
    }

    /**
     * <p>getStorageFlag</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getStorageFlag() {
        String collectionName = getName();
        String storageFlag = getDataCollectionConfigDao().getSnmpStorageFlag(collectionName);
        if (storageFlag == null) {
            LOG.warn("getStorageFlag: Configuration error, failed to retrieve SNMP storage flag for collection: {}", collectionName);
            storageFlag = SnmpCollector.SNMP_STORAGE_PRIMARY;
        }
        return storageFlag;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * <p>createCollectionSet</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @return a {@link org.opennms.netmgt.collectd.SnmpCollectionSet} object.
     */
    public SnmpCollectionSet createCollectionSet(CollectionAgent agent) {
        return new SnmpCollectionSet(agent, this);
    }
    
    private List<SnmpAttributeType> getIndexedAttributeTypes(CollectionAgent agent) {
        if (m_indexedAttributeTypes == null) {
            m_indexedAttributeTypes = loadAttributeTypes(agent, DataCollectionConfigDao.ALL_IF_ATTRIBUTES);
        }
        return m_indexedAttributeTypes;
    }
    
    /**
     * <p>getIndexedAttributeTypesForResourceType</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @param resourceType a {@link org.opennms.netmgt.collectd.ResourceType} object.
     * @return a {@link java.util.List} object.
     */
    public List<SnmpAttributeType> getIndexedAttributeTypesForResourceType(CollectionAgent agent, ResourceType resourceType) {
        LinkedList<SnmpAttributeType> resAttrTypes = new LinkedList<SnmpAttributeType>();
        for(SnmpAttributeType attrType : getIndexedAttributeTypes(agent)) {
            if (attrType.getResourceType().equals(resourceType)) {
                resAttrTypes.add(attrType);
            }
        }
        return resAttrTypes;
    }

    /**
     * <p>getNodeAttributeTypes</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @return a {@link java.util.List} object.
     */
    public List<SnmpAttributeType> getNodeAttributeTypes(CollectionAgent agent) {
        if (m_nodeAttributeTypes == null) {
            m_nodeAttributeTypes = loadAttributeTypes(agent, DataCollectionConfigDao.NODE_ATTRIBUTES);
        }
        return m_nodeAttributeTypes;
    }

    /**
     * <p>loadAttributeTypes</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @param ifType a int.
     * @return a {@link java.util.List} object.
     */
    public List<SnmpAttributeType> loadAttributeTypes(CollectionAgent agent, int ifType) {
        String sysObjectId = agent.getSysObjectId();
        String hostAddress = agent.getHostAddress();
        List<MibObject> oidList = getDataCollectionConfigDao().getMibObjectList(getName(), sysObjectId, hostAddress, ifType);

        Map<String, AttributeGroupType> groupTypes = new HashMap<String, AttributeGroupType>();

        List<SnmpAttributeType> typeList = new LinkedList<SnmpAttributeType>();
        for (MibObject mibObject : oidList) {
            String instanceName = mibObject.getInstance();
            AttributeGroupType groupType = findGroup(groupTypes, mibObject);
            SnmpAttributeType attrType = SnmpAttributeType.create(getResourceType(agent, instanceName), getName(), mibObject, groupType);
            groupType.addAttributeType(attrType);
            typeList.add(attrType);
        }
        LOG.debug("getAttributeTypes({}, {}): {}", agent, ifType, typeList);
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

    /**
     * <p>getResourceType</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @param instanceName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.collectd.ResourceType} object.
     */
    public ResourceType getResourceType(CollectionAgent agent, String instanceName) {
        if (MibObject.INSTANCE_IFINDEX.equals(instanceName)) {
            return getIfResourceType(agent);
        } else if (getGenericIndexResourceType(agent, instanceName) != null) {
            return getGenericIndexResourceType(agent, instanceName);
        } else {
            return getNodeResourceType(agent);
        }
    }

    /**
     * <p>getNodeResourceType</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @return a {@link org.opennms.netmgt.collectd.NodeResourceType} object.
     */
    public NodeResourceType getNodeResourceType(CollectionAgent agent) {
        if (m_nodeResourceType == null)
            m_nodeResourceType = new NodeResourceType(agent, this);
        return m_nodeResourceType;
    }

    /**
     * <p>getIfResourceType</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @return a {@link org.opennms.netmgt.collectd.IfResourceType} object.
     */
    public IfResourceType getIfResourceType(CollectionAgent agent) {
        if (m_ifResourceType == null) {
            m_ifResourceType = new IfResourceType(agent, this);
        }
        return m_ifResourceType;
    }

    /**
     * <p>getIfAliasResourceType</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @return a {@link org.opennms.netmgt.collectd.IfAliasResourceType} object.
     */
    public IfAliasResourceType getIfAliasResourceType(CollectionAgent agent) {
        if (m_ifAliasResourceType == null) {
            m_ifAliasResourceType = new IfAliasResourceType(agent, this, m_params, getIfResourceType(agent));            
        }
        return m_ifAliasResourceType;

    }
    
    /**
     * <p>getGenericIndexResourceTypes</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @return a {@link java.util.Collection} object.
     */
    public Collection<ResourceType> getGenericIndexResourceTypes(CollectionAgent agent) {
        return Collections.unmodifiableCollection(getGenericIndexResourceTypeMap(agent).values());
    }

    private Map<String, ResourceType> getGenericIndexResourceTypeMap(CollectionAgent agent) {
        if (m_genericIndexResourceTypes == null) {
            Collection<org.opennms.netmgt.config.datacollection.ResourceType> configuredResourceTypes =
                getDataCollectionConfigDao().getConfiguredResourceTypes().values();
            Map<String,ResourceType> resourceTypes = new HashMap<String,ResourceType>();
            for (org.opennms.netmgt.config.datacollection.ResourceType configuredResourceType : configuredResourceTypes) {
                try {
                    resourceTypes.put(configuredResourceType.getName(), new GenericIndexResourceType(agent, this, configuredResourceType));
                } catch (IllegalArgumentException e) {
                    LOG.warn("Ignoring resource type {} ({}) because it is not properly configured.", configuredResourceType.getLabel(), configuredResourceType.getName());
                }
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

    /**
     * <p>getAttributeTypes</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @return a {@link java.util.Collection} object.
     */
    public Collection<SnmpAttributeType> getAttributeTypes(CollectionAgent agent) {
        HashSet<SnmpAttributeType> set = new HashSet<SnmpAttributeType>();
        for (ResourceType resourceType : getResourceTypes(agent)) {
            set.addAll(resourceType.getAttributeTypes());
        }
        return set;

    }

    /**
     * <p>getResources</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @return a {@link java.util.Collection} object.
     */
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

    /**
     * <p>loadAliasAttributeTypes</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @return a {@link java.util.List} object.
     */
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

    /**
     * <p>getAliasAttributeTypes</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @return a {@link java.util.List} object.
     */
    public List<SnmpAttributeType> getAliasAttributeTypes(CollectionAgent agent) {
        if (m_aliasAttributeTypes == null) {
            m_aliasAttributeTypes = loadAliasAttributeTypes(agent);
        }
        return m_aliasAttributeTypes;
    }


}
