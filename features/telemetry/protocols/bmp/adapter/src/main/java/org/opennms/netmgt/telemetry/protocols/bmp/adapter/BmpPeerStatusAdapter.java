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

import static org.opennms.netmgt.telemetry.protocols.bmp.adapter.BmpAdapterTools.address;

import java.net.InetAddress;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.opennms.core.rpc.utils.mate.ContextKey;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.dao.api.InterfaceToNodeCache;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.OnmsNode;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLog;
import org.opennms.netmgt.telemetry.api.adapter.TelemetryMessageLogEntry;
import org.opennms.netmgt.telemetry.config.api.AdapterDefinition;
import org.opennms.netmgt.telemetry.protocols.bmp.transport.Transport;
import org.opennms.netmgt.telemetry.protocols.collection.AbstractAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Strings;
import com.google.protobuf.InvalidProtocolBufferException;

public class BmpPeerStatusAdapter extends AbstractAdapter {

    private static final Logger LOG = LoggerFactory.getLogger(BmpPeerStatusAdapter.class);

    private final InterfaceToNodeCache interfaceToNodeCache;

    private final EventForwarder eventForwarder;

    private final NodeDao nodeDao;

    private String metaDataNodeLookup;
    private ContextKey contextKey;

    public BmpPeerStatusAdapter(final AdapterDefinition adapterConfig,
                                final InterfaceToNodeCache interfaceToNodeCache,
                                final EventForwarder eventForwarder,
                                final MetricRegistry metricRegistry,
                                final NodeDao nodeDao) {
        super(adapterConfig, metricRegistry);

        this.interfaceToNodeCache = Objects.requireNonNull(interfaceToNodeCache);
        this.eventForwarder = Objects.requireNonNull(eventForwarder);
        this.nodeDao = nodeDao;
    }

    @Override
    public void handleMessage(final TelemetryMessageLogEntry messageLogEntry,
                              final TelemetryMessageLog messageLog) {
        LOG.trace("Parsing packet: {}", messageLogEntry);
        final Transport.Message message;
        try {
            message = Transport.Message.parseFrom(messageLogEntry.getByteArray());
        } catch (final InvalidProtocolBufferException e) {
            LOG.error("Invalid message", e);
            return;
        }

        // This adapter only cares about peer up/down packets
        final Transport.PeerUpPacket peerUp = message.hasPeerUp() ? message.getPeerUp() : null;
        final Transport.PeerDownPacket peerDown = message.hasPeerDown() ? message.getPeerDown() : null;
        if (peerUp == null && peerDown == null) {
            return;
        }

        // Find the node for the router who has exported the peer status notification
        final InetAddress exporterAddress = InetAddressUtils.getInetAddress(messageLog.getSourceAddress());
        Optional<Integer> exporterNodeId = this.interfaceToNodeCache.getFirstNodeId(messageLog.getLocation(), exporterAddress);

        if (!exporterNodeId.isPresent()) {
            LOG.info("Unable to find node for exporter address: {}", exporterAddress);

            if (message.hasBgpId()) {
                final String bgpId = InetAddressUtils.toIpAddrString(address(message.getBgpId()));
                final List<OnmsNode> nodes = nodeDao.findNodeWithMetaData(contextKey.getContext(), contextKey.getKey(), bgpId);

                if (nodes.size() > 0) {
                    if (nodes.size() > 1) {
                        LOG.warn("More that one node match bgpId: {}", bgpId);
                    }

                    exporterNodeId = Optional.of(nodes.get(0).getId());
                } else {
                    LOG.warn("Unable to find node for bgpId: {}", bgpId);
                    return;
                }
            } else {
                return;
            }
        }

        final String uei = peerUp != null
                           ? EventConstants.BMP_PEER_UP
                           : EventConstants.BMP_PEER_DOWN;

        final Transport.Peer peer = peerUp != null
                                    ? peerUp.getPeer()
                                    : peerDown.getPeer();

        final Instant timestamp = Instant.ofEpochSecond(peer.getTimestamp().getSeconds(), peer.getTimestamp().getNanos());

        final EventBuilder event = new EventBuilder(uei, "telemetryd:" + this.adapterConfig.getFullName(), Date.from(timestamp));
        event.setNodeid(exporterNodeId.get());
        event.setInterface(exporterAddress);

        // Extract peer details
        event.addParam("distinguisher", peer.getDistinguisher());
        event.addParam("address", InetAddressUtils.str(address(peer.getAddress())));
        event.addParam("as", Long.toString(peer.getAs()));
        event.addParam("id", InetAddressUtils.str(address(peer.getId())));

        // Extract error details
        if (peerDown != null) {
            switch (peerDown.getReasonCase()) {
                case LOCAL_BGP_NOTIFICATION:
                    event.addParam("error", "Local disconnect: " + peerDown.getLocalBgpNotification());
                    break;

                case LOCAL_NO_NOTIFICATION:
                    event.addParam("error", "Local disconnect without notification: code = " + peerDown.getLocalNoNotification());
                    break;

                case REMOTE_BGP_NOTIFICATION:
                    event.addParam("error", "Remote disconnect: " + peerDown.getRemoteBgpNotification());
                    break;

                case REMOTE_NO_NOTIFICATION:
                    event.addParam("error", "Remote disconnect without notification");
                    break;
            }
        }

        this.eventForwarder.sendNow(event.getEvent());
    }

    public String getMetaDataNodeLookup() {
        return metaDataNodeLookup;
    }

    public void setMetaDataNodeLookup(String metaDataNodeLookup) {
        this.metaDataNodeLookup = metaDataNodeLookup;

        if (!Strings.isNullOrEmpty(this.metaDataNodeLookup)) {
            this.contextKey = new ContextKey(metaDataNodeLookup);
        } else {
            this.contextKey = null;
        }
    }
}
