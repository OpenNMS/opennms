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

package org.opennms.features.telemetry.protocols.openconfig;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.opennms.features.openconfig.proto.gnmi.Gnmi;
import org.opennms.features.openconfig.proto.gnmi.gNMIGrpc;
import org.opennms.features.openconfig.proto.jti.OpenConfigTelemetryGrpc;
import org.opennms.features.openconfig.proto.jti.Telemetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;

public class OpenConfigTestServer {

    private final int port;
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private static final Logger LOG = LoggerFactory.getLogger(OpenConfigTestServer.class);
    private Server server;
    private Boolean errorStream = false;
    private StreamObserver<Telemetry.OpenConfigData> observer;
    private StreamObserver<Gnmi.SubscribeResponse> gnmiStream;
    private StreamObserver<Gnmi.SubscribeRequest> requestHandler;

    public OpenConfigTestServer(int port) {
        this.port = port;
    }

    public void start() throws IOException {

        NettyServerBuilder serverBuilder = NettyServerBuilder.forAddress(new InetSocketAddress(port))
                .addService(new TelemetryServer())
                .addService(new GnmiServer());
        server = serverBuilder.build();
        server.start();
    }

    class TelemetryServer extends OpenConfigTelemetryGrpc.OpenConfigTelemetryImplBase {


        public void telemetrySubscribe(Telemetry.SubscriptionRequest request,
                                       io.grpc.stub.StreamObserver<Telemetry.OpenConfigData> responseObserver) {
            observer = responseObserver;
            LOG.info("Got request {}", request.toString());
            List<Telemetry.Path> paths  = request.getPathListList();
            if(paths.isEmpty()) {
                LOG.info("No paths found");
                return;
            }
            Telemetry.Path path = paths.get(0);
            int frequency = path.getSampleFrequency();
            Telemetry.OpenConfigData.Builder builder = Telemetry.OpenConfigData.newBuilder();
            builder.setPath(path.getPath());
            builder.setComponentId(1);
            builder.setSystemId("localhost");
            builder.setTimestamp(System.currentTimeMillis());
            Telemetry.KeyValue keyValue = Telemetry.KeyValue.newBuilder().setKey("in-octets")
                    .setIntValue(4000).build();
            builder.addKv(keyValue);
            Telemetry.KeyValue keyValue1 = Telemetry.KeyValue.newBuilder().setKey("name")
                    .setStrValue("eth0").build();
            builder.addKv(keyValue1);

            if(errorStream) {
                responseObserver.onError(new IllegalArgumentException());
                errorStream = false;
                return;
            }
            executor.scheduleAtFixedRate(() ->
                    responseObserver.onNext(builder.build()), 0, frequency, TimeUnit.MILLISECONDS);
        }

    }

    class GnmiServer extends gNMIGrpc.gNMIImplBase {

        @Override
        public io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.gnmi.Gnmi.SubscribeRequest> subscribe(
                io.grpc.stub.StreamObserver<org.opennms.features.openconfig.proto.gnmi.Gnmi.SubscribeResponse> responseObserver) {
            gnmiStream = responseObserver;
            // In real server, this request should be handled only once.
            requestHandler = new SubscribeRequestReceiver(gnmiStream);
            return requestHandler;

        }
    }

    class SubscribeRequestReceiver implements StreamObserver<Gnmi.SubscribeRequest> {

        private final StreamObserver<Gnmi.SubscribeResponse> gnmiStream;

        public SubscribeRequestReceiver(StreamObserver<Gnmi.SubscribeResponse> gnmiStream) {
            this.gnmiStream = gnmiStream;
        }

        @Override
        public void onNext(Gnmi.SubscribeRequest subscribeRequest) {
            // Send Response.
            Gnmi.SubscriptionList subscriptionList =   subscribeRequest.getSubscribe();
            subscriptionList.getSubscriptionList().forEach( subscription -> {
                Gnmi.SubscribeResponse.Builder responseBuilder = Gnmi.SubscribeResponse.newBuilder();
                Gnmi.Path.Builder pathBuilder = Gnmi.Path.newBuilder().addElem(Gnmi.PathElem.newBuilder().setName("eth1").build())
                        .addElem(Gnmi.PathElem.newBuilder().setName("ifInOctets"));
                responseBuilder.setUpdate(Gnmi.Notification.newBuilder().setTimestamp(System.currentTimeMillis())
                        .addUpdate(Gnmi.Update.newBuilder().setPath(pathBuilder.build()).setVal(Gnmi.TypedValue.newBuilder().setUintVal(4000).build()).build()).build());
                executor.scheduleAtFixedRate(() ->
                        gnmiStream.onNext(responseBuilder.build()), 0, subscription.getSampleInterval(), TimeUnit.MILLISECONDS);
            });

        }

        @Override
        public void onError(Throwable throwable) {

        }

        @Override
        public void onCompleted() {

        }
    }

    public void stop() {
        if (server != null) {
            if(observer != null) {
                observer.onCompleted();
            }
            if(gnmiStream != null) {
                gnmiStream.onCompleted();
            }
            server.shutdownNow();
        }
    }

    protected void setErrorStream() {
        this.errorStream = true;
    }

    static int getAvailablePort(final AtomicInteger current, final int max) {
        while (current.get() < max) {
            try (final ServerSocket socket = new ServerSocket(current.get())) {
                return socket.getLocalPort();
            } catch (final Throwable e) {
            }
            current.incrementAndGet();
        }
        throw new IllegalStateException("Can't find an available network port");
    }
}
