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

import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.CLIENT_CERTIFICATE_FILE_PATH;
import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.DEFAULT_GRPC_PORT;
import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.DEFAULT_GRPC_TTL;
import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.DEFAULT_MESSAGE_SIZE;
import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.GRPC_MAX_INBOUND_SIZE;
import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.GRPC_SERVER_PID;
import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.GRPC_SERVER_PORT;
import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.GRPC_TTL_PROPERTY;
import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.PRIVATE_KEY_FILE_PATH;
import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.SERVER_CERTIFICATE_FILE_PATH;
import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.TLS_ENABLED;
import static org.opennms.core.rpc.api.RpcModule.MINION_HEADERS_MODULE;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.ByteString;

import io.grpc.Server;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.ClientAuth;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslProvider;
import io.grpc.stub.StreamObserver;

public class OpennmsGrpcServer extends AbstractMessageConsumerManager implements RpcClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(OpennmsGrpcServer.class);
    private ConfigurationAdmin configAdmin;
    private Server server;
    private String location;
    private Identity identity;
    private Properties properties;
    private long ttl;
    private final ThreadFactory responseHandlerThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("rpc-response-handler-%d")
            .build();
    private final ThreadFactory timerThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("rpc-timeout-tracker-%d")
            .build();
    private final ExecutorService rpcTimeoutExecutor = Executors.newSingleThreadExecutor(timerThreadFactory);
    private final Map<String, RpcResponseHandler> rpcResponseMap = new ConcurrentHashMap<>();
    private DelayQueue<RpcResponseHandler> rpcTimeoutQueue = new DelayQueue<>();
    private Map<String, StreamObserver<RpcMessage>> rpcHandlerByMinionId = new HashMap<>();
    private Multimap<String, StreamObserver<RpcMessage>> rpcHandlerByLocation = LinkedListMultimap.create();
    private Map<Collection<StreamObserver<RpcMessage>>, Iterator<StreamObserver<RpcMessage>>> observerIteratorMap = new ConcurrentHashMap<>();

    private final Map<String, SinkModule<?, Message>> sinkModulesById = new ConcurrentHashMap<>();
    private final ExecutorService responseHandlerExecutor = Executors.newCachedThreadPool(responseHandlerThreadFactory);

    public void start() throws IOException {
        properties = ConfigUtils.getPropertiesFromConfig(configAdmin, GRPC_SERVER_PID);
        int port = PropertiesUtils.getProperty(properties, GRPC_SERVER_PORT, DEFAULT_GRPC_PORT);
        int maxInboundMessageSize = PropertiesUtils.getProperty(properties, GRPC_MAX_INBOUND_SIZE, DEFAULT_MESSAGE_SIZE);
        ttl = PropertiesUtils.getProperty(properties, GRPC_TTL_PROPERTY, DEFAULT_GRPC_TTL);
        boolean tlsEnabled = PropertiesUtils.getProperty(properties, TLS_ENABLED, false);

        NettyServerBuilder serverBuilder = NettyServerBuilder.forAddress(new InetSocketAddress(port))
                .addService(new OnmsIpcService())
                .maxInboundMessageSize(maxInboundMessageSize);
        if (tlsEnabled) {
            SslContextBuilder sslContextBuilder = getSslContextBuilder();
            if (sslContextBuilder != null) {
                serverBuilder.sslContext(sslContextBuilder.build());
                LOG.info("tls enabled for gRPC");
            }
        }
        server = serverBuilder.build();

        rpcTimeoutExecutor.execute(this::handleRpcTimeouts);
        server.start();
        LOG.info("OpenNMS gRPC server started");
    }


    private SslContextBuilder getSslContextBuilder() {
        String certChainFilePath = properties.getProperty(SERVER_CERTIFICATE_FILE_PATH);
        String privateKeyFilePath = properties.getProperty(PRIVATE_KEY_FILE_PATH);
        String clientCertChainFilePath = properties.getProperty(CLIENT_CERTIFICATE_FILE_PATH);
        if (Strings.isNullOrEmpty(certChainFilePath) || Strings.isNullOrEmpty(privateKeyFilePath)) {
            return null;
        }

        SslContextBuilder sslClientContextBuilder = SslContextBuilder.forServer(new File(certChainFilePath),
                new File(privateKeyFilePath));
        if (!Strings.isNullOrEmpty(clientCertChainFilePath)) {
            sslClientContextBuilder.trustManager(new File(clientCertChainFilePath));
            sslClientContextBuilder.clientAuth(ClientAuth.OPTIONAL);
        }
        return GrpcSslContexts.configure(sslClientContextBuilder,
                SslProvider.OPENSSL);
    }


    @Override
    public void startConsumingForModule(SinkModule<?, Message> module) throws Exception {
        sinkModulesById.putIfAbsent(module.getId(), module);
    }

    @Override
    public void stopConsumingForModule(SinkModule<?, Message> module) throws Exception {
        sinkModulesById.values().remove(module);
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
                Long timeToLive = request.getTimeToLiveMs();
                timeToLive = (timeToLive != null && timeToLive > 0) ? timeToLive : ttl;
                long expirationTime = System.currentTimeMillis() + timeToLive;
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
                    future.completeExceptionally(new RuntimeException("No minion found at location " + request.getLocation()));
                    return future;
                }
                LOG.debug("RPC request with RpcId {} sent to minion at location {}", rpcId, request.getLocation());
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


    private void handleResponse(RpcMessage rpcMessage) {
        // Handle response from the Minion.
        RpcResponseHandler responseHandler = rpcResponseMap.get(rpcMessage.getRpcId());
        if (responseHandler != null) {
            responseHandler.sendResponse(rpcMessage.getRpcContent().toStringUtf8());
        } else {
            LOG.debug("Received a response for request with ID:{}, but no outstanding request was found with this id." +
                    "The request may have timed out", rpcMessage.getRpcId());
        }
    }

    private boolean sendRequest(RpcMessage rpcMessage) {
        StreamObserver<RpcMessage> rpcHandler = getRpcHandler(rpcMessage.getLocation(), rpcMessage.getSystemId());
        if (rpcHandler == null) {
            LOG.warn("No RPC handlers found for location {}", rpcMessage.getLocation());
            return false;
        }
        try {
            sendRpcRequest(rpcHandler, rpcMessage);
            return true;
        } catch (Throwable e) {
            LOG.error("Encountered exception while sending request {}", rpcMessage, e);
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


    private boolean isHeaders(RpcMessage rpcMessage) {
        return !Strings.isNullOrEmpty(rpcMessage.getModuleId()) &&
                rpcMessage.getModuleId().equals(MINION_HEADERS_MODULE);
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


    public void shutdown() {
        server.shutdown();
        rpcTimeoutExecutor.shutdown();
        responseHandlerExecutor.shutdown();
        LOG.info("OpenNMS gRPC server stopped");
    }

    @VisibleForTesting
    public Multimap<String, StreamObserver<RpcMessage>> getRpcHandlerByLocation() {
        return rpcHandlerByLocation;
    }

    private class OnmsIpcService extends OnmsIpcGrpc.OnmsIpcImplBase {

        @Override
        public StreamObserver<RpcMessage> rpcStreaming(
                StreamObserver<RpcMessage> responseObserver) {

            return new StreamObserver<RpcMessage>() {

                @Override
                public void onNext(RpcMessage rpcMessage) {
                    // Register client when message is metadata.
                    if (isHeaders(rpcMessage)) {
                        addRpcHandler(rpcMessage.getLocation(), rpcMessage.getSystemId(), responseObserver);
                    } else {
                        responseHandlerExecutor.execute(() -> handleResponse(rpcMessage));
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    LOG.error("Error in rpc streaming", throwable);
                }

                @Override
                public void onCompleted() {
                    LOG.info("Minion RPC handler closed");
                }
            };
        }

        public io.grpc.stub.StreamObserver<SinkMessage> sinkStreaming(
                io.grpc.stub.StreamObserver<Empty> responseObserver) {

            return new StreamObserver<SinkMessage>() {

                @Override
                public void onNext(SinkMessage sinkMessage) {
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Received sink message {} from module {}", sinkMessage, sinkMessage.getModuleId());
                    }
                    SinkModule<?, Message> sinkModule = sinkModulesById.get(sinkMessage.getModuleId());
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
                        responseFuture.complete(response);
                    }
                    LOG.debug("RPC Response handled successfully for RpcId = {}", rpcId);
                    isProcessed = true;
                } else {
                    LOG.warn("RPC request with id {} timedout", rpcId);
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
