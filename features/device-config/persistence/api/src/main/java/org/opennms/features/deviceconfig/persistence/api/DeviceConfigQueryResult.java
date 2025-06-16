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
    private DeviceConfigStatus status;

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
    public DeviceConfigStatus getStatus() { return this.status; }
    public DeviceConfigStatus getStatusOrDefault() { return this.status != null ? this.status : DeviceConfigStatus.NONE; }

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
    public void setStatus(DeviceConfigStatus status) { this.status = status; }

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
        this.status = dc.getStatus();
    }
}
