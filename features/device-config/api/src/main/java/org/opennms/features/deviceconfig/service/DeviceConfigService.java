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

import org.opennms.netmgt.poller.DeviceConfig;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface DeviceConfigService {
    public static final String DEVICE_CONFIG_PREFIX = "DeviceConfig";

    /**
     *   Trigger device config backup for the given ipAddress at given location.
     *
     * @param ipAddress  specific IpAddress for which we need to fetch device config.
     * @param location   specific minion location at which we need to fetch device config.
     * @param service    name of the bound service.
     * @param persist
     * @throws IOException
     * @return
     */
    CompletableFuture<Boolean> triggerConfigBackup(String ipAddress, String location, String service, boolean persist) throws IOException;

    /**
     * Get device config for the given ipAddress at given location.
     *
     * @param ipAddress  specific IpAddress for which we need to fetch device config.
     * @param location   specific minion location at which we need to fetch device config.
     * @param service    name of the bound service.
     * @param persist
     * @param timeout    timeout in milliseconds for retrieving device config
     * @throws IOException
     * @return
     */
    CompletableFuture<DeviceConfig> getDeviceConfig(String ipAddress, String location, String service, boolean persist, int timeout) throws IOException;

    /**
     * Gets the backup jobs defined for the given interface.
     *
     * @param ipAddress the IP address of the interface.
     * @param location the location of the interface.
     * @return the {@link RetrievalDefinition}s for this interface
     */
    List<RetrievalDefinition> getRetrievalDefinitions(String ipAddress, String location);

    /**
     * Definition of a backup job.
     */
    interface RetrievalDefinition {
        String getServiceName();
        String getConfigType();
        String getSchedule();
    }
}
