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

package org.opennms.netmgt.telemetry.protocols.netflow.adapter.netflow5;

import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.getBool;
import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.getInt64;
import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.getString;

import java.util.Objects;
import java.util.Optional;

import org.bson.BsonDocument;
import org.opennms.netmgt.flows.api.Flow;

class Netflow5Flow implements Flow {
    private final BsonDocument document;

    public Netflow5Flow(final BsonDocument document) {
        this.document = Objects.requireNonNull(document);
    }

    @Override
    public long getTimestamp() {
        return getInt64(this.document, "@unixSecs").get() * 1000L
                + getInt64(this.document, "@unixNSecs").get() / 1000_000L;
    }

    @Override
    public Long getBytes() {
        return getInt64(this.document, "dOctets").get();
    }

    @Override
    public Direction getDirection() {
        return getBool(this.document, "egress").get()
                ? Direction.EGRESS
                : Direction.INGRESS;
    }

    @Override
    public String getDstAddr() {
        return getString(this.document, "dstAddr", "address")
                .get();
    }

    @Override
    public Optional<String> getDstAddrHostname() {
        return getString(this.document, "dstAddr", "hostname");
    }

    @Override
    public Long getDstAs() {
        return getInt64(this.document, "dstAs")
                .get();
    }

    @Override
    public Integer getDstMaskLen() {
        return getInt64(this.document, "dstMask")
                .map(Long::intValue)
                .get();
    }

    @Override
    public Integer getDstPort() {
        return getInt64(this.document, "dstPort")
                .map(Long::intValue)
                .get();
    }

    @Override
    public Integer getEngineId() {
        return getInt64(this.document, "@engineId")
                .map(Long::intValue)
                .get();
    }

    @Override
    public Integer getEngineType() {
        return getInt64(this.document, "@engineType")
                .map(Long::intValue)
                .get();
    }

    @Override
    public Long getFirstSwitched() {
        return getInt64(this.document, "first")
                .map(t -> this.getBootTime() + t)
                .get();
    }

    @Override
    public int getFlowRecords() {
        return getInt64(this.document, "@count")
                .map(Long::intValue)
                .get();
    }

    @Override
    public long getFlowSeqNum() {
        return getInt64(this.document, "@flowSequence").get();
    }

    @Override
    public Integer getInputSnmp() {
        return getInt64(this.document, "input")
                .map(Long::intValue)
                .get();
    }

    @Override
    public Integer getIpProtocolVersion() {
        return Flow.IPV4_PROTOCOL_VERSION;
    }

    @Override
    public Long getLastSwitched() {
        return getInt64(this.document, "last")
                .map(t -> this.getBootTime() + t)
                .get();
    }

    @Override
    public String getNextHop() {
        return getString(this.document, "nextHop", "address")
                .get();
    }

    @Override
    public Optional<String> getNextHopHostname() {
        return getString(this.document, "nextHop", "hostname");
    }

    @Override
    public Integer getOutputSnmp() {
        return getInt64(this.document, "output")
                .map(Long::intValue)
                .get();
    }

    @Override
    public Long getPackets() {
        return getInt64(this.document, "dPkts").get();
    }

    @Override
    public Integer getProtocol() {
        return getInt64(this.document, "proto")
                .map(Long::intValue)
                .get();
    }

    @Override
    public Flow.SamplingAlgorithm getSamplingAlgorithm() {
        switch (getInt64(this.document, "@samplingAlgorithm")
                .map(Long::intValue)
                .get()) {
            case 1:
                return Flow.SamplingAlgorithm.SystematicCountBasedSampling;

            case 2:
                return Flow.SamplingAlgorithm.RandomNoutOfNSampling;

            default:
                return Flow.SamplingAlgorithm.Unassigned;
        }
    }

    @Override
    public Double getSamplingInterval() {
        return getInt64(this.document, "@samplingInterval")
                .map(Long::doubleValue)
                .get();
    }

    @Override
    public String getSrcAddr() {
        return getString(this.document, "srcAddr", "address")
                .get();
    }

    @Override
    public Optional<String> getSrcAddrHostname() {
        return getString(this.document, "srcAddr", "hostname");
    }

    @Override
    public Long getSrcAs() {
        return getInt64(this.document, "srcAs")
                .get();
    }

    @Override
    public Integer getSrcMaskLen() {
        return getInt64(this.document, "srcMask")
                .map(Long::intValue)
                .get();
    }

    @Override
    public Integer getSrcPort() {
        return getInt64(this.document, "srcPort")
                .map(Long::intValue)
                .get();
    }

    @Override
    public Integer getTcpFlags() {
        return getInt64(this.document, "tcpFlags")
                .map(Long::intValue)
                .get();
    }

    @Override
    public Long getDeltaSwitched() {
        return this.getFirstSwitched();
    }

    @Override
    public Integer getTos() {
        return getInt64(this.document, "tos")
                .map(Long::intValue)
                .get();
    }

    @Override
    public NetflowVersion getNetflowVersion() {
        return NetflowVersion.V5;
    }

    @Override
    public Integer getVlan() {
        return null;
    }

    private long getSysUpTime() {
        return getInt64(this.document, "@sysUptime").get();
    }

    private long getBootTime() {
        return this.getTimestamp() - this.getSysUpTime();
    }

    @Override
    public String getNodeIdentifier() {
        return getInt64(this.document, "@engineId")
                .map(String::valueOf)
                .orElse(null);
    }
}
