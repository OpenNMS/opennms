/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018-2018 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.protocols.netflow.parser;

import static org.opennms.netmgt.telemetry.listeners.utils.BufferUtils.slice;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.distributed.core.api.Identity;
import org.opennms.netmgt.dnsresolver.api.DnsResolver;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.telemetry.listeners.Dispatchable;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.listeners.utils.BufferUtils;
import org.opennms.netmgt.telemetry.listeners.UdpParser;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow5.proto.Header;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow5.proto.Packet;

import com.codahale.metrics.MetricRegistry;

import io.netty.buffer.ByteBuf;

public class Netflow5UdpParser extends ParserBase implements UdpParser, Dispatchable {

    public Netflow5UdpParser(final String name,
                             final AsyncDispatcher<TelemetryMessage> dispatcher,
                             final EventForwarder eventForwarder,
                             final Identity identity,
                             final DnsResolver dnsResolver,
                             final MetricRegistry metricRegistry) {
        super(Protocol.NETFLOW5, name, dispatcher, eventForwarder, identity, dnsResolver, metricRegistry);
    }

    @Override
    public boolean handles(final ByteBuf buffer) {
        return BufferUtils.uint16(buffer) == 0x0005;
    }

    @Override
    public CompletableFuture<?> parse(final ByteBuf buffer,
                                      final InetSocketAddress remoteAddress,
                                      final InetSocketAddress localAddress) throws Exception {
        final Header header = new Header(slice(buffer, Header.SIZE));
        final Packet packet = new Packet(header, buffer);

        detectClockSkew(header.unixSecs * 1000L + header.unixNSecs / 1000L, remoteAddress.getAddress());

        return this.transmit(packet, remoteAddress);
    }

}
