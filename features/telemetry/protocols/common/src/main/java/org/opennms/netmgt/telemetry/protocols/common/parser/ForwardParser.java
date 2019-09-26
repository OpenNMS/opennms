/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.common.parser;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.listeners.UdpParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ForwardParser implements UdpParser {
    private static final Logger LOG = LoggerFactory.getLogger(ForwardParser.class);

    private final String name;
    private final AsyncDispatcher<TelemetryMessage> dispatcher;

    public ForwardParser(final String name, final AsyncDispatcher<TelemetryMessage> dispatcher) {
        this.name = Objects.requireNonNull(name);
        this.dispatcher = Objects.requireNonNull(dispatcher);
    }

    public String getName() {
        return name;
    }

    @Override
    public void start(final ScheduledExecutorService executorService) {
    }

    @Override
    public void stop() {
    }

    @Override
    public CompletableFuture<?> parse(final ByteBuffer buffer,
                                      final InetSocketAddress remoteAddress,
                                      final InetSocketAddress localAddress) throws Exception {
        LOG.trace("Got packet from: {}", remoteAddress);

        // Build the message to dispatch
        final TelemetryMessage msg = new TelemetryMessage(remoteAddress, buffer);

        // Dispatch and retain a reference to the packet
        // in the case that we are sharing the underlying byte array
        return dispatcher.send(msg);
    }
}
