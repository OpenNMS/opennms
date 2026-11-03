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
package org.opennms.features.openconfig.telemetry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.awaitility.Awaitility.await;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.After;
import org.junit.Test;
import org.opennms.features.openconfig.api.OpenConfigClient;
import org.opennms.features.openconfig.proto.gnmi.Gnmi;

import io.grpc.CallOptions;
import io.grpc.ClientCall;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;
import io.grpc.Status;

public class OpenConfigClientLifecycleTest {
    private final ScheduledThreadPoolExecutor executor = OpenConfigClientImpl.newExecutor(1);
    private final BlockingQueue<TestChannel> channels = new LinkedBlockingQueue<>();
    private final AtomicInteger attempts = new AtomicInteger();
    private final AtomicInteger errors = new AtomicInteger();
    private final List<TestChannel> createdChannels = new CopyOnWriteArrayList<>();
    private final AtomicInteger peakUnclosedChannels = new AtomicInteger();
    private OpenConfigClientImpl client;

    @After
    public void cleanup() throws Exception {
        if (client != null) {
            client.shutdown();
        }
        executor.shutdownNow();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));
    }

    private void startClient(int retries, int interval, long timeoutMillis) throws Exception {
        startClient(retries, interval, timeoutMillis, false);
    }

    private void startClient(int retries, int interval, long timeoutMillis, boolean failReadiness) throws Exception {
        startClient(retries, interval, timeoutMillis, failReadiness, new OpenConfigClient.Handler() {
            @Override
            public void accept(InetAddress host, Integer port, byte[] data) { }

            @Override
            public void onError(String error) {
                errors.incrementAndGet();
            }
        });
    }

    private void startClient(int retries, int interval, long timeoutMillis, boolean failReadiness,
                             OpenConfigClient.Handler handler) throws Exception {
        client = new OpenConfigClientImpl(InetAddress.getByName("127.0.0.1"),
                List.of(Map.of("port", "9339", "retries", Integer.toString(retries),
                        "interval", Integer.toString(interval))),
                executor, false, ignored -> { }, timeoutMillis) {
            @Override
            ManagedChannel createChannel() {
                TestChannel channel = new TestChannel(failReadiness);
                createdChannels.add(channel);
                int unclosed = (int) createdChannels.stream().filter(c -> !c.isShutdown()).count();
                peakUnclosedChannels.accumulateAndGet(unclosed, Math::max);
                attempts.incrementAndGet();
                channels.add(channel);
                return channel;
            }
        };
        client.subscribe(handler);
    }

    private TestChannel nextChannel() throws Exception {
        TestChannel channel = channels.poll(5, TimeUnit.SECONDS);
        assertNotNull("Expected a connection attempt", channel);
        assertTrue("Connection was not requested", channel.connectRequested.await(5, TimeUnit.SECONDS));
        return channel;
    }

    // Asserts a condition keeps holding for the whole window, for "nothing else happens" checks.
    private static void assertHoldsFor(Duration window, Callable<Boolean> condition) {
        await().during(window).atMost(window.plusSeconds(2)).until(condition);
    }

    @Test(timeout = 15000)
    public void subscribesWhenConnectionBecomesReadyWithoutBlockingCaller() throws Exception {
        startClient(1, 0, 5000);
        TestChannel channel = nextChannel();
        assertEquals("Must wait for readiness", 1, channel.subscribed.getCount());
        channel.setState(ConnectivityState.CONNECTING);
        assertEquals(1, channel.subscribed.getCount());
        channel.setState(ConnectivityState.READY);
        assertTrue(channel.subscribed.await(5, TimeUnit.SECONDS));
        assertFalse(channel.isShutdown());
    }

    @Test(timeout = 15000)
    public void closesTimedOutChannelsAndHonorsRetryLimit() throws Exception {
        startClient(2, 0, 50);
        // Initial attempt, immediate recovery, then two configured retries.
        for (int i = 0; i < 4; i++) {
            TestChannel channel = nextChannel();
            assertTrue("Timed-out channel was not closed", channel.closed.await(5, TimeUnit.SECONDS));
        }
        // Budget exhausted: no further attempt across a full retry interval.
        assertHoldsFor(Duration.ofMillis(1200), () -> attempts.get() == 4);
        assertEveryAttemptWasClosedBeforeReplacement();
    }

    @Test(timeout = 15000)
    public void closesAllocatedChannelsWhenReadinessCheckThrows() throws Exception {
        startClient(2, 0, 5000, true);
        for (int i = 0; i < 4; i++) {
            assertTrue("Channel allocated before exception was not closed",
                    nextChannel().closed.await(5, TimeUnit.SECONDS));
        }
        assertHoldsFor(Duration.ofMillis(1200), () -> attempts.get() == 4);
        assertEveryAttemptWasClosedBeforeReplacement();
    }

    private void assertEveryAttemptWasClosedBeforeReplacement() {
        assertEquals("A retry allocated a channel before closing its predecessor", 1, peakUnclosedChannels.get());
        assertTrue("The final failed attempt also needs cleanup",
                createdChannels.stream().allMatch(ManagedChannel::isShutdown));
    }

    @Test(timeout = 15000)
    public void shutdownDuringConnectionIgnoresLateReadiness() throws Exception {
        startClient(1, 0, 5000);
        TestChannel channel = nextChannel();
        client.shutdown();
        channel.setState(ConnectivityState.READY);
        assertTrue(channel.isShutdown());
        assertHoldsFor(Duration.ofMillis(200), () -> channel.subscribed.getCount() == 1 && attempts.get() == 1);
    }

    @Test(timeout = 15000)
    public void shutdownCancelsPendingRetry() throws Exception {
        startClient(1, 1, 50);
        assertTrue(nextChannel().closed.await(5, TimeUnit.SECONDS));
        assertTrue(nextChannel().closed.await(5, TimeUnit.SECONDS));
        client.shutdown();
        assertHoldsFor(Duration.ofMillis(1200), () -> attempts.get() == 2);
    }

    @Test(timeout = 15000)
    public void staleStreamFailureCannotCloseReplacementChannel() throws Exception {
        startClient(1, 0, 5000);
        TestChannel first = nextChannel();
        first.setState(ConnectivityState.READY);
        assertTrue(first.subscribed.await(5, TimeUnit.SECONDS));
        first.failStream();
        TestChannel second = nextChannel();
        second.setState(ConnectivityState.READY);
        assertTrue(second.subscribed.await(5, TimeUnit.SECONDS));
        first.failStream();
        assertTrue(first.isShutdown());
        assertHoldsFor(Duration.ofMillis(200), () -> !second.isShutdown() && attempts.get() == 2 && errors.get() == 1);
    }

    @Test(timeout = 15000)
    public void streamThatDeliveredDataIsReconnectedImmediatelyWithFreshBudget() throws Exception {
        startClient(1, 1, 5000);
        TestChannel first = nextChannel();
        first.setState(ConnectivityState.READY);
        assertTrue(first.subscribed.await(5, TimeUnit.SECONDS));
        first.deliver(Gnmi.SubscribeResponse.getDefaultInstance());
        first.failStream();
        TestChannel second = nextChannel();
        assertTrue("Established stream was not reconnected immediately", second.connectRequested.await(1, TimeUnit.SECONDS));
        assertEquals(2, attempts.get());
        second.setState(ConnectivityState.READY);
        assertTrue(second.subscribed.await(5, TimeUnit.SECONDS));
        second.failStream();
        await("Rejected reconnect should still get the configured retry")
                .atMost(3, TimeUnit.SECONDS).until(() -> attempts.get() == 3);
    }

    @Test(timeout = 15000)
    public void zeroIntervalWithUnlimitedRetriesDoesNotSpin() throws Exception {
        startClient(0, 0, 50);
        // Initial attempt and its immediate recovery, then interval-paced retries.
        assertTrue(nextChannel().closed.await(5, TimeUnit.SECONDS));
        assertTrue(nextChannel().closed.await(5, TimeUnit.SECONDS));
        // The third attempt is due one second after the second failed; nothing may run before then.
        assertHoldsFor(Duration.ofMillis(600), () -> attempts.get() == 2);
    }

    @Test(timeout = 15000)
    public void missingPortFailsOnceWithoutRetrying() throws Exception {
        client = new OpenConfigClientImpl(InetAddress.getByName("127.0.0.1"),
                List.of(Map.of("retries", "0", "interval", "1")), executor, false, ignored -> { }, 50) {
            @Override
            ManagedChannel createChannel() {
                attempts.incrementAndGet();
                return new TestChannel(false);
            }
        };
        client.subscribe(new OpenConfigClient.Handler() {
            @Override
            public void accept(InetAddress host, Integer port, byte[] data) { }

            @Override
            public void onError(String error) { }
        });
        assertHoldsFor(Duration.ofMillis(300), () -> attempts.get() == 0);
        assertFalse(client.isClosed());
        client.shutdown();
        assertTrue(client.isClosed());
    }

    @Test(timeout = 15000)
    public void defaultRetriesIsUnlimited() throws Exception {
        client = new OpenConfigClientImpl(InetAddress.getByName("127.0.0.1"),
                List.of(Map.of("port", "9339", "interval", "1")), executor, false, ignored -> { }, 25) {
            @Override
            ManagedChannel createChannel() {
                TestChannel channel = new TestChannel(false);
                attempts.incrementAndGet();
                channels.add(channel);
                return channel;
            }
        };
        client.subscribe(new OpenConfigClient.Handler() {
            @Override
            public void accept(InetAddress host, Integer port, byte[] data) { }

            @Override
            public void onError(String error) { }
        });
        // Initial, immediate recovery, then interval-paced retries that never stop without a retries parameter.
        for (int i = 0; i < 4; i++) {
            assertTrue(nextChannel().closed.await(5, TimeUnit.SECONDS));
        }
        assertTrue(attempts.get() >= 4);
    }


    // Control connection state and callbacks without mocking generated gRPC stubs.
    private static class TestChannel extends ManagedChannel {
        private final CountDownLatch connectRequested = new CountDownLatch(1);
        private final CountDownLatch subscribed = new CountDownLatch(1);
        private final CountDownLatch closed = new CountDownLatch(1);
        private final List<Runnable> stateCallbacks = new ArrayList<>();
        private ConnectivityState state = ConnectivityState.IDLE;
        private volatile ClientCall.Listener<?> streamListener;
        private final boolean failReadiness;

        private TestChannel(boolean failReadiness) {
            this.failReadiness = failReadiness;
        }

        @Override
        public synchronized ConnectivityState getState(boolean requestConnection) {
            if (requestConnection) {
                connectRequested.countDown();
            }
            if (failReadiness) {
                throw new IllegalStateException("Test failure after channel allocation");
            }
            return state;
        }

        @Override
        public void notifyWhenStateChanged(ConnectivityState source, Runnable callback) {
            synchronized (this) {
                if (state == source) {
                    stateCallbacks.add(callback);
                    return;
                }
            }
            callback.run();
        }

        private void setState(ConnectivityState next) {
            List<Runnable> callbacks;
            synchronized (this) {
                state = next;
                callbacks = new ArrayList<>(stateCallbacks);
                stateCallbacks.clear();
            }
            callbacks.forEach(Runnable::run);
        }

        private void failStream() {
            streamListener.onClose(Status.UNAVAILABLE.withDescription("Test disconnect"), new Metadata());
        }

        @SuppressWarnings({"unchecked", "rawtypes"})
        private void deliver(Object message) {
            ((ClientCall.Listener) streamListener).onMessage(message);
        }

        @Override
        public <ReqT, RespT> ClientCall<ReqT, RespT> newCall(MethodDescriptor<ReqT, RespT> method, CallOptions options) {
            assertNull("Connection timeout must not become a stream deadline", options.getDeadline());
            return new ClientCall<ReqT, RespT>() {
                @Override
                public void start(Listener<RespT> listener, Metadata headers) {
                    streamListener = listener;
                }
                @Override
                public void request(int messages) { }
                @Override
                public void cancel(String message, Throwable cause) { }
                @Override
                public void halfClose() { }
                @Override
                public void sendMessage(ReqT message) {
                    subscribed.countDown();
                }
            };
        }

        @Override
        public String authority() {
            return "test";
        }
        @Override
        public ManagedChannel shutdown() {
            closed.countDown();
            setState(ConnectivityState.SHUTDOWN);
            return this;
        }
        @Override
        public ManagedChannel shutdownNow() {
            return shutdown();
        }
        @Override
        public boolean isShutdown() {
            return closed.getCount() == 0;
        }
        @Override
        public boolean isTerminated() {
            return isShutdown();
        }
        @Override
        public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
            return closed.await(timeout, unit);
        }
    }
}
