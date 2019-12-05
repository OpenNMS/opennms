/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.core.ipc.grpc.server;

import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.DEFAULT_ACK_TIMEOUT;
import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.DEFAULT_GRPC_PORT;
import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.DEFAULT_GRPC_TTL;
import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.GRPC_SERVER_PID;
import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.GRPC_SERVER_PORT;
import static org.opennms.core.rpc.api.RpcModule.MINION_HEADERS;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.opennms.core.ipc.grpc.common.ConfigUtils;
import org.opennms.core.ipc.grpc.common.Empty;
import org.opennms.core.ipc.grpc.common.OnmsIpcGrpc;
import org.opennms.core.ipc.grpc.common.RpcMessage;
import org.opennms.core.ipc.grpc.common.SinkMessage;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.common.AbstractMessageConsumerManager;
import org.opennms.core.logging.Logging;
import org.opennms.core.rpc.api.RemoteExecutionException;
import org.opennms.core.rpc.api.RequestTimedOutException;
import org.opennms.core.rpc.api.RpcClient;
import org.opennms.core.rpc.api.RpcClientFactory;
import org.opennms.core.rpc.api.RpcModule;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcResponse;
import org.opennms.core.rpc.api.RpcResponseHandler;
import org.opennms.core.utils.PropertiesUtils;
import org.opennms.distributed.core.api.Identity;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.ByteString;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

public class OpennmsGrpcServer extends AbstractMessageConsumerManager implements RpcClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(OpennmsGrpcServer.class);
    private ConfigurationAdmin configAdmin;
    private Server server;
    private String location;
    private Identity identity;
    private final ThreadFactory responseHandlerThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("rpc-response-handler-%d")
            .build();
    private final ThreadFactory timerThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("rpc-timeout-tracker-%d")
            .build();
    private final ExecutorService rpcTimeoutExecutor = Executors.newCachedThreadPool(timerThreadFactory);
    private final Map<String, RpcResponseHandler> rpcResponseMap = new ConcurrentHashMap<>();
    private DelayQueue<RpcResponseHandler> rpcTimeoutQueue = new DelayQueue<>();
    private DelayQueue<RpcId> rpcIdDelayQueue = new DelayQueue<>();
    private Map<String, RpcMessage> rpcRequestMap = new ConcurrentHashMap<>();
    private DelayQueue<DelayedRpcMessage> delayedRequestQueue = new DelayQueue<>();
    private Map<String, StreamObserver<RpcMessage>> rpcHandlerByMinionId = new HashMap<>();
    private Multimap<String, StreamObserver<RpcMessage>> rpcHandlerByLocation = LinkedListMultimap.create();
    ;
    private Map<Collection<StreamObserver<RpcMessage>>, Iterator<StreamObserver<RpcMessage>>> observerIteratorMap = new ConcurrentHashMap<>();
    private final Map<String, SinkModule<?, Message>> modulesById = new ConcurrentHashMap<>();
    private final ExecutorService responseHandlerExecutor = Executors.newCachedThreadPool(responseHandlerThreadFactory);

    public void start() throws IOException {
        Properties properties = ConfigUtils.getPropertiesFromConfig(configAdmin, GRPC_SERVER_PID);
        int port = PropertiesUtils.getProperty(properties, GRPC_SERVER_PORT, DEFAULT_GRPC_PORT);
        server = ServerBuilder.forPort(port)
                .addService(new OnmsIpcService()).build();
        rpcTimeoutExecutor.execute(this::handleRpcTimeouts);
        rpcTimeoutExecutor.execute(this::handleDelayedRequests);
        rpcTimeoutExecutor.execute(this::handleRpcAcknowledgement);
        server.start();
        LOG.info("OpenNMS gRPC server started");
    }

    public void shutdown() {
        server.shutdown();
        rpcTimeoutExecutor.shutdown();
        responseHandlerExecutor.shutdown();
        LOG.info("OpenNMS gRPC server stopped");
    }

    @Override
    public void startConsumingForModule(SinkModule<?, Message> module) throws Exception {
        modulesById.putIfAbsent(module.getId(), module);
    }

    @Override
    public void stopConsumingForModule(SinkModule<?, Message> module) throws Exception {
        modulesById.values().remove(module);
    }

    @Override
    public <S extends RpcRequest, T extends RpcResponse> RpcClient<S, T> getClient(RpcModule<S, T> module) {

        return new RpcClient<S, T>() {
            @Override
            public CompletableFuture<T> execute(S request) {
                if (request.getLocation() == null || request.getLocation().equals(getLocation())) {
                    // The request is for the current location, invoke it directly
                    return module.execute(request);
                }
                final Map<String, String> loggingContext = Logging.getCopyOfContextMap();
                String marshalRequest = module.marshalRequest(request);
                String rpcId = UUID.randomUUID().toString();
                CompletableFuture<T> future = new CompletableFuture<T>();
                Long ttl = request.getTimeToLiveMs();
                ttl = (ttl != null && ttl > 0) ? ttl : DEFAULT_GRPC_TTL;
                long expirationTime = System.currentTimeMillis() + ttl;
                RpcResponseHandlerImpl responseHandler = new RpcResponseHandlerImpl<S, T>(future,
                        module, rpcId, expirationTime, loggingContext);
                rpcResponseMap.put(rpcId, responseHandler);
                rpcTimeoutQueue.offer(responseHandler);
                RpcMessage.Builder builder = RpcMessage.newBuilder()
                        .setRpcId(rpcId)
                        .setLocation(request.getLocation())
                        .setModuleId(module.getId())
                        .setRpcContent(ByteString.copyFrom(marshalRequest.getBytes()));
                if (!Strings.isNullOrEmpty(request.getSystemId())) {
                    builder.setSystemId(request.getSystemId());
                }
                RpcMessage rpcMessage = builder.build();
                boolean succeeded = sendRequest(rpcMessage);
                if (!succeeded) {
                    long delayedTime = System.currentTimeMillis() + DEFAULT_ACK_TIMEOUT;
                    delayedRequestQueue.offer(new DelayedRpcMessage(rpcMessage, delayedTime));
                }
                return future;
            }
        };
    }


    private void handleRpcTimeouts() {
        while (true) {
            try {
                RpcResponseHandler responseHandler = rpcTimeoutQueue.take();
                if (!responseHandler.isProcessed()) {
                    LOG.warn("RPC request from module {} with id {} timedout ", responseHandler.getRpcModule().getId(),
                            responseHandler.getRpcId());
                    responseHandlerExecutor.execute(() -> responseHandler.sendResponse(null));
                }
            } catch (InterruptedException e) {
                LOG.info("interrupted while waiting for an element from rpcTimeoutQueue", e);
                break;
            } catch (Exception e) {
                LOG.warn("error while sending response from timeout handler", e);
            }
        }
    }

    private void handleDelayedRequests() {
        while (true) {
            try {
                DelayedRpcMessage delayedRpcMessage = delayedRequestQueue.take();
                LOG.debug("DelayedRequest : Re-attempting RPC request with Id {}", delayedRpcMessage.getRpcMessage().getRpcId());
                sendRequest(delayedRpcMessage.getRpcMessage());
            } catch (InterruptedException e) {
                LOG.info("interrupted while waiting for an element from rpcTimeoutQueue", e);
                break;
            } catch (Exception e) {
                LOG.warn("error while sending response from timeout handler", e);
            }
        }
    }

    private void handleRpcAcknowledgement() {
        while (true) {
            try {
                RpcId rpcId = rpcIdDelayQueue.take();
                RpcMessage rpcMessage = rpcRequestMap.get(rpcId.getRpcId());
                if (rpcMessage != null) {
                    LOG.debug("AckMissed : Re-attempting RPC request with Id {}", rpcId.getRpcId());
                    sendRequest(rpcMessage);
                }
            } catch (InterruptedException e) {
                LOG.info("interrupted while waiting for an element from rpcTimeoutQueue", e);
                break;
            } catch (Exception e) {
                LOG.warn("error while sending response from timeout handler", e);
            }
        }
    }


    private void handleResponse(RpcMessage rpcMessage) {
        // Handle response from the Minion.
        LOG.debug("Received message from at location {} for module {}", rpcMessage.getLocation(), rpcMessage.getModuleId());
        RpcResponseHandler responseHandler = rpcResponseMap.get(rpcMessage.getRpcId());
        if (responseHandler != null) {
            responseHandler.sendResponse(rpcMessage.getRpcContent().toStringUtf8());
        }
    }

    private boolean sendRequest(RpcMessage rpcMessage) {
        StreamObserver<RpcMessage> rpcHandler = getRpcHandler(rpcMessage.getLocation(), rpcMessage.getSystemId());
        if (rpcHandler == null) {
            LOG.warn("No RPC handlers found for location {}", getLocation());
            return false;
        }
        try {
            sendRpcRequest(rpcHandler, rpcMessage);
            long expirationTime = System.currentTimeMillis() + DEFAULT_ACK_TIMEOUT;
            if (rpcRequestMap.get(rpcMessage.getRpcId()) == null) {
                rpcIdDelayQueue.offer(new RpcId(rpcMessage.getRpcId(), expirationTime));
                rpcRequestMap.put(rpcMessage.getRpcId(), rpcMessage);
            }
            LOG.debug("Request with id {} being sent", rpcMessage.getRpcId(), rpcMessage.getModuleId());
            return true;
        } catch (Throwable e) {
            LOG.error("Encountered exception while sending request {}", rpcMessage, e);
            removeRpcHandler(rpcHandler);
        }
        return false;
    }

    private synchronized void sendRpcRequest(StreamObserver<RpcMessage> rpcHandler, RpcMessage rpcMessage) {
        rpcHandler.onNext(rpcMessage);
    }

    private StreamObserver<RpcMessage> getRpcHandler(String location, String systemId) {

        if (!Strings.isNullOrEmpty(systemId)) {
            return rpcHandlerByMinionId.get(systemId);
        }
        Collection<StreamObserver<RpcMessage>> streamObservers = rpcHandlerByLocation.get(location);
        if (streamObservers.isEmpty()) {
            return null;
        }
        Iterator<StreamObserver<RpcMessage>> iterator = observerIteratorMap.get(streamObservers);
        if (iterator == null) {
            iterator = Iterables.cycle(streamObservers).iterator();
            observerIteratorMap.put(streamObservers, iterator);
        }
        return iterator.next();
    }

    private synchronized void addRpcHandler(String location, String systemId, StreamObserver<RpcMessage> rpcHandler) {
        if (!rpcHandlerByLocation.containsValue(rpcHandler)) {
            StreamObserver<RpcMessage> obsoleteObserver = rpcHandlerByMinionId.get(systemId);
            if (obsoleteObserver != null) {
                rpcHandlerByLocation.values().remove(obsoleteObserver);
            }
            rpcHandlerByLocation.put(location, rpcHandler);
            rpcHandlerByMinionId.put(systemId, rpcHandler);
            LOG.info("Added rpc observer for minion {} at location {}", systemId, location);
        }
    }

    private synchronized void removeRpcHandler(StreamObserver<RpcMessage> rpcHandler) {

        if (rpcHandlerByLocation.containsValue(rpcHandler)) {
            rpcHandlerByLocation.values().remove(rpcHandler);
            rpcHandlerByMinionId.values().remove(rpcHandler);
            try {
                rpcHandler.onCompleted();
            } catch (Throwable e) {
                LOG.error("Exception while closing rpc handler", e);
            }
            LOG.info("Removed rpc observer");
        }
    }


    private boolean isHeaders(RpcMessage rpcMessage) {
        return !Strings.isNullOrEmpty(rpcMessage.getModuleId()) &&
                rpcMessage.getModuleId().equals(MINION_HEADERS);
    }

    Server getServer() {
        return server;
    }

    public String getLocation() {
        if (location == null && getIdentity() != null) {
            return getIdentity().getLocation();
        }
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Identity getIdentity() {
        return identity;
    }

    public void setIdentity(Identity identity) {
        this.identity = identity;
    }

    public void setConfigAdmin(ConfigurationAdmin configAdmin) {
        this.configAdmin = configAdmin;
    }

    private class OnmsIpcService extends OnmsIpcGrpc.OnmsIpcImplBase {

        @Override
        public StreamObserver<RpcMessage> rpcStreaming(
                StreamObserver<RpcMessage> responseObserver) {

            return new StreamObserver<RpcMessage>() {

                @Override
                public void onNext(RpcMessage rpcMessage) {
                    // Register client when message is metadata.
                    LOG.debug("Received RPC message from module {}", rpcMessage.getModuleId());
                    if (isHeaders(rpcMessage)) {
                        if (!Strings.isNullOrEmpty(rpcMessage.getSystemId()) && rpcMessage.getRpcId().equals(rpcMessage.getSystemId())) {
                            addRpcHandler(rpcMessage.getLocation(), rpcMessage.getSystemId(), responseObserver);
                        } else {
                            rpcRequestMap.remove(rpcMessage.getRpcId());
                        }
                    } else {
                        responseHandlerExecutor.execute(() -> handleResponse(rpcMessage));
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    LOG.error("Error in rpc streaming", throwable);
                    removeRpcHandler(responseObserver);
                }

                @Override
                public void onCompleted() {
                    LOG.info("Minion is shutting down, received onCompleted event");
                    removeRpcHandler(responseObserver);
                }
            };
        }

        public io.grpc.stub.StreamObserver<SinkMessage> sinkStreaming(
                io.grpc.stub.StreamObserver<Empty> responseObserver) {

            return new StreamObserver<SinkMessage>() {

                @Override
                public void onNext(SinkMessage sinkMessage) {
                    LOG.debug("Received sink message from module {}", sinkMessage.getModuleId());
                    SinkModule<?, Message> sinkModule = modulesById.get(sinkMessage.getModuleId());
                    if (sinkModule != null) {
                        Message message = sinkModule.unmarshal(sinkMessage.getContent().toByteArray());
                        dispatch(sinkModule, message);
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    LOG.error("Error in sink streaming", throwable);
                }

                @Override
                public void onCompleted() {

                }
            };
        }
    }

    private class RpcResponseHandlerImpl<S extends RpcRequest, T extends RpcResponse> implements RpcResponseHandler {

        private final CompletableFuture<T> responseFuture;
        private final RpcModule<S, T> rpcModule;
        private final String rpcId;
        private final long expirationTime;
        private final Map<String, String> loggingContext;
        private boolean isProcessed = false;

        private RpcResponseHandlerImpl(CompletableFuture<T> responseFuture, RpcModule<S, T> rpcModule, String rpcId,
                                       long timeout, Map<String, String> loggingContext) {
            this.responseFuture = responseFuture;
            this.rpcModule = rpcModule;
            this.rpcId = rpcId;
            this.expirationTime = timeout;
            this.loggingContext = loggingContext;
        }

        @Override
        public void sendResponse(String message) {

            try (Logging.MDCCloseable mdc = Logging.withContextMapCloseable(loggingContext)) {
                if (message != null) {
                    T response = rpcModule.unmarshalResponse(message);
                    if (response.getErrorMessage() != null) {
                        responseFuture.completeExceptionally(new RemoteExecutionException(response.getErrorMessage()));
                    } else {
                        LOG.debug("Response handled successfully");
                        responseFuture.complete(response);
                    }
                    isProcessed = true;
                } else {
                    responseFuture.completeExceptionally(new RequestTimedOutException(new TimeoutException()));
                }
                rpcResponseMap.remove(rpcId);
            } catch (Throwable e) {
                LOG.error("Error while processing response {}", message, e);
            }
        }

        @Override
        public boolean isProcessed() {
            return isProcessed;
        }

        @Override
        public String getRpcId() {
            return rpcId;
        }


        @Override
        public int compareTo(Delayed other) {
            long myDelay = getDelay(TimeUnit.MILLISECONDS);
            long otherDelay = other.getDelay(TimeUnit.MILLISECONDS);
            return Long.compare(myDelay, otherDelay);
        }

        @Override
        public long getDelay(TimeUnit unit) {
            long now = System.currentTimeMillis();
            return unit.convert(expirationTime - now, TimeUnit.MILLISECONDS);
        }

        public RpcModule<S, T> getRpcModule() {
            return rpcModule;
        }
    }

}
