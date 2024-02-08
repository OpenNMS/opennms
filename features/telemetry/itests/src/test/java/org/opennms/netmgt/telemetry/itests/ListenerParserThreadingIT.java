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
import org.opennms.netmgt.telemetry.protocols.netflow.parser.transport.Netflow5MessageBuilder;
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
                return CompletableFuture.completedFuture(Optional.empty());

            }

            @Override
            public CompletableFuture<Optional<String>> reverseLookup(InetAddress inetAddress) {
                return CompletableFuture.completedFuture(Optional.empty());

            }
        };

        int udpPort = SocketUtils.findAvailableUdpPort();
        Netflow5UdpParser parser = new Netflow5UdpParser("FLOW", this, eventForwarder, identity, dnsResolver, new MetricRegistry()) {
            @Override
            public Netflow5MessageBuilder getMessageBuilder() {
                threadLocker.park();
                return super.getMessageBuilder();
            }
        };
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
        // pass
    }
}