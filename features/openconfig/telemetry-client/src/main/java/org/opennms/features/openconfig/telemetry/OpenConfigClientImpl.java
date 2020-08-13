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
import java.util.stream.Collectors;

import org.opennms.core.grpc.common.GrpcClientBuilder;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.utils.StringUtils;
import org.opennms.features.openconfig.api.OpenConfigClient;
import org.opennms.features.openconfig.proto.jti.OpenConfigTelemetryGrpc;
import org.opennms.features.openconfig.proto.jti.Telemetry;
import org.opennms.features.openconfig.proto.jti.Telemetry.OpenConfigData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

public class OpenConfigClientImpl implements OpenConfigClient {

    private static final Logger LOG = LoggerFactory.getLogger(OpenConfigClientImpl.class);
    private static final int DEFAULT_RETRIES = 15;
    private static final int DEFAULT_TIMEOUT = 1000;
    private static final int DEFAULT_FREQUENCY = 300000; //5minutes
    private ManagedChannel channel;
    private final InetAddress host;
    private Integer port;
    private Map<String, String> parameters = new HashMap<>();
    private AtomicBoolean closed = new AtomicBoolean(false);
    private AtomicBoolean scheduled = new AtomicBoolean(false);
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private ScheduledExecutorService scheduledExecutor = Executors.newSingleThreadScheduledExecutor();

    public OpenConfigClientImpl(InetAddress host, Map<String, String> params) {
        this.host = Objects.requireNonNull(host);
        this.port = Objects.requireNonNull(StringUtils.parseInt(params.get("port"), null));
        parameters.putAll(params);
    }

    @Override
    public void subscribe(OpenConfigClient.Handler handler) {
        boolean succeeded = trySubscribing(handler);
        if (!succeeded) {
            executor.execute(() -> scheduleSubscription(handler));
        }
    }

    private boolean trySubscribing(OpenConfigClient.Handler handler) {
        try {
            Map<String, String> tlsFilePaths = parameters.entrySet().stream()
                    .filter(configuration -> configuration.getKey().contains("tls"))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
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
        Integer frequency = StringUtils.parseInt(parameters.get("frequency"), DEFAULT_FREQUENCY);
        String pathString = parameters.get("paths");
        List<String> paths = pathString != null ? Arrays.asList(pathString.split(",", -1)) : new ArrayList<>();
        OpenConfigTelemetryGrpc.OpenConfigTelemetryStub asyncStub = OpenConfigTelemetryGrpc.newStub(channel);
        Telemetry.SubscriptionRequest.Builder requestBuilder = Telemetry.SubscriptionRequest.newBuilder();
        paths.forEach(path -> requestBuilder.addPathList(Telemetry.Path.newBuilder().setPath(path).setSampleFrequency(frequency).build()));
        asyncStub.telemetrySubscribe(requestBuilder.build(), new TelemetryDataHandler(host, port, handler));
        LOG.info("Subscribed to OpenConfig telemetry stream at {}", host);
    }

    private void scheduleSubscription(OpenConfigClient.Handler handler) {
        if (scheduled.get()) {
            // Task is already scheduled.
            return;
        }
        scheduled.set(true);
        boolean succeeded = trySubscribing(handler);
        if (succeeded) {
            scheduled.set(false);
            return;
        }
        while (!closed.get()) {
            // If it's not subscribed, schedule this to run after configured delay
            Integer delay = StringUtils.parseInt(parameters.get("frequency"), DEFAULT_FREQUENCY);
            ScheduledFuture<Boolean> future = scheduledExecutor.schedule(() -> trySubscribing(handler), delay, TimeUnit.MILLISECONDS);
            try {
                succeeded = future.get();
                if (succeeded) {
                    scheduled.set(false);
                    break;
                }
            } catch (InterruptedException | ExecutionException e) {
                LOG.warn("Exception while scheduling subscription at host `{}` ", InetAddressUtils.str(host), e);
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
            LOG.error("Received error on stream for host {}", host, t);
            handler.onError(t.getMessage());
            close();
            executor.execute(() -> scheduleSubscription(handler));
        }

        @Override
        public void onCompleted() {
            LOG.info("Response stream closed for host {}", host);
            handler.onError("Server closed connection");
            close();
            executor.execute(() -> scheduleSubscription(handler));
        }
    }


    /*gRPC channel may not be in ready state instantly, this is internal wait to make a connection*/
    private ConnectivityState retrieveChannelState() {
        int retries = DEFAULT_RETRIES;
        ConnectivityState state = null;
        while (retries > 0) {
            state = channel.getState(true);
            if (!state.equals(READY)) {
                LOG.warn("OpenConfig Server at `{}` is not in ready state, current state {}, retrying..", host, state);
                waitBeforeRetrying(DEFAULT_TIMEOUT);
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
