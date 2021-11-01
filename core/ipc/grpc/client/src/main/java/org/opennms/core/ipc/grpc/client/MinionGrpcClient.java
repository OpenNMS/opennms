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

package org.opennms.core.ipc.grpc.client;

import static org.opennms.core.ipc.grpc.client.GrpcClientConstants.CLIENT_CERTIFICATE_FILE_PATH;
import static org.opennms.core.ipc.grpc.client.GrpcClientConstants.CLIENT_PRIVATE_KEY_FILE_PATH;
import static org.opennms.core.ipc.grpc.client.GrpcClientConstants.DEFAULT_GRPC_HOST;
import static org.opennms.core.ipc.grpc.client.GrpcClientConstants.DEFAULT_GRPC_PORT;
import static org.opennms.core.ipc.grpc.client.GrpcClientConstants.DEFAULT_MESSAGE_SIZE;
import static org.opennms.core.ipc.grpc.client.GrpcClientConstants.GRPC_CLIENT_PID;
import static org.opennms.core.ipc.grpc.client.GrpcClientConstants.GRPC_HOST;
import static org.opennms.core.ipc.grpc.client.GrpcClientConstants.GRPC_MAX_INBOUND_SIZE;
import static org.opennms.core.ipc.grpc.client.GrpcClientConstants.GRPC_PORT;
import static org.opennms.core.ipc.grpc.client.GrpcClientConstants.TLS_ENABLED;
import static org.opennms.core.ipc.grpc.client.GrpcClientConstants.TRUST_CERTIFICATE_FILE_PATH;
import static org.opennms.core.ipc.sink.api.Message.SINK_METRIC_PRODUCER_DOMAIN;
import static org.opennms.core.ipc.sink.api.SinkModule.HEARTBEAT_MODULE_ID;
import static org.opennms.core.rpc.api.RpcModule.MINION_HEADERS_MODULE;
import static org.opennms.core.tracing.api.TracerConstants.TAG_LOCATION;
import static org.opennms.core.tracing.api.TracerConstants.TAG_RPC_FAILED;
import static org.opennms.core.tracing.api.TracerConstants.TAG_SYSTEM_ID;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLException;

import org.opennms.core.ipc.grpc.common.ConfigUtils;
import org.opennms.core.ipc.grpc.common.OpenNMSIpcGrpc;
import org.opennms.core.ipc.grpc.common.RpcRequestProto;
import org.opennms.core.ipc.grpc.common.RpcResponseProto;
import org.opennms.core.ipc.grpc.common.SinkMessage;
import org.opennms.core.ipc.sink.api.Message;
import org.opennms.core.ipc.sink.api.MessageConsumerManager;
import org.opennms.core.ipc.sink.api.SinkModule;
import org.opennms.core.ipc.sink.common.AbstractMessageDispatcherFactory;
import org.opennms.core.logging.Logging;
import org.opennms.core.rpc.api.RpcModule;
import org.opennms.core.rpc.api.RpcRequest;
import org.opennms.core.rpc.api.RpcResponse;
import org.opennms.core.tracing.api.TracerConstants;
import org.opennms.core.tracing.api.TracerRegistry;
import org.opennms.core.tracing.util.TracingInfoCarrier;
import org.opennms.core.utils.PropertiesUtils;
import org.opennms.distributed.core.api.MinionIdentity;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.ConfigurationAdmin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Strings;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.google.protobuf.ByteString;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.shaded.io.grpc.netty.NegotiationType;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder;
import io.grpc.stub.StreamObserver;
import io.opentracing.Span;
import io.opentracing.SpanContext;
import io.opentracing.Tracer;
import io.opentracing.propagation.Format;
import io.opentracing.propagation.TextMapExtractAdapter;
import io.opentracing.util.GlobalTracer;
/**
 * Minion GRPC client runs both RPC/Sink together.
 * gRPC runs in a typical web server/client mode, so gRPC client runs on each minion and gRPC server runs on OpenNMS.
 * Minion GRPC Client tries to get two stubs/streams (RPC/Sink) from OpenNMS server when it is in active state.
 * It also initializes RPC observer/handler to receive requests from OpenNMS.
 * <p>
 * RPC : RPC runs in bi-directional streaming mode. Minion gets each request and it handles each request in a separate
 * thread. Once the request is executed, the response sender call is synchronized as writing to observer is not thread-safe.
 * Minion also sends it's headers (SystemId/location) to OpenNMS whenever the stub is initialized.
 * <p>
 * Sink: Sink runs in uni-directional streaming mode. If the sink module is async and OpenNMS Server is not active, the
 * messages are buffered and blocked till minion is able to connect to OpenNMS.
 */
public class MinionGrpcClient extends AbstractMessageDispatcherFactory<String> {

    private static final Logger LOG = LoggerFactory.getLogger(MinionGrpcClient.class);
    private static final long SINK_BLOCKING_TIMEOUT = 1000;
    private static final int SINK_BLOCKING_THREAD_POOL_SIZE = 100;
    private ManagedChannel channel;
    private OpenNMSIpcGrpc.OpenNMSIpcStub asyncStub;
    private Properties properties;
    private BundleContext bundleContext;
    private MinionIdentity minionIdentity;
    private ConfigurationAdmin configAdmin;
    private StreamObserver<RpcResponseProto> rpcStream;
    private StreamObserver<SinkMessage> sinkStream;
    private ConnectivityState currentChannelState;
    private MetricRegistry metrics;
    private TracerRegistry tracerRegistry;
    private final ThreadFactory requestHandlerThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("rpc-request-handler-%d")
            .build();
    private final ThreadFactory blockingSinkMessageThreadFactory = new ThreadFactoryBuilder()
            .setNameFormat("blocking-sink-message-%d")
            .build();
    // Each request is handled in a new thread which unmarshals and executes the request.
    private final ExecutorService requestHandlerExecutor = Executors.newCachedThreadPool(requestHandlerThreadFactory);
    // Maintain the map of RPC modules and their ID.
    private final Map<String, RpcModule<RpcRequest, RpcResponse>> registerdModules = new ConcurrentHashMap<>();
    // This maintains a blocking thread for each dispatch module when OpenNMS is not in active state.
    private final ScheduledExecutorService blockingSinkMessageScheduler = Executors.newScheduledThreadPool(SINK_BLOCKING_THREAD_POOL_SIZE,
            blockingSinkMessageThreadFactory);


    public MinionGrpcClient(MinionIdentity identity, ConfigurationAdmin configAdmin) {
        this.minionIdentity = identity;
        this.configAdmin = configAdmin;
    }


    public void start() throws IOException {
        properties = ConfigUtils.getPropertiesFromConfig(configAdmin, GRPC_CLIENT_PID);
        String host = PropertiesUtils.getProperty(properties, GRPC_HOST, DEFAULT_GRPC_HOST);
        int port = PropertiesUtils.getProperty(properties, GRPC_PORT, DEFAULT_GRPC_PORT);
        boolean tlsEnabled = PropertiesUtils.getProperty(properties, TLS_ENABLED, false);
        int maxInboundMessageSize = PropertiesUtils.getProperty(properties, GRPC_MAX_INBOUND_SIZE, DEFAULT_MESSAGE_SIZE);

        NettyChannelBuilder channelBuilder = NettyChannelBuilder.forAddress(host, port)
                .keepAliveWithoutCalls(true)
                .maxInboundMessageSize(maxInboundMessageSize);

        if (tlsEnabled) {
            channel = channelBuilder
                    .negotiationType(NegotiationType.TLS)
                    .sslContext(buildSslContext().build())
                    .build();
            LOG.info("TLS enabled for gRPC");
        } else {
            channel = channelBuilder.usePlaintext().build();
        }

        asyncStub = OpenNMSIpcGrpc.newStub(channel);
        initializeRpcStub();
        initializeSinkStub();
        if (tracerRegistry != null) {
            tracerRegistry.init(minionIdentity.getLocation() + "@" + minionIdentity.getId());
        }
        LOG.info("Minion at location {} with systemId {} started", minionIdentity.getLocation(), minionIdentity.getId());

    }

    private SslContextBuilder buildSslContext() throws SSLException {
        SslContextBuilder builder = GrpcSslContexts.forClient();
        String clientCertChainFilePath = properties.getProperty(CLIENT_CERTIFICATE_FILE_PATH);
        String clientPrivateKeyFilePath = properties.getProperty(CLIENT_PRIVATE_KEY_FILE_PATH);
        String trustCertCollectionFilePath = properties.getProperty(TRUST_CERTIFICATE_FILE_PATH);

        if (trustCertCollectionFilePath != null) {
            builder.trustManager(new File(trustCertCollectionFilePath));
        }
        if (clientCertChainFilePath != null && clientPrivateKeyFilePath != null) {
            builder.keyManager(new File(clientCertChainFilePath), new File(clientPrivateKeyFilePath));
        }
        return builder;
    }

    private void initializeRpcStub() {
        if (getChannelState().equals(ConnectivityState.READY)) {
            rpcStream = asyncStub.rpcStreaming(new RpcMessageHandler());
            // Need to send minion headers to gRPC server in order to register.
            sendMinionHeaders();
            LOG.info("Initialized RPC stream");
        } else {
            LOG.warn("gRPC IPC server is not in ready state");
        }
    }

    private void initializeSinkStub() {
        if (getChannelState().equals(ConnectivityState.READY)) {
            sinkStream = asyncStub.sinkStreaming(new EmptyMessageReceiver());
            LOG.info("Initialized Sink stream");
        } else {
            LOG.warn("gRPC IPC server is not in ready state");
        }
    }


    @SuppressWarnings({"rawtypes", "unchecked"})
    public void bind(RpcModule module) throws Exception {
        if (module != null) {
            final RpcModule<RpcRequest, RpcResponse> rpcModule = (RpcModule<RpcRequest, RpcResponse>) module;
            if (registerdModules.containsKey(rpcModule.getId())) {
                LOG.warn(" {} module is already registered", rpcModule.getId());
            } else {
                registerdModules.put(rpcModule.getId(), rpcModule);
                LOG.info("Registered module {} with gRPC IPC client", rpcModule.getId());
            }
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void unbind(RpcModule module) throws Exception {
        if (module != null) {
            final RpcModule<RpcRequest, RpcResponse> rpcModule = (RpcModule<RpcRequest, RpcResponse>) module;
            registerdModules.remove(rpcModule.getId());
            LOG.info("Removing module {} from gRPC IPC client.", rpcModule.getId());
        }
    }

    private boolean hasChangedToReadyState() {
        ConnectivityState prevState = currentChannelState;
        return !prevState.equals(ConnectivityState.READY) && getChannelState().equals(ConnectivityState.READY);
    }

    public void shutdown() {
        requestHandlerExecutor.shutdown();
        blockingSinkMessageScheduler.shutdown();
        registerdModules.clear();
        if (rpcStream != null) {
            rpcStream.onCompleted();
        }
        channel.shutdown();
        LOG.info("Minion at location {} with systemId {} stopped", minionIdentity.getLocation(), minionIdentity.getId());
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }


    @Override
    public String getMetricDomain() {
        return SINK_METRIC_PRODUCER_DOMAIN;
    }

    @Override
    public BundleContext getBundleContext() {
        return bundleContext;
    }

    @Override
    public Tracer getTracer() {
        if (tracerRegistry != null) {
            return tracerRegistry.getTracer();
        }
        return GlobalTracer.get();
    }

    @Override
    public MetricRegistry getMetrics() {
        if (metrics == null) {
            return new MetricRegistry();
        }
        return metrics;
    }

    public void setMetrics(MetricRegistry metrics) {
        this.metrics = metrics;
    }

    public TracerRegistry getTracerRegistry() {
        return tracerRegistry;
    }

    public void setTracerRegistry(TracerRegistry tracerRegistry) {
        this.tracerRegistry = tracerRegistry;
    }

    ConnectivityState getChannelState() {
        return currentChannelState = channel.getState(true);
    }


    @Override
    public <S extends Message, T extends Message> void dispatch(SinkModule<S, T> module, String metadata, T message) {

        try (Logging.MDCCloseable mdc = Logging.withPrefixCloseable(MessageConsumerManager.LOG_PREFIX)) {
            byte[] sinkMessageContent = module.marshal(message);
            String messageId = UUID.randomUUID().toString();
            SinkMessage.Builder sinkMessageBuilder = SinkMessage.newBuilder()
                    .setMessageId(messageId)
                    .setLocation(minionIdentity.getLocation())
                    .setModuleId(module.getId())
                    .setContent(ByteString.copyFrom(sinkMessageContent));

            if (module.getId().equals(HEARTBEAT_MODULE_ID)) {
                if (rpcStream == null || sinkStream == null || hasChangedToReadyState()) {
                    initializeSinkStub();
                    initializeRpcStub();
                }
            }
            setTagsForSink(sinkMessageBuilder);
            // If module has asyncpolicy, keep attempting to send message.
            if (module.getAsyncPolicy() != null) {
                sendBlockingSinkMessage(sinkMessageBuilder.build());
            } else {
                sendSinkMessage(sinkMessageBuilder.build());
            }
        }
    }

    private void sendBlockingSinkMessage(SinkMessage sinkMessage) {
        boolean succeeded = sendSinkMessage(sinkMessage);
        if (succeeded) {
            return;
        }
        //Recursively try to send sink message until it succeeds.
        scheduleSinkMessageAfterDelay(sinkMessage);
    }

    private boolean scheduleSinkMessageAfterDelay(SinkMessage sinkMessage) {
        ScheduledFuture<Boolean> future = blockingSinkMessageScheduler.schedule(
                () -> sendSinkMessage(sinkMessage), SINK_BLOCKING_TIMEOUT, TimeUnit.MILLISECONDS);
        try {
            boolean succeeded = future.get();
            if (succeeded) {
                return true;
            }
        } catch (InterruptedException | ExecutionException e) {
            LOG.error("Error while attempting to send sink message with id {} from module {} to gRPC IPC server",
                    sinkMessage.getMessageId(), sinkMessage.getModuleId(), e);
        }
        return scheduleSinkMessageAfterDelay(sinkMessage);
    }


    private synchronized boolean sendSinkMessage(SinkMessage sinkMessage) {
        if (getChannelState().equals(ConnectivityState.READY)) {
            if (sinkStream != null) {
                try {
                    sinkStream.onNext(sinkMessage);
                    return true;
                } catch (Throwable e) {
                    LOG.error("Exception while sending sinkMessage to gRPC IPC server", e);
                }
            }
        } else {
            LOG.info("gRPC IPC server is not in ready state");
        }
        return false;
    }


    private void sendMinionHeaders() {
        RpcResponseProto rpcHeader = RpcResponseProto.newBuilder()
                .setLocation(minionIdentity.getLocation())
                .setSystemId(minionIdentity.getId())
                .setModuleId(MINION_HEADERS_MODULE)
                .setRpcId(minionIdentity.getId())
                .build();
        sendRpcResponse(rpcHeader);
        LOG.info("Sending Minion Headers from SystemId {} to gRPC server", minionIdentity.getId());
    }

    private void processRpcRequest(RpcRequestProto requestProto) {
        long currentTime = requestProto.getExpirationTime();
        if (requestProto.getExpirationTime() < currentTime) {
            LOG.debug("ttl already expired for the request id = {}, won't process.", requestProto.getRpcId());
            return;
        }
        String moduleId = requestProto.getModuleId();
        if (Strings.isNullOrEmpty(moduleId)) {
            return;
        }
        LOG.debug("Received RPC request with RpcID:{} for module {}", requestProto.getRpcId(), requestProto.getModuleId());
        RpcModule<RpcRequest, RpcResponse> rpcModule = registerdModules.get(moduleId);
        if (rpcModule == null) {
            return;
        }
        //Build child span from rpcMessage and start minion span.
        Tracer.SpanBuilder spanBuilder = buildSpanFromRpcMessage(requestProto);
        Span minionSpan = spanBuilder.start();
        setTagsForRpc(requestProto, minionSpan);

        RpcRequest rpcRequest = rpcModule.unmarshalRequest(requestProto.getRpcContent().toStringUtf8());
        CompletableFuture<RpcResponse> future = rpcModule.execute(rpcRequest);
        future.whenComplete((res, ex) -> {
            final RpcResponse rpcResponse;
            if (ex != null) {
                // An exception occurred, store the exception in a new response
                LOG.warn("An error occured while executing a call in {}.", rpcModule.getId(), ex);
                rpcResponse = rpcModule.createResponseWithException(ex);
                minionSpan.log(ex.getMessage());
                minionSpan.setTag(TAG_RPC_FAILED, Boolean.TRUE.toString());
            } else {
                // No exception occurred, use the given response
                rpcResponse = res;
            }
            minionSpan.finish();
            // Construct response using the same rpcId;
            String responseAsString = rpcModule.marshalResponse(rpcResponse);
            RpcResponseProto responseProto = RpcResponseProto.newBuilder()
                    .setRpcId(requestProto.getRpcId())
                    .setSystemId(minionIdentity.getId())
                    .setLocation(requestProto.getLocation())
                    .setModuleId(requestProto.getModuleId())
                    .setRpcContent(ByteString.copyFrom(responseAsString, StandardCharsets.UTF_8))
                    .build();
            if (getChannelState().equals(ConnectivityState.READY)) {
                try {
                    sendRpcResponse(responseProto);
                    LOG.debug("Request with RpcId:{} for module {} handled successfully, and response was sent",
                            responseProto.getRpcId(), responseProto.getModuleId());
                } catch (Throwable e) {
                    LOG.error("Error while sending RPC response {}", responseProto, e);
                }
            } else {
                LOG.warn("gRPC IPC server is not in ready state");
            }
        });
    }

    private Tracer.SpanBuilder buildSpanFromRpcMessage(RpcRequestProto requestProto) {
        // Initializer tracer and extract parent tracer context from TracingInfo
        final Tracer tracer = getTracer();
        Tracer.SpanBuilder spanBuilder;
        Map<String, String> tracingInfoMap = new HashMap<>();
        requestProto.getTracingInfoMap().forEach(tracingInfoMap::put);
        SpanContext context = tracer.extract(Format.Builtin.TEXT_MAP, new TextMapExtractAdapter(tracingInfoMap));
        if (context != null) {
            spanBuilder = tracer.buildSpan(requestProto.getModuleId()).asChildOf(context);
        } else {
            spanBuilder = tracer.buildSpan(requestProto.getModuleId());
        }
        return spanBuilder;
    }

    private void setTagsForRpc(RpcRequestProto requestProto, Span minionSpan) {
        // Retrieve custom tags from rpcMessage and add them as tags.
        requestProto.getTracingInfoMap().forEach(minionSpan::setTag);
        // Set tags for minion span
        minionSpan.setTag(TAG_LOCATION, requestProto.getLocation());
        if (!Strings.isNullOrEmpty(requestProto.getSystemId())) {
            minionSpan.setTag(TAG_SYSTEM_ID, requestProto.getSystemId());
        }
    }

    private void setTagsForSink(SinkMessage.Builder sinkMessageBuilder) {
        // Add tracing info
        final Tracer tracer = getTracer();
        if (tracer.activeSpan() != null) {
            TracingInfoCarrier tracingInfoCarrier = new TracingInfoCarrier();
            tracer.inject(tracer.activeSpan().context(), Format.Builtin.TEXT_MAP, tracingInfoCarrier);
            tracer.activeSpan().setTag(TracerConstants.TAG_LOCATION, minionIdentity.getLocation());
            tracer.activeSpan().setTag(TracerConstants.TAG_THREAD, Thread.currentThread().getName());
            tracingInfoCarrier.getTracingInfoMap().forEach(sinkMessageBuilder::putTracingInfo);
        }
    }

    private synchronized void sendRpcResponse(RpcResponseProto rpcResponseProto) {
        if (rpcStream != null) {
            try {
                rpcStream.onNext(rpcResponseProto);
            } catch (Exception e) {
                LOG.error("Exception while sending RPC response : {}", rpcResponseProto);
            }
        } else {
            throw new RuntimeException("RPC response handler not found");
        }
    }

    private class RpcMessageHandler implements StreamObserver<RpcRequestProto> {

        @Override
        public void onNext(RpcRequestProto rpcRequestProto) {

            try {
                // Run processing of RPC request in a different thread.
                requestHandlerExecutor.execute(() -> processRpcRequest(rpcRequestProto));
            } catch (Throwable e) {
                LOG.error("Error while processing the RPC Request {}", rpcRequestProto, e);
            }
        }

        @Override
        public void onError(Throwable throwable) {
            LOG.error("Error in RPC streaming", throwable);
            rpcStream = null;
        }

        @Override
        public void onCompleted() {
            LOG.error("Closing RPC message handler");
            rpcStream = null;
        }

    }

}
