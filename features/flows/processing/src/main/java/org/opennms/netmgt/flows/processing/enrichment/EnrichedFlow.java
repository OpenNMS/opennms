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

package org.opennms.netmgt.flows.processing.enrichment;

import java.util.Optional;

import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.flows.processing.ConversationKeyUtils;

public class EnrichedFlow {
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
    private Integer dscp;
    private Integer ecn;
    private Flow.NetflowVersion netflowVersion;
    private Integer vlan;
    private String nodeIdentifier;

    private String application;
    private String host;
    private String location;
    private Locality srcLocality;
    private Locality dstLocality;
    private Locality flowLocality;
    private NodeInfo srcNodeInfo;
    private NodeInfo dstNodeInfo;
    private NodeInfo exporterNodeInfo;
    private long clockCorrection;

    public EnrichedFlow() {
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Locality getSrcLocality() {
        return srcLocality;
    }

    public void setSrcLocality(Locality srcLocality) {
        this.srcLocality = srcLocality;
    }

    public Locality getDstLocality() {
        return dstLocality;
    }

    public void setDstLocality(Locality dstLocality) {
        this.dstLocality = dstLocality;
    }

    public Locality getFlowLocality() {
        return flowLocality;
    }

    public void setFlowLocality(Locality flowLocality) {
        this.flowLocality = flowLocality;
    }

    public NodeInfo getSrcNodeInfo() {
        return srcNodeInfo;
    }

    public void setSrcNodeInfo(NodeInfo srcNodeInfo) {
        this.srcNodeInfo = srcNodeInfo;
    }

    public NodeInfo getDstNodeInfo() {
        return dstNodeInfo;
    }

    public void setDstNodeInfo(NodeInfo dstNodeInfo) {
        this.dstNodeInfo = dstNodeInfo;
    }

    public NodeInfo getExporterNodeInfo() {
        return exporterNodeInfo;
    }

    public void setExporterNodeInfo(NodeInfo exporterNodeInfo) {
        this.exporterNodeInfo = exporterNodeInfo;
    }

    public long getClockCorrection() {
        return this.clockCorrection;
    }

    public void setClockCorrection(final long clockCorrection) {
        this.clockCorrection = clockCorrection;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public Long getFirstSwitched() {
        return this.firstSwitched;
    }

    public Long getDeltaSwitched() {
        return this.deltaSwitched;
    }

    public Long getLastSwitched() {
        return this.lastSwitched;
    }

    public long getReceivedAt() {
        return this.receivedAt;
    }

    public void setTimestamp(final Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getBytes() {
        return this.bytes;
    }

    public Flow.Direction getDirection() {
        return this.direction;
    }

    public String getDstAddr() {
        return this.dstAddr;
    }

    public Optional<String> getDstAddrHostname() {
        return Optional.ofNullable(this.dstAddrHostname);
    }

    public Long getDstAs() {
        return this.dstAs;
    }

    public Integer getDstMaskLen() {
        return this.dstMaskLen;
    }

    public Integer getDstPort() {
        return this.dstPort;
    }

    public Integer getEngineId() {
        return this.engineId;
    }

    public Integer getEngineType() {
        return this.engineType;
    }

    public void setDeltaSwitched(final Long deltaSwitched) {
        this.deltaSwitched = deltaSwitched;
    }

    public void setFirstSwitched(final Long firstSwitched) {
        this.firstSwitched = firstSwitched;
    }

    public int getFlowRecords() {
        return this.flowRecords;
    }

    public long getFlowSeqNum() {
        return this.flowSeqNum;
    }

    public Integer getInputSnmp() {
        return this.inputSnmp;
    }

    public Integer getIpProtocolVersion() {
        return this.ipProtocolVersion;
    }

    public void setLastSwitched(final Long lastSwitched) {
        this.lastSwitched = lastSwitched;
    }

    public String getNextHop() {
        return this.nextHop;
    }

    public Optional<String> getNextHopHostname() {
        return Optional.ofNullable(this.nextHopHostname);
    }

    public Integer getOutputSnmp() {
        return this.outputSnmp;
    }

    public Long getPackets() {
        return this.packets;
    }

    public Integer getProtocol() {
        return this.protocol;
    }

    public Flow.SamplingAlgorithm getSamplingAlgorithm() {
        return this.samplingAlgorithm;
    }

    public Double getSamplingInterval() {
        return this.samplingInterval;
    }

    public String getSrcAddr() {
        return this.srcAddr;
    }

    public Optional<String> getSrcAddrHostname() {
        return Optional.ofNullable(this.srcAddrHostname);
    }

    public Long getSrcAs() {
        return this.srcAs;
    }

    public Integer getSrcMaskLen() {
        return this.srcMaskLen;
    }

    public Integer getSrcPort() {
        return this.srcPort;
    }

    public Integer getTcpFlags() {
        return this.tcpFlags;
    }

    public Integer getTos() {
        return this.tos;
    }

    public Flow.NetflowVersion getNetflowVersion() {
        return this.netflowVersion;
    }

    public Integer getVlan() {
        return this.vlan;
    }

    public String getNodeIdentifier() {
        return this.nodeIdentifier;
    }

    public void setReceivedAt(final long receivedAt) {
        this.receivedAt = receivedAt;
    }

    public void setTimestamp(final long timestamp) {
        this.timestamp = timestamp;
    }

    public void setBytes(final Long bytes) {
        this.bytes = bytes;
    }

    public void setDirection(final Flow.Direction direction) {
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

    public void setSamplingAlgorithm(final Flow.SamplingAlgorithm samplingAlgorithm) {
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

    public Integer getDscp() {
        return this.dscp;
    }

    public void setDscp(final Integer dscp) {
        this.dscp = dscp;
    }

    public Integer getEcn() {
        return this.ecn;
    }

    public void setEcn(final Integer ecn) {
        this.ecn = ecn;
    }

    public void setNetflowVersion(final Flow.NetflowVersion netflowVersion) {
        this.netflowVersion = netflowVersion;
    }

    public void setVlan(final Integer vlan) {
        this.vlan = vlan;
    }

    public void setNodeIdentifier(final String nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
    }

    public String getConvoKey() {
        return ConversationKeyUtils.getConvoKeyAsJsonString(this.getLocation(),
                                                            this.getProtocol(),
                                                            this.getSrcAddr(),
                                                            this.getDstAddr(),
                                                            this.getApplication());
    }

    public enum Locality {
        PUBLIC, PRIVATE
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
        enriched.setNodeIdentifier(flow.getNodeIdentifier());

        return enriched;
    }
}
