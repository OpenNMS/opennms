/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonBackReference;
import org.opennms.netmgt.poller.PollStatus;

@Entity
@Table(name="scanReportPollResults")
@XmlRootElement(name="poll-result")
@XmlAccessorType(XmlAccessType.NONE)
public class ScanReportPollResult implements Serializable {
    private static final long serialVersionUID = 1L;

    @XmlAttribute(name="id")
    @XmlID
    private String m_id = UUID.randomUUID().toString();

    @XmlIDREF
    @XmlAttribute(name="scan-report-id")
    @JsonBackReference
    private ScanReport m_scanReport;

    @XmlAttribute(name="service-name")
    private String m_service;

    @XmlAttribute(name="service-id")
    private Integer m_serviceId;

    @XmlAttribute(name="node-label")
    private String m_nodeLabel;

    @XmlAttribute(name="node-id")
    private Integer m_nodeId;

    @XmlAttribute(name="ip-address")
    private String m_ipAddress;

    @XmlElement(name="poll-status")
    private PollStatus m_status;

    public ScanReportPollResult() {
    }

    public ScanReportPollResult(final String serviceName, final Integer serviceId, final String nodeLabel, final Integer nodeId, final String ipAddress, final PollStatus status) {
        m_service = serviceName;
        m_serviceId = serviceId;
        m_nodeLabel = nodeLabel;
        m_nodeId = nodeId;
        m_ipAddress = ipAddress;
        m_status = status;
    }

    public ScanReportPollResult(final ScanReport scanReport, final String serviceName, final Integer serviceId, final String nodeLabel, final Integer nodeId, final String ipAddress, final PollStatus status) {
        m_scanReport = scanReport;
        m_service = serviceName;
        m_serviceId = serviceId;
        m_nodeLabel = nodeLabel;
        m_nodeId = nodeId;
        m_ipAddress = ipAddress;
        m_status = status;
    }

    @Id
    @Column(name="id", unique=true)
    public String getId() {
        return m_id;
    }

    public void setId(final String id) {
        m_id = id;
    }

    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="scanReportId")
    public ScanReport getScanReport() {
        return m_scanReport;
    }

    public void setScanReport(final ScanReport report) {
        m_scanReport = report;
    }

    public String getServiceName() {
        return m_service;
    }

    public void setServiceName(final String serviceName) {
        m_service = serviceName;
    }

    public Integer getServiceId() {
        return m_serviceId;
    }

    public void setServiceId(final Integer serviceId) {
        m_serviceId = serviceId;
    }

    public String getNodeLabel() {
        return m_nodeLabel;
    }

    public void setNodeLabel(final String nodeLabel) {
        m_nodeLabel = nodeLabel;
    }

    public Integer getNodeId() {
        return m_nodeId;
    }

    public void setNodeId(final Integer nodeId) {
        m_nodeId = nodeId;
    }

    public String getIpAddress() {
        return m_ipAddress;
    }

    public void setIpAddress(final String ipAddress) {
        m_ipAddress = ipAddress;
    }

    @Embedded
    public PollStatus getPollStatus() {
        return m_status;
    }

    public void setPollStatus(final PollStatus ps) {
        m_status = ps;
    }

    @Transient
    @XmlTransient
    public String getResult() {
        if (m_status == null) {
            return "Unknown";
        }
        if (m_status.isUp()) {
            return "Success";
        } else {
            return "Failed: " + m_status.getReason();
        }
    }

    @Transient
    @XmlTransient
    public boolean isUp() {
        if (m_status != null) {
            return m_status.isUp();
        }
        return true;
    }

    @Transient
    @XmlTransient
    public boolean isAvailable() {
        if (m_status != null) {
            return m_status.isAvailable();
        }
        return true;
    }

    @Override
    public String toString() {
        return "ScanReportPollResult [service=" + m_service + ", serviceId=" + m_serviceId + ", nodeLabel=" + m_nodeLabel + ", nodeId=" + m_nodeId + ", ipAddress=" + m_ipAddress
                + ", status=" + m_status + "]";
    }
}
