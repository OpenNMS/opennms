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

import java.util.Objects;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.opennms.netmgt.collection.adapters.GenericTypeResourceAdapter;
import org.opennms.netmgt.collection.api.CollectionResource;
import org.opennms.netmgt.collection.api.PersistenceSelectorStrategy;
import org.opennms.netmgt.collection.api.ResourceType;
import org.opennms.netmgt.collection.api.StorageStrategy;
import org.opennms.netmgt.model.ResourcePath;
import org.springframework.orm.ObjectRetrievalFailureException;

@XmlJavaTypeAdapter(GenericTypeResourceAdapter.class)
public class GenericTypeResource extends DeferredGenericTypeResource {

    private final ResourceType m_resourceType;
    private final StorageStrategy m_storageStrategy;
    private final PersistenceSelectorStrategy m_persistenceSelectorStrategy;

    public GenericTypeResource(NodeLevelResource node, ResourceType resourceType, String instance) {
        super(node, Objects.requireNonNull(resourceType, "resourceType argument").getName(), instance);
        m_resourceType = Objects.requireNonNull(resourceType, "resourceType argument");
        m_storageStrategy = instantiateStorageStrategy(resourceType.getStorageStrategy().getClazz(), resourceType.getName());
        m_storageStrategy.setParameters(resourceType.getStorageStrategy().getParameters());
        m_persistenceSelectorStrategy =  instantiatePersistenceSelector(resourceType.getPersistenceSelectorStrategy().getClazz());
        m_persistenceSelectorStrategy.setParameters(resourceType.getPersistenceSelectorStrategy().getParameters());
    }

    protected static String sanitizeInstance(String instance) {
        return instance.replaceAll("\\s+", "_").replaceAll(":", "_").replaceAll("\\\\", "_").replaceAll("[\\[\\]]", "_");
    }

    public ResourceType getResourceType() {
        return m_resourceType;
    }

    @Override
    public String getLabel(CollectionResource resource) {
        return getStorageStrategy().getResourceNameFromIndex(resource);
    }

    @Override
    public ResourcePath getPath(CollectionResource resource) {
        return getStorageStrategy().getRelativePathForAttribute(ResourcePath.get(), getStorageStrategy().getResourceNameFromIndex(resource));
    }

    @Override
    public Resource resolve() {
        return this;
    }

    public StorageStrategy getStorageStrategy() {
        return m_storageStrategy;
    }

    public PersistenceSelectorStrategy getPersistenceSelectorStrategy() {
        return m_persistenceSelectorStrategy;
    }

    private StorageStrategy instantiateStorageStrategy(String className, String resourceTypeName) {
        Class<?> cinst;
        try {
            cinst = Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new ObjectRetrievalFailureException(StorageStrategy.class, className, "Could not load class", e);
        }
        try {
            StorageStrategy storageStrategy = (StorageStrategy) cinst.newInstance();
            storageStrategy.setResourceTypeName(resourceTypeName);
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
        return String.format("GenericTypeResource[node=%s, instance=%s, resourceType=%s,"
                + "storageStrategy=%s, persistenceSelectorStrategy=%s]",
                getParent(), getInstance(), m_resourceType, m_storageStrategy, m_persistenceSelectorStrategy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getParent(), getInstance(), m_resourceType, getTimestamp());
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof GenericTypeResource)) {
            return false;
        }
        GenericTypeResource other = (GenericTypeResource) obj;
        return super.equals(other)
                && Objects.equals(this.m_resourceType, other.m_resourceType);
    }


}
