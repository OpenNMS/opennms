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

package org.opennms.netmgt.telemetry.protocols.openconfig.connector;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.openconfig.api.OpenConfigClient;
import org.opennms.features.openconfig.api.OpenConfigClientFactory;
import org.opennms.netmgt.telemetry.api.receiver.Connector;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.util.internal.SocketUtils;

public class OpenConfigConnector implements Connector {
    private static final Logger LOG = LoggerFactory.getLogger(OpenConfigConnector.class);

    private final OpenConfigClientFactory clientFactory;
    private final AsyncDispatcher<TelemetryMessage> dispatcher;
    private OpenConfigClient openConfigClient;

    public OpenConfigConnector(AsyncDispatcher<TelemetryMessage> dispatcher, OpenConfigClientFactory clientFactory) {
        this.dispatcher = Objects.requireNonNull(dispatcher);
        this.clientFactory = Objects.requireNonNull(clientFactory);
    }

    @Override
    public void stream(int nodeId, InetAddress ipAddress, List<Map<String, String>> paramList) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Starting new OpenConfig client for: {}", InetAddressUtils.str(ipAddress));
        }
        openConfigClient = clientFactory.create(ipAddress, paramList);
        openConfigClient.subscribe(new OpenConfigClient.Handler() {
            @Override
            public void accept(InetAddress host, Integer port, byte[] data) {
                InetSocketAddress remoteAddress = SocketUtils.socketAddress(host.getHostAddress(), port);
                final TelemetryMessage msg = new TelemetryMessage(remoteAddress, ByteBuffer.wrap(data));
                dispatcher.send(msg);
            }

            @Override
            public void onError(String error) {
                LOG.warn("Error while streaming openconfig data, {}", error);
            }
        });

    }

    @Override
    public void close() {
        openConfigClient.shutdown();
    }
}
