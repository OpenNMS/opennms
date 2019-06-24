/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.netflow.adapter.ipfix;

import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.first;
import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.getDouble;
import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.getInt64;
import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.getString;
import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.getTime;

import java.time.Instant;
import java.util.Objects;

import org.bson.BsonDocument;
import org.opennms.netmgt.flows.api.Flow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.primitives.UnsignedLong;

class IpfixFlow implements Flow {
    private static final Logger LOG = LoggerFactory.getLogger(IpfixFlow.class);

    private final BsonDocument document;

    public IpfixFlow(final BsonDocument document) {
        this.document = Objects.requireNonNull(document);
    }

    @Override
    public long getTimestamp() {
        return getInt64(this.document, "@exportTime").get() * 1000;
    }

    @Override
    public Long getBytes() {
        // TODO: What about the totals?
        return first(getInt64(this.document, "octetDeltaCount"),
                getInt64(this.document, "postOctetDeltaCount"),
                getInt64(this.document, "layer2OctetDeltaCount"),
                getInt64(this.document, "postLayer2OctetDeltaCount"),
                getInt64(this.document, "transportOctetDeltaCount"))
                .orElse(null);
    }

    @Override
    public Direction getDirection() {
        return getInt64(this.document, "flowDirection")
                .map(v -> v == 0x00 ? Direction.INGRESS
                        : v == 0x01 ? Direction.EGRESS
                        : null)
                .orElse(null);
    }

    @Override
    public String getDstAddr() {
        return first(getString(this.document, "destinationIPv6Address"),
                getString(this.document, "destinationIPv4Address"))
                .orElse(null);
    }

    @Override
    public Integer getDstAs() {
        return getInt64(this.document, "bgpDestinationAsNumber")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getDstMaskLen() {
        return first(getInt64(this.document, "destinationIPv6PrefixLength"),
                getInt64(this.document, "destinationIPv4PrefixLength"))
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getDstPort() {
        return getInt64(this.document, "destinationTransportPort")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getEngineId() {
        return getInt64(this.document, "engineId")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getEngineType() {
        return getInt64(this.document, "engineType")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Long getFirstSwitched() {
        // TODO: What about flowDuration* ?
        return first(
                first(getTime(this.document, "flowStartSeconds"),
                        getTime(this.document, "flowStartMilliseconds"),
                        getTime(this.document, "flowStartMicroseconds"),
                        getTime(this.document, "flowStartNanoseconds")
                ).map(Instant::toEpochMilli),
                getInt64(this.document, "flowStartDeltaMicroseconds").map(t -> this.getTimestamp() + t),
                getInt64(this.document, "flowStartSysUpTime").flatMap(t ->
                        getTime(this.document, "systemInitTimeMilliseconds").map(ts -> ts.toEpochMilli() + t)
                )
        ).orElse(null);
    }

    @Override
    public int getFlowRecords() {
        return getInt64(this.document, "@recordCount")
                .map(Long::intValue)
                .orElse(0);
    }

    @Override
    public long getFlowSeqNum() {
        return getInt64(this.document, "@sequenceNumber")
                .orElse(0L);
    }

    @Override
    public Integer getInputSnmp() {
        return getInt64(this.document, "ingressInterface")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getIpProtocolVersion() {
        return getInt64(this.document, "ipVersion")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Long getLastSwitched() {
        // TODO: What about flowDuration* ?
        return first(
                first(getTime(this.document, "flowEndSeconds"),
                        getTime(this.document, "flowEndMilliseconds"),
                        getTime(this.document, "flowEndMicroseconds"),
                        getTime(this.document, "flowEndNanoseconds")
                ).map(Instant::toEpochMilli),
                getInt64(this.document, "flowEndDeltaMicroseconds").map(t -> this.getTimestamp() + t),
                getInt64(this.document, "flowEndSysUpTime").flatMap(t ->
                        getTime(this.document, "systemInitTimeMilliseconds").map(ts -> ts.toEpochMilli() + t)
                )
        ).orElse(null);
    }

    @Override
    public String getNextHop() {
        return first(getString(this.document, "ipNextHopIPv6Address"),
                getString(this.document, "ipNextHopIPv4Address"),
                getString(this.document, "bgpNextHopIPv6Address"),
                getString(this.document, "bgpNextHopIPv4Address"))
                .orElse(null);
    }

    @Override
    public Integer getOutputSnmp() {
        return getInt64(this.document, "egressInterface")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Long getPackets() {
        // TODO: What about the totals?
        return first(getInt64(this.document, "packetDeltaCount"),
                getInt64(this.document, "postPacketDeltaCount"),
                getInt64(this.document, "transportPacketDeltaCount"))
                .orElse(null);
    }

    @Override
    public Integer getProtocol() {
        return getInt64(this.document, "protocolIdentifier")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Flow.SamplingAlgorithm getSamplingAlgorithm() {
        final Integer deprecatedSamplingAlgorithm = first(
                getInt64(this.document, "samplingAlgorithm"),
                getInt64(this.document, "samplerMode"))
                .map(Long::intValue).orElse(null);

        if (deprecatedSamplingAlgorithm != null) {
            if (deprecatedSamplingAlgorithm == 1) {
                return Flow.SamplingAlgorithm.SystematicCountBasedSampling;
            }
            if (deprecatedSamplingAlgorithm == 2) {
                return Flow.SamplingAlgorithm.RandomNoutOfNSampling;
            }
        }

        final Integer selectorAlgorithm = getInt64(this.document, "selectorAlgorithm").map(Long::intValue).orElse(null);

        if (selectorAlgorithm != null) {
            switch (selectorAlgorithm) {
                case 0:
                    return SamplingAlgorithm.Unassigned;
                case 1:
                    return SamplingAlgorithm.SystematicCountBasedSampling;
                case 2:
                    return SamplingAlgorithm.SystematicTimeBasedSampling;
                case 3:
                    return SamplingAlgorithm.RandomNoutOfNSampling;
                case 4:
                    return SamplingAlgorithm.UniformProbabilisticSampling;
                case 5:
                    return SamplingAlgorithm.PropertyMatchFiltering;
                case 6:
                case 7:
                case 8:
                    return SamplingAlgorithm.HashBasedFiltering;
                case 9:
                    return SamplingAlgorithm.FlowStateDependentIntermediateFlowSelectionProcess;
                default:
                    LOG.warn("Unknown selector algorithm: {}", selectorAlgorithm);
            }
        }

        return Flow.SamplingAlgorithm.Unassigned;
    }

    @Override
    public Double getSamplingInterval() {
        final Double deprecatedSamplingInterval = first(
                getInt64(this.document, "samplingInterval"),
                getInt64(this.document, "samplerRandomInterval"))
                .map(Long::doubleValue).orElse(null);

        if (deprecatedSamplingInterval != null) {
            return deprecatedSamplingInterval;
        }

        final Integer selectorAlgorithm = getInt64(this.document, "selectorAlgorithm").map(Long::intValue).orElse(null);

        if (selectorAlgorithm != null) {
            switch (selectorAlgorithm) {
                case 0: {
                    return null;
                }
                case 1: {
                    final Double samplingInterval =
                            getInt64(this.document, "samplingFlowInterval")
                                    .map(Long::doubleValue).orElse(1.0);
                    final Double samplingSpacing =
                            getInt64(this.document, "samplingFlowSpacing")
                                    .map(Long::doubleValue).orElse(0.0);

                    return (samplingInterval + samplingSpacing) / samplingInterval;
                }
                case 2: {
                    final Double flowSamplingTimeInterval =
                            getInt64(this.document, "flowSamplingTimeInterval")
                                    .map(Long::doubleValue).orElse(1.0);
                    final Double flowSamplingTimeSpacing =
                            getInt64(this.document, "flowSamplingTimeSpacing")
                                    .map(Long::doubleValue).orElse(0.0);

                    return (flowSamplingTimeInterval + flowSamplingTimeSpacing) / flowSamplingTimeInterval;
                }
                case 3: {
                    final Double samplingSize =
                            getInt64(this.document, "samplingSize")
                                    .map(Long::doubleValue).orElse(1.0); // n
                    final Double samplingPopulation =
                            getInt64(this.document, "samplingPopulation")
                                    .map(Long::doubleValue).orElse(1.0); // N

                    return samplingPopulation / samplingSize;
                }
                case 4: {
                    final Double samplingProbability =
                            getDouble(this.document, "samplingProbability")
                                    .orElse(1.0);

                    return 1.0 / samplingProbability;
                }
                case 5:
                case 6:
                case 7: {
                    final UnsignedLong hashSelectedRangeMin =
                            getInt64(this.document, "hashSelectedRangeMin")
                                    .map(UnsignedLong::fromLongBits).orElse(UnsignedLong.ZERO);
                    final UnsignedLong hashSelectedRangeMax =
                            getInt64(this.document, "hashSelectedRangeMax")
                                    .map(UnsignedLong::fromLongBits).orElse(UnsignedLong.MAX_VALUE);
                    final UnsignedLong hashOutputRangeMin =
                            getInt64(this.document, "hashOutputRangeMin")
                                    .map(UnsignedLong::fromLongBits).orElse(UnsignedLong.ZERO);
                    final UnsignedLong hashOutputRangeMax =
                            getInt64(this.document, "hashOutputRangeMax")
                                    .map(UnsignedLong::fromLongBits).orElse(UnsignedLong.MAX_VALUE);

                    return (hashOutputRangeMax.minus(hashOutputRangeMin)).dividedBy(hashSelectedRangeMax.minus(hashSelectedRangeMin)).doubleValue();
                }
                case 8:
                case 9:
                default: {
                    LOG.warn("Unsupported sampling algorithm: {}", selectorAlgorithm);
                    return Double.NaN;
                }
            }
        }
        return 1.0;
    }

    @Override
    public String getSrcAddr() {
        return first(getString(this.document, "sourceIPv6Address"),
                getString(this.document, "sourceIPv4Address"))
                .orElse(null);
    }

    @Override
    public Integer getSrcAs() {
        return getInt64(this.document, "bgpSourceAsNumber")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getSrcMaskLen() {
        return first(getInt64(this.document, "sourceIPv6PrefixLength"),
                getInt64(this.document, "sourceIPv4PrefixLength"))
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getSrcPort() {
        return getInt64(this.document, "sourceTransportPort")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getTcpFlags() {
        return getInt64(this.document, "tcpControlBits")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public Integer getTos() {
        return getInt64(this.document, "ipClassOfService")
                .map(Long::intValue)
                .orElse(null);
    }

    @Override
    public NetflowVersion getNetflowVersion() {
        return NetflowVersion.IPFIX;
    }

    @Override
    public Integer getVlan() {
        return first(getInt64(this.document, "vlanId"),
                getInt64(this.document, "postVlanId"),
                getInt64(this.document, "dot1qVlanId"),
                getInt64(this.document, "dot1qCustomerVlanId"),
                getInt64(this.document, "postDot1qVlanId"),
                getInt64(this.document, "postDot1qCustomerVlanId"))
                .map(Long::intValue)
                .orElse(null);
    }
}
