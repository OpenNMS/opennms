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
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.opennms.features.openconfig.proto.jti.OpenConfigTelemetryGrpc;
import org.opennms.features.openconfig.proto.jti.Telemetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;

public class OpenConfigTestServer {

    private static int DEFAULT_SERVER_PORT = 50052;
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private static final Logger LOG = LoggerFactory.getLogger(OpenConfigTestServer.class);
    private Server server;
    private Boolean errorStream = false;
    private StreamObserver<Telemetry.OpenConfigData> observer;


    public void start() throws IOException {

        NettyServerBuilder serverBuilder = NettyServerBuilder.forAddress(new InetSocketAddress(DEFAULT_SERVER_PORT))
                .addService(new TelemetryServer());
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
            Telemetry.KeyValue keyValue1 = Telemetry.KeyValue.newBuilder().setKey("name")
                    .setStrValue("eth0").build();
            builder.addKv(keyValue1);
            builder.addKv(keyValue);
            Telemetry.OpenConfigData openConfigData = builder.build();
            Optional<Telemetry.KeyValue> ifName =
                    openConfigData.getKvList().stream().filter(kv -> kv.getKey().contains("name")).findFirst();

            if(errorStream) {
                responseObserver.onError(new IllegalArgumentException());
                errorStream = false;
                return;
            }
            executor.scheduleAtFixedRate(() ->
                    responseObserver.onNext(builder.build()), 0, frequency, TimeUnit.MILLISECONDS);
        }

    }

    public void stop() {
        if (server != null) {
            observer.onCompleted();
            server.shutdown();
        }
    }

    protected void setErrorStream() {
        this.errorStream = true;
    }
}
