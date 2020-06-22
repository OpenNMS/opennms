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

package org.opennms.features.openconfig.telemetry.client;


import static io.grpc.ConnectivityState.READY;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.opennms.core.grpc.common.GrpcClientBuilder;
import org.opennms.features.openconfig.api.Handler;
import org.opennms.features.openconfig.api.HostConfig;
import org.opennms.features.openconfig.api.OpenConfigClient;
import org.opennms.features.openconfig.api.StreamConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import telemetry.OpenConfigTelemetryGrpc;
import telemetry.OpenConfigTelemetryProto;

public class OpenConfigClientImpl implements OpenConfigClient {

    private static final Logger LOG = LoggerFactory.getLogger(OpenConfigClientImpl.class);
    private static final int DEFAULT_RETRIES = 5;
    private static final int DEFAULT_TIMEOUT = 1000;
    private final ManagedChannel channel;
    private final String host;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public OpenConfigClientImpl(HostConfig hostConfig) throws IOException {
        Objects.requireNonNull(hostConfig);
        this.host = Objects.requireNonNull(hostConfig.getHost());
        this.channel = GrpcClientBuilder.getChannel(host, hostConfig.getPort(), hostConfig.getParameters());
    }

    public boolean subscribe(StreamConfig config, Handler handler) {
        if (READY.equals(retrieveChannelState())) {
            subscribe(config.getPaths(), config.getFrequency(), handler);
            return true;
        } else {
            LOG.error("Unable to subscribe, OpenConfig gRPC Server at `{}` is not in ready state", host);
            return false;
        }
    }

    /**
     * gRPC channel may not be in ready state instantly, this is internal wait to make a connection.
     */
    private ConnectivityState retrieveChannelState() {
        int retries = DEFAULT_RETRIES;
        ConnectivityState state = null;
        while (retries > 0) {
            state = channel.getState(true);
            if (!state.equals(READY)) {
                LOG.warn("OpenConfig gRPC Server at `{}` is not in ready state, current state {}, retrying..", host, state);
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

    @Override
    public void close() {
        if (executor != null) {
            executor.shutdown();
        }
        if (channel != null) {
            channel.shutdown();
        }
    }

    private class TelemetryDataHandler implements StreamObserver<OpenConfigTelemetryProto.OpenConfigData> {

        private final Handler handler;
        private final String host;

        private TelemetryDataHandler(String host, Handler handler) {
            this.host = host;
            this.handler = handler;
        }

        @Override
        public void onNext(OpenConfigTelemetryProto.OpenConfigData value) {
            LOG.debug("OpenConfig response : {}", value);
            executor.execute(() -> handler.accept(value.toByteArray()));
        }

        @Override
        public void onError(Throwable t) {
            LOG.error("Received error on stream for host {}", host, t);
            handler.onError(t.getMessage());
        }

        @Override
        public void onCompleted() {
            LOG.info("Response stream closed for host {}", host);
            handler.onError("Server closed connection");
        }
    }

    private void subscribe(List<String> paths, int frequency, Handler handler) {
        OpenConfigTelemetryGrpc.OpenConfigTelemetryStub asyncStub = OpenConfigTelemetryGrpc.newStub(channel);
        OpenConfigTelemetryProto.SubscriptionRequest.Builder requestBuilder = OpenConfigTelemetryProto.SubscriptionRequest.newBuilder();
        paths.forEach(path -> requestBuilder.addPathList(OpenConfigTelemetryProto.Path.newBuilder().setPath(path).setSampleFrequency(frequency).build()));
        asyncStub.telemetrySubscribe(requestBuilder.build(), new TelemetryDataHandler(host, handler));
    }

}
