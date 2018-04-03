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

@XmlRootElement(name = "event")
@XmlAccessorType(XmlAccessType.NONE)
public class EventDTO {

    @XmlAttribute(name = "id")
    private Integer id;

    @XmlElement(name = "uei")
    private String uei;

    @XmlElement(name = "label")
    private String label;

    @XmlElement(name = "time")
    private Date time;

    @XmlElement(name = "host")
    private String host;

    @XmlElement(name = "source")
    private String source;

    @XmlElement(name = "ipAddress")
    @XmlJavaTypeAdapter(InetAddressXmlAdapter.class)
    private InetAddress ipAddress;

    @XmlElement(name = "snmpHost")
    private String snmpHost;

    @XmlElement(name = "serviceType")
    private ServiceTypeDTO serviceType;

    @XmlElement(name = "snmp")
    private String snmp;

    @XmlElementWrapper(name = "parameters")
    @XmlElement(name = "parameter")
    private List<EventParameterDTO> parameters;

    @XmlElement(name = "createTime")
    private Date createTime;

    @XmlElement(name = "description")
    private String description;

    @XmlElement(name = "logGroup")
    private String logGroup;

    @XmlElement(name = "logMessage")
    private String logMessage;

    @XmlAttribute(name = "severity")
    private String severity;

    @XmlElement(name = "pathOutage")
    private String pathOutage;

    @XmlElement(name = "correlation")
    private String correlation;

    @XmlElement(name = "suppressedCount")
    private Integer suppressedCount;

    @XmlElement(name = "operatorInstructions")
    private String operatorInstructions;

    @XmlElement(name = "autoAction")
    private String autoAction;

    @XmlElement(name = "operatorAction")
    private String operatorAction;

    @XmlElement(name = "operationActionMenuText")
    private String operationActionMenuText;

    @XmlElement(name = "notification")
    private String notification;

    @XmlElement(name = "troubleTicket")
    private String troubleTicket;

    @XmlElement(name = "troubleTicketState")
    private Integer troubleTicketState;

    @XmlElement(name = "mouseOverText")
    private String mouseOverText;

    @XmlAttribute(name = "log")
    private String log;

    @XmlAttribute(name = "display")
    private String display;

    @XmlElement(name = "ackUser")
    private String ackUser;

    @XmlElement(name = "ackTime")
    private Date ackTime;

    @XmlElement(name = "nodeId")
    private Integer nodeId;

    @XmlElement(name = "nodeLabel")
    private String nodeLabel;

    @XmlElement(name = "ifIndex")
    private Integer ifIndex;

    @XmlElement(name = "location")
    private String location;

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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public InetAddress getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(InetAddress ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getSnmpHost() {
        return snmpHost;
    }

    public void setSnmpHost(String snmpHost) {
        this.snmpHost = snmpHost;
    }

    public ServiceTypeDTO getServiceType() {
        return serviceType;
    }

    public void setServiceType(ServiceTypeDTO serviceType) {
        this.serviceType = serviceType;
    }

    public String getSnmp() {
        return snmp;
    }

    public void setSnmp(String snmp) {
        this.snmp = snmp;
    }

    public List<EventParameterDTO> getParameters() {
        return parameters;
    }

    public void setParameters(List<EventParameterDTO> parameters) {
        this.parameters = parameters;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogGroup() {
        return logGroup;
    }

    public void setLogGroup(String logGroup) {
        this.logGroup = logGroup;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getPathOutage() {
        return pathOutage;
    }

    public void setPathOutage(String pathOutage) {
        this.pathOutage = pathOutage;
    }

    public String getCorrelation() {
        return correlation;
    }

    public void setCorrelation(String correlation) {
        this.correlation = correlation;
    }

    public Integer getSuppressedCount() {
        return suppressedCount;
    }

    public void setSuppressedCount(Integer suppressedCount) {
        this.suppressedCount = suppressedCount;
    }

    public String getOperatorInstructions() {
        return operatorInstructions;
    }

    public void setOperatorInstructions(String operatorInstructions) {
        this.operatorInstructions = operatorInstructions;
    }

    public String getAutoAction() {
        return autoAction;
    }

    public void setAutoAction(String autoAction) {
        this.autoAction = autoAction;
    }

    public String getOperatorAction() {
        return operatorAction;
    }

    public void setOperatorAction(String operatorAction) {
        this.operatorAction = operatorAction;
    }

    public String getOperationActionMenuText() {
        return operationActionMenuText;
    }

    public void setOperationActionMenuText(String operationActionMenuText) {
        this.operationActionMenuText = operationActionMenuText;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
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

    public String getMouseOverText() {
        return mouseOverText;
    }

    public void setMouseOverText(String mouseOverText) {
        this.mouseOverText = mouseOverText;
    }

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
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

    public Integer getIfIndex() {
        return ifIndex;
    }

    public void setIfIndex(Integer ifIndex) {
        this.ifIndex = ifIndex;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EventDTO eventDTO = (EventDTO) o;
        return Objects.equals(id, eventDTO.id) &&
                Objects.equals(uei, eventDTO.uei) &&
                Objects.equals(label, eventDTO.label) &&
                Objects.equals(time, eventDTO.time) &&
                Objects.equals(host, eventDTO.host) &&
                Objects.equals(source, eventDTO.source) &&
                Objects.equals(ipAddress, eventDTO.ipAddress) &&
                Objects.equals(snmpHost, eventDTO.snmpHost) &&
                Objects.equals(serviceType, eventDTO.serviceType) &&
                Objects.equals(snmp, eventDTO.snmp) &&
                Objects.equals(parameters, eventDTO.parameters) &&
                Objects.equals(createTime, eventDTO.createTime) &&
                Objects.equals(description, eventDTO.description) &&
                Objects.equals(logGroup, eventDTO.logGroup) &&
                Objects.equals(logMessage, eventDTO.logMessage) &&
                Objects.equals(severity, eventDTO.severity) &&
                Objects.equals(pathOutage, eventDTO.pathOutage) &&
                Objects.equals(correlation, eventDTO.correlation) &&
                Objects.equals(suppressedCount, eventDTO.suppressedCount) &&
                Objects.equals(operatorInstructions, eventDTO.operatorInstructions) &&
                Objects.equals(autoAction, eventDTO.autoAction) &&
                Objects.equals(operatorAction, eventDTO.operatorAction) &&
                Objects.equals(operationActionMenuText, eventDTO.operationActionMenuText) &&
                Objects.equals(notification, eventDTO.notification) &&
                Objects.equals(troubleTicket, eventDTO.troubleTicket) &&
                Objects.equals(troubleTicketState, eventDTO.troubleTicketState) &&
                Objects.equals(mouseOverText, eventDTO.mouseOverText) &&
                Objects.equals(log, eventDTO.log) &&
                Objects.equals(display, eventDTO.display) &&
                Objects.equals(ackUser, eventDTO.ackUser) &&
                Objects.equals(ackTime, eventDTO.ackTime) &&
                Objects.equals(nodeId, eventDTO.nodeId) &&
                Objects.equals(nodeLabel, eventDTO.nodeLabel) &&
                Objects.equals(ifIndex, eventDTO.ifIndex) &&
                Objects.equals(location, eventDTO.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, uei, label, time, host, source, ipAddress, snmpHost, serviceType, snmp, parameters, createTime, description, logGroup, logMessage, severity, pathOutage, correlation, suppressedCount, operatorInstructions, autoAction, operatorAction, operationActionMenuText, notification, troubleTicket, troubleTicketState, mouseOverText, log, display, ackUser, ackTime, nodeId, nodeLabel, ifIndex, location);
    }
}
