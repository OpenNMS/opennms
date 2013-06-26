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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.config.StorageStrategy;
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

    private Map<SnmpInstId, GenericIndexResource> m_resourceMap = new HashMap<SnmpInstId, GenericIndexResource>();

    /**
     * <p>Constructor for GenericIndexResourceType.</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @param snmpCollection a {@link org.opennms.netmgt.collectd.OnmsSnmpCollection} object.
     * @param resourceType a {@link org.opennms.netmgt.config.datacollection.ResourceType} object.
     */
    public GenericIndexResourceType(CollectionAgent agent, OnmsSnmpCollection snmpCollection, org.opennms.netmgt.config.datacollection.ResourceType resourceType) throws IllegalArgumentException {
        super(agent, snmpCollection);

        Assert.notNull(resourceType, "resourceType argument must not be null");
        
        m_name = resourceType.getName();
        instantiatePersistenceSelectorStrategy(resourceType.getPersistenceSelectorStrategy().getClazz());
        instantiateStorageStrategy(resourceType.getStorageStrategy().getClazz());
        m_storageStrategy.setParameters(resourceType.getStorageStrategy().getParameterCollection());
        m_persistenceSelectorStrategy.setParameters(resourceType.getPersistenceSelectorStrategy().getParameterCollection());
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
     * @return a {@link org.opennms.netmgt.config.StorageStrategy} object.
     */
    public StorageStrategy getStorageStrategy() {
        return m_storageStrategy;
    }

    public PersistenceSelectorStrategy getPersistenceSelectorStrategy() {
        return m_persistenceSelectorStrategy;
    }

}
