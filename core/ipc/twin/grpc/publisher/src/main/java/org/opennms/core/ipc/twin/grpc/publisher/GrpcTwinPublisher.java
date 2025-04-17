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
package org.opennms.core.ipc.twin.grpc.publisher;

import com.google.common.base.Strings;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.grpc.stub.StreamObserver;
import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.Tracer;
import org.opennms.core.grpc.common.GrpcIpcServer;
import org.opennms.core.grpc.common.GrpcIpcUtils;
import org.opennms.core.ipc.twin.api.TwinStrategy;
import org.opennms.core.ipc.twin.common.AbstractTwinPublisher;
import org.opennms.core.ipc.twin.api.LocalTwinSubscriber;
import org.opennms.core.ipc.twin.api.TwinRequest;
import org.opennms.core.ipc.twin.api.TwinUpdate;
import org.opennms.core.ipc.twin.grpc.common.MinionHeader;
import org.opennms.core.ipc.twin.grpc.common.OpenNMSTwinIpcGrpc;
import org.opennms.core.ipc.twin.model.TwinRequestProto;
import org.opennms.core.ipc.twin.model.TwinResponseProto;
import org.opennms.core.logging.Logging;
import org.opennms.core.tracing.util.TracingInfoCarrier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

public class GrpcTwinPublisher extends AbstractTwinPublisher {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcTwinPublisher.class);
    private final GrpcIpcServer grpcIpcServer;
    private Multimap<String, StreamObserver<TwinResponseProto>> sinkStreamsByLocation = LinkedListMultimap.create();
    private Map<String, StreamObserver<TwinResponseProto>> sinkStreamsBySystemId = new HashMap<>();
    private final ThreadFactory twinRpcThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("twin-rpc-handler-%d")
            .build();
    private final ExecutorService twinRpcExecutor = Executors.newCachedThreadPool(twinRpcThreadFactory);

    public GrpcTwinPublisher(LocalTwinSubscriber twinSubscriber, GrpcIpcServer grpcIpcServer) {
        super(twinSubscriber);
        this.grpcIpcServer = grpcIpcServer;
    }

    @Override
    protected void handleSinkUpdate(TwinUpdate sinkUpdate) {
        sendTwinResponseForSink(mapTwinResponse(sinkUpdate));
    }

    private synchronized boolean sendTwinResponseForSink(TwinResponseProto twinResponseProto) {
        if (sinkStreamsByLocation.isEmpty()) {
            return false;
        }
        try {
            if (Strings.isNullOrEmpty(twinResponseProto.getLocation())) {
                LOG.debug("Sending sink update for key {} at all locations", twinResponseProto.getConsumerKey());
                sinkStreamsByLocation.values().forEach(stream -> {
                    stream.onNext(twinResponseProto);
                });
            } else {
                String location = twinResponseProto.getLocation();
                sinkStreamsByLocation.get(location).forEach(stream -> {
                    stream.onNext(twinResponseProto);
                    LOG.debug("Sending sink update for key {} at location {}", twinResponseProto.getConsumerKey(), twinResponseProto.getLocation());
                });
            }
        } catch (Exception e) {
            LOG.error("Error while sending Twin response for Sink stream", e);
        }
        return true;
    }

    public void start() throws IOException {
        try (Logging.MDCCloseable mdc = Logging.withPrefixCloseable(GrpcIpcUtils.LOG_PREFIX)) {
            grpcIpcServer.startServer(new StreamHandler());
            LOG.info("Added Twin Service to OpenNMS IPC Grpc Server");
        }

    }


    public void close() throws IOException {
        try (Logging.MDCCloseable mdc = Logging.withPrefixCloseable(TwinStrategy.LOG_PREFIX)) {
            grpcIpcServer.stopServer();
            twinRpcExecutor.shutdown();
            LOG.info("Stopped Twin GRPC Server");
        }
    }

    private class StreamHandler extends OpenNMSTwinIpcGrpc.OpenNMSTwinIpcImplBase {

        @Override
        public io.grpc.stub.StreamObserver<org.opennms.core.ipc.twin.model.TwinRequestProto> rpcStreaming(
                io.grpc.stub.StreamObserver<org.opennms.core.ipc.twin.model.TwinResponseProto> responseObserver) {
            StreamObserver<TwinResponseProto> rpcStream = responseObserver;
            return new StreamObserver<>() {
                @Override
                public void onNext(TwinRequestProto twinRequestProto) {
                    CompletableFuture.runAsync(() -> {
                        try {
                            TwinRequest twinRequest = mapTwinRequestProto(twinRequestProto.toByteArray());
                            String tracingOperationKey = generateTracingOperationKey(twinRequest.getLocation(), twinRequest.getKey());
                            Tracer.SpanBuilder spanBuilder = TracingInfoCarrier.buildSpanFromTracingMetadata(getTracer(),
                                    tracingOperationKey, twinRequest.getTracingInfo(), References.FOLLOWS_FROM);
                            try (Scope scope = spanBuilder.startActive(true)){
                                TwinUpdate twinUpdate = getTwin(twinRequest);
                                addTracingInfo(scope.span(), twinUpdate);
                                TwinResponseProto twinResponseProto = mapTwinResponse(twinUpdate);
                                LOG.debug("Sent Twin response for key {} at location {}", twinRequest.getKey(), twinRequest.getLocation());
                                sendTwinResponse(twinResponseProto, rpcStream);
                            }
                        } catch (Exception e) {
                            LOG.error("Exception while processing request", e);
                        }
                    }, twinRpcExecutor);
                }

                @Override
                public void onError(Throwable throwable) {
                    LOG.error("Error in Rpc stream handler", throwable);
                }

                @Override
                public void onCompleted() {
                    LOG.info("Closed Rpc Stream handler");
                }
            };
        }

        private synchronized void sendTwinResponse(TwinResponseProto twinResponseProto, StreamObserver<TwinResponseProto> rpcStream) {
            if (rpcStream != null) {
                rpcStream.onNext(twinResponseProto);
            }
        }

        private synchronized void handleSinkStreamUpdate(MinionHeader request, StreamObserver<TwinResponseProto> responseObserver) {
            if (sinkStreamsBySystemId.containsKey(request.getSystemId())) {
                StreamObserver<org.opennms.core.ipc.twin.model.TwinResponseProto> sinkStream = sinkStreamsBySystemId.remove(request.getSystemId());
                sinkStreamsByLocation.remove(request.getLocation(), sinkStream);
            }
            sinkStreamsByLocation.put(request.getLocation(), responseObserver);
            sinkStreamsBySystemId.put(request.getSystemId(), responseObserver);

            forEachSession(((sessionKey, twinTracker) -> {
                if(sessionKey.location == null || sessionKey.location.equals(request.getLocation())) {
                    TwinUpdate twinUpdate = new TwinUpdate(sessionKey.key, sessionKey.location, twinTracker.getObj());
                    twinUpdate.setSessionId(twinTracker.getSessionId());
                    twinUpdate.setVersion(twinTracker.getVersion());
                    twinUpdate.setPatch(false);
                    TwinResponseProto twinResponseProto = mapTwinResponse(twinUpdate);
                    responseObserver.onNext(twinResponseProto);
                }
            }));
        }

        @Override
        public void sinkStreaming(org.opennms.core.ipc.twin.grpc.common.MinionHeader request,
                                  io.grpc.stub.StreamObserver<org.opennms.core.ipc.twin.model.TwinResponseProto> responseObserver) {
             handleSinkStreamUpdate(request, responseObserver);
        }

    }

}
