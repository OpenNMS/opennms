/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2018 The OpenNMS Group, Inc.
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

package org.opennms.web.rest.model.v2;

import java.beans.Transient;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="alarm")
@XmlAccessorType(XmlAccessType.NONE)
public class AlarmSummaryDTO {

    @XmlAttribute(name="id")
    private Integer id;

    @XmlAttribute(name="type")
    private Integer type;

    @XmlAttribute(name="severity")
    private String severity;

    @XmlElement(name="reductionKey")
    private String reductionKey;

    @XmlElement(name="description")
    private String description;

    @XmlElement(name="label")
    private String label;

    @XmlElement(name="nodeLabel")
    private String nodeLabel;

    @XmlElement(name="logMessage")
    private String logMessage;

    private String uei;

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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getNodeLabel() {
        return label;
    }

    public void setNodeLabel(final String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    public String getLogMessage() {
        return logMessage;
    }

    public void setLogMessage(String logMessage) {
        this.logMessage = logMessage;
    }

    @Transient
    public String getUei() {
        return uei;
    }

    public void setUei(String uei) {
        this.uei = uei;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlarmSummaryDTO alarmDTO = (AlarmSummaryDTO) o;
        return Objects.equals(id, alarmDTO.id) &&
                Objects.equals(reductionKey, alarmDTO.reductionKey) &&
                Objects.equals(type, alarmDTO.type) &&
                Objects.equals(severity, alarmDTO.severity) &&
                Objects.equals(description, alarmDTO.description) &&
                Objects.equals(label, alarmDTO.label) &&
                Objects.equals(nodeLabel, alarmDTO.nodeLabel) &&
                Objects.equals(logMessage, alarmDTO.logMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, reductionKey, type, severity, description, label, nodeLabel, logMessage);
    }

}
