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


import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.stub.StreamObserver;
import org.opennms.core.grpc.common.GrpcClientBuilder;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.StringUtils;
import org.opennms.features.openconfig.api.OpenConfigClient;
import org.opennms.features.openconfig.proto.gnmi.Gnmi;
import org.opennms.features.openconfig.proto.gnmi.gNMIGrpc;
import org.opennms.features.openconfig.proto.jti.OpenConfigTelemetryGrpc;
import org.opennms.features.openconfig.proto.jti.Telemetry;
import org.opennms.features.openconfig.proto.jti.Telemetry.OpenConfigData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static io.grpc.ConnectivityState.READY;

/**
 * OpenConfig Client makes a gRPC connection and subscribes to telemetry data for the paths specified.
 * When it fails to make a connection, it attempts to make a connection after given interval.
 * When retries are specified, it bails out after those many attempts.
 * If retries <=0 is specified, it always attempts to connect after the given interval.
 */
public class OpenConfigClientImpl implements OpenConfigClient {

    private static final Logger LOG = LoggerFactory.getLogger(OpenConfigClientImpl.class);
    private static final Pattern STRINGS_IN_SQUARE_BRACKETS = Pattern.compile("\\[(.+?=.+?)\\]");
    // Path separator but exclude in square brackets.
    private static final Pattern PATH_SEPARATOR = Pattern.compile("\\/(?![^\\[]*])");
    private static final int DEFAULT_RETRIES = 0;
    private static final long CONNECTION_TIMEOUT_MILLIS = 5000;
    private static final int DEFAULT_FREQUENCY = 300000; //5min
    private static final long DEFAULT_FREQUENCY_FOR_GNMI = 300L * 1_000_000_000L; // 5 min in ns
    // 5mins in nano seconds
    private static final int DEFAULT_INTERVAL_IN_SEC = 300; //5min
    private static final int MIN_INTERVAL_IN_SEC = 1;
    private static final String PORT = "port";
    private static final String HOSTNAME = "hostname";
    private static final String MODE = "mode";
    private static final String PATHS = "paths";
    private static final String FREQUENCY = "frequency";
    private static final String INTERVAL = "interval";
    private static final String RETRIES = "retries";
    private static final String JTI_MODE = "jti";
    private static final String ORIGIN = "origin";
    private static final String DEFAULT_ORIGIN = "openconfig";
    private static final String USERNAME_FIELD = "username";
    private static final String PASSWORD_FIELD = "password";
    private final InetAddress host;
    private String hostName;
    private Integer port;
    private String mode;
    private int interval = DEFAULT_INTERVAL_IN_SEC;
    private int retries = DEFAULT_RETRIES;
    private final List<Map<String,String>> paramList = new ArrayList<>();
    private final ScheduledExecutorService executor;
    private final boolean ownsExecutor;
    private final Consumer<OpenConfigClientImpl> onShutdown;
    private final long connectionTimeoutMillis;
    // Lifecycle state is guarded by this client, including callbacks from gRPC.
    private boolean closed;
    private Handler handler;
    private Attempt attempt;
    private ScheduledFuture<?> retry;
    private int remainingRetries;

    private static class Attempt {
        private final boolean initial;
        private ManagedChannel channel;
        private ScheduledFuture<?> timeout;
        private boolean subscribed;
        // Set once the stream has delivered data; only then is the retry budget reset.
        private boolean received;

        private Attempt(boolean initial) {
            this.initial = initial;
        }
    }

    public OpenConfigClientImpl(InetAddress host, List<Map<String, String>> paramList) {
        this(host, paramList, newExecutor(1), true, client -> { }, CONNECTION_TIMEOUT_MILLIS);
    }

    OpenConfigClientImpl(InetAddress host, List<Map<String, String>> paramList,
                         ScheduledExecutorService executor, Consumer<OpenConfigClientImpl> onShutdown) {
        this(host, paramList, executor, false, onShutdown, CONNECTION_TIMEOUT_MILLIS);
    }

    OpenConfigClientImpl(InetAddress host, List<Map<String, String>> paramList,
                         ScheduledExecutorService executor, boolean ownsExecutor,
                         Consumer<OpenConfigClientImpl> onShutdown, long connectionTimeoutMillis) {
        this.host = Objects.requireNonNull(host);
        this.executor = Objects.requireNonNull(executor);
        this.ownsExecutor = ownsExecutor;
        this.onShutdown = Objects.requireNonNull(onShutdown);
        this.connectionTimeoutMillis = connectionTimeoutMillis;
        this.paramList.addAll(paramList);
        // Extract port and mode which are global.
        this.paramList.stream().filter(entry -> entry.containsKey(PORT) && entry.get(PORT) != null)
                .findFirst().ifPresent(entry ->
                this.port = StringUtils.parseInt(entry.get(PORT), null));
        this.paramList.stream().filter(entry -> entry.get(MODE) != null)
                .findFirst().ifPresent(entry -> this.mode = entry.get(MODE));
        this.paramList.stream().filter(entry -> entry.containsKey(HOSTNAME) && entry.get(HOSTNAME) != null)
                .findFirst().ifPresent(entry ->
                        this.hostName = entry.get(HOSTNAME));
        this.paramList.stream().filter(entry -> entry.get(INTERVAL) != null)
                .findFirst().ifPresent(entry -> this.interval = StringUtils.parseInt(entry.get(INTERVAL), DEFAULT_INTERVAL_IN_SEC));
        // An interval of zero with unlimited retries would spin on the shared pool.
        this.interval = Math.max(MIN_INTERVAL_IN_SEC, this.interval);
        this.paramList.stream().filter(entry -> entry.get(RETRIES) != null)
                .findFirst().ifPresent(entry -> this.retries = StringUtils.parseInt(entry.get(RETRIES), DEFAULT_RETRIES));
    }

    static ScheduledThreadPoolExecutor newExecutor(int threads) {
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(threads,
                new ThreadFactoryBuilder()
                        .setNameFormat("openconfig-client-%d").build());
        executor.setRemoveOnCancelPolicy(true);
        return executor;
    }

    @Override
    public synchronized void subscribe(OpenConfigClient.Handler handler) {
        Objects.requireNonNull(handler);
        if (closed || this.handler != null) {
            return;
        }
        this.handler = handler;
        if (port == null) {
            // Retrying cannot fix a missing parameter; fail once instead of once per interval.
            LOG.error("OpenConfig Server at `{}` has no port configured, not subscribing", InetAddressUtils.str(host));
            return;
        }
        remainingRetries = retries;
        retry = executor.schedule(() -> startAttempt(true), 0, TimeUnit.SECONDS);
    }

    private synchronized void startAttempt(boolean initial) {
        if (closed) {
            return;
        }
        retry = null;
        Attempt next = new Attempt(initial);
        attempt = next;
        try {
            next.channel = createChannel();
            next.timeout = executor.schedule(() -> connectionTimedOut(next), connectionTimeoutMillis, TimeUnit.MILLISECONDS);
            awaitReady(next);
        } catch (Exception e) {
            LOG.warn("Exception while subscribing to OpenConfig Server at `{}`", InetAddressUtils.str(host), e);
            retry(next);
        }
    }

    ManagedChannel createChannel() throws Exception {
        Map<String, String> tlsFilePaths = new HashMap<>();
        paramList.forEach(entry -> {
            tlsFilePaths.putAll(entry.entrySet().stream()
                    .filter(configuration -> configuration.getKey().contains("tls"))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        });
        String host = this.hostName != null ? this.hostName : this.host.getHostAddress();
        var optionalUsername =
                this.paramList.stream().filter(entry -> entry.get(USERNAME_FIELD) != null).findFirst();
        var optionalPassword =
                this.paramList.stream().filter(entry -> entry.get(PASSWORD_FIELD) != null).findFirst();
        if (optionalUsername.isPresent() && optionalPassword.isPresent()) {
            String username = optionalUsername.get().get(USERNAME_FIELD);
            String password = optionalPassword.get().get(PASSWORD_FIELD);
            Metadata metadata = new Metadata();
            metadata.put(Metadata.Key.of(USERNAME_FIELD, Metadata.ASCII_STRING_MARSHALLER), username);
            metadata.put(Metadata.Key.of(PASSWORD_FIELD, Metadata.ASCII_STRING_MARSHALLER), password);
            var clientInterceptor = new GrpcClientInterceptor(metadata);
            return GrpcClientBuilder.getChannelWithInterceptor(host, port, tlsFilePaths, clientInterceptor);
        } else {
            return GrpcClientBuilder.getChannel(host, port, tlsFilePaths);
        }
    }

    private synchronized void awaitReady(Attempt current) {
        if (!isCurrent(current) || current.subscribed) {
            return;
        }
        try {
            ConnectivityState state = current.channel.getState(true);
            if (state == READY) {
                current.timeout.cancel(false);
                subscribeToTelemetry(current);
                current.subscribed = true;
            } else if (state == ConnectivityState.SHUTDOWN) {
                retry(current);
            } else {
                current.channel.notifyWhenStateChanged(state, () -> awaitReady(current));
            }
        } catch (Exception e) {
            LOG.warn("Exception while subscribing to OpenConfig Server at `{}`", InetAddressUtils.str(host), e);
            retry(current);
        }
    }

    private synchronized void connectionTimedOut(Attempt current) {
        if (isCurrent(current) && !current.subscribed) {
            LOG.warn("Timed out connecting to OpenConfig Server at `{}`, current state {}",
                    InetAddressUtils.str(host), current.channel.getState(false));
            retry(current);
        }
    }

    private void subscribeToTelemetry(Attempt current) {
        ManagedChannel channel = current.channel;
        String host = hostName != null ? hostName : this.host.getHostAddress();

        // Defaults to gnmi
        if (JTI_MODE.equalsIgnoreCase(mode)) {
            OpenConfigTelemetryGrpc.OpenConfigTelemetryStub asyncStub = OpenConfigTelemetryGrpc.newStub(channel);
            Telemetry.SubscriptionRequest.Builder requestBuilder = Telemetry.SubscriptionRequest.newBuilder();
            paramList.forEach(entry -> {
                Integer frequency = StringUtils.parseInt(entry.get(FREQUENCY), DEFAULT_FREQUENCY);
                String pathString = entry.get(PATHS);
                List<String> paths = pathString != null ? Arrays.asList(pathString.split(",", -1)) : new ArrayList<>();
                paths.forEach(path -> requestBuilder.addPathList(Telemetry.Path.newBuilder().setPath(path).setSampleFrequency(frequency).build()));
            });
            asyncStub.telemetrySubscribe(requestBuilder.build(), new TelemetryDataHandler(current));
            LOG.info("Subscribed to OpenConfig telemetry stream at {}:{}", host, port);
        } else {

            gNMIGrpc.gNMIStub gNMIStub = gNMIGrpc.newStub(channel);
            Gnmi.SubscribeRequest.Builder requestBuilder = Gnmi.SubscribeRequest.newBuilder();
            Gnmi.SubscriptionList.Builder subscriptionListBuilder = Gnmi.SubscriptionList.newBuilder();
            paramList.forEach(entry -> {
                Long frequency = StringUtils.parseLong(entry.get(FREQUENCY), DEFAULT_FREQUENCY_FOR_GNMI);
                String pathString = entry.get(PATHS);
                String origin = entry.get(ORIGIN);
                List<String> paths = pathString != null ? Arrays.asList(pathString.split(",", -1)) : new ArrayList<>();
                paths.forEach(path -> {
                    Gnmi.Path gnmiPath = buildGnmiPath(path, origin);
                    Gnmi.Subscription subscription = Gnmi.Subscription.newBuilder()
                            .setPath(gnmiPath)
                            .setSampleInterval(frequency)
                            .setMode(Gnmi.SubscriptionMode.SAMPLE).build();
                    subscriptionListBuilder.addSubscription(subscription);
                    subscriptionListBuilder.setMode(Gnmi.SubscriptionList.Mode.STREAM);
                });
            });
            requestBuilder.setSubscribe(subscriptionListBuilder.build());
            StreamObserver<Gnmi.SubscribeRequest> requestStreamObserver = gNMIStub.subscribe(new GnmiDataHandler(current));
            requestStreamObserver.onNext(requestBuilder.build());
            LOG.info("Subscribed to OpenConfig telemetry stream at {}:{}", host, port);
        }
    }

    // Builds gnmi path based on https://github.com/openconfig/reference/blob/master/rpc/gnmi/gnmi-path-conventions.md
    static Gnmi.Path buildGnmiPath(String path, String origin) {
        Gnmi.Path.Builder gnmiPathBuilder = Gnmi.Path.newBuilder();
        List<String> elemList =  Splitter.on(PATH_SEPARATOR).omitEmptyStrings().splitToList(path);
        elemList.forEach(elem -> {
            if (elem.contains("[")) {
                String name = elem.substring(0, elem.indexOf("["));
                Map<String, String> keyValues = getPathElemParam(elem);
                Gnmi.PathElem.Builder builder = Gnmi.PathElem.newBuilder();
                builder.setName(name);
                keyValues.forEach(builder::putKey);
                gnmiPathBuilder.addElem(builder.build());
            } else {
                gnmiPathBuilder.addElem(Gnmi.PathElem.newBuilder().setName(elem).build());
            }
        });
        if (Strings.isNullOrEmpty(origin)) {
            gnmiPathBuilder.setOrigin(DEFAULT_ORIGIN);
        } else {
            gnmiPathBuilder.setOrigin(origin);
        }
        return gnmiPathBuilder.build();
    }

    private static Map<String, String> getPathElemParam(String element) {
        Map<String, String> params = new HashMap<>();
        List<String> matches = new ArrayList<String>();
        Matcher matcher = STRINGS_IN_SQUARE_BRACKETS.matcher(element);
        while (matcher.find()) {
            matches.add(matcher.group(1));
        }
        matches.forEach(match -> {
            String[] keyValues = match.split("=", 2);
                params.put(keyValues[0], keyValues[1]);
        });
        return params;
    }


    private boolean isCurrent(Attempt current) {
        return !closed && attempt == current;
    }

    // Called with the client lock held. The initial attempt and an established stream get one
    // immediate reconnect; anything else waits for the interval, so a device that accepts the
    // connection but rejects the RPC cannot reconnect in a tight loop.
    private void retry(Attempt current) {
        if (!isCurrent(current)) {
            return;
        }
        attempt = null;
        close(current);
        if (current.initial || current.received) {
            retry = executor.schedule(() -> startAttempt(false), 0, TimeUnit.SECONDS);
        } else if (retries <= 0 || remainingRetries > 0) {
            if (remainingRetries > 0) {
                remainingRetries--;
            }
            retry = executor.schedule(() -> startAttempt(false), interval, TimeUnit.SECONDS);
        } else {
            LOG.warn("Giving up on OpenConfig Server at `{}` after {} retries at {}s interval",
                    InetAddressUtils.str(host), retries, interval);
        }
    }

    @Override
    public synchronized void shutdown() {
        if (closed) {
            return;
        }
        // Mark closed before cancelling the stream: cancellation also invokes onError.
        closed = true;
        if (retry != null) {
            retry.cancel(false);
            retry = null;
        }
        if (attempt != null) {
            close(attempt);
            attempt = null;
        }
        if (ownsExecutor) {
            executor.shutdownNow();
        }
        onShutdown.accept(this);
    }

    synchronized boolean isClosed() {
        return closed;
    }

    private void close(Attempt current) {
        if (current.timeout != null) {
            current.timeout.cancel(false);
        }
        if (current.channel != null) {
            // A streaming RPC can outlive orderly shutdown indefinitely.
            current.channel.shutdownNow();
        }
    }

    // The handler may block (e.g. on sink back-pressure), so it is never invoked with the lock held.
    private void accept(Attempt current, byte[] data) {
        synchronized (this) {
            if (!isCurrent(current)) {
                return;
            }
            if (!current.received) {
                current.received = true;
                remainingRetries = retries;
            }
        }
        handler.accept(host, port, data);
    }

    private void streamFailed(Attempt current, String message, Throwable cause) {
        synchronized (this) {
            if (!isCurrent(current)) {
                return;
            }
            LOG.warn("OpenConfig stream at `{}` ended: {}", InetAddressUtils.str(host), message, cause);
        }
        try {
            handler.onError(message);
        } finally {
            synchronized (this) {
                retry(current);
            }
        }
    }

    private class TelemetryDataHandler implements StreamObserver<OpenConfigData> {
        private final Attempt current;

        private TelemetryDataHandler(Attempt current) {
            this.current = current;
        }

        @Override
        public void onNext(OpenConfigData value) {
            accept(current, value.toByteArray());
        }

        @Override
        public void onError(Throwable t) {
            streamFailed(current, t.getMessage(), t);
        }

        @Override
        public void onCompleted() {
            streamFailed(current, "OpenConfig Server closed connection for host " + InetAddressUtils.str(host), null);
        }
    }

    private class GnmiDataHandler implements StreamObserver<Gnmi.SubscribeResponse> {
        private final Attempt current;

        private GnmiDataHandler(Attempt current) {
            this.current = current;
        }

        @Override
        public void onNext(Gnmi.SubscribeResponse response) {
            if (response != null) {
                accept(current, response.toByteArray());
            }
        }

        @Override
        public void onError(Throwable t) {
            streamFailed(current, t.getMessage(), t);
        }

        @Override
        public void onCompleted() {
            streamFailed(current, "OpenConfig Server closed connection for host " + InetAddressUtils.str(host), null);
        }
    }
}
