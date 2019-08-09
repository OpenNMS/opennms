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

package org.opennms.netmgt.telemetry.protocols.sflow.parser;

import static org.opennms.netmgt.telemetry.common.utils.BufferUtils.uint32;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;

import org.bson.BsonBinaryWriter;
import org.bson.io.BasicOutputBuffer;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.netmgt.dnsresolver.api.DnsResolver;
import org.opennms.netmgt.telemetry.api.receiver.Dispatchable;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.listeners.UdpParser;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.DatagramVersion;
import org.opennms.netmgt.telemetry.protocols.sflow.parser.proto.flows.SampleDatagram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SFlowUdpParser implements UdpParser, Dispatchable {

    private static final Logger LOG = LoggerFactory.getLogger(SFlowUdpParser.class);

    private final String name;

    private final AsyncDispatcher<TelemetryMessage> dispatcher;

    private final SampleDatagramEnricher enricher;

    private boolean lookupsEnabled = true;

    public SFlowUdpParser(final String name,
                          final AsyncDispatcher<TelemetryMessage> dispatcher,
                          final DnsResolver dnsResolver) {
        this.name = Objects.requireNonNull(name);
        this.dispatcher = Objects.requireNonNull(dispatcher);
        enricher = new SampleDatagramEnricher(dnsResolver);
    }

    @Override
    public boolean handles(final ByteBuffer buffer) {
        return uint32(buffer) == DatagramVersion.VERSION5.value;
    }

    @Override
    public CompletableFuture<?> parse(final ByteBuffer buffer,
                                      final InetSocketAddress remoteAddress,
                                      final InetSocketAddress localAddress) throws Exception {
        final SampleDatagram packet = new SampleDatagram(buffer);

        LOG.trace("Got packet: {}", packet);

        final CompletableFuture<TelemetryMessage> future = new CompletableFuture<>();
        enricher.enrich(packet, getLookupsEnabled()).whenComplete((enrichment,ex) -> {
            if (ex != null) {
                // Enrichment failed
                future.completeExceptionally(ex);
                return;
            }

            // Serialize
            final BasicOutputBuffer output = new BasicOutputBuffer();
            try (final BsonBinaryWriter bsonWriter = new BsonBinaryWriter(output)) {
                bsonWriter.writeStartDocument();

                bsonWriter.writeName("time");
                bsonWriter.writeInt64(System.currentTimeMillis());

                bsonWriter.writeName("data");
                packet.version.datagram.writeBson(bsonWriter, enrichment);

                bsonWriter.writeEndDocument();
            }

            // Build the message to be sent
            final TelemetryMessage msg = new TelemetryMessage(remoteAddress, output.getByteBuffers().get(0).asNIO());
            dispatcher.send(msg).whenComplete((any, exx) -> {
               if (exx != null) {
                   // Dispatching failed
                   future.completeExceptionally(exx);
               }
               future.complete(any);
            });
        });
        return future;
    }

    public boolean getLookupsEnabled() {
        return lookupsEnabled;
    }

    public void setLookupsEnabled(boolean lookupsEnabled) {
        this.lookupsEnabled = lookupsEnabled;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public void start(final ScheduledExecutorService executorService) {
    }

    @Override
    public void stop() {

    }

}
