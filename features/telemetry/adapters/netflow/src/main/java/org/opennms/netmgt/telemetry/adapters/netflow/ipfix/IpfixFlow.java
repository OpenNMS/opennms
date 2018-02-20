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

import static org.opennms.netmgt.telemetry.adapters.netflow.BsonUtils.first;
import static org.opennms.netmgt.telemetry.adapters.netflow.BsonUtils.getInt64;
import static org.opennms.netmgt.telemetry.adapters.netflow.BsonUtils.getString;
import static org.opennms.netmgt.telemetry.adapters.netflow.BsonUtils.getTime;

import java.time.Instant;
import java.util.Objects;

import org.bson.BsonDocument;
import org.opennms.netmgt.flows.api.Flow;

class IpfixFlow implements Flow {
    private final BsonDocument document;

    public IpfixFlow(final BsonDocument document) {
        this.document = Objects.requireNonNull(document);
    }

    @Override
    public long getTimestamp() {
        return getInt64(this.document, "@exportTime").get() * 1000;
    }

    @Override
    public Long getBytes() {
        // TODO: What about the totals?
        return first(getInt64(this.document,  "octetDeltaCount"),
                     getInt64(this.document,  "postOctetDeltaCount"),
                     getInt64(this.document,  "layer2OctetDeltaCount"),
                     getInt64(this.document,  "postLayer2OctetDeltaCount"),
                     getInt64(this.document,  "transportOctetDeltaCount"))
                .orElse(null);
    }

    @Override
    public Direction getDirection() {
        return getInt64(this.document,  "flowDirection")
                .map(v -> v == 0x00 ? Direction.INGRESS
                        : v == 0x01 ? Direction.EGRESS
                        : null)
                .orElse(null);
    }

    @Override
    public String getDstAddr() {
        return first(getString(this.document,  "destinationIPv6Address"),
                     getString(this.document,  "destinationIPv4Address"))
                .orElse(null);
    }

    @Override
    public Integer getDstAs() {
        return getInt64(this.document,  "bgpDestinationAsNumber")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getDstMaskLen() {
        return first(getInt64(this.document,  "destinationIPv6PrefixLength"),
                     getInt64(this.document,  "destinationIPv4PrefixLength"))
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getDstPort() {
        return getInt64(this.document,  "destinationTransportPort")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getEngineId() {
        return getInt64(this.document,  "engineId")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getEngineType() {
        return getInt64(this.document,  "engineType")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Long getFirstSwitched() {
        // TODO: What about flowDuration* ?
        return first(
                first(getTime(this.document, "flowStartSeconds"),
                      getTime(this.document, "flowStartMilliseconds"),
                      getTime(this.document, "flowStartMicroseconds"),
                      getTime(this.document, "flowStartNanoseconds")
                ).map(Instant::toEpochMilli),
                getInt64(this.document, "flowStartDeltaMicroseconds").map(t -> this.getTimestamp() + t),
                getInt64(this.document, "flowStartSysUpTime").flatMap(t ->
                    getTime(this.document, "systemInitTimeMilliseconds").map(ts -> ts.toEpochMilli() + t)
                )
        ).orElse(null);
    }

    @Override
    public int getFlowRecords() {
        return getInt64(this.document, "@recordCount")
                .map(Long::intValue)
                .orElse(0);
    }

    @Override
    public long getFlowSeqNum() {
        return getInt64(this.document, "@sequenceNumber")
                .orElse(0L);
    }

    @Override
    public Integer getInputSnmp() {
        return getInt64(this.document,  "ingressInterface")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getIpProtocolVersion() {
        return getInt64(this.document,  "ipVersion")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Long getLastSwitched() {
        // TODO: What about flowDuration* ?
        return first(
                first(getTime(this.document, "flowEndSeconds"),
                      getTime(this.document, "flowEndMilliseconds"),
                      getTime(this.document, "flowEndMicroseconds"),
                      getTime(this.document, "flowEndNanoseconds")
                ).map(Instant::toEpochMilli),
                getInt64(this.document, "flowEndDeltaMicroseconds").map(t -> this.getTimestamp() + t),
                getInt64(this.document, "flowEndSysUpTime").flatMap(t ->
                        getTime(this.document, "systemInitTimeMilliseconds").map(ts -> ts.toEpochMilli() + t)
                )
        ).orElse(null);
    }

    @Override
    public String getNextHop() {
        return first(getString(this.document,  "ipNextHopIPv6Address"),
                     getString(this.document,  "ipNextHopIPv4Address"),
                     getString(this.document,  "bgpNextHopIPv6Address"),
                     getString(this.document,  "bgpNextHopIPv4Address"))
                .orElse(null);
    }

    @Override
    public Integer getOutputSnmp() {
        return getInt64(this.document,  "egressInterface")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Long getPackets() {
        // TODO: What about the totals?
        return first(getInt64(this.document,  "packetDeltaCount"),
                     getInt64(this.document,  "postPacketDeltaCount"),
                     getInt64(this.document,  "transportPacketDeltaCount"))
                .orElse(null);
    }

    @Override
    public Integer getProtocol() {
        return getInt64(this.document,  "protocolIdentifier")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getSamplingAlgorithm() {
        return first(getInt64(this.document,  "selectorAlgorithm"),
                     getInt64(this.document,  "samplingAlgorithm"))
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getSamplingInterval() {
        return first(getInt64(this.document,  "samplingPacketInterval"),
                     getInt64(this.document,  "samplingInterval"))
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public String getSrcAddr() {
        return first(getString(this.document,  "sourceIPv6Address"),
                     getString(this.document,  "sourceIPv4Address"))
                .orElse(null);
    }

    @Override
    public Integer getSrcAs() {
        return getInt64(this.document,  "bgpSourceAsNumber")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getSrcMaskLen() {
        return first(getInt64(this.document,  "sourceIPv6PrefixLength"),
                     getInt64(this.document,  "sourceIPv4PrefixLength"))
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getSrcPort() {
        return getInt64(this.document,  "sourceTransportPort")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getTcpFlags() {
        return getInt64(this.document,  "tcpControlBits")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getTos() {
        return getInt64(this.document,  "ipClassOfService")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public NetflowVersion getNetflowVersion() {
        return NetflowVersion.IPFIX;
    }

    @Override
    public Integer getVlan() {
        return first(getInt64(this.document,  "vlanId"),
                     getInt64(this.document,  "postVlanId"),
                     getInt64(this.document,  "dot1qVlanId"),
                     getInt64(this.document,  "dot1qCustomerVlanId"),
                     getInt64(this.document,  "postDot1qVlanId"),
                     getInt64(this.document,  "postDot1qCustomerVlanId"))
                .map(Long::intValue)
                .orElse(null);
    }
}
