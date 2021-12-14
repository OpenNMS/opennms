/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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
