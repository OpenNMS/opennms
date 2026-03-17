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
package org.opennms.web.rest.v2.model;

import org.opennms.netmgt.config.trapd.Snmpv3User;
import org.opennms.netmgt.config.trapd.TrapdConfiguration;

import java.util.List;

public class TrapdConfigDto {
    private String snmpTrapAddress;
    private Integer snmpTrapPort;
    private Boolean newSuspectOnTrap;
    private Boolean includeRawMessage;
    private Integer threads;
    private Integer queueSize;
    private Integer batchSize;
    private Integer batchInterval;
    private Boolean useAddressFromVarbind;
    private List<Snmpv3User> snmpv3User;

    public String getSnmpTrapAddress() {
        return snmpTrapAddress;
    }

    public void setSnmpTrapAddress(final String snmpTrapAddress) {
        this.snmpTrapAddress = snmpTrapAddress;
    }

    public Integer getSnmpTrapPort() {
        return snmpTrapPort;
    }

    public void setSnmpTrapPort(final Integer snmpTrapPort) {
        this.snmpTrapPort = snmpTrapPort;
    }

    public Boolean getNewSuspectOnTrap() {
        return newSuspectOnTrap;
    }

    public void setNewSuspectOnTrap(final Boolean newSuspectOnTrap) {
        this.newSuspectOnTrap = newSuspectOnTrap;
    }

    public Boolean getIncludeRawMessage() {
        return includeRawMessage;
    }

    public void setIncludeRawMessage(final Boolean includeRawMessage) {
        this.includeRawMessage = includeRawMessage;
    }

    public Integer getThreads() {
        return threads;
    }

    public void setThreads(final Integer threads) {
        this.threads = threads;
    }

    public Integer getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(final Integer queueSize) {
        this.queueSize = queueSize;
    }

    public Integer getBatchSize() {
        return batchSize;
    }

    public void setBatchSize(final Integer batchSize) {
        this.batchSize = batchSize;
    }

    public Integer getBatchInterval() {
        return batchInterval;
    }

    public void setBatchInterval(final Integer batchInterval) {
        this.batchInterval = batchInterval;
    }

    public Boolean getUseAddressFromVarbind() {
        return useAddressFromVarbind;
    }

    public void setUseAddressFromVarbind(final Boolean useAddressFromVarbind) {
        this.useAddressFromVarbind = useAddressFromVarbind;
    }

    public List<Snmpv3User> getSnmpv3User() { return snmpv3User; }

    public void setSnmpv3User(List<Snmpv3User> snmpv3Users) { this.snmpv3User = snmpv3Users; }

    public TrapdConfigDto toDto(final TrapdConfiguration config) {
        TrapdConfigDto dto = new TrapdConfigDto();
        dto.setSnmpTrapAddress(config.getSnmpTrapAddress());
        dto.setSnmpTrapPort(config.getSnmpTrapPort());
        dto.setNewSuspectOnTrap(config.getNewSuspectOnTrap());
        dto.setIncludeRawMessage(config.isIncludeRawMessage());
        dto.setThreads(config.getThreads());
        dto.setQueueSize(config.getQueueSize());
        dto.setBatchSize(config.getBatchSize());
        dto.setBatchInterval(config.getBatchInterval());
        dto.setUseAddressFromVarbind(config.shouldUseAddressFromVarbind());
        dto.setSnmpv3User(List.of(config.getSnmpv3User()));
        return dto;
    }
}
