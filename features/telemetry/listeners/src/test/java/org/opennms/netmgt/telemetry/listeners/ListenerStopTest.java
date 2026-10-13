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

import static org.junit.Assert.assertTrue;

import java.net.InetSocketAddress;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

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
}
