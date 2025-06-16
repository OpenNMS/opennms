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
package org.opennms.features.deviceconfig.service;

import org.opennms.netmgt.poller.DeviceConfig;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface DeviceConfigService {
    public static final String DEVICE_CONFIG_PREFIX = "DeviceConfig";

    /**
     * Trigger device config backup for the given ipAddress at given location.
     *
     * @param ipAddress specific IpAddress for which we need to fetch device config.
     * @param location  specific minion location at which we need to fetch device config.
     * @param service   name of the bound service.
     * @param persist
     * @return
     * @throws IOException
     */
    CompletableFuture<DeviceConfigBackupResponse> triggerConfigBackup(String ipAddress, String location, String service, boolean persist) throws IOException;

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

    public class DeviceConfigBackupResponse {
        private String errorStr;
        private String scriptOutput;

        public DeviceConfigBackupResponse(String error, String scriptOutput) {
            this.errorStr = error;
            this.scriptOutput = scriptOutput;
        }

        public String getErrorMessage() {
            return errorStr;
        }

        public String getScriptOutput() {
            return scriptOutput;
        }
    }
}
