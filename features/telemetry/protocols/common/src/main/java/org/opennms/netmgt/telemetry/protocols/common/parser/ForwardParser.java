/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.telemetry.protocols.common.parser;

import java.net.InetSocketAddress;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.listeners.UdpParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;

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
    public String getDescription() {
        return "Forward";
    }

    @Override
    public Object dumpInternalState() {
        return null;
    }

    @Override
    public void start(final ScheduledExecutorService executorService) {
    }

    @Override
    public void stop() {
    }

    @Override
    public CompletableFuture<?> parse(final ByteBuf buffer,
                                      final InetSocketAddress remoteAddress,
                                      final InetSocketAddress localAddress) throws Exception {
        LOG.trace("Got packet from: {}", remoteAddress);

        // Build the message to dispatch
        final TelemetryMessage msg = new TelemetryMessage(remoteAddress, buffer.nioBuffer());

        // Dispatch and retain a reference to the packet
        // in the case that we are sharing the underlying byte array
        return dispatcher.send(msg);
    }
}
