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

import org.opennms.netmgt.telemetry.protocols.netflow.parser.IllegalFlowException;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.RecordEnrichment;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.Direction;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.NetflowVersion;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.SamplingAlgorithm;

import com.google.common.primitives.UnsignedLong;

public class IpFixMessageBuilder {

    private final FlowMessage.Builder builder;
    private final Iterable<Value<?>> values;
    private final RecordEnrichment enrichment;
    private Long exportTime;
    private Long octetDeltaCount;
    private Long postOctetDeltaCount;
    private Long layer2OctetDeltaCount;
    private Long postLayer2OctetDeltaCount;
    private Long transportOctetDeltaCount;
    private InetAddress destinationIPv6Address;
    private InetAddress destinationIPv4Address;
    private Long destinationIPv6PrefixLength;
    private Long destinationIPv4PrefixLength;
    private Instant flowStartSeconds;
    private Instant flowStartMilliseconds;
    private Instant flowStartMicroseconds;
    private Instant flowStartNanoseconds;
    private Long flowStartDeltaMicroseconds;
    private Long flowStartSysUpTime;
    private Instant systemInitTimeMilliseconds;
    private Instant flowEndSeconds;
    private Instant flowEndMilliseconds;
    private Instant flowEndMicroseconds;
    private Instant flowEndNanoseconds;
    private Long flowEndDeltaMicroseconds;
    private Long flowEndSysUpTime;
    private InetAddress ipNextHopIPv6Address;
    private InetAddress ipNextHopIPv4Address;
    private InetAddress bgpNextHopIPv6Address;
    private InetAddress bgpNextHopIPv4Address;
    private Long packetDeltaCount;
    private Long postPacketDeltaCount;
    private Long transportPacketDeltaCount;
    private Long samplingAlgorithm;
    private Long samplerMode;
    private Long selectorAlgorithm;
    private Long samplingInterval;
    private Long samplerRandomInterval;
    private Long samplingFlowInterval;
    private Long samplingFlowSpacing;
    private Long flowSamplingTimeInterval;
    private Long flowSamplingTimeSpacing;
    private Long samplingSize;
    private Long samplingPopulation;
    private Long samplingProbability;
    private Long hashSelectedRangeMin;
    private Long hashSelectedRangeMax;
    private Long hashOutputRangeMin;
    private Long hashOutputRangeMax;
    private InetAddress sourceIPv6Address;
    private InetAddress sourceIPv4Address;
    private Long sourceIPv6PrefixLength;
    private Long sourceIPv4PrefixLength;
    private Long vlanId;
    private Long postVlanId;
    private Long dot1qVlanId;
    private Long dot1qCustomerVlanId;
    private Long postDot1qVlanId;
    private Long postDot1qCustomerVlanId;
    private Long flowActiveTimeout;
    private Long flowInactiveTimeout;
    private Long numBytes;
    private Long numPackets;

    public IpFixMessageBuilder(Iterable<Value<?>> values, RecordEnrichment enrichment) {
        this.values = values;
        this.enrichment = enrichment;
        this.builder = FlowMessage.newBuilder();
    }

    public byte[] buildData() throws IllegalFlowException {

        values.forEach(this::addField);

        first(octetDeltaCount,
                postOctetDeltaCount,
                layer2OctetDeltaCount,
                postLayer2OctetDeltaCount,
                transportOctetDeltaCount)
                .ifPresent(bytes -> {
                    numBytes = bytes;
                    builder.setNumBytes(setLongValue(bytes));
                });

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
                .ifPresent(vlanId -> builder.setVlan(setIntValue(vlanId.intValue())));

        long timeStamp = this.exportTime  != null ? exportTime * 1000 : 0;
        builder.setTimestamp(timeStamp);

        // Set first switched
        Long flowStartDeltaMicroseconds = this.flowStartDeltaMicroseconds != null ?
                this.flowStartDeltaMicroseconds + timeStamp : null;
        Long systemInitTimeMilliseconds = this.systemInitTimeMilliseconds != null ?
                this.systemInitTimeMilliseconds.toEpochMilli() : null;
        Long flowStartSysUpTime = this.flowStartSysUpTime != null && systemInitTimeMilliseconds != null ?
                this.flowStartSysUpTime + systemInitTimeMilliseconds : null;

        Optional<Long> firstSwitchedInMilli = first(flowStartSeconds,
                flowStartMilliseconds,
                flowStartMicroseconds,
                flowStartNanoseconds).map(Instant::toEpochMilli);
        if (firstSwitchedInMilli.isPresent()) {
            builder.setFirstSwitched(setLongValue(firstSwitchedInMilli.get()));
        } else {
            first(flowStartDeltaMicroseconds,
                    flowStartSysUpTime).ifPresent(firstSwitched -> {
                        builder.setFirstSwitched(setLongValue(firstSwitched));
                    }
            );
        }

        // Set lastSwitched
        Long flowEndDeltaMicroseconds = this.flowEndDeltaMicroseconds != null ?
                this.flowEndDeltaMicroseconds + timeStamp : null;
        Long flowEndSysUpTime = this.flowEndSysUpTime != null && systemInitTimeMilliseconds != null ?
                this.flowEndSysUpTime + systemInitTimeMilliseconds : null;

        Optional<Long> lastSwitchedInMilli = first(flowEndSeconds,
                flowEndMilliseconds,
                flowEndMicroseconds,
                flowEndNanoseconds).map(Instant::toEpochMilli);

        if(lastSwitchedInMilli.isPresent()) {
            builder.setLastSwitched(setLongValue(lastSwitchedInMilli.get()));
        } else {
            first(flowEndDeltaMicroseconds,
                    flowEndSysUpTime).ifPresent(lastSwitchedValue -> {
                builder.setLastSwitched(setLongValue(lastSwitchedValue));
            });
        }

        first(packetDeltaCount,
                postPacketDeltaCount,
                transportPacketDeltaCount).ifPresent(packets -> {
            builder.setNumPackets(setLongValue(packets));
            this.numPackets = packets;
        });

        SamplingAlgorithm samplingAlgorithm = SamplingAlgorithm.UNASSIGNED;
        final Integer deprecatedSamplingAlgorithm = first(this.samplingAlgorithm,
                samplerMode)
                .map(Long::intValue).orElse(null);
        if (deprecatedSamplingAlgorithm != null) {
            if (deprecatedSamplingAlgorithm == 1) {
                samplingAlgorithm = SamplingAlgorithm.SYSTEMATIC_COUNT_BASED_SAMPLING;
            }
            if (deprecatedSamplingAlgorithm == 2) {
                samplingAlgorithm = SamplingAlgorithm.RANDOM_N_OUT_OF_N_SAMPLING;
            }
        }

        final Integer selectorAlgorithm = this.selectorAlgorithm != null ? this.selectorAlgorithm.intValue() : null;

        if (selectorAlgorithm != null) {
            switch (selectorAlgorithm) {
                case 0:
                    samplingAlgorithm = SamplingAlgorithm.UNASSIGNED;
                    break;
                case 1:
                    samplingAlgorithm = SamplingAlgorithm.SYSTEMATIC_COUNT_BASED_SAMPLING;
                    break;
                case 2:
                    samplingAlgorithm = SamplingAlgorithm.SYSTEMATIC_TIME_BASED_SAMPLING;
                    break;
                case 3:
                    samplingAlgorithm = SamplingAlgorithm.RANDOM_N_OUT_OF_N_SAMPLING;
                    break;
                case 4:
                    samplingAlgorithm = SamplingAlgorithm.UNIFORM_PROBABILISTIC_SAMPLING;
                    break;
                case 5:
                    samplingAlgorithm = SamplingAlgorithm.PROPERTY_MATCH_FILTERING;
                    break;
                case 6:
                case 7:
                case 8:
                    samplingAlgorithm = SamplingAlgorithm.HASH_BASED_FILTERING;
                    break;
                case 9:
                    samplingAlgorithm = SamplingAlgorithm.FLOW_STATE_DEPENDENT_INTERMEDIATE_FLOW_SELECTION_PROCESS;
                    break;
            }
        }
        builder.setSamplingAlgorithm(samplingAlgorithm);

        final Double deprecatedSamplingInterval = first(
                samplingInterval,
                samplerRandomInterval)
                .map(Long::doubleValue).orElse(null);

        if (deprecatedSamplingInterval != null) {
            builder.setSamplingInterval(setDoubleValue(deprecatedSamplingInterval));
        } else {
            if (selectorAlgorithm != null) {
                switch (selectorAlgorithm) {
                    case 0:
                        break;
                    case 1:
                        Double samplingInterval = this.samplingFlowInterval != null ?
                                samplingFlowInterval.doubleValue() : 1.0;
                        Double samplingSpacing = samplingFlowSpacing != null ?
                                samplingFlowSpacing.doubleValue() : 0.0;
                        Double samplingIntervalValue = samplingInterval + samplingSpacing / samplingInterval;
                        builder.setSamplingInterval(setDoubleValue(samplingIntervalValue));
                        break;
                    case 2:
                        Double flowSamplingTimeInterval = this.flowSamplingTimeInterval != null ?
                                this.flowSamplingTimeInterval.doubleValue() : 1.0;
                        Double flowSamplingTimeSpacing = this.flowSamplingTimeSpacing != null ?
                                this.flowSamplingTimeSpacing.doubleValue() : 0.0;
                        samplingIntervalValue = flowSamplingTimeInterval + flowSamplingTimeSpacing / flowSamplingTimeSpacing;
                        builder.setSamplingInterval(setDoubleValue(samplingIntervalValue));
                        break;
                    case 3:
                        Double samplingSize = this.samplingSize != null ?
                                this.samplingSize.doubleValue() : 1.0;
                        Double samplingPopulation = this.samplingPopulation != null ?
                                this.samplingPopulation.doubleValue() : 1.0;
                        samplingIntervalValue = samplingPopulation / samplingSize;
                        builder.setSamplingInterval(setDoubleValue(samplingIntervalValue));
                        break;
                    case 4:
                        Double samplingProbability = this.samplingProbability != null ?
                                this.samplingProbability.doubleValue() : 1.0;
                        builder.setSamplingInterval(setDoubleValue(1.0 / samplingProbability));
                        break;
                    case 5:
                    case 6:
                    case 7:
                        UnsignedLong hashSelectedRangeMin = this.hashSelectedRangeMin != null ?
                                UnsignedLong.fromLongBits(this.hashSelectedRangeMin) : UnsignedLong.ZERO;
                        UnsignedLong hashSelectedRangeMax = this.hashSelectedRangeMax != null ?
                                UnsignedLong.fromLongBits(this.hashSelectedRangeMax) : UnsignedLong.MAX_VALUE;
                        UnsignedLong hashOutputRangeMin = this.hashOutputRangeMin != null ?
                                UnsignedLong.fromLongBits(this.hashOutputRangeMin) : UnsignedLong.ZERO;
                        UnsignedLong hashOutputRangeMax = this.hashOutputRangeMax != null ?
                                UnsignedLong.fromLongBits(this.hashOutputRangeMax) : UnsignedLong.MAX_VALUE;
                        samplingIntervalValue = (hashOutputRangeMax.minus(hashOutputRangeMin)).dividedBy(hashSelectedRangeMax.minus(hashSelectedRangeMin)).doubleValue();
                        builder.setSamplingInterval(setDoubleValue(samplingIntervalValue));
                        break;
                    case 8:
                    case 9:
                    default:
                        builder.setSamplingInterval(setDoubleValue(Double.NaN));
                }
            } else {
                builder.setSamplingInterval(setDoubleValue(1.0));
            }
        }

        if (builder.getFirstSwitched().getValue() > builder.getLastSwitched().getValue()) {
            throw new IllegalFlowException(
                    String.format("lastSwitched must be greater than firstSwitched: srcAddress=%s, dstAddress=%s, firstSwitched=%d, lastSwitched=%d, duration=%d",
                            builder.getSrcAddress(),
                            builder.getDstAddress(),
                            builder.getFirstSwitched().getValue(),
                            builder.getLastSwitched().getValue(),
                            builder.getLastSwitched().getValue() - builder.getFirstSwitched().getValue()));
        }

        // Build delta switched
        Long firstSwitched = builder.hasFirstSwitched() ? builder.getFirstSwitched().getValue() : null;
        Long lastSwitched = builder.hasLastSwitched() ? builder.getLastSwitched().getValue() : null;

        Timeout timeout = new Timeout(flowActiveTimeout, flowInactiveTimeout);
        timeout.setFirstSwitched(firstSwitched);
        timeout.setLastSwitched(lastSwitched);
        timeout.setNumBytes(this.numBytes);
        timeout.setNumPackets(this.numPackets);
        Long deltaSwitched = timeout.getDeltaSwitched();
        getUInt64Value(deltaSwitched).ifPresent(builder::setDeltaSwitched);

        builder.setNetflowVersion(NetflowVersion.IPFIX);
        return builder.build().toByteArray();
    }


    private void addField(Value<?> value) {
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
                    this.builder.setDirection(direction);
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
                getUInt32Value(value).ifPresent(builder::setInputSnmpIfindex);
                break;
            case "ipVersion":
                Long ipVersion = getLongValue(value);
                if (ipVersion != null) {
                    builder.setIpProtocolVersion(setIntValue(ipVersion.intValue()));
                }
                break;
            case "egressInterface":
                getUInt32Value(value).ifPresent( builder::setOutputSnmpIfindex);
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
        }
    }
}
