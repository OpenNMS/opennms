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

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public interface DeviceConfigService {

    /**
     *   Trigger device config backup for the given ipAddress at given location.
     *
     * @param ipAddress  specific IpAddress for which we need to fetch device config.
     * @param location   specific minion location at which we need to fetch device config.
     * @param configType  configType whether it is Default or Running.
     * @throws IOException
     */
    void triggerConfigBackup(String ipAddress, String location, String configType) throws IOException;

    /**
     * Get device config for the given ipAddress at given location.
     *
     * @param ipAddress  specific IpAddress for which we need to fetch device config.
     * @param location   specific minion location at which we need to fetch device config.
     * @param configType configType whether it is Default or Running.
     * @param timeout    timeout in milliseconds for retrieving device config
     * @throws IOException
     * @return
     */
    CompletableFuture<byte[]> getDeviceConfig(String ipAddress, String location, String configType, int timeout) throws IOException;
}
