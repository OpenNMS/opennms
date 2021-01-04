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

package org.opennms.netmgt.telemetry.protocols.netflow.parser.transport;

import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.first;
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.getDoubleValue;
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.getInetAddress;
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.getLongValue;
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.getUInt32Value;
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.getUInt64Value;
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.setIntValue;
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.setLongValue;

import java.net.InetAddress;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.IllegalFlowException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.RecordEnrichment;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.Direction;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.NetflowVersion;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.SamplingAlgorithm;

public class Netflow9MessageBuilder {

    private final FlowMessage.Builder builder;
    private final Iterable<Value<?>> values;
    private final RecordEnrichment enrichment;
    private InetAddress ipv4DstAddress;
    private InetAddress ipv6DstAddress;
    private Long dstMask;
    private Long ipv6DstMask;
    private InetAddress ipv4NextHop;
    private InetAddress ipv6NextHop;
    private InetAddress bgpIpv4NextHop;
    private InetAddress bgpIpv6NextHop;
    private InetAddress ipv4SrcAddress;
    private InetAddress ipv6SrcAddress;
    private Long srcMask;
    private Long ipv6SrcMask;
    private Long srcVlan;
    private Long dstVlan;
    private Long flowActiveTimeout;
    private Long flowInActiveTimeout;
    private Long sysUpTime;
    private Long unixSecs;
    private Long numBytes;
    private Long numPackets;
    private Long firstSwitched;
    private Long lastSwitched;
    private Long flowStartMilliseconds;
    private Long flowEndMilliseconds;


    public Netflow9MessageBuilder(Iterable<Value<?>> values, RecordEnrichment enrichment) {
        this.values = values;
        this.enrichment = enrichment;
        builder = FlowMessage.newBuilder();
    }


    public byte[] buildData() throws IllegalFlowException {

        values.forEach(this::addField);

        long timeStampInMsecs = this.unixSecs != null ? this.unixSecs * 1000 : 0;
        builder.setTimestamp(timeStampInMsecs);

        long bootTime = timeStampInMsecs - this.sysUpTime;

        if (this.firstSwitched != null) {
            builder.setFirstSwitched(setLongValue(this.firstSwitched + bootTime));
        }
        if(this.lastSwitched != null) {
            builder.setLastSwitched(setLongValue(this.lastSwitched + bootTime));
        }

        // Some Cisco platforms also support absolute timestamps in NetFlow v9 (like defined in IPFIX). See NMS-13006
        if (this.flowStartMilliseconds != null) {
            builder.setFirstSwitched(setLongValue(flowStartMilliseconds));
        }
        if (this.flowEndMilliseconds != null) {
            builder.setLastSwitched(setLongValue(flowEndMilliseconds));
        }

        // Set Destination address and host name.
        first(ipv6DstAddress, ipv4DstAddress).ifPresent(inetAddress -> {
            enrichment.getHostnameFor(inetAddress).ifPresent(builder::setDstHostname);
            builder.setDstAddress(inetAddress.getHostAddress());
        });

        // dst mask
        first(ipv6DstMask, dstMask).ifPresent(dstMaskLen ->
            builder.setDstMaskLen(setIntValue(dstMaskLen.intValue())));

        // Set Source address and host name.
        first(ipv6SrcAddress, ipv4SrcAddress).ifPresent(inetAddress -> {
            enrichment.getHostnameFor(inetAddress).ifPresent(builder::setSrcHostname);
            builder.setSrcAddress(inetAddress.getHostAddress());
        });
        // src mask
        first(ipv6SrcMask, srcMask).ifPresent(srcMaskLen -> builder.setSrcMaskLen(setIntValue(srcMaskLen.intValue())));

        // Set next hop address, hostname.
        first(ipv6NextHop, ipv4NextHop, bgpIpv6NextHop, bgpIpv4NextHop).ifPresent(inetAddress -> {
            enrichment.getHostnameFor(inetAddress).ifPresent(builder::setNextHopHostname);
            builder.setNextHopAddress(inetAddress.getHostAddress());
        });

        if (builder.getFirstSwitched().getValue() > builder.getLastSwitched().getValue()) {
            throw new IllegalFlowException(
                    String.format("lastSwitched must be greater than firstSwitched: srcAddress=%s, dstAddress=%s, firstSwitched=%d, lastSwitched=%d, duration=%d",
                            builder.getSrcAddress(),
                            builder.getDstAddress(),
                            builder.getFirstSwitched().getValue(),
                            builder.getLastSwitched().getValue(),
                            builder.getLastSwitched().getValue() - builder.getFirstSwitched().getValue()));
        }

        // set vlan
        first(srcVlan, dstVlan).ifPresent( vlan -> builder.setVlan(setIntValue(vlan.intValue())));

        Long firstSwitched = builder.hasFirstSwitched() ? builder.getFirstSwitched().getValue() : null;
        Long lastSwitched = builder.hasLastSwitched() ? builder.getLastSwitched().getValue() : null;

        Timeout timeout = new Timeout(flowActiveTimeout, flowInActiveTimeout);
        timeout.setFirstSwitched(firstSwitched);
        timeout.setLastSwitched(lastSwitched);
        timeout.setNumBytes(this.numBytes);
        timeout.setNumPackets(this.numPackets);
        Long deltaSwitched = timeout.getDeltaSwitched();
        getUInt64Value(deltaSwitched).ifPresent(builder::setDeltaSwitched);

        builder.setNetflowVersion(NetflowVersion.V9);
        return builder.build().toByteArray();
    }

    private void addField(Value<?> value) {

        switch (value.getName()) {
            // Header
            case "@recordCount":
                getUInt32Value(value).ifPresent(builder::setNumFlowRecords);
                break;
            case "@sequenceNumber":
                getUInt64Value(value).ifPresent(builder::setFlowSeqNum);
                break;
            case "@sourceId":
                getUInt64Value(value).ifPresent(srcId ->
                        builder.setNodeIdentifier(String.valueOf(srcId.getValue())));
                break;
            case "@sysUpTime":
                sysUpTime = getLongValue(value);
                break;
            case "@unixSecs":
                unixSecs = getLongValue(value);
                break;
            case "IN_BYTES":
                getUInt64Value(value).ifPresent(bytes -> {
                    numBytes = bytes.getValue();
                    builder.setNumBytes(bytes);
                });
                break;
            case "DIRECTION":
                Long directionValue = getLongValue(value);
                Direction direction = Direction.UNRECOGNIZED;
                if (directionValue != null) {
                    switch (directionValue.intValue()) {
                        case 0:
                            direction = Direction.INGRESS;
                            break;
                        case 1:
                            direction = Direction.EGRESS;
                            break;
                    }
                }
                if (!direction.equals(Direction.UNRECOGNIZED)) {
                    this.builder.setDirection(direction);
                }
                break;
            case "IPV4_DST_ADDR":
                ipv4DstAddress = getInetAddress(value);
                break;
            case "IPV6_DST_ADDR":
                ipv6DstAddress = getInetAddress(value);
                break;
            case "DST_AS":
                getUInt64Value(value).ifPresent(builder::setDstAs);
                break;
            case "IPV6_DST_MASK":
                ipv6DstMask = getLongValue(value);
                break;
            case "DST_MASK":
                dstMask = getLongValue(value);
                break;
            case "L4_DST_PORT":
                getUInt32Value(value).ifPresent(builder::setDstPort);
                break;
            case "ENGINE_ID":
                getUInt32Value(value).ifPresent(builder::setEngineId);
                break;
            case "ENGINE_TYPE":
                getUInt32Value(value).ifPresent(builder::setEngineType);
                break;
            case "FIRST_SWITCHED":
                firstSwitched = getLongValue(value);
                break;
            case "LAST_SWITCHED":
                lastSwitched = getLongValue(value);
                break;
            case "INPUT_SNMP":
                getUInt32Value(value).ifPresent(builder::setInputSnmpIfindex);
                break;
            case "IP_PROTOCOL_VERSION":
                getUInt32Value(value).ifPresent(builder::setIpProtocolVersion);
                break;
            case "OUTPUT_SNMP":
                getUInt32Value(value).ifPresent(builder::setOutputSnmpIfindex);
                break;
            case "IPV6_NEXT_HOP":
                ipv6NextHop = getInetAddress(value);
                break;
            case "IPV4_NEXT_HOP":
                ipv4NextHop = getInetAddress(value);
                break;
            case "BPG_IPV6_NEXT_HOP":
                bgpIpv6NextHop = getInetAddress(value);
                break;
            case "BPG_IPV4_NEXT_HOP":
                bgpIpv4NextHop = getInetAddress(value);
                break;
            case "IN_PKTS":
                getUInt64Value(value).ifPresent(numPackets -> {
                    this.numPackets = numPackets.getValue();
                    builder.setNumPackets(numPackets);
                });
                break;
            case "PROTOCOL":
                getUInt32Value(value).ifPresent(builder::setProtocol);
                break;
            case "SAMPLING_ALGORITHM":
                Long saValue = getLongValue(value);
                SamplingAlgorithm samplingAlgorithm = SamplingAlgorithm.UNASSIGNED;
                if (saValue != null) {
                    if (saValue.intValue() == 1) {
                        samplingAlgorithm = SamplingAlgorithm.SYSTEMATIC_COUNT_BASED_SAMPLING;
                    }
                    if (saValue.intValue() == 2) {
                        samplingAlgorithm = SamplingAlgorithm.RANDOM_N_OUT_OF_N_SAMPLING;
                    }
                }
                builder.setSamplingAlgorithm(samplingAlgorithm);
                break;
            case "SAMPLING_INTERVAL":
                getDoubleValue(value).ifPresent(builder::setSamplingInterval);
                break;
            case "IPV6_SRC_ADDR":
                ipv6SrcAddress = getInetAddress(value);
                break;
            case "IPV4_SRC_ADDR":
                ipv4SrcAddress = getInetAddress(value);
                break;
            case "IPV6_SRC_MASK":
                ipv6SrcMask = getLongValue(value);
                break;
            case "SRC_MASK":
                srcMask = getLongValue(value);
                break;
            case "SRC_AS":
                getUInt64Value(value).ifPresent(builder::setSrcAs);
                break;
            case "L4_SRC_PORT":
                getUInt32Value(value).ifPresent(builder::setSrcPort);
                break;
            case "TCP_FLAGS":
                getUInt32Value(value).ifPresent(builder::setTcpFlags);
                break;
            case "TOS":
                getUInt32Value(value).ifPresent(builder::setTos);
                break;
            case "SRC_VLAN":
                srcVlan = getLongValue(value);
                break;
            case "DST_VLAN":
                dstVlan = getLongValue(value);
                break;
            case "FLOW_ACTIVE_TIMEOUT":
                flowActiveTimeout = getLongValue(value);
                break;
            case "FLOW_INACTIVE_TIMEOUT":
                flowInActiveTimeout = getLongValue(value);
                break;
            case "flowStartMilliseconds":
                flowStartMilliseconds = getLongValue(value);
                break;
            case "flowEndMilliseconds":
                flowEndMilliseconds = getLongValue(value);
                break;
        }

    }


}
