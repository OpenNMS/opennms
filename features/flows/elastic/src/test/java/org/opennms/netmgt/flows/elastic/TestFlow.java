/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

import java.util.Optional;

import org.opennms.netmgt.flows.api.Flow;

public class TestFlow implements Flow {

    final FlowDocument flowDocument;
    String nodeIdentifier;

    public TestFlow(final FlowDocument flowDocument) {
        this.flowDocument = flowDocument;
    }

    @Override
    public long getTimestamp() {
        return flowDocument.getTimestamp();
    }

    @Override
    public Long getBytes() {
        return flowDocument.getBytes();
    }

    @Override
    public Direction getDirection() {
        if (flowDocument.getDirection() == null)
            return Direction.INGRESS;

        switch (flowDocument.getDirection()) {
            case INGRESS:
                return Direction.INGRESS;
            case EGRESS:
                return Direction.EGRESS;
            default:
                throw new IllegalArgumentException("Unknown direction: " + flowDocument.getDirection().name());
        }
    }

    @Override
    public String getDstAddr() {
        return flowDocument.getDstAddr();
    }

    @Override
    public Optional<String> getDstAddrHostname() {
        return Optional.ofNullable(flowDocument.getDstAddrHostname());
    }

    @Override
    public Long getDstAs() {
        return flowDocument.getDstAs();
    }

    @Override
    public Integer getDstMaskLen() {
        return flowDocument.getDstMaskLen();
    }

    @Override
    public Integer getDstPort() {
        return flowDocument.getDstPort();
    }

    @Override
    public Integer getEngineId() {
        return flowDocument.getEngineId();
    }

    @Override
    public Integer getEngineType() {
        return flowDocument.getEngineType();
    }

    @Override
    public Long getDeltaSwitched() {
        return flowDocument.getDeltaSwitched();
    }

    @Override
    public Long getFirstSwitched() {
        return flowDocument.getFirstSwitched();
    }

    @Override
    public int getFlowRecords() {
        return flowDocument.getFlowRecords();
    }

    @Override
    public long getFlowSeqNum() {
        return flowDocument.getFlowSeqNum();
    }

    @Override
    public Integer getInputSnmp() {
        return flowDocument.getInputSnmp();
    }

    @Override
    public Integer getIpProtocolVersion() {
        return flowDocument.getIpProtocolVersion();
    }

    @Override
    public Long getLastSwitched() {
        return flowDocument.getLastSwitched();
    }

    @Override
    public String getNextHop() {
        return flowDocument.getNextHop();
    }

    @Override
    public Optional<String> getNextHopHostname() {
        return Optional.ofNullable(flowDocument.getNextHopHostname());
    }

    @Override
    public Integer getOutputSnmp() {
        return flowDocument.getOutputSnmp();
    }

    @Override
    public Long getPackets() {
        return flowDocument.getPackets();
    }

    @Override
    public Integer getProtocol() {
        return flowDocument.getProtocol();
    }

    @Override
    public SamplingAlgorithm getSamplingAlgorithm() {
        if (flowDocument.getSamplingAlgorithm() == null) {
            return null;
        }

        switch (flowDocument.getSamplingAlgorithm()) {
            case Unassigned:
                return SamplingAlgorithm.Unassigned;
            case SystematicCountBasedSampling:
                return SamplingAlgorithm.SystematicCountBasedSampling;
            case SystematicTimeBasedSampling:
                return SamplingAlgorithm.SystematicTimeBasedSampling;
            case RandomNoutOfNSampling:
                return SamplingAlgorithm.RandomNoutOfNSampling;
            case UniformProbabilisticSampling:
                return SamplingAlgorithm.UniformProbabilisticSampling;
            case PropertyMatchFiltering:
                return SamplingAlgorithm.PropertyMatchFiltering;
            case HashBasedFiltering:
                return SamplingAlgorithm.HashBasedFiltering;
            case FlowStateDependentIntermediateFlowSelectionProcess:
                return SamplingAlgorithm.FlowStateDependentIntermediateFlowSelectionProcess;
            default:
                throw new IllegalArgumentException("Unknown sampling algorithm: " + flowDocument.getSamplingAlgorithm().name());
        }
    }

    @Override
    public Double getSamplingInterval() {
        return flowDocument.getSamplingInterval();
    }

    @Override
    public String getSrcAddr() {
        return flowDocument.getSrcAddr();
    }

    @Override
    public Optional<String> getSrcAddrHostname() {
        return Optional.ofNullable(flowDocument.getSrcAddrHostname());
    }

    @Override
    public Long getSrcAs() {
        return flowDocument.getSrcAs();
    }

    @Override
    public Integer getSrcMaskLen() {
        return flowDocument.getSrcMaskLen();
    }

    @Override
    public Integer getSrcPort() {
        return flowDocument.getSrcPort();
    }

    @Override
    public Integer getTcpFlags() {
        return flowDocument.getTcpFlags();
    }

    @Override
    public Integer getTos() {
        return flowDocument.getTos();
    }

    @Override
    public NetflowVersion getNetflowVersion() {
        if (flowDocument.getNetflowVersion() == null) {
            return null;
        }

        switch (flowDocument.getNetflowVersion()) {
            case V5:
                return Flow.NetflowVersion.V5;
            case V9:
                return Flow.NetflowVersion.V9;
            case IPFIX:
                return Flow.NetflowVersion.IPFIX;
            case SFLOW:
                return Flow.NetflowVersion.SFLOW;
            default:
                throw new IllegalArgumentException("Unknown protocol version: " + flowDocument.getNetflowVersion().name());
        }
    }

    @Override
    public Integer getVlan() {
        return flowDocument.getVlan() == null ? null : Integer.valueOf(flowDocument.getVlan());
    }

    @Override
    public String getNodeIdentifier() {
        return this.nodeIdentifier;
    }

    public void setNodeIdentifier(String nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
    }
}
