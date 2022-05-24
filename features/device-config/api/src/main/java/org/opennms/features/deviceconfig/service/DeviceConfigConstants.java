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

package org.opennms.features.deviceconfig.service;

public interface DeviceConfigConstants {
    static final String TRIGGERED_POLL = "dcbTriggeredPoll";
    static final String CONFIG_TYPE = "config-type";
    static final String SCHEDULE = "schedule";
    static final String DEFAULT_CRON_SCHEDULE = "0 0 0 * * ?";
    static final String NEVER = "never";
    static final String RETENTION_PERIOD = "retention-period";
    static final String REST = "REST";
    static final String CRON = "cron";
    static final String PARM_DEVICE_CONFIG_BACKUP_START_TIME = "backupStartTime";
    static final String PARM_DEVICE_CONFIG_BACKUP_DATA_PROTOCOL = "backupDataProtocol";
    static final String PARM_DEVICE_CONFIG_BACKUP_CONTROL_PROTOCOL = "backupControlProtocol";
}
