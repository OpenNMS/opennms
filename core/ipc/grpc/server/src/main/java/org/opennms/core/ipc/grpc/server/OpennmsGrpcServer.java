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

import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.DEFAULT_GRPC_TTL;
import static org.opennms.core.ipc.grpc.server.GrpcServerConstants.GRPC_TTL_PROPERTY;
import static org.opennms.core.ipc.sink.api.Message.SINK_METRIC_CONSUMER_DOMAIN;
import static org.opennms.core.rpc.api.RpcModule.MINION_HEADERS_MODULE;
import static org.opennms.core.tracing.api.TracerConstants.TAG_LOCATION;
import static org.opennms.core.tracing.api.TracerConstants.TAG_SYSTEM_ID;
import static org.opennms.core.tracing.api.TracerConstants.TAG_TIMEOUT;

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
import java.util.concurrent.atomic.AtomicBoolean;

import org.opennms.core.grpc.common.GrpcIpcServer;
import org.opennms.core.ipc.grpc.common.Empty;
import org.opennms.core.ipc.grpc.common.OpenNMSIpcGrpc;
import org.opennms.core.ipc.grpc.common.RpcRequestProto;
import org.opennms.core.ipc.grpc.common.RpcResponseProto;
import org.opennms.core.ipc.grpc.common.SinkMessage;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
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
import org.opennms.core.tracing.api.TracerConstants;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.core.tracing.util.TracingInfoCarrier;
import org.opennms.core.utils.PropertiesUtils;
import org.opennms.distributed.core.api.Identity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.ByteString;

import io.grpc.stub.StreamObserver;
import io.opentracing.References;
import io.opentracing.Scope;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.util.GlobalTracer;

/**
 * OpenNMS GRPC Server runs as OSGI bundle and it runs both RPC/Sink together.
 * gRPC runs in a typical web server/client mode, so gRPC client runs on each minion and gRPC server runs on OpenNMS.
 * Server initializes and creates two observers (RPC/Sink) that receive messages from the client (Minion).
 * <p>
 * RPC : RPC runs in bi-directional streaming mode. OpenNMS needs a client(minion) handle for sending RPC request
 * so minion always sends it's headers (SystemId/location) when it initializes. This Server maintains a list of
 * client(minion) handles and sends RPC request to each minion in round-robin fashion. When it is directed RPC, server
 * invokes specific minion handle directly.
 * For each RPC request received, server creates a rpcId and maintains the state of this request in the concurrent map.
 * The request is also added to a delay queue which can timeout the request if response is not received within expiration
 * time. RPC responses are received in the observers that are created at start. Each response handling is done in a
 * separate thread which may be used by rpc module to process the response.
 * <p>
 * Sink: Sink runs in uni-directional streaming mode. OpenNMS receives sink messages from client and they are dispatched
 * in the consumer threads that are initialized at start.
 */

public class OpennmsGrpcServer extends AbstractMessageConsumerManager implements RpcClientFactory {

    private static final Logger LOG = LoggerFactory.getLogger(OpennmsGrpcServer.class);
    private final GrpcIpcServer grpcIpcServer;
    private String location;
    private Identity identity;
    private Properties properties;
    private long ttl;
    private MetricRegistry rpcMetrics;
    private MetricRegistry sinkMetrics;
    private JmxReporter rpcMetricsReporter;
    private JmxReporter sinkMetricsReporter;
    private TracerRegistry tracerRegistry;
    private AtomicBoolean closed = new AtomicBoolean(false);
    private final ThreadFactory responseHandlerThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("rpc-response-handler-%d")
            .build();
    private final ThreadFactory timerThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("rpc-timeout-tracker-%d")
            .build();
    private final ThreadFactory sinkConsumerThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("sink-consumer-%d")
            .build();

    // RPC timeout executor thread retrieves elements from delay queue used to timeout rpc requests.
    private final ExecutorService rpcTimeoutExecutor = Executors.newSingleThreadExecutor(timerThreadFactory);
    // Each RPC response is handled on a new thread which does unmarshalling and returning response to corresponding module.
    private final ExecutorService responseHandlerExecutor = Executors.newCachedThreadPool(responseHandlerThreadFactory);
    // This map used to maintain all the requests that are sent with unique Id and all the context related to the request.
    private final Map<String, RpcResponseHandler> rpcResponseMap = new ConcurrentHashMap<>();
    // Delay queue maintains the priority queue of RPC requests and times out the requests if no response was received
    // within the delay specified.
    private DelayQueue<RpcResponseHandler> rpcTimeoutQueue = new DelayQueue<>();
    // Maintains map of minionId and rpc handler for that minion. Used for directed RPC requests.
    private Map<String, StreamObserver<RpcRequestProto>> rpcHandlerByMinionId = new HashMap<>();
    // Maintains multi element map of location and rpc handlers for that location.
    // Used to get one of the rpc handlers for a specific location.
    private Multimap<String, StreamObserver<RpcRequestProto>> rpcHandlerByLocation = LinkedListMultimap.create();
    // Maintains the state of iteration for the list of minions for a given location.
    private Map<String, Iterator<StreamObserver<RpcRequestProto>>> rpcHandlerIteratorMap = new HashMap<>();
    // Maintains the map of sink modules by it's id.
    private final Map<String, SinkModule<?, Message>> sinkModulesById = new ConcurrentHashMap<>();
    // Maintains the map of sink consumer executor and by module Id.
    private final Map<String, ExecutorService> sinkConsumersByModuleId = new ConcurrentHashMap<>();

    public OpennmsGrpcServer(GrpcIpcServer grpcIpcServer) {
        this.grpcIpcServer = grpcIpcServer;
    }


    public void start() throws IOException {
        try (Logging.MDCCloseable mdc = Logging.withPrefixCloseable(RpcClientFactory.LOG_PREFIX)) {

            grpcIpcServer.startServer(new OpennmsIpcService());
            LOG.info("Added RPC/Sink Service to OpenNMS IPC Grpc Server");

            properties = grpcIpcServer.getProperties();
            ttl = PropertiesUtils.getProperty(properties, GRPC_TTL_PROPERTY, DEFAULT_GRPC_TTL);
            rpcTimeoutExecutor.execute(this::handleRpcTimeouts);
            rpcMetricsReporter = JmxReporter.forRegistry(getRpcMetrics())
                    .inDomain(JMX_DOMAIN_RPC)
                    .build();
            rpcMetricsReporter.start();
            sinkMetricsReporter = JmxReporter.forRegistry(getRpcMetrics())
                    .inDomain(SINK_METRIC_CONSUMER_DOMAIN)
                    .build();
            sinkMetricsReporter.start();
            // Initialize tracer from tracer registry.
            if (tracerRegistry != null) {
                tracerRegistry.init(identity.getId());
            }
        }
    }

    @Override
    protected void startConsumingForModule(SinkModule<?, Message> module) throws Exception {
        if (sinkConsumersByModuleId.get(module.getId()) == null) {
            int numOfThreads = getNumConsumerThreads(module);
            ExecutorService executor = Executors.newFixedThreadPool(numOfThreads, sinkConsumerThreadFactory);
            sinkConsumersByModuleId.put(module.getId(), executor);
            LOG.info("Adding {} consumers for module: {}", numOfThreads, module.getId());
        }
        sinkModulesById.putIfAbsent(module.getId(), module);
    }

    @Override
    protected void stopConsumingForModule(SinkModule<?, Message> module) throws Exception {

        ExecutorService executor = sinkConsumersByModuleId.get(module.getId());
        if (executor != null) {
            executor.shutdownNow();
        }
        LOG.info("Stopped consumers for module: {}", module.getId());
        sinkModulesById.remove(module.getId());
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

                Span span = getTracer().buildSpan(module.getId()).start();
                String marshalRequest = module.marshalRequest(request);
                String rpcId = UUID.randomUUID().toString();
                CompletableFuture<T> future = new CompletableFuture<T>();
                Long timeToLive = request.getTimeToLiveMs();
                timeToLive = (timeToLive != null && timeToLive > 0) ? timeToLive : ttl;
                long expirationTime = System.currentTimeMillis() + timeToLive;
                RpcResponseHandlerImpl responseHandler = new RpcResponseHandlerImpl<S, T>(future,
                        module, rpcId, request.getLocation(), expirationTime, span, loggingContext);
                rpcResponseMap.put(rpcId, responseHandler);
                rpcTimeoutQueue.offer(responseHandler);
                RpcRequestProto.Builder builder = RpcRequestProto.newBuilder()
                        .setRpcId(rpcId)
                        .setLocation(request.getLocation())
                        .setModuleId(module.getId())
                        .setRpcContent(ByteString.copyFrom(marshalRequest.getBytes()));
                if (!Strings.isNullOrEmpty(request.getSystemId())) {
                    builder.setSystemId(request.getSystemId());
                }
                addTracingInfo(request, span, builder);
                RpcRequestProto requestProto = builder.build();

                boolean succeeded = sendRequest(requestProto);

                addMetrics(request, requestProto.getSerializedSize());
                if (!succeeded) {
                    RpcClientFactory.markFailed(getRpcMetrics(), request.getLocation(), module.getId());
                    future.completeExceptionally(new RuntimeException("No minion found at location " + request.getLocation()));
                    return future;
                }
                LOG.debug("RPC request from module: {} with RpcId:{} sent to minion at location {}", module.getId(), rpcId, request.getLocation());
                return future;
            }

            private void addMetrics(RpcRequest request, int messageLen) {
                RpcClientFactory.markRpcCount(getRpcMetrics(), request.getLocation(), module.getId());
                RpcClientFactory.updateRequestSize(getRpcMetrics(), request.getLocation(), module.getId(), messageLen);
            }

            private void addTracingInfo(RpcRequest request, Span span, RpcRequestProto.Builder builder) {
                //Add tags to span.
                span.setTag(TAG_LOCATION, request.getLocation());
                if (request.getSystemId() != null) {
                    span.setTag(TAG_SYSTEM_ID, request.getSystemId());
                }
                request.getTracingInfo().forEach(span::setTag);
                TracingInfoCarrier tracingInfoCarrier = new TracingInfoCarrier();
                getTracer().inject(span.context(), Format.Builtin.TEXT_MAP, tracingInfoCarrier);
                // Tracer adds it's own metadata.
                tracingInfoCarrier.getTracingInfoMap().forEach(builder::putTracingInfo);
                //Add custom tags from RpcRequest.
                request.getTracingInfo().forEach(builder::putTracingInfo);
            }
        };
    }


    private void handleRpcTimeouts() {
        while (!closed.get()) {
            try {
                RpcResponseHandler responseHandler = rpcTimeoutQueue.take();
                if (!responseHandler.isProcessed()) {
                    LOG.warn("RPC request from module: {} with RpcId:{} timedout ", responseHandler.getRpcModule().getId(),
                            responseHandler.getRpcId());
                    responseHandlerExecutor.execute(() -> responseHandler.sendResponse(null));
                }
            } catch (InterruptedException e) {
                LOG.info("interrupted while waiting for an element from rpcTimeoutQueue", e);
                Thread.currentThread().interrupt();
                break;
            } catch (Exception e) {
                LOG.warn("error while sending response from timeout handler", e);
            }
        }
    }


    private void handleResponse(RpcResponseProto responseProto) {

        if (Strings.isNullOrEmpty(responseProto.getRpcId())) {
            return;
        }
        // Handle response from the Minion.
        RpcResponseHandler responseHandler = rpcResponseMap.get(responseProto.getRpcId());
        if (responseHandler != null && responseProto.getRpcContent() != null) {
            responseHandler.sendResponse(responseProto.getRpcContent().toStringUtf8());
        } else {
            LOG.debug("Received a response for request for module: {} with RpcId:{}, but no outstanding request was found with this id." +
                    "The request may have timed out", responseProto.getModuleId(), responseProto.getRpcId());
        }
    }

    private boolean sendRequest(RpcRequestProto requestProto) {
        StreamObserver<RpcRequestProto> rpcHandler = getRpcHandler(requestProto.getLocation(), requestProto.getSystemId());
        if (rpcHandler == null) {
            LOG.warn("No RPC handlers found for location {}", requestProto.getLocation());
            return false;
        }
        try {
            sendRpcRequest(rpcHandler, requestProto);
            return true;
        } catch (Throwable e) {
            LOG.error("Encountered exception while sending request {}", requestProto, e);
        }
        return false;
    }

    /**
     * Writing message through stream observer is not thread safe.
     */
    private synchronized void sendRpcRequest(StreamObserver<RpcRequestProto> rpcHandler, RpcRequestProto rpcMessage) {
        rpcHandler.onNext(rpcMessage);
    }

    @VisibleForTesting
    public synchronized StreamObserver<RpcRequestProto> getRpcHandler(String location, String systemId) {

        if (!Strings.isNullOrEmpty(systemId)) {
            return rpcHandlerByMinionId.get(systemId);
        }
        Iterator<StreamObserver<RpcRequestProto>> iterator = rpcHandlerIteratorMap.get(location);
        if (iterator == null) {
            return null;
        }
        return iterator.next();
    }

    private synchronized void addRpcHandler(String location, String systemId, StreamObserver<RpcRequestProto> rpcHandler) {
        if (Strings.isNullOrEmpty(location) || Strings.isNullOrEmpty(systemId)) {
            LOG.error("Invalid metadata received with location = {} , systemId = {}", location, systemId);
            return;
        }
        if (!rpcHandlerByLocation.containsValue(rpcHandler)) {
            StreamObserver<RpcRequestProto> obsoleteObserver = rpcHandlerByMinionId.get(systemId);
            if (obsoleteObserver != null) {
                rpcHandlerByLocation.values().remove(obsoleteObserver);
            }
            rpcHandlerByLocation.put(location, rpcHandler);
            updateIterator(location);
            rpcHandlerByMinionId.put(systemId, rpcHandler);
            LOG.info("Added RPC handler for minion {} at location {}", systemId, location);
        }
    }

    private synchronized void updateIterator(String location) {
        Collection<StreamObserver<RpcRequestProto>> streamObservers = rpcHandlerByLocation.get(location);
        Iterator<StreamObserver<RpcRequestProto>> iterator = Iterables.cycle(streamObservers).iterator();
        rpcHandlerIteratorMap.put(location, iterator);
    }


    private synchronized void removeRpcHandler(StreamObserver<RpcRequestProto> rpcHandler) {

        Map.Entry<String, StreamObserver<RpcRequestProto>> matchingHandler =
                rpcHandlerByLocation.entries().stream().
                        filter(entry -> entry.getValue().equals(rpcHandler)).findFirst().orElse(null);
        if (matchingHandler != null) {
            rpcHandlerByLocation.remove(matchingHandler.getKey(), matchingHandler.getValue());
            updateIterator(matchingHandler.getKey());
        }
    }


    private boolean isHeaders(RpcResponseProto rpcMessage) {
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

    private MetricRegistry getRpcMetrics() {
        if (rpcMetrics == null) {
            rpcMetrics = new MetricRegistry();
        }
        return rpcMetrics;
    }

    public void setRpcMetrics(MetricRegistry metricRegistry) {
        this.rpcMetrics = metricRegistry;
    }

    public MetricRegistry getSinkMetrics() {

        if (sinkMetrics == null) {
            sinkMetrics = new MetricRegistry();
        }
        return sinkMetrics;
    }

    public void setSinkMetrics(MetricRegistry sinkMetrics) {
        this.sinkMetrics = sinkMetrics;
    }

    public TracerRegistry getTracerRegistry() {
        return tracerRegistry;
    }

    public void setTracerRegistry(TracerRegistry tracerRegistry) {
        this.tracerRegistry = tracerRegistry;
    }

    public Tracer getTracer() {
        if (tracerRegistry != null) {
            return tracerRegistry.getTracer();
        }
        return GlobalTracer.get();
    }

    public void shutdown() {
        closed.set(true);
        rpcTimeoutQueue.clear();
        rpcHandlerByLocation.clear();
        rpcHandlerByMinionId.clear();
        rpcHandlerIteratorMap.clear();
        rpcResponseMap.clear();
        sinkModulesById.clear();
        if (rpcMetricsReporter != null) {
            rpcMetricsReporter.close();
        }
        if (sinkMetricsReporter != null) {
            sinkMetricsReporter.close();
        }
        grpcIpcServer.stopServer();
        rpcTimeoutExecutor.shutdownNow();
        responseHandlerExecutor.shutdownNow();
        LOG.info("OpenNMS gRPC server stopped");
    }

    @VisibleForTesting
    public Multimap<String, StreamObserver<RpcRequestProto>> getRpcHandlerByLocation() {
        return rpcHandlerByLocation;
    }

    private class OpennmsIpcService extends OpenNMSIpcGrpc.OpenNMSIpcImplBase {

        @Override
        public StreamObserver<RpcResponseProto> rpcStreaming(
                StreamObserver<RpcRequestProto> responseObserver) {

            return new StreamObserver<RpcResponseProto>() {

                @Override
                public void onNext(RpcResponseProto rpcResponseProto) {
                    // Register client when message is metadata.
                    if (isHeaders(rpcResponseProto)) {
                        addRpcHandler(rpcResponseProto.getLocation(), rpcResponseProto.getSystemId(), responseObserver);
                    } else {
                        responseHandlerExecutor.execute(() -> handleResponse(rpcResponseProto));
                    }
                }

                @Override
                public void onError(Throwable throwable) {
                    LOG.error("Error in rpc streaming", throwable);
                }

                @Override
                public void onCompleted() {
                    LOG.info("Minion RPC handler closed");
                    removeRpcHandler(responseObserver);
                }
            };
        }

        @Override
        public io.grpc.stub.StreamObserver<SinkMessage> sinkStreaming(
                io.grpc.stub.StreamObserver<Empty> responseObserver) {


            return new StreamObserver<SinkMessage>() {

                @Override
                public void onNext(SinkMessage sinkMessage) {

                    if (!Strings.isNullOrEmpty(sinkMessage.getModuleId())) {
                        ExecutorService sinkModuleExecutor = sinkConsumersByModuleId.get(sinkMessage.getModuleId());
                        if(sinkModuleExecutor != null) {
                            sinkModuleExecutor.execute(() -> dispatchSinkMessage(sinkMessage));
                        }
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

    private void dispatchSinkMessage(SinkMessage sinkMessage) {
        SinkModule<?, Message> sinkModule = sinkModulesById.get(sinkMessage.getModuleId());
        if (sinkModule != null && sinkMessage.getContent() != null) {
            Message message = sinkModule.unmarshal(sinkMessage.getContent().toByteArray());

            MessageConsumerManager.updateMessageSize(getSinkMetrics(), sinkMessage.getLocation(),
                    sinkMessage.getModuleId(), sinkMessage.getSerializedSize());
            Timer dispatchTime = MessageConsumerManager.getDispatchTimerMetric(getSinkMetrics(),
                    sinkMessage.getLocation(), sinkMessage.getModuleId());

            Tracer.SpanBuilder spanBuilder = buildSpanFromSinkMessage(sinkMessage);

            try (Scope scope = spanBuilder.startActive(true);
                 Timer.Context context = dispatchTime.time()) {
                scope.span().setTag(TracerConstants.TAG_MESSAGE_SIZE, sinkMessage.getSerializedSize());
                scope.span().setTag(TracerConstants.TAG_THREAD, Thread.currentThread().getName());
                dispatch(sinkModule, message);
            }
        }
    }

    private Tracer.SpanBuilder buildSpanFromSinkMessage(SinkMessage sinkMessage) {

        Tracer tracer = getTracer();
        Tracer.SpanBuilder spanBuilder;
        Map<String, String> tracingInfoMap = new HashMap<>();
        sinkMessage.getTracingInfoMap().forEach(tracingInfoMap::put);
        SpanContext context = tracer.extract(Format.Builtin.TEXT_MAP, new TextMapExtractAdapter(tracingInfoMap));
        if (context != null) {
            // Span on consumer side will follow the span from producer (minion).
            spanBuilder = tracer.buildSpan(sinkMessage.getModuleId()).addReference(References.FOLLOWS_FROM, context);
        } else {
            spanBuilder = tracer.buildSpan(sinkMessage.getModuleId());
        }
        return spanBuilder;
    }

    private class RpcResponseHandlerImpl<S extends RpcRequest, T extends RpcResponse> implements RpcResponseHandler {

        private final CompletableFuture<T> responseFuture;
        private final RpcModule<S, T> rpcModule;
        private final String rpcId;
        private final String location;
        private final long expirationTime;
        private final Map<String, String> loggingContext;
        private boolean isProcessed = false;
        private final Long requestCreationTime;
        private Span span;

        private RpcResponseHandlerImpl(CompletableFuture<T> responseFuture, RpcModule<S, T> rpcModule, String rpcId,
                                       String location, long timeout, Span span, Map<String, String> loggingContext) {
            this.responseFuture = responseFuture;
            this.rpcModule = rpcModule;
            this.rpcId = rpcId;
            this.location = location;
            this.expirationTime = timeout;
            this.loggingContext = loggingContext;
            this.span = span;
            this.requestCreationTime = System.currentTimeMillis();
        }

        @Override
        public void sendResponse(String message) {

            try (Logging.MDCCloseable mdc = Logging.withContextMapCloseable(loggingContext)) {
                if (message != null) {
                    T response = rpcModule.unmarshalResponse(message);
                    if (response.getErrorMessage() != null) {
                        span.log(response.getErrorMessage());
                        RpcClientFactory.markFailed(getRpcMetrics(), this.location, rpcModule.getId());
                        responseFuture.completeExceptionally(new RemoteExecutionException(response.getErrorMessage()));
                    } else {
                        responseFuture.complete(response);
                    }
                    isProcessed = true;
                    RpcClientFactory.updateResponseSize(getRpcMetrics(), this.location, rpcModule.getId(), message.getBytes().length);
                } else {
                    span.setTag(TAG_TIMEOUT, "true");
                    RpcClientFactory.markFailed(getRpcMetrics(), this.location, rpcModule.getId());
                    responseFuture.completeExceptionally(new RequestTimedOutException(new TimeoutException()));
                }
                RpcClientFactory.updateDuration(getRpcMetrics(), this.location, rpcModule.getId(), System.currentTimeMillis() - requestCreationTime);
                rpcResponseMap.remove(rpcId);
                span.finish();
            } catch (Throwable e) {
                LOG.error("Error while processing RPC response {}", message, e);
            }
            if (isProcessed) {
                LOG.debug("RPC Response from module: {} handled successfully for RpcId:{}.", rpcId, rpcModule.getId());
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
