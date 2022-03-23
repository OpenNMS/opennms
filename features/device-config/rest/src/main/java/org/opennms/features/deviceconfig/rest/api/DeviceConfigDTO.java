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
    private final long id;

    /** Database id of monitored service */
    private final int monitoredServiceId;

    /** IP address as a text string */
    private final String ipAddress;

    /** Date backup config data was last stored. */
    private final Date lastBackupDate;

    /** Date of last backup attempt. */
    private final Date lastUpdatedDate;

    /**
     * Date backup last succeeded, or null if no backups succeeded.
     * If the backup process succeeded but the configuration did not change, this date could be
     * more recent than 'createdTime'.
     */
    private final Date lastSucceededDate;

    /** Date backup last failed, or null if no backups failed. */
    private final Date lastFailedDate;

    /** Encoding of the config, possible values include 'text', 'binary', 'ascii', 'json', 'xml'. */
    private final String encoding;

    /** The device configuration type, either 'default' or 'running'. */
    private final String configType;

    /** Filename of the configuration data as received from the device. */
    private final String fileName;

    /** Configuration data as a string */
    private final String config;

    /** Failure reason description, if there was a backup failure. */
    private final String failureReason;

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

    public DeviceConfigDTO(
        long id,
        int monitoredServiceId,
        String ipAddress,
        Date createdTime,
        Date lastUpdated,
        Date lastSucceeded,
        Date lastFailed,
        String encoding,
        String configType,
        String fileName,
        String config,
        String failureReason
    ) {
        this.id = id;
        this.monitoredServiceId = monitoredServiceId;
        this.ipAddress = ipAddress;
        this.lastBackupDate = createdTime;
        this.lastUpdatedDate = lastUpdated;
        this.lastSucceededDate = lastSucceeded;
        this.lastFailedDate = lastFailed;
        this.encoding = encoding;
        this.configType = configType;
        this.fileName = fileName;
        this.config = config;
        this.failureReason = failureReason;
    }

    public long getId() {
        return id;
    }

    public int getMonitoredServiceId() { return this.monitoredServiceId; }

    public String getIpAddress() { return this.ipAddress; }

    public Date getLastBackupDate() { return this.lastBackupDate; }

    public Date getLastUpdatedDate() { return this.lastUpdatedDate; }

    public Date getLastSucceededDate() { return this.lastSucceededDate; }

    public Date getLastFailedDate() { return this.lastFailedDate; }

    public String getEncoding() { return this.encoding; }

    public String getConfigType() { return this.configType; }

    public String getFileName() { return this.fileName; }

    public String getConfig() { return config; }

    public String getFailureReason() { return this.failureReason; }

    public String getDeviceName(){ return this.deviceName; }

    public Integer getIpInterfaceId() { return this.ipInterfaceId; }

    public Integer getNodeId() { return this.nodeId; }

    public String getNodeLabel() { return this.nodeLabel; }

    public String getLocation() { return this.location; }

    public String getOperatingSystem() { return this.operatingSystem; }

    public boolean getIsSuccessfulBackup() { return this.isSuccessfulBackup; }

    public String getBackupStatus() { return this.backupStatus; }

    public Date getNextScheduledBackupDate() { return this.nextScheduledBackupDate; }

    public Map<String, String> getScheduledInterval() { return this.scheduledInterval; }

    public void setDeviceName(String name) { this.deviceName = name; }

    public void setIpInterfaceId(Integer id) { this.ipInterfaceId = id; }

    public void setNodeId(Integer id) { this.nodeId = id; }

    public void setNodeLabel(String label) { this.nodeLabel = label; }

    public void setLocation(String location) { this.location = location; }

    public void setOperatingSystem(String os) { this.operatingSystem = os; }

    public void setIsSuccessfulBackup(boolean b) { this.isSuccessfulBackup = b; }

    public void setBackupStatus(String status) { this.backupStatus = status; }

    public void setNextScheduledBackupDate(Date date) { this.nextScheduledBackupDate = date; }

    public void setScheduledInterval(Map<String, String> interval) { this.scheduledInterval = interval; }
}
