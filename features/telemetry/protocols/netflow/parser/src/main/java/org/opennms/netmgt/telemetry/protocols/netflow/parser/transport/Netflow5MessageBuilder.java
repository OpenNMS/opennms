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


import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.getBooleanValue;
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.getDoubleValue;
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.getInetAddress;
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.getLongValue;
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.getUInt32Value;
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.getUInt64Value;
import static org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.MessageUtils.setLongValue;

import java.net.InetAddress;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.RecordEnrichment;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.Direction;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.FlowMessage;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.NetflowVersion;
import org.opennms.netmgt.telemetry.protocols.netflow.transport.SamplingAlgorithm;

public class Netflow5MessageBuilder implements MessageBuilder {

    public Netflow5MessageBuilder() {
    }

    @Override
    public FlowMessage.Builder buildMessage(final Iterable<Value<?>> values, final RecordEnrichment enrichment) {
        final FlowMessage.Builder builder = FlowMessage.newBuilder();

        Long unixSecs = null;
        Long unixNSecs = null;
        Long sysUpTime = null;
        Long first = null;
        Long last = null;
        InetAddress srcAddr = null;
        InetAddress dstAddr = null;
        InetAddress nextHop = null;

        for (Value<?> value : values) {
            switch (value.getName()) {
                case "@count":
                    getUInt32Value(value).ifPresent(builder::setNumFlowRecords);
                    break;
                case "@unixSecs":
                    unixSecs = getLongValue(value);
                    break;
                case "@unixNSecs":
                    unixNSecs = getLongValue(value);
                    break;
                case "@sysUptime":
                    sysUpTime = getLongValue(value);
                    break;
                case "@flowSequence":
                    getUInt64Value(value).ifPresent(builder::setFlowSeqNum);
                    break;
                case "@engineType":
                    getUInt32Value(value).ifPresent(builder::setEngineType);
                    break;
                case "@engineId":
                    getUInt32Value(value).ifPresent(builder::setEngineId);
                    break;
                case "@samplingAlgorithm":
                    Long saValue = getLongValue(value);
                    SamplingAlgorithm samplingAlgorithm = SamplingAlgorithm.UNASSIGNED;
                    if (saValue != null) {
                        switch (saValue.intValue()) {
                            case 1:
                                samplingAlgorithm = SamplingAlgorithm.SYSTEMATIC_COUNT_BASED_SAMPLING;
                                break;
                            case 2:
                                samplingAlgorithm = SamplingAlgorithm.RANDOM_N_OUT_OF_N_SAMPLING;
                                break;
                        }
                    }
                    builder.setSamplingAlgorithm(samplingAlgorithm);
                    break;
                case "@samplingInterval":
                    getDoubleValue(value).ifPresent(builder::setSamplingInterval);
                    break;

                case "srcAddr":
                    srcAddr = getInetAddress(value);
                    break;
                case "dstAddr":
                    dstAddr = getInetAddress(value);
                    break;
                case "nextHop":
                    nextHop = getInetAddress(value);
                    break;
                case "input":
                    getUInt32Value(value).ifPresent(builder::setInputSnmpIfindex);
                    break;
                case "output":
                    getUInt32Value(value).ifPresent(builder::setOutputSnmpIfindex);
                    break;
                case "dPkts":
                    getUInt64Value(value).ifPresent(builder::setNumPackets);
                    break;
                case "dOctets":
                    getUInt64Value(value).ifPresent(builder::setNumBytes);
                    break;
                case "first":
                    first = getLongValue(value);
                    break;
                case "last":
                    last = getLongValue(value);
                    break;
                case "srcPort":
                    getUInt32Value(value).ifPresent(builder::setSrcPort);
                    break;
                case "dstPort":
                    getUInt32Value(value).ifPresent(builder::setDstPort);
                    break;
                case "tcpFlags":
                    getUInt32Value(value).ifPresent(builder::setTcpFlags);
                    break;
                case "proto":
                    getUInt32Value(value).ifPresent(builder::setProtocol);
                    break;
                case "srcAs":
                    getUInt64Value(value).ifPresent(builder::setSrcAs);
                    break;
                case "dstAs":
                    getUInt64Value(value).ifPresent(builder::setDstAs);
                    break;
                case "tos":
                    getUInt32Value(value).ifPresent(builder::setTos);
                    break;
                case "srcMask":
                    getUInt32Value(value).ifPresent(builder::setSrcMaskLen);
                    break;
                case "dstMask":
                    getUInt32Value(value).ifPresent(builder::setDstMaskLen);
                    break;
                case "egress":
                    Boolean egress = getBooleanValue(value);
                    Direction direction = egress ? Direction.EGRESS : Direction.INGRESS;
                    builder.setDirection(direction);
                    break;
            }
        }

        long timeStamp = unixSecs * 1000L + unixNSecs / 1000_000L;
        long bootTime = timeStamp - sysUpTime;

        builder.setNetflowVersion(NetflowVersion.V5);
        builder.setFirstSwitched(setLongValue(bootTime + first));
        builder.setLastSwitched(setLongValue(bootTime + last));
        builder.setTimestamp(timeStamp);

        if (srcAddr != null) {
            builder.setSrcAddress(srcAddr.getHostAddress());
            enrichment.getHostnameFor(srcAddr).ifPresent(builder::setSrcHostname);
        }
        if (dstAddr != null) {
            builder.setDstAddress(dstAddr.getHostAddress());
            enrichment.getHostnameFor(dstAddr).ifPresent(builder::setDstHostname);
        }
        if (nextHop != null) {
            builder.setNextHopAddress(nextHop.getHostAddress());
            enrichment.getHostnameFor(nextHop).ifPresent(builder::setNextHopHostname);
        }

        return builder;
    }
}
