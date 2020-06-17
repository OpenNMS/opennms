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
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.opennms.core.grpc.common.GrpcClientBuilder;
import org.opennms.features.openconfig.api.Config;
import org.opennms.features.openconfig.api.TelemetryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import telemetry.OpenConfigTelemetryGrpc;
import telemetry.OpenConfigTelemetryProto;

public class TelemetryClientImpl implements TelemetryClient {

    private static final Logger LOG = LoggerFactory.getLogger(TelemetryClientImpl.class);
    private final ManagedChannel channel;
    private final String host;
    private final int DEFAULT_RETRIES = 10;
    private final int DEFAULT_TIMEOUT = 3000;
    private Config config;

    public TelemetryClientImpl(String host, int port, Map<String, String> tlsFilePaths) throws IOException {
        this.host = Objects.requireNonNull(host);
        this.channel = GrpcClientBuilder.getChannel(host, port, tlsFilePaths);
    }

    public boolean subscribe(Config config, Consumer<byte[]> dataConsumer) {
        if (getChannelState() != null && getChannelState().equals(READY)) {
            this.config = config;
            return subscribe(config.getPaths(), config.getFrequency(), dataConsumer);
        } else {
            LOG.error("Telemetry gRPC Server is not in ready state, current state {}", host);
            return false;
        }
    }

    private ConnectivityState getChannelState() {
        int retries = DEFAULT_RETRIES;
        ConnectivityState state = null;
        while (retries > 0) {
            state = channel.getState(true);
            if (!state.equals(READY)) {
                LOG.warn("Telemetry gRPC Server at `{}` is not in ready state, current state {}, retrying..", host, state);
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
    public void stop() {
        if (channel != null) {
            channel.shutdown();
        }
    }

    private class TelemetryDataHandler implements StreamObserver<OpenConfigTelemetryProto.OpenConfigData> {

        private final Consumer<byte[]> consumer;
        private final String host;

        private TelemetryDataHandler(String host, Consumer<byte[]> consumer) {
            this.host = host;
            this.consumer = consumer;
        }

        @Override
        public void onNext(OpenConfigTelemetryProto.OpenConfigData value) {
            LOG.debug("Telemetry data : {}", value);
            Executors.newSingleThreadScheduledExecutor().execute(() -> consumer.accept(value.toByteArray()));
        }

        @Override
        public void onError(Throwable t) {
            LOG.error("Received error on stream for host {}", host, t);
            stop();
        }

        @Override
        public void onCompleted() {
            LOG.info("Response stream closed for host {}", host);
            stop();
        }
    }

    private boolean subscribe(List<String> paths, int frequency, Consumer<byte[]> dataConsumer) {
        OpenConfigTelemetryGrpc.OpenConfigTelemetryStub asyncStub = OpenConfigTelemetryGrpc.newStub(channel);
        OpenConfigTelemetryProto.SubscriptionRequest.Builder requestBuilder = OpenConfigTelemetryProto.SubscriptionRequest.newBuilder();
        paths.forEach(path -> requestBuilder.addPathList(OpenConfigTelemetryProto.Path.newBuilder().setPath(path).setSampleFrequency(frequency).build()));
        asyncStub.telemetrySubscribe(requestBuilder.build(), new TelemetryDataHandler(host, dataConsumer));
        return true;
    }

}
