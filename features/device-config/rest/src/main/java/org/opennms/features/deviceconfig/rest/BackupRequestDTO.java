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
package org.opennms.features.deviceconfig.rest;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "A device config backup to trigger.")
public class BackupRequestDTO {

    @Schema(description = "IP address of the interface to back up.", required = true, example = "192.168.1.1")
    private String ipAddress;

    @Schema(description = "Monitoring location of the interface.", required = true, example = "Default")
    private String location;

    @Schema(description = "`DeviceConfig` service bound to the interface that defines how to retrieve the configuration.", required = true, example = "DeviceConfig-running")
    private String serviceName;

    @Schema(description = "Wait for the retrieval to finish and report its failure, rather than returning once it is dispatched.", defaultValue = "false", example = "false")
    private Boolean blocking = false;

    public BackupRequestDTO(String ipAddress, String location, String serviceName) {
        this.ipAddress = ipAddress;
        this.location = location;
        this.serviceName = serviceName;
    }

    public BackupRequestDTO(String ipAddress, String location, String serviceName, boolean blocking) {
        this.ipAddress = ipAddress;
        this.location = location;
        this.serviceName = serviceName;
        this.blocking = blocking;
    }

    public BackupRequestDTO() {
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getLocation() {
        return location;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Boolean getBlocking() {
        return blocking;
    }

    public void setBlocking(Boolean blocking) {
        this.blocking = blocking;
    }
}
