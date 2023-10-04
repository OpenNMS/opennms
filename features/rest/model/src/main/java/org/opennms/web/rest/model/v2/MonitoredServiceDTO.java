/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2023 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2023 The OpenNMS Group, Inc.
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

import java.util.Date;
import java.util.Objects;

import javax.xml.bind.annotation.XmlAttribute;

import org.codehaus.jackson.annotate.JsonProperty;

public class MonitoredServiceDTO {
    @XmlAttribute(name="id")
    private Integer id;

    @XmlAttribute(name="down")
    private Boolean down;

    @XmlAttribute(name="notify")
    private String notify;

    @XmlAttribute(name="status")
    private String status;

    @XmlAttribute(name="source")
    private String source;

    @XmlAttribute(name="serviceType")
    private ServiceTypeDTO serviceType;

    @XmlAttribute(name="qualifier")
    private String qualifier;

    @XmlAttribute(name="lastFail")
    private Date lastFail;

    @XmlAttribute(name="lastGood")
    private Date lastGood;

    @XmlAttribute(name="statusLong")
    private String statusLong;

    @XmlAttribute(name="ipInterfaceId")
    private Integer ipInterfaceId;

    @XmlAttribute(name="ipAddress")
    private String ipAddress;

    @XmlAttribute(name="nodeId")
    private Integer nodeId;

    @XmlAttribute(name="nodeLabel")
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
