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

package org.opennms.netmgt.telemetry.protocols.netflow.parser;

import static com.jayway.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.distributed.core.api.Identity;
import org.opennms.netmgt.dnsresolver.api.DnsResolver;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.listeners.UdpListener;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Value;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.values.UnsignedValue;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.Netflow9MessageBuilder;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.springframework.util.SocketUtils;

import com.codahale.metrics.MetricRegistry;

public class IllegalFlowTest {
    private final static Path FOLDER = Paths.get("src/test/resources/flows");
    private final AtomicInteger messagesSent = new AtomicInteger();
    private int eventCount = 0;

    @Test(expected = IllegalFlowException.class)
    public void illegalFlowTest() throws Exception {
        final RecordEnrichment enrichment = (address -> Optional.empty());
        final List<Value<?>> record = new ArrayList<>();
        record.add(new UnsignedValue("@unixSecs", 1000));
        record.add(new UnsignedValue("@sysUpTime", 1000));
        // first > last, this should trigger an exception
        record.add(new UnsignedValue("FIRST_SWITCHED", 3000));
        record.add(new UnsignedValue("LAST_SWITCHED", 2000));
        final Netflow9MessageBuilder builder = new Netflow9MessageBuilder(record, enrichment);
        builder.buildData();
    }

    @Test
    public void validFlowTest() throws Exception {
        final RecordEnrichment enrichment = (address -> Optional.empty());
        final List<Value<?>> record = new ArrayList<>();
        record.add(new UnsignedValue("@unixSecs", 1000));
        record.add(new UnsignedValue("@sysUpTime", 1000));
        // first < last, this is valid
        record.add(new UnsignedValue("FIRST_SWITCHED", 2000));
        record.add(new UnsignedValue("LAST_SWITCHED", 3000));
        final Netflow9MessageBuilder builder = new Netflow9MessageBuilder(record, enrichment);
        builder.buildData();
    }

    @Test
    public void testEventsForIllegalFlows() throws Exception {

        final EventForwarder eventForwarder = new EventForwarder() {
            @Override
            public void sendNow(Event event) {
                System.out.println("Sending event: " + event);
                eventCount++;
            }

            @Override
            public void sendNow(Log eventLog) {
                Assert.fail();
            }

            @Override
            public void sendNowSync(Event event) {
                Assert.fail();
            }

            @Override
            public void sendNowSync(Log eventLog) {
                Assert.fail();
            }
        };

        final Identity identity = mock(Identity.class);

        final DnsResolver dnsResolver = new DnsResolver() {
            @Override
            public CompletableFuture<Optional<InetAddress>> lookup(String hostname) {
                return CompletableFuture.completedFuture(Optional.empty());
            }

            @Override
            public CompletableFuture<Optional<String>> reverseLookup(InetAddress inetAddress) {
                return CompletableFuture.completedFuture(Optional.empty());
            }
        };

        final int udpPort = SocketUtils.findAvailableUdpPort();

        // setting up nf9 parser

        final Netflow9UdpParser parser = new Netflow9UdpParser("FLOW", new AsyncDispatcher<TelemetryMessage>() {
            @Override
            public CompletableFuture<DispatchStatus> send(TelemetryMessage message) {
                messagesSent.incrementAndGet();
                return CompletableFuture.completedFuture(DispatchStatus.DISPATCHED);
            }

            @Override
            public int getQueueSize() {
                return 0;
            }

            @Override
            public void close()  {
            }
        }, eventForwarder, identity, dnsResolver, new MetricRegistry());

        // setting up listener

        final UdpListener listener = new UdpListener("FLOW", Collections.singletonList(parser), new MetricRegistry());

        listener.setPort(udpPort);
        listener.start();

        // send template

        sendTemplate(udpPort);

        // check that event is delivered only once

        parser.setIllegalFlowEventRate(3600);
        sendValid(udpPort);
        Assert.assertEquals(0, eventCount);
        sendIllegal(udpPort);
        Assert.assertEquals(1, eventCount);
        sendIllegal(udpPort);
        Assert.assertEquals(1, eventCount);

        // reset counter

        eventCount = 0;

        // check that event is delivered again after delay

        parser.setIllegalFlowEventRate(1);
        sendValid(udpPort);
        Assert.assertEquals(0, eventCount);

        sendIllegal(udpPort);
        Assert.assertEquals(1, eventCount);

        sendIllegal(udpPort);
        Assert.assertEquals(1, eventCount);

        sendIllegal(udpPort);
        Assert.assertEquals(1, eventCount);

        Thread.sleep(1000);

        sendIllegal(udpPort);
        Assert.assertEquals(2, eventCount);
    }

    private void sendTemplate(final int udpPort) throws Exception {
        sendPacket("nf9_template.dat", udpPort, false);
    }

    private void sendIllegal(final int udpPort) throws Exception {
        sendPacket("nf9_illegal.dat", udpPort, true);
    }

    private void sendValid(final int udpPort) throws Exception {
        sendPacket("nf9_valid.dat", udpPort, true);
    }

    private void sendPacket(final String file, final int udpPort, final boolean wait) throws Exception {
        try (final FileChannel channel = FileChannel.open(FOLDER.resolve(file))) {
            final ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
            channel.read(buffer);
            buffer.flip();

            int i = messagesSent.get();

            try (final DatagramSocket socket = new DatagramSocket()) {
                final DatagramPacket packet = new DatagramPacket(
                        buffer.array(),
                        buffer.array().length,
                        InetAddress.getLocalHost(),
                        udpPort
                );

                socket.send(packet);

                if (wait) {
                    await().atMost(1, TimeUnit.SECONDS)
                            .pollDelay(50, TimeUnit.MILLISECONDS)
                            .until(() -> messagesSent.get() > i);
                }
            }
        }
    }
}
