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

package org.opennms.netmgt.flows.persistence;


import static org.opennms.netmgt.flows.persistence.model.SamplingAlgorithm.FLOW_STATE_DEPENDENT_INTERMEDIATE_FLOW_SELECTION_PROCESS;
import static org.opennms.netmgt.flows.persistence.model.SamplingAlgorithm.HASH_BASED_FILTERING;
import static org.opennms.netmgt.flows.persistence.model.SamplingAlgorithm.PROPERTY_MATCH_FILTERING;
import static org.opennms.netmgt.flows.persistence.model.SamplingAlgorithm.RANDOM_N_OUT_OF_N_SAMPLING;
import static org.opennms.netmgt.flows.persistence.model.SamplingAlgorithm.SYSTEMATIC_COUNT_BASED_SAMPLING;
import static org.opennms.netmgt.flows.persistence.model.SamplingAlgorithm.SYSTEMATIC_TIME_BASED_SAMPLING;
import static org.opennms.netmgt.flows.persistence.model.SamplingAlgorithm.UNASSIGNED;
import static org.opennms.netmgt.flows.persistence.model.SamplingAlgorithm.UNIFORM_PROBABILISTIC_SAMPLING;
import static org.opennms.netmgt.flows.persistence.model.SamplingAlgorithm.UNRECOGNIZED;

import java.util.Optional;

import org.opennms.netmgt.flows.api.EnrichedFlow;
import org.opennms.netmgt.flows.persistence.model.Direction;
import org.opennms.netmgt.flows.persistence.model.FlowDocument;
import org.opennms.netmgt.flows.persistence.model.Locality;
import org.opennms.netmgt.flows.persistence.model.NetflowVersion;
import org.opennms.netmgt.flows.persistence.model.NodeInfo;
import org.opennms.netmgt.flows.persistence.model.SamplingAlgorithm;

import com.google.common.base.Strings;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;

public class FlowDocumentBuilder {


    public static FlowDocument buildFlowDocument(EnrichedFlow enrichedFlow) {
        FlowDocument.Builder builder = FlowDocument.newBuilder();
        builder.setTimestamp(enrichedFlow.getFlow().getTimestamp());
        builder.setDirection(fromDirection(enrichedFlow.getFlow().getDirection()));
        getUInt64Value(enrichedFlow.getFlow().getDstAs()).ifPresent(builder::setDstAs);
        getString(enrichedFlow.getFlow().getDstAddr()).ifPresent(builder::setDstAddress);
        enrichedFlow.getFlow().getDstAddrHostname().ifPresent(builder::setDstHostname);
        getUInt32Value(enrichedFlow.getFlow().getDstMaskLen()).ifPresent(builder::setDstMaskLen);
        getUInt32Value(enrichedFlow.getFlow().getDstPort()).ifPresent(builder::setDstPort);
        getString(enrichedFlow.getFlow().getSrcAddr()).ifPresent(builder::setSrcAddress);
        enrichedFlow.getFlow().getSrcAddrHostname().ifPresent(builder::setSrcHostname);
        getUInt64Value(enrichedFlow.getFlow().getSrcAs()).ifPresent(builder::setSrcAs);
        getUInt32Value(enrichedFlow.getFlow().getSrcMaskLen()).ifPresent(builder::setSrcMaskLen);
        getUInt32Value(enrichedFlow.getFlow().getSrcPort()).ifPresent(builder::setSrcPort);
        getUInt32Value(enrichedFlow.getFlow().getEngineId()).ifPresent(builder::setEngineId);
        getUInt32Value(enrichedFlow.getFlow().getEngineType()).ifPresent(builder::setEngineType);
        getUInt64Value(enrichedFlow.getFlow().getBytes()).ifPresent(builder::setNumBytes);
        getUInt64Value(enrichedFlow.getFlow().getPackets()).ifPresent(builder::setNumPackets);
        getUInt64Value(enrichedFlow.getFlow().getDeltaSwitched()).ifPresent(builder::setDeltaSwitched);
        getUInt64Value(enrichedFlow.getFlow().getFirstSwitched()).ifPresent(builder::setFirstSwitched);
        getUInt64Value(enrichedFlow.getFlow().getLastSwitched()).ifPresent(builder::setLastSwitched);
        getUInt32Value(enrichedFlow.getFlow().getInputSnmp()).ifPresent(builder::setInputSnmpIfindex);
        getUInt32Value(enrichedFlow.getFlow().getOutputSnmp()).ifPresent(builder::setOutputSnmpIfindex);
        getUInt32Value(enrichedFlow.getFlow().getIpProtocolVersion()).ifPresent(builder::setIpProtocolVersion);
        getUInt32Value(enrichedFlow.getFlow().getProtocol()).ifPresent(builder::setProtocol);
        getUInt32Value(enrichedFlow.getFlow().getTcpFlags()).ifPresent(builder::setTcpFlags);
        getUInt32Value(enrichedFlow.getFlow().getTos()).ifPresent(builder::setTos);
        getUInt32Value(enrichedFlow.getFlow().getDscp()).ifPresent(dscp -> builder.setDscp(dscp));
        getUInt32Value(enrichedFlow.getFlow().getEcn()).ifPresent(ecn -> builder.setEcn(ecn));
        getUInt32Value(enrichedFlow.getFlow().getFlowRecords()).ifPresent(builder::setNumFlowRecords);
        getUInt64Value(enrichedFlow.getFlow().getFlowSeqNum()).ifPresent(builder::setFlowSeqNum);
        String vlan = enrichedFlow.getFlow().getVlan() != null ? Integer.toUnsignedString(enrichedFlow.getFlow().getVlan()) : null;
        getString(vlan).ifPresent(builder::setVlan);
        getString(enrichedFlow.getFlow().getNextHop()).ifPresent(builder::setNextHopAddress);
        enrichedFlow.getFlow().getNextHopHostname().ifPresent(builder::setNextHopHostname);
        builder.setSamplingAlgorithm(fromSamplingAlgorithm(enrichedFlow.getFlow().getSamplingAlgorithm()));
        getDoubleValue(enrichedFlow.getFlow().getSamplingInterval()).ifPresent(builder::setSamplingInterval);
        NetflowVersion netflowVersion = fromNetflowVersion(enrichedFlow.getFlow().getNetflowVersion());
        Optional.ofNullable(netflowVersion).ifPresent(builder::setNetflowVersion);
        getString(enrichedFlow.getApplication()).ifPresent(builder::setApplication);
        getString(enrichedFlow.getHost()).ifPresent(builder::setHost);
        getString(enrichedFlow.getLocation()).ifPresent(builder::setLocation);
        builder.setDstLocality(fromLocality(enrichedFlow.getDstLocality()));
        builder.setSrcLocality(fromLocality(enrichedFlow.getSrcLocality()));
        builder.setFlowLocality(fromLocality(enrichedFlow.getFlowLocality()));
        buildNodeInfo(enrichedFlow.getSrcNodeInfo()).ifPresent(builder::setSrcNode);
        buildNodeInfo(enrichedFlow.getExporterNodeInfo()).ifPresent(builder::setExporterNode);
        buildNodeInfo(enrichedFlow.getDstNodeInfo()).ifPresent(builder::setDestNode);
        builder.setClockCorrection(enrichedFlow.getClockCorrection());

        return builder.build();
    }

    private static Optional<UInt64Value> getUInt64Value(Long value) {
        if (value != null) {
            return Optional.of(UInt64Value.newBuilder().setValue(value).build());
        }
        return Optional.empty();
    }

    private static Optional<UInt32Value> getUInt32Value(Integer value) {
        if (value != null) {
            return Optional.of(UInt32Value.newBuilder().setValue(value).build());
        }
        return Optional.empty();
    }

    private static Optional<DoubleValue> getDoubleValue(Double value) {
        if (value != null) {
            return Optional.of(DoubleValue.newBuilder().setValue(value).build());
        }
        return Optional.empty();
    }

    private static Optional<String> getString(String value) {
        if (!Strings.isNullOrEmpty(value)) {
            return Optional.of(value);
        }
        return Optional.empty();
    }


    private static Direction fromDirection(org.opennms.netmgt.flows.api.Flow.Direction direction) {
        switch (direction) {
            case EGRESS:
                return Direction.EGRESS;
            case INGRESS:
                return Direction.INGRESS;
        }
        return Direction.EGRESS;
    }

    private static Locality fromLocality(EnrichedFlow.Locality locality) {
        if (locality == null) {
            return Locality.PUBLIC;
        }
        switch (locality) {
            case PRIVATE:
                return Locality.PRIVATE;
            case PUBLIC:
                return Locality.PUBLIC;
        }
        return Locality.PUBLIC;
    }


    private static SamplingAlgorithm fromSamplingAlgorithm(org.opennms.netmgt.flows.api.Flow.SamplingAlgorithm samplingAlgorithm) {
        if (samplingAlgorithm == null) {
            return UNASSIGNED;
        }
        switch (samplingAlgorithm) {
            case SystematicCountBasedSampling:
                return SYSTEMATIC_COUNT_BASED_SAMPLING;
            case SystematicTimeBasedSampling:
                return SYSTEMATIC_TIME_BASED_SAMPLING;
            case RandomNoutOfNSampling:
                return RANDOM_N_OUT_OF_N_SAMPLING;
            case UniformProbabilisticSampling:
                return UNIFORM_PROBABILISTIC_SAMPLING;
            case PropertyMatchFiltering:
                return PROPERTY_MATCH_FILTERING;
            case HashBasedFiltering:
                return HASH_BASED_FILTERING;
            case FlowStateDependentIntermediateFlowSelectionProcess:
                return FLOW_STATE_DEPENDENT_INTERMEDIATE_FLOW_SELECTION_PROCESS;
            case Unassigned:
                return UNASSIGNED;
        }
        return UNRECOGNIZED;
    }

    private static NetflowVersion fromNetflowVersion(org.opennms.netmgt.flows.api.Flow.NetflowVersion netflowVersion) {
        if (netflowVersion == null) {
            return null;
        }
        switch (netflowVersion) {
            case V5:
                return NetflowVersion.V5;
            case V9:
                return NetflowVersion.V9;
            case IPFIX:
                return NetflowVersion.IPFIX;
            case SFLOW:
                return NetflowVersion.SFLOW;
        }
        return NetflowVersion.UNRECOGNIZED;
    }

    private static Optional<NodeInfo> buildNodeInfo(org.opennms.netmgt.flows.api.NodeInfo nodeInfo) {
        if (nodeInfo != null) {
            NodeInfo.Builder builder = NodeInfo.newBuilder();
            builder.setNodeId(nodeInfo.getNodeId());
            getString(nodeInfo.getForeignId()).ifPresent(builder::setForeginId);
            getString(nodeInfo.getForeignSource()).ifPresent(builder::setForeignSource);
            nodeInfo.getCategories().forEach(builder::addCategories);
            return Optional.of(builder.build());
        }
        return Optional.empty();
    }
}
