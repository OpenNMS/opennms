/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.flows.api;

/**
 * The document to be persisted.
 */
public class NetflowDocument extends FlowDocument {

    private int version;

    private long flowSequenceNumber; //flow_sequence

    private int engineType;

    private int engineId;

    private int samplingInterval;

    private int flowRecords; // count

    private long sysUptime; // sysUptime

    private long timestamp; // unix_secs * 1000 + unix_nsecs / 1000 / 1000;

    private String ipv4SourceAddress;

    private String ipv4DestAddress;

    private String ipv4NextHopAddress;

    private int inputSnmpInterfaceIndex;

    private int outputSnmpInterfaceIndex;

    private long inPackets;

    private long inBytes;

    private long first;

    private long last;

    private int sourcePort;

    private int destPort;

    private int tcpFlags;

    private int ipProtocol;

    private int tos;

    private int sourceAs;

    private int destAs;

    private int sourceMask;

    private int destMask;

    private String application;

    private NodeInfo exporterNodeInfo;
    private NodeInfo sourceNodeInfo;
    private NodeInfo destNodeInfo;

    public void setVersion(int version) {
        this.version = version;
    }

    public void setFlowSequenceNumber(long flowSequenceNumber) {
        this.flowSequenceNumber = flowSequenceNumber;
    }

    public void setEngineType(int engineType) {
        this.engineType = engineType;
    }

    public void setEngineId(int engineId) {
        this.engineId = engineId;
    }

    public void setSamplingInterval(int samplingInterval) {
        this.samplingInterval = samplingInterval;
    }

    public void setFlowRecords(int flowRecords) {
        this.flowRecords = flowRecords;
    }

    public void setSysUptime(long sysUptime) {
        this.sysUptime = sysUptime;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setIpv4SourceAddress(String ipV4SourceAddress) {
        this.ipv4SourceAddress = ipV4SourceAddress;
    }

    public void setIpv4DestAddress(String ipV4DestAddress) {
        this.ipv4DestAddress = ipV4DestAddress;
    }

    public void setIpv4NextHopAddress(String ipv4NextHopAddress) {
        this.ipv4NextHopAddress = ipv4NextHopAddress;
    }

    public void setInputSnmpInterfaceIndex(int inputSnmpInterfaceIndex) {
        this.inputSnmpInterfaceIndex = inputSnmpInterfaceIndex;
    }

    public void setOutputSnmpInterfaceIndex(int outputSnmpInterfaceIndex) {
        this.outputSnmpInterfaceIndex = outputSnmpInterfaceIndex;
    }

    public void setInPackets(long inPackets) {
        this.inPackets = inPackets;
    }

    public void setInBytes(long inBytes) {
        this.inBytes = inBytes;
    }

    public void setFirst(long first) {
        this.first = first;
    }

    public void setLast(long last) {
        this.last = last;
    }

    public void setSourcePort(int sourcePort) {
        this.sourcePort = sourcePort;
    }

    public void setDestPort(int destPort) {
        this.destPort = destPort;
    }

    public void setTcpFlags(int tcpFlags) {
        this.tcpFlags = tcpFlags;
    }

    public void setIpProtocol(int ipProtocol) {
        this.ipProtocol = ipProtocol;
    }

    public void setTos(int tos) {
        this.tos = tos;
    }

    public void setSourceAutonomousSystemNumber(int sourceAs) {
        this.sourceAs = sourceAs;
    }

    public void setDestAutonomousSystemNumber(int destAs) {
        this.destAs = destAs;
    }

    public void setSourceMask(int sourceMask) {
        this.sourceMask = sourceMask;
    }

    public void setDestMask(int destMask) {
        this.destMask = destMask;
    }

    public int getVersion() {
        return version;
    }

    public long getFlowSequenceNumber() {
        return flowSequenceNumber;
    }

    public int getEngineType() {
        return engineType;
    }

    public int getEngineId() {
        return engineId;
    }

    public int getSamplingInterval() {
        return samplingInterval;
    }

    public int getFlowRecords() {
        return flowRecords;
    }

    public long getSysUptime() {
        return sysUptime;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getIpv4SourceAddress() {
        return ipv4SourceAddress;
    }

    public String getIpv4DestAddress() {
        return ipv4DestAddress;
    }

    public String getIpv4NextHopAddress() {
        return ipv4NextHopAddress;
    }

    public int getInputSnmpInterfaceIndex() {
        return inputSnmpInterfaceIndex;
    }

    public int getOutputSnmpInterfaceIndex() {
        return outputSnmpInterfaceIndex;
    }

    public long getInPackets() {
        return inPackets;
    }

    public long getInBytes() {
        return inBytes;
    }

    public long getFirst() {
        return first;
    }

    public long getLast() {
        return last;
    }

    public int getSourcePort() {
        return sourcePort;
    }

    public int getDestPort() {
        return destPort;
    }

    public int getTcpFlags() {
        return tcpFlags;
    }

    public int getIpProtocol() {
        return ipProtocol;
    }

    public int getTos() {
        return tos;
    }

    public int getSourceAs() {
        return sourceAs;
    }

    public int getDestAs() {
        return destAs;
    }

    public int getSourceMask() {
        return sourceMask;
    }

    public int getDestMask() {
        return destMask;
    }

    public void setSourceAs(int sourceAs) {
        this.sourceAs = sourceAs;
    }

    public void setDestAs(int destAs) {
        this.destAs = destAs;
    }

    public void setExporterNodeInfo(NodeInfo exporterNodeInfo) {
        this.exporterNodeInfo = exporterNodeInfo;
    }

    public NodeInfo getExporterNodeInfo() {
        return exporterNodeInfo;
    }

    public void setSourceNodeInfo(NodeInfo sourceNodeInfo) {
        this.sourceNodeInfo = sourceNodeInfo;
    }

    public NodeInfo getSourceNodeInfo() {
        return sourceNodeInfo;
    }

    public void setDestNodeInfo(NodeInfo destNodeInfo) {
        this.destNodeInfo = destNodeInfo;
    }

    public NodeInfo getDestNodeInfo() {
        return destNodeInfo;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public String getApplication() {
        return application;
    }
}
