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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.opennms.core.grpc.common.GrpcClientBuilder;
import org.opennms.features.openconfig.proto.jti.OpenConfigTelemetryGrpc;
import org.opennms.features.openconfig.proto.jti.Telemetry;
import org.opennms.features.openconfig.proto.jti.Telemetry.OpenConfigData;
import org.opennms.netmgt.telemetry.stream.listeners.Config;
import org.opennms.netmgt.telemetry.stream.listeners.Connection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;

public class OpenConfigClient implements Connection {

    private static final Logger LOG = LoggerFactory.getLogger(OpenConfigClient.class);
    private static final int DEFAULT_RETRIES = 5;
    private static final int DEFAULT_TIMEOUT = 1000;
    private static final int DEFAULT_FREQUENCY = 300000; //5minutes
    private final ManagedChannel channel;
    private final String host;
    private final Config config;

    public OpenConfigClient(Config config) throws IOException {
        this.config = Objects.requireNonNull(config);
        this.host = Objects.requireNonNull(config.getIpAddress());
        String port = config.getParams().get("port");
        Integer portNum = Objects.requireNonNull(getInteger(port));
        Map<String, String> tlsFilePaths = config.getParams().entrySet().stream()
                .filter(configuration -> configuration.getKey().contains("tls"))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        this.channel = GrpcClientBuilder.getChannel(host, portNum, tlsFilePaths);
    }

    @Override
    public  void subscribe(Handler handler) throws Exception {
        if (READY.equals(retrieveChannelState())) {
            subscribeToTelemetry(handler);
        } else {
            LOG.error("Unable to subscribe, OpenConfig gRPC Server at `{}` is not in ready state", host);
            throw new IOException("Couldn't connect to gRPC Server at " + host);
        }
    }

    /*gRPC channel may not be in ready state instantly, this is internal wait to make a connection*/
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
        if (channel != null) {
            channel.shutdown();
        }
    }

    private void subscribeToTelemetry(Handler handler) {
        Integer frequency = getInteger(config.getParams().get("frequency"), DEFAULT_FREQUENCY);
        String pathString = config.getParams().get("paths");
        List<String> paths = pathString != null ? Arrays.asList(pathString.split(",", -1)) : new ArrayList<>();
        OpenConfigTelemetryGrpc.OpenConfigTelemetryStub asyncStub = OpenConfigTelemetryGrpc.newStub(channel);
        Telemetry.SubscriptionRequest.Builder requestBuilder = Telemetry.SubscriptionRequest.newBuilder();
        paths.forEach(path -> requestBuilder.addPathList(Telemetry.Path.newBuilder().setPath(path).setSampleFrequency(frequency).build()));
        asyncStub.telemetrySubscribe(requestBuilder.build(), new TelemetryDataHandler(host, handler));
    }


    private class TelemetryDataHandler implements StreamObserver<OpenConfigData> {

        private final Handler handler;
        private final String host;

        private TelemetryDataHandler(String host, Handler handler) {
            this.host = host;
            this.handler = handler;
        }

        @Override
        public void onNext(OpenConfigData value) {
            handler.accept(value.toByteArray());
        }

        @Override
        public void onError(Throwable t) {
            LOG.error("Received error on stream for host {}", host, t);
            handler.onError(t.getMessage());
            close();
        }

        @Override
        public void onCompleted() {
            LOG.info("Response stream closed for host {}", host);
            handler.onError("Server closed connection");
            close();
        }
    }

    private static Integer getInteger(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer getInteger(String value, Integer defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }



}
