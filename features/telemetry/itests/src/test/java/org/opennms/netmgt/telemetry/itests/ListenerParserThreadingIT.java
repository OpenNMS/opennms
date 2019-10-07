/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.telemetry.itests;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.distributed.core.api.Identity;
import org.opennms.netmgt.dnsresolver.api.DnsResolver;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.listeners.UdpListener;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.Netflow5UdpParser;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow5.proto.Header;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow5.proto.Record;
import org.opennms.test.ThreadLocker;
import org.springframework.util.SocketUtils;

import com.codahale.metrics.MetricRegistry;

public class ListenerParserThreadingIT implements AsyncDispatcher<TelemetryMessage> {

    private final AtomicInteger messagesSent = new AtomicInteger();

    /**
     * This test is used to validate that we can process many packets in parallel.
     *
     * We do this by blocking the reverse DNS lookup calls and verifying that the expected
     * number of threads are locked.
     *
     * @throws Exception on error
     */
    @Test
    public void canProcessManyPacketsInParallel() throws Exception {
        final int NUM_THREADS = 100;

        EventForwarder eventForwarder = mock(EventForwarder.class);
        Identity identity = mock(Identity.class);

        ThreadLocker threadLocker = new ThreadLocker();
        CompletableFuture<Integer> future = threadLocker.waitForThreads(NUM_THREADS);
        DnsResolver dnsResolver = new DnsResolver() {
            @Override
            public CompletableFuture<Optional<InetAddress>> lookup(String hostname) {
                threadLocker.park();
                return CompletableFuture.completedFuture(Optional.empty());
            }

            @Override
            public CompletableFuture<Optional<String>> reverseLookup(InetAddress inetAddress) {
                threadLocker.park();
                return CompletableFuture.completedFuture(Optional.empty());
            }
        };

        int udpPort = SocketUtils.findAvailableUdpPort();
        Netflow5UdpParser parser = new Netflow5UdpParser("FLOW", this, eventForwarder, identity, dnsResolver, new MetricRegistry());
        parser.setThreads(NUM_THREADS);
        UdpListener listener = new UdpListener("FLOW", Collections.singletonList(parser), new MetricRegistry());
        listener.setPort(udpPort);
        listener.start();

        sendNetflow5Packets(udpPort, NUM_THREADS * 2);

        // Wait until the expected number of threads are locked
        future.get(1, TimeUnit.MINUTES);
        // Wait a little longer and make sure that no extra threads are locked too
        Thread.sleep(500);
        assertThat(threadLocker.getNumExtraThreadsWaiting(), equalTo(0));
        // Open the gates
        threadLocker.release();
    }

    private void sendNetflow5Packets(int udpPort, int numPackets) throws IOException {
        // Generate minimal Netflow v5 packet with 1 record
        byte[] bytes = new byte[Header.SIZE + Record.SIZE];
        Arrays.fill(bytes, (byte) 0xFF);
        bytes[0] = 0x00;
        bytes[1] = 0x05;
        bytes[2] = 0x00;
        bytes[3] = 0x01;

        System.out.printf("Sending %d packets to localhost:%d\n", numPackets, udpPort);
        try (DatagramSocket socket = new DatagramSocket()) {
            for (int i = 0; i < numPackets; i++) {
                final DatagramPacket packet = new DatagramPacket(bytes, bytes.length, InetAddress.getLocalHost(), udpPort);
                socket.send(packet);
            }
        }
    }

    @Override
    public CompletableFuture<TelemetryMessage> send(TelemetryMessage message) {
        messagesSent.incrementAndGet();
        return CompletableFuture.completedFuture(message);
    }

    @Override
    public int getQueueSize() {
        return 0;
    }

    @Override
    public void close()  {
        // pass
    }
}