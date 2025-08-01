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
package org.opennms.netmgt.flows.processing.enrichment;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.flows.processing.ConversationKeyUtils;

public class EnrichedFlow implements org.opennms.integration.api.v1.flows.Flow {

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
    private Integer dscp;
    private Integer ecn;
    private NetflowVersion netflowVersion;
    private Integer vlan;

    private String application;
    private String host;
    private String location;
    private Locality srcLocality;
    private Locality dstLocality;
    private Locality flowLocality;
    private NodeInfo srcNodeInfo;
    private NodeInfo dstNodeInfo;
    private NodeInfo exporterNodeInfo;
    private Duration clockCorrection;

    public EnrichedFlow() {
    }

    @Override
    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    @Override
    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    @Override
    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public Locality getSrcLocality() {
        return srcLocality;
    }

    public void setSrcLocality(Locality srcLocality) {
        this.srcLocality = srcLocality;
    }

    @Override
    public Locality getDstLocality() {
        return dstLocality;
    }

    public void setDstLocality(Locality dstLocality) {
        this.dstLocality = dstLocality;
    }

    @Override
    public Locality getFlowLocality() {
        return flowLocality;
    }

    public void setFlowLocality(Locality flowLocality) {
        this.flowLocality = flowLocality;
    }

    @Override
    public NodeInfo getSrcNodeInfo() {
        return srcNodeInfo;
    }

    public void setSrcNodeInfo(NodeInfo srcNodeInfo) {
        this.srcNodeInfo = srcNodeInfo;
    }

    @Override
    public NodeInfo getDstNodeInfo() {
        return dstNodeInfo;
    }

    public void setDstNodeInfo(NodeInfo dstNodeInfo) {
        this.dstNodeInfo = dstNodeInfo;
    }

    @Override
    public NodeInfo getExporterNodeInfo() {
        return exporterNodeInfo;
    }

    public void setExporterNodeInfo(NodeInfo exporterNodeInfo) {
        this.exporterNodeInfo = exporterNodeInfo;
    }

    @Override
    public Duration getClockCorrection() {
        return this.clockCorrection;
    }

    public void setClockCorrection(final Duration clockCorrection) {
        this.clockCorrection = clockCorrection;
    }

    @Override
    public Instant getTimestamp() {
        return this.timestamp;
    }

    @Override
    public Instant getFirstSwitched() {
        return this.firstSwitched;
    }

    @Override
    public Instant getDeltaSwitched() {
        return this.deltaSwitched;
    }

    @Override
    public Instant getLastSwitched() {
        return this.lastSwitched;
    }

    @Override
    public Instant getReceivedAt() {
        return this.receivedAt;
    }

    @Override
    public Long getBytes() {
        return this.bytes;
    }

    @Override
    public Direction getDirection() {
        return this.direction;
    }

    @Override
    public String getDstAddr() {
        return this.dstAddr;
    }

    @Override
    public Optional<String> getDstAddrHostname() {
        return Optional.ofNullable(this.dstAddrHostname);
    }

    @Override
    public Long getDstAs() {
        return this.dstAs;
    }

    @Override
    public Integer getDstMaskLen() {
        return this.dstMaskLen;
    }

    @Override
    public Integer getDstPort() {
        return this.dstPort;
    }

    @Override
    public Integer getEngineId() {
        return this.engineId;
    }

    @Override
    public Integer getEngineType() {
        return this.engineType;
    }

    public void setDeltaSwitched(final Instant deltaSwitched) {
        this.deltaSwitched = deltaSwitched;
    }

    public void setFirstSwitched(final Instant firstSwitched) {
        this.firstSwitched = firstSwitched;
    }

    @Override
    public int getFlowRecords() {
        return this.flowRecords;
    }

    @Override
    public long getFlowSeqNum() {
        return this.flowSeqNum;
    }

    @Override
    public Integer getInputSnmp() {
        return this.inputSnmp;
    }

    @Override
    public Integer getIpProtocolVersion() {
        return this.ipProtocolVersion;
    }

    public void setLastSwitched(final Instant lastSwitched) {
        this.lastSwitched = lastSwitched;
    }

    @Override
    public String getNextHop() {
        return this.nextHop;
    }

    @Override
    public Optional<String> getNextHopHostname() {
        return Optional.ofNullable(this.nextHopHostname);
    }

    @Override
    public Integer getOutputSnmp() {
        return this.outputSnmp;
    }

    @Override
    public Long getPackets() {
        return this.packets;
    }

    @Override
    public Integer getProtocol() {
        return this.protocol;
    }

    @Override
    public SamplingAlgorithm getSamplingAlgorithm() {
        return this.samplingAlgorithm;
    }

    @Override
    public Double getSamplingInterval() {
        return this.samplingInterval;
    }

    @Override
    public String getSrcAddr() {
        return this.srcAddr;
    }

    @Override
    public Optional<String> getSrcAddrHostname() {
        return Optional.ofNullable(this.srcAddrHostname);
    }

    @Override
    public Long getSrcAs() {
        return this.srcAs;
    }

    @Override
    public Integer getSrcMaskLen() {
        return this.srcMaskLen;
    }

    @Override
    public Integer getSrcPort() {
        return this.srcPort;
    }

    @Override
    public Integer getTcpFlags() {
        return this.tcpFlags;
    }

    @Override
    public Integer getTos() {
        return this.tos;
    }

    @Override
    public NetflowVersion getNetflowVersion() {
        return this.netflowVersion;
    }

    @Override
    public Integer getVlan() {
        return this.vlan;
    }

    public void setReceivedAt(final Instant receivedAt) {
        this.receivedAt = receivedAt;
    }

    public void setTimestamp(final Instant timestamp) {
        this.timestamp = timestamp;
    }

    public void setBytes(final Long bytes) {
        this.bytes = bytes;
    }

    public void setDirection(final Direction direction) {
        this.direction = direction;
    }

    public void setDstAddr(final String dstAddr) {
        this.dstAddr = dstAddr;
    }

    public void setDstAddrHostname(final String dstAddrHostname) {
        this.dstAddrHostname = dstAddrHostname;
    }

    public void setDstAs(final Long dstAs) {
        this.dstAs = dstAs;
    }

    public void setDstMaskLen(final Integer dstMaskLen) {
        this.dstMaskLen = dstMaskLen;
    }

    public void setDstPort(final Integer dstPort) {
        this.dstPort = dstPort;
    }

    public void setEngineId(final Integer engineId) {
        this.engineId = engineId;
    }

    public void setEngineType(final Integer engineType) {
        this.engineType = engineType;
    }

    public void setFlowRecords(final int flowRecords) {
        this.flowRecords = flowRecords;
    }

    public void setFlowSeqNum(final long flowSeqNum) {
        this.flowSeqNum = flowSeqNum;
    }

    public void setInputSnmp(final Integer inputSnmp) {
        this.inputSnmp = inputSnmp;
    }

    public void setIpProtocolVersion(final Integer ipProtocolVersion) {
        this.ipProtocolVersion = ipProtocolVersion;
    }

    public void setNextHop(final String nextHop) {
        this.nextHop = nextHop;
    }

    public void setNextHopHostname(final String nextHopHostname) {
        this.nextHopHostname = nextHopHostname;
    }

    public void setOutputSnmp(final Integer outputSnmp) {
        this.outputSnmp = outputSnmp;
    }

    public void setPackets(final Long packets) {
        this.packets = packets;
    }

    public void setProtocol(final Integer protocol) {
        this.protocol = protocol;
    }

    public void setSamplingAlgorithm(final SamplingAlgorithm samplingAlgorithm) {
        this.samplingAlgorithm = samplingAlgorithm;
    }

    public void setSamplingInterval(final Double samplingInterval) {
        this.samplingInterval = samplingInterval;
    }

    public void setSrcAddr(final String srcAddr) {
        this.srcAddr = srcAddr;
    }

    public void setSrcAddrHostname(final String srcAddrHostname) {
        this.srcAddrHostname = srcAddrHostname;
    }

    public void setSrcAs(final Long srcAs) {
        this.srcAs = srcAs;
    }

    public void setSrcMaskLen(final Integer srcMaskLen) {
        this.srcMaskLen = srcMaskLen;
    }

    public void setSrcPort(final Integer srcPort) {
        this.srcPort = srcPort;
    }

    public void setTcpFlags(final Integer tcpFlags) {
        this.tcpFlags = tcpFlags;
    }

    public void setTos(final Integer tos) {
        this.tos = tos;
    }

    @Override
    public Integer getDscp() {
        return this.dscp;
    }

    public void setDscp(final Integer dscp) {
        this.dscp = dscp;
    }

    @Override
    public Integer getEcn() {
        return this.ecn;
    }

    public void setEcn(final Integer ecn) {
        this.ecn = ecn;
    }

    public void setNetflowVersion(final NetflowVersion netflowVersion) {
        this.netflowVersion = netflowVersion;
    }

    public void setVlan(final Integer vlan) {
        this.vlan = vlan;
    }

    @Override
    public String getConvoKey() {
        return ConversationKeyUtils.getConvoKeyAsJsonString(this.getLocation(),
                                                            this.getProtocol(),
                                                            this.getSrcAddr(),
                                                            this.getDstAddr(),
                                                            this.getApplication());
    }

    public static EnrichedFlow from(final Flow flow) {
        final var enriched = new EnrichedFlow();

        enriched.setReceivedAt(flow.getReceivedAt());
        enriched.setTimestamp(flow.getTimestamp());
        enriched.setBytes(flow.getBytes());
        enriched.setDirection(flow.getDirection());
        enriched.setDstAddr(flow.getDstAddr());
        flow.getDstAddrHostname().ifPresent(enriched::setDstAddrHostname);
        enriched.setDstAs(flow.getDstAs());
        enriched.setDstMaskLen(flow.getDstMaskLen());
        enriched.setDstPort(flow.getDstPort());
        enriched.setEngineId(flow.getEngineId());
        enriched.setEngineType(flow.getEngineType());
        enriched.setDeltaSwitched(flow.getDeltaSwitched());
        enriched.setFirstSwitched(flow.getFirstSwitched());
        enriched.setFlowRecords(flow.getFlowRecords());
        enriched.setFlowSeqNum(flow.getFlowSeqNum());
        enriched.setInputSnmp(flow.getInputSnmp());
        enriched.setIpProtocolVersion(flow.getIpProtocolVersion());
        enriched.setLastSwitched(flow.getLastSwitched());
        enriched.setNextHop(flow.getNextHop());
        flow.getNextHopHostname().ifPresent(enriched::setNextHopHostname);
        enriched.setOutputSnmp(flow.getOutputSnmp());
        enriched.setPackets(flow.getPackets());
        enriched.setProtocol(flow.getProtocol());
        enriched.setSamplingAlgorithm(flow.getSamplingAlgorithm());
        enriched.setSamplingInterval(flow.getSamplingInterval());
        enriched.setSrcAddr(flow.getSrcAddr());
        flow.getSrcAddrHostname().ifPresent(enriched::setSrcAddrHostname);
        enriched.setSrcAs(flow.getSrcAs());
        enriched.setSrcMaskLen(flow.getSrcMaskLen());
        enriched.setSrcPort(flow.getSrcPort());
        enriched.setTcpFlags(flow.getTcpFlags());
        enriched.setTos(flow.getTos());
        enriched.setDscp(flow.getDscp());
        enriched.setEcn(flow.getEcn());
        enriched.setNetflowVersion(flow.getNetflowVersion());
        enriched.setVlan(flow.getVlan());

        return enriched;
    }
}
