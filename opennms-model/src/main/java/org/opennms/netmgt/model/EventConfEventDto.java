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

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class EventConfEventDto {

    private Long id;
    private String uei;
    private String eventLabel;
    private String description;
    private Boolean enabled;
    private String xmlContent;
    private Date createdTime;
    private Date lastModified;
    private String modifiedBy;
    private String severity;

    // Flattened fields from EventConfSource
    private String sourceName;
    private String vendor;
    private Integer fileOrder;

    public EventConfEventDto() {
    }

    public EventConfEventDto(Long id, String uei, String eventLabel, String description, Boolean enabled,
                             String xmlContent, Date createdTime, Date lastModified, String modifiedBy,
                             String sourceName, String vendor, Integer fileOrder,String severity) {
        this.id = id;
        this.uei = uei;
        this.eventLabel = eventLabel;
        this.description = description;
        this.enabled = enabled;
        this.xmlContent = xmlContent;
        this.createdTime = createdTime;
        this.lastModified = lastModified;
        this.modifiedBy = modifiedBy;
        this.sourceName = sourceName;
        this.vendor = vendor;
        this.fileOrder = fileOrder;
        this.severity = severity;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUei() { return uei; }
    public void setUei(String uei) { this.uei = uei; }

    public String getEventLabel() { return eventLabel; }
    public void setEventLabel(String eventLabel) { this.eventLabel = eventLabel; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Boolean getEnabled() { return enabled; }
    public void setEnabled(Boolean enabled) { this.enabled = enabled; }

    public String getXmlContent() { return xmlContent; }
    public void setXmlContent(String xmlContent) { this.xmlContent = xmlContent; }

    public Date getCreatedTime() { return createdTime; }
    public void setCreatedTime(Date createdTime) { this.createdTime = createdTime; }

    public Date getLastModified() { return lastModified; }
    public void setLastModified(Date lastModified) { this.lastModified = lastModified; }

    public String getModifiedBy() { return modifiedBy; }
    public void setModifiedBy(String modifiedBy) { this.modifiedBy = modifiedBy; }

    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public Integer getFileOrder() {
        return fileOrder;
    }

    public void setFileOrder(Integer fileOrder) {
        this.fileOrder = fileOrder;
    }
    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public static List<EventConfEventDto> fromEntity(List<EventConfEvent> entityList) {

        return entityList.stream()
                .map(e -> new EventConfEventDto(
                        e.getId(),
                        e.getUei(),
                        e.getEventLabel(),
                        e.getDescription(),
                        e.getEnabled(),
                        e.getXmlContent(),
                        e.getCreatedTime(),
                        e.getLastModified(),
                        e.getModifiedBy(),
                        e.getSource() != null ? e.getSource().getName() : null,
                        e.getSource() != null ? e.getSource().getVendor() : null,
                        e.getSource() != null ? e.getSource().getFileOrder() : null,
                        e.getSeverity()
                ))
                .collect(Collectors.toList());
    }
}
