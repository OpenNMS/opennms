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
package org.opennms.web.rest.v2.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.netmgt.config.datacollection.Parameter;
import org.opennms.netmgt.config.datacollection.PersistenceSelectorStrategy;
import org.opennms.netmgt.config.datacollection.ResourceType;
import org.opennms.netmgt.config.datacollection.ResourceTypes;
import org.opennms.netmgt.config.datacollection.StorageStrategy;
import org.opennms.web.rest.v2.model.ParameterDto;
import org.opennms.web.rest.v2.model.ResourceTypeDto;
import org.opennms.web.rest.v2.model.ResourceTypeStrategyDto;
import org.opennms.web.rest.v2.model.ResourceTypesConfigDto;
import org.opennms.web.rest.v2.model.ResourceTypesConfigSummaryDto;

/**
 * Maps the resource types of a configuration manager configuration to the DTOs of the REST API and back.
 * The entity setters intern their strings and reject null, so the DTOs must have passed
 * {@code ResourceTypesConfigValidator} before {@link #toEntity(ResourceTypesConfigDto)} is called.
 */
public final class ResourceTypesConfigMapper {

    private ResourceTypesConfigMapper() {
    }

    public static ResourceTypesConfigSummaryDto toSummaryDto(final String id, final ResourceTypes resourceTypes) {
        final ResourceTypesConfigSummaryDto dto = new ResourceTypesConfigSummaryDto();
        dto.setId(id);
        dto.setResourceTypeNames(resourceTypes.getResourceTypes().stream()
                .map(ResourceType::getName)
                .collect(Collectors.toList()));
        return dto;
    }

    public static ResourceTypesConfigDto toDto(final String id, final ResourceTypes resourceTypes) {
        final ResourceTypesConfigDto dto = new ResourceTypesConfigDto();
        dto.setId(id);
        dto.setResourceTypes(resourceTypes.getResourceTypes().stream()
                .map(ResourceTypesConfigMapper::toDto)
                .collect(Collectors.toList()));
        return dto;
    }

    public static ResourceTypeDto toDto(final ResourceType resourceType) {
        final ResourceTypeDto dto = new ResourceTypeDto();
        dto.setName(resourceType.getName());
        dto.setLabel(resourceType.getLabel());
        dto.setResourceLabel(resourceType.getResourceLabel());
        final PersistenceSelectorStrategy persistence = resourceType.getPersistenceSelectorStrategy();
        if (persistence != null) {
            dto.setPersistenceSelectorStrategy(toStrategyDto(persistence.getClazz(), persistence.getParameters()));
        }
        final StorageStrategy storage = resourceType.getStorageStrategy();
        if (storage != null) {
            dto.setStorageStrategy(toStrategyDto(storage.getClazz(), storage.getParameters()));
        }
        return dto;
    }

    public static ResourceTypes toEntity(final ResourceTypesConfigDto dto) {
        final ResourceTypes resourceTypes = new ResourceTypes();
        final List<ResourceType> types = new ArrayList<>();
        if (dto.getResourceTypes() != null) {
            dto.getResourceTypes().forEach(type -> types.add(toEntity(type)));
        }
        resourceTypes.setResourceTypes(types);
        return resourceTypes;
    }

    public static ResourceType toEntity(final ResourceTypeDto dto) {
        final ResourceType type = new ResourceType();
        type.setName(dto.getName().trim());
        type.setLabel(dto.getLabel().trim());
        if (dto.getResourceLabel() != null && !dto.getResourceLabel().trim().isEmpty()) {
            type.setResourceLabel(dto.getResourceLabel().trim());
        }

        final PersistenceSelectorStrategy persistence = new PersistenceSelectorStrategy(dto.getPersistenceSelectorStrategy().getClassName().trim());
        persistence.setParameters(toParameters(dto.getPersistenceSelectorStrategy()));
        type.setPersistenceSelectorStrategy(persistence);

        final StorageStrategy storage = new StorageStrategy(dto.getStorageStrategy().getClassName().trim());
        storage.setParameters(toParameters(dto.getStorageStrategy()));
        type.setStorageStrategy(storage);
        return type;
    }

    private static ResourceTypeStrategyDto toStrategyDto(final String className,
                                                         final List<? extends org.opennms.netmgt.collection.api.Parameter> parameters) {
        final ResourceTypeStrategyDto dto = new ResourceTypeStrategyDto();
        dto.setClassName(className);
        final List<ParameterDto> parameterDtos = new ArrayList<>();
        if (parameters != null) {
            for (final org.opennms.netmgt.collection.api.Parameter parameter : parameters) {
                final ParameterDto parameterDto = new ParameterDto();
                parameterDto.setKey(parameter.getKey());
                parameterDto.setValue(parameter.getValue());
                parameterDtos.add(parameterDto);
            }
        }
        dto.setParameters(parameterDtos);
        return dto;
    }

    private static List<org.opennms.netmgt.collection.api.Parameter> toParameters(final ResourceTypeStrategyDto dto) {
        final List<org.opennms.netmgt.collection.api.Parameter> parameters = new ArrayList<>();
        if (dto.getParameters() != null) {
            // values are kept as entered: a regular expression may end with a blank on purpose
            dto.getParameters().forEach(p -> parameters.add(new Parameter(p.getKey().trim(), p.getValue())));
        }
        return parameters;
    }
}
