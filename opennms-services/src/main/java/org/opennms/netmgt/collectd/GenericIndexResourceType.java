/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2006 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * 2007 May 14: Patch for bug 1850 from Alejandro Galue. - dj@opennms.org
 * 2006 Aug 15: Created this file. - dj@opennms.org
 * 
 * Copyright (C) 2006 The OpenNMS Group.  All rights reserved.
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
package org.opennms.netmgt.collectd;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.opennms.netmgt.config.StorageStrategy;
import org.opennms.netmgt.snmp.SnmpInstId;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.util.Assert;

/**
 * <p>GenericIndexResourceType class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class GenericIndexResourceType extends ResourceType {
    private String m_name;
//  private String m_persistenceSelectorStrategy;
    private StorageStrategy m_storageStrategy;

    private Map<SnmpInstId, GenericIndexResource> m_resourceMap = new HashMap<SnmpInstId, GenericIndexResource>();

    /**
     * <p>Constructor for GenericIndexResourceType.</p>
     *
     * @param agent a {@link org.opennms.netmgt.collectd.CollectionAgent} object.
     * @param snmpCollection a {@link org.opennms.netmgt.collectd.OnmsSnmpCollection} object.
     * @param resourceType a {@link org.opennms.netmgt.config.datacollection.ResourceType} object.
     */
    public GenericIndexResourceType(CollectionAgent agent, OnmsSnmpCollection snmpCollection, org.opennms.netmgt.config.datacollection.ResourceType resourceType) {
        super(agent, snmpCollection);

        Assert.notNull(resourceType, "resourceType argument must not be null");
        
        m_name = resourceType.getName();
        instantiatePersistenceSelectorStrategy(resourceType.getPersistenceSelectorStrategy().getClazz());
        instantiateStorageStrategy(resourceType.getStorageStrategy().getClazz());
        m_storageStrategy.setParameters(resourceType.getStorageStrategy().getParameterCollection());
    }

    private void instantiatePersistenceSelectorStrategy(String className) {
        // TODO write me
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
    public SnmpCollectionResource findAliasedResource(SnmpInstId inst, String ifAlias) {
        // This is here for completeness but it should not get called from here.
        // findResource should be called instead
        log().debug("findAliasedResource: Should not get called from GenericIndexResourceType");
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

}
