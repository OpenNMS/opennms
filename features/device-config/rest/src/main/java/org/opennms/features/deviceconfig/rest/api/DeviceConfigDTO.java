/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2022 The OpenNMS Group, Inc.
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
 *     http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.deviceconfig.rest.api;

import java.util.Date;
import java.util.Map;

/**
 * DTO for response value of DeviceConfigRestService.
 * 'final' properties are retrieved directly from 'device_config' table.
 * Other properties are calculated or from other data sources.
 * Configuration data itself is returned as text.
 */
public class DeviceConfigDTO {
    /** Database id of 'device_config' table entry */
    private long id;

    /** Name of monitored service */
    private String serviceName;

    /** IP address as a text string */
    private String ipAddress;

    /** Date backup config data was last stored. */
    private Date lastBackupDate;

    /** Date of last backup attempt. */
    private Date lastUpdatedDate;

    /**
     * Date backup last succeeded, or null if no backups succeeded.
     * If the backup process succeeded but the configuration did not change, this date could be
     * more recent than 'createdTime'.
     */
    private Date lastSucceededDate;

    /** Date backup last failed, or null if no backups failed. */
    private Date lastFailedDate;

    /** Encoding of the config, possible values include 'text', 'binary', 'ascii', 'json', 'xml'. */
    private String encoding;

    /** The device configuration type, either 'default' or 'running'. */
    private String configType;

    /** Filename of the configuration data as received from the device. */
    private String fileName;

    /** Configuration data as a string */
    private String config;

    /** Failure reason description, if there was a backup failure. */
    private String failureReason;

    /** Device name. Currently this is actually the nodeLabel. */
    private String deviceName;

    /** Database id of corresponding node table entry. */
    private Integer ipInterfaceId;

    /** Database id of corresponding node table entry. */
    private Integer nodeId;

    /** Label/name of the corresponding node object. */
    private String nodeLabel;

    /** Location, from node. */
    private String location;

    /** Operating system of corresponding node. */
    private String operatingSystem;

    /** True if there are any successful backups */
    private boolean isSuccessfulBackup;

    /** Description of most recent backup status. 'success', 'failure'. May add others like 'paused', 'storing'. */
    private String backupStatus;

    /** Estimate of next scheduled backup date. */
    private Date nextScheduledBackupDate;

    /** Friendly description of backup schedule interval, parsed from cron schedule data. */
    private Map<String, String> scheduledInterval;

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getServiceName() { return this.serviceName; }

    public void setServiceName(final String serviceName) {
        this.serviceName = serviceName;
    }

    public boolean isSuccessfulBackup() {
        return this.isSuccessfulBackup;
    }

    public void setSuccessfulBackup(final boolean successfulBackup) {
        isSuccessfulBackup = successfulBackup;
    }

    public String getIpAddress() { return this.ipAddress; }

    public void setIpAddress(final String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Date getLastBackupDate() { return this.lastBackupDate; }

    public void setLastBackupDate(final Date lastBackupDate) {
        this.lastBackupDate = lastBackupDate;
    }

    public Date getLastUpdatedDate() { return this.lastUpdatedDate; }

    public void setLastUpdatedDate(final Date lastUpdatedDate) {
        this.lastUpdatedDate = lastUpdatedDate;
    }

    public Date getLastSucceededDate() { return this.lastSucceededDate; }

    public void setLastSucceededDate(final Date lastSucceededDate) {
        this.lastSucceededDate = lastSucceededDate;
    }

    public Date getLastFailedDate() { return this.lastFailedDate; }

    public void setLastFailedDate(final Date lastFailedDate) {
        this.lastFailedDate = lastFailedDate;
    }

    public String getEncoding() { return this.encoding; }

    public void setEncoding(final String encoding) {
        this.encoding = encoding;
    }

    public String getConfigType() { return this.configType; }

    public void setConfigType(final String configType) {
        this.configType = configType;
    }

    public String getFileName() { return this.fileName; }

    public void setFileName(final String fileName) {
        this.fileName = fileName;
    }

    public String getConfig() { return config; }

    public void setConfig(final String config) {
        this.config = config;
    }

    public String getFailureReason() { return this.failureReason; }

    public void setFailureReason(final String failureReason) {
        this.failureReason = failureReason;
    }

    public String getDeviceName(){ return this.deviceName; }

    public void setDeviceName(String name) { this.deviceName = name; }

    public Integer getIpInterfaceId() { return this.ipInterfaceId; }

    public void setIpInterfaceId(Integer id) { this.ipInterfaceId = id; }

    public Integer getNodeId() { return this.nodeId; }

    public void setNodeId(Integer id) { this.nodeId = id; }

    public String getNodeLabel() { return this.nodeLabel; }

    public void setNodeLabel(String label) { this.nodeLabel = label; }

    public String getLocation() { return this.location; }

    public void setLocation(String location) { this.location = location; }

    public String getOperatingSystem() { return this.operatingSystem; }

    public void setOperatingSystem(String os) { this.operatingSystem = os; }

    public boolean getIsSuccessfulBackup() { return this.isSuccessfulBackup; }

    public void setIsSuccessfulBackup(boolean b) { this.isSuccessfulBackup = b; }

    public String getBackupStatus() { return this.backupStatus; }

    public void setBackupStatus(String status) { this.backupStatus = status; }

    public Date getNextScheduledBackupDate() { return this.nextScheduledBackupDate; }

    public void setNextScheduledBackupDate(Date date) { this.nextScheduledBackupDate = date; }

    public Map<String, String> getScheduledInterval() { return this.scheduledInterval; }

    public void setScheduledInterval(Map<String, String> interval) { this.scheduledInterval = interval; }
}
