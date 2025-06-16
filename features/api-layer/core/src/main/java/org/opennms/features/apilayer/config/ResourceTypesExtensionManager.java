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
package org.opennms.features.apilayer.config;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.integration.api.v1.config.datacollection.ResourceTypesExtension;
import org.opennms.netmgt.collection.api.Parameter;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.ResourceTypes;
import org.opennms.netmgt.config.datacollection.StorageStrategy;

public class ResourceTypesExtensionManager extends ConfigExtensionManager<ResourceTypesExtension, ResourceTypes> {


    public ResourceTypesExtensionManager() {
        super(ResourceTypes.class, new ResourceTypes());
    }

    @Override
    protected ResourceTypes getConfigForExtensions(Set<ResourceTypesExtension> extensions) {
        ResourceTypes resourceTypes = new ResourceTypes();
        List<ResourceType> resourceTypeList = extensions.stream()
                .flatMap(r -> toResourceTypeList(r.getResourceTypes()).stream())
                .collect(Collectors.toList());
        resourceTypes.setResourceTypes(resourceTypeList);
        return  resourceTypes;
    }

    @Override
    protected void triggerReload() {
        // no need to trigger reload for ResourceTypesDao
    }

    public static List<ResourceType> toResourceTypeList(List<org.opennms.integration.api.v1.config.datacollection.ResourceType> resourceTypes) {
        return resourceTypes.stream()
                .map(ResourceTypesExtensionManager::toResourceType)
                .collect(Collectors.toList());
    }

    public static ResourceType toResourceType(org.opennms.integration.api.v1.config.datacollection.ResourceType rt) {
        ResourceType resourceType = new ResourceType();
        resourceType.setLabel(rt.getLabel());
        resourceType.setName(rt.getName());
        resourceType.setResourceLabel(rt.getResourceLabel());
        if(rt.getStorageStrategy() != null) {
            StorageStrategy storageStrategy = new StorageStrategy();
            storageStrategy.setClazz(rt.getStorageStrategy().getClazz());
            storageStrategy.setParameters(rt.getStorageStrategy().getParameters().stream()
                    .map(ResourceTypesExtensionManager::toParameter)
                    .collect(Collectors.toList()));
            resourceType.setStorageStrategy(storageStrategy);
        }
        if(rt.getPersistenceSelectorStrategy() != null) {
            PersistenceSelectorStrategy selectorStrategy = new PersistenceSelectorStrategy();
            selectorStrategy.setClazz(rt.getPersistenceSelectorStrategy().getClazz());
            selectorStrategy.setParameters(rt.getPersistenceSelectorStrategy().getParameters().stream()
                    .map(ResourceTypesExtensionManager::toParameter)
                    .collect(Collectors.toList()));
            resourceType.setPersistenceSelectorStrategy(selectorStrategy);
        }
        return resourceType;
    }

    public static Parameter toParameter(org.opennms.integration.api.v1.config.datacollection.Parameter parameter) {
        return new Parameter() {
            @Override
            public String getKey() {
                return parameter.getKey();
            }

            @Override
            public String getValue() {
                return parameter.getValue();
            }
        };
    }
}
