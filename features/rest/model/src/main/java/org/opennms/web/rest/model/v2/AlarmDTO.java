/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.model.v2;

import org.opennms.core.network.InetAddressXmlAdapter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.net.InetAddress;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@XmlRootElement(name="alarm")
@XmlAccessorType(XmlAccessType.NONE)
public class AlarmDTO {

    @XmlAttribute(name="id")
    private Integer id;

    @XmlElement(name="uei")
    private String uei;

    @XmlElement(name="location")
    private String location;

    @XmlElement(name="nodeId")
    private Integer nodeId;

    @XmlElement(name="nodeLabel")
    private String nodeLabel;

    @XmlElement(name="ipAddress")
    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
    private InetAddress ipAddress;

    @XmlElement(name = "serviceType")
    private ServiceTypeDTO serviceType;

    @XmlElement(name="reductionKey")
    private String reductionKey;

    @XmlAttribute(name="type")
    private Integer type;

    @XmlAttribute(name="count")
    private Integer count;

    @XmlAttribute(name="severity")
    private String severity;

    @XmlElement(name="firstEventTime")
    private Date firstEventTime;

    @XmlElement(name="description")
    private String description;

    @XmlElement(name="logMessage")
    private String logMessage;

    @XmlElement(name="operatorInstructions")
    private String operatorInstructions;

    @XmlElement(name="troubleTicket")
    private String troubleTicket;

    @XmlElement(name="troubleTicketState")
    private Integer troubleTicketState;

    @XmlElement(name = "troubleTicketLink")
    private String troubleTicketLink;

    @XmlElement(name="mouseOverText")
    private String mouseOverText;

    @XmlElement(name="suppressedUntil")
    private Date suppressedUntil;

    @XmlElement(name="suppressedBy")
    private String suppressedBy;

    @XmlElement(name="suppressedTime")
    private Date suppressedTime;

    @XmlElement(name="ackUser")
    private String ackUser;

    @XmlElement(name="ackTime")
    private Date ackTime;

    @XmlElement(name="clearKey")
    private String clearKey;

    @XmlElement(name="lastEvent")
    private EventDTO lastEvent;

    @XmlElementWrapper(name="parameters")
    @XmlElement(name="parameter")
    private List<EventParameterDTO> parameters;

    @XmlElement(name="lastEventTime")
    private Date lastEventTime;

    @XmlElement(name="applicationDN")
    private String applicationDN;

    @XmlElement(name="managedObjectInstance")
    private String managedObjectInstance;

    @XmlElement(name="managedObjectType")
    private String managedObjectType;

    @XmlElement(name="ossPrimaryKey")
    private String ossPrimaryKey;

    @XmlElement(name="x733AlarmType")
    private String x733AlarmType;

    @XmlElement(name="x733ProbableCause")
    private Integer x733ProbableCause;

    @XmlElement(name="qosAlarmState")
    private String qosAlarmState;

    @XmlElement(name="firstAutomationTime")
    private Date firstAutomationTime;

    @XmlElement(name="lastAutomationTime")
    private Date lastAutomationTime;

    @XmlAttribute(name="ifIndex")
    private Integer ifIndex;

    @XmlElement(name="reductionKeyMemo")
    private ReductionKeyMemoDTO reductionKeyMemo;

    @XmlElement(name="stickyMemo")
    private MemoDTO stickyMemo;

    @XmlElement(name="relatedAlarms")
    private List<AlarmSummaryDTO> relatedAlarms;
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUei() {
        return uei;
    }

    public void setUei(String uei) {
        this.uei = uei;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Integer getNodeId() {
        return nodeId;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeLabel() {
        return nodeLabel;
    }

    public void setNodeLabel(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(InetAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    public ServiceTypeDTO getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceTypeDTO serviceType) {
        this.serviceType = serviceType;
    }

    public String getReductionKey() {
        return reductionKey;
    }

    public void setReductionKey(String reductionKey) {
        this.reductionKey = reductionKey;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public Date getFirstEventTime() {
        return firstEventTime;
    }

    public void setFirstEventTime(Date firstEventTime) {
        this.firstEventTime = firstEventTime;
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

    public String getTroubleTicket() {
        return troubleTicket;
    }

    public void setTroubleTicket(String troubleTicket) {
        this.troubleTicket = troubleTicket;
    }

    public Integer getTroubleTicketState() {
        return troubleTicketState;
    }

    public void setTroubleTicketState(Integer troubleTicketState) {
        this.troubleTicketState = troubleTicketState;
    }

    public void setTroubleTicketLink(String troubleTicketLink) {
        this.troubleTicketLink = troubleTicketLink;
    }

    public String getTroubleTicketLink() {
        return troubleTicketLink;
    }

    public String getMouseOverText() {
        return mouseOverText;
    }

    public void setMouseOverText(String mouseOverText) {
        this.mouseOverText = mouseOverText;
    }

    public Date getSuppressedUntil() {
        return suppressedUntil;
    }

    public void setSuppressedUntil(Date suppressedUntil) {
        this.suppressedUntil = suppressedUntil;
    }

    public String getSuppressedBy() {
        return suppressedBy;
    }

    public void setSuppressedBy(String suppressedBy) {
        this.suppressedBy = suppressedBy;
    }

    public Date getSuppressedTime() {
        return suppressedTime;
    }

    public void setSuppressedTime(Date suppressedTime) {
        this.suppressedTime = suppressedTime;
    }

    public String getAckUser() {
        return ackUser;
    }

    public void setAckUser(String ackUser) {
        this.ackUser = ackUser;
    }

    public Date getAckTime() {
        return ackTime;
    }

    public void setAckTime(Date ackTime) {
        this.ackTime = ackTime;
    }

    public String getClearKey() {
        return clearKey;
    }

    public void setClearKey(String clearKey) {
        this.clearKey = clearKey;
    }

    public EventDTO getLastEvent() {
        return lastEvent;
    }

    public void setLastEvent(EventDTO lastEvent) {
        this.lastEvent = lastEvent;
    }

    public List<EventParameterDTO> getParameters() {
        return parameters;
    }

    public void setParameters(List<EventParameterDTO> parameters) {
        this.parameters = parameters;
    }

    public Date getLastEventTime() {
        return lastEventTime;
    }

    public void setLastEventTime(Date lastEventTime) {
        this.lastEventTime = lastEventTime;
    }

    public String getApplicationDN() {
        return applicationDN;
    }

    public void setApplicationDN(String applicationDN) {
        this.applicationDN = applicationDN;
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

    public Integer getX733ProbableCause() {
        return x733ProbableCause;
    }

    public void setX733ProbableCause(Integer x733ProbableCause) {
        this.x733ProbableCause = x733ProbableCause;
    }

    public String getQosAlarmState() {
        return qosAlarmState;
    }

    public void setQosAlarmState(String qosAlarmState) {
        this.qosAlarmState = qosAlarmState;
    }

    public Date getFirstAutomationTime() {
        return firstAutomationTime;
    }

    public void setFirstAutomationTime(Date firstAutomationTime) {
        this.firstAutomationTime = firstAutomationTime;
    }

    public Date getLastAutomationTime() {
        return lastAutomationTime;
    }

    public void setLastAutomationTime(Date lastAutomationTime) {
        this.lastAutomationTime = lastAutomationTime;
    }

    public Integer getIfIndex() {
        return ifIndex;
    }

    public void setIfIndex(Integer ifIndex) {
        this.ifIndex = ifIndex;
    }

    public ReductionKeyMemoDTO getReductionKeyMemo() {
        return reductionKeyMemo;
    }

    public void setReductionKeyMemo(ReductionKeyMemoDTO reductionKeyMemo) {
        this.reductionKeyMemo = reductionKeyMemo;
    }

    public MemoDTO getStickyMemo() {
        return stickyMemo;
    }

    public void setStickyMemo(MemoDTO stickyMemo) {
        this.stickyMemo = stickyMemo;
    }

    public List<AlarmSummaryDTO> getRelatedAlarms() {
        return relatedAlarms;
    }

    public void setRelatedAlarms(List<AlarmSummaryDTO> relatedAlarms) {
        this.relatedAlarms = relatedAlarms;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlarmDTO alarmDTO = (AlarmDTO) o;
        return Objects.equals(id, alarmDTO.id) &&
                Objects.equals(uei, alarmDTO.uei) &&
                Objects.equals(location, alarmDTO.location) &&
                Objects.equals(nodeId, alarmDTO.nodeId) &&
                Objects.equals(nodeLabel, alarmDTO.nodeLabel) &&
                Objects.equals(ipAddress, alarmDTO.ipAddress) &&
                Objects.equals(serviceType, alarmDTO.serviceType) &&
                Objects.equals(reductionKey, alarmDTO.reductionKey) &&
                Objects.equals(type, alarmDTO.type) &&
                Objects.equals(count, alarmDTO.count) &&
                Objects.equals(severity, alarmDTO.severity) &&
                Objects.equals(firstEventTime, alarmDTO.firstEventTime) &&
                Objects.equals(description, alarmDTO.description) &&
                Objects.equals(logMessage, alarmDTO.logMessage) &&
                Objects.equals(operatorInstructions, alarmDTO.operatorInstructions) &&
                Objects.equals(troubleTicket, alarmDTO.troubleTicket) &&
                Objects.equals(troubleTicketState, alarmDTO.troubleTicketState) &&
                Objects.equals(troubleTicketLink, alarmDTO.troubleTicketLink) &&
                Objects.equals(mouseOverText, alarmDTO.mouseOverText) &&
                Objects.equals(suppressedUntil, alarmDTO.suppressedUntil) &&
                Objects.equals(suppressedBy, alarmDTO.suppressedBy) &&
                Objects.equals(suppressedTime, alarmDTO.suppressedTime) &&
                Objects.equals(ackUser, alarmDTO.ackUser) &&
                Objects.equals(ackTime, alarmDTO.ackTime) &&
                Objects.equals(clearKey, alarmDTO.clearKey) &&
                Objects.equals(lastEvent, alarmDTO.lastEvent) &&
                Objects.equals(parameters, alarmDTO.parameters) &&
                Objects.equals(lastEventTime, alarmDTO.lastEventTime) &&
                Objects.equals(applicationDN, alarmDTO.applicationDN) &&
                Objects.equals(managedObjectInstance, alarmDTO.managedObjectInstance) &&
                Objects.equals(managedObjectType, alarmDTO.managedObjectType) &&
                Objects.equals(ossPrimaryKey, alarmDTO.ossPrimaryKey) &&
                Objects.equals(x733AlarmType, alarmDTO.x733AlarmType) &&
                Objects.equals(x733ProbableCause, alarmDTO.x733ProbableCause) &&
                Objects.equals(qosAlarmState, alarmDTO.qosAlarmState) &&
                Objects.equals(firstAutomationTime, alarmDTO.firstAutomationTime) &&
                Objects.equals(lastAutomationTime, alarmDTO.lastAutomationTime) &&
                Objects.equals(ifIndex, alarmDTO.ifIndex) &&
                Objects.equals(reductionKeyMemo, alarmDTO.reductionKeyMemo) &&
                Objects.equals(stickyMemo, alarmDTO.stickyMemo) &&
                Objects.equals(relatedAlarms, alarmDTO.relatedAlarms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uei, location, nodeId, nodeLabel, ipAddress, serviceType, reductionKey, type, count, severity,
                            firstEventTime, description, logMessage, operatorInstructions, troubleTicket, troubleTicketState,
                            troubleTicketLink, mouseOverText, suppressedUntil, suppressedBy, suppressedTime, ackUser, ackTime,
                            clearKey, lastEvent, parameters, lastEventTime, applicationDN, managedObjectInstance, managedObjectType,
                            ossPrimaryKey, x733AlarmType, x733ProbableCause, qosAlarmState, firstAutomationTime, lastAutomationTime,
                            ifIndex, reductionKeyMemo, stickyMemo, relatedAlarms);
    }
}
