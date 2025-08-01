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

import org.opennms.netmgt.dao.api.OnmsDao;
import org.opennms.netmgt.model.OnmsIpInterface;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


public interface DeviceConfigDao extends OnmsDao<DeviceConfig, Long> {

    List<DeviceConfig> findConfigsForInterfaceSortedByDate(OnmsIpInterface ipInterface, String serviceName);

    /**
     * Find device configs for the given interface and service, excluding the given 'excludedId' if
     * present, that are older than the 'staleDate'.
     * Used to find state device configs - records that are not the latest good config and are
     * older than the staleDate.
     * @param ipInterface The {@link OnmsIpInterface} to filter on
     * @param serviceName The name of the service to use
     * @param staleDate Records returned will be older than this date
     * @param excludedId If supplied, an id of a {@link DeviceConfig} record to exclude
     */
    List<DeviceConfig> findStaleConfigs(OnmsIpInterface ipInterface, String serviceName,
        Date staleDate, Optional<Long> excludedId);

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

    List<DeviceConfig> getAllDeviceConfigsWithAnInterfaceId(Integer ipInterfaceId);

    Map<String, Long> getNumberOfNodesWithDeviceConfigBySysOid();

    /**
     * Update the content of the specific device config.
     * @return An {@link Optional} containing the id of the {@link DeviceConfig} record that was updated.
     */
    Optional<Long> updateDeviceConfigContent(
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

    void deleteDeviceConfigs(Collection<DeviceConfig> entities);
}
