/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.processing;

import java.util.Optional;

import org.opennms.netmgt.flows.api.Flow;

public class TestFlow implements Flow {
    private long receivedAt;
    private long timestamp;
    private Long bytes;
    private Flow.Direction direction;
    private String dstAddr;
    private String dstAddrHostname;
    private Long dstAs;
    private Integer dstMaskLen;
    private Integer dstPort;
    private Integer engineId;
    private Integer engineType;
    private Long deltaSwitched;
    private Long firstSwitched;
    private int flowRecords;
    private long flowSeqNum;
    private Integer inputSnmp;
    private Integer ipProtocolVersion;
    private Long lastSwitched;
    private String nextHop;
    private String nextHopHostname;
    private Integer outputSnmp;
    private Long packets;
    private Integer protocol;
    private Flow.SamplingAlgorithm samplingAlgorithm;
    private Double samplingInterval;
    private String srcAddr;
    private String srcAddrHostname;
    private Long srcAs;
    private Integer srcMaskLen;
    private Integer srcPort;
    private Integer tcpFlags;
    private Integer tos;
    private Flow.NetflowVersion netflowVersion;
    private Integer vlan;
    private String nodeIdentifier;


    @Override
    public long getReceivedAt() {
        return this.receivedAt;
    }

    public void setReceivedAt(final long receivedAt) {
        this.receivedAt = receivedAt;
    }

    @Override
    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public Long getBytes() {
        return this.bytes;
    }

    public void setBytes(final Long bytes) {
        this.bytes = bytes;
    }

    @Override
    public Direction getDirection() {
        return this.direction;
    }

    public void setDirection(final Direction direction) {
        this.direction = direction;
    }

    @Override
    public String getDstAddr() {
        return this.dstAddr;
    }

    public void setDstAddr(final String dstAddr) {
        this.dstAddr = dstAddr;
    }

    @Override
    public Optional<String> getDstAddrHostname() {
        return Optional.ofNullable(this.dstAddrHostname);
    }

    public void setDstAddrHostname(final String dstAddrHostname) {
        this.dstAddrHostname = dstAddrHostname;
    }

    @Override
    public Long getDstAs() {
        return this.dstAs;
    }

    public void setDstAs(final Long dstAs) {
        this.dstAs = dstAs;
    }

    @Override
    public Integer getDstMaskLen() {
        return this.dstMaskLen;
    }

    public void setDstMaskLen(final Integer dstMaskLen) {
        this.dstMaskLen = dstMaskLen;
    }

    @Override
    public Integer getDstPort() {
        return this.dstPort;
    }

    public void setDstPort(final Integer dstPort) {
        this.dstPort = dstPort;
    }

    @Override
    public Integer getEngineId() {
        return this.engineId;
    }

    public void setEngineId(final Integer engineId) {
        this.engineId = engineId;
    }

    @Override
    public Integer getEngineType() {
        return this.engineType;
    }

    public void setEngineType(final Integer engineType) {
        this.engineType = engineType;
    }

    @Override
    public Long getDeltaSwitched() {
        return this.deltaSwitched;
    }

    public void setDeltaSwitched(final Long deltaSwitched) {
        this.deltaSwitched = deltaSwitched;
    }

    @Override
    public Long getFirstSwitched() {
        return this.firstSwitched;
    }

    public void setFirstSwitched(final Long firstSwitched) {
        this.firstSwitched = firstSwitched;
    }

    @Override
    public int getFlowRecords() {
        return this.flowRecords;
    }

    public void setFlowRecords(final int flowRecords) {
        this.flowRecords = flowRecords;
    }

    @Override
    public long getFlowSeqNum() {
        return this.flowSeqNum;
    }

    public void setFlowSeqNum(final long flowSeqNum) {
        this.flowSeqNum = flowSeqNum;
    }

    @Override
    public Integer getInputSnmp() {
        return this.inputSnmp;
    }

    public void setInputSnmp(final Integer inputSnmp) {
        this.inputSnmp = inputSnmp;
    }

    @Override
    public Integer getIpProtocolVersion() {
        return this.ipProtocolVersion;
    }

    public void setIpProtocolVersion(final Integer ipProtocolVersion) {
        this.ipProtocolVersion = ipProtocolVersion;
    }

    @Override
    public Long getLastSwitched() {
        return this.lastSwitched;
    }

    public void setLastSwitched(final Long lastSwitched) {
        this.lastSwitched = lastSwitched;
    }

    @Override
    public String getNextHop() {
        return this.nextHop;
    }

    public void setNextHop(final String nextHop) {
        this.nextHop = nextHop;
    }

    @Override
    public Optional<String> getNextHopHostname() {
        return Optional.ofNullable(this.nextHopHostname);
    }

    public void setNextHopHostname(final String nextHopHostname) {
        this.nextHopHostname = nextHopHostname;
    }

    @Override
    public Integer getOutputSnmp() {
        return this.outputSnmp;
    }

    public void setOutputSnmp(final Integer outputSnmp) {
        this.outputSnmp = outputSnmp;
    }

    @Override
    public Long getPackets() {
        return this.packets;
    }

    public void setPackets(final Long packets) {
        this.packets = packets;
    }

    @Override
    public Integer getProtocol() {
        return this.protocol;
    }

    public void setProtocol(final Integer protocol) {
        this.protocol = protocol;
    }

    @Override
    public SamplingAlgorithm getSamplingAlgorithm() {
        return this.samplingAlgorithm;
    }

    public void setSamplingAlgorithm(final SamplingAlgorithm samplingAlgorithm) {
        this.samplingAlgorithm = samplingAlgorithm;
    }

    @Override
    public Double getSamplingInterval() {
        return this.samplingInterval;
    }

    public void setSamplingInterval(final Double samplingInterval) {
        this.samplingInterval = samplingInterval;
    }

    @Override
    public String getSrcAddr() {
        return this.srcAddr;
    }

    public void setSrcAddr(final String srcAddr) {
        this.srcAddr = srcAddr;
    }

    @Override
    public Optional<String> getSrcAddrHostname() {
        return Optional.ofNullable(this.srcAddrHostname);
    }

    public void setSrcAddrHostname(final String srcAddrHostname) {
        this.srcAddrHostname = srcAddrHostname;
    }

    @Override
    public Long getSrcAs() {
        return this.srcAs;
    }

    public void setSrcAs(final Long srcAs) {
        this.srcAs = srcAs;
    }

    @Override
    public Integer getSrcMaskLen() {
        return this.srcMaskLen;
    }

    public void setSrcMaskLen(final Integer srcMaskLen) {
        this.srcMaskLen = srcMaskLen;
    }

    @Override
    public Integer getSrcPort() {
        return this.srcPort;
    }

    public void setSrcPort(final Integer srcPort) {
        this.srcPort = srcPort;
    }

    @Override
    public Integer getTcpFlags() {
        return this.tcpFlags;
    }

    public void setTcpFlags(final Integer tcpFlags) {
        this.tcpFlags = tcpFlags;
    }

    @Override
    public Integer getTos() {
        return this.tos;
    }

    public void setTos(final Integer tos) {
        this.tos = tos;
    }

    @Override
    public NetflowVersion getNetflowVersion() {
        return this.netflowVersion;
    }

    public void setNetflowVersion(final NetflowVersion netflowVersion) {
        this.netflowVersion = netflowVersion;
    }

    @Override
    public Integer getVlan() {
        return this.vlan;
    }

    public void setVlan(final Integer vlan) {
        this.vlan = vlan;
    }

    @Override
    public String getNodeIdentifier() {
        return this.nodeIdentifier;
    }

    public void setNodeIdentifier(final String nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
    }
}
