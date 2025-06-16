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
package org.opennms.netmgt.telemetry.protocols.netflow.parser;

import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.opennms.core.ipc.sink.api.AsyncDispatcher;
import org.opennms.distributed.core.api.Identity;
import org.opennms.netmgt.dnsresolver.api.DnsResolver;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.telemetry.api.receiver.TelemetryMessage;
import org.opennms.netmgt.telemetry.listeners.UdpListener;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Log;
import org.springframework.util.SocketUtils;

import com.codahale.metrics.MetricRegistry;

public class IllegalFlowTest {
    private final static Path FOLDER = Paths.get("src/test/resources/flows");
    private final AtomicInteger messagesSent = new AtomicInteger();
    private final AtomicInteger eventCount = new AtomicInteger();

    @BeforeClass
    public static void beforeClass() {
        System.setProperty("karaf.etc", "src/test/resources");
    }

    @Test
    public void testEventsForIllegalFlows() throws Exception {

        final EventForwarder eventForwarder = new EventForwarder() {
            @Override
            public void sendNow(Event event) {
                System.out.println("Sending event: " + event);
                eventCount.incrementAndGet();
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
        await().pollDelay(250, TimeUnit.MILLISECONDS).atMost(2, TimeUnit.SECONDS).until(() -> eventCount.get(), is(0));
        await().pollDelay(250, TimeUnit.MILLISECONDS).atMost(2, TimeUnit.SECONDS).until(() -> messagesSent.get(), is(5));
        sendIllegal(udpPort);
        await().pollDelay(250, TimeUnit.MILLISECONDS).atMost(2, TimeUnit.SECONDS).until(() -> eventCount.get(), is(1));
        await().pollDelay(250, TimeUnit.MILLISECONDS).atMost(2, TimeUnit.SECONDS).until(() -> messagesSent.get(), is(10));
        sendIllegal(udpPort);
        await().pollDelay(250, TimeUnit.MILLISECONDS).atMost(2, TimeUnit.SECONDS).until(() -> eventCount.get(), is(1));
        await().pollDelay(250, TimeUnit.MILLISECONDS).atMost(2, TimeUnit.SECONDS).until(() -> messagesSent.get(), is(15));

        // reset counter

        eventCount.set(0);
        messagesSent.set(0);

        // check that event is delivered again after delay

        parser.setIllegalFlowEventRate(2);
        sendValid(udpPort);
        await().pollDelay(250, TimeUnit.MILLISECONDS).atMost(2, TimeUnit.SECONDS).until(() -> eventCount.get(), is(0));
        await().pollDelay(250, TimeUnit.MILLISECONDS).atMost(2, TimeUnit.SECONDS).until(() -> messagesSent.get(), is(5));

        sendIllegal(udpPort);
        await().pollDelay(250, TimeUnit.MILLISECONDS).atMost(2, TimeUnit.SECONDS).until(() -> eventCount.get(), is(1));
        await().pollDelay(250, TimeUnit.MILLISECONDS).atMost(2, TimeUnit.SECONDS).until(() -> messagesSent.get(), is(10));

        sendIllegal(udpPort);
        await().pollDelay(250, TimeUnit.MILLISECONDS).atMost(2, TimeUnit.SECONDS).until(() -> eventCount.get(), is(1));
        await().pollDelay(250, TimeUnit.MILLISECONDS).atMost(2, TimeUnit.SECONDS).until(() -> messagesSent.get(), is(15));

        sendIllegal(udpPort);
        await().pollDelay(250, TimeUnit.MILLISECONDS).atMost(2, TimeUnit.SECONDS).until(() -> eventCount.get(), is(1));
        await().pollDelay(250, TimeUnit.MILLISECONDS).atMost(2, TimeUnit.SECONDS).until(() -> messagesSent.get(), is(20));

        Thread.sleep(2000);

        sendIllegal(udpPort);
        await().pollDelay(250, TimeUnit.MILLISECONDS).atMost(2, TimeUnit.SECONDS).until(() -> eventCount.get(), is(2));
        await().pollDelay(250, TimeUnit.MILLISECONDS).atMost(2, TimeUnit.SECONDS).until(() -> messagesSent.get(), is(25));
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
