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
package org.opennms.netmgt.telemetry.protocols.sflow.adapter;

import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.first;
import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.get;
import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.getString;

import static org.opennms.integration.api.v1.flows.Flow.Direction;
import static org.opennms.integration.api.v1.flows.Flow.NetflowVersion;
import static org.opennms.integration.api.v1.flows.Flow.SamplingAlgorithm;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils;

public class SFlow implements Flow {

    public static class Header {
        private final BsonDocument document;

        public Header(final BsonDocument document) {
            this.document = document;
        }

        public Long getTimestamp() {
            return BsonUtils.get(document, "time")
                    .map(v -> v.asInt64().getValue())
                    .orElse(null);
        }

        public Integer getSubAgentId() {
            return get(document, "sub_agent_id")
                    .map(v -> (int) v.asInt64().getValue())
                    .orElse(null);
        }

        public Long getSequenceNumber() {
            return first(get(document, "sequence_number", "source_id_index"),
                         get(document, "sequence_number"))
                    .map(v -> v.asInt64().getValue())
                    .orElse(0L);
        }
    }

    private final Header header;
    private final BsonDocument document;
    private final Instant receivedAt;

    public SFlow(final Header header, final BsonDocument document, final Instant receivedAt) {
        this.header = header;
        this.document = Objects.requireNonNull(document);
        this.receivedAt = Objects.requireNonNull(receivedAt);
    }

    @Override
    public Instant getReceivedAt() {
        return this.receivedAt;
    }

    @Override
    public Instant getTimestamp() {
        return Instant.ofEpochMilli(this.header.getTimestamp());
    }

    @Override
    public Long getBytes() {
        return first(get(document, "flows", "0:3", "length"),
                get(document, "flows", "0:4", "length"),
                get(document, "flows", "0:1", "ipv4", "length"),
                get(document, "flows", "0:1", "ipv6", "length"))
                .map(v -> (long) v.asInt32().getValue())
                .orElse(null);
    }

    @Override
    public Direction getDirection() {
        final Optional<BsonValue> source = first(get(document, "source_id", "source_id_index"),
                get(document, "source_id"));

        final Optional<BsonValue> input = first(get(document, "input", "value"),
                get(document, "input"));

        if (source.isPresent() && input.isPresent() && !Objects.equals(source, input)) {
            return Direction.EGRESS;
        } else {
            return Direction.INGRESS;
        }
    }

    @Override
    public String getDstAddr() {
        return first(get(document, "flows", "0:3", "dst_ip", "address"),
                get(document, "flows", "0:4", "dst_ip", "address"),
                get(document, "flows", "0:1", "ipv4", "dst_ip", "address"),
                get(document, "flows", "0:1", "ipv6", "dst_ip", "address"))
                .map(v -> v.asString().getValue())
                .orElse(null);
    }

    @Override
    public Optional<String> getDstAddrHostname() {
        return first(get(document, "flows", "0:3", "dst_ip", "hostname"),
                get(document, "flows", "0:4", "dst_ip", "hostname"),
                get(document, "flows", "0:1", "ipv4", "dst_ip", "hostname"),
                get(document, "flows", "0:1", "ipv6", "dst_ip", "hostname"))
                .map(v -> v.asString().getValue());
    }

    @Override
    public Long getDstAs() {
        // TODO: Multi-path, any-cast, etc...
        return null;
    }

    @Override
    public Integer getDstMaskLen() {
        return get(document, "flows", "0:1002", "dst_mask_len")
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getDstPort() {
        return first(get(document, "flows", "0:3", "dst_port"),
                get(document, "flows", "0:4", "dst_port"),
                get(document, "flows", "0:1", "ipv4", "dst_port"),
                get(document, "flows", "0:1", "ipv6", "dst_port"))
                .map(v -> v.asInt32().getValue())
                .orElse(null);
    }

    @Override
    public Integer getEngineId() {
        return this.header.getSubAgentId();
    }

    @Override
    public Integer getEngineType() {
        return null;
    }

    @Override
    public Instant getFirstSwitched() {
        // As this flow represents a single packet, there is no "duration" of the flow
        return Instant.ofEpochMilli(this.header.getTimestamp());
    }

    @Override
    public Instant getLastSwitched() {
        return Instant.ofEpochMilli(this.header.getTimestamp());
    }

    @Override
    public int getFlowRecords() {
        return get(document, "flows")
                .map(v -> v.asDocument().size())
                .orElse(0);
    }

    @Override
    public long getFlowSeqNum() {
        return this.header.getSequenceNumber();
    }

    @Override
    public Integer getInputSnmp() {
        return first(get(document, "input", "value"),
                     get(document, "input"))
                .map(v -> v.asInt64().getValue() == 0x3FFFFFFFL ? null : (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getOutputSnmp() {
        return first(get(document, "output", "value"),
                     get(document, "output"))
                .map(v -> v.asInt64().getValue() == 0x3FFFFFFFL ? null : (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getIpProtocolVersion() {
        return first(get(document, "flows", "0:1", "protocol")
                        .flatMap(v -> {
                            switch (v.asInt32().getValue()) {
                                case 11:
                                    return Optional.of(4);
                                case 12:
                                    return Optional.of(6);
                                default:
                                    return Optional.empty();
                            }
                        }),
                get(document, "flows", "0:3").map(v -> 4),
                get(document, "flows", "0:4").map(v -> 6))
                .orElse(null);
    }

    @Override
    public String getNextHop() {
        return first(get(document, "flows", "0:1002", "nexthop", "ipv6", "address"),
                get(document, "flows", "0:1002", "nexthop", "ipv4", "address"))
                .map(v -> v.asString().getValue())
                .orElse(null);
    }

    @Override
    public Optional<String> getNextHopHostname() {
        return first(get(document, "flows", "0:1002", "nexthop", "ipv6", "hostname"),
                get(document, "flows", "0:1002", "nexthop", "ipv4", "hostname"))
                .map(v -> v.asString().getValue());
    }

    @Override
    public Long getPackets() {
        return 1L;
    }

    @Override
    public Integer getProtocol() {
        return first(get(document, "flows", "0:3", "protocol"),
                get(document, "flows", "0:4", "protocol"),
                get(document, "flows", "0:1", "ipv4", "protocol"),
                get(document, "flows", "0:1", "ipv6", "protocol"))
                .map(v -> v.asInt32().getValue())
                .orElse(null);
    }

    @Override
    public SamplingAlgorithm getSamplingAlgorithm() {
        return SamplingAlgorithm.Unassigned;
    }

    @Override
    public Double getSamplingInterval() {
        return get(document, "sampling_rate")
                .map(v -> (double) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public String getSrcAddr() {
        return first(get(document, "flows", "0:3", "src_ip", "address"),
                get(document, "flows", "0:4", "src_ip", "address"),
                get(document, "flows", "0:1", "ipv4", "src_ip", "address"),
                get(document, "flows", "0:1", "ipv6", "src_ip", "address"))
                .map(v -> v.asString().getValue())
                .orElse(null);
    }

    @Override
    public Optional<String> getSrcAddrHostname() {
        return first(get(document, "flows", "0:3", "src_ip", "hostname"),
                get(document, "flows", "0:4", "src_ip", "hostname"),
                get(document, "flows", "0:1", "ipv4", "src_ip", "hostname"),
                get(document, "flows", "0:1", "ipv6", "src_ip", "hostname"))
                .map(v -> v.asString().getValue());
    }

    @Override
    public Long getSrcAs() {
        return get(document, "flows", "0:1003", "src_as")
                .map(v -> v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getSrcMaskLen() {
        return get(document, "flows", "0:1002", "src_mask_len")
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getSrcPort() {
        return first(get(document, "flows", "0:3", "src_port"),
                get(document, "flows", "0:4", "src_port"),
                get(document, "flows", "0:1", "ipv4", "src_port"),
                get(document, "flows", "0:1", "ipv6", "src_port"))
                .map(v -> v.asInt32().getValue())
                .orElse(null);
    }

    @Override
    public Integer getTcpFlags() {
        return first(get(document, "flows", "0:3", "tcp_flags"),
                get(document, "flows", "0:4", "tcp_flags"),
                get(document, "flows", "0:1", "ipv4", "tcp_flags"),
                get(document, "flows", "0:1", "ipv6", "tcp_flags"))
                .map(v -> v.asInt32().getValue())
                .orElse(null);
    }

    @Override
    public Instant getDeltaSwitched() {
        return this.getFirstSwitched();
    }

    @Override
    public Integer getTos() {
        return first(get(document, "flows", "0:3", "tos"),
                get(document, "flows", "0:4", "tos"),
                get(document, "flows", "0:1", "ipv4", "tos"),
                get(document, "flows", "0:1", "ipv6", "tos"))
                .map(v -> v.asInt32().getValue())
                .orElse(null);
    }

    @Override
    public NetflowVersion getNetflowVersion() {
        return NetflowVersion.SFLOW;
    }

    @Override
    public Integer getVlan() {
        return first(get(document, "flows", "0:1001", "src_vlan"),
                get(document, "flows", "0:1", "ethernet", "vlan"))
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public String getNodeIdentifier() {
        final String address = first(
                getString(document, "agent_address", "ipv6", "address"),
                getString(document, "agent_address", "ipv4", "address"))
                .orElse("unknown");

        final String subAgentId = header.getSubAgentId() == null ? "unknown" : String.valueOf(header.getSubAgentId());

        return address + ":" + subAgentId;
    }
}
