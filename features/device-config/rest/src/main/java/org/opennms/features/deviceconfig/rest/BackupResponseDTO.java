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

@Schema(description = "A triggered backup that failed.")
public class BackupResponseDTO {

    @Schema(description = "HTTP status describing the failure: 400 when the request named an unknown interface or unbound service, 500 when the backup itself failed.", example = "400")
    private Integer status;
    @Schema(description = "The request that failed.")
    private BackupRequestDTO request;
    @Schema(description = "Why the backup failed.", example = "No interface found with ipAddress 10.0.0.5 at location Branch-Office")
    private String failureMessage;

    public BackupResponseDTO(Integer status, String failureMessage, BackupRequestDTO backupRequestDTO) {
        this.status = status;
        this.failureMessage = failureMessage;
        this.request = backupRequestDTO;
    }

    public BackupResponseDTO() {
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public BackupRequestDTO getRequest() {
        return request;
    }

    public void setRequest(BackupRequestDTO request) {
        this.request = request;
    }

    public String getFailureMessage() {
        return failureMessage;
    }

    public void setFailureMessage(String failureMessage) {
        this.failureMessage = failureMessage;
    }
}
