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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.awaitility.Awaitility.await;

import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.opennms.features.openconfig.api.OpenConfigClient;
import org.opennms.features.openconfig.proto.gnmi.Gnmi;
import org.opennms.features.openconfig.proto.gnmi.gNMIGrpc;
import org.opennms.features.openconfig.proto.jti.OpenConfigTelemetryGrpc;
import org.opennms.features.openconfig.proto.jti.Telemetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Metadata;
import io.grpc.Server;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.ServerCallStreamObserver;
import io.grpc.stub.StreamObserver;

/**
 * Drives the client against a real in-process gRPC server over plaintext and TLS.
 */
@RunWith(Parameterized.class)
public class OpenConfigClientGrpcTest {
    private static final Logger LOG = LoggerFactory.getLogger(OpenConfigClientGrpcTest.class);
    private static final InetAddress LOCALHOST = InetAddress.getLoopbackAddress();

    @Parameterized.Parameters(name = "TLS={0}")
    public static Object[] transports() {
        return new Object[] { false, true };
    }

    @Parameterized.Parameter
    public boolean tls;

    private final OpenConfigClientFactoryImpl factory = new OpenConfigClientFactoryImpl();
    private final ScheduledThreadPoolExecutor executor = OpenConfigClientImpl.newExecutor(2);
    private final ScheduledExecutorService serverPusher = new ScheduledThreadPoolExecutor(1);
    private final List<OpenConfigClientImpl> directClients = new ArrayList<>();
    private final AtomicReference<String> streamError = new AtomicReference<>();
    private final AtomicInteger errors = new AtomicInteger();

    // Server-side observations.
    private final AtomicInteger subscribeCalls = new AtomicInteger();
    private final List<Long> subscribeTimes = new CopyOnWriteArrayList<>();
    private final AtomicInteger activeStreams = new AtomicInteger();
    private final Map<String, String> lastHeaders = new ConcurrentHashMap<>();
    private volatile Status rejectSubscriptionsWith;
    private volatile long pushEveryMillis;
    private Server server;
    private int port;

    @Before
    public void startServer() throws Exception {
        server = buildServer(0).build().start();
        port = server.getPort();
    }

    @After
    public void stop() throws Exception {
        factory.shutdown();
        new ArrayList<>(directClients).forEach(OpenConfigClientImpl::shutdown);
        executor.shutdownNow();
        serverPusher.shutdownNow();
        stopServer();
    }

    @Test(timeout = 30000)
    public void receivesTelemetryFromHealthyServer() throws Exception {
        CountDownLatch responses = new CountDownLatch(1);
        subscribe(responses);
        assertTrue("No gNMI response", responses.await(5, TimeUnit.SECONDS));
        assertNull("Unexpected stream error", streamError.get());
    }

    @Test(timeout = 30000)
    public void receivesJtiTelemetryFromHealthyServer() throws Exception {
        CountDownLatch responses = new CountDownLatch(1);
        subscribe(responses, "jti");
        assertTrue("No JTI response", responses.await(5, TimeUnit.SECONDS));
        assertNull("Unexpected stream error", streamError.get());
    }

    @Test(timeout = 30000)
    public void stalledConnectionDoesNotDelayHealthyDevices() throws Exception {
        // TCP accepts connections, but never responds to TLS or HTTP/2 negotiation.
        try (ServerSocket stalled = new ServerSocket(0, 1, LOCALHOST)) {
            CountDownLatch responses = new CountDownLatch(1);
            long started = System.nanoTime();
            subscribe(new CountDownLatch(1), "gnmi", stalled.getLocalPort());
            subscribe(responses);
            assertTrue("Healthy device was blocked behind stalled device", responses.await(10, TimeUnit.SECONDS));
            assertTrue("Starting the stalled device blocked the caller",
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started) < 10000);
        }
    }

    @Test(timeout = 30000)
    public void startsHealthyDevicesWithoutOneSecondDelayPerDevice() throws Exception {
        // Warm up class loading and the gRPC server before measuring the batch.
        CountDownLatch warmup = new CountDownLatch(1);
        subscribe(warmup);
        assertTrue("Warmup did not receive telemetry", warmup.await(5, TimeUnit.SECONDS));

        CountDownLatch responses = new CountDownLatch(5);
        long started = System.nanoTime();
        // Twin invokes connector startup sequentially on its subscriber thread.
        for (int i = 0; i < 5; i++) {
            subscribe(responses);
        }
        assertTrue("Not all devices received telemetry", responses.await(10, TimeUnit.SECONDS));
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);
        LOG.info("Five healthy subscriptions completed in {} ms (TLS={})", elapsedMillis, tls);
        assertNull("Unexpected stream error", streamError.get());
        // Well under five serial one-second waits, with headroom for a loaded CI executor.
        assertTrue("Five healthy subscriptions took " + elapsedMillis
                + " ms; connection setup must not impose a one-second delay per device",
                elapsedMillis < 4500);
    }

    @Test(timeout = 30000)
    public void reconnectsAfterServerRestart() throws Exception {
        CountDownLatch first = new CountDownLatch(1);
        OpenConfigClientImpl client = newDirectClient(port, "gnmi", 500, Map.of("interval", "1"));
        client.subscribe(handler(first));
        assertTrue(first.await(5, TimeUnit.SECONDS));

        // Device reboot: the stream dies, the port refuses connections, then the device is back.
        stopServer();
        await("Client did not observe the stream ending").atMost(5, TimeUnit.SECONDS).until(() -> errors.get() >= 1);
        // Baseline before the replacement server exists, so a fast reconnect cannot be counted twice.
        int callsBefore = subscribeCalls.get();
        server = buildServer(port).build().start();

        await("Client did not resubscribe to the restarted server")
                .atMost(10, TimeUnit.SECONDS).until(() -> subscribeCalls.get() > callsBefore);
        awaitActiveStreams(1);
    }

    @Test(timeout = 30000)
    public void rejectedSubscriptionRetriesAtIntervalThenGivesUp() throws Exception {
        // A device that accepts the connection but refuses the RPC must not reconnect in a tight loop.
        rejectSubscriptionsWith = Status.PERMISSION_DENIED.withDescription("bad credentials");
        OpenConfigClientImpl client = newDirectClient(port, "gnmi", 5000, Map.of("interval", "1", "retries", "1"));
        client.subscribe(handler(new CountDownLatch(1)));

        // Initial attempt, its immediate recovery, one interval-paced retry, then give up.
        await().atMost(5, TimeUnit.SECONDS).until(() -> subscribeCalls.get() >= 3);
        long secondToThirdMillis = TimeUnit.NANOSECONDS.toMillis(subscribeTimes.get(2) - subscribeTimes.get(1));
        assertTrue("Retry after a rejected subscription came " + secondToThirdMillis + " ms after the previous"
                + " attempt; it must wait for the configured interval", secondToThirdMillis >= 800);
        // Budget exhausted: no fourth attempt across more than two further intervals.
        await("Rejected subscription was retried past its budget")
                .during(Duration.ofMillis(2500)).atMost(5, TimeUnit.SECONDS).until(() -> subscribeCalls.get() == 3);
        assertEquals(3, errors.get());
        assertTrue(streamError.get().contains("PERMISSION_DENIED"));
        assertEquals("Rejected streams must not linger on the server", 0, activeStreams.get());
    }

    @Test(timeout = 30000)
    public void connectionTimeoutDoesNotLimitStreamLifetime() throws Exception {
        pushEveryMillis = 100;
        CountDownLatch responses = new CountDownLatch(8);
        OpenConfigClientImpl client = newDirectClient(port, "gnmi", 300, Map.of());
        client.subscribe(handler(responses));
        // Eight pushes take well past the 300 ms connection timeout.
        assertTrue("Stream was cut short by the connection timeout", responses.await(10, TimeUnit.SECONDS));
        assertNull(streamError.get());
        assertEquals("Timeout must not have forced a resubscribe", 1, subscribeCalls.get());
    }

    @Test(timeout = 30000)
    public void clientShutdownCancelsServerStream() throws Exception {
        CountDownLatch responses = new CountDownLatch(1);
        OpenConfigClient client = subscribe(responses);
        assertTrue(responses.await(5, TimeUnit.SECONDS));
        assertEquals(1, activeStreams.get());
        client.shutdown();
        awaitActiveStreams(0);
        assertEquals(0, factory.activeClients());
    }

    @Test(timeout = 30000)
    public void factoryShutdownCancelsEveryServerStream() throws Exception {
        CountDownLatch responses = new CountDownLatch(3);
        for (int i = 0; i < 3; i++) {
            subscribe(responses);
        }
        assertTrue(responses.await(5, TimeUnit.SECONDS));
        assertEquals(3, activeStreams.get());
        factory.shutdown();
        awaitActiveStreams(0);
        assertEquals(0, factory.activeClients());
    }

    @Test(timeout = 30000)
    public void blockedHandlerDoesNotStallShutdown() throws Exception {
        // Sink back-pressure parks the handler on the gRPC thread; shutdown must still complete.
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        OpenConfigClient client = subscribe(new OpenConfigClient.Handler() {
            @Override
            public void accept(InetAddress host, Integer port, byte[] data) {
                entered.countDown();
                try {
                    release.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            @Override
            public void onError(String error) { }
        }, "gnmi", port);
        assertTrue(entered.await(5, TimeUnit.SECONDS));
        try {
            client.shutdown();
            awaitActiveStreams(0);
        } finally {
            release.countDown();
        }
    }

    @Test(timeout = 30000)
    public void credentialsAreSentAsRequestMetadata() throws Exception {
        CountDownLatch responses = new CountDownLatch(1);
        OpenConfigClientImpl client = newDirectClient(port, "gnmi", 5000,
                Map.of("username", "operator", "password", "s3cret"));
        client.subscribe(handler(responses));
        assertTrue(responses.await(5, TimeUnit.SECONDS));
        assertEquals("operator", lastHeaders.get("username"));
        assertEquals("s3cret", lastHeaders.get("password"));
    }

    // ---- client helpers ----

    private OpenConfigClient subscribe(CountDownLatch responses) throws Exception {
        return subscribe(responses, "gnmi");
    }

    private OpenConfigClient subscribe(CountDownLatch responses, String mode) throws Exception {
        return subscribe(responses, mode, port);
    }

    private OpenConfigClient subscribe(CountDownLatch responses, String mode, int port) throws Exception {
        return subscribe(handler(responses), mode, port);
    }

    private OpenConfigClient subscribe(OpenConfigClient.Handler handler, String mode, int port) throws Exception {
        OpenConfigClient client = factory.create(LOCALHOST, List.of(params(port, mode, Map.of())));
        client.subscribe(handler);
        return client;
    }

    private OpenConfigClientImpl newDirectClient(int port, String mode, long connectionTimeoutMillis,
                                                 Map<String, String> extra) {
        OpenConfigClientImpl client = new OpenConfigClientImpl(LOCALHOST, List.of(params(port, mode, extra)),
                executor, false, directClients::remove, connectionTimeoutMillis);
        directClients.add(client);
        return client;
    }

    private Map<String, String> params(int port, String mode, Map<String, String> extra) {
        Map<String, String> params = new HashMap<>(Map.of(
                "port", Integer.toString(port),
                "paths", "/interfaces/interface/state/counters",
                "mode", mode,
                "tls.enabled", Boolean.toString(tls),
                "tls.skip.verify", "true"));
        params.putAll(extra);
        return params;
    }

    private OpenConfigClient.Handler handler(CountDownLatch responses) {
        return new OpenConfigClient.Handler() {
            @Override
            public void accept(InetAddress host, Integer port, byte[] data) {
                responses.countDown();
            }

            @Override
            public void onError(String error) {
                errors.incrementAndGet();
                streamError.compareAndSet(null, error);
            }
        };
    }

    private void awaitActiveStreams(int expected) {
        await("Server-side live stream count did not reach " + expected)
                .atMost(5, TimeUnit.SECONDS).until(() -> activeStreams.get() == expected);
    }

    // ---- server ----

    private NettyServerBuilder buildServer(int port) throws Exception {
        NettyServerBuilder builder = NettyServerBuilder.forAddress(new InetSocketAddress(LOCALHOST, port));
        if (tls) {
            try (InputStream cert = getClass().getResourceAsStream("/tls/server.crt");
                 InputStream key = getClass().getResourceAsStream("/tls/server.pem")) {
                builder.useTransportSecurity(cert, key);
            }
        }
        return builder
                .addService(ServerInterceptors.intercept(new GnmiService(), new HeaderCapture()))
                .addService(new JtiService());
    }

    private void stopServer() throws Exception {
        if (server != null) {
            server.shutdownNow();
            assertTrue("Test server did not terminate", server.awaitTermination(5, TimeUnit.SECONDS));
            server = null;
        }
    }

    private class GnmiService extends gNMIGrpc.gNMIImplBase {
        @Override
        public StreamObserver<Gnmi.SubscribeRequest> subscribe(StreamObserver<Gnmi.SubscribeResponse> response) {
            subscribeTimes.add(System.nanoTime());
            subscribeCalls.incrementAndGet();
            ServerCallStreamObserver<Gnmi.SubscribeResponse> call =
                    (ServerCallStreamObserver<Gnmi.SubscribeResponse>) response;
            AtomicBoolean live = new AtomicBoolean(true);
            Runnable ended = () -> {
                if (live.compareAndSet(true, false)) {
                    activeStreams.decrementAndGet();
                }
            };
            activeStreams.incrementAndGet();
            call.setOnCancelHandler(ended);
            Status reject = rejectSubscriptionsWith;
            if (reject != null) {
                ended.run();
                response.onError(reject.asRuntimeException());
                return new StreamObserver<>() {
                    @Override public void onNext(Gnmi.SubscribeRequest request) { }
                    @Override public void onError(Throwable t) { }
                    @Override public void onCompleted() { }
                };
            }
            return new StreamObserver<>() {
                @Override
                public void onNext(Gnmi.SubscribeRequest request) {
                    push(call, live);
                    if (pushEveryMillis > 0) {
                        serverPusher.scheduleAtFixedRate(() -> push(call, live), pushEveryMillis, pushEveryMillis,
                                TimeUnit.MILLISECONDS);
                    }
                }

                @Override
                public void onError(Throwable error) {
                    ended.run();
                }

                @Override
                public void onCompleted() {
                    ended.run();
                    response.onCompleted();
                }
            };
        }

        private void push(ServerCallStreamObserver<Gnmi.SubscribeResponse> call, AtomicBoolean live) {
            if (!live.get() || call.isCancelled()) {
                throw new IllegalStateException("stream ended");   // stops the scheduled pusher
            }
            try {
                call.onNext(Gnmi.SubscribeResponse.newBuilder().setSyncResponse(true).build());
            } catch (StatusRuntimeException | IllegalStateException e) {
                // Raced with cancellation.
            }
        }
    }

    private static class JtiService extends OpenConfigTelemetryGrpc.OpenConfigTelemetryImplBase {
        @Override
        public void telemetrySubscribe(Telemetry.SubscriptionRequest request,
                                       StreamObserver<Telemetry.OpenConfigData> response) {
            response.onNext(Telemetry.OpenConfigData.newBuilder().setSystemId("test-device").build());
        }
    }

    private class HeaderCapture implements ServerInterceptor {
        @Override
        public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
                                                                     ServerCallHandler<ReqT, RespT> next) {
            for (String name : List.of("username", "password")) {
                String value = headers.get(Metadata.Key.of(name, Metadata.ASCII_STRING_MARSHALLER));
                if (value != null) {
                    lastHeaders.put(name, value);
                }
            }
            return next.startCall(call, headers);
        }
    }
}
