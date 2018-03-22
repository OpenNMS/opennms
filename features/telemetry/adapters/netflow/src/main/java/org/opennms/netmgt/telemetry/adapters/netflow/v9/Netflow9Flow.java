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

import static org.opennms.netmgt.telemetry.adapters.netflow.BsonUtils.first;
import static org.opennms.netmgt.telemetry.adapters.netflow.BsonUtils.getInt64;
import static org.opennms.netmgt.telemetry.adapters.netflow.BsonUtils.getString;

import java.util.Objects;

import org.bson.BsonDocument;
import org.opennms.netmgt.flows.api.Flow;

class Netflow9Flow implements Flow {
    private final BsonDocument document;

    public Netflow9Flow(final BsonDocument document) {
        this.document = Objects.requireNonNull(document);
    }

    @Override
    public long getTimestamp() {
        return getInt64(this.document, "@unixSecs").get() * 1000;
    }

    @Override
    public Long getBytes() {
        return getInt64(this.document, "octetDeltaCount")
                .orElse(null);
    }

    @Override
    public Direction getDirection() {
        return getInt64(this.document, "flowDirection")
                .map(v -> v == 0 ? Direction.INGRESS
                        : v == 1 ? Direction.EGRESS
                        : null)
                .orElse(null);
    }

    @Override
    public String getDstAddr() {
        return first(getString(this.document, "destinationIPv6Address"),
                     getString(this.document, "destinationIPv4Address"))
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
        return getInt64(this.document,  "flowStartSysUpTime")
                .map(t -> this.getBootTime() + t)
                .orElse(null);
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
        return getInt64(this.document,  "flowEndSysUpTime")
                .map(t -> this.getBootTime() + t)
                .orElse(null);
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
        return getInt64(this.document,  "packetDeltaCount")
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
        return getInt64(this.document,  "samplingAlgorithm")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getSamplingInterval() {
        return getInt64(this.document,  "samplingInterval")
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
        return NetflowVersion.V9;
    }

    @Override
    public Integer getVlan() {
        return first(getInt64(this.document,  "vlanId"),
                     getInt64(this.document,  "postVlanId"))
                .map(Long::intValue)
                .orElse(null);
    }

    private long getSysUpTime() {
        return getInt64(this.document, "@sysUpTime").get() * 1000;
    }

    private long getBootTime() {
        return this.getTimestamp() - getSysUpTime();
    }
}
