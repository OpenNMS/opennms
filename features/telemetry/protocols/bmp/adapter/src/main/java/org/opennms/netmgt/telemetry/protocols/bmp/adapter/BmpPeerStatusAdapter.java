/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.bmp.adapter;

import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.first;
import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.getInt64;
import static org.opennms.netmgt.telemetry.protocols.common.utils.BsonUtils.getString;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import org.bson.BsonDocument;
import org.bson.RawBsonDocument;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.protocols.bmp.parser.proto.bmp.Header;
import org.opennms.netmgt.telemetry.protocols.collection.AbstractAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;

public class BmpPeerStatusAdapter extends AbstractAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(BmpPeerStatusAdapter.class);

    private final InterfaceToNodeCache interfaceToNodeCache;

    private final EventForwarder eventForwarder;

    public BmpPeerStatusAdapter(final AdapterDefinition adapterConfig,
                                final InterfaceToNodeCache interfaceToNodeCache,
                                final EventForwarder eventForwarder,
                                final MetricRegistry metricRegistry) {
        super(adapterConfig, metricRegistry);

        this.interfaceToNodeCache = Objects.requireNonNull(interfaceToNodeCache);
        this.eventForwarder = Objects.requireNonNull(eventForwarder);
    }

    @Override
    public void handleMessage(final TelemetryMessageLogEntry message,
                              final TelemetryMessageLog messageLog) {
        LOG.trace("Parsing packet: {}", message);
        final BsonDocument document = new RawBsonDocument(message.getByteArray());

        // This adapter only cares about peer up/down packets
        if (!getString(document, "@type")
                .map(type -> Header.Type.PEER_UP_NOTIFICATION.name().equals(type) ||
                             Header.Type.PEER_DOWN_NOTIFICATION.name().equals(type))
                .orElse(false)) {
            return;
        }

        // Find the node for the router who has exported the peer status notification
        final InetAddress exporterAddress = InetAddressUtils.getInetAddress(messageLog.getSourceAddress());
        final Optional<Integer> exporterNodeId = this.interfaceToNodeCache.getFirstNodeId(messageLog.getLocation(), exporterAddress);
        if (!exporterNodeId.isPresent()) {
            LOG.warn("Unable to find node for exporter: {}", exporterAddress);
            return;
        }

        final boolean up = getString(document, "@type")
                .map(type -> Header.Type.PEER_UP_NOTIFICATION.name().equals(type))
                .orElse(false);

        final String uei = up
                           ? EventConstants.BMP_PEER_UP
                           : EventConstants.BMP_PEER_DOWN;

        final Instant timestamp = Instant.ofEpochSecond(getInt64(document, "peer", "timestamp", "epoch").get(),
                                                        getInt64(document, "peer", "timestamp", "nanos").orElse(0L));

        final EventBuilder event = new EventBuilder(uei, "telemetryd:" + this.adapterConfig.getName(), Date.from(timestamp));
        event.setNodeid(exporterNodeId.get());
        event.setInterface(exporterAddress);

        // Extract peer details
        getInt64(document, "peer", "distinguisher").ifPresent(value -> event.addParam("distinguisher", Long.toString(value)));
        getString(document, "peer", "address").ifPresent(value -> event.addParam("address", value));
        getInt64(document, "peer", "as").ifPresent(value -> event.addParam("as", Long.toString(value)));
        getInt64(document, "peer", "id").ifPresent(value -> event.addParam("id", Long.toString(value)));

        // Extract error details
        if (!up) {
            getString(document, "type").ifPresent(value -> event.addParam("type", value));
            first(getInt64(document, "code").map(code -> "Code " + code),
                  getString(document, "error"))
                    .ifPresent(value -> event.addParam("error", value));
        }

        this.eventForwarder.sendNow(event.getEvent());
    }
}
