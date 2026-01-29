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


import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class SnmpCollectionSourceDto {

    private Integer id;
    private String name;
    private String vendor;
    private String description;
    private Boolean enabled;
    private Date createdTime;
    private Date lastModified;
    private String uploadedBy;

    // All-args constructor
    public SnmpCollectionSourceDto(Integer id, String name, String vendor,
                                   String description, Boolean enabled,
                                   Date createdTime, Date lastModified,
                                   String uploadedBy) {
        this.id = id;
        this.name = name;
        this.vendor = vendor;
        this.description = description;
        this.enabled = enabled;
        this.createdTime = createdTime;
        this.lastModified = lastModified;
        this.uploadedBy = uploadedBy;
    }

    // Getters & setters
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

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }

    // Mapper for single entity
    public static SnmpCollectionSourceDto fromEntity(SnmpCollectionSource entity) {
        if (entity == null) {
            return null;
        }
        return new SnmpCollectionSourceDto(
                entity.getId(),
                entity.getName(),
                entity.getVendor(),
                entity.getDescription(),
                entity.getEnabled(),
                entity.getCreatedTime(),
                entity.getLastModified(),
                entity.getUploadedBy()
        );
    }

    // Mapper for list
    public static List<SnmpCollectionSourceDto> fromEntity(List<SnmpCollectionSource> entityList) {
        if (entityList == null) {
            return Collections.emptyList();
        }
        return entityList.stream()
                .map(SnmpCollectionSourceDto::fromEntity)
                .collect(Collectors.toList());
    }
}
