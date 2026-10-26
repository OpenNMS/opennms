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
package org.opennms.netmgt.telemetry.listeners;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.Test;

import com.codahale.metrics.MetricRegistry;

import io.netty.buffer.ByteBuf;

/**
 * Verifies that the listeners stop without a Netty quiet period and that the
 * channel close does not run into an already terminated event loop.
 */
public class ListenerStopTest {

    // Netty's default quiet period is 2000 ms per group. A stop must finish well below that.
    private static final long MAX_STOP_MS = 1500;

    @Test
    public void udpListenerStopsPromptly() throws Exception {
        final UdpListener listener = new UdpListener("udp-stop-test", Collections.singletonList(new StubUdpParser()), new MetricRegistry());
        listener.setPort(0);
        listener.start();

        assertStopsPromptly(listener::stop, listener::getShutdownFuture);
    }

    @Test
    public void tcpListenerStopsPromptly() throws Exception {
        final TcpListener listener = new TcpListener("tcp-stop-test", new StubTcpParser(), new MetricRegistry());
        listener.setPort(0);
        listener.start();

        assertStopsPromptly(listener::stop, listener::getShutdownFuture);
    }

    /** The clients must be disconnected before the parser stops, or their records are lost. */
    @Test
    public void tcpListenerDisconnectsClientsBeforeParserStops() throws Exception {
        final int port = freePort();
        final CountDownLatch accepted = new CountDownLatch(1);
        final AtomicReference<Socket> client = new AtomicReference<>();
        final AtomicBoolean clientClosedBeforeParserStop = new AtomicBoolean(false);
        final StubTcpParser parser = new StubTcpParser() {
            @Override
            public Handler accept(final InetSocketAddress remoteAddress, final InetSocketAddress localAddress) {
                accepted.countDown();
                return super.accept(remoteAddress, localAddress);
            }

            @Override
            public void stop() {
                super.stop();
                try {
                    client.get().setSoTimeout(2000);
                    clientClosedBeforeParserStop.set(client.get().getInputStream().read() == -1);
                } catch (final IOException e) {
                    // read timeout: the connection is still open
                }
            }
        };
        final TcpListener listener = new TcpListener("tcp-clients-test", parser, new MetricRegistry());
        listener.setPort(port);
        listener.start();

        try (Socket socket = new Socket("127.0.0.1", port)) {
            client.set(socket);
            assertTrue("the listener must accept the connection", accepted.await(5, TimeUnit.SECONDS));

            listener.stop();
        }

        assertTrue("the client must see the connection closed before the parser stops", clientClosedBeforeParserStop.get());
    }

    /** Each parser may wait for its records on stop. The listener must not add up these waits. */
    @Test
    public void udpListenerStopsParsersInParallel() throws Exception {
        final long stopMs = 1000;
        final AtomicInteger stopped = new AtomicInteger();
        final List<UdpParser> parsers = Arrays.asList(
                new SlowStubUdpParser(stopMs, stopped),
                new SlowStubUdpParser(stopMs, stopped),
                new SlowStubUdpParser(stopMs, stopped),
                new SlowStubUdpParser(stopMs, stopped));
        final UdpListener listener = new UdpListener("udp-parallel-test", parsers, new MetricRegistry());
        listener.setPort(0);
        listener.start();

        final long start = System.nanoTime();
        listener.stop();
        final long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        assertEquals("all parsers must be stopped", parsers.size(), stopped.get());
        assertTrue("stop took " + elapsedMs + " ms, sequential would take " + (parsers.size() * stopMs) + " ms",
                elapsedMs < (parsers.size() - 1) * stopMs);
    }

    private static int freePort() throws IOException {
        try (ServerSocket socket = new ServerSocket(0)) {
            return socket.getLocalPort();
        }
    }

    private interface Stop {
        void run() throws InterruptedException;
    }

    private interface ShutdownFuture {
        Future<?> get();
    }

    private static void assertStopsPromptly(final Stop stop, final ShutdownFuture shutdownFuture) throws InterruptedException {
        final long start = System.nanoTime();
        stop.run();
        final long deadline = start + TimeUnit.MILLISECONDS.toNanos(MAX_STOP_MS);
        while (!shutdownFuture.get().isDone() && System.nanoTime() < deadline) {
            Thread.sleep(10);
        }
        final long elapsedMs = (System.nanoTime() - start) / 1_000_000;
        assertTrue("shutdown future not done after " + elapsedMs + " ms", shutdownFuture.get().isDone());
        assertTrue("stop took " + elapsedMs + " ms", elapsedMs < MAX_STOP_MS);
    }

    /** Schedules a periodic task on the event loop group like the real parsers do. */
    private static abstract class StubParser {
        private ScheduledFuture<?> housekeeping;

        public String getName() {
            return "stub";
        }

        public String getDescription() {
            return "stub";
        }

        public Object dumpInternalState() {
            return null;
        }

        public void start(final ScheduledExecutorService executorService) {
            housekeeping = executorService.scheduleAtFixedRate(() -> { }, 1, 1, TimeUnit.SECONDS);
        }

        public void stop() {
            housekeeping.cancel(false);
        }
    }

    private static class StubUdpParser extends StubParser implements UdpParser {
        @Override
        public CompletableFuture<?> parse(final ByteBuf buffer, final InetSocketAddress remoteAddress, final InetSocketAddress localAddress) {
            return CompletableFuture.completedFuture(null);
        }
    }

    private static class StubTcpParser extends StubParser implements TcpParser {
        @Override
        public Handler accept(final InetSocketAddress remoteAddress, final InetSocketAddress localAddress) {
            return new Handler() {
                @Override
                public void active() { }

                @Override
                public void inactive() { }

                @Override
                public Optional<CompletableFuture<?>> parse(final ByteBuf buffer) {
                    return Optional.empty();
                }
            };
        }
    }

    /** Takes a fixed time to stop, like a parser that waits for its records. */
    private static class SlowStubUdpParser extends StubUdpParser implements Dispatchable {
        private final long stopMs;
        private final AtomicInteger stopped;

        SlowStubUdpParser(final long stopMs, final AtomicInteger stopped) {
            this.stopMs = stopMs;
            this.stopped = stopped;
        }

        @Override
        public boolean handles(final ByteBuf buffer) {
            return true;
        }

        @Override
        public void stop() {
            super.stop();
            try {
                Thread.sleep(stopMs);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            stopped.incrementAndGet();
        }
    }
}
