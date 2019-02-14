/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.features.alarms.history.elastic.dto;

import org.opennms.features.alarms.history.api.RelatedAlarmState;

import com.google.gson.annotations.SerializedName;

/**
 * The related alarm document contains a subset of the alarm fields.
 * <p>
 * The remaining attributes can be retrieved directly from the document representing the related alarm.
 */
public class RelatedAlarmDocumentDTO implements RelatedAlarmState {

    @SerializedName("first_event_time")
    private Long firstEventTime;

    @SerializedName("id")
    private Integer id;

    @SerializedName("last_event")
    private EventDocumentDTO lastEvent;

    @SerializedName("last_event_time")
    private Long lastEventTime;

    @SerializedName("managed_object_instance")
    private String managedObjectInstance;

    @SerializedName("managed_object_type")
    private String managedObjectType;

    @SerializedName("reduction_key")
    private String reductionKey;

    @SerializedName("severity_id")
    private Integer severityId;

    @SerializedName("severity_label")
    private String severityLabel;

    public Long getFirstEventTime() {
        return firstEventTime;
    }

    public void setFirstEventTime(Long firstEventTime) {
        this.firstEventTime = firstEventTime;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public EventDocumentDTO getLastEvent() {
        return lastEvent;
    }

    public void setLastEvent(EventDocumentDTO lastEvent) {
        this.lastEvent = lastEvent;
    }

    public Long getLastEventTime() {
        return lastEventTime;
    }

    public void setLastEventTime(Long lastEventTime) {
        this.lastEventTime = lastEventTime;
    }

    public String getManagedObjectInstance() {
        return managedObjectInstance;
    }

    public void setManagedObjectInstance(String managedObjectInstance) {
        this.managedObjectInstance = managedObjectInstance;
    }

    public String getManagedObjectType() {
        return managedObjectType;
    }

    public void setManagedObjectType(String managedObjectType) {
        this.managedObjectType = managedObjectType;
    }

    public String getReductionKey() {
        return reductionKey;
    }

    public void setReductionKey(String reductionKey) {
        this.reductionKey = reductionKey;
    }

    public Integer getSeverityId() {
        return severityId;
    }

    public void setSeverityId(Integer severityId) {
        this.severityId = severityId;
    }

    public String getSeverityLabel() {
        return severityLabel;
    }

    public void setSeverityLabel(String severityLabel) {
        this.severityLabel = severityLabel;
    }

    @Override
    public String toString() {
        return "RelatedAlarmDocumentDTO{" +
                "firstEventTime=" + firstEventTime +
                ", id=" + id +
                ", lastEvent=" + lastEvent +
                ", lastEventTime=" + lastEventTime +
                ", managedObjectInstance='" + managedObjectInstance + '\'' +
                ", managedObjectType='" + managedObjectType + '\'' +
                ", reductionKey='" + reductionKey + '\'' +
                ", severityId=" + severityId +
                ", severityLabel='" + severityLabel + '\'' +
                '}';
    }
}
