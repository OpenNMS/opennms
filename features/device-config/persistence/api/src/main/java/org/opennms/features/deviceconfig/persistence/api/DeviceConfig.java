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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;
import org.opennms.netmgt.model.OnmsIpInterface;

@Entity
@Table(name = "device_config")
public class DeviceConfig implements Serializable {

    private static final long serialVersionUID = 1078656993339537763L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "deviceConfigSequence")
    @SequenceGenerator(name = "deviceConfigSequence", sequenceName = "deviceconfignxtid")
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "ipinterface_id")
    private OnmsIpInterface ipInterface;

    @Column(name = "service_name", nullable = true)
    private String serviceName;

    @Type(type = "org.hibernate.type.BinaryType")
    @Column(name = "config")
    private byte[] config;

    @Column(name = "encoding", nullable = false)
    private String encoding;

    @Column(name = "config_type", nullable = false)
    private String configType;

    @Column(name = "filename")
    private String fileName;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "created_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdTime;

    @Column(name = "last_updated")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastUpdated;

    @Column(name = "last_failed")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastFailed;

    @Column(name = "last_succeeded")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastSucceeded;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private DeviceConfigStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public OnmsIpInterface getIpInterface() {
        return ipInterface;
    }

    public void setIpInterface(OnmsIpInterface ipInterface) {
        this.ipInterface = ipInterface;
    }

    public String getServiceName() {
        return this.serviceName;
    }

    public void setServiceName(final String serviceName) {
        this.serviceName = serviceName;
    }

    public byte[] getConfig() {
        return config;
    }

    public void setConfig(byte[] config) {
        this.config = config;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getConfigType() {
        return configType;
    }

    public String getFileName() { return fileName; }

    public void setConfigType(String configType) {
        this.configType = configType;
    }

    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Date getLastFailed() {
        return lastFailed;
    }

    public void setLastFailed(Date lastFailed) {
        this.lastFailed = lastFailed;
    }

    public Date getLastSucceeded() {
        return lastSucceeded;
    }

    public void setLastSucceeded(Date lastSucceeded) {
        this.lastSucceeded = lastSucceeded;
    }

    public DeviceConfigStatus getStatus() { return this.status; }

    public void setStatus(DeviceConfigStatus status) { this.status = status; }

    public DeviceConfigStatus getStatusOrDefault() {
        return this.status != null ? this.status : DeviceConfigStatus.NONE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DeviceConfig that = (DeviceConfig) o;
        return Objects.equals(createdTime, that.createdTime) &&
               Objects.equals(lastUpdated, that.lastUpdated) &&
               Objects.equals(lastFailed, that.lastFailed) &&
               Objects.equals(lastSucceeded, that.lastSucceeded) &&
               Objects.equals(encoding, that.encoding) &&
               Objects.equals(configType, that.configType) &&
               Objects.equals(fileName, that.fileName) &&
               Objects.equals(failureReason, that.failureReason) &&
               Objects.equals(ipInterface, that.ipInterface) &&
               Objects.equals(serviceName, that.serviceName) &&
               Objects.equals(status, that.status) &&
               Arrays.equals(config, that.config);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(ipInterface, serviceName, encoding, configType, fileName, failureReason, createdTime, lastUpdated, lastFailed, lastSucceeded, status);
        result = 31 * result + Arrays.hashCode(config);
        return result;
    }
}
