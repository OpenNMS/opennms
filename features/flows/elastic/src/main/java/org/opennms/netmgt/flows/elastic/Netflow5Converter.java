/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.netmgt.flows.api.NF5Packet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Netflow5Converter {

    private static final Logger LOG = LoggerFactory.getLogger(Netflow5Converter.class);

    public List<FlowDocument> convert(NF5Packet packet) {
        if (packet == null) {
            LOG.debug("Nothing to convert.");
            return Collections.emptyList();
        }
        return packet.getRecords().stream()
                .map(record -> {
                    final FlowDocument document = new FlowDocument();

                    // Netflow 5
                    document.setNetflowVersion(NetflowVersion.V5);

                    // Compute the effective timestamp
                    final long timestampMs = packet.getUnixSecs() * 1000L + packet.getUnixNSecs() / 1000L / 1000L;
                    document.setTimestamp(timestampMs);

                    // Netflow 5 is only captured on ingress
                    document.setDirection(Direction.INGRESS.getValue());

                    // All Netflow 5 flows are IPv4
                    document.setIpProtocolVersion(FlowDocument.IPV4_PROTOCOL_VERSION);

                    // Header
                    document.setFlowRecords(packet.getCount());
                    document.setFlowSeqNum(packet.getFlowSequence());
                    document.setEngineType(packet.getEngineType());
                    document.setEngineId(packet.getEngineId());
                    document.setSamplingInterval(packet.getSamplingInterval());

                    // Body
                    document.setSrcAddr(record.getSrcAddr());
                    document.setSrcPort(record.getSrcPort());
                    document.setDstAddr(record.getDstAddr());
                    document.setDstPort(record.getDstPort());
                    document.setNextHop(record.getNextHop());
                    document.setInputSnmp(record.getInput());
                    document.setOutputSnmp(record.getOutput());
                    document.setBytes(record.getDOctets());
                    document.setPackets(record.getDPkts());
                    document.setFirstSwitched(getSwitched(timestampMs, packet.getSysUptime(), record.getFirst()));
                    document.setLastSwitched(getSwitched(timestampMs, packet.getSysUptime(), record.getLast()));
                    document.setTcpFlags(record.getTcpFlags());
                    document.setProtocol(record.getProt());
                    document.setTos(record.getToS());
                    document.setSrcAs(record.getSrcAs());
                    document.setDstAs(record.getDstAs());
                    document.setSrcMaskLen(record.getSrcMask());
                    document.setDstMaskLen(record.getDstMask());

                    return document;
                })
                .collect(Collectors.toList());
    }

    /**
     * @param timestampMs Current unix timestamp in milliseconds.
     * @param sysUptimeMs Current time in milliseconds since the export device booted.
     * @param switchedUptimeMs System uptime at which the packet was switched.
     * @return Unix timestamp in milliseconds at which the packet was switched.
     */
    private static long getSwitched(long timestampMs, long sysUptimeMs, long switchedUptimeMs) {
        // The packet was switched deltaMs ago
        final long deltaMs = sysUptimeMs - switchedUptimeMs;
        // Substract this duration from the timestamp
        return timestampMs - deltaMs;
    }
}
