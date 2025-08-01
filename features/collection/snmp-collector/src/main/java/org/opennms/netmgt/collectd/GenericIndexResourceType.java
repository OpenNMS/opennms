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
package org.opennms.netmgt.collectd;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.collection.api.PersistenceSelectorStrategy;
import org.opennms.netmgt.collection.api.StorageStrategy;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.util.Assert;

/**
 * <p>GenericIndexResourceType class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class GenericIndexResourceType extends ResourceType {
    private static final Logger LOG = LoggerFactory.getLogger(GenericIndexResourceType.class);
    private String m_name;
    private PersistenceSelectorStrategy m_persistenceSelectorStrategy;
    private StorageStrategy m_storageStrategy;
    private org.opennms.netmgt.config.datacollection.ResourceType m_resourceType;

    private Map<SnmpInstId, GenericIndexResource> m_resourceMap = new HashMap<SnmpInstId, GenericIndexResource>();

    /**
     * <p>Constructor for GenericIndexResourceType.</p>
     *
     * @param agent a {@link org.opennms.netmgt.collection.api.CollectionAgent} object.
     * @param snmpCollection a {@link org.opennms.netmgt.collectd.OnmsSnmpCollection} object.
     * @param resourceType a {@link org.opennms.netmgt.config.datacollection.ResourceType} object.
     */
    public GenericIndexResourceType(SnmpCollectionAgent agent, OnmsSnmpCollection snmpCollection, org.opennms.netmgt.config.datacollection.ResourceType resourceType) throws IllegalArgumentException {
        super(agent, snmpCollection);

        Assert.notNull(resourceType, "resourceType argument must not be null");
        
        m_name = resourceType.getName();
        instantiatePersistenceSelectorStrategy(resourceType.getPersistenceSelectorStrategy().getClazz());
        instantiateStorageStrategy(resourceType.getStorageStrategy().getClazz());
        m_storageStrategy.setParameters(resourceType.getStorageStrategy().getParameters());
        m_persistenceSelectorStrategy.setParameters(resourceType.getPersistenceSelectorStrategy().getParameters());
        m_resourceType = resourceType;
    }

    org.opennms.netmgt.config.datacollection.ResourceType getResourceType(){
        return m_resourceType;
    }

    private void instantiatePersistenceSelectorStrategy(String className) {
        Class<?> cinst;
        try {
            cinst = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ObjectRetrievalFailureException(PersistenceSelectorStrategy.class,
                    className, "Could not load class", e);
        }
        try {
            m_persistenceSelectorStrategy = (PersistenceSelectorStrategy) cinst.newInstance();
        } catch (InstantiationException e) {
            throw new ObjectRetrievalFailureException(PersistenceSelectorStrategy.class,
                    className, "Could not instantiate", e);
        } catch (IllegalAccessException e) {
            throw new ObjectRetrievalFailureException(PersistenceSelectorStrategy.class,
                    className, "Could not instantiate", e);
        }
    }

    private void instantiateStorageStrategy(String className) {
        Class<?> cinst;
        try {
            cinst = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ObjectRetrievalFailureException(StorageStrategy.class,
                    className, "Could not load class", e);
        }
        try {
            m_storageStrategy = (StorageStrategy) cinst.newInstance();
        } catch (InstantiationException e) {
            throw new ObjectRetrievalFailureException(StorageStrategy.class,
                    className, "Could not instantiate", e);
        } catch (IllegalAccessException e) {
            throw new ObjectRetrievalFailureException(StorageStrategy.class,
                    className, "Could not instantiate", e);
        }

        m_storageStrategy.setResourceTypeName(m_name);
        if (getAgent() != null)
            m_storageStrategy.setStorageStrategyService(getAgent());
    }

    /** {@inheritDoc} */
    @Override
    public SnmpCollectionResource findResource(SnmpInstId inst) {
        if (!m_resourceMap.containsKey(inst)) {
            m_resourceMap.put(inst, new GenericIndexResource(this, getName(), inst));
        }
        return m_resourceMap.get(inst);
    }

    /** {@inheritDoc} */
    @Override
    public SnmpCollectionResource findAliasedResource(SnmpInstId inst, String ifAlias) {
        // This is here for completeness but it should not get called from here.
        // findResource should be called instead
        LOG.debug("findAliasedResource: Should not get called from GenericIndexResourceType");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Collection<SnmpAttributeType> loadAttributeTypes() {
        return getCollection().getIndexedAttributeTypesForResourceType(getAgent(), this);
    }

    /** {@inheritDoc} */
    @Override
    public Collection<GenericIndexResource> getResources() {
        return m_resourceMap.values();
    }

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }

    /**
     * <p>getStorageStrategy</p>
     *
     * @return a {@link org.opennms.netmgt.collection.api.StorageStrategy} object.
     */
    public StorageStrategy getStorageStrategy() {
        return m_storageStrategy;
    }

    public PersistenceSelectorStrategy getPersistenceSelectorStrategy() {
        return m_persistenceSelectorStrategy;
    }

}
