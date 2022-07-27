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
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.getInetAddress;
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.getLongValue;
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.getTime;
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.getUInt32Value;
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.getUInt64Value;
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.setDoubleValue;
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.setIntValue;
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.setLongValue;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Optional;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.RecordEnrichment;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.Direction;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.NetflowVersion;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.SamplingAlgorithm;

import com.google.common.primitives.UnsignedLong;
import com.google.protobuf.UInt32Value;

public class IpFixMessageBuilder implements MessageBuilder {

    private Long flowActiveTimeoutFallback;
    private Long flowInactiveTimeoutFallback;
    private Long flowSamplingIntervalFallback;

    public IpFixMessageBuilder() {
    }

    @Override
    public FlowMessage.Builder buildMessage(final Iterable<Value<?>> values, final RecordEnrichment enrichment) {
        final FlowMessage.Builder builder = FlowMessage.newBuilder();

        Long exportTime = null;
        Long octetDeltaCount = null;
        Long postOctetDeltaCount = null;
        Long layer2OctetDeltaCount = null;
        Long postLayer2OctetDeltaCount = null;
        Long transportOctetDeltaCount = null;
        InetAddress destinationIPv6Address = null;
        InetAddress destinationIPv4Address = null;
        Long destinationIPv6PrefixLength = null;
        Long destinationIPv4PrefixLength = null;
        Instant flowStartSeconds = null;
        Instant flowStartMilliseconds = null;
        Instant flowStartMicroseconds = null;
        Instant flowStartNanoseconds = null;
        Long flowStartDeltaMicroseconds = null;
        Long flowStartSysUpTime = null;
        Instant systemInitTimeMilliseconds = null;
        Instant flowEndSeconds = null;
        Instant flowEndMilliseconds = null;
        Instant flowEndMicroseconds = null;
        Instant flowEndNanoseconds = null;
        Long flowEndDeltaMicroseconds = null;
        Long flowEndSysUpTime = null;
        InetAddress ipNextHopIPv6Address = null;
        InetAddress ipNextHopIPv4Address = null;
        InetAddress bgpNextHopIPv6Address = null;
        InetAddress bgpNextHopIPv4Address = null;
        Long packetDeltaCount = null;
        Long postPacketDeltaCount = null;
        Long transportPacketDeltaCount = null;
        Long samplingAlgorithm = null;
        Long samplerMode = null;
        Long selectorAlgorithm = null;
        Long samplingInterval = this.flowSamplingIntervalFallback;
        Long samplerRandomInterval = null;
        Long samplingFlowInterval = null;
        Long samplingFlowSpacing = null;
        Long flowSamplingTimeInterval = null;
        Long flowSamplingTimeSpacing = null;
        Long samplingSize = null;
        Long samplingPopulation = null;
        Long samplingProbability = null;
        Long hashSelectedRangeMin = null;
        Long hashSelectedRangeMax = null;
        Long hashOutputRangeMin = null;
        Long hashOutputRangeMax = null;
        InetAddress sourceIPv6Address = null;
        InetAddress sourceIPv4Address = null;
        Long sourceIPv6PrefixLength = null;
        Long sourceIPv4PrefixLength = null;
        Long vlanId = null;
        Long postVlanId = null;
        Long dot1qVlanId = null;
        Long dot1qCustomerVlanId = null;
        Long postDot1qVlanId = null;
        Long postDot1qCustomerVlanId = null;
        Long flowActiveTimeout = this.flowActiveTimeoutFallback;
        Long flowInactiveTimeout = this.flowInactiveTimeoutFallback;
        UInt32Value ingressPhysicalInterface = null;
        UInt32Value egressPhysicalInterface = null;
        UInt32Value inputSnmp = null;
        UInt32Value outputSnmp = null;


        for (Value<?> value : values) {
            switch (value.getName()) {
                case "@exportTime":
                    exportTime = getLongValue(value);
                    break;
                case "octetDeltaCount":
                    octetDeltaCount = getLongValue(value);
                    break;
                case "postOctetDeltaCount":
                    postOctetDeltaCount = getLongValue(value);
                    break;
                case "layer2OctetDeltaCount":
                    layer2OctetDeltaCount = getLongValue(value);
                    break;
                case "postLayer2OctetDeltaCount":
                    postLayer2OctetDeltaCount = getLongValue(value);
                    break;
                case "transportOctetDeltaCount":
                    transportOctetDeltaCount = getLongValue(value);
                    break;
                case "flowDirection":
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
                        builder.setDirection(direction);
                    }
                    break;
                case "destinationIPv6Address":
                    destinationIPv6Address = getInetAddress(value);
                    break;
                case "destinationIPv4Address":
                    destinationIPv4Address = getInetAddress(value);
                    break;
                case "bgpDestinationAsNumber":
                    getUInt64Value(value).ifPresent(builder::setDstAs);
                    break;
                case "destinationIPv6PrefixLength":
                    destinationIPv6PrefixLength = getLongValue(value);
                    break;
                case "destinationIPv4PrefixLength":
                    destinationIPv4PrefixLength = getLongValue(value);
                    break;
                case "destinationTransportPort":
                    getUInt32Value(value).ifPresent(builder::setDstPort);
                    break;
                case "engineId":
                    getUInt32Value(value).ifPresent(builder::setEngineId);
                    break;
                case "engineType":
                    getUInt32Value(value).ifPresent(builder::setEngineType);
                    break;
                case "@recordCount":
                    getUInt32Value(value).ifPresent(builder::setNumFlowRecords);
                    break;
                case "@sequenceNumber":
                    getUInt64Value(value).ifPresent(builder::setFlowSeqNum);
                    break;
                case "ingressInterface":
                    inputSnmp = getUInt32Value(value).orElse(null);
                    break;
                case "ipVersion":
                    Long ipVersion = getLongValue(value);
                    if (ipVersion != null) {
                        builder.setIpProtocolVersion(setIntValue(ipVersion.intValue()));
                    }
                    break;
                case "egressInterface":
                    outputSnmp = getUInt32Value(value).orElse(null);
                    break;
                case "protocolIdentifier":
                    getUInt32Value(value).ifPresent(builder::setProtocol);
                    break;
                case "tcpControlBits":
                    getUInt32Value(value).ifPresent(builder::setTcpFlags);
                    break;
                case "ipClassOfService":
                    getUInt32Value(value).ifPresent(builder::setTos);
                    break;
                case "@observationDomainId":
                    Long observationDomainId = getLongValue(value);
                    if (observationDomainId != null) {
                        builder.setNodeIdentifier(String.valueOf(observationDomainId));
                    }
                    break;

                case "flowStartSeconds":
                    flowStartSeconds = getTime(value);
                    break;
                case "flowStartMilliseconds":
                    flowStartMilliseconds = getTime(value);
                    break;
                case "flowStartMicroseconds":
                    flowStartMicroseconds = getTime(value);
                    break;
                case "flowStartNanoseconds":
                    flowStartNanoseconds = getTime(value);
                    break;
                case "flowStartDeltaMicroseconds":
                    flowStartDeltaMicroseconds = getLongValue(value);
                    break;
                case "flowStartSysUpTime":
                    flowStartSysUpTime = getLongValue(value);
                    break;
                case "systemInitTimeMilliseconds":
                    systemInitTimeMilliseconds = getTime(value);
                    break;
                case "flowEndSeconds":
                    flowEndSeconds = getTime(value);
                    break;
                case "flowEndMilliseconds":
                    flowEndMilliseconds = getTime(value);
                    break;
                case "flowEndMicroseconds":
                    flowEndMicroseconds = getTime(value);
                    break;
                case "flowEndNanoseconds":
                    flowEndNanoseconds = getTime(value);
                case "flowEndDeltaMicroseconds":
                    flowEndDeltaMicroseconds = getLongValue(value);
                    break;
                case "flowEndSysUpTime":
                    flowEndSysUpTime = getLongValue(value);
                    break;
                case "ipNextHopIPv6Address":
                    ipNextHopIPv6Address = getInetAddress(value);
                    break;
                case "ipNextHopIPv4Address":
                    ipNextHopIPv4Address = getInetAddress(value);
                    break;
                case "bgpNextHopIPv6Address":
                    bgpNextHopIPv6Address = getInetAddress(value);
                    break;
                case "bgpNextHopIPv4Address":
                    bgpNextHopIPv4Address = getInetAddress(value);
                    break;
                case "packetDeltaCount":
                    packetDeltaCount = getLongValue(value);
                    break;
                case "postPacketDeltaCount":
                    postPacketDeltaCount = getLongValue(value);
                    break;
                case "transportPacketDeltaCount":
                    transportPacketDeltaCount = getLongValue(value);
                    break;
                case "samplingAlgorithm":
                    samplingAlgorithm = getLongValue(value);
                    break;
                case "samplerMode":
                    samplerMode = getLongValue(value);
                    break;
                case "selectorAlgorithm":
                    selectorAlgorithm = getLongValue(value);
                    break;
                case "samplingInterval":
                    samplingInterval = getLongValue(value);
                    break;
                case "samplerRandomInterval":
                    samplerRandomInterval = getLongValue(value);
                    break;
                case "samplingFlowInterval":
                    samplingFlowInterval = getLongValue(value);
                    break;
                case "samplingFlowSpacing":
                    samplingFlowSpacing = getLongValue(value);
                    break;
                case "flowSamplingTimeInterval":
                    flowSamplingTimeInterval = getLongValue(value);
                    break;
                case "flowSamplingTimeSpacing":
                    flowSamplingTimeSpacing = getLongValue(value);
                    break;
                case "samplingSize":
                    samplingSize = getLongValue(value);
                    break;
                case "samplingPopulation":
                    samplingPopulation = getLongValue(value);
                    break;
                case "samplingProbability":
                    samplingProbability = getLongValue(value);
                    break;
                case "hashSelectedRangeMin":
                    hashSelectedRangeMin = getLongValue(value);
                    break;
                case "hashSelectedRangeMax":
                    hashSelectedRangeMax = getLongValue(value);
                    break;
                case "hashOutputRangeMin":
                    hashOutputRangeMin = getLongValue(value);
                    break;
                case "hashOutputRangeMax":
                    hashOutputRangeMax = getLongValue(value);
                    break;
                case "sourceIPv6Address":
                    sourceIPv6Address = getInetAddress(value);
                    break;
                case "sourceIPv4Address":
                    sourceIPv4Address = getInetAddress(value);
                    break;
                case "sourceIPv6PrefixLength":
                    sourceIPv6PrefixLength = getLongValue(value);
                    break;
                case "sourceIPv4PrefixLength":
                    sourceIPv4PrefixLength = getLongValue(value);
                    break;
                case "sourceTransportPort":
                    getUInt32Value(value).ifPresent(builder::setSrcPort);
                    break;
                case "vlanId":
                    vlanId = getLongValue(value);
                    break;
                case "postVlanId":
                    postVlanId = getLongValue(value);
                    break;
                case "dot1qVlanId":
                    dot1qVlanId = getLongValue(value);
                    break;
                case "dot1qCustomerVlanId":
                    dot1qCustomerVlanId = getLongValue(value);
                    break;
                case "postDot1qVlanId":
                    postDot1qVlanId = getLongValue(value);
                    break;
                case "postDot1qCustomerVlanId":
                    postDot1qCustomerVlanId = getLongValue(value);
                    break;
                case "flowActiveTimeout":
                    flowActiveTimeout = getLongValue(value);
                    break;
                case "flowInactiveTimeout":
                    flowInactiveTimeout = getLongValue(value);
                    break;
                case "ingressPhysicalInterface":
                    ingressPhysicalInterface = getUInt32Value(value).orElse(null);
                    break;
                case "egressPhysicalInterface":
                    egressPhysicalInterface = getUInt32Value(value).orElse(null);
                    break;
            }
        }

        // Set input interface
        first(inputSnmp, ingressPhysicalInterface).ifPresent(ifIndex -> {
            builder.setInputSnmpIfindex(ifIndex);
        });

        // Set output interface
        first(outputSnmp, egressPhysicalInterface).ifPresent(ifIndex -> {
            builder.setOutputSnmpIfindex(ifIndex);
        });

        first(octetDeltaCount,
                postOctetDeltaCount,
                layer2OctetDeltaCount,
                postLayer2OctetDeltaCount,
                transportOctetDeltaCount)
                .ifPresent(bytes ->
                    builder.setNumBytes(setLongValue(bytes))
                );

        first(destinationIPv6Address,
                destinationIPv4Address).ifPresent(ipAddress -> {
            builder.setDstAddress(ipAddress.getHostAddress());
            enrichment.getHostnameFor(ipAddress).ifPresent(builder::setDstHostname);
        });

        first(destinationIPv6PrefixLength,
                destinationIPv4PrefixLength)
                .ifPresent(prefixLen -> builder.setDstMaskLen(setIntValue(prefixLen.intValue())));


        first(ipNextHopIPv6Address,
                ipNextHopIPv4Address,
                bgpNextHopIPv6Address,
                bgpNextHopIPv4Address).ifPresent(ipAddress -> {
            builder.setNextHopAddress(ipAddress.getHostAddress());
            enrichment.getHostnameFor(ipAddress).ifPresent(builder::setNextHopHostname);
        });

        first(sourceIPv6Address,
                sourceIPv4Address).ifPresent(ipAddress -> {
            builder.setSrcAddress(ipAddress.getHostAddress());
            enrichment.getHostnameFor(ipAddress).ifPresent(builder::setSrcHostname);
        });

        first(sourceIPv6PrefixLength,
                sourceIPv4PrefixLength)
                .ifPresent(prefixLen -> builder.setSrcMaskLen(setIntValue(prefixLen.intValue())));

        first(vlanId,
                postVlanId,
                dot1qVlanId,
                dot1qCustomerVlanId,
                postDot1qVlanId,
                postDot1qCustomerVlanId)
                .ifPresent(vlan -> builder.setVlan(setIntValue(vlan.intValue())));

        long timeStamp = exportTime  != null ? exportTime * 1000 : 0;
        builder.setTimestamp(timeStamp);

        // Set first switched
        Long flowStartDelta = flowStartDeltaMicroseconds != null ?
                flowStartDeltaMicroseconds + timeStamp : null;
        Long systemInitTime = systemInitTimeMilliseconds != null ?
                systemInitTimeMilliseconds.toEpochMilli() : null;
        Long flowStart = flowStartSysUpTime != null && systemInitTime != null ?
                flowStartSysUpTime + systemInitTime : null;

        Optional<Long> firstSwitchedInMilli = first(flowStartSeconds,
                flowStartMilliseconds,
                flowStartMicroseconds,
                flowStartNanoseconds).map(Instant::toEpochMilli);
        if (firstSwitchedInMilli.isPresent()) {
            builder.setFirstSwitched(setLongValue(firstSwitchedInMilli.get()));
        } else {
            first(flowStartDelta,
                    flowStart).ifPresent(firstSwitched -> {
                        builder.setFirstSwitched(setLongValue(firstSwitched));
                    }
            );
        }

        // Set lastSwitched
        Long flowEndDelta = flowEndDeltaMicroseconds != null ?
                flowEndDeltaMicroseconds + timeStamp : null;
        Long flowEnd = flowEndSysUpTime != null && systemInitTime != null ?
                flowEndSysUpTime + systemInitTime : null;

        Optional<Long> lastSwitchedInMilli = first(flowEndSeconds,
                flowEndMilliseconds,
                flowEndMicroseconds,
                flowEndNanoseconds).map(Instant::toEpochMilli);

        if(lastSwitchedInMilli.isPresent()) {
            builder.setLastSwitched(setLongValue(lastSwitchedInMilli.get()));
        } else {
            first(flowEndDelta,
                    flowEnd).ifPresent(lastSwitchedValue -> {
                builder.setLastSwitched(setLongValue(lastSwitchedValue));
            });
        }

        first(packetDeltaCount,
                postPacketDeltaCount,
                transportPacketDeltaCount).ifPresent(packets -> {
            builder.setNumPackets(setLongValue(packets));
        });

        SamplingAlgorithm sampling = SamplingAlgorithm.UNASSIGNED;
        final Integer deprecatedSamplingAlgorithm = first(samplingAlgorithm, samplerMode)
                .map(Long::intValue).orElse(null);
        if (deprecatedSamplingAlgorithm != null) {
            if (deprecatedSamplingAlgorithm == 1) {
                sampling = SamplingAlgorithm.SYSTEMATIC_COUNT_BASED_SAMPLING;
            }
            if (deprecatedSamplingAlgorithm == 2) {
                sampling = SamplingAlgorithm.RANDOM_N_OUT_OF_N_SAMPLING;
            }
        }

        if (selectorAlgorithm != null) {
            switch (selectorAlgorithm.intValue()) {
                case 0:
                    sampling = SamplingAlgorithm.UNASSIGNED;
                    break;
                case 1:
                    sampling = SamplingAlgorithm.SYSTEMATIC_COUNT_BASED_SAMPLING;
                    break;
                case 2:
                    sampling = SamplingAlgorithm.SYSTEMATIC_TIME_BASED_SAMPLING;
                    break;
                case 3:
                    sampling = SamplingAlgorithm.RANDOM_N_OUT_OF_N_SAMPLING;
                    break;
                case 4:
                    sampling = SamplingAlgorithm.UNIFORM_PROBABILISTIC_SAMPLING;
                    break;
                case 5:
                    sampling = SamplingAlgorithm.PROPERTY_MATCH_FILTERING;
                    break;
                case 6:
                case 7:
                case 8:
                    sampling = SamplingAlgorithm.HASH_BASED_FILTERING;
                    break;
                case 9:
                    sampling = SamplingAlgorithm.FLOW_STATE_DEPENDENT_INTERMEDIATE_FLOW_SELECTION_PROCESS;
                    break;
            }
        }
        builder.setSamplingAlgorithm(sampling);

        final Double deprecatedSamplingInterval = first(
                samplingInterval,
                samplerRandomInterval)
                .map(Long::doubleValue).orElse(null);

        if (deprecatedSamplingInterval != null) {
            builder.setSamplingInterval(setDoubleValue(deprecatedSamplingInterval));
        } else {
            if (selectorAlgorithm != null) {
                switch (selectorAlgorithm.intValue()) {
                    case 0:
                        break;
                    case 1: {
                        double interval = samplingFlowInterval != null ?
                                          samplingFlowInterval.doubleValue() : 1.0;
                        double spacing = samplingFlowSpacing != null ?
                                         samplingFlowSpacing.doubleValue() : 0.0;
                        double samplingIntervalValue = interval + spacing / interval;
                        builder.setSamplingInterval(setDoubleValue(samplingIntervalValue));
                        break;
                    }
                    case 2: {
                        double interval = flowSamplingTimeInterval != null ?
                                          flowSamplingTimeInterval.doubleValue() : 1.0;
                        double spacing = flowSamplingTimeSpacing != null ?
                                         flowSamplingTimeSpacing.doubleValue() : 0.0;
                        double samplingIntervalValue = interval + spacing / spacing;
                        builder.setSamplingInterval(setDoubleValue(samplingIntervalValue));
                        break;
                    }
                    case 3: {
                        double size = samplingSize != null ? samplingSize.doubleValue() : 1.0;
                        double population = samplingPopulation != null ? samplingPopulation.doubleValue() : 1.0;
                        double samplingIntervalValue = population / size;
                        builder.setSamplingInterval(setDoubleValue(samplingIntervalValue));
                        break;
                    }
                    case 4: {
                        Double probability = samplingProbability != null ? samplingProbability.doubleValue() : 1.0;
                        builder.setSamplingInterval(setDoubleValue(1.0 / probability));
                        break;
                    }
                    case 5:
                    case 6:
                    case 7: {
                        UnsignedLong selectedRangeMin = hashSelectedRangeMin != null ? UnsignedLong.fromLongBits(hashSelectedRangeMin) : UnsignedLong.ZERO;
                        UnsignedLong selectedRangeMax = hashSelectedRangeMax != null ? UnsignedLong.fromLongBits(hashSelectedRangeMax) : UnsignedLong.MAX_VALUE;
                        UnsignedLong outputRangeMin = hashOutputRangeMin != null ? UnsignedLong.fromLongBits(hashOutputRangeMin) : UnsignedLong.ZERO;
                        UnsignedLong outputRangeMax = hashOutputRangeMax != null ? UnsignedLong.fromLongBits(hashOutputRangeMax) : UnsignedLong.MAX_VALUE;
                        double samplingIntervalValue = (outputRangeMax.minus(outputRangeMin)).dividedBy(selectedRangeMax.minus(selectedRangeMin)).doubleValue();
                        builder.setSamplingInterval(setDoubleValue(samplingIntervalValue));
                        break;
                    }
                    case 8:
                    case 9:
                    default:
                        builder.setSamplingInterval(setDoubleValue(Double.NaN));
                }
            } else {
                builder.setSamplingInterval(setDoubleValue(1.0));
            }
        }

        // Build delta switched
        Timeout timeout = new Timeout(flowActiveTimeout, flowInactiveTimeout);
        timeout.setFirstSwitched(builder.hasFirstSwitched() ? builder.getFirstSwitched().getValue() : null);
        timeout.setLastSwitched(builder.hasLastSwitched() ? builder.getLastSwitched().getValue() : null);
        timeout.setNumBytes(builder.getNumBytes().getValue());
        timeout.setNumPackets(builder.getNumPackets().getValue());
        Long deltaSwitched = timeout.getDeltaSwitched();
        getUInt64Value(deltaSwitched).ifPresent(builder::setDeltaSwitched);

        builder.setNetflowVersion(NetflowVersion.IPFIX);
        return builder;
    }

    public Long getFlowActiveTimeoutFallback() {
        return this.flowActiveTimeoutFallback;
    }

    public void setFlowActiveTimeoutFallback(final Long flowActiveTimeoutFallback) {
        this.flowActiveTimeoutFallback = flowActiveTimeoutFallback;
    }

    public Long getFlowInactiveTimeoutFallback() {
        return this.flowInactiveTimeoutFallback;
    }

    public void setFlowInactiveTimeoutFallback(final Long flowInactiveTimeoutFallback) {
        this.flowInactiveTimeoutFallback = flowInactiveTimeoutFallback;
    }

    public Long getFlowSamplingIntervalFallback() {
        return this.flowSamplingIntervalFallback;
    }

    public void setFlowSamplingIntervalFallback(final Long flowSamplingIntervalFallback) {
        this.flowSamplingIntervalFallback = flowSamplingIntervalFallback;
    }
}
