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

package org.opennms.netmgt.telemetry.adapters.netflow;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.opennms.netmgt.flows.api.FlowType;
import org.opennms.netmgt.flows.api.NetflowDocument;
import org.opennms.netmgt.telemetry.adapters.netflow.v5.NetflowPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Netflow5Converter {

    private static final Logger LOG = LoggerFactory.getLogger(Netflow5Converter.class);

    public List<NetflowDocument> convert(NetflowPacket netflowPacket) {
        if (netflowPacket == null) {
            LOG.debug("Nothing to convert.");
            return new ArrayList<>();
        }
        return netflowPacket.getRecords().stream()
                .map(record -> {
                    final NetflowDocument document = new NetflowDocument();

                    // META
                    document.setFlowType(FlowType.NETFLOW_5);
                    document.setTimestamp(netflowPacket.getUnixSecs() * 1000L + netflowPacket.getUnixNSecs() / 1000L / 1000L);

                    // Header
                    document.setVersion(netflowPacket.getVersion());
                    document.setFlowRecords(netflowPacket.getCount());
                    document.setSysUptime(netflowPacket.getSysUptime());
                    document.setFlowSequenceNumber(netflowPacket.getFlowSequence());
                    document.setEngineType(netflowPacket.getEngineType());
                    document.setEngineId(netflowPacket.getEngineId());
                    document.setSamplingInterval(netflowPacket.getSamplingInterval());

                    // Body
                    document.setIpv4SourceAddress(record.getSrcAddr());
                    document.setSourcePort(record.getSrcPort());
                    document.setIpv4DestAddress(record.getDstAddr());
                    document.setDestPort(record.getDstPort());
                    document.setIpv4NextHopAddress(record.getNextHop());
                    document.setInputSnmpInterfaceIndex(record.getInput());
                    document.setOutputSnmpInterfaceIndex(record.getOutput());
                    document.setInBytes(record.getDOctets());
                    document.setInPackets(record.getDPkts());
                    document.setFirst(record.getFirst());
                    document.setLast(record.getLast());
                    document.setTcpFlags(record.getTcpFlags());
                    document.setIpProtocol(record.getProt());
                    document.setTos(record.getToS());
                    document.setSourceAutonomousSystemNumber(record.getSrcAs());
                    document.setDestAutonomousSystemNumber(record.getDstAs());
                    document.setSourceMask(record.getSrcMask());
                    document.setDestMask(record.getDstMask());

                    return document;
                })
                .collect(Collectors.toList());
    }
}
