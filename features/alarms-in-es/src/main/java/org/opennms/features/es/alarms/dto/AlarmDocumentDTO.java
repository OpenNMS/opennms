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

package org.opennms.features.es.alarms.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.gson.annotations.SerializedName;

public class AlarmDocumentDTO {
    private static final int DOCUMENT_VERSION = 1;

    public static final String TYPE = "alarm";

    @SerializedName("@first-event-time")
    private Long firstEventTime;

    @SerializedName("@last-event-time")
    private Long lastEventTime;

    @SerializedName("@update-time")
    private Long updateTime;

    @SerializedName("@deleted-time")
    private Long deletedTime;

    @SerializedName("@version")
    private Integer version = DOCUMENT_VERSION;

    @SerializedName("id")
    private Integer id;

    @SerializedName("reduction-key")
    private String reductionKey;

    @SerializedName("node")
    private NodeDocumentDTO node;

    @SerializedName("ip-address")
    private String ipAddress;

    @SerializedName("ifindex")
    private Integer ifIndex;

    @SerializedName("service-name")
    private String serviceName;

    @SerializedName("type")
    private Integer type;

    @SerializedName("counter")
    private Integer counter;

    @SerializedName("severity-id")
    private Integer severityId;

    @SerializedName("severity-label")
    private String severityLabel;

    @SerializedName("first-automation-time")
    private Long firstAutomationTime;

    @SerializedName("last-automation-time")
    private Long lastAutomationTime;

    @SerializedName("description")
    private String description;

    @SerializedName("log-message")
    private String logMessage;

    @SerializedName("operator-instructions")
    private String operatorInstructions;

    @SerializedName("ticket-id")
    private String ticketId;

    @SerializedName("ticket-state-id")
    private Integer ticketStateId;

    @SerializedName("ticket-state-name")
    private String ticketStateName;

    @SerializedName("mouse-over-text")
    private String mouseOverText;

    @SerializedName("suppressed-until")
    private Long suppressedUntil;

    @SerializedName("suppressed-user")
    private String suppressedUser;

    @SerializedName("ack-time")
    private Long ackTime;

    @SerializedName("ack-user")
    private String ackUser;

    @SerializedName("clear-key")
    private String clearKey;

    @SerializedName("last-event")
    private EventDocumentDTO lastEvent;

    @SerializedName("managed-object-instance")
    private String managedObjectInstance;

    @SerializedName("managed-object-type")
    private String managedObjectType;

    @SerializedName("application-dn")
    private String applicationDN;

    @SerializedName("oss-primary-key")
    private String ossPrimaryKey;

    @SerializedName("x733-alarm-type")
    private String x733AlarmType;;

    @SerializedName("qos-alarm-state")
    private String qosAlarmState;

    @SerializedName("x733-probable-cause")
    private Integer x733ProbableCause;

    @SerializedName("attributes")
    private Map<String, String> attributes = new HashMap<>();

    @SerializedName("sticky-memo")
    private MemoDocumentDTO stickyMemo;

    @SerializedName("journal-memo")
    private MemoDocumentDTO journalMemo;

    @SerializedName("related-alarm-ids")
    private List<Integer> relatedAlarmIds;

    @SerializedName("related-alarm-reduction-keys")
    private List<String> relatedAlarmReductionKeys;

    @SerializedName("related-alarms")
    private List<RelatedAlarmDocumentDTO> relatedAlarms;

    @SerializedName("situation")
    private Boolean situation;

    @SerializedName("archived")
    private Boolean archived;

    @SerializedName("archived-time")
    private Long archivedTime;

    public void addRelatedAlarm(RelatedAlarmDocumentDTO relatedAlarm) {
        if (relatedAlarms == null) {
            relatedAlarms = new LinkedList<>();
        }
        relatedAlarms.add(relatedAlarm);
    }

    public boolean isSituation() {
        return situation != null && situation;
    }

    // Generated getters/setters

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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReductionKey() {
        return reductionKey;
    }

    public void setReductionKey(String reductionKey) {
        this.reductionKey = reductionKey;
    }

    public NodeDocumentDTO getNode() {
        return node;
    }

    public void setNode(NodeDocumentDTO node) {
        this.node = node;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getIfIndex() {
        return ifIndex;
    }

    public void setIfIndex(Integer ifIndex) {
        this.ifIndex = ifIndex;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getCounter() {
        return counter;
    }

    public void setCounter(Integer counter) {
        this.counter = counter;
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

    public Long getFirstAutomationTime() {
        return firstAutomationTime;
    }

    public void setFirstAutomationTime(Long firstAutomationTime) {
        this.firstAutomationTime = firstAutomationTime;
    }

    public Long getLastAutomationTime() {
        return lastAutomationTime;
    }

    public void setLastAutomationTime(Long lastAutomationTime) {
        this.lastAutomationTime = lastAutomationTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

    public String getOperatorInstructions() {
        return operatorInstructions;
    }

    public void setOperatorInstructions(String operatorInstructions) {
        this.operatorInstructions = operatorInstructions;
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

    public String getMouseOverText() {
        return mouseOverText;
    }

    public void setMouseOverText(String mouseOverText) {
        this.mouseOverText = mouseOverText;
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

    public Long getAckTime() {
        return ackTime;
    }

    public void setAckTime(Long ackTime) {
        this.ackTime = ackTime;
    }

    public String getAckUser() {
        return ackUser;
    }

    public void setAckUser(String ackUser) {
        this.ackUser = ackUser;
    }

    public String getClearKey() {
        return clearKey;
    }

    public void setClearKey(String clearKey) {
        this.clearKey = clearKey;
    }

    public EventDocumentDTO getLastEvent() {
        return lastEvent;
    }

    public void setLastEvent(EventDocumentDTO lastEvent) {
        this.lastEvent = lastEvent;
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

    public String getApplicationDN() {
        return applicationDN;
    }

    public void setApplicationDN(String applicationDN) {
        this.applicationDN = applicationDN;
    }

    public String getOssPrimaryKey() {
        return ossPrimaryKey;
    }

    public void setOssPrimaryKey(String ossPrimaryKey) {
        this.ossPrimaryKey = ossPrimaryKey;
    }

    public String getX733AlarmType() {
        return x733AlarmType;
    }

    public void setX733AlarmType(String x733AlarmType) {
        this.x733AlarmType = x733AlarmType;
    }

    public String getQosAlarmState() {
        return qosAlarmState;
    }

    public void setQosAlarmState(String qosAlarmState) {
        this.qosAlarmState = qosAlarmState;
    }

    public Integer getX733ProbableCause() {
        return x733ProbableCause;
    }

    public void setX733ProbableCause(Integer x733ProbableCause) {
        this.x733ProbableCause = x733ProbableCause;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    public MemoDocumentDTO getStickyMemo() {
        return stickyMemo;
    }

    public void setStickyMemo(MemoDocumentDTO stickyMemo) {
        this.stickyMemo = stickyMemo;
    }

    public MemoDocumentDTO getJournalMemo() {
        return journalMemo;
    }

    public void setJournalMemo(MemoDocumentDTO journalMemo) {
        this.journalMemo = journalMemo;
    }

    public List<RelatedAlarmDocumentDTO> getRelatedAlarms() {
        return relatedAlarms;
    }

    public void setRelatedAlarms(List<RelatedAlarmDocumentDTO> relatedAlarms) {
        this.relatedAlarms = relatedAlarms;
    }

    public Long getDeletedTime() {
        return deletedTime;
    }

    public void setDeletedTime(Long deletedTime) {
        this.deletedTime = deletedTime;
    }

    public Boolean getSituation() {
        return situation;
    }

    public void setSituation(Boolean situation) {
        this.situation = situation;
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

    public List<Integer> getRelatedAlarmIds() {
        return relatedAlarmIds;
    }

    public void setRelatedAlarmIds(List<Integer> relatedAlarmIds) {
        this.relatedAlarmIds = relatedAlarmIds;
    }

    public List<String> getRelatedAlarmReductionKeys() {
        return relatedAlarmReductionKeys;
    }

    public void setRelatedAlarmReductionKeys(List<String> relatedAlarmReductionKeys) {
        this.relatedAlarmReductionKeys = relatedAlarmReductionKeys;
    }

    @Override
    public String toString() {
        return "AlarmDocumentDTO{" +
                "firstEventTime=" + firstEventTime +
                ", lastEventTime=" + lastEventTime +
                ", updateTime=" + updateTime +
                ", deletedTime=" + deletedTime +
                ", version=" + version +
                ", id=" + id +
                ", reductionKey='" + reductionKey + '\'' +
                ", node=" + node +
                ", ipAddress='" + ipAddress + '\'' +
                ", ifIndex=" + ifIndex +
                ", serviceName='" + serviceName + '\'' +
                ", type=" + type +
                ", counter=" + counter +
                ", severityId=" + severityId +
                ", severityLabel='" + severityLabel + '\'' +
                ", firstAutomationTime=" + firstAutomationTime +
                ", lastAutomationTime=" + lastAutomationTime +
                ", description='" + description + '\'' +
                ", logMessage='" + logMessage + '\'' +
                ", operatorInstructions='" + operatorInstructions + '\'' +
                ", ticketId='" + ticketId + '\'' +
                ", ticketStateId=" + ticketStateId +
                ", ticketStateName='" + ticketStateName + '\'' +
                ", mouseOverText='" + mouseOverText + '\'' +
                ", suppressedUntil=" + suppressedUntil +
                ", suppressedUser='" + suppressedUser + '\'' +
                ", ackTime=" + ackTime +
                ", ackUser='" + ackUser + '\'' +
                ", clearKey='" + clearKey + '\'' +
                ", lastEvent=" + lastEvent +
                ", managedObjectInstance='" + managedObjectInstance + '\'' +
                ", managedObjectType='" + managedObjectType + '\'' +
                ", applicationDN='" + applicationDN + '\'' +
                ", ossPrimaryKey='" + ossPrimaryKey + '\'' +
                ", x733AlarmType='" + x733AlarmType + '\'' +
                ", qosAlarmState='" + qosAlarmState + '\'' +
                ", x733ProbableCause=" + x733ProbableCause +
                ", attributes=" + attributes +
                ", stickyMemo=" + stickyMemo +
                ", journalMemo=" + journalMemo +
                ", relatedAlarmIds=" + relatedAlarmIds +
                ", relatedAlarmReductionKeys=" + relatedAlarmReductionKeys +
                ", relatedAlarms=" + relatedAlarms +
                ", situation=" + situation +
                ", archived=" + archived +
                ", archivedTime=" + archivedTime +
                '}';
    }
}
