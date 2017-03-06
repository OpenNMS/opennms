/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.collectd.wmi;

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.PersistenceSelectorStrategy;
import org.opennms.netmgt.collection.api.StorageStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.springframework.orm.ObjectRetrievalFailureException;

/**
 * The class WmiResourceType, used by WmiCollectionResource implementations.
 * @author <a href="mailto:david.schlenk@spanlink.com">David Schlenk</a>
 *
 */
public class WmiResourceType {
    private String m_resourceType;
    private PersistenceSelectorStrategy m_persistenceSelectorStrategy;
    private StorageStrategy m_storageStrategy;

    public WmiResourceType(CollectionAgent agent, ResourceType resourceType){
        m_resourceType = resourceType.getName();
        instantiatePersistenceSelector(resourceType.getPersistenceSelectorStrategy().getClazz());
        instantiateStorageStrategy(resourceType.getStorageStrategy().getClazz());
        m_storageStrategy.setParameters(resourceType.getStorageStrategy().getParameters());
        m_persistenceSelectorStrategy.setParameters(resourceType.getPersistenceSelectorStrategy().getParameters());
    }
    
    private void instantiateStorageStrategy(String className) {
        Class<?> cinst;
        try {
            cinst = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ObjectRetrievalFailureException(StorageStrategy.class, className, "Could not load class", e);
        }
        try {
            m_storageStrategy = (StorageStrategy) cinst.newInstance();
        } catch (InstantiationException e) {
            throw new ObjectRetrievalFailureException(StorageStrategy.class, className, "Could not instantiate", e);
        } catch (IllegalAccessException e) {
            throw new ObjectRetrievalFailureException(StorageStrategy.class, className, "Could not instantiate", e);
        }
        m_storageStrategy.setResourceTypeName(m_resourceType);
    }

    private void instantiatePersistenceSelector(String className) {
        Class<?> cinst;
        try {
            cinst = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ObjectRetrievalFailureException(PersistenceSelectorStrategy.class, className, "Could not load class", e);
        }
        try {
            m_persistenceSelectorStrategy = (PersistenceSelectorStrategy) cinst.newInstance();
        } catch (InstantiationException e) {
            throw new ObjectRetrievalFailureException(PersistenceSelectorStrategy.class, className, "Could not instantiate", e);
        } catch (IllegalAccessException e) {
            throw new ObjectRetrievalFailureException(PersistenceSelectorStrategy.class, className, "Could not instantiate", e);
        }        
    }

    /**
     * Gets the resource type name.
     *
     * @return the name
     */
    public String getName() {
        return m_resourceType;
    }

    /**
     * Gets the storage strategy.
     *
     * @return the storage strategy
     */
    public StorageStrategy getStorageStrategy() {
        return m_storageStrategy;
    }

    /**
     * Gets the persistence selector strategy.
     *
     * @return the persistence selector strategy
     */
    public PersistenceSelectorStrategy getPersistenceSelectorStrategy() {
        return m_persistenceSelectorStrategy;
    }
    
}
