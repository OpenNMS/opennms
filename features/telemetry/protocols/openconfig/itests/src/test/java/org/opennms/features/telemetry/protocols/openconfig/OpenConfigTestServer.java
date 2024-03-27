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
package org.opennms.features.telemetry.protocols.openconfig;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.stub.StreamObserver;
import org.opennms.features.openconfig.proto.gnmi.Gnmi;
import org.opennms.features.openconfig.proto.gnmi.gNMIGrpc;
import org.opennms.features.openconfig.proto.jti.OpenConfigTelemetryGrpc;
import org.opennms.features.openconfig.proto.jti.Telemetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

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
                Gnmi.Path.Builder pathBuilder = Gnmi.Path.newBuilder()
                        .addElem(Gnmi.PathElem.newBuilder().setName("ifInOctets"));
                responseBuilder.setUpdate(Gnmi.Notification.newBuilder().setTimestamp(System.currentTimeMillis())
                        .addUpdate(Gnmi.Update.newBuilder().setPath(pathBuilder.build())
                                .setVal(Gnmi.TypedValue.newBuilder().setUintVal(4000).build()).build())
                        .setPrefix(Gnmi.Path.newBuilder()
                                .addElem(Gnmi.PathElem.newBuilder().setName("interfaces").build())
                                .addElem(Gnmi.PathElem.newBuilder().setName("interface").putKey("name", "eth1")).build())).build();

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
