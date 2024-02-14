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
package org.opennms.netmgt.flows.processing;

import java.time.Instant;
import java.util.Optional;

import static org.opennms.integration.api.v1.flows.Flow.Direction;
import static org.opennms.integration.api.v1.flows.Flow.NetflowVersion;
import static org.opennms.integration.api.v1.flows.Flow.SamplingAlgorithm;

import org.opennms.netmgt.flows.api.Flow;

public class TestFlow implements Flow {
    private Instant receivedAt;
    private Instant timestamp;
    private Long bytes;
    private Direction direction;
    private String dstAddr;
    private String dstAddrHostname;
    private Long dstAs;
    private Integer dstMaskLen;
    private Integer dstPort;
    private Integer engineId;
    private Integer engineType;
    private Instant deltaSwitched;
    private Instant firstSwitched;
    private int flowRecords;
    private long flowSeqNum;
    private Integer inputSnmp;
    private Integer ipProtocolVersion;
    private Instant lastSwitched;
    private String nextHop;
    private String nextHopHostname;
    private Integer outputSnmp;
    private Long packets;
    private Integer protocol;
    private SamplingAlgorithm samplingAlgorithm;
    private Double samplingInterval;
    private String srcAddr;
    private String srcAddrHostname;
    private Long srcAs;
    private Integer srcMaskLen;
    private Integer srcPort;
    private Integer tcpFlags;
    private Integer tos;
    private NetflowVersion netflowVersion;
    private Integer vlan;
    private String nodeIdentifier;


    @Override
    public Instant getReceivedAt() {
        return this.receivedAt;
    }

    public void setReceivedAt(final Instant receivedAt) {
        this.receivedAt = receivedAt;
    }

    @Override
    public Instant getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(final Instant timestamp) {
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
    public Instant getDeltaSwitched() {
        return this.deltaSwitched;
    }

    public void setDeltaSwitched(final Instant deltaSwitched) {
        this.deltaSwitched = deltaSwitched;
    }

    @Override
    public Instant getFirstSwitched() {
        return this.firstSwitched;
    }

    public void setFirstSwitched(final Instant firstSwitched) {
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
    public Instant getLastSwitched() {
        return this.lastSwitched;
    }

    public void setLastSwitched(final Instant lastSwitched) {
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
