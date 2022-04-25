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

import org.opennms.netmgt.dao.api.OnmsDao;
import org.opennms.netmgt.model.OnmsIpInterface;

import java.util.List;
import java.util.Set;
import java.util.Optional;

public interface DeviceConfigDao extends OnmsDao<DeviceConfig, Long> {

    List<DeviceConfig> findConfigsForInterfaceSortedByDate(OnmsIpInterface ipInterface, String serviceName);

    Optional<DeviceConfig> getLatestConfigForInterface(OnmsIpInterface ipInterface, String serviceName);

    /**
     * Get latest device configuration for each interface. Returns a single record per device/config type combination.
     *
     * @param limit Limit of number of records to return; defaults to 20
     * @param offset Zero-based offset of records to return, used for pagination; defaults to 0.
     * @param orderBy Property to order by, see implementation for exact options. Default is "lastUpdated".
     * @param sortOrder Sort order for the sort type specified in 'orderBy'. Options are "desc" and "asc", defaults to "desc"
     * @param searchTerm Search term to filter by, Currently searches device name and ip address.
     * @param statuses If provided, a list of {@link DeviceConfigStatus} to filter on. If null or empty,
     *                     does not do any filtering.
     * @return A list of {@link DeviceConfigQueryResult } objects
     */
    List<DeviceConfigQueryResult> getLatestConfigForEachInterface(Integer limit, Integer offset, String orderBy,
        String sortOrder, String searchTerm, Set<DeviceConfigStatus> statuses);

    int getLatestConfigCountForEachInterface(String searchTerm, Set<DeviceConfigStatus> statuses);

    void updateDeviceConfigContent(
            OnmsIpInterface ipInterface,
            String serviceName,
            String configType,
            String encoding,
            byte[] deviceConfigBytes,
            String filename
    );

    void updateDeviceConfigFailure(
            OnmsIpInterface ipInterface,
            String serviceName,
            String configType,
            String encoding,
            String reason
    );

    void createEmptyDeviceConfig(
            OnmsIpInterface ipInterface,
            String serviceName,
            String configType);
}
