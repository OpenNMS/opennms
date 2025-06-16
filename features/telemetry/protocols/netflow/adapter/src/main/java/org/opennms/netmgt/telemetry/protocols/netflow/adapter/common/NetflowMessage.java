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
package org.opennms.netmgt.telemetry.protocols.netflow.adapter.common;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.opennms.netmgt.flows.api.Flow;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.Value;

import static org.opennms.integration.api.v1.flows.Flow.Direction;
import static org.opennms.integration.api.v1.flows.Flow.NetflowVersion;
import static org.opennms.integration.api.v1.flows.Flow.SamplingAlgorithm;

import com.google.common.base.Strings;

public class NetflowMessage implements Flow {

    private final FlowMessage flowMessageProto;
    private final Instant receivedAt;

    public NetflowMessage(FlowMessage flowMessageProto, final Instant receivedAt) {
        this.flowMessageProto = flowMessageProto;
        this.receivedAt = Objects.requireNonNull(receivedAt);
    }

    @Override
    public Instant getReceivedAt() {
        return this.receivedAt;
    }

    @Override
    public Instant getTimestamp() {
        return Instant.ofEpochMilli(flowMessageProto.getTimestamp());
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
            default:
                return Direction.UNKNOWN;
        }
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
    public Instant getDeltaSwitched() {
        return flowMessageProto.hasDeltaSwitched() ? Instant.ofEpochMilli(flowMessageProto.getDeltaSwitched().getValue()) : getFirstSwitched();
    }

    @Override
    public Instant getFirstSwitched() {
        return flowMessageProto.hasFirstSwitched() ? Instant.ofEpochMilli(flowMessageProto.getFirstSwitched().getValue()) : null;
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
    public Instant getLastSwitched() {
        return flowMessageProto.hasLastSwitched() ? Instant.ofEpochMilli(flowMessageProto.getLastSwitched().getValue()) : null;
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
                return SamplingAlgorithm.RandomNOutOfNSampling;
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

    public Map<String, Object> getRawMessage() {
        return flowMessageProto.getRawMessageList().stream().collect(Collectors.toMap(Value::getName, this::transformValue));
    }

    private Object transformValue(final Value value) {
        if (value.hasBoolean()) {
            return value.getBoolean().getBool().getValue();
        }
        if (value.hasDatetime()) {
            return value.getDatetime().getUint64().getValue();
        }
        if (value.hasFloat()) {
            return value.getFloat().getDouble().getValue();
        }
        if (value.hasIpv4Address()) {
            return value.getIpv4Address().getString().getValue();
        }
        if (value.hasIpv6Address()) {
            return value.getIpv6Address().getString().getValue();
        }
        if (value.hasList()) {
            final List<Object> transformedList = new ArrayList<>();
            final List<org.opennms.netmgt.telemetry.protocols.netflow.transport.List> listOfLists = value.getList().getListList();
            for(final org.opennms.netmgt.telemetry.protocols.netflow.transport.List list : listOfLists) {
                transformedList.add(list.getValueList().stream().map(this::transformValue).collect(Collectors.toList()));
            }
            return transformedList;
        }
        if (value.hasMacaddress()) {
            return value.getMacaddress().getString().getValue();
        }
        if (value.hasNull()) {
            return null;
        }
        if (value.hasOctetarray()) {
            return value.getOctetarray().getBytes().getValue().asReadOnlyByteBuffer();
        }
        if (value.hasSigned()) {
            return value.getSigned().getInt64().getValue();
        }
        if(value.hasString()) {
            return value.getString().getString().getValue();
        }
        if (value.hasUndeclared()) {
            return value.getUndeclared().getBytes().getValue().asReadOnlyByteBuffer();
        }
        if (value.hasUnsigned()) {
            return value.getUnsigned().getUint64().getValue();
        }
        return null;
    }
}
