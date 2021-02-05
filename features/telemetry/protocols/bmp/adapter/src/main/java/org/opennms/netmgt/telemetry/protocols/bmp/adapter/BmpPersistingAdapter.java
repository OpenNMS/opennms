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

package org.opennms.netmgt.telemetry.protocols.bmp.adapter;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.BmpMessageHandler;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.Context;
import org.opennms.netmgt.telemetry.protocols.bmp.adapter.openbmp.proto.Record;
import org.opennms.netmgt.telemetry.protocols.bmp.transport.Transport;
import org.opennms.netmgt.telemetry.protocols.collection.AbstractAdapter;

import com.codahale.metrics.MetricRegistry;
import com.google.protobuf.InvalidProtocolBufferException;

public class BmpPersistingAdapter extends AbstractAdapter {

    private final BmpMessageHandler bmpMessageHandler;

    private final AtomicLong sequence = new AtomicLong();

    public BmpPersistingAdapter(AdapterDefinition adapterConfig, MetricRegistry metricRegistry, BmpMessageHandler bmpMessageHandler) {
        super(adapterConfig, metricRegistry);
        this.bmpMessageHandler = bmpMessageHandler;
    }

    @Override
    public void handleMessage(TelemetryMessageLogEntry messageLogEntry, TelemetryMessageLog messageLog) {

        LOG.trace("Parsing packet: {}", messageLogEntry);
        final Transport.Message message;
        try {
            message = Transport.Message.parseFrom(messageLogEntry.getByteArray());
        } catch (final InvalidProtocolBufferException e) {
            LOG.error("Invalid message", e);
            return;
        }

        final String collectorHashId = Record.hash(messageLog.getSystemId());
        final String routerHashId = Record.hash(messageLog.getSourceAddress(), collectorHashId);
        final Context context = new Context(messageLog.getSystemId(),
                collectorHashId,
                routerHashId,
                Instant.ofEpochMilli(messageLogEntry.getTimestamp()),
                InetAddressUtils.addr(messageLog.getSourceAddress()),
                messageLog.getSourcePort(), messageLog.getLocation());

        switch(message.getPacketCase()) {
            case HEARTBEAT:
                BmpAdapterCommon.handleHeartbeatMessage(bmpMessageHandler, message, message.getHeartbeat(), context, sequence);
                break;
            case INITIATION:
                BmpAdapterCommon.handleInitiationMessage(bmpMessageHandler, message, message.getInitiation(), context, sequence);
                break;
            case TERMINATION:
                BmpAdapterCommon.handleTerminationMessage(bmpMessageHandler, message, message.getTermination(), context, sequence);
                break;
            case PEER_UP:
                BmpAdapterCommon.handlePeerUpNotification(bmpMessageHandler, message, message.getPeerUp(), context, sequence);
                break;
            case PEER_DOWN:
                BmpAdapterCommon.handlePeerDownNotification(bmpMessageHandler, message, message.getPeerDown(), context, sequence);
                break;
            case STATISTICS_REPORT:
                BmpAdapterCommon.handleStatisticReport(bmpMessageHandler, message, message.getStatisticsReport(), context, sequence);
                break;
            case ROUTE_MONITORING:
                BmpAdapterCommon.handleRouteMonitoringMessage(bmpMessageHandler, message, message.getRouteMonitoring(), context, sequence);
                break;
            case PACKET_NOT_SET:
                break;
        }
    }
}
