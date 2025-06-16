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
