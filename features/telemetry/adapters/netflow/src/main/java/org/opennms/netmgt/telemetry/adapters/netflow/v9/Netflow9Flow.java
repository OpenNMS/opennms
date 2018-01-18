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

package org.opennms.netmgt.telemetry.adapters.netflow.v9;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.opennms.netmgt.flows.api.Flow;

class Netflow9Flow implements Flow {
    private final BsonDocument document;

    public Netflow9Flow(final BsonDocument document) {
        this.document = Objects.requireNonNull(document);
    }

    @Override
    public Long getTimestamp() {
        return get(document, "exportTime")
                .map(v -> v.asTimestamp().getTime() * 1000L)
                .orElse(null);
    }

    @Override
    public Long getBytes() {
        return get(document, "elements", "IN_BYTES", "v")
                .map(v -> v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Direction getDirection() {
        return get(document, "elements", "DIRECTION", "v")
                .map(v -> v.asInt32().getValue() == 0 ? Direction.INGRESS
                        : v.asInt32().getValue() == 1 ? Direction.EGRESS
                        : null)
                .orElse(null);
    }

    @Override
    public String getDstAddr() {
        return first(get(document, "elements", "IPV6_DST_ADDR", "v"),
                     get(document, "elements", "IPV4_DST_ADDR", "v"))
                .map(v -> v.asString().getValue())
                .orElse(null);
    }

    @Override
    public Integer getDstAs() {
        return get(document, "elements", "DST_AS", "v")
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getDstMaskLen() {
        return first(get(document, "elements", "IPV6_DST_MASK", "v"),
                     get(document, "elements", "DST_MASK", "v"))
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getDstPort() {
        return get(document, "elements", "L4_DST_PORT", "v")
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getEngineId() {
        return get(document, "elements", "ENGINE_ID", "v")
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getEngineType() {
        return get(document, "elements", "ENGINE_TYPE", "v")
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Long getFirstSwitched() {
        return get(document, "elements", "FIRST_SWITCHED", "v")
                .map(v -> v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public int getFlowRecords() {
        return get(document, "recordCount")
                .map(v -> v.asInt32().getValue())
                .orElse(0);
    }

    @Override
    public long getFlowSeqNum() {
        return get(document, "sequenceNumber")
                .map(v -> v.asInt64().getValue())
                .orElse(0L);
    }

    @Override
    public Integer getInputSnmp() {
        return get(document, "elements", "INPUT_SNMP", "v")
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getIpProtocolVersion() {
        return get(document, "elements", "IP_PROTOCOL_VERSION", "v")
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Long getLastSwitched() {
        return get(document, "elements", "LAST_SWITCHED", "v")
                .map(v -> v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public String getNextHop() {
        return first(get(document, "elements", "IPV6_NEXT_HOP", "v"),
                     get(document, "elements", "IPV4_NEXT_HOP", "v"),
                     get(document, "elements", "BPG_IPV6_NEXT_HOP", "v"),
                     get(document, "elements", "BPG_IPV4_NEXT_HOP", "v"))
                .map(v -> v.asString().getValue())
                .orElse(null);
    }

    @Override
    public Integer getOutputSnmp() {
        return get(document, "elements", "OUTPUT_SNMP", "v")
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Long getPackets() {
        return get(document, "elements", "IN_PKTS", "v")
                .map(v -> v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getProtocol() {
        return get(document, "elements", "PROTOCOL", "v")
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getSamplingAlgorithm() {
        return get(document, "elements", "SAMPLING_ALGORITHM", "v")
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getSamplingInterval() {
        return get(document, "elements", "SAMPLING_INTERVAL", "v")
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public String getSrcAddr() {
        return first(get(document, "elements", "IPV6_SRC_ADDR", "v"),
                     get(document, "elements", "IPV4_SRC_ADDR", "v"))
                .map(v -> v.asString().getValue())
                .orElse(null);
    }

    @Override
    public Integer getSrcAs() {
        return get(document, "elements", "SRC_AS", "v")
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getSrcMaskLen() {
        return first(get(document, "elements", "IPV6_SRC_MASK", "v"),
                     get(document, "elements", "SRC_MASK", "v"))
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getSrcPort() {
        return get(document, "elements", "L4_SRC_PORT", "v")
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getTcpFlags() {
        return get(document, "elements", "TCP_FLAGS", "v")
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getTos() {
        return get(document, "elements", "TOS", "v")
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public NetflowVersion getNetflowVersion() {
        return NetflowVersion.V9;
    }

    @Override
    public Integer getVlan() {
        return first(get(document, "elements", "SRC_VLAN", "v"),
                     get(document, "elements", "DST_VLAN", "v"))
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    private static Optional<BsonValue> get(final BsonDocument doc, final String... path) {
        BsonValue value = doc;
        for (final String p : path) {
            value = value.asDocument().get(p);
            if (value == null) {
                return Optional.empty();
            }
        }

        return Optional.of(value);
    }

    private static <V> Optional<V> first(final Optional<V>... values) {
        return Stream.of(values)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .findFirst();
    }
}
