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
package org.opennms.netmgt.telemetry.protocols.netflow.parser.transport;

import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.first;
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.getDoubleValue;
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.getInetAddress;
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.getLongValue;
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.getUInt32Value;
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.getUInt64Value;
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.setDoubleValue;
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.setIntValue;
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.setLongValue;

import java.net.InetAddress;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.RecordEnrichment;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.Direction;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.NetflowVersion;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.SamplingAlgorithm;

import com.google.protobuf.UInt32Value;

@SuppressWarnings("java:S109")
public class Netflow9MessageBuilder implements MessageBuilder {

    private Long flowActiveTimeoutFallback;
    private Long flowInactiveTimeoutFallback;
    private Long flowSamplingIntervalFallback;

    @Override
    public FlowMessage.Builder buildMessage(final Iterable<Value<?>> values, final RecordEnrichment enrichment) {
        final FlowMessage.Builder builder = FlowMessage.newBuilder();
        builder.setDirection(Direction.UNKNOWN);

        InetAddress ipv4DstAddress = null;
        InetAddress ipv6DstAddress = null;
        Long dstMask = null;
        Long ipv6DstMask = null;
        InetAddress ipv4NextHop = null;
        InetAddress ipv6NextHop = null;
        InetAddress bgpIpv4NextHop = null;
        InetAddress bgpIpv6NextHop = null;
        InetAddress ipv4SrcAddress = null;
        InetAddress ipv6SrcAddress = null;
        Long srcMask = null;
        Long ipv6SrcMask = null;
        Long srcVlan = null;
        Long dstVlan = null;
        Long flowActiveTimeout = this.flowActiveTimeoutFallback;
        Long flowInActiveTimeout = this.flowInactiveTimeoutFallback;
        long sysUpTime = 0;
        long unixSecs = 0;
        Long firstSwitched = null;
        Long lastSwitched = null;
        Long flowStartMilliseconds = null;
        Long flowEndMilliseconds = null;
        UInt32Value ingressPhysicalInterface = null;
        UInt32Value egressPhysicalInterface = null;
        UInt32Value inputSnmp = null;
        UInt32Value outputSnmp = null;

	if (this.flowSamplingIntervalFallback != null) {
	    builder.setSamplingInterval(setDoubleValue(this.flowSamplingIntervalFallback));
	}

        for (final Value<?> value : values) {
            switch (value.getName()) {
                // Header
                case "@recordCount":
                    getUInt32Value(value).ifPresent(builder::setNumFlowRecords);
                    break;
                case "@sequenceNumber":
                    getUInt64Value(value).ifPresent(builder::setFlowSeqNum);
                    break;
                case "@sourceId":
                    getUInt64Value(value).ifPresent(srcId -> builder.setNodeIdentifier(String.valueOf(srcId.getValue())));
                    break;
                case "@sysUpTime":
                    sysUpTime = getLongValue(value);
                    break;
                case "@unixSecs":
                    unixSecs = getLongValue(value);
                    break;
                case "IN_BYTES":
                    getUInt64Value(value).ifPresent(builder::setNumBytes);
                    break;
                case "DIRECTION":
                builder.setDirection(getDirection(value));
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
                    inputSnmp = getUInt32Value(value).orElse(null);
                    break;
                case "IP_PROTOCOL_VERSION":
                    getUInt32Value(value).ifPresent(builder::setIpProtocolVersion);
                    break;
                case "OUTPUT_SNMP":
                    outputSnmp = getUInt32Value(value).orElse(null);
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
                    getUInt64Value(value).ifPresent(builder::setNumPackets);
                    break;
                case "PROTOCOL":
                    getUInt32Value(value).ifPresent(builder::setProtocol);
                    break;
                case "SAMPLING_ALGORITHM":
                builder.setSamplingAlgorithm(getSamplingAlgorithm(value));
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
                case "ingressPhysicalInterface":
                    ingressPhysicalInterface = getUInt32Value(value).orElse(null);
                    break;
                case "egressPhysicalInterface":
                    egressPhysicalInterface = getUInt32Value(value).orElse(null);
                    break;
                default:
                    break;
            }
        }

        long timeStampInMsecs = unixSecs * 1000;
        builder.setTimestamp(timeStampInMsecs);

        long bootTime = timeStampInMsecs - sysUpTime;

        if (firstSwitched != null) {
            builder.setFirstSwitched(setLongValue(firstSwitched + bootTime));
        } else {
            // Some Cisco platforms also support absolute timestamps in NetFlow v9 (like defined in IPFIX). See NMS-13006
            if (flowStartMilliseconds != null) {
                builder.setFirstSwitched(setLongValue(flowStartMilliseconds));
            }
        }
        if(lastSwitched != null) {
            builder.setLastSwitched(setLongValue(lastSwitched + bootTime));
        } else {
            // Some Cisco platforms also support absolute timestamps in NetFlow v9 (like defined in IPFIX). See NMS-13006
            if (flowEndMilliseconds != null) {
                builder.setLastSwitched(setLongValue(flowEndMilliseconds));
            }
        }

        // Set input interface
        first(ingressPhysicalInterface, inputSnmp).ifPresent(builder::setInputSnmpIfindex);

        // Set output interface
        first(egressPhysicalInterface, outputSnmp).ifPresent(builder::setOutputSnmpIfindex);


        // Set Destination address and host name.
        first(ipv6DstAddress, ipv4DstAddress).ifPresent((final InetAddress inetAddress) -> {
            enrichment.getHostnameFor(inetAddress).ifPresent(builder::setDstHostname);
            builder.setDstAddress(inetAddress.getHostAddress());
        });

        // dst mask
        first(ipv6DstMask, dstMask).ifPresent(dstMaskLen ->
            builder.setDstMaskLen(setIntValue(dstMaskLen.intValue())));

        // Set Source address and host name.
        first(ipv6SrcAddress, ipv4SrcAddress).ifPresent((final InetAddress inetAddress) -> {
            enrichment.getHostnameFor(inetAddress).ifPresent(builder::setSrcHostname);
            builder.setSrcAddress(inetAddress.getHostAddress());
        });
        // src mask
        first(ipv6SrcMask, srcMask).ifPresent(srcMaskLen -> builder.setSrcMaskLen(setIntValue(srcMaskLen.intValue())));

        // Set next hop address, hostname.
        first(ipv6NextHop, ipv4NextHop, bgpIpv6NextHop, bgpIpv4NextHop).ifPresent((final InetAddress inetAddress) -> {
            enrichment.getHostnameFor(inetAddress).ifPresent(builder::setNextHopHostname);
            builder.setNextHopAddress(inetAddress.getHostAddress());
        });

        // set vlan
        first(srcVlan, dstVlan).ifPresent( vlan -> builder.setVlan(setIntValue(vlan.intValue())));

        Timeout timeout = new Timeout(flowActiveTimeout, flowInActiveTimeout);
        timeout.setFirstSwitched(builder.hasFirstSwitched() ? builder.getFirstSwitched().getValue() : null);
        timeout.setLastSwitched(builder.hasLastSwitched() ? builder.getLastSwitched().getValue() : null);
        timeout.setNumBytes(builder.getNumBytes().getValue());
        timeout.setNumPackets(builder.getNumPackets().getValue());
        Long deltaSwitched = timeout.getDeltaSwitched();
        getUInt64Value(deltaSwitched).ifPresent(builder::setDeltaSwitched);

        builder.setNetflowVersion(NetflowVersion.V9);
        return builder;
    }

    private static Direction getDirection(final Value<?> value) {
        Long directionValue = getLongValue(value);
        Direction direction = Direction.UNKNOWN;
        if (directionValue != null) {
            switch (directionValue.intValue()) {
                case 0:
                    direction = Direction.INGRESS;
                    break;
                case 1:
                    direction = Direction.EGRESS;
                    break;
                default:
                    break;
            }
        }
        return direction;
    }

    private static SamplingAlgorithm getSamplingAlgorithm(final Value<?> value) {
        Long saValue = getLongValue(value);
        SamplingAlgorithm samplingAlgorithm = SamplingAlgorithm.UNASSIGNED;
        if (saValue != null) {
            switch(saValue.intValue()) {
                case 1:
                    samplingAlgorithm = SamplingAlgorithm.SYSTEMATIC_COUNT_BASED_SAMPLING;
                    break;
                case 2:
                    samplingAlgorithm = SamplingAlgorithm.RANDOM_N_OUT_OF_N_SAMPLING;
                    break;
                default:
                    break;
            }
        }
        return samplingAlgorithm;
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
