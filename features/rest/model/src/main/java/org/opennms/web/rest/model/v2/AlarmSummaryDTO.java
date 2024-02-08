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
