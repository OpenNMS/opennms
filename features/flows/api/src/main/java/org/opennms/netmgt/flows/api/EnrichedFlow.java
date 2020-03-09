/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.api;

public class EnrichedFlow {

    public enum Locality {
        PUBLIC,
        PRIVATE
    }

    private String application;

    private String host;

    private String location;

    private Locality srcLocality;

    private Locality dstLocality;

    private Locality flowLocality;

    private NodeInfo srcNodeInfo;

    private NodeInfo dstNodeInfo;

    private NodeInfo exporterNodeInfo;

    private long timeStamp;

    private Long bytes;

    private Flow.Direction direction;

    private String dstAddr;

    private String dstAddrHostName;

    private Long dstAs;

    private Integer dstMaskLen;

    private Integer dstPort;

    private String srcAddr;

    private String srcAddrHostName;

    private Long srcAs;

    private Integer srcMaskLen;

    private Integer srcPort;

    private String nextHopAddr;

    private String nextHopHostName;

    private Integer engineId;

    private Integer engineType;

    private Long firstSwitched;

    private Long lastSwitched;

    private Long deltaSwitched;

    private int flowRecords;

    private long flowSeqNum;

    private Integer inputSnmp;

    private Integer ipProtocolVersion;

    private Integer outputSnmp;

    private Integer protocol;

    private Long packets;

    private Flow.SamplingAlgorithm samplingAlgorithm;

    private Double samplingInterval;

    private Integer tcpFlags;

    private Integer tos;

    private Flow.NetflowVersion netflowVersion;

    private String vlan;

    private String nodeIdentifier;


    public long getTimestamp() {
        return this.timeStamp;
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


    public String getDstAddrHostname() {
        return this.dstAddrHostName;
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

    public Long getDeltaSwitched() {
        return this.deltaSwitched;
    }

    public Long getFirstSwitched() {
        return this.firstSwitched;
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

    public Long getLastSwitched() {
        return this.lastSwitched;
    }

    public String getNextHop() {
        return this.nextHopAddr;
    }

    public String getNextHopHostname() {
        return this.nextHopHostName;
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

    public String getSrcAddrHostname() {
        return this.srcAddrHostName;
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

    public String getVlan() {
        return this.vlan;
    }

    public String getNodeIdentifier() {
        return this.nodeIdentifier;
    }

    public String getApplication() {
        return this.application;
    }

    public String getHost() {
        return this.host;
    }

    public String getLocation() {
        return this.location;
    }

    public Locality getSrcLocality() {
        return this.srcLocality;
    }

    public Locality getDstLocality() {
        return this.dstLocality;
    }

    public Locality getFlowLocality() {
        return flowLocality;
    }

    public NodeInfo getSrcNodeInfo() {
        return this.srcNodeInfo;
    }

    public NodeInfo getDstNodeInfo() {
        return this.dstNodeInfo;
    }

    public NodeInfo getExporterNodeInfo() {
        return this.exporterNodeInfo;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setBytes(Long bytes) {
        this.bytes = bytes;
    }

    public void setDirection(Flow.Direction direction) {
        this.direction = direction;
    }

    public void setDstAddr(String dstAddr) {
        this.dstAddr = dstAddr;
    }

    public void setDstAddrHostName(String dstAddrHostName) {
        this.dstAddrHostName = dstAddrHostName;
    }

    public void setDstAs(Long dstAs) {
        this.dstAs = dstAs;
    }

    public void setDstMaskLen(Integer dstMaskLen) {
        this.dstMaskLen = dstMaskLen;
    }

    public void setDstPort(Integer dstPort) {
        this.dstPort = dstPort;
    }

    public void setSrcAddr(String srcAddr) {
        this.srcAddr = srcAddr;
    }

    public void setSrcAddrHostName(String srcAddrHostName) {
        this.srcAddrHostName = srcAddrHostName;
    }

    public void setSrcAs(Long srcAs) {
        this.srcAs = srcAs;
    }

    public void setSrcMaskLen(Integer srcMaskLen) {
        this.srcMaskLen = srcMaskLen;
    }

    public void setSrcPort(Integer srcPort) {
        this.srcPort = srcPort;
    }

    public void setNextHopAddr(String nextHopAddr) {
        this.nextHopAddr = nextHopAddr;
    }

    public void setNextHopHostName(String nextHopHostName) {
        this.nextHopHostName = nextHopHostName;
    }

    public void setEngineId(Integer engineId) {
        this.engineId = engineId;
    }

    public void setEngineType(Integer engineType) {
        this.engineType = engineType;
    }

    public void setFirstSwitched(Long firstSwitched) {
        this.firstSwitched = firstSwitched;
    }

    public void setLastSwitched(Long lastSwitched) {
        this.lastSwitched = lastSwitched;
    }

    public void setDeltaSwitched(Long deltaSwitched) {
        this.deltaSwitched = deltaSwitched;
    }

    public void setFlowRecords(int flowRecords) {
        this.flowRecords = flowRecords;
    }

    public void setFlowSeqNum(long flowSeqNum) {
        this.flowSeqNum = flowSeqNum;
    }

    public void setInputSnmp(Integer inputSnmp) {
        this.inputSnmp = inputSnmp;
    }

    public void setIpProtocolVersion(Integer ipProtocolVersion) {
        this.ipProtocolVersion = ipProtocolVersion;
    }

    public void setOutputSnmp(Integer outputSnmp) {
        this.outputSnmp = outputSnmp;
    }

    public void setProtocol(Integer protocol) {
        this.protocol = protocol;
    }

    public void setPackets(Long packets) {
        this.packets = packets;
    }

    public void setSamplingAlgorithm(Flow.SamplingAlgorithm samplingAlgorithm) {
        this.samplingAlgorithm = samplingAlgorithm;
    }

    public void setSamplingInterval(Double samplingInterval) {
        this.samplingInterval = samplingInterval;
    }

    public void setTcpFlags(Integer tcpFlags) {
        this.tcpFlags = tcpFlags;
    }

    public void setTos(Integer tos) {
        this.tos = tos;
    }

    public void setNetflowVersion(Flow.NetflowVersion netflowVersion) {
        this.netflowVersion = netflowVersion;
    }

    public void setVlan(String vlan) {
        this.vlan = vlan;
    }

    public void setNodeIdentifier(String nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setSrcLocality(Locality srcLocality) {
        this.srcLocality = srcLocality;
    }

    public void setDstLocality(Locality dstLocality) {
        this.dstLocality = dstLocality;
    }

    public void setFlowLocality(Locality flowLocality) {
        this.flowLocality = flowLocality;
    }

    public void setSrcNodeInfo(NodeInfo srcNodeInfo) {
        this.srcNodeInfo = srcNodeInfo;
    }

    public void setDstNodeInfo(NodeInfo dstNodeInfo) {
        this.dstNodeInfo = dstNodeInfo;
    }

    public void setExporterNodeInfo(NodeInfo exporterNodeInfo) {
        this.exporterNodeInfo = exporterNodeInfo;
    }
}
