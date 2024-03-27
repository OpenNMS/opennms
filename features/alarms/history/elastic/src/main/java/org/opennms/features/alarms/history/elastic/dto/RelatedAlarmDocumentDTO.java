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
