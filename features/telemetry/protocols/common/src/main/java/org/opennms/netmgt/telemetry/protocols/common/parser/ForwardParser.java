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
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.listeners.simple.SimpleUdpParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.EventLoopGroup;

public class ForwardParser implements SimpleUdpParser.Factory {
    private static final Logger LOG = LoggerFactory.getLogger(ForwardParser.class);

    @Override
    public SimpleUdpParser createUdpParser(final String name,
                                           final Map<String, String> parameters,
                                           final AsyncDispatcher<TelemetryMessage> dispatcher) {
        return new SimpleUdpParser() {

            public String getName() {
                return name;
            }

            @Override
            public void start(EventLoopGroup eventLoopGroup) {
            }

            @Override
            public void stop() {
            }

            @Override
            public void parse(final ByteBuffer buffer,
                              final InetSocketAddress remoteAddress,
                              final InetSocketAddress localAddress) throws Exception {
                LOG.trace("Got packet from: {}", remoteAddress);

                // Build the message to dispatch
                final TelemetryMessage msg = new TelemetryMessage(remoteAddress, buffer);

                // Dispatch and retain a reference to the packet
                // in the case that we are sharing the underlying byte array
                final CompletableFuture<TelemetryMessage> future = dispatcher.send(msg);

                // Pass exception if dispatching fails
                // FIXME: fooker - use futures everywhere
//        future.handle((result, ex) -> {
//            if (ex != null) {
//                ctx.fireExceptionCaught(ex);
//            }
//            return result;
//        });
            }
        };
    }
}
