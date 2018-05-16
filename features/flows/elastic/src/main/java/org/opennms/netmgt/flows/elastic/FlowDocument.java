/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.elastic;

import org.opennms.netmgt.flows.api.Flow;

import com.google.gson.annotations.SerializedName;

/**
 * Member variables are sorted by the value of the @SerializedName annotation.
 */
public class FlowDocument {
    private static final int DOCUMENT_VERSION = 1;

    /**
     * Flow timestamp in milliseconds.
     */
    @SerializedName("@timestamp")
    private long timestamp;

    /**
     * Schema version.
     */
    @SerializedName("@version")
    private Integer version = DOCUMENT_VERSION;

    /**
     * Exporter IP address.
     */
    @SerializedName("host")
    private String host;

    /**
     * Exported location.
     */
    @SerializedName("location")
    private String location;

    /**
     * Application name as determined by the
     * classification engine.
     */
    @SerializedName("netflow.application")
    private String application;

    /**
     * Number of bytes transferred in the flow.
     */
    @SerializedName("netflow.bytes")
    private Long bytes;

    /**
     * Key used to group and identify conversations
     */
    @SerializedName("netflow.convo_key")
    private String convoKey;

    /**
     * Direction of the flow (egress vs ingress)
     */
    @SerializedName("netflow.direction")
    private Direction direction;

    /**
     * Destination address.
     */
    @SerializedName("netflow.dst_addr")
    private String dstAddr;

    /**
     * Destination autonomous system (AS).
     */
    @SerializedName("netflow.dst_as")
    private Integer dstAs;

    /**
     * Locality of the destination address (i.e. private vs public address)
     */
    @SerializedName("netflow.dst_locality")
    private Locality dstLocality;

    /**
     * The number of contiguous bits in the source address subnet mask.
     */
    @SerializedName("netflow.dst_mask_len")
    private Integer dstMaskLen;

    /**
     * Destination port.
     */
    @SerializedName("netflow.dst_port")
    private Integer dstPort;

    /**
     * Slot number of the flow-switching engine.
     */
    @SerializedName("netflow.engine_id")
    private Integer engineId;

    /**
     * Type of flow-switching engine.
     */
    @SerializedName("netflow.engine_type")
    private Integer engineType;

    /**
     * Unix timestamp in ms at which the first packet
     * associated with this flow was switched.
     */
    @SerializedName("netflow.first_switched")
    private Long firstSwitched;

    /**
     * Locality of the flow:
     * private if both the source and destination localities are private,
     * and public otherwise.
     */
    @SerializedName("netflow.flow_locality")
    private Locality flowLocality;

    /**
     * Number of flow records in the associated packet.
     */
    @SerializedName("netflow.flow_records")
    private int flowRecords;

    /**
     * Flow packet sequence number.
     */
    @SerializedName("netflow.flow_seq_num")
    private long flowSeqNum;

    /**
     * SNMP ifIndex
     */
    @SerializedName("netflow.input_snmp")
    private Integer inputSnmp;

    /**
     * IPv4 vs IPv6
     */
    @SerializedName("netflow.ip_protocol_version")
    private Integer ipProtocolVersion;

    /**
     * Unix timestamp in ms at which the last packet
     * associated with this flow was switched.
     */
    @SerializedName("netflow.last_switched")
    private Long lastSwitched;

    /**
     * Next hop
     */
    @SerializedName("netflow.next_hop")
    private String nextHop;

    /**
     * SNMP ifIndex
     */
    @SerializedName("netflow.output_snmp")
    private Integer outputSnmp;

    /**
     * Number of packets in the flow
     */
    @SerializedName("netflow.packets")
    private Long packets;

    /**
     * IP protocol number i.e 6 for TCP, 17 for UDP
     */
    @SerializedName("netflow.protocol")
    private Integer protocol;

    /**
     * Sampling algorithm ID
     */
    @SerializedName("netflow.sampling_algorithm")
    private SamplingAlgorithm samplingAlgorithm;

    /**
     * Sampling interval
     */
    @SerializedName("netflow.sampling_interval")
    private Double samplingInterval;

    /**
     * Source address.
     */
    @SerializedName("netflow.src_addr")
    private String srcAddr;

    /**
     * Source autonomous system (AS).
     */
    @SerializedName("netflow.src_as")
    private Integer srcAs;

    /**
     * Locality of the source address (i.e. private vs public address)
     */
    @SerializedName("netflow.src_locality")
    private Locality srcLocality;

    /**
     * The number of contiguous bits in the destination address subnet mask.
     */
    @SerializedName("netflow.src_mask_len")
    private Integer srcMaskLen;

    /**
     * Source port.
     */
    @SerializedName("netflow.src_port")
    private Integer srcPort;

    /**
     * TCP Flags.
     */
    @SerializedName("netflow.tcp_flags")
    private Integer tcpFlags;

    /**
     * TOS.
     */
    @SerializedName("netflow.tos")
    private Integer tos;

    /**
     * Netfow version
     */
    @SerializedName("netflow.version")
    private NetflowVersion netflowVersion;

    /**
     * VLAN Name.
     */
    @SerializedName("netflow.vlan")
    private String vlan;

    /**
     * Destination node details.
     */
    @SerializedName("node_dst")
    private NodeDocument nodeDst;

    /**
     * Exported node details.
     */
    @SerializedName("node_exporter")
    private NodeDocument nodeExporter;

    /**
     * Source node details.
     */
    @SerializedName("node_src")
    private NodeDocument nodeSrc;

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public Long getBytes() {
        return bytes;
    }

    public void setBytes(Long bytes) {
        this.bytes = bytes;
    }

    public String getConvoKey() {
        return convoKey;
    }

    public void setConvoKey(String convoKey) {
        this.convoKey = convoKey;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public String getDstAddr() {
        return dstAddr;
    }

    public void setDstAddr(String dstAddr) {
        this.dstAddr = dstAddr;
    }

    public Integer getDstAs() {
        return dstAs;
    }

    public void setDstAs(Integer dstAs) {
        this.dstAs = dstAs;
    }

    public Locality getDstLocality() {
        return dstLocality;
    }

    public void setDstLocality(Locality dstLocality) {
        this.dstLocality = dstLocality;
    }

    public Integer getDstMaskLen() {
        return dstMaskLen;
    }

    public void setDstMaskLen(Integer dstMaskLen) {
        this.dstMaskLen = dstMaskLen;
    }

    public Integer getDstPort() {
        return dstPort;
    }

    public void setDstPort(Integer dstPort) {
        this.dstPort = dstPort;
    }

    public Integer getEngineId() {
        return engineId;
    }

    public void setEngineId(Integer engineId) {
        this.engineId = engineId;
    }

    public Integer getEngineType() {
        return engineType;
    }

    public void setEngineType(Integer engineType) {
        this.engineType = engineType;
    }

    public Long getFirstSwitched() {
        return firstSwitched;
    }

    public void setFirstSwitched(Long firstSwitched) {
        this.firstSwitched = firstSwitched;
    }

    public Locality getFlowLocality() {
        return flowLocality;
    }

    public void setFlowLocality(Locality flowLocality) {
        this.flowLocality = flowLocality;
    }

    public int getFlowRecords() {
        return flowRecords;
    }

    public void setFlowRecords(int flowRecords) {
        this.flowRecords = flowRecords;
    }

    public long getFlowSeqNum() {
        return flowSeqNum;
    }

    public void setFlowSeqNum(long flowSeqNum) {
        this.flowSeqNum = flowSeqNum;
    }

    public Integer getInputSnmp() {
        return inputSnmp;
    }

    public void setInputSnmp(Integer inputSnmp) {
        this.inputSnmp = inputSnmp;
    }

    public Integer getIpProtocolVersion() {
        return ipProtocolVersion;
    }

    public void setIpProtocolVersion(Integer ipProtocolVersion) {
        this.ipProtocolVersion = ipProtocolVersion;
    }

    public Long getLastSwitched() {
        return lastSwitched;
    }

    public void setLastSwitched(Long lastSwitched) {
        this.lastSwitched = lastSwitched;
    }

    public String getNextHop() {
        return nextHop;
    }

    public void setNextHop(String nextHop) {
        this.nextHop = nextHop;
    }

    public Integer getOutputSnmp() {
        return outputSnmp;
    }

    public void setOutputSnmp(Integer outputSnmp) {
        this.outputSnmp = outputSnmp;
    }

    public Long getPackets() {
        return packets;
    }

    public void setPackets(Long packets) {
        this.packets = packets;
    }

    public Integer getProtocol() {
        return protocol;
    }

    public void setProtocol(Integer protocol) {
        this.protocol = protocol;
    }

    public SamplingAlgorithm getSamplingAlgorithm() {
        return samplingAlgorithm;
    }

    public void setSamplingAlgorithm(SamplingAlgorithm samplingAlgorithm) {
        this.samplingAlgorithm = samplingAlgorithm;
    }

    public Double getSamplingInterval() {
        return samplingInterval;
    }

    public void setSamplingInterval(Double samplingInterval) {
        this.samplingInterval = samplingInterval;
    }

    public String getSrcAddr() {
        return srcAddr;
    }

    public void setSrcAddr(String srcAddr) {
        this.srcAddr = srcAddr;
    }

    public Integer getSrcAs() {
        return srcAs;
    }

    public void setSrcAs(Integer srcAs) {
        this.srcAs = srcAs;
    }

    public Locality getSrcLocality() {
        return srcLocality;
    }

    public void setSrcLocality(Locality srcLocality) {
        this.srcLocality = srcLocality;
    }

    public Integer getSrcMaskLen() {
        return srcMaskLen;
    }

    public void setSrcMaskLen(Integer srcMaskLen) {
        this.srcMaskLen = srcMaskLen;
    }

    public Integer getSrcPort() {
        return srcPort;
    }

    public void setSrcPort(Integer srcPort) {
        this.srcPort = srcPort;
    }

    public Integer getTcpFlags() {
        return tcpFlags;
    }

    public void setTcpFlags(Integer tcpFlags) {
        this.tcpFlags = tcpFlags;
    }

    public Integer getTos() {
        return tos;
    }

    public void setTos(Integer tos) {
        this.tos = tos;
    }

    public NetflowVersion getNetflowVersion() {
        return netflowVersion;
    }

    public void setNetflowVersion(NetflowVersion netflowVersion) {
        this.netflowVersion = netflowVersion;
    }

    public String getVlan() {
        return vlan;
    }

    public void setVlan(String vlan) {
        this.vlan = vlan;
    }

    public NodeDocument getNodeDst() {
        return nodeDst;
    }

    public void setNodeDst(NodeDocument nodeDst) {
        this.nodeDst = nodeDst;
    }

    public NodeDocument getNodeExporter() {
        return nodeExporter;
    }

    public void setNodeExporter(NodeDocument nodeExporter) {
        this.nodeExporter = nodeExporter;
    }

    public NodeDocument getNodeSrc() {
        return nodeSrc;
    }

    public void setNodeSrc(NodeDocument nodeSrc) {
        this.nodeSrc = nodeSrc;
    }

    public static FlowDocument from(final Flow flow) {
        final FlowDocument doc = new FlowDocument();
        doc.setTimestamp(flow.getTimestamp());
        doc.setBytes(flow.getBytes());
        doc.setDirection(Direction.from(flow.getDirection()));
        doc.setDstAddr(flow.getDstAddr());
        doc.setDstAs(flow.getDstAs());
        doc.setDstMaskLen(flow.getDstMaskLen());
        doc.setDstPort(flow.getDstPort());
        doc.setEngineId(flow.getEngineId());
        doc.setEngineType(flow.getEngineType());
        doc.setFirstSwitched(flow.getFirstSwitched());
        doc.setFlowRecords(flow.getFlowRecords());
        doc.setFlowSeqNum(flow.getFlowSeqNum());
        doc.setInputSnmp(flow.getInputSnmp());
        doc.setIpProtocolVersion(flow.getIpProtocolVersion());
        doc.setLastSwitched(flow.getLastSwitched());
        doc.setNextHop(flow.getNextHop());
        doc.setOutputSnmp(flow.getOutputSnmp());
        doc.setPackets(flow.getPackets());
        doc.setProtocol(flow.getProtocol());
        doc.setSamplingAlgorithm(SamplingAlgorithm.from(flow.getSamplingAlgorithm()));
        doc.setSamplingInterval(flow.getSamplingInterval());
        doc.setSrcAddr(flow.getSrcAddr());
        doc.setSrcAs(flow.getSrcAs());
        doc.setSrcMaskLen(flow.getSrcMaskLen());
        doc.setSrcPort(flow.getSrcPort());
        doc.setTcpFlags(flow.getTcpFlags());
        doc.setTos(flow.getTos());
        doc.setNetflowVersion(NetflowVersion.from(flow.getNetflowVersion()));
        doc.setVlan(flow.getVlan() != null ? Integer.toUnsignedString(flow.getVlan()) : null);

        return doc;
    }
}
