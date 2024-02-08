/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
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

import java.time.Instant;
import java.util.Optional;

import org.opennms.integration.api.v1.flows.Flow;
import org.opennms.netmgt.flows.persistence.model.Direction;
import org.opennms.netmgt.flows.persistence.model.FlowDocument;
import org.opennms.netmgt.flows.persistence.model.Locality;
import org.opennms.netmgt.flows.persistence.model.NetflowVersion;
import org.opennms.netmgt.flows.persistence.model.NodeInfo;
import org.opennms.netmgt.flows.persistence.model.SamplingAlgorithm;
import org.opennms.netmgt.flows.processing.enrichment.EnrichedFlow;

import com.google.common.base.Strings;
import com.google.protobuf.DoubleValue;
import com.google.protobuf.UInt32Value;
import com.google.protobuf.UInt64Value;

public class FlowDocumentBuilder {

    public static FlowDocument buildFlowDocument(final Flow enrichedFlow) {
        FlowDocument.Builder builder = FlowDocument.newBuilder();
        builder.setTimestamp(enrichedFlow.getTimestamp() != null ? enrichedFlow.getTimestamp().toEpochMilli() : 0);
        builder.setDirection(fromDirection(enrichedFlow.getDirection()));
        getUInt64Value(enrichedFlow.getDstAs()).ifPresent(builder::setDstAs);
        getString(enrichedFlow.getDstAddr()).ifPresent(builder::setDstAddress);
        enrichedFlow.getDstAddrHostname().ifPresent(builder::setDstHostname);
        getUInt32Value(enrichedFlow.getDstMaskLen()).ifPresent(builder::setDstMaskLen);
        getUInt32Value(enrichedFlow.getDstPort()).ifPresent(builder::setDstPort);
        getString(enrichedFlow.getSrcAddr()).ifPresent(builder::setSrcAddress);
        enrichedFlow.getSrcAddrHostname().ifPresent(builder::setSrcHostname);
        getUInt64Value(enrichedFlow.getSrcAs()).ifPresent(builder::setSrcAs);
        getUInt32Value(enrichedFlow.getSrcMaskLen()).ifPresent(builder::setSrcMaskLen);
        getUInt32Value(enrichedFlow.getSrcPort()).ifPresent(builder::setSrcPort);
        getUInt32Value(enrichedFlow.getEngineId()).ifPresent(builder::setEngineId);
        getUInt32Value(enrichedFlow.getEngineType()).ifPresent(builder::setEngineType);
        getUInt64Value(enrichedFlow.getBytes()).ifPresent(builder::setNumBytes);
        getUInt64Value(enrichedFlow.getPackets()).ifPresent(builder::setNumPackets);
        getUInt64Value(enrichedFlow.getDeltaSwitched()).ifPresent(builder::setDeltaSwitched);
        getUInt64Value(enrichedFlow.getFirstSwitched()).ifPresent(builder::setFirstSwitched);
        getUInt64Value(enrichedFlow.getLastSwitched()).ifPresent(builder::setLastSwitched);
        getUInt32Value(enrichedFlow.getInputSnmp()).ifPresent(builder::setInputSnmpIfindex);
        getUInt32Value(enrichedFlow.getOutputSnmp()).ifPresent(builder::setOutputSnmpIfindex);
        getUInt32Value(enrichedFlow.getIpProtocolVersion()).ifPresent(builder::setIpProtocolVersion);
        getUInt32Value(enrichedFlow.getProtocol()).ifPresent(builder::setProtocol);
        getUInt32Value(enrichedFlow.getTcpFlags()).ifPresent(builder::setTcpFlags);
        getUInt32Value(enrichedFlow.getTos()).ifPresent(builder::setTos);
        getUInt32Value(enrichedFlow.getDscp()).ifPresent(dscp -> builder.setDscp(dscp));
        getUInt32Value(enrichedFlow.getEcn()).ifPresent(ecn -> builder.setEcn(ecn));
        getUInt32Value(enrichedFlow.getFlowRecords()).ifPresent(builder::setNumFlowRecords);
        getUInt64Value(enrichedFlow.getFlowSeqNum()).ifPresent(builder::setFlowSeqNum);
        String vlan = enrichedFlow.getVlan() != null ? Integer.toUnsignedString(enrichedFlow.getVlan()) : null;
        getString(vlan).ifPresent(builder::setVlan);
        getString(enrichedFlow.getNextHop()).ifPresent(builder::setNextHopAddress);
        enrichedFlow.getNextHopHostname().ifPresent(builder::setNextHopHostname);
        builder.setSamplingAlgorithm(fromSamplingAlgorithm(enrichedFlow.getSamplingAlgorithm()));
        getDoubleValue(enrichedFlow.getSamplingInterval()).ifPresent(builder::setSamplingInterval);
        NetflowVersion netflowVersion = fromNetflowVersion(enrichedFlow.getNetflowVersion());
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
        builder.setClockCorrection(enrichedFlow.getClockCorrection() != null ? enrichedFlow.getClockCorrection().toMillis() : 0);

        return builder.build();
    }

    private static Optional<UInt64Value> getUInt64Value(Long value) {
        if (value != null) {
            return Optional.of(UInt64Value.newBuilder().setValue(value).build());
        }
        return Optional.empty();
    }

    private static Optional<UInt64Value> getUInt64Value(final Instant value) {
        if (value != null) {
            return Optional.of(UInt64Value.newBuilder().setValue(value.toEpochMilli()).build());
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


    private static Direction fromDirection(Flow.Direction direction) {
        switch (direction) {
            case EGRESS:
                return Direction.EGRESS;
            case INGRESS:
                return Direction.INGRESS;
            case UNKNOWN:
                return Direction.UNKNOWN;
            default:
                throw new IllegalArgumentException("Unknown direction: " + direction.name());
        }
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


    private static SamplingAlgorithm fromSamplingAlgorithm(Flow.SamplingAlgorithm samplingAlgorithm) {
        if (samplingAlgorithm == null) {
            return UNASSIGNED;
        }
        switch (samplingAlgorithm) {
            case SystematicCountBasedSampling:
                return SYSTEMATIC_COUNT_BASED_SAMPLING;
            case SystematicTimeBasedSampling:
                return SYSTEMATIC_TIME_BASED_SAMPLING;
            case RandomNOutOfNSampling:
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

    private static NetflowVersion fromNetflowVersion(Flow.NetflowVersion netflowVersion) {
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

    private static Optional<NodeInfo> buildNodeInfo(Flow.NodeInfo nodeInfo) {
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
