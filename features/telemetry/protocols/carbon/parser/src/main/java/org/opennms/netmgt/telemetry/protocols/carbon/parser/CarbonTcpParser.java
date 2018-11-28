/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.carbon.parser;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.listeners.TcpParser;

import com.google.common.collect.Lists;

public class CarbonTcpParser implements TcpParser {

    private final String name;

    private final AsyncDispatcher<TelemetryMessage> dispatcher;

    public CarbonTcpParser(final String name,
                           final AsyncDispatcher<TelemetryMessage> dispatcher) {
        this.name = Objects.requireNonNull(name);
        this.dispatcher = Objects.requireNonNull(dispatcher);
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public void start(final ScheduledExecutorService executorService) {
    }

    @Override
    public void stop() {
    }

    @Override
    public Handler accept(final InetSocketAddress remoteAddress,
                          final InetSocketAddress localAddress) {
        return new Handler() {
            private final ByteBuffer line = ByteBuffer.allocate(1024);

            @Override
            public CompletableFuture<?> parse(final ByteBuffer buffer) throws Exception {
                final List<CompletableFuture<?>> results = Lists.newArrayList();

                for (int i = 0; i < buffer.remaining(); i++) {
                    final byte b = buffer.get(i);
                    if (b == '\n') {
                        final TelemetryMessage msg = new TelemetryMessage(remoteAddress, this.line);
                        results.add(dispatcher.send(msg));

                        this.line.clear();
                    } else {
                        this.line.put(b);
                    }
                }

                return CompletableFuture.allOf(results.toArray(new CompletableFuture[0]));
            }
        };
    }
}
