/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.collection.support.builder;

import java.nio.file.Path;
import java.util.Objects;

import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.PersistenceSelectorStrategy;
import org.opennms.netmgt.collection.api.StorageStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.springframework.orm.ObjectRetrievalFailureException;

public class GenericTypeResource implements Resource {

    private final NodeLevelResource m_node;
    private final String m_instance;
    private final ResourceType m_resourceType;
    private final StorageStrategy m_storageStrategy;
    private final PersistenceSelectorStrategy m_persistenceSelectorStrategy;

    public GenericTypeResource(NodeLevelResource node, ResourceType resourceType, String instance) {
        m_node = Objects.requireNonNull(node, "node argument");
        m_instance = Objects.requireNonNull(instance, "instance argument");
        m_resourceType = Objects.requireNonNull(resourceType, "resourceType argument");
        m_storageStrategy = instantiateStorageStrategy(m_resourceType.getStorageStrategy().getClazz());
        m_storageStrategy.setParameters(m_resourceType.getStorageStrategy().getParameters());
        m_persistenceSelectorStrategy =  instantiatePersistenceSelector(m_resourceType.getPersistenceSelectorStrategy().getClazz());
        m_persistenceSelectorStrategy.setParameters(m_resourceType.getPersistenceSelectorStrategy().getParameters());
    }

    @Override
    public NodeLevelResource getParent() {
        return m_node;
    }

    @Override
    public String getInstance() {
        return m_instance;
    }

    @Override
    public String getLabel(CollectionResource resource) {
        return m_storageStrategy.getResourceNameFromIndex(resource);
    }

    @Override
    public Path getPath(CollectionResource resource) {
        return getStorageStrategy().getRelativePathForAttribute("", getStorageStrategy().getResourceNameFromIndex(resource));
    }

    public StorageStrategy getStorageStrategy() {
        return m_storageStrategy;
    }

    public PersistenceSelectorStrategy getPersistenceSelectorStrategy() {
        return m_persistenceSelectorStrategy;
    }

    private StorageStrategy instantiateStorageStrategy(String className) {
        Class<?> cinst;
        try {
            cinst = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ObjectRetrievalFailureException(StorageStrategy.class, className, "Could not load class", e);
        }
        try {
            StorageStrategy storageStrategy = (StorageStrategy) cinst.newInstance();
            storageStrategy.setResourceTypeName(m_resourceType.getName());
            return storageStrategy;
        } catch (InstantiationException e) {
            throw new ObjectRetrievalFailureException(StorageStrategy.class, className, "Could not instantiate", e);
        } catch (IllegalAccessException e) {
            throw new ObjectRetrievalFailureException(StorageStrategy.class, className, "Could not instantiate", e);
        }
    }

    private PersistenceSelectorStrategy instantiatePersistenceSelector(String className) {
        Class<?> cinst;
        try {
            cinst = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ObjectRetrievalFailureException(PersistenceSelectorStrategy.class, className, "Could not load class", e);
        }
        try {
            return (PersistenceSelectorStrategy) cinst.newInstance();
        } catch (InstantiationException e) {
            throw new ObjectRetrievalFailureException(PersistenceSelectorStrategy.class, className, "Could not instantiate", e);
        } catch (IllegalAccessException e) {
            throw new ObjectRetrievalFailureException(PersistenceSelectorStrategy.class, className, "Could not instantiate", e);
        }
    }

    @Override
    public String getTypeName() {
        return m_resourceType.getName();
    }

    @Override
    public String toString() {
        return String.format("GenericTypeResource[node=%s, resourceType=%s,"
                + "storageStrategy=%s, persistenceSelectorStrategy=%s",
                m_node, m_resourceType, m_storageStrategy, m_persistenceSelectorStrategy);
    }

}
