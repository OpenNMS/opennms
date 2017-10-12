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

import java.util.List;
import java.util.stream.Collectors;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.flows.api.FlowType;
import org.opennms.netmgt.flows.api.NetflowDocument;
import org.opennms.netmgt.telemetry.adapters.api.Adapter;
import org.opennms.netmgt.telemetry.adapters.netflow.v5.NetflowPacket;
import org.opennms.netmgt.telemetry.config.model.Protocol;
import org.opennms.netmgt.telemetry.ipc.TelemetryMessageDTO;
import org.opennms.netmgt.telemetry.ipc.TelemetryMessageLogDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Netflow5Adapter implements Adapter {


    private static final Logger LOG = LoggerFactory.getLogger(Netflow5Adapter.class);

    private FlowRepositoryProvider provider = new FlowRepositoryProvider();

    @Override
    public void setProtocol(Protocol protocol) {
        // we do not need the protocol
    }

    @Override
    public void handleMessageLog(TelemetryMessageLogDTO messageLog) {
        LOG.debug("Received {} telemetry messages", messageLog.getMessages().size());

        for (TelemetryMessageDTO eachMessage : messageLog.getMessages()) {
            LOG.debug("Parse log message {}", eachMessage);

            final NetflowPacket flowPacket = parseMessage(eachMessage);
            if (flowPacket != null) {
                LOG.debug("Flow packet received: {}. Try persisting.", flowPacket);
                persist(flowPacket, messageLog);
            }
        }
    }
    
    private NetflowPacket parseMessage(TelemetryMessageDTO messageDTO) {
        // Create NetflowPacket which delegates all calls to the byte array
        try {
            final NetflowPacket flowPacket = new NetflowPacket(messageDTO.getBytes());
            if (flowPacket.getVersion() != NetflowPacket.VERSION) {
                LOG.warn("Invalid Version. Expected {}, received {}. Skipping flow packet.", NetflowPacket.VERSION, flowPacket.getVersion());
            } else if (flowPacket.getCount() == 0) {
                LOG.warn("Received packet has no content. Skipping flow packet.");
            } else if (!flowPacket.isValid()) {
                // TODO MVR an invalid packet is skipped for now, but we may want to persist it anyways
                LOG.warn("Received packet is not valid. Skipping flow packet.");
            } else {
                return flowPacket;
            }
        } catch (Throwable e) {
            LOG.error("Received packet cannot be read.", e);
        }
        return null;
    }
    
    private void persist(NetflowPacket netflowPacket, TelemetryMessageLogDTO messageLog) {
        final List<NetflowDocument> flowDocuments = convert(netflowPacket, messageLog);
        try {
            provider.getFlowRepository().save(flowDocuments);
        } catch (Throwable e) {
            // TODO MVR deal with this accordingly
            LOG.error("Error", e);
        }
    }

    private List<NetflowDocument> convert(NetflowPacket netflowPacket, TelemetryMessageLogDTO messageLog) {
        return netflowPacket.getRecords().stream()
                .map(record -> {
                    final NetflowDocument document = new NetflowDocument();

                    // META
                    document.setExporterAddress(InetAddressUtils.toIpAddrString(messageLog.getSourceAddress()));
                    document.setLocation(messageLog.getLocation());
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
