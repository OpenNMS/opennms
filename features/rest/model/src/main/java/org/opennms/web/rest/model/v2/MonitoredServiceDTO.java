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

import java.util.Date;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonProperty;

@XmlRootElement(name="service")
@XmlAccessorType(XmlAccessType.NONE)
public class MonitoredServiceDTO {
    @XmlAttribute(name="id")
    private Integer id;

    @XmlElement(name="down")
    private Boolean down;

    @XmlElement(name="notify")
    private String notify;

    @XmlElement(name="status")
    private String status;

    @XmlElement(name="source")
    private String source;

    @XmlElement(name="serviceType")
    private ServiceTypeDTO serviceType;

    @XmlElement(name="qualifier")
    private String qualifier;

    @XmlElement(name="lastFail")
    private Date lastFail;

    @XmlElement(name="lastGood")
    private Date lastGood;

    @XmlElement(name="statusLong")
    private String statusLong;

    @XmlElement(name="ipInterfaceId")
    private Integer ipInterfaceId;

    @XmlElement(name="ipAddress")
    private String ipAddress;

    @XmlElement(name="nodeId")
    private Integer nodeId;

    @XmlElement(name="nodeLabel")
    private String nodeLabel;

    // Getters

    @JsonProperty("id")
    public Integer getId() {
        return id;
    }

    @JsonProperty("down")
    public Boolean getDown() {
        return down;
    }

    @JsonProperty("notify")
    public String getNotify() {
        return notify;
    }

    @JsonProperty("status")
    public String getStatus() {
        return status;
    }

    @JsonProperty("source")
    public String getSource() {
        return source;
    }

    @JsonProperty("serviceType")
    public ServiceTypeDTO getServiceType() {
        return serviceType;
    }

    @JsonProperty("qualifier")
    public String getQualifier() {
        return qualifier;
    }

    @JsonProperty("lastFail")
    public Date getLastFail() {
        return lastFail;
    }

    @JsonProperty("lastGood")
    public Date getLastGood() {
        return lastGood;
    }

    @JsonProperty("statusLong")
    public String getStatusLong() {
        return statusLong;
    }

    @JsonProperty("ipInterfaceId")
    public Integer getIpInterfaceId() {
        return ipInterfaceId;
    }

    @JsonProperty("ipAddress")
    public String getIpAddress() {
        return ipAddress;
    }

    @JsonProperty("nodeId")
    public Integer getNodeId() {
        return nodeId;
    }

    @JsonProperty("nodeLabel")
    public String getNodeLabel() {
        return nodeLabel;
    }

    // Setters

    public void setId(Integer id) {
        this.id = id;
    }

    public void setDown(Boolean down) {
        this.down = down;
    }

    public void setNotify(String notify) {
        this.notify = notify;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public void setServiceType(ServiceTypeDTO dto) {
        this.serviceType = dto;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    public void setLastFail(Date lastFail) {
        this.lastFail = lastFail;
    }

    public void setLastGood(Date lastGood) {
        this.lastGood = lastGood;
    }

    public void setStatusLong(String statusLong) {
        this.statusLong = statusLong;
    }

    public void setIpInterfaceId(Integer ipInterfaceId) {
        this.ipInterfaceId = ipInterfaceId;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setNodeId(Integer nodeId) {
        this.nodeId = nodeId;
    }

    public void setNodeLabel(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MonitoredServiceDTO otherDTO = (MonitoredServiceDTO) o;

        return Objects.equals(id, otherDTO.id) &&
                Objects.equals(down, otherDTO.down) &&
                Objects.equals(notify, otherDTO.notify) &&
                Objects.equals(status, otherDTO.status) &&
                Objects.equals(source, otherDTO.source) &&
                Objects.equals(serviceType, otherDTO.serviceType) &&
                Objects.equals(qualifier, otherDTO.qualifier) &&
                Objects.equals(lastFail, otherDTO.lastFail) &&
                Objects.equals(lastGood, otherDTO.lastGood) &&
                Objects.equals(statusLong, otherDTO.statusLong) &&
                Objects.equals(ipInterfaceId, otherDTO.ipInterfaceId) &&
                Objects.equals(ipAddress, otherDTO.ipAddress) &&
                Objects.equals(nodeId, otherDTO.nodeId) &&
                Objects.equals(nodeLabel, otherDTO.nodeLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, down, notify, status, source, serviceType, qualifier, lastFail, lastGood,
                statusLong, ipInterfaceId, ipAddress, nodeId, nodeLabel);
    }
}
