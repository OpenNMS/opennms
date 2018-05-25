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
        return getInt64(this.document, "IN_BYTES")
                .orElse(null);
    }

    @Override
    public Direction getDirection() {
        return getInt64(this.document, "DIRECTION")
                .map(v -> v == 0 ? Direction.INGRESS
                        : v == 1 ? Direction.EGRESS
                        : null)
                .orElse(null);
    }

    @Override
    public String getDstAddr() {
        return first(getString(this.document, "IPV6_DST_ADDR"),
                getString(this.document, "IPV4_DST_ADDR"))
                .orElse(null);
    }

    @Override
    public Integer getDstAs() {
        return getInt64(this.document, "DST_AS")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getDstMaskLen() {
        return first(getInt64(this.document, "IPV6_DST_MASK"),
                getInt64(this.document, "DST_MASK"))
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getDstPort() {
        return getInt64(this.document, "L4_DST_PORT")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getEngineId() {
        return getInt64(this.document, "ENGINE_ID")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getEngineType() {
        return getInt64(this.document, "ENGINE_TYPE")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Long getFirstSwitched() {
        return getInt64(this.document, "FIRST_SWITCHED")
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
        return getInt64(this.document, "INPUT_SNMP")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getIpProtocolVersion() {
        return getInt64(this.document, "IP_PROTOCOL_VERSION")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Long getLastSwitched() {
        return getInt64(this.document, "LAST_SWITCHED")
                .map(t -> this.getBootTime() + t)
                .orElse(null);
    }

    @Override
    public String getNextHop() {
        return first(getString(this.document, "IPV6_NEXT_HOP"),
                getString(this.document, "IPV4_NEXT_HOP"),
                getString(this.document, "BPG_IPV6_NEXT_HOP"),
                getString(this.document, "BPG_IPV4_NEXT_HOP"))
                .orElse(null);
    }

    @Override
    public Integer getOutputSnmp() {
        return getInt64(this.document, "OUTPUT_SNMP")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Long getPackets() {
        return getInt64(this.document, "IN_PKTS")
                .orElse(null);
    }

    @Override
    public Integer getProtocol() {
        return getInt64(this.document, "PROTOCOL")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Flow.SamplingAlgorithm getSamplingAlgorithm() {
        final int samplingAlgorithm = getInt64(this.document, "SAMPLING_ALGORITHM")
                .map(Long::intValue)
                .orElse(0);

        if (samplingAlgorithm == 1) {
            return Flow.SamplingAlgorithm.SystematicCountBasedSampling;
        }
        if (samplingAlgorithm == 2) {
            return Flow.SamplingAlgorithm.RandomNoutOfNSampling;
        }

        return Flow.SamplingAlgorithm.Unassigned;
    }

    @Override
    public Double getSamplingInterval() {
        return getInt64(this.document, "SAMPLING_INTERVAL")
                .map(Long::doubleValue)
                .orElse(null);
    }

    @Override
    public String getSrcAddr() {
        return first(getString(this.document, "IPV6_SRC_ADDR"),
                getString(this.document, "IPV4_SRC_ADDR"))
                .orElse(null);
    }

    @Override
    public Integer getSrcAs() {
        return getInt64(this.document, "SRC_AS")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getSrcMaskLen() {
        return first(getInt64(this.document, "IPV6_SRC_MASK"),
                getInt64(this.document, "SRC_MASK"))
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getSrcPort() {
        return getInt64(this.document, "L4_SRC_PORT")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getTcpFlags() {
        return getInt64(this.document, "TCP_FLAGS")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getTos() {
        return getInt64(this.document, "TOS")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public NetflowVersion getNetflowVersion() {
        return NetflowVersion.V9;
    }

    @Override
    public Integer getVlan() {
        return first(getInt64(this.document, "SRC_VLAN"),
                getInt64(this.document, "DST_VLAN"))
                .map(Long::intValue)
                .orElse(null);
    }

    private long getSysUpTime() {
        return getInt64(this.document, "@sysUpTime").get();
    }

    private long getBootTime() {
        return this.getTimestamp() - getSysUpTime();
    }
}
