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

package org.opennms.netmgt.telemetry.protocols.netflow.adapter.common;

import java.util.Optional;

import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;

import com.google.common.base.Strings;

public class NetflowMessage implements Flow {

    private final FlowMessage flowMessageProto;


    public NetflowMessage(FlowMessage flowMessageProto) {
        this.flowMessageProto = flowMessageProto;
    }

    @Override
    public long getTimestamp() {
        return flowMessageProto.getTimestamp();
    }

    @Override
    public Long getBytes() {
        return flowMessageProto.hasNumBytes() ? flowMessageProto.getNumBytes().getValue() : null;
    }

    @Override
    public Direction getDirection() {
        switch (flowMessageProto.getDirection()) {
            case INGRESS:
                return Direction.INGRESS;
            case EGRESS:
                return Direction.EGRESS;
        }
        return Direction.INGRESS;

    }

    @Override
    public String getDstAddr() {

        if (!Strings.isNullOrEmpty(flowMessageProto.getDstAddress())) {
            return flowMessageProto.getDstAddress();
        }
        return null;
    }

    @Override
    public Optional<String> getDstAddrHostname() {
        if (!Strings.isNullOrEmpty(flowMessageProto.getDstHostname())) {
            return Optional.of(flowMessageProto.getDstHostname());
        }
        return Optional.empty();
    }

    @Override
    public Long getDstAs() {
        return flowMessageProto.hasDstAs() ? flowMessageProto.getDstAs().getValue() : null;
    }

    @Override
    public Integer getDstMaskLen() {
        return flowMessageProto.hasDstMaskLen() ? flowMessageProto.getDstMaskLen().getValue() : null;
    }

    @Override
    public Integer getDstPort() {
        return flowMessageProto.hasDstPort() ? flowMessageProto.getDstPort().getValue() : null;
    }

    @Override
    public Integer getEngineId() {
        return flowMessageProto.hasEngineId() ? flowMessageProto.getEngineId().getValue() : null;
    }

    @Override
    public Integer getEngineType() {
        return flowMessageProto.hasEngineType() ? flowMessageProto.getEngineType().getValue() : null;
    }

    @Override
    public Long getDeltaSwitched() {
        return flowMessageProto.hasDeltaSwitched() ? new Long(flowMessageProto.getDeltaSwitched().getValue()) : getFirstSwitched();
    }

    @Override
    public Long getFirstSwitched() {
        return flowMessageProto.hasFirstSwitched() ? flowMessageProto.getFirstSwitched().getValue() : null;
    }

    @Override
    public int getFlowRecords() {
        return flowMessageProto.hasNumFlowRecords() ? flowMessageProto.getNumFlowRecords().getValue() : 0;
    }

    @Override
    public long getFlowSeqNum() {
        return flowMessageProto.hasFlowSeqNum() ? flowMessageProto.getFlowSeqNum().getValue() : 0L;
    }

    @Override
    public Integer getInputSnmp() {
        return flowMessageProto.hasInputSnmpIfindex() ? flowMessageProto.getInputSnmpIfindex().getValue() : null;
    }

    @Override
    public Integer getIpProtocolVersion() {
        return flowMessageProto.hasIpProtocolVersion() ? flowMessageProto.getIpProtocolVersion().getValue() : null;
    }

    @Override
    public Long getLastSwitched() {
        return flowMessageProto.hasLastSwitched() ? flowMessageProto.getLastSwitched().getValue() : null;
    }

    @Override
    public String getNextHop() {
        if (!Strings.isNullOrEmpty(flowMessageProto.getNextHopAddress())) {
            return flowMessageProto.getNextHopAddress();
        }
        return null;
    }

    @Override
    public Optional<String> getNextHopHostname() {
        if (!Strings.isNullOrEmpty(flowMessageProto.getNextHopHostname())) {
            return Optional.of(flowMessageProto.getNextHopHostname());
        }
        return Optional.empty();
    }

    @Override
    public Integer getOutputSnmp() {
        return flowMessageProto.hasOutputSnmpIfindex() ? flowMessageProto.getOutputSnmpIfindex().getValue() : null;
    }

    @Override
    public Long getPackets() {
        return flowMessageProto.hasNumPackets() ? flowMessageProto.getNumPackets().getValue() : null;
    }

    @Override
    public Integer getProtocol() {
        return flowMessageProto.hasProtocol() ? flowMessageProto.getProtocol().getValue() : null;
    }

    @Override
    public SamplingAlgorithm getSamplingAlgorithm() {

        switch (flowMessageProto.getSamplingAlgorithm()) {
            case SYSTEMATIC_COUNT_BASED_SAMPLING:
                return SamplingAlgorithm.SystematicCountBasedSampling;
            case SYSTEMATIC_TIME_BASED_SAMPLING:
                return SamplingAlgorithm.SystematicTimeBasedSampling;
            case RANDOM_N_OUT_OF_N_SAMPLING:
                return SamplingAlgorithm.RandomNoutOfNSampling;
            case UNIFORM_PROBABILISTIC_SAMPLING:
                return SamplingAlgorithm.UniformProbabilisticSampling;
            case PROPERTY_MATCH_FILTERING:
                return SamplingAlgorithm.PropertyMatchFiltering;
            case HASH_BASED_FILTERING:
                return SamplingAlgorithm.HashBasedFiltering;
            case FLOW_STATE_DEPENDENT_INTERMEDIATE_FLOW_SELECTION_PROCESS:
                return SamplingAlgorithm.FlowStateDependentIntermediateFlowSelectionProcess;
        }
        return SamplingAlgorithm.Unassigned;
    }

    @Override
    public Double getSamplingInterval() {
        return flowMessageProto.hasSamplingInterval() ? flowMessageProto.getSamplingInterval().getValue() : null;
    }

    @Override
    public String getSrcAddr() {
        if (!Strings.isNullOrEmpty(flowMessageProto.getSrcAddress())) {
            return flowMessageProto.getSrcAddress();
        }
        return null;
    }

    @Override
    public Optional<String> getSrcAddrHostname() {
        if (!Strings.isNullOrEmpty(flowMessageProto.getSrcHostname())) {
            return Optional.of(flowMessageProto.getSrcHostname());
        }
        return Optional.empty();
    }

    @Override
    public Long getSrcAs() {
        return flowMessageProto.hasSrcAs() ? flowMessageProto.getSrcAs().getValue() : null;
    }

    @Override
    public Integer getSrcMaskLen() {
        return flowMessageProto.hasSrcMaskLen() ? flowMessageProto.getSrcMaskLen().getValue() : null;
    }

    @Override
    public Integer getSrcPort() {
        return flowMessageProto.hasSrcPort() ? flowMessageProto.getSrcPort().getValue() : null;
    }

    @Override
    public Integer getTcpFlags() {
        return flowMessageProto.hasTcpFlags() ? flowMessageProto.getTcpFlags().getValue() : null;
    }

    @Override
    public Integer getTos() {
        return flowMessageProto.hasTos() ? flowMessageProto.getTos().getValue() : null;
    }

    @Override
    public NetflowVersion getNetflowVersion() {
        switch (flowMessageProto.getNetflowVersion()) {
            case V5:
                return NetflowVersion.V5;
            case V9:
                return NetflowVersion.V9;
            case IPFIX:
                return NetflowVersion.IPFIX;
            default:
                return NetflowVersion.V5;
        }
    }

    @Override
    public Integer getVlan() {
        return flowMessageProto.hasVlan() ? flowMessageProto.getVlan().getValue() : null;
    }

    @Override
    public String getNodeIdentifier() {
        if (!Strings.isNullOrEmpty(flowMessageProto.getNodeIdentifier())) {
            return flowMessageProto.getNodeIdentifier();
        }
        return null;
    }
}
