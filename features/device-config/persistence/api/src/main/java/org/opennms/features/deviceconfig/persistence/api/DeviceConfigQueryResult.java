/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.features.deviceconfig.persistence.api;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.OnmsNode;

import java.util.Date;

public class DeviceConfigQueryResult {
    private Long id;
    private Integer ipInterfaceId;
    private Date createdTime;
    private String configType;
    private Date lastUpdated;
    private Date lastSucceeded;
    private Date lastFailed;
    private String encoding;
    private String filename;
    private String failureReason;
    private byte[] config;
    private String serviceName;
    private String ipAddr;
    private Integer nodeId;
    private String nodeLabel;
    private String operatingSystem;
    private String location;

    public Long getId() { return this.id; }
    public Integer getIpInterfaceId() { return this.ipInterfaceId; }
    public Date getCreatedTime() { return this.createdTime; }
    public String getConfigType() { return this.configType; }
    public Date getLastUpdated() { return this.lastUpdated; }
    public Date getLastSucceeded() { return this.lastSucceeded; }
    public Date getLastFailed() { return this.lastFailed; }
    public String getEncoding() { return this.encoding; }
    public String getFilename() { return this.filename; }
    public String getFailureReason() { return this.failureReason; }
    public byte[] getConfig() { return this.config; }
    public String getServiceName() { return this.serviceName; }
    public String getIpAddr() { return this.ipAddr; }
    public Integer getNodeId() { return this.nodeId; }
    public String getNodeLabel() { return this.nodeLabel; }
    public String getOperatingSystem() { return this.operatingSystem; }
    public String getLocation() { return this.location; }

    public void setId(Long n) { this.id = n; }
    public void setIpInterfaceId(Integer n) { this.ipInterfaceId = n; }
    public void setCreatedTime(Date createdTime) { this.createdTime = createdTime; }
    public void setConfigType(String configType) { this.configType = configType; }
    public void setLastUpdated(Date lastUpdated) { this.lastUpdated = lastUpdated; }
    public void setLastSucceeded(Date lastSucceeded) { this.lastSucceeded = lastSucceeded; }
    public void setLastFailed(Date lastFailed) { this.lastFailed = lastFailed; }
    public void setEncoding(String encoding) { this.encoding = encoding; }
    public void setFilename(String filename) { this.filename = filename; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }
    public void setConfig(byte[] config) { this.config = config; }
    public void setServiceName(String serviceName) { this.serviceName = serviceName; }
    public void setIpAddr(String s) { this.ipAddr = s; }
    public void setNodeId(Integer n) { this.nodeId = n; }
    public void setNodeLabel(String s) { this.nodeLabel = s; }
    public void setOperatingSystem(String s) { this.operatingSystem = s; }
    public void setLocation(String s) { this.location = s; }

    public DeviceConfigQueryResult() {
    }

    public DeviceConfigQueryResult(DeviceConfig dc, OnmsIpInterface ip, OnmsNode node) {
        this.id = dc.getId();
        this.ipInterfaceId = ip.getId();
        this.createdTime = dc.getCreatedTime();
        this.configType = dc.getConfigType();
        this.lastUpdated = dc.getLastUpdated();
        this.lastSucceeded = dc.getLastSucceeded();
        this.lastFailed = dc.getLastFailed();
        this.encoding = dc.getEncoding();
        this.filename = dc.getFileName();
        this.failureReason = dc.getFailureReason();
        this.config = dc.getConfig();
        this.serviceName = dc.getServiceName();
        this.ipAddr = InetAddressUtils.str(ip.getIpAddress());
        this.nodeId = node.getId();
        this.nodeLabel = node.getLabel();
        this.operatingSystem = node.getOperatingSystem();
        this.location = node.getLocation().getLocationName();
    }
}
