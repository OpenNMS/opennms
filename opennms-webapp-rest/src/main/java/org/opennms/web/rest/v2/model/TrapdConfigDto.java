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

import io.swagger.v3.oas.annotations.media.Schema;
import org.opennms.netmgt.config.trapd.TrapdConfiguration;

import java.util.Arrays;
import java.util.List;

@Schema(description = """
        Complete trapd configuration. Writes replace the stored document rather than patching it, so a
        field left out falls back to its XSD default instead of keeping its current value.""")
public class TrapdConfigDto {
    @Schema(description = "Address trapd binds its listener to. `*` binds every interface.",
            example = "*")
    private String snmpTrapAddress;

    @Schema(description = "UDP port trapd listens on. Required, 1 to 65535.",
            example = "10162", required = true, minimum = "1", maximum = "65535")
    private Integer snmpTrapPort;

    @Schema(description = "Whether a trap from an unknown address raises a newSuspect event. Required.",
            example = "false", required = true)
    private Boolean newSuspectOnTrap;

    @Schema(description = "Whether the raw trap PDU is attached to the generated event.",
            example = "false")
    private Boolean includeRawMessage;

    @Schema(description = "Worker threads for trap processing. 0 sizes the pool from the available "
            + "processors. Must be non-negative.",
            example = "0", minimum = "0")
    private Integer threads;

    @Schema(description = "Depth of the queue between the listener and the workers. Must be greater "
            + "than 0.",
            example = "10000", minimum = "1")
    private Integer queueSize;

    @Schema(description = "Maximum traps sent onward in one batch. Must be greater than 0.",
            example = "1000", minimum = "1")
    private Integer batchSize;

    @Schema(description = "Milliseconds to wait before flushing a partial batch. Must be non-negative.",
            example = "500", minimum = "0")
    private Integer batchInterval;

    @Schema(description = "Whether the agent address is taken from the trap's snmpTrapAddress varbind "
            + "rather than the packet source.",
            example = "false")
    private Boolean useAddressFromVarbind;

    @Schema(description = "SNMPv3 users trapd accepts traps from. Each `id` must be unique.")
    private List<Snmpv3UserDto> snmpv3User;

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

    public List<Snmpv3UserDto> getSnmpv3User() {
        return snmpv3User;
    }

    public void setSnmpv3User(List<Snmpv3UserDto> snmpv3Users) {
        this.snmpv3User = snmpv3Users;
    }

    public static TrapdConfigDto toDto(final TrapdConfiguration config) {
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
        dto.setSnmpv3User(Arrays.stream(config.getSnmpv3User()).map(Snmpv3UserDto::toDto).toList());
        return dto;
    }

    public TrapdConfiguration toEntity() {
        TrapdConfiguration config = new TrapdConfiguration();
        if (snmpTrapAddress != null) config.setSnmpTrapAddress(snmpTrapAddress);
        if (snmpTrapPort != null) config.setSnmpTrapPort(snmpTrapPort);
        if (newSuspectOnTrap != null) config.setNewSuspectOnTrap(newSuspectOnTrap);
        if (includeRawMessage != null) config.setIncludeRawMessage(includeRawMessage);
        if (threads != null) config.setThreads(threads);
        if (queueSize != null) config.setQueueSize(queueSize);
        if (batchSize != null) config.setBatchSize(batchSize);
        if (batchInterval != null) config.setBatchInterval(batchInterval);
        if (useAddressFromVarbind != null) config.setUseAddressFromVarbind(useAddressFromVarbind);
        if (snmpv3User != null) {
            config.setSnmpv3User(snmpv3User.stream().map(Snmpv3UserDto::toEntity).toArray(size -> new org.opennms.netmgt.config.trapd.Snmpv3User[size]));
        }
        return config;
    }
}
