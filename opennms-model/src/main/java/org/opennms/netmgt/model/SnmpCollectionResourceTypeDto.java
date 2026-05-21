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

package org.opennms.netmgt.model;

import java.util.List;
import java.util.stream.Collectors;

public class SnmpCollectionResourceTypeDto {

    private Integer id;
    private String name;
    private String label;
    private String resourceLabel;
    private String persistenceSelectorStrategy;
    private String persistenceSelectorParams;
    private String storageStrategy;
    private String storageStrategyParams;
    private Boolean enabled;

    // Flattened fields from SnmpCollectionSource
    private Integer collectionSourceId;
    private String collectionSourceName;

    public SnmpCollectionResourceTypeDto() {
    }

    public SnmpCollectionResourceTypeDto(
            Integer id,
            String name,
            String label,
            String resourceLabel,
            String persistenceSelectorStrategy,
            String persistenceSelectorParams,
            String storageStrategy,
            String storageStrategyParams,
            Boolean enabled,
            Integer collectionSourceId,
            String collectionSourceName) {

        this.id = id;
        this.name = name;
        this.label = label;
        this.resourceLabel = resourceLabel;
        this.persistenceSelectorStrategy = persistenceSelectorStrategy;
        this.persistenceSelectorParams = persistenceSelectorParams;
        this.storageStrategy = storageStrategy;
        this.storageStrategyParams = storageStrategyParams;
        this.enabled = enabled;
        this.collectionSourceId = collectionSourceId;
        this.collectionSourceName = collectionSourceName;
    }

    /* ===================== Getters & Setters ===================== */

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getResourceLabel() {
        return resourceLabel;
    }

    public void setResourceLabel(String resourceLabel) {
        this.resourceLabel = resourceLabel;
    }

    public String getPersistenceSelectorStrategy() {
        return persistenceSelectorStrategy;
    }

    public void setPersistenceSelectorStrategy(String persistenceSelectorStrategy) {
        this.persistenceSelectorStrategy = persistenceSelectorStrategy;
    }

    public String getPersistenceSelectorParams() {
        return persistenceSelectorParams;
    }

    public void setPersistenceSelectorParams(String persistenceSelectorParams) {
        this.persistenceSelectorParams = persistenceSelectorParams;
    }

    public String getStorageStrategy() {
        return storageStrategy;
    }

    public void setStorageStrategy(String storageStrategy) {
        this.storageStrategy = storageStrategy;
    }

    public String getStorageStrategyParams() {
        return storageStrategyParams;
    }

    public void setStorageStrategyParams(String storageStrategyParams) {
        this.storageStrategyParams = storageStrategyParams;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Integer getCollectionSourceId() {
        return collectionSourceId;
    }

    public void setCollectionSourceId(Integer collectionSourceId) {
        this.collectionSourceId = collectionSourceId;
    }

    public String getCollectionSourceName() {
        return collectionSourceName;
    }

    public void setCollectionSourceName(String collectionSourceName) {
        this.collectionSourceName = collectionSourceName;
    }

    public static List<SnmpCollectionResourceTypeDto> fromEntity(
            List<SnmpCollectionResourceType> entities) {

        return entities.stream()
                .map(e -> new SnmpCollectionResourceTypeDto(
                        e.getId(),
                        e.getName(),
                        e.getLabel(),
                        e.getResourceLabel(),
                        e.getPersistenceSelectorStrategy(),
                        e.getPersistenceSelectorParams(),
                        e.getStorageStrategy(),
                        e.getStorageStrategyParams(),
                        e.getEnabled(),
                        e.getCollectionSource() != null
                                ? e.getCollectionSource().getId()
                                : null,
                        e.getCollectionSource() != null
                                ? e.getCollectionSource().getName()
                                : null
                ))
                .collect(Collectors.toList());
    }

    public static SnmpCollectionResourceType updateEntity(SnmpCollectionResourceType entity, final SnmpCollectionResourceTypeDto dto) {
        if (dto == null) {
            return null;
        }

        entity.setName(dto.getName());
        entity.setLabel(dto.getLabel());
        entity.setResourceLabel(dto.getResourceLabel());
        entity.setPersistenceSelectorStrategy(dto.getPersistenceSelectorStrategy());
        entity.setPersistenceSelectorParams(dto.getPersistenceSelectorParams());
        entity.setStorageStrategyParams(dto.getStorageStrategyParams());
        entity.setStorageStrategy(dto.getStorageStrategy());
        entity.setEnabled(dto.getEnabled());

        return entity;
    }
}
