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

package org.opennms.netmgt.telemetry.adapters.netflow.v5;

import java.util.Objects;

import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.telemetry.adapters.netflow.v5.proto.NetflowPacket;
import org.opennms.netmgt.telemetry.adapters.netflow.v5.proto.NetflowRecord;

class Netflow5Flow implements Flow {
    private final NetflowPacket packet;
    private final NetflowRecord record;

    public Netflow5Flow(final NetflowPacket packet, final NetflowRecord record) {
        this.packet = Objects.requireNonNull(packet);
        this.record = Objects.requireNonNull(record);
    }

    @Override
    public long getTimestamp() {
        return this.packet.getUnixSecs() * 1000L + this.packet.getUnixNSecs() / 1000L / 1000L;
    }

    @Override
    public Long getBytes() {
        return this.record.getDOctets();
    }

    @Override
    public Direction getDirection() {
        return this.record.isEgress() ? Direction.EGRESS : Direction.INGRESS;
    }

    @Override
    public String getDstAddr() {
        return this.record.getDstAddr();
    }

    @Override
    public Integer getDstAs() {
        return this.record.getDstAs();
    }

    @Override
    public Integer getDstMaskLen() {
        return this.record.getDstMask();
    }

    @Override
    public Integer getDstPort() {
        return this.record.getDstPort();
    }

    @Override
    public Integer getEngineId() {
        return this.packet.getEngineId();
    }

    @Override
    public Integer getEngineType() {
        return this.packet.getEngineType();
    }

    @Override
    public Long getFirstSwitched() {
        return getSwitched(this.getTimestamp(), this.packet.getSysUptime(), this.record.getFirst());
    }

    @Override
    public int getFlowRecords() {
        return this.packet.getCount();
    }

    @Override
    public long getFlowSeqNum() {
        return this.packet.getFlowSequence();
    }

    @Override
    public Integer getInputSnmp() {
        return this.record.getInput();
    }

    @Override
    public Integer getIpProtocolVersion() {
        return Flow.IPV4_PROTOCOL_VERSION;
    }

    @Override
    public Long getLastSwitched() {
        return getSwitched(this.getTimestamp(), this.packet.getSysUptime(), this.record.getLast());
    }

    @Override
    public String getNextHop() {
        return this.record.getNextHop();
    }

    @Override
    public Integer getOutputSnmp() {
        return this.record.getOutput();
    }

    @Override
    public Long getPackets() {
        return this.record.getDPkts();
    }

    @Override
    public Integer getProtocol() {
        return this.record.getProt();
    }

    @Override
    public Flow.SamplingAlgorithm getSamplingAlgorithm() {
        if (this.packet.getSamplingAlgorithm() == 1) {
            return Flow.SamplingAlgorithm.SystematicCountBasedSampling;
        }
        if (this.packet.getSamplingAlgorithm() == 2) {
            return Flow.SamplingAlgorithm.RandomNoutOfNSampling;
        }

        return Flow.SamplingAlgorithm.Unassigned;
    }

    @Override
    public Double getSamplingInterval() {
        return (double) this.packet.getSamplingInterval();
    }

    @Override
    public String getSrcAddr() {
        return this.record.getSrcAddr();
    }

    @Override
    public Integer getSrcAs() {
        return this.record.getSrcAs();
    }

    @Override
    public Integer getSrcMaskLen() {
        return this.record.getSrcMask();
    }

    @Override
    public Integer getSrcPort() {
        return this.record.getSrcPort();
    }

    @Override
    public Integer getTcpFlags() {
        return this.record.getTcpFlags();
    }

    @Override
    public Integer getTos() {
        return this.record.getToS();
    }

    @Override
    public NetflowVersion getNetflowVersion() {
        return NetflowVersion.V5;
    }

    @Override
    public Integer getVlan() {
        return null;
    }

    /**
     * @param timestampMs      Current unix timestamp in milliseconds.
     * @param sysUptimeMs      Current time in milliseconds since the export device booted.
     * @param switchedUptimeMs System uptime at which the this.packet was switched.
     * @return Unix timestamp in milliseconds at which the this.packet was switched.
     */
    private static long getSwitched(long timestampMs, long sysUptimeMs, long switchedUptimeMs) {
        // The this.packet was switched deltaMs ago
        final long deltaMs = sysUptimeMs - switchedUptimeMs;
        // Substract this duration from the timestamp
        return timestampMs - deltaMs;
    }
}
