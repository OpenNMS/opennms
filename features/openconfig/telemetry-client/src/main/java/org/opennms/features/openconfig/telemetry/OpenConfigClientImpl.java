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

package org.opennms.features.openconfig.telemetry;


import static io.grpc.ConnectivityState.READY;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

import com.google.common.base.Splitter;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

/**
 * OpenConfig Client makes a gRPC connection and subscribes to telemetry data for the paths specified.
 * When it fails to make a connection, it attempts to make a connection after given interval.
 * When retries are specified, it bails out after those many attempts.
 * If no retries or <=0 specified, it always attempts to connect after given interval.
 */
public class OpenConfigClientImpl implements OpenConfigClient {

    private static final Logger LOG = LoggerFactory.getLogger(OpenConfigClientImpl.class);
    private static final Pattern STRINGS_IN_SQUARE_BRACKETS = Pattern.compile("\\[(.+?=.+?)\\]");
    // Path separator but exclude in square brackets.
    private static final Pattern PATH_SEPARATOR = Pattern.compile("\\/(?![^\\[]*])");
    // Internal retries and timeout are used to make a connection and wait till channel is active.
    private static final int DEFAULT_INTERNAL_RETRIES = 5;
    private static final int DEFAULT_INTERNAL_TIMEOUT = 1000;
    private static final int DEFAULT_FREQUENCY = 300000; //5min
    private static final int DEFAULT_INTERVAL_IN_SEC = 300; //5min
    private static final String PORT = "port";
    private static final String MODE = "mode";
    private static final String PATHS = "paths";
    private static final String FREQUENCY = "frequency";
    private static final String INTERVAL = "interval";
    private static final String RETRIES = "retries";
    private static final String JTI_MODE = "jti";
    private ManagedChannel channel;
    private final InetAddress host;
    private Integer port;
    private String mode;
    private Integer interval;
    private Integer retries;
    private List<Map<String,String>> paramList = new ArrayList<>();
    private AtomicBoolean closed = new AtomicBoolean(false);
    private AtomicBoolean scheduled = new AtomicBoolean(false);
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    public OpenConfigClientImpl(InetAddress host, List<Map<String, String>> paramList) {
        this.host = Objects.requireNonNull(host);
        this.paramList.addAll(paramList);
        // Extract port and mode which are global.
        this.paramList.stream().filter(entry -> entry.get(PORT) != null)
                .findFirst().ifPresent(entry ->
                this.port = Objects.requireNonNull(StringUtils.parseInt(entry.get(PORT), null)));
        this.paramList.stream().filter(entry -> entry.get(MODE) != null)
                .findFirst().ifPresent(entry -> this.mode = entry.get(MODE));
    }

    @Override
    public void subscribe(OpenConfigClient.Handler handler) {
        boolean succeeded = trySubscribing(handler);
        if (!succeeded) {
            close();
            executor.execute(() -> scheduleSubscription(handler));
        }
    }

    private boolean trySubscribing(OpenConfigClient.Handler handler) {
        try {
            Map<String, String> tlsFilePaths = new HashMap<>();
            paramList.forEach(entry -> {
                tlsFilePaths.putAll(entry.entrySet().stream()
                        .filter(configuration -> configuration.getKey().contains("tls"))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            });
            this.channel = GrpcClientBuilder.getChannel(host.getHostAddress(), port, tlsFilePaths);
            if (READY.equals(retrieveChannelState())) {
                subscribeToTelemetry(handler);
                return true;
            }
        } catch (Exception e) {
            LOG.warn("Exception while subscribing to OpenConfig Server at `{}` ", InetAddressUtils.str(host), e);
        }
        return false;
    }


    private void subscribeToTelemetry(OpenConfigClient.Handler handler) {

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
            asyncStub.telemetrySubscribe(requestBuilder.build(), new TelemetryDataHandler(host, port, handler));
            LOG.info("Subscribed to OpenConfig telemetry stream at {}", InetAddressUtils.str(host));
        } else {
            gNMIGrpc.gNMIStub gNMIStub = gNMIGrpc.newStub(channel);
            Gnmi.SubscribeRequest.Builder requestBuilder = Gnmi.SubscribeRequest.newBuilder();
            Gnmi.SubscriptionList.Builder subscriptionListBuilder = Gnmi.SubscriptionList.newBuilder();
            paramList.forEach(entry -> {
                Integer frequency = StringUtils.parseInt(entry.get(FREQUENCY), DEFAULT_FREQUENCY);
                String pathString = entry.get(PATHS);
                List<String> paths = pathString != null ? Arrays.asList(pathString.split(",", -1)) : new ArrayList<>();
                paths.forEach(path -> {
                    Gnmi.Path gnmiPath = buildGnmiPath(path);
                    Gnmi.Subscription subscription = Gnmi.Subscription.newBuilder()
                            .setPath(gnmiPath)
                            .setSampleInterval(frequency)
                            .setMode(Gnmi.SubscriptionMode.SAMPLE).build();
                    subscriptionListBuilder.addSubscription(subscription);
                });
            });
            requestBuilder.setSubscribe(subscriptionListBuilder.build());
            StreamObserver<Gnmi.SubscribeRequest> requestStreamObserver = gNMIStub.subscribe(new GnmiDataHandler(handler, host, port));
            requestStreamObserver.onNext(requestBuilder.build());
            LOG.info("Subscribed to OpenConfig telemetry stream at {}", InetAddressUtils.str(host));
        }
    }

    // Builds gnmi path based on https://github.com/openconfig/reference/blob/master/rpc/gnmi/gnmi-path-conventions.md
    static Gnmi.Path buildGnmiPath(String path) {
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


    private void scheduleSubscription(OpenConfigClient.Handler handler) {
        if (scheduled.get()) {
            // Task is already scheduled.
            return;
        }
        scheduled.set(true);
        // Try at least once.
        boolean succeeded = trySubscribing(handler);
        if (succeeded) {
            scheduled.set(false);
            return;
        }

        // If it's not subscribed, schedule this to run after configured timeout
        this.paramList.stream().filter(entry -> entry.get(INTERVAL) != null)
                .findFirst().ifPresent(entry ->
                this.interval = StringUtils.parseInt(entry.get(INTERVAL), DEFAULT_INTERVAL_IN_SEC));
        // When retries is null or <= 0, scheduling will happen indefinitely until it succeeds.
        this.paramList.stream().filter(entry -> entry.get(RETRIES) != null)
                .findFirst().ifPresent(entry ->
                this.retries = StringUtils.parseInt(entry.get(RETRIES), null));

        Integer retries = this.retries;
        while (!closed.get()) {
            ScheduledFuture<Boolean> future = scheduledExecutor.schedule(() -> trySubscribing(handler), this.interval, TimeUnit.SECONDS);
            try {
                succeeded = future.get();
                if (succeeded) {
                    scheduled.set(false);
                    break;
                }
            } catch (InterruptedException | ExecutionException e) {
                LOG.warn("Exception while scheduling subscription at host `{}` ", InetAddressUtils.str(host), e);
            }
            if (retries != null && retries > 0) {
                retries--;
                if (retries == 0) {
                    scheduled.set(false);
                    break;
                }
            }
        }
    }

    @Override
    public void shutdown() {
        close();
        closed.set(true);
        if (scheduledExecutor != null) {
            scheduledExecutor.shutdown();
        }
        if (executor != null) {
            executor.shutdown();
        }
    }

    private void close() {
        if (channel != null) {
            LOG.info("Closing the OpenConfig Client at {}", host);
            channel.shutdown();
        }
    }
    // Handles JTI Telemetry data
    private class TelemetryDataHandler implements StreamObserver<OpenConfigData> {

        private final OpenConfigClient.Handler handler;
        private final InetAddress host;
        private final Integer port;

        private TelemetryDataHandler(InetAddress host, Integer port, Handler handler) {
            this.host = host;
            this.port = port;
            this.handler = handler;
        }

        @Override
        public void onNext(OpenConfigData value) {
            handler.accept(host, port, value.toByteArray());
        }

        @Override
        public void onError(Throwable t) {
            LOG.error("Received error on stream for host {}", InetAddressUtils.str(host), t);
            handler.onError(t.getMessage());
            close();
            executor.execute(() -> scheduleSubscription(handler));
        }

        @Override
        public void onCompleted() {
            LOG.info("Response stream closed for host {}", InetAddressUtils.str(host));
            handler.onError("OpenConfig Server closed connection for host " + InetAddressUtils.str(host));
            close();
            executor.execute(() -> scheduleSubscription(handler));
        }
    }

    // Handles Gnmi Telemetry data
    private class GnmiDataHandler implements StreamObserver<Gnmi.SubscribeResponse> {

        private final OpenConfigClient.Handler handler;
        private final InetAddress host;
        private final Integer port;

        public GnmiDataHandler(Handler handler, InetAddress host, Integer port) {
            this.handler = handler;
            this.host = host;
            this.port = port;
        }

        @Override
        public void onNext(Gnmi.SubscribeResponse subscribeResponse) {
            if(subscribeResponse != null) {
                handler.accept(host, port, subscribeResponse.toByteArray());
            }
        }

        @Override
        public void onError(Throwable t) {
            LOG.error("Received error on stream for host {}", InetAddressUtils.str(host), t);
            handler.onError(t.getMessage());
            close();
            executor.execute(() -> scheduleSubscription(handler));
        }

        @Override
        public void onCompleted() {
            LOG.info("Response stream closed for host {}", InetAddressUtils.str(host));
            handler.onError("OpenConfig Server closed connection for host " + InetAddressUtils.str(host));
            close();
            executor.execute(() -> scheduleSubscription(handler));
        }
    }


    /*gRPC channel may not be in ready state instantly, this is internal wait to make a connection*/
    private ConnectivityState retrieveChannelState() {
        int retries = DEFAULT_INTERNAL_RETRIES;
        ConnectivityState state = null;
        while (retries > 0 && !closed.get()) {
            state = channel.getState(true);
            if (!state.equals(READY)) {
                LOG.warn("OpenConfig Server at `{}` is not in ready state, current state {}, retrying..", InetAddressUtils.str(host), state);
                waitBeforeRetrying(DEFAULT_INTERNAL_TIMEOUT);
                retries--;
            } else {
                break;
            }
        }
        return state;
    }

    private void waitBeforeRetrying(long timeout) {
        try {
            Thread.sleep(timeout);
        } catch (InterruptedException e) {
            LOG.warn("Sleep was interrupted", e);
        }
    }


}
