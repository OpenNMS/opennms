/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.adapters.netflow.sflow;

import static org.opennms.netmgt.telemetry.adapters.netflow.BsonUtils.first;
import static org.opennms.netmgt.telemetry.adapters.netflow.BsonUtils.get;

import java.util.Objects;
import java.util.Optional;

import org.bson.BsonDocument;
import org.opennms.netmgt.flows.api.Flow;

class SFlow implements Flow {

    static class Header {
        private final BsonDocument document;

        public Header(final BsonDocument document) {
            this.document = document;
        }

        public Long getTimestamp() {
            return get(document, "time")
                    .map(v -> v.asInt64().getValue())
                    .orElse(null);
        }

        public Integer getSubAgentId() {
            return get(document, "sub_agent_id")
                    .map(v -> (int) v.asInt64().getValue())
                    .orElse(null);
        }

        public Long getSequenceNumber() {
            return get(document, "sequence_number")
                    .map(v -> v.asInt64().getValue())
                    .orElse(0L);
        }
    }

    private final Header header;
    private final BsonDocument document;

    public SFlow(final Header header, final BsonDocument document) {
        this.header = header;
        this.document = Objects.requireNonNull(document);
    }

    @Override
    public long getTimestamp() {
        return this.header.getTimestamp();
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
        return Direction.INGRESS;
    }

    @Override
    public String getDstAddr() {
        return first(get(document, "flows", "0:3", "dst_ip"),
                get(document, "flows", "0:4", "dst_ip"),
                get(document, "flows", "0:1", "ipv4", "dst_ip"),
                get(document, "flows", "0:1", "ipv6", "dst_ip"))
                .map(v -> v.asString().getValue())
                .orElse(null);
    }

    @Override
    public Integer getDstAs() {
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
    public Long getFirstSwitched() {
        // As this flow represents a single packet, there is no "duration" of the flow
        return this.header.getTimestamp();
    }

    @Override
    public Long getLastSwitched() {
        return this.header.getTimestamp();
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
        return get(document, "input")
                .map(v -> v.asInt64().getValue() == 0x3FFFFFFFL ? null : (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getOutputSnmp() {
        return get(document, "output")
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
        return first(get(document, "flows", "0:1002", "netxhop", "ipv6"),
                get(document, "flows", "0:1002", "netxhop", "ipv4"))
                .map(v -> v.asString().getValue())
                .orElse(null);
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
    public Flow.SamplingAlgorithm getSamplingAlgorithm() {
        return Flow.SamplingAlgorithm.Unassigned;
    }

    @Override
    public Double getSamplingInterval() {
        return get(document, "sampling_rate")
                .map(v -> (double) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public String getSrcAddr() {
        return first(get(document, "flows", "0:3", "src_ip"),
                get(document, "flows", "0:4", "src_ip"),
                get(document, "flows", "0:1", "ipv4", "src_ip"),
                get(document, "flows", "0:1", "ipv6", "src_ip"))
                .map(v -> v.asString().getValue())
                .orElse(null);
    }

    @Override
    public Integer getSrcAs() {
        return get(document, "flows", "0:1003", "src_as")
                .map(v -> (int) v.asInt64().getValue())
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
}
