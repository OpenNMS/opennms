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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.opennms.features.alarms.history.api.AlarmState;

import com.google.common.collect.ImmutableMap;
import com.google.gson.annotations.SerializedName;

public class AlarmDocumentDTO implements AlarmState {
    private static final int DOCUMENT_VERSION = 1;

    public static final String TYPE = "alarm";

    @SerializedName("@deleted_time")
    private Long deletedTime;

    @SerializedName("@first_event_time")
    private Long firstEventTime;

    @SerializedName("@last_event_time")
    private Long lastEventTime;

    @SerializedName("@update_time")
    private Long updateTime;

    @SerializedName("@version")
    private Integer version = DOCUMENT_VERSION;

    @SerializedName("ack_time")
    private Long ackTime;

    @SerializedName("ack_user")
    private String ackUser;

    @SerializedName("application_dn")
    private String applicationDN;

    @SerializedName("archived")
    private Boolean archived;

    @SerializedName("archived_time")
    private Long archivedTime;

    @SerializedName("attributes")
    private Map<String, String> attributes = new HashMap<>();

    @SerializedName("clear_key")
    private String clearKey;

    @SerializedName("counter")
    private Integer counter;

    @SerializedName("description")
    private String description;

    @SerializedName("dist_poller")
    private MonitoringSystemDTO distPoller;

    @SerializedName("first_automation_time")
    private Long firstAutomationTime;

    @SerializedName("id")
    private Integer id;

    @SerializedName("if_index")
    private Integer ifIndex;

    @SerializedName("ip_address")
    private String ipAddress;

    @SerializedName("journal_memo")
    private MemoDocumentDTO journalMemo;

    @SerializedName("last_automation_time")
    private Long lastAutomationTime;

    @SerializedName("last_event")
    private EventDocumentDTO lastEvent;

    @SerializedName("log_message")
    private String logMessage;

    @SerializedName("managed_object_instance")
    private String managedObjectInstance;

    @SerializedName("managed_object_type")
    private String managedObjectType;

    @SerializedName("mouse_over_text")
    private String mouseOverText;

    @SerializedName("node")
    private NodeDocumentDTO node;

    @SerializedName("operator_instructions")
    private String operatorInstructions;

    @SerializedName("oss_primary_key")
    private String ossPrimaryKey;

    @SerializedName("part_of_situation")
    private Boolean partOfSituation;

    @SerializedName("qos_alarm_state")
    private String qosAlarmState;

    @SerializedName("reduction_key")
    private String reductionKey;

    @SerializedName("related_alarm_count")
    private int relatedAlarmCount;

    @SerializedName("related_alarm_ids")
    private final Set<Integer> relatedAlarmIds = new HashSet<>();

    @SerializedName("related_alarm_reduction_keys")
    private final List<String> relatedAlarmReductionKeys = new ArrayList<>();

    @SerializedName("related_alarms")
    private List<RelatedAlarmDocumentDTO> relatedAlarms;

    @SerializedName("service_name")
    private String serviceName;

    @SerializedName("service_type")
    private String serviceType;

    @SerializedName("severity_id")
    private Integer severityId;

    @SerializedName("severity_label")
    private String severityLabel;

    @SerializedName("situation")
    private Boolean situation;

    @SerializedName("sticky_memo")
    private MemoDocumentDTO stickyMemo;

    @SerializedName("suppressed_time")
    private Long suppressedTime;
    
    @SerializedName("suppressed_until")
    private Long suppressedUntil;

    @SerializedName("suppressed_user")
    private String suppressedUser;

    @SerializedName("ticket_id")
    private String ticketId;

    @SerializedName("ticket_state_id")
    private Integer ticketStateId;

    @SerializedName("ticket_state_name")
    private String ticketStateName;

    @SerializedName("type")
    private Integer type;

    @SerializedName("x733_alarm_type")
    private String x733AlarmType;

    @SerializedName("x733_probable_cause")
    private Integer x733ProbableCause;

    public void addRelatedAlarm(RelatedAlarmDocumentDTO relatedAlarm) {
        if (relatedAlarms == null) {
            relatedAlarms = new LinkedList<>();
        }
        relatedAlarms.add(Objects.requireNonNull(relatedAlarm));
        relatedAlarmCount++;
        relatedAlarmIds.add(relatedAlarm.getId());
        relatedAlarmReductionKeys.add(relatedAlarm.getReductionKey());
    }

    @Override
    public boolean isSituation() {
        return situation != null && situation;
    }

    // Generated getters/setters

    @Override
    public Long getDeletedTime() {
        return deletedTime;
    }

    public void setDeletedTime(Long deletedTime) {
        this.deletedTime = deletedTime;
    }

    public Long getFirstEventTime() {
        return firstEventTime;
    }

    public void setFirstEventTime(Long firstEventTime) {
        this.firstEventTime = firstEventTime;
    }

    public Long getLastEventTime() {
        return lastEventTime;
    }

    public void setLastEventTime(Long lastEventTime) {
        this.lastEventTime = lastEventTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    @Override
    public Long getAckTime() {
        return ackTime;
    }

    public void setAckTime(Long ackTime) {
        this.ackTime = ackTime;
    }

    @Override
    public String getAckUser() {
        return ackUser;
    }

    public void setAckUser(String ackUser) {
        this.ackUser = ackUser;
    }

    public String getApplicationDN() {
        return applicationDN;
    }

    public void setApplicationDN(String applicationDN) {
        this.applicationDN = applicationDN;
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }

    public Long getArchivedTime() {
        return archivedTime;
    }

    public void setArchivedTime(Long archivedTime) {
        this.archivedTime = archivedTime;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes == null ? null : ImmutableMap.copyOf(attributes);
    }

    public String getClearKey() {
        return clearKey;
    }

    public void setClearKey(String clearKey) {
        this.clearKey = clearKey;
    }

    @Override
    public Integer getCounter() {
        return counter;
    }

    public void setCounter(Integer counter) {
        this.counter = counter;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MonitoringSystemDTO getDistPoller() {
        return distPoller;
    }

    public void setDistPoller(MonitoringSystemDTO distPoller) {
        this.distPoller = distPoller;
    }

    public Long getFirstAutomationTime() {
        return firstAutomationTime;
    }

    public void setFirstAutomationTime(Long firstAutomationTime) {
        this.firstAutomationTime = firstAutomationTime;
    }

    @Override
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIfIndex() {
        return ifIndex;
    }

    public void setIfIndex(Integer ifIndex) {
        this.ifIndex = ifIndex;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public MemoDocumentDTO getJournalMemo() {
        return journalMemo;
    }

    public void setJournalMemo(MemoDocumentDTO journalMemo) {
        this.journalMemo = journalMemo;
    }

    public Long getLastAutomationTime() {
        return lastAutomationTime;
    }

    public void setLastAutomationTime(Long lastAutomationTime) {
        this.lastAutomationTime = lastAutomationTime;
    }

    public EventDocumentDTO getLastEvent() {
        return lastEvent;
    }

    public void setLastEvent(EventDocumentDTO lastEvent) {
        this.lastEvent = lastEvent;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
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

    public String getMouseOverText() {
        return mouseOverText;
    }

    public void setMouseOverText(String mouseOverText) {
        this.mouseOverText = mouseOverText;
    }

    public NodeDocumentDTO getNode() {
        return node;
    }

    public void setNode(NodeDocumentDTO node) {
        this.node = node;
    }

    public String getOperatorInstructions() {
        return operatorInstructions;
    }

    public void setOperatorInstructions(String operatorInstructions) {
        this.operatorInstructions = operatorInstructions;
    }

    public String getOssPrimaryKey() {
        return ossPrimaryKey;
    }

    public void setOssPrimaryKey(String ossPrimaryKey) {
        this.ossPrimaryKey = ossPrimaryKey;
    }

    public Boolean getPartOfSituation() {
        return partOfSituation;
    }

    public void setPartOfSituation(Boolean partOfSituation) {
        this.partOfSituation = partOfSituation;
    }

    public String getQosAlarmState() {
        return qosAlarmState;
    }

    public void setQosAlarmState(String qosAlarmState) {
        this.qosAlarmState = qosAlarmState;
    }

    @Override
    public String getReductionKey() {
        return reductionKey;
    }

    public void setReductionKey(String reductionKey) {
        this.reductionKey = reductionKey;
    }

    public int getRelatedAlarmCount() {
        return relatedAlarmCount;
    }

    public Set<Integer> getRelatedAlarmIds() {
        return relatedAlarmIds;
    }

    public List<String> getRelatedAlarmReductionKeys() {
        return relatedAlarmReductionKeys;
    }

    @Override
    public List<RelatedAlarmDocumentDTO> getRelatedAlarms() {
        return relatedAlarms;
    }

    // Also updates the count, Ids and reduction keys
    public void setRelatedAlarms(List<RelatedAlarmDocumentDTO> relatedAlarms) {
        this.relatedAlarms = relatedAlarms;
        relatedAlarmCount = relatedAlarms.size();
        relatedAlarmIds.clear();
        relatedAlarmReductionKeys.clear();
        for (RelatedAlarmDocumentDTO relatedAlarm : relatedAlarms) {
            relatedAlarmIds.add(relatedAlarm.getId());
            relatedAlarmReductionKeys.add(relatedAlarm.getReductionKey());
        }
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    @Override
    public Integer getSeverityId() {
        return severityId;
    }

    public void setSeverityId(Integer severityId) {
        this.severityId = severityId;
    }

    @Override
    public String getSeverityLabel() {
        return severityLabel;
    }

    public void setSeverityLabel(String severityLabel) {
        this.severityLabel = severityLabel;
    }

    public Boolean getSituation() {
        return situation;
    }

    public void setSituation(Boolean situation) {
        this.situation = situation;
    }

    public MemoDocumentDTO getStickyMemo() {
        return stickyMemo;
    }

    public void setStickyMemo(MemoDocumentDTO stickyMemo) {
        this.stickyMemo = stickyMemo;
    }

    public Long getSuppressedTime() {
        return suppressedTime;
    }

    public void setSuppressedTime(Long suppressedTime) {
        this.suppressedTime = suppressedTime;
    }

    public Long getSuppressedUntil() {
        return suppressedUntil;
    }

    public void setSuppressedUntil(Long suppressedUntil) {
        this.suppressedUntil = suppressedUntil;
    }

    public String getSuppressedUser() {
        return suppressedUser;
    }

    public void setSuppressedUser(String suppressedUser) {
        this.suppressedUser = suppressedUser;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public Integer getTicketStateId() {
        return ticketStateId;
    }

    public void setTicketStateId(Integer ticketStateId) {
        this.ticketStateId = ticketStateId;
    }

    public String getTicketStateName() {
        return ticketStateName;
    }

    public void setTicketStateName(String ticketStateName) {
        this.ticketStateName = ticketStateName;
    }

    @Override
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getX733AlarmType() {
        return x733AlarmType;
    }

    public void setX733AlarmType(String x733AlarmType) {
        this.x733AlarmType = x733AlarmType;
    }

    public Integer getX733ProbableCause() {
        return x733ProbableCause;
    }

    public void setX733ProbableCause(Integer x733ProbableCause) {
        this.x733ProbableCause = x733ProbableCause;
    }

    @Override
    public String toString() {
        return "AlarmDocumentDTO{" +
                "deletedTime=" + deletedTime +
                ", firstEventTime=" + firstEventTime +
                ", lastEventTime=" + lastEventTime +
                ", updateTime=" + updateTime +
                ", version=" + version +
                ", ackTime=" + ackTime +
                ", ackUser='" + ackUser + '\'' +
                ", applicationDN='" + applicationDN + '\'' +
                ", archived=" + archived +
                ", archivedTime=" + archivedTime +
                ", attributes=" + attributes +
                ", clearKey='" + clearKey + '\'' +
                ", counter=" + counter +
                ", description='" + description + '\'' +
                ", distPoller=" + distPoller +
                ", firstAutomationTime=" + firstAutomationTime +
                ", id=" + id +
                ", ifIndex=" + ifIndex +
                ", ipAddress='" + ipAddress + '\'' +
                ", journalMemo=" + journalMemo +
                ", lastAutomationTime=" + lastAutomationTime +
                ", lastEvent=" + lastEvent +
                ", logMessage='" + logMessage + '\'' +
                ", managedObjectInstance='" + managedObjectInstance + '\'' +
                ", managedObjectType='" + managedObjectType + '\'' +
                ", mouseOverText='" + mouseOverText + '\'' +
                ", node=" + node +
                ", operatorInstructions='" + operatorInstructions + '\'' +
                ", ossPrimaryKey='" + ossPrimaryKey + '\'' +
                ", partOfSituation=" + partOfSituation +
                ", qosAlarmState='" + qosAlarmState + '\'' +
                ", reductionKey='" + reductionKey + '\'' +
                ", relatedAlarmCount=" + relatedAlarmCount +
                ", relatedAlarmIds=" + relatedAlarmIds +
                ", relatedAlarmReductionKeys=" + relatedAlarmReductionKeys +
                ", relatedAlarms=" + relatedAlarms +
                ", serviceName='" + serviceName + '\'' +
                ", serviceType='" + serviceType + '\'' +
                ", severityId=" + severityId +
                ", severityLabel='" + severityLabel + '\'' +
                ", situation=" + situation +
                ", stickyMemo=" + stickyMemo +
                ", suppressedTime=" + suppressedTime +
                ", suppressedUntil=" + suppressedUntil +
                ", suppressedUser='" + suppressedUser + '\'' +
                ", ticketId='" + ticketId + '\'' +
                ", ticketStateId=" + ticketStateId +
                ", ticketStateName='" + ticketStateName + '\'' +
                ", type=" + type +
                ", x733AlarmType='" + x733AlarmType + '\'' +
                ", x733ProbableCause=" + x733ProbableCause +
                '}';
    }
}
