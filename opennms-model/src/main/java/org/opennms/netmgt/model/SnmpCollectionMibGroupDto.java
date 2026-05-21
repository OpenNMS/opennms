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

public class SnmpCollectionMibGroupDto {

    private Integer id;
    private String name;
    private String ifType;
    private String mibGroupNames;
    private String mibObjects;
    private String mibObjProperties;
    private Boolean enabled;

    // Flattened fields from SnmpCollectionSource
    private Integer collectionSourceId;
    private String collectionSourceName;

    public SnmpCollectionMibGroupDto() {
    }

    public SnmpCollectionMibGroupDto(
            Integer id,
            String name,
            String ifType,
            String mibGroupNames,
            String mibObjects,
            String mibObjProperties,
            Boolean enabled,
            Integer collectionSourceId,
            String collectionSourceName) {

        this.id = id;
        this.name = name;
        this.ifType = ifType;
        this.mibGroupNames = mibGroupNames;
        this.mibObjects = mibObjects;
        this.mibObjProperties = mibObjProperties;
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

    public String getIfType() {
        return ifType;
    }

    public void setIfType(String ifType) {
        this.ifType = ifType;
    }

    public String getMibGroupNames() {
        return mibGroupNames;
    }

    public void setMibGroupNames(String mibGroupNames) {
        this.mibGroupNames = mibGroupNames;
    }

    public String getMibObjects() {
        return mibObjects;
    }

    public void setMibObjects(String mibObjects) {
        this.mibObjects = mibObjects;
    }

    public String getMibObjProperties() {
        return mibObjProperties;
    }

    public void setMibObjProperties(String mibObjProperties) {
        this.mibObjProperties = mibObjProperties;
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

    public static List<SnmpCollectionMibGroupDto> fromEntity(
            List<SnmpCollectionMibGroup> entities) {

        return entities.stream()
                .map(e -> new SnmpCollectionMibGroupDto(
                        e.getId(),
                        e.getName(),
                        e.getIfType(),
                        e.getMibGroupNames(),
                        e.getMibObjects(),
                        e.getMibObjProperties(),
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

    public static SnmpCollectionMibGroup updateEntity(SnmpCollectionMibGroup entity, final SnmpCollectionMibGroupDto dto) {
        if (dto == null) {
            return null;
        }
        entity.setName(dto.getName());
        entity.setIfType(dto.getIfType());
        entity.setMibGroupNames(dto.getMibGroupNames());
        entity.setMibObjects(dto.getMibObjects());
        entity.setMibObjProperties(dto.getMibObjProperties());
        entity.setEnabled(dto.getEnabled());

        return entity;
    }
}
