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

package org.opennms.netmgt.telemetry.adapters.netflow.ipfix;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.opennms.netmgt.flows.api.Flow;

class IpfixFlow implements Flow {
    private final BsonDocument document;

    public IpfixFlow(final BsonDocument document) {
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
        // TODO: What about the totals?
        return first(get(document, "elements", "octetDeltaCount", "v"),
                     get(document, "elements", "postOctetDeltaCount", "v"),
                     get(document, "elements", "postMCastOctetDeltaCount", "v"),
                     get(document, "elements", "initiatorOctets", "v"),
                     get(document, "elements", "responderOctets", "v"),
                     get(document, "elements", "layer2OctetDeltaCount", "v"),
                     get(document, "elements", "postLayer2OctetDeltaCount", "v"),
                     get(document, "elements", "postMCastLayer2OctetDeltaCount", "v"),
                     get(document, "elements", "flowSelectedOctetDeltaCount", "v"),
                     get(document, "elements", "transportOctetDeltaCount", "v"))
                .map(v -> v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Direction getDirection() {
        return get(document, "elements", "flowDirection", "v")
                .map(v -> v.asInt32().getValue() == 0x00 ? Direction.INGRESS
                        : v.asInt32().getValue() == 0x01 ? Direction.EGRESS
                        : null)
                .orElse(null);
    }

    @Override
    public String getDstAddr() {
        return first(get(document, "elements", "destinationIPv6Address", "v"),
                     get(document, "elements", "destinationIPv4Address", "v"))
                .map(v -> v.asString().getValue())
                .orElse(null);
    }

    @Override
    public Integer getDstAs() {
        return get(document, "elements", "bgpDestinationAsNumber", "v")
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getDstMaskLen() {
        return first(get(document, "elements", "destinationIPv6PrefixLength", "v"),
                     get(document, "elements", "destinationIPv4PrefixLength", "v"))
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getDstPort() {
        return get(document, "elements", "destinationTransportPort", "v")
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getEngineId() {
        return get(document, "elements", "engineId", "v")
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getEngineType() {
        return get(document, "elements", "engineType", "v")
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Long getFirstSwitched() {
        // TODO: What about flowStart*, flowStartDelta*, flowDuration*, minFlowStart* ?
        return get(document, "elements", "flowStartSysUpTime", "v")
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
        return get(document, "elements", "ingressInterface", "v")
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
        // TODO: What about flowEnd*, flowEndDelta*, flowDuration*, maxFlowEnd* ?
        return get(document, "elements", "flowEndSysUpTime", "v")
                .map(v -> v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public String getNextHop() {
        return first(get(document, "elements", "ipNextHopIPv6Address", "v"),
                     get(document, "elements", "ipNextHopIPv4Address", "v"),
                     get(document, "elements", "bgpNextHopIPv6Address", "v"),
                     get(document, "elements", "bgpNextHopIPv4Address", "v"))
                .map(v -> v.asString().getValue())
                .orElse(null);
    }

    @Override
    public Integer getOutputSnmp() {
        return get(document, "elements", "egressInterface", "v")
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Long getPackets() {
        // TODO: What about the totals?
        return first(get(document, "elements", "packetDeltaCount", "v"),
                     get(document, "elements", "postPacketDeltaCount", "v"),
                     get(document, "elements", "postMCastPacketDeltaCount", "v"),
                     get(document, "elements", "initiatorPackets", "v"),
                     get(document, "elements", "responderPackets", "v"),
                     get(document, "elements", "flowSelectedPacketDeltaCount", "v"),
                     get(document, "elements", "transportPacketDeltaCount", "v"))
                .map(v -> v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getProtocol() {
        return get(document, "elements", "protocolIdentifier", "v")
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getSamplingAlgorithm() {
        return first(get(document, "elements", "selectorAlgorithm", "v"),
                     get(document, "elements", "samplingAlgorithm", "v"))
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getSamplingInterval() {
        return first(get(document, "elements", "samplingPacketInterval", "v"),
                     get(document, "elements", "samplingInterval", "v"))
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public String getSrcAddr() {
        return first(get(document, "elements", "sourceIPv6Address", "v"),
                     get(document, "elements", "sourceIPv4Address", "v"))
                .map(v -> v.asString().getValue())
                .orElse(null);
    }

    @Override
    public Integer getSrcAs() {
        return get(document, "elements", "bgpSourceAsNumber", "v")
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getSrcMaskLen() {
        return first(get(document, "elements", "sourceIPv6PrefixLength", "v"),
                     get(document, "elements", "sourceIPv4PrefixLength", "v"))
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getSrcPort() {
        return get(document, "elements", "sourceTransportPort", "v")
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getTcpFlags() {
        return get(document, "elements", "tcpControlBits", "v")
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public Integer getTos() {
        return get(document, "elements", "ipClassOfService", "v")
                .map(v -> (int) v.asInt64().getValue())
                .orElse(null);
    }

    @Override
    public NetflowVersion getNetflowVersion() {
        return NetflowVersion.V9;
    }

    @Override
    public Integer getVlan() {
        return first(get(document, "elements", "vlanId", "v"),
                     get(document, "elements", "postVlanId", "v"),
                     get(document, "elements", "dot1qVlanId", "v"),
                     get(document, "elements", "dot1qCustomerVlanId", "v"),
                     get(document, "elements", "postDot1qVlanId", "v"),
                     get(document, "elements", "postDot1qCustomerVlanId", "v"))
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
