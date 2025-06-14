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
package org.opennms.core.ipc.twin.grpc.subscriber;


import com.codahale.metrics.MetricRegistry;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.stub.StreamObserver;
import org.opennms.core.grpc.common.GrpcIpcUtils;
import org.opennms.core.ipc.twin.common.AbstractTwinSubscriber;
import org.opennms.core.ipc.twin.api.TwinRequest;
import org.opennms.core.ipc.twin.api.TwinUpdate;
import org.opennms.core.ipc.twin.grpc.common.MinionHeader;
import org.opennms.core.ipc.twin.grpc.common.OpenNMSTwinIpcGrpc;
import org.opennms.core.ipc.twin.model.TwinRequestProto;
import org.opennms.core.ipc.twin.model.TwinResponseProto;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.distributed.core.api.MinionIdentity;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;


public class GrpcTwinSubscriber extends AbstractTwinSubscriber {

    private static final Logger LOG = LoggerFactory.getLogger(GrpcTwinSubscriber.class);
    private static final long RETRIEVAL_TIMEOUT = 1000;
    private static final int TWIN_REQUEST_POOL_SIZE = 100;
    private final int port;
    private final ConfigurationAdmin configAdmin;
    private ManagedChannel channel;
    private Properties clientProperties;
    private OpenNMSTwinIpcGrpc.OpenNMSTwinIpcStub asyncStub;
    private StreamObserver<TwinRequestProto> rpcStream;
    private AtomicBoolean isShutDown = new AtomicBoolean(false);
    private ResponseHandler responseHandler = new ResponseHandler();
    private final ThreadFactory twinRequestSenderThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("twin-request-sender-%d")
            .build();
    private final ScheduledExecutorService twinRequestSenderExecutor = Executors.newScheduledThreadPool(TWIN_REQUEST_POOL_SIZE,
            twinRequestSenderThreadFactory);

    public GrpcTwinSubscriber(MinionIdentity minionIdentity,
                              ConfigurationAdmin configAdmin,
                              TracerRegistry tracerRegistry,
                              MetricRegistry metricRegistry,
                              int port) {
        super(minionIdentity, tracerRegistry, metricRegistry);
        this.configAdmin = configAdmin;
        this.port = port;
    }

    public void start() throws IOException {
        // Twin inherits all properties from ipc client
        clientProperties = GrpcIpcUtils.getPropertiesFromConfig(configAdmin, GrpcIpcUtils.GRPC_CLIENT_PID);

        channel = GrpcIpcUtils.getChannel(clientProperties, this.port);

        asyncStub = OpenNMSTwinIpcGrpc.newStub(channel);
        retryInitializeRpcStream();
        LOG.info("Started Twin gRPC Subscriber at location {} with systemId {}", getIdentity().getLocation(), getIdentity().getId());

    }

    private boolean initRpcStream() {
        ConnectivityState currentChannelState = channel.getState(true);
        if (currentChannelState.equals(ConnectivityState.READY) && this.rpcStream == null) {
            this.rpcStream = asyncStub.rpcStreaming(responseHandler);
            // Send minion header whenever we re-initialize rpc stream.
            sendMinionHeader();
            return true;
        }
        return false;
    }

    private void retryInitializeRpcStream() {
        scheduleWithDelayUntilGetSucceeds(twinRequestSenderExecutor, this::initRpcStream, RETRIEVAL_TIMEOUT);
    }

    private synchronized void sendMinionHeader() {
        // Sink stream is unidirectional Response stream from OpenNMS <-> Minion.
        // gRPC Server needs at least one message to initialize the stream
        MinionHeader minionHeader = MinionHeader.newBuilder().setLocation(getIdentity().getLocation())
                                                .setSystemId(getIdentity().getId()).build();
        asyncStub.sinkStreaming(minionHeader, responseHandler);
    }


    public void close() throws IOException {
        isShutDown.set(true);
        super.close();
        if (channel != null) {
            channel.shutdown();
        }
        twinRequestSenderExecutor.shutdown();
    }

    @Override
    protected void sendRpcRequest(TwinRequest twinRequest) {
        try {
            TwinRequestProto twinRequestProto = mapTwinRequestToProto(twinRequest);
            // Send RPC Request asynchronously.
            CompletableFuture.runAsync(() -> retrySendRpcRequest(twinRequestProto), twinRequestSenderExecutor);
        } catch (Exception e) {
            LOG.error("Exception while sending request with key {}", twinRequest.getKey());
        }
    }

    private void retrySendRpcRequest(TwinRequestProto twinRequestProto) {
        // We can only send RPC If channel is active and RPC stream is not in error.
        // Schedule sending RPC request with given retrieval timeout until it succeeds.
        scheduleWithDelayUntilFunctionSucceeds(twinRequestSenderExecutor, this::sendTwinRpcRequest, RETRIEVAL_TIMEOUT, twinRequestProto);
    }

    private <T> void scheduleWithDelayUntilFunctionSucceeds(ScheduledExecutorService executorService,
                                                            Function<T, Boolean> function,
                                                            long delayInMsec,
                                                            T obj) {
        boolean succeeded = function.apply(obj);
        if (!succeeded) {
            do {
                ScheduledFuture<Boolean> future = executorService.schedule(() -> function.apply(obj), delayInMsec, TimeUnit.MILLISECONDS);
                try {
                    succeeded = future.get();
                    if (succeeded) {
                        break;
                    }
                } catch (Exception e) {
                    // It's likely that error persists, bail out
                    succeeded = true;
                    LOG.warn("Error while attempting to schedule the task", e);
                }
            } while (!succeeded || !isShutDown.get());
        }
    }

    private void scheduleWithDelayUntilGetSucceeds(ScheduledExecutorService executorService,
                                                   Supplier<Boolean> supplier,
                                                   long delayInMsec) {
        boolean succeeded = supplier.get();
        if (!succeeded) {
            do {
                ScheduledFuture<Boolean> future = executorService.schedule(() -> supplier.get(), delayInMsec, TimeUnit.MILLISECONDS);
                try {
                    succeeded = future.get();
                    if (succeeded) {
                        break;
                    }
                } catch (Exception e) {
                    // It's likely that error persists, bail out
                    succeeded = true;
                    LOG.warn("Error while attempting to schedule the task", e);
                }
            } while (!succeeded || !isShutDown.get());
        }
    }

    private synchronized boolean sendTwinRpcRequest(TwinRequestProto twinRequestProto) {
        if(this.rpcStream == null) {
            initRpcStream();
        }
        if (rpcStream != null) {
            rpcStream.onNext(twinRequestProto);
            return true;
        } else {
            return false;
        }
    }

    private class ResponseHandler implements StreamObserver<TwinResponseProto> {

        @Override
        public void onNext(TwinResponseProto twinResponseProto) {
            try {
                TwinUpdate twinUpdate = mapTwinResponseToProto(twinResponseProto.toByteArray());
                accept(twinUpdate);
            } catch (Exception e) {
                LOG.error("Exception while processing twin update for key {} ", twinResponseProto.getConsumerKey(), e);
            }
        }

        @Override
        public void onError(Throwable throwable) {
            LOG.error("Error in Twin streaming", throwable);
            rpcStream = null;
            CompletableFuture.runAsync(() -> retryInitializeRpcStream(), twinRequestSenderExecutor);
        }

        @Override
        public void onCompleted() {
            LOG.error("Closing Twin Response Handler");
            rpcStream = null;
            CompletableFuture.runAsync(() -> retryInitializeRpcStream(), twinRequestSenderExecutor);
        }

    }


}
